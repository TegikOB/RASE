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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package com.openbravo.gps.GoogleMapLocalization.process;

import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Tab;

public class ButtonTabsActionHandler extends BaseActionHandler {

  // private static final Logger log = Logger.getLogger(PresenseControlActionHandler.class);

  protected JSONObject execute(Map<String, Object> parameters, String data) {

    try {

      OBContext.setAdminMode(true);

      // get the data as json
      final JSONObject jsonData = new JSONObject(data);

      if ("data".equals(jsonData.get("accion").toString())) {
        OBCriteria<Tab> criteriaADTab = OBDal.getInstance().createCriteria(Tab.class);
        // criteriaADTab.add(Restrictions.eq(Tab.PROPERTY_OBGMPSLISLOCALIZATION, true));
        List<Tab> tabs = criteriaADTab.list();

        final JSONArray tabsJS = new JSONArray();
        for (int i = 0; i < tabs.size(); i++) {
          tabsJS.put(tabs.get(i).getId());
        }

        final JSONObject json = new JSONObject();

        json.put("tabs", tabsJS);

        return json;
      }
    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return null;
  }
}