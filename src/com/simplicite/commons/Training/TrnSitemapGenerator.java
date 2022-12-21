package com.simplicite.commons.Training;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;
import com.simplicite.util.tools.HTTPTool;
import com.simplicite.util.tools.APITool;

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

	public static String generateSitemap(Grant g) throws TrnSitemapGeneratorException {
		TrnSitemapGenerator smg = new TrnSitemapGenerator(g);
		return smg.generateContent();
	}
	
	private String generateContent() {
		try {
			Path temp = Files.createTempFile("sitemap", ".txt");
			String fileContent = "";
			// front base url
			String baseUrl = g.getContextURL() + "/";
			fileContent += baseUrl + "\n";

			ObjectDB category = g.getTmpObject("TrnCategory");
			category.resetFilters();
			category.setFieldFilter("trnCatPublish", true);

			ObjectDB lesson = g.getTmpObject("TrnLesson");
			lesson.resetFilters();
			lesson.setFieldFilter("trnLsnPublish", true);
			
			// loop on published category and lesson
			for (String[] cat : category.search()) {
				fileContent += baseUrl + "category" + cat[5] + "\n";
				lesson.setFieldFilter("trnLsnCatId", cat[0]);
				for(String[] lsn : lesson.search()) {
					fileContent += baseUrl + "lesson" + lsn[6] + "\n";
				}
			}

			ObjectDB page = g.getTmpObject("TrnPage");
			page.resetFilters();
			page.setFieldFilter("trnLsnPublish", true);
			for (String[] p : page.search()) {
				fileContent += baseUrl + "page" + p[5] + "\n";
			}
			AppLog.info("Sitemap created at " + temp, g);	
			return fileContent;
		} catch(java.io.IOException e) {
			AppLog.error(e, g);
		}
		return "";
	}
}
