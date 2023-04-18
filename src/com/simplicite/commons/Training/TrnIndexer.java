package com.simplicite.commons.Training;

import com.simplicite.util.*;
import org.json.JSONObject;
import com.simplicite.objects.Training.TrnLesson;

/**
 * Shared code TrnIndexer
 */
public class TrnIndexer implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public static void forceIndex(Grant g) throws Exception{
		TrnEsiHelper es = TrnEsiHelper.getEsHelper(g);
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
		indexLesson(TrnEsiHelper.getEsHelper(lsn.getGrant()), lsn);
	}
	
	private static void indexLesson(TrnEsiHelper es, TrnLesson lsn) throws Exception{
		if(es!=null)
			es.indexEsDoc(lsn.getRowId(), lsn.getLessonJSON(null, true));
	}

	public static void deleteLessonIndex(TrnLesson lsn) throws Exception {
		deleteLessonIndex(TrnEsiHelper.getEsHelper(lsn.getGrant()) , lsn);
	}

	private static void deleteLessonIndex(TrnEsiHelper es, TrnLesson lsn) throws Exception {
		if(es!=null) 
			es.deleteEsLesson(lsn.getRowId());
		
	}
}
