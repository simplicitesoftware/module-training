package com.simplicite.commons.Training;

import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.exceptions.HTTPException;
import com.simplicite.util.tools.RESTTool;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Shared code EsHelper
 */
public class TrnEsHelper implements java.io.Serializable {
  private static final long serialVersionUID = 1L;

  private Grant g;
  private String esInstance;
  private String esIndex;
  private String esUser;
  private String esPassword;

  public TrnEsHelper(Grant g) throws TrnConfigException {
    this.g = g;
    this.esInstance = TrnTools.getEsUrl();
    this.esIndex = TrnTools.getEsIndex();
    String esCredentials = TrnTools.getEsCredentials();
    if (!esCredentials.isEmpty()) {
      this.esUser = esCredentials.split(":")[0];
      this.esPassword = esCredentials.split(":")[1];
    }
  }

  public static TrnEsHelper getEsHelper(Grant g) throws TrnConfigException {
    if (TrnTools.isElasticSearchMode()) {
      return new TrnEsHelper(g);
    }
    else {
      return null;
    }
  }

  public String getDefaultIndex() {
    return esIndex;
  }

  public void indexEsDoc(int docId, JSONObject doc) throws TrnDiscourseIndexerException {
    String url = getEsDocUrl(docId);
    try {
      RESTTool.post(doc, "application/json", url, esUser, esPassword);
      if (doc.has("path")) {
        AppLog.info("Indexing lesson: " + doc.getString("path"), g);
      }
      else if (doc.has("url")) {
        AppLog.info("Indexing community topic: " + doc.getString("url"), g);
      }
    }
    catch (Exception e) {
      throw new TrnDiscourseIndexerException("Error calling elasticsearch on url: " + url + " doc: " + doc.toString());
    }
  }

  public void deleteIndex() throws HTTPException {
    RESTTool.delete(getEsUrl(), esUser, esPassword);
  }

  public void createIndex() throws HTTPException {
    RESTTool.put(null, getEsUrl(), esUser, esPassword);
  }

  public void deleteEsDoc(int docId) {
    String url = getEsUrl() + "_doc/" + docId;
    try {
      RESTTool.delete(url, esUser, esPassword);
      AppLog.info("Deleted index doc: " + url, g);
    }
    catch (Exception e) {
      AppLog.error("Error deleting elastic index doc: " + url, e, g);
    }
  }

  public String getEsDoc(int docId) {
    try {
      return RESTTool.get(getEsDocUrl(docId), esUser, esPassword);
    }
    catch (HTTPException e) {
      return "";
    }
  }

  public JSONObject searchRequest(JSONObject fullQuery) throws Exception {
    String res = null;
    try {
      res = RESTTool.post(fullQuery.toString(), getEsSearchUrl(), esUser, esPassword,
          Map.of("Content-Type", "application/json"));
      JSONObject resJson = new JSONObject(res);
      JSONObject json = new JSONObject();
      JSONObject hits = resJson.getJSONObject("hits");
      json.put("results", hits.getJSONArray("hits"))
        .put("search_duration", resJson.getInt("took"))
        .put("total_hits", hits.getJSONObject("total").getInt("value"));
      return json;
    }
    catch (Exception e) {
      AppLog.info("ES query --- " + fullQuery.toString(), g);
      if (res != null)
        AppLog.info("ES response --- " + res, g);

      AppLog.error("Error requesting elastic search", e, g);
      throw e;
    }
  }

  /*
   * public JSONArray searchRequest(JSONObject fullQuery) throws
   * TrnConfigException { AppLog.info("---"+fullQuery.toString(),
   * Grant.getSystemAdmin()); JSONArray res = Unirest.post(getEsSearchUrl())
   * .header("Content-Type", "application/json") .header("Authorization", "Basic "
   * + Base64Coder.encodeString(TrnTools.getEsCredentials()))
   * .body(fullQuery.toString()) .asJson() .getBody() .getObject()
   * .getJSONObject("hits") .getJSONArray("hits"); return res; }
   */
  private String getEsSearchUrl() {
    return this.getEsUrl() + "_search";
  }

  private String getEsUrl() {
    return esInstance + "/" + esIndex + "/";
  }

  public String getEsDocUrl(int docId) {
    return getEsUrl() + "_doc/" + docId;
  }
}