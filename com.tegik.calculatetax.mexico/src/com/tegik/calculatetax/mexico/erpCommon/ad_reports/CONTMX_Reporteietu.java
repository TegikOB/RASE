/*
 * 
 * The contents of this file are subject to the Openbravo Public License Version
 * 1.0 (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SLU All
 * portions are Copyright (C) 2001-2009 Openbravo SLU All Rights Reserved.
 * Contributor(s): ______________________________________.
 */
package com.tegik.calculatetax.mexico.erpCommon.ad_reports;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;

public class CONTMX_Reporteietu extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {

      String strcContmxReporteietuId = vars.getSessionValue("CONTMX_ReporteietuJR.inpcontmxReporteietuId_R");

      if (strcContmxReporteietuId.equals(""))
        strcContmxReporteietuId = vars.getSessionValue("CONTMX_ReporteietuJR.inpcontmxReporteietuId");

        
      if (log4j.isDebugEnabled())
        log4j.debug("strcContmxReporteietuId: " + strcContmxReporteietuId);
        
      printPagePartePDF(response, vars, strcContmxReporteietuId);
      
    } else
      pageError(response);
  }

  private void printPagePartePDF(HttpServletResponse response, VariablesSecureApp vars,
      String strcContmxReporteietuId) throws IOException, ServletException {

    if (log4j.isDebugEnabled())
      log4j.debug("Output: CONTMX_Reporteietu - pdf");

    String strLanguage = vars.getLanguage();
    String strBaseDesign = getBaseDesignPath(strLanguage);

	log4j.error("strcContmxReporteietuId: " + strcContmxReporteietuId);
	strcContmxReporteietuId=strcContmxReporteietuId.replace("(", "");
	strcContmxReporteietuId=strcContmxReporteietuId.replace(")", "");
	strcContmxReporteietuId=strcContmxReporteietuId.replace("'", "");

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    log4j.error("strcContmxReporteietuId: " + strcContmxReporteietuId);
    parameters.put("DOCUMENT_ID", strcContmxReporteietuId);
    
    response.setHeader("Content-disposition", "inline; filename=CONTMX_Reporteietu.pdf");
    String strReportName = "@basedesign@/com/tegik/contmx/modulo/erpCommon/ad_reports/CONTMX_ReporteietuJR.jrxml";
    renderJR(vars, response, strReportName, "pdf", parameters, null, null);
  }

  public String getServletInfo() {
    return "Servlet that print IETU Report Document";
  } // End of getServletInfo() method
}
