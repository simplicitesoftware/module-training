package com.simplicite.objects.Training;

import com.simplicite.util.ObjectDB;
import com.simplicite.util.AppLog;

import org.json.JSONObject;

import com.simplicite.commons.Training.TrnObject;

/**
 * Business object TrnTag
 */
public class TrnTag extends TrnObject {
	private static final long serialVersionUID = 1L;
    private static final String LANG_FIELD = "trnTagTranslateLang";
    private static final String TRAD_FIELD = "trnTagTranslateTrad";
    private static final String TAG_ID = "trnTaglangTagId";
	
	public JSONObject getTagForFront(String lang) throws Exception {
		JSONObject json = new JSONObject()
			.put("row_id", getRowId())
			.put("code", getFieldValue("trnTagCode"));
		ObjectDB tagTranslate = getGrant().getTmpObject("TrnTagTranslate");
		synchronized(tagTranslate) {
			tagTranslate.resetFilters();
			tagTranslate.setFieldFilter(TAG_ID, getRowId());
			tagTranslate.setFieldFilter(LANG_FIELD, lang);
			if(tagTranslate.getCount() == 0) {
				tagTranslate.setFieldFilter(LANG_FIELD, "ANY");
			}
			if(tagTranslate.getCount() != 1) {
				throw new Exception("TAG_TRANSLATION_NOT_FOUND");
			} else {
				tagTranslate.setValues(tagTranslate.search().get(0));
				json.put("display_value", tagTranslate.getFieldValue(TRAD_FIELD));
			}
		}
		return json;
	}

    @Override
	public String postCreate() {
		try{
			ObjectDB tsl = getGrant().getTmpObject("TrnTagTranslate");
			synchronized(tsl.getLock()){
				tsl.resetValues();
				tsl.setFieldValue(LANG_FIELD, "ANY");
				tsl.setFieldValue(TRAD_FIELD, getFieldValue("trnTagCode"));
				tsl.setFieldValue(TAG_ID, getRowId());
				tsl.getTool().validateAndCreate();
			}
		}catch(Exception e){
			AppLog.error(e, getGrant());
		}

		return null;
	}
}