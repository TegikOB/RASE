package com.tegik.facelectr.ad_actionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.common.invoice.Invoice;

/**
 * Creando una factura electrónica para México.
 * 
 * @author Tegik
 */

public class CancelarFacturaBoton extends HttpSecureAppServlet {

  private static final Logger log = Logger.getLogger(CancelarFacturaBoton.class);

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

      OBContext.setAdminMode(true);

      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strWindowPath = Utility.getTabURL(this, strTab, "R");

      // obtengo el id
      String strInvoiceId = vars.getStringParameter("inpcInvoiceId");

      String Separador = System.getProperty("file.separator");

      // Obtengo el objecto Invoice con el id
      Invoice factura = OBDal.getInstance().get(Invoice.class, strInvoiceId);

      // Obtengo el tab de la pestaña
      Tab tabFactura = OBDal.getInstance().get(Tab.class, strTab);
      // Obtengo el passwordPac
      String PasswordPAC = factura.getOrganization().getFetPasspac();

      // Obtengo la ruta
      String attachFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("attach.path");
      String ruta = attachFolder + "/" + tabFactura.getTable().getId() + "-" + strInvoiceId
          + Separador;

      // Obtengo el numero de factura
      String NumFac = factura.getDocumentNo();

      // obtengo el RFC del Emisor
      String RFC_Emisor = factura.getOrganization().getOrganizationInformationList().get(0)
          .getTaxID();

      // Obtengo el RCF del receptor
      String RFC_Receptor = factura.getBusinessPartner().getTaxID();

      final OBCriteria<Attachment> attachmentList = OBDal.getInstance().createCriteria(
          Attachment.class);
      attachmentList.add(Expression.eq(Attachment.PROPERTY_TABLE,
          OBDal.getInstance().get(Table.class, "155")));
      attachmentList.add(Expression.eq(Attachment.PROPERTY_RECORD, factura.getOrganization()
          .getId()));

      String nombrePfx = "";
      String pathPfx = "";

      for (Attachment attachmentUd : attachmentList.list()) {
        if (attachmentUd.getName().indexOf(".pfx") != -1) {
	  nombrePfx = attachmentUd.getName();
	  pathPfx = attachmentUd.getPath();
	  if (pathPfx == "" || pathPfx == null) {
	      pathPfx = "155" + "-" + factura.getOrganization().getId();
	  }
	}
      }
          
      String archivoPac = attachFolder + "/" + pathPfx + Separador.substring(0, 1) + nombrePfx;    

      String uuidFactura = factura.getFetFoliofiscal();

      String urlCancelar = factura.getClient().getFetUrlwebser();

      String refId = SequenceIdData.getUUID();

      cancelarFactura cancelFact = new cancelarFactura();
      String ruta_response, canceled, mensage, fechaCanc;

      OBError msg = new OBError();
      ruta_response = ruta + "ResponseCancela" + NumFac + ".xml";

      File f = new File(ruta_response);
      if (f.exists()) {
        canceled = getValuefromXML(ruta_response, "canceled");
        fechaCanc = getValuefromXML(ruta_response, "fecha");
        if (canceled.equals("true")) {
          factura.setFetDocstatus("Cancelado");
	  factura.setFetFechacancel(fechaCanc);
          OBDal.getInstance().save(factura);
          OBDal.getInstance().flush();
          msg.setType("Success");
          msg.setMessage("La Factura ha sido cancelada");

        } else {
        try
        {
          ruta_response = cancelFact.cancelarFac(ruta, NumFac, RFC_Emisor, RFC_Receptor,
              uuidFactura, refId, PasswordPAC, archivoPac, urlCancelar);
          canceled = getValuefromXML(ruta_response, "canceled");
          mensage = getValuefromXML(ruta_response, "message");
          fechaCanc = getValuefromXML(ruta_response, "fecha");
          if (canceled.equals("true")) {
            factura.setFetDocstatus("Cancelado");
            factura.setFetFechacancel(fechaCanc);
            OBDal.getInstance().save(factura);
            OBDal.getInstance().flush();
            msg.setType("Success");
            msg.setMessage("La Factura ha sido cancelada");
          } else {
            msg.setType("Error");
            msg.setMessage(mensage);
          }
         }
         catch (Exception e)
         {
	    msg.setType("Error");
            msg.setMessage("Error en la comunicación con el PAC. Contacte al equipo de soporte");
         }
        }

      }

      else {
	try
        {
	      ruta_response = cancelFact.cancelarFac(ruta, NumFac, RFC_Emisor, RFC_Receptor, uuidFactura,
		  refId, PasswordPAC, archivoPac, urlCancelar);
	      canceled = getValuefromXML(ruta_response, "canceled");
	      mensage = getValuefromXML(ruta_response, "message");
	      fechaCanc = getValuefromXML(ruta_response, "fecha");
	      if (canceled.equals("true")) {
		factura.setFetDocstatus("Cancelado");
		factura.setFetFechacancel(fechaCanc);
		OBDal.getInstance().save(factura);
		OBDal.getInstance().flush();
		msg.setType("Success");
		msg.setMessage("La Factura ha sido cancelada");
	      } else {
		msg.setType("Error");
		msg.setMessage(mensage);
	      }
	 }
	catch (Exception e)
         {
	    msg.setType("Error");
            msg.setMessage("Error en la comunicación con el PAC. Contacte al equipo de soporte");
         }
      }

      vars.setMessage(strTab, msg);

      OBContext.restorePreviousMode();

      printPageClosePopUp(response, vars, strWindowPath);
    }
  }

  String getValuefromXML(String f, String str) {
    int ch;
    String xml1;
    StringBuffer strContent = new StringBuffer("");
    FileInputStream fin = null;
    try {
      fin = new FileInputStream(f);
      while ((ch = fin.read()) != -1)
        strContent.append((char) ch);
      fin.close();
    } catch (Exception e) {
      return "";
    }
    xml1 = strContent.toString();
    try {
      String[] ret = xml1.split(str + "=\"");
      if (ret.length >= 2)
        return ret[1].substring(0, ret[1].indexOf("\""));
      else
        return "";
    } catch (Exception e) {
      return "";
    }
  }

}
