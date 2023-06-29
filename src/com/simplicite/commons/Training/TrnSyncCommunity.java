package com.simplicite.commons.Training;

import org.json.JSONArray;

import com.simplicite.util.Grant;


/**
 * Shared code TrnSyncCommunity
 */
public class TrnSyncCommunity implements java.io.Serializable {
  private static final long serialVersionUID = 1L;

  private final String DISCOURSE_URL;
  private final JSONArray DISCOURSE_TAGS;
  private final String AUTH_TOKEN; 

  Grant g;

  public TrnSyncCommunity(Grant g) {
    this.g = g;
    this.DISCOURSE_URL = "test";
    this.DISCOURSE_TAGS = new JSONArray();
    this.AUTH_TOKEN= "test";
  }

}
