package com.simplicite.extobjects.Training;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import com.simplicite.util.AppLog;
import com.simplicite.util.DocumentDB;
import com.simplicite.util.Grant;
import com.simplicite.util.ObjectDB;
import com.simplicite.util.ObjectField;
import com.simplicite.util.Tool;
import com.simplicite.util.tools.HTMLTool;
import com.simplicite.util.tools.Parameters;
import org.json.JSONObject;
import java.util.List;

/**
 * External object TrnFront
 */
public class TrnFront extends com.simplicite.webapp.web.StaticSiteExternalObject {
	private static final long serialVersionUID = 1L;
	private static final String NOT_FOUND = "index.html";
	private static HashMap<String,String> rewritesMap = null;
	
	@Override
	public Object display(Parameters params){
		redirectIfNecessary(params);
		Object display = super.display(params);
		return addOpenGraphMetaData(params,display);
	}
	  
	private void redirectIfNecessary(Parameters params){
		loadRedirectsCache();
		try {
			//ex: "/lesson/docs/integration/io-commandline"
			String requestedUrl = getRequestedUrl(params); 
			
			// figure out possibilities : "/lesson", "/lesson/docs"...
			ArrayList<String> toSearch = new ArrayList<>();
			String url = "";
			for(String part : params.getLocationParts(getName())){
				url += "/"+part;
				toSearch.add(0,url);
			}
			
			for(String src : toSearch){
				if(rewritesMap.containsKey(src)){
					//ex "/lesson/docs/data-in-out/integration
					String dest = rewritesMap.get(src); 
					
					if(requestedUrl.length() > src.length())
						dest += requestedUrl.split(src)[1]; //ex: add "/io-commandline" to dest
					
					sendHttpRedirect(params, dest);
				}
			}
		}
		catch (Exception e) {
			// other exception => log
			AppLog.error("Error redirecting", e, getGrant());
		}
	}
	
	private String getRequestedUrl(Parameters params) throws Exception{
		String extUrl = HTMLTool.getPublicExternalObjectURL(this.getName());
		String requestUrl = params.getLocation();
		
		if(!requestUrl.contains(extUrl))
			throw new Exception(requestUrl+" does not contain "+extUrl);
		
		return Tool.toSQL(requestUrl.split(extUrl)[1]);
	}
	
	private static void loadRedirectsCache(){
		if(rewritesMap==null){
			rewritesMap = new HashMap<>();
			ObjectDB r = Grant.getSystemAdmin().getTmpObject("TrnUrlRewriting");
			synchronized(r.getLock()){
				for(String[] row : r.search()){
					r.setValues(row);
					rewritesMap.put(r.getFieldValue("trnSourceUrl"), r.getFieldValue("trnDestinationUrl"));
				}
			}
		}
	}
	
	public static void clearRedirectsCache(){
		rewritesMap = null;
	}

	@Override
	public Object notfound(Parameters params) {
		AppLog.info("Not found on: " + params.getBaseLocation(), getGrant());
		try {
			if (NOT_FOUND.equals(params.getURI())) // Avoid infinite loop
			throw new IOException();
			
			return displayFile(NOT_FOUND);
		} catch (IOException e) {
			AppLog.error(null, e, getGrant());
			return super.notfound(params);
		}
	}
	private Object addOpenGraphMetaData(Parameters params,Object display) {
		
		String requestedUrl =params.getLocation();
		JSONObject og = getOpenGraphInfo(requestedUrl, params);
		if(requestedUrl.contains("lesson") && display instanceof byte[]){
			String html = new String((byte[])display);
			if(!html.endsWith("</html>")) return display;
			for (String key : og.keySet()){
				html = html.replaceAll("<meta property=\""+key+"\" content=\"[^\"]*\">", "<meta property=\""+key+"\" content=\""+og.getString(key)+"\">");
			}
			AppLog.info(html, getGrant());
			return html;
		} 
		return display;
	}
	private JSONObject getOpenGraphInfo(String requestedUrl, Parameters params) {
		String host = params.getHeader("host");
		String img = getThemeFavIcon(host);
		String title = getThemeTitle();
		if(requestedUrl.contains("lesson")) {
			String lesson = requestedUrl.substring(requestedUrl.indexOf("lesson") + 6);
			String id = getObjectFieldContent("TrnLesson" ,"row_id" , new JSONObject().put("trnLsnFrontPath", lesson));
			if (!Tool.isEmpty(id)){
				title = getObjectFieldContent("TrnLsnTranslate" , "trnLtrTitle", new JSONObject().put("trnLtrLsnId", id).put("trnLtrLang", "='ENU' or ='ANY'"),new JSONObject().put("trnLtrLang",-1));
				title = "Docs | "+title;
			}
		}
		return new JSONObject().put("og:description", "Platform documentation").put("og:url", "https://"+host).put("og:title", title).put("og:image", Tool.isEmpty(img)?"https://cdn.jsdelivr.net/gh/simplicitesoftware/resources@latest/public/logo_simplicite/icon/icon128.png":img);

	}
	private String getThemeFavIcon(String host){
		String iconUrl = "https://" + host +"/simplicite.svg";
		// overridable favicon
		ObjectDB obj = getGrant().getTmpObject("TrnSiteTheme");
		synchronized(obj.getLock()){
			obj.resetFilters();
			List<String[]> res = obj.search();
			if (!Tool.isEmpty(res)){
				String objId = res.get(0)[obj.getRowIdFieldIndex()];
				String docId =  res.get(0)[obj.getFieldIndex("trnSitethemeFavicon")];
				iconUrl = "https://" + host + HTMLTool.getPublicDocumentURL("object=TrnSiteTheme&inst=api_TrnSiteTheme&field=trnSitethemeFavicon&row_id="+objId+"&doc_id="+docId);
			}
		} 
		return iconUrl;
	}
	private String getThemeTitle(){
		String title = "Docs";
		// overridable favicon
		ObjectDB obj = getGrant().getTmpObject("TrnSiteTheme");
		synchronized(obj.getLock()){
			obj.resetFilters();
			List<String[]> res = obj.search();
			if (!Tool.isEmpty(res)){
				title = res.get(0)[obj.getFieldIndex("trnTrnsitethemeBaseTitle")];
			}
		} 
		return title;
	}
	private String getObjectFieldContent(String object, String field, JSONObject filters){
		return getObjectFieldContent(object, field, filters,null);	
	}
	private String getObjectFieldContent(String object, String field, JSONObject filters,JSONObject orders){
		ObjectDB obj = getGrant().getTmpObject(object);
		if(!Tool.isEmpty(orders)){
			try{
				obj.resetOrders();
				for(String order : orders.keySet()){
					obj.setFieldOrder(order, orders.getInt(order));
				}
			}catch(Exception e){
				obj.resetOrders();
			}
		}
		synchronized(obj.getLock()){
			obj.resetFilters();
			obj.setFilters(filters);
			List<String[]> res = obj.search();
			if (Tool.isEmpty(res))
				return "";
			return obj.search().get(0)[obj.getFieldIndex(field)];
		}	
	}
}