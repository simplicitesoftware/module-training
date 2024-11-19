package com.simplicite.extobjects.Training;

import com.simplicite.util.*;
import com.simplicite.util.tools.*;

/**
 * External object TrnSitemap
 */
public class TrnSitemap extends ExternalObject { 
	
	private static final long serialVersionUID = 1L;
	
	@Override
    public Object display(Parameters params) {
    	setDecoration(false);
        setMIMEType(HTTPTool.MIME_TYPE_TXT);
        setContentDisposition("attachment", "sitemap.txt");
        return getSiteMap().getBytes();
    }
    
    private String getSiteMap(){
    	Grant g = getGrant();
		String fileContent = "";
		// front base url
		String baseUrl = getGrant().getContextURL() + "/";
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
		return fileContent;
    }
}