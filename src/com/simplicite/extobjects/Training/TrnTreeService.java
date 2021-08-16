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
		String lang = params.getParameter("lang", "ENU");
		
		if(!"".equals(params.getParameter("getImages", "")))
			return getImages(params.getParameter("lessonId", "0"));
		if(!"".equals(params.getParameter("array", "")))
			return getTree(lang);
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

	private JSONArray getTree(String lang){
		boolean[] oldcrudCat = getGrant().changeAccess("TrnCategory", READ_ACCESS);
		boolean[] oldcrudCatTsl = getGrant().changeAccess("TrnCategoryTranslate", READ_ACCESS);
		boolean[] oldcrudLsn = getGrant().changeAccess("TrnLesson", READ_ACCESS);
		boolean[] oldcrudLsnTsl = getGrant().changeAccess("TrnLsnTranslate", READ_ACCESS);
		
		TrnCategory tmpCategory = (TrnCategory) getGrant().getObject("tree_TrnCategory", "TrnCategory");
		TrnLesson tmpLesson = (TrnLesson) getGrant().getObject("tree_TrnLesson", "TrnLesson");
		
		JSONArray tree = getCategoriesRecursive("is null", tmpCategory, tmpLesson, lang);
		
		getGrant().changeAccess("TrnCategory", oldcrudCat);
		getGrant().changeAccess("TrnCategoryTranslate", oldcrudCatTsl);
		getGrant().changeAccess("TrnLesson", oldcrudLsn);
		getGrant().changeAccess("TrnLsnTranslate", oldcrudLsnTsl);
	
	
		for(int i = 0; i < lessonsNeedingNextPath.size()-1; i++ )
			lessonsNeedingNextPath.get(i).put("next_path", nextPaths.get(i+1));
		
		return tree;
	}
	
	private JSONArray getCategoriesRecursive(String parentId, TrnCategory tmpCategory, TrnLesson tmpLesson, String lang){
		JSONArray cats = new JSONArray();
		for(JSONObject cat : getCategories(parentId, tmpCategory, lang)){
			cat.put("categories", getCategoriesRecursive(cat.getString("row_id"), tmpCategory, tmpLesson, lang));
			cat.put("lessons", getLessons(cat.getString("row_id"), tmpLesson, lang));
			cats.put(cat);
		}
		return cats;
	}
	
	private List<JSONObject> getCategories(String parentId, TrnCategory tmpCategory, String lang){
		List<JSONObject> catList = new ArrayList();
		JSONObject cat;
		synchronized(tmpCategory){
			tmpCategory.resetFilters();
			tmpCategory.setFieldOrder("trnCatOrder", 1);
	        tmpCategory.setFieldFilter("trnCatId", parentId);
	        tmpCategory.setFieldFilter("trnCatPublish", "1");
	        for(String[] row : tmpCategory.search()){
	        	tmpCategory.setValues(row);
	        	try{
	        		catList.add(tmpCategory.getCategoryForFront(lang));
	        	} catch(Exception e){
	        		AppLog.error(getClass(), "getCategories", "error getting front-oriented cat", e, getGrant());
	        	}
	        }
		}
		return catList;
	}
	
	private JSONArray getLessons(String categoryId, TrnLesson tmpLesson, String lang){
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
				try{
					lsn = tmpLesson.getLessonForFront(lang, false);
					lsn.put("previous_path", i==0 ? previousLesson : rows.get(i-1)[INDEX_PATH]);
					lsn.put("next_path", i==rows.size()-1 ? "" : rows.get(i+1)[INDEX_PATH]);
					
					lessons.put(lsn);
	
		        	if(i == rows.size()-1)
		        		lessonsNeedingNextPath.add(lsn);
		        	if(i == 0)
		        		nextPaths.add(tmpLesson.getFieldValue("trnLsnFrontPath"));
				} catch(Exception e){
	        		AppLog.error(getClass(), "getLessons2", "error getting front-oriented cat", e, getGrant());
	        	}
			}
			if(rows.size() > 0) 
				previousLesson = rows.get(rows.size()-1)[INDEX_PATH];
		}
		return lessons;
	}
}

