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
public class TrnTreeService extends RESTServiceExternalObject  {
	private static final long serialVersionUID = 1L;
	private static final boolean[] READ_ACCESS = {false,true,false,false};
	
	private String previousLesson = "";
	private ArrayList<JSONObject> lessonsNeedingNextPath = new ArrayList<>(); 
	private ArrayList<String> nextPaths = new ArrayList<>(); 

	@Override
	public Object post(Parameters params) {
		if(!"".equals(params.getParameter("getImages", "")))
			return getImages(params.getParameter("lessonId", "0"));
		if(!"".equals(params.getParameter("array", "")))
			return getTree2();
		else
			return getTree();
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
	
	private JSONObject getTree(){
		boolean[] oldcrudCat = getGrant().changeAccess("TrnCategory", READ_ACCESS);
		boolean[] oldcrudLsn = getGrant().changeAccess("TrnLesson", READ_ACCESS);
		
		ObjectDB tmpCategory = getGrant().getObject("tree_TrnCategory", "TrnCategory");
		ObjectDB tmpLesson = getGrant().getObject("tree_TrnLesson", "TrnLesson");
		
		JSONObject tree = getCategoriesRecursive("is null", tmpCategory, tmpLesson);
		
		getGrant().changeAccess("TrnCategory", oldcrudCat);
		getGrant().changeAccess("TrnLesson", oldcrudLsn);
		
		return tree;
	}
	
	private JSONArray getTree2(){
		boolean[] oldcrudCat = getGrant().changeAccess("TrnCategory", READ_ACCESS);
		boolean[] oldcrudLsn = getGrant().changeAccess("TrnLesson", READ_ACCESS);
		
		ObjectDB tmpCategory = getGrant().getObject("tree_TrnCategory", "TrnCategory");
		ObjectDB tmpLesson = getGrant().getObject("tree_TrnLesson", "TrnLesson");
		
		JSONArray tree = getCategoriesRecursive2("is null", tmpCategory, tmpLesson);
		
		getGrant().changeAccess("TrnCategory", oldcrudCat);
		getGrant().changeAccess("TrnLesson", oldcrudLsn);
	
	
		for(int i = 0; i < lessonsNeedingNextPath.size()-1; i++ )
			lessonsNeedingNextPath.get(i).put("trnLsnNext", nextPaths.get(i+1));
		
		return tree;
	}
	
	private JSONObject getCategoriesRecursive(String parentId, ObjectDB tmpCategory, ObjectDB tmpLesson){
		JSONObject cats = new JSONObject();
		for(JSONObject cat : getCategories(parentId, tmpCategory)){
			cat.put("categories", getCategoriesRecursive(cat.getString("row_id"), tmpCategory, tmpLesson));
			cat.put("lessons", getLessons(cat.getString("row_id"), tmpLesson));
			cats.put(cat.getString("trnCatPath"), cat);
		}
		return cats;
	}
	
	private JSONArray getCategoriesRecursive2(String parentId, ObjectDB tmpCategory, ObjectDB tmpLesson){
		JSONArray cats = new JSONArray();
		for(JSONObject cat : getCategories(parentId, tmpCategory)){
			cat.put("categories", getCategoriesRecursive2(cat.getString("row_id"), tmpCategory, tmpLesson));
			cat.put("lessons", getLessons2(cat.getString("row_id"), tmpLesson));
			cats.put(cat);
		}
		return cats;
	}
	
	private List<JSONObject> getCategories(String parentId, ObjectDB tmpCategory){
		List<JSONObject> catList = new ArrayList();
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
	
	private JSONObject getLessons(String categoryId, ObjectDB tmpLesson){
		JSONObject lessons = new JSONObject();
		JSONObject lsn;
		synchronized(tmpLesson){
			tmpLesson.resetFilters();
	        tmpLesson.setFieldFilter("trnLsnCatId", categoryId);
			List<String[]> rows = tmpLesson.search();
			for(int i=0; i<rows.size(); i++){
				tmpLesson.setValues(rows.get(i));
				lsn = new JSONObject(tmpLesson.toJSON());
	        	lsn.remove("trnLsnContent");
	        	lsn.remove("trnLsnHtmlContent");
	        	lsn.put("trnLsnPrevious", i!=0 ? rows.get(i-1)[tmpLesson.getFieldIndex("trnLsnPath")] : "");
	        	lsn.put("trnLsnNext", i!=rows.size()-1 ? rows.get(i+1)[tmpLesson.getFieldIndex("trnLsnPath")] : "");
	        	lessons.put(tmpLesson.getFieldValue("trnLsnPath"), lsn);
			}
		}
		return lessons;
	}
	
	private JSONArray getLessons2(String categoryId, ObjectDB tmpLesson){
		JSONArray lessons = new JSONArray();
		JSONObject lsn;
		synchronized(tmpLesson){
			tmpLesson.resetFilters();
	        tmpLesson.setFieldFilter("trnLsnCatId", categoryId);
			tmpLesson.setFieldFilter("trnLsnPublish", "1");
			List<String[]> rows = tmpLesson.search();
			for(int i=0; i<rows.size(); i++){
				tmpLesson.setValues(rows.get(i));
				AppLog.info(getClass(), "getLessons2", tmpLesson.getFieldValue("trnLsnTitle"), getGrant());
				lsn = new JSONObject(tmpLesson.toJSON());
	        	lsn.remove("trnLsnContent");
	        	lsn.remove("trnLsnHtmlContent");
	        	lsn.put("trnLsnPrevious", i!=0 ? rows.get(i-1)[tmpLesson.getFieldIndex("trnLsnPath")] : previousLesson);
	        	lsn.put("trnLsnNext", i!=rows.size()-1 ? rows.get(i+1)[tmpLesson.getFieldIndex("trnLsnPath")] : "");
	        	lessons.put(lsn);
	        	if(i == rows.size()-1){
	        		lessonsNeedingNextPath.add(lsn);
	        	}
	        	if(i == 0) {
	        		nextPaths.add(tmpLesson.getFieldValue("trnLsnPath"));
	        	}
			}
			if(rows.size() > 0) {
				previousLesson = rows.get(rows.size()-1)[tmpLesson.getFieldIndex("trnLsnPath")];
			}
		}
		return lessons;
	}
}

