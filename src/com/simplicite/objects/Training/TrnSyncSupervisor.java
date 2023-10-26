package com.simplicite.objects.Training;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.simplicite.commons.Training.TrnCommunityIndexer;
import com.simplicite.commons.Training.TrnConfigException;
import com.simplicite.commons.Training.TrnEsHelper;
import com.simplicite.commons.Training.TrnEsIndexer;
import com.simplicite.commons.Training.TrnFsSyncTool;
import com.simplicite.commons.Training.TrnGitCheckout;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.util.Action;
import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.ObjectDB;
import com.simplicite.util.Tool;
import com.simplicite.util.exceptions.HTTPException;

/**
 * Business object TrnSyncSupervisor
 */
public class TrnSyncSupervisor extends ObjectDB {
	private static final long serialVersionUID = 1L;

	@Override
	public void initList(ObjectDB parent) {
		getField("trnSyncDate").setOrder(-1);
	}

	public String dropData() {
		try {
			if (!TrnTools.isFileSystemMode())
				return "Data drop only available in FILESYSTEM mode";

			TrnFsSyncTool.dropDbData();
			TrnFsSyncTool.deleteStore();
            TrnSyncSupervisor.logSync(true, "DROP", null);
			return "Dropped data & hashes";
		} catch (Exception e) {
            TrnSyncSupervisor.logSync(false, "DROP", null);
			return "Error dropping data";
		}
	}

	public String forceSync(Action action) {
		try {
			if (!TrnTools.isFileSystemMode())
				return "Synchronization only available in FILESYSTEM mode";
			String syncType = action.getConfirmField("trnSyncActionType").getValue();
			if ("GIT".equals(syncType)) {
				TrnGitCheckout tgc = new TrnGitCheckout(getGrant());
				tgc.checkout();
			} else if ("FS".equals(syncType)) {
				TrnFsSyncTool.triggerSyncOnly();
			} else {
				throw new Exception("Unknown sync type");
			}
			return "Synchronization done";
		} catch (Exception e) {
			AppLog.error(getClass(), "forceDirSync", e.getMessage(), e, Grant.getSystemAdmin());
			return "Error syncing";
		}
	}

	public String indexDiscourse() {
		try {
			if (!TrnTools.isElasticSearchMode())
				return "Indexation only available with elastic search mode";

			TrnCommunityIndexer tdi = new TrnCommunityIndexer(getGrant());
			tdi.indexAll();
			return "Discourse indexation done";
		} catch (Exception e) {
			AppLog.error(getClass(), "indexDiscourse", "Error: ", e, getGrant());
			return "Error while indexing discourse";
		}
	}

	public String reIndexAll() {
		try {
			if (!TrnTools.isElasticSearchMode())
				return "indexation only available with elastic search mode";

			resetIndex();
			TrnEsIndexer.forceIndex((getGrant()));
            TrnSyncSupervisor.logSync(true, "REINDEX", null);
			return "All lessons reindexed in elastic node";
		} catch (Exception e) {
            TrnSyncSupervisor.logSync(false, "REINDEX", e.getMessage());
			return "Error while indexing lessons in elastic node";
		}
	}

	private static void resetIndex() throws TrnConfigException, HTTPException {
		TrnEsHelper eh = TrnEsHelper.getEsHelper(Grant.getSystemAdmin());
		eh.deleteIndex();
		eh.createIndex();
	}

    public static void logSync(boolean ok, String type, String msg)
    {
		Grant g = Grant.getSystemAdmin();
		try
        {
			boolean[] oldcrud = g.changeAccess("TrnSyncSupervisor", true,true,false,false);
			ObjectDB o = g.getTmpObject("TrnSyncSupervisor");
			o.getTool().getForCreate();
			o.setFieldValue("trnSyncDate", Tool.getCurrentDatetime());
			o.setFieldValue("trnSyncStatus", ok ? "OK" : "KO");
			o.setFieldValue("trnSyncLog", msg);
            o.setFieldValue("trnSyncType", type);
			o.getTool().validateAndCreate();
			g.changeAccess("TrnSyncSupervisor", oldcrud);
		}
        catch(Exception e)
        {
			AppLog.error("Error logging sync", e, g);
		}
	}
}
