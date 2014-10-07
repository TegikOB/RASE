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
 * All portions are Copyright (C) 2012-2013 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.warehouse.pickinglist;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

public class CreateActionHandler extends BaseActionHandler {
  final private static Logger log = Logger.getLogger(CreateActionHandler.class);
  List<String> pickingLists = new ArrayList<String>();
  HashMap<Organization, PickingList> pL = new HashMap<Organization, PickingList>();
  HashMap<BusinessPartner, List<PickingList>> pLBP = new HashMap<BusinessPartner, List<PickingList>>();
  long lineNo;
  String notCompletedPL = "";

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    try {
      jsonRequest = new JSONObject(content);
      if ("getcheck".equals(jsonRequest.getString("action"))) {
        VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
        jsonRequest.put("valuecheck", getActionComboBox(vars));
        return jsonRequest;
      }
      final JSONArray orderIds = jsonRequest.getJSONArray("orders");
      String groupPL = jsonRequest.getString("checkvalue");
      // Get orders
      boolean hasEnoughStock = true;
      Order order = null;
      PickingList pickingList;
      boolean hasEnoughStock2;
      for (int i = 0; i < orderIds.length(); i++) {
        pickingList = null;
        if ("GO".equals(groupPL)) {
          order = OBDal.getInstance().get(Order.class, orderIds.getString(i));
          if (!pL.containsKey(order.getOrganization())) {
            // Create Picking List
            pickingList = createPL(order, groupPL);
            pL.put(pickingList.getOrganization(), pickingList);
          } else {
            pickingList = pL.get(order.getOrganization());
          }
          String msg = pickingList.getDescription() == null ? OBMessageUtils
              .messageBD("OBWPL_docNo")
              + " "
              + order.getDocumentNo()
              + " "
              + OBMessageUtils.messageBD("OBWPL_BPartner") + order.getBusinessPartner().getName()
              : pickingList.getDescription() + ", \n" + OBMessageUtils.messageBD("OBWPL_docNo")
                  + " " + order.getDocumentNo() + " " + OBMessageUtils.messageBD("OBWPL_BPartner")
                  + order.getBusinessPartner().getName();
          if (msg.length() > 2000) {
            msg = msg.substring(0, 2000);
          }
          pickingList.setDescription(msg);
          OBDal.getInstance().save(pickingList);

          hasEnoughStock2 = processOrder(order, pickingList);
        } else if ("GBP".equals(groupPL)) {
          order = OBDal.getInstance().get(Order.class, orderIds.getString(i));
          // check if there is a PL with the order's BusinessPartner
          if (!pLBP.containsKey(order.getBusinessPartner())) {
            // Create Picking List
            pickingList = createPL(order, groupPL);
            List<PickingList> pick = new ArrayList<PickingList>();
            if (pLBP.get(order.getBusinessPartner()) != null) {
              pick = pLBP.get(order.getBusinessPartner());
            }
            pick.add(pick.size(), pickingList);
            pLBP.put(order.getBusinessPartner(), pick);
          } else { // exist the business partner in PL. check if order's organization is the same
            for (PickingList pl : pLBP.get(order.getBusinessPartner())) {
              if (pl.getOrganization() == order.getOrganization()) {
                pickingList = pl;
                break;
              }
            }
            if (pickingList == null) {
              // Create Picking List
              pickingList = createPL(order, groupPL);
              List<PickingList> pick = new ArrayList<PickingList>();
              pick = pLBP.get(order.getBusinessPartner());
              pick.add(pick.size(), pickingList);
              pLBP.put(order.getBusinessPartner(), pick);
            }
          }
          String msg = pickingList.getDescription() == null ? OBMessageUtils
              .messageBD("OBWPL_docNo")
              + " "
              + order.getDocumentNo()
              + " "
              + OBMessageUtils.messageBD("OBWPL_BPartner") + order.getBusinessPartner().getName()
              : pickingList.getDescription() + ", \n" + OBMessageUtils.messageBD("OBWPL_docNo")
                  + " " + order.getDocumentNo() + " " + OBMessageUtils.messageBD("OBWPL_BPartner")
                  + order.getBusinessPartner().getName();

          if (msg.length() > 2000) {
            msg = msg.substring(0, 2000);
          }
          pickingList.setDescription(msg);
          OBDal.getInstance().save(pickingList);

          hasEnoughStock2 = processOrder(order, pickingList);
        } else {
          hasEnoughStock2 = processOrder(orderIds.getString(i), groupPL);
        }

        if (!hasEnoughStock2) {
          hasEnoughStock = false;
        }
      }
      if (pickingLists.size() > 0) {
        String strPickingListDocNos = "";
        for (String plDocNo : pickingLists) {
          strPickingListDocNos += plDocNo + ", ";
        }
        strPickingListDocNos = strPickingListDocNos.substring(0,
            strPickingListDocNos.lastIndexOf(","));
        if (!hasEnoughStock) {
          strPickingListDocNos = strPickingListDocNos + "</br>"
              + OBMessageUtils.messageBD("OBWPL_PartiallyReserved") + ": " + notCompletedPL;
        }

        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "TYPE_SUCCESS");
        errorMessage.put("text", strPickingListDocNos);

        errorMessage.put("title", OBMessageUtils.messageBD("OBWPL_PickingList_Created"));
        jsonRequest.put("message", errorMessage);

      }

    } catch (Exception e) {
      log.error("Error in CreateActionHandler", e);
      OBDal.getInstance().rollbackAndClose();

      try {
        jsonRequest = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "TYPE_ERROR");
        errorMessage.put("text", "".equals(message) ? e : message);
        jsonRequest.put("message", errorMessage);
      } catch (Exception e2) {
        log.error("Error generating the error message", e2);
        // do nothing, give up
      }
    }

    return jsonRequest;

  }

  private PickingList createPL(Order order, String groupPL) {
    PickingList pickingList = null;
    // Create Picking List
    pickingList = OBProvider.getInstance().get(PickingList.class);
    pickingList.setOrganization(order.getOrganization());
    pickingList.setDocumentdate(new Date());
    DocumentType plDocType = OBWPL_Utils.getDocumentType(order.getOrganization(), "OBWPL_doctype");
    if (plDocType == null) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_DoctypeMissing"));
    }
    pickingList.setDocumentType(plDocType);
    pickingList.setDocumentNo(OBWPL_Utils.getDocumentNo(plDocType, "OBWPL_PickingList"));
    pickingList.setPickliststatus("DR");
    if ("NG".equals(groupPL)) {
      String msg = OBMessageUtils.messageBD("OBWPL_docNo") + " " + order.getDocumentNo() + " "
          + OBMessageUtils.messageBD("OBWPL_BPartner") + order.getBusinessPartner().getName();
      pickingList.setDescription(msg);
    }
    OBDal.getInstance().save(pickingList);
    pickingLists.add(pickingList.getDocumentNo());

    return pickingList;

  }

  private JSONObject getActionComboBox(VariablesSecureApp vars) throws Exception {
    final String SO_WINDOW_ID = "143";
    final String PICK_OPTIONS = "C13AD141699C45168090496CF88FEED9";
    JSONObject response = new JSONObject();
    DalConnectionProvider conn = new DalConnectionProvider(false);
    ComboTableData comboTableData = new ComboTableData(vars, conn, "LIST", "", PICK_OPTIONS, "",
        Utility.getContext(conn, vars, "#AccessibleOrgTree", SO_WINDOW_ID), Utility.getContext(
            conn, vars, "#User_Client", SO_WINDOW_ID), 0);
    Utility.fillSQLParameters(conn, vars, null, comboTableData, SO_WINDOW_ID, "");
    FieldProvider[] fpArray = comboTableData.select(false);
    JSONObject valueMap = new JSONObject();
    for (FieldProvider fp : fpArray) {
      String key = fp.getField("id");
      String value = fp.getField("name");
      valueMap.put(key, value);
    }
    response.put("valueMap", valueMap);
    String val = "";
    try {
      val = Preferences.getPreferenceValue("OBWPL_GroupPL", true, OBContext.getOBContext()
          .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
          .getOBContext().getUser(), OBContext.getOBContext().getRole(), null);
    } catch (PropertyNotFoundException e) {
      // Do nothing
    } catch (PropertyException e) {
      log.error("Error retrieving preference", e);
    }
    response.put("defaultValue", val);
    return response;
  }

  private boolean processOrder(String strOrderId, String groupPL) {
    Order order = OBDal.getInstance().get(Order.class, strOrderId);
    // Create Picking List
    PickingList pickingList = createPL(order, groupPL);
    return processOrder(order, pickingList);
  }

  private boolean processOrder(Order order, PickingList pickingList) {

    if (order.isObwplIsinpickinglist()) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_IsInPL") + order.getDocumentNo());
    }

    if (order.isObwplReadypl()) {
      throw new OBException(OBMessageUtils.messageBD("OBWPL_IsExcluded") + order.getDocumentNo());
    }
    order.setObwplIsinpickinglist(true);

    // Create In Out
    ShipmentInOut shipment = OBProvider.getInstance().get(ShipmentInOut.class);
    shipment.setOrganization(order.getOrganization());
    shipment.setSalesTransaction(true);
    shipment.setMovementType("C-");
    DocumentType shipDocType = OBWPL_Utils.getDocumentType(order.getOrganization(), "MMS");
    shipment.setDocumentType(shipDocType);
    shipment.setDocumentNo(OBWPL_Utils.getDocumentNo(shipDocType, "M_InOut"));
    shipment.setWarehouse(order.getWarehouse());
    shipment.setBusinessPartner(order.getBusinessPartner());
    shipment.setPartnerAddress(order.getPartnerAddress());
    shipment.setDeliveryLocation(order.getDeliveryLocation());
    shipment.setDeliveryMethod(order.getDeliveryMethod());
    shipment.setDeliveryTerms(order.getDeliveryTerms());

    shipment.setMovementDate(new Date());
    shipment.setAccountingDate(new Date());
    shipment.setSalesOrder(order);
    shipment.setUserContact(order.getUserContact());
    shipment.setOrderReference(order.getOrderReference());
    shipment.setFreightCostRule(order.getFreightCostRule());
    shipment.setFreightAmount(order.getFreightAmount());
    shipment.setShippingCompany(order.getShippingCompany());
    shipment.setPriority(order.getPriority());

    shipment.setProject(order.getProject());
    shipment.setActivity(order.getActivity());
    shipment.setSalesCampaign(order.getSalesCampaign());
    shipment.setStDimension(order.getStDimension());
    shipment.setNdDimension(order.getNdDimension());
    shipment.setTrxOrganization(order.getTrxOrganization());

    shipment.setDocumentStatus("DR");
    shipment.setDocumentAction("CO");
    shipment.setProcessNow(false);
    OBDal.getInstance().save(shipment);
    lineNo = 10L;
    boolean hasEnoughStock = true;
    for (OrderLine orderLine : order.getOrderLineList()) {
      // Only consider pending to deliver lines of stocked item products.
      if (orderLine.getProduct() != null && orderLine.getProduct().isStocked()
          && orderLine.getProduct().getProductType().equals("I")
          && orderLine.getOrderedQuantity().compareTo(BigDecimal.ZERO) < 0) {
        throw new OBException(OBMessageUtils.messageBD("OBWPL_OrderedQtyMustBePositive"));
      } else if (orderLine.getProduct() != null && orderLine.getProduct().isStocked()
          && orderLine.getProduct().getProductType().equals("I")
          && orderLine.getOrderedQuantity().compareTo(BigDecimal.ZERO) != 0
          && orderLine.getOrderedQuantity().compareTo(orderLine.getDeliveredQuantity()) != 0
          && (orderLine.isObwplReadypl() == null || !orderLine.isObwplReadypl())) {
        boolean hasEnoughStock2 = processOrderLine(orderLine, shipment, pickingList);
        if (!hasEnoughStock2) {
          hasEnoughStock = false;
        }
      } else if (orderLine.getProduct() != null
          && orderLine.getOrderedQuantity().compareTo(BigDecimal.ZERO) != 0
          && orderLine.getOrderedQuantity().compareTo(orderLine.getDeliveredQuantity()) != 0
          && (orderLine.isObwplReadypl() == null || !orderLine.isObwplReadypl())) {
        processRestOrderLine(orderLine, shipment, pickingList);
      }
    }
    OBDal.getInstance().refresh(shipment);
    if (shipment.getMaterialMgmtShipmentInOutLineList().size() == 0) {
      throw new OBException(OBMessageUtils.messageBD("NotEnoughAvailableStock"));
    }
    return hasEnoughStock;
  }

  private boolean processOrderLine(OrderLine orderLine, ShipmentInOut shipment,
      PickingList pickingList) {
    // Reserve Order Line
    boolean hasEnoughStock = true;
    boolean existsReservation = !orderLine.getMaterialMgmtReservationList().isEmpty();
    Reservation res = ReservationUtils.getReservationFromOrder(orderLine);
    if (res.getRESStatus().equals("DR")) {
      ReservationUtils.processReserve(res, "PR");
    } else if (res.getQuantity().compareTo(res.getReservedQty()) != 0) {
      ReservationUtils.reserveStockAuto(res);
    }

    for (ReservationStock a : res.getMaterialMgmtReservationStockList()) {
      if (!a.isAllocated()) {
        a.setAllocated(true);
        OBDal.getInstance().save(a);
        OBDal.getInstance().flush();
      }
    }

    if (!existsReservation) {
      res.setOBWPLGeneratedByPickingList(true);
      OBDal.getInstance().save(res);
      OBDal.getInstance().flush();
    }
    OBDal.getInstance().refresh(res);
    if (res.getQuantity().compareTo(res.getReservedQty()) != 0) {
      if ("".equals(notCompletedPL)) {
        notCompletedPL = pickingList.getDocumentNo();
      } else if (!notCompletedPL.contains(pickingList.getDocumentNo())) {
        notCompletedPL = notCompletedPL + ", " + pickingList.getDocumentNo();
      }

      hasEnoughStock = false;
    }

    List<ShipmentInOutLine> shipmentLines = new ArrayList<ShipmentInOutLine>();
    for (ReservationStock resStock : res.getMaterialMgmtReservationStockList()) {

      if (resStock.getStorageBin() == null) { // If pre-reserve is not yet reserve
        continue;
      }

      if (resStock.getQuantity()
          .subtract(resStock.getReleased() == null ? BigDecimal.ZERO : resStock.getReleased())
          .compareTo(BigDecimal.ZERO) > 0) {

        // Create InOut line.
        ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);
        line.setOrganization(shipment.getOrganization());
        line.setShipmentReceipt(shipment);
        line.setSalesOrderLine(orderLine);
        line.setObwplPickinglist(pickingList);
        line.setLineNo(lineNo);
        lineNo += 10L;
        line.setProduct(orderLine.getProduct());
        line.setUOM(orderLine.getUOM());
        line.setAttributeSetValue(resStock.getAttributeSetValue());
        line.setStorageBin(resStock.getStorageBin());
        line.setMovementQuantity(resStock.getQuantity().subtract(
            resStock.getReleased() == null ? BigDecimal.ZERO : resStock.getReleased()));
        line.setDescription(orderLine.getDescription());
        line.setExplode(orderLine.isExplode());
        if (orderLine.getBOMParent() != null) {
          OBCriteria<ShipmentInOutLine> obc = OBDal.getInstance().createCriteria(
              ShipmentInOutLine.class);
          obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SHIPMENTRECEIPT, shipment));
          obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SALESORDERLINE,
              orderLine.getBOMParent()));
          obc.setMaxResults(1);
          if (obc.uniqueResult() != null) {
            ShipmentInOutLine iol = (ShipmentInOutLine) obc.uniqueResult();
            line.setBOMParent(iol);
          }
        }

        OBDal.getInstance().save(line);
        shipmentLines.add(line);
      }
    }
    OBDal.getInstance().flush();
    return hasEnoughStock;
  }

  private void processRestOrderLine(OrderLine orderLine, ShipmentInOut shipment,
      PickingList pickingList) {
    ShipmentInOutLine line = OBProvider.getInstance().get(ShipmentInOutLine.class);
    line.setOrganization(shipment.getOrganization());
    line.setShipmentReceipt(shipment);
    line.setSalesOrderLine(orderLine);
    line.setObwplPickinglist(pickingList);
    line.setLineNo(lineNo);
    lineNo += 10L;
    line.setProduct(orderLine.getProduct());
    line.setUOM(orderLine.getUOM());
    line.setAttributeSetValue(orderLine.getProduct().getAttributeSetValue());
    line.setMovementQuantity(orderLine.getOrderedQuantity().subtract(
        orderLine.getDeliveredQuantity()));
    line.setDescription(orderLine.getDescription());
    line.setExplode(orderLine.isExplode());

    if (orderLine.getBOMParent() != null) {
      OBCriteria<ShipmentInOutLine> obc = OBDal.getInstance().createCriteria(
          ShipmentInOutLine.class);
      obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SHIPMENTRECEIPT, shipment));
      obc.add(Restrictions.eq(ShipmentInOutLine.PROPERTY_SALESORDERLINE, orderLine.getBOMParent()));
      obc.setMaxResults(1);
      if (obc.uniqueResult() != null) {
        ShipmentInOutLine iol = (ShipmentInOutLine) obc.uniqueResult();
        line.setBOMParent(iol);
      }
    }

    OBDal.getInstance().save(line);
    OBDal.getInstance().flush();
  }

}
