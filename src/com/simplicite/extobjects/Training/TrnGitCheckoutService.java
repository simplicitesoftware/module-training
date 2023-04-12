package com.simplicite.extobjects.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;

/**
 * External object TrnGitCheckoutService
 */
public class TrnGitCheckoutService extends com.simplicite.webapp.services.RESTServiceExternalObject {
	private static final long serialVersionUID = 1L;

	@Override
	public Object post(Parameters params) {
		try{
			return "test";
		}
		catch(Exception e){
			AppLog.error(getClass(), "post", e.getMessage(), e, getGrant());
			return "ERROR "+e.getMessage();
		}
	}
	
}
