package com.simplicite.commons.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.bpm.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;

/**
 * Shared code TrnSearch
 */
public class TrnSearch implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

    public static ArrayList<Object> search(String indexEngine, String query, String lang, Grant g) throws Exception {
        ArrayList<Object> searchResults = new ArrayList<>();
        if("elasticsearch".equals(indexEngine)) {
            try {
                searchResults = TrnSearchElastic.search(query, lang, g);
            } catch(Exception e) {
                AppLog.warning("Error, unable to retrieve elasticsearch search results. Fallback to simplicite engine.", e, g);
                searchResults = TrnSearchSimplicite.search(query, lang, g);
            }
        } else {
            if(!"simplicite".equals(indexEngine)) {
                AppLog.warning("Unkown index engine: "+indexEngine+"found in TRN_CONFIG. Fallback to simplicite engine.", null, g);
            }
            searchResults = TrnSearchSimplicite.search(query, lang, g);
        }
        return searchResults;
    }
}
