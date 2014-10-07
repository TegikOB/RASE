package com.tegik.declaracion.bancos.mexico.utilities;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.tegik.declaracion.bancos.mexico.BanMexColumnas;
import com.tegik.declaracion.bancos.mexico.BanMexConfiguracion;
import com.tegik.declaracion.bancos.mexico.sources.DatosCSV;
import com.tegik.declaracion.bancos.mexico.sources.DeclaracionBancos;

public class Utilidad {
  private static final Logger log = Logger.getLogger(Utilidad.class);

  public static long fila_inicia;
  public static String separador;
  public static List<BanMexColumnas> columnas;

  public static List<DeclaracionBancos> revisarArchivo(InputStream in, BanMexConfiguracion config)
      throws ExceptionConfig, ExceptionFile, ExceptionConvertObject {

    columnas = config.getBanMexColumnasList();

    if (columnas.isEmpty()) {
      throw new ExceptionConfig("BANMEX_NoDataColumnas");
    }

    List<DeclaracionBancos> listaDeclaraciones = new ArrayList<DeclaracionBancos>();

    DatosCSV datos = new DatosCSV();
    datos.separador = (config.getSeparador() == null ? "," : config.getSeparador());
    datos.inicio_fila = config.getInicioFila().intValue();
    datos.init(in);

    if (datos.isEmplyFile()) {
      throw new ExceptionFile("BANMEX_EmptyFile");
    }

    long linea = fila_inicia;

    for (int rows = 0; rows < datos.sizeRow(); rows++) {
      linea++;
      DeclaracionBancos declBancos = new DeclaracionBancos();

      for (int col = 0; col < datos.sizeColumns(rows); col++) {
        int noConfCol = 0;

        String finalString = datos.getColumn(rows, col);

        while (noConfCol < columnas.size() && !(datos.isEmptyData(rows, col))) {
          BanMexColumnas ConfCol = columnas.get(noConfCol);
          if (ConfCol.getColumna().intValue() == col) {
            if (ConfCol.isBuscar()) {

              finalString = buscar(datos.getColumn(rows, col), ConfCol.getSubfijo(),
                  ConfCol.getPrefijo(), ConfCol.getCantidad().intValue() + 1);

            }// termina buscar

            try {

              Object obj = Convertidor.obtenerObjecto(finalString, ConfCol.getDato(),
                  ConfCol.getFormatoDate());

              if (obj != null) {
                Convertidor.guardarDeclaracion(declBancos, obj, ConfCol.getCorresponde());

              }
            } catch (ExceptionConvertObject excepConvObj) {
              excepConvObj.setLinea(linea);
              throw excepConvObj;
            } catch (ExceptionConfig excepConfig) {
              throw excepConfig;
            }

          } // termina si encontro el config
          finalString = datos.getColumn(rows, col);
          noConfCol++;

        }// ciclo termina config de columnas

      }// termina cols

      listaDeclaraciones.add(declBancos);

    }// termina rows

    return listaDeclaraciones;

  }

  public static String buscar(String dato, String subfijo, String prefijo, int cantidad) {

    int inicio = 0;
    if (subfijo != null) {

      inicio = dato.indexOf(subfijo);
      if (inicio < 0) {
        return "";
      }
      inicio = inicio + subfijo.length();
    }

    if (prefijo != null) {
      int fin = dato.indexOf(prefijo);
      return dato.substring(inicio, fin).trim();

    } else if ((cantidad - 1) > 0) {
      return dato.substring(inicio, inicio + cantidad).trim();
    } else {
      return dato.substring(inicio).trim();

    }
  }

}
