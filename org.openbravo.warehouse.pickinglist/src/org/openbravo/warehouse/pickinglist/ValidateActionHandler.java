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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.warehouse.pickinglist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DbUtility;

public class ValidateActionHandler extends BaseActionHandler {
  final private static Logger log = Logger.getLogger(ValidateActionHandler.class);
  List<String> pickingLists = new ArrayList<String>();

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    JSONObject response = null;
    try {
      jsonRequest = new JSONObject(content);
      response = new JSONObject();
      final String action = jsonRequest.getString("action");
      if ("validate".equals(action)) {
        final String recordId = jsonRequest.getString("recordId");
        response = getGridData(recordId);
      }

    } catch (Exception e) {
      log.error("Error in ValidateActionHandler", e);
      try {
        response = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "TYPE_ERROR");
        errorMessage.put("text", message);
        response.put("message", errorMessage);
      } catch (Exception e2) {
        log.error("Error generating the error message", e2);
        // do nothing, give up
      }
    }
    return response;
  }

  private JSONObject getGridData(String recordId) {
    JSONObject response = new JSONObject();
    JSONArray data = new JSONArray();
    JSONObject item = null;
    OBContext.setAdminMode();
    try {
      final String hqlGridData = "SELECT Product.uPCEAN, Product.name, Product.id, sum(MaterialMgmtShipmentInOutLine.movementQuantity), min(MaterialMgmtShipmentInOutLine.lineNo) as newLineNo "
          + "FROM MaterialMgmtShipmentInOutLine MaterialMgmtShipmentInOutLine, Product Product "
          + "WHERE MaterialMgmtShipmentInOutLine.obwplPickinglist.id = :theRecordId "
          + "AND MaterialMgmtShipmentInOutLine.product.id = Product.id "
          + "AND MaterialMgmtShipmentInOutLine.product.productType = 'I' "
          + "AND MaterialMgmtShipmentInOutLine.product.stocked = 'Y' "
          + "GROUP BY Product.name, Product.id, Product.uPCEAN " + "ORDER BY newLineNo";

      Query qryGridData = OBDal.getInstance().getSession().createQuery(hqlGridData);
      qryGridData.setParameter("theRecordId", recordId);

      int queryCount = 0;
      for (Object qryGridDataObject : qryGridData.list()) {
        queryCount++;
        final Object[] qryGridDataObjectItem = (Object[]) qryGridDataObject;
        item = new JSONObject();
        if (qryGridDataObjectItem[0] != null) {
          item.put("barcode", qryGridDataObjectItem[0]);
        } else {
          item.put("barcode", "");
        }
        item.put("product", qryGridDataObjectItem[1]);
        item.put("productId", qryGridDataObjectItem[2]);
        item.put("quantity", qryGridDataObjectItem[3]);
        data.put(item);
      }
      response.put("startRow", 0);
      response.put("endRow", (queryCount == 0 ? 0 : queryCount - 1));
      response.put("totalRows", queryCount);
      response.put("data", data);
    } catch (JSONException e) {
      log.error(e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return response;
  }
}
