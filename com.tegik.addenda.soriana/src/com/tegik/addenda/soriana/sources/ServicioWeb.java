package com.tegik.addenda.soriana.sources;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException; 
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.net.HttpURLConnection;

import javax.xml.soap.*;

import org.apache.axis.soap.SOAPConnectionImpl;
import org.apache.log4j.Logger;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.model.common.invoice.Invoice;

import com.tegik.addenda.soriana.sources.ackerrorapl.AckErrorApplication;


public class ServicioWeb {
  
  private static final Logger log = Logger.getLogger(ServicioWeb.class);
  public static String mensajeExito="";

  public static void call( Invoice invoice) throws ExceptionServicioWeb {
    
    try {
      
      // Create SOAP Connection
      SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
      SOAPConnection soapConnection = soapConnectionFactory.createConnection();

      // Send SOAP Message to SOAP Server
      URL endpoint = new URL(null, 
          "http://www.soriana.com/recibecfd/wseDocRecibo.asmx", 
          new URLStreamHandler() {
             @Override
             protected URLConnection openConnection(URL url) throws IOException {
                URL clone_url = new URL(url.toString());
                HttpURLConnection clone_urlconnection = (HttpURLConnection) clone_url.openConnection();
                clone_urlconnection.setConnectTimeout(30000);
                clone_urlconnection.setReadTimeout(6000);
                return(clone_urlconnection); 
             }
      });
      
      
      //Leer en un archivo la factura
      String attachFolder = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("attach.path");
      String directorio = attachFolder +"/" +"318-"+invoice.getId();
      String archivo = invoice.getDocumentNo()+ ".xml";
      String xml = getXML(directorio + "/" + archivo);   
      
 
      //create el SOAP
      SOAPMessage soapRequest = createSOAPRequest(xml);
      String strResquest = toString(soapRequest);     
      
     
      //SOAP response
      SOAPMessage soapResponse;
      try {
        
        soapResponse = soapConnection.call(soapRequest, endpoint);
        String strResponse = getResult(soapResponse.getSOAPBody());
        
        log.info("REPONSE " + strResponse );
        
        if(strResponse.contains("AckErrorApplication")){
          
          AckErrorApplication app = Convertidor.toAckErrorApplication(strResponse);
          
          if(app.getDocumentStatus().equals("REJECT")){
            throw new ExceptionServicioWeb(app.getMessageError().getErrorDescription().getText());
          } else if(app.getDocumentStatus().equals("ACCEPTED")){
            mensajeExito = "Folio de Atención " + app.getReferenceNumber().getReferenceIdentification();
          }
          
          
         }
         
      } catch (SOAPException excep){
        log.info(excep.getMessage());
        throw  new ExceptionServicioWeb("El tiempo de respuesta fue muy largo, consultar al proveedor de addenda ");
      }
      

      soapConnection.close();
      
  } catch (Exception e1) {
    throw new ExceptionServicioWeb(e1.getMessage());
  } 
    
  }
  
  public static SOAPMessage createSOAPRequest(String xml) throws Exception {
    

    MessageFactory messageFactory = MessageFactory.newInstance();
    SOAPMessage soapMessage = messageFactory.createMessage();
    SOAPPart soapPart = soapMessage.getSOAPPart();

    String serverURI = "http://www.sci-grupo.com.mx/";

    // SOAP Envelope
    SOAPEnvelope envelope = soapPart.getEnvelope();

    /*
    Constructed SOAP Request Message:
    <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:example="http://ws.cdyne.com/">
        <SOAP-ENV:Header/>
        <SOAP-ENV:Body>
            <RecibeCFD>
                <XMLCFD>String</XMLCFD>
            </RecibeCFD>
        </SOAP-ENV:Body>
    </SOAP-ENV:Envelope>
     */

    // SOAP Body
    SOAPBody soapBody = envelope.getBody();
    Name recibeCDFD = envelope.createName("RecibeCFD", null,
        "http://www.sci-grupo.com.mx/");
    SOAPBodyElement cfd = soapBody.addBodyElement(recibeCDFD);
    
    Name xmlcfd = envelope.createName("XMLCFD");
    SOAPElement mySymbol = cfd.addChildElement(xmlcfd);
    mySymbol.addTextNode(xml);
    
    //SOAP Action
    MimeHeaders headers = soapMessage.getMimeHeaders();
    headers.addHeader("SOAPAction", serverURI  + "RecibeCFD");  

    soapMessage.saveChanges();

    return soapMessage;
}
  
  public static String getXML( String archivo) throws IOException{ 

    BufferedReader br = new BufferedReader(new FileReader(archivo));
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null) {
          sb.append(line);
          line = br.readLine();
      }
      return sb.toString();
  
    }
       
  
  public static String toString(SOAPMessage msg) throws SOAPException, IOException{
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    msg.writeTo(out);
    String strMsg = new String(out.toByteArray());
    
    return strMsg;
    
  }
  
  public static String getResult(SOAPBody body) {
    
    Node nodo = (Node) body.getFirstChild().getFirstChild();
       
    return nodo.getTextContent();
    
  }
  
  protected URLConnection openConnection(String url) throws IOException {
    URL clone_url = new URL(url.toString());
    HttpURLConnection clone_urlconnection = (HttpURLConnection) clone_url.openConnection();
    clone_urlconnection.setConnectTimeout(10000);
    clone_urlconnection.setReadTimeout(10000);
    return(clone_urlconnection); 
 }
  


}
