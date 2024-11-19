package com.simplicite.commons.Training;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;

/**
 * Shared code TrnSearchElastic
 */
public class TrnSearchElastic implements java.io.Serializable {
  private static final long serialVersionUID = 1L;
  private static final int MAX_RESULTS = 20;
  private static final int CONTENT_MAX_LENGTH = 500;

  public static final class EsSearchField {
    String fieldName;
    String weight;

    private EsSearchField(String fieldName, String weight) {
      this.fieldName = fieldName;
      this.weight = weight;
    }
  }

  // fields required for every research
  static EsSearchField[] documentationAnySearchField = {
      // any content
      new EsSearchField("title_any", "5"), new EsSearchField("raw_content_any", "2"), };

  static EsSearchField[] discourseSearchField = { new EsSearchField("title", "1"),
      new EsSearchField("posts.content", "1") };

  static EsSearchField[] documentationLangSearchFields = { new EsSearchField("title", "5"),
      new EsSearchField("raw_content", "1"), };

  public static JSONObject search(String input, String lang, Grant g, String[] filters, int page) 
  throws Exception {
    TrnEsHelper esHelper = new TrnEsHelper(g);
    // es lang format is lowercase
    lang = lang.toLowerCase();
    JSONObject searchRes = esHelper.searchRequest(getFullQuery(input, lang, filters, page));
    JSONObject json = new JSONObject();
    json.put("results", formatResults(searchRes.getJSONArray("results"), lang, g, input))
      .put("search_info", getSearchInfo(searchRes.getInt("search_duration"), searchRes.getInt("total_hits")));
    return json;
  }

  private static JSONObject getFullQuery(String input, String lang, String[] filters, int page) {
    return new JSONObject()
        .put("highlight",
            new JSONObject().put("fields", getHighlightFields(lang, filters)).put("fragment_size", CONTENT_MAX_LENGTH))
        .put("size", MAX_RESULTS)
        .put("query",
            new JSONObject().put("simple_query_string",
                new JSONObject().put("query", input + "*").put("fields", getQueryFields(lang, filters))))
        .put("from", page);
  }

  private static JSONObject getHighlightFields(String lang, String[] filters) {
    JSONObject highlightFields = new JSONObject();
    if (filters.length == 0) {
      setDiscourseEsHighlightsFields(highlightFields);
      setDocumentationEsHighlightsFields(highlightFields, lang);
    }
    for (var i = 0; i < filters.length; i++) {
      String filter = filters[i];
      if ("discourse".equals(filter)) {
        setDiscourseEsHighlightsFields(highlightFields);
      }
      if ("documentation".equals(filter)) {
        setDocumentationEsHighlightsFields(highlightFields, lang);
      }
    }
    return highlightFields;
  }

  private static void setDiscourseEsHighlightsFields(JSONObject highlightFields) {
    for (EsSearchField field : discourseSearchField) {
      highlightFields.put(field.fieldName, new JSONObject());
    }
  }

  private static void setDocumentationEsHighlightsFields(JSONObject highlightFields, String lang) {
    for (EsSearchField field : documentationLangSearchFields) {
      highlightFields.put(field.fieldName + "_" + lang, new JSONObject());
    }
    for (EsSearchField field : documentationAnySearchField) {
      highlightFields.put(field.fieldName, new JSONObject());
    }
  }

  private static JSONArray getQueryFields(String lang, String[] filters) {
    JSONArray queryFields = new JSONArray();
    if (filters.length == 0) {
      setDiscourseEsSearchFields(queryFields);
      setDocumentationEsSearchFields(queryFields, lang);
    }
    else {
      for (int i = 0; i < filters.length; i++) {
        String filter = filters[i];
        if ("discourse".equals(filter)) {
          setDiscourseEsSearchFields(queryFields);
        }
        if ("documentation".equals(filter)) {
          setDocumentationEsSearchFields(queryFields, lang);
        }
      }
    }
    return queryFields;
  }

  // return JSONObject with the total amount of hits in elasticsearch, the search duration etc..
  private static JSONObject getSearchInfo(int searchDuration, int totalHits) {
    JSONObject metadata = new JSONObject();
    metadata.put("total_hits", totalHits)
      .put("search_duration", searchDuration)
      .put("search_type", "elasticsearch")
      .put("page_increment", MAX_RESULTS);
    return metadata;
  }

  private static void setDiscourseEsSearchFields(JSONArray queryFields) {
    for (EsSearchField field : discourseSearchField) {
      queryFields.put(field.fieldName + "^" + field.weight);
    }
  }

  private static void setDocumentationEsSearchFields(JSONArray queryFields, String lang) {
    for (EsSearchField field : documentationAnySearchField) {
      queryFields.put(field.fieldName + "^" + field.weight);
    }
    for (EsSearchField field : documentationLangSearchFields) {
      queryFields.put(field.fieldName + "_" + lang + "^" + field.weight);
    }
  }

  private static ArrayList<JSONObject> formatResults(JSONArray results, String lang, Grant g, String input) {
    ArrayList<JSONObject> formated = new ArrayList<>();
    for (int i = 0; i < results.length(); i++) {
      try {
        formated.add(format((JSONObject) results.get(i), lang, input));
      }
      catch (Exception e) {
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
    if ("lesson".equals(type)) {
      return formatLesson(highlight, source, lang, input, id);
    }
    else if ("discourse".equals(type)) {
      return formatDiscourse(highlight, source, lang, input, id);
    }
    else {
      throw new Exception("elasticsearch doc format unknown: " + type);
    }
  }

  private static JSONObject formatLesson(JSONObject highlight, JSONObject source, String lang, String input, String id)
      throws Exception {
    JSONObject formated = new JSONObject();
    String title = getLessonTitle(highlight, source, lang, input);
    String content = getLessonContent(highlight, source, lang, input);
    formated.put("title", title).put("id", id).put("type", "lesson").put("path", source.getString("path"))
        .put("content", content);
    return formated;
  }

  private static String getLessonTitle(JSONObject highlight, JSONObject source, String lang, String input)
      throws Exception {
    if (highlight.has("title_" + lang)) {
      return highlight.getJSONArray("title_" + lang).get(0).toString();
    }
    else if (source.has("title_" + lang)) {
      return TrnTools.highlightContent(source.getString("title_" + lang), input);
    }
    else if (!"any".equals(lang)) {
      return getLessonTitle(highlight, source, "any", input);
    }
    else {
      throw new Exception("Unable to read lesson doc title from elasticsearch result");
    }
  }

  private static String getLessonContent(JSONObject highlight, JSONObject source, String lang, String input)
      throws Exception {
    if (highlight.has("raw_content_" + lang)) {
      return stringifyHighlightedContent(highlight.getJSONArray("raw_content_" + lang));
    }
    else if (source.has("raw_content_" + lang)) {
      return TrnTools.highlightContent(source.getString("raw_content_" + lang), input);
    }
    else if (!"any".equals(lang)) {
      return getLessonContent(highlight, source, "any", input);
    }
    else {
      throw new Exception("Unable to read lesson doc raw_content from elasticsearch result");
    }
  }

  private static String stringifyHighlightedContent(JSONArray contentArray) {
    StringBuilder content = new StringBuilder();
    for (int i = 0; i < contentArray.length(); i++) {
      if (content.length() > CONTENT_MAX_LENGTH) {
        return content.toString();
      }
      else {
        content.append(contentArray.getString(i) + " [...]<br>");
      }
    }
    return content.toString();
  }

  private static JSONObject formatDiscourse(JSONObject highlight, JSONObject source, String lang, String input,
      String id) throws Exception {
    JSONObject formated = new JSONObject();
    String title = getDiscourseTitle(highlight, source, input);
    String content = getDiscourseContent(highlight, source, input);
    formated.put("title", title).put("id", id).put("type", "discourse").put("path", source.getString("category"))
        .put("content", content).put("url", source.getString("url"));

    return formated;
  }

  private static String getDiscourseTitle(JSONObject highlight, JSONObject source, String input) throws Exception {
    if (highlight.has("title")) {
      return highlight.getJSONArray("title").getString(0);
    }
    else if (source.has("title")) {
      return TrnTools.highlightContent(source.getString("title"), input);
    }
    else {
      throw new Exception("Unable to read discourse doc title from elasticsearch result");
    }
  }

  private static String getDiscourseContent(JSONObject highlight, JSONObject source, String input) throws Exception {
    if (highlight.has("posts.content")) {
      return highlight.getJSONArray("posts.content").getString(0);
    }
    else if (source.has("posts.content")) {
      return TrnTools.highlightContent(source.getString("posts.content"), input);
    }
    else {
      throw new Exception("Unable to read discourse doc content from elasticsearch result");
    }
  }
}