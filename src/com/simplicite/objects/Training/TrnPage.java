package com.simplicite.objects.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.*;

/**
 * Business object TrnPage
 */
public class TrnPage extends TrnObject {
	private static final long serialVersionUID = 1L;

	@Override
	public List<String> preValidate() {
	    List<String> msgs = new ArrayList<String>();
		String value = getFieldValue("trnPageType");
		if(value.equals("homepage") && isNew()) { // if new page is a homepage check if one does not already exists
			ObjectDB page = getGrant().getTmpObject("TrnPage");
			page.resetFilters();
			page.setFieldFilter("trnPageType", "homepage");
			if(page.count() == 1) {
				msgs.add(Message.formatError("You already have a homepage", null, "trnPageType"));
			}
		}
	    return msgs; // Return a list of messages
	}
}
