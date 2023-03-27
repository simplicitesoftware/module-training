package com.simplicite.adapters.Training;

import java.util.*;
import java.util.regex.PatternSyntaxException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import com.simplicite.util.*;
import com.simplicite.util.integration.*;
import com.simplicite.util.tools.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.ZIPTool;

import org.json.*;

// -----------------------------------------------------------------------------------------------------------
// Note: you should consider using one of com.simplicite.util.integration sub classes instead of SimpleAdapter
// -----------------------------------------------------------------------------------------------------------

/**
 * Adapter TrainingContentAdapter
 */
public class TrainingContentAdapter extends SimpleAdapter {
	private static final long serialVersionUID = 1L;
	private static final String[] IMAGE_EXTENSIONS = new String[] {".png", ".jpeg", ".jpg", ".gif"};
	private static final String[] VIDEO_EXTENSIONS = new String[] {".webm"};
	private static final String[] CONTENT_FILE_EXTENSIONS = new String[] {".md"};
	private static final String[] LANGUAGES = new String[] {"ANY", "FRA", "ENU"};
	private static final String DEFAULT_LANG = "ANY";
	private static final String CATEGORY_PREFIX = "CTG";
	private static final String LESSON_PREFIX = "LSN";
	private static final String FOLDER_SEPARATOR = "_";

	/**
	 * Process method
	 */
	@Override
	public void process() throws InterruptedException {
		InputStream zipData = null;
		try {
			File destination = new File("new_content");
			zipData = getInputStream();
			ZIPTool.extract(zipData, destination);

			// 1. Erase all the content in the database (categories, lessons, pictures ...)
			// eraseRootCategories();
			// eraseParentlessLessons();
			
			// 2. Find the "content" folder in the ZIP folder
			File[] children = destination.listFiles();
			for(File child : children) {
				if (child.getName().equals("content")) {
					// 2.1 Iterate through the folder to create the root categories or root lessons
					File[] rootChildren = child.listFiles();
					for(File rootChild : rootChildren) {
						if (rootChild.isFile()) AppLog.info(getClass(), "process", "There should not be files at the root level of the content : " + rootChild.getName(), getGrant());
						else if (rootChild.isDirectory() && rootChild.getName().startsWith(CATEGORY_PREFIX)) readCategoryFolder(rootChild, null);
						else if (rootChild.isDirectory() && rootChild.getName().startsWith(LESSON_PREFIX))   readLessonFolder(rootChild, null);
						else if (rootChild.isDirectory()) AppLog.info(getClass(), "process", "Folder does not respect naming conventions : " + rootChild.getName(), getGrant());
					}
				}
			}
			// 3.  Delete zip and destination directory if it is not automated
		} catch (Exception e) {
			AppLog.error(getClass(), "process", "Global Exception in process method", e, getGrant());
		}
	}
	
	private void readCategoryFolder(File categoryFolder, String parentId) {
		AppLog.info(getClass(), "readCategoryFolder", "CREATING CATEGORY FROM FOLDER " + categoryFolder.getName(), getGrant());
		try {
			// 1. Read category.json and create TrnCategory & TrnCategoryTranslate
			JSONObject jsonCategoryDescription = new JSONObject(getFileContentAsString(categoryFolder.getAbsolutePath() + "/category.json"));
			int order = getOrderFromFolderName(categoryFolder.getName());
			String categoryId = createCategory(jsonCategoryDescription, order, parentId);
			// 2. Iterate through elements and when starting with 'LSN' => call createLessonFromDirectory
			for(File child : categoryFolder.listFiles()) {
				if (child.getName().startsWith(CATEGORY_PREFIX)) readCategoryFolder(child, categoryId);
				else if (child.getName().startsWith(LESSON_PREFIX))	readLessonFolder(child, categoryId);
			}
		} catch(IOException e) {
			AppLog.error(getClass(), "readCategoryFolder", "error reading category.json", e, getGrant());
		}
	}
	
	private String createCategory(JSONObject categoryDescription, int order, String parentId) {
		try {
			// 1. Create TrnCategory Object
			JSONObject defaultDescription = categoryDescription.getJSONObject(DEFAULT_LANG);
			ObjectDB category = getGrant().getObject(order +"_TrnCategory", "TrnCategory");
			category.setFieldValue("trnCatOrder", order);
			category.setFieldValue("trnCatTitle", defaultDescription.getString("title"));
			category.setFieldValue("trnCatPublish", false);
			category.setFieldValue("trnCatId", parentId);
			new BusinessObjectTool(category).validateAndCreate();
			// 2. Create TrnCategoryTranslation objects
			createCategoryTranslations(categoryDescription, category.getFieldValue("row_id"));
			
			return category.getFieldValue("row_id");
		} catch(ValidateException e) {
			AppLog.error(getClass(), "createCategory", "ValidateException when creating a TrnCategory", e, getGrant());
			return null;
		}
		catch(CreateException e) {
			AppLog.error(getClass(), "createCategory", "CreateException when creating a TrnCategory", e, getGrant());
			return null;
		}
	}
	
	private void createCategoryTranslations(JSONObject categoryDescription, String categoryId) {
		for(String lang : LANGUAGES) {
			try {
				JSONObject description = categoryDescription.getJSONObject(lang);
				if (description != null) {
					ObjectDB translation = getGrant().getObject(lang +"_TrnCategoryTranslate", "TrnCategoryTranslate");
					translation.setFieldValue("trnCtrLang", lang.toUpperCase());
					translation.setFieldValue("trnCtrTitle", description.getString("title"));
					translation.setFieldValue("trnCtrDescription", description.getString("description"));
					translation.setFieldValue("trnCtrCatId", categoryId);
					new BusinessObjectTool(translation).validateAndCreate();
				} else 
					AppLog.info(getClass(), "createCategoryTranslations", "NO DEFAULT DESCRIPTION FOR THE CATEGORY " + categoryId, getGrant());
				
			} catch(JSONException e) {
				AppLog.info(getClass(), "createCategoryTranslations", "ERROR WHILE READING category.json of " + categoryId, getGrant());
			} catch(ValidateException e) {
				AppLog.error(getClass(), "createCategoryTranslations", "ValidateException when creating a TrnCategoryTranslation", e, getGrant());
			} catch(CreateException e) {
				AppLog.error(getClass(), "createCategoryTranslations", "CreateException when creating a TrnCategoryTranslation", e, getGrant());
			}
		}
	}
	
	private void readLessonFolder(File lessonFolder, String categoryId) {
		try {
			// 1. Read lesson.json file and create corresponding TrnLesson
			JSONObject lessonDescription = new JSONObject(Files.readString(Paths.get(lessonFolder.getAbsolutePath() + "/lesson.json"), StandardCharsets.UTF_8));
			String title = lessonDescription.getJSONObject(DEFAULT_LANG).getString("title");
			int order = getOrderFromFolderName(lessonFolder.getName());
			ObjectDB lesson = getGrant().getObject(lessonFolder.getName() + "_TrnLesson", "TrnLesson");
			if (title != null) {
				lesson.setFieldValue("trnLsnOrder", order);
				lesson.setFieldValue("trnLsnTitle", title);
				lesson.setFieldValue("trnLsnPublish", false);
				lesson.setFieldValue("trnLsnVisualization", "TUTO");
				lesson.setFieldValue("trnLsnCatId", categoryId);
				new BusinessObjectTool(lesson).validateAndCreate();
			}
			// 2. Create translations by reading lesson.json
			createLessonTranslations(lessonDescription, lesson.getFieldValue("row_id"));
			//3. Create/Update TrnPicture & TrnLsnTranslations 
			for (File file : lessonFolder.listFiles()) {
				String language  = determineFileLanguage(file.getName());
				if (isFileAContent(file.getName())) addContentToTranslation(language, lesson.getFieldValue("row_id"), getFileContentAsString(file.getAbsolutePath()));
				else if (isFileAnImage(file.getName())) createTrnPicture(language, lesson.getFieldValue("row_id"), file);
				else if (isFileAVideo(file.getName())) addVideoToTranslation(language, lesson.getFieldValue("row_id"), file);
				else AppLog.info(getClass(), "readLessonFolder", "UNKNOWN FILE TYPE : " + file.getName(), getGrant());
			}
		} catch(JSONException e) {
			AppLog.error(getClass(), "readLessonFolder", "Error while parsing lesson.json", e, getGrant());
		} catch(IOException e) {
			AppLog.error(getClass(), "readLessonFolder", "IOException while reading lesson.json", e, getGrant());
		} catch(ValidateException e) {
			AppLog.error(getClass(), "readLessonFolder", "ValidateException when creating a TrnLsn", e, getGrant());
		} catch(CreateException e) {
			AppLog.error(getClass(), "readLessonFolder", "CreateException when creating a TrnLsn", e, getGrant());
		} catch (Exception e) {
			AppLog.error(getClass(), "readLessonFolder", "GlobalException in readLessonFolder", e, getGrant());
		} 
	}
	
	private void createLessonTranslations(JSONObject lessonDescription, String lessonId) {
		for(String lang : LANGUAGES) {
			try {
				JSONObject description = lessonDescription.getJSONObject(lang);
				if (description != null) {
					ObjectDB translation = getGrant().getObject(lang + description.getString("title") + "_TrnLsnTranslate", "TrnLsnTranslate");
					translation.setFieldValue("trnLtrLan", lang.toUpperCase());
					translation.setFieldValue("trnLtrTitle", description.getString("title"));
					translation.setFieldValue("trnLtrLsnId", lessonId);
					new BusinessObjectTool(translation).validateAndCreate();
				} else {
					AppLog.info(getClass(), "createLessonTranslations", "LESSON " + lessonId + " HAS NO " + lang + " INFORMATION", getGrant());
				}
			} catch(JSONException e) {
				AppLog.error(getClass(), "createCategoryTranslations", "ERROR WHILE READING lesson.json of " + lessonId, e, getGrant());
			} catch(ValidateException e) {
				AppLog.error(getClass(), "createCategoryTranslations", "ValidateException when creating a TrnLsnTranslation", e, getGrant());
			} catch(CreateException e) {
				AppLog.error(getClass(), "createCategoryTranslations", "CreateException when creating a TrnLsnTranslation", e, getGrant());
			} catch (Exception e) {
				AppLog.error(getClass(), "createCategoryTranslations", "GlobalException in createLessonTranslations", e, getGrant());
			} 
		}
	}
	
	private void addContentToTranslation(String lang, String lessonId, String content) {
		ObjectDB translation = getGrant().getObject(lang + lessonId + "AddContent_TrnLsnTranslate", "TrnLsnTranslate");
		translation.resetFilters();
		translation.setFieldFilter("trnLtrLsnId", lessonId);
		translation.setFieldFilter("trnLtrLan", lang);
		translation.search();
		List<String[]> rows = translation.search();
		if (rows.size() >= 1 ) {
			translation.setValues(rows.get(0));
			translation.setFieldValue("trnLtrContent", content);
			translation.update();
		} else {
			AppLog.info(getClass(), "addContentToTranslation", "There is no " + lang + " translation in lesson.json for lesson with ID : " + lessonId, getGrant());
		}
	}
	
	private void addVideoToTranslation(String lang, String lessonId, File video) {
		ObjectDB translation = getGrant().getObject(lang + lessonId + "AddVideo_TrnLsnTranslate", "TrnLsnTranslate");
		translation.resetFilters();
		translation.setFieldFilter("trnLtrLsnId", lessonId);
		translation.setFieldFilter("trnLtrLan", lang);
		translation.search();
		List<String[]> rows = translation.search();
		if (rows.size() >= 1 ) {
			translation.setValues(rows.get(0));
			try {
				translation.getField("trnLtrVideo").setDocument(translation, video.getName(), new FileInputStream(video));
			} catch(IOException e) {
				AppLog.error(getClass(), "createTrnPicture", "Error with image file of lesson" + lessonId, e, getGrant());
			} finally {
				String result = translation.update();
				AppLog.info(getClass(), "addVideoToTranslation", "UPDATE RESULT : " + result, getGrant());
			}
		} else {
			AppLog.info(getClass(), "addVideoToTranslation", "There is no " + lang + " translation in lesson.json for lesson with ID : " + lessonId, getGrant());
		}
	}
	
	private void createTrnPicture(String language, String lessonId, File image){
		try {
			// AppLog.info(getClass(), "createTrnPicture", "Creating image : " + image.getName() + " in lang : " + language + " for lesson : " + lessonId, getGrant());
			ObjectDB picture = getGrant().getObject(language + lessonId +"_TrnPicture", "TrnPicture");
			picture.setFieldValue("trnPicLsnId", lessonId);
			picture.setFieldValue("trnPicLang", language.toUpperCase());
			picture.getField("trnPicImage").setDocument(picture, image.getName(), new FileInputStream(image));
			new BusinessObjectTool(picture).validateAndCreate();
		} catch(IOException e) {
			AppLog.error(getClass(), "createTrnPicture", "Error with image file of lesson" + lessonId, e, getGrant());
		} catch(ValidateException e) {
				AppLog.error(getClass(), "createTrnPicture", "ValidateException when creating a TrnPicture", e, getGrant());
		} catch(CreateException e) {
			AppLog.error(getClass(), "createTrnPicture", "CreateException when creating a TrnPicture", e, getGrant());
		}
	}
	
	private int getOrderFromFolderName(String folderName) {
		try {
			String[] splitted = folderName.split(FOLDER_SEPARATOR);
			if (splitted.length >= 2) return Integer.parseInt(splitted[1]);
			else return 0;		
		} catch(PatternSyntaxException | NumberFormatException e) {
			AppLog.error(getClass(), "getOrderFromFolderName", "Exception while reading order from the folderName" + folderName, e, getGrant());
			return 0;
		} catch(Exception e) {
			return 0;
		}
	}
	
	private String getFileContentAsString(String filePath) throws IOException {
		return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
	}
	
	private String determineFileLanguage(String fileName) {
		String language = DEFAULT_LANG;
		for (String lang : LANGUAGES)
			if (fileName.contains(FOLDER_SEPARATOR + lang.toLowerCase()) || fileName.contains(FOLDER_SEPARATOR + lang.toUpperCase())) language = lang;
		return language;
	}
	
	private boolean isFileAnImage(String fileName) {
		for (String extension : IMAGE_EXTENSIONS)
			if (fileName.endsWith(extension)) return true;
		return false; 
	}
	
	private boolean isFileAContent(String fileName) {
		for (String extension : CONTENT_FILE_EXTENSIONS)
			if (fileName.endsWith(extension)) return true;
		return false; 
	}
	
	private boolean isFileAVideo(String fileName) {
		for (String extension : VIDEO_EXTENSIONS)
			if (fileName.endsWith(extension)) return true;
		return false; 
	}
	
	// Category deleting is configured to cascade : deleting all root categories will delete all the courses related data (categories, lessons, pictures ...)
	private void eraseRootCategories(){
		ObjectDB category = getGrant().getObject("Erase_TrnCategory", "TrnCategory");
		category.resetFilters();
		category.setFieldFilter("trnCatId", "is null");
		for (String[] row : category.search()) {
			category.setValues(row);
			try {
				new BusinessObjectTool(category).delete();
			} catch(DeleteException e) {
				AppLog.error(getClass(), "eraseRootCategories", "DeleteException while deleting root categories", e, getGrant());
			} catch(Exception e) {
				AppLog.error(getClass(), "eraseRootCategories", "Global Exception while deleting root categories", e, getGrant());
			}
		}
	}
	
	private void eraseParentlessLessons(){
		ObjectDB lesson = getGrant().getObject("Erase_TrnLesson", "TrnLesson");
		lesson.resetFilters();
		lesson.setFieldFilter("trnLsnCatId", "is null");
		for (String[] row : lesson.search()) {
			lesson.setValues(row);
			try {
				new BusinessObjectTool(lesson).delete();
			} catch(DeleteException e) {
				AppLog.error(getClass(), "eraseRootCategories", "DeleteException while deleting parentless lessons", e, getGrant());
			} catch(Exception e) {
				AppLog.error(getClass(), "eraseRootCategories", "Global Exception while deleting parentless lessons", e, getGrant());
			}
		}
	}
}
