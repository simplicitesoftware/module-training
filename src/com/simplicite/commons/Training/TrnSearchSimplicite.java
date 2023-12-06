package com.simplicite.commons.Training;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import com.simplicite.objects.Training.TrnLesson;
import com.simplicite.objects.Training.TrnLsnTranslate;
import com.simplicite.util.Grant;
import com.simplicite.util.Tool;
import com.simplicite.util.tools.IndexCore;
import com.simplicite.util.tools.SearchItem;

/**
 * Shared code TrnSearchSimplicite
 */
public class TrnSearchSimplicite implements java.io.Serializable {
	private static final long serialVersionUID = 1L; 
    private static final int maxResult = 20;
    private static int contentMaxLength = 500;

	public static ArrayList<JSONObject> search(String query, String lang, Grant g) throws Exception {
        return getFilteredResults(query, lang, g);
    }

    private static ArrayList<JSONObject> getFilteredResults(String query, String lang, Grant g) throws Exception {
        List<SearchItem> searchResults = getSearchResults(query, g);
        TrnLsnTranslate lsnTranslate = (TrnLsnTranslate) g.getTmpObject("TrnLsnTranslate");
        TrnLesson trnLesson = (TrnLesson) g.getTmpObject("TrnLesson");
        ArrayList<JSONObject> filteredResults =  new ArrayList<>();
        for(SearchItem item : searchResults) {            
            lsnTranslate.setValues(item.values);
            // check if lesson is published            
            if(!isLessonPublished(lsnTranslate.getFieldValue("trnLtrLsnId"), trnLesson)) {
                continue;
            }
            String translateLang = lsnTranslate.getFieldValue("trnLtrLang");
            String rawContent = lsnTranslate.getFieldValue("trnLtrRawContent");
            if(lang.equals(translateLang) || "ANY".equals(translateLang) && !rawContent.isEmpty()) {
                filteredResults.add(formatResult(lsnTranslate, query, g));
            }
            if(filteredResults.size() >= maxResult) {
                break;
            }
        }
        return filteredResults;
    }

    private static boolean isLessonPublished(String lsnRowId, TrnLesson trnLesson) {
        trnLesson.resetFilters();
        trnLesson.setFieldFilter("row_id", lsnRowId);
        List<String[]> res = trnLesson.search();
        if(res.size() > 0) {
            trnLesson.setValues(res.get(0));
            return trnLesson.isLessonPublishedRecursive();
        } else {
            return false;
        }
    }

    private static List<SearchItem> getSearchResults(String query, Grant g) throws Exception {
        List<SearchItem> searchResults = IndexCore.searchIndex(g, query, Arrays.asList("TrnLsnTranslate"));
        return searchResults;
    }

    private static JSONObject formatResult(TrnLsnTranslate lsnTranslate, String query, Grant g) {
        JSONObject json = new JSONObject();
        String title = TrnTools.highlightContent(lsnTranslate.getFieldValue("trnLtrTitle"), query);
        String content = lsnTranslate.getFieldValue("trnLtrRawContent"); 
        String truncated = TrnTools.truncateContent(content, contentMaxLength);
        String highlightedContent = TrnTools.highlightContent(truncated, query);
        boolean isPage = isPage(lsnTranslate.getFieldValue("trnLtrLsnId"), g);
        String type = isPage ? "page" : "lesson";
        
        json.put("title", title)
            .put("row_id", lsnTranslate.getRowId())
            .put("path", lsnTranslate.getFieldValue("trnLsnFrontPath"))
            .put("type", type)
            .put("content", highlightedContent);

        return json;
    }

    private static boolean isPage(String lsnRowId, Grant g) {
        String res = g.simpleQuery("select row_id from trn_page where trn_page_trn_lessonid="+Tool.toSQL(lsnRowId));
        if(res.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
}
