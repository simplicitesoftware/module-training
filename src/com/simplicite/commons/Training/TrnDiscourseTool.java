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

	public static JSONArray getCategories() throws JSONException {
		return getConfig().getJSONArray("categories");
	}

	// url to access the topic page
	public static String getTopicUrl(String url, int topicId, String topicSlug) {
		return url + "/t/" + topicSlug + "/" + topicId;
	}

	// url to fetch posts from topic
	// max 1000 posts ?
	// https://meta.discourse.org/t/fetch-all-posts-from-a-topic-using-the-api/260886
	public static String getPostFetchUrl(String url, int topicId) {
		return url + "/t/" + topicId + ".json";
	}

	// url to fetch topics from a given category
	public static String getCategoryFetchUrl(String url, String category, int page) {
		return url + "/c/" + category + ".json?page=" + page;
	}

	public static String getEsiTopicId(String docId) {
		return "topic_" + docId;
	}

	public static String getEsiTopicId(int docId) {
		return "topic_" + docId;
	}
}
