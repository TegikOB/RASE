package com.tegik.facelectr.ad_actionButton;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;
//import mx.bigdata.cfdi.CFDv3;
//import java.net.*;

public class cancelarFactura {

  private static final Logger log = Logger.getLogger(cancelarFactura.class);

  String cancelarFac(String ruta, String NumFac, String RFC_Emisor, String RFC_Receptor,
      String uuid, String refID, String PasswordPAC, String archivoPac, String urlWebService)
      throws IOException {
    String strTimbrar = "";

    log.info(ruta + "Request_Cancela" + NumFac + ".xml");
    try {

      // Empieza Crear el encapsualado SOAP para la conexion a WS del PAC
      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(ruta
          + "Request_Cancela" + NumFac + ".xml", false), "UTF-8");
      BufferedWriter encabezado = new BufferedWriter(writer);
      // encabezado.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      encabezado
          .write("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://www.buzonfiscal.com/ns/xsd/bf/bfcorp/3\">\n");
      encabezado.write("  <soapenv:Header/>\n");
      encabezado.write("  <soapenv:Body>\n");
      // encabezado.write("      <ns:RequestCancelaCFDi RfcEmisor=\""+RFC_Emisor+"\" RfcReceptor=\""+RFC_Receptor+"\" uuid=\""+uuid+"\" refID=\""+refID+"\"/>\n");
      encabezado.write("      <ns:RequestCancelaCFDi rfcEmisor=\"" + RFC_Emisor
          + "\" rfcReceptor=\"" + RFC_Receptor + "\" uuid=\"" + uuid + "\"/>\n");
      encabezado.write("  </soapenv:Body>\n");
      encabezado.write("</soapenv:Envelope>\n");
      encabezado.close();
      // Termina crear el encapsualado SOAP

      String respuesta;
      clientCancela client = new clientCancela();
      System.setProperty("javax.net.debug", "all");
      log.info("DEMO-CANCELACION // archivoPac // " + archivoPac);
      log.info("DEMO-CANCELACION // PasswordPAC // " + PasswordPAC);
      log.info("DEMO-CANCELACION // NumFac // " + NumFac);
      log.info("DEMO-CANCELACION // ruta // " + ruta);
      respuesta = client.call(urlWebService, ruta, NumFac, PasswordPAC, archivoPac);
      return respuesta;

    }

    catch (Exception e) {
      e.printStackTrace();
      return e.getLocalizedMessage();
    }
  }

}
