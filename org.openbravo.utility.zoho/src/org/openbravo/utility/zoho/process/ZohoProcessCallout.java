/*
 ************************************************************************************
 * Copyright (C) 2011 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 ************************************************************************************
 */

package org.openbravo.utility.zoho.process;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class ZohoProcessCallout extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    info.addResult("inpadProcessId", "FF8081812E0979D5012E097EC9900017");
    try {
      // Obtaining a JSON like:
      // {"map": {"entry": {"string": ["zohoProcess", "FF8081813063BFC0013063C22CE10009"]}}}
      // It should be doable by
      // XStream xstream = new XStream(new JettisonMappedXmlDriver());
      // HashMap<String, Object> paramObject = new HashMap<String, Object>();
      // paramObject.put("zohoProcess", info.vars.getStringParameter("inpemObuzohoImportId"));
      // But since library upgde (xstream-1.3 -> xstream-1.3.1) it fails when deserializing, so
      // creating the JSON manually as it should be

      JSONArray value = new JSONArray();
      value.put("zohoProcess");
      value.put(info.vars.getStringParameter("inpemObuzohoImportId"));

      JSONObject string = new JSONObject();
      string.put("string", value);

      JSONObject entry = new JSONObject();
      entry.put("entry", string);

      JSONObject map = new JSONObject();
      map.put("map", entry);
      log4j.info("param:\n" + map.toString(1));
      info.addResult("inpparams", map.toString().replace('"', '\''));
    } catch (Exception e) {
      log4j.error("Error generating param", e);
    }
  }
}
