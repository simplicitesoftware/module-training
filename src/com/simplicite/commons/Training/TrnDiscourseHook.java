package com.simplicite.commons.Training;

import org.json.JSONArray;
import org.json.JSONObject;

import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.exceptions.HTTPException;
import com.simplicite.util.tools.RESTTool;

/**
 * Shared code TrnDiscourseIndexer
 */
public class TrnDiscourseHook implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private final TrnEsHelper esHelper;
	private final String discourseUrl;
	private final Grant g;

	public TrnDiscourseHook(Grant g) throws TrnConfigException {
		this.esHelper = TrnEsHelper.getEsHelper(g);
		this.discourseUrl = TrnDiscourseTool.getUrl();
		this.g = g;
	}

	private void upsertTopic(JSONObject body) throws HTTPException, TrnDiscourseIndexerException, TrnConfigException {
		// need to fetch topic, if it exists need to set the posts array
		int topicId = body.getInt("id");
		int esTopiciId = TrnDiscourseTool.getEsTopicId(topicId);

		String topicSlug = body.getString("slug");
		
		JSONObject doc = new JSONObject();
		int categoryId = body.getInt("category_id");
		String catInfoUrl = TrnDiscourseTool.getCategoryInfoUrl(discourseUrl, categoryId);
		JSONObject catInfo = new JSONObject(RESTTool.get(catInfoUrl));
		TrnCommunityIndexer ci = new TrnCommunityIndexer(g);

		doc.put("title", body.getString("title"))
			.put("type", "discourse")
			.put("slug", topicSlug)
			.put("url", TrnDiscourseTool.getTopicUrl(discourseUrl, topicId, topicSlug))
			.put("category_id", categoryId)
			.put("category", catInfo.getJSONObject("category").getString("name"))
			.put("posts", ci.getPostsAsArray(topicId));
		
		esHelper.indexEsDoc(esTopiciId, doc);
	}

	private void deleteTopic(JSONObject body) {
		esHelper.deleteEsDoc(TrnDiscourseTool.getEsTopicId(body.getInt("id")));
	}

	private void upsertPost(JSONObject body) throws TrnDiscourseIndexerException {
		// get ids and the topic doc stored in elasticsearch
		int topicId = body.getInt("topic_id");
		int esTopicId = TrnDiscourseTool.getEsTopicId(topicId);
		int postId = body.getInt("id");

		String resTopic =  esHelper.getEsDoc(esTopicId);
		if(resTopic.isEmpty()) {
			AppLog.info(getClass(), "upsertPost", "Ignoring upsert post as associated topic does not exist. Topic id: "+topicId+" postId: "+postId, g);
			return;
		}
		JSONObject remoteTopic = new JSONObject(resTopic);
		JSONObject topic = remoteTopic.getJSONObject("_source");

		JSONArray posts = topic.getJSONArray("posts");
		JSONObject post = new JSONObject();


		post.put("id", postId)
			.put("content", body.getString("raw"));

		removePostFromArray(posts, postId);
		posts.put(post);
		esHelper.indexEsDoc(esTopicId, topic);
	}

	private void deletePost(JSONObject body) throws TrnDiscourseIndexerException {
		int topicId = body.getInt("topic_id");
		int esTopicId = TrnDiscourseTool.getEsTopicId(topicId);
		int postId = body.getInt("id");

		String resTopic = esHelper.getEsDoc(esTopicId);
		if(resTopic.isEmpty()) {
			AppLog.info(getClass(), "upsertPost", "Ignoring delete post as associated topic does not exist. Topic id: "+topicId+" postId: "+postId, g);
			return;
		}
		JSONObject remoteTopic = new JSONObject(resTopic);
		JSONObject topic = remoteTopic.getJSONObject("_source");
		JSONArray posts = topic.getJSONArray("posts");
		removePostFromArray(posts, postId);

		esHelper.indexEsDoc(esTopicId, topic);
	}

	private static void removePostFromArray(JSONArray posts, int postId) {
		for (int i = 0; i < posts.length(); i++) {
			if(posts.getJSONObject(i).getInt("id") == postId) {
				posts.remove(i);
			}
		}
	}

	public void handleHook(JSONObject body, String action) throws HTTPException, TrnConfigException, TrnDiscourseIndexerException {
		if (body.has("topic")) {
			handleTopic(body.getJSONObject("topic"), action);
		} else if (body.has("post")) {
			handlePost(body.getJSONObject("post"), action);
		}
	}

	private void handleTopic(JSONObject body, String action) throws HTTPException, TrnDiscourseIndexerException, TrnConfigException {
		if ("topic_created".equals(action) || "topic_edited".equals(action) || "topic_recovered".equals(action)) {
			upsertTopic(body);
		} else if ("topic_destroyed".equals(action)) {
			deleteTopic(body);
		} else {
			AppLog.info("Ignored discourse topic action: " + action + ". Topic id: " + body.getInt("id"), g);
		}
	}

	public void handlePost(JSONObject body, String action) throws HTTPException, TrnDiscourseIndexerException {
		if ("post_created".equals(action) || "post_edited".equals(action) || "post_recovered".equals(action)) {
			upsertPost(body);
		} else if ("post_destroyed".equals(action)) {
			deletePost(body);
		} else {
			AppLog.info("Ignored discourse post action: " + action + ". Post id: " + body.getInt("id"), g);
		}
	}
}
