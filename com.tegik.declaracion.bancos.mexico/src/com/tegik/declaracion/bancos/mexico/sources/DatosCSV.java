package com.tegik.declaracion.bancos.mexico.sources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class DatosCSV {

  public static int inicio_fila = 1;
  public static String separador = ",";
  private static final Logger log = Logger.getLogger(DatosCSV.class);

  public List<String[]> rows = new ArrayList<String[]>();

  public DatosCSV() {

  }

  public void init(InputStream in) {

    int contFila = 1;

    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String strLine;
      while ((strLine = br.readLine()) != null) {
        if (contFila >= inicio_fila) {
          log.info("fila:" + contFila);
          strLine = removeDoubleComma(strLine);
          strLine = removeManySpaces(strLine);
          strLine = strLine.trim();
          rows.add(strLine.split(separador));

        }
        contFila++;
      }

      in.close();
    } catch (Exception e) {
      log.info(e.getMessage());
    }
  }

  public boolean isEmplyFile() {
    return rows.isEmpty();
  }

  public boolean isEmptyData(int row, int column) {
    String cols[] = rows.get(row);
    if (cols[column] == null || cols[column].equals("")) {
      return true;
    }
    return false;
  }

  public String getColumn(int row, int column) {
    String col[] = rows.get(row);
    return col[column];
  }

  public String[] getRow(int row) {
    return rows.get(row);
  }

  public int sizeRow() {
    return rows.size();
  }

  public int sizeColumns(int row) {
    String data[] = rows.get(row);
    return data.length;
  }

  public String removeDoubleComma(String readline) {
    StringBuilder sb = new StringBuilder(readline);
    int inicio = sb.indexOf("\"");
    if (inicio < 0) {
      return readline;
    }
    int fin = sb.substring(inicio + 1).indexOf("\"");
    int comma = sb.substring(inicio + 1).indexOf(",");
    if (comma < 0) {

      sb.deleteCharAt(inicio);
      sb.deleteCharAt(inicio + fin);

      return removeDoubleComma(sb.toString());

    } else if (inicio + comma < inicio + fin) {

      String begin = readline.substring(0, inicio);
      String last = readline.substring(inicio + fin + 2);
      String borrar = readline.substring(inicio + 1, inicio + fin + 1);

      borrar = borrar.replace(",", "");
      String replacement = begin + borrar + last;

      return removeDoubleComma(replacement);

    } else if (inicio + comma > inicio + fin) {
      sb.deleteCharAt(inicio);
      sb.deleteCharAt(inicio + fin);
      return removeDoubleComma(sb.toString());
    } else {
      return sb.toString();
    }

  }

  public String removeManySpaces(String dato) {
    int repeat = 0;
    String temp = "";

    for (int i = 0; i < dato.length(); i++) {
      if (repeat == 0 && (int) dato.charAt(i) == 32) {
        temp = temp + dato.charAt(i);
        repeat++;
      } else if ((int) dato.charAt(i) != 32) {
        temp = temp + dato.charAt(i);
        repeat = 0;
      } else {
        repeat++;
      }
    }
    return temp;
  }

}
