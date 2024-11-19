package com.simplicite.extobjects.Training;

import com.simplicite.webapp.services.RESTServiceExternalObject;
import com.simplicite.commons.Training.TrnFsSyncTool;
import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.tools.Parameters;

/**
 * External object TrnSyncService
 */
public class TrnSyncService extends RESTServiceExternalObject {
	private static final long serialVersionUID = 1L;

	@Override
	public Object get(Parameters params) {
		try {
			TrnFsSyncTool sync = new TrnFsSyncTool(Grant.getSystemAdmin());
			sync.sync();
			setHTTPStatus(200);
			return "OK";
		} catch (Exception e) {
			AppLog.error(getClass(), "sync", e.getMessage(), e, Grant.getSystemAdmin());
			setHTTPStatus(500);
			return e.getMessage();
		}
	}
}