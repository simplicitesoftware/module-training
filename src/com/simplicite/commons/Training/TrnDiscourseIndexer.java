package com.simplicite.commons.Training;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.exceptions.HTTPException;
import com.simplicite.util.tools.RESTTool;

/**
 * Shared code TrnDiscourseIndexer
 */
public class TrnDiscourseIndexer implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private static final TrnTokenBucket tokenBucket = new TrnTokenBucket(40);
	private final String url;
	private final JSONArray categories;
	private final TrnEsiHelper esiHelper;
	private int totalTopics;
	private int totalPosts;

	Grant g;

	public TrnDiscourseIndexer(Grant g) throws JSONException, TrnConfigException {
		this.g = g;
		this.url = TrnDiscourseTool.getUrl();
		this.categories = TrnDiscourseTool.getCategories();
		this.esiHelper = TrnEsiHelper.getEsHelper(g);
	}

	// loop on categories from the parameter TRN_DISCOURSE_INDEX
	// get every topic from given category
	// then concatenate every post content from one topic in a single string for indexation
	public void indexAll() {
		try {
			for (int i = 0; i < categories.length(); i++) {
				String category = (String) categories.get(i);
				indexTopics(category, 0);
			}
			AppLog.info("Discourse indexation done. Total: "+totalTopics+" topics, "+totalPosts+" posts", g);
		} catch (JSONException e) {
			AppLog.error(getClass(), "indexAll", "JSON error: ", e, g);
		} catch (HTTPException e) {
			AppLog.error(getClass(), "indexAll", "HTTP error: ", e, g);
		} catch (TrnDiscourseIndexerException e) {
			AppLog.error(getClass(), "indexAll", "Discourse indexation error: ", e, g);
		} catch (Exception e) {
			AppLog.error(getClass(), "indexAll", "Error: ", e, g);
		}
	}

	// each request fetches 30 topics
	// need to fetch pages until response topics array is empty
	private void indexTopics(String category, int page) throws HTTPException, JSONException, TrnDiscourseIndexerException {
		String catUrl = getCategoryFetchUrl(category, page);
		AppLog.info("TOPICS FROM CATEGORY: " + catUrl, g);
		String res = makeRequest(catUrl);
		JSONObject json = new JSONObject(res);
		JSONObject topicList = json.getJSONObject("topic_list");
		JSONArray topics = topicList.getJSONArray("topics");
		if (!topics.isEmpty()) {
			for (int i = 0; i < topics.length(); i++) {
				indexSingleTopic(category, topics.getJSONObject(i));
			}
			indexTopics(category, page + 1);
		}
	}

	private void indexSingleTopic(String category, JSONObject topic) 
		throws HTTPException, JSONException, TrnDiscourseIndexerException {
		int topicId = topic.getInt("id");
		String esTopicId = "topic_" + topicId;
		String topicSlug = topic.getString("slug");

		JSONObject doc = new JSONObject();
		doc.put("slug", topicSlug);
		doc.put("url", getTopicUrl(topicId, topicSlug));
		doc.put("category", category);
		doc.put("posts", getPostsAsSingleString(topicId));

		// complete doc with topic informations
		esiHelper.indexEsDoc(esTopicId, doc);
		totalTopics++;
	}

	// fetches all posts from topic and create a single string containing the
	// content of every post
	private String getPostsAsSingleString(int topicId) throws HTTPException, JSONException, TrnDiscourseIndexerException {
		String postUrl = getPostFetchUrl(topicId);
		AppLog.info("POSTS FROM TOPIC: " + postUrl, g);

		String res = makeRequest(postUrl);
		JSONObject json = new JSONObject(res);
		JSONObject postStream = json.getJSONObject("post_stream");
		JSONArray posts = postStream.getJSONArray("posts");

		StringBuilder postsString = new StringBuilder();
		for (int i = 0; i < posts.length(); i++) {
			postsString.append(posts.getJSONObject(i).getString("cooked"));
		}
		totalPosts += posts.length();
		return postsString.toString();
	}

	private static String makeRequest(String url) throws HTTPException, TrnDiscourseIndexerException {
		if(tokenBucket.getToken()) {
			return RESTTool.get(url);
		} else {
			while(!tokenBucket.getToken()) {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new TrnDiscourseIndexerException(e.getMessage());
				}
			}
			// Token should be acquired at this point
			return RESTTool.get(url);
		}
	}

	// url to access the topic page
	private String getTopicUrl(int topicId, String topicSlug) {
		return url + "/t/" + topicSlug + "/" + topicId;
	}

	// url to fetch posts from topic
	// max 1000 posts ?
	// https://meta.discourse.org/t/fetch-all-posts-from-a-topic-using-the-api/260886
	private String getPostFetchUrl(int topicId) {
		return url + "/t/" + topicId + ".json";
	}

	// url to fetch topics from a given category
	private String getCategoryFetchUrl(String category, int page) {
		return url + "/c/" + category + ".json?page=" + page;
	}
}


