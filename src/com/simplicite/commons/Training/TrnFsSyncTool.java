package com.simplicite.commons.Training;

import com.simplicite.objects.Training.TrnTagLsn;
import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.ObjectDB;
import com.simplicite.util.Tool;
import com.simplicite.util.engine.Platform;
import com.simplicite.util.exceptions.CreateException;
import com.simplicite.util.exceptions.DeleteException;
import com.simplicite.util.exceptions.GetException;
import com.simplicite.util.exceptions.ValidateException;
import com.simplicite.util.tools.BusinessObjectTool;
import com.simplicite.util.tools.FileTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.io.FilenameUtils;

import org.apache.commons.codec.digest.DigestUtils;



/**
 * Shared code TrnFsSyncTool
 */
public class TrnFsSyncTool implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private static final String HASHSTORE_FILENAME = "glo.ser";
    private static final String CONTENT_FOLDERNAME = "docs/content";
    private static final String TAG_JSON_NAME = "tags.json";
    private static final String URL_REWRITING_JSON_NAME = "url_rewriting.json";
    private static final String THEME_JSON_NAME = "theme.json";
	
	private final File contentDir;
	private final File hashStoreFile;
	private final String[] langCodes;
	private final String defaultLangCode;
	
	private ObjectDB category;
	private ObjectDB categoryContent;
	private ObjectDB lesson;
	private ObjectDB lessonContent;
	private ObjectDB picture;
	private ObjectDB tag;
    private TrnTagLsn tagLsn;
	private ObjectDB tagTranslation;
	private ObjectDB urlRewriting;
	private ObjectDB theme;
	private ObjectDB page;
	
	private HashMap<String, String> hashStore;
    // the following attributes are used to keep track of the content diffs and remove deleted content
	private ArrayList<String> foundPaths;
	private ArrayList<String> foundTags;
    private ArrayList<String> foundTagsTranslations;
	private ArrayList<String> foundUrlsRewriting;
	
	Grant g;
	
	public static void dropDbData() throws TrnSyncException {
		TrnFsSyncTool t = new TrnFsSyncTool(Grant.getSystemAdmin());
		t.dropData();
	}
	
	public static void deleteStore() throws TrnSyncException {
		TrnFsSyncTool t = new TrnFsSyncTool(Grant.getSystemAdmin());
		t.deleteHashStore();
	}
	
	public static void triggerSync() throws TrnSyncException {
		TrnFsSyncTool t = new TrnFsSyncTool(Grant.getSystemAdmin());
		t.sync();
	}
	
	public TrnFsSyncTool(Grant g) {
		this.g = g;
        String baseContentDir = Platform.getContentDir();
		contentDir = new File(baseContentDir, CONTENT_FOLDERNAME);
		hashStoreFile = new File(baseContentDir, HASHSTORE_FILENAME);
		langCodes = TrnTools.getLangs(g);
		defaultLangCode = TrnTools.getDefaultLang();
	}
	
	public void dropData() throws TrnSyncException{
		try{
			loadTrnObjects();
			dropCategory(false);
			dropTag(false);
            AppLog.info("Successfully droped data", g);
		}catch(DeleteException e){
			throw new TrnSyncException("TRN_DROP_ERROR", e.getMessage());
		}
	}

	private void dropCategory(boolean loadAccess) throws DeleteException {
		if(loadAccess) {
            loadTrnObjectAccess();
        } 
		synchronized(category){
			category.resetFilters();
			category.setFieldFilter("trnCatId.trnCatPath", "is null");
			for(String[] row : category.search()){
				category.setValues(row);
				(new BusinessObjectTool(category)).delete();
			}
		}
	}

	private void dropTag(boolean loadAccess) throws DeleteException {
		if(loadAccess) {
            loadTrnObjectAccess();
        } 
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
		initFoundArrays();
		loadTrnObjects();
		// injects contents at `contentDir` into DB, sets new hashes (recursive)
		syncPath("/");
        upsertHomepage(); // homepage content depends on the lesson homepage, so it must be executed after syncPath
        AppLog.info("Content has been synced", g);
        
		// deletes from DB paths that are not in the file structure anymore
        AppLog.info("Removing deleted items...", g);
		deleteDeleted();
		// save new hashes (in glo.ser)
		// NB: if there was an exception above, glo.ser not modified => sync() would rerun similarly
		storeHashes();
        AppLog.info("Synchronization done", g);
	}
	
	public void verifyContentStructure() throws TrnSyncException{
		TrnVerifyContent.verifyContentStructure(contentDir, TrnTools.getLangs(g));
        AppLog.info("Training content has been verified", g);
	}
	
    private void initFoundArrays() {
        foundPaths = new ArrayList<>();
		foundTags = new ArrayList<>();
        foundTagsTranslations = new ArrayList<>();
		foundUrlsRewriting = new ArrayList<>();
    }
	
    // deletes the elements that do not exists anymore
	private void deleteDeleted(){
        deleteDeletedPaths();
        deleteDiff(foundTags, tag, "TAG");
		deleteDiff(foundTagsTranslations, tagTranslation, "TAG_TRANSLATION");
        deleteDiff(foundUrlsRewriting, urlRewriting, "URL_REWRITING");
	}

    private void deleteDeletedPaths() {
        for(String path : hashStore.keySet()) {
            if(!foundPaths.contains(path) && path.equals(TAG_JSON_NAME) && path.equals(URL_REWRITING_JSON_NAME) && path.equals(THEME_JSON_NAME)) {
				deletePath(path);
            }
        }
    }

	private void deletePath(String path) {
		BusinessObjectTool bot;
		String rowId;
        String object;
		if(TrnVerifyContent.isCategory(path)){
			bot = new BusinessObjectTool(category);
			rowId = getCatRowIdFromPath(path);
            object = "CATEGORY";
		}
		else{
			bot = new BusinessObjectTool(lesson);
			rowId = getLsnRowIdFromPath(path);
            object = "LESSON";
		}
        deleteRecord(rowId, bot, object);
        hashStore.remove(path);
	}

    private void deleteDiff(ArrayList<String> foundObject, ObjectDB object, String objectName) {
        if(foundObject.isEmpty()) return;
		BusinessObjectTool bot = new BusinessObjectTool(object);
		object.resetFilters();
		for(String[] row : object.search()) {
			object.resetValues();
			object.setValues(row);
			String rowId = object.getRowId();
			if(!foundObject.contains(rowId)) {
				deleteRecord(rowId, bot, objectName);
			}
		}
    }

    private void deleteRecord(String rowId, BusinessObjectTool bot, String objectName) {
		try {
			synchronized(bot.getObject()) {
				bot.getForDelete(rowId);
				bot.delete();
			}
		} catch(Exception e) {
			AppLog.warning(getClass(), "deleteRecord", "TRN_WARN_UNABLE_TO_DELETE_"+objectName+": row_id="+rowId, e, g);
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
        g.changeAccess("TrnPage", crud);
        g.changeAccess("TrnSiteTheme", crud);
        g.changeAccess("TrnPage", crud);
	}
	
	private void loadTrnObjects(){
		loadTrnObjectAccess();
		category = g.getObject("sync_TrnCategory", "TrnCategory");
		categoryContent = g.getObject("sync_TrnCategoryTranslate", "TrnCategoryTranslate");
		lesson = g.getObject("sync_TrnLesson", "TrnLesson");
		lessonContent = g.getObject("sync_TrnLsnTranslate", "TrnLsnTranslate");
		picture = g.getObject("sync_TrnPicture", "TrnPicture");
		tag = g.getObject("sync_TrnTag", "TrnTag");
        tagLsn = (TrnTagLsn) g.getObject("sync_TrnTagLsn", "TrnTagLsn");
		tagTranslation = g.getObject("sync_TrnTagTranslate", "TrnTagTranslate");
		urlRewriting = g.getObject("sync_TrnUrlRewriting", "TrnUrlRewriting");
        theme = g.getObject("sync_TrnSiteTheme", "TrnSiteTheme");
        page = g.getObject("sync_TrnPage", "TrnPage");
	}
	
	private void syncPath(String relativePath) throws TrnSyncException{
        File dir = new File(contentDir.getPath()+relativePath);
        String newHash = getNewHash(relativePath, dir);
        if(newHash != null) {
            if("/".equals(relativePath)) {
                // todo, implement hashStore with these specific files (tags.json, theme.json etc..)
                // currently it only checks if the root directy has changed, and if so it triggers all the following methods
                syncTags();
                syncUrlsRewriting();
                syncTheme();
			} else {
                updateDbWithDir(dir);
            }
            hashStore.put(relativePath, newHash);
        }
	
        // add every found path, even if it has not changed since last sync
        // so we can compare the differences with glo.ser keys (old paths) and remove the deleted path
		foundPaths.add(relativePath);
		
		File[] files = dir.listFiles();
		for(File file : files) {
            if(file.isDirectory()) {
				syncPath(relativePath+file.getName()+"/");
            }
        }
			
	}
	
	private void updateDbWithDir(File dir) throws TrnSyncException{
		if(TrnVerifyContent.isCategory(dir)) {
			upsertCategory(dir);
        }
		else if(TrnVerifyContent.isLesson(dir)) {
            upsertLessonAndContent(dir);
        }
		else {
			throw new TrnSyncException("TRN_SYNC_ERROR_NOT_CATEGORY_NOR_LESSON", dir);
        }
	}

    private void syncTags() throws TrnSyncException {
        File f = new File(contentDir.getPath(), TAG_JSON_NAME);
        String newHash = getNewHash(TAG_JSON_NAME, f);
        if(newHash != null) {
            AppLog.info("Tags modifications detected, Syncing...", g);
            upsertTags(f);
            hashStore.put(TAG_JSON_NAME, newHash);
            AppLog.info("Tags successfully synced", g);
        }
    }

	private void upsertTags(File jsonFile) throws TrnSyncException {
		try {
			JSONArray json = new JSONArray(FileTool.readFile(jsonFile));
			
            for (int i = 0; i < json.length(); i++) {
				JSONObject tagObject = json.getJSONObject(i);
				String tagCode = tagObject.optString("code");
				String rowId = upsertTag(tagCode);
                foundTags.add(rowId); // to keep track of added tags, needed to clean deleted tags
                JSONObject tradJSON = tagObject.optJSONObject("translation");
                upsertTagTranslations(tradJSON, rowId);
			}
		} catch(Exception e) {
			throw new TrnSyncException("TRN_SYNC_UPSERT_TAGS_FROM_JSON", e.getMessage());
		}          
	}

    private String upsertTag(String code) throws TrnSyncException {
		try {
			String rowId = getTagRowIdFromCode(code);
			BusinessObjectTool bot = new BusinessObjectTool(tag);
			if(!Tool.isEmpty(rowId)) {
                return rowId;
            }
			synchronized(tag) {
                bot.selectForCreate();
				tag.resetValues();
				tag.resetValues();
				tag.setFieldValue("trnTagCode", code);
				bot.validateAndSave();
                rowId = tag.getRowId();
				return rowId;
			}
		} catch(Exception e) {
			throw new TrnSyncException("TRN_SYNC_UPSERT_TAG", e.getMessage()+" "+ code);
		}
	}

    private void upsertTagTranslations(JSONObject tradJSON, String tagRowId) throws TrnSyncException {
        for(String lang : langCodes) {
            if(tradJSON.has(lang)) {
                upsertTagTranslation(tagRowId, lang, tradJSON.getString(lang));
            }
        }
    }

	private void upsertTagTranslation(String tagRowId, String lang, String translation) throws TrnSyncException {
		try {
			BusinessObjectTool bot = new BusinessObjectTool(tagTranslation);
			String translateRowId = getTagTranslateRowId(tagRowId, lang, translation);
			synchronized(tagTranslation) {
				tagTranslation.resetValues();
				if(Tool.isEmpty(translateRowId)) {
					bot.selectForCreate();
                }
				else {
					bot.selectForUpdate(translateRowId);
                }
				tagTranslation.setFieldValue("trnTagTranslateLang", lang);
				tagTranslation.setFieldValue("trnTagTranslateTrad", translation);
				tagTranslation.setFieldValue("trnTaglangTagId", tagRowId);
				bot.validateAndSave();
                foundTagsTranslations.add(tagTranslation.getRowId());
			}
		} catch(Exception e) {
			throw new TrnSyncException("TRN_SYNC_UPSERT_TAG_TRANSLATION", e.getMessage());
		}
	}

    private void syncUrlsRewriting() throws TrnSyncException {
        File f = new File(contentDir.getPath(), URL_REWRITING_JSON_NAME);
        String newHash = getNewHash(URL_REWRITING_JSON_NAME, f);
        if(newHash != null) {
            AppLog.info("Urls rewriting modifications detected, Syncing...", g);
            upsertUrlsRewriting(f);
            hashStore.put(URL_REWRITING_JSON_NAME, newHash);
            AppLog.info("Urls rewriting successfully synced", g);
        }
    }

	private void upsertUrlsRewriting(File f) throws TrnSyncException {
		try {
			JSONArray json = new JSONArray(FileTool.readFile(f));
			for (int i = 0; i < json.length(); i++) {
				JSONObject jsonRecord = json.getJSONObject(i);
				String rowId = upsertUrlRewriting(jsonRecord.optString("sourceUrl"), jsonRecord.optString("destinationUrl"));
                foundUrlsRewriting.add(rowId);
			}
		} catch(Exception e) {
			throw new TrnSyncException("TRN_SYNC_UPSERT_URLS_REWRITING_FROM_JSON", e.getMessage());
		}
	}

	private String upsertUrlRewriting(String sourceUrl, String destinationUrl) throws TrnSyncException {
		try {
			BusinessObjectTool bot = new BusinessObjectTool(urlRewriting);
			String rowId = getUrlRewritingRowId(sourceUrl, destinationUrl);
            if(!Tool.isEmpty(rowId)) {
                return rowId;
            }
			synchronized(urlRewriting) {
				urlRewriting.resetValues();			
                bot.selectForCreate();
				urlRewriting.setFieldValue("trnSourceUrl", sourceUrl);
				urlRewriting.setFieldValue("trnDestinationUrl", destinationUrl);

				bot.validateAndSave();
                return urlRewriting.getRowId();
			}
		} catch(Exception e) {
			throw new TrnSyncException("TRN_SYNC_UPSERT_URL_REWRITING",  e.getMessage()+" "+urlRewriting.toJSON());
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
				if(Tool.isEmpty(rowId)) {
					bot.selectForCreate();
                }
				else {
					bot.selectForUpdate(rowId);
                }
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
                // create "ANY" cat title if it does not exist
                category.postCreate();
			}
		}
		catch(Exception e){
			throw new TrnSyncException("TRN_SYNC_UPSERT_CATEGORY", e.getMessage()+" "+category.toJSON()+ " "+dir.getPath());
		}
	}
	
	private boolean jsonCatPublish(JSONObject json){
		return json.optBoolean("published", true);
	}
	
	private List<String[]> jsonCatContents(JSONObject json){
		List<String[]> l = new ArrayList<>();
		for(String lang : langCodes){
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

	private String getUrlRewritingRowId(String source, String destination) {
		return Tool.isEmpty(source) || Tool.isEmpty(destination) ? "" : g.simpleQuery("select row_id from trn_url_rewriting where trn_source_url='"+source+"' AND trn_destination_url='"+destination+"'");
	}
	
	private String getLsnRowIdFromPath(String path){
		return Tool.isEmpty(path) ? "" : g.simpleQuery("select row_id from trn_lesson where trn_lsn_path='"+path+"'");
	}

    private String getPageRowIdFromCode(String code) {
        return Tool.isEmpty(code) ? "" : g.simpleQuery("select row_id from trn_page where trn_page_code='"+code+"'");
    }

    private String getThemeRowId() {
        return g.simpleQuery("select row_id from trn_site_theme");
    }
	
	private void upsertLessonAndContent(File dir) throws TrnSyncException{
		try{
			String relativePath = getRelativePath(dir);
			String lsnRowId = getLsnRowIdFromPath(relativePath);
			
			JSONObject json = getLsnData(dir);

			lsnRowId = upsertLesson(lsnRowId, json, relativePath, dir);
			// create tag N-N lesson if tag exists and if association does not already exist
			upsertTrnTagLsnRecords(lsnRowId, json, relativePath);
			// create contents
			upsertLessonTranslations(lsnRowId, json);
		}
		catch(Exception e){
			throw new TrnSyncException("TRN_SYNC_UPSERT_LESSON_AND_CONTENT", e.getMessage()+" "+lesson.toJSON()+ " "+dir.getPath());
		}
	}

    // upsert lesson only (no content), returns lesson row_id 
    private String upsertLesson(String rowId, JSONObject json, String relativePath, File dir) throws TrnSyncException {
        try {
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
                return lesson.getRowId();
            }
        } catch(Exception e) {
            throw new TrnSyncException("TRN_SYNC_UPSERT_LESSON", e.getMessage()+" "+lesson.toJSON());
        }
    }

    private void upsertTrnTagLsnRecords(String lsnRowId, JSONObject json, String relativePath) throws TrnSyncException {
        try {
            BusinessObjectTool bot = new BusinessObjectTool(tagLsn);
            JSONArray tags = json.optJSONArray("tags");
            if(!Tool.isEmpty(tags) && tags != null) {
                for(int i = 0; i < tags.length(); i++) {
                    String tagCode = tags.getString(i);
                    String tagRowId = getTagRowIdFromCode(tagCode);
                    upsertTrnTagLsn(lsnRowId, tagRowId, tagCode, relativePath, bot);
                }
            }
        } catch(Exception e) {
            throw new TrnSyncException("TRN_SYNC_UPSERT_TRN_TAG_LSN_RECORDS", e.getMessage());
        }
    }

    private void upsertTrnTagLsn(String lsnRowId, String tagRowId, String tagCode, String relativePath, BusinessObjectTool bot) throws TrnSyncException, GetException, CreateException, ValidateException {
        if(Tool.isEmpty(tagRowId)) {
            throw new TrnSyncException("TRN_SYNC_UPSERT_TAG_LSN_NO_TAG_ROW_ID", "tag does not exist: " + tagCode);
        }
        try {
            String tagLsnRowId = tagLsn.getTagLsnRowId(tagRowId, lsnRowId);
            if(Tool.isEmpty(tagLsnRowId)) {
                synchronized(tagLsn) {
                    bot.selectForCreate();
                    tagLsn.setFieldValue("trnTaglsnLsnId", lsnRowId);
                    tagLsn.setFieldValue("trnLsnPath", relativePath);
                    tagLsn.setFieldValue("trnTaglsnTagId", tagRowId);
                    bot.validateAndCreate();
                }
            }
        } catch(Exception e) {
            throw new TrnSyncException("TRN_SYNC_UPSERT_TAG_LSN", e.getMessage());   
        }
    }

    private void upsertLessonTranslations(String rowId, JSONObject json) throws TrnSyncException {
        BusinessObjectTool bot = new BusinessObjectTool(lessonContent);
        for(String lang : langCodes){
            if(json.getJSONObject("contents").has(lang)){					
                upsertLessonTranslation(rowId, json, lang, bot);
            }
        }
    }

    private void upsertLessonTranslation(String rowId, JSONObject json, String lang, BusinessObjectTool bot) throws TrnSyncException {
        try {
            JSONObject content = json.getJSONObject("contents").getJSONObject(lang);
            if(content.has("markdown") && content.has("title") || content.has("video")) {
                synchronized(lessonContent){
                    bot.selectForCreate();
                    setLessonTranslation(rowId, content, json, lang);
                    // validateAndUpdate not working
                    if(lang.equals("ANY")) {
                        ObjectDB translate = g.getObject("trn_any_content", "TrnLsnTranslate");
                        translate.resetFilters();
                        translate.setFieldFilter("trnLtrLsnId", rowId);
                        translate.setFieldFilter("trnLtrLang", "ANY");
                        List<String[]> res = translate.search();
                        if(!res.isEmpty()) {
                            translate.setValues(res.get(0));
                            translate.delete();
                        }
                    }
                    bot.validateAndCreate();
                    createPics(rowId, content, lang);
                }
            }
        } catch(Exception e) {
            throw new TrnSyncException("TRN_SYNC_UPSERT_LESSON_TRANSLATION", e.getMessage()+" "+json.toString(2));
        }
    }

    private void setLessonTranslation(String rowId, JSONObject content, JSONObject json, String lang) throws TrnSyncException {
        try {
            lessonContent.setFieldValue("trnLtrLsnId", rowId);
            lessonContent.setFieldValue("trnLtrLang", lang);
            if(content.has("markdown")  && content.has("title")) {
                String ltrContent = FileTool.readFile(new File(content.getString("markdown")));
                String ltrTitle = content.getString("title");
                lessonContent.setFieldValue("trnLtrContent", ltrContent);
                lessonContent.setFieldValue("trnLtrTitle", ltrTitle);
            }
            
            if(content.has("video")){
                File video = new File(content.getString("video"));
                lessonContent.getField("trnLtrVideo").setDocument(lessonContent, video.getName(), new FileInputStream(video));
                if(lessonContent.getFieldValue("trnLtrTitle").isEmpty()) {
                    lessonContent.setFieldValue("trnLtrTitle", json.get("code"));
                }
            }
        } catch(Exception e) {
            throw new TrnSyncException("TRN_SYNC_SET_LESSON_TRANSLATION", e.getMessage());
        }
    }

    private void createPics(String rowId, JSONObject content, String lang) throws TrnSyncException {
        try {
            if(content.has("pics")){
                JSONArray pics = content.getJSONArray("pics");
                BusinessObjectTool bot = new BusinessObjectTool(picture);
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
        } catch(Exception e) {
            throw new TrnSyncException("TRN_CREATE_PICS", e.getMessage());
        }
    }

    private void syncTheme() throws TrnSyncException{
        File f = new File(contentDir.getPath(), THEME_JSON_NAME);
        String newHash = getNewHash(THEME_JSON_NAME, f);
        if(newHash != null) {
            AppLog.info("Theme modifications detected, Syncing...", g);
            upsertTheme(f);
            hashStore.put(THEME_JSON_NAME, newHash);
            AppLog.info("Theme successfully synced", g);
        }
    }

    private void upsertTheme(File f) throws TrnSyncException {
        try {
            JSONObject json = new JSONObject(FileTool.readFile(f));
            File icon = new File(contentDir.getPath(), json.getString("logo_path"));
            synchronized(theme) {
                theme.getTool().getForCreateOrUpdate(Map.of()); 
                theme.setFieldValue("trnThemeColor", json.getString("main_color"));
                theme.setFieldValue("trnThemeSecondaryColor", json.getString("secondary_color"));
                theme.getField("trnThemeIcon").setDocument(theme, icon.getName(), new FileInputStream(icon));
                theme.getTool().validateAndSave();
            }
        } catch(Exception e) {
            throw new TrnSyncException("TRN_SYNC_ERROR_SYNC_THEME", e.getMessage());
        }
    }
    

    private void upsertHomepage() throws TrnSyncException {
        try {
            if(getPageRowIdFromCode("homepage").isEmpty()) {
                lesson.resetFilters();
                lesson.setFieldFilter("trnLsnFrontPath", "/pages/homepage");
                List<String[]> res = lesson.search();
                if(res.isEmpty()) {
                    throw new TrnSyncException("syncHomepage", "Unable to find lesson homepage");
                }
                lesson.setValues(res.get(0));
                page.resetValues();
                BusinessObjectTool bot = new BusinessObjectTool(page);
                bot.selectForCreate();
                synchronized(page) {
                    page.resetValues();
                    page.setFieldValue("trnPageCode", "homepage");
                    page.setFieldValue("trnPageType", "homepage");
                    page.setFieldValue("trnPageTrnLessonid", lesson.getRowId());
                    bot.validateAndSave();
                }
            }
        } catch(Exception e) {
            throw new TrnSyncException("TRN_SYNC_ERROR_SYNC_HOMEPAGE", e.getMessage());
        }
    }

	private String getTagRowIdFromCode(String code) {
		return Tool.isEmpty(code) ? "" : g.simpleQuery("select row_id from trn_tag where trn_tag_code='"+code+"'");
	}

	private String getTagTranslateRowId(String tagRowId, String lang, String translation) {
		if(Tool.isEmpty(tagRowId) || Tool.isEmpty(lang) || Tool.isEmpty(translation)) return "";
		return g.simpleQuery("select row_id from trn_tag_translate where trn_taglang_tag_id='"+tagRowId+"' AND trn_tag_translate_lang='"+lang+"' AND trn_tag_translate_trad='"+translation+"'");
	}
	
	private File getLsnMdFile(File lsnDir, String lang){
		return getLsnFile(lsnDir, lang, "md");
	}
	
	private File getLsnVideoFile(File lsnDir, String lang){
		return getLsnFile(lsnDir, lang, "webm");
	}	
	
	private File getLsnFile(File lsnDir, String lang, String extension){
		if(lang.equals("ANY")) {
			lang = "";
		} else {
			lang = "_"+lang;
		}
		return new File(lsnDir.getPath()+"/"+getLsnCode(lsnDir)+lang+"."+extension);
	}
	
	private JSONObject getPics(File lsnDir){
		JSONObject pics = new JSONObject();
		for(String lang : langCodes)
			pics.put(lang, new JSONArray());
		
		for(File f : lsnDir.listFiles())
			if(isPic(f))
				pics.getJSONArray(getLocale(f)).put(f.getPath());
		
		return pics;
	}

    private boolean isPic(File f){
		String extension = FilenameUtils.getExtension(f.getName()).toLowerCase();
		return "png".equals(extension) || "jpg".equals(extension);
	}
	
	private String getLocale(File f){
		String[] split = FilenameUtils.getBaseName(f.getName()).toUpperCase().split("_");
		String locale = split.length>0 ? split[split.length-1] : null;
		return locale!=null && Arrays.asList(langCodes).contains(locale) ? locale : defaultLangCode;
	}
	
	private String getLsnCode(File lsnDir){
		return lsnDir.getName().split("_")[2];
	}
	
	private String getLsnOrder(File lsnDir){
		return lsnDir.getName().split("_")[1];
	}
	
	private JSONObject getLsnData(File dir) throws Exception {
		JSONObject lsn = new JSONObject();
		
		lsn.put("order", getLsnOrder(dir));
		lsn.put("code", getLsnCode(dir));
		
		JSONObject json = new JSONObject(FileTool.readFile(dir.getPath()+"/lesson.json"));
        if(!json.has("ANY")) json.put("ANY", new JSONObject());
		lsn.put("published", json.optBoolean("published", true));
		lsn.put("viz", json.optString("display", "TUTO"));
		lsn.put("tags", json.optJSONArray("tags"));
		
		JSONObject contents = new JSONObject();
		JSONObject content;
		File f;
		
		JSONObject pictures = getPics(dir);
		
		for(String lang : langCodes){
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
    private static String hashDirectoryFiles(File file) throws java.io.IOException
    {
        Vector<FileInputStream> fileStreams = new Vector<>();
        
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null){
                Arrays.sort(files, Comparator.comparing(File::getName));
                for(File child : files)
                    if (!child.isDirectory())
                        fileStreams.add(new FileInputStream(child));
            }
        } else {
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

    // compares hashes from the last successfull synchronization with the current content to synchronize
    // and returns the calculated hash if no hash has been found in glo.ser, or if old hash is different from the new hash
    // returns null if the hashes are the same (no modification of docs content)
    private String getNewHash(String relativePath, File dir) throws TrnSyncException {
		String oldHash = hashStore.get(relativePath);
		String newHash = "";
		try{
			newHash = hashDirectoryFiles(dir);
		}
		catch(Exception e){
			throw new TrnSyncException("TRN_SYNC_ERROR_HASHING_FILES", relativePath);
		}
		
		if(oldHash==null || !oldHash.equals(newHash)) {
            return newHash;
        } else {
            return null;
        }
    }
	
	private void storeHashes() {
		Tool.objectToFile(hashStore, hashStoreFile);
	}
	
	private boolean isTrainingDbEmpty(){
		return 0 == (int) g.simpleQueryAsDouble("select count(*) from trn_category");
	}
}