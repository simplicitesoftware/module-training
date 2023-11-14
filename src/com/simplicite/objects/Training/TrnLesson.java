package com.simplicite.objects.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.tools.HTMLTool;
import com.simplicite.commons.Training.*;

import org.json.JSONObject;

/**
 * Business object TrnLesson
 */
public class TrnLesson extends TrnObject {
	private static final long serialVersionUID = 1L;
	private TrnLsnTranslate lsnTranslate;

	@Override
	public String getUserKeyLabel(String[] row) {
		return getFieldValue("trnLsnFrontPath", row);
	}

	@Override
	public void postLoad() {
		super.postLoad();
		if (getGrant().hasResponsibility("TRN_READ")) {
			setDefaultSearchSpec("t.trn_lsn_publish='1'");
		}
	}

	@Override
	public void initCreate() {
		setFieldValue("trnLsnOrder", getNextOrder());
	}

	private int getNextOrder() {
		String ref = " is null";
		if (getParentObject() != null && "TrnCategory".equals(getParentObject().getName())
				&& !Tool.isEmpty(getGrant().getParameter("LAST_VISITED_CATEGORY_ID"))) {
			ref = "=" + Tool.toSQL(getGrant().getParameter("LAST_VISITED_CATEGORY_ID"));
			AppLog.info("=====" + getParentObject().getRowId(), Grant.getSystemAdmin());
		}

		String lastOrder = getGrant().simpleQuery(
				"SELECT trn_lsn_order FROM trn_lesson WHERE trn_lsn_cat_id" + ref + " ORDER BY trn_lsn_order DESC");
		return 10 + Tool.parseInt(lastOrder, 0);
	}

	@Override
	public List<String> postValidate() {
		if (!isSyncInstance() && !"updateChildren_TrnLesson".equals(getInstanceName())) {
			setFieldValue("trnLsnPath", getFieldValue("trnLsnCatId.trnCatPath") + "/" + getFieldValue("trnLsnCode"));
		}
		setFieldValue("trnLsnFrontPath", TrnTools.path2Front(getFieldValue("trnLsnPath")));
		return Collections.emptyList();
	}

	@Override
	public String postCreate() {
		try {
            // quick fix, so sync instance translations are not indexed multiple times for nothing
            // indexation is done once all translations have been creating, see upsertLessonAndContent in sync
			ObjectDB tsl = getGrant().getObject("sync_TrnLsnTranslate", "TrnLsnTranslate");
			synchronized (tsl.getLock()) {
				tsl.resetValues();
				tsl.setFieldValue("trnLtrLang", "ANY");
				tsl.setFieldValue("trnLtrTitle", getFieldValue("trnLsnCode"));
				tsl.setFieldValue("trnLtrLsnId", getRowId());
				tsl.getTool().validateAndCreate();
			}
		} catch (Exception e) {
			AppLog.error(e, getGrant());
		}
		return null;
	}

	@Override
	public String preDelete() {
		try {
			if (isLessonPublishedRecursive()) {
				TrnEsIndexer.deleteLessonIndex(this);
			}
		} catch (Exception e) {
			AppLog.error("Error removing index doc of lesson " + getFieldValue("trnLsnCode") + " : " + e.getMessage(),
					e,
					getGrant());
		}
		return null;
	}

	public void index() {
		try {
			boolean res = isLessonPublishedRecursive();
			if (res) {
				TrnEsIndexer.indexLesson(this);
			}
		} catch (TrnConfigException e) {
			AppLog.error(getClass(), "index", e.getMessage(), e, getGrant());
		} catch (Exception e) {
			AppLog.error("Error indexing lessson", e, getGrant());
		}
	}

	// check if lesson is published and if parent categories are published
	public boolean isLessonPublishedRecursive() {
		try {
			String published = getFieldValue("trnLsnPublish");
			if ("0".equals(published)) {
				return false;
			}
			TrnCategory cat = TrnCategory.getCategoryObject(getGrant(), getFieldValue("trnLsnCatId"));
			return cat.isCategoryPublishedRecursive();
		} catch (Exception e) {
			AppLog.warning(e, getGrant());
			return false;
		}
	}

	// set lang as null for index json
	public JSONObject getLessonJSON(String lang, boolean includeHtml) throws Exception {
		lsnTranslate = (TrnLsnTranslate) getGrant().getObject("tree_TrnTrnLsnTranslate", "TrnLsnTranslate");
		if (lang == null) {
			return getLessonForIndex();
		} else {
			return getLessonForFront(lang, includeHtml);
		}
	}

	public JSONObject getLessonForFront(String lang, boolean includeHtml) throws Exception {
		JSONObject json = initLessonJson();
		fillJsonFront(json, lang, includeHtml);
		return json;
	}

	public JSONObject getLessonForIndex() throws Exception {
		JSONObject json = initLessonJson();
		fillJsonIndex(json);
		return json;
	}

	private JSONObject initLessonJson() throws Exception {
		JSONObject json = (new JSONObject())
				.put("row_id", getRowId())
				.put("type", "lesson")
				.put("order", getFieldValue("trnLsnOrder"))
				.put("path", getFieldValue("trnLsnFrontPath"))
				.put("viz", getFieldValue("trnLsnVisualization"))
				.put("is_category", false);

		TrnCategory cat = (TrnCategory) getGrant().getTmpObject("TrnCategory");
		json.put("catPath", cat.getCatFrontPath(getFieldValue("trnLsnCatId")));

		return json;
	}

	private void fillJsonIndex(JSONObject json) throws Exception {
		synchronized (lsnTranslate) {
			lsnTranslate.resetFilters();
			lsnTranslate.setFieldFilter("trnLtrLsnId", getRowId());
			for (String lang : TrnTools.getLangs(getGrant(), true)) {
				// fill json with asked lang
				String attributeLang = "_" + lang.toLowerCase();
				lsnTranslate.setFieldFilter("trnLtrLang", lang);
				if (lsnTranslate.getCount() == 1) {
					lsnTranslate.setValues((lsnTranslate.search()).get(0));
					ObjectField f;
					f = lsnTranslate.getField("trnLtrTitle");
					if (!f.getValue().equals("default")) {
						json.put("title" + attributeLang, f.getValue());

						f = lsnTranslate.getField("trnLtrRawContent");
						String htmlContent = f.getValue();
						// if LINEAR, then change content images link
						json.put("raw_content" + attributeLang, HTMLTool.toSafeHTML(htmlContent));
					}
				}
			}
		}
	}

	private void fillJsonFront(JSONObject json, String lang, boolean includeHtml) throws Exception {
		synchronized (lsnTranslate) {
			lsnTranslate.resetFilters();
			lsnTranslate.setFieldFilter("trnLtrLsnId", getRowId());
			lsnTranslate.setFieldFilter("trnLtrLang", lang);
			if (lsnTranslate.getCount() == 1) {
				lsnTranslate.setValues((lsnTranslate.search()).get(0));

				ObjectField f;
				f = lsnTranslate.getField("trnLtrTitle");
				if (!f.isEmpty() && !json.has("title")) {
					json.put("title", f.getValue());
				}

				f = lsnTranslate.getField("trnLtrVideo");
				if (!f.isEmpty() && !json.has("video")) {
					json.put("video", f.getValue());
					json.put("ltr_id", lsnTranslate.getRowId());
				}

				f = lsnTranslate.getField("trnLtrHtmlContent");
				if (!f.isEmpty() && !json.has("html") && includeHtml) {
					String htmlContent = f.getValue();
					json.put("html", htmlContent);
				}
			}
			// if lang is not any and there is no content found from the language, try to
			// add content from any
			if (!lang.equals("ANY") && (!json.has("title") || !json.has("video") || !json.has("html"))) {
				fillJsonFront(json, "ANY", includeHtml);
			}
		}
	}
}
