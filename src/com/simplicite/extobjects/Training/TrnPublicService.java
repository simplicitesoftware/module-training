package com.simplicite.extobjects.Training;

import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.TrnTools;
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
		JSONObject r = new JSONObject();
        String searchType = TrnTools.getIndexEngine();
		r.put("search_type", searchType);
		if(TrnTools.isElasticSearchMode()){
            JSONObject esiConfig = TrnTools.getEsiConfig();
			r.put("es_instance", esiConfig.getString("instance"));
			r.put("es_index", esiConfig.getString("index"));
			r.put("es_credentials", esiConfig.optString("public_credentials", ""));
		}
		return r;
	}
}