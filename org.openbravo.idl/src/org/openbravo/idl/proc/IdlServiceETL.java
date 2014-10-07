/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

/**
 *
 * @author adrian
 */
public abstract class IdlServiceETL extends IdlService {

  protected boolean executeImport(String filename, boolean insert) throws Exception {

    String[][] exitCode = runJob(new String[] { "--context_param", "filename=" + filename,
            "--context_param", "executeInsert=" + Boolean.toString(insert) });

    String status = getStatus();
    clear();

    return exitCode != null && !"failure".equals(status);
  }

  protected abstract String[][] runJob(String[] args);

  protected abstract String getStatus();

  protected abstract void clear();

}
