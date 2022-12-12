package com.simplicite.objects.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.*;

/**
 * Business object TrnPage
 */
public class TrnPage extends TrnLesson {
	private static final long serialVersionUID = 1L;

	@Override
	public List<String> postValidate() {
		List<String> msgs = new ArrayList<>();
		setFieldValue("trnLsnPath", getPath());
		setFieldValue("trnLsnFrontPath", getFrontPath());	
		ObjectDB page = getGrant().getTmpObject("TrnPage");
		if(isNew()) {
			synchronized(page) {
				page.resetFilters();
				page.setFieldFilter("trnPageType", "homepage");
				if(page.count() >= 1)
					msgs.add("You already have a homepage");
			}
		}
		return msgs;
	}

	private String getPath() {
		return getFieldValue("trnLsnCatId.trnCatPath")+"/"+"PAGE_"+getFieldValue("trnLsnOrder")+"_"+TrnTools.toSnake(getFieldValue("trnLsnCode"));
	}

	private String getFrontPath() {
		return "/" + getFieldValue("trnLsnCode");
	}
}
