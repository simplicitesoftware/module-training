package com.simplicite.commons.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;
import com.simplicite.util.integration.DataXML;

/**
 * Shared code TrnSitemapGenerator
 */
public class TrnSitemapGenerator implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	Grant g;
	
	public class TrnSitemapGeneratorException extends Exception {
		public TrnSitemapGeneratorException(String msg){
			super(msg);
		}
	}
	
	public TrnSitemapGenerator(Grant g) throws TrnSitemapGeneratorException {
		this.g = g;		
	}

	public static void generateSitemap(Grant g) throws TrnSitemapGeneratorException {
		TrnSitemapGenerator smg = new TrnSitemapGenerator(g);
		
	}
	
	private void generateXML() {
		DataXML xml = new DataXML();
		xml.add("", "?xml version=\"1.0\" encoding=\"UTF-8\"?");
		AppLog.info(xml.toString(), g);
	}
}
