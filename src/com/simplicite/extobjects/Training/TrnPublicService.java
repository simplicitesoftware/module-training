package com.simplicite.extobjects.Training;

import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.TrnConfigException;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.util.AppLog;
import com.simplicite.util.exceptions.*;
import com.simplicite.webapp.services.RESTServiceExternalObject;
import org.json.*;

/**
 * External object TrnTagService
 */
public class TrnPublicService extends RESTServiceExternalObject {
	private static final long serialVersionUID = 1L;

	@Override
	public Object get(Parameters params) throws HTTPException {
		try {
			JSONObject r = new JSONObject();
			String searchType = TrnTools.getIndexEngine();
			r.put("search_type", searchType);
			if (TrnTools.isElasticSearchMode()) {
				String instance;
				if (TrnTools.hasEsiFrontInstance()) {
					instance = TrnTools.getEsiFrontInstance();
				} else {
					instance = TrnTools.getEsiUrl();
				}
				r.put("es_instance", instance);
				r.put("es_index", TrnTools.getEsiIndex());
				r.put("es_credentials", TrnTools.getEsiCredentials());
			}
			return r;
		} catch (TrnConfigException e) {
			AppLog.error(getClass(), "get", e.getMessage(), e, getGrant());
			return "An error occured while parsing TRN_CONFIG, " + e.getMessage();
		} catch (Exception e) {
			AppLog.error(getClass(), "get", e.getMessage(), e, getGrant());
			return "An error occured, " + e.getMessage();
		}
	}
}