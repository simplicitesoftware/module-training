package com.simplicite.extobjects.Training;


import com.simplicite.util.*;
import com.simplicite.util.tools.*;

/**
 * External object TrnCommunityIndexService
 */
public class TrnCommunityIndexService extends com.simplicite.webapp.services.RESTServiceExternalObject {
	private static final long serialVersionUID = 1L;

    @Override
	public Object post(Parameters params) {
		try {
            AppLog.info("TrnCommunityIndexService " + params.toString(), getGrant());
			setHTTPStatus(200);
			return "OK";
		} catch (Exception e) {
			AppLog.error(getClass(), "sync", e.getMessage(), e, Grant.getSystemAdmin());
			setHTTPStatus(500);
			return e.getMessage();
		}
	}
}
