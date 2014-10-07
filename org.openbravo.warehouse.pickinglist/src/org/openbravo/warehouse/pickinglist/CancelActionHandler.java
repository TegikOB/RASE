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

import java.math.BigDecimal;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.DbUtility;

public class CancelActionHandler extends BaseActionHandler {
  final private static Logger log = Logger.getLogger(CancelActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();

    try {
      jsonRequest = new JSONObject(content);
      final JSONArray PLIds = jsonRequest.getJSONArray("pickinglist");
      boolean processedShip = false;
      // final String action = jsonRequest.getString("action");
      // Get orders
      for (int i = 0; i < PLIds.length(); i++) {
        PickingList pl = OBDal.getInstance().get(PickingList.class, PLIds.getString(i));
        ShipmentInOut ship = null;
        for (ShipmentInOutLine iol : pl.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList()) {
          OrderLine oline = iol.getSalesOrderLine();
          oline.getSalesOrder().setObwplIsinpickinglist(false);
          ship = iol.getShipmentReceipt();
          // if shipment is completed or voided,
          // then picklist status is set to cancel
          if (("CO").equals(ship.getDocumentStatus()) || ("VO").equals(ship.getDocumentStatus())) {
            pl.setPickliststatus("CA");
            processedShip = true;
            OBDal.getInstance().save(pl);
          } else {
            Reservation res = ReservationUtils.getReservationFromOrder(oline);
            if (res.isOBWPLGeneratedByPickingList()) {
              // If the reservation was created using picking list it has to be deleted.
              removeReservation(iol);
            }
            OBDal.getInstance().remove(iol);

          }
        }
        OBDal.getInstance().flush();
        if (!processedShip) {
          // Only remove ship that has no lines.
          pl.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList().clear();
          OBDal.getInstance().remove(pl);
          OBDal.getInstance().remove(ship);
        }
        OBDal.getInstance().flush();
      }
      JSONObject errorMessage = new JSONObject();
      errorMessage.put("severity", "TYPE_SUCCESS");
      errorMessage.put("text", "");
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

  private void removeReservation(ShipmentInOutLine iol) {
    Reservation res = ReservationUtils.getReservationFromOrder(iol.getSalesOrderLine());
    String iolLocator = iol.getStorageBin().getId();
    String iolAttr = iol.getAttributeSetValue() == null ? "0" : iol.getAttributeSetValue().getId();
    BigDecimal iolQty = iol.getMovementQuantity();
    ReservationStock resStock = null;
    for (ReservationStock rSAux : res.getMaterialMgmtReservationStockList()) {
      String rSLocator = rSAux.getStorageBin().getId();
      String rSAttr = rSAux.getAttributeSetValue() == null ? "0" : rSAux.getAttributeSetValue()
          .getId();
      BigDecimal rSQty = rSAux.getQuantity();
      if (rSLocator.equals(iolLocator) && iolAttr.equals(rSAttr) && iolQty == rSQty) {
        resStock = rSAux;
        break;
      }
    }
    if (resStock != null) {
      resStock.setQuantity(resStock.getQuantity().subtract(iol.getMovementQuantity()));
      if (resStock.getQuantity().equals(BigDecimal.ZERO)) {
        res.getMaterialMgmtReservationStockList().remove(resStock);
        // Flush to persist changes in database and execute triggers.
        OBDal.getInstance().flush();
        OBDal.getInstance().refresh(res);
        if (res.getReservedQty().equals(BigDecimal.ZERO)) {
          if (res.getRESStatus().equals("CO")) {
            // Unprocess reservation
            ReservationUtils.processReserve(res, "RE");
            OBDal.getInstance().save(res);
            OBDal.getInstance().flush();
          }
          OBDal.getInstance().remove(res);
        }
      }
    }
  }

}
