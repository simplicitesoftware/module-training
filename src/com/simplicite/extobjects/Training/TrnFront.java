package com.simplicite.extobjects.Training;

import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.util.exceptions.*;
import java.util.*;

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
		return super.display(params);
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
}
