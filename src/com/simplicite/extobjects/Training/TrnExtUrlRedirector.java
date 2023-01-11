package com.simplicite.extobjects.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;

/**
 * External object TrnExtUrlRedirector
 */
public class TrnExtUrlRedirector extends ExternalObject {
	private static final long serialVersionUID = 1L;

	/**
	 * Display method (only relevant if extending the base ExternalObject)
	 * @param params Request parameters
	 */
	@Override
	public Object display(Parameters params) {
		try {
			setDecoration(false);
			// ctn is the "div.extern-content" to fill on UI
			return javascript("window.location.replace(\"https://google.com\")");
		} catch (Exception e) {
			AppLog.error(null, e, getGrant());
			return e.getMessage();
		}
	}
}
