/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.mobile.core.model;

/**
 * Convenience class to store HQLProperties for model extensions
 * 
 * @author alostale
 * 
 */
public class HQLProperty {
  /**
   * propertie in the select clause of the HQL
   */
  String hqlProperty;

  /**
   * name of the property in the generated JSONObject
   */
  String jsonName;

  Boolean includeInGroupBy;

  public HQLProperty(String hqlProperty, String jsonName) {
    this.hqlProperty = hqlProperty;
    this.jsonName = jsonName;
    this.includeInGroupBy = true;
  }

  public HQLProperty(String hqlProperty, String jsonName, Boolean includeInGroupBy) {
    this.hqlProperty = hqlProperty;
    this.jsonName = jsonName;
    this.includeInGroupBy = includeInGroupBy;
  }
}
