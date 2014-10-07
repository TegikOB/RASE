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

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.model.ad.access.User;

/**
 * 
 * This class provides a series of defaults for Mobile Applications. In this way it is possible to
 * easily extend some methods (such as Login...) when only these defaults are required to customize
 * the default behavior.
 * 
 * To do use of it, just extend it using as Qualifier your application's name. If there is no
 * extension, this one will be used by default.
 * 
 * @author alostale
 * 
 */
@ApplicationScoped
@Qualifier(MobileCoreConstants.APP_IDENTIFIER)
public class MobileDefaults {

  /**
   * Returns the ID of the form that secures the application. This is having access to this form
   * grants access to the application
   * 
   * @return
   */
  public String getFormId() {
    return null;
  }

  /**
   * User readable application name
   * 
   * @return
   */
  public String getAppName() {
    return "Openbravo Mobile";
  }

  public String getDefaultRoleProperty() {
    return User.PROPERTY_DEFAULTROLE;
  }
}
