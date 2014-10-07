/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.mobile.core.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class to store a List of HQLProperties for model extensions
 * 
 * @author alostale
 * 
 */
public class HQLPropertyList {
  private static final Logger log = LoggerFactory.getLogger(HQLPropertyList.class);

  private List<HQLProperty> properties;

  public HQLPropertyList() {
    properties = new ArrayList<HQLProperty>();
  }

  public void addAll(List<HQLProperty> hqlProperties) {
    properties.addAll(hqlProperties);
  }

  /**
   * Returns a String to be used in the HQL's select clause based on all properties defined for this
   * model.
   * 
   */
  public String getHqlSelect() {
    boolean firstProperty = true;
    String hqlSelect = "";
    for (HQLProperty property : properties) {
      if (!firstProperty) {
        hqlSelect += ", \n";
      }
      firstProperty = false;
      hqlSelect += property.hqlProperty + " as " + property.jsonName;
    }
    return " " + hqlSelect + " ";
  }

  /**
   * Returns a String to be used in the HQL's groupBy clause based on all properties defined for
   * this model.
   * 
   */
  public String getHqlGroupBy() {
    boolean firstProperty = true;
    String hqlGroupBy = "";
    for (HQLProperty property : properties) {
      if (property.includeInGroupBy) {
        if (!firstProperty) {
          hqlGroupBy += ", \n";
        }
        firstProperty = false;
        hqlGroupBy += property.hqlProperty;
      }
    }
    return " " + hqlGroupBy + " ";
  }

  /**
   * Returns a JSONArray with an element for each of the rows returned in the query. Query should
   * have been generated using the getHqlSelect method
   * 
   */
  public JSONArray getJSONObject(Query query) {
    JSONArray result = new JSONArray();

    // TODO: make this scrollable
    for (Object obj : query.list()) {
      Object[] row = (Object[]) obj;
      result.put(getJSONObjectRow(row));
    }

    return result;
  }

  /**
   * Returns a JSONObject for a single row
   */
  public JSONObject getJSONObjectRow(Object[] row) {
    JSONObject result = new JSONObject();
    int i = 0;
    for (HQLProperty property : properties) {
      try {
        result.put(property.jsonName, row[i]);
      } catch (JSONException e) {
        log.error("Error generating JSONObject for property " + property, e);
      }
      i++;
    }
    return result;
  }
}
