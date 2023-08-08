package com.simplicite.extobjects.Training;

import org.json.JSONObject;

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
			String event = params.getHeader("x-discourse-event");
			if(event.equals("post_created")) {
				
			} else if(event.equals("post_detroyed")) {

			} else if(event.equals("post_edited")) {
				
			} else {
				AppLog.info("TODO error handler and throw", Grant.getSystemAdmin());
			}
			JSONObject body = params.getJSONObject();
			AppLog.info("POST FROM DISCOURSE", Grant.getSystemAdmin());
			//RESTTool.post(TrnTools.getEsiUrl());
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
