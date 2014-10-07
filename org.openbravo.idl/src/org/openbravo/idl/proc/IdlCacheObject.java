/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

/**
 * @author guilleaer
 * 
 *         This class is used to store a DB request parameters using dal as key of hashmap. The
 *         value associated to this key will be the object which DAL returns
 * 
 *         Then a hashMap could be used as a cache.
 */

public class IdlCacheObject {
  Value[] values;
  String classType;

  private volatile int hashCode = 0;

  public IdlCacheObject(Value[] vals, String classType) {
    this.values = vals;
    this.classType = classType;
  }

  public String getClassType() {
    return this.classType;
  }

  public Value[] getValues() {
    return values;
  }

  @Override
  public boolean equals(Object obj) {
    boolean isEqual = false;
    try {
      IdlCacheObject curObj = (IdlCacheObject) obj;
      if (curObj.getValues() == null && this.getValues() == null) {
        isEqual = true;
      } else {
        if (curObj.getValues() == null || this.getValues() == null) {
          return false;
        } else if (curObj.getValues().length == this.getValues().length) {
          if (curObj.getValues().length > 0) {
            for (int i = 0; i < curObj.getValues().length; i++) {
              if (this.getValues()[i] == null && curObj.getValues()[i] == null) {
                isEqual = true;
              } else {
                if (this.getValues()[i].getField() == null
                    && curObj.getValues()[i].getField() == null) {
                  isEqual = true;
                } else {
                  if (this.getValues()[i].getField().equals(curObj.getValues()[i].getField())) {
                    isEqual = true;
                  } else {
                    return false;
                  }
                }
                if (isEqual) {
                  if (this.getValues()[i].getValue() == null
                      && curObj.getValues()[i].getValue() == null) {
                    isEqual = true;
                  } else {
                    if (this.getValues()[i].getValue().equals(curObj.getValues()[i].getValue())) {
                      isEqual = true;
                    } else {
                      return false;
                    }
                  }
                }
              }
            }
          }
        }
      }
      if (isEqual) {
        if (this.getClassType().toString().equals(curObj.getClassType().toString())) {
          isEqual = true;
          return isEqual;
        }
      } else {
        return false;
      }
      return isEqual;
    } catch (ClassCastException e) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    final int multiplier = 23;
    if (hashCode == 0) {
      int code = 133;
      if (this.getValues().length > 0) {
        for (int i = 0; i < this.getValues().length; i++) {
          code = multiplier * code + this.getValues()[i].toString().hashCode();
        }
      } else {
        code = "nullField - nullValue".toString().hashCode();
      }
      code = multiplier * code + this.classType.hashCode();
      hashCode = code;
    }
    return hashCode;
  }

  @Override
  public String toString() {
    String strReturn = "";
    if (this.getValues().length > 0) {
      for (int i = 0; i < this.getValues().length; i++) {
        strReturn += i + "- " + this.getValues()[i].toString() + " ";
      }
    } else {
      strReturn += "nullField - nullValue ".toString();
    }
    strReturn += " - " + this.getClassType();
    return strReturn;
  }

}
