/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class IDL_Check_DataType extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      final String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      final String strDefaultValue = vars.getStringParameter("inpdefaultvalue");
      final String strDataType = vars.getStringParameter("inpdatatype");
      printPage(response, vars, strChanged, strDefaultValue, strDataType);

    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strDefValue, String strDataType) throws IOException, ServletException {

    boolean validation = true;
    String infoMsg = null;
    StringBuffer resultado = new StringBuffer();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    resultado.append("var calloutName='IDL_Check_DataType';\n\n");

    if (!strDefValue.isEmpty()) {

      if (strDataType.equals("string")) {
        // No validation

      } else if (strDataType.equals("boolean")) {
        if (strDefValue.equalsIgnoreCase("TRUE") || strDefValue.equalsIgnoreCase("FALSE")) {
          // No validation
        } else {
          validation = false;
          infoMsg = "IDL_VALIDATION_BOOLEAN";
        }
      } else if (strDataType.equals("numeric")) {
        try {
          Double.parseDouble(strDefValue);
        } catch (NumberFormatException nfe) {
          validation = false;
          if (strDefValue.contains(",")) {
            infoMsg = "IDL_VALIDATION_COMMA";
          }
        }
      } else if (strDataType.equals("date")) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date testDate = null;
        try {
          testDate = sdf.parse(strDefValue);
        } catch (ParseException e) {
          testDate = new Date();

        }
        if (!sdf.format(testDate).equals(strDefValue)) {
          validation = false;
          infoMsg = "IDL_VALIDATION_DATE";
        }

      }

      resultado.append("var respuesta = new Array(");
      if (!validation) {
        resultado.append("new Array(\"MESSAGE\", \""
            + FormatUtilities.replaceJS(
                  Utility.messageBD(this, "IDL_VALIDATION_FAIL", vars.getLanguage())
                + "<br>"
                + Utility.messageBD(this, "IDL_VALIDATION_VALUE", vars.getLanguage())
                + strDefValue
                + "<br>"
                + Utility.messageBD(this, "IDL_VALIDATION_TYPE", vars.getLanguage())
                + strDataType
                + ((infoMsg == null) ? "" : ("<br>" + Utility.messageBD(this, infoMsg, vars.getLanguage())))
                ) + "\")");
      } else {
        resultado.append("new Array(\"MESSAGE\", \"\")");
      }
      resultado.append(");");

    } else {
      resultado.append("var respuesta = null;");
    }

    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

}
