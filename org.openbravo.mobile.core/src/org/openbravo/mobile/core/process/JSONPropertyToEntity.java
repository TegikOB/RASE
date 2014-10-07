/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.process;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.utils.OBMOBCUtils;
import org.openbravo.service.json.JsonToDataConverter;

public class JSONPropertyToEntity {

  private static final Logger log = Logger.getLogger(JSONPropertyToEntity.class);

  private static final DateFormat isodatefmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  public static void fillBobFromJSON(Entity entity, BaseOBObject bob, JSONObject json)
      throws JSONException {
    JSONPropertyToEntity.fillBobFromJSON(entity, bob, json, 0L);
  }

  public static void fillBobFromJSON(Entity entity, BaseOBObject bob, JSONObject json,
      Long dateOffset) throws JSONException {
    @SuppressWarnings("unchecked")
    Iterator<String> keys = json.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      if (key.equals("id")) {
        continue;
      }

      String oldKey = key;

      if (key.contains(".")) {
        if (key.startsWith(entity.getName() + ".")) {
          // property is specific for this entity
          key = key.substring(key.indexOf(".") + 1);
        } else {
          // property for a different entity, don't check it
          continue;
        }
      } else {
        if (json.has(entity.getName() + "." + key)) {
          // there is a more specific key for this entity
          continue;
        }
      }

      if (entity.hasProperty(key)) {
        log.debug("Found property: " + key + " in entity " + entity.getName());
      } else {
        key = getEquivalentKey(key);
        if (key == null) {
          log.debug("Did not find property: " + oldKey);
          continue;
        } else {
          if (entity.hasProperty(key)) {
            log.debug("Found equivalent key: " + key);
          } else {
            log.debug("Did not find property: " + oldKey);
            continue;
          }
        }
      }

      Property p = entity.getProperty(key);
      Object value;
      if (json.isNull(oldKey)) {
        continue;
      } else {
        value = json.get(oldKey);
      }
      if (p.isPrimitive()) {
        if (p.isDate()) {
          bob.set(p.getName(), OBMOBCUtils.calculateServerDate((String) value, dateOffset));
        } else if (p.isDatetime() || p.isTime()) {
          String strValue = (String) value;
          if (strValue.length() == 23 && strValue.charAt(10) == ' ') {
            // It is an ISO Date. An Absolute date. The server has to save it as is.
            try {
              bob.set(p.getName(), isodatefmt.parse(strValue));
            } catch (ParseException e) {
              throw new JSONException(e);
            }
          } else {
            // It is a JS Date. A UTC date and the server needs to add the timezone.
            String transformedValue = (String) strValue.subSequence(0, strValue.lastIndexOf("."))
                + "+0000";
            bob.set(p.getName(), (Date) JsonToDataConverter.convertJsonToPropertyValue(
                PropertyByType.DATETIME, transformedValue));
          }
        } else if (p.getPrimitiveType() == Long.class) {
          bob.set(p.getName(), json.getLong(oldKey));
        } else if (p.isNumericType()) {
          value = json.getString(oldKey);
          bob.set(key, new BigDecimal((String) value));
        } else {
          bob.set(p.getName(), value);
        }
      } else {
        Property refProp = p.getReferencedProperty();
        Entity refEntity = refProp.getEntity();
        if (value instanceof JSONObject) {
          value = ((JSONObject) value).getString("id");
        }
        BaseOBObject refBob = OBDal.getInstance().getProxy(refEntity.getName(), value.toString());
        bob.set(p.getName(), refBob);
      }

    }
  }

  private static String getEquivalentKey(String key) {
    if (key.equals("bp")) {
      return "businessPartner";
    } else if (key.equals("bploc")) {
      return "partnerAddress";
    } else if (key.equals("qty")) {
      return "orderedQuantity";
    } else if (key.equals("price")) {
      return "baseGrossUnitPrice";
    } else if (key.equals("posTerminal")) {
      return "obposApplications";
    } else if (key.equals("pricenet")) {
      return "unitPrice";
    } else if (key.equals("discountPercentage")) {
      return "discount";
    }

    // Mappings for promotions
    else if (key.equals("ruleId")) {
      return "priceAdjustment";
    } else if (key.equals("basePrice")) {
      return "baseGrossUnitPrice";
    } else if (key.equals("unitDiscount")) {
      return "priceAdjustmentAmt";
    }

    return null;
  }
}
