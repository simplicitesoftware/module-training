package com.simplicite.extobjects.Training;

import java.util.*;

import com.simplicite.commons.Training.TrnTools;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;

/**
 * External object TrnSearchService
 */
public class TrnSearchService extends com.simplicite.webapp.services.RESTServiceExternalObject { 

    @Override
	public Object get(Parameters params) {
        Grant g = getGrant();
        try {
            
            String query = params.getParameter("query");
            String indexEngine = TrnTools.getIndexEngine();
            ArrayList<Object> searchResults = new ArrayList<>();
            if("elasticsearch".equals(indexEngine)) {

            } else if("simplicite".equals(indexEngine)) {

            } else {
                AppLog.warning(getClass(), "get", 
                    "Unkown index engine: "+indexEngine+"found in TRN_CONFIG. Fallback to simplicite engine.",
                    null, g);
            }

            return searchResults;
        } catch(Exception e) {
            AppLog.error(getClass(), "get", e.getMessage(), e, g);
            return "Unable to get results for the requested query";
        }
        
    }
}
