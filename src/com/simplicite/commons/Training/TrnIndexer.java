package com.simplicite.commons.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import org.json.JSONObject;
import com.simplicite.commons.Training.TrnEsiHelper;
import com.simplicite.objects.Training.TrnLesson;
import com.simplicite.commons.Training.TrnTools;

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
			for(String lang: TrnTools.getLangs(lsn.getGrant(), false)){
				es.setIndex(es.getDefaultIndex()+"_"+lang);
				es.indexEsDoc(lsn.getRowId(), lsn.getLessonForIndex(lang));
			}
	}
}
