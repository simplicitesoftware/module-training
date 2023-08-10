package com.simplicite.extobjects.Training;

import org.json.JSONObject;

import com.simplicite.commons.Training.TrnConfigException;
import com.simplicite.commons.Training.TrnDiscourseHook;
import com.simplicite.commons.Training.TrnDiscourseIndexerException;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.util.*;
import com.simplicite.util.exceptions.HTTPException;
import com.simplicite.util.tools.*;

/**
 * External object TrnDiscourseHookService
 */
public class TrnDiscourseHookService extends com.simplicite.webapp.services.RESTServiceExternalObject {
	private static final long serialVersionUID = 1L;

	@Override
	public Object post(Parameters params) {
		String msg;
		try {
			AppLog.info("TrnDiscourseHookService " + params.toString(), getGrant());
			Grant g = Grant.getSystemAdmin();
			JSONObject body = params.getJSONObject();
			TrnDiscourseHook discourseHook = new TrnDiscourseHook(g);
			discourseHook.handleHook(body, params.getHeader("x-discourse-event"));
			setHTTPStatus(200);
			msg = "OK";
		} catch (TrnDiscourseIndexerException e) {
			msg = "Indexation error: " + e.getMessage();
			AppLog.error(getClass(), "post", msg, e, Grant.getSystemAdmin());
		} catch (HTTPException e) {
			msg = "HTTP error: " + e.getMessage();
			AppLog.error(getClass(), "post", msg, e, Grant.getSystemAdmin());
		} catch (TrnConfigException e) {
			msg = "Training config error: " + e.getMessage();
			AppLog.error(getClass(), "post", msg, e, Grant.getSystemAdmin());
		} catch (Exception e) {
			AppLog.error(getClass(), "post", e.getMessage(), e, Grant.getSystemAdmin());
			setHTTPStatus(500);
			msg = e.getMessage();
		}
		return msg;
	}
}
