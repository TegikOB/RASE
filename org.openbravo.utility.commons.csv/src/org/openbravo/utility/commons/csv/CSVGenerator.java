/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.utility.commons.csv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.writer.CSVConfig;
import org.apache.commons.csv.writer.CSVField;
import org.apache.commons.csv.writer.CSVWriter;
import org.apache.log4j.Logger;
import org.hibernate.Query;

public class CSVGenerator {
  public static enum BooleanType {
    YES_NO, TRUE_FALSE
  };

  private String filenamePrefix;
  private File directory;
  private static final Logger log4j = Logger.getLogger(CSVGenerator.class);
  private BooleanType boolType = BooleanType.TRUE_FALSE;
  private Map<String, String> aliases = new HashMap<String, String>();

  public CSVGenerator(File directory, String filenamePrefix) {
    this.directory = directory;
    this.filenamePrefix = filenamePrefix;
  }

  public void setBooleanType(BooleanType type) {
    boolType = type;
  }

  public File generateCSV(Query q) throws IOException {
    CSVConfig cf = new CSVConfig();

    List<String> fields = new ArrayList<String>();

    for (String field : q.getReturnAliases()) {
      String alias = aliases.get(field);
      String fieldName;
      if (alias != null) {
        fieldName = alias;
      } else {
        fieldName = field;
      }
      fields.add(fieldName);
      CSVField csvField = new CSVField(fieldName);
      cf.addField(csvField);
    }

    CSVWriter w = new CSVWriter();

    SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd-hhmmss");
    String filename = filenamePrefix + "-" + sf.format(new Date()) + ".csv";
    File csvFile = new File(directory, filename);
    OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8");
    cf.setFieldHeader(true);
    w.setConfig(cf);

    w.setWriter(fw);

    Map<String, String> record = new HashMap<String, String>();

    for (String field : fields) {
      record.put(field, field);
    }
    w.writeRecord(record);

    int lines = 0;
    for (Object v : q.list()) {
      if (v instanceof Object[]) {
        int i = 0;
        for (Object a : (Object[]) v) {
          record.put(fields.get(i), stringfy(a, false));
          i++;
        }
      } else {
        record.put(fields.get(0), stringfy(v, false));
      }
      w.writeRecord(record);
      lines++;
      if (lines % 100 == 0) {
        log4j.debug("...lines generated so far: " + lines);
        fw.flush();
      }
    }
    fw.flush();
    fw.close();
    log4j.debug("Total lines:" + lines);
    return csvFile;
  }

  public File generateCSV(Query q, boolean b) throws IOException {
    CSVConfig cf = new CSVConfig();

    List<String> fields = new ArrayList<String>();

    for (String field : q.getReturnAliases()) {
      String alias = aliases.get(field);
      String fieldName;
      if (alias != null) {
        fieldName = alias;
      } else {
        fieldName = field;
      }
      fields.add(fieldName);
      CSVField csvField = new CSVField(fieldName);
      cf.addField(csvField);
    }

    CSVWriter w = new CSVWriter();

    SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd-hhmmss");
    String filename = filenamePrefix + "-" + sf.format(new Date()) + ".csv";
    File csvFile = new File(directory, filename);
    OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8");
    cf.setFieldHeader(true);
    w.setConfig(cf);

    w.setWriter(fw);

    Map<String, String> record = new HashMap<String, String>();

    for (String field : fields) {
      record.put(field, field);
    }
    w.writeRecord(record);

    int lines = 0;
    for (Object v : q.list()) {
      if (v instanceof Object[]) {
        int i = 0;
        for (Object a : (Object[]) v) {
          record.put(fields.get(i), stringfy(a, b));
          i++;
        }
      } else {
        record.put(fields.get(0), stringfy(v, b));
      }
      w.writeRecord(record);
      lines++;
      if (lines % 100 == 0) {
        log4j.debug("...lines generated so far: " + lines);
        fw.flush();
      }
    }
    fw.flush();
    fw.close();
    log4j.debug("Total lines:" + lines);
    return csvFile;
  }

  private String stringfy(Object a, boolean dateTimeFormat) {
    String value;
    if (a == null) {
      value = "";
    } else if (a instanceof Date) {
      SimpleDateFormat df;
      if (dateTimeFormat)
        df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
      else
        df = new SimpleDateFormat("MM-dd-yyyy");
      value = df.format((Date) a);
    } else if (a instanceof Boolean) {
      if (boolType == BooleanType.TRUE_FALSE) {
        value = a.toString();
      } else {
        value = (Boolean) a ? "Y" : "N";
      }
    } else {
      value = a.toString();
    }

    value = value.replace('\n', ' ').replace("\"", "'");
    if (value.contains(",")) {
      value = "\"" + value + "\"";
    }
    return value;
  }

  public void setColumnAliases(Map<String, String> aliases) {
    this.aliases = aliases;

  }
}
