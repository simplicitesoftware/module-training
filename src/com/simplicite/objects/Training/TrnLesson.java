package com.simplicite.objects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.*;

/**
 * Business object TrnLesson
 */
public class TrnLesson extends TrnObject {
	private static final long serialVersionUID = 1L;

	@Override
	public String getUserKeyLabel(String[] row) {
		return getFieldValue("trnLsnFrontPath", row);
	}

	@Override
	public List<String> postValidate() {
		if(!isSyncInstance())
			setFieldValue("trnLsnPath", getPath());
		
		setFieldValue("trnLsnFrontPath", TrnTools.path2Front(getFieldValue("trnLsnPath")));
		
		return null;
	}

	private String getPath(){
		return getFieldValue("trnLsnCatId.trnCatPath")+"/"+"LSN_"+getFieldValue("trnLsnOrder")+"_"+TrnTools.toSnake(getFieldValue("trnLsnCode"));
	}
}
