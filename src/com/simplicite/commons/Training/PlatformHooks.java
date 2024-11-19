package com.simplicite.commons.Training;

import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;

/**
 * Platform Hooks
 */
public class PlatformHooks extends com.simplicite.util.engine.PlatformHooksInterface {

  @Override
  public boolean isMenuEnable(Grant g, String domain, String item) {
    // Example to hide to group SIMPLE_USER the Product in the Marketing domain.
    try {
      return !("TrnDomain".equals(domain) && "TrnSyncSupervisor".equals(item) && TrnTools.isUiMode());
    } catch(TrnConfigException e) {
      AppLog.error(getClass(), "isMenuEnable", "Configuration error: content_edition mode not found", e, g);
      return true;
    }
  }
}