package com.simplicite.commons.Training;

import com.simplicite.util.*;
import com.simplicite.objects.Training.TrnLesson;
import com.simplicite.objects.Training.TrnSyncSupervisor;

/**
 * Shared code TrnEsIndexer
 */
public class TrnEsIndexer implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public static void forceIndex(Grant g) throws Exception{
		TrnEsHelper es = TrnEsHelper.getEsHelper(g);
		if(es!=null){
			TrnLesson lsn = (TrnLesson) g.getTmpObject("TrnLesson");
			synchronized(lsn){
				lsn.resetFilters();
				for(String[] row : lsn.search()){
					lsn.setValues(row);
					indexLesson(es, lsn);
				}
			}
		}
        TrnSyncSupervisor.addInfoLog("Reindexed lessons in elastic");
        TrnSyncSupervisor.logSync(true);
	}
	
	public static void indexLesson(TrnLesson lsn) throws Exception{
		indexLesson(TrnEsHelper.getEsHelper(lsn.getGrant()), lsn);
	}
	
	private static void indexLesson(TrnEsHelper es, TrnLesson lsn) throws Exception{
		if(es!=null)
			es.indexEsDoc(Integer.parseInt(lsn.getRowId()), lsn.getLessonJSON(null, true));
	}

	public static void deleteLessonIndex(TrnLesson lsn) throws Exception {
		deleteLessonIndex(TrnEsHelper.getEsHelper(lsn.getGrant()) , lsn);
	}

	private static void deleteLessonIndex(TrnEsHelper es, TrnLesson lsn) throws Exception {
		if(es!=null) {
			es.deleteEsDoc(Integer.parseInt(lsn.getRowId()));
        } 
	}
}
