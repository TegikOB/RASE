/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.module.idljava.proc;


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

/**
 *
 * @author adrian
 */
public class SampleProductsProcess extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "Simple Products";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("Organization", Parameter.STRING),
            new Parameter("SearchKey", Parameter.STRING),
            new Parameter("Name", Parameter.STRING),
            new Parameter("Description", Parameter.STRING),
            new Parameter("UPCEAN", Parameter.STRING),
            new Parameter("ProductCategory", Parameter.STRING),
            new Parameter("UOM", Parameter.STRING),
            new Parameter("ProductType", Parameter.STRING),
            new Parameter("Production", Parameter.STRING),
            new Parameter("BillOfMaterial", Parameter.STRING),
            new Parameter("Discontinued", Parameter.STRING),
            new Parameter("CostType", Parameter.STRING),
            new Parameter("AttributeSet", Parameter.STRING),
            new Parameter("AttributeValue", Parameter.STRING),
            new Parameter("Stocked", Parameter.STRING),
            new Parameter("Purchase", Parameter.STRING),
            new Parameter("Sale", Parameter.STRING),
            new Parameter("TaxCategory", Parameter.STRING),
            new Parameter("StandardCost", Parameter.STRING),
            new Parameter("PriceSales", Parameter.STRING),
            new Parameter("PricePurchase", Parameter.STRING) };
    }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
        validator.checkOrganization(values[0]);
        validator.checkNotNull(validator.checkString(values[1], 40), "SearchKey");
        validator.checkNotNull(validator.checkString(values[2], 60), "Name");
        validator.checkString(values[3], 255);
	validator.checkString(values[4], 30);
        validator.checkNotNull(validator.checkString(values[5], 32, "Category"), "ProductCategory");
        validator.checkNotNull(validator.checkString(values[6], 32, "UOM"), "UOM");
	validator.checkNotNull(validator.checkString(values[7], 60, "ProductType"), "ProductType");
        validator.checkNotNull(validator.checkBoolean(values[8], "Production"), "Production");
        validator.checkNotNull(validator.checkBoolean(values[9], "BillOfMaterials"), "BillOfMaterial");
	validator.checkNotNull(validator.checkBoolean(values[10], "Discontinued"), "Discontinued");
        validator.checkString(values[11], 32, "CostType");
	validator.checkString(values[12], 60, "AttributeSet");
        validator.checkString(values[13], 60, "AttributeSetValue");
        validator.checkNotNull(validator.checkBoolean(values[14], "Stocked"), "Stocked");
        validator.checkNotNull(validator.checkBoolean(values[15], "Purchase"), "Purchase");
        validator.checkNotNull(validator.checkBoolean(values[16], "Sale"), "Sale");
        validator.checkNotNull(validator.checkString(values[17], 32, "TaxCategory"), "TaxCategory");
        validator.checkBigDecimal(values[18]);
        validator.checkBigDecimal(values[19]);
        validator.checkBigDecimal(values[20]);
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {

    return createProduct((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4], (String) values[5], (String) values[6],
        (String) values[7], (String) values[8], (String) values[9], (String) values[10],
        (String) values[11], (String) values[12], (String) values[13], (String) values[14],
        (String) values[15], (String) values[16], (String) values[17], (String) values[18],
        (String) values[19], (String) values[20]);
  }

  public BaseOBObject createProduct(final String Organization, final String searchkey,
      final String name, final String description, final String upcean,
      final String productcategory, final String uom, final String producttype,

      final String production, final String billofmaterial, final String discontinued,
      final String costtype, final String attributeset, final String attributevalue,

      final String stocked, final String purchase, final String sale, final String taxcategory,
      final String standardcost, final String pricesales, final String pricepurchase)
      throws Exception {

    // INITIAL AND DEFAULT VALUES

    // Product Category
    // + By default could be "Standard Category"

    // UOM:
    // + UOM EDICode
    // + Standard precision
    // + Costing precision

    // Product type
    // + By default is Item

    // Tax Category
    // + Name

    // Price List
    // + Name for PriceList

    // PRODUCT
    Product productExist = findDALInstance(false, Product.class, new Value("searchKey", searchkey));
    if (productExist != null) {
      throw new OBException(Utility.messageBD(conn, "IDL_PR_EXISTS", vars.getLanguage())
          + searchkey);
    }

    Product product = OBProvider.getInstance().get(Product.class);
    product.setActive(true);
    product.setOrganization(rowOrganization);
    product.setSearchKey(searchkey);
    product.setName(name);
    product.setDescription(description);
    product.setUPCEAN(upcean);

    // Search Default Category
    ProductCategory productCategory = findDALInstance(false, ProductCategory.class, new Value(
        "searchKey", productcategory));
    if (productCategory == null) {
      ProductCategory proCat = OBProvider.getInstance().get(ProductCategory.class);
      proCat.setActive(true);
      proCat.setOrganization(rowOrganization);
      proCat.setName(productcategory);
      proCat.setSearchKey(productcategory);
      proCat.setPlannedMargin(BigDecimal.ZERO);
      OBDal.getInstance().save(proCat);
      OBDal.getInstance().flush();
      product.setProductCategory(proCat);

    } else {
      product.setProductCategory(productCategory);
    }

    // Search Default UOM
    UOM unitOfMeasure = findDALInstance(false, UOM.class, new Value("name", uom));
    // Search Default Standard and Costing precision
    String defStdPrecision = searchDefaultValue("Product", "StandardPrecision", null);
    String defCostingPrecision = searchDefaultValue("Product", "CostingPrecision", null);

    if (unitOfMeasure == null) {
      UOM uomCreated = OBProvider.getInstance().get(UOM.class);
      uomCreated.setActive(true);
      uomCreated.setOrganization(DALUtils.findOrganization("0"));
      uomCreated.setName(uom);
      uomCreated.setEDICode(uom.length() > 2 ? uom.substring(0, 2) : uom);
      uomCreated.setStandardPrecision(Parameter.LONG.parse(defStdPrecision));
      uomCreated.setCostingPrecision(Parameter.LONG.parse(defCostingPrecision));
      OBDal.getInstance().save(uomCreated);
      OBDal.getInstance().flush();
      product.setUOM(uomCreated);

    } else {
      product.setUOM(unitOfMeasure);
    }

    product.setProductType(getReferenceValue("M_Product_ProductType", producttype));

    // Search Default value for stocked, purchase and sale
    product.setStocked(Parameter.BOOLEAN.parse(stocked));
    product.setPurchase(Parameter.BOOLEAN.parse(purchase));
    product.setSale(Parameter.BOOLEAN.parse(sale));
    product.setProduction(Parameter.BOOLEAN.parse(production));
    product.setBillOfMaterials(Parameter.BOOLEAN.parse(billofmaterial));
    product.setDiscontinued(Parameter.BOOLEAN.parse(discontinued));
    product.setCostType(getReferenceValue("Cost Type", costtype));

    // Search Default value for Tax Category
    TaxCategory taxCategory = findDALInstance(false, TaxCategory.class, new Value("name",
        taxcategory));
    if (taxCategory == null) {
      TaxCategory taxCatCreated = OBProvider.getInstance().get(TaxCategory.class);
      taxCatCreated.setActive(true);
      taxCatCreated.setOrganization(rowOrganization);
      taxCatCreated.setName(taxcategory);
      taxCatCreated.setDescription("Created using default values");
      OBDal.getInstance().save(taxCatCreated);
      OBDal.getInstance().flush();
      product.setTaxCategory(taxCatCreated);

    } else {
      product.setTaxCategory(taxCategory);
    }

    if (standardcost != null && !standardcost.equals("")) {
      product.setStandardCost(Parameter.BIGDECIMAL.parse(standardcost));
    }

    AttributeSet attset = findDALInstance(false, AttributeSet.class,
        new Value("name", attributeset));
    product.setAttributeSet(attset);

    if (attset != null && attributevalue != null) {

      String defAttSetValueType = searchDefaultValue("Product", "AttributeSetValueType", null);
      String attSetValueType = getReferenceValue("Use Attribute Set Value As", defAttSetValueType);

      // for some reason these entities need
      // to be processed in admin mode.
      OBContext.getOBContext().setInAdministratorMode(true);

      AttributeSetInstance attsetinst = OBProvider.getInstance().get(AttributeSetInstance.class);
      attsetinst.setActive(true);
      attsetinst.setOrganization(rowOrganization);
      attsetinst.setAttributeSet(attset); // not directly readable

      if (attSetValueType == null) {
        attSetValueType = "D";
      }
      product.setUseAttributeSetValueAs(attSetValueType);

      if (attset.isSerialNo()) {
        attsetinst.setDescription("#" + attributevalue);
        attsetinst.setSerialNo(attributevalue);
      } else if (attset.isLot()) {
        attsetinst.setDescription("L" + attributevalue);
        attsetinst.setLotName(attributevalue);
      } else if (attset.getAttributeUseList().size() == 1) {

        Attribute att = attset.getAttributeUseList().get(0).getAttribute();

        AttributeValue attvalue = findDALInstance(false, AttributeValue.class, new Value(
            AttributeValue.PROPERTY_ATTRIBUTE, att), new Value("name", attributevalue));

        if (attvalue != null) {
          attsetinst.setDescription(attvalue.getName());

          AttributeInstance attinst = OBProvider.getInstance().get(AttributeInstance.class);
          attinst.setActive(true);
          attinst.setOrganization(rowOrganization);
          attinst.setAttributeSetValue(attsetinst);
          attinst.setAttribute(attvalue.getAttribute());
          attinst.setAttributeValue(attvalue);

          attsetinst.getAttributeInstanceList().add(attinst);
        }
      }

      OBDal.getInstance().save(attsetinst);
      OBDal.getInstance().flush();
      product.setAttributeSetValue(attsetinst);

      OBContext.getOBContext().setInAdministratorMode(false);
    }

    OBDal.getInstance().save(product);
    OBDal.getInstance().flush();

    // Time part of the date must be set to 0
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    // 1) Create PriceListSchema
    if ((pricesales != null && !pricesales.equals(""))
        || (pricepurchase != null && !pricepurchase.equals(""))) {

      String priceListSchema = searchDefaultValue("Product", "PriceListSchema", null);
      if (priceListSchema == null) {
        priceListSchema = "Default Price List Schema";
      }
      PriceListSchema pListSchema = findDALInstance(false, PriceListSchema.class, new Value("name",
          priceListSchema));
      if (pListSchema == null) {
        pListSchema = OBProvider.getInstance().get(PriceListSchema.class);
        pListSchema.setActive(true);
        pListSchema.setOrganization(rowOrganization);
        pListSchema.setName(priceListSchema);
        pListSchema.setValidFromDate(cal.getTime());
        OBDal.getInstance().save(pListSchema);
        OBDal.getInstance().flush();

        PriceListSchemeLine pSaleListSchemaLine = OBProvider.getInstance().get(
            PriceListSchemeLine.class);
        pSaleListSchemaLine.setActive(true);
        pSaleListSchemaLine.setOrganization(rowOrganization);
        pSaleListSchemaLine.setPriceListSchema(pListSchema);
        pSaleListSchemaLine.setSequenceNumber(10L);
        pSaleListSchemaLine.setActive(true);

        pSaleListSchemaLine.setConversionRateType("S");
        pSaleListSchemaLine.setConversionDate(new java.util.Date());
        pSaleListSchemaLine.setBaseListPrice("L");
        pSaleListSchemaLine.setSurchargeListPriceAmount(BigDecimal.ZERO);
        pSaleListSchemaLine.setListPriceDiscount(BigDecimal.ZERO);
        pSaleListSchemaLine.setStandardBasePrice("S");
        pSaleListSchemaLine.setSurchargeStandardPriceAmount(BigDecimal.ZERO);
        pSaleListSchemaLine.setStandardPriceDiscount(BigDecimal.ZERO);
        pSaleListSchemaLine.setBaseLimitPrice("X");
        pSaleListSchemaLine.setSurchargePriceLimitAmount(BigDecimal.ZERO);
        pSaleListSchemaLine.setPriceLimitDiscount(BigDecimal.ZERO);
        pSaleListSchemaLine.setMinListPriceMargin(BigDecimal.ZERO);
        pSaleListSchemaLine.setMaxPriceLimitMargin(BigDecimal.ZERO);
        pSaleListSchemaLine.setListPriceRounding("C");
        pSaleListSchemaLine.setMinStandardPriceMargin(BigDecimal.ZERO);
        pSaleListSchemaLine.setMaxStandardMargin(BigDecimal.ZERO);
        pSaleListSchemaLine.setStandardPriceRounding("C");
        pSaleListSchemaLine.setLimitPriceMinMargin(BigDecimal.ZERO);
        pSaleListSchemaLine.setMaxPriceLimitMargin(BigDecimal.ZERO);
        pSaleListSchemaLine.setPriceLimitRounding("C");
        pSaleListSchemaLine.setMaxListPriceMargin(BigDecimal.ZERO);

        OBDal.getInstance().save(pSaleListSchemaLine);
        OBDal.getInstance().flush();
      }

      // 1) Create PriceList
      // 2) Create PriceListVersion

      if (pricesales != null && !pricesales.equals("")) {

        String priceListSale = searchDefaultValue("Product", "PriceListSale", null);
        if (priceListSale == null) {
          priceListSale = "Default Sale Price List";
        }
        PriceList pSaleList = findDALInstance(false, PriceList.class, new Value("name",
            priceListSale));
        if (pSaleList == null) {
          // Search Default Currency
          Currency currencyinst = findDALInstance(false, Currency.class, new Value("iSOCode",
              searchDefaultValue("Product", "Currency", null)));

          pSaleList = OBProvider.getInstance().get(PriceList.class);
          pSaleList.setActive(true);
          pSaleList.setOrganization(rowOrganization);
          pSaleList.setName(priceListSale);
          pSaleList.setCurrency(currencyinst);
          pSaleList.setSalesPriceList(Boolean.TRUE);
          OBDal.getInstance().save(pSaleList);
          OBDal.getInstance().flush();
        }

        // Price
        String defPLVSale = searchDefaultValue("Product", "PriceListVersionSale", null);
        if (defPLVSale == null) {
          defPLVSale = "Default Sale Price List Version";
        }
        PriceListVersion plvSale = findDALInstance(false, PriceListVersion.class, new Value("name",
            defPLVSale));
        if (plvSale == null) {
          plvSale = OBProvider.getInstance().get(PriceListVersion.class);
          plvSale.setActive(true);
          plvSale.setOrganization(rowOrganization);
          plvSale.setName(defPLVSale);
          plvSale.setPriceList(pSaleList);
          plvSale.setPriceListSchema(pListSchema);
          plvSale.setValidFromDate(cal.getTime());
          OBDal.getInstance().save(plvSale);
          OBDal.getInstance().flush();
        }

        ProductPrice productPrice = OBProvider.getInstance().get(ProductPrice.class);
        productPrice.setActive(true);
        productPrice.setOrganization(rowOrganization);
        productPrice.setProduct(product);
        productPrice.setListPrice(new BigDecimal(pricesales));
        productPrice.setStandardPrice(new BigDecimal(pricesales));
        productPrice.setPriceLimit(new BigDecimal(pricesales));
        productPrice.setPriceListVersion(plvSale);
        OBDal.getInstance().save(productPrice);
        OBDal.getInstance().flush();
      }

      if (pricepurchase != null && !pricepurchase.equals("")) {

        String priceListPurchase = searchDefaultValue("Product", "PriceListPurchase", null);
        if (priceListPurchase == null) {
          priceListPurchase = "Default Purchase Price List";
        }
        PriceList pPurchaseList = findDALInstance(false, PriceList.class, new Value("name",
            priceListPurchase));
        if (pPurchaseList == null) {
          // Search Default Currency
          Currency currencyinst = findDALInstance(false, Currency.class, new Value("iSOCode",
              searchDefaultValue("Product", "Currency", null)));

          pPurchaseList = OBProvider.getInstance().get(PriceList.class);
          pPurchaseList.setActive(true);
          pPurchaseList.setOrganization(rowOrganization);
          pPurchaseList.setName(priceListPurchase);
          pPurchaseList.setCurrency(currencyinst);
          pPurchaseList.setSalesPriceList(Boolean.FALSE);
          OBDal.getInstance().save(pPurchaseList);
          OBDal.getInstance().flush();
        }

        // Price purchase
        String defPLVPurchase = searchDefaultValue("Product", "PriceListVersionPurchase", null);
        if (defPLVPurchase == null) {
          defPLVPurchase = "Default Purchase Price List Version";
        }
        PriceListVersion plvPurchase = findDALInstance(false, PriceListVersion.class, new Value(
            "name", defPLVPurchase));
        if (plvPurchase == null) {
          plvPurchase = OBProvider.getInstance().get(PriceListVersion.class);
          plvPurchase.setActive(true);
          plvPurchase.setOrganization(rowOrganization);
          plvPurchase.setName(defPLVPurchase);
          plvPurchase.setPriceList(pPurchaseList);
          plvPurchase.setPriceListSchema(pListSchema);
          plvPurchase.setValidFromDate(cal.getTime());
          OBDal.getInstance().save(plvPurchase);
          OBDal.getInstance().flush();
        }

        ProductPrice productPricePurchase = OBProvider.getInstance().get(ProductPrice.class);
        productPricePurchase.setProduct(product);
        productPricePurchase.setActive(true);
        productPricePurchase.setOrganization(rowOrganization);
        productPricePurchase.setStandardPrice(Parameter.BIGDECIMAL.parse(pricepurchase));
        productPricePurchase.setPriceLimit(Parameter.BIGDECIMAL.parse(pricepurchase));
        productPricePurchase.setListPrice(Parameter.BIGDECIMAL.parse(pricepurchase));
        productPricePurchase.setPriceListVersion(plvPurchase);
        OBDal.getInstance().save(productPricePurchase);
        OBDal.getInstance().flush();
      }
    }

    // End process
    OBDal.getInstance().commitAndClose();

    return product;
  }
}
