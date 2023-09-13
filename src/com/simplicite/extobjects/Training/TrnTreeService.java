package com.simplicite.extobjects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.objects.Training.TrnCategory;
import com.simplicite.objects.Training.TrnLesson;
import com.simplicite.objects.Training.TrnTagLsn;

import org.json.*;
import com.simplicite.webapp.services.RESTServiceExternalObject ;
import com.simplicite.commons.Training.TrnTools;

/**
 * External object TrnExternalTreeView
 */
public class TrnTreeService extends RESTServiceExternalObject  {
	private static final long serialVersionUID = 1L;
    private TrnLesson tmpLesson;
    private TrnCategory tmpCategory;
    private TrnTagLsn tagLsn;

	private String previousLesson = "";
	private ArrayList<JSONObject> lessonsNeedingNextPath = new ArrayList<>(); 
	private ArrayList<String> nextPaths = new ArrayList<>(); 

	@Override
	public Object post(Parameters params) {
		String defaultLang = TrnTools.getLangs(g(), false)[0];//use first lang as default
		String lang = params.getParameter("lang", defaultLang);
		try{
			if(!"".equals(params.getParameter("getImages", "")))
				return getImages(params.getParameter("lessonId", "0"));
			if(!"".equals(params.getParameter("getLesson", "")))
				return getLesson(params.getParameter("getLesson"), lang);
			if(!"".equals(params.getParameter("array", ""))) {
				JSONArray tags = params.getJSONObject().getJSONArray("tags");
				return getTree(lang, tags);
			}
			else
				return "getTree deprecated";
		}
		catch(Exception e){
            setHTTPStatus(500);
			AppLog.error(getClass(), "post", e.getMessage(), e, g());
			return "ERROR "+e.getMessage();
		}
	}
	
	@Override
	public Object get(Parameters params) {
		return post(params);
	}
	
	private Grant g(){
		return getGrant();
	}
	
	private JSONArray getImages(String lessonId){
		JSONArray arr = new JSONArray();
		for(String[] row : g().query("select row_id, trn_pic_image from trn_picture where trn_pic_lsn_id="+Tool.toSQL(lessonId)))
			arr.put(new JSONObject("{row_id:'"+row[0]+"', trnPicImage:'"+row[1]+"'}"));
		return arr; 
	}
	
	private JSONObject getLesson(String lessonId, String lang) throws Exception{
        tmpLesson = (TrnLesson) g().getObject("tree_TrnLesson", "TrnLesson");
		synchronized(tmpLesson){
			tmpLesson.resetFilters();
			if(!tmpLesson.select(lessonId))
				throw new Exception("Lesson "+lessonId+" not found with user "+g().getLogin());
			return tmpLesson.getLessonJSON(lang, true);
		}
	}

	private JSONArray getTree(String lang, JSONArray tags){
		tmpCategory = (TrnCategory) g().getObject("tree_TrnCategory", "TrnCategory");
        tmpLesson = (TrnLesson) g().getObject("tree_TrnLesson", "TrnLesson");
        tagLsn = (TrnTagLsn) g().getObject("tree_TrnTagLsn", "TrnTagLsn");
		JSONArray tree = getCategoriesRecursive("is null", tags, lang);
	
		for(int i = 0; i < lessonsNeedingNextPath.size()-1; i++ )
			lessonsNeedingNextPath.get(i).put("next_path", nextPaths.get(i+1));
		
		return tree;
	}
	
	private JSONArray getCategoriesRecursive(String parentId, JSONArray tags, String lang){
		JSONArray cats = new JSONArray();
		for(JSONObject cat : getCategories(parentId, lang)){
            JSONArray items = new JSONArray();
            items.putAll(getCategoriesRecursive(cat.getString("row_id"), tags, lang));
            items.putAll(getLessons(cat.getString("row_id"), tagLsn, lang, tags));
            cat.put("items", orderMenuItems(items));
			if(cat.getJSONArray("items").length() == 0) continue;
			cats.put(cat);
		}
        return cats;
	}

    private List<JSONObject> convertJSONArrayToList(JSONArray json) {
        List<JSONObject> list = new ArrayList<JSONObject>();
        for (int i=0; i<json.length(); i++) {
            list.add(json.getJSONObject(i));
        }
        return list;
    }

    private JSONArray orderMenuItems(JSONArray items) {
        List<JSONObject> list = convertJSONArrayToList(items);
        Collections.sort(list, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject item1, JSONObject item2) {
                String val1 = new String();
                String val2 = new String();
                try {
                    val1 = item1.getString("order");
                    val2 = item2.getString("order");
                } catch(JSONException e) {
                    AppLog.error(getClass(), "orderMenuItems", "JSON error. Cannot order menu items", e, getGrant());
                }
                return val1.compareTo(val2);
            }
        });

        JSONArray orderedArray = new JSONArray();
        for(int i = 0; i < items.length(); i++) {
            orderedArray.put(list.get(i));
            if("/docs".equals(list.get(i).getString("path"))) {
                String test = "";
            }
        }
        return orderedArray;
    }
	
	private List<JSONObject> getCategories(String parentId, String lang){
		List<JSONObject> catList = new ArrayList<>();
		synchronized(tmpCategory){
			tmpCategory.resetFilters();
	    	tmpCategory.setFieldFilter("trnCatId", parentId);
	  		tmpCategory.setFieldFilter("trnCatPublish", "1");
			for(String[] row : tmpCategory.search()){
				tmpCategory.setValues(row);
				try{
					catList.add(tmpCategory.getCategoryForFront(lang));
				} catch(Exception e){
					AppLog.error(getClass(), "getCategories", "error getting front-oriented cat", e, g());
				}
			}
		}
		return catList;
	}
	
	private JSONArray getLessons(String categoryId, TrnTagLsn tagLsn, String lang, JSONArray tags){
		JSONArray lessons = new JSONArray();
		JSONObject lsn;
		
		int indexPath = tmpLesson.getFieldIndex("trnLsnFrontPath");
		
		synchronized(tmpLesson){
		tmpLesson.resetFilters();
	    tmpLesson.setFieldFilter("trnLsnCatId", categoryId);
		tmpLesson.setFieldFilter("trnLsnPublish", "1");
		List<String[]> rows = tmpLesson.search();
		for(int i=0; i<rows.size(); i++){
			tmpLesson.setValues(rows.get(i));
			try{
				lsn = tmpLesson.getLessonJSON(lang, false);
				lsn.put("previous_path", i==0 ? previousLesson : rows.get(i-1)[indexPath]);
				lsn.put("next_path", i==rows.size()-1 ? "" : rows.get(i+1)[indexPath]);
				boolean addLessonFlag = true;
				if(tags.length() > 0 && !tagLsn.lessonHasTag(rows.get(i)[0], tags)) addLessonFlag = false; 
				if(addLessonFlag) lessons.put(lsn);
				if(i == rows.size()-1) lessonsNeedingNextPath.add(lsn);
				if(i == 0) nextPaths.add(tmpLesson.getFieldValue("trnLsnFrontPath"));
			} catch(Exception e){
				AppLog.error(getClass(), "getLessons2", "error getting front-oriented cat", e, g());
			}
		}
		if(!rows.isEmpty()) 
			previousLesson = rows.get(rows.size()-1)[indexPath];
		}
		return lessons;
	}
}