package com.simplicite.extobjects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.webapp.services.RESTServiceExternalObject;
import com.simplicite.commons.Training.TrnFsSyncTool;

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
