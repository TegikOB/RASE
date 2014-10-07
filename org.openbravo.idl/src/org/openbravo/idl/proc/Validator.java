/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

import org.openbravo.model.common.enterprise.Organization;

/**
 * 
 * @author adrian
 */
public class Validator {

  private IdlService service;
  private String entity;

  private String errorCode;
  private String errorMessage;

  public Validator(IdlService service, String entity) {
    this.service = service;
    this.entity = entity;
    errorCode = "0";
    errorMessage = "";
  }

  public String checkNotNull(String value) {
    return checkNotNull(value, "<NULL>");
  }

  public String checkNotNull(String value, String fieldName) {
    if (value == null) {
      setErrorMessage(service.Utility_messageBD("IDL_CANNOT_BE_NULL") + fieldName);
    }
    return value;
  }

  public String checkOrganization(String value) {
    if (value == null) {
      setErrorMessage(service.Utility_messageBD("IDL_BAD_CSV"));
      return value;
    } else {
      Organization org = service.checkOrganization(entity, value);
      if (org == null) {
        setErrorMessage(service.Utility_messageBD("IDL_NO_ORGANIZATION") + value);
        return value;
      } else {
        return org.getSearchKey();
      }
    }
  }

  public String checkTransactionalOrganization(String value) {
    if (value == null) {
      setErrorMessage(service.Utility_messageBD("IDL_BAD_CSV"));
      return value;
    } else {
      Organization org = service.checkTransactionalOrganization(entity, value);
      if (org == null) {
        setErrorMessage(service.Utility_messageBD("IDL_NO_ORGANIZATION") + value);
        return value;
      } else {
        return org.getSearchKey();
      }
    }
  }

  public String checkString(String value) {
    return checkString(value, 0, null);
  }

  public String checkString(String value, int maxlen) {
    return checkString(value, maxlen, null);
  }

  public String checkString(String value, int maxlen, String defFieldName) {
    String def = null;

    if (value == null) {
      setErrorMessage(service.Utility_messageBD("IDL_BAD_CSV"));
    } else {
      def = service.searchDefaultValue(this.entity, defFieldName, value);
      if (def != null) {
        if (def.length() > maxlen && def.length() > 0) {
          setErrorMessage(service.Utility_messageBD("IDL_LENGTH_MAX") + def);
        }
      }
    }

    return def;
  }

  public String checkDate(String value) {
    return checkDate(value, null);
  }

  public String checkDate(String value, String defFieldName) {
    String def = null;
    if (value == null) {
      setErrorMessage(service.Utility_messageBD("IDL_BAD_CSV"));
    } else {
      def = service.searchDefaultValue(this.entity, defFieldName, value);
      if (def != null) {
        try {
          Parameter.DATE.parse(def);
        } catch (Exception e) {
          setErrorMessage(service.Utility_messageBD("IDL_BAD_DATE") + def);
        }
      }
    }
    return def;
  }

  public String checkBoolean(String value) {
    return checkBoolean(value, null);
  }

  public String checkBoolean(String value, String defFieldName) {
    String def = null;
    if (value == null) {
      setErrorMessage(service.Utility_messageBD("IDL_BAD_CSV"));
    } else {
      def = service.searchDefaultValue(this.entity, defFieldName, value);
      if (def != null) {
        try {
          Parameter.BOOLEAN.parse(def);
        } catch (Exception e) {
          setErrorMessage(service.Utility_messageBD("IDL_BAD_BOOLEAN") + def);
        }
      }
    }
    return def;
  }

  public String checkLong(String value) {
    return checkLong(value, null);
  }

  public String checkLong(String value, String defFieldName) {
    String def = null;
    if (value == null) {
      setErrorMessage(service.Utility_messageBD("IDL_BAD_CSV"));
    } else {
      def = service.searchDefaultValue(this.entity, defFieldName, value);
      if (def != null) {
        try {
          Parameter.LONG.parse(def);
        } catch (Exception e) {
          setErrorMessage(service.Utility_messageBD("IDL_BAD_NUMBER") + def);
        }
      }
    }
    return def;
  }

//  public String checkFloat(String value) {
//    return checkFloat(value, null);
//  }

//  public String checkFloat(String value, String defFieldName) {
//    String def = null;
//    if (value == null) {
//      setErrorMessage(service.Utility_messageBD("IDL_BAD_CSV"));
//    } else {
//      def = service.searchDefaultValue(this.entity, defFieldName, value);
//      if (def != null) {
//        try {
//          Parameter.FLOAT.parse(def);
//        } catch (Exception e) {
//          setErrorMessage(service.Utility_messageBD("IDL_BAD_NUMBER") + def);
//        }
//      }
//    }
//    return def;
//  }

  public String checkBigDecimal(String value) {
    return checkBigDecimal(value, null);
  }

  public String checkBigDecimal(String value, String defFieldName) {
    String def = null;
    if (value == null) {
      setErrorMessage(service.Utility_messageBD("IDL_BAD_CSV"));
    } else {
      def = service.searchDefaultValue(this.entity, defFieldName, value);
      if (def != null) {
        try {
          Parameter.BIGDECIMAL.parse(def);
        } catch (Exception e) {
          setErrorMessage(service.Utility_messageBD("IDL_BAD_NUMBER") + def);
        }
      }
    }
    return def;
  }

  private void setErrorMessage(String msg) {
    errorCode = "-1";
    errorMessage += msg + "\n";
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
