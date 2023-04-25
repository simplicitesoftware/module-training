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
		JSONObject p = new JSONObject(getGrant().getSystemParam("TRN_CONFIG"));
		JSONObject r = new JSONObject();
        String searchType = TrnTools.getContentIndexation().getString("engine");
		r.put("search_type", searchType);
		if("elasticsearch".equals(searchType)){
            JSONObject esConfig = p.getJSONObject("esi_config");
			r.put("es_instance", esConfig.getString("instance"));
			r.put("es_index", esConfig.getString("index"));
			r.put("es_credentials", esConfig.optString("public_credentials", ""));
		}
		return r;
	}
}