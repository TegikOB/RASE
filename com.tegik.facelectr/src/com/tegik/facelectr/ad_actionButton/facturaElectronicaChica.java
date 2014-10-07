package com.tegik.facelectr.ad_actionButton;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;

/**
 * Creando una factura electrónica para México.
 * 
 * @author Tegik
 */
public class facturaElectronicaChica extends HttpSecureAppServlet {

  private static final Logger log = Logger.getLogger(facturaElectronicaChica.class);

  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  // main HTTP call handler
  synchronized public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {

      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strInvoiceId = vars.getStringParameter("inpcInvoiceId");
      String strWindowPath = Utility.getTabURL(this, strTab, "R");

      log.info("En la factura electrónica chica");

      // parse required Guest ID parameter to be processed
      facturaElectronica facturaProcess = new facturaElectronica();

      ServletConfig srvConfig = getServletConfig();

      OBError myMessage = facturaProcess.facturar(null, null, null, null, strInvoiceId, null);

      vars.setMessage(strTab, myMessage);
      printPageClosePopUp(response, vars, strWindowPath);
    }
  }

}
