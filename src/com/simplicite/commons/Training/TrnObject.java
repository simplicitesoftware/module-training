package com.simplicite.commons.Training;

import com.simplicite.util.*;

/**
 * Shared code TrnObject
 */
public class TrnObject extends ObjectDB {
	private static final long serialVersionUID = 1L;

	@Override
	public void postLoad() {
        try {
    		isNotUiModeWarning();
        } catch(TrnConfigException e) {
            AppLog.error(getClass(), "postLoad", e.getMessage(), e, getGrant());
        }
	}
	
	private void isNotUiModeWarning() throws TrnConfigException{
		if(!TrnTools.isUiMode()){
			getCtxHelps().add(new ObjectCtxHelp(getName(), new String[]{ 
				ObjectCtxHelp.CTXHELP_LIST, 
				ObjectCtxHelp.CTXHELP_UPDATE,
				ObjectCtxHelp.CTXHELP_PANELLIST
			}, getGrant().T("TRN_INACTIVATED_UI_WARNING")));
		}
	}

	@Override
	public boolean isCreateEnable() {
        try {
            return isChangeEnable();
        } catch(TrnConfigException e) {
            AppLog.error(getClass(), "isCreateEnable", e.getMessage(), e, getGrant());
            return true;
        }
	}
	
	@Override
	public boolean isUpdateEnable(String[] row) {
        try {
		    return isChangeEnable();
        } catch(TrnConfigException e) {
            AppLog.error(getClass(), "isUpdateEnable", e.getMessage(), e, getGrant());
            return true;
        }
	}
	
	@Override
	public boolean isDeleteEnable(String[] row) {
        try {
    		return isChangeEnable();
        } catch(TrnConfigException e) {
            AppLog.error(getClass(), "isDeleteEnable", e.getMessage(), e, getGrant());
            return true;
        }
	}

	private boolean isChangeEnable() throws TrnConfigException{
		return TrnTools.isUiMode() || isSyncInstance();
	}
	
	protected boolean isSyncInstance(){
		return getInstanceName().startsWith("sync_");
	}	
	

	

}
