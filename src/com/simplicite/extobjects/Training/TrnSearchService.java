package com.simplicite.extobjects.Training;

import java.util.Map;

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
      Map<String, String[]> multiParams = params.getMultiParameters();

      // https://www.elastic.co/guide/en/elasticsearch/reference/current/point-in-time-api.html
      // no fallback because it's not a search request
      if ("elasticsearch".equals(indexEngine) && multiParams.containsKey("actionType")
          && "getPIT".equals(multiParams.get("actionType")[0])) {
        return TrnSearchElastic.getPIT(g);
      }

      String query = multiParams.get("query")[0];
      String lang = multiParams.get("lang")[0];
      String[] filters = getFilters(multiParams.get("filters"));

      if ("elasticsearch".equals(indexEngine)) {
        try {
          // JSONObject pit = multiParams.get("pit");
          return TrnSearchElastic.search(query, lang, g, filters);
        }
        catch (Exception e) {
          AppLog.warning("Error, unable to retrieve elasticsearch search results. Fallback to simplicite engine.", e,
              g);
        }
      }
      else if (!"simplicite".equals(indexEngine)) {
        AppLog.warning("Unkown index engine: " + indexEngine + " found in TRN_CONFIG. Fallback to simplicite engine.",
            null, g);
      }
      return TrnSearchSimplicite.search(query, lang, g);
    }
    catch (Exception e) {
      AppLog.error(getClass(), "get", e.getMessage(), e, g);
      setHTTPStatus(500);
      return "Search service error" + e.getMessage();
    }
  }

  private static String[] getFilters(String[] filters) {
    if (filters.length == 1) {
      filters = filters[0].split(",");
      if (filters.length == 1 && filters[0].isEmpty()) {
        return new String[0];
      }
    }
    return filters;
  }

  @Override
  public Object del(Parameters params) {
    try {
      // delete pit
      String pit = params.getParameter("pit");
      String msg = TrnSearchElastic.deletePit(getGrant(), pit);
      setHTTPStatus(200);
      return msg;
    }
    catch (Exception e) {
      AppLog.error(getClass(), "get", e.getMessage(), e, getGrant());
      setHTTPStatus(500);
      return "ERROR : " + e.getMessage();
    }
  }
}
