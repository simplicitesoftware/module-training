package com.simplicite.commons.Training;

import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import java.util.*;
import java.io.File;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.json.JSONArray;

// This code is also used in the content migration script. 
// see CheckDocs2.java -> https://github.com/simplicitesoftware/migdocs2
public class TrnVerifyContent implements java.io.Serializable {
	private static final long serialVersionUID = 1L;	
	private static final Pattern PATTERN_CATEGORY = Pattern.compile("^CTG_[0-9]+_[a-z0-9\\-]+$");
	private static final Pattern PATTERN_LESSON = Pattern.compile("^LSN_[0-9]+_[a-z0-9\\-]+$");
	private static String[] LANG_CODES;
	
	public static void verifyContentStructure(File contentDir, Grant g) throws TrnSyncException{
		LANG_CODES = TrnTools.getLangs(g);
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
	
	private static void validateRootContent(File dir) throws TrnSyncException{
		for(File child : dir.listFiles()) {
			if(!isCategory(child) && !child.getName().equals("tags.json") && !child.getName().equals("url_rewriting.json"))
				throw new TrnSyncException("TRN_SYNC_ROOT_NON_CONFORMITY", child);
			else if (child.getName().equals("tags.json")) {
				// check if tags.json has a correct structure
				try {
					JSONArray tags = new JSONArray(FileTool.readFile(dir.getPath()+"/tags.json"));
					for(int i = 0; i < tags.length(); i++) {
						JSONObject tag = tags.getJSONObject(i);
						if(Tool.isEmpty(tag.getString("code")) && Tool.isEmpty(tag.getJSONObject("translation"))) {
							throw new Exception();
						}
					}
				} catch(Exception e) {
					throw new TrnSyncException("TRN_SYNC_ROOT_TAGS_JSON_NON_CONFORMITY", dir);
				}
				try {
					JSONArray urlRewriting = new JSONArray(FileTool.readFile(dir.getPath()));
					for(int i = 0; i < urlRewriting.length(); i++) {
						JSONObject record = urlRewriting.getJSONObject(i);
						if(Tool.isEmpty(record.getString("sourceUrl")) && Tool.isEmpty(record.getString("destinationUrl"))) {
							throw new Exception();
						}
					}
				} catch(Exception e) {
					throw new TrnSyncException("TRN_SYNC_ROOT_URL_REWRITING_JSON_NON_CONFORMITY", dir);
				}
			}
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
		return FileTool.getExtension(f.getName()).toLowerCase().equals("md");
	}
	
	private static boolean isPic(File f){
		String extension = FilenameUtils.getExtension(f.getName()).toLowerCase();
		return "png".equals(extension) || "jpg".equals(extension);
	}
	
	private static boolean isVideo(File f){
		return FileTool.getExtension(f.getName()).toLowerCase().equals("webm");
	}
	
	private static boolean isCategory(File dir){
		return dir.isDirectory() && isCategory(dir.getName());
	}
	
	private static boolean isCategory(String path){
		return PATTERN_CATEGORY.matcher(path).matches();
	}
	
	private static boolean isLesson(File dir){
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
		return locale!=null && Arrays.asList(LANG_CODES).contains(locale) ? baseName.replace("_"+locale, "") : baseName;
	}
}

