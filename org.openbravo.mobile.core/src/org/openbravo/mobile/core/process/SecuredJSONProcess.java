/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.mobile.core.process;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.model.ad.access.FormAccess;

public abstract class SecuredJSONProcess implements JSONProcess {
  private final static Logger log = Logger.getLogger(SecuredJSONProcess.class);

  @Override
  public abstract void exec(Writer w, JSONObject jsonsent) throws IOException, ServletException;

  public void secureExec(Writer w, JSONObject jsonsent) throws IOException, ServletException {
    if (hasPermission()) {
      long t = System.currentTimeMillis();
      try {
        exec(w, jsonsent);
      } catch (Exception e) {
        log.error(e);
        JSONException exception = new JSONException(this.getClass() + ": " + e.getMessage());
        JSONRowConverter.addJSONExceptionFields(w, exception);
      }
      long processTime = System.currentTimeMillis() - t;
      if (processTime > 1000) {
        log.info("Process " + this.getClass().getName() + " took:" + processTime + "ms params: "
            + jsonsent);
      }
    } else {
      log.warn("User " + OBContext.getOBContext().getUser() + " with role "
          + OBContext.getOBContext().getRole() + " has no access to " + this.getClass());
      try {
        JSONObject response = new JSONObject();
        response.put("status", -1);
        JSONObject error = new JSONObject();
        error.put("message", "Current user/role has no access to " + this.getClass());
        response.put("error", error);
        String s = response.toString();
        if (s.startsWith("{") && s.endsWith("}")) {
          // write only the properties, brackets are written outside.
          w.write(s.substring(1, s.length() - 1));
        } else {
          w.write(response.toString());
        }
      } catch (Exception e) {
        log.error("Error generating error message", e);
        throw new RuntimeException("No permission!!");
      }
    }
  }

  protected boolean hasPermission() {
    if (bypassSecurity()) {
      log.debug("Bypassing security for " + this.getClass());
      return true;
    }

    OBContext.setAdminMode(false);
    try {
      String formId = getFormId();
      String roleId = OBContext.getOBContext().getRole().getId(); // TODO: use proxy?
      if (StringUtils.isNotEmpty(formId)) {
        String where = "as a " + //
            "where a.specialForm.id = :formId " + //
            "  and a.role.id = :roleId";

        OBQuery<FormAccess> qForm = OBDal.getInstance().createQuery(FormAccess.class, where);
        qForm.setNamedParameter("formId", formId);
        qForm.setNamedParameter("roleId", roleId);

        if (qForm.count() == 0) {
          return false;
        }
      } else {
        log.warn("Not checking form access for " + this.getClass());
      }

      if (bypassPreferenceCheck()) {
        log.debug("Bypassing preference security for " + this.getClass());
        return true;
      }

      if (!OBContext.getOBContext().getRole().isManual()) {
        // automatic roles have access regardless preference
        return true;
      }

      String property = getProperty();
      if (StringUtils.isNotEmpty(property)) {
        try {
          return "Y".equals(Preferences.getPreferenceValue(property, true, OBContext.getOBContext()
              .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
              .getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
        } catch (PropertyException e) {
          return false;
        }
      } else {
        log.info("Not checking property access for " + this.getClass());
      }
      return true;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  protected boolean bypassSecurity() {
    return false;
  }

  protected boolean bypassPreferenceCheck() {
    return false;
  }

  protected String getProperty() {
    return null;
  }

  protected String getFormId() {
    return null;
  }

  protected JSONObject getContextInformation() {
    JSONObject contextInformation = new JSONObject();
    try {
      contextInformation.put("userId", OBContext.getOBContext().getUser().getId());
      contextInformation.put("roleId", OBContext.getOBContext().getRole().getId());
      contextInformation.put("orgId", OBContext.getOBContext().getCurrentOrganization().getId());
      contextInformation.put("clientId", OBContext.getOBContext().getCurrentClient().getId());
    } catch (JSONException e) {
      throw new OBException("Couldn't generate JSON", e);
    }
    return contextInformation;
  }
}
