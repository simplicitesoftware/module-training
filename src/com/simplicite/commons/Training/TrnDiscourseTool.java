package com.simplicite.commons.Training;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.simplicite.util.Grant;

/**
 * Shared code TrnDiscourseTool
 */
public class TrnDiscourseTool implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static JSONObject getConfig() throws JSONException {
        return new JSONObject(Grant.getSystemAdmin().getParameter("TRN_DISCOURSE_INDEX"));
    }

    public static String getUrl() throws JSONException {
        return getConfig().getString("url");
    }

    public static String getUsername() throws JSONException {
        return getConfig().getString("username");
    }

    public static String getToken() throws JSONException {
        return getConfig().getString("token");
    }

    public static JSONArray getTags() throws JSONException {
        return getConfig().getJSONArray("tags");
    }
}
