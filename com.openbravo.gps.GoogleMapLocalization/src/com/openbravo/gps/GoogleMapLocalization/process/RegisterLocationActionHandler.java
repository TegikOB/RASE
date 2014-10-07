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
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.common.enterprise.Organization;

import com.openbravo.gps.GoogleMapLocalization.Localization;

public class RegisterLocationActionHandler extends BaseActionHandler {

  protected JSONObject execute(Map<String, Object> parameters, String data) {

    try {

      // get the data as json
      final JSONObject jsonData = new JSONObject(data);
      final JSONArray datos = jsonData.getJSONArray("data");

      OBContext.setAdminMode(true);

      final Tab tab = OBDal.getInstance().get(Tab.class, datos.get(1));
      final Organization organization = OBDal.getInstance().get(Organization.class,
          datos.getString(4));
      final Client client = OBDal.getInstance().get(Client.class, datos.getString(5));

      // CHECK IF THE LOCALIZATION IS HAS BEEN REGISTERED BEFORE

      final Localization localization;

      OBCriteria<Localization> criteriaLocalization = OBDal.getInstance().createCriteria(
          Localization.class);
      criteriaLocalization.add(Restrictions.eq(Localization.PROPERTY_TABLE, tab.getTable()));
      criteriaLocalization.add(Restrictions.eq(Localization.PROPERTY_RECORDID, datos.getString(0)));

      List<Localization> localizationList = criteriaLocalization.list();

      if (localizationList.size() > 0) {
        localization = OBDal.getInstance().get(Localization.class, localizationList.get(0).getId());
      } else {
        localization = OBProvider.getInstance().get(Localization.class);
      }

      // SET DATA TO LOCALIZATION INSTANCE

      localization.setOrganization(organization);
      localization.setClient(client);
      localization.setRecordID(datos.getString(0));
      localization.setTable(tab.getTable());
      localization.setLatitude(datos.getString(2));
      localization.setLongitude(datos.getString(3));
      localization.setDescription(datos.getString(6));

      // SAVE AND FLUSH

      OBDal.getInstance().save(localization);

      OBDal.getInstance().flush();

      final JSONObject json = new JSONObject();

      json.put("response", datos);

      return json;

    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}