package com.simplicite.commons.Training;

import java.io.File;
import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;

/**
 * Shared code TrnSyncException
 */
public class TrnSyncException extends Exception {
	private static final long serialVersionUID = 1L;
	private String additional;
	public TrnSyncException(String msg){
		super(msg);
		additional = "";
	}
	
	public TrnSyncException(String msg, String additional){
		super(msg);
		this.additional = additional;
	}
	
	public TrnSyncException(String msg, File f){
		super(msg);
		this.additional = f.getPath();
	}
	
	public String getMessage(){
		return super.getMessage()+" : "+additional;
	}
	
}
