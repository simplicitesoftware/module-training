package com.simplicite.commons.Training;

import com.simplicite.util.*;
import com.simplicite.objects.Training.TrnLesson;

/**
 * Shared code TrnIndexer
 */
public class TrnIndexer implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public static void forceIndex(Grant g) throws Exception{
		TrnEsiHelper es = TrnEsiHelper.getEsiHelper(g);
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
	}
	
	public static void indexLesson(TrnLesson lsn) throws Exception{
		indexLesson(TrnEsiHelper.getEsiHelper(lsn.getGrant()), lsn);
	}
	
	private static void indexLesson(TrnEsiHelper es, TrnLesson lsn) throws Exception{
		if(es!=null)
			es.indexEsiDoc(Integer.parseInt(lsn.getRowId()), lsn.getLessonJSON(null, true));
	}

	public static void deleteLessonIndex(TrnLesson lsn) throws Exception {
		deleteLessonIndex(TrnEsiHelper.getEsiHelper(lsn.getGrant()) , lsn);
	}

	private static void deleteLessonIndex(TrnEsiHelper es, TrnLesson lsn) throws Exception {
		if(es!=null) {
			es.deleteEsiDoc(Integer.parseInt(lsn.getRowId()));
        } 
	}
}
