/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author adrian
 */
public class Parameter {

  public static final ParameterType<String> STRING = new ParameterTypeString();
  public static final ParameterType<Date> DATE = new ParameterTypeDate();
  public static final ParameterType<Long> LONG = new ParameterTypeLong();
//  public static final ParameterType<Float> FLOAT = new ParameterTypeFloat();
  public static final ParameterType<Boolean> BOOLEAN = new ParameterTypeBoolean();
  public static final ParameterType<BigDecimal> BIGDECIMAL = new ParameterTypeBigDecimal();

  private String parameter;
  private ParameterType type;

  public Parameter(String parameter, ParameterType type) {
    this.parameter = parameter;
    this.type = type;
  }

  public String getParameter() {
    return parameter;
  }

  public ParameterType getType() {
    return type;
  }

  public static interface ParameterType<T> {
      public T parse(String value) throws Exception;
  }
  
  public static class ParameterTypeString implements ParameterType<String> {
      public String parse(String value) throws Exception {
          return value == null || value.equals("") ? null : value;
      }
  }

  public static class ParameterTypeDate implements ParameterType<Date> {
      private static DateFormat tsf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

      public Date parse(String value) throws Exception {
          if (value == null || value.equals("")) {
              return null;
          } else {

              Date result;
              try {
                  result = tsf.parse(value);
              } catch (ParseException e) {
                  result = df.parse(value);
              }

            if (!df.format(result).equals(value) && !tsf.format(result).equals(value)) {
              throw new Exception("Date not valid");
            }
            return result;
          }
      }
  }

  public static class ParameterTypeLong implements ParameterType<Long> {
      public Long parse(String value) throws Exception {
          if (value == null || value.equals("")) {
              return null;
          } else {
            return Long.parseLong(value);
          }
      }
  }

//  public static class ParameterTypeFloat implements ParameterType<Float> {
//      public Float parse(String value) throws Exception {
//          if (value == null || value.equals("")) {
//              return null;
//          } else {
//            return Float.parseFloat(value);
//          }
//      }
//  }

  public static class ParameterTypeBigDecimal implements ParameterType<BigDecimal> {
      public BigDecimal parse(String value) throws Exception {
          if (value == null || value.equals("")) {
              return null;
          } else {
            return new BigDecimal(value);
          }
      }
  }

  public static class ParameterTypeBoolean implements ParameterType<Boolean> {
      public Boolean parse(String value) throws Exception {
          if (!"TRUE".equalsIgnoreCase(value) &&
              !"YES".equalsIgnoreCase(value) &&
              !"Y".equalsIgnoreCase(value) &&
              !"FALSE".equalsIgnoreCase(value) &&
              !"NO".equalsIgnoreCase(value) &&
              !"N".equalsIgnoreCase(value)
              ) {
              throw new Exception("Boolean not valid");
          }

          return "TRUE".equalsIgnoreCase(value) ||
                 "YES".equalsIgnoreCase(value)||
                 "Y".equalsIgnoreCase(value);
      }
  }
}
