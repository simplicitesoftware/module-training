package com.simplicite.commons.Training;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;

/**
 * Shared code TrnSearchElastic
 */
public class TrnSearchElastic implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
    private static final int maxResults = 30;
    private static final int contentMaxLength = 500;

    public static class EsSearchField {
        String fieldName;
        String weight;

        private EsSearchField(String fieldName, String weight) {
            this.fieldName = fieldName;
            this.weight = weight;
        }
    }

    // fields required for every research
    static EsSearchField[] anySearchFields = {
        // any content
        new EsSearchField("title_any", "5"),
        new EsSearchField("raw_content_any", "2"),
        // discourse content
        new EsSearchField("title", "1"),
        new EsSearchField("posts.content", "1")
    };

    static EsSearchField[] currentLangSearchFields = {
        new EsSearchField("title", "5"),
        new EsSearchField("raw_content", "1"),
    };

	public static ArrayList<JSONObject> search(String input, String lang, Grant g) throws Exception {
        TrnEsHelper esHelper = new TrnEsHelper(g);
        // es lang format is lowercase
        lang = lang.toLowerCase();
        JSONArray results = esHelper.searchRequest(getFullQuery(input, lang));
        return formatResults(results, lang, g, input);
    }

    private static JSONObject getFullQuery(String input, String lang) {
    	input += "*"; // treat last term as prefix
		JSONObject q = new JSONObject(String.format(
            """
            {
                "highlight": {
                   "fields": %s,
                   "fragment_size": %d
                },
                "size": %d,
                "query": {
                   "simple_query_string": {
                      "fields": %s
                   }
                }
             }
            """,
            getHighlightFields(lang).toString(),
            contentMaxLength,
            maxResults,
            getQueryFields(lang).toString()
        ));
        
        // SECURITY // must use `put` method in case input needs to be escaped.
		q.getJSONObject("query").getJSONObject("simple_query_string").put("query", input);
        return q;
    }

    private static JSONObject getHighlightFields(String lang) {
        JSONObject highlightFields = new JSONObject();
        for (EsSearchField field : anySearchFields) {
            highlightFields.put(field.fieldName, new JSONObject());
        }
        for(EsSearchField field : currentLangSearchFields) {
            highlightFields.put(field.fieldName+"_"+lang, new JSONObject());
        }
        return highlightFields;
    }

    private static JSONArray getQueryFields(String lang) {
        JSONArray queryFields = new JSONArray();
        for (EsSearchField field : anySearchFields) {
            queryFields.put(field.fieldName+"^"+field.weight);
        }
        for(EsSearchField field : currentLangSearchFields) {
            queryFields.put(field.fieldName+"_"+lang+"^"+field.weight);
        }
        return queryFields;
    }

    private static ArrayList<JSONObject> formatResults(JSONArray results, String lang, Grant g, String input) {
        ArrayList<JSONObject> formated = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            try {
                formated.add(format((JSONObject) results.get(i), lang, input));
            } catch(Exception e) {
                AppLog.error(e, g);
            }
        }
        return formated;
    }

    private static JSONObject format(JSONObject res, String lang, String input) throws Exception {
        JSONObject source = res.getJSONObject("_source");
        String id = res.getString("_id");
        JSONObject highlight = res.getJSONObject("highlight");
        String type = source.getString("type");
        if("lesson".equals(type)) {
            return formatLesson(highlight, source, lang, input, id);
        } else if("discourse".equals(type)) {
            return formatDiscourse(highlight, source, lang, input, id);
        } else {
            throw new Exception("elasticsearch doc format unknown: " + type);
        }
    }

    private static JSONObject formatLesson(JSONObject highlight, JSONObject source, String lang, String input, String id) throws Exception  {
        JSONObject formated = new JSONObject();
        String title = getLessonTitle(highlight, source, lang, input);
        String content = getLessonContent(highlight, source, lang, input);
        formated.put("title", title)
            .put("id", id)
            .put("type", "lesson")
            .put("path", source.getString("path"))
            .put("content", content);
        return formated;
    }

    private static String getLessonTitle(JSONObject highlight, JSONObject source, String lang, String input) throws Exception {
        if(highlight.has("title_" + lang)) {
            return highlight.getJSONArray("title_" + lang).get(0).toString();
        } else if(source.has("title_"+lang)) {
            return TrnTools.highlightContent(source.getString("title_"+lang), input);
        } else if(!"any".equals(lang)) {
            return getLessonTitle(highlight, source, "any", input);
        } else {
            throw new Exception("Unable to read lesson doc title from elasticsearch result");
        }
    }

    private static String getLessonContent(JSONObject highlight, JSONObject source, String lang, String input) throws Exception {
        if(highlight.has("raw_content_" + lang)) {
            return stringifyHighlightedContent(highlight.getJSONArray("raw_content_" + lang));
        }else if(source.has("raw_content_"+lang)) {
            return TrnTools.highlightContent(source.getString("raw_content_"+lang), input);
        } else if(!"any".equals(lang)) {
            return getLessonContent(highlight, source, "any", input);
        } else {
            throw new Exception("Unable to read lesson doc raw_content from elasticsearch result");
        }
    }

    private static String stringifyHighlightedContent(JSONArray contentArray) {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < contentArray.length(); i++) {
            if(content.length() > contentMaxLength) {
                return content.toString();
            } else {
                content.append(contentArray.getString(i) + " [...]<br>");
            }
        }
        return content.toString();
    }

    private static JSONObject formatDiscourse(JSONObject highlight, JSONObject source, String lang, String input, String id) throws Exception {
        JSONObject formated = new JSONObject();
        String title = getDiscourseTitle(highlight, source, input);
        String content = getDiscourseContent(highlight, source, input);
        formated.put("title", title)
            .put("id", id)
            .put("type", "discourse")
            .put("path", source.getString("category"))
            .put("content", content)
            .put("url", source.getString("url"));

        return formated;
    }

    private static String getDiscourseTitle(JSONObject highlight, JSONObject source, String input) throws Exception {
        if(highlight.has("title")) {
            return highlight.getJSONArray("title").getString(0);
        } else if(source.has("title")) {
            return TrnTools.highlightContent(source.getString("title"), input);
        } else {
            throw new Exception("Unable to read discourse doc title from elasticsearch result");
        }
    }

    private static String getDiscourseContent(JSONObject highlight, JSONObject source, String input) throws Exception {
        if(highlight.has("posts.content")) {
            return highlight.getJSONArray("posts.content").getString(0);
        } else if(source.has("posts.content")) {
            return TrnTools.highlightContent(source.getString("posts.content"), input);
        } else {
            throw new Exception("Unable to read discourse doc content from elasticsearch result");
        }
    }
}
