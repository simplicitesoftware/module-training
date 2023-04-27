package com.simplicite.objects.Training;

import com.simplicite.util.tools.MarkdownTool;
import com.simplicite.commons.Training.*;
import org.jsoup.Jsoup;
import java.util.*;
import com.simplicite.util.*;

/**
 * Business object TrnLsnTranslate
 */
public class TrnLsnTranslate extends TrnObject {
	private static final long serialVersionUID = 1L;
	
	@Override
	public String preSave() {
		String md = getFieldValue("trnLtrContent");
		String html = MarkdownTool.toHTML(md);

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
	
	@Override
	public List<String[]> postSearch(List<String[]> rows) {
		for(String[] row : rows)
			row[getFieldIndex("trnLtrHasContent")] = ""+!Tool.isEmpty(row[getFieldIndex("trnLtrRawContent")]);
		return rows;
	}
}
