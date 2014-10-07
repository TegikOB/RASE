package com.tegik.facelectr.ad_actionButton;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class extraeCER {

  private static String Certificado = "";
  private static String NoCertificado = "";

  public static void extrae(String ruta, String NumFac) {
    try {
      // creates and returns new instance of SAX-implementation:
      SAXParserFactory factory = SAXParserFactory.newInstance();

      // create SAX-parser...
      SAXParser parser = factory.newSAXParser();
      // .. define our handler:
      SaxHandler handler = new SaxHandler();

      // and parse:
      parser.parse(ruta + NumFac + ".xml", handler); // Se abre el archivo Factura origial para
                                                     // extraer el codigo del Certificado

    } catch (Exception ex) {
      ex.printStackTrace(System.out);
    }
  }

  private static final class SaxHandler extends DefaultHandler {

    // we enter to element 'qName':
    public void startElement(String uri, String localName, String qName, Attributes attrs)
        throws SAXException {

      // Se agrega el nombre del atributo del nombre
      if (qName.equals("cfdi:Comprobante")) {
        // Se agrega el nombre del atributo a buscar
        Certificado = attrs.getValue("certificado");
        NoCertificado = attrs.getValue("noCertificado");

      }
    }
  }

  public static String getCertificado() {
    return Certificado;
  }

  public static String getNoCertificado() {
    return NoCertificado;
  }
}