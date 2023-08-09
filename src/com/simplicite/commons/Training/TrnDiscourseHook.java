package com.simplicite.commons.Training;

import java.util.*;

import org.json.JSONObject;

import com.simplicite.util.*;
import com.simplicite.bpm.*;
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

	private void upsertTopic(JSONObject body) throws HTTPException {
		// need to fetch topic, if it exists need to set the posts array
		int topicId = body.getInt("id");
		String esTopiciId = TrnDiscourseTool.getEsiTopicId(topicId);
		JSONObject remoteTopic = new JSONObject(esiHelper.getEsiDoc(esTopiciId));
		//if();
		String topicSlug = body.getString("slug"); 
		JSONObject doc = new JSONObject();
		doc.put("title", body.getString("title"));
		doc.put("slug", topicSlug);
		doc.put("url", TrnDiscourseTool.getTopicUrl(discourseUrl, topicId, topicSlug));
		doc.put("category_id", body.getString("category_id"));
		doc.put("posts", new ArrayList<>());
		esiHelper.indexEsiDoc(esTopiciId, doc);
	}

	// delete every post associated to the topic
	private void deleteTopic(JSONObject body) {
		esiHelper.deleteEsiDoc(TrnDiscourseTool.getEsiTopicId(body.getInt("id")));
	}


	private void upsertPost(JSONObject body) throws HTTPException {
		int postId = body.getInt("id");
		int topicId = body.getInt("topic_id");
		String esiTopicId = TrnDiscourseTool.getEsiTopicId(topicId);
		JSONObject remoteTopic = new JSONObject(esiHelper.getEsiDoc(esiTopicId));
		
		JSONObject doc = new JSONObject();
		// append to 
		//doc.put(null, doc)
		//String postId = String.valueOf(body.getInt("id"));
		//esiHelper.indexEsiDoc(postId, body);
	}

	private void deletePost(JSONObject body) {

	}

	private void deleteCategory(JSONObject body) {

	}

	public void handleHook(JSONObject body, String action) throws HTTPException {
		if(body.has("topic")) {
			handleTopic(body.getJSONObject("topic"), action);
		} else if(body.has("post")) {
			handlePost(body.getJSONObject("post"), action);
		} else if(body.has("category")) {
			handleCategory(body.getJSONObject("category"), action);
		}
	}

	private void handleTopic(JSONObject body, String action) throws HTTPException {
		if("topic_created".equals(action) || "topic_edited".equals(action)) {
			upsertTopic(body);
		} else if("topic_destroyed".equals(action)) {
			deleteTopic(body);
		} else {
			AppLog.info("Ignored discourse topic action: " + action + ". Topic id: " + body.getInt("id"), g);
		}
	}

	public void handlePost(JSONObject body, String action) throws HTTPException {
		if("post_created".equals(action) || "post_edited".equals(action)) {
			upsertPost(body);
		} else if("post_destroyed".equals(action)) {
			deletePost(body);
		} else {
			AppLog.info("Ignored discourse post action: " + action + ". Post id: " + body.getInt("id"), g);
		}
	}

	public void handleCategory(JSONObject body, String action) {
		if("category_destroyed".equals(action)) {
			deleteCategory(body);
		} else {
			AppLog.info("Ignored discourse category action: " + action + ". Category id: ", g);
		}
	}
}
