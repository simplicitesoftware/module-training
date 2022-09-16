package com.simplicite.extobjects.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;
import com.simplicite.webapp.services.RESTServiceExternalObject;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.objects.Training.TrnTag;
import org.json.*;

/**
 * External object TrnTagService
 */
public class TrnTagService extends RESTServiceExternalObject { 
	private static final long serialVersionUID = 1L;

	@Override
	public Object post(Parameters params) {
		String defaultLang = TrnTools.getLangs(g(), false)[0];//use first lang as default
		String lang = params.getParameter("lang", defaultLang);
		try {
			return getTags(lang);
		} catch(Exception e) {
			AppLog.error(getClass(), "post", e.getMessage(), e, g());
			return "Error"+e.getMessage();
		}
		
	}
	
	@Override
	public Object get(Parameters params) {
		return post(params);
	}
	
	private Grant g(){
		return getGrant();
	}
	
	private JSONArray getTags(String lang) {
		TrnTag trnTag = (TrnTag) g().getObject("tag_TrnTag", "TrnTag");
		JSONArray tagList = new JSONArray();
		synchronized(trnTag){
			trnTag.resetFilters();
			for(String[] row : trnTag.search()){
				trnTag.setValues(row);
				try{
					tagList.put(trnTag.getTagForFront(lang));
				} catch(Exception e){
					AppLog.error(getClass(), "getTags", "error getting front-oriented tag", e, g());
				}
			}
		}
		return tagList;
	}
}
