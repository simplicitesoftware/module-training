package com.simplicite.objects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.*;
import org.json.JSONObject;

/**
 * Business object TrnCategory
 */
public class TrnCategory extends TrnObject {
	private static final long serialVersionUID = 1L;
	
	@Override
	public List<String> postValidate() {
		List<String> msgs = new ArrayList<String>();
		
		if(!isSyncInstance())
			setFieldValue("trnCatPath", getPath());
		
		setFieldValue("trnCatFrontPath", TrnTools.path2Front(getFieldValue("trnCatPath")));
		
		if(getFieldValue("trnCatId.trnCatPath").contains(getFieldValue("trnCatPath")))
			msgs.add(Message.formatError("TRN_CAT_LOOP", "A category cannot be referenced as one of its ancestors", "trnCatId"));
			
		return msgs;
	}

	@Override
	public void postLoad() {
		super.postLoad();
		if(getGrant().hasResponsibility("TRN_READ")){
			setDefaultSearchSpec("t.trn_cat_publish='1'");
		}
	}
	
	@Override
	public String getUserKeyLabel(String[] row) {
		return getFieldValue("trnCatFrontPath", row);
	}
	
	private String getPath(){
		ObjectField parent = getField("trnCatId.trnCatPath");
		String parentPath = parent.isEmpty() ? "" : parent.getValue() + "/";
		return parentPath+"CTG_"+getFieldValue("trnCatOrder")+"_"+TrnTools.toSnake(getFieldValue("trnCatCode"));
	}
	
	@Override
	public String postSave() {
		try{
			updateChildren();
		} catch(Exception e){
			AppLog.error(getClass(), "postSave", "Error updating children", e, getGrant());
		}
		return null;
	}

	private void updateChildren() throws ValidateException, SaveException{
		BusinessObjectTool bot;
		
		ObjectDB recursive_TrnCategory = getGrant().getObject("updateChildren_TrnCategory_"+getRowId(), "TrnCategory");
		synchronized(recursive_TrnCategory){
	        recursive_TrnCategory.resetFilters();
	        recursive_TrnCategory.setFieldFilter("trnCatId", getRowId());
	        bot = new BusinessObjectTool(recursive_TrnCategory);
	        
	        for(String[] row : recursive_TrnCategory.search()){
	        	recursive_TrnCategory.setValues(row);
	        	bot.validateAndSave(); // Triggers recursion !!!!
	        }
		}
        
        ObjectDB recursive_TrnLesson = getGrant().getObject("updateChildren_TrnLesson", "TrnLesson");
        synchronized(recursive_TrnLesson){
			recursive_TrnLesson.resetFilters();
			recursive_TrnLesson.setFieldFilter("trnLsnCatId", getRowId());
			bot = new BusinessObjectTool(recursive_TrnLesson);
			
			for(String[] row : recursive_TrnLesson.search()){
				recursive_TrnLesson.setValues(row);
	        	bot.validateAndSave();
			}
        }
	}
	
	public JSONObject getCategoryForFront(String lang) throws Exception{
		JSONObject json = new JSONObject()
			.put("row_id", getRowId())
			.put("path", getFieldValue("trnCatFrontPath"))
			.put("is_category", true);
			
		ObjectDB content = getGrant().getTmpObject("TrnCategoryTranslate");
		synchronized(content){
			content.resetFilters();
			content.setFieldFilter("trnCtrCatId", getRowId());
			content.setFieldFilter("trnCtrLang", lang);
			if(content.getCount()==0)
				content.setFieldFilter("trnCtrLang", "ANY");
			
			if(content.getCount()!=1)
				throw new Exception("CAT_CONTENT_NOT_FOUND");
			else{
				content.setValues(content.search().get(0));
				json.put("title", content.getFieldValue("trnCtrTitle"));
			}
		}
		return json;
	}

	public Boolean categoryHasAtLeastOneLesson(String categoryId, String tagId) {
		String res = getGrant().simpleQuery("select COUNT(*) FROM trn_lesson as lesson INNER JOIN trn_tag_lsn as ttl ON lesson.row_id = ttl.trn_taglsn_lsn_id where ttl.trn_taglsn_tag_id ='"+ tagId +"' AND lesson.trn_lsn_cat_id ='"+categoryId+"'");
		if(Integer.parseInt(res) > 0) return true;
		return false;
	}
	
	@Override
	public boolean isActionEnable(String[] row, String action) {
		//deactivate sync action if ui mode
		if(TrnTools.isUiMode() && "forceDirSync".equals(action))
			return false;
		return true;
	}
	
	public void forceDirSync(){
		try {
			TrnFsSyncTool.triggerSync();
		} catch (TrnFsSyncTool.TrnSyncException e) {
			AppLog.error(getClass(), "forceDirSync", e.getMessage(), e, Grant.getSystemAdmin());
		}
	}
	
	public void reIndexAll(){
		try{
			//TrnIndexer.forceIndex(getGrant());
			TrnIndexer.forceIndex((getGrant()));
		}
		catch(Exception e){
			AppLog.error(getClass(), "reIndexAll", e.getMessage(), e, Grant.getSystemAdmin());
		}
	}
}
