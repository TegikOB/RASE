/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.discounts;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderLineOffer;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.priceadjustment.PriceAdjustment;
import org.openbravo.model.pricing.priceadjustment.Product;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;

public class AddPack extends DalBaseProcess {

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    Boolean callProcess = false;
    String dateFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);

    // TODO: support for invoces
    Long qty = Long.valueOf((String) bundle.getParams().get("qty"));

    Order order = OBDal.getInstance().get(Order.class,
        (String) bundle.getParams().get("C_Order_ID"));

    PriceAdjustment pack = OBDal.getInstance().get(PriceAdjustment.class,
        (String) bundle.getParams().get("mOfferId"));

    PriceList priceList = order.getPriceList();
    int precision = priceList.getCurrency().getStandardPrecision().intValue();
    Date orderDate = order.getOrderDate();

    OBCriteria<PriceListVersion> qPlv = OBDal.getInstance().createCriteria(PriceListVersion.class);
    qPlv.add(Restrictions.eq(PriceListVersion.PROPERTY_PRICELIST, priceList));
    qPlv.add(Restrictions.le(PriceListVersion.PROPERTY_VALIDFROMDATE, orderDate));
    qPlv.setMaxResults(1);
    PriceListVersion plv = qPlv.list().get(0);

    OBCriteria<OrderLine> qLine = OBDal.getInstance().createCriteria(OrderLine.class);
    qLine.add(Restrictions.eq(OrderLine.PROPERTY_SALESORDER, order));
    qLine.setProjection(Projections.max(OrderLine.PROPERTY_LINENO));
    Long nextLine = (Long) qLine.uniqueResult();
    if (nextLine == null) {
      nextLine = 0L;
    }

    final OBQuery<OrderLine> qSamePackLines = OBDal
        .getInstance()
        .createQuery(
            OrderLine.class,
            " as e where e.salesOrder='"
                + order.getId()
                + "' and exists(select 1 from OrderLineOffer offer where e = offer.salesOrderLine and offer.priceAdjustment.id = '"
                + pack.getId() + "')");

    List<OrderLine> lst_samePackLines = qSamePackLines.list();

    if (lst_samePackLines.size() > 0) {
      for (OrderLine orderLine : lst_samePackLines) {
        OBCriteria<Product> qProductOfPack = OBDal.getInstance().createCriteria(Product.class);
        qProductOfPack.add(Restrictions.eq(Product.PROPERTY_PRICEADJUSTMENT, pack));
        qProductOfPack.add(Restrictions.eq(Product.PROPERTY_PRODUCT, orderLine.getProduct()));
        qProductOfPack.setMaxResults(1);
        Product productOfPack = qProductOfPack.list().get(0);
        if (orderLine.getProduct().getId() == productOfPack.getProduct().getId()) {
          BigDecimal qtyToAdd = BigDecimal.valueOf(productOfPack.getObdiscQty() * qty);
          orderLine.setOrderedQuantity(orderLine.getOrderedQuantity().add(qtyToAdd));
          orderLine = calculateOrderLinePrices(orderLine, productOfPack.getProduct(), priceList,
              plv, precision);
          for (OrderLineOffer oloffer : orderLine.getOrderLineOfferList()) {
            OBDal.getInstance().remove(oloffer);
          }
          orderLine.getOrderLineOfferList().clear();
          OBDal.getInstance().save(orderLine);
          callProcess = true;
        }
      }
    } else {
      for (Product packProduct : pack.getPricingAdjustmentProductList()) {
        nextLine += 10L;
        org.openbravo.model.common.plm.Product product = packProduct.getProduct();
        System.out.println(product.getName() + " - " + packProduct.getObdiscQty());
        BigDecimal qtyLine = BigDecimal.valueOf(packProduct.getObdiscQty() * qty);
        OrderLine ol = OBProvider.getInstance().get(OrderLine.class);
        ol.setOrganization(order.getOrganization());
        ol.setSalesOrder(order);
        ol.setProduct(product);
        ol.setLineNo(nextLine);
        ol.setOrderDate(orderDate);
        ol.setWarehouse(order.getWarehouse());
        ol.setCurrency(order.getCurrency());
        ol.setOrderedQuantity(qtyLine);

        ol.setUOM(product.getUOM());

        String taxId = Tax.get(new DalConnectionProvider(false), product.getId(), dateFormat
            .format(orderDate), order.getOrganization().getId(), order.getWarehouse().getId(),
            order.getInvoiceAddress().getId(), order.getInvoiceAddress().getId(), "", order
                .isSalesTransaction());
        TaxRate lineTax = OBDal.getInstance().get(TaxRate.class, taxId);
        ol.setTax(lineTax);

        ol = calculateOrderLinePrices(ol, product, priceList, plv, precision);

        OBDal.getInstance().save(ol);
        callProcess = true;
      }
    }

    if (callProcess) {
      List<Object> parameters = new ArrayList<Object>();
      parameters.add("O");
      parameters.add(order.getId());
      parameters.add(OBContext.getOBContext().getUser().getId());
      CallStoredProcedure.getInstance()
          .call("M_PROMOTION_CALCULATE", parameters, null, true, false);
      // OBError is also used for successful results
      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");
      msg.setMessage(OBMessageUtils.getI18NMessage("OBDISC_packAddedToOrder",
          new String[] { pack.getIdentifier() }));
      bundle.setResult(msg);
    } else {
      // OBError is also used for successful results
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setTitle("@Error@");
      msg.setMessage(OBMessageUtils.getI18NMessage("OBDISC_packWithoutProducts",
          new String[] { pack.getIdentifier() }));
      bundle.setResult(msg);
    }
  }

  protected OrderLine calculateOrderLinePrices(OrderLine ol,
      org.openbravo.model.common.plm.Product product, PriceList priceList, PriceListVersion plv,
      int precision) {
    OBCriteria<ProductPrice> qPp = OBDal.getInstance().createCriteria(ProductPrice.class);
    qPp.add(Restrictions.eq(ProductPrice.PROPERTY_PRICELISTVERSION, plv));
    qPp.add(Restrictions.eq(ProductPrice.PROPERTY_PRODUCT, product));
    ProductPrice pp = qPp.list().get(0);

    if (priceList.isPriceIncludesTax()) {
      ol.setBaseGrossUnitPrice(pp.getStandardPrice());
      ol.setGrossUnitPrice(pp.getStandardPrice());
      ol.setGrossListPrice(pp.getListPrice());
      BigDecimal amt = pp.getStandardPrice().multiply(ol.getOrderedQuantity())
          .setScale(precision, BigDecimal.ROUND_HALF_UP);
      ol.setLineGrossAmount(amt);

      final BigDecimal netUnitPrice = FinancialUtils.calculateNetFromGross(ol.getTax().getId(),
          amt, precision, amt, ol.getOrderedQuantity());
      ol.setUnitPrice(netUnitPrice);
      ol.setLineNetAmount(netUnitPrice.multiply(ol.getOrderedQuantity()).setScale(precision));
    } else {
      ol.setStandardPrice(pp.getStandardPrice());
      ol.setUnitPrice(pp.getStandardPrice());
      ol.setListPrice(pp.getListPrice());
      ol.setLineNetAmount(pp.getStandardPrice().multiply(ol.getOrderedQuantity())
          .setScale(precision, BigDecimal.ROUND_HALF_UP));
    }
    return ol;
  }
}
