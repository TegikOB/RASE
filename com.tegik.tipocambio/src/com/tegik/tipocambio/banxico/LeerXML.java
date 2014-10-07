package com.tegik.tipocambio.banxico;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.tegik.tipocambio.banxico.sources.Convertidor;
import com.tegik.tipocambio.banxico.sources.Moneda;

public class LeerXML {

  private static final Logger log = Logger.getLogger(LeerXML.class);

  public static Convertidor obtenerConvertidordeXML(String xml) {

    Convertidor conversion = new Convertidor();

    try {

      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xml));

      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(is);

      doc.getDocumentElement().normalize();

      NodeList listaSeries = doc.getElementsByTagName("bm:Series");

      for (int temp = 0; temp < listaSeries.getLength(); temp++) {

        Node PrimerNodoSerie = listaSeries.item(temp);

        if (PrimerNodoSerie.getNodeType() == Node.ELEMENT_NODE) {

          Element primerElemento = (Element) PrimerNodoSerie;
          String idserie = primerElemento.getAttribute("IDSERIE");

          NodeList obsList = primerElemento.getElementsByTagName("bm:Obs");
          Element obs = (Element) obsList.item(0);

          Moneda equi = new Moneda();

          // Obtener la fecha
          String fecha = obs.getAttribute("TIME_PERIOD");
          SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd");
          Date date = sdfSource.parse(fecha);
          equi.setFecha(date);

          // Obtener la cantidad de moneda
          String cantidad = obs.getAttribute("OBS_VALUE");
          if (!(cantidad.equals("N/A"))) {
            equi.setCantidad(new BigDecimal(cantidad));
          }

          // Obtener el tipo de cambio
          if (idserie.equals("SF60653")) {
            log.info("ENTRO AL CODIGO");
            equi.setMoneda("USD");
            conversion.agregarLista("USD", equi);
          } else if (idserie.equals("SF43718")) {
            // Pesos por dólar E.U.A. Tipo de cambio para solventar obligaciones denominadas en
            // moneda extranjera Fecha de determinación (FIX)
            // equi.setMoneda("USD");
            // conversion.agregarLista(equi);
          } else if (idserie.equals("SF46410")) {
            equi.setMoneda("EUR");
            conversion.agregarLista("EUR", equi);
          } else if (idserie.equals("SF60632")) {
            equi.setMoneda("CAD");
            conversion.agregarLista("CAD", equi);
          } else if (idserie.equals("SF46406")) {
            equi.setMoneda("JPY");
            conversion.agregarLista("JPY", equi);
          } else if (idserie.equals("SF46407")) {
            equi.setMoneda("GBP");
            conversion.agregarLista("GBP", equi);
          }

        }

      }

    } catch (Exception e) {
      log.info(e.getMessage());
      ;
    }

    return conversion;
  }
}
