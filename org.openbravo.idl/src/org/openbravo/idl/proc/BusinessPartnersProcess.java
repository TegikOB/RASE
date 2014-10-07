/*
 ************************************************************************************
 * Copyright (C) 2010-2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.initial_data_load.businesspartnerjob_0_1.BusinessPartnerJob;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.businesspartner.TaxCategory;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Location;
import org.openbravo.model.common.geography.Region;
import org.openbravo.model.common.invoice.InvoiceSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.pricing.pricelist.PriceList;

/**
 * 
 * @author Sivaraman Rajagopal
 */
public class BusinessPartnersProcess extends IdlServiceETL {

  final BusinessPartnerJob job = new BusinessPartnerJob();

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
        new Parameter("SearchKey", Parameter.STRING), new Parameter("Name", Parameter.STRING),
        new Parameter("Description", Parameter.STRING), new Parameter("TaxID", Parameter.STRING),
        new Parameter("TaxExempt", Parameter.STRING),
        new Parameter("ReferenceNo", Parameter.STRING),
        new Parameter("Category", Parameter.STRING), new Parameter("Language", Parameter.STRING),
        new Parameter("Location1stLine", Parameter.STRING),
        new Parameter("Location2ndLine", Parameter.STRING),
        new Parameter("LocationPostalCode", Parameter.STRING),
        new Parameter("LocationCity", Parameter.STRING),
        new Parameter("LocationRegion", Parameter.STRING),
        new Parameter("LocationCountry", Parameter.STRING),
        new Parameter("LocationPhone", Parameter.STRING),
        new Parameter("LocationAlternativePhone", Parameter.STRING),
        new Parameter("LocationFax", Parameter.STRING),
        new Parameter("LocationShipToAddress", Parameter.STRING),
        new Parameter("LocationPayFromAddress", Parameter.STRING),
        new Parameter("LocationInvoiceToAddress", Parameter.STRING),
        new Parameter("LocationRemitToAddress", Parameter.STRING),
        new Parameter("ConsumptionDays", Parameter.STRING),
        new Parameter("BankAccountShowGeneric", Parameter.STRING),
        new Parameter("BankAccountShowIBAN", Parameter.STRING),
        new Parameter("BankAccountNumber", Parameter.STRING),
        new Parameter("BankAccountIBAN", Parameter.STRING),
        new Parameter("ContactUser", Parameter.STRING),
        new Parameter("ContactFirstName", Parameter.STRING),
        new Parameter("ContactLastName", Parameter.STRING),
        new Parameter("ContactDescription", Parameter.STRING),
        new Parameter("ContactEmail", Parameter.STRING),
        new Parameter("ContactPosition", Parameter.STRING),
        new Parameter("ContactPhone", Parameter.STRING),
        new Parameter("ContactAlternativePhone", Parameter.STRING),
        new Parameter("ContactFax", Parameter.STRING), new Parameter("Vendor", Parameter.STRING),
        new Parameter("VendorPaymentMethod", Parameter.STRING),
        new Parameter("VendorPaymentTerms", Parameter.STRING),
        new Parameter("VendorPurchasePriceList", Parameter.STRING),
        new Parameter("VendorTaxCategory", Parameter.STRING),
        new Parameter("VendorFinancialAccount", Parameter.STRING),
        new Parameter("Customer", Parameter.STRING),
        new Parameter("CustomerPaymentMethod", Parameter.STRING),
        new Parameter("CustomerPaymentTerms", Parameter.STRING),
        new Parameter("CustomerPriceList", Parameter.STRING),
        new Parameter("CustomerTaxCategory", Parameter.STRING),
        new Parameter("CustomerFinancialAccount", Parameter.STRING),
        new Parameter("CustomerInvoiceSchedule", Parameter.STRING),
        new Parameter("CustomerInvoicesTerms", Parameter.STRING),
        new Parameter("Employee", Parameter.STRING),
        new Parameter("EmployeeSalesRepresentative", Parameter.STRING) };
  }

  @Override
  protected BaseOBObject internalProcess(Object... values) throws Exception {

    return createBusinessPartner((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4], (String) values[5], (String) values[6],
        (String) values[7], (String) values[8],
        (( (String) values[9] != null) ? ((String) values[9]).replaceAll("\\r\\n|\\r|\\n", " ") : null),
        (( (String) values[10]!= null) ? ((String) values[10]).replaceAll("\\r\\n|\\r|\\n", " ") : null),
        (String) values[11], (String) values[12], (String) values[13], (String) values[14],
        (String) values[15], (String) values[16], (String) values[17], (String) values[18],
        (String) values[19], (String) values[20], (String) values[21], (String) values[22],
        (String) values[23], (String) values[24], (String) values[25], (String) values[26],
        (String) values[27], (String) values[28], (String) values[29], (String) values[30],
        (String) values[31], (String) values[32], (String) values[33], (String) values[34],
        (String) values[35], (String) values[36], (String) values[37], (String) values[38],
        (String) values[39], (String) values[40], (String) values[41], (String) values[42],
        (String) values[43], (String) values[44], (String) values[45], (String) values[46],
        (String) values[47], (String) values[48], (String) values[49], (String) values[50],
        (String) values[51]);
  }

  public BaseOBObject createBusinessPartner(
      // General tab
      final String organization, final String searchkey, final String name,
      final String description, final String taxid, final String taxexempt,
      final String referenceno, final String category, final String language,
      final String address1stline, final String address2ndline, final String postalcode,
      final String city, final String region,
      final String country,
      final String phone,
      final String alternativephone,
      final String fax,

      final String shipToAddress,
      final String payFromAddress,
      final String invoiceToAddress,
      final String remitToAddress,

      final String cunsumptiondays,

      // Bank Account tab
      final String showGeneric,
      final String showIban,
      final String bankAccountNo,
      final String iban,

      // Contact tab
      final String contactuser, final String firstname, final String lastname,
      final String contactdescription, final String email, final String position,
      final String contactphone,
      final String contactalternativephone,
      final String contactfax,

      // Vendor tab
      final String vendor, final String popaymentmethod, final String popaymentterms,
      final String purchasepricelist, final String taxcategory,
      final String pofinancialaccount,

      // Customer tab
      final String customer, final String cuspaymentmethod, final String paymentterm,
      final String pricelist, final String soTaxCategory, final String cusfinancialaccount,
      final String soInvoiceSchedule, final String soInvoicesTerms,

      // Employee tab
      final String employee, final String issalesrepresentative

  ) throws Exception {

    // Business Partner
    BusinessPartner bp = findDALInstance(false, BusinessPartner.class, new Value("searchKey",
        searchkey), new Value("name", name), new Value("taxID", taxid));

    if (bp != null) {
      // Insert only new Location, Contact and Bank Account

      // Bank Account
      if ((DALUtils.getBoolean(showGeneric) && bankAccountNo != null)
          || (DALUtils.getBoolean(showIban) && iban != null)) {

        org.openbravo.model.common.businesspartner.BankAccount bankAccount = null;
        if (DALUtils.getBoolean(showGeneric) && bankAccountNo != null) {
          bankAccount = findDALInstance(false,
              org.openbravo.model.common.businesspartner.BankAccount.class, new Value(
                  org.openbravo.model.common.businesspartner.BankAccount.PROPERTY_BUSINESSPARTNER,
                  bp), new Value(
                  org.openbravo.model.common.businesspartner.BankAccount.PROPERTY_ACCOUNTNO,
                  bankAccountNo));
        } else if (DALUtils.getBoolean(showIban) && iban != null) {
          bankAccount = findDALInstance(false,
              org.openbravo.model.common.businesspartner.BankAccount.class, new Value(
                  org.openbravo.model.common.businesspartner.BankAccount.PROPERTY_BUSINESSPARTNER,
                  bp), new Value(
                  org.openbravo.model.common.businesspartner.BankAccount.PROPERTY_IBAN, iban));
        }

        if (bankAccount == null) {
          bankAccount = OBProvider.getInstance().get(
              org.openbravo.model.common.businesspartner.BankAccount.class);
          bankAccount.setOrganization(rowOrganization);
          bankAccount.setActive(true);
          bankAccount.setBusinessPartner(bp);
          bankAccount.setAccountNo(bankAccountNo);
          bankAccount.setShowGeneric(DALUtils.getBoolean(showGeneric));
          bankAccount.setIBAN(iban);
          bankAccount.setShowIBAN(DALUtils.getBoolean(showIban));

          if (iban != null) {
            bankAccount.setCountry(DALUtils.getCountryByIban(iban));
          }

          OBDal.getInstance().save(bankAccount);
          OBDal.getInstance().flush();
        }
      }

      org.openbravo.model.common.businesspartner.Location bpLocation = null;
      // Business Partner Location
      if (country != null) {

        StringBuilder sb = new StringBuilder();
        for (String s : OBContext.getOBContext().getOrganizationStructureProvider()
            .getNaturalTree(rowOrganization.getId())) {
          sb.append("'" + s + "',");
        }

        String city1 = (city != null) ? city : null;
        String city2 = (city != null) ? null : "1";
        String address1a = (address1stline != null) ? address1stline : null;
        String address1b = (address1stline != null) ? null : "1";
        String address2a = (address2ndline != null) ? address2ndline : null;
        String address2b = (address2ndline != null) ? null : "1";
        String postal1 = (postalcode != null) ? postalcode : null;
        String postal2 = (postalcode != null) ? null : "1";
        String country1 = (country != null) ? country : null;
        String country2 = (country != null) ? null : "1";
        String region1 = (region != null) ? region : null;
        String region2 = (region != null) ? null : "1";

        BusinessPartnerLocationData[] bpld = BusinessPartnerLocationData
            .select(conn, bp.getId(), sb.toString().substring(0, sb.toString().length() - 1),
                city1, city2, address1a, address1b, address2a, address2b, postal1, postal2,
                country1, country2, region1, region2);

        if (bpld == null || bpld.length == 0) {
          Location location = OBProvider.getInstance().get(Location.class);
          location.setOrganization(rowOrganization);
          location.setActive(true);
          location.setAddressLine1(address1stline);
          location.setAddressLine2(address2ndline);
          location.setPostalCode(postalcode);
          location.setCityName(city);
          Country countryvalue = findDALInstance(false, Country.class, new Value("name", country));
          location.setCountry(countryvalue);
          location.setRegion(findDALInstance(false, Region.class,
              new Value("country", countryvalue), new Value("name", region)));
          OBDal.getInstance().save(location);
          OBDal.getInstance().flush();

          bpLocation = OBProvider.getInstance().get(
              org.openbravo.model.common.businesspartner.Location.class);
          bpLocation.setOrganization(rowOrganization);
          bpLocation.setActive(true);
          bpLocation.setBusinessPartner(bp);
          bpLocation.setFax(fax);
          bpLocation.setPhone(phone);
          bpLocation.setAlternativePhone(alternativephone);
          bpLocation.setLocationAddress(location);
          bpLocation.setShipToAddress(DALUtils.getBoolean(shipToAddress));
          bpLocation.setPayFromAddress(DALUtils.getBoolean(payFromAddress));
          bpLocation.setInvoiceToAddress(DALUtils.getBoolean(invoiceToAddress));
          bpLocation.setRemitToAddress(DALUtils.getBoolean(remitToAddress));
          OBDal.getInstance().save(bpLocation);
          OBDal.getInstance().flush();
        }

      }

      // Contact
      if (contactuser != null) {
        User contact = findDALInstance(false, User.class,
            new Value(User.PROPERTY_NAME, contactuser), new Value(User.PROPERTY_FIRSTNAME,
                firstname), new Value(User.PROPERTY_LASTNAME, lastname), new Value(
                User.PROPERTY_BUSINESSPARTNER, bp));
        if (contact == null) {
          contact = OBProvider.getInstance().get(User.class);
          contact.setOrganization(rowOrganization);
          contact.setActive(true);
          contact.setBusinessPartner(bp);
          // get existing location if not created.
          if (bpLocation == null) {
            // issue 24474, force reload of business partner to prevent LazyInitializationException
            bp = OBDal.getInstance().get(BusinessPartner.class, bp.getId());
            java.util.List<org.openbravo.model.common.businesspartner.Location> bplocations = bp
                .getBusinessPartnerLocationList();
            for (org.openbravo.model.common.businesspartner.Location l : bplocations) {
              if (strequal(city, l.getLocationAddress().getCityName())
                  && strequal(address1stline, l.getLocationAddress().getAddressLine1())
                  && strequal(address2ndline, l.getLocationAddress().getAddressLine2())
                  && strequal(postalcode, l.getLocationAddress().getPostalCode())
                  && strequal(country, l.getLocationAddress().getCountry() != null ? l
                      .getLocationAddress().getCountry().getName() : null)
                  && strequal(region, l.getLocationAddress().getRegion() != null ? l
                      .getLocationAddress().getRegion().getName() : null)) {
                bpLocation = l;
              }
            }
          }
          contact.setPartnerAddress(bpLocation);
          contact.setName(contactuser);
          contact.setFirstName(firstname);
          contact.setLastName(lastname);
          contact.setDescription(contactdescription);
          contact.setEmail(email);
          contact.setPosition(position);
          contact.setPhone(contactphone);
          contact.setAlternativePhone(contactalternativephone);
          contact.setFax(contactfax);
          OBDal.getInstance().save(contact);
          OBDal.getInstance().flush();
        }
      }
    } else {

      BusinessPartner bpExist = findDALInstance(false, BusinessPartner.class, new Value(
          "searchKey", searchkey));
      if (bpExist != null) {
        throw new OBException(Utility.messageBD(conn, "IDL_BP_EXISTS", vars.getLanguage())
            + searchkey);
      }

      bp = OBProvider.getInstance().get(BusinessPartner.class);
      bp.setOrganization(rowOrganization);
      bp.setActive(true);
      bp.setSearchKey(searchkey);
      bp.setName(name);
      bp.setDescription(description);
      bp.setTaxID(taxid);
      bp.setTaxExempt(DALUtils.getBoolean(taxexempt));
      bp.setReferenceNo(referenceno);

      if (cunsumptiondays != null) {
        bp.setConsumptionDays(new Long(cunsumptiondays));
      }

      // Search Default Category
      Category bpCategory = findDALInstance(false, Category.class, new Value("searchKey", category));
      if (bpCategory == null) {
        bpCategory = OBProvider.getInstance().get(Category.class);
        bpCategory.setActive(true);
        bpCategory.setOrganization(rowOrganization);
        bpCategory.setName(category);
        bpCategory.setSearchKey(category);
        OBDal.getInstance().save(bpCategory);
        OBDal.getInstance().flush();
      }
      bp.setBusinessPartnerCategory(bpCategory);

      Language langinst = findDALInstance(false, Language.class, new Value("language", language));
      bp.setLanguage(langinst);

      bp.setVendor(Parameter.BOOLEAN.parse(vendor));
      bp.setCustomer(Parameter.BOOLEAN.parse(customer));
      bp.setEmployee(Parameter.BOOLEAN.parse(employee));

      bp.setSalesRepresentative(DALUtils.getBoolean(issalesrepresentative));

      if (bp.isVendor()) {
        // Form of payment
        FIN_PaymentMethod objPaymentMethod = findDALInstance(false, FIN_PaymentMethod.class,
            new Value("name", popaymentmethod));
        if (objPaymentMethod == null && popaymentmethod != null) {
          objPaymentMethod = OBProvider.getInstance().get(FIN_PaymentMethod.class);
          objPaymentMethod.setOrganization(rowOrganization);
          objPaymentMethod.setActive(true);
          objPaymentMethod.setName(popaymentmethod);
          OBDal.getInstance().save(objPaymentMethod);
          OBDal.getInstance().flush();
        }
        bp.setPOPaymentMethod(objPaymentMethod);
        // Payment term
        PaymentTerm paymentTerm = findDALInstance(false, PaymentTerm.class, new Value("searchKey",
            popaymentterms));
        bp.setPOPaymentTerms(paymentTerm);
        // Tax category
        TaxCategory taxCat = findDALInstance(false, TaxCategory.class, new Value("name",
            taxcategory));
        bp.setTaxCategory(taxCat);
        // Price List
        PriceList purchasePriceList = findDALInstance(false, PriceList.class, new Value("name",
            purchasepricelist), new Value("salesPriceList", false));
        bp.setPurchasePricelist(purchasePriceList);
        // BankAccount
        FIN_FinancialAccount objFinancialAccount = findDALInstance(false,
            FIN_FinancialAccount.class, new Value("name", pofinancialaccount));
        bp.setPOFinancialAccount(objFinancialAccount);
      }

      if (bp.isCustomer()) {
        FIN_PaymentMethod objPaymentMethod = findDALInstance(false, FIN_PaymentMethod.class,
            new Value("name", cuspaymentmethod));
        if (objPaymentMethod == null && cuspaymentmethod != null) {
          objPaymentMethod = OBProvider.getInstance().get(FIN_PaymentMethod.class);
          objPaymentMethod.setOrganization(rowOrganization);
          objPaymentMethod.setActive(true);
          objPaymentMethod.setName(cuspaymentmethod);
          OBDal.getInstance().save(objPaymentMethod);
          OBDal.getInstance().flush();
        }
        bp.setPaymentMethod(objPaymentMethod);

        // Invoices terms
        bp.setInvoiceTerms(getReferenceValue("C_Order InvoiceRule", soInvoicesTerms));

        // Invoices schedule
        InvoiceSchedule invSchedule = findDALInstance(false, InvoiceSchedule.class, new Value(
            "name", soInvoiceSchedule));
        bp.setInvoiceSchedule(invSchedule);

        // BankAccount
        FIN_FinancialAccount objFinancialAccount = findDALInstance(false,
            FIN_FinancialAccount.class, new Value("name", cusfinancialaccount));
        bp.setAccount(objFinancialAccount);

        // Tax category
        TaxCategory taxCat = findDALInstance(false, TaxCategory.class, new Value("name",
            soTaxCategory));
        bp.setSOBPTaxCategory(taxCat);

        // Search Default Payment Term
        PaymentTerm paymentTerm = findDALInstance(false, PaymentTerm.class, new Value("searchKey",
            paymentterm));
        bp.setPaymentTerms(paymentTerm);

        // Search Default PriceList
        PriceList priceList = findDALInstance(false, PriceList.class, new Value("name", pricelist),
            new Value("salesPriceList", true));
        bp.setPriceList(priceList);
      }

      OBDal.getInstance().save(bp);
      OBDal.getInstance().flush();

      // Bank Account
      if ((DALUtils.getBoolean(showGeneric) && bankAccountNo != null)
          || (DALUtils.getBoolean(showIban) && iban != null)) {
        org.openbravo.model.common.businesspartner.BankAccount bankAccount = OBProvider
            .getInstance().get(org.openbravo.model.common.businesspartner.BankAccount.class);
        bankAccount.setOrganization(rowOrganization);
        bankAccount.setActive(true);
        bankAccount.setBusinessPartner(bp);
        bankAccount.setAccountNo(bankAccountNo);
        bankAccount.setShowGeneric(DALUtils.getBoolean(showGeneric));
        bankAccount.setIBAN(iban);
        bankAccount.setShowIBAN(DALUtils.getBoolean(showIban));

        if (bankAccount.isShowIBAN()) {
          bankAccount.setBankFormat("IBAN");
        } else {
          bankAccount.setBankFormat("GENERIC");
        }
        if (iban != null) {
          bankAccount.setCountry(DALUtils.getCountryByIban(iban));
        }

        OBDal.getInstance().save(bankAccount);
        OBDal.getInstance().flush();
      }

      // Business Partner Location
      OBContext.setAdminMode(true);
      org.openbravo.model.common.businesspartner.Location bpLocation = null;
      try {
        if (country != null) {
          Location location = OBProvider.getInstance().get(Location.class);
          location.setOrganization(rowOrganization);
          location.setActive(true);
          location.setAddressLine1(address1stline);
          location.setAddressLine2(address2ndline);
          location.setPostalCode(postalcode);
          location.setCityName(city);
          Country countryvalue = findDALInstance(false, Country.class, new Value("name", country));
          location.setCountry(countryvalue);
          location.setRegion(findDALInstance(false, Region.class,
              new Value("country", countryvalue), new Value("name", region)));
          OBDal.getInstance().save(location);
          OBDal.getInstance().flush();

          bpLocation = OBProvider.getInstance().get(
              org.openbravo.model.common.businesspartner.Location.class);
          bpLocation.setOrganization(rowOrganization);
          bpLocation.setActive(true);
          bpLocation.setBusinessPartner(bp);
          bpLocation.setFax(fax);
          bpLocation.setPhone(phone);
          bpLocation.setAlternativePhone(alternativephone);
          bpLocation.setLocationAddress(location);
          if (shipToAddress == null) {
            bpLocation.setShipToAddress(true);
          } else {
            bpLocation.setShipToAddress(Parameter.BOOLEAN.parse(shipToAddress));
          }
          bpLocation.setPayFromAddress(DALUtils.getBoolean(payFromAddress));
          if (invoiceToAddress == null) {
            bpLocation.setInvoiceToAddress(true);
          } else {
            bpLocation.setInvoiceToAddress(Parameter.BOOLEAN.parse(invoiceToAddress));
          }
          bpLocation.setRemitToAddress(DALUtils.getBoolean(remitToAddress));
          OBDal.getInstance().save(bpLocation);
          OBDal.getInstance().flush();
        }
      } finally {
        OBContext.restorePreviousMode();
      }

      // Contact
      if (contactuser != null) {
        User contact = OBProvider.getInstance().get(User.class);
        contact.setOrganization(rowOrganization);
        contact.setActive(true);
        contact.setBusinessPartner(bp);
        contact.setPartnerAddress(bpLocation);
        contact.setName(contactuser);
        contact.setFirstName(firstname);
        contact.setLastName(lastname);
        contact.setDescription(contactdescription);
        contact.setEmail(email);
        contact.setPosition(position);
        contact.setPhone(contactphone);
        contact.setAlternativePhone(contactalternativephone);
        contact.setFax(contactfax);
        OBDal.getInstance().save(contact);
        OBDal.getInstance().flush();
      }

    }

    // End process
    OBDal.getInstance().commitAndClose();

    return bp;
  }

  private static boolean strequal(String s1, String s2) {
    if (s1 == null) {
      return s2 == null;
    } else {
      return s1.equals(s2);
    }
  }
}
