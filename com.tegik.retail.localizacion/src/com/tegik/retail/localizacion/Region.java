/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package com.tegik.retail.localizacion;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class Region extends ProcessHQLQuery {
  public static final String regionPropertyExtension = "OBLOC_RegionExtension";

  @Inject
  @Any
  @Qualifier(regionPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    List<String> hqlQueries = new ArrayList<String>();

    HQLPropertyList regularRegionHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    hqlQueries.add("select "
		+ regularRegionHQLProperties.getHqlSelect()
		+"r.id as id, "
		+"r.name as name, "
		+"r.name as _identifier "
		+"from Region as r "
		+"where country = '247' "
        + " AND r.$readableClientCriteria AND " + "r.$naturalOrgCriteria AND "
        + "(r.$incrementalUpdateCriteria) AND (r.$incrementalUpdateCriteria)) order by r.name");

    return hqlQueries;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}