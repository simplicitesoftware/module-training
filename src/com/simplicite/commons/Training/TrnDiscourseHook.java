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
	private final Grant g;

	public TrnDiscourseHook(Grant g) throws TrnConfigException {
		this.esiHelper = TrnEsiHelper.getEsHelper(g);
		this.g = g;
	}

	private void upsertTopic(JSONObject body) {
		JSONObject doc = new JSONObject();
		//doc.
	}

	// delete every post associated to the topic
	private void deleteTopic(JSONObject body) {

	}

	private void upsertPost(JSONObject body) {
		JSONObject doc = new JSONObject();
		// append to 
		//doc.put(null, doc)
		//String postId = String.valueOf(body.getInt("id"));
		//esiHelper.indexEsDoc(postId, body);
	}

	private void deletePost(JSONObject body) {

	}

	private void deleteCategory(JSONObject body) {

	}

	public void handleTopic(JSONObject body, String action) {
		if("post_created".equals(action) || "post_edited".equals(action)) {
			upsertTopic(body);
		} else if("post_detroyed".equals(action)) {
			deleteTopic(body);
		} else {
			AppLog.info("Ignored discourse post action: " + action + ". Post id: " + body.getInt("id"), g);
		}
	}

	public void handlePost(JSONObject body, String action) {
		if("post_created".equals(action) || "post_edited".equals(action)) {
			upsertPost(body);
		} else if("post_detroyed".equals(action)) {
			deletePost(body);
		} else {
			AppLog.info("Ignored discourse post action: " + action + ". Post id: " + body.getInt("id"), g);
		}
	}

	public void handleCategory(JSONObject body, String action) {
		if("post_detroyed".equals(action)) {
			deleteCategory(body);
		} else {
			AppLog.info("Ignored discourse category action: " + action + ". Category id: ", g);
		}
	}
}
