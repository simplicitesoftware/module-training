package com.simplicite.commons.Training;

import org.json.JSONObject;

import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.ModuleDB;
import com.simplicite.util.ObjectDB;
import com.simplicite.util.ObjectField;
import com.simplicite.util.Tool;
import com.simplicite.util.exceptions.HTTPException;
import com.simplicite.util.tools.RESTTool;

/**
 * Shared code EsiHelper
 */
public class TrnEsiHelper implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private Grant g;
	private String esInstance;
	private String esIndex;
	private String esUser;
	private String esPassword;

	public TrnEsiHelper(Grant g) throws TrnConfigException {
		this.g = g;
		this.esInstance = TrnTools.getEsiUrl();
		this.esIndex = TrnTools.getEsiIndex();
		String esCredentials = TrnTools.getEsiCredentials();
		if (!esCredentials.isEmpty()) {
			this.esUser = esCredentials.split(":")[0];
			this.esPassword = esCredentials.split(":")[1];
		}
	}

	public static TrnEsiHelper getEsiHelper(Grant g) throws TrnConfigException {
		if (TrnTools.isElasticSearchMode()) {
			return new TrnEsiHelper(g);
		} else {
			return null;
		}
	}

	public String getDefaultIndex() {
		return esIndex;
	}

	public void indexEsiDoc(int docId, JSONObject doc) {
		String url = getEsiDocUrl(docId);
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
			RESTTool.delete(getEsiUrl(), esUser, esPassword);
		} catch (Exception e) {
			AppLog.error("Error deleting index " + esIndex + " on elasticsearch instance " + esInstance, e, g);
		}
	}

	public void deleteByQuery(int categoryId) {
		try {
			RESTTool.delete(getEsiUrl()+"_delete_by_query?{\"query\":{\"match\":{\"category_id\": "+categoryId+"}}}", 
				esUser, esPassword);
		} catch (Exception e) {
			AppLog.error("Error creating index " + esIndex + " on elasticsearch instance " + esInstance, e, g);

		}
	}

	public void createIndex() {
		String url = getEsiUrl();
		try {
			RESTTool.put("simplicite", url, esUser, esPassword);
		} catch (Exception e) {
			AppLog.error("Error creating index " + esIndex + " on elasticsearch instance " + esInstance, e, g);
		}
	}

	public void deleteEsiDoc(int docId) {
		String url = getEsiUrl() + "_doc/" + docId;
		try {
			RESTTool.delete(url, esUser, esPassword);
			AppLog.info("Deleted index doc: " + url, Grant.getSystemAdmin());
		} catch (Exception e) {
			AppLog.warning("Error deleting elastic index doc: " + url + " : " + e.getMessage(), e,
					Grant.getSystemAdmin());
		}
	}

	public String getEsiDoc(int docId) throws HTTPException {
		return RESTTool.get(getEsiDocUrl(docId), esUser, esPassword);
	}

	private String getEsiUrl() {
		return esInstance + "/" + esIndex + "/";
	}

	public String getEsiDocUrl(int docId) {
		return getEsiUrl() + "_doc/" + docId;
	}
}
