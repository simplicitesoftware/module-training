package com.simplicite.extobjects.Training;

import org.json.JSONObject;

import com.simplicite.commons.Training.TrnConfigException;
import com.simplicite.commons.Training.TrnDiscourseHook;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;

/**
 * External object TrnDiscourseHookService
 */
public class TrnDiscourseHookService extends com.simplicite.webapp.services.RESTServiceExternalObject {
	private static final long serialVersionUID = 1L;

	@Override
	public Object post(Parameters params) {
		try {
			AppLog.info("TrnDiscourseHookService " + params.toString(), getGrant());
			Grant g = Grant.getSystemAdmin();
			JSONObject body = params.getJSONObject();
			TrnDiscourseHook discourseHook = new TrnDiscourseHook(g);
			discourseHook.handleHook(body, params.getHeader("x-discourse-event"));
			setHTTPStatus(200);
			return "OK";
		} catch (Exception e) {
			AppLog.error(getClass(), "sync", e.getMessage(), e, Grant.getSystemAdmin());
			setHTTPStatus(500);
			return e.getMessage();
		}
	}
}
