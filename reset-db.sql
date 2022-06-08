DELETE FROM trn_category;
DELETE FROM trn_category_translate;
DELETE FROM trn_lesson;
DELETE FROM trn_lsn_translate;
DELETE FROM trn_picture;
DELETE FROM trn_site_content;

DELETE FROM m_document WHERE dbd_object_id IN (SELECT row_id FROM m_object WHERE obj_name='TrnCategory' OR
obj_name='TrnCategoryTranslate' OR 
obj_name='TrnLesson' OR 
obj_name='TrnLsnTranslate' OR 
obj_name='TrnPicture' OR 
obj_name='TrnSiteContent');