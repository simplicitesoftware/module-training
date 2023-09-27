package com.simplicite.commons.Training;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.simplicite.util.Grant;

/**
 * Shared code TrnSearchElastic
 */
public class TrnSearchElastic implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
    private static final int maxResults = 10;

	public static ArrayList<JSONObject> search(String input, String lang, Grant g) throws TrnConfigException {
        TrnEsHelper esHelper = new TrnEsHelper(g);
        esHelper.searchRequest(getFullQuery(input, lang));
        return new ArrayList<>();
    }

    private static JSONObject getFullQuery(String input, String lang) {
        JSONObject json = new JSONObject()
            .put("query", getQuery(input, lang))
            .put("highlight", getHighlight())
            .put("size", maxResults);
        return json;
    }

    private static JSONObject getQuery(String input, String lang) {
        JSONObject multiMatch = new JSONObject()
            .put("type", "phrase_prefix")
            .put("query", input)
            .put("fields", getQueryFields(lang));
        return multiMatch;
    }

    private static JSONObject getHighlight() {
        return new JSONObject();
    }

    private static JSONArray getQueryFields(String lang) {
        return new JSONArray();
    }
}
