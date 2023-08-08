package com.simplicite.extobjects.Training;

import org.json.JSONObject;

import com.simplicite.commons.Training.TrnDiscourseHook;
import com.simplicite.commons.Training.TrnTools;
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
			Grant g = Grant.getSystemAdmin();
			String event = params.getHeader("x-discourse-event");
			JSONObject body = params.getJSONObject();
			TrnDiscourseHook discourseHook = new TrnDiscourseHook(g);
			setHTTPStatus(200);
			if(body.has("topic")) {
				discourseHook.handleTopic(body, event);	
			} else if(body.has("post")) {
				discourseHook.handlePost(body, event);
			} else if(body.has("category")) {
				discourseHook.handleCategory(body, event);
			} else {
				String key = body.keys().toString();
				AppLog.warning("Discourse hook send a non-supported object to index", null, g);
				setHTTPStatus(200);
				// test this response;
				return "The hook was not processed because the server does not handle this type of discourse object: " + key;
			}
			return "OK";
		} catch (Exception e) {
			AppLog.error(getClass(), "sync", e.getMessage(), e, Grant.getSystemAdmin());
			setHTTPStatus(500);
			return e.getMessage();
		}
	}
}
