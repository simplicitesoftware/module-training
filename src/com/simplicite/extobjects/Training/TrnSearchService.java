package com.simplicite.extobjects.Training;

import com.simplicite.commons.Training.TrnSearch;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.tools.Parameters;
import com.simplicite.webapp.services.RESTServiceExternalObject;
/**
 * External object TrnSearchService
 */
public class TrnSearchService extends RESTServiceExternalObject { 

    @Override
	public Object get(Parameters params) {
        Grant g = getGrant();
        try {
            return TrnSearch.search(
            	TrnTools.getIndexEngine(), 
            	params.getParameter("query"), 
            	params.getParameter("lang"),
            	g
            );
        } catch(Exception e) {
            AppLog.error(getClass(), "get", e.getMessage(), e, g);
            setHTTPStatus(500);
            return "Unable to get results for the requested query";
        }
    }
}
