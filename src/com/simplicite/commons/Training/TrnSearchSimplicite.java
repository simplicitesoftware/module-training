package com.simplicite.commons.Training;

import java.util.*;

import org.json.JSONObject;

import com.simplicite.objects.Training.TrnLsnTranslate;
import com.simplicite.util.Grant;
import com.simplicite.util.tools.IndexCore;
import com.simplicite.util.tools.SearchItem;

/**
 * Shared code TrnSearchSimplicite
 */
public class TrnSearchSimplicite implements java.io.Serializable {
	private static final long serialVersionUID = 1L; 
    private static final int maxResult = 30;
    private static int contentMaxLength = 500;

	public static ArrayList<JSONObject> search(String query, String lang, Grant g) throws Exception {
        return getFilteredResults(query, lang, g);
    }

    private static ArrayList<JSONObject> getFilteredResults(String query, String lang, Grant g) throws Exception {
        List<SearchItem> searchResults = getSearchResults(query, g);
        TrnLsnTranslate lsnTranslate = (TrnLsnTranslate) g.getTmpObject("TrnLsnTranslate");
        ArrayList<JSONObject> filteredResults =  new ArrayList<>();
        for(SearchItem item : searchResults) {
            lsnTranslate.setValues(item.values);
            String translateLang = lsnTranslate.getFieldValue("trnLtrLang");
            String rawContent = lsnTranslate.getFieldValue("trnLtrRawContent");
            if(lang.equals(translateLang) || "ANY".equals(translateLang) && !rawContent.isEmpty()) {
                filteredResults.add(formatResult(lsnTranslate, query));
            }
            if(filteredResults.size() >= maxResult) {
                break;
            }
        }
        return filteredResults;
    }

    private static List<SearchItem> getSearchResults(String query, Grant g) throws Exception {
        List<SearchItem> searchResults = IndexCore.searchIndex(g, query, Arrays.asList("TrnLsnTranslate"));
        return searchResults;
    }

    private static JSONObject formatResult(TrnLsnTranslate lsnTranslate, String query) {
        JSONObject json = new JSONObject();
        String title = TrnTools.highlightContent(lsnTranslate.getFieldValue("trnLtrTitle"), query);
        String content = lsnTranslate.getFieldValue("trnLtrRawContent"); 
        String truncated = TrnTools.truncateContent(content, contentMaxLength);
        String highlightedContent = TrnTools.highlightContent(truncated, query);
        
        json.put("title", title)
            .put("row_id", lsnTranslate.getRowId())
            .put("path", lsnTranslate.getFieldValue("trnLsnFrontPath"))
            .put("type", "lesson")
            .put("content", highlightedContent);
        return json;
    }
}