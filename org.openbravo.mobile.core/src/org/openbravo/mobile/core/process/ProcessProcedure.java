/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.process;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

public class ProcessProcedure extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    ConnectionProvider dalconn = new DalConnectionProvider();
    OBContext cnx = OBContext.getOBContext();
    String user = cnx.getUser().getId();
    String client = cnx.getCurrentClient().getId();
    String organization = cnx.getCurrentOrganization().getId();
    String role = cnx.getRole().getId();
    VariablesSecureApp vars = new VariablesSecureApp(user, client, organization, role);

    OBCriteria<org.openbravo.model.ad.ui.Process> crProcess = OBDal.getInstance().createCriteria(
        org.openbravo.model.ad.ui.Process.class);
    crProcess.add(Restrictions.eq("searchKey", jsonsent.getString("process")));
    crProcess.setFilterOnReadableClients(true);
    crProcess.setFilterOnReadableOrganization(true);
    crProcess.setFilterOnActive(true);
    List<org.openbravo.model.ad.ui.Process> processes = crProcess.list();
    if (processes.size() != 1) {
      throw new JSONException(MessageFormat.format("Process \"{0}\" does not exist.",
          jsonsent.getString("process")));
    }
    org.openbravo.model.ad.ui.Process process = processes.get(0);

    // Calls the process
    final ProcessInstance pinstance = CallProcess.getInstance().callProcess(process,
        jsonsent.optString("record"), new HashMap<String, Object>());

    final JSONObject jsonResponse = new JSONObject();

    String message = pinstance.getErrorMsg();
    int i = message.indexOf("@ERROR=");
    if (i >= 0) {
      throw new JSONException(message.substring(i + 7));
    } else {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put("result", pinstance.getResult());
      jsonResponse.put("record", pinstance.getRecordID());
      jsonResponse.put("message",
          Utility.parseTranslation(dalconn, vars, vars.getLanguage(), message));
    }

    return jsonResponse;
  }

  // private JSONObject execJavaProcess(JSONObject jsonsent) throws JSONException, ServletException
  // {
  //
  // OBContext cnx = OBContext.getOBContext();
  // String user = cnx.getUser().getId();
  // String client = cnx.getCurrentClient().getId();
  // String organization = cnx.getCurrentOrganization().getId();
  // String role = cnx.getRole().getId();
  //
  // OBError myMessage = null;
  // ProcessBundle pb = new ProcessBundle(process.getId(), new VariablesSecureApp(user, client,
  // organization, role)).init(dalconn);
  //
  // // HashMap<String, Object> params= new HashMap<String, Object>();
  // // params.put("FIN_Bankstatement_ID", strFIN_Bankstatement_ID);
  // // params.put("adOrgId", vars.getStringParameter("inpadOrgId"));
  // // params.put("adClientId", vars.getStringParameter("inpadClientId"));
  // // params.put("tabId", tabId);
  // // pb.setParams(params);
  //
  // new ProcessRunner(pb).execute(dalconn);
  // myMessage = (OBError) pb.getResult();
  //
  // final JSONObject jsonResponse = new JSONObject();
  // jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
  //
  // final JSONObject jsonResult = new JSONObject();
  // jsonResult.put("title", myMessage.getTitle());
  // jsonResult.put("message", myMessage.getMessage());
  // jsonResponse.put("result", jsonResult);
  //
  // final JSONObject jsonResultLoc = new JSONObject();
  // jsonResultLoc.put("title",
  // Utility.parseTranslation(dalconn, vars, vars.getLanguage(), myMessage.getTitle()));
  // jsonResultLoc.put("message",
  // Utility.parseTranslation(dalconn, vars, vars.getLanguage(), myMessage.getMessage()));
  // jsonResponse.put("resultLocalized", jsonResultLoc);
  //
  // return jsonResponse;
  // }
}
