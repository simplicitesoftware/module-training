package com.simplicite.extobjects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.objects.Training.TrnCategory;
import com.simplicite.objects.Training.TrnLesson;
import org.json.*;
import com.simplicite.webapp.services.RESTServiceExternalObject ;
import com.simplicite.commons.Training.TrnTools;

/**
 * External object TrnExternalTreeView
 */
public class TrnTreeService extends RESTServiceExternalObject  {
	private static final long serialVersionUID = 1L;
	private static final boolean[] READ_ACCESS = {false,true,false,false};
	
	private String previousLesson = "";
	private ArrayList<JSONObject> lessonsNeedingNextPath = new ArrayList<>(); 
	private ArrayList<String> nextPaths = new ArrayList<>(); 

	@Override
	public Object post(Parameters params) {
		String lang = params.getParameter("array", "ENU");
		
		if(!"".equals(params.getParameter("getImages", "")))
			return getImages(params.getParameter("lessonId", "0"));
		if(!"".equals(params.getParameter("array", "")))
			return getTree2(lang);
		else
			return "getTree deprecated";
	}
	
	@Override
	public Object get(Parameters params) {
		return post(params);
	}
	
	private JSONArray getImages(String lessonId){
		JSONArray arr = new JSONArray();
		for(String[] row : getGrant().query("select row_id, trn_pic_image from trn_picture where trn_pic_lsn_id="+Tool.toSQL(lessonId)))
			arr.put(new JSONObject("{row_id:'"+row[0]+"', trnPicImage:'"+row[1]+"'}"));
		return arr; 
	}

	private JSONArray getTree2(String lang){
		boolean[] oldcrudCat = getGrant().changeAccess("TrnCategory", READ_ACCESS);
		boolean[] oldcrudLsn = getGrant().changeAccess("TrnLesson", READ_ACCESS);
		
		ObjectDB tmpCategory = getGrant().getObject("tree_TrnCategory", "TrnCategory");
		ObjectDB tmpLesson = getGrant().getObject("tree_TrnLesson", "TrnLesson");
		
		JSONArray tree = getCategoriesRecursive2("is null", tmpCategory, tmpLesson, lang);
		
		getGrant().changeAccess("TrnCategory", oldcrudCat);
		getGrant().changeAccess("TrnLesson", oldcrudLsn);
	
	
		for(int i = 0; i < lessonsNeedingNextPath.size()-1; i++ )
			lessonsNeedingNextPath.get(i).put("trnLsnNext", nextPaths.get(i+1));
		
		return tree;
	}
	
	private JSONArray getCategoriesRecursive2(String parentId, ObjectDB tmpCategory, ObjectDB tmpLesson, String lang){
		JSONArray cats = new JSONArray();
		for(JSONObject cat : getCategories(parentId, tmpCategory, lang)){
			cat.put("categories", getCategoriesRecursive2(cat.getString("row_id"), tmpCategory, tmpLesson, lang));
			cat.put("lessons", getLessons2(cat.getString("row_id"), tmpLesson, lang));
			cats.put(cat);
		}
		return cats;
	}
	
	private List<JSONObject> getCategories(String parentId, ObjectDB tmpCategory, String lang){
		List<JSONObject> catList = new ArrayList();
		JSONObject cat;
		synchronized(tmpCategory){
			tmpCategory.resetFilters();
			tmpCategory.setFieldOrder("trnCatOrder", 1);
	        tmpCategory.setFieldFilter("trnCatId", parentId);
	        tmpCategory.setFieldFilter("trnCatPublish", "1");
	        for(String[] row : tmpCategory.search()){
	        	tmpCategory.setValues(row);
	        	catList.add(new JSONObject(tmpCategory.toJSON()));
	        }
		}
		return catList;
	}
	
	private JSONArray getLessons2(String categoryId, ObjectDB tmpLesson, String lang){
		JSONArray lessons = new JSONArray();
		JSONObject lsn;
		String path, next, prev;
		
		int INDEX_PATH = tmpLesson.getFieldIndex("trnLsnFrontPath");
		
		synchronized(tmpLesson){
			tmpLesson.resetFilters();
	        tmpLesson.setFieldFilter("trnLsnCatId", categoryId);
			tmpLesson.setFieldFilter("trnLsnPublish", "1");
			List<String[]> rows = tmpLesson.search();
			for(int i=0; i<rows.size(); i++){
				tmpLesson.setValues(rows.get(i));
				
				lsn = lesson2Front(
					new JSONObject(tmpLesson.toJSON()),
					i==0 ? previousLesson : rows.get(i-1)[INDEX_PATH],
					rows.get(i)[INDEX_PATH],
					i==rows.size()-1 ? "" : rows.get(i+1)[INDEX_PATH]
				);
				
				lessons.put(lsn);

	        	if(i == rows.size()-1)
	        		lessonsNeedingNextPath.add(lsn);
	        	if(i == 0)
	        		nextPaths.add(tmpLesson.getFieldValue("trnLsnFrontPath"));
			}
			if(rows.size() > 0) 
				previousLesson = rows.get(rows.size()-1)[INDEX_PATH];
		}
		return lessons;
	}
	
	private JSONObject category2Front(JSONObject cat){
		return cat;
	}
	
	private JSONObject lesson2Front(JSONObject lsn, String previousPath, String path, String nextPath){
       	lsn.remove("trnLsnContent");
    	lsn.remove("trnLsnHtmlContent");
		lsn.put("trnLsnPrevious", previousPath);
		lsn.put("trnLsnNext", nextPath);
		return lsn;
	}
}

