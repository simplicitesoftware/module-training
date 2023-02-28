package com.simplicite.commons.Training;


import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import kong.unirest.Unirest;
import org.json.JSONObject;

/**
 * Shared code EsiHelper
 */
public class TrnEsiHelper implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	 
	private Grant g;
	private String esInstance;
	private String esIndex;
	private String esIndexOverride;
	private String esUser;
	private String esPassword;
	
	public TrnEsiHelper(Grant g){
		this(g, new JSONObject(g.getParameter("ESI_CONFIG")));
	}
	
	public TrnEsiHelper(Grant g, JSONObject p){
		this.g=g;
		this.esInstance=p.getString("instance");
		this.esIndex=p.optString("index", "simplicite").toLowerCase();
		String esCredentials=p.optString("credentials", null);
		this.esUser = esCredentials!=null ? esCredentials.split(":")[0] : null;
		this.esPassword = esCredentials!=null ? esCredentials.split(":")[1] : null;
	}
	
	public void setIndex(String index){
		esIndexOverride = index.toLowerCase();
	}
	
	public void setDefaultIndex(){
		esIndexOverride = null;
	}
	
	public String getDefaultIndex(){
		return esIndex;
	}
	
	public void indexEsDoc(String id, JSONObject doc){
		String index = esIndexOverride!=null ? esIndexOverride : esIndex;
		String url = esInstance+"/"+index+"/_doc/"+id;
		//AppLog.info("Indexing at "+url+" : "+doc.toString(), Grant.getSystemAdmin());
		try{
			String result = RESTTool.post(doc, "application/json", url);
			AppLog.info("Indexing at "+url+" : "+doc.getString("path"), Grant.getSystemAdmin());
		}
		catch(Exception e){
			AppLog.error("Error calling elasticsearch", e, g);
		}
	}
	
	public void indexAllModules(){
		for(String mdl : getModules())
			if(!ModuleDB.isSystemModule(mdl))
				indexModule(mdl);
	}
	
	public void indexModule(String mdl){
		for(String obj : getObjects(mdl))
			indexObject(obj);
	}
	
	private void indexObject(String objectName){
		ObjectDB o = g.getTmpObject(objectName);
		synchronized(o){
			if(o.isIndexable()){
				o.resetFilters();
				for(String[] rslt : o.search()){
					JSONObject doc = new JSONObject();
					for(ObjectField f : o.getFields())
						if(f.isIndexable())
							doc.put(f.getName(), rslt[o.getFieldIndex(f.getName())]);
					indexEsDoc(o.getName()+":"+rslt[0], doc);
				}
			}
		}
	}

	public void deleteEsLesson(String lsnId) {
		String index = esIndexOverride!=null ? esIndexOverride : esIndex;
		String url = esInstance+"/"+index+"/_doc/"+lsnId;
		try {
			String result = RESTTool.delete(url);
			AppLog.info("Deleted lesson index: " + url, Grant.getSystemAdmin());
		} catch(Exception e) {
			AppLog.error("Error deleting lesson: "+url, e, Grant.getSystemAdmin());
		}
	}
	
	private String[] getModules(){
		ObjectDB m = g.getTmpObject("Module");
		synchronized(m){
			m.resetFilters();
			return Tool.getColumnOfMatrixAsArray(m.search(), m.getFieldIndex("mdl_name"));
		}
	}
	 
	private String[] getObjects(String mdl){
		ObjectDB o = g.getTmpObject("ObjectInternal");
		synchronized(o){
			o.resetFilters();
			o.setFieldFilter("mdl_name", mdl);
			return Tool.getColumnOfMatrixAsArray(o.search(), o.getFieldIndex("obo_name"));
		}
	}
	 
}

