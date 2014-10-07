/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package com.tegik.idl.module.proc;


import org.openbravo.idl.proc.Parameter;
import org.openbravo.idl.proc.Validator;
import java.math.BigDecimal;
import java.util.Calendar;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.proc.DALUtils;
import org.openbravo.idl.proc.Value;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.plm.Attribute;
import org.openbravo.model.common.plm.AttributeInstance;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.AttributeValue;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.tax.TaxCategory;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListSchema;
import org.openbravo.model.pricing.pricelist.PriceListSchemeLine;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.module.idljava.proc.IdlServiceJava;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.enterprise.Organization;
import org.apache.log4j.Logger;

/**
 *
 * @author Carlos Salinas
 */
public class ordenInventarioProcess extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "Orden de Inventario Tegik";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("AttributeSetValue", Parameter.STRING),
            new Parameter("Product", Parameter.STRING),
            new Parameter("UnitPrice", Parameter.STRING),
            new Parameter("OrderedQuantity", Parameter.STRING),
	    new Parameter("Order", Parameter.STRING)};
    }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
        //validator.checkNotNull(validator.checkString(values[0], 40), "AttributeSetValue");
	validator.checkNotNull(validator.checkString(values[1], 40), "Product");
	validator.checkBigDecimal(values[2]);
	validator.checkBigDecimal(values[3]);
	validator.checkNotNull(validator.checkString(values[4], 32), "Order");
	Logger log4j = Logger.getLogger(ordenInventarioProcess.class);
	Product productoValidate = findDALInstance(false, Product.class, new Value("searchKey", values[1]));
	if (productoValidate == null) {
	  log4j.info("El producto no existe -- ;" + values[1] + "; sda");
	}
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {

    return createProduct((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4]);
  }

  public BaseOBObject createProduct(final String asi, final String producto,
      final String precio, final String cantidad, final String orden)
      throws Exception {

    // INITIAL AND DEFAULT VALUES

    Order ordenObject = findDALInstance(false, Order.class, new Value("documentNo", orden));

    if (ordenObject == null) {
      throw new OBException(Utility.messageBD(conn, "La orden no existe -- " , vars.getLanguage()) + orden);
    }

    OrderLine linea = OBProvider.getInstance().get(OrderLine.class);

    Long numLinea = new Long(10);
    
    linea.setActive(Parameter.BOOLEAN.parse("Y"));
    linea.setBusinessPartner(ordenObject.getBusinessPartner());
    linea.setCurrency(ordenObject.getCurrency());
    linea.setLineNo(numLinea);
    linea.setOrganization(ordenObject.getOrganization());
    linea.setPartnerAddress(ordenObject.getPartnerAddress());
    linea.setSalesOrder(ordenObject);
    linea.setWarehouse(ordenObject.getWarehouse());
    linea.setOrderDate(ordenObject.getOrderDate());

    Product productoObject = findDALInstance(false, Product.class, new Value("searchKey", producto));
    if (productoObject == null) {
      throw new OBException(Utility.messageBD(conn, "El producto no existe -- " , vars.getLanguage()) + producto);
      //return null;
    }

    linea.setProduct(productoObject);
    linea.setUOM(productoObject.getUOM());

    linea.setUnitPrice(Parameter.BIGDECIMAL.parse(precio));
    linea.setOrderedQuantity(Parameter.BIGDECIMAL.parse(cantidad));

    linea.setListPrice(linea.getUnitPrice());
    linea.setPriceLimit(linea.getUnitPrice());
    linea.setStandardPrice(linea.getUnitPrice());

    linea.setLineNetAmount(linea.getUnitPrice().multiply(linea.getOrderedQuantity()));
    linea.setTaxableAmount(linea.getUnitPrice().multiply(linea.getOrderedQuantity()));
    
    TaxRate taxObject = findDALInstance(false, TaxRate.class, new Value("name", "EXENTO"));

     if (taxObject == null) {
      throw new OBException(Utility.messageBD(conn, "No existe un impuesto -- " , vars.getLanguage()) + "EXENTO");
    }

    linea.setTax(taxObject);

    if (asi.equals("") || asi == null){
	int numero = 1;
    }
    else {
	  AttributeSetInstance asiExists = findDALInstance(false, AttributeSetInstance.class, new Value("description", asi));

	  if (asiExists == null) {
	      OBContext.setAdminMode(true);
	      AttributeSetInstance asiNuevo = OBProvider.getInstance().get(AttributeSetInstance.class);

	      asiNuevo.setAttributeSet(linea.getProduct().getAttributeSet());
	      asiNuevo.setDescription(asi);
	      asiNuevo.setLotName(asi);
	      asiNuevo.setOrganization(linea.getOrganization());

	      OBDal.getInstance().save(asiNuevo);
	      OBDal.getInstance().flush();

	      linea.setAttributeSetValue(asiNuevo);
	      OBContext.restorePreviousMode();
	  }else
	  {
	    linea.setAttributeSetValue(asiExists);
	  }
    }

    OBDal.getInstance().save(linea);
    OBDal.getInstance().flush();

    // End process
    OBDal.getInstance().commitAndClose();

    return linea;
  }
}
