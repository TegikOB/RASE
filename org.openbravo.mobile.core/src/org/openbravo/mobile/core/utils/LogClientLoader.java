/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.obmobcLogClient;
import org.openbravo.mobile.core.process.JSONProcessSimple;
import org.openbravo.mobile.core.process.JSONPropertyToEntity;
import org.openbravo.model.ad.access.User;
import org.openbravo.service.json.JsonConstants;

public class LogClientLoader extends JSONProcessSimple {
  private static final Logger log = Logger.getLogger(LogClientLoader.class);

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONArray jsonarraylogclient = jsonsent.getJSONArray("logclient");

    long t1 = System.currentTimeMillis();
    JSONObject result = this.saveLogClient(jsonarraylogclient);
    log.debug("Final total time: " + (System.currentTimeMillis() - t1));
    return result;
  }

  public JSONObject saveLogClient(JSONArray jsonarray) throws JSONException {
    boolean error = false;
    OBContext.setAdminMode(true);
    long t1 = System.currentTimeMillis();
    try {
      for (int i = 0; i < jsonarray.length(); i++) {
        JSONObject jsonlogclient = jsonarray.getJSONObject(i);
        try {
          JSONObject result = saveLogClient(jsonlogclient);
          if (!result.get(JsonConstants.RESPONSE_STATUS).equals(
              JsonConstants.RPCREQUEST_STATUS_SUCCESS)) {
            log.error("There was an error importing log clients: " + jsonlogclient.toString());
            error = true;
          }
        } catch (Exception e) {
          log.error("An error happened when processing a logClient: ", e);
          OBDal.getInstance().rollbackAndClose();
          log.error("Error while loading log client", e);

        }
      }
      OBDal.getInstance().flush();
      log.debug("Total log import time: " + (System.currentTimeMillis() - t1));
    } finally {
      OBContext.restorePreviousMode();
    }
    JSONObject jsonResponse = new JSONObject();
    if (!error) {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put("result", "0");
    } else {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);
      jsonResponse.put("result", "0");
    }
    return jsonResponse;
  }

  public JSONObject saveLogClient(JSONObject jsonlogclient) throws Exception {

    long t0 = System.currentTimeMillis();
    long t1;
    obmobcLogClient logClient = null;

    logClient = OBProvider.getInstance().get(obmobcLogClient.class);

    JSONObject objJson = new JSONObject(jsonlogclient.getString("json"));

    logClient.setJson(jsonlogclient.getString("json"));

    JSONPropertyToEntity.fillBobFromJSON(
        ModelProvider.getInstance().getEntity(obmobcLogClient.class), logClient, objJson);

    updateAuditInfo(logClient, jsonlogclient);
    OBDal.getInstance().save(logClient);

    log.debug("Creation of Log Client records: " + (System.currentTimeMillis() - t0));
    return successMessage();
  }

  protected JSONObject successMessage() throws Exception {
    final JSONObject jsonResponse = new JSONObject();

    jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    jsonResponse.put("result", "0");
    return jsonResponse;
  }

  public static String getErrorMessage(Exception e) {
    StringWriter sb = new StringWriter();
    e.printStackTrace(new PrintWriter(sb));
    return sb.toString();
  }

  private void updateAuditInfo(obmobcLogClient lc, JSONObject jsonorder) throws JSONException {
    Long value = jsonorder.getLong("created");
    lc.setCreationDate(new Date(value));
    lc.setUpdated(new Date(value));
    String strUserId = "0";
    if (jsonorder.getString("createdby") != null && !"".equals(jsonorder.getString("createdby"))) {
      strUserId = jsonorder.getString("createdby");
    }
    User createdby = OBDal.getInstance().get(User.class, strUserId);
    lc.setCreatedBy(createdby);
    lc.setUpdatedBy(createdby);
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  protected boolean bypassSecurity() {
    return true;
  }
}
