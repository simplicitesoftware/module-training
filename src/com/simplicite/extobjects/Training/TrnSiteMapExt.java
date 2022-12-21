package com.simplicite.extobjects.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;
import com.simplicite.webapp.services.RESTServiceExternalObject;

/**
 * External object TrnSiteMapExt
 */
public class TrnSiteMapExt extends RESTServiceExternalObject { 
	
	private static final long serialVersionUID = 1L;
	
	@Override
    public Object get(Parameters params) throws HTTPException {
        
    }

    @Override
    public Object post(Parameters params) throws HTTPException {
        try {
            JSONObject req = params.getJSONObject();
            if (req != null ) {
                return new JSONObject()
                    .put("request", req)
                    .put("response", "Hello " + req.optString("name", "Unknown"));
            } else {
                return error(400, "Call me with a request please!");
            }
        } catch (Exception e) {
            return error(e);
        }
    }
}
