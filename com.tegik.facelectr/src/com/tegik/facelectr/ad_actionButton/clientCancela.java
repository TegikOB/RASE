package com.tegik.facelectr.ad_actionButton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Created by IntelliJ IDEA. User: hari Date: 10/16/12 Time: 3:11 PM To change this template use
 * File | Settings | File Templates.
 */

public class clientCancela {
  private static final Logger log = Logger.getLogger(clientCancela.class);

  static {
    System.setProperty("VerifyHostName", "false");
    // CustomSSLSocketFactory full class path should be provided.
    System.setProperty("axis.socketSecureFactory",
        "com.tegik.facelectr.ad_actionButton.CustomSSLSocketFactory");
    System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
  }

  Call call = null;

  public void initialize() throws ServiceException {

    Service service = new Service(); // initializing a dummy service
    call = (Call) service.createCall(); // initializing a call

    // Setting Soap Action property
    call.setUseSOAPAction(true);
    call.setSOAPActionURI("http://www.buzonfiscal.com/CorporativoWS3.0/cancelaCFDi");

    // Setting Operation Name
    call.setOperationName(new javax.xml.namespace.QName(
        "http://www.buzonfiscal.com/CorporativoWS3.0/", "cancelaCFDi"));

    // Setting return type to ANY so that it accepts document type xml
    call.setReturnType(XMLType.XSD_ANY);

  }

  private boolean initialized = false;

  public synchronized String call(String endpointAddress, String ruta, String NumFac,
      String PasswordPAC, String archivoPac) {
    String rutaResponse;

    FileInputStream in = null;
    FileOutputStream out = null;
    try {
      if (archivoPac != null) {
        log.info(archivoPac);
        System.setProperty(CustomSSLSocketFactory.KEY_STORE, archivoPac);
      }
      if (archivoPac != null) {
        log.info(PasswordPAC);
        System.setProperty(CustomSSLSocketFactory.KEY_STORE_PASSWORD, PasswordPAC);
      }

      log.info("1");
      System.setProperty(CustomSSLSocketFactory.KEY_STORE_TYPE, "PKCS12");
      log.info("2");
      System.setProperty(CustomSSLSocketFactory.KEY_MANAGER_TYPE, "SunX509");
      log.info("3");
      System.setProperty(CustomSSLSocketFactory.SECURITY_PROVIDER_CLASS,
          "com.sun.net.ssl.internal.ssl.Provider");
      log.info("4");
      System.setProperty(CustomSSLSocketFactory.SECURITY_PROTOCOL, "SSLv3");
      log.info("5");
      System.setProperty(CustomSSLSocketFactory.PROTOCOL_HANDLER_PACKAGES,
          "com.sun.net.ssl.internal.www.protocol");
      log.info("6");

      // Creating SOAP Envelop from the xml file
      in = new FileInputStream(ruta + "Request_Cancela" + NumFac + ".xml");
      log.info("7");

      if (!initialized) {
	log.info("8");
        initialize();
        log.info("9");
        initialized = true;
        log.info("10");
      }
      log.info("11");
      SOAPEnvelope soapEnvelope = new SOAPEnvelope(in);
      log.info("12");

      // Setting End point Address
      // call.setTargetEndpointAddress("https://demonegocios.buzonfiscal.com/bfcorpcfdiws");
      call.setTargetEndpointAddress(endpointAddress);
      log.info("13");

      // Calling the webservice with the defined soap envelope
      SOAPEnvelope response = call.invoke(soapEnvelope);
      log.info("14");

      out = new FileOutputStream(ruta + "ResponseCancela" + NumFac + ".xml", false);
      log.info("15");
      log.info ("RESPONSE.TOSTRING()" + response.toString());
      out.write(response.toString().getBytes());
      log.info("16");
      out.flush();
      log.info("17");
      rutaResponse = ruta + "ResponseCancela" + NumFac + ".xml";
      log.info("18");

    } catch (AxisFault fault) {
    
      log.info(fault.dumpToString());
      log.info(fault.getFaultActor());
      log.info(fault.getFaultNode());
      log.info(fault.getFaultReason());
      log.info(fault.getFaultRole());
      log.info(fault.getFaultString());
      log.info(fault.getFaultCode().getLocalPart());
      log.info(fault.getFaultCode().getNamespaceURI());
      log.info(fault.getFaultCode().getPrefix());      
      
      if (fault.detail instanceof ConnectException
          || fault.detail instanceof InterruptedIOException
          || (fault.getFaultString().indexOf("Connection timed out") != -1)
          || fault.getFaultCode().getLocalPart().equals("HTTP")) {
        log.info("Unable to reach the end point.");
        log.info(fault.getLocalizedMessage());
      }
      return fault.getLocalizedMessage();
    } catch (FileNotFoundException e) {
      log.info("Unable to find the request/response file");
      log.info(e.getLocalizedMessage());
      return "Unable to find the request/response file. Reason + " + e.getLocalizedMessage();
    } catch (SAXException e) {
      log.info("Unable to parse the request");
      log.info(e.getLocalizedMessage());
      return "Unable to parse the request. Reason + " + e.getLocalizedMessage();
    } catch (IOException e) {
      log.info("Unable to open/write request or response from/to the file");
      log.info(e.getLocalizedMessage());
      return "Unable to open/write request or response from/to the file. Reason + "
          + e.getLocalizedMessage();
    } catch (ServiceException e) {
      log.info("Unable to initialize Call.");
      log.info(e.getLocalizedMessage());
      return "Unable to initialize Call. Reason + " + e.getLocalizedMessage();
    }
    catch (Exception e) {
      log.info("Error en el proceso.");
      log.info(e.getLocalizedMessage());
      return "Unable to initialize Call. Reason + " + e.getLocalizedMessage();
    }
    finally {
      log.info("19");
      if (in != null) {
        log.info("20");
        try {
          log.info("21");
          in.close();
          log.info("22");
        } catch (IOException e) {
          log.info("23");
          e.printStackTrace(); // To change body of catch statement use File | Settings | File
                               // Templates.
        }
      }
      if (out != null) {
        log.info("24");
        try {
          log.info("25");
          out.close();
          log.info("26");
        } catch (IOException e) {
          log.info("27");
          e.printStackTrace(); // To change body of catch statement use File | Settings | File
                               // Templates.
        }
      }
    }
    log.info("28");
    log.info("RUTARESPONSE // " + rutaResponse);
    return rutaResponse;
  }

  public static void main(String[] args) {
    Client client = new Client();
    log.info(client.call("https://tf.buzonfiscal.com/timbrado", "ruta", "2",
        "HBA011026RC4", "E:\\projects\\DynSSL\\HBA011026RC4.pfx"));
    // log.info(client.call("https://tf.buzonfiscal.com/timbrado", "ruta", "2", "fiorano",
    // "E:\\localhost.ks"));
    log.info(client.call("https://tf.buzonfiscal.com/timbrado", "ruta", "2",
        "HBA011026RC4", "E:\\projects\\DynSSL\\HBA011026RC4.pfx"));
  }
}
