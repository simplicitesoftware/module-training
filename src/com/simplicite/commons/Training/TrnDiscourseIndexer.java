package com.simplicite.commons.Training;

import org.json.JSONArray;
import org.json.JSONException;

import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.exceptions.HTTPException;
import com.simplicite.util.tools.RESTTool;

/**
 * Shared code TrnDiscourseIndexer
 */
public class TrnDiscourseIndexer implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private final String URL;
    private final String USERNAME;
    private final String AUTH_TOKEN;
    private final JSONArray TAGS;

    Grant g;

    public TrnDiscourseIndexer(Grant g) throws JSONException {
        this.g = g;
        this.URL = TrnDiscourseTool.getUrl();
        this.USERNAME = TrnDiscourseTool.getUsername();
        this.AUTH_TOKEN = TrnDiscourseTool.getToken();
        this.TAGS = TrnDiscourseTool.getTags();
    }

    public void indexAll() {
        try {
            String res = RESTTool.get(URL, USERNAME, AUTH_TOKEN);
        } catch (HTTPException e) {
            AppLog.error(getClass(), "indexAll", "HTTP error: ", e, g);
        } catch(Exception e) {
            AppLog.error(getClass(), "indexAll", "Error: ", e, g);
        }
    }
}
