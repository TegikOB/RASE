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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DbUtility;

public class ProcessActionHandler extends BaseActionHandler {
  final private static Logger log = Logger.getLogger(ProcessActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;

    try {
      jsonRequest = new JSONObject(content);
      final JSONArray PLIds = jsonRequest.getJSONArray("pickinglist");
      // Get pickinglist
      for (int i = 0; i < PLIds.length(); i++) {
        processPL(PLIds.getString(i));
      }
      JSONObject errorMessage = new JSONObject();
      errorMessage.put("severity", "TYPE_SUCCESS");
      errorMessage.put("text", OBMessageUtils.messageBD("Success"));
      jsonRequest.put("message", errorMessage);

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      OBDal.getInstance().rollbackAndClose();
      try {
        jsonRequest = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();

        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "TYPE_ERROR");
        errorMessage.put("text", message);
        jsonRequest.put("message", errorMessage);
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }
    }
    return jsonRequest;

  }

  private void processPL(String plID) {
    PickingList pl = OBDal.getInstance().get(PickingList.class, plID);
    final Set<String> ships = new HashSet<String>();
    final Set<String> orders = new HashSet<String>();
    for (ShipmentInOutLine iol : pl.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList()) {
      if (("DR").equals(iol.getShipmentReceipt().getDocumentStatus())) {
        ships.add(iol.getShipmentReceipt().getId());
      }
      orders.add(iol.getSalesOrderLine().getSalesOrder().getId());
    }
    for (String strShipId : ships) {
      final List<Object> param = new ArrayList<Object>();
      param.add(null);
      param.add(strShipId);
      CallStoredProcedure.getInstance().call("M_Inout_POST", param, null, true, false);
    }
    pl.setPickliststatus("CO");
    for (String strOrderId : orders) {
      // check there is no orderline in other draft pickinglist
      StringBuilder whereClause = new StringBuilder();
      List<Object> parameters = new ArrayList<Object>();
      whereClause.append(" as iol ");
      whereClause.append(" where iol.");
      whereClause.append(ShipmentInOutLine.PROPERTY_OBWPLPICKINGLIST);
      whereClause.append("." + PickingList.PROPERTY_PICKLISTSTATUS + " = 'DR'");
      whereClause.append(" and ");
      whereClause.append(ShipmentInOutLine.PROPERTY_OBWPLPICKINGLIST);
      whereClause.append(" is not null ");
      whereClause.append(" and ");
      whereClause.append(ShipmentInOutLine.PROPERTY_OBWPLPICKINGLIST);
      whereClause.append(" <> ? ");
      parameters.add(pl);
      whereClause.append(" and ");
      whereClause.append(ShipmentInOutLine.PROPERTY_SALESORDERLINE);
      whereClause.append("." + OrderLine.PROPERTY_ID + " IN (");
      whereClause.append(" SELECT ");
      whereClause.append(OrderLine.PROPERTY_ID);
      whereClause.append(" FROM OrderLine as ol");
      whereClause.append(" WHERE ol." + OrderLine.PROPERTY_SALESORDER + ".id = ?");
      parameters.add(strOrderId);
      whereClause.append(" )");
      OBQuery<ShipmentInOutLine> obData = OBDal.getInstance().createQuery(ShipmentInOutLine.class,
          whereClause.toString(), parameters);
      if (obData.count() == 0) {
        Order o = OBDal.getInstance().get(Order.class, strOrderId);
        o.setObwplIsinpickinglist(false);
      }
    }
    OBDal.getInstance().flush();
  }

}
