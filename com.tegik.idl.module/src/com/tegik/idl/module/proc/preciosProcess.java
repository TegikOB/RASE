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
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.module.idljava.proc.IdlServiceJava;
import org.hibernate.criterion.Expression;
import org.openbravo.dal.service.OBCriteria;

/**
 *
 * @author Carlos Salinas
 */
public class preciosProcess extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "Precios Tegik";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("PriceList", Parameter.STRING),
	    new Parameter("Product", Parameter.STRING),
            new Parameter("Price", Parameter.STRING)};
    }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
        //validator.checkNotNull(validator.checkString(values[0], 40), "SearchKey");
	//validator.checkNotNull(validator.checkString(values[1], 40), "SearchKey");
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {
    return crearPrecio((String) values[0], (String) values[1], (String) values[2]);
  }

  public BaseOBObject crearPrecio(final String plv, final String product, final String precio) throws Exception {

    Product productObject = findDALInstance(false, Product.class, new Value("searchKey", product));

    if (productObject == null) {
	return null;
	//throw new OBException(Utility.messageBD(conn, "No existe el producto: ", vars.getLanguage()) + product);
    }

    PriceListVersion plvObject = findDALInstance(false, PriceListVersion.class, new Value("name", plv));

    if (plvObject == null) {
	throw new OBException(Utility.messageBD(conn, "La lista de precios no existe : ", vars.getLanguage()) + plv);
    }

    final OBCriteria<ProductPrice> pplist = OBDal.getInstance().createCriteria(ProductPrice.class);
             pplist.add(Expression.eq(ProductPrice.PROPERTY_PRODUCT, productObject));
	     pplist.add(Expression.eq(ProductPrice.PROPERTY_PRICELISTVERSION, plvObject));


    for (ProductPrice precioObject : pplist.list()) {
	  precioObject.setListPrice(Parameter.BIGDECIMAL.parse(precio));
	  precioObject.setPriceLimit(Parameter.BIGDECIMAL.parse(precio));
	  precioObject.setStandardPrice(Parameter.BIGDECIMAL.parse(precio));

	  OBDal.getInstance().save(precioObject);
	  OBDal.getInstance().flush();
	
	  // End process
	  OBDal.getInstance().commitAndClose();

	  return precioObject;
    }

    ProductPrice productpriceObject = OBProvider.getInstance().get(ProductPrice.class);

    productpriceObject.setListPrice(Parameter.BIGDECIMAL.parse(precio));
    productpriceObject.setOrganization(productObject.getOrganization());
    productpriceObject.setPriceLimit(Parameter.BIGDECIMAL.parse(precio));
    productpriceObject.setPriceListVersion(plvObject);
    productpriceObject.setProduct(productObject);
    productpriceObject.setStandardPrice(Parameter.BIGDECIMAL.parse(precio));

    OBDal.getInstance().save(productpriceObject);
    OBDal.getInstance().flush();
    
    // End process
    OBDal.getInstance().commitAndClose();

    return productpriceObject;
  }
}
