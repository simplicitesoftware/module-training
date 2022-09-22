package com.simplicite.objects.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.commons.Training.*;
import com.simplicite.util.tools.*;

/**
 * Business object TrnTagTranslate
 */
public class TrnTagTranslate extends TrnObject {
	private static final long serialVersionUID = 1L;

	public List<String[]> getTranslatesFromTagId(String tag_id) {	
		List<String[]> res = new ArrayList<>();
		ObjectDB tagTranslate = getGrant().getTmpObject("TrnTagTranslate");
		synchronized(tagTranslate){
			tagTranslate.resetFilters();
			tagTranslate.setFieldFilter("trnTaglangTagId", tag_id);
			res = tagTranslate.search();
		}
		return res;
	}
}
