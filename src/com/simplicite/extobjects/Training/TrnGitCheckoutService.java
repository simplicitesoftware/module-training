package com.simplicite.extobjects.Training;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.simplicite.commons.Training.TrnConfigException;
import com.simplicite.commons.Training.TrnGitCheckout;
import com.simplicite.commons.Training.TrnSyncException;
import com.simplicite.util.AppLog;
import com.simplicite.util.tools.Parameters;

/**
 * External object TrnGitCheckoutService
 */
public class TrnGitCheckoutService extends com.simplicite.webapp.services.RESTServiceExternalObject {
	private static final long serialVersionUID = 1L;
	
	// credentialsProvider are used only in a https scenario
	// they will remain null if the url is in a ssh format

	@Override
	public Object post(Parameters params) {
		try {
            TrnGitCheckout tgc = new TrnGitCheckout(getGrant());
			return tgc.checkout();
		} catch (Exception e) {
			setHTTPStatus(500);
			AppLog.error(getClass(), "post", e.getMessage(), e, getGrant());
			return "ERROR : " + e.getMessage();
		}
	}

    @Override
	public Object del(Parameters params) {
		try {
            TrnGitCheckout tgc = new TrnGitCheckout(getGrant());
			return tgc.deleteRepository();
		} catch (IOException e) {
			return "ERROR : " + e.getMessage();
		}
	}
}