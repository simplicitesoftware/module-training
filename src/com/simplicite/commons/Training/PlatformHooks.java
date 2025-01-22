package com.simplicite.commons.Training;

import com.simplicite.bpm.Activity;
import com.simplicite.util.AppLog;
import com.simplicite.util.EnumItem;
import com.simplicite.util.Grant;
import com.simplicite.util.MenuItem;
import com.simplicite.util.ObjectField;

/**
 * Platform Hooks
 */
public class PlatformHooks extends com.simplicite.util.engine.PlatformHooksInterface {

  @Override
  public boolean isMenuItemEnabled​(Grant g, MenuItem item, ObjectField field, MenuItem.State state, EnumItem enumItem, Activity activity) {
    try {
      return !("TrnDomain".equals(item.getDomain().getName()) && "TrnSyncSupervisor".equals(item.getObjectName()) && TrnTools.isUiMode());
    } catch(TrnConfigException e) {
      AppLog.error(getClass(), "isMenuItemEnabled", "Configuration error: content_edition mode not found", e, g);
      return true;
    }
  }
}