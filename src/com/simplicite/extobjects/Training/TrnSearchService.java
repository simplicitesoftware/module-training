package com.simplicite.extobjects.Training;

import com.simplicite.commons.Training.TrnSearchElastic;
import com.simplicite.commons.Training.TrnSearchSimplicite;
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
            String indexEngine = TrnTools.getIndexEngine();
            String query = params.getParameter("query");
            String lang = params.getParameter("lang");
            
            if("elasticsearch".equals(indexEngine)){
                try {
                    return TrnSearchElastic.search(query, lang, g);
                }
                catch(Exception e) {
                    AppLog.warning("Error, unable to retrieve elasticsearch search results. Fallback to simplicite engine.", e, g);
                }
            }
            else if(!"simplicite".equals(indexEngine)) {
                AppLog.warning("Unkown index engine: "+indexEngine+" found in TRN_CONFIG. Fallback to simplicite engine.", null, g);
            }
            
            return TrnSearchSimplicite.search(query, lang, g);
        } catch(Exception e) {
            AppLog.error(getClass(), "get", e.getMessage(), e, g);
            setHTTPStatus(500);
            return "Unable to get results for the requested query";
        }
    }
}
