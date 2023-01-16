package com.simplicite.extobjects.Training;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.simplicite.util.*;
import com.simplicite.util.tools.*;

/**
 * External object TrnExtUrlRedirector
 */
public class TrnExtUrlRedirector extends ExternalObject {
	private static final long serialVersionUID = 1L;
	private static final Pattern urlParser = Pattern.compile("^/ext/TrnExtUrlRedirector(/.*)$");

	@Override
	public Object display(Parameters params) {
		try {
			Grant g = getGrant();
			Matcher matcher = urlParser.matcher(params.getLocation());
			if(matcher.find()) {
				String requestPath = matcher.group(1);
				ObjectDB urlRewriter = g.getTmpObject("TrnUrlRewriting");
				List<String[]> rows = urlRewriter.search();
				String destinationUrl = "";
				for(int i = 0; i < rows.size(); i++) {
					urlRewriter.setValues(rows.get(i));
					String source = urlRewriter.getFieldValue("trnSourceUrl");
					if(source.equals((requestPath))) {
						destinationUrl = urlRewriter.getFieldValue("trnDestinationUrl");
						break;
					}
				}
				//setPublic(true);
				return this.sendHttpRedirect(params, destinationUrl);
			} else {
				try { return sendHttpError(params, 500); } catch (IOException ie) { return "Unexpected error"; }
			}
		} catch(Exception e) {
			return e.getMessage();
		}
	}
}
