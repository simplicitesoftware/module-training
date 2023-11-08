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

	/*@Override
	public Object displayFile(String path){
		redirectIfNecessary(path);
		super.displayFile(path);
	}
	
	/*private void redirectIfNecessary(String requestedUrl){
		try {
			//String requestedUrl = params.getLocation();
			
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
		}
		catch(GetException e){
			// ignore those exceptions, url was not found => no redirect
		}
		catch (Exception e) {
			// other exception => log
			AppLog.error("Error redirecting", e, getGrant());
		}
	}*/

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
