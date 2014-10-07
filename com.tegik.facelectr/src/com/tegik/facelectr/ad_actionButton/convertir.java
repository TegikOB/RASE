package com.tegik.facelectr.ad_actionButton;

/**
 * @author Gabriel Mtz.
 */

public class convertir {

  private static int _counter = 0;

  public static String Numeroaletra(double num, String nombreMoneda, String codigoMoneda) {
    String enteros = doThings(num);
    String montoEnLetras = Double.toString(num);
    String[] nums = montoEnLetras.split("\\.");
    if (nums[1].equals("0"))
      return enteros + " " + nombreMoneda + " 00/100 " + codigoMoneda;
    else if (nums[1].length() == 1)
      return enteros + " " + nombreMoneda + " " + nums[1].substring(0, 1) + "0/100 " + codigoMoneda;
    else
      return enteros + " " + nombreMoneda + " " + nums[1].substring(0, 2) + "/100 " + codigoMoneda;
  }

  private static String doThings(double _counter) {
    // Limite
    if (_counter > 10000000)
      return "";
    int cont = (int) _counter;
    switch (cont) {
    case 0:
      return "CERO";
    case 1:
      return "UN"; // UNO
    case 2:
      return "DOS";
    case 3:
      return "TRES";
    case 4:
      return "CUATRO";
    case 5:
      return "CINCO";
    case 6:
      return "SEIS";
    case 7:
      return "SIETE";
    case 8:
      return "OCHO";
    case 9:
      return "NUEVE";
    case 10:
      return "DIEZ";
    case 11:
      return "ONCE";
    case 12:
      return "DOCE";
    case 13:
      return "TRECE";
    case 14:
      return "CATORCE";
    case 15:
      return "QUINCE";
    case 20:
      return "VEINTE";
    case 30:
      return "TREINTA";
    case 40:
      return "CUARENTA";
    case 50:
      return "CINCUENTA";
    case 60:
      return "SESENTA";
    case 70:
      return "SETENTA";
    case 80:
      return "OCHENTA";
    case 90:
      return "NOVENTA";
    case 100:
      return "CIEN";
    case 200:
      return "DOSCIENTOS";
    case 300:
      return "TRESCIENTOS";
    case 400:
      return "CUATROCIENTOS";
    case 500:
      return "QUINIENTOS";
    case 600:
      return "SEISCIENTOS";
    case 700:
      return "SETECIENTOS";
    case 800:
      return "OCHOCIENTOS";
    case 900:
      return "NOVECIENTOS";
    case 1000:
      return "MIL";
    case 1000000:
      return "UN MILLON";
    case 2000000:
      return "DOS MILLONES";
    }
    if (_counter < 20) {
      // System.out.println(">15");
      return "DIECI" + doThings(_counter - 10);
    }
    if (_counter < 30) {
      // System.out.println(">20");
      return "VEINTI" + doThings(_counter - 20);
    }
    if (_counter < 100) {
      // System.out.println("<100");
      return doThings((int) (_counter / 10) * 10) + " Y " + doThings(_counter % 10);
    }
    if (_counter < 200) {
      // System.out.println("<200");
      return "CIENTO " + doThings(_counter - 100);
    }
    if (_counter < 1000) {
      // System.out.println("<1000");
      return doThings((int) (_counter / 100) * 100) + " " + doThings(_counter % 100);
    }
    if (_counter < 2000) {
      // System.out.println("<2000");
      return "MIL " + doThings(_counter % 1000);
    }
    if (_counter < 1000000) {
      String var = "";
      // System.out.println("<1000000");
      var = doThings((int) (_counter / 1000)) + " MIL";
      if (_counter % 1000 != 0) {
        // System.out.println(var);
        var += " " + doThings(_counter % 1000);
      }
      return var;
    }
    if (_counter < 2000000) {
      return "UN MILLON " + doThings(_counter % 1000000);
    }
    if (_counter < 3000000) {
      return "DOS MILLONES " + doThings(_counter % 1000000);
    }
    if (_counter < 4000000) {
      return "TRES MILLONES " + doThings(_counter % 1000000);
    }
    if (_counter < 5000000) {
      return "CUATRO MILLONES " + doThings(_counter % 1000000);
    }
    if (_counter < 6000000) {
      return "CINCO MILLONES " + doThings(_counter % 1000000);
    }
    if (_counter < 7000000) {
      return "SEIS MILLONES " + doThings(_counter % 1000000);
    }
    if (_counter < 8000000) {
      return "SIETE MILLONES " + doThings(_counter % 1000000);
    }
    if (_counter < 9000000) {
      return "OCHO MILLONES " + doThings(_counter % 1000000);
    }
    if (_counter < 10000000) {
      return "NUEVE MILLONES " + doThings(_counter % 1000000);
    }

    // return "";
    return "Error en la conversion.";
  }

}
