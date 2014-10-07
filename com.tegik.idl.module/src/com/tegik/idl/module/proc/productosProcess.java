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
import org.hibernate.criterion.Expression;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.model.common.enterprise.Organization;
import org.apache.log4j.Logger;

/**
 *
 * @author adrian
 */
public class productosProcess extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "Productos Tegik";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("Organization", Parameter.STRING), //0
            new Parameter("SearchKey", Parameter.STRING),  //1
            new Parameter("Name", Parameter.STRING),  //2
            new Parameter("Description", Parameter.STRING),  //3
            new Parameter("UPCEAN", Parameter.STRING),  //4
            new Parameter("ProductCategory", Parameter.STRING),  //5
           // new Parameter("UOM", Parameter.STRING),
            new Parameter("ProductType", Parameter.STRING),  //6
            //new Parameter("Production", Parameter.STRING),
            //new Parameter("BillOfMaterial", Parameter.STRING),
            //new Parameter("Discontinued", Parameter.STRING),
            //new Parameter("CostType", Parameter.STRING),
            //new Parameter("AttributeSet", Parameter.STRING),
            //new Parameter("AttributeValue", Parameter.STRING),
            new Parameter("Stocked", Parameter.STRING),  //7
            new Parameter("Purchase", Parameter.STRING),   //8
            new Parameter("Sale", Parameter.STRING),   //9
	    new Parameter("Width", Parameter.STRING),   //10
            new Parameter("Heigth", Parameter.STRING),   //11
            new Parameter("Depth", Parameter.STRING),    //12
            new Parameter("TaxCategory", Parameter.STRING),  //13
            new Parameter("Accion", Parameter.STRING)};  //14
    }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
        validator.checkOrganization(values[0]);
        validator.checkNotNull(validator.checkString(values[1], 40), "SearchKey");
        validator.checkNotNull(validator.checkString(values[2], 60), "Name");
        validator.checkString(values[3], 255);
	//validator.checkString(values[4], 30);
        validator.checkNotNull(validator.checkString(values[5], 32, "Category"), "ProductCategory");
        //validator.checkNotNull(validator.checkString(values[6], 32, "UOM"), "UOM");
	validator.checkNotNull(validator.checkString(values[6], 60, "ProductType"), "ProductType");
        //validator.checkNotNull(validator.checkBoolean(values[8], "Production"), "Production");
        //validator.checkNotNull(validator.checkBoolean(values[9], "BillOfMaterials"), "BillOfMaterial");
	//validator.checkNotNull(validator.checkBoolean(values[10], "Discontinued"), "Discontinued");
        //validator.checkString(values[11], 32, "CostType");
	//validator.checkString(values[12], 60, "AttributeSet");
        //validator.checkString(values[13], 60, "AttributeSetValue");
        validator.checkNotNull(validator.checkBoolean(values[7], "Stocked"), "Stocked");
        validator.checkNotNull(validator.checkBoolean(values[8], "Purchase"), "Purchase");
        validator.checkNotNull(validator.checkBoolean(values[9], "Sale"), "Sale");
        validator.checkBigDecimal(values[10]);
	validator.checkBigDecimal(values[11]);
        validator.checkBigDecimal(values[12]);
        validator.checkNotNull(validator.checkString(values[13], 32, "TaxCategory"), "TaxCategory");
        //validator.checkBigDecimal(values[18]);
        //validator.checkBigDecimal(values[19]);
        //validator.checkBigDecimal(values[20]);
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {

    return createProduct((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[5], (String) values[6], (String) values[7],
        (String) values[8], (String) values[9], (String) values[10], (String) values[11],
        (String) values[12], (String) values[13], (String) values[14]);
  }

  public BaseOBObject createProduct(final String strOrganizacion, final String searchkey, final String name,
      final String description, final String productcategory, final String producttype, final String stocked, 
      final String purchase, final String sale, final String width, final String heigth, 
      final String depth, final String taxcategory, final String strAccion)
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
    Logger log4j2 = Logger.getLogger(productosProcess.class);
    
    log4j2.info("CSM > // " + strAccion);
    
    Product productExist = null;
    
    if (strAccion.equals("C")){ 
	  productExist = findDALInstance(false, Product.class, new Value("searchKey", searchkey));
	  if (productExist != null) {
	    throw new OBException(Utility.messageBD(conn, "IDL_PR_EXISTS", vars.getLanguage()) + searchkey);
	  }else
	  {
	    productExist = OBProvider.getInstance().get(Product.class);
	  }
    }else
    {
	  productExist = findDALInstance(false, Product.class, new Value("searchKey", searchkey));
	  if (productExist == null) {
	    throw new OBException(Utility.messageBD(conn, "No existe el producto -- ", vars.getLanguage()) + searchkey);
	  }
	  
    }

    String uom = "Piezas";

    //Product productExist = OBProvider.getInstance().get(Product.class);
    productExist.setActive(true);
    
    final OBCriteria<Organization> orglist = OBDal.getInstance().createCriteria(Organization.class);
             orglist.add(Expression.eq(Product.PROPERTY_NAME, strOrganizacion));

    for (Organization orgObject : orglist.list()) {
	  productExist.setOrganization(orgObject);
    }
    
    productExist.setSearchKey(searchkey);
    productExist.setName(name);
    productExist.setDescription(description);
    //productExist.setUPCEAN(upcean);

    // Search Default Category
    ProductCategory productCategory = findDALInstance(false, ProductCategory.class, new Value(
        "searchKey", productcategory));
    if (productCategory == null) {
      /*
      ProductCategory proCat = OBProvider.getInstance().get(ProductCategory.class);
      proCat.setActive(true);
      proCat.setOrganization(rowOrganization);
      proCat.setName(productcategory);
      proCat.setSearchKey(productcategory);
      proCat.setPlannedMargin(BigDecimal.ZERO);
      OBDal.getInstance().save(proCat);
      OBDal.getInstance().flush();
      productExist.setProductCategory(proCat);
      */
      //Mensaje de error

      throw new OBException(Utility.messageBD(conn, "No existe la categoría " + productcategory, vars.getLanguage()));

    } else {
      productExist.setProductCategory(productCategory);
    }

    // Search Default UOM
    UOM unitOfMeasure = findDALInstance(false, UOM.class, new Value("name", uom));
    // Search Default Standard and Costing precision
    //String defStdPrecision = searchDefaultValue("Product", "StandardPrecision", null);
    //String defCostingPrecision = searchDefaultValue("Product", "CostingPrecision", null);

    if (unitOfMeasure == null) {
      /*
      UOM uomCreated = OBProvider.getInstance().get(UOM.class);
      uomCreated.setActive(true);
      uomCreated.setOrganization(DALUtils.findOrganization("0"));
      uomCreated.setName(uom);
      uomCreated.setEDICode(uom.length() > 2 ? uom.substring(0, 2) : uom);
      uomCreated.setStandardPrecision(Parameter.LONG.parse(defStdPrecision));
      uomCreated.setCostingPrecision(Parameter.LONG.parse(defCostingPrecision));
      OBDal.getInstance().save(uomCreated);
      OBDal.getInstance().flush();
      productExist.setUOM(uomCreated);
      */
      //Mensaje de error
      throw new OBException(Utility.messageBD(conn, "No existe la unidad de medida " + uom, vars.getLanguage()));

    } else {
      productExist.setUOM(unitOfMeasure);
    }

    productExist.setProductType(producttype);

    // Search Default value for stocked, purchase and sale
    productExist.setStocked(Parameter.BOOLEAN.parse(stocked));
    productExist.setPurchase(Parameter.BOOLEAN.parse(purchase));
    productExist.setSale(Parameter.BOOLEAN.parse(sale));
    /*
    productExist.setProduction(Parameter.BOOLEAN.parse(production));
    productExist.setBillOfMaterials(Parameter.BOOLEAN.parse(billofmaterial));
    productExist.setDiscontinued(Parameter.BOOLEAN.parse(discontinued));
    */
    productExist.setCostType("AV");
    productExist.setShelfWidth(Parameter.BIGDECIMAL.parse(width));
    productExist.setShelfHeight(Parameter.BIGDECIMAL.parse(heigth));
    productExist.setShelfDepth(Parameter.BIGDECIMAL.parse(depth));
    

    // Search Default value for Tax Category
    TaxCategory taxCategory = findDALInstance(false, TaxCategory.class, new Value("name",
        taxcategory));
    if (taxCategory == null) {
      /*
      TaxCategory taxCatCreated = OBProvider.getInstance().get(TaxCategory.class);
      taxCatCreated.setActive(true);
      taxCatCreated.setOrganization(rowOrganization);
      taxCatCreated.setName(taxcategory);
      taxCatCreated.setDescription("Created using default values");
      OBDal.getInstance().save(taxCatCreated);
      OBDal.getInstance().flush();
      productExist.setTaxCategory(taxCatCreated);
      */
      //Mensaje de error
      throw new OBException(Utility.messageBD(conn,  "No existe la categoría de impuestos " + taxcategory, vars.getLanguage()));

    } else {
      productExist.setTaxCategory(taxCategory);
    }

    OBDal.getInstance().save(productExist);
    OBDal.getInstance().flush();

    // End process
    OBDal.getInstance().commitAndClose();

    return productExist;
  }
}
