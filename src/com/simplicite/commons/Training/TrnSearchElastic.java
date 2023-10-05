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
        new EsSearchField("raw_content_any", "1"),
        // discourse content
        new EsSearchField("title", "2"),
        new EsSearchField("posts.content", "1")
    };

    static EsSearchField[] currentLangSearchFields = {
        new EsSearchField("title", "5"),
        new EsSearchField("raw_content", "1"),
    };

	public static ArrayList<JSONObject> search(String input, String lang, Grant g) throws TrnConfigException {
        TrnEsHelper esHelper = new TrnEsHelper(g);
        // es lang format is lowercase
        lang = lang.toLowerCase();
        kong.unirest.json.JSONArray results = esHelper.searchRequest(getFullQuery(input, lang));
        return formatResults(results, lang, g, input);
    }

    private static ArrayList<JSONObject> formatResults(kong.unirest.json.JSONArray results, String lang, Grant g, String input) {
        ArrayList<JSONObject> formated = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            try {
                formated.add(format((kong.unirest.json.JSONObject) results.get(i), lang, input));
            } catch(Exception e) {
                AppLog.error(e, g);
            }
        }
        return formated;
    }

    private static JSONObject format(kong.unirest.json.JSONObject res, String lang, String input) throws Exception {
        kong.unirest.json.JSONObject source = res.getJSONObject("_source");
        kong.unirest.json.JSONObject highlight = res.getJSONObject("highlight");
        String type = source.getString("type");
        if("lesson".equals(type)) {
            return formatLesson(highlight, source, lang, input);
        } else if("discourse".equals(type)) {
            return formatDiscourse(highlight, source, lang, input);
        } else {
            throw new Exception("elasticsearch doc format unknown: " + type);
        }
    }

    private static JSONObject formatLesson(kong.unirest.json.JSONObject highlight, kong.unirest.json.JSONObject source, String lang, String input) throws Exception  {
        JSONObject formated = new JSONObject();
        String title = getLessonTitle(highlight, source, lang, input);
        String content = getLessonContent(highlight, source, lang, input);
        formated.put("title", title)
            .put("row_id", source.getString("row_id"))
            .put("path", source.getString("path"))
            .put("type", "lesson")
            .put("content", content);
        return formated;
    }

    private static String getLessonTitle(kong.unirest.json.JSONObject highlight, kong.unirest.json.JSONObject source, String lang, String input) throws Exception {
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

    private static String getLessonContent(kong.unirest.json.JSONObject highlight, kong.unirest.json.JSONObject source, String lang, String input) throws Exception {
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

    private static String stringifyHighlightedContent(kong.unirest.json.JSONArray contentArray) {
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

    private static JSONObject formatDiscourse(kong.unirest.json.JSONObject highlight, kong.unirest.json.JSONObject source, String lang, String input) throws Exception {
        JSONObject formated = new JSONObject();
        String title = getDiscourseTitle(highlight, source, input);
        String content = getDiscourseContent(highlight, source, input);
        formated.put("title", title)
            .put("row_id", source.getString("row_id"))
            .put("path", source.getString("path"))
            .put("type", "lesson")
            .put("content", content);

        return formated;
    }

    private static String getDiscourseTitle(kong.unirest.json.JSONObject highlight, kong.unirest.json.JSONObject source, String input) throws Exception {
        if(highlight.has("title")) {
            return highlight.getString("title");
        } else if(source.has("title")) {
            return TrnTools.highlightContent(source.getString("title"), input);
        } else {
            throw new Exception("Unable to read discourse doc title from elasticsearch result");
        }
    }

    private static String getDiscourseContent(kong.unirest.json.JSONObject highlight, kong.unirest.json.JSONObject source, String input) throws Exception {
        if(highlight.has("posts.content")) {
            return highlight.getString("posts.content");
        } else if(source.has("posts.content")) {
            return TrnTools.highlightContent(source.getString("posts.content"), input);
        } else {
            throw new Exception("Unable to read discourse doc content from elasticsearch result");
        }
    }

    private static JSONObject getFullQuery(String input, String lang) {
        JSONObject json = new JSONObject()
            .put("query", getQuery(input, lang))
            .put("highlight", getHighlight(lang))
            .put("size", maxResults);
        return json;
    }

    private static JSONObject getQuery(String input, String lang) {
        JSONObject multiMatch = new JSONObject();
        JSONObject matchContent = new JSONObject();
        matchContent.put("type", "phrase_prefix")
            .put("query", input)
            .put("fields", getQueryFields(lang));
        multiMatch.put("multi_match", matchContent);
        return multiMatch;
    }

    private static JSONObject getHighlight(String lang) {
        JSONObject highlight = new JSONObject();
        highlight.put("fields", getHighlightFields(lang))
            .put("fragment_size", contentMaxLength);
        return highlight;
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
}
