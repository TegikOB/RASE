/*
 ************************************************************************************
 * Copyright (C) 2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

import java.math.BigDecimal;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.Category;

import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Location;
import org.openbravo.model.common.geography.Region;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.MatchingAlgorithm;
import org.openbravo.idl.initial_data_load.financialaccountsjob_0_1.FinancialAccountsJob;
import org.openbravo.service.web.ResourceNotFoundException;

/**
 *
 * @author adrian
 */
public class FinAccountsProcess extends IdlServiceETL {

  final FinancialAccountsJob job = new FinancialAccountsJob();


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
      return new Parameter[]{
          new Parameter("Organization", Parameter.STRING),
          new Parameter("Name", Parameter.STRING),
          new Parameter("Description", Parameter.STRING),
          new Parameter("Currency", Parameter.STRING),
          new Parameter("Type", Parameter.STRING),
          new Parameter("IsDefault", Parameter.BOOLEAN),
          new Parameter("BusinessPartner", Parameter.STRING),
          new Parameter("Location1stLine", Parameter.STRING),
          new Parameter("Location2ndLine", Parameter.STRING),
          new Parameter("LocationPostalCode", Parameter.STRING),
          new Parameter("LocationCity", Parameter.STRING),
          new Parameter("LocationRegion", Parameter.STRING),
          new Parameter("LocationCountry", Parameter.STRING),
          new Parameter("BankCode", Parameter.STRING),
          new Parameter("BranchCode", Parameter.STRING),
          new Parameter("BankControlDigit", Parameter.STRING),
          new Parameter("INENo", Parameter.STRING),
          new Parameter("SwiftCode", Parameter.STRING),
          new Parameter("AccountControlDigit", Parameter.STRING),
          new Parameter("PartialAccountNo", Parameter.STRING),
          new Parameter("AccountNo", Parameter.STRING),
          new Parameter("IBAN", Parameter.STRING),
          new Parameter("InitialBalance", Parameter.BIGDECIMAL),
          new Parameter("CreditLimit", Parameter.BIGDECIMAL),
          new Parameter("MatchingAlgorithm", Parameter.STRING),
      };
  }

  @Override
  protected BaseOBObject internalProcess(Object... values) throws Exception {

    return createFinAccounts((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4], (String) values[5], (String) values[6],
        (String) values[7], (String) values[8], (String) values[9], (String) values[10],
        (String) values[11], (String) values[12], (String) values[13], (String) values[14],
        (String) values[15], (String) values[16], (String) values[17], (String) values[18],
        (String) values[19], (String) values[20], (String) values[21], (String) values[22],
        (String) values[23], (String) values[24]);
  }

  public BaseOBObject createFinAccounts(
          final String org,
          final String name,
          final String description,
          final String currency,
          final String type,
          final String isdefault,
          final String businesspartner,
          final String location1stline,
          final String location2ndline,
          final String locationpostalcode,
          final String locationcity,
          final String locationregion,
          final String locationcountry,
          final String bankcode,
          final String branchcode,
          final String bankcontroldigit,
          final String ineno,
          final String swiftcode,
          final String accountcontroldigit,
          final String partialaccountno,
          final String accountno,
          final String iban,
          final String initialbalance,
          final String creditlimit,
          final String matchingalgorithm) throws Exception {

    // Check that the Financial Account does not already exists
    FIN_FinancialAccount financialAccountExist = findDALInstance(false, FIN_FinancialAccount.class, new Value(FIN_FinancialAccount.PROPERTY_NAME, name));
    if (financialAccountExist != null) {
      throw new OBException(Utility.messageBD(conn, "IDL_FA_EXISTS", vars.getLanguage())
          + name);
    }

    // Find the currency instance
    Currency currencyinst = findDALInstance(false, Currency.class,
            new Value("iSOCode", searchDefaultValue("Financial Account", "Currency", currency)));
    if (currencyinst == null) {
      throw new ResourceNotFoundException(
              Utility.messageBD(conn, "IDL_CURRENCY_NOT_FOUND", vars.getLanguage()) + currency);
    }
    // find the Matching algorithm
    MatchingAlgorithm mainst = findDALInstance(false, MatchingAlgorithm.class, new Value(MatchingAlgorithm.PROPERTY_NAME,  searchDefaultValue("Financial Account", "MatchingAlgorithm", matchingalgorithm)));


    // Create the Financial Account record.
    FIN_FinancialAccount financialaccount = OBProvider.getInstance().get(FIN_FinancialAccount.class);
    financialaccount.setActive(true);
    financialaccount.setOrganization(rowOrganization);
    financialaccount.setName(name);
    financialaccount.setDescription(description);
    financialaccount.setCurrency(currencyinst);
    financialaccount.setType(getReferenceValue("Financial account type", searchDefaultValue("Financial Account", "Type", type)));
    financialaccount.setDefault(Parameter.BOOLEAN.parse(searchDefaultValue("Financial Account", "IsDefault", isdefault)));

    // find the BP instance
    BusinessPartner bp = null;

    if (businesspartner == null) {
      String defBPCategory = searchDefaultValue("Financial Account", "BPCategory", null);
      if (defBPCategory != null) {

        String potentialSearchKey = (name.length() > 40) ? name.substring(0, 39) : name;

        bp = findDALInstance(false, BusinessPartner.class, new Value(
            BusinessPartner.PROPERTY_SEARCHKEY, potentialSearchKey));

        if (bp == null) {

          bp = OBProvider.getInstance().get(BusinessPartner.class);
          bp.setActive(true);
          bp.setOrganization(rowOrganization);
          bp.setName(name);
          bp.setSearchKey(potentialSearchKey);

          Category bpCat = findDALInstance(false, Category.class, new Value(Category.PROPERTY_SEARCHKEY,
              defBPCategory));
          if (bpCat == null) {
            bpCat = OBProvider.getInstance().get(Category.class);
            bpCat.setActive(true);
            bpCat.setOrganization(rowOrganization);
            bpCat.setName(defBPCategory);
            bpCat.setSearchKey(defBPCategory);
            OBDal.getInstance().save(bpCat);
            OBDal.getInstance().flush();

          }

          bp.setBusinessPartnerCategory(bpCat);

        }

        OBDal.getInstance().save(bp);
        OBDal.getInstance().flush();
      }
    } else {
      bp = findDALInstance(false, BusinessPartner.class, new Value(BusinessPartner.PROPERTY_SEARCHKEY, businesspartner));
    }

    financialaccount.setBusinessPartner(bp);

    // Set location values
    if (locationcountry != null && locationregion != null) {
      Location location = OBProvider.getInstance().get(Location.class);
      location.setActive(true);
      location.setOrganization(rowOrganization);
      location.setAddressLine1(location1stline);
      location.setAddressLine2(location2ndline);
      location.setPostalCode(locationpostalcode);
      location.setCityName(locationcity);
      // Search Default Country
      Country country = findDALInstance(false, Country.class,
              new Value("name", searchDefaultValue("Financial Account", "Country", locationcountry)));
      location.setCountry(country);
      location.setRegion(findDALInstance(false, Region.class,
              new Value("country", country),
              new Value("name", locationregion)));
      OBDal.getInstance().save(location);
      OBDal.getInstance().flush();

      financialaccount.setLocationAddress(location);
    }

    financialaccount.setBankCode(bankcode);
    financialaccount.setBranchCode(branchcode);
    financialaccount.setBankDigitcontrol(bankcontroldigit);
    financialaccount.setINENo(ineno);
    financialaccount.setSwiftCode(swiftcode);
    financialaccount.setAccountDigitcontrol(accountcontroldigit);
    financialaccount.setPartialAccountNo(partialaccountno);
    financialaccount.setAccountNo(accountno);
    financialaccount.setIBAN(iban);
    BigDecimal bdvalue = Parameter.BIGDECIMAL.parse(initialbalance);
    financialaccount.setInitialBalance(bdvalue == null ? BigDecimal.ZERO : bdvalue);
    bdvalue = Parameter.BIGDECIMAL.parse(creditlimit);
    financialaccount.setCreditLimit(bdvalue == null ? BigDecimal.ZERO : bdvalue);
    financialaccount.setMatchingAlgorithm(mainst);

    OBDal.getInstance().save(financialaccount);

    OBDal.getInstance().flush();
    OBDal.getInstance().commitAndClose();

    return financialaccount;
  }
}
