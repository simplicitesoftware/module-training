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
		TrnEsiHelper es = getEsHelper(g);
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
	
	private static TrnEsiHelper getEsHelper(Grant g){
		JSONObject conf = new JSONObject(g.getParameter("TRN_CONFIG"));
		if("elasticsearch".equals(conf.optString("index_type"))){
			JSONObject esConf = conf.getJSONObject("esi_config");
			return new TrnEsiHelper(g, esConf);
		}
		else
			return null;
	}
	
	public static void indexLesson(TrnLesson lsn) throws Exception{
		indexLesson(getEsHelper(lsn.getGrant()), lsn);
	}
	
	private static void indexLesson(TrnEsiHelper es, TrnLesson lsn) throws Exception{
		if(es!=null)
			es.indexEsDoc(lsn.getRowId(), lsn.getLessonJSON(null, true));
	}

	public static void deleteLessonIndex(TrnLesson lsn) throws Exception {
		deleteLessonIndex(getEsHelper(lsn.getGrant()) , lsn);
	}

	private static void deleteLessonIndex(TrnEsiHelper es, TrnLesson lsn) throws Exception {
		if(es!=null) 
			es.deleteEsLesson(lsn.getRowId());
		
	}
}
