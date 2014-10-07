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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 **/

package org.openbravo.agingbalance.ad_reports;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.financial.paymentreport.erpCommon.ad_reports.PaymentReportDao;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationInformation;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.model.financialmgmt.payment.DoubtfulDebt;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;

public class AgingDao {

  private final PaymentReportDao dao = new PaymentReportDao();
  static Logger log4j = Logger.getLogger(Utility.class);
  private static String salesInvoiceTab = "263";
  private static String purchaseInvoiceTab = "290";
  private static String paymentInTab = "C4B6506838E14A349D6717D6856F1B56";
  private static String paymentOutTab = "F7A52FDAAA0346EFA07D53C125B40404";

  public AgingDao() {
  }

  /**
   * This method recovers the necessary data from the database to create an array fieldProviders of
   * objects containing the information for the report
   * 
   */
  public FieldProvider[] getOpenReceivablesAgingSchedule(String strcBpartnerId,
      String strAccSchema, Date currentDate, String strcolumn1, String strcolumn2,
      String strcolumn3, String strcolumn4, String strOrg, String strOrgFamily, String recOrPay,
      boolean showDoubtfulDebt) throws IOException, ServletException {

    // Initialization of some variables
    List<AgingData> agingBalanceData = new ArrayList<AgingData>();
    FieldProvider[] data = null;
    List<Organization> organizations = OBDao.getOBObjectListFromString(Organization.class,
        strOrgFamily);
    List<BusinessPartner> bPartners = OBDao.getOBObjectListFromString(BusinessPartner.class,
        strcBpartnerId);
    String dateFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
    Currency convCurrency = null;
    String auxBPID = "";
    FIN_Payment auxCreditPayment = null;
    Organization organization = null;
    OBContext.setAdminMode(true);
    try {
      if (strAccSchema == null || "".equals(strAccSchema)) {
        organization = OBDal.getInstance().get(Organization.class, strOrg);
        convCurrency = organization.getCurrency();
      } else {
        AcctSchema acctSchema = OBDal.getInstance().get(AcctSchema.class, strAccSchema);
        convCurrency = acctSchema.getCurrency();
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    int scale = convCurrency.getStandardPrecision().intValue();

    OBContext.setAdminMode(true);
    try {
      // create a Query for retrieving the data
      OBQuery<FIN_PaymentScheduleDetail> query = createObCriteria(organizations, bPartners,
          strAccSchema, currentDate, recOrPay, strcBpartnerId, strcolumn1, strcolumn2, strcolumn3,
          strcolumn4);

      // loop the data
      for (FIN_PaymentScheduleDetail psd : query.list()) {

        if (psd.getInvoicePaymentSchedule() != null) {
          // Receivables/Payables: In this section the Receivables/Payables are going to be
          // processed.
          Invoice invoice = psd.getInvoicePaymentSchedule().getInvoice();
          BusinessPartner bPartner = invoice.getBusinessPartner();
          String strAcctDate = dateFormat.format(invoice.getAccountingDate());
          BigDecimal convRate = null;
          if (!convCurrency.equals(invoice.getCurrency())) {
            convRate = getConversionRate(invoice.getCurrencyConversionRateDocList(),
                invoice.getCurrency(), convCurrency, strAcctDate);
          } else {
            convRate = BigDecimal.ONE;
          }
          if (convRate == null) {
            throw new OBException("No Conversion Rate");
          }
          // Range bucket depending on duedate
          int intScope = getScope(psd.getInvoicePaymentSchedule().getDueDate(), strcolumn1,
              strcolumn2, strcolumn3, strcolumn4, currentDate);
          BigDecimal amount = BigDecimal.ZERO;
          BigDecimal doubtfulDebtAmount = BigDecimal.ZERO;
          if (psd.getPaymentDetails() != null
              && psd.getPaymentDetails().getFinPayment().getPaymentDate().after(currentDate)
              && psd.getWriteoffAmount().compareTo(BigDecimal.ZERO) != 0) {
            if (showDoubtfulDebt && isDoubtfultDebtPreviousToDate(psd, currentDate)) {
              amount = psd.getAmount().add(psd.getWriteoffAmount())
                  .subtract(psd.getDoubtfulDebtAmount()).multiply(convRate)
                  .setScale(scale, BigDecimal.ROUND_HALF_UP);

              doubtfulDebtAmount = psd.getDoubtfulDebtAmount().multiply(convRate)
                  .setScale(scale, BigDecimal.ROUND_HALF_UP);
            } else {
              amount = psd.getAmount().add(psd.getWriteoffAmount()).multiply(convRate)
                  .setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
          } else {
            if (showDoubtfulDebt && isDoubtfultDebtPreviousToDate(psd, currentDate)) {
              amount = psd.getAmount().subtract(psd.getDoubtfulDebtAmount()).multiply(convRate)
                  .setScale(scale, BigDecimal.ROUND_HALF_UP);
              doubtfulDebtAmount = psd.getDoubtfulDebtAmount().multiply(convRate)
                  .setScale(scale, BigDecimal.ROUND_HALF_UP);
            } else {
              amount = psd.getAmount().multiply(convRate).setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
          }
          if (bPartner.getId().equals(auxBPID)) {
            // if the business partner has been inserted already
            agingBalanceData.get(agingBalanceData.size() - 1).addAmount(amount, intScope);
            if (showDoubtfulDebt) {
              agingBalanceData.get(agingBalanceData.size() - 1).addDoubtfulDebt(doubtfulDebtAmount);
            }
          } else {
            // if there is the first time the Business Partner is inserted
            agingBalanceData.add(new AgingData(bPartner.getId(), bPartner.getName(), amount,
                intScope));
            if (showDoubtfulDebt) {
              agingBalanceData.get(agingBalanceData.size() - 1).addDoubtfulDebt(doubtfulDebtAmount);
            }
            auxBPID = bPartner.getId();
          }
        } else {
          // Credits: In this section the Credits are going to be processed.
          FIN_Payment payment = psd.getPaymentDetails().getFinPayment();
          BusinessPartner bPartner = payment.getBusinessPartner();
          if (!payment.equals(auxCreditPayment)) {
            // If the credit payment has not been processed
            if (bPartner.getId().equals(auxBPID)) {
              // if the business partner has been inserted already
              agingBalanceData.get(agingBalanceData.size() - 1).addCredit(
                  getCreditLeft(payment, currentDate, convCurrency));
            } else {
              BigDecimal creditLeft = getCreditLeft(payment, currentDate, convCurrency);
              if (creditLeft.compareTo(BigDecimal.ZERO) != 0) {
                // if there is the first time the Business Partner is inserted
                agingBalanceData.add(new AgingData(bPartner.getId(), bPartner.getName(),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, creditLeft, BigDecimal.ZERO));
                auxBPID = bPartner.getId();
              }
            }
            auxCreditPayment = payment;
          }
        }
      }

    } finally {
      OBContext.restorePreviousMode();
    }
    data = FieldProviderFactory.getFieldProviderArray(agingBalanceData);
    convertAmountsToString(data, agingBalanceData);
    return data;
  }

  /**
   * This method returns an array of fieldProviders with the necessary information to print the
   * Aging Schedule Details report.
   */
  public FieldProvider[] getOpenReceivablesAgingScheduleDetails(List<BusinessPartner> bPartners,
      String strAccSchema, Date currentDate, SimpleDateFormat dateFormat, Currency convCurrency,
      String strOrg, String strOrgFamily, String recOrPay, String strcolumn1, String strcolumn2,
      String strcolumn3, String strcolumn4, String strcBpartnerId, boolean showDoubtfulDebt)
      throws IOException, ServletException {

    List<HashMap<String, String>> hashMapList = new ArrayList<HashMap<String, String>>();
    FieldProvider[] data = null;
    List<Organization> organizations = OBDao.getOBObjectListFromString(Organization.class,
        strOrgFamily);
    int scale = convCurrency.getStandardPrecision().intValue();

    OBContext.setAdminMode(true);
    try {
      // create a Query object and add a filter
      OBQuery<FIN_PaymentScheduleDetail> query = createObCriteria(organizations, bPartners,
          strAccSchema, currentDate, recOrPay, strcBpartnerId, strcolumn1, strcolumn2, strcolumn3,
          strcolumn4);
      // Data
      Invoice auxInvoice = null;
      FIN_Payment auxCreditPayment = null;
      int index = -1;
      int group;
      for (FIN_PaymentScheduleDetail psd : query.list()) {

        // Organization Information related to the Payment Schedule Detail
        OrganizationInformation orgInfo = OBDao.getActiveOBObjectList(psd.getOrganization(),
            Organization.PROPERTY_ORGANIZATIONINFORMATIONLIST) != null ? (OrganizationInformation) OBDao
            .getActiveOBObjectList(psd.getOrganization(),
                Organization.PROPERTY_ORGANIZATIONINFORMATIONLIST).get(0) : null;

        if (psd.getInvoicePaymentSchedule() != null) {
          // Receivables/Payables: In this section the Receivables/Payables are going to be
          // processed.
          // One row will be created for each Invoice
          Invoice invoice = psd.getInvoicePaymentSchedule().getInvoice();
          String strAcctDate = dateFormat.format(invoice.getAccountingDate());
          BigDecimal convRate = null;
          if (!convCurrency.equals(invoice.getCurrency())) {
            convRate = getConversionRate(invoice.getCurrencyConversionRateDocList(),
                invoice.getCurrency(), convCurrency, strAcctDate);
          } else {
            convRate = BigDecimal.ONE;
          }
          if (convRate == null) {
            throw new OBException("No Conversion Rate");
          }
          // If the PaymentScheduleDetail belongs to the same Invoice
          if (invoice.equals(auxInvoice)) {
            BigDecimal amount = BigDecimal.ZERO;
            BigDecimal doubtfulDebtAmount = BigDecimal.ZERO;
            if (psd.getPaymentDetails() != null
                && psd.getPaymentDetails().getFinPayment().getPaymentDate().after(currentDate)
                && psd.getWriteoffAmount().compareTo(BigDecimal.ZERO) != 0) {
              if (showDoubtfulDebt && isDoubtfultDebtPreviousToDate(psd, currentDate)) {
                amount = psd.getAmount().add(psd.getWriteoffAmount())
                    .subtract(psd.getDoubtfulDebtAmount()).multiply(convRate)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP);
                doubtfulDebtAmount = psd.getDoubtfulDebtAmount().multiply(convRate)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP);
              } else {
                amount = psd.getAmount().add(psd.getWriteoffAmount()).multiply(convRate)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP);
              }
            } else {
              if (showDoubtfulDebt && isDoubtfultDebtPreviousToDate(psd, currentDate)) {
                amount = psd.getAmount().subtract(psd.getDoubtfulDebtAmount()).multiply(convRate)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP);
                doubtfulDebtAmount = psd.getDoubtfulDebtAmount().multiply(convRate)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP);
              } else {
                amount = psd.getAmount().multiply(convRate)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP);
              }
            }
            group = getScope(psd.getInvoicePaymentSchedule().getDueDate(), strcolumn1, strcolumn2,
                strcolumn3, strcolumn4, currentDate);
            BigDecimal previousAmount = BigDecimal.ZERO;
            if (hashMapList.get(index).get("AMOUNT" + group) != null) {
              previousAmount = new BigDecimal(hashMapList.get(index).get("AMOUNT" + group));
            }
            BigDecimal previousDoubtfulDebt = null;
            String strPrevDoubtfulDebt = hashMapList.get(index).get("DOUBTFUL_DEBT");
            if (strPrevDoubtfulDebt == null) {
              previousDoubtfulDebt = BigDecimal.ZERO;
            } else {
              previousDoubtfulDebt = new BigDecimal(strPrevDoubtfulDebt);

            }
            BigDecimal netDue = new BigDecimal(hashMapList.get(index).get("NETDUE"));
            hashMapList.get(index).put("AMOUNT" + group, previousAmount.add(amount).toString());
            hashMapList.get(index).put("DOUBTFUL_DEBT",
                previousDoubtfulDebt.add(doubtfulDebtAmount).toString());
            hashMapList.get(index).put(
                "PERCENTAGE",
                calculatePercentage(netDue.add(amount).add(doubtfulDebtAmount), doubtfulDebtAmount)
                    .toString());
            hashMapList.get(index).put("NETDUE",
                netDue.add(amount).add(doubtfulDebtAmount).toString());
            hashMapList.get(index).put("SHOW_NETDUE", netDue.add(amount).toString());

            // If the paymentScheduleDetail belongs to a different Invoice
          } else {
            auxInvoice = invoice;
            HashMap<String, String> psData = new HashMap<String, String>();
            group = getScope(psd.getInvoicePaymentSchedule().getDueDate(), strcolumn1, strcolumn2,
                strcolumn3, strcolumn4, currentDate);
            BigDecimal amount = BigDecimal.ZERO;
            BigDecimal doubtfulDebtAmount = BigDecimal.ZERO;
            if (psd.getPaymentDetails() != null
                && psd.getPaymentDetails().getFinPayment().getPaymentDate().after(currentDate)
                && psd.getWriteoffAmount().compareTo(BigDecimal.ZERO) != 0) {
              if (showDoubtfulDebt && isDoubtfultDebtPreviousToDate(psd, currentDate)) {
                amount = psd.getAmount().add(psd.getWriteoffAmount())
                    .subtract(psd.getDoubtfulDebtAmount()).multiply(convRate)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP);
                doubtfulDebtAmount = psd.getDoubtfulDebtAmount().multiply(convRate)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP);
              } else {
                amount = psd.getAmount().add(psd.getWriteoffAmount()).multiply(convRate)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP);
              }
            } else {
              if (showDoubtfulDebt && isDoubtfultDebtPreviousToDate(psd, currentDate)) {
                amount = psd.getAmount().subtract(psd.getDoubtfulDebtAmount()).multiply(convRate)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP);
                doubtfulDebtAmount = psd.getDoubtfulDebtAmount().multiply(convRate)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP);
              } else {
                amount = psd.getAmount().multiply(convRate)
                    .setScale(scale, BigDecimal.ROUND_HALF_UP);
              }
            }
            String DocumentNo = invoice.getDocumentNo();
            if (!recOrPay.equals("RECEIVABLES")
                && orgInfo.getAPRMPaymentDescription().equals("Supplier Reference")
                && invoice.getOrderReference() != null) {
              DocumentNo = invoice.getOrderReference();
            }
            psData = insertData(DocumentNo, invoice.getId(), invoice.getAccountingDate(), amount,
                invoice.getBusinessPartner(), group,
                recOrPay.equals("RECEIVABLES") ? salesInvoiceTab : purchaseInvoiceTab, dateFormat,
                false, doubtfulDebtAmount);
            hashMapList.add(psData);
            index++;
          }
        } else {
          // Credits: In this section the Credits are going to be processed.
          FIN_Payment payment = psd.getPaymentDetails().getFinPayment();
          if (!payment.equals(auxCreditPayment)) {
            BigDecimal creditLeft = getCreditLeft(payment, currentDate, convCurrency);
            if (creditLeft.compareTo(BigDecimal.ZERO) != 0) {
              HashMap<String, String> psData = new HashMap<String, String>();
              group = 6;
              psData = insertData(payment.getDocumentNo(), payment.getId(),
                  payment.getPaymentDate(), creditLeft, payment.getBusinessPartner(), group,
                  recOrPay.equals("RECEIVABLES") ? paymentInTab : paymentOutTab, dateFormat, true,
                  BigDecimal.ZERO);
              hashMapList.add(psData);
              index++;
            }
          }
          auxCreditPayment = payment;
        }
      }

      data = FieldProviderFactory.getFieldProviderArray(hashMapList);
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  /**
   * This method is used to insert Data in the details section of the report.
   */
  private HashMap<String, String> insertData(String documentNo, String id, Date date,
      BigDecimal amount, BusinessPartner bpartner, int group, String tabId,
      SimpleDateFormat dateFormat, boolean credits, BigDecimal doubtfulDebt) {
    HashMap<String, String> psData = new HashMap<String, String>();
    psData.put("INVOICE_NUMBER", documentNo);
    psData.put("INVOICE_ID", id);
    psData.put("INVOICE_DATE", dateFormat.format(date));
    psData.put("AMOUNT" + group, amount.compareTo(BigDecimal.ZERO) == 0 ? null : amount.toString());
    psData.put("DOUBTFUL_DEBT",
        doubtfulDebt.compareTo(BigDecimal.ZERO) == 0 ? null : doubtfulDebt.toString());
    BigDecimal percentage = calculatePercentage(amount.add(doubtfulDebt), doubtfulDebt);
    psData.put("PERCENTAGE",
        percentage.compareTo(BigDecimal.ZERO) == 0 ? null : percentage.toString());
    if (credits) {
      psData.put("SHOW_NETDUE", amount.add(doubtfulDebt).toString());
    } else {
      psData.put("NETDUE", amount.add(doubtfulDebt).toString());
      psData.put("SHOW_NETDUE", amount.add(doubtfulDebt).toString());
    }
    psData.put("BPARTNER", bpartner.getId().toString());
    psData.put("BPARTNERNAME", bpartner.getIdentifier().toString());
    psData.put("TABID", tabId);
    return psData;

  }

  /**
   * This method creates an OBCriteria object with the restrictions necessary for creating the query
   * 
   * @return
   */
  private OBQuery<FIN_PaymentScheduleDetail> createObCriteria(List<Organization> organizations,
      List<BusinessPartner> bPartners, String strAccSchema, Date currentDate, String recOrPay,
      String strcBpartnerId, String strcolumn1, String strcolumn2, String strcolumn3,
      String strcolumn4) {
    final StringBuilder hsqlScript = new StringBuilder();

    hsqlScript.append(" as psd");
    hsqlScript.append("   left outer join psd.invoicePaymentSchedule as ps");
    hsqlScript.append("   left outer join ps.invoice as i");
    hsqlScript.append("   left outer join i.businessPartner as bpi");
    hsqlScript.append("   left outer join psd.paymentDetails  as pd");
    hsqlScript.append("   left outer join pd.finPayment as p");
    hsqlScript.append("   left outer join p.businessPartner as bpp");
    hsqlScript.append(" where psd.active=true");
    hsqlScript.append("   and psd.canceled = false");
    hsqlScript.append("   and psd.organization in :organizations");
    // Receivables / Payables
    // PaymentScheduleDetail has an invoice
    hsqlScript.append("   and ((psd.invoicePaymentSchedule is not null");
    hsqlScript.append("   and i.documentStatus <> 'VO'");
    // Issotrx
    hsqlScript.append("     and i.salesTransaction = :recOrPay");
    // Business Partner filter
    if (bPartners.size() > 0) {
      hsqlScript.append("     and bpi.id in " + strcBpartnerId);
    }
    // invoice accounting date is before as of date
    hsqlScript.append("     and trunc(i.accountingDate) <= :asOfDate");
    // PaymentScheduleDetail is not fully paid
    hsqlScript.append("     and (psd.paymentDetails is null ");
    // or the payment is not executed
    hsqlScript.append("       or psd.invoicePaid='N'");
    // or the payment is executed, but after as of date
    hsqlScript.append("       or (psd.invoicePaid='Y' and trunc(p.paymentDate) > :asOfDate))");
    hsqlScript.append("   ) or (");
    // Credit generated by Payments
    // PaymentScheduleDetail has a payment and PaymentSchedule (Invoice) is null
    hsqlScript.append("     psd.paymentDetails is not null");
    hsqlScript.append("     and psd.invoicePaymentSchedule is null");
    // Payment is confirmed and payment date is <= as of Date
    hsqlScript.append("     and (psd.invoicePaid='Y' and trunc(p.paymentDate) <= :asOfDate)");
    // Issotrx
    hsqlScript.append("     and p.receipt = :recOrPay");
    // Business Partner filter
    if (bPartners.size() > 0) {
      hsqlScript.append("     and bpp.id in " + strcBpartnerId);
    }
    // The Payment has to be related to a Business Partner
    hsqlScript.append("     and p.businessPartner is not null");
    // The generated credit of the Payment has to be <> 0
    hsqlScript.append("     and p.generatedCredit <> 0))");
    // Order by Business Partner name, DueDate, Accounting Date and Document Number
    hsqlScript.append(" order by  coalesce(bpi.name, bpp.name),");
    // Order by ID, see issue 26115: is not grouping if there are more than one business
    // partner with the same name
    hsqlScript.append(" coalesce(bpi.id, bpp.id),");
    hsqlScript.append("     (case when psd.invoicePaymentSchedule is null then 6");
    hsqlScript.append("           when trunc(ps.dueDate) > :asOfDate then 5");
    hsqlScript.append("           when trunc(ps.dueDate) > :firstRangeBucket then 4");
    hsqlScript.append("           when trunc(ps.dueDate) > :secondRangeBucket then 3");
    hsqlScript.append("           when trunc(ps.dueDate) > :thirdRangeBucket then 2");
    hsqlScript.append("           when trunc(ps.dueDate) > :fourthRangeBucket then 1");
    hsqlScript.append("           else 0");
    hsqlScript.append("      end),");
    hsqlScript
        .append(" coalesce(i.accountingDate, p.paymentDate), coalesce(i.documentNo, p.documentNo)");

    final OBQuery<FIN_PaymentScheduleDetail> query = OBDal.getInstance().createQuery(
        FIN_PaymentScheduleDetail.class, hsqlScript.toString());
    query.setNamedParameter("organizations", organizations);
    query.setNamedParameter("asOfDate", currentDate);
    query.setNamedParameter("firstRangeBucket", convertToDate(currentDate, strcolumn1));
    query.setNamedParameter("secondRangeBucket", convertToDate(currentDate, strcolumn2));
    query.setNamedParameter("thirdRangeBucket", convertToDate(currentDate, strcolumn3));
    query.setNamedParameter("fourthRangeBucket", convertToDate(currentDate, strcolumn4));
    if (recOrPay.equals("RECEIVABLES")) {
      query.setNamedParameter("recOrPay", true);
    } else {
      // PAYABLES
      query.setNamedParameter("recOrPay", false);
    }

    return query;
  }

  /**
   * This method attemps to convert one amount of an invoice from one currency to another. First it
   * checks if there is data in the Exchange rates tab of the invoice. If not it calls to the
   * conversion method of the PaymentReportDao.
   */
  private BigDecimal getConversionRate(List<ConversionRateDoc> lisConversionRateDoc,
      Currency currency, Currency convCurrency, String strDueDate) {

    for (ConversionRateDoc convRateDoc : lisConversionRateDoc) {
      if (convRateDoc != null && convRateDoc.getToCurrency().equals(convCurrency)
          && convRateDoc.getCurrency().equals(currency)) {
        return convRateDoc.getRate();
      }
    }

    ConversionRate conversionRate = dao.getConversionRate(currency, convCurrency, strDueDate);
    if (conversionRate != null) {
      return conversionRate.getMultipleRateBy();
    }

    return null;
  }

  /**
   * Returns the result date of subtracting the date range to the as Of Date field.
   */
  private Date convertToDate(Date currentDate, String strcolumn) {
    Calendar cal = new GregorianCalendar();
    cal.setTime(currentDate);
    cal.add(Calendar.DATE, -Integer.parseInt(strcolumn));
    return cal.getTime();
  }

  /**
   * Given a date and the day ranges, this method returns the range the date belongs
   * 
   * @return
   */
  private int getScope(Date scope, String strcolumn1, String strcolumn2, String strcolumn3,
      String strcolumn4, Date currentDate) {
    if (scope.after(currentDate)) {
      return 0;
    }
    if (scope.after(convertToDate(currentDate, strcolumn1))) {
      return 1;
    }
    if (scope.after(convertToDate(currentDate, strcolumn2))) {
      return 2;
    }
    if (scope.after(convertToDate(currentDate, strcolumn3))) {
      return 3;
    }
    if (scope.after(convertToDate(currentDate, strcolumn4))) {
      return 4;
    }
    return 5;
  }

  /**
   * This method transforms all the amounts into Strings for the report
   * 
   */
  private void convertAmountsToString(FieldProvider[] data, List<AgingData> agingBalanceData) {
    for (int i = 0; i < data.length; i++) {
      FieldProviderFactory.setField(data[i], "amount0", agingBalanceData.get(i).getcurrent()
          .toString());
      FieldProviderFactory.setField(data[i], "amount1", agingBalanceData.get(i).getamount1()
          .toString());
      FieldProviderFactory.setField(data[i], "amount2", agingBalanceData.get(i).getamount2()
          .toString());
      FieldProviderFactory.setField(data[i], "amount3", agingBalanceData.get(i).getamount3()
          .toString());
      FieldProviderFactory.setField(data[i], "amount4", agingBalanceData.get(i).getamount4()
          .toString());
      FieldProviderFactory.setField(data[i], "amount5", agingBalanceData.get(i).getamount5()
          .toString());
      FieldProviderFactory
          .setField(data[i], "Total", agingBalanceData.get(i).getTotal().toString());
      FieldProviderFactory.setField(data[i], "credit", agingBalanceData.get(i).getCredit()
          .toString());
      FieldProviderFactory.setField(data[i], "net", agingBalanceData.get(i).getNet().toString());
      FieldProviderFactory.setField(data[i], "doubtfulDebt", agingBalanceData.get(i)
          .getDoubtfulDebt().toString());
      FieldProviderFactory.setField(data[i], "percentage", agingBalanceData.get(i).getPercentage()
          .toString());
    }
  }

  /**
   * This method returns the credit left to be used for a payment that has generated credit as of
   * currentDate already converted to the conCurrency if necessary.
   */
  private BigDecimal getCreditLeft(FIN_Payment pay, Date currentDate, Currency convCurrency) {
    OBContext.setAdminMode(true);
    try {
      final StringBuilder hsqlScript = new StringBuilder();

      hsqlScript.append(" select sum(credit.amount) ");
      hsqlScript.append(" from FIN_Payment_Credit as credit ");
      hsqlScript.append(" left outer join credit.payment as payment");
      hsqlScript.append(" left outer join payment.fINPaymentDetailList  as pd");
      hsqlScript.append(" left outer join pd.fINPaymentScheduleDetailList as psd");
      hsqlScript.append(" where");
      hsqlScript.append(" credit.creditPaymentUsed = :payment");
      hsqlScript.append(" and psd.invoicePaid='Y'");
      hsqlScript
          .append(" and trunc(payment." + FIN_Payment.PROPERTY_PAYMENTDATE + ") <= :asOfDate");

      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hsqlScript.toString());
      query.setParameter("payment", pay);
      query.setParameter("asOfDate", currentDate);

      int scale = convCurrency.getStandardPrecision().intValue();
      BigDecimal usedCredit = (BigDecimal) query.uniqueResult();
      if (usedCredit == null) {
        usedCredit = BigDecimal.ZERO;
      }
      BigDecimal generatedCredit = pay.getGeneratedCredit();
      BigDecimal creditLeft = generatedCredit.subtract(usedCredit);

      // Conversion Rate
      BigDecimal convRate = null;
      if (convCurrency != null && !convCurrency.equals(pay.getCurrency())) {
        convRate = getConversionRate(pay.getCurrencyConversionRateDocList(), pay.getCurrency(),
            convCurrency, OBDateUtils.formatDate(pay.getPaymentDate()));
      } else {
        convRate = BigDecimal.ONE;
      }
      if (convRate == null) {
        throw new OBException("No Conversion Rate");

      }
      return creditLeft.multiply(convRate).setScale(scale, BigDecimal.ROUND_HALF_UP);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private boolean isDoubtfultDebtPreviousToDate(FIN_PaymentScheduleDetail psd, Date currentDate) {
    OBCriteria<DoubtfulDebt> obcDD = OBDal.getInstance().createCriteria(DoubtfulDebt.class);
    obcDD.add(Restrictions.eq(DoubtfulDebt.PROPERTY_FINPAYMENTSCHEDULE,
        psd.getInvoicePaymentSchedule()));
    DoubtfulDebt dd = (DoubtfulDebt) obcDD.uniqueResult();
    if (dd != null && (!dd.getFINDoubtfulDebtRun().getRundate().after(currentDate))) {
      return true;
    }
    return false;
  }

  private BigDecimal calculatePercentage(BigDecimal totalAmount, BigDecimal doubtfulDebtAmount) {
    if (doubtfulDebtAmount.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return doubtfulDebtAmount.divide(totalAmount, 5, RoundingMode.HALF_UP).multiply(
        new BigDecimal("100"));
  }

}
