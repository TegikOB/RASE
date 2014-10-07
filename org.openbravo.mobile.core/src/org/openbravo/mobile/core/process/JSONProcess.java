/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.process;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONObject;

public interface JSONProcess {

  public void exec(Writer w, JSONObject jsonsent) throws IOException, ServletException;
}
