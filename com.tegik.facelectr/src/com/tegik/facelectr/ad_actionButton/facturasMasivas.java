package com.tegik.facelectr.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.invoice.Invoice;

/**
 * Creando una factura electrónica para México.
 * 
 * @author Tegik
 */
public class facturasMasivas extends HttpSecureAppServlet {

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  private static final Logger log = Logger.getLogger(facturasMasivas.class);

  // main HTTP call handler
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {

      // parse required Guest ID parameter to be processed
      String strInvoiceId = "nada";
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

      int year = 2012;
      int month = 8;
      int day = 1;

      String date = year + "/" + month + "/" + day;
      Date utilDate = null;

      try {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        utilDate = formatter.parse(date);
        System.out.println("utilDate:" + utilDate);
      } catch (ParseException e) {
        System.out.println(e.toString());
        e.printStackTrace();
      }

      String strMensajeSalida = "";
      OBContext.setAdminMode(true);
      OBError myMessage = new OBError();
      String strClientId = vars.getClient();
      log.info("antes");
      final OBCriteria<Invoice> invoiceList = OBDal.getInstance().createCriteria(Invoice.class);
      invoiceList.add(Expression.eq(Invoice.PROPERTY_DOCUMENTSTATUS, "CO"));
      invoiceList.add(Expression.eq(Invoice.PROPERTY_FETCORREOENVIADO, false));
      invoiceList.add(Expression.eq(Invoice.PROPERTY_FETDOCSTATUS, "Facturado"));
      invoiceList.add(Expression.eq(Invoice.PROPERTY_SALESTRANSACTION, true));
      invoiceList.add(Expression.ge(Invoice.PROPERTY_INVOICEDATE, utilDate));
      invoiceList.addOrderBy(Invoice.PROPERTY_FETINTENTO, true);

      enviadorCorreos enviador = new enviadorCorreos();
      enviador.init(getServletConfig());
      log.info("despues");

      int contador = 0;
      for (Invoice factura : invoiceList.list()) {
        log.info("CSM>CLIENT_ID>" + factura.getClient().getId());
        // Invoice factura = OBDal.getInstance().get(Invoice.class,strInvoiceId);
        // ServletConfig config2 = ;

        if (contador == 100) {
          break;
        }

        if (factura.getFetCorreoalternativo() == null
            && factura.getBusinessPartner().getFetEmail() == null) {
          continue;
        }

        String respuestaEnvio = enviador.solicitarEnvio(vars, factura, "Y", "Y", null);
        log.info("CSM>CORREOS -- " + respuestaEnvio);
        if (respuestaEnvio == "OK") {
          // myMessage.setType("Success");
          // myMessage.setTitle("Correo enviado correctamente");
          strMensajeSalida = strMensajeSalida + " -- \n" + "Factura: " + factura.getDocumentNo()
              + "> OK > " + respuestaEnvio;
          OBContext.restorePreviousMode();
          // return myMessage;
        } else {
          // myMessage.setType("Error");
          // myMessage.setTitle("Hubo un error al enviar el correo electrónico");
          strMensajeSalida = strMensajeSalida + " -- \n" + "Factura: " + factura.getDocumentNo()
              + "> ERROR > " + respuestaEnvio;
          OBContext.restorePreviousMode();
          // return myMessage;
        }
        contador++;
      }

      myMessage.setType("Success");
      myMessage.setTitle("Estatus de los correos");
      myMessage.setMessage(strMensajeSalida);
      return myMessage;

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
