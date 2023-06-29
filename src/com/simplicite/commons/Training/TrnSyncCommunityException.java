package com.simplicite.commons.Training;

import kong.unirest.json.JSONObject;

/**
 * Shared code TrnSyncCommunityException
 */
public class TrnSyncCommunityException extends Exception {
	private static final long serialVersionUID = 1L;

	private final String additional;
	public TrnSyncCommunityException(String msg){
		super(msg);
		this.additional = "";
	}
	
	public TrnSyncCommunityException(String msg, String additional){
		super(msg);
		this.additional = additional;
	}
	
	public TrnSyncCommunityException(String msg, JSONObject json){
		super(msg);
		this.additional = json.toString();
	}
	
  @Override
	public String getMessage(){
		return super.getMessage()+" : "+this.additional;
	}
}
