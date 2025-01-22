package com.simplicite.extobjects.Training;

import java.util.Map;
import com.simplicite.commons.Training.TrnSearchElastic;
import com.simplicite.commons.Training.TrnSearchSimplicite;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.util.AppLog;
import com.simplicite.util.ObjectDB;
import com.simplicite.util.Grant;
import com.simplicite.util.tools.BusinessObjectTool;
import com.simplicite.util.tools.HTMLTool;
import com.simplicite.util.Tool;
import com.simplicite.util.exceptions.GetException;
import com.simplicite.util.tools.Parameters;
import com.simplicite.webapp.services.RESTServiceExternalObject;

import org.json.JSONArray;
import org.json.JSONObject;

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
	  boolean getCategories = multiParams.containsKey("getCategories") && Boolean.parseBoolean(multiParams.get("getCategories")[0]);
      if (getCategories) {
        return getCategories(indexEngine,g);
      }
      String query = multiParams.get("query")[0];
      String lang = multiParams.get("lang")[0];
      int page = Integer.parseInt(multiParams.get("page")[0]);
      String[] filters = getFilters(multiParams.get("filters"));
	  
      if ("elasticsearch".equals(indexEngine)) {
        try {
          return TrnSearchElastic.search(query, lang, g, filters, page);
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
      return TrnSearchSimplicite.search(query, lang, g, page);
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
  private static JSONArray getCategories(String indexEngine, Grant g) {
    JSONArray categories = new JSONArray();
    ObjectDB obj = g.getTmpObject("TrnSearchIndex");
    synchronized(obj.getLock()) {
      BusinessObjectTool objt = obj.getTool();
      //documentation categorie
      try{
        if("elasticsearch".equals(indexEngine)) {
          obj.resetFilters();
          if(objt.getForCreateOrUpdate(new JSONObject().put("trnTsiFilter", "lesson"))){
            categories.put(parseSearchIndex(obj));
          }
          //discourse categorie
           obj.resetFilters();
          if(objt.getForCreateOrUpdate(new JSONObject().put("trnTsiFilter", "discourse"))){
            categories.put(parseSearchIndex(obj));
          }
        }else{
          obj.resetFilters();
          if(objt.getForCreateOrUpdate(new JSONObject().put("trnTsiFilter", "lesson"))){
            categories.put(parseSearchIndex(obj));
          }
          
        }
      }catch(GetException e){
        AppLog.error(e,g);
      }
    }
    return categories;
  }
  private static JSONObject parseSearchIndex(ObjectDB obj){
    String iconSrc = obj.getFieldValue("trnTsiMaterialIconsName");
    if(Tool.isEmpty(iconSrc)){
    	iconSrc = HTMLTool.getPublicDocumentURL("TrnSearchIndex","trnTsiIcon",obj.getRowId(),obj.getFieldValue("trnTsiIcon"),"",false,false);
    }
    return new JSONObject().put("type", obj.getFieldValue("trnTsiFilter")).put("display", obj.getFieldValue("trnTsiDisplay")).put("icon", iconSrc);
  }
}