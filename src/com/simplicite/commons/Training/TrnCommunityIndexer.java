package com.simplicite.commons.Training;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.simplicite.objects.Training.TrnSyncSupervisor;
import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.exceptions.HTTPException;
import com.simplicite.util.tools.HTMLTool;
import com.simplicite.util.tools.RESTTool;

/**
 * Shared code TrnDiscourseIndexer
 */
public class TrnCommunityIndexer implements java.io.Serializable {
  private static final long serialVersionUID = 1L;

  private static final TrnTokenBucket tokenBucket = new TrnTokenBucket(40);
  private final String url;
  private final JSONArray categories;
  private final TrnEsHelper esHelper;
  private int totalTopics;
  private int totalPosts;

  Grant g;

  public TrnCommunityIndexer(Grant g) throws JSONException, TrnConfigException {
    this.g = g;
    this.url = TrnDiscourseTool.getUrl();
    this.categories = TrnDiscourseTool.getCategories();
    this.esHelper = TrnEsHelper.getEsHelper(g);
  }

  public TrnCommunityIndexer(Grant g, JSONArray categories) throws JSONException, TrnConfigException {
    this.g = g;
    this.url = TrnDiscourseTool.getUrl();
    this.categories = categories;
    this.esHelper = TrnEsHelper.getEsHelper(g);
  }

  // loop on categories from the parameter TRN_DISCOURSE_INDEX
  // get every topic from given category
  // then concatenate every post content from one topic in a single string for
  // indexation
  public void indexAll() {
    boolean success = false;
    String msg = null;
    try {
      AppLog.info("Starting discourse indexation", g);
      indexCategories();
      AppLog.info("Discourse indexation done. Total: " + totalTopics + " topics, " + totalPosts + " posts", g);
      success = true;
    }
    catch (JSONException e) {
      msg = "JSON error: " + e.getMessage();
    }
    catch (HTTPException e) {
      msg = "HTTP error: " + e.getMessage();
    }
    catch (TrnDiscourseIndexerException e) {
      msg = "Discourse indexation error: " + e.getMessage();
    }
    catch (Exception e) {
      msg = "Error: " + e.getMessage();
    }
    finally {
      TrnSyncSupervisor.logSync(success, "DISCOURSE", msg, g.getLogin(), null);
    }
  }

  private void indexCategories() throws TrnDiscourseIndexerException, HTTPException {
    for (int i = 0; i < categories.length(); i++) {
      String category = (String) categories.get(i);
      indexTopics(category, 0);
    }
  }

  // each request fetches 30 topics
  // need to fetch pages until response topics array is empty
  private void indexTopics(String category, int page)
      throws HTTPException, JSONException, TrnDiscourseIndexerException {
    String catUrl = TrnDiscourseTool.getCategoryFetchUrl(url, category, page);
    String res = makeRequest(catUrl);
    var json = new JSONObject(res);
    var topicList = json.getJSONObject("topic_list");
    var topics = topicList.getJSONArray("topics");
    if (!topics.isEmpty()) {
      for (var i = 0; i < topics.length(); i++) {
        indexSingleTopic(topics.getJSONObject(i));
      }
      indexTopics(category, page + 1);
    }
  }

  private void indexSingleTopic(JSONObject topic) throws HTTPException, JSONException, TrnDiscourseIndexerException {
    var topicId = topic.getInt("id");
    int esTopicId = TrnDiscourseTool.getEsTopicId(topicId);
    var topicSlug = topic.getString("slug");

    // get category name using category_id
    int categoryId = topic.getInt("category_id");
    String catInfoUrl = TrnDiscourseTool.getCategoryInfoUrl(url, categoryId);
    JSONObject catInfo = new JSONObject(makeRequest(catInfoUrl));

    JSONObject doc = new JSONObject();
    doc.put("title", topic.getString("title")).put("type", "discourse").put("slug", topicSlug)
        .put("url", TrnDiscourseTool.getTopicUrl(url, topicId, topicSlug)).put("category_id", categoryId)
        .put("category", catInfo.getJSONObject("category").getString("name")).put("posts", getPostsAsArray(topicId));

    // complete doc with topic informations
    esHelper.indexEsDoc(esTopicId, doc);
    totalTopics++;
  }

  // fetches all posts from topic and create a single string containing the
  // content of every post
  public JSONArray getPostsAsArray(int topicId) throws HTTPException, JSONException, TrnDiscourseIndexerException {
    String postUrl = TrnDiscourseTool.getPostFetchUrl(url, topicId);

    String res = makeRequest(postUrl);
    JSONObject json = new JSONObject(res);
    JSONObject postStream = json.getJSONObject("post_stream");
    JSONArray posts = postStream.getJSONArray("posts");

    JSONArray postsArray = new JSONArray();
    for (int i = 0; i < posts.length(); i++) {
      JSONObject postObject = new JSONObject();
      postObject.put("id", posts.getJSONObject(i).getInt("id")).put("content",
          HTMLTool.toSafeHTML(posts.getJSONObject(i).getString("raw")));

      postsArray.put(i, postObject);
    }
    totalPosts += posts.length();
    return postsArray;
  }

  private static String makeRequest(String url) throws HTTPException, TrnDiscourseIndexerException {
    if (tokenBucket.getToken()) {
      return RESTTool.get(url);
    }
    else {
      while (!tokenBucket.getToken()) {
        try {
          Thread.sleep(1000);
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new TrnDiscourseIndexerException(e.getMessage());
        }
      }
      // Token should be acquired at this point
      return RESTTool.get(url);
    }
  }
}