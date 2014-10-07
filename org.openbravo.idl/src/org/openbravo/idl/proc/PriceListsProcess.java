/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

import java.math.BigDecimal;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.initial_data_load.pricelistjob_0_1.PriceListJob;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListSchema;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.service.web.ResourceNotFoundException;

/**
 * 
 * @author mirurita
 */
public class PriceListsProcess extends IdlServiceETL {

  final PriceListJob job = new PriceListJob();

  @Override
  protected String[][] runJob(String[] args) {
    return job.runJob(args);
  }

  @Override
  protected String getStatus() {
    return job.getStatus();
  }

  @Override
  protected void clear() {
    job.globalBuffer.clear();
  }

  @Override
  public Parameter[] getParameters() {
    return new Parameter[] { new Parameter("Organization", Parameter.STRING),
        new Parameter("PriceListName", Parameter.STRING),
        new Parameter("IsDefault", Parameter.STRING), new Parameter("IsTaxInc", Parameter.STRING),
        new Parameter("IsSales", Parameter.STRING), new Parameter("Currency", Parameter.STRING),
        new Parameter("EnforcePriceLimit", Parameter.STRING),
        new Parameter("PriceListVersionName", Parameter.STRING),
        new Parameter("DiscountSchema", Parameter.STRING),
        new Parameter("ValidFrom", Parameter.STRING),
        new Parameter("PriceListVersionBase", Parameter.STRING),
        new Parameter("ProductName", Parameter.STRING),
        new Parameter("ListPrice", Parameter.STRING),
        new Parameter("StandardPrice", Parameter.STRING),
        new Parameter("PriceLimit", Parameter.STRING)};
  }

  @Override
  protected BaseOBObject internalProcess(Object... values) throws Exception {

    return createPriceList((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4], (String) values[5], (String) values[6],
        (String) values[7], (String) values[8], (String) values[9], (String) values[10],
        (String) values[11], (String) values[12], (String) values[13], (String) values[14]);

  }

  public BaseOBObject createPriceList(final String Organization, final String plName,
      final String isDefault, final String isTaxInc, final String isSales, final String currency,
      final String enforcePriceLimit, final String plvName, final String discountSchema,
      final String validFrom, final String plvBase, final String productName,
      final String listPrice, final String standardPrice, final String priceLimit) throws Exception {

    // Search Default Currency
    Currency currencyinst = findDALInstance(false, Currency.class, new Value("iSOCode", currency));
    if (currencyinst == null) {
      throw new ResourceNotFoundException(Utility.messageBD(conn, "IDL_CURRENCY_NOT_FOUND", vars
          .getLanguage())
          + currency);
    }

    // Create Price List
    PriceList priceList = findDALInstance(false, PriceList.class, new Value("name", plName));

    if (priceList == null) {
      priceList = OBProvider.getInstance().get(PriceList.class);
      priceList.setOrganization(rowOrganization);
      priceList.setActive(true);
      priceList.setName(plName);

      priceList.setCurrency(currencyinst);
      priceList.setSalesPriceList(Parameter.BOOLEAN.parse(isSales));
      priceList.setPriceIncludesTax(Parameter.BOOLEAN.parse(isTaxInc));
      priceList.setEnforcePriceLimit(Parameter.BOOLEAN.parse(enforcePriceLimit));
      priceList.setDefault(Parameter.BOOLEAN.parse(isDefault));

      OBDal.getInstance().save(priceList);
      OBDal.getInstance().flush();
    }

    // Create Price List Version
    PriceListVersion plVersion = findDALInstance(false, PriceListVersion.class, new Value("name",
        plvName), new Value("priceList", priceList), new Value("validFromDate", Parameter.DATE
        .parse(validFrom)));
    if (plVersion == null) {
      plVersion = OBProvider.getInstance().get(PriceListVersion.class);
      plVersion.setOrganization(rowOrganization);
      plVersion.setActive(true);
      plVersion.setPriceList(priceList);
      plVersion.setName(plvName);
      plVersion.setValidFromDate(Parameter.DATE.parse(validFrom));

      PriceListSchema defObjpls = findDALInstance(false, PriceListSchema.class, new Value("name",
          discountSchema));

      if (defObjpls == null) {
        defObjpls = OBProvider.getInstance().get(PriceListSchema.class);
        defObjpls.setOrganization(rowOrganization);
        defObjpls.setActive(true);
        defObjpls.setName(discountSchema);
        OBDal.getInstance().save(defObjpls);
        OBDal.getInstance().flush();

      }
      plVersion.setPriceListSchema(defObjpls);

      PriceListVersion objBasePriceListVersion = findDALInstance(false, PriceListVersion.class,
          new Value("name", plvBase));
      plVersion.setBasePriceListVersion(objBasePriceListVersion);

      OBDal.getInstance().save(plVersion);
      OBDal.getInstance().flush();
    }

    // Product price
    ProductPrice productPrice;
    Product objProduct = findDALInstance(false, Product.class, new Value("searchKey", productName));

    productPrice = findDALInstance(false, ProductPrice.class, new Value("product", objProduct),
        new Value("priceListVersion", plVersion));

    if (productPrice == null) {
      productPrice = OBProvider.getInstance().get(ProductPrice.class);
      productPrice.setOrganization(rowOrganization);
      productPrice.setActive(true);
      productPrice.setPriceListVersion(plVersion);
      productPrice.setProduct(objProduct);

    }

    productPrice.setPriceLimit(priceLimit == null ? BigDecimal.ZERO : Parameter.BIGDECIMAL
        .parse(priceLimit));
    productPrice.setListPrice(listPrice == null ? BigDecimal.ZERO : Parameter.BIGDECIMAL
        .parse(listPrice));
    productPrice.setStandardPrice(standardPrice == null ? BigDecimal.ZERO : Parameter.BIGDECIMAL
        .parse(standardPrice));

    OBDal.getInstance().save(productPrice);
    OBDal.getInstance().flush();

    // End process
    OBDal.getInstance().commitAndClose();

    return priceList;

  }

}
