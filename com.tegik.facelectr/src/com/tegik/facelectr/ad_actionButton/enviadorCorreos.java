package com.tegik.facelectr.ad_actionButton;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openbravo.database.ConnectionProvider;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

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
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.ConfigParameters;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.JRFieldProviderDataSource;
import org.openbravo.erpCommon.utility.JRFormatFactory;
import org.openbravo.erpCommon.utility.PrintJRData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.poc.EmailManager;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.EmailServerConfiguration;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.utils.FileUtility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.utils.Replace;
import org.openbravo.scheduling.ProcessBundle;
import org.apache.commons.io.FileUtils;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.service.db.DalConnectionProvider;

import com.tegik.facelectr.data.MensajeCorreo;

import java.util.Arrays;
import java.util.Iterator;

public class enviadorCorreos extends HttpSecureAppServlet {

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  private static final Logger log = Logger.getLogger(enviadorCorreos.class);
  protected ConfigParameters globalParameters;

  public String enviaCorreo(VariablesSecureApp vars, Invoice factura, String enviarPDF,
      String enviarXML, ProcessBundle bundle) {
    try {
      // VariablesSecureApp vars = new VariablesSecureApp(request);
      log.info("METODO, ENVIA CORREO");
      OBContext.setAdminMode(true);

      String correoAlternativo = factura.getFetCorreoalternativo();
      
      //String correoAlternativo = prepararCorreos(correoAlternativo);

      String correoCliente = factura.getBusinessPartner().getFetEmail();
      
      //String correoCliente = prepararCorreos(correoCliente);

      String correoElectronico = factura.getFETEmail();
      
      //String correoElectronico = prepararCorreos(correoElectronico);


      String Correo = "";

      if (correoCliente == null) {
        correoCliente = "";
      }

      if (correoAlternativo == null) {
        correoAlternativo = "";
      }

      // revisa que el correo no este vacio
      if (correoElectronico == null) {
        correoElectronico = "";
      }

      if (correoCliente != null || !correoCliente.equals("")) {
          Correo = prepararCorreos(correoCliente);
      }

      if (correoAlternativo != null || !correoAlternativo.equals("")) {

          if (Correo == null || Correo.equals("")) {
            Correo = prepararCorreos(correoAlternativo);
          } 
          else 
          {
            Correo = Correo + ", " + prepararCorreos(correoAlternativo);
          }
      }

      // revisa formato de correo
      if (correoElectronico != null || !correoElectronico.equals("")) {
      
          if (Correo == null || Correo.equals("")) 
          {
            Correo = prepararCorreos(correoElectronico);
          } 
          else 
          {

            Correo = Correo + ", " + prepararCorreos(correoElectronico);
          }
          
      }

      Correo = prepararCorreos(Correo);

      final OBCriteria<EmailServerConfiguration> configList = OBDal.getInstance().createCriteria(
          EmailServerConfiguration.class);
      configList.add(Expression.eq(EmailServerConfiguration.PROPERTY_CLIENT, factura.getClient()));

      EmailServerConfiguration emailConfig = null;

      for (EmailServerConfiguration emailConfigUd : configList.list()) {
        emailConfig = emailConfigUd;
      }

      long LongPuerto = emailConfig.getSmtpPort();
      int Puerto = (int) LongPuerto;

      String Servidor = emailConfig.getSmtpServer();
      
      
      boolean Auth = emailConfig.isSMTPAuthentification();

      String Cuenta = emailConfig.getSmtpServerAccount();
      String Password = FormatUtilities.encryptDecrypt(emailConfig.getSmtpServerPassword(), false);
      String CuentaEnvio = emailConfig.getSmtpServerSenderAddress();

      
      //Obteniendo el mensaje de una configuracion 
      String mensaje="";
      String asunto="";
      if(factura.getBusinessPartner().getFetMensajecorreo() != null ){
        String msj= factura.getBusinessPartner().getFetMensajecorreo().getMensaje();
        Parametrizacion para = new  Parametrizacion(factura);
        mensaje = para.getMensajeParametros(msj);
        
        String asu= factura.getBusinessPartner().getFetMensajecorreo().getAsunto();
        Parametrizacion paraAsu = new  Parametrizacion(factura);
        asunto = paraAsu.getMensajeParametros(asu);
        
      }
      else if(!(factura.getOrganization().getFetMensajeCorreoList().isEmpty())){
        
        
       MensajeCorreo mensageCorreo= getMensajeCorreoDefault(factura);
       
       if(mensageCorreo == null){
         mensaje = "Buen día. Le hacemos llegar sus archivos .xml y .pdf correspondientes a su factura #"
             + factura.getDocumentNo() + ". Gracias.";
         asunto = "Factura " + factura.getDocumentNo() + " correspondiente a su compra en "
             + factura.getOrganization().getSocialName();
         
       } else {
       
       String msj= mensageCorreo.getMensaje();
       Parametrizacion para = new  Parametrizacion(factura);
       mensaje = para.getMensajeParametros(msj);
       
       String asu= mensageCorreo.getAsunto();
       Parametrizacion paraAsu = new  Parametrizacion(factura);
       asunto = paraAsu.getMensajeParametros(asu);
       }
       
      }else {

      mensaje = "Buen día. Le hacemos llegar sus archivos .xml y .pdf correspondientes a su factura #"
          + factura.getDocumentNo() + ". Gracias.";
      asunto = "Factura " + factura.getDocumentNo() + " correspondiente a su compra en "
          + factura.getOrganization().getSocialName();
      
    }
      
      String idTabla = "318";

      String Separador = System.getProperty("file.separator");
      String attachFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("attach.path");
      String seguridad = emailConfig.getSmtpConnectionSecurity();
      String rutaAttach = idTabla + "-" + factura.getId();
      String NumFac = factura.getDocumentNo();
      String invoiceID = factura.getId();

      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("DOCUMENT_ID", factura.getId());
      log.info("22.5");

      /*
       * final OBCriteria<DocumentTemplate> templateList =
       * OBDal.getInstance().createCriteria(DocumentTemplate.class);
       * templateList.add(Expression.eq(DocumentTemplate.PROPERTY_DOCUMENTTYPE,
       * factura.getDocumentType()));
       * 
       * for (DocumentTemplate templateImpresiones : templateList.list()) {
       * if(!templateImpresiones.isDttIssalidaalmacen() && !templateImpresiones.isDttIscopia()){
       * String templateId = templateImpresiones.getId(); //strReportName = templateImpresiones. } }
       */
      String strReportName = "@basedesign@/com/tegik/facelectr/ad_actionButton/reports/EM_FET_Plantilla_Factura_VentaJR.jrxml";
      facturaElectronica facelectr = new facturaElectronica();
      String rutaPDF = renderJRPiso(vars, strReportName, "pdf", parameters, null, null,
          attachFolder, NumFac, bundle, factura);
      // log.info("CSM>CORREOS" + rutaPDF);

      File archivoPDF = new File(rutaPDF);
      if (!archivoPDF.exists()) {
        return "No se puede enviar el correo ya que no se encuentra el archivo pdf";
      }
      File archivoXML = new File(attachFolder + Separador + idTabla + "-" + factura.getId()
          + Separador + factura.getDocumentNo() + ".xml");
      // log.info(attachFolder + Separador + idTabla + "-" + factura.getId() + Separador +
      // factura.getDocumentNo() + ".xml");
      if (!archivoXML.exists()) {
        return "No se puede enviar el correo ya que no se encuentra el archivo xml";
      }

      // log.info("CSM -- Archivo -- " + archivoXML.toString());
      String archivoPDFAttachString = attachFolder + "/318-"+ factura.getId() + Separador.substring(0, 1) + factura.getDocumentNo() + ".pdf";
      log.info("CSM -- Archivo -- archivoPDFAttachString -- " + archivoPDFAttachString);
      //File archivoPDFAttach = new File(archivoPDFAttachString);
      Tab tabFactura = OBDal.getInstance().get(Tab.class, "263");
      //FileUtils.copyFile(archivoPDF, new File(attachFolder))
      String strSubirArchivo = subirArchivo(vars, "263", factura, tabFactura);
      
      List<File> listaArchivos = new ArrayList();
      listaArchivos.add(archivoPDF);
      listaArchivos.add(archivoXML);
      
      if (Correo.equals("")) {
        return "El correo electrónico del receptor no es válido";
      }
      
      EmailManager correo = new EmailManager();

      // correo.sendEmail(Servidor, Auth, Cuenta, Password, seguridad,Puerto, CuentaEnvio, Correo,
      // "", "csalinas@tegik.com", CuentaEnvio, Asunto, Mensaje, null, listaArchivos, new Date(),
      // null);
      correo.sendEmail(Servidor, Auth, Cuenta, Password, seguridad, Puerto, CuentaEnvio, Correo,
          "", "", CuentaEnvio, asunto, mensaje, null, listaArchivos, new Date(), null);

      OBContext.restorePreviousMode();
      return "OK";
    } catch (Exception e) {
      log.info("EXCEPTION DEL ENVIA" + e.getMessage());
      StringWriter w = new StringWriter();
      e.printStackTrace(new PrintWriter(w));
      String errorfactura = w.toString();
      log.info("EXCEPTION DEL ENVIA 2 " + errorfactura);
      // log.info("CSM>CORREOS --ENVIACORREO-- " + errorfactura);
      return e.toString();
    }
  }

  public String solicitarEnvio(VariablesSecureApp vars, Invoice factura, String enviarPDF, String enviarXML, ProcessBundle bundle) {
    try {
      log.info("ENTRO A SOLICITAR ENVIO");
      String statusEnvio = enviaCorreo(vars, factura, enviarPDF, enviarXML, bundle);
      log.info(statusEnvio);
      if (statusEnvio == "OK") {
        OBContext.setAdminMode(true);
        factura.setFetCorreoenviado(true);
        factura.setFetStatuscorreo("Correo enviado exitosamente");
        long intento = 0;
        factura.setFetIntento(intento);
        OBDal.getInstance().save(factura);
        OBDal.getInstance().flush();
        OBContext.restorePreviousMode();
        return "OK";
      } else {
        OBContext.setAdminMode(true);
        factura.setFetCorreoenviado(false);
        factura.setFetStatuscorreo(statusEnvio);
        Long intento = factura.getFetIntento();
        long uno = 1;
        if (intento != null) {
          factura.setFetIntento(intento + uno);
        } else {
          factura.setFetIntento(uno);
        }
        OBDal.getInstance().save(factura);
        OBDal.getInstance().flush();
        OBContext.restorePreviousMode();
        return statusEnvio;
      }

    } catch (Exception e) {
      StringWriter w = new StringWriter();
      e.printStackTrace(new PrintWriter(w));
      String errorfactura = w.toString();
      // log.info("CSM>CORREOS --SOLICITARENVIO-- " + errorfactura);
      return e.toString();
    }
  }

  public String checaFormatoEmail(String correo) {
    Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
    Matcher m = p.matcher(correo);
    boolean matchFound = m.matches();

    if (matchFound) {
      return "OK";
    } else {
      return "ERROR";
    }
  }

  public void saveReport(VariablesSecureApp vars, JasperPrint jp,
      Map<Object, Object> exportParameters, String fileName, String rutaAttach, String NumFac)
      throws JRException {

    OBContext.setAdminMode(true);

    // final String outputFile = globalParameters.strFTPDirectory + "/"+ rutaAttach + "/" + NumFac +
    // ".pdf";
    final String outputFile = fileName;
    // log.info("CSM > outputFile > " + outputFile);
    final String reportType = fileName.substring(fileName.lastIndexOf(".") + 1);
    // log.info("CSM > reportType > " + reportType);
    if (reportType.equalsIgnoreCase("pdf")) {
      JasperExportManager.exportReportToPdfFile(jp, outputFile);
      OBContext.restorePreviousMode();
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
      OBContext.restorePreviousMode();
    } else {
      OBContext.restorePreviousMode();
      throw new JRException("Report type not supported");
    }

  }

  public String renderJRPiso(VariablesSecureApp variables, String strReportName,
      String strOutputType, HashMap<String, Object> designParameters, FieldProvider[] data,
      Map<Object, Object> exportParameters, String rutaAttach, String NumFac, ProcessBundle bundle, Invoice factura)
      throws IOException, ServletException {
    log.info("ENTRO A RENDERJRPISO");
    OBContext.setAdminMode(true);
    
    String Separador = System.getProperty("file.separator");
    String archivoPDFAttachString = rutaAttach + "/318-"+ factura.getId() + Separador.substring(0, 1) + factura.getDocumentNo() + ".pdf";
    log.info("CSM -- Archivo -- archivoPDFAttachString -- " + archivoPDFAttachString);
    
    String invoiceID = factura.getId();

    if (strReportName == null || strReportName.equals(""))
      strReportName = PrintJRData.getReportName(this, classInfo.id);

    log.info("CSM > strReportName > " + strReportName);
    final String strAttach = rutaAttach + "/284-" + invoiceID;
    log.info("CSM > strAttach > " + strAttach);

    final String strLanguage = "es_MX";
    log.info("CSM > strLanguage > " + strLanguage);

    final Locale locLocale = new Locale(strLanguage.substring(0, 2), strLanguage.substring(3, 5));
    log.info("CSM > locLocale > " + locLocale);

    String strBaseDesign = "";
    try
    {
	strBaseDesign = getBaseDesignPath(strLanguage);
    }
    catch (Exception e)
    {
	log.info("Excepción con el base design orignal, intentando con el bundle...");
    }
    
    if (strBaseDesign == "")
    {
	log.info("CSM > no hay config> ");
	strBaseDesign = getBaseDesignPath2();
    }
	
    
    log.info("CSM > strBaseDesign > " + strBaseDesign);

    strReportName = Replace.replace(Replace.replace(strReportName, "@basedesign@", strBaseDesign),
        "@attach@", strAttach);
    log.info("CSM > strReportName > " + strReportName);
    final String strFileName = strReportName.substring(strReportName.lastIndexOf("/") + 1);
    log.info("CSM > strFileName > " + strFileName);

    ServletOutputStream os = null;
    UUID reportId = null;
    log.info("linea 433");
    try {
    log.info("linea 435");
      ConnectionProvider connCSM = null;
      if (bundle == null)
      {log.info("linea 438");
	//connCSM = getTransactionConnection();
	DalConnectionProvider conn = new  DalConnectionProvider(false);
	connCSM = conn;
      }
      else
      {log.info("linea 443");
	connCSM = bundle.getConnection();
      }
      log.info("linea 446");
      final JasperReport jasperReport = Utility.getTranslatedJasperReport(connCSM, strReportName,
          strLanguage, strBaseDesign);
          log.info("linea 449");
      if (designParameters == null)
        designParameters = new HashMap<String, Object>();

      Boolean pagination = true;
      if (strOutputType.equals("pdf"))
        pagination = false;
      log.info("REN3");
      designParameters.put("IS_IGNORE_PAGINATION", pagination);
      log.info("REN4");
      //designParameters.put("BASE_WEB", strReplaceWithFull);
      log.info("strReportName" + strReplaceWithFull);
      log.info("REN5");
      designParameters.put("BASE_WEB", "http://localhost/openbravo/web");
      log.info("REN6");
      designParameters.put("BASE_DESIGN", strBaseDesign);
      log.info("REN7");
      designParameters.put("ATTACH", strAttach);
      log.info("REN8");
      designParameters.put("USER_CLIENT", factura.getClient());
      log.info("REN9");
      designParameters.put("USER_ORG", factura.getOrganization());
      log.info("REN10");
      designParameters.put("LANGUAGE", strLanguage);
      log.info("REN11");
      designParameters.put("LOCALE", locLocale);
      log.info("REN12");
      designParameters.put("REPORT_TITLE",
          PrintJRData.getReportTitle(connCSM, "es_MX", invoiceID));
      log.info("REN12.1");

      final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      log.info("REN12.2");
      dfs.setDecimalSeparator(".".charAt(0));
      log.info("REN12.3");
      dfs.setGroupingSeparator(",".charAt(0));
      log.info("REN12.4");
      final DecimalFormat numberFormat = new DecimalFormat("#,##0.##", dfs);
      log.info("REN12.5");    
      designParameters.put("NUMBERFORMAT", numberFormat);
      log.info("REN12.6");
     
      String javaDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
      final JRFormatFactory jrFormatFactory = new JRFormatFactory();
      jrFormatFactory.setDatePattern(javaDateFormat);
      
      log.info("creating the format factory: " + javaDateFormat);
      log.info("REN13");
      designParameters.put(JRParameter.REPORT_FORMAT_FACTORY, jrFormatFactory);
      log.info("REN14");
      JasperPrint jasperPrint;
      Connection con = null;
      try {
        try
        { 
          DalConnectionProvider conn = new  DalConnectionProvider(false);
	  con = conn.getTransactionConnection();
        }
        catch (Exception e)
        {
	  log.info("No hay conexión, intentando con el bundle");
        }
        
        if (con == null) {
	    log.info("CONEXION USANDO LA DEL BUNDLE");
	    con = bundle.getConnection().getConnection();
        }
        
        if (con == null) {
        
	  log.info("CONEXION NULA");
        
        }
        
        log.info("REN15");
        if (data != null) {
          designParameters.put("REPORT_CONNECTION", con);
          log.info("REN16");
          jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters,
              new JRFieldProviderDataSource(data, variables.getJavaDateFormat()));
        } else {
          log.info("REN17");
          jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters, con);
        }
        con.close();
      } catch (final Exception e) {
        log.info("EXPCETION RENDER" + e.getMessage());
        throw new ServletException(e.getCause() instanceof SQLException ? e.getCause().getMessage()
            : e.getMessage(), e);
      } finally {
        //releaseRollbackConnection(con);
      }
      log.info("REN18");
      
      if (exportParameters == null)
        exportParameters = new HashMap<Object, Object>();
      if (strOutputType == null || strOutputType.equals(""))
        strOutputType = "pdf";
      if (strOutputType.equals("pdf")) {
        reportId = UUID.randomUUID();
        
        saveReport(variables, jasperPrint, exportParameters, archivoPDFAttachString, rutaAttach, NumFac);
      } else {
        log.info("Output format no supported");
        throw new ServletException("Output format no supported");
      }
    } catch (final JRException e) {
      log.info("JR: Error: " + e.getMessage());
      throw new ServletException(e.getMessage(), e);
    } catch (Exception ioe) {
      try {
        log.info("REN19");
        StringWriter sw = new StringWriter();
	PrintWriter pw = new PrintWriter(sw);
	ioe.printStackTrace(pw);
	
        log.info("EXCEPTION RENDER 2" + sw.toString());
        FileUtility f = new FileUtility(rutaAttach, strFileName + "-" + (reportId) + "."
            + strOutputType, false, true);
        if (f.exists())
          f.deleteFile();
      } catch (IOException ioex) {
        log.info("Error trying to delete temporary report file " + strFileName + "-" + (reportId)
            + "." + strOutputType + " : " + ioex.getMessage());
      }
    }
    OBContext.restorePreviousMode();
    // log.info(rutaAttach + "/" + strFileName + "-" + (reportId) + "." + strOutputType);
    return archivoPDFAttachString;

  }
  
  
  private String getBaseDesignPath2() {

     ConfigParameters confParam = 
     ConfigParameters.retrieveFrom(RequestContext.getServletContext());
     String strNewAddBase = confParam.strDefaultDesignPath;
     String strFinal = confParam.strBaseDesignPath;
     if (!strFinal.endsWith("/" + strNewAddBase)) {
        strFinal += "/" + strNewAddBase;
     }
      return confParam.prefix + strFinal;
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
      archivoDAL.setName(factura.getDocumentNo() + ".pdf");
      // archivoDAL.setDataType("103");
      long secuencia = 20; // Falta saber como sacar la secuencia correcta, siempre se están
                           // subiendo con la secuencia 10.
      archivoDAL.setSequenceNumber(secuencia);
      archivoDAL.setText("PDF Correspondiente");
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
  
    synchronized String prepararCorreos(String correos)
    {
	String regresar = "";
	log.info("PREPARARCORREOS - ENTRADA - " + correos);
	List<String> listaCorreos = Arrays.asList(correos.split(","));
	// Obtenemos un Iterador y recorremos la lista.
	Iterator iter = listaCorreos.iterator();
	while (iter.hasNext())
	{
	    String correoUnit = (String) iter.next();
	    correoUnit = correoUnit.trim().replaceAll("\\s+","");
	    
	    if (checaFormatoEmail(correoUnit).equals("OK"))
	    {
		regresar = regresar + correoUnit + ", ";
	    }
	}
	
	log.info("PREPARARCORREOS - SALIDA - " + regresar);
	return regresar;
    }
    
    public MensajeCorreo getMensajeCorreoDefault(Invoice inv) throws Exception{

      OBContext.setAdminMode(true);
      OBCriteria<MensajeCorreo> obMen = OBDal.getInstance().createCriteria(MensajeCorreo.class);
      obMen.add(Restrictions.eq(MensajeCorreo.PROPERTY_PORDEFECTO, true));
      obMen.add(Restrictions.eq(MensajeCorreo.PROPERTY_ORGANIZATION, inv.getOrganization()));
      List<MensajeCorreo> mensajes = obMen.list();
   
      
      if(mensajes.isEmpty()){
        return null;
      }
      OBContext.restorePreviousMode();

      return mensajes.get(0);
      
      
    }


}
