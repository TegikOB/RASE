package com.tegik.facelectr.ad_actionButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

import org.hibernate.criterion.Expression;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.invoice.Invoice;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.tegik.facelectr.data.fet_facturaadendalinea;
import com.tegik.facelectr.data.fetfacturaadenda;

public class addenda extends DefaultHandler {

  public static String agregaAddenda(String strInvoiceId, String ruta, String NumFac) {

    try {

      // Eliminar la ultima linea porque se va a recorrer al final </cfdi:Comprobante> y asi poder
      // meter los datos que se reciberon del PAC
      // cuenta las lineas del archivo
      Vector lineas = new Vector();
      FileReader contarlineas = new FileReader(ruta + NumFac + ".xml"); // Abre el archivo de la
                                                                        // factura original para
                                                                        // leer
      BufferedReader br = new BufferedReader(contarlineas);
      String linea;
      int cont = 0;
      while ((linea = br.readLine()) != null) { // Cuenta cuantas lineas tiene el archivo y las
                                                // guarda en el contador
        cont++;
      }
      br.close(); // Se cierra el archivo

      // elimina la ultima linea del archivo
      FileReader leerarchivo = new FileReader(ruta + NumFac + ".xml"); // Abre el archivo de la
                                                                       // factura original para
                                                                       // escribir
      BufferedReader la = new BufferedReader(leerarchivo);
      int n = cont; // n es igual al numero de lineas que tiene la factura original
      cont = 0;
      while ((linea = la.readLine()) != null) { // Se recorre el archivo leyendo las lineas
        cont++;
        if (cont != n) // Si el contador es diferente a la linea, se agrega la linea al arreglo de
                       // elementos
          lineas.addElement(linea);
      }
      la.close(); // Se cierra el archivo

      FileWriter fichero = new FileWriter(ruta + NumFac + ".xml"); // Abre el archivo de la factura
                                                                   // original para escribir
      PrintWriter escribe = new PrintWriter(fichero);
      for (int i = 0; i < lineas.size(); i++)
        escribe.println(lineas.elementAt(i)); // Escribe las lienas que tenia en el arreglo de
                                              // elementos
      fichero.close();// Se cierra el archivo
      // Termina: Eliminar la ultima linea porque se va a recorrer al final </cfdi:Comprobante>

      // Create SAX 2 parser...
      XMLReader xr = XMLReaderFactory.createXMLReader();

      // Set the ContentHandler...
      xr.setContentHandler(new addenda());

      // Parse the file...
      // xr.parse(new InputSource(new FileReader(ruta+"Timbrado"+NumFac+".xml")));

      OutputStreamWriter escribetimbrado = new OutputStreamWriter(new FileOutputStream(ruta
          + NumFac + ".xml", true), "UTF-8");// Abre el archivo de Factura Original para escribir
                                             // los datos del timbrado
      BufferedWriter entradatimbrado = new BufferedWriter(escribetimbrado);
      // Se agrega el codigo xml del timbrado a la factura original
      entradatimbrado.write("      <cfdi:Addenda> \n");
      entradatimbrado.write("         <Documento> \n");

      Invoice factura = OBDal.getInstance().get(Invoice.class, strInvoiceId);
      for (fetfacturaadenda lineafac : factura.getFetFacturaadendaList()) {
        entradatimbrado.write("             <" + lineafac.getNombre() + ">\n");
        if (lineafac.getFetFacturaadendalineaList() != null) {
          // fetfacturaadenda addenda =
          // OBDal.getInstance().get(fetfacturaadenda.class,strInvoiceId);
          final OBCriteria<fet_facturaadendalinea> adendaLineaList = OBDal.getInstance()
              .createCriteria(fet_facturaadendalinea.class);
          adendaLineaList.add(Expression.eq(fet_facturaadendalinea.PROPERTY_FETFACTURAADENDA,
              lineafac));
          adendaLineaList.addOrderBy(fet_facturaadendalinea.PROPERTY_NUMERO, true);

          for (fet_facturaadendalinea addendalinea : adendaLineaList.list()) {
            if (addendalinea.getValor() != null) {
              if (!addendalinea.getValor().equals("")) {
                entradatimbrado.write("                <" + addendalinea.getNombre() + ">"
                    + addendalinea.getValor() + "</" + addendalinea.getNombre() + ">\n");
              } else {
                entradatimbrado.write("                <" + addendalinea.getNombre() + "/>\n");
              }
            } else {
              entradatimbrado.write("                <" + addendalinea.getNombre() + "/>\n");
            }

          }
        }
        entradatimbrado.write("             </" + lineafac.getNombre() + ">\n");
      }

      entradatimbrado.write("         </Documento>\n");
      entradatimbrado.write("      </cfdi:Addenda> \n");
      entradatimbrado.write("</cfdi:Comprobante>\n");
      entradatimbrado.close();
      // Termian Se agrega el codigo xml del timbrado a la factura original

    } catch (Exception e) {
      e.printStackTrace();
    }

    return "OK";
  }

}