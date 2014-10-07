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
import org.xml.sax.SAXException;

/**
 * Created by IntelliJ IDEA. User: hari Date: 10/16/12 Time: 3:11 PM To change this template use
 * File | Settings | File Templates.
 */
public class Client {

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
    call.setSOAPActionURI("http://www.buzonfiscal.com/TimbradoCFDI/timbradoCFD");

    // Setting Operation Name
    call.setOperationName(new javax.xml.namespace.QName("http://www.buzonfiscal.com/TimbradoCFDI/",
        "timbradoCFD"));

    // Setting return type to ANY so that it accepts document type xml
    call.setReturnType(XMLType.XSD_ANY);

  }

  private boolean initialized = false;

  public synchronized String call(String endpointAddress, String ruta, String NumFac,
      String PasswordPAC, String archivoPac) {

    FileInputStream in = null;
    FileOutputStream out = null;
    try {
      if (archivoPac != null) {
        System.setProperty(CustomSSLSocketFactory.KEY_STORE, archivoPac);
        // System.setProperty(CustomSSLSocketFactory.TRUST_STORE,archivoPac);
      }
      if (archivoPac != null) {
        System.setProperty(CustomSSLSocketFactory.KEY_STORE_PASSWORD, PasswordPAC);
        // System.setProperty(CustomSSLSocketFactory.TRUST_STORE_PASSWORD,PasswordPAC);
      }

      // System.setProperty(CustomSSLSocketFactory.TRUST_STORE_TYPE,"PKCS12");
      // System.setProperty(CustomSSLSocketFactory.TRUST_MANAGER_TYPE,"SunX509");
      System.setProperty(CustomSSLSocketFactory.KEY_STORE_TYPE, "PKCS12");
      System.setProperty(CustomSSLSocketFactory.KEY_MANAGER_TYPE, "SunX509");

      System.setProperty(CustomSSLSocketFactory.SECURITY_PROVIDER_CLASS,
          "com.sun.net.ssl.internal.ssl.Provider");
      System.setProperty(CustomSSLSocketFactory.SECURITY_PROTOCOL, "SSLv3");
      System.setProperty(CustomSSLSocketFactory.PROTOCOL_HANDLER_PACKAGES,
          "com.sun.net.ssl.internal.www.protocol");

      // Creating SOAP Envelop from the xml file

      // in = new BufferedReader(new FileReader(ruta + "requestTimbrado" + NumFac + ".xml"));
      in = new FileInputStream(ruta + "requestTimbrado" + NumFac + ".xml");
      // //
      // StringBuffer buf = new StringBuffer();
      // String line = null;
      // while((line = in.readLine()) != null){
      // buf.append(line);
      // buf.append(System.getProperty("line.separator"));
      // }

      if (!initialized) {
        initialize();
        initialized = true;
      }

      SOAPEnvelope soapEnvelope = new SOAPEnvelope(in);
      // SOAPEnvelope soapEnvelope = new SOAPEnvelope(new
      // ByteArrayInputStream(buf.toString().trim().getBytes()));

      // Setting End point Address
      call.setTargetEndpointAddress(endpointAddress);

      // Calling the webservice with the defined soap envelope
      SOAPEnvelope response = call.invoke(soapEnvelope);

      out = new FileOutputStream(ruta + "Timbrado" + NumFac + ".xml", true); // Se crea el archivo
                                                                             // donde se guardará la
                                                                             // informacion que se
                                                                             // reciba del PAC
      out.write(response.toString().getBytes());
      out.flush();

    } catch (AxisFault fault) {
      if (fault.detail instanceof ConnectException
          || fault.detail instanceof InterruptedIOException
          || (fault.getFaultString().indexOf("Connection timed out") != -1)
          || fault.getFaultCode().getLocalPart().equals("HTTP")) {
        System.err.println("Unable to reach the end point.");
        System.err.println(fault.getLocalizedMessage());
      }
      return fault.getLocalizedMessage();
    } catch (FileNotFoundException e) {
      System.out.println("Unable to find the request/response file");
      System.err.println(e.getLocalizedMessage());
      return "Unable to find the request/response file. Reason + " + e.getLocalizedMessage();
    } catch (SAXException e) {
      System.out.println("Unable to parse the request");
      System.err.println(e.getLocalizedMessage());
      return "Unable to parse the request. Reason + " + e.getLocalizedMessage();
    } catch (IOException e) {
      System.out.println("Unable to open/write request or response from/to the file");
      System.err.println(e.getLocalizedMessage());
      return "Unable to open/write request or response from/to the file. Reason + "
          + e.getLocalizedMessage();
    } catch (ServiceException e) {
      System.err.println("Unable to initialize Call.");
      System.err.println(e.getLocalizedMessage());
      return "Unable to initialize Call. Reason + " + e.getLocalizedMessage();
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace(); // To change body of catch statement use File | Settings | File
                               // Templates.
        }
      }
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          e.printStackTrace(); // To change body of catch statement use File | Settings | File
                               // Templates.
        }
      }
    }

    return "OK";
  }

  public static void main(String[] args) {
    Client client = new Client();
    System.out.println(client.call("https://tf.buzonfiscal.com/timbrado", "ruta", "2",
        "HBA011026RC4", "E:\\projects\\DynSSL\\HBA011026RC4.pfx"));
    // System.out.println(client.call("https://tf.buzonfiscal.com/timbrado", "ruta", "2", "fiorano",
    // "E:\\localhost.ks"));
    System.out.println(client.call("https://tf.buzonfiscal.com/timbrado", "ruta", "2",
        "HBA011026RC4", "E:\\projects\\DynSSL\\HBA011026RC4.pfx"));
  }
}
