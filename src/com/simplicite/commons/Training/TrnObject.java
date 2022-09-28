package com.simplicite.commons.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import org.json.JSONObject;

/**
 * Shared code TrnObject
 */
public class TrnObject extends ObjectDB {
	private static final long serialVersionUID = 1L;

	@Override
	public void postLoad() {
		isNotUiModeWarning();
	}
	
	private void isNotUiModeWarning(){
		if(!TrnTools.isUiMode()){
			List<ObjectCtxHelp> help = new ArrayList<>();
			help.add(new ObjectCtxHelp(getName(), new String[]{ 
				ObjectCtxHelp.CTXHELP_LIST, 
				ObjectCtxHelp.CTXHELP_UPDATE,
				ObjectCtxHelp.CTXHELP_PANELLIST
			}, getGrant().T("TRN_INACTIVATED_UI_WARNING")));
			setCtxHelps(help);
		}
	}

	@Override
	public boolean isCreateEnable() {
		return isChangeEnable();
	}
	
	@Override
	public boolean isUpdateEnable(String[] row) {
		return isChangeEnable();
	}
	
	@Override
	public boolean isDeleteEnable(String[] row) {
		return isChangeEnable();
	}

	private boolean isChangeEnable(){
		return TrnTools.isUiMode() || isSyncInstance();
	}
	
	protected boolean isSyncInstance(){
		return getInstanceName().startsWith("sync_");
	}	
	

	

}
