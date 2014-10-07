package com.tegik.facelectr.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.invoice.Invoice;

/**
 * Creando una factura electrónica para México.
 * 
 * @author Tegik
 */
public class enviarFactura extends HttpSecureAppServlet {

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  private static final Logger log = Logger.getLogger(enviarFactura.class);

  // main HTTP call handler
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {

      // parse required Guest ID parameter to be processed
      String strInvoiceId = vars.getStringParameter("inpcInvoiceId");
      log.info("CSM>CORREOS -- " + strInvoiceId);

      // construct the reload path so that upon completion of the process
      // the main editing window is reloaded and the resulting message is
      // shown
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strWindowPath = Utility.getTabURL(this, strTab, "R");
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      OBError myMessage = enviarFacturaCorreo(vars, strInvoiceId, strTab);
      vars.setMessage(strTab, myMessage);
      printPageClosePopUp(response, vars, strWindowPath);

    }

  }

  public OBError enviarFacturaCorreo(VariablesSecureApp vars, String strInvoiceId, String strTab)
      throws IOException, ServletException {
    try {
      OBContext.setAdminMode(true);
      OBError myMessage = new OBError();
      Invoice factura = OBDal.getInstance().get(Invoice.class, strInvoiceId);
      enviadorCorreos enviador = new enviadorCorreos();
      // ServletConfig config2 = ;
      enviador.init(getServletConfig());
      String respuestaEnvio = enviador.solicitarEnvio(vars, factura, "Y", "Y", null);
      log.info("CSM>CORREOS -- " + respuestaEnvio);
      if (respuestaEnvio == "OK") {
        myMessage.setType("Success");
        myMessage.setTitle("Correo enviado correctamente");
        OBContext.restorePreviousMode();
        return myMessage;
      } else {
        myMessage.setType("Error");
        myMessage.setTitle("Hubo un error al enviar el correo electrónico");
        OBContext.restorePreviousMode();
        return myMessage;
      }

    } catch (Exception e) {
      log.info("CSM>CORREOS -- " + e.toString());
      OBError myMessage2 = new OBError();
      StringWriter w = new StringWriter();
      e.printStackTrace(new PrintWriter(w));
      String errorfactura = w.toString();
      log.info("CSM>CORREOS -- " + errorfactura);
      myMessage2.setType("Error");
      myMessage2.setTitle("Hubo un error al enviar el correo electrónico");
      OBContext.restorePreviousMode();
      return myMessage2;
    }
  }

}
