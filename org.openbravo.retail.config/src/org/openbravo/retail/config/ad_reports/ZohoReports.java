/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 * 
 * This code is part of a demo, and should not be taken as an example on how to implement a report
 * 
 ************************************************************************************
 */

package org.openbravo.retail.config.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;

public class ZohoReports extends HttpSecureAppServlet {

  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    String iFrameString = "<iframe frameborder=0 width='100%' height='100%' src='https://reports.zoho.com/ZDBDataSheetView.cc?OBJID=112904000005683166&STANDALONE=true&privatelink=6ce85be3a314ba9f49cd382fc5d37bc6&WIDTH=400&HEIGHT=300&ZDB_THEME_NAME=blue&REMTOOLBAR=true&INCLUDETITLE=true&INCLUDEDESC=true'></iframe>";
    PrintWriter out = response.getWriter();
    out.write(iFrameString);
  }
}
