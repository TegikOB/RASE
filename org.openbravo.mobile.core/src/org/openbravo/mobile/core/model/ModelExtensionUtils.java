/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.mobile.core.model;

import java.util.Iterator;

import javax.enterprise.inject.Instance;

/**
 * Utility methods to deal with ModelExtensions
 * 
 * @author alostale
 * 
 */
public class ModelExtensionUtils {

  /**
   * Returns the merged list of HQL properties for the instances of ModuleExtension for a model
   * 
   */
  public static HQLPropertyList getPropertyExtensions(Instance<ModelExtension> extensions,
      Object params) {
    HQLPropertyList properties = new HQLPropertyList();

    Iterator<ModelExtension> exts = extensions.iterator();
    while (exts.hasNext()) {
      ModelExtension ext = exts.next();
      properties.addAll(ext.getHQLProperties(params));
    }
    return properties;
  }

  public static HQLPropertyList getPropertyExtensions(Instance<ModelExtension> extensions) {
    return getPropertyExtensions(extensions, null);
  }
}
