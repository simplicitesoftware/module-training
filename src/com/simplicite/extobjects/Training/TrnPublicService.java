package com.simplicite.extobjects.Training;

import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.webapp.services.RESTServiceExternalObject;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.objects.Training.TrnTag;
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
		r.put("search_type", p.getString("index_type"));
		if("elasticsearch".equals(p.getString("index_type"))){
			r.put("es_instance", p.getJSONObject("esi_config").getString("instance"));
			r.put("es_index", p.getJSONObject("esi_config").getString("index"));
			r.put("es_credentials", p.getJSONObject("esi_config").optString("public_credentials", ""));
		}
		return r;
	}
}