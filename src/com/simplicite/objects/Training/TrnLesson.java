package com.simplicite.objects.Training;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.simplicite.util.*;
import com.simplicite.commons.Training.*;

import org.json.JSONObject;
/**
 * Business object TrnLesson
 */
public class TrnLesson extends TrnObject {
	private static final long serialVersionUID = 1L;
	private static final Pattern PATTERN_LINEAR_PICS = Pattern.compile("src\\s*=\\s*\\\"(.+?)\\\"");
    private TrnLsnTranslate lsnTranslate;

	@Override
	public String getUserKeyLabel(String[] row) {
		return getFieldValue("trnLsnFrontPath", row);
	}

	@Override
	public void postLoad() {
		super.postLoad();
		if(getGrant().hasResponsibility("TRN_READ")){
			setDefaultSearchSpec("t.trn_lsn_publish='1'");
		}
	}

	@Override
	public void initCreate() {
		setFieldValue("trnLsnOrder", getNextOrder());
	}
	
	private int getNextOrder(){
		String ref = " is null";
		if(getParentObject()!=null && "TrnCategory".equals(getParentObject().getName()) && !Tool.isEmpty(getGrant().getParameter("LAST_VISITED_CATEGORY_ID"))){
			ref = "="+Tool.toSQL(getGrant().getParameter("LAST_VISITED_CATEGORY_ID"));
			AppLog.info("====="+getParentObject().getRowId(), Grant.getSystemAdmin());
		}
		
		String lastOrder = getGrant().simpleQuery("SELECT trn_lsn_order FROM trn_lesson WHERE trn_lsn_cat_id"+ref+" ORDER BY trn_lsn_order DESC");
		return 10 + Tool.parseInt(lastOrder, 0);
	}

	@Override
	public List<String> postValidate() {
		if(!isSyncInstance())
			setFieldValue("trnLsnPath", getPath());
		
		setFieldValue("trnLsnFrontPath", TrnTools.path2Front(getFieldValue("trnLsnPath")));
		
		return Collections.emptyList();
	}
	
	@Override
	public String postCreate() {
		try{
			ObjectDB tsl = getGrant().getTmpObject("TrnLsnTranslate");
			synchronized(tsl.getLock()){
				tsl.resetValues();
				tsl.setFieldValue("trnLtrLang", "ANY");
				tsl.setFieldValue("trnLtrTitle", getFieldValue("trnLsnCode"));
				tsl.setFieldValue("trnLtrLsnId", getRowId());
				tsl.getTool().validateAndCreate();
			}
		}
		catch(Exception e){
			AppLog.error(e, getGrant());
		}

		return null;
	}
	
	@Override
	public String postSave() {
		index();
		return null;
	}

	@Override
	public String preDelete() {
		try {
			TrnIndexer.deleteLessonIndex(this);
		} catch(Exception e) {
			AppLog.error("Error removing index doc of lesson " + getFieldValue("trnLsnCode") + " : " + e.getMessage(), e, getGrant());
		}
		return null;
	}
	
	public void index(){
        String published = getFieldValue("trnLsnPublish");
        if(published.equals("0")) return; 
		try{
			TrnIndexer.indexLesson(this);
		} catch(TrnConfigException e) {
            AppLog.error(getClass(), "index", e.getMessage(), e, getGrant());
        } catch(Exception e){
			AppLog.error("Error indexing lessson", e, getGrant());
		}
	}

	private String getPath(){
		return getFieldValue("trnLsnCatId.trnCatPath")+"/"+"LSN_"+getFieldValue("trnLsnOrder")+"_"+TrnTools.toSnake(getFieldValue("trnLsnCode"));
	}

	// set lang as null for index json
	public JSONObject getLessonJSON(String lang, boolean includeHtml) throws Exception {
		lsnTranslate = (TrnLsnTranslate) getGrant().getObject("tree_TrnTrnLsnTranslate", "TrnLsnTranslate");
		if(lang == null) return getLessonForIndex();
		else return getLessonForFront(lang, includeHtml);
	}
	
	public JSONObject getLessonForFront(String lang, boolean includeHtml) throws Exception{
		JSONObject json = initLessonJson();
		fillJsonFront(json, lang, includeHtml);
		return json;
	}
	
	public JSONObject getLessonForIndex() throws Exception{
		JSONObject json = initLessonJson();
		fillJsonIndex(json);
		return json;
	}
	
	private JSONObject initLessonJson() throws Exception {
		JSONObject json = (new JSONObject())
			.put("row_id", getRowId())
			.put("path", getFieldValue("trnLsnFrontPath"))
			.put("viz", getFieldValue("trnLsnVisualization"));

		TrnCategory cat = (TrnCategory) getGrant().getTmpObject("TrnCategory");
		json.put("catPath", cat.getCatFrontPath(getFieldValue("trnLsnCatId")));


		return json;
	}

	private void fillJsonIndex(JSONObject json) throws Exception {
		synchronized(lsnTranslate) {
			lsnTranslate.resetFilters();
			lsnTranslate.setFieldFilter("trnLtrLsnId", getRowId());
			for(String lang: TrnTools.getLangs(getGrant(), true)) {
				// fill json with asked lang
				String attributeLang = "_" + lang.toLowerCase();
				lsnTranslate.setFieldFilter("trnLtrLang", lang);
				if(lsnTranslate.getCount()==1){
					lsnTranslate.setValues((lsnTranslate.search()).get(0));
					ObjectField f;
					f = lsnTranslate.getField("trnLtrTitle");
					if(!f.getValue().equals("default")) {
						json.put("title"+attributeLang, f.getValue());

						f = lsnTranslate.getField("trnLtrRawContent");
						String htmlContent = f.getValue();
						// if LINEAR, then change content images link
						json.put("raw_content"+attributeLang, htmlContent);
					}
				}
			}
		}
	}

	private void fillJsonFront(JSONObject json, String lang, boolean includeHtml) throws Exception {
		synchronized(lsnTranslate){
			lsnTranslate.resetFilters();
			lsnTranslate.setFieldFilter("trnLtrLsnId", getRowId());
			lsnTranslate.setFieldFilter("trnLtrLang", lang);
			if(lsnTranslate.getCount()==1){
				lsnTranslate.setValues((lsnTranslate.search()).get(0));
				
				ObjectField f;
				f = lsnTranslate.getField("trnLtrTitle");
				if(!f.isEmpty() && !json.has("title")){
					json.put("title", f.getValue());
				}
				
				f = lsnTranslate.getField("trnLtrVideo");
				if(!f.isEmpty() && !json.has("video")){
					json.put("video", f.getValue());
					json.put("ltr_id", lsnTranslate.getRowId());
				}
				
				f = lsnTranslate.getField("trnLtrHtmlContent");
				if(!f.isEmpty() && !json.has("html") && includeHtml) {
					String htmlContent = f.getValue();
					// if LINEAR, then change content images link
					if(json.get("viz").equals("LINEAR")) {
						json.put("html", setLinearPictureContent(htmlContent));
					} else {
						json.put("html", htmlContent);
					}
				}
			}
			// if lang is not any and there is no content found from the language, try to add content from any
			if(!lang.equals("ANY") && (!json.has("title") || !json.has("video") || !json.has("html"))) {
				fillJsonFront(json, "ANY", includeHtml);
			}
		}
	}

	// When fetching a linear lesson, converts pictures old urls to current 
	private String setLinearPictureContent(String htmlContent) throws Exception {
        try {
            Matcher m = PATTERN_LINEAR_PICS.matcher(htmlContent);
            ObjectDB picObject = getGrant().getTmpObject("TrnPicture");
            picObject.resetFilters();
            picObject.setFieldFilter("trnPicLsnId", getRowId());
            List<String[]> pics = picObject.search();
            // rebuilding string with StringBuilder
            StringBuilder out = new StringBuilder();
            int nextIndex = 0;
            while(m.find()) {
                String imgPath = m.group(1);
                for(String[] pic : pics) {
                    // optimiser en cherchant les documents par doc name ?
                    // ou possible de fetch la liste des docs correspondants aux images de la leçon ?
                    picObject.setValues(pic);
                    String docId = picObject.getFieldValue("trnPicImage");
                    DocumentDB doc = DocumentDB.getDocument(docId, getGrant());
                    String frontUrl = getGrant().getContextURL() + doc.getURL("").replace("/ui", "");;
                    String docName = doc.getName();
                    if(imgPath.contains(docName)) {
                        // append the content of html from the nextIndex (beginning of string to copy) to start index (end of string to copy)
                        // in a nutshell add all the htmlContent before the match (match not included)
                        int startIndex = m.start();
                        out.append(htmlContent, nextIndex, startIndex)
                        // then append the new Url
                        .append("src=\""+frontUrl+"\"");
                        nextIndex = m.end();
                        break;
                    }
                }
            }
            if(nextIndex < htmlContent.length()) {
                out.append(htmlContent, nextIndex, htmlContent.length());
            }
            return out.toString();
        } catch(Exception e) {
            throw new Exception("An error occured while parsing linear picture content: "+e.getMessage());
        }
	}
}
