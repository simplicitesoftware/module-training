package com.simplicite.objects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.*;

/**
 * Business object TrnLsnTranslate
 */
public class TrnLsnTranslate extends TrnObject {
	private static final long serialVersionUID = 1L;
	
	@Override
	public String preSave() {
		setFieldValue("trnLtrHtmlContent", MarkdownTool.toHTML(getFieldValue("trnLtrContent")));
		return null;
	}
}
