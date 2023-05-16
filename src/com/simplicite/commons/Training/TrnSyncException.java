package com.simplicite.commons.Training;

import java.io.File;

/**
 * Shared code TrnSyncException
 */
public class TrnSyncException extends Exception {
	private static final long serialVersionUID = 1L;
	private final String additional;
	public TrnSyncException(String msg){
		super(msg);
		this.additional = "";
	}
	
	public TrnSyncException(String msg, String additional){
		super(msg);
		this.additional = additional;
	}
	
	public TrnSyncException(String msg, File f){
		super(msg);
		this.additional = f.getPath();
	}
	
    @Override
	public String getMessage(){
		return super.getMessage()+" : "+this.additional;
	}
}
