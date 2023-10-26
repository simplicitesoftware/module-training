package com.simplicite.commons.Training;

import org.json.JSONObject;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.simplicite.objects.Training.TrnSyncSupervisor;
import com.simplicite.util.Grant;
import com.simplicite.util.exceptions.HTTPException;
import com.simplicite.util.tools.RESTTool;

import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;

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
				TrnSyncSupervisor.addInfoLog("Indexing lesson: " + doc.getString("path"));
			} else if (doc.has("url")) {
				TrnSyncSupervisor.addInfoLog("Indexing community topic: " + doc.getString("url"));
			}
		} catch (Exception e) {
			TrnSyncSupervisor.addErrorLog("Error calling elasticsearch on url: "+url+" doc: "+doc.toString());
		}
	}

	public void deleteIndex() {
		try {
			RESTTool.delete(getEsUrl(), esUser, esPassword);
		} catch (Exception e) {
			TrnSyncSupervisor.addErrorLog("Error deleting index " + esIndex + " on elasticsearch instance " + esInstance);
		}
	}

	public void createIndex() {
		String url = getEsUrl();
		try {
			RESTTool.put("simplicite", url, esUser, esPassword);
		} catch (Exception e) {
			TrnSyncSupervisor.addErrorLog("Error creating index " + esIndex + " on elasticsearch instance " + esInstance);
		}
	}

	public void deleteEsDoc(int docId) {
		String url = getEsUrl() + "_doc/" + docId;
		try {
			RESTTool.delete(url, esUser, esPassword);
			TrnSyncSupervisor.addInfoLog("Deleted index doc: " + url);
		} catch (Exception e) {
			TrnSyncSupervisor.addWarnLog("Error deleting elastic index doc: " + url + " : " + e.getMessage());
		}
	}

	public String getEsDoc(int docId) {
		try {
			return RESTTool.get(getEsDocUrl(docId), esUser, esPassword);
		} catch(HTTPException e) {
			return "";
		}
	}

    public JSONArray searchRequest(JSONObject fullQuery) throws TrnConfigException {
        JSONArray res =  Unirest.post(getEsSearchUrl())
            .header("Content-Type", "application/json")
            .header("Authorization", "Basic " + Base64Coder.encodeString(TrnTools.getEsCredentials()))
            .body(fullQuery.toString())
            .asJson()
            .getBody()
            .getObject()
            .getJSONObject("hits")
            .getJSONArray("hits");
        return res;
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
