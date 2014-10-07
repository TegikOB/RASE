/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.mobile.core;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openbravo.client.kernel.ComponentProvider;

@ApplicationScoped
public class MobileDefaultsHandler {
  @Inject
  @Any
  private Instance<MobileDefaults> mobileDefaultsInstances;
  private static final Logger log = Logger.getLogger(MobileDefaultsHandler.class);

  public MobileDefaults getDefaults(String appName) {
    MobileDefaults mobileDefault = null;
    try {
      mobileDefault = mobileDefaultsInstances.select(new ComponentProvider.Selector(appName)).get();
    } catch (Exception e) {
      log.debug("Error retrieving defaults handler for " + appName, e);
      // ignore it
    }
    if (mobileDefault == null) {
      mobileDefault = mobileDefaultsInstances.select(
          new ComponentProvider.Selector(MobileCoreConstants.APP_IDENTIFIER)).get();
    }
    return mobileDefault;
  }
}
