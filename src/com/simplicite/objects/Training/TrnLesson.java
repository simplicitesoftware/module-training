package com.simplicite.objects.Training;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.simplicite.util.*;
import com.simplicite.util.tools.HTTPTool;
import com.simplicite.util.tools.Parameters;
import com.simplicite.commons.Training.*;
import org.json.JSONObject;

/**
 * Business object TrnLesson
 */
public class TrnLesson extends TrnObject {
	private static final long serialVersionUID = 1L;
	private final Pattern PATTERN_LINEAR_PICS = Pattern.compile("src\\s*=\\s*\\\"(.+?)\\\"");

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
	public List<String> postValidate() {
		if(!isSyncInstance())
			setFieldValue("trnLsnPath", getPath());
		
		setFieldValue("trnLsnFrontPath", TrnTools.path2Front(getFieldValue("trnLsnPath")));
		
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
			AppLog.error("Error removing index doc of lesson " + getFieldValue("trnLsnCode"), e, getGrant());
		}
		return null;
	}
	
	public void index(){
		try{
			TrnIndexer.indexLesson(this);
		}
		catch(Exception e){
			AppLog.error("Error indexing lessson", e, getGrant());
		}
	}

	private String getPath(){
		return getFieldValue("trnLsnCatId.trnCatPath")+"/"+"LSN_"+getFieldValue("trnLsnOrder")+"_"+TrnTools.toSnake(getFieldValue("trnLsnCode"));
	}
	
	public JSONObject getLessonForFront(String lang, boolean includeHtml) throws Exception{
		return _getLessonAsJson(lang, includeHtml, false);
	}
	
	public JSONObject getLessonForIndex(String lang) throws Exception{
		return _getLessonAsJson(lang, false, true);
	}
	
	private JSONObject _getLessonAsJson(String lang, boolean includeHtml, boolean includeRaw) throws Exception{
		JSONObject json = (new JSONObject())
			.put("row_id", getRowId())
			.put("path", getFieldValue("trnLsnFrontPath"))
			.put("viz", getFieldValue("trnLsnVisualization"));
		
		TrnCategory cat = (TrnCategory) getGrant().getTmpObject("TrnCategory");
		json.put("catPath", cat.getCatFrontPath(getFieldValue("trnLsnCatId")));

		ObjectDB content = getGrant().getTmpObject("TrnLsnTranslate");
		synchronized(content){
			content.resetFilters();
			content.setFieldFilter("trnLtrLsnId", getRowId());
			
			// fill json with asked lang
			content.setFieldFilter("trnLtrLang", lang);
			fillJsonLesson(json, content, includeHtml, includeRaw);
			
			// fill empty fields with default lang
			content.setFieldFilter("trnLtrLang", "ANY");
			fillJsonLesson(json, content, includeHtml, includeRaw);
		}
		return json;
	}
	
	private void fillJsonLesson(JSONObject json, ObjectDB content, boolean includeHtml, boolean includeRaw){
		if(!json.has("title") || !json.has("video") || (includeHtml && !json.has("html")) || (includeRaw && !json.has("raw_content"))){
			if(content.getCount()==1){
				content.setValues((content.search()).get(0));
				
				ObjectField f;
				f = content.getField("trnLtrTitle");
				if(!f.isEmpty() && !json.has("title")){
					json.put("title", f.getValue());
				}
				
				f = content.getField("trnLtrVideo");
				if(!f.isEmpty() && !json.has("video")){
					json.put("video", f.getValue());
					json.put("ltr_id", content.getRowId());
				}
				
				f = content.getField("trnLtrHtmlContent");
				if(includeHtml && !f.isEmpty() && !json.has("html")) {
					String htmlContent = f.getValue();
					// if LINEAR, then change content images link
					if(json.get("viz").equals("LINEAR")) {
						json.put("html", setLinearPictureContent(htmlContent));
					} else {
						json.put("html", htmlContent);
					}
				}
				
				f = content.getField("trnLtrRawContent");
				if(includeRaw && !f.isEmpty() && !json.has("raw_content"))
					json.put("raw_content", f.getValue());
			}
		}
	}

	// When fetching a linear lesson, converts pictures old urls to current 
	private String setLinearPictureContent(String htmlContent) {
		Matcher m = PATTERN_LINEAR_PICS.matcher(htmlContent);
		ObjectDB picObject = getGrant().getTmpObject("TrnPicture");
		picObject.resetFilters();
		picObject.setFieldFilter("trnPicLsnId", getRowId());
		List<String[]> pics = picObject.search();
		// rebuilding string with StringBuilder
		StringBuilder out = new StringBuilder();
		int lastIndex = 0;
		while(m.find()) {
			String imgPath = m.group(1);
			for(String[] pic : pics) {
				picObject.setValues(pic);
				String docId = picObject.getFieldValue("trnPicImage");
				DocumentDB doc = DocumentDB.getDocument(docId, getGrant());
				String frontUrl = getGrant().getContextURL() + doc.getURL("").replace("/ui", "");;
				if(imgPath.contains(doc.getName())) {
					// append the content of html from the lastIndex (beginning of string to copy) to start index (end of string to copy)
					// in a nutshell add all the htmlContent before the match (match not included)
					out.append(htmlContent, lastIndex, m.start())
					// then append the new Url
					.append("src=\""+frontUrl+"\"");
				}
			}
			lastIndex = m.end();
		}
		if(lastIndex < htmlContent.length()) {
			out.append(htmlContent, lastIndex, htmlContent.length());
		}
		String returnValue = out.toString();
		return returnValue;
	}
	
	
}
