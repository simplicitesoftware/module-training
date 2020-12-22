package com.simplicite.objects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.TrnTools;

/**
 * Business object TrnLesson
 */
public class TrnLesson extends ObjectDB {
	private static final long serialVersionUID = 1L;
	
	@Override
	public String preSave() {
		setFieldValue("trnLsnHtmlContent", MarkdownTool.toHTML(getFieldValue("trnLsnContent")));
		
		if(!isSyncInstance()){
			ObjectDB category = getGrant().getTmpObject("TrnCategory");
			synchronized(category){
				category.select(getFieldValue("trnLsnCatId"));
				setFieldValue("trnLsnPath", category.getFieldValue("trnCatPath") + "/" + TrnTools.toSnake(getFieldValue("trnLsnTitle")));
			}
		}
		setFieldValue("trnLsnFrontPath", TrnTools.path2Front(getFieldValue("trnLsnPath")));
		return null;
	}
	
	private boolean isSyncInstance(){
		return "sync_TrnLesson".equals(getInstanceName());
	}
}
