package com.simplicite.extobjects.Training;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.util.exceptions.*;
import java.util.*;

/**
 * External object TrnExtUrlRedirector
 */
public class TrnExtUrlRedirector extends ExternalObject {
	private static final long serialVersionUID = 1L;
	private static final Pattern urlParser = Pattern.compile("^/ext/TrnExtUrlRedirector(/.*)$");
	private static final String ROOT_PATH = "/ext/TrnExtUrlRedirector";
	
	//General algorithm:
	// 1- check if redirect 
	// 2- send to front
	
	@Override
	public Object display(Parameters params){
		try {
			if(!params.getLocation().startsWith(ROOT_PATH))
				return sendHttpError(params, 500);
			
			String requestedUrl = Tool.toSQL(params.getLocation().split(ROOT_PATH)[1]);
			
			
			// TODO cache results to go faster
			ObjectDB urlRewriter = getGrant().getTmpObject("TrnUrlRewriting");
			urlRewriter.getTool().select(Map.of(
				"trnSourceUrl", 
				requestedUrl+"%" //starts with
			));
			
			String src = urlRewriter.getFieldValue("trnSourceUrl");
			String dest = urlRewriter.getFieldValue("trnDestinationUrl");
			
			if(requestedUrl.length() > src.length())
				dest += requestedUrl.split(src)[1];
			
			AppLog.info(requestedUrl+" "+src+" "+dest, Grant.getSystemAdmin());

			sendHttpRedirect(params, dest);
			return "OK";
		}
		catch(GetException e){ 
			// GetException thrown by select if 0 or more than one result
			setHTTPStatus(404);
			return "Unknown url";
		}
		catch (Exception e) {
			AppLog.error("Error redirecting", e, getGrant());
			return e.getMessage();
		}
	}
}
