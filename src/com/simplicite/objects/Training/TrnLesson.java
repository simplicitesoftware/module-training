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
			content.setFieldFilter("trnLtrLang", lang);
			if(content.getCount()==0)
				content.setFieldFilter("trnLtrLang", "ANY");
			
			if(content.getCount()!=1)
				throw new Exception("LSN_CONTENT_NOT_FOUND");
			else{
				content.setValues((content.search()).get(0));
				json.put("title", content.getFieldValue("trnLtrTitle"));
				json.put("video", content.getFieldValue("trnLtrVideo"));
				json.put("ltr_id", content.getRowId());
				if(includeHtml)
					json.put("html", content.getFieldValue("trnLtrHtmlContent"));
				if(includeRaw)
					json.put("raw_content", content.getFieldValue("trnLtrRawContent"));
			}
		}
		return json;
	}
	
	
}
