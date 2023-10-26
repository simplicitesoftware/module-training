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

/**
 * Business object TrnSyncSupervisor
 */
public class TrnSyncSupervisor extends ObjectDB {
	private static final long serialVersionUID = 1L;
    private static StringBuilder logContainer = new StringBuilder();

	@Override
	public void initList(ObjectDB parent) {
		getField("trnSyncDate").setOrder(-1);
	}

	public String dropData() {
        boolean success = false;
		try {
			if (!TrnTools.isFileSystemMode())
				return "Data drop only available in FILESYSTEM mode";

			if (TrnTools.isElasticSearchMode()) {
				resetIndex();
			}

			TrnFsSyncTool.dropDbData();
			TrnFsSyncTool.deleteStore();
            success = true;
			return "Dropped data & hashes";
		} catch (Exception e) {
			TrnSyncSupervisor.addErrorLog(e.getMessage());
			return "Error dropping data";
		}
        finally
        {
            TrnSyncSupervisor.logSync(success, "DROP");
        }
	}

	public String forceSync(Action action) {
		try {
			if (!TrnTools.isFileSystemMode())
				return "Synchronization only available in FILESYSTEM mode";

			String syncType = action.getConfirmField("trnSyncType").getValue();
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
			return "All lessons reindexed in elastic node";
		} catch (Exception e) {
			AppLog.error(getClass(), "reIndexAll", e.getMessage(), e, Grant.getSystemAdmin());
			return "Error while indexing lessons in elastic node";
		}
	}

	private static void resetIndex() throws TrnConfigException {
		TrnEsHelper eh = TrnEsHelper.getEsHelper(Grant.getSystemAdmin());
		eh.deleteIndex();
		eh.createIndex();
        addInfoLog("Reset index done");
	}

    private static void addLog(String msg) {
        // see formating of the log message (date / hour ?)
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        logContainer.append(dtf.format(now)+" | "+msg+"\n");
    }

    public static void addInfoLog(String msg) {
        addLog("INFO"+" | "+msg);
    }

    public static void addWarnLog(String msg) {
        addLog("WARN"+" | "+msg);
    }

    public static void addErrorLog(String msg) {
        addLog("ERROR"+" | "+msg);
    }

    public static void logSync(boolean ok, String type)
    {
		Grant g = Grant.getSystemAdmin();
		try
        {
			boolean[] oldcrud = g.changeAccess("TrnSyncSupervisor", true,true,false,false);
			ObjectDB o = g.getTmpObject("TrnSyncSupervisor");
			o.getTool().getForCreate();
			o.setFieldValue("trnSyncDate", Tool.getCurrentDatetime());
			o.setFieldValue("trnSyncStatus", ok ? "OK" : "KO");
			o.setFieldValue("trnSyncLog", logContainer.toString());
            o.setFieldValue("trnSyncType", type);
			o.getTool().validateAndCreate();
			g.changeAccess("TrnSyncSupervisor", oldcrud);
		}
        catch(Exception e)
        {
			AppLog.error("Error logging sync", e, g);
		}
        finally
        {
            logContainer = new StringBuilder();
        }
	}
}
