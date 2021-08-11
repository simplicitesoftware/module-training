package com.simplicite.commons.Training;

import java.util.*;
import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import java.io.File;
import java.util.regex.Pattern;
import org.json.JSONObject;

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
	private final Pattern PATTERN_LESSON = Pattern.compile("^LSN_[0-9]+_[a-z\\-]+$");
	private final File contentDir;
	private final File hashStoreFile;
	
	private ObjectDB category, lesson, picture;
	
	private HashMap<String, String> hashStore;
	private ArrayList<String> foundPaths;
	
	Grant g;
	
	public class TrnSyncException extends Exception{
		private String additional;
		public TrnSyncException(String msg){
			super(msg);
			additional = "";
		}
		
		public TrnSyncException(String msg, String additional){
			super(msg);
			this.additional = additional;
		}
		
		public TrnSyncException(String msg, File f){
			super(msg);
			this.additional = f.getPath();
		}
		
		public String getMessage(){
			return super.getMessage()+" : "+additional;
		}
	}
	
	public static void triggerSync() throws TrnSyncException{
		(new TrnFsSyncTool(Grant.getSystemAdmin())).sync();
	}
	
	public TrnFsSyncTool(Grant g) throws TrnSyncException{
		this.g = g;
		JSONObject conf = new JSONObject(g.getParameter("TRN_CONFIG"));
		if(Tool.isEmpty(conf.getString("filesystem_contentdir")))
			throw new TrnSyncException("TRN_SYNC_EMPTY_CONTENT_PATH");
		contentDir = new File(conf.getString("filesystem_contentdir"));
		hashStoreFile = new File(g.getContentDir()+"/"+HASHSTORE_FILENAME);
	}
	
	// Main sync algorithm : injects contents at `contentDir` into DB
	public void sync() throws TrnSyncException{
		// load (from glo.ser) a hashmap of the checksum of files for each directory, so we know which have changed
		loadHashes();
		// verify the structure (recursive)
		verifyContentStructure();
		// initialize some usefull objects
		foundPaths = new ArrayList<>();
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
		verifyFolderStructure(contentDir, true);
	}
	
	private void verifyFolderStructure(File dir, boolean isRoot) throws TrnSyncException{
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
	
	private void validateRootContent(File dir) throws TrnSyncException{
		for(File child : dir.listFiles())
			if(!isCategory(child))
				throw new TrnSyncException("TRN_SYNC_ROOT_NON_CONFORMITY", child);
	}
	
	private void validateCategoryContent(File dir) throws TrnSyncException{
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
	
	private void validateLessonContent(File dir) throws TrnSyncException{
		String lessonCode = dir.getName().split("_")[2];
		boolean hasJson = false;
		boolean hasMd = false;
		boolean hasWebm = false;
		for(File child : dir.listFiles()){
			if("lesson.json".equals(child.getName()))
				hasJson = true;
			else if(child.isDirectory())
				throw new TrnSyncException("TRN_SYNC_LESSON_CONTAINING_FOLDER", child);
			else if(child.getName().equals(lessonCode+".md"))
				hasMd = true;
			else if(FileTool.getExtension(child.getName()).equals("md")){
				hasMd = true;
				if(!child.getName().equals(lessonCode+".md"))
					throw new TrnSyncException("TRN_SYNC_LESSON_MARDOWN_NAME_INCONSISTENT", child);
			}
			else if(FileTool.getExtension(child.getName()).equals("webm")){
				hasWebm = true;
				if(!child.getName().equals(lessonCode+".webm"))
					throw new TrnSyncException("TRN_SYNC_LESSON_VIDEO_NAME_INCONSISTENT", child);
			}
			else if(!FileTool.getExtension(child.getName()).equals("png")){
				AppLog.info(FileTool.getExtension(child.getName()), Grant.getSystemAdmin());
				throw new TrnSyncException("TRN_SYNC_LESSON_EXTENSION_NOT_ALLOWED", child);
			}
		}
		if(!hasJson)
			throw new TrnSyncException("TRN_SYNC_LESSON_JSON_MISSING", dir);
	}
	
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
	}
	
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
	
	private void loadTrnObjects(){
		g.changeAccess("TrnCategory", true,true,true,true);
		g.changeAccess("TrnLesson", true,true,true,true);
		g.changeAccess("TrnPicture", true,true,true,true);
		category = g.getObject("sync_TrnCategory", "TrnCategory");
		lesson = g.getObject("sync_TrnLesson", "TrnLesson");
		picture = g.getObject("sync_TrnPicture", "TrnPicture");
	}
	
	private void syncPath(String relativePath) throws TrnSyncException{
		File dir = new File(contentDir.getPath()+relativePath);
		
		if(!"/".equals(relativePath)){
			String oldHash = hashStore.get(relativePath);
			String newHash = "";
			try{
				newHash = hashDirectoryFiles(dir);
			}
			catch(Exception e){
				throw new TrnSyncException("TRN_SYNC_ERROR_HASHING_FILES", relativePath);
			}
			
			if(oldHash==null || !oldHash.equals(newHash))
				updateDbWithDir(dir);
			
			hashStore.put(relativePath, newHash);
			foundPaths.add(relativePath);
		}
		
		File[] files = dir.listFiles();
		for(File file : files)
			if(file.isDirectory())
				syncPath(relativePath+file.getName()+"/");
	}
	
	private void updateDbWithDir(File dir) throws TrnSyncException{
		if(isCategory(dir))
			upsertCategory(dir);
		else if(isLesson(dir))
			updateLesson(dir);
		else
			throw new TrnSyncException("TRN_SYNC_ERROR_NOT_CATEGORY_NOR_LESSON", dir);
	}
	
	private void upsertCategory(File dir) throws TrnSyncException{
		try{
			String relativePath = getRelativePath(dir);
			String rowId = getCatRowIdFromPath(relativePath);
			String[] name = dir.getName().split("_");
			
			JSONObject json = new JSONObject(FileTool.readFile(dir.getPath()+"/category.json"));
			synchronized(category){
				category.resetValues();
				if(Tool.isEmpty(rowId))
					(new BusinessObjectTool(category)).selectForCreate();
				else
					(new BusinessObjectTool(category)).selectForUpdate(rowId);
				category.setFieldValue("trnCatOrder", name[1]);
				category.setFieldValue("trnCatPublish", json.optBoolean("published", true));
				category.setFieldValue("trnCatTitle", json.getJSONObject("ANY").getString("title"));
				category.setFieldValue("trnCatPath", relativePath);
				category.setFieldValue("trnCatId", getCatRowIdFromPath(getParentRelativePath(dir)));
				(new BusinessObjectTool(category)).validateAndSave();
			}
		}
		catch(Exception e){
			AppLog.error(getClass(), "upsertCategory", e.getMessage(), e, g);
			throw new TrnSyncException("TRN_SYNC_UPSERT_CATEGORY", e.getMessage()+" "+category.toJSON()+ " "+dir.getPath());
		}
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
	
	private void updateLesson(File dir) throws TrnSyncException{
		try{
			String relativePath = getRelativePath(dir);
			String rowId = getLsnRowIdFromPath(relativePath);
			String[] name = dir.getName().split("_");
			File markdown = new File(dir.getPath()+"/"+name[2]+".md");
			File video = new File(dir.getPath()+"/"+name[2]+".webm");
			
			JSONObject json = new JSONObject(FileTool.readFile(dir.getPath()+"/lesson.json"));
			synchronized(lesson){
				BusinessObjectTool bot = new BusinessObjectTool(lesson);
				lesson.resetValues();
				if(!Tool.isEmpty(rowId)){
					bot.selectForDelete(rowId);
					bot.delete();
				}
				
				bot.selectForCreate();
				lesson.setFieldValue("trnLsnOrder", name[1]);
				lesson.setFieldValue("trnLsnTitle", json.getJSONObject("ANY").getString("title"));
				lesson.setFieldValue("trnLsnPath", relativePath);
				lesson.setFieldValue("trnLsnCatId", getCatRowIdFromPath(getParentRelativePath(dir)));
				lesson.setFieldValue("trnLsnPublish", json.optBoolean("published", true));
				if(markdown.exists())
					lesson.setFieldValue("trnLsnContent", FileTool.readFile(markdown));
				if(video.exists())
					lesson.getField("trnLsnVideo").setDocument(lesson, video.getName(), new FileInputStream(video));
					
				bot.validateAndSave(true);
				rowId = lesson.getRowId();
			}
			
			for(File f : dir.listFiles())
				if(f.getName().endsWith(".png"))
					synchronized(picture){
						picture.resetValues();
						picture.getField("trnPicImage").setDocument(picture, f.getName(), new FileInputStream(f));
						picture.setFieldValue("trnPicLang", "ANY");
						picture.setFieldValue("trnPicLsnId", rowId);
						(new BusinessObjectTool(picture)).validateAndSave(true);
					}
		}
		catch(Exception e){
			AppLog.error(getClass(), "upsertCategory", e.getMessage(), e, g);
			throw new TrnSyncException("TRN_SYNC_UPSERT_LESSON", e.getMessage()+" "+lesson.toJSON()+ " "+dir.getPath());
		}
	}
	
	// Non-recursive version of https://stackoverflow.com/a/46899517/1612642 => ignores directories
    private static String hashDirectoryFiles(File directory) throws IOException
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