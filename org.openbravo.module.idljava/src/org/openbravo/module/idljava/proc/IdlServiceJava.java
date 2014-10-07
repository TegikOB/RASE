/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.module.idljava.proc;

import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.openbravo.base.exception.OBException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.proc.IdlService;
import org.openbravo.idl.proc.Parameter;
import org.openbravo.idl.proc.Validator;

import au.com.bytecode.opencsv.CSVReader;

/**
 * 
 * @author adrian
 */
public abstract class IdlServiceJava extends IdlService {

  @Override
  protected boolean executeImport(String filename, boolean insert) throws Exception {

    CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"),
        ',', '\"', '\\', 0, false, true);

    String[] nextLine;

    // Check header
    nextLine = reader.readNext();
    if (nextLine == null) {
      throw new OBException(Utility.messageBD(conn, "IDLJAVA_HEADER_MISSING", vars.getLanguage()));
    }
    Parameter[] parameters = getParameters();
    if (parameters.length > nextLine.length) {
      throw new OBException(Utility
          .messageBD(conn, "IDLJAVA_HEADER_BAD_LENGTH", vars.getLanguage()));
    }

    Validator validator;

    while ((nextLine = reader.readNext()) != null) {

      if (nextLine.length > 1 || nextLine[0].length() > 0) {
        // It is not an empty line

        // Validate types
        if (parameters.length > nextLine.length) {
          throw new OBException(Utility.messageBD(conn, "IDLJAVA_LINE_BAD_LENGTH", vars
              .getLanguage()));
        }

        validator = getValidator(getEntityName());
        Object[] result = validateProcess(validator, nextLine);
        if ("0".equals(validator.getErrorCode())) {
          finishRecordProcess(result);
        } else {
          logRecordError(validator.getErrorMessage(), result);
        }
      }
    }

    return true;
  }

  protected abstract String getEntityName();

  protected abstract Object[] validateProcess(Validator validator, String... values)
      throws Exception;
}
