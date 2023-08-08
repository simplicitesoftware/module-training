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

	public void indexEsiDoc(String docId, JSONObject doc) {
		String url = (docId);
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

	public void createIndex() {
		String url = getEsiUrl();
		try {
			RESTTool.put("simplicite", url, esUser, esPassword);
		} catch (Exception e) {
			AppLog.error("Error creating index " + esIndex + " on elasticsearch instance " + esInstance, e, g);
		}
	}

	public void indexAllModules() {
		for (String mdl : getModules()) {
			if (!ModuleDB.isSystemModule(mdl)) {
				indexModule(mdl);
			}
		}
	}

	public void indexModule(String mdl) {
		for (String obj : getObjects(mdl)) {
			indexObject(obj);
		}
	}

	private void indexObject(String objectName) {
		ObjectDB o = g.getTmpObject(objectName);
		synchronized (o) {
			if (o.isIndexable()) {
				o.resetFilters();
				for (String[] rslt : o.search()) {
					JSONObject doc = new JSONObject();
					for (ObjectField f : o.getFields()) {
						if (f.isIndexable()) {
							doc.put(f.getName(), rslt[o.getFieldIndex(f.getName())]);
						}
					}
					indexEsiDoc(o.getName() + ":" + rslt[0], doc);
				}
			}
		}
	}

	public void deleteEsiDoc(String docId) {
		String url = getEsiUrl() + "_doc/" + docId;
		try {
			RESTTool.delete(url, esUser, esPassword);
			AppLog.info("Deleted index doc: " + url, Grant.getSystemAdmin());
		} catch (Exception e) {
			AppLog.warning("Error deleting elastic index doc: " + url + " : " + e.getMessage(), e,Grant.getSystemAdmin());
		}
	}

	public String getEsiDoc(String docId) throws HTTPException {
		return RESTTool.get(getEsiDocUrl(docId));
	}

	

	private String[] getModules() {
		ObjectDB m = g.getTmpObject("Module");
		synchronized (m) {
			m.resetFilters();
			return Tool.getColumnOfMatrixAsArray(m.search(), m.getFieldIndex("mdl_name"));
		}
	}

	private String[] getObjects(String mdl) {
		ObjectDB o = g.getTmpObject("ObjectInternal");
		synchronized (o) {
			o.resetFilters();
			o.setFieldFilter("mdl_name", mdl);
			return Tool.getColumnOfMatrixAsArray(o.search(), o.getFieldIndex("obo_name"));
		}
	}

	private String getEsiUrl() {
		return esInstance + "/" + esIndex + "/";
	}

	public String getEsiDocUrl(String docId)  {
		return getEsiUrl()+"_doc/"+docId;
	}

	public static String getTopicId(String docId) {
		return "topic_" + docId;
	}

	public static String getTopicId(int docId) {
		return "topic_" + docId;
	}
}
