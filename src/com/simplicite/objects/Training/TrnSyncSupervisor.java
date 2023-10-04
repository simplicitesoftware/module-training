package com.simplicite.objects.Training;

import com.simplicite.util.*;
import com.simplicite.commons.Training.*;

/**
 * Business object TrnSyncSupervisor
 */
public class TrnSyncSupervisor extends ObjectDB {
	private static final long serialVersionUID = 1L;
	
	public String dropData(){
		try {
			if(!TrnTools.isFileSystemMode())
				return "Data drop only available in filesystem mode";
			
			if(TrnTools.isElasticSearchMode()) {
                resetIndex();
            }
            
			TrnFsSyncTool.dropDbData();
    		TrnFsSyncTool.deleteStore();
    		
			return "Dropped data & hashes";
		} catch (Exception e) {
			AppLog.error(getClass(), "testSync", e.getMessage(), e, Grant.getSystemAdmin());
			return "Error dropping data";
		}
	}
	
	public String forceDirSync() {
		try {
			if(!TrnTools.isFileSystemMode())
				return "Data drop only available in filesystem mode";
				
			TrnFsSyncTool.triggerSync();
			return "Synchronization done";
		} catch (Exception e) {
			AppLog.error(getClass(), "forceDirSync", e.getMessage(), e, Grant.getSystemAdmin());
			return "Error syncing";
		}
	}
	
	public String indexDiscourse() {
		try {
			if(!TrnTools.isElasticSearchMode())
				return "indexation only available with elastic search mode";
				
			TrnCommunityIndexer tdi = new TrnCommunityIndexer(getGrant());
			tdi.indexAll();
			return "Discourse indexation done";
		} catch(Exception e) {
			AppLog.error(getClass(), "indexDiscourse", "Error: ", e, getGrant());
			return "Error while indexing discourse";
		} 
	}
	
	public String reIndexAll() {
		try {
			if(!TrnTools.isElasticSearchMode())
				return "indexation only available with elastic search mode";
				
            resetIndex();
			TrnEsIndexer.forceIndex((getGrant()));
			return "All lessons reindexed in elastic node";
		} catch (Exception e) {
			AppLog.error(getClass(), "reIndexAll", e.getMessage(), e, Grant.getSystemAdmin());
			return "Error while indexing lessons in elastic node";
		}
	}

    private void resetIndex() throws TrnConfigException {
        TrnEsHelper eh = TrnEsHelper.getEsHelper(Grant.getSystemAdmin());
        eh.deleteIndex();
        eh.createIndex();
    } 
}
