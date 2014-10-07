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
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Category;
import org.apache.log4j.Logger;
/**
 *
 * @author adrian
 */
public class clientesProcess extends IdlServiceJava {

    @Override
    public String getEntityName() {
        return "Productos Tegik";
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter("Organization", Parameter.STRING),
	    new Parameter("SearchKey", Parameter.STRING),
            new Parameter("Name", Parameter.STRING),
            new Parameter("NombreFiscal", Parameter.STRING),
            new Parameter("RFC", Parameter.STRING),
            new Parameter("LimiteCredito", Parameter.STRING),
            new Parameter("NoReferencia", Parameter.STRING),
            new Parameter("Descripcion", Parameter.STRING),
            new Parameter("MetodoPago", Parameter.STRING),
	    new Parameter("Banco", Parameter.STRING),
            new Parameter("ListaPrecios", Parameter.STRING),
            new Parameter("TerminoPago", Parameter.STRING),
	    new Parameter("Categoria", Parameter.STRING),
            new Parameter("Activo", Parameter.STRING),
	    new Parameter("iscustomer", Parameter.STRING),
	    new Parameter("isvendor", Parameter.STRING)};
    }

    @Override
    protected Object[] validateProcess(Validator validator, String... values) throws Exception {
        return values;
    }

  @Override
  public BaseOBObject internalProcess(Object... values) throws Exception {

    return createCliente((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4], (String) values[5], (String) values[6],
        (String) values[7], (String) values[8], (String) values[9], (String) values[10], 
	(String) values[11], (String) values[12], (String) values[13], (String) values[14], (String) values[15]);
  }

  public BaseOBObject createCliente(final String strOrganizacion, final String strIdentificador, final String strNombre,
      final String strNombreFiscal, final String strRFC, final String strLimiteCredito, final String strReferencia,
      final String strDescripcion, final String strMetodoPago, final String strBanco, 
      final String strListaPrecios, final String strTerminoPago, final String strCategoria, final String strActivo,
      final String strIscustomer, final String strIsvendor)
      throws Exception {

    Logger log4j = Logger.getLogger(clientesProcess.class);

    BusinessPartner bpExist = findDALInstance(false, BusinessPartner.class, new Value("name", strNombre));
    if (bpExist != null) {
      //throw new OBException(Utility.messageBD(conn, "El cliente ya existe", vars.getLanguage()) + strNombre);
       return null;
    }

    BusinessPartner cliente = OBProvider.getInstance().get(BusinessPartner.class);
    cliente.setActive(DALUtils.getBoolean(strActivo));
    cliente.setCustomer(DALUtils.getBoolean(strIscustomer));
    cliente.setVendor(DALUtils.getBoolean(strIsvendor));
    
    final OBCriteria<Organization> orglist = OBDal.getInstance().createCriteria(Organization.class);
             orglist.add(Expression.eq(Product.PROPERTY_NAME, strOrganizacion));

    for (Organization orgObject : orglist.list()) {
	  cliente.setOrganization(orgObject);
    }
    
    cliente.setSearchKey(strIdentificador);
    cliente.setName(strNombre);
    cliente.setDescription(strDescripcion);
    cliente.setName2(strNombreFiscal);
    cliente.setTaxID(strRFC);
    cliente.setCreditLimit(Parameter.BIGDECIMAL.parse(strLimiteCredito));
    cliente.setReferenceNo(strReferencia);

    PaymentTerm paymentTerm = findDALInstance(false, PaymentTerm.class, new Value("searchKey", strTerminoPago));
    if (paymentTerm == null) {
      throw new OBException(Utility.messageBD(conn,  "No existen las condiciones de pago: " + strTerminoPago, vars.getLanguage()));
    } else {
      cliente.setPaymentTerms(paymentTerm);
    }
  
    /*FIN_FinancialAccount banco = findDALInstance(false, FIN_FinancialAccount.class, new Value("name", strBanco));
    if (banco == null && (strBanco != null || !strBanco.equals(""))) {
      throw new OBException(Utility.messageBD(conn,  "No existe el banco: " + strBanco, vars.getLanguage()));
    } else {
      cliente.setPOFinancialAccount(banco);
    }*/

    Category categoria = findDALInstance(false, Category.class, new Value("searchKey", strCategoria));
    if (categoria == null) {
      throw new OBException(Utility.messageBD(conn,  "No existe la categoria: " + strCategoria, vars.getLanguage()));
    } else {
      cliente.setBusinessPartnerCategory(categoria);
    }

    PriceList pricelist = findDALInstance(false, PriceList.class, new Value("name", strListaPrecios));
    if (pricelist == null) {
      throw new OBException(Utility.messageBD(conn,  "No existe la lista de precios: " + strListaPrecios, vars.getLanguage()));
    } else {
      cliente.setPriceList(pricelist);
    }

    FIN_PaymentMethod metodoPago = findDALInstance(false, FIN_PaymentMethod.class, new Value("name", strMetodoPago));
    if (metodoPago == null && (strMetodoPago != null || !strMetodoPago.equals(""))) {
      throw new OBException(Utility.messageBD(conn,  "No existe el metodo de pago: " + strMetodoPago, vars.getLanguage()));
    } else {
      cliente.setPaymentMethod(metodoPago);
    }

    OBDal.getInstance().save(cliente);
    OBDal.getInstance().flush();

    // End process
    OBDal.getInstance().commitAndClose();

    return cliente;
  }
}
