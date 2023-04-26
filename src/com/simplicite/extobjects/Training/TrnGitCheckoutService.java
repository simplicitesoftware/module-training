package com.simplicite.extobjects.Training;

import org.json.JSONObject;

import com.simplicite.commons.Training.TrnTools;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;

/**
 * External object TrnGitCheckoutService
 */
public class TrnGitCheckoutService extends com.simplicite.webapp.services.RESTServiceExternalObject {
	private static final long serialVersionUID = 1L;

	@Override
	public Object post(Parameters params) {
		try{
            JSONObject gitConfig = TrnTools.getGitConfig();
            String gitUrl = params.getParameter("url");
            String branch = params.getParameter("branch");
            if(!gitUrl.equals(gitConfig.getJSONObject("repository").getString("uri"))) {
                throw new Exception(gitUrl + " is not a supported url");
            }
            String command = "git clone --filter=blob:none https://github.com/simplicitesoftware/module-pizzeria.git ../../test-git";
            SystemTool.ExecResult res = SystemTool.exec(command , null, null);
            String path = res.getStdoutAsString();
            AppLog.info(res.toString(), getGrant());
			return "test";
        } catch(Exception e){
			AppLog.error(getClass(), "post", e.getMessage(), e, getGrant());
			return "ERROR "+e.getMessage();
		}
	}
}