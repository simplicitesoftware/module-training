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
	
	private final Grant g = getGrant();
	
	public Boolean lessonHasTag(String lsn_row_id, JSONArray tags) {
		Boolean hasTag = false;
		for(int i = 0; i < tags.length(); i++) {
			String res = g.simpleQuery("select row_id from trn_tag_lsn where trn_taglsn_tag_id='"+tags.get(i)+"' AND trn_taglsn_lsn_id='"+lsn_row_id+"'");
			if(!Tool.isEmpty(res)) hasTag = true;
		}
		return hasTag;
	}
	
	public String getTagLsnRowId(String code, String lsnPath) {
		if(Tool.isEmpty(code) || Tool.isEmpty(lsnPath)) return "";
		return g.simpleQuery("select row_id from trn_tag_lsn where trn_tag_code='"+code+"' AND trn_lsn_path='"+lsnPath+"'");
	}
}
