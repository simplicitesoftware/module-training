package com.simplicite.commons.Training;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

/**
 * Shared code TrnTools
 */
public class TrnTools implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	public static String toSnake(String str){
		return SyntaxTool.forceCase(StringUtils.stripAccents(str), SyntaxTool.SNAKE, true);
	}
	
	public static String path2Front(String path){
		return path.replaceAll("(CTG|LSN)_[0-9]+_", "");
	}

    public static boolean isUiMode(){
		return "UI".equals(getContentEdition().optString("mode"));
	}

    public static boolean isFileSystemMode() {
		return "FILESYSTEM".equals(getContentEdition().optString("mode"));
    }

    public static boolean isElasticSearchMode() {
		return "elasticsearch".equals(getContentIndexation().optString("engine"));
    }

    public static boolean isSimpliciteMode() {
		return "simplicite".equals(getContentIndexation().optString("engine"));
    }

    private static JSONObject getTrnConfig() {
        return new JSONObject(Grant.getSystemAdmin().getParameter("TRN_CONFIG"));
    }

    public static JSONObject getContentEdition() {
        return getTrnConfig().getJSONObject("content_edition");
    }

    public static JSONObject getContentIndexation() {
        return getTrnConfig().getJSONObject("content_indexation");
    }

    public static JSONObject getEsiConfig() {
        return getContentIndexation().getJSONObject("esi_config");
    }

	public static String[] getLangs(Grant g){
		return g.getListOfValues("LANG_ALL").getCodesArray("LANG_ALL");
	}
	
	public static String[] getLangs(Grant g, boolean includeDefault){
		if(includeDefault)
			return getLangs(g);
		else{
			List<String> langs = new LinkedList<String>(Arrays.asList(getLangs(g)));
			langs.remove(getDefaultLang());
			return langs.toArray(new String[0]);
		}
	}
	
	public static String getDefaultLang(){
		return "ANY";
	}
}
