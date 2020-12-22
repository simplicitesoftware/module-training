package com.simplicite.objects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.TrnTools;

/**
 * Business object TrnCategory
 */
public class TrnCategory extends ObjectDB {
	private static final long serialVersionUID = 1L;
	
	@Override
	public List<String> postValidate() {
		List<String> msgs = new ArrayList<String>();
		
		if (getFieldValue("trnCatId").equals(getRowId()) || getFieldValue("trnCatId.trnCatPath").contains(getFieldValue("trnCatPath")))
			msgs.add(Message.formatError("ERROR_CODE", "A category cannot be referenced as one of its ancestors", "trnCatId"));
			
		return msgs;
	}
	
	@Override
	public String preSave() {
		if(!isSyncInstance())
			setFieldValue("trnCatPath", getPath());
		setFieldValue("trnCatFrontPath", TrnTools.path2Front(getFieldValue("trnCatPath")));
		return null;
	}
	
	private String getPath(){
		ObjectField parent = getField("trnCatId.trnCatPath");
		String parentPath = parent.isEmpty() ? "" : parent.getValue() + "/";
		return parentPath+"CTG_"+getFieldValue("trnCatOrder")+"_"+TrnTools.toSnake(getFieldValue("trnCatTitle"));
	}
	
	@Override
	public String postSave() {
		updateChildren();
		return null;
	}

	private void updateChildren(){
		ObjectDB recursive_TrnCategory = getGrant().getObject("recursive_TrnCategory", "TrnCategory");
        recursive_TrnCategory.resetFilters();
        recursive_TrnCategory.setFieldFilter("trnCatId", this.getRowId());
        
        for(String[] row : recursive_TrnCategory.search()){
        	recursive_TrnCategory.setValues(row);
        	recursive_TrnCategory.save();
        }
        
        ObjectDB recursive_TrnLesson = getGrant().getObject("recursive_TrnLesson", "TrnLesson");
		recursive_TrnLesson.resetFilters();
		recursive_TrnLesson.setFieldFilter("trnLsnCatId", this.getRowId());
		for(String[] row : recursive_TrnLesson.search()){
			recursive_TrnLesson.setValues(row);
        	recursive_TrnLesson.save();
		}
	}
	
	private boolean isSyncInstance(){
		return "sync_TrnCategory".equals(getInstanceName());
	}
	
}
