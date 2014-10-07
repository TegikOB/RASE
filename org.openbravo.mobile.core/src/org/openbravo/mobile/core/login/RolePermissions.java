/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.mobile.core.login;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.MobileCoreConstants;
import org.openbravo.mobile.core.process.JSONRowConverter;
import org.openbravo.mobile.core.process.SecuredJSONProcess;
import org.openbravo.service.json.JsonConstants;

public class RolePermissions extends SecuredJSONProcess {

  private static final Logger log = Logger.getLogger(RolePermissions.class);

  @Override
  protected boolean bypassSecurity() {
    return true;
  }

  @Override
  public void exec(Writer w, JSONObject jsonsent) throws IOException, ServletException {
    OBContext.setAdminMode(true);
    try {
      String moduleId = jsonsent.getJSONObject("parameters").getJSONObject("appModuleId")
          .getString("value");
      String whereClause = "where reference.id = :refId and module.id in "
          + LabelsComponent.getMobileAppDependantModuleIds(moduleId);
      OBQuery<org.openbravo.model.ad.domain.List> refLists = OBDal.getInstance().createQuery(
          org.openbravo.model.ad.domain.List.class, whereClause);
      refLists.setNamedParameter("refId", MobileCoreConstants.PREFERENCE_LIST_ID);
      List<String> preferenceList = new ArrayList<String>();
      for (org.openbravo.model.ad.domain.List listRef : refLists.list()) {
        preferenceList.add(listRef.getSearchKey());
      }
      buildResponse(w, preferenceList);
    } catch (Exception e) {
      log.error("error getting role permissions", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void buildResponse(Writer w, List<String> prefs) throws IOException {

    final int startRow = 0;
    int rows = 0;
    Throwable t = null;

    try {
      w.write("\"data\":[");
      while (rows < prefs.size()) {
        if (rows > 0) {
          w.write(',');
        }
        JSONObject json = new JSONObject();
        json.put("key", prefs.get(rows));
        json.put("value", getPreferenceValue(prefs.get(rows)));
        w.write(json.toString());
        rows++;
      }
    } catch (JSONException e) {
      t = e;
    } finally {
      w.write("],");
      if (t == null) {
        // Add success fields
        w.write("\"");
        w.write(JsonConstants.RESPONSE_STARTROW);
        w.write("\":");
        w.write(Integer.toString(startRow));
        w.write(",\"");
        w.write(JsonConstants.RESPONSE_ENDROW);
        w.write("\":");
        w.write(Integer.toString(rows > 0 ? rows + startRow - 1 : 0));
        w.write(",\"");
        if (rows == 0) {
          w.write(JsonConstants.RESPONSE_TOTALROWS);
          w.write("\":0,\"");
        }
        w.write(JsonConstants.RESPONSE_STATUS);
        w.write("\":");
        w.write(Integer.toString(JsonConstants.RPCREQUEST_STATUS_SUCCESS));
      } else {
        JSONRowConverter.addJSONExceptionFields(w, t);
      }
    }
  }

  private Object getPreferenceValue(String p) {
    try {
      String prefValue = Preferences.getPreferenceValue(p, true, OBContext.getOBContext()
          .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
          .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
      if ("Y".equals(prefValue)) {
        return Boolean.TRUE;
      } else if ("N".equals(prefValue)) {
        return Boolean.FALSE;
      } else {
        return prefValue;
      }
    } catch (PropertyException e) {
      return false;
    }
  }

}
