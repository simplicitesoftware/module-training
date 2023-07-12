package com.simplicite.extobjects.Training;

import com.simplicite.util.*;
import com.simplicite.util.tools.*;
import java.io.IOException;

/**
 * External object TrnFront
 */
public class TrnFront extends com.simplicite.webapp.web.StaticSiteExternalObject {
  private static final long serialVersionUID = 1L;
  private static final String NOT_FOUND = "index.html";

  @Override
  public Object notfound(Parameters params) {
    AppLog.info("Not found on: " + params.getBaseLocation(), getGrant());
    try {
      if (NOT_FOUND.equals(params.getURI())) // Avoid infinite loop
        throw new IOException();

      return displayFile(NOT_FOUND);
    } catch (IOException e) {
      AppLog.error(null, e, getGrant());
      return super.notfound(params);
    }
  }
}
