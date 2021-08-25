package com.simplicite.objects.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import com.simplicite.commons.Training.*;
import org.jsoup.Jsoup;

/**
 * Business object TrnLsnTranslate
 */
public class TrnLsnTranslate extends TrnObject {
	private static final long serialVersionUID = 1L;
	
	@Override
	public String preSave() {
		String html = MarkdownTool.toHTML(getFieldValue("trnLtrContent"));
		setFieldValue("trnLtrHtmlContent", html);
		// Remove HTML tags (https://stackoverflow.com/a/9036849/1612642)
		setFieldValue("trnLtrRawContent", Jsoup.parse(html).text());
		return null;
	}
	
	@Override
	public String postSave() {
		TrnLesson lsn = (TrnLesson) getGrant().getTmpObject("TrnLesson");
		synchronized(lsn){
			lsn.resetFilters();
			lsn.select(getFieldValue("trnLtrLsnId"));
			lsn.index();
		}
		return null;
	}
}
