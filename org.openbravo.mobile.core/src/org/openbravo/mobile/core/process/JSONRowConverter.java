/*
 ************************************************************************************
 * Copyright (C) 2012-2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.process;

import java.io.IOException;
import java.io.Writer;
import java.sql.BatchUpdateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

/**
 * A JSON objects converter.
 * 
 * @author adrianromero
 */
public class JSONRowConverter {

  private static final Logger log = Logger.getLogger(JSONRowConverter.class);

  private final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
      DataToJsonConverter.class);
  private SimpleDateFormat xmlDateFormat = JsonUtils.createDateFormat();
  private SimpleDateFormat xmlDateTimeFormat = JsonUtils.createDateTimeFormat();
  private final static SimpleDateFormat xmlTimeFormat = JsonUtils.createTimeFormat();
  private String[] fields;
  private DataResolvingMode mode;

  public JSONRowConverter(String[] fields, DataResolvingMode mode) {
    this.fields = fields;
    this.mode = mode;
  }

  public JSONRowConverter(String[] fields) {
    this.fields = fields;
    this.mode = DataResolvingMode.FULL;
  }

  public JSONRowConverter(DataResolvingMode mode) {
    this.fields = new String[0];
    this.mode = mode;
  }

  public JSONRowConverter() {
    this.fields = new String[0];
    this.mode = DataResolvingMode.FULL;
  }

  public Object convert(Object obj) throws JSONException {
    return convert(fields, obj);
  }

  private Object convert(String[] fi, Object obj) throws JSONException {

    if (obj instanceof BaseOBObject) {
      JSONObject jsonobj = toJsonConverter.toJsonObject((BaseOBObject) obj, mode);
      // Performance improvement. These json fields are not needed by WebPOS and for instance no
      // need to send.
      jsonobj.remove("_entityName");
      jsonobj.remove("$ref");
      jsonobj.remove("active");
      jsonobj.remove("created");
      jsonobj.remove("createdBy");
      jsonobj.remove("createdBy" + DalUtil.FIELDSEPARATOR + "_identifier");
      jsonobj.remove("updated");
      jsonobj.remove("updatedBy");
      jsonobj.remove("updatedBy" + DalUtil.FIELDSEPARATOR + "_identifier");
      jsonobj.remove("recordTime");
      return jsonobj;
    } else if (obj instanceof Object[]) {
      if (fi == null) {
        JSONArray row = new JSONArray();
        for (Object o : (Object[]) obj) {
          row.put(convert(null, o));
        }
        return row;
      } else {
        JSONObject item = new JSONObject();
        for (int i = 0; i < fields.length; i++) {
          item.put(fields[i], convert(null, ((Object[]) obj)[i]));
        }
        return item;
      }
    } else {
      if (fi == null || fi.length == 0) {
        return convertPrimitiveValue(obj);
      } else {
        JSONObject item = new JSONObject();
        item.put(fi[0], convertPrimitiveValue(obj));
        return item;
      }
    }
  }

  private Object convertPrimitiveValue(Object value) {
    if (value == null) {
      return JSONObject.NULL;
    } else if (Date.class.isAssignableFrom(value.getClass())) {
      if (value instanceof java.sql.Timestamp) {
        final String formattedValue = xmlDateTimeFormat.format(value);
        return JsonUtils.convertToCorrectXSDFormat(formattedValue);
      } else if (value instanceof java.sql.Time) {
        final String formattedValue = xmlTimeFormat.format(value);
        return JsonUtils.convertToCorrectXSDFormat(formattedValue);
      } else if (value instanceof java.sql.Date) {
        return xmlDateFormat.format(value);
      } else {
        // Timestamp formating by default
        final String formattedValue = xmlDateTimeFormat.format(value);
        return JsonUtils.convertToCorrectXSDFormat(formattedValue);
      }
    } else if (value instanceof byte[]) {
      return Base64.encodeBase64String((byte[]) value);
    } else {
      if (value.equals("[[null]]")) {
        return JSONObject.NULL;
      }
      return value;
    }
  }

  public static int buildResponse(Writer w, Scroll listdata, String[] aliases, boolean firstQuery,
      String dateFormat) throws IOException, JSONException {
    final JSONRowConverter converter = new JSONRowConverter(aliases,
        DataResolvingMode.FULL_TRANSLATABLE);

    if (!StringUtils.isEmpty(dateFormat)) {
      converter.xmlDateFormat = new SimpleDateFormat(dateFormat);
      converter.xmlDateFormat.setLenient(true);

      converter.xmlDateTimeFormat = new SimpleDateFormat(dateFormat + "'T'HH:mm:ssZZZZZ");
      converter.xmlDateTimeFormat.setLenient(true);
    }

    int rows = 0;
    try {
      while (listdata.next()) {
        if (rows == 0 && !firstQuery) {
          w.write(",");
        }
        if (rows > 0) {
          w.write(',');
        }
        w.write(converter.convert(listdata.get()).toString());
        rows++;
      }
    } finally {
      listdata.close();
    }
    return rows;
  }

  public static int buildResponse(Writer w, Scroll listdata, String[] aliases, boolean firstQuery)
      throws IOException, JSONException {
    return buildResponse(w, listdata, aliases, firstQuery, null);
  }

  public static int buildResponse(Writer w, List<?> listdata, String[] aliases, boolean firstQuery)
      throws IOException, JSONException {

    final JSONRowConverter converter = new JSONRowConverter(aliases);

    int rows = 0;
    for (Object element : listdata) {
      if (rows == 0 && !firstQuery) {
        w.write(",");
      }
      if (rows > 0) {
        w.write(',');
      }
      w.write(converter.convert(element).toString());
      rows++;
    }
    return rows;
  }

  public static void startResponse(Writer w) throws IOException {
    w.write("\"data\":[");
  }

  public static void endResponse(Writer w, int rows) throws IOException {
    final int startRow = 0;
    w.write("],");
    // Add success fields
    w.write("\"");
    w.write(JsonConstants.RESPONSE_STARTROW);
    w.write("\":");
    w.write(Integer.toString(startRow));
    w.write(",\"");
    w.write(JsonConstants.RESPONSE_ENDROW);
    w.write("\":");
    w.write(Integer.toString(rows > 0 ? rows + startRow - 1 : 0));
    w.write(",\"");
    if (rows == 0) {
      w.write(JsonConstants.RESPONSE_TOTALROWS);
      w.write("\":0,\"");
    }
    w.write(JsonConstants.RESPONSE_STATUS);
    w.write("\":");
    w.write(Integer.toString(JsonConstants.RPCREQUEST_STATUS_SUCCESS));
    w.write(",\"lastUpdated\":" + (new Date()).getTime());
  }

  public static void addJSONExceptionFields(Writer w, Throwable throwable) {
    Throwable localThrowable = throwable;
    log.error("Error in JSON process", throwable);
    if (throwable.getCause() instanceof BatchUpdateException) {
      final BatchUpdateException batchException = (BatchUpdateException) throwable.getCause();
      localThrowable = batchException.getNextException();
    }

    JSONObject objException = new JSONObject();
    try {
      objException.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_FAILURE);

      final VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      final OBError obError = Utility.translateError(new DalConnectionProvider(), vars, OBContext
          .getOBContext().getLanguage().getLanguage(), localThrowable.getMessage());
      JSONObject errorObj = new JSONObject();

      if (localThrowable instanceof OBSecurityException) {
        errorObj.put("message", "OBUIAPP_ActionNotAllowed");
        errorObj.put("type", "OBUIAPP_ActionNotAllowed");
      } else if (obError != null) {
        errorObj.put("message", obError.getMessage());
        errorObj.put("title", StringEscapeUtils.escapeJavaScript(obError.getTitle()));
        errorObj.put("className", localThrowable.getClass().getName());
      } else {
        errorObj.put("message", StringEscapeUtils.escapeJavaScript(localThrowable.getMessage()));
        errorObj.put("messageType", "Exception");
        errorObj.put("className", localThrowable.getClass().getName());
      }
      objException.put(JsonConstants.RESPONSE_ERROR, errorObj);
      objException.put(JsonConstants.RESPONSE_TOTALROWS, 0);
      w.write(objException.toString().substring(1, objException.toString().length() - 1));

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
