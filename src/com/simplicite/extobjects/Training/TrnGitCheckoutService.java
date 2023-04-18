package com.simplicite.extobjects.Training;

import java.io.File;
import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;

/**
 * External object TrnGitCheckoutService
 */
public class TrnGitCheckoutService extends com.simplicite.webapp.services.RESTServiceExternalObject {
	private static final long serialVersionUID = 1L;
    private static final String expectedUrl = "https://github.com/simplicitesoftware/migdocs2.git";

	@Override
	public Object post(Parameters params) {
		try{
            String gitUrl = params.getParameter("url");
            String branch = params.getParameter("branch");
            if(!gitUrl.equals(expectedUrl)) {
                throw new Exception("Url is not supported");
            }
            SystemTool.ExecResult res = SystemTool.exec("ls" , null, null);
            String path = res.getStdoutAsString();
            AppLog.info(res.toString(), getGrant());
			return "test";
        } catch(Exception e){
			AppLog.error(getClass(), "post", e.getMessage(), e, getGrant());
			return "ERROR "+e.getMessage();
		}
	}
	
}