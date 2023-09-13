package com.simplicite.extobjects.Training;

import com.simplicite.commons.Training.TrnSearch;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.tools.Parameters;

/**
 * External object TrnSearchService
 */
public class TrnSearchService extends com.simplicite.webapp.services.RESTServiceExternalObject { 

    @Override
	public Object get(Parameters params) {
        Grant g = getGrant();
        try {
            String query = params.getParameter("query");
            String frontLang = params.getParameter("lang");
            String indexEngine = TrnTools.getIndexEngine();
        
            return TrnSearch.search(indexEngine, query, frontLang, g);
        } catch(Exception e) {
            AppLog.error(getClass(), "get", e.getMessage(), e, g);
            return "Unable to get results for the requested query";
        }
        
    }
}
