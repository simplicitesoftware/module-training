package com.simplicite.extobjects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.objects.Training.TrnCategory;
import com.simplicite.objects.Training.TrnLesson;
import org.json.*;
import com.simplicite.webapp.services.RESTServiceExternalObject ;

/**
 * External object TrnExternalTreeView
 */
public class TrnExternalTreeView extends RESTServiceExternalObject  {
	private static final long serialVersionUID = 1L;

	/**
	 * Display method
	 * @param params Request parameters
	 */
	@Override
	public Object post(Parameters params) {
		JSONArray treeView = new JSONArray();
		JSONObject jsCat = new JSONObject();
		
		ObjectDB tmpCategory = getGrant().getObject("temporary", "TrnCategory");
		
		//1.Search for parentless categories 
        tmpCategory.resetFilters();
        tmpCategory.setFieldFilter("trnCatId", "is null");
        for(String[] row : tmpCategory.search()){
        	tmpCategory.setValues(row);
        	jsCat = new JSONObject(tmpCategory.toJSON());
        	attachChildrenCategories(jsCat);
        	attachRelatedLessons(jsCat);
        	treeView.put(jsCat);
	    }
	    
		return treeView;
	}
	
		@Override
	public Object get(Parameters params) {
		return post(params);
	}
	
	private void attachChildrenCategories(JSONObject jsCat){
		AppLog.log("DEBUG_CODE", TrnExternalTreeView.class, "attachChildrenCategories : " + jsCat.toString());
		
		JSONArray childrenCategories = new JSONArray();
		String categoryId = jsCat.getString("row_id");
		
		ObjectDB category = getGrant().getObject(categoryId, "TrnCategory");
		category.resetFilters();
        category.setFieldFilter("trnCatId", categoryId);
        
        for(String[] row : category.search()){
        	category.setValues(row);
        	JSONObject childCat = new JSONObject(category.toJSON());
        	attachRelatedLessons(childCat);
        	attachChildrenCategories(childCat);
        	childrenCategories.put(childCat);
        }
        
        if(childrenCategories.length() > 0){
        	AppLog.log("DEBUG_CODE", TrnExternalTreeView.class, "attachChildrenCategories : " + childrenCategories.length());
        	jsCat.put("categories", childrenCategories);
        }
	}
	
	private void attachRelatedLessons(JSONObject jsCat){
		JSONArray lessons = new JSONArray();
		String categoryId = jsCat.getString("row_id");
		
		ObjectDB tmpLesson = getGrant().getObject(categoryId, "TrnLesson");
		tmpLesson.resetFilters();
        tmpLesson.setFieldFilter("trnLsnCatId", categoryId);
		JSONObject lsn;
		List<String[]> relatedLessons = tmpLesson.search();
		for(int i=0; i<relatedLessons.size(); i++){
			tmpLesson.setValues(relatedLessons.get(i));
			lsn = new JSONObject(tmpLesson.toJSON());
        	lsn.remove("trnLsnContent");
        	lsn.remove("trnLsnHtmlContent");
        	lsn.put("trnLsnPrevious", i!=0 ? relatedLessons.get(i-1)[tmpLesson.getFieldIndex("trnLsnPath")] : "null");
        	lsn.put("trnLsnNext", i!=relatedLessons.size()-1 ? relatedLessons.get(i+1)[tmpLesson.getFieldIndex("trnLsnPath")] : "null");
        	lessons.put(lsn);
		}
	        
	    if(lessons.length() > 0){
	        jsCat.put("lessons", lessons);
	    }
	}
	
}

