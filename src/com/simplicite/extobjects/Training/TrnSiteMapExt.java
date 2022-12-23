package com.simplicite.extobjects.Training;

import java.util.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;

/**
 * External object TrnSiteMapExt
 */
public class TrnSiteMapExt extends ExternalObject { 
	
	private static final long serialVersionUID = 1L;
	
	@Override
    public Object display(Parameters params) {
    	setDecoration(false);
        setMIMEType(HTTPTool.MIME_TYPE_TXT);
        setContentDisposition("attachment", "sitemap.txt");
        byte[] file = getSiteMap().getBytes();
        return file;
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

    /*private FileWriter getOutputStream() {
        try (FileWriter out = new FileWriter(new File("sitemap.txt"))) {
			out.write(getSiteMap());
			return out;
		} catch(java.io.IOException e) {
			AppLog.error(e, getGrant());
		}
        return null;
    }*/
}
