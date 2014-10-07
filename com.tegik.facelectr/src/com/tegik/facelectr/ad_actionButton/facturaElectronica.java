package com.tegik.facelectr.ad_actionButton;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import mx.bigdata.sat.cfdi.CFDv32;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante;
import mx.bigdata.sat.cfdi.v32.schema.ObjectFactory;
import mx.bigdata.sat.cfdi.v32.schema.TimbreFiscalDigital;
import mx.bigdata.sat.common.NamespacePrefixMapperImpl;
import mx.bigdata.sat.security.KeyLoader;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.JRFieldProviderDataSource;
import org.openbravo.erpCommon.utility.JRFormatFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.PrintJRData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.utils.FileUtility;
import org.openbravo.utils.Replace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import mx.bigdata.sat.cfdi.v32.schema.Comprobante.Complemento;

import org.openbravo.scheduling.ProcessBundle;

/**
 * Creando una factura electrónica para México.
 * 
 * @author Tegik
 */
public class facturaElectronica extends HttpSecureAppServlet {

  private static final Logger log = Logger.getLogger(facturaElectronica.class);

  private static final long serialVersionUID = 1L;

  private static String rutaAttach = "";
  private static String NumFac = "";
  private static String ruta = "";
  private static String Servidor = "";
  private static String Puerto = "";
  private static String Correo_servidor = "";
  private static String Contra_Servidor = "";
  private static String Correo_origen = "";
  private static String Correo_destino = "";
  private static String Correo_alternativo = "";
  private static String Asunto = "";
  private static String MensajeCorreo = "";
  private static String strTimbrar = "";
  private static String Statuscorreo = "OK";
  // private static String attachFolder =
  // OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("attach.path");
  private static String attachFolder = "";
  private static String Separador = "/";
  private static Boolean banderaSeguir = true;
  private static String timbradoTestStatus = "NE";
  private final JAXBContext context = createContext();

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  // main HTTP call handler
  synchronized public OBError facturar(HttpServletRequest request, HttpServletResponse response, ServletConfig srvConfig, VariablesSecureApp vars2, String invoiceId, ProcessBundle bundle) throws IOException, ServletException {
    
    VariablesSecureApp vars = null;
    
    if (request != null){
      vars = new VariablesSecureApp(request);
    }
    else
    {
      vars = vars2;
    }

    // ////log.info("dopost -- 1");

    if (1 == 1) {

      rutaAttach = "";
      NumFac = "";
      ruta = "";
      Servidor = "";
      Puerto = "";
      Correo_servidor = "";
      Contra_Servidor = "";
      Correo_origen = "";
      Correo_destino = "";
      Correo_alternativo = "";
      Asunto = "";
      MensajeCorreo = "";
      strTimbrar = "";
      Statuscorreo = "OK";
      Separador = System.getProperty("file.separator");
      banderaSeguir = true;
      timbradoTestStatus = "NE";
      // attachFolder = "/opt/OpenbravoERP-3.0/attachments";
      attachFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("attach.path");
      attachFolder = attachFolder + Separador.substring(0, 1);
      log.info("CSM> attachFolderInicial -- " + attachFolder);
      log.info("CSM> Separador -- " + Separador);

      // log.info("CSM> Separador -- " + Separador);

      // log.info("CSM> attachFolder -- " + attachFolder);

      // parse required Guest ID parameter to be processed
      String strInvoiceId = null;
      String strWindow = null;
      String strTab = null;
      
      if (vars != null)
      {
	strInvoiceId = vars.getStringParameter("inpcInvoiceId");
	strWindow = vars.getStringParameter("inpwindowId");
	strTab = vars.getStringParameter("inpTabId");
	
      }

      
      log.info("primero - strInvoiceId" + strInvoiceId);
      log.info("primero - invoiceId" + invoiceId);
      
      if (strInvoiceId == null){
	  strInvoiceId = invoiceId;
      }
      
      if (strInvoiceId.trim() == ""){
	  strInvoiceId = invoiceId;
      } 
      
      log.info("segundo - strInvoiceId" + strInvoiceId);
      // log.info("CSM //  strInvoiceId // " + strInvoiceId);
      Invoice facturaTest = OBDal.getInstance().get(Invoice.class, strInvoiceId);
      
      log.info("FacturaTest.getId()" + facturaTest.getId());

      // construct the reload path so that upon completion of the process
      // the main editing window is reloaded and the resulting message is
      // shown
      
      if (strWindow == null){
	  strWindow = "167";
      }
      
      if (strWindow.trim() == ""){
	  strWindow = "167";
      } 
      
      if (strTab == null){
	  strTab = "263";
      }
      
      if (strTab.trim() == ""){
	  strTab = "263";
      }
      
      String strWindowPath = Utility.getTabURL(this, strTab, "R");
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      // Busca el archivo de timbrado

      // log.info("dopost -- 3");
      OBError myMessage2 = new OBError();
      // myMessage2.setMessage("Aquí se debe de terminar el proceso");
      // myMessage2.setType("Error");
      // myMessage2.setTitle("Facturación");
      String rutaTimbradoTest = attachFolder + "318-" + strInvoiceId + Separador.substring(0, 1)
          + "Timbrado" + facturaTest.getDocumentNo() + ".xml";
      // log.info("CSM> rutaTimbradoTest -- " + rutaTimbradoTest);
      String rutaRequestTest = attachFolder + "318-" + strInvoiceId + Separador.substring(0, 1)
          + "requestTimbrado" + facturaTest.getDocumentNo() + ".xml";
      // log.info("CSM> rutaRequestTest -- " + rutaRequestTest);
      String rutaXMLTest = attachFolder + "318-" + strInvoiceId + Separador.substring(0, 1)
          + facturaTest.getDocumentNo() + ".xml";
      // log.info("CSM> rutaXMLTest -- " + rutaXMLTest);

      File timbradoTest = new File(rutaTimbradoTest);
      File requestTest = new File(rutaRequestTest);
      File XMLTest = new File(rutaXMLTest);

      if (!timbradoTest.exists()) {
        banderaSeguir = true;
        timbradoTestStatus = "NE";
        if (requestTest.exists()) {
          if (requestTest.delete()) {
            Boolean bool_requestTest = requestTest.delete();
            log.info("RESULTADO DEL REQUESTTEST.DELETE" + bool_requestTest.toString());
          }

        }

        if (XMLTest.exists()) {
          if (XMLTest.delete()) {
            Boolean bool_XMLTest = XMLTest.delete();
            log.info("RESULTADO DEL XMLTest.DELETE" + bool_XMLTest.toString());
          }
        }
      } else {
        FileInputStream fis = new FileInputStream(timbradoTest);
        int b = fis.read();
        if (b == -1) {
          banderaSeguir = false;
          timbradoTestStatus = "AV";
        } else {
          banderaSeguir = false;
          timbradoTestStatus = "AT";
        }
        fis.close();
      }

      if (timbradoTestStatus == "AV") {
        String banderaTestStatus = "N";

        if (timbradoTest.exists()) {
          if (timbradoTest.delete()) {
            banderaTestStatus = "Y";
          } else {
            banderaTestStatus = "N";
          }
        } else {
          banderaTestStatus = "Y";
        }

        if (banderaTestStatus == "Y") {
          if (requestTest.exists()) {
            if (requestTest.delete()) {
              banderaTestStatus = "Y";
            } else {
              banderaTestStatus = "N";
            }
          } else {
            banderaTestStatus = "Y";
          }
        }

        if (banderaTestStatus == "Y") {
          if (XMLTest.exists()) {
            if (XMLTest.delete()) {
              banderaTestStatus = "Y";
            } else {
              banderaTestStatus = "N";
            }
          } else {
            banderaTestStatus = "Y";
          }
        }

        if (banderaTestStatus == "Y") {
          banderaSeguir = true;
          // myMessage2.setMessage("Se eliminaron correctamente los archivos. Se procederá al timbrado");
          // myMessage2.setType("Success");
          // myMessage2.setTitle("Factura electrónica");
          // banderaSeguir = false;
        } else {
          myMessage2.setMessage("El archivo de timbrado se encuentra vacío. Contacte a soporte");
          myMessage2.setType("Error");
          myMessage2.setTitle("Error al leer archivo de timbrado");
          banderaSeguir = false;
        }
      }

      if (timbradoTestStatus == "AT") {
        facturaTest.setFetSellosat(getValuefromXML(timbradoTest, "selloSAT"));
        // log.info("CSM > SelloSat -- " + getValuefromXML(timbradoTest, "selloSAT"));
        // Guarda el selloCFD en un Campo de Openbravo
        facturaTest.setFetSellocfd(getValuefromXML(timbradoTest, "selloCFD"));
        // log.info("CSM > selloCFD -- " + getValuefromXML(timbradoTest, "selloCFD"));
        // Guarda la cadena Original del SAT en un Campo de Openbravo
        facturaTest.setFetCadenaoriginalSat(getValuefromXML(timbradoTest, "version")
            + getValuefromXML(timbradoTest, "UUID")
            + getValuefromXML(timbradoTest, "FechaTimbrado")
            + getValuefromXML(timbradoTest, "selloSAT")
            + getValuefromXML(timbradoTest, "noCertificadoSAT"));
        // log.info("CSM > CadenaOriginal -- " + getValuefromXML(timbradoTest, "version") +
        // getValuefromXML(timbradoTest, "UUID") + getValuefromXML(timbradoTest, "FechaTimbrado") +
        // getValuefromXML(timbradoTest, "selloSAT") + getValuefromXML(timbradoTest,
        // "noCertificadoSAT"));
        // Guarda la fecha de timbrado en base de datos
        facturaTest.setFetFechaTimbre(getValuefromXML(timbradoTest, "FechaTimbrado"));
        // log.info("CSM > FechaTimbrado -- " + getValuefromXML(timbradoTest, "FechaTimbrado"));
        // Guarda el Numero de Certificado en base de datos
        facturaTest.setFetCertificadoSat(getValuefromXML(timbradoTest, "noCertificadoSAT"));
        // log.info("CSM > noCertificadoSAT -- " + getValuefromXML(timbradoTest,
        // "noCertificadoSAT"));
        // Guarda el uuid o folio fiscal en base de datos
        facturaTest.setFetFoliofiscal(getValuefromXML(timbradoTest, "UUID"));
        // log.info("CSM > UUID -- " + getValuefromXML(timbradoTest, "UUID"));

        String rutaComprobanteTest = attachFolder + "318-" + strInvoiceId
            + Separador.substring(0, 1) + facturaTest.getDocumentNo() + ".xml";
        // log.info("CSM> rutaComprobanteTest -- " + rutaComprobanteTest);
        FileInputStream fisComprobante = new FileInputStream(new File(rutaComprobanteTest));
        try {
          Comprobante comprobanteTest = CFDv32.newComprobante(fisComprobante);
          facturaTest.setFetNumcertificado(comprobanteTest.getNoCertificado());
          // log.info("CSM > NumCertificado -- " + comprobanteTest.getNoCertificado());
          OBDal.getInstance().save(facturaTest); // Guarda el attachment
          OBDal.getInstance().flush();
          OBDal.getInstance().commitAndClose();
        } catch (Exception e) {
          myMessage2
              .setMessage("No se pudo obtener la información del comprobante. Contacte al equipo de soporte");
          myMessage2.setType("Error");
          myMessage2.setTitle("Error al leear archivo de comprobante");
        }

        // Guarda el NoCertificado de la cadena original en base de datos
        fisComprobante.close();

        myMessage2.setMessage("");
        myMessage2.setType("Success");
        myMessage2.setTitle("Se creó con éxito la factura electrónica");
      }

      if (timbradoTestStatus == "NE") {
        // myMessage2.setMessage("Se creará la nueva factura electrónica");
        // myMessage2.setType("Success");
        // myMessage2.setTitle("Cambiar bool");
        facturaTest = null;
        banderaSeguir = true;
      }

      if (!banderaSeguir) {
        /*
         * vars.setMessage(strTab, myMessage2); printPageClosePopUp(response, vars, strWindowPath);
         */
        return myMessage2;
        /*
         * System.runFinalization(); System.gc();
         */
      }
      // run the calculation
      if (banderaSeguir) {
        OBError myMessage = creaFacturaElectronica(vars, strInvoiceId, strTab);
        // log.info(myMessage.getType());
        // log.info(myMessage.getMessage());

        // log.info("dopost -- 4");

        // Guardar el el PDF en los attachments
        if (strTimbrar.equals("OK") && myMessage.getType() == "Success") {
          // log.info("dopost -- 5");
          HashMap<String, Object> parameters = new HashMap<String, Object>();
          parameters.put("DOCUMENT_ID", strInvoiceId);
          // String strReportName =
          // "@basedesign@/com/tegik/facelectr/ad_actionButton/reports/EM_FET_Plantilla_Factura_VentaJR.jrxml";
          // log.info("dopost -- 6");
          // String rutaPDF = renderJRPiso(vars, strReportName, "pdf", parameters, null, null);
          // log.info("dopost -- 7");
          // Termina Guardar el el PDF en los attachments
          // Enviar correo Electronico con los attachments
          // log.info("CSM STRTIMBRAR>" + strTimbrar);
          if (strTimbrar == "OK") {
            // log.info("dopost -- 8");

            try {
              OBContext.setAdminMode(true);
              // OBError myMessagew = new OBError();
              // Invoice factura = OBDal.getInstance().get(Invoice.class,strInvoiceId); IGUALES
              log.info("1");
              Invoice facturaParaCorreo = OBDal.getInstance().get(Invoice.class, strInvoiceId);
              log.info("2");
              enviadorCorreos enviador = new enviadorCorreos(); // clase existente en el package
              log.info("3");
              if (srvConfig != null) 
              {
		log.info("Si hay configuración");
		enviador.init(srvConfig);
	      }
	      else
	      {
		log.info("Se salta la configuración");
	      }
              log.info("4");
              String respuestaEnvio = enviador.solicitarEnvio(vars, facturaParaCorreo, "Y", "Y", bundle);
              log.info("CSM>CORREOS -- " + respuestaEnvio);
              if (respuestaEnvio == "OK") {
                // se creo la factura y se envio correctamente al correo electronico.
                myMessage.setType("Success");
                myMessage.setTitle("Se ha creado existosamente la Factura Electrónica");
                OBContext.restorePreviousMode();

              } else {
                // se creo la factura pero no se envio al correo electronico.
                myMessage.setType("Success");
                myMessage
                    .setMessage("Se ha creado existosamente la Factura Electrónica: No se pudo enviar el correo el electrónico");
                myMessage.setTitle("Se ha creado existosamente la Factura Electrónica");
                OBContext.restorePreviousMode();
              }
            } catch (Exception e) {
              // se creo la factura, pero hubo un error en ella.
              // log.info("CSM>CORREOS -- " + e.toString());
              StringWriter w = new StringWriter();
              e.printStackTrace(new PrintWriter(w));
              String errorfactura = w.toString();
              log.info("EXCEPTION" + e.getMessage());
              log.info("CSM>CORREOS -- " + errorfactura);
              myMessage.setType("Success");
              OBContext.restorePreviousMode();
              myMessage.setTitle("Se ha creado existosamente la Factura Electrónica");
              myMessage
                  .setMessage("Se ha creado existosamente la Factura Electrónica: No se pudo enviar el correo el electrónico");
            }

          } else {
            // myMessage.setMessage("Se ha creado existosamente la Factura Electrónica: No se pudo enviar el correo el electrónico");
            // myMessage.setType("Success");
            myMessage
                .setMessage("Error en el timbrado. No se ha creado la Factura Electrónica: No se pudo enviar el correo el electrónico");
            myMessage.setType("Error");

            myMessage.setTitle("Titulo");
          }
          // Termina Enviar correo Electronico con los attachments
          // log.info("dopost -- 8");
          // log.info("Se creo con éxito la factura: "+NumFac);
        }

        // ////////////////// MENSAJE PARA FACTURAR JCI PRIMER DIA ---
        // BORRAR/////////////////////////////
        /*
         * myMessage.setMessage("Se ha creado la Factura Electrónica");
         * myMessage.setType("Success"); myMessage.setTitle("Titulo");
         */
        // ///////////////////////////////////////////////////////////////

        // set resulting message and reload main editing window
        /*
         * vars.setMessage(strTab, myMessage); printPageClosePopUp(response, vars, strWindowPath);
         */
        return myMessage;
        /*
         * System.runFinalization(); System.gc();
         */
      }

    } else {
      // pageErrorPopUp(response);
      OBError messageNull = new OBError();
      return messageNull;
      /*
       * System.runFinalization(); System.gc();
       */
    }

    OBError messageNull = new OBError();
    return messageNull;
  }

  synchronized OBError creaFacturaElectronica(VariablesSecureApp vars, String strInvoiceId,
      String strTab) throws IOException, ServletException {
    try {

      // log.info("cfe -- 1");
      OBError myMessage = new OBError();
      // Cargas la factura del DAL de Openbravo
      OBContext.setAdminMode(true);
      Invoice factura = OBDal.getInstance().get(Invoice.class, strInvoiceId);
      Tab tabFactura = OBDal.getInstance().get(Tab.class, strTab);
      myMessage.setMessage("Mi mensaje de prueba");
      myMessage.setType("Error");
      myMessage.setTitle("Error en la creación de la factura electrónica");
      // log.info("cfe -- 2");
      String tipoDoc = "";
      // log.info("CSM // tipoDoc // " + tipoDoc);
      String nombreCer = "", nombreKey = "", nombrePfx = ""; // Inicializacion archivo .cer .key
                                                             // .pfx
      String pathCer = "", pathKey = "", pathPfx = ""; // Inicializacion archivo .cer .key .pfx
      // log.info("CSM // nombreCer // " + nombreCer);
      // log.info("CSM // nombreKey // " + nombreKey);
      // log.info("CSM // nombrePfx // " + nombrePfx);
      String archivoPac = ""; // Inicializacion ruta de archivo .pfx
      // log.info("CSM // archivoPac // " + archivoPac);
      // log.info("cfe -- 3");
      String PasswordFiel = factura.getOrganization().getFetPassfiel(); // password de fiel
      // log.info("CSM // PasswordFiel // " + PasswordFiel);
      String PasswordPAC = factura.getOrganization().getFetPasspac(); // password de pac
      // log.info("CSM // PasswordPAC // " + PasswordPAC);
      File archivo = new File(attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId
          + Separador.substring(0, 1) + factura.getDocumentNo() + ".xml"); // Liga del archivo en
                                                                           // tipo File
      // log.info("CSM> archivo -- " + attachFolder + tabFactura.getTable().getId() + "-" +
      // strInvoiceId + Separador.substring(0,1) + factura.getDocumentNo() + ".xml");
      // log.info("cfe -- 4");
      File path = new File(attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId
          + Separador.substring(0, 1)); // Ruta de la factura en tipo File
      // log.info("CSM> path -- " + attachFolder + tabFactura.getTable().getId() + "-" +
      // strInvoiceId + Separador.substring(0,1));
      // Checa si el directorio existe antes de guardar el comprobante.
      boolean exists = path.exists();
      if (!exists) {
        // Si no existe, creamos el directorio.
        path.mkdirs();
      }

      String archivobase = attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId
          + Separador.substring(0, 1) + factura.getDocumentNo() + ".xml"; // Liga del archivo en
                                                                          // tipo String
      // log.info("CSM> archivobase -- " + archivobase);
      ruta = attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId
          + Separador.substring(0, 1); // Ruta de la factura en tipo String
      // log.info("CSM> ruta -- " + ruta);
      // log.info("cfe -- 5");
      NumFac = factura.getDocumentNo();
      // log.info("CSM // NumFac // " + NumFac);
      String Documento = factura.getTransactionDocument().getDocumentCategory(); // Tipo de
                                                                                 // documento de
                                                                                 // openbravo
      // log.info("CSM // Documento // " + Documento);
      rutaAttach = tabFactura.getTable().getId() + "-" + strInvoiceId;
      // log.info("cfe -- 6");
      File archivoTimbrado = new File(attachFolder + tabFactura.getTable().getId() + "-"
          + strInvoiceId + Separador.substring(0, 1) + "Timbrado" + factura.getDocumentNo()
          + ".xml"); // Liga del archivo en tipo File
      // log.info("CSM> archivoTimbrado -- " + attachFolder + tabFactura.getTable().getId() + "-" +
      // strInvoiceId + Separador.substring(0,1)+ "Timbrado" + factura.getDocumentNo() + ".xml");
      File archivoResquet = new File(attachFolder + tabFactura.getTable().getId() + "-"
          + strInvoiceId + Separador.substring(0, 1) + "requestTimbrado" + factura.getDocumentNo()
          + ".xml"); // Liga del archivo en tipo File
      // log.info("CSM> archivoResquet -- " + attachFolder + tabFactura.getTable().getId() + "-" +
      // strInvoiceId + Separador.substring(0,1)+ "requestTimbrado" + factura.getDocumentNo() +
      // ".xml");
      // log.info("cfe -- 7");

      // log.info("cfe -- 8");
      Statuscorreo = "OK";
      if (Statuscorreo == "OK") {
        // Verifica que la año de facturacion sea igual que el año actual
        Date Fecha_factura = factura.getInvoiceDate();
        int Ano_factura = Fecha_factura.getYear() + 1900;
        Date Fecha_Creacion = factura.getCreationDate();
        int Ano_creacion = Fecha_Creacion.getYear() + 1900;
        // log.info("cfe -- 9");

        if (Ano_factura == Ano_creacion) {

          // Obtiene el RFC del emisor y del receptor
          // log.info("cfe -- 10");
          String RFC_Emisor = factura.getOrganization().getOrganizationInformationList().get(0)
              .getTaxID();
          // log.info("CSM // RFC_Emisor // " + RFC_Emisor);
          String RFC_Receptor = factura.getBusinessPartner().getTaxID();
          // log.info("CSM // RFC_Receptor // " + RFC_Receptor);
          // Crea el comprobante y el cfdi a partir de la factura que estamos utilizando.
          // log.info("cfe -- 11");
          // ARI Factura. ARC Nota de credito
          // Obtiene los archivos para utilizar desde los attachmentes.

          final OBCriteria<Attachment> attachmentList = OBDal.getInstance().createCriteria(
              Attachment.class);
          attachmentList.add(Expression.eq(Attachment.PROPERTY_TABLE,
              OBDal.getInstance().get(Table.class, "155")));
          attachmentList.add(Expression.eq(Attachment.PROPERTY_RECORD, factura.getOrganization()
              .getId()));

          // log.info("CSM > Id de la organización de la factura -- " +
          // factura.getOrganization().getId());

          // log.info("CSM > Lista de attachments tamaño-- " + attachmentList.list().size());

          // log.info("cfe -- 12");
          for (Attachment attachmentUd : attachmentList.list()) {
            // log.info("CSM > Entro a la lista -- ");
            if (attachmentUd.getName().indexOf(".cer") != -1) {
              nombreCer = attachmentUd.getName();
              pathCer = attachmentUd.getPath();
              if (pathCer == "" || pathCer == null) {
                pathCer = "155" + "-" + factura.getOrganization().getId();
              }
            }

            if (attachmentUd.getName().indexOf(".key") != -1) {
              nombreKey = attachmentUd.getName();
              pathKey = attachmentUd.getPath();
              if (pathKey == "" || pathKey == null) {
                pathKey = "155" + "-" + factura.getOrganization().getId();
              }
            }

            if (attachmentUd.getName().indexOf(".pfx") != -1) {
              nombrePfx = attachmentUd.getName();
              pathPfx = attachmentUd.getPath();
              if (pathPfx == "" || pathPfx == null) {
                pathPfx = "155" + "-" + factura.getOrganization().getId();
              }
            }

          }

          // log.info("CSM > nombreCer -- " + nombreCer);
          // log.info("CSM > nombreKey -- " + nombreKey);
          // log.info("CSM > nombrePfx -- " + nombrePfx);

          // log.info("cfe -- 13");
          // Se cierra Obtiene los archivos para utilizar desde los attachmentes.

          // Busca el archivo .cer y .key
          // File archivoCer = new File(attachFolder + "155" + "-" +
          // factura.getOrganization().getId() + Separador.substring(0,1) + nombreCer);
          File archivoCer = new File(attachFolder + pathCer + Separador.substring(0, 1) + nombreCer);
          // log.info("CSM > archivoCer -- " + attachFolder + "155" + "-" +
          // factura.getOrganization().getId() + Separador.substring(0,1) + nombreCer);

          // log.info("cfe -- 14");

          if (!archivoCer.exists() || nombreCer == "") {
            myMessage.setMessage("No se encontró el archivo .cer");
            myMessage.setType("Error");
            myMessage.setTitle("Error en la creación de la factura electrónica");
          }

          // log.info("cfe -- 15");

          // File archivoKey = new File(attachFolder + "155" + "-" +
          // factura.getOrganization().getId() + Separador.substring(0,1) + nombreKey);
          File archivoKey = new File(attachFolder + pathKey + Separador.substring(0, 1) + nombreKey);
          // log.info("CSM > archivoKey -- " + attachFolder + "155" + "-" +
          // factura.getOrganization().getId() + Separador.substring(0,1) + nombreKey);

          if (!archivoKey.exists() || nombreKey == "") {
            myMessage.setMessage("No se encontró el archivo .key");
            myMessage.setType("Error");
            myMessage.setTitle("Error en la creación de la factura electrónica");
          }

          // log.info("cfe -- 16");
          // Se cierra Busca el archivo .cer y .key

          // Busca el archivo .pfx para timbrar
          if (nombrePfx != "") {
            // archivoPac = attachFolder + "155" + "-" + factura.getOrganization().getId() +
            // Separador.substring(0,1) + nombrePfx;
            archivoPac = attachFolder + pathPfx + Separador.substring(0, 1) + nombrePfx;
            // log.info("CSM > archivoPac -- " + attachFolder + "155" + "-" +
            // factura.getOrganization().getId() + Separador.substring(0,1) + nombrePfx);
          } else {
            myMessage.setMessage("No se encontró el archivo .pfx para Timbrar");
            myMessage.setType("Error");
            myMessage.setTitle("Error en la creación de la factura electrónica");
          }
          // Se cierra Busca el archivo .pfx para timbrar

          // log.info("cfe -- 17");

          // Verifica si es una factura o una nota de credito
          if (Documento.equals("ARC"))
            tipoDoc = "N";
          else
            tipoDoc = "F";
          // log.info("cfe -- 18");

          // log.info("CSM // tipoDoc // " + tipoDoc);

          // termina verifica si es una factura o una nota de credito
          creadorFacturas comprobante = new creadorFacturas(strInvoiceId, tipoDoc); // Crea la
                                                                                    // Cadena
                                                                                    // Original de
                                                                                    // la Factura
          Comprobante comp = null;
          if (comprobante.getHayError()) {
            log.info("Error en la creación del comprobante");
            myMessage.setMessage(comprobante.getMensajeError());
            myMessage.setType("Error");
            myMessage.setTitle("Error en la creación de la factura electrónica");
            return myMessage;
          } else {
            comp = comprobante.getComprobante();
          }

          log.info("FACTURAELECTRONICA.JAVA");

          CFDv32 cfd = null;

          String paqueteJavaAddenda = null;

          if (addendasInstaladas()) {
            log.info("El módulo de addendas está instalado");

            Class claseAddenda = Class.forName("com.tegik.addenda.module.proc.manejadorAddenda");
            Constructor<Object> ctor = claseAddenda.getDeclaredConstructor(String.class,
                Boolean.class);
            Object instance = ctor.newInstance(strInvoiceId, false);
            paqueteJavaAddenda = (String) claseAddenda.getMethod("getPaqueteJavaAddenda").invoke(
                instance);

            log.info("paqueteJava Objeto ADDENDA " + paqueteJavaAddenda);

            if (paqueteJavaAddenda != null) {
              cfd = new CFDv32(comp, paqueteJavaAddenda);
            } else {
              cfd = new CFDv32(comp);
            }

          } else {
            log.info("El módulo de addendas NO está instalado");
            cfd = new CFDv32(comp);
          }

          log.info("CFD");

          // log.info("cfe -- 19");

          PrivateKey key = KeyLoader.loadPKCS8PrivateKey(new FileInputStream(archivoKey),
              PasswordFiel); // Carga la llave privada
          X509Certificate cert = KeyLoader.loadX509Certificate(new FileInputStream(archivoCer)); // Carga
                                                                                                 // el
                                                                                                 // certificado
          Comprobante sellado = cfd.sellarComprobante(key, cert); // Sella la cadena original

          log.info("entro1");
          cfd.validar(); // Valida la factura
          log.info("entro2");

          cfd.verificar(); // verifica la factura
          log.info("entro3");

          cfd.guardar(System.out); // guarda la factura
          log.info("entro4");

          // log.info("cfe -- 20");

          // log.info("cfe -- 21");
          // Guardar el comprobante y adjuntar el archivo al registro de Openbravo
          factura.setFetCadenaoriginal(cfd.getCadenaOriginal()); // Pone la cadena original en el
                                                                 // campo descripción de la factura.
          OBDal.getInstance().save(factura); // Guarda los nuevos datos de la factura
          cfd.guardar(new FileOutputStream(archivo)); // Guarda el archivo
          // log.info("cfe -- 22");
          // Convertir el xml a un string para enviarlo en el SOAP
          String codigo = convertXMLFileToString(ruta + NumFac + ".xml");
          // log.info("CSM > codigo > " + codigo);
          String FacturaString = Base64Coder.encodeString(codigo);
          // log.info("CSM > FacturaString > " + FacturaString);
          // log.info("cfe -- 23");
          NumFac = cambiarCaracteres(NumFac);
          RFC_Emisor = cambiarCaracteres(RFC_Emisor);
          RFC_Receptor = cambiarCaracteres(RFC_Receptor);
          // log.info("CSM > NumFac > " + NumFac);
          // log.info("CSM > RFC_Emisor > " + RFC_Emisor);
          // log.info("CSM > RFC_Receptor > " + RFC_Receptor);
          // Empieza Crear el encapsualado SOAP para la conexion a WS del PAC
          OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(ruta
              + "requestTimbrado" + NumFac + ".xml", true), "UTF-8");
          BufferedWriter encabezado = new BufferedWriter(writer);
          encabezado.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
          encabezado
              .write("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tim=\"http://www.buzonfiscal.com/ns/xsd/bf/TimbradoCFD\" xmlns:req=\"http://www.buzonfiscal.com/ns/xsd/bf/RequestTimbraCFDI\"> \n");
          encabezado.write("   <soapenv:Header/>\n");
          encabezado.write("   <soapenv:Body>\n");
          encabezado.write("    <tim:RequestTimbradoCFD req:RefID=\"" + factura.getId() + "\"> \n");
          encabezado.write("       <req:Documento Archivo=\"" + FacturaString
              + "\" NombreArchivo=\"" + NumFac + ".xml\" Tipo=\"XML\" Version=\"32\"/>  \n");
          encabezado.write("      <req:InfoBasica RfcEmisor=\"" + RFC_Emisor + "\" RfcReceptor=\""
              + RFC_Receptor + "\"/> \n");
          encabezado.write("    </tim:RequestTimbradoCFD>\n");
          encabezado.write("   </soapenv:Body>\n");
          encabezado.write("</soapenv:Envelope>\n");
          encabezado.close();
          // Termina crear el encapsualado SOAP
          // log.info("CSM // RutaRequestTimbrado // " + ruta+"requestTimbrado"+NumFac+".xml");
          // log.info("CSM // ArchivoRequestTimbrado // " +
          // fileToString(ruta+"requestTimbrado"+NumFac+".xml"));
          // log.info("cfe -- 24");
          // Se sube el archiv que se creo
          String strSubirArchivo = subirArchivo(vars, strTab, factura, tabFactura);
          // log.info("CSM // strSubirArchivo // " + strSubirArchivo);
          // log.info("cfe -- 25");
          // Llama a la funcion timbrar para hacer la conexion al PAC
          // strTimbrar = timbrar("https://demotf.buzonfiscal.com/timbrado",ruta, NumFac,
          // PasswordPAC, archivoPac);
          // strTimbrar = timbrar("https://tf.buzonfiscal.com/timbrado",ruta, NumFac, PasswordPAC,
          // archivoPac);
          log.info("CSM // factura.getClient().getFetUrltimbrado() // "
              + factura.getClient().getFetUrltimbrado());
          strTimbrar = timbrar(factura.getClient().getFetUrltimbrado(), ruta, NumFac, PasswordPAC,
              archivoPac);
          // strTimbrar = "OK";
          String mensaje = strSubirArchivo + " -- " + strTimbrar;
          // log.info("CSM // strSubirArchivo // " + strSubirArchivo);
          // log.info("cfe -- 26");
          if (strTimbrar == "OK") {

            // Se manda a llamar a la funcion extraeTimbrado para que extraiga del archivo Timbrado
            // los attributos
            // extraeTimbrado.timbra(ruta,NumFac);

            extraeTimbrado timbrado = new extraeTimbrado(new File(attachFolder + "318-"
                + strInvoiceId + Separador.substring(0, 1) + "Timbrado" + factura.getDocumentNo()
                + ".xml"));

            ObjectFactory of = new ObjectFactory();

            TimbreFiscalDigital timbre = of.createTimbreFiscalDigital();

            log.info(timbrado.get_Fechatimbrado());
            log.info(timbrado.get_noCertificadoSAT());
            log.info(timbrado.get_selloCFD());
            log.info(timbrado.get_selloSAT());
            log.info(timbrado.get_uuid());
            log.info(timbrado.get_version());

            java.util.Date dateTimbrado = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .parse(timbrado.get_Fechatimbrado());
            timbre.setFechaTimbrado(dateTimbrado);
            timbre.setNoCertificadoSAT(timbrado.get_noCertificadoSAT());
            timbre.setSelloCFD(timbrado.get_selloCFD());
            timbre.setSelloSAT(timbrado.get_selloSAT());
            timbre.setUUID(timbrado.get_uuid());
            timbre.setVersion(timbrado.get_version());

            Complemento complemento = of.createComprobanteComplemento();
	      complemento.getAny().add(timbre);
	      //cfd.getComprobante().getComplementoGetAny().add(timbre);
	      sellado.setComplemento(complemento);
	      CFDv32 cfdTimbrado = null;
	      if  (paqueteJavaAddenda == null)
	      {
		    cfdTimbrado = new CFDv32(sellado);
	      }
	      else
	      {
		    cfdTimbrado = new CFDv32(sellado, paqueteJavaAddenda);
	      }
	      
	      //Comprobante sellado2 = cfdTimbrado.sellarComprobante(key, cert);
	      cfdTimbrado.validar();
	      cfdTimbrado.verificar();
	      //archivo.delete();
	      //cfdTimbrado.guardar(new FileOutputStream(new File(attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId + Separador.substring(0,1) + "TIMBRADA-" + factura.getDocumentNo() + ".xml")));
	      cfdTimbrado.guardar(new FileOutputStream(new File(attachFolder + tabFactura.getTable().getId() + "-" + strInvoiceId + Separador.substring(0,1) + factura.getDocumentNo() + ".xml")));
	      
	      String facturaStringCarlos = convertXMLFileToString(ruta + NumFac + ".xml");
	      
	      log4j.info("INFO PARA EL COMPLEMENTO");
	      log4j.info("INFO PARA EL COMPLEMENTO -- facturaStringCarlos -- " + facturaStringCarlos);
	      log4j.info("INFO PARA EL COMPLEMENTO -- timbrado.get_Fechatimbrado() -- " + timbrado.get_Fechatimbrado());
	      log4j.info("INFO PARA EL COMPLEMENTO -- timbrado.get_noCertificadoSAT() -- " + timbrado.get_noCertificadoSAT());
	      log4j.info("INFO PARA EL COMPLEMENTO -- timbrado.get_selloCFD() -- " + timbrado.get_selloCFD());
	      log4j.info("INFO PARA EL COMPLEMENTO -- timbrado.get_selloSAT() -- " + timbrado.get_selloSAT());
	      log4j.info("INFO PARA EL COMPLEMENTO -- timbrado.get_uuid() -- " + timbrado.get_uuid());
	      log4j.info("INFO PARA EL COMPLEMENTO -- timbrado.get_version() -- " + timbrado.get_version());
	      
	      String v_headerXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	      log.info("v_headerXML: " + v_headerXML);
	      String v_antesNamespace = facturaStringCarlos.substring(facturaStringCarlos.indexOf("<cfdi:Comprobante"), facturaStringCarlos.indexOf("<tfd:TimbreFiscalDigital") + "<tfd:TimbreFiscalDigital".length());
	      log.info("v_antesNamespace: " + v_antesNamespace);
	      String v_namespace = " xmlns:tfd=\"http://www.sat.gob.mx/TimbreFiscalDigital\" xsi:schemaLocation=\"http://www.sat.gob.mx/TimbreFiscalDigital http://www.sat.gob.mx/TimbreFiscalDigital/TimbreFiscalDigital.xsd\" ";
	      log.info("v_namespace: " + v_namespace);
	      
	      String v_fecha = "FechaTimbrado=\"" + timbrado.get_Fechatimbrado() + "\"";
	      log.info("v_fecha: " + v_fecha);
	      String v_certificadosat = "noCertificadoSAT=\"" + timbrado.get_noCertificadoSAT() + "\"";
	      log.info("v_certificadosat: " + v_certificadosat);
	      String v_sellocfd = "selloCFD=\"" + timbrado.get_selloCFD() + "\"";
	      log.info("v_sellocfd: " + v_sellocfd);
	      String v_sellosat = "selloSAT=\"" + timbrado.get_selloSAT() + "\"";
	      log.info("v_sellosat: " + v_sellosat);
	      String v_uuid = "UUID=\"" + timbrado.get_uuid() + "\"";
	      log.info("v_uuid: " + v_uuid);
	      String v_version = "version=\"" + timbrado.get_version() + "\"";
	      log.info("v_version: " + v_version);
	      
	      String v_timbrado = v_version + " " + v_uuid + " " + v_fecha + " " + v_sellocfd + " " + v_certificadosat + " " + v_sellosat + "/>";
	      log.info("v_timbrado: " + v_timbrado);
	      
	      String v_despuesNamespace = facturaStringCarlos.substring(facturaStringCarlos.indexOf("</cfdi:Complemento>"), facturaStringCarlos.length());
	      log.info("v_despuesNamespace: " + v_despuesNamespace);
	      
	      String xmlFinal = v_headerXML + v_antesNamespace + v_namespace + v_timbrado + "    " + v_despuesNamespace;
	      log.info("xmlFinal: " + xmlFinal);
	    
	      /*String xmlFinal = SqlComplemento.devuelveComplemento(this, facturaStringCarlos, 
								      timbrado.get_Fechatimbrado(),
								      timbrado.get_noCertificadoSAT(),
								      timbrado.get_selloCFD(),
								      timbrado.get_selloSAT(),
								      timbrado.get_uuid(),
								      timbrado.get_version());*/
	    log.info("CSM > CSALINAS > xmlFinal // " + xmlFinal);
	    
            /*String antesComplemento = facturaStringCarlos.substring(0,
                facturaStringCarlos.indexOf("<cfdi:Complemento/>"));
                
            String despuesComplemento = facturaStringCarlos.substring(
                facturaStringCarlos.indexOf("<cfdi:Complemento/>") + 19,
                facturaStringCarlos.length());
                
            String complementoStringCarlos = "<cfdi:Complemento>\n" + "        "
                + nodeToString(tfdMarshalled) + "\n    </cfdi:Complemento>";
                
            log.info("CSM > CSALINAS > complementoStringCarlos // " + complementoStringCarlos);
            
            //String resultadoXMLFinal = antesComplemento + complementoXSQL + despuesComplemento;*/

            File renombrarXML = new File(ruta + NumFac + ".xml");
            File renombrarAXML = new File(ruta + NumFac + "-respaldo.xml");
            renombrarXML.renameTo(renombrarAXML);
            
            File borrarXML = new File(ruta + NumFac + ".xml");
            borrarXML.delete();

            OutputStreamWriter writerTimbrado = new OutputStreamWriter(new FileOutputStream(ruta
                + NumFac + ".xml", true), "UTF-8");
            BufferedWriter bufferTimbrado = new BufferedWriter(writerTimbrado);

            bufferTimbrado.write(xmlFinal);
            bufferTimbrado.close();

            // Se manda a llamar a la funcion extraeCER para que extraiga del archivo Original los
            // attributos
            extraeCER.extrae(ruta, NumFac);
            // log.info("cfe -- 27");
            String rutaArchivoTimbrado = attachFolder + "318-" + strInvoiceId
                + Separador.substring(0, 1) + "Timbrado" + factura.getDocumentNo() + ".xml";
            File archivoTimbradoNuevo = new File(rutaArchivoTimbrado);
            // Guarda el selloSAT en un Campo de Openbravo
            factura.setFetSellosat(getValuefromXML(archivoTimbradoNuevo, "selloSAT"));
            // Guarda el selloCFD en un Campo de Openbravo
            factura.setFetSellocfd(getValuefromXML(archivoTimbradoNuevo, "selloCFD"));
            // Guarda la cadena Original del SAT en un Campo de Openbravo
            factura.setFetCadenaoriginalSat(getValuefromXML(archivoTimbradoNuevo, "version")
                + getValuefromXML(archivoTimbradoNuevo, "UUID")
                + getValuefromXML(archivoTimbradoNuevo, "FechaTimbrado")
                + getValuefromXML(archivoTimbradoNuevo, "selloSAT")
                + getValuefromXML(archivoTimbradoNuevo, "noCertificadoSAT"));
            // Guarda la fecha de timbrado en base de datos
            factura.setFetFechaTimbre(getValuefromXML(archivoTimbradoNuevo, "FechaTimbrado"));
            // Guarda el Numero de Certificado en base de datos
            factura.setFetCertificadoSat(getValuefromXML(archivoTimbradoNuevo, "noCertificadoSAT"));
            // Guarda el uuid o folio fiscal en base de datos
            factura.setFetFoliofiscal(getValuefromXML(archivoTimbradoNuevo, "UUID"));
            // Guarda el NoCertificado de la cadena original en base de datos
            String rutaComprobanteNueva = attachFolder + "318-" + strInvoiceId
                + Separador.substring(0, 1) + factura.getDocumentNo() + ".xml";
            // log.info("CSM> rutaComprobanteNueva -- " + rutaComprobanteNueva);
            FileInputStream fisComprobanteNuevo = new FileInputStream(
                new File(rutaComprobanteNueva));

            Comprobante nuevoComprobanteTest = CFDv32.newComprobante(fisComprobanteNuevo);
            factura.setFetNumcertificado(nuevoComprobanteTest.getNoCertificado());
            // log.info("CSM > NumCertificado -- " + nuevoComprobanteTest.getNoCertificado());

            // log.info("cfe -- 28");
            // Guarda la cantidad total en letra en base de datos
            double cantidadnumero = Double.parseDouble((factura.getGrandTotalAmount()).toString());
            String nombreMoneda = factura.getCurrency().getFetNombre();
            String isoMoneda = factura.getCurrency().getFetIso();
            // Pone la factura en status de facturada.
            factura.setFetDocstatus("Facturado");
            // log.info("JCBM -- cantidadnumero= "+cantidadnumero+" -- nombreMoneda= "+nombreMoneda+" -- isoMoneda= "+isoMoneda);
            double cantnum = Math.abs(cantidadnumero);
            // String cantidadletra = convertir.Numeroaletra(cantidadnumero,nombreMoneda,isoMoneda);
            String cantidadletra = convertir.Numeroaletra(cantnum, nombreMoneda, isoMoneda);
            if (cantidadnumero < 0) {
              cantidadletra = "MENOS " + cantidadletra;
              factura.setFetCantidadenletras(cantidadletra);
            } else {
              factura.setFetCantidadenletras(cantidadletra);
            }
            /*
             * try { String cantidadletra = convertir.Numeroaletra(cantidadnumero, nombreMoneda,
             * isoMoneda); factura.setFetCantidadenletras(cantidadletra); } catch (Exception e) {
             * String cantidadletra1 = "ERROR al obtener información";
             * factura.setFetCantidadenletras(cantidadletra1); }
             */

            // factura.setFetCantidadenletras(cantidadletra);

          } else {
            // factura.setFetDocstatus("Facturado");
          }

          OBContext.restorePreviousMode();

          // log.info("CSM -- strSubirArchivo -- " + strSubirArchivo);
          // log.info("CSM -- strTimbrar -- " + strTimbrar);

          // Mensajes de suceso o error.
          if (strSubirArchivo.equals("OK") && strTimbrar.equals("OK")) {
            // myMessage.setMessage("TEGIK-PRUEBA: La factura " + factura.getDocumentNo() +
            // " fue creada exitosamente");
            myMessage.setMessage("Se ha creado existosamente la Factura Electrónica");
            myMessage.setType("Success");
            myMessage.setTitle("Titulo");
            OBDal.getInstance().commitAndClose();
            return myMessage;
          } else {
            if (strSubirArchivo.equals("OK"))
              myMessage.setMessage("Error timbrado: " + strTimbrar);
            else if (strTimbrar.equals("OK"))
              myMessage.setMessage("Error Subir archivo: " + strSubirArchivo);
            else
              myMessage.setMessage(mensaje);
            myMessage.setType("Error");
            myMessage.setTitle("Error en la creación de la factura electrónica: ");
            OBDal.getInstance().commitAndClose();
            return myMessage;
          }
        }

        else {
          myMessage
              .setMessage("Error en la creación de la factura electrónica: La fecha de la factura no es válida");
          myMessage.setType("Error");
          myMessage.setTitle("Titulo");
          OBDal.getInstance().commitAndClose();
          return myMessage;
        }

      } else {
        myMessage
            .setMessage("Error en la creación de la factura electrónica: El correo electronico no es válido");
        myMessage.setType("Error");
        myMessage.setTitle("Titulo");
        OBDal.getInstance().commitAndClose();
        return myMessage;
      }

    } catch (Exception e) {

      StringWriter w = new StringWriter();
      e.printStackTrace(new PrintWriter(w));
      String errorfactura = w.toString();
      OBError myMessage = new OBError();
      if (errorfactura.indexOf("rfc") != -1 && errorfactura.indexOf("Receptor") != -1)
        myMessage.setMessage("El RFC del Receptor no es válido");
      else if (errorfactura.indexOf("t_RFC") != -1)
        myMessage.setMessage("El RFC no es válido");
      else if (errorfactura.indexOf("BadPaddingException") != -1
          && errorfactura.indexOf("PKCS8Key") != -1)
        myMessage.setMessage("La contraseña CSD no es válida");
      else if (errorfactura.indexOf("Traslados") != -1
          && errorfactura.indexOf("not complete") != -1)
        myMessage.setMessage("El impuesto no es válido");
      else if (errorfactura
          .indexOf("is not facet-valid with respect to enumeration '[ISR, IVA]'. It must be a value from the enumeration") > -1)
        myMessage.setMessage("Error en el impuesto, revisar la configuración del impuesto");
      else
        myMessage.setMessage(w.toString());
      myMessage.setType("Error");
      myMessage.setTitle("Error en la creación de la factura electrónica");
      return myMessage;

    }

  }

  synchronized String subirArchivo(VariablesSecureApp vars, String strTab, Invoice factura,
      Tab tabFactura) throws IOException, ServletException {
    try {
      OBContext.setAdminMode(true); // Para poder crear el Attachment

      final Attachment archivoDAL = OBProvider.getInstance().get(Attachment.class);
      // Se agregan las propiedades del attachment
      archivoDAL.setClient(factura.getClient());
      archivoDAL.setOrganization(factura.getOrganization());
      archivoDAL.setActive(true);
      archivoDAL.setCreationDate(new Date());
      archivoDAL.setUpdated(new Date());
      archivoDAL.setCreatedBy(factura.getCreatedBy());
      archivoDAL.setUpdatedBy(factura.getUpdatedBy());
      archivoDAL.setName(factura.getDocumentNo() + ".xml");
      // archivoDAL.setDataType("103");
      long secuencia = 10; // Falta saber como sacar la secuencia correcta, siempre se están
                           // subiendo con la secuencia 10.
      archivoDAL.setSequenceNumber(secuencia);
      archivoDAL.setText("Factura electrónica validada correctamente");
      archivoDAL.setTable(tabFactura.getTable());
      archivoDAL.setRecord(factura.getId());
      // Se guarda el attachment
      OBDal.getInstance().save(archivoDAL); // Guarda el attachment
      OBDal.getInstance().flush();
      // OBDal.getInstance().commitAndClose();

      return "OK";
    } catch (Exception e) {
      StringWriter w = new StringWriter();
      e.printStackTrace(new PrintWriter(w));
      return w.toString();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  // Web Service Call -- Carlos Salinas
  synchronized String timbrar(String endpointAddress, String ruta, String NumFac,
      String PasswordPAC, String archivoPac) throws SOAPException, IOException,
      ParserConfigurationException, SAXException, KeyManagementException, NoSuchAlgorithmException,
      ServletException {
    try {

      Client client = new Client();
      System.setProperty("javax.net.debug", "all");
      return client.call(endpointAddress, ruta, NumFac, PasswordPAC, archivoPac);

    } catch (Exception e) {
      e.printStackTrace();
      String mensajetimbrado = e.toString();
      log.error("CSM // ERROR EN EL TIMBRAR()// " + mensajetimbrado);
      if (mensajetimbrado.indexOf("facturaElectronica.java:448") != -1)// Linea de Password PAC en
                                                                       // este documento la 448 -->
                                                                       // System.setProperty("javax.net.ssl.keyStorePassword",
                                                                       // PasswordPAC)
        return "La contraseña del archivo PAC (.pfx) no es válida";
      else
        return mensajetimbrado;
    }
    // Este metodo puede sustituir a la llamada a la clase RunWS
    // return "OK";
  }

  synchronized public static String convertXMLFileToString(String fileName) {
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      InputStream inputStream = new FileInputStream(new File(fileName));
      org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
      StringWriter stw = new StringWriter();
      Transformer serializer = TransformerFactory.newInstance().newTransformer();
      serializer.transform(new DOMSource(doc), new StreamResult(stw));
      return stw.toString();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  synchronized public String cambiarCaracteres(String cadena) {
    cadena = cadena.replace("&", "&amp;");
    cadena = cadena.replace("<", "&lt;");
    cadena = cadena.replace(">", "&gt;");
    cadena = cadena.replace("\"", "&quot;");
    cadena = cadena.replace("'", "&#39;");
    return cadena;
  }

  public static String fileToString(String file) {
    String result = null;
    DataInputStream in = null;

    try {
      File f = new File(file);
      byte[] buffer = new byte[(int) f.length()];
      in = new DataInputStream(new FileInputStream(f));
      in.readFully(buffer);
      result = new String(buffer);
    } catch (IOException e) {
      throw new RuntimeException("IO problem in fileToString", e);
    } finally {
      try {
        in.close();
      } catch (IOException e) { /* ignore it */
      }
    }
    return result;
  }

  synchronized public static String getValuefromXML(File f, String str) {
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

  synchronized public static Boolean addendasInstaladas() {
    try {
      Class.forName("com.tegik.addenda.module.proc.manejadorAddenda");
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private static final JAXBContext createContext() {
    try {
      return JAXBContext.newInstance("mx.bigdata.sat.cfdi.v32.schema");
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  synchronized private Element marshalTFD(TimbreFiscalDigital tfd) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.newDocument();
    Marshaller m = context.createMarshaller();
    m.setProperty(
        Marshaller.JAXB_SCHEMA_LOCATION,
        "http://www.sat.gob.mx/TimbreFiscalDigital http://www.sat.gob.mx/TimbreFiscalDigital/TimbreFiscalDigital.xsd");
    m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl(
        CFDv32.PREFIXES));
    m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
    m.marshal(tfd, doc);
    return doc.getDocumentElement();
  }

  private void saveReport(VariablesSecureApp vars, JasperPrint jp,
      Map<Object, Object> exportParameters, String fileName) throws JRException {

    final String outputFile = globalParameters.strFTPDirectory + Separador.substring(0, 1)
        + rutaAttach + Separador.substring(0, 1) + NumFac + ".pdf";
    // log.info("CSM > outputFile > " + outputFile);
    final String reportType = fileName.substring(fileName.lastIndexOf(".") + 1);
    if (reportType.equalsIgnoreCase("pdf")) {
      JasperExportManager.exportReportToPdfFile(jp, outputFile);
    } else if (reportType.equalsIgnoreCase("xls")) {
      JExcelApiExporter exporter = new JExcelApiExporter();
      exportParameters.put(JRExporterParameter.JASPER_PRINT, jp);
      exportParameters.put(JRExporterParameter.OUTPUT_FILE_NAME, outputFile);
      exportParameters.put(JExcelApiExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
      exportParameters.put(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
          Boolean.TRUE);
      exportParameters.put(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, true);
      exporter.setParameters(exportParameters);
      exporter.exportReport();
    } else {
      throw new JRException("Report type not supported");
    }

  }

  private String nodeToString(org.w3c.dom.Node node) {
    StringWriter sw = new StringWriter();
    try {
      Transformer t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      t.transform(new DOMSource(node), new StreamResult(sw));
    } catch (TransformerException te) {
      System.out.println("nodeToString Transformer Exception");
    }
    return sw.toString();
  }

  protected String renderJRPiso(VariablesSecureApp variables, String strReportName,
      String strOutputType, HashMap<String, Object> designParameters, FieldProvider[] data,
      Map<Object, Object> exportParameters) throws IOException, ServletException {

    if (strReportName == null || strReportName.equals(""))
      strReportName = PrintJRData.getReportName(this, classInfo.id);

    final String strAttach = globalParameters.strFTPDirectory + "/284-" + classInfo.id;

    final String strLanguage = variables.getLanguage();
    final Locale locLocale = new Locale(strLanguage.substring(0, 2), strLanguage.substring(3, 5));

    final String strBaseDesign = getBaseDesignPath(strLanguage);

    strReportName = Replace.replace(Replace.replace(strReportName, "@basedesign@", strBaseDesign),
        "@attach@", strAttach);
    final String strFileName = strReportName.substring(strReportName.lastIndexOf(Separador
        .substring(0, 1)) + 1);
    // log.info("CSM > strFileName > " + strFileName);

    ServletOutputStream os = null;
    UUID reportId = null;

    try {
      final JasperReport jasperReport = Utility.getTranslatedJasperReport(this, strReportName,
          strLanguage, strBaseDesign);
      if (designParameters == null)
        designParameters = new HashMap<String, Object>();

      Boolean pagination = true;
      if (strOutputType.equals("pdf"))
        pagination = false;

      designParameters.put("IS_IGNORE_PAGINATION", pagination);
      designParameters.put("BASE_WEB", strReplaceWithFull);
      designParameters.put("BASE_DESIGN", strBaseDesign);
      designParameters.put("ATTACH", strAttach);
      designParameters.put("USER_CLIENT", Utility.getContext(this, variables, "#User_Client", ""));
      designParameters.put("USER_ORG", Utility.getContext(this, variables, "#User_Org", ""));
      designParameters.put("LANGUAGE", strLanguage);
      designParameters.put("LOCALE", locLocale);
      designParameters.put("REPORT_TITLE",
          PrintJRData.getReportTitle(this, variables.getLanguage(), classInfo.id));

      final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      dfs.setDecimalSeparator(variables.getSessionValue("#AD_ReportDecimalSeparator").charAt(0));
      dfs.setGroupingSeparator(variables.getSessionValue("#AD_ReportGroupingSeparator").charAt(0));
      final DecimalFormat numberFormat = new DecimalFormat(
          variables.getSessionValue("#AD_ReportNumberFormat"), dfs);
      designParameters.put("NUMBERFORMAT", numberFormat);

      if (log4j.isDebugEnabled())
        log4j.debug("creating the format factory: " + variables.getJavaDateFormat());
      final JRFormatFactory jrFormatFactory = new JRFormatFactory();
      jrFormatFactory.setDatePattern(variables.getJavaDateFormat());
      designParameters.put(JRParameter.REPORT_FORMAT_FACTORY, jrFormatFactory);

      JasperPrint jasperPrint;
      Connection con = null;
      try {
        con = getTransactionConnection();
        if (data != null) {
          designParameters.put("REPORT_CONNECTION", con);
          jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters,
              new JRFieldProviderDataSource(data, variables.getJavaDateFormat()));
        } else {
          jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters, con);
        }
      } catch (final Exception e) {
        log4j.error("OBSUPPORT : __________________________ ERROR: CATCH CONNECTION");
        throw new ServletException(e.getCause() instanceof SQLException ? e.getCause().getMessage()
            : e.getMessage(), e);
      } finally {
        log4j.error("OBSUPPORT: : _________________________ ERROR: FINALY CLOSE CONECTION");
        releaseRollbackConnection(con);
      }
      if (exportParameters == null)
        exportParameters = new HashMap<Object, Object>();
      if (strOutputType == null || strOutputType.equals(""))
        strOutputType = "pdf";
      if (strOutputType.equals("pdf")) {
        reportId = UUID.randomUUID();
        saveReport(variables, jasperPrint, exportParameters, strFileName + "-" + (reportId) + "."
            + strOutputType);
      } else {
        throw new ServletException("Output format no supported");
      }
    } catch (final JRException e) {
      log4j.error("JR: Error: ", e);
      throw new ServletException(e.getMessage(), e);
    } catch (Exception ioe) {
      try {
        FileUtility f = new FileUtility(globalParameters.strFTPDirectory, strFileName + "-"
            + (reportId) + "." + strOutputType, false, true);
        if (f.exists())
          f.deleteFile();
      } catch (IOException ioex) {
        log4j.error("Error trying to delete temporary report file " + strFileName + "-"
            + (reportId) + "." + strOutputType + " : " + ioex.getMessage());
      }
    }
    return globalParameters.strFTPDirectory + Separador.substring(0, 1) + strFileName + "-"
        + (reportId) + "." + strOutputType;

  }

}
