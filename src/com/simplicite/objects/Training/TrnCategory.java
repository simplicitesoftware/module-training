package com.simplicite.objects.Training;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.simplicite.commons.Training.TrnObject;
import com.simplicite.commons.Training.TrnTools;
import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.Message;
import com.simplicite.util.ObjectDB;
import com.simplicite.util.ObjectField;
import com.simplicite.util.Tool;
import com.simplicite.util.exceptions.SaveException;
import com.simplicite.util.exceptions.ValidateException;
import com.simplicite.util.tools.BusinessObjectTool;

/**
 * Business object TrnCategory
 */
public class TrnCategory extends TrnObject {
  private static final long serialVersionUID = 1L;

  @Override
  public List<String> postValidate() {
    List<String> msgs = new ArrayList<String>();

    if (!isSyncInstance())
      setFieldValue("trnCatPath", getPath());

    setFieldValue("trnCatFrontPath", TrnTools.path2Front(getFieldValue("trnCatPath")));

    if (getFieldValue("trnCatId.trnCatPath").contains(getFieldValue("trnCatPath")))
      msgs.add(
          Message.formatError("TRN_CAT_LOOP", "A category cannot be referenced as one of its ancestors",
              "trnCatId"));

    return msgs;
  }

  @Override
  public void postLoad() {
    super.postLoad();
    if (getGrant().hasResponsibility("TRN_READ")) {
      setDefaultSearchSpec("t.trn_cat_publish='1'");
    }
  }

  @Override
  public void initCreate() {
    setFieldValue("trnCatOrder", getNextOrder());
  }

  @Override
  public void initUpdate() {
    // cf getNextOrder() &
    // https://community.simplicite.io/t/getparentobject-dans-les-hooks-init/6080
    getGrant().setParameter("LAST_VISITED_CATEGORY_ID", getRowId());
  }

  private int getNextOrder() {
    String ref = " is null";
    if (getParentObject() != null && "TrnCategory".equals(getParentObject().getName())
        && !Tool.isEmpty(getGrant().getParameter("LAST_VISITED_CATEGORY_ID"))) {
      ref = "=" + Tool.toSQL(getGrant().getParameter("LAST_VISITED_CATEGORY_ID"));
    }

    String lastOrder = getGrant()
        .simpleQuery("SELECT trn_cat_order FROM trn_category WHERE trn_cat_id" + ref
            + " ORDER BY trn_cat_order DESC");
    AppLog.info("---" + Tool.parseInt(lastOrder, 0), Grant.getSystemAdmin());
    return Tool.parseInt(lastOrder, 0) + 10;
  }

  @Override
  public String getUserKeyLabel(String[] row) {
    return getFieldValue("trnCatFrontPath", row);
  }

  private String getPath() {
    ObjectField parent = getField("trnCatId.trnCatPath");
    String parentPath = parent.isEmpty() ? "/" : parent.getValue() + "/";
    return parentPath + "CTG_" + getFieldValue("trnCatOrder") + "_" + TrnTools.toSnake(getFieldValue("trnCatCode"));
  }

  @Override
  public String postSave() {
    try {
      updateChildren();
    } catch (Exception e) {
      AppLog.error(getClass(), "postSave", "Error updating children", e, getGrant());
    }
    return null;
  }

  @Override
  public String postCreate() {
    try {
      ObjectDB tcl = getGrant().getTmpObject("TrnCategoryTranslate");
      synchronized (tcl.getLock()) {
        tcl.resetValues();
        tcl.resetFilters();
        tcl.setFieldFilter("trnCtrCatId", getRowId());
        tcl.setFieldFilter("trnCtrLang", "ANY");
        if (tcl.count() == 0) {
          tcl.setFieldValue("trnCtrLang", "ANY");
          String test = getFieldValue("trnCatCode");
          tcl.setFieldValue("trnCtrTitle", getFieldValue("trnCatCode"));
          tcl.setFieldValue("trnCtrCatId", getRowId());
          tcl.getTool().validateAndCreate();
        }
      }
    } catch (Exception e) {
      AppLog.error(e, getGrant());
    }
    return null;
  }

  private void updateChildren() throws ValidateException, SaveException {
    BusinessObjectTool bot;

    ObjectDB recursive_TrnCategory = getGrant().getObject("updateChildren_TrnCategory_" + getRowId(),
        "TrnCategory");
    synchronized (recursive_TrnCategory) {
      recursive_TrnCategory.resetFilters();
      recursive_TrnCategory.setFieldFilter("trnCatId", getRowId());
      bot = new BusinessObjectTool(recursive_TrnCategory);

      for (String[] row : recursive_TrnCategory.search()) {
        recursive_TrnCategory.setValues(row);
        bot.validateAndSave(); // Triggers recursion !!!!
      }
    }

    ObjectDB recursive_TrnLesson = getGrant().getObject("updateChildren_TrnLesson", "TrnLesson");
    synchronized (recursive_TrnLesson) {
      recursive_TrnLesson.resetFilters();
      recursive_TrnLesson.setFieldFilter("trnLsnCatId", getRowId());
      bot = new BusinessObjectTool(recursive_TrnLesson);

      for (String[] row : recursive_TrnLesson.search()) {
        recursive_TrnLesson.setValues(row);
        bot.validateAndSave();
      }
    }
  }

  public JSONObject getCategoryForFront(String lang) throws Exception {
    JSONObject json = new JSONObject()
        .put("row_id", getRowId())
        .put("path", getFieldValue("trnCatFrontPath"))
        .put("order", getFieldValue("trnCatOrder"))
        .put("is_category", true);

    ObjectDB content = getGrant().getTmpObject("TrnCategoryTranslate");
    synchronized (content) {
      content.resetFilters();
      content.setFieldFilter("trnCtrCatId", getRowId());
      content.setFieldFilter("trnCtrLang", lang);
      if (content.getCount() == 0)
        content.setFieldFilter("trnCtrLang", "ANY");

      if (content.getCount() != 1)
        throw new Exception("CAT_CONTENT_NOT_FOUND");
      else {
        content.setValues(content.search().get(0));
        json.put("title", content.getFieldValue("trnCtrTitle"));
      }
    }
    return json;
  }

  public String getCatFrontPath(String catId) {
    resetFilters();
    setFieldFilter("row_id", catId);
    List<String[]> res = search();
    if (res.size() > 0) {
      setValues(res.get(0));
      return getFieldValue("trnCatFrontPath");
    }
    return null;
  }

  public Boolean categoryHasAtLeastOneLesson(String categoryId, String tagId) {
    String res = getGrant().simpleQuery(
        "select COUNT(*) FROM trn_lesson as lesson INNER JOIN trn_tag_lsn as ttl ON lesson.row_id = ttl.trn_taglsn_lsn_id where ttl.trn_taglsn_tag_id ='"
            + tagId + "' AND lesson.trn_lsn_cat_id ='" + categoryId + "'");
    if (Integer.parseInt(res) > 0)
      return true;
    return false;
  }

  public boolean isCategoryPublishedRecursive() throws Exception {
    if (getFieldValue("trnCatPublish").equals("1")) {
      String parentCatId = getFieldValue("trnCatId");
      if (!parentCatId.isEmpty()) {
        TrnCategory cat = TrnCategory.getCategoryObject(getGrant(), parentCatId);
        return cat.isCategoryPublishedRecursive();
      }
    } else {
      return false;
    }
    return true;
  }

  public static TrnCategory getCategoryObject(Grant g, String id) throws Exception {
    TrnCategory cat = (TrnCategory) g.getObject("temp_TrnCategory", "TrnCategory");
    cat.resetFilters();
    cat.setFieldFilter("row_id", id);
    List<String[]> rows = cat.search();
    if (rows.size() > 0) {
      cat.setValues(rows.get(0));
    } else {
      throw new Exception("Cannot find a category with the following row_id: " + id);
    }
    return cat;
  }
}
