package com.simplicite.objects.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;
import org.json.JSONArray;


/**
 * Business object TrnTagLsn
 */
public class TrnTagLsn extends ObjectDB {
	private static final long serialVersionUID = 1L;
	
	public Boolean lessonHasTag(String lsn_row_id, JSONArray tags) {
		Boolean hasTag = false;
		for(int i = 0; i < tags.length(); i++) {
			String tag_id = tags.getJSONObject(i).getString("row_id");
			String res = getGrant().simpleQuery("select row_id from trn_tag_lsn where trn_taglsn_tag_id='"+tag_id+"' AND trn_taglsn_lsn_id='"+lsn_row_id+"'");
			if(!Tool.isEmpty(res)) {
				hasTag = true;
			} 
		}
		return hasTag;
	}
	
	public String getTagLsnRowId(String code, String lsnPath) {
		if(Tool.isEmpty(code) || Tool.isEmpty(lsnPath)) return "";
		return getGrant().simpleQuery("select row_id from trn_tag_lsn where trn_tag_code='"+code+"' AND trn_lsn_path='"+lsnPath+"'");
	}
}
