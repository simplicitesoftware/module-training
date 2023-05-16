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
    // test
	@Override
	public Object post(Parameters params) {
		try{
            String gitUrl = TrnTools.getGitUrl();
            String branch = TrnTools.getGitBranch();
            String command = "git clone --filter=blob:none -b "+branch+" "+gitUrl+" ../../test-git";
            SystemTool.ExecResult res = SystemTool.exec(command , null, null);
            String stdout = res.getStdoutAsString();
            AppLog.info(res.toString(), getGrant());
			return "success ??";
        } catch(Exception e){
			AppLog.error(getClass(), "post", e.getMessage(), e, getGrant());
			return "ERROR "+e.getMessage();
		}
	}
}