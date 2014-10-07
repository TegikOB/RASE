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
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.order.Order;

import com.openbravo.gps.GoogleMapLocalization.Localization;

public class MapElementDataActionHandler extends BaseActionHandler {

  protected JSONObject execute(Map<String, Object> parameters, String data) {

    try {

      OBContext.setAdminMode(true);

      // get the data as json
      final JSONObject jsonData = new JSONObject(data);
      final JSONArray recordIds = jsonData.getJSONArray("lines");

      if ("showInitialElements".equals(recordIds.getString(0).toString())) {

        final JSONArray localizationArrayJS = new JSONArray();
        final Tab tab = OBDal.getInstance().get(Tab.class, recordIds.get(1));

        for (int i = 2; i < recordIds.length(); i++) {
          final String recordId = recordIds.getString(i);

          if ("220".equals(recordIds.get(1))) {
            final BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
                recordId);
            final JSONArray locationAddressArray = new JSONArray();
            int j = 0;
            for (Location location : businessPartner.getBusinessPartnerLocationList()) {
              final JSONObject jsonArrayItem = new JSONObject();
              jsonArrayItem.put("Id", businessPartner.getId());
              jsonArrayItem.put("Identifier", businessPartner.getIdentifier());
              final String address = location.getLocationAddress().getAddressLine1() + ", "
                  // + location.getLocationAddress().getAddressLine2() + ", "
                  + location.getLocationAddress().getPostalCode() + " "
                  + location.getLocationAddress().getCityName() + ", "
                  // + location.getLocationAddress().getRegionName() + ", "
                  + location.getLocationAddress().getCountry().getName();
              jsonArrayItem.put("address", address);
              locationAddressArray.put(jsonArrayItem);
            }
            localizationArrayJS.put(locationAddressArray);

          } else if ("186".equals(recordIds.get(1)) || "294".equals(recordIds.get(1))) {
            final Order order = OBDal.getInstance().get(Order.class, recordId);
            final JSONArray locationAddressArray = new JSONArray();
            int j = 0;
            for (Location location : order.getBusinessPartner().getBusinessPartnerLocationList()) {
              final JSONObject jsonArrayItem = new JSONObject();
              jsonArrayItem.put("Id", order.getId());
              jsonArrayItem.put("Identifier", order.getIdentifier());
              final String address = location.getLocationAddress().getAddressLine1() + ", "
                  // + location.getLocationAddress().getAddressLine2() + ", "
                  + location.getLocationAddress().getPostalCode() + " "
                  + location.getLocationAddress().getCityName() + ", "
                  // + location.getLocationAddress().getRegionName() + ", "
                  + location.getLocationAddress().getCountry().getName();
              jsonArrayItem.put("address", address);
              locationAddressArray.put(jsonArrayItem);
            }
            localizationArrayJS.put(locationAddressArray);
          } else {
            OBCriteria<Localization> criteriaLocalization = OBDal.getInstance().createCriteria(
                Localization.class);
            criteriaLocalization.add(Restrictions.eq(Localization.PROPERTY_TABLE, tab.getTable()));
            criteriaLocalization.add(Restrictions.eq(Localization.PROPERTY_RECORDID, recordId));

            List<Localization> itr = criteriaLocalization.list();

            if (itr.size() == 1) {
              final Localization localization = OBDal.getInstance().get(Localization.class,
                  itr.get(0).getId());

              if (localization.getLatitude() != null && localization.getLongitude() != null) {

                final JSONObject jsonArrayItem = new JSONObject();
                jsonArrayItem.put("Id", localization.getId());
                jsonArrayItem.put("Identifier", localization.getIdentifier());
                jsonArrayItem.put("Latitude", localization.getLatitude());
                jsonArrayItem.put("Longitude", localization.getLongitude());

                localizationArrayJS.put(jsonArrayItem);
              }
            }
          }

        }

        final JSONObject json = new JSONObject();

        json.put("localizationArrayJS", localizationArrayJS);

        return json;
      } else if ("updateLocalizationPosition".equals(recordIds.getString(0).toString())) {
        final Localization localization = OBDal.getInstance().get(Localization.class,
            recordIds.getString(1));

        // SET NEW LATITUDE AND LONGITUDE

        localization.setLatitude(recordIds.getString(2));
        localization.setLongitude(recordIds.getString(3));

        // SAVE AND FLUSH

        OBDal.getInstance().save(localization);

        OBDal.getInstance().flush();

        OBDal.getInstance().commitAndClose();

        final JSONObject json = new JSONObject();

        return json;
      } else if ("getLocalizationLastPosition".equals(recordIds.getString(0).toString())) {
        final Localization localization = OBDal.getInstance().get(Localization.class,
            recordIds.getString(1));

        final JSONObject json = new JSONObject();
        json.put("lat", localization.getLatitude());
        json.put("lng", localization.getLongitude());

        return json;

      } else if ("getDistance".equals(recordIds.getString(0).toString())) {
        final JSONObject json = new JSONObject();

        final JSONArray localizationArrayJS = new JSONArray();

        final Tab tab = OBDal.getInstance().get(Tab.class, recordIds.get(1));
        final double latP = recordIds.getDouble(2);
        final double lngP = recordIds.getDouble(3);
        final double dist = recordIds.getDouble(4);

        final OBCriteria<Localization> criteriaLocalization = OBDal.getInstance().createCriteria(
            Localization.class);
        criteriaLocalization.add(Restrictions.eq(Localization.PROPERTY_TABLE, tab.getTable()));

        final List<Localization> criteriaLocalizationList = criteriaLocalization.list();
        for (Localization localization : criteriaLocalizationList) {

          if ((localization.getLatitude() != null && localization.getLongitude() != null)) {
            final double lat = Double.parseDouble(localization.getLatitude());
            final double lng = Double.parseDouble(localization.getLongitude());
            final double distance = gps2m(latP, lngP, lat, lng);
            if (distance < dist) {

              final JSONObject jsonArrayItem = new JSONObject();
              jsonArrayItem.put("Id", localization.getId());
              jsonArrayItem.put("Identifier", localization.getIdentifier());
              jsonArrayItem.put("Latitude", localization.getLatitude());
              jsonArrayItem.put("Longitude", localization.getLongitude());

              localizationArrayJS.put(jsonArrayItem);
            }
          }
        }
        json.put("localizationArrayJS", localizationArrayJS);
        return json;

      } else {

        final JSONObject json = new JSONObject();
        return json;
      }

    } catch (Exception e) {
      throw new OBException(e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
    double pk = (double) (180 / 3.14169);

    double a1 = lat_a / pk;
    double a2 = lng_a / pk;
    double b1 = lat_b / pk;
    double b2 = lng_b / pk;

    double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
    double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
    double t3 = Math.sin(a1) * Math.sin(b1);
    double tt = Math.acos(t1 + t2 + t3);

    return 6366000 * tt;
  }

}