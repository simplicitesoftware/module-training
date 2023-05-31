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
	public Object get(Parameters params) throws HTTPException{
        try {
            JSONObject r = new JSONObject();
            String searchType = TrnTools.getIndexEngine();
            r.put("search_type", searchType);
            if(TrnTools.isElasticSearchMode()){
                JSONObject esiConfig = TrnTools.getEsiConfig();
                String instance;
                if(esiConfig.has("front_instance")) {
                    instance = esiConfig.getString("front_instance");
                } else {
                    instance = esiConfig.getString("instance");
                }
                r.put("es_instance", instance);
                r.put("es_index", esiConfig.getString("index"));
                r.put("es_credentials", esiConfig.optString("public_credentials", ""));
            }
            return r;
        } catch(TrnConfigException e) {
            AppLog.error(getClass(), "get", e.getMessage(), e, getGrant());
            return "An error occured while parsing TRN_CONFIG";
        } catch(Exception e) {
            AppLog.error(getClass(), "get", e.getMessage(), e, getGrant());
            return "An error occured";
        }
	}
}