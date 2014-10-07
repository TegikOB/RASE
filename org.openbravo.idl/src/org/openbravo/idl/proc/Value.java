/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

public class Value {
  private String field;
  private Object value;

  public Value(String field, Object value) {
    this.field = field;
    this.value = value;
  }

  public String getField() {
    return field;
  }

  public Object getValue() {
    return value;
  }

  public String toString() {
    try {
      String finalStr = "";
      if (this.getField() != null) {
        finalStr = this.getField().toString();
      } else {
        finalStr = "nullField";
      }
      finalStr += " - ";
      if (this.getValue() != null) {
        finalStr += this.getValue().toString();
      } else {
        finalStr += "nullValue";
      }
      return finalStr;
    } catch (Exception e) {
      return "nullField - nullValue";
    }
  }
}