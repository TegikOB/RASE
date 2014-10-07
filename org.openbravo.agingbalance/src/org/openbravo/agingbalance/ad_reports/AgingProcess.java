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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.agingbalance.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.info.SelectorUtilityData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.xmlEngine.XmlDocument;

public class AgingProcess extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private final AgingDao dao = new AgingDao();

  /**
   * Method for selecting what type of document is it going to be printed and for recovering the
   * parameters from the HTML doc.
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String recOrPay = "";
      if (request.getRequestURL().toString()
          .contains("/org.openbravo.agingbalance/ad_reports/ReceivablesAgingInquiry.html")) {
        recOrPay = "RECEIVABLES";
      } else {
        recOrPay = "PAYABLES";
      }
      String strcolumn1 = vars.getNumericGlobalVariable("inpColumn1", "AgingInquiry|Column1", "30");
      String strcolumn2 = vars.getNumericGlobalVariable("inpColumn2", "AgingInquiry|Column2", "60");
      String strcolumn3 = vars.getNumericGlobalVariable("inpColumn3", "AgingInquiry|Column3", "90");
      String strcolumn4 = vars
          .getNumericGlobalVariable("inpColumn4", "AgingInquiry|Column4", "120");
      String strOrg = vars.getGlobalVariable("inpOrg", "AgingInquiry|Org", "");
      String strcBpartnerId = vars.getInGlobalVariable("inpBPartnerId_IN",
          "AgingInquiry|inpBPartnerId_IN", "", IsIDFilter.instance);
      String strAccSchema = vars.getGlobalVariable("inpAccSchema", "AgingInquiry|AccSchema", "");
      String strCurrentDate = vars.getGlobalVariable("inpCurrentDate", "AgingInquiry|CurrentDate",
          "");
      String strDetails = vars.getGlobalVariable("inpDetails", "AgingInquiry|details", "false");
      String strDoubtful = vars.getGlobalVariable("inpDoubtful", "AgingInquiry|doubtful", "false");
      printPageDataSheet(response, vars, strOrg, strcBpartnerId, strAccSchema, strCurrentDate,
          strcolumn1, strcolumn2, strcolumn3, strcolumn4, recOrPay, strDetails, strDoubtful);
    } else if (vars.commandIn("DIRECT", "PRINT_PDF", "PRINT_XLS")) {
      String recOrPay = "";
      if (request.getRequestURL().toString()
          .contains("/org.openbravo.agingbalance/ad_reports/ReceivablesAgingSchedule.html")
          || request.getRequestURL().toString()
              .contains("/org.openbravo.agingbalance/ad_reports/ReceivablesAgingInquiry.html")
          || request
              .getRequestURL()
              .toString()
              .contains(
                  "/org.openbravo.agingbalance/ad_reports/ReceivablesAgingScheduleDetail.html")) {
        recOrPay = "RECEIVABLES";
      } else {
        recOrPay = "PAYABLES";
      }
      String strcolumn1 = vars
          .getNumericRequestGlobalVariable("inpColumn1", "AgingInquiry|Column1");
      String strcolumn2 = vars
          .getNumericRequestGlobalVariable("inpColumn2", "AgingInquiry|Column2");
      String strcolumn3 = vars
          .getNumericRequestGlobalVariable("inpColumn3", "AgingInquiry|Column3");
      String strcolumn4 = vars
          .getNumericRequestGlobalVariable("inpColumn4", "AgingInquiry|Column4");
      String strOrg = vars.getRequestGlobalVariable("inpOrg", "AgingInquiry|Org");
      String strcBpartnerId = vars.getRequestGlobalVariable("inpBPartnerId_IN",
          "AgingInquiry|inpBPartnerId_IN");
      String strAccSchema = vars.getRequestGlobalVariable("inpAccSchema", "AgingInquiry|AccSchema");
      String strCurrentDate = vars.getRequestGlobalVariable("inpCurrentDate",
          "AgingInquiry|CurrentDate");
      String strDetails = vars.getRequestGlobalVariable("inpDetails", "AgingInquiry|details");
      String strDoubtful = vars.getRequestGlobalVariable("inpDoubtful", "AgingInquiry|doubtful");
      String strOutput = "html";
      if (vars.commandIn("PRINT_PDF")) {
        strOutput = "pdf";
      } else if (vars.commandIn("PRINT_XLS")) {
        strOutput = "xls";
      }
      if (strDetails.equals("true")) {
        printPageDetails(request, response, vars, strOrg, strcBpartnerId, strAccSchema,
            strCurrentDate, recOrPay, strOutput, strcolumn1, strcolumn2, strcolumn3, strcolumn4,
            strDoubtful);
      } else {
        printPageSchedule(request, response, vars, strOrg, strcBpartnerId, strAccSchema,
            strCurrentDate, strcolumn1, strcolumn2, strcolumn3, strcolumn4, strOutput, recOrPay,
            strDoubtful);
      }
    } else if (vars.commandIn("GRID", "GRID_PDF", "GRID_XLS")) {
      // The reason for using GRID as the command parameter is because of the method called later to
      // open a tab,
      // only four commands are supported without giving error messages in the log
      String recOrPay = "";
      if (request.getRequestURL().toString()
          .contains("/org.openbravo.agingbalance/ad_reports/ReceivablesAgingScheduleDetail.html")) {
        recOrPay = "RECEIVABLES";
      } else {
        recOrPay = "PAYABLES";
      }
      String strDoubtful = vars.getRequestGlobalVariable("inpDoubtful", "AgingInquiry|doubtful");
      String strOrg = vars.getRequestGlobalVariable("inpOrg", "AgingInquiry|Org");
      String strcBpartnerId = vars.getRequestGlobalVariable("inpBPartnerId",
          "AgingInquiry|inpBPartnerId");
      String strAccSchema = vars.getRequestGlobalVariable("inpAccSchema", "AgingInquiry|AccSchema");
      String strCurrentDate = vars.getRequestGlobalVariable("inpCurrentDate",
          "AgingInquiry|CurrentDate");
      String strcolumn1 = vars
          .getNumericRequestGlobalVariable("inpColumn1", "AgingInquiry|Column1");
      String strcolumn2 = vars
          .getNumericRequestGlobalVariable("inpColumn2", "AgingInquiry|Column2");
      String strcolumn3 = vars
          .getNumericRequestGlobalVariable("inpColumn3", "AgingInquiry|Column3");
      String strcolumn4 = vars
          .getNumericRequestGlobalVariable("inpColumn4", "AgingInquiry|Column4");
      String strOutput = "html";
      if (vars.commandIn("GRID_PDF")) {
        strOutput = "pdf";
      } else if (vars.commandIn("GRID_XLS")) {
        strOutput = "xls";
      }
      printPageDetails(request, response, vars, strOrg, strcBpartnerId, strAccSchema,
          strCurrentDate, recOrPay, strOutput, strcolumn1, strcolumn2, strcolumn3, strcolumn4,
          strDoubtful);
    } else if (vars.commandIn("ACCTSCHEMA")) {
      // This one is for filling the combobox with the general ledger values
      String strOrg = vars.getGlobalVariable("inpOrg", "AgingInquiry|Org", "");
      printPageAcctSchemaComboBox(response, vars, strOrg);
    } else {
      pageError(response);
    }
  }

  /**
   * This method prints the information of the Open Receivables Aging Schedule. It can be printed in
   * HTML, XLS or PDF formats
   * 
   */
  private void printPageSchedule(final HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrgTrx, String strcBpartnerId, String strAccSchema,
      String strCurrentDate, String strcolumn1, String strcolumn2, String strcolumn3,
      String strcolumn4, String strOutput, String recOrPay, String strDoubtful) throws IOException,
      ServletException {

    String dateFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);

    try {

      Date currentDate = dateFormat.parse(strCurrentDate);
      Calendar cal = new GregorianCalendar();
      cal.setTime(currentDate);
      Currency toCurrency = null;
      String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
      String strOrgFamily = getFamily(strTreeOrg, strOrgTrx);
      boolean showDoubtful = "true".equals(strDoubtful);

      // Get the Data
      FieldProvider[] data = dao.getOpenReceivablesAgingSchedule(strcBpartnerId, strAccSchema,
          currentDate, strcolumn1, strcolumn2, strcolumn3, strcolumn4, strOrgTrx, strOrgFamily,
          recOrPay, showDoubtful);

      // Set the Parameters
      AcctSchema acctSchema = null;
      Organization organization = null;
      OBContext.setAdminMode(true);
      try {
        if (strAccSchema == null || "".equals(strAccSchema)) {
          organization = OBDal.getInstance().get(Organization.class, strOrgTrx);
          toCurrency = organization.getCurrency();
        } else {
          acctSchema = OBDal.getInstance().get(AcctSchema.class, strAccSchema);
          toCurrency = acctSchema.getCurrency();
        }
      } finally {
        OBContext.restorePreviousMode();
      }
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("currentDate", dateFormat.format(currentDate));
      parameters.put("col1", "1-" + strcolumn1);
      parameters.put("inpColumn1", strcolumn1);
      cal.add(Calendar.DATE, -Integer.parseInt(strcolumn1));
      parameters.put("Date1", dateFormat.format(cal.getTime()));
      parameters.put("col2", String.valueOf((Integer.valueOf(strcolumn1) + 1)) + "-" + strcolumn2);
      parameters.put("inpColumn2", strcolumn2);
      cal.add(Calendar.DATE, -((Integer.parseInt(strcolumn2) - (Integer.parseInt(strcolumn1)))));
      parameters.put("Date2", dateFormat.format(cal.getTime()));
      parameters.put("col3", String.valueOf((Integer.valueOf(strcolumn2) + 1)) + "-" + strcolumn3);
      parameters.put("inpColumn3", strcolumn3);
      cal.add(Calendar.DATE, -((Integer.parseInt(strcolumn3) - (Integer.parseInt(strcolumn2)))));
      parameters.put("Date3", dateFormat.format(cal.getTime()));
      parameters.put("col4", String.valueOf((Integer.valueOf(strcolumn3) + 1)) + "-" + strcolumn4);
      parameters.put("inpColumn4", strcolumn4);
      cal.add(Calendar.DATE, -((Integer.parseInt(strcolumn4) - (Integer.parseInt(strcolumn3)))));
      parameters.put("Date4", dateFormat.format(cal.getTime()));
      parameters.put("col5", ">" + strcolumn4);
      parameters.put("BPartners", strcBpartnerId.replace("'", "\\\'"));
      parameters.put("Organization", strOrgTrx);
      parameters.put("AccSchema", strAccSchema);
      parameters.put("OrganizationName", OBDal.getInstance().get(Organization.class, strOrgTrx)
          .getIdentifier());
      parameters.put("AccSchemaName", (acctSchema != null) ? acctSchema.getIdentifier() : "");
      parameters.put("Currency", Utility.stringBaseCurrencyId(this, vars.getClient()));
      parameters.put("toCurrency", toCurrency.getId());
      if (recOrPay.equals("RECEIVABLES")) {
        parameters.put("tabTitle", Utility.messageBD(this, "AGING_ORASD", vars.getLanguage()));
        parameters.put("title", Utility.messageBD(this, "AGING_ORAS", vars.getLanguage()));
        parameters.put("url",
            "/org.openbravo.agingbalance/ad_reports/ReceivablesAgingScheduleDetail.html");
      } else {
        // PAYABLES
        parameters.put("tabTitle", Utility.messageBD(this, "AGING_PASD", vars.getLanguage()));
        parameters.put("title", Utility.messageBD(this, "AGING_PAS", vars.getLanguage()));
        parameters.put("url",
            "/org.openbravo.agingbalance/ad_reports/PayablesAgingScheduleDetail.html");
      }
      parameters.put("showDoubtfulDebt", strDoubtful);

      String strReportName = "";

      if (strOutput.equals("html")) {
        strReportName = "@basedesign@/org/openbravo/agingbalance/ad_reports/AgingScheduleHTML.jrxml";
      } else if (strOutput.equals("pdf")) {
        if (showDoubtful) {
          strReportName = "@basedesign@/org/openbravo/agingbalance/ad_reports/AgingSchedulePDFDoubtfulDebt.jrxml";
        } else {
          strReportName = "@basedesign@/org/openbravo/agingbalance/ad_reports/AgingSchedulePDF.jrxml";
        }
        response.setHeader("Content-disposition", "attach; filename=ReceivablesAgingSchedule.pdf");
      } else if (strOutput.equals("xls")) {
        strReportName = "@basedesign@/org/openbravo/agingbalance/ad_reports/AgingScheduleXLS.jrxml";
        response.setHeader("Content-disposition", "attach; filename=ReceivablesAgingSchedule.xls");
      }

      if (data.length == 0) {
        advise(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      } else {
        renderJR(vars, response, strReportName, strOutput, parameters, data, null);
      }

    } catch (OBException ex) {
      if (ex.getMessage().equals("No Conversion Rate")) {
        final OBError message = new OBError();
        message.setType("Error");
        message.setTitle(Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()));
        message.setMessage(Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage())
            .getMessage());
        advise(request, response, message.getType(), message.getTitle(), message.getMessage());
      } else {
        advise(request, response, "Error", Utility.messageBD(this, "Error", vars.getLanguage()),
            Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage()).getMessage());
      }
    } catch (Exception ex) {
      // Error parsing date
      advise(request, response, "Error", Utility.messageBD(this, "Error", vars.getLanguage()),
          Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage()).getMessage());
    }
  }

  /**
   * This method prints the information of the Open Receivables Aging Schedule Details in HTML
   * format.
   */
  private void printPageDetails(final HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrg, String strcBpartnerId, String strAccSchema,
      String strCurrentDate, String recOrPay, String strOutput, String strcolumn1,
      String strcolumn2, String strcolumn3, String strcolumn4, String strDoubtful)
      throws IOException, ServletException {

    List<BusinessPartner> bPartners = OBDao.getOBObjectListFromString(BusinessPartner.class,
        strcBpartnerId);
    String dateFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
    Currency convCurrency = null;
    boolean showDoubtful = "true".equals(strDoubtful);

    try {

      Date currentDate = dateFormat.parse(strCurrentDate);
      String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
      String strOrgFamily = getFamily(strTreeOrg, strOrg);

      AcctSchema acctSchema = null;
      Organization organization = null;
      OBContext.setAdminMode(true);
      try {
        if (strAccSchema == null || "".equals(strAccSchema)) {
          organization = OBDal.getInstance().get(Organization.class, strOrg);
          convCurrency = organization.getCurrency();
        } else {
          acctSchema = OBDal.getInstance().get(AcctSchema.class, strAccSchema);
          convCurrency = acctSchema.getCurrency();
        }
      } finally {
        OBContext.restorePreviousMode();
      }

      // Get the data
      FieldProvider[] data = dao.getOpenReceivablesAgingScheduleDetails(bPartners, strAccSchema,
          currentDate, dateFormat, convCurrency, strOrg, strOrgFamily, recOrPay, strcolumn1,
          strcolumn2, strcolumn3, strcolumn4, strcBpartnerId, showDoubtful);

      // Parameters
      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("currentDate", strCurrentDate);
      parameters.put("BPartnerID", strcBpartnerId.replace("'", "\\\'"));
      parameters.put("organizationID", strOrg);
      parameters.put("accSchemaID", strAccSchema);
      parameters.put("Organization", OBDal.getInstance().get(Organization.class, strOrg)
          .getIdentifier());
      parameters.put("AccSchema", acctSchema.getIdentifier());
      parameters.put("recOrPay", recOrPay);
      parameters.put("showDoubtfulDebt", strDoubtful);
      parameters.put("inpColumn1", strcolumn1);
      parameters.put("inpColumn2", strcolumn2);
      parameters.put("inpColumn3", strcolumn3);
      parameters.put("inpColumn4", strcolumn4);
      parameters.put("inpLabel1", "1-" + strcolumn1);
      parameters.put("inpLabel2", (Integer.valueOf(strcolumn1) + 1) + "-" + strcolumn2);
      parameters.put("inpLabel3", (Integer.valueOf(strcolumn2) + 1) + "-" + strcolumn3);
      parameters.put("inpLabel4", (Integer.valueOf(strcolumn3) + 1) + "-" + strcolumn4);
      parameters.put("inpLabel5", "Over " + strcolumn4);

      if (recOrPay.equals("RECEIVABLES")) {
        parameters.put("title", Utility.messageBD(this, "AGING_ORASD", vars.getLanguage()));
        parameters.put("tabID", "263");
        parameters.put("tabTitle", Utility.messageBD(this, "AGING_ORASD", vars.getLanguage()));
        parameters.put("url",
            "/org.openbravo.agingbalance/ad_reports/ReceivablesAgingScheduleDetail.html");
      } else {
        parameters.put("title", Utility.messageBD(this, "AGING_PASD", vars.getLanguage()));
        parameters.put("tabID", "290");
        parameters.put("tabTitle", Utility.messageBD(this, "AGING_PASD", vars.getLanguage()));
        parameters.put("url",
            "/org.openbravo.agingbalance/ad_reports/PayablesAgingScheduleDetail.html");
      }

      String strReportName = "";
      if (strOutput.equals("html")) {
        strReportName = "@basedesign@/org/openbravo/agingbalance/ad_reports/AgingScheduleDetailHTML.jrxml";
      } else if (strOutput.equals("pdf")) {
        if (showDoubtful) {
          strReportName = "@basedesign@/org/openbravo/agingbalance/ad_reports/AgingScheduleDetailPDFDoubtfulDebt.jrxml";
        } else {
          strReportName = "@basedesign@/org/openbravo/agingbalance/ad_reports/AgingScheduleDetailPDF.jrxml";
        }
        response.setHeader("Content-disposition", "attach; filename=ReceivablesAgingSchedule.pdf");
      } else if (strOutput.equals("xls")) {
        if (showDoubtful) {
          strReportName = "@basedesign@/org/openbravo/agingbalance/ad_reports/AgingScheduleDetailXLSDoubtfulDebt.jrxml";
        } else {
          strReportName = "@basedesign@/org/openbravo/agingbalance/ad_reports/AgingScheduleDetailXLS.jrxml";
        }
        response.setHeader("Content-disposition", "attach; filename=ReceivablesAgingSchedule.xls");
      }

      if (data.length == 0) {
        advise(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
      } else {
        renderJR(vars, response, strReportName, strOutput, parameters, data, null);
      }

    } catch (OBException ex) {
      if (ex.getMessage().equals("No Conversion Rate")) {
        final OBError message = new OBError();
        message.setType("Error");
        message.setTitle(Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()));
        message.setMessage(Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage())
            .getMessage());
        advise(request, response, message.getType(), message.getTitle(), message.getMessage());
      } else {
        advise(request, response, "Error", Utility.messageBD(this, "Error", vars.getLanguage()),
            Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage()).getMessage());
      }
    } catch (Exception ex) {
      // Error parsing date
      advise(request, response, "Error", Utility.messageBD(this, "Error", vars.getLanguage()),
          Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage()).getMessage());
    }
  }

  /**
   * If DEFAULT type was selected, then this method is called. Sets the parameters of the HTML file
   * using a .XML file for mapping them.
   * 
   * @throws IOException
   * @throws ServletException
   */
  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strOrgTrx, String strcBpartnerId, String strAccSchema, String strCurrentDate,
      String strcolumn1, String strcolumn2, String strcolumn3, String strcolumn4, String recOrPay,
      String strDetails, String strDoubtful) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    String dateFormat = vars.getSessionValue("#AD_SqlDateFormat");
    String tabTitle = "";
    String tabTitleDetails = "";

    xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/agingbalance/ad_reports/AgingInquiry")
        .createXmlDocument();

    if (recOrPay.equals("RECEIVABLES")) {
      tabTitle = Utility.messageBD(this, "AGING_ORAS", vars.getLanguage());
      tabTitleDetails = Utility.messageBD(this, "AGING_ORASD", vars.getLanguage());
    } else if (recOrPay.equals("PAYABLES")) {
      tabTitle = Utility.messageBD(this, "AGING_PAS", vars.getLanguage());
      tabTitleDetails = Utility.messageBD(this, "AGING_PASD", vars.getLanguage());
    } else {
      throw new OBException("Error");
    }

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "AgingInquiry", false, "", "",
        "printPDF();return false;", false, "ad_reports", strReplaceWith, false, true);
    toolbar.setEmail(false);
    toolbar.removeElement("REFRESH");
    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "org.openbravo.agingbalance.ad_reports.AgingInquiry");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "AgingInquiry", classInfo.id,
          classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "AgingInquiry", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("AgingInquiry");
      vars.removeMessage("AgingInquiry");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");

    xmlDocument.setParameter("tabTitle", tabTitle);
    xmlDocument.setParameter("tabTitleDetails", tabTitleDetails);
    xmlDocument.setParameter("currentDate", DateTimeData.today(this));
    xmlDocument.setParameter("currentDateDisplayFormat", dateFormat);
    xmlDocument.setParameter("currentDateSaveFormat", dateFormat);

    xmlDocument.setParameter("column1", strcolumn1);
    xmlDocument.setParameter("column2greater", Integer.toString(Integer.parseInt(strcolumn1) + 1));
    xmlDocument.setParameter("column2", strcolumn2);
    xmlDocument.setParameter("column3greater", Integer.toString(Integer.parseInt(strcolumn2) + 1));
    xmlDocument.setParameter("column3", strcolumn3);
    xmlDocument.setParameter("column4greater", Integer.toString(Integer.parseInt(strcolumn3) + 1));
    xmlDocument.setParameter("column4", strcolumn4);
    xmlDocument.setParameter("paramAD_ORG_Id", strOrgTrx);
    xmlDocument.setParameter("details", strDetails);
    xmlDocument.setParameter("doubtful", strDoubtful);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
          "AD_Org is transactions allowed", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "AgingInquiry"), Utility.getContext(this, vars, "#User_Client", "AgingInquiry"), '0');
      comboTableData.fillParameters(null, "AgingInquiry", strOrgTrx);
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setData(
        "inpBPartnerId_IN",
        "liststructure",
        SelectorUtilityData.selectBpartner(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", ""),
            Utility.getContext(this, vars, "#User_Client", ""), strcBpartnerId));

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * This method is used to fill the combobox with the information of the generalLedger
   */
  private void printPageAcctSchemaComboBox(HttpServletResponse response, VariablesSecureApp vars,
      String strOrg) throws IOException, ServletException {
    Set<String> orgPartentTree = OBContext.getOBContext().getOrganizationStructureProvider()
        .getParentTree(strOrg, true);

    OBContext.setAdminMode(true);
    try {
      OBCriteria<AcctSchema> obCriteria = OBDal.getInstance().createCriteria(AcctSchema.class);
      obCriteria.createAlias(AcctSchema.PROPERTY_ORGANIZATION, "o", OBCriteria.LEFT_JOIN);
      obCriteria.add(Restrictions.in("o." + Organization.PROPERTY_ID, orgPartentTree));
      List<AcctSchema> acctSchemaList = obCriteria.list();
      if (acctSchemaList.size() > 0) {
        String combobox = getJSONComboBox(acctSchemaList, acctSchemaList.get(0).getId(), true);
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("objson = " + combobox);
        out.close();
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Returns a String containing the html code with the options based on the given List of
   * BaseOBObjects
   * 
   * @param <T>
   *          Class that extends BaseOBObject.
   * @param obObjectList
   *          List containing the values to be included in the options.
   * @param selectedValue
   *          value to set as selected by default.
   * @param isMandatory
   *          boolean to add a blank option in the options list.
   * @return a String containing the html code with the options. *
   */
  private <T extends BaseOBObject> String getJSONComboBox(List<T> obObjectList,
      String selectedValue, boolean isMandatory) {
    // Create JSON object
    // {"optionlist":[{"text":"English (USA)","selected":"true","value":"192"},
    // {"text":"Spanish (Spain)","selected":"false","value":"140"}],
    // "ismandatory":"true"}

    JSONObject json = new JSONObject();
    JSONArray select = new JSONArray();
    Map<String, String> attr = null;
    try {
      int i = 0;
      for (T ob : obObjectList) {
        attr = new HashMap<String, String>();
        attr.put("value", ob.getId().toString());
        attr.put("selected", (ob.getId().equals(selectedValue)) ? "true" : "false");
        attr.put("text", ob.getIdentifier());
        select.put(i, attr);
        json.put("optionlist", select);
        i++;
      }
      json.put("ismandatory", String.valueOf(isMandatory));

    } catch (JSONException e) {
      log4j.error("Error creating JSON object for representing subaccount lines", e);
    }

    return json.toString();
  }

  private String getFamily(String strTree, String strChild) throws IOException, ServletException {
    return Tree.getMembers(this, strTree, strChild);
    /*
     * ReportGeneralLedgerData [] data = ReportGeneralLedgerData.selectChildren(this, strTree,
     * strChild); String strFamily = ""; if(data!=null && data.length>0) { for (int i =
     * 0;i<data.length;i++){ if (i>0) strFamily = strFamily + ","; strFamily = strFamily +
     * data[i].id; } return strFamily; }else return "'1'";
     */
  }

  public String getServletInfo() {
    return "Servlet AgingInquiry.";
  } // end of getServletInfo() method
}
