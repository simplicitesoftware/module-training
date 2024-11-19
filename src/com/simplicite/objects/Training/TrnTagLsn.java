package com.simplicite.objects.Training;

import org.json.JSONArray;

import com.simplicite.commons.Training.TrnObject;
import com.simplicite.util.Tool;

/**
 * Business object TrnTagLsn
 */
public class TrnTagLsn extends TrnObject {
	private static final long serialVersionUID = 1L;
	
	public boolean lessonHasTag(String lsn_row_id, JSONArray tags) {
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
	
	public String getTagLsnRowId(String tagId, String lsnId) {
		if(Tool.isEmpty(tagId) || Tool.isEmpty(lsnId)) return "";
		return getGrant().simpleQuery("select row_id from trn_tag_lsn where trn_taglsn_tag_id='"+tagId+"' AND trn_taglsn_lsn_id='"+lsnId+"'");
	}
}