package com.simplicite.commons.Training;

import java.util.*;

import com.simplicite.objects.Training.TrnTagLsn;
import com.simplicite.objects.Training.TrnTagTranslate;
import com.simplicite.objects.Training.TrnUrlRewriting;
//import com.simplicite.commons.Training.TrnVerifyContent;
import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;
import java.io.File;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.io.FilenameUtils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Files;

/**
 * Shared code TrnFsSyncTool
 */
public class TrnFsSyncTool implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private static final String HASHSTORE_FILENAME = "glo.ser";
	
	private final Pattern PATTERN_CATEGORY = Pattern.compile("^CTG_[0-9]+_[a-z\\-]+$");
	private final Pattern PATTERN_LESSON = Pattern.compile("^LSN_[0-9]+_[a-z0-9-]+$");
	private final File contentDir;
	private final File hashStoreFile;
	private final String[] LANG_CODES;
	private final String DEFAULT_LANG_CODE;
	
	private ObjectDB category, categoryContent, lesson, lessonContent, picture, tag, translateTag, trnUrlRewriting;
	
	private HashMap<String, String> hashStore;
	private ArrayList<String> foundPaths;
	private ArrayList<String> existingTags;
	
	Grant g;
	
	public static void dropDbData() throws TrnSyncException{
		TrnFsSyncTool t = new TrnFsSyncTool(Grant.getSystemAdmin());
		t.dropData();
	}
	
	public static void deleteStore() throws TrnSyncException{
		TrnFsSyncTool t = new TrnFsSyncTool(Grant.getSystemAdmin());
		t.deleteHashStore();
	}
	
	public static void triggerSync() throws TrnSyncException{
		TrnFsSyncTool t = new TrnFsSyncTool(Grant.getSystemAdmin());
		t.sync();
	}
	
	public TrnFsSyncTool(Grant g) throws TrnSyncException{
		this.g = g;
		JSONObject conf = new JSONObject(g.getParameter("TRN_CONFIG"));
		if(Tool.isEmpty(conf.getString("filesystem_contentdir")))
			throw new TrnSyncException("TRN_SYNC_EMPTY_CONTENT_PATH");
		contentDir = new File(conf.getString("filesystem_contentdir"));
		hashStoreFile = new File(g.getContentDir()+"/"+HASHSTORE_FILENAME);
		LANG_CODES = TrnTools.getLangs(g);
		DEFAULT_LANG_CODE = TrnTools.getDefaultLang();
	}
	
	public void dropData() throws TrnSyncException{
		try{
			loadTrnObjectAccess();
			dropCategory(false);
			dropTag(false);
		}catch(DeleteException e){
			throw new TrnSyncException("TRN_DROP_ERROR", e.getMessage());
		}
	}

	private void dropCategory(boolean loadAccess) throws DeleteException {
		if(loadAccess) loadTrnObjectAccess();
		ObjectDB cat = g.getTmpObject("TrnCategory");
		synchronized(cat){
			cat.resetFilters();
			cat.setFieldFilter("trnCatId.trnCatPath", "is null");
			for(String[] row : cat.search()){
				cat.setValues(row);
				(new BusinessObjectTool(cat)).delete();
			}
		}
	}

	private void dropTag(boolean loadAccess) throws DeleteException {
		if(loadAccess) loadTrnObjectAccess();
		ObjectDB tag = g.getTmpObject("TrnTag");
		synchronized(tag){
			tag.resetFilters();
			for(String[] row : tag.search()){
				tag.setValues(row);
				(new BusinessObjectTool(tag)).delete();
			}
		}
	}
	
	private void deleteHashStore() throws TrnSyncException{
		try{
			Files.deleteIfExists(hashStoreFile.toPath());
		} catch(Exception e){
			AppLog.error(getClass(), "deleteStore", e.getMessage(), e, Grant.getSystemAdmin());
			throw new TrnSyncException("TRN_SYNC_DELETE_STORE", hashStoreFile);
		}
	}
	
	// Main sync algorithm : injects contents at `contentDir` into DB
	public void sync() throws TrnSyncException{
		// load (from glo.ser) a hashmap of the checksum of files for each directory, so we know which have changed
		loadHashes();
		// verify the structure (recursive)
		verifyContentStructure();
		// initialize some usefull objects
		foundPaths = new ArrayList<>();
		existingTags = new ArrayList<>();
		loadTrnObjects();
		// injects contents at `contentDir` into DB, sets new hashes (recursive)
		syncPath("/");
		// deletes from DB paths that are not in the file structure anymore
		deleteDeleted();
		// save new hashes (in glo.ser)
		// NB: if there was an exception above, glo.ser not modified => sync() would rerun similarly
		storeHashes();
	}
	
	public void verifyContentStructure() throws TrnSyncException{
		TrnVerifyContent.verifyContentStructure(contentDir, g);
	}
	
	// private void verifyFolderStructure(File dir, boolean isRoot) throws TrnSyncException{
	// 	if(!dir.isDirectory())
	// 		throw new TrnSyncException("TRN_SYNC_NOT_DIR", dir);
			
	// 	if(isRoot)
	// 		validateRootContent(dir);
	// 	else if(isLesson(dir))
	// 		validateLessonContent(dir);
	// 	else if(isCategory(dir))
	// 		validateCategoryContent(dir);
	// 	else
	// 		throw new TrnSyncException("TRN_SYNC_UNKNOWN_DIR_TYPE", dir);
		
	// 	if(isRoot || isCategory(dir))	
	// 		for(File child : dir.listFiles())
	// 			if(child.isDirectory())
	// 				verifyFolderStructure(child, false);
	// }
	
	// private void validateRootContent(File dir) throws TrnSyncException{
	// 	for(File child : dir.listFiles())
	// 		if(!isCategory(child) && !child.getName().equals("tags.json") && !child.getName().equals("url_rewriting.json"))
	// 			throw new TrnSyncException("TRN_SYNC_ROOT_NON_CONFORMITY", child);
	// 		else if (child.getName().equals("tags.json")) {
	// 			// check if tags.json has a correct structure
	// 			try {
	// 				JSONArray tags = new JSONArray(FileTool.readFile(dir.getPath()+"/tags.json"));
	// 				for(int i = 0; i < tags.length(); i++) {
	// 					JSONObject tag = tags.getJSONObject(i);
	// 					if(Tool.isEmpty(tag.getString("code")) && Tool.isEmpty(tag.getJSONObject("translation"))) {
	// 						throw new Exception();
	// 					}
	// 				}
	// 			} catch(Exception e) {
	// 				throw new TrnSyncException("TRN_SYNC_ROOT_TAGS_JSON_NON_CONFORMITY", dir);
	// 			}
	// 		}
	// }
	
	// private void validateCategoryContent(File dir) throws TrnSyncException{
	// 	boolean hasJson = false;
	// 	for(File child : dir.listFiles()){
	// 		if("category.json".equals(child.getName()))
	// 			hasJson = true;
	// 		else if(!isCategory(child) && !isLesson(child))
	// 			throw new TrnSyncException("TRN_SYNC_CATEGORY_NON_CONFORMITY", child);
	// 	}
	// 	if(!hasJson)
	// 		throw new TrnSyncException("TRN_SYNC_CATEGORY_JSON_MISSING", dir);
	// }
	
	// private void validateLessonContent(File dir) throws TrnSyncException{
	// 	String lessonCode = getLsnCode(dir);
	// 	boolean hasJson = false;
	// 	for(File child : dir.listFiles()){
	// 		if("lesson.json".equals(child.getName()))
	// 			hasJson = true;
	// 		else if(child.isDirectory())
	// 			throw new TrnSyncException("TRN_SYNC_LESSON_CONTAINING_FOLDER", child);
	// 		else if(isMarkdown(child) && !lessonCode.equals(getLocaleStrippedBaseName(child)))
	// 			throw new TrnSyncException("TRN_SYNC_LESSON_MARDOWN_NAME_INCONSISTENT", child);
	// 		else if(isVideo(child) && !lessonCode.equals(getLocaleStrippedBaseName(child)))	
	// 			throw new TrnSyncException("TRN_SYNC_LESSON_VIDEO_NAME_INCONSISTENT", child);
	// 		else if(!isPic(child) && !isMarkdown(child) && !isVideo(child))
	// 			throw new TrnSyncException("TRN_SYNC_FILETYPE_NOT_ALLOWED", child);
	// 	}
	// 	if(!hasJson)
	// 		throw new TrnSyncException("TRN_SYNC_LESSON_JSON_MISSING", dir);
	// }
	
	// private boolean isMarkdown(File f){
	// 	return FileTool.getExtension(f.getName()).toLowerCase().equals("md");
	// }
	
	private boolean isPic(File f){
		String extension = FilenameUtils.getExtension(f.getName()).toLowerCase();
		return "png".equals(extension) || "jpg".equals(extension);
	}
	
	// private boolean isVideo(File f){
	// 	return FileTool.getExtension(f.getName()).toLowerCase().equals("webm");
	// }
	
	private boolean isCategory(File dir){
		return dir.isDirectory() && isCategory(dir.getName());
	}
	
	private boolean isCategory(String path){
		return PATTERN_CATEGORY.matcher(path).matches();
	}
	
	private boolean isLesson(File dir){
		return dir.isDirectory() && isLesson(dir.getName());
	}
	
	private boolean isLesson(String path){
		return PATTERN_LESSON.matcher(path).matches();
	}
	
	private void deleteDeleted(){
		for(String path : hashStore.keySet())
			if(!foundPaths.contains(path))
				deleteForPath(path);
		deleteDeletedTags();
	}

	private void deleteDeletedTags() {
		BusinessObjectTool trnTag = new BusinessObjectTool(tag);
		tag.resetFilters();
		for(String[] row : tag.search()) {
			String rowId = row[0];
			String code = row[1];
			if(!existingTags.contains(code)) {
				deleteTag(rowId, trnTag);
			}
		}
	}

	private void deleteTag(String rowId, BusinessObjectTool trnTag) {
		try {
			synchronized(trnTag.getObject()) {
				trnTag.getForDelete(rowId);
				trnTag.delete();
			}
		} catch(Exception e) {
			AppLog.warning(getClass(), "deleteTag", "TRN_WARN_TAG_NOT_EXISTANT_IN_DB", e, g);
		}
	}

	// private void deleteTagTranslation(String rowId, TrnTagTranslate tagLsnTranslateObject) {
	// 	BusinessObjectTool trnTagTranslate = new BusinessObjectTool(translateTag);
	// 	try {
	// 		synchronized(trnTagTranslate.getObject()) {
	// 			for(String[] row : tagLsnTranslateObject.getTranslatesFromTagId(rowId)) {
	// 				trnTagTranslate.getForDelete(row[0]);
	// 				trnTagTranslate.delete();
	// 			}
	// 		}
	// 	} catch(Exception e) {
	// 		AppLog.warning(getClass(), "deleteTagTranslation", "TRN_WARN_TAG_TRANSLATION_NOT_EXISTANT_IN_DB", e, g);
	// 	}
	// } 
	
	private void deleteForPath(String path){
		BusinessObjectTool bot;
		String id;
		if(isCategory(path)){
			bot = new BusinessObjectTool(category);
			id = getCatRowIdFromPath(path);
		}
		else{
			bot = new BusinessObjectTool(lesson);
			id = getLsnRowIdFromPath(path);
		}
		
		try{
			synchronized(bot.getObject()){
				bot.getForDelete(id);
				bot.delete();
			}
		}
		catch(Exception e){
			AppLog.warning(getClass(), "deleteForPath", "TRN_WARN_DEL_PATH_NOT_EXISTANT_IN_DB", e, g);
		}
	}
	
	private void loadTrnObjectAccess(){
		boolean[] crud = new boolean[]{true, true, true, true};
		g.changeAccess("TrnCategory", crud);
		g.changeAccess("TrnCategoryTranslate", crud);
		g.changeAccess("TrnLesson", crud);
		g.changeAccess("TrnLsnTranslate", crud);
		g.changeAccess("TrnPicture", crud);
		g.changeAccess("TrnTag", crud);
		g.changeAccess("TrnTagLsn", crud);
		g.changeAccess("TrnTagTranslate", crud);

	}
	
	private void unloadTrnObjectAccess(){
		boolean[] crud = new boolean[]{false, false, false, false};
		g.changeAccess("TrnCategory", crud);
		g.changeAccess("TrnCategoryTranslate", crud);
		g.changeAccess("TrnLesson", crud);
		g.changeAccess("TrnLsnTranslate", crud);
		g.changeAccess("TrnPicture", crud);
		g.changeAccess("TrnTag", crud);
		g.changeAccess("TrnTagLsn", crud);
		g.changeAccess("TrnTagTranslate", crud);
	}
	
	private void loadTrnObjects(){
		loadTrnObjectAccess();
		category = g.getObject("sync_TrnCategory", "TrnCategory");
		categoryContent = g.getObject("sync_TrnCategoryTranslate", "TrnCategoryTranslate");
		lesson = g.getObject("sync_TrnLesson", "TrnLesson");
		lessonContent = g.getObject("sync_TrnLsnTranslate", "TrnLsnTranslate");
		picture = g.getObject("sync_TrnPicture", "TrnPicture");
		tag = g.getObject("sync_TrnTag", "TrnTag");
		translateTag = g.getObject("sync_TrnTagTranslate", "TrnTagTranslate");
		trnUrlRewriting = g.getObject("sync_TrnUrlRewriting", "TrnUrlRewriting");
	}
	
	private void syncPath(String relativePath) throws TrnSyncException{
		File dir = new File(contentDir.getPath()+relativePath);
	
		String oldHash = hashStore.get(relativePath);
		String newHash = "";
		try{
			newHash = hashDirectoryFiles(dir);
		}
		catch(Exception e){
			throw new TrnSyncException("TRN_SYNC_ERROR_HASHING_FILES", relativePath);
		}
		
		if(oldHash==null || !oldHash.equals(newHash))
			if("/".equals(relativePath)) {
				upsertTags(dir);
				upsertUrlsRewriting(dir);
			}
			else updateDbWithDir(dir);
		
		hashStore.put(relativePath, newHash);
		foundPaths.add(relativePath);
		
		File[] files = dir.listFiles();
		for(File file : files)
			if(file.isDirectory())
				syncPath(relativePath+file.getName()+"/");
	}
	
	private void updateDbWithDir(File dir) throws TrnSyncException{
		if(isCategory(dir))
			upsertCategory(dir);
		else if(isLesson(dir))
			upsertLesson(dir);
		else
			throw new TrnSyncException("TRN_SYNC_ERROR_NOT_CATEGORY_NOR_LESSON", dir);
	}

	private void upsertTags(File dir) throws TrnSyncException {
		try {
			JSONArray json = new JSONArray(FileTool.readFile(dir.getPath()+"/tags.json"));
			// delete all tags
			BusinessObjectTool bot = new BusinessObjectTool(tag);
			synchronized(tag){
				tag.resetFilters();
				for(String[] row: tag.search()){
					tag.setValues(row);
					bot.delete();
				}
			}
			for (int i = 0; i < json.length(); i++) {
				JSONObject tagObject = json.getJSONObject(i);
				String tagCode = tagObject.optString("code");
				String rowId = getTagRowIdFromCode(tagCode);
				if(Tool.isEmpty(rowId)) {
					upsertTag(tagCode);
					rowId = tag.getRowId();
				}

				// clean translation records
				//if(!Tool.isEmpty(getTagTranslateRowId(rowId))) deleteTagTranslation(rowId, tagTranslate); 
				// loop on the translate object
				JSONObject tradObj = tagObject.optJSONObject("translation");
				for(String lang : LANG_CODES) {
					if(tradObj.has(lang)) {
						String translation = tradObj.getString(lang);
						upsertTranslate(rowId, lang, translation);
					}
				}
				existingTags.add(tagCode);
			}
		} catch(Exception e) {
			AppLog.error(getClass(), "upsertTags", e.getMessage(), e, g);
			throw new TrnSyncException("TRN_SYNC_UPSERT_TAGS_FROM_JSON");
		}          
	}

	private void upsertTranslate(String tagRowId, String lang, String translation) throws TrnSyncException {
		try {
			BusinessObjectTool bot = new BusinessObjectTool(translateTag);
			synchronized(translateTag) {
				translateTag.resetValues();
				translateTag.setFieldValue("trnTagTranslateLang", lang);
				translateTag.setFieldValue("trnTagTranslateTrad", translation);
				translateTag.setFieldValue("trnTaglangTagId", tagRowId);
				bot.validateAndSave();
			}
		} catch(Exception e) {
			AppLog.error(getClass(), "upsertTagTranslate", e.getMessage(), e, g);
			throw new TrnSyncException("TRN_SYNC_UPSERT_TRANSLATE");
		}
	}

	private void upsertTag(String code) throws TrnSyncException {
		try {
			BusinessObjectTool bot = new BusinessObjectTool(tag);
			synchronized(tag) {
				tag.resetValues();
				tag.setFieldValue("trnTagCode", code);
				bot.validateAndSave();
			}
		} catch(Exception e) {
			throw new TrnSyncException("TRN_SYNC_UPSERT_TAG", e.getMessage()+" "+ code);
		}
	}

	private void upsertUrlsRewriting(File dir)throws TrnSyncException {
		try {
			JSONArray json = new JSONArray(FileTool.readFile(dir.getPath()+"/url_rewriting.json"));
			TrnUrlRewriting urlRewriting = (TrnUrlRewriting) g.getObject("sync_TrnUrlRewriting", "TrnUrlRewriting");
			// delete all records of TrnUrlRewriting
			BusinessObjectTool bot = new BusinessObjectTool(urlRewriting);
			synchronized(urlRewriting){
				urlRewriting.resetFilters();
				for(String[] row: urlRewriting.search()){
					urlRewriting.setValues(row);
					bot.delete();
				}
			}
			// test
			for (int i = 0; i < json.length(); i++) {
				JSONObject jsonRecord = json.getJSONObject(i);
				String sourceUrl = jsonRecord.optString("sourceUrl");
				String destination = jsonRecord.optString("destinationUrl");
				upsertUrlRewriting(sourceUrl, destination);
			}
		} catch(Exception e) {
			AppLog.error(getClass(), "upsertTags", e.getMessage(), e, g);
			throw new TrnSyncException("TRN_SYNC_UPSERT_URLS_REWRITING_FROM_JSON");
		}    
	}

	private void upsertUrlRewriting(String sourceUrl, String destinationUrl) throws TrnSyncException {
		try {
			BusinessObjectTool bot = new BusinessObjectTool(trnUrlRewriting);
			synchronized(trnUrlRewriting) {
				trnUrlRewriting.resetValues();
				trnUrlRewriting.setFieldValue("trnSourceUrl", sourceUrl);
				trnUrlRewriting.setFieldValue("trnDestinationUrl", destinationUrl);

				bot.validateAndSave();
			}
		} catch(Exception e) {
			throw new TrnSyncException("TRN_SYNC_UPSERT_URL_REWRITING", e.getMessage());
		}
	}
	
	private void upsertCategory(File dir) throws TrnSyncException{
		try{
			String relativePath = getRelativePath(dir);
			String rowId = getCatRowIdFromPath(relativePath);
			String[] name = dir.getName().split("_");
			
			JSONObject json = new JSONObject(FileTool.readFile(dir.getPath()+"/category.json"));
			BusinessObjectTool bot = new BusinessObjectTool(category);
			synchronized(category){
				category.resetValues();
				if(Tool.isEmpty(rowId))
					bot.selectForCreate();
				else
					bot.selectForUpdate(rowId);
				category.setFieldValue("trnCatOrder", name[1]);
				category.setFieldValue("trnCatPublish", jsonCatPublish(json));
				category.setFieldValue("trnCatCode", name[2]);
				category.setFieldValue("trnCatPath", relativePath);
				category.setFieldValue("trnCatId", getCatRowIdFromPath(getParentRelativePath(dir)));
				bot.validateAndSave();
				rowId = category.getRowId();
			}
			
			//recreate contents
			bot = new BusinessObjectTool(categoryContent);
			synchronized(categoryContent){
				//delete all content
				categoryContent.resetFilters();
				categoryContent.setFieldFilter("trnCtrCatId",rowId);
				for(String[] row: categoryContent.search()){
					categoryContent.setValues(row);
					bot.delete();
				}
				//create all content
				for(String[] row : jsonCatContents(json)){
					bot.getForCreate();
					bot.getObject().setValuesFromJSONObject(new JSONObject() // or its alias getForUpsert 
						.put("trnCtrLang", row[0])
						.put("trnCtrCatId", rowId)
						.put("trnCtrTitle", row[1])
						.put("trnCtrDescription", row[2])
					, false, false);
					bot.validateAndCreate();
				}
			}
		}
		catch(Exception e){
			AppLog.error(getClass(), "upsertCategory", e.getMessage(), e, g);
			throw new TrnSyncException("TRN_SYNC_UPSERT_CATEGORY", e.getMessage()+" "+category.toJSON()+ " "+dir.getPath());
		}
	}
	
	private boolean jsonCatPublish(JSONObject json){
		return json.optBoolean("published", true);
	}
	
	private List<String[]> jsonCatContents(JSONObject json){
		List<String[]> l = new ArrayList<>();
		for(String lang : LANG_CODES){
			if(json.has(lang)){
				JSONObject content = json.getJSONObject(lang);
				l.add(new String[]{
					lang,
					content.getString("title"),
					content.optString("description", "")
				});
			}
				
		}
		return l;
	}
	
	private String getRelativePath(File dir){
		return dir.getPath().replace(contentDir.getPath(), ""); //convert to substring
	}
	
	private String getParentRelativePath(File dir){
		return getRelativePath(dir.getParentFile());
	}
	
	private String getCatRowIdFromPath(String path){
		return Tool.isEmpty(path) ? "" : g.simpleQuery("select row_id from trn_category where trn_cat_path='"+path+"'");
	}
	
	private String getLsnRowIdFromPath(String path){
		return Tool.isEmpty(path) ? "" : g.simpleQuery("select row_id from trn_lesson where trn_lsn_path='"+path+"'");
	}
	
	private void upsertLesson(File dir) throws TrnSyncException{
		try{
			String relativePath = getRelativePath(dir);
			String rowId = getLsnRowIdFromPath(relativePath);
			
			JSONObject json = getLsnData(dir);
			
			BusinessObjectTool bot = new BusinessObjectTool(lesson);
			synchronized(lesson){
				lesson.resetValues();
				if(!Tool.isEmpty(rowId)){
					bot.selectForDelete(rowId);
					bot.delete();
				}
				
				bot.selectForCreate();
				lesson.setFieldValue("trnLsnOrder", json.getString("order"));
				lesson.setFieldValue("trnLsnCode", json.getString("code"));
				lesson.setFieldValue("trnLsnPath", relativePath);
				lesson.setFieldValue("trnLsnCatId", getCatRowIdFromPath(getParentRelativePath(dir)));
				lesson.setFieldValue("trnLsnPublish", json.getBoolean("published"));
				lesson.setFieldValue("trnLsnVisualization", json.getString("viz"));
				
				lesson.populate(true);
				bot.validateAndSave(true);
				rowId = lesson.getRowId();
			}

			// create tag N-N lesson if tag exists and if association does not already exist
			TrnTagLsn tagLsn = (TrnTagLsn) g.getObject("sync_TrnTagLsn", "TrnTagLsn");
			bot = new BusinessObjectTool(tagLsn);
			JSONArray tags = json.optJSONArray("tags");
			if(!Tool.isEmpty(tags) && tags != null) {
				for(int i = 0; i < tags.length(); i++) {
					String tagCode = tags.getString(i);
					String tagRowId = getTagRowIdFromCode(tagCode);
					if(Tool.isEmpty(tagRowId)) throw new TrnSyncException("TRN_SYNC_UPSERT_TAG_LSN", "tag does not exist" + tagCode);
					String lsnRowId = getLsnRowIdFromPath(relativePath);
					if(Tool.isEmpty(lsnRowId)) throw new TrnSyncException("TRN_SYNC_UPSERT_TAG_LSN", "lesson does not exist" + tagCode);
					String tagLsnRowId = tagLsn.getTagLsnRowId(tagRowId, lsnRowId);
					if(Tool.isEmpty(tagLsnRowId)) {
						bot.selectForCreate();
						tagLsn.setFieldValue("trnTaglsnLsnId", rowId);
						tagLsn.setFieldValue("trnLsnPath", relativePath);
						tagLsn.setFieldValue("trnTaglsnTagId", tagRowId);
						bot.validateAndCreate();
					}
				}
			}
			
			// create contents
			for(String lang : LANG_CODES){
				if(json.getJSONObject("contents").has(lang)){
					JSONObject content = json.getJSONObject("contents").getJSONObject(lang);
					
					bot = new BusinessObjectTool(lessonContent);
					synchronized(lessonContent){
						bot.selectForCreate();
						lessonContent.setFieldValue("trnLtrLsnId", rowId);
						lessonContent.setFieldValue("trnLtrLang", lang);
						if(content.has("markdown"))
							lessonContent.setFieldValue("trnLtrContent", FileTool.readFile(new File(content.getString("markdown"))));
						if(content.has("video")){
							File video = new File(content.getString("video"));
							lessonContent.getField("trnLtrVideo").setDocument(lessonContent, video.getName(), new FileInputStream(video));
						}
						if(content.has("title"))
							lessonContent.setFieldValue("trnLtrTitle", content.getString("title"));
						if(content.has("description"))
							lessonContent.setFieldValue("trnLtrDescription", content.getString("description"));
						bot.validateAndCreate();
					}
					
					if(content.has("pics")){
						JSONArray pics = content.getJSONArray("pics");
						
						bot = new BusinessObjectTool(picture);
						synchronized(picture){
							for(int i=0; i<pics.length(); i++){
								File f = new File(pics.getString(i));
								picture.resetValues();
								picture.getField("trnPicImage").setDocument(picture, f.getName(), new FileInputStream(f));
								picture.setFieldValue("trnPicLang", lang);
								picture.setFieldValue("trnPicLsnId", rowId);
								bot.validateAndCreate();
							}
						}
					}
				}
			}
		}
		catch(Exception e){
			AppLog.error(getClass(), "upsertCategory", e.getMessage(), e, g);
			throw new TrnSyncException("TRN_SYNC_UPSERT_LESSON", e.getMessage()+" "+lesson.toJSON()+ " "+dir.getPath());
		}
	}

	private String getTagRowIdFromCode(String code) {
		return Tool.isEmpty(code) ? "" : g.simpleQuery("select row_id from trn_tag where trn_tag_code='"+code+"'");
	}

	// private String getTagTranslateRowId(String tagRowId) {
	// 	if(Tool.isEmpty(tagRowId)) return "";
	// 	return g.simpleQuery("select row_id from trn_tag_translate where trn_taglang_tag_id='"+tagRowId+"'");
	// }
	
	private File getLsnMdFile(File lsnDir, String lang){
		return getLsnFile(lsnDir, lang, "md");
	}
	
	private File getLsnVideoFile(File lsnDir, String lang){
		return getLsnFile(lsnDir, lang, "webm");
	}	
	
	private File getLsnFile(File lsnDir, String lang, String extension){
		lang = DEFAULT_LANG_CODE.equals(lang) ? "" : "_"+lang;
		return new File(lsnDir.getPath()+"/"+getLsnCode(lsnDir)+lang+"."+extension);
	}
	
	private JSONObject getPics(File lsnDir){
		JSONObject pics = new JSONObject();
		for(String lang : LANG_CODES)
			pics.put(lang, new JSONArray());
		
		for(File f : lsnDir.listFiles())
			if(isPic(f))
				pics.getJSONArray(getLocale(f)).put(f.getPath());
		
		return pics;
	}
	
	// private String getLocaleStrippedBaseName(File f){
	// 	String baseName = FilenameUtils.getBaseName(f.getName());
	// 	String[] split = baseName.toUpperCase().split("_");
	// 	String locale = split.length>0 ? split[split.length-1] : null;
	// 	return locale!=null && Arrays.asList(LANG_CODES).contains(locale) ? baseName.replace("_"+locale, "") : baseName;
	// }
	
	private String getLocale(File f){
		String[] split = FilenameUtils.getBaseName(f.getName()).toUpperCase().split("_");
		String locale = split.length>0 ? split[split.length-1] : null;
		return locale!=null && Arrays.asList(LANG_CODES).contains(locale) ? locale : DEFAULT_LANG_CODE;
	}
	
	private String getLsnCode(File lsnDir){
		return lsnDir.getName().split("_")[2];
	}
	
	private String getLsnOrder(File lsnDir){
		return lsnDir.getName().split("_")[1];
	}
	
	private JSONObject getLsnData(File dir) throws Exception{
		JSONObject lsn = new JSONObject();
		
		lsn.put("order", getLsnOrder(dir));
		lsn.put("code", getLsnCode(dir));
		
		JSONObject json = new JSONObject(FileTool.readFile(dir.getPath()+"/lesson.json"));
		lsn.put("published", json.optBoolean("published", true));
		lsn.put("viz", json.optString("display", "TUTO"));
		lsn.put("tags", json.optJSONArray("tags"));
		
		JSONObject contents = new JSONObject();
		JSONObject content;
		File f;
		
		JSONObject pictures = getPics(dir);
		
		for(String lang : LANG_CODES){
			if(json.has(lang)){
				content = json.getJSONObject(lang);
				
				// add markdown content
				f = getLsnMdFile(dir, lang);
				if(f.exists())
					content.put("markdown", f.getPath());
				
				// add video file
				f = getLsnVideoFile(dir, lang);
				if(f.exists())
					content.put("video", f.getPath());
				
				// add pics
				if(pictures.getJSONArray(lang).length()>0)
					content.put("pics", pictures.getJSONArray(lang));
					
				contents.put(lang, content);
			}
		}
		
		lsn.put("contents", contents);
		return lsn;
	}
	
	// Non-recursive version of https://stackoverflow.com/a/46899517/1612642 => ignores directories
    private static String hashDirectoryFiles(File directory) throws java.io.IOException
    {
        if (!directory.isDirectory())
            throw new IllegalArgumentException("Not a directory");

        Vector<FileInputStream> fileStreams = new Vector<>();
        
        File[] files = directory.listFiles();
        if (files != null){
            Arrays.sort(files, Comparator.comparing(File::getName));
            for(File file : files)
                if (!file.isDirectory())
                    fileStreams.add(new FileInputStream(file));
        }

        try (SequenceInputStream sequenceInputStream = new SequenceInputStream(fileStreams.elements()))
        {
            return DigestUtils.md5Hex(sequenceInputStream);
        }
    }
	
	private void loadHashes() throws TrnSyncException{
		if(!hashStoreFile.exists() && !isTrainingDbEmpty())
			throw new TrnSyncException("TRN_SYNC_NO_HASHSTORE", "The glo.ser file could not be found. Please empty the training objects manually before syncronization with filesystem.");
		else if(!hashStoreFile.exists())
			hashStore = new HashMap<>();
		else
			hashStore = (HashMap<String, String>) Tool.fileToObject(hashStoreFile);
	}
	
	private void storeHashes() throws TrnSyncException{
		Tool.objectToFile(hashStore, hashStoreFile);
	}
	
	private boolean isTrainingDbEmpty(){
		return 0==g.simpleQueryAsDouble("select count(*) from trn_category");
	}
}