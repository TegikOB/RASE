/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.mobile.core.model;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

/**
 * Models can be extended by creating classes of ModelExtension qualified with the model's
 * qualifier. In this way it is possible to add new properties to models from external modules.
 * 
 * @author alostale
 * 
 */

@ApplicationScoped
public abstract class ModelExtension {

  /**
   * Returns a List of HQLPropery that will be used to generate the query for the model
   * 
   * @param params
   *          any object used to compute the properties, this object is defined within the model
   * @return
   */
  public abstract List<HQLProperty> getHQLProperties(Object params);
}
