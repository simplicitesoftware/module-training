package com.simplicite.objects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.*;

/**
 * Business object TrnPicture
 */
public class TrnPicture extends TrnObject {
	private static final long serialVersionUID = 1L;

	@Override
	public List<String> preValidate() {
		List<String> msgs = new ArrayList<>();
		ObjectField f = getField("trnPicId");
		AppLog.info("testtestest", getGrant());
		if (isNew()||isCopied()) {
			AppLog.info(getTable() + " : " + f.getDBName(), getGrant());
			AppLog.info(getGrant().getNextIdForColumn(getTable(),f.getDBName()), getGrant());
			f.setValue(getGrant().getNextIdForColumn(getTable(),f.getDBName()));
		}
		return msgs;
	}
	
}
