/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.financial.cashflowforecast;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.JRFieldProviderDataSource;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

public class CashflowForecast extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private CashflowForecastData[][] data;
  CashflowForecastData[] dataHeader;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strFinancialAccount = vars.getRequestGlobalVariable("inpFinancialAccountId",
          "CashflowForecast|FinancialAccountId");
      String strDatePlanned = vars.getRequestGlobalVariable("inpDatePlanned",
          "CashflowForecast|DatePlanned");
      String strBreakDate = vars.getRequestGlobalVariable("inpBreakDate",
          "CashflowForecast|BreakDate");

      printPageDataSheet(response, vars, strFinancialAccount, strDatePlanned, strBreakDate);
    } else if (vars.commandIn("FIND")) {
      String strBankAccount = vars.getRequestGlobalVariable("inpFinancialAccountId",
          "CashflowForecast|FinancialAccountId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDatePlanned",
          "CashflowForecast|DatePlanned");
      String strBreakDate = vars.getRequestGlobalVariable("inpBreakDate",
          "CashflowForecast|BreakDate");

      printPageDataSheet(response, vars, strBankAccount, strDateFrom, strBreakDate);
    } else if (vars.commandIn("PrintJR")) {
      String strOutputtype = vars.getStringParameter("outputtype",
          new ValueListFilter("PDF", "XLS"));
      String strFinancialAccount = vars.getStringParameter("inpFinancialAccountId",
          IsIDFilter.instance);
      String strDatePlanned = vars.getRequiredStringParameter("inpDatePlanned");
      boolean breakByDate = "on".equals(vars.getStringParameter("inpBreakDate"));

      printJRXML(response, vars, strFinancialAccount, strDatePlanned, breakByDate, strOutputtype);
    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strFinancialAccount, String strDatePlanned, String strBreakDate) throws IOException,
      ServletException {
    String[] discard = { "", "" };
    boolean breakByDate = !strBreakDate.equals("");

    if (vars.commandIn("DEFAULT")) {
      discard[0] = "sectionAccount";
      discard[1] = "sectionAccountSummaryTable";
    } else {
      discard[0] = breakByDate ? "reportAccount" : "reportAccountDate";
    }

    // Create toolbar with excel and pdf
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "CashflowForecast", false, "", "",
        "print('PDF');", false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "print('XLS');");

    CashflowForecastData[] dataSummary = obtainSummaryFieldProvider(vars, strFinancialAccount,
        strDatePlanned);

    // Hide payment details html part if there are no payemnts to thow
    if (dataSummary.length == 0) {
      discard[0] = "sectionAccount";
      discard[1] = "sectionAccountSummaryTable";
    }

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/financial/cashflowforecast/CashflowForecast", discard).createXmlDocument();

    if (!vars.commandIn("DEFAULT")) {
      dataSummary = obtainLinesFieldProvider(dataSummary, vars, strDatePlanned, breakByDate);
      xmlDocument.setData("structureDetail", dataHeader);
      xmlDocument.setDataArray(breakByDate ? "reportAcctDate" : "reportAcct", "structureAccount",
          data);
    } else {
      xmlDocument.setData("structureDetail", CashflowForecastData.set());
    }

    // Financial Account combo
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "FIN_FINANCIAL_ACCOUNT_ID", "", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "CashflowForecast"), Utility.getContext(this, vars, "#User_Client",
              "CashflowForecast"), '*');
      comboTableData.fillParameters(null, "CashflowForecast", "");
      xmlDocument.setData("reportFIN_FINANCIAL_ACCOUNT", "liststructure",
          comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("toolbar", toolbar.toString());
    xmlDocument.setParameter("datePlanned", strDatePlanned);
    xmlDocument.setParameter("datePlanneddisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("datePlannedsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("financialAccountId", strFinancialAccount);
    xmlDocument.setParameter("finalDate", strDatePlanned);
    xmlDocument.setParameter("now", DateTimeData.today(this));
    xmlDocument.setParameter("breakDate", strBreakDate.equals("") ? "0" : "1");
    xmlDocument.setData("structureSummary", dataSummary);

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.financial.cashflowforecast.CashflowForecast");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "CashflowForecast.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "CashflowForecast.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    OBError myMessage = vars.getMessage("CashflowForecast");
    vars.removeMessage("CashflowForecast");
    if (myMessage != null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private CashflowForecastData[] obtainLinesFieldProvider(CashflowForecastData[] dataSummary,
      VariablesSecureApp vars, String strDatePlanned, boolean breakByDate) throws ServletException {
    Vector<Object> vDatas = new Vector<Object>();
    Vector<Object> vHeader = new Vector<Object>();
    CashflowForecastData[] dataDetail = null;

    // For each financial account get the details of all the movements
    for (int j = 0; j < dataSummary.length; j++) {
      dataDetail = CashflowForecastData.selectLines(this, vars.getLanguage(),
          dataSummary[j].finFinancialAccountId, strDatePlanned,
          breakByDate ? ", duedate asc, isreceipt desc" : ", isreceipt desc, duedate asc");
      BigDecimal payment = BigDecimal.ZERO;
      BigDecimal income = BigDecimal.ZERO;

      BigDecimal acum = new BigDecimal(dataSummary[j].initialbalance);
      // Summary information is retrieved
      for (int i = 0; i < dataDetail.length; i++) {
        dataDetail[i].initialbalance = dataSummary[j].initialbalance;
        dataDetail[i].currentbalance = dataSummary[j].initialbalance;
        acum = acum.add(new BigDecimal(dataDetail[i].convertedamount));
        if ("Y".equals(dataDetail[i].isreceipt)) {
          income = income.add(new BigDecimal(dataDetail[i].convertedamount));
        } else {
          payment = payment.subtract(new BigDecimal(dataDetail[i].convertedamount));
        }
      }
      dataSummary[j].income = income.toString();
      dataSummary[j].payment = payment.toString();
      dataSummary[j].finalsummary = acum.toString();
      if (dataDetail.length > 0) {
        vDatas.addElement(dataDetail);
        vHeader.addElement(dataSummary[j]);
      }
    }
    data = new CashflowForecastData[vDatas.size()][];
    vDatas.copyInto(data);
    dataHeader = new CashflowForecastData[vHeader.size()];
    vHeader.copyInto(dataHeader);

    return dataSummary;
  }

  private CashflowForecastData[] obtainSummaryFieldProvider(VariablesSecureApp vars,
      String strFinancialAccount, String strDatePlanned) throws ServletException {
    return CashflowForecastData.selectSummary(this, vars.getLanguage(),
        Utility.getContext(this, vars, "#User_Client", "CashflowForecast"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "CashflowForecast"),
        strFinancialAccount, strDatePlanned);
  }

  private void printJRXML(HttpServletResponse response, VariablesSecureApp vars,
      String strFinancialAccountId, String strDateMax, boolean breakByDate, String strOutputtype)
      throws IOException, ServletException {

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    final String strLanguage = vars.getLanguage();
    final String strBaseDesign = getBaseDesignPath(strLanguage);

    // compile subreports
    JasperReport jrDetail = null;
    try {
      jrDetail = Utility.getTranslatedJasperReport(this, strBaseDesign
          + "/org/openbravo/financial/cashflowforecast/"
          + (breakByDate ? "CashflowForecastLinesByDate.jrxml" : "CashflowForecastLines.jrxml"),
          vars.getLanguage(), strBaseDesign);
    } catch (JRException e) {
      log4j.error("Error Compiling report ", e);
      throw new ServletException(e.getMessage(), e);
    }

    JasperReport jrSummary = null;
    try {
      jrSummary = Utility.getTranslatedJasperReport(this, strBaseDesign
          + "/org/openbravo/financial/cashflowforecast/CashflowForecastSummary.jrxml",
          vars.getLanguage(), strBaseDesign);
    } catch (JRException e) {
      log4j.error("Error Compiling report ", e);
      throw new ServletException(e.getMessage(), e);
    }

    CashflowForecastData[] dataSummary = obtainSummaryFieldProvider(vars, strFinancialAccountId,
        strDateMax);
    dataSummary = obtainLinesFieldProvider(dataSummary, vars, strDateMax, breakByDate);

    parameters.put("BreakByDate", breakByDate);
    parameters.put("DetailMainJR", jrDetail);
    parameters.put("DetailSummary", jrSummary);
    parameters.put("DatePlanned", FIN_Utility.getDate(strDateMax));
    parameters.put("FinancialAccountId", strFinancialAccountId);
    if ("PDF".equals(strOutputtype))
      parameters.put("fieldProviderSubReport",
          new JRFieldProviderDataSource(unifyData(data), vars.getJavaDateFormat()));
    parameters.put("fieldProviderSummary",
        new JRFieldProviderDataSource(dataSummary, vars.getJavaDateFormat()));
    String strReportName = ("PDF".equals(strOutputtype)) ? "@basedesign@/org/openbravo/financial/cashflowforecast/CashflowForecast.jrxml"
        : "@basedesign@/org/openbravo/financial/cashflowforecast/CashflowForecastExcel.jrxml";
    renderJR(vars, response, strReportName, strOutputtype.toLowerCase(), parameters,
        ("PDF".equals(strOutputtype)) ? null : unifyData(data), null);
  }

  private FieldProvider[] unifyData(CashflowForecastData[][] dataToUnify) {
    int length = 0;
    for (int i = 0; i < dataToUnify.length; i++)
      length += dataToUnify[i].length;
    FieldProvider[] result = new FieldProvider[length];
    int index = 0;
    for (int i = 0; i < dataToUnify.length; i++)
      for (int j = 0; j < dataToUnify[i].length; j++)
        result[index++] = dataToUnify[i][j];
    return result;
  }

  public String getServletInfo() {
    return "Servlet CashflowForecast";
  } // end of the getServletInfo() method
}
