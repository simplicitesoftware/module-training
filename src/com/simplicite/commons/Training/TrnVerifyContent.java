package com.simplicite.commons.Training;

import java.util.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

// This code is also used in the content migration script. 
// see CheckDocs2.java -> https://github.com/simplicitesoftware/migdocs2
public class TrnVerifyContent implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private static final Pattern PATTERN_CATEGORY = Pattern.compile("^CTG_\\d+_[a-z0-9\\-]+$");
	private static final Pattern PATTERN_LESSON = Pattern.compile("^LSN_\\d+_[a-z0-9\\-]+$");
	private static String[] langCodes;
	
	public static void verifyContentStructure(File contentDir, String[] lang) throws TrnSyncException{
		langCodes = lang;
		verifyFolderStructure(contentDir, true);
	}
	
	private static void verifyFolderStructure(File dir, boolean isRoot) throws TrnSyncException{
		if(!dir.isDirectory())
			throw new TrnSyncException("TRN_SYNC_NOT_DIR", dir);
			
		if(isRoot)
			validateRootContent(dir);
		else if(isLesson(dir))
			validateLessonContent(dir);
		else if(isCategory(dir))
			validateCategoryContent(dir);
		else
			throw new TrnSyncException("TRN_SYNC_UNKNOWN_DIR_TYPE", dir);
		
		if(isRoot || isCategory(dir))	
			for(File child : dir.listFiles())
				if(child.isDirectory())
					verifyFolderStructure(child, false);
	}
	
	private static String readFile(String filePath) throws TrnSyncException{
		try{
			return FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
		}
		catch(IOException e){
			throw new TrnSyncException("TRN_SYNC_READFILEERROR" + e.getMessage());
		}
	}
	
	private static void validateRootContent(File dir) throws TrnSyncException{
		for(File child : dir.listFiles()) {
			if (isTagJson(child)) {
				validateTagsJson(child);
            }
            else if(isUrlRewritingJson(child)) {
                validateUrlRewritingJson(child);
			} 
            else if(isThemeJson(child)) {
                validateThemeJson(child);
            }
            else if(!isLogo(child) && !isCategory(child)) {
				throw new TrnSyncException("TRN_SYNC_ROOT_NON_CONFORMITY", child);
            }
		}
	}

    private static boolean isTagJson(File f) {
        return f.getName().equals("tags.json");
    }

    private static boolean isUrlRewritingJson(File f) {
        return f.getName().equals("url_rewriting.json");
    }

    private static boolean isThemeJson(File f) {
        return f.getName().equals("theme.json");
    }

    private static boolean isLogo(File f) {
        return f.getName().equals("logo500-white.png");
    }

    private static void validateTagsJson(File f) throws TrnSyncException {
        try {
            JSONArray tags = new JSONArray(readFile(f.getPath()));
            for(int i = 0; i < tags.length(); i++) {
                JSONObject tag = tags.getJSONObject(i);
                if(StringUtils.isEmpty(tag.getString("code")) 
                || StringUtils.isEmpty(tag.getJSONObject("translation").toString())) {
                    throw new TrnSyncException("TRN_SYNC_ROOT_TAGS_JSON_NON_CONFORMITY", f);
                }
            }
        } catch(JSONException e) {
            throw new TrnSyncException("tags.json unvalid format: "+e.getMessage());
        }
    }

    private static void validateUrlRewritingJson(File f) throws TrnSyncException {
        try {
            JSONArray urlRewriting = new JSONArray(readFile(f.getPath()));
            for(int i = 0; i < urlRewriting.length(); i++) {
                JSONObject row = urlRewriting.getJSONObject(i);
                if(StringUtils.isEmpty(row.getString("sourceUrl")) 
                || StringUtils.isEmpty(row.getString("destinationUrl"))) {
                    throw new TrnSyncException("TRN_SYNC_ROOT_URL_REWRITING_JSON_NON_CONFORMITY", f);
                }
            }
        } catch(JSONException e) {
            throw new TrnSyncException("url_rewriting.json unvalid format: "+e.getMessage());
        }
    }

    private static void validateThemeJson(File f) throws TrnSyncException {
        try {
            JSONObject json = new JSONObject(readFile(f.getPath()));
            if(StringUtils.isEmpty(json.getString("main_color"))
            || StringUtils.isEmpty(json.getString("secondary_color"))
            || StringUtils.isEmpty(json.getString("logo_path"))) {
                throw new TrnSyncException("TRN_SYNC_ROOT_THEME_JSON_NON_CONFORMITY");
            }
        } catch(JSONException e) {
            throw new TrnSyncException("theme.json unvalid format: "+e.getMessage());
        }
    }
	
	private static void validateCategoryContent(File dir) throws TrnSyncException{
		boolean hasJson = false;
		for(File child : dir.listFiles()){
			if("category.json".equals(child.getName()))
				hasJson = true;
			else if(!isCategory(child) && !isLesson(child))
				throw new TrnSyncException("TRN_SYNC_CATEGORY_NON_CONFORMITY", child);
		}
		if(!hasJson)
			throw new TrnSyncException("TRN_SYNC_CATEGORY_JSON_MISSING", dir);
	}
	
	private static void validateLessonContent(File dir) throws TrnSyncException{
		String lessonCode = getLsnCode(dir);
		boolean hasJson = false;
		for(File child : dir.listFiles()){
			if("lesson.json".equals(child.getName()))
				hasJson = true;
			else if(child.isDirectory())
				throw new TrnSyncException("TRN_SYNC_LESSON_CONTAINING_FOLDER", child);
			else if(isMarkdown(child) && !lessonCode.equals(getLocaleStrippedBaseName(child)))
				throw new TrnSyncException("TRN_SYNC_LESSON_MARDOWN_NAME_INCONSISTENT", child);
			else if(isVideo(child) && !lessonCode.equals(getLocaleStrippedBaseName(child)))	
				throw new TrnSyncException("TRN_SYNC_LESSON_VIDEO_NAME_INCONSISTENT", child);
			else if(!isPic(child) && !isMarkdown(child) && !isVideo(child))
				throw new TrnSyncException("TRN_SYNC_FILETYPE_NOT_ALLOWED", child);
		}
		if(!hasJson)
			throw new TrnSyncException("TRN_SYNC_LESSON_JSON_MISSING", dir);
	}
	
	private static boolean isMarkdown(File f){
		return FilenameUtils.getExtension(f.getName()).equalsIgnoreCase("md");
	}
	
	private static boolean isPic(File f){
		String extension = FilenameUtils.getExtension(f.getName()).toLowerCase();
		return "png".equals(extension) || "jpg".equals(extension) || "gif".equals(extension) || "svg".equals(extension);
	}
	
	private static boolean isVideo(File f){
		return FilenameUtils.getExtension(f.getName()).equalsIgnoreCase("webm");
	}
	
	public static boolean isCategory(File dir){
		return dir.isDirectory() && isCategory(dir.getName());
	}
	
	public static boolean isCategory(String path){
		return PATTERN_CATEGORY.matcher(path).matches();
	}
	
	public static boolean isLesson(File dir){
		return dir.isDirectory() && isLesson(dir.getName());
	}
	
	private static boolean isLesson(String path){
		return PATTERN_LESSON.matcher(path).matches();
	}
	
	private static String getLsnCode(File lsnDir){
		return lsnDir.getName().split("_")[2];
	}
	
	private static String getLocaleStrippedBaseName(File f){
		String baseName = FilenameUtils.getBaseName(f.getName());
		String[] split = baseName.toUpperCase().split("_");
		String locale = split.length>0 ? split[split.length-1] : null;
		return locale!=null && Arrays.asList(langCodes).contains(locale) ? baseName.replace("_"+locale, "") : baseName;
	}
}