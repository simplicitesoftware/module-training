package com.simplicite.objects.Training;

import java.util.ArrayList;
import java.util.List;

import com.simplicite.commons.Training.TrnObject;
import com.simplicite.util.Message;
import com.simplicite.util.ObjectDB;

/**
 * Business object TrnPage
 */
public class TrnPage extends TrnObject {
  private static final long serialVersionUID = 1L;

  @Override
  public List<String> preValidate() {
    List<String> msgs = new ArrayList<String>();
    String value = getFieldValue("trnPageType");
    // if new page is a homepage check if one does not already exists
    if ("homepage".equals(value) && isNew()) {
      ObjectDB page = getGrant().getTmpObject("TrnPage");
      page.resetFilters();
      page.setFieldFilter("trnPageType", "homepage");
      if (page.count() == 1) {
        msgs.add(Message.formatError("You already have a homepage", null, "trnPageType"));
      }
    }
    // Return a list of messages
    return msgs; 
  }
}
