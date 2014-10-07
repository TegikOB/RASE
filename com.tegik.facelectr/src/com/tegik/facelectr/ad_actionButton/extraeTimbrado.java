package com.tegik.facelectr.ad_actionButton;

import java.io.File;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class extraeTimbrado extends DefaultHandler {

  // Inicializacion de variables para sacar el timbrado del archivo xml
  public String Fechatimbrado = "";
  public String uuid = "";
  public String noCertificadoSAT = "";
  public String selloCFD = "";
  public String selloSAT = "";
  public String version = "";

  private static String Fechatimbrado_atributo = "";
  private static String uuid_atributo = "";
  private static String noCertificadoSAT_atributo = "";
  private static String selloCFD_atributo = "";
  private static String selloSAT_atributo = "";
  private static String version_atributo = "";

  private static String CadenaOriginalTimbrado = "";

  public extraeTimbrado(File archivoTimbrado) {
    this.selloSAT = facturaElectronica.getValuefromXML(archivoTimbrado, "selloSAT");
    this.selloCFD = facturaElectronica.getValuefromXML(archivoTimbrado, "selloCFD");
    this.Fechatimbrado = facturaElectronica.getValuefromXML(archivoTimbrado, "FechaTimbrado");
    this.noCertificadoSAT = facturaElectronica.getValuefromXML(archivoTimbrado, "noCertificadoSAT");
    this.uuid = facturaElectronica.getValuefromXML(archivoTimbrado, "UUID");
    this.version = facturaElectronica.getValuefromXML(archivoTimbrado, "version");
    this.CadenaOriginalTimbrado = "ǀǀ" + this.version + "ǀ" + this.uuid + "ǀ" + this.Fechatimbrado
        + "ǀ" + this.selloSAT + "ǀ" + this.noCertificadoSAT + "ǀǀ";
  }

  public void startElement(String namespaceURI, String localName, String qName, Attributes attr)
      throws SAXException {

    // Obtinene los nombre de atributos y los atributos del archivo timbrado y los guarda en la
    // variable
    Fechatimbrado = attr.getLocalName(0) + "=\"" + attr.getValue(0) + "\"";
    uuid = attr.getLocalName(1) + "=\"" + attr.getValue(1) + "\"";
    noCertificadoSAT = attr.getLocalName(2) + "=\"" + attr.getValue(2) + "\"";
    selloCFD = attr.getLocalName(3) + "=\"" + attr.getValue(3) + "\"";
    selloSAT = attr.getLocalName(4) + "=\"" + attr.getValue(4) + "\"";
    version = attr.getLocalName(5) + "=\"" + attr.getValue(5) + "\"";

    // Obtinene los atributos del archivo timbrado y los guarda en la variable
    Fechatimbrado_atributo = attr.getValue(0);
    uuid_atributo = attr.getValue(1);
    noCertificadoSAT_atributo = attr.getValue(2);
    selloCFD_atributo = attr.getValue(3);
    selloSAT_atributo = attr.getValue(4);
    version_atributo = attr.getValue(5);
  }

  public static void timbra(String ruta, String NumFac) throws IOException {

    /*
     * //Eliminar la ultima linea porque se va a recorrer al final </cfdi:Comprobante> y asi poder
     * meter los datos que se reciberon del PAC Vector lineas=new Vector(); FileReader contarlineas
     * = new FileReader (ruta+NumFac+".xml"); //Abre el archivo de la factura original para leer
     * BufferedReader br = new BufferedReader(contarlineas); String linea; int cont=0;
     * while((linea=br.readLine())!=null){ //Cuenta cuantas lineas tiene el archivo y las guarda en
     * el contador cont++; } br.close(); //Se cierra el archivo
     * 
     * 
     * FileReader leerarchivo = new FileReader (ruta+NumFac+".xml"); //Abre el archivo de la factura
     * original para escribir BufferedReader la = new BufferedReader(leerarchivo); int n=cont; //n
     * es igual al numero de lineas que tiene la factura original cont=0;
     * while((linea=la.readLine())!=null){ //Se recorre el archivo leyendo las lineas cont++;
     * if(cont!=n) //Si el contador es diferente a la linea, se agrega la linea al arreglo de
     * elementos lineas.addElement(linea); } la.close(); //Se cierra el archivo
     * 
     * 
     * FileWriter fichero = new FileWriter(ruta+NumFac+".xml"); //Abre el archivo de la factura
     * original para escribir PrintWriter escribe = new PrintWriter(fichero); for(int
     * i=0;i<lineas.size();i++) escribe.println(lineas.elementAt(i)); //Escribe las lienas que tenia
     * en el arreglo de elementos fichero.close();//Se cierra el archivo //Termina: Eliminar la
     * ultima linea porque se va a recorrer al final </cfdi:Comprobante>
     */

    try {
      /*
       * String namespaceURI; String localName; String qName; Attributes attr;
       * 
       * // Create SAX 2 parser... XMLReader xr = XMLReaderFactory.createXMLReader();
       * 
       * // Set the ContentHandler... xr.setContentHandler(new extraeTimbrado());
       * 
       * // Parse the file... xr.parse(new InputSource(new
       * FileReader(ruta+"Timbrado"+NumFac+".xml")));
       */

      /*
       * OutputStreamWriter escribetimbrado = new OutputStreamWriter(new
       * FileOutputStream(ruta+NumFac+".xml", true), "UTF-8");//Abre el archivo de Factura Original
       * para escribir los datos del timbrado BufferedWriter entradatimbrado = new
       * BufferedWriter(escribetimbrado); //Se agrega el codigo xml del timbrado a la factura
       * original entradatimbrado.write("    <cfdi:Complemento>\n");
       * entradatimbrado.write("    <tfd:TimbreFiscalDigital\n");
       * entradatimbrado.write("    "+selloSAT+"\n");
       * entradatimbrado.write("    "+noCertificadoSAT+"\n");
       * entradatimbrado.write("    "+selloCFD+"\n"); //El sello ya aparece antes con el nombre
       * sello en la factura original entradatimbrado.write("    "+Fechatimbrado);
       * entradatimbrado.write("  "+uuid); entradatimbrado.write("  "+version+"/>\n");
       * entradatimbrado.write("    </cfdi:Complemento>\n");
       * entradatimbrado.write("</cfdi:Comprobante>\n"); entradatimbrado.close(); //Termian Se
       * agrega el codigo xml del timbrado a la factura original
       */

      CadenaOriginalTimbrado = "ǀǀ" + version_atributo + "ǀ" + uuid_atributo + "ǀ"
          + Fechatimbrado_atributo + "ǀ" + selloSAT_atributo + "ǀ" + noCertificadoSAT_atributo
          + "ǀǀ";

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public String get_selloSAT() {
    return this.selloSAT;
  }

  public String get_noCertificadoSAT() {
    return this.noCertificadoSAT;
  }

  public String get_selloCFD() {
    return this.selloCFD;
  }

  public String get_Fechatimbrado() {
    return this.Fechatimbrado;
  }

  public String get_uuid() {
    return this.uuid;
  }

  public String get_version() {
    return this.version;
  }

  public String get_CadenaOriginalTimbrado() {
    return this.CadenaOriginalTimbrado;
  }

}