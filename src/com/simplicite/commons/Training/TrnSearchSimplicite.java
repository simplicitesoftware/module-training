package com.simplicite.commons.Training;

import java.util.*;

import com.simplicite.util.Grant;

import kong.unirest.Unirest;

/**
 * Shared code TrnSearchSimplicite
 */
public class TrnSearchSimplicite implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	public static ArrayList<Object> search(String query, String lang, Grant g) {
        
        return new ArrayList<>();
    }


    private static String getRequestSearchQueryUrl() throws TrnConfigException{
        return TrnTools.getEsUrl();
    }
}
