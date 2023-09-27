package com.simplicite.commons.Training;

import java.util.*;

import com.simplicite.util.Grant;
import com.simplicite.util.tools.IndexCore;
import com.simplicite.util.tools.SearchItem;

/**
 * Shared code TrnSearchSimplicite
 */
public class TrnSearchSimplicite implements java.io.Serializable {
	private static final long serialVersionUID = 1L; 

	public static ArrayList<Object> search(String query, String lang, Grant g) throws Exception {
        getFilteredResults(query, lang, g);
        return new ArrayList<>();
    }

    private static ArrayList<Object> getFilteredResults(String query, String lang, Grant g) throws Exception {
        List<SearchItem> searchResults = getSearchResults(query, g);
        ArrayList<Object> filteredResults =  new ArrayList<>();
        for(SearchItem item : searchResults) {
            
        }

        return filteredResults;
    } 

    private static List<SearchItem> getSearchResults(String query, Grant g) throws Exception {
        List<SearchItem> searchResults = IndexCore.searchIndex(g, query, Arrays.asList("TrnLsnTranslate"));
        return searchResults;
    }

    private static String getRequestSearchQueryUrl() throws TrnConfigException{
        return TrnTools.getEsUrl();
    }
}
