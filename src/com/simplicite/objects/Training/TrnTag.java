package com.simplicite.objects.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;
import org.json.*;

/**
 * Business object TrnTag
 */
public class TrnTag extends ObjectDB {
	private static final long serialVersionUID = 1L;
	
	public JSONObject getTagForFront(String lang) throws Exception {
		JSONObject json = new JSONObject()
			.put("row_id", getRowId())
			.put("code", getFieldValue("trnTagCode"));
		ObjectDB tagTranslate = getGrant().getTmpObject("TrnTagTranslate");
		synchronized(tagTranslate) {
			tagTranslate.resetFilters();
			tagTranslate.setFieldFilter("trnTaglangTagId", getRowId());
			tagTranslate.setFieldFilter("trnTagTranslateLang", lang);
			if(tagTranslate.getCount() == 0) {
				tagTranslate.setFieldFilter("trnTagTranslateLang", "ANY");
			}
			if(tagTranslate.getCount() != 1) {
				throw new Exception("TAG_TRANSLATION_NOT_FOUND");
			} else {
				tagTranslate.setValues(tagTranslate.search().get(0));
				json.put("display_value", tagTranslate.getFieldValue("trnTagTranslateTrad"));
			}
		}
		return json;
	}
}
