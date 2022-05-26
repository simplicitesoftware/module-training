package com.simplicite.objects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.*;
import org.json.JSONObject;

/**
 * Business object TrnLesson
 */
public class TrnLesson extends TrnObject {
	private static final long serialVersionUID = 1L;

	@Override
	public String getUserKeyLabel(String[] row) {
		return getFieldValue("trnLsnFrontPath", row);
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
				if(includeHtml && !f.isEmpty() && !json.has("html"))
					json.put("html", f.getValue());
				
				f = content.getField("trnLtrRawContent");
				if(includeRaw && !f.isEmpty() && !json.has("raw_content"))
					json.put("raw_content", f.getValue());
			}
		}
	}
	
	
}
