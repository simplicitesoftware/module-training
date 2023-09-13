package com.simplicite.commons.Training;

import java.util.ArrayList;

import org.json.JSONObject;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.exceptions.HTTPException;
import com.simplicite.util.tools.RESTTool;

import kong.unirest.Unirest;

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
		} else {
			return null;
		}
	}

	public String getDefaultIndex() {
		return esIndex;
	}

	public void indexEsDoc(int docId, JSONObject doc) {
		String url = getEsDocUrl(docId);
		try {
			RESTTool.post(doc, "application/json", url, esUser, esPassword);
			if (doc.has("path")) {
				AppLog.info("Indexing at " + url + " : " + doc.getString("path"), Grant.getSystemAdmin());
			} else if (doc.has("url")) {
				AppLog.info("Indexing at " + url + " : " + doc.getString("url"), Grant.getSystemAdmin());
			}
		} catch (Exception e) {
			AppLog.error("Error calling elasticsearch", e, g);
		}
	}

	public void deleteIndex() {
		try {
			RESTTool.delete(getEsUrl(), esUser, esPassword);
		} catch (Exception e) {
			AppLog.error("Error deleting index " + esIndex + " on elasticsearch instance " + esInstance, e, g);
		}
	}

	public void deleteByQuery(int categoryId) {
		try {
			RESTTool.delete(getEsUrl()+"_delete_by_query?{\"query\":{\"match\":{\"category_id\": "+categoryId+"}}}", 
				esUser, esPassword);
		} catch (Exception e) {
			AppLog.error("Error creating index " + esIndex + " on elasticsearch instance " + esInstance, e, g);

		}
	}

	public void createIndex() {
		String url = getEsUrl();
		try {
			RESTTool.put("simplicite", url, esUser, esPassword);
		} catch (Exception e) {
			AppLog.error("Error creating index " + esIndex + " on elasticsearch instance " + esInstance, e, g);
		}
	}

	public void deleteEsDoc(int docId) {
		String url = getEsUrl() + "_doc/" + docId;
		try {
			RESTTool.delete(url, esUser, esPassword);
			AppLog.info("Deleted index doc: " + url, Grant.getSystemAdmin());
		} catch (Exception e) {
			AppLog.warning("Error deleting elastic index doc: " + url + " : " + e.getMessage(), e,
					Grant.getSystemAdmin());
		}
	}

	public String getEsDoc(int docId) {
		try {
			return RESTTool.get(getEsDocUrl(docId), esUser, esPassword);
		} catch(HTTPException e) {
			return "";
		}
	}

    public ArrayList<String> searchRequest(JSONObject fullQuery) {
        try {
            Unirest.get(getEsSearchUrl())
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + Base64Coder.encodeString(TrnTools.getEsCredentials()))
                .queryString("query", fullQuery.toString());
        } catch(Exception e) {

        }
        return new ArrayList<>();
    }

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
