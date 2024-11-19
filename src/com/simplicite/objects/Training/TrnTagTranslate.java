package com.simplicite.objects.Training;

import java.util.List;

import com.simplicite.commons.Training.TrnObject;
import com.simplicite.util.ObjectDB;

/**
 * Business object TrnTagTranslate
 */
public class TrnTagTranslate extends TrnObject {
	private static final long serialVersionUID = 1L;

	public List<String[]> getTranslatesFromTagId(String tagId) {	
		ObjectDB tagTranslate = getGrant().getTmpObject("TrnTagTranslate");
		synchronized(tagTranslate){
			tagTranslate.resetFilters();
			tagTranslate.setFieldFilter("trnTaglangTagId", tagId);
			return tagTranslate.search();
		}
	}
}