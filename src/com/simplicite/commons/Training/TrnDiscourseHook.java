package com.simplicite.commons.Training;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;

/**
 * Shared code TrnDiscourseIndexer
 */
public class TrnDiscourseHook implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private final TrnEsiHelper esiHelper;
	private final String discourseUrl;
	private final Grant g;

	public TrnDiscourseHook(Grant g) throws TrnConfigException {
		this.esiHelper = TrnEsiHelper.getEsiHelper(g);
		this.discourseUrl = TrnDiscourseTool.getUrl();
		this.g = g;
	}

	private void upsertTopic(JSONObject body) throws HTTPException, TrnDiscourseIndexerException, TrnConfigException {
		// need to fetch topic, if it exists need to set the posts array
		int topicId = body.getInt("id");
		int esTopiciId = TrnDiscourseTool.getEsiTopicId(topicId);

		String topicSlug = body.getString("slug");
		JSONObject doc = new JSONObject();

		doc.put("title", body.getString("title"));
		doc.put("slug", topicSlug);
		doc.put("url", TrnDiscourseTool.getTopicUrl(discourseUrl, topicId, topicSlug));

		int categoryId = body.getInt("category_id");
		doc.put("category_id", categoryId);
		String catInfoUrl = TrnDiscourseTool.getCategoryInfoUrl(discourseUrl, categoryId);
		JSONObject catInfo = new JSONObject(RESTTool.get(catInfoUrl));
		doc.put("category", catInfo.getJSONObject("category").getString("name"));

		TrnCommunityIndexer ci = new TrnCommunityIndexer(g);
		
		doc.put("posts", ci.getPostsAsArray(topicId));
		
		esiHelper.indexEsiDoc(esTopiciId, doc);
	}

	private void deleteTopic(JSONObject body) {
		esiHelper.deleteEsiDoc(TrnDiscourseTool.getEsiTopicId(body.getInt("id")));
	}

	private void upsertPost(JSONObject body) {
		// get ids and the topic doc stored in elasticsearch
		int topicId = body.getInt("topic_id");
		int esiTopicId = TrnDiscourseTool.getEsiTopicId(topicId);
		int postId = body.getInt("id");

		String resTopic =  esiHelper.getEsiDoc(esiTopicId);
		if(resTopic.isEmpty()) {
			AppLog.info(getClass(), "upsertPost", "Ignoring upsert post as associated topic does not exist. Topic id: "+topicId+" postId: "+postId, g);
			return;
		}
		JSONObject remoteTopic = new JSONObject(resTopic);
		JSONObject topic = remoteTopic.getJSONObject("_source");

		// create post object
		JSONObject post = new JSONObject();

		post.put("id", postId);
		post.put("content", body.getString("raw"));

		JSONArray posts = topic.getJSONArray("posts");
		removePostFromArray(posts, postId);
		posts.put(post);
		esiHelper.indexEsiDoc(esiTopicId, topic);
	}

	private void deletePost(JSONObject body) {
		int topicId = body.getInt("topic_id");
		int esiTopicId = TrnDiscourseTool.getEsiTopicId(topicId);
		int postId = body.getInt("id");

		String resTopic = esiHelper.getEsiDoc(esiTopicId);
		if(resTopic.isEmpty()) {
			AppLog.info(getClass(), "upsertPost", "Ignoring delete post as associated topic does not exist. Topic id: "+topicId+" postId: "+postId, g);
			return;
		}
		JSONObject remoteTopic = new JSONObject(resTopic);
		JSONObject topic = remoteTopic.getJSONObject("_source");
		JSONArray posts = topic.getJSONArray("posts");
		removePostFromArray(posts, postId);

		esiHelper.indexEsiDoc(esiTopicId, topic);
	}

	private static void removePostFromArray(JSONArray posts, int postId) {
		for (int i = 0; i < posts.length(); i++) {
			if(posts.getJSONObject(i).getInt("id") == postId) {
				posts.remove(i);
			}
		}
	}

	// private void deleteCategory(JSONObject body) {
	// 	esiHelper.deleteByQuery(body.getJSONObject("category").getInt("id"));
	// }

	// private void indexCategory(JSONObject body) throws TrnConfigException {
	// 	TrnCommunityIndexer ci = new TrnCommunityIndexer(g, new JSONArray(body.getJSONObject("category").getString("name")));
	// 	ci.indexAll();
	// }

	public void handleHook(JSONObject body, String action) throws HTTPException, TrnConfigException, TrnDiscourseIndexerException {
		if (body.has("topic")) {
			handleTopic(body.getJSONObject("topic"), action);
		} else if (body.has("post")) {
			handlePost(body.getJSONObject("post"), action);
		}
		// else if (body.has("category")) {
		// 	handleCategory(body.getJSONObject("category"), action);
		// }
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

	// public void handleCategory(JSONObject body, String action) throws TrnConfigException {
	// 	if ("category_recovered".equals(action)) {
	// 		indexCategory(body);
	// 	} else if ("category_destroyed".equals(action)) {
	// 		deleteCategory(body);
	// 	} else {
	// 		AppLog.info("Ignored discourse category action: " + action + ". Category id: ", g);
	// 	}
	// }
}
