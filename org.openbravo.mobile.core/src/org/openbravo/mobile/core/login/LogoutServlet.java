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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.openbravo.mobile.core.process.WebServiceAbstractServlet;
import org.openbravo.service.json.JsonUtils;

public class LogoutServlet extends WebServiceAbstractServlet {

  private static final Logger log = Logger.getLogger(LogoutServlet.class);

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    try {
      writeResult(response, getJSONResult("success"));
    } catch (JSONException e) {
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    }
  }
}
