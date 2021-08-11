package com.simplicite.objects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.*;

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
	
}
