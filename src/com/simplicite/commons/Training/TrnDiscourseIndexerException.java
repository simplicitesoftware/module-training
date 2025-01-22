package com.simplicite.commons.Training;

import org.json.JSONObject;

/**
 * Shared code TrnDiscourseIndexerException
 */
public class TrnDiscourseIndexerException extends Exception {
  private static final long serialVersionUID = 1L;

  private final String additional;

  public TrnDiscourseIndexerException(String msg) {
    super(msg);
    this.additional = "";
  }

  public TrnDiscourseIndexerException(String msg, String additional) {
    super(msg);
    this.additional = additional;
  }

  public TrnDiscourseIndexerException(String msg, JSONObject json) {
    super(msg);
    this.additional = json.toString();
  }

  @Override
  public String getMessage() {
    return super.getMessage() + " : " + this.additional;
  }
}