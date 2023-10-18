package com.simplicite.objects.Training;

import com.simplicite.util.tools.MarkdownTool;
import com.simplicite.commons.Training.*;
import org.jsoup.Jsoup;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.simplicite.util.*;

/**
 * Business object TrnLsnTranslate
 */
public class TrnLsnTranslate extends TrnObject {
	private static final long serialVersionUID = 1L;
	private static final Pattern PATTERN_LINEAR_PICS = Pattern.compile("src\\s*=\\s*\\\"(.+?)\\\"");
	private static final Pattern PATTERN_BLOCKQUOTE = Pattern.compile("<blockquote>\n<p><strong>([A-Za-z]+)");

	@Override
	public String preSave() {
		String md = getFieldValue("trnLtrContent");
		if(!md.isEmpty()) {
			String html = MarkdownTool.toHTML(md);

			TrnLesson lsn = (TrnLesson) getGrant().getTmpObject("TrnLesson");
			lsn.resetFilters();
			String rowId = getFieldValue("trnLtrLsnId");
			String lang = getFieldValue("trnLtrLang");
			lsn.select(getFieldValue("trnLtrLsnId"));
			// if LINEAR, then change content images link, might want to limit this to
			// filesystem mode
			try {
				if (lsn.getFieldValue("trnLsnVisualization").equals("LINEAR")) {
					String title = lsn.getFieldValue("trnLsnCode");
					html = setLinearPictureContent(html, lsn.getRowId());
					html = setBlockquoteType(html);
				}
			} catch (Exception e) {
				AppLog.error(getClass(), "preSave", "An error occured during the parsing of linear content pictures", e,
						getGrant());
				return e.getMessage();
			}

			setFieldValue("trnLtrHtmlContent", html);

			// Remove HTML tags (https://stackoverflow.com/a/9036849/1612642)
			setFieldValue("trnLtrRawContent", Jsoup.parse(html).text());
		}
		return null;		
	}

	@Override
	public String postSave() {
		TrnLesson lsn = (TrnLesson) getGrant().getTmpObject("TrnLesson");
		synchronized (lsn) {
			lsn.resetFilters();
			lsn.select(getFieldValue("trnLtrLsnId"));
			lsn.index();
		}
		return null;
	}

	@Override
	public List<String[]> postSearch(List<String[]> rows) {
		for (String[] row : rows)
			row[getFieldIndex("trnLtrHasContent")] = "" + !Tool.isEmpty(row[getFieldIndex("trnLtrRawContent")]);
		return rows;
	}

	// When fetching a linear lesson, converts pictures old urls to current
	private String setLinearPictureContent(String htmlContent, String lsnRowId) throws Exception {
		try {
			Matcher m = PATTERN_LINEAR_PICS.matcher(htmlContent);
			ObjectDB picObject = getGrant().getTmpObject("TrnPicture");
			picObject.resetFilters();
			picObject.setFieldFilter("trnPicLsnId", lsnRowId);
			List<String[]> pics = picObject.search();
			// rebuilding string with StringBuilder
			StringBuilder out = new StringBuilder();
			int nextIndex = 0;
			while (m.find()) {
				String imgPath = m.group(1);
				for (String[] pic : pics) {
					// optimiser en cherchant les documents par doc name ?
					// ou possible de fetch la liste des docs correspondants aux images de la leçon
					// ?
					picObject.setValues(pic);
					String docId = picObject.getFieldValue("trnPicImage");
					DocumentDB doc = DocumentDB.getDocument(docId, getGrant());
					String frontUrl = getGrant().getContextURL() + doc.getURL("").replace("/ui", "");
					String docName = doc.getName();
					if (imgPath.contains(docName)) {
						// append the content of html from the nextIndex (beginning of string to copy)
						// to start index (end of string to copy)
						// in a nutshell add all the htmlContent before the match (match not included)
						int startIndex = m.start();
						out.append(htmlContent, nextIndex, startIndex)
								// then append the new Url
								.append("src=\"" + frontUrl + "\"");
						nextIndex = m.end();
						break;
					}
				}
			}
			if (nextIndex < htmlContent.length()) {
				out.append(htmlContent, nextIndex, htmlContent.length());
			}
			return out.toString();
		} catch (Exception e) {
			throw new Exception("An error occured while parsing linear picture content: " + e.getMessage());
		}
	}

	private static String setBlockquoteType(String html) {
		StringBuilder out = new StringBuilder();
		Matcher m = PATTERN_BLOCKQUOTE.matcher(html);
		int nextIndex = 0;
		while(m.find()) {
			String className = m.group(1).toLowerCase(Locale.ROOT);
			out.append(html, nextIndex, m.start())
				.append("<blockquote class=\""+className+"\"><p><strong>"+m.group(1));
			nextIndex = m.end();
		}
		if (nextIndex < html.length()) {
			out.append(html, nextIndex, html.length());
		}
		return out.toString();
	}
}