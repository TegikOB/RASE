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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.initial_data_load.paymentjob_0_1.PaymentJob;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.service.web.ResourceNotFoundException;

/**
 * 
 * @author adrian
 */
public class PayablesProcess extends IdlServiceETL {

  final PaymentJob job = new PaymentJob();

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
        new Parameter("DocumentNo", Parameter.STRING),
        new Parameter("BusinessPartner", Parameter.STRING),
        new Parameter("IsReceipt", Parameter.BOOLEAN), new Parameter("Currency", Parameter.STRING),
        new Parameter("PaymentMethod", Parameter.STRING),
        new Parameter("PaymentDate", Parameter.DATE),
        new Parameter("FinancialAccount", Parameter.STRING),
        new Parameter("Project", Parameter.STRING), new Parameter("Campaign", Parameter.STRING),
        new Parameter("Activity", Parameter.STRING), new Parameter("InvoiceNo", Parameter.STRING),
        new Parameter("OrderNo", Parameter.STRING), new Parameter("GLItem", Parameter.STRING),
        new Parameter("Amount", Parameter.STRING) };
  }

  @Override
  protected BaseOBObject internalProcess(Object... values) throws Exception {

    return createPayables((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4], (String) values[5], (String) values[6],
        (String) values[7], (String) values[8], (String) values[9], (String) values[10],
        (String) values[11], (String) values[12], (String) values[13], (String) values[14]);
  }

  public BaseOBObject createPayables(final String org, final String documentno,
      final String businesspartner, final String isreceipt, final String currency,
      final String paymentmethod, final String paymentdate, final String financialaccount,
      final String project, final String campaign, final String activity, final String invoiceno,
      final String orderno, final String glitem, final String amount) throws Exception {

    // The Document No if it is null is calculated this way
    // String strPaymentDocumentNo = FIN_Utility.getDocumentNo(orgId, (isReceipt) ? "ARR": "APP",
    // "DocumentNo_FIN_Payment_Proposal");

    boolean breceipt = Parameter.BOOLEAN.parse(isreceipt);

    FIN_Payment payment;
    FIN_PaymentMethod instpaymentmethod = null;
    FinAccPaymentMethod accountmethod = null;
    String paymentdocumentno;
    if (documentno == null || documentno.equals("")) {
      payment = null;
      paymentdocumentno = FIN_Utility.getDocumentNo(rowOrganization, (breceipt) ? "ARR" : "APP",
          "DocumentNo_FIN_Payment_Proposal");
    } else {
      payment = findDALInstance(false, FIN_Payment.class, new Value(
          FIN_Payment.PROPERTY_DOCUMENTNO, documentno));
      paymentdocumentno = documentno;
    }

    if (payment == null) {
      // find the BP instance
      BusinessPartner bpinst = findDALInstance(false, BusinessPartner.class, new Value(
          BusinessPartner.PROPERTY_SEARCHKEY, businesspartner));

      // find the Project instance
      Project instproject = findDALInstance(false, Project.class, new Value(Project.PROPERTY_NAME,
          project));

      // find the Campaign instance
      Campaign instcampaign = findDALInstance(false, Campaign.class, new Value(
          Campaign.PROPERTY_NAME, campaign));

      // find the Activity instance
      ABCActivity instactivity = findDALInstance(false, ABCActivity.class, new Value(
          ABCActivity.PROPERTY_NAME, activity));

      // Find the currency instance
      Currency currencyinst = findDALInstance(false, Currency.class, new Value("iSOCode",
          searchDefaultValue("Financial Account", "Currency", currency)));
      if (currencyinst == null) {
        throw new ResourceNotFoundException(Utility.messageBD(conn, "IDL_CURRENCY_NOT_FOUND",
            vars.getLanguage())
            + currency);
      }

      // Find the financial account
      FIN_FinancialAccount instfinaccount = findDALInstance(false, FIN_FinancialAccount.class,
          new Value(FIN_FinancialAccount.PROPERTY_NAME, financialaccount));
      if (instfinaccount == null) {
        throw new ResourceNotFoundException(Utility.messageBD(conn,
            "IDL_FINANCIALACCOUNT_NOT_FOUND", vars.getLanguage()) + financialaccount);
      }

      // It is a new Payment object
      payment = OBProvider.getInstance().get(FIN_Payment.class);
      payment.setDocumentNo(paymentdocumentno);
      payment.setAmount(BigDecimal.ZERO);
      payment.setDescription("");
      payment.setActive(true);

      payment.setOrganization(rowOrganization);
      payment.setBusinessPartner(bpinst);
      payment.setReceipt(breceipt);
      payment.setCurrency(currencyinst);
      payment.setDescription(null);

      instpaymentmethod = findDALInstance(false, FIN_PaymentMethod.class, new Value(
          FIN_PaymentMethod.PROPERTY_NAME, paymentmethod));
      if (instpaymentmethod == null) {
        instpaymentmethod = createPaymentMethod(paymentmethod, rowOrganization);
      } else {
        if ((payment.isReceipt() && instpaymentmethod.getPayinExecutionProcess() == null)
            || (!payment.isReceipt() && instpaymentmethod.getPayoutExecutionProcess() == null)) {
          Organization pOrg = instpaymentmethod.getOrganization();
          final OBCriteria<FIN_PaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
              FIN_PaymentMethod.class);
          obCriteria.setFilterOnActive(false);
          obCriteria.add(Restrictions.eq(FIN_PaymentMethod.PROPERTY_NAME, "IDL " + paymentmethod));
          final List<FIN_PaymentMethod> paymentMethodList = obCriteria.list();
          if (paymentMethodList.isEmpty()) {
            instpaymentmethod = createPaymentMethod("IDL " + paymentmethod, pOrg);
          } else {
            instpaymentmethod = paymentMethodList.get(0);
          }
        }
      }
      // Checkif the financial account exists for the payment method
      accountmethod = findDALInstance(false, FinAccPaymentMethod.class, new Value(
          FinAccPaymentMethod.PROPERTY_ACCOUNT, instfinaccount), new Value(
          FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, instpaymentmethod));
      if (accountmethod == null) {
        accountmethod = OBProvider.getInstance().get(FinAccPaymentMethod.class);
        accountmethod.setOrganization(rowOrganization);
        accountmethod.setActive(true);
        accountmethod.setAccount(instfinaccount);
        accountmethod.setPaymentMethod(instpaymentmethod);
        accountmethod.setPayinExecutionType(instpaymentmethod.getPayinExecutionType());
        accountmethod.setPayinExecutionProcess(instpaymentmethod.getPayinExecutionProcess());
        accountmethod.setPayoutExecutionType(instpaymentmethod.getPayoutExecutionType());
        accountmethod.setPayoutExecutionProcess(instpaymentmethod.getPayoutExecutionProcess());
        OBDal.getInstance().save(accountmethod);
      }
      payment.setPaymentMethod(instpaymentmethod);

      payment.setPaymentDate(Parameter.DATE.parse(paymentdate));
      payment.setAccount(instfinaccount);
      payment.setProject(instproject);
      payment.setSalesCampaign(instcampaign);
      payment.setActivity(instactivity);
      payment.setStatus("RPAE"); // Awaiting execution
      payment.setAPRMProcessPayment("RE");
      payment.setDocumentType(DALUtils.getDocumentTypeRecursive(rowOrganization, breceipt ? "ARR"
          : "APP"));
    }

    // Set first to processed = false to allow us to insert records
    payment.setProcessed(false);
    OBDal.getInstance().save(payment);

    OBDal.getInstance().flush();

    // Now continue with the line

    payment.setAmount(payment.getAmount().add(Parameter.BIGDECIMAL.parse(amount)));

    OBDal.getInstance().save(payment);

    if (payment.getAccount().getCurrency().getId().equals(payment.getCurrency().getId())) {
      payment.setFinancialTransactionAmount(payment.getAmount());
    } else {
      throw new Exception(Utility.messageBD(conn, "IDL_NotSupport_MultyCurrency",
          vars.getLanguage())
          + financialaccount);
    }

    OBDal.getInstance().commitAndClose();

    ArrayList<String> descriptions;
    if (payment.getDescription() == null) {
      descriptions = new ArrayList();
    } else {
      descriptions = Collections.list(Collections.enumeration(Arrays.asList(payment
          .getDescription().split("\\n"))));
      int j = 0;
      while (j < descriptions.size()) {
        if (descriptions.get(j) == null || descriptions.get(j).equals("")) {
          descriptions.remove(j);
        } else {
          j++;
        }
      }
    }

    if (!(invoiceno == null || invoiceno.isEmpty())) {
      appendToDescriptions(descriptions,
          Utility.messageBD(conn, "InvoiceDocumentno", vars.getLanguage()) + ": ", invoiceno);
    }
    if (!(orderno == null || orderno.isEmpty())) {
      appendToDescriptions(descriptions,
          Utility.messageBD(conn, "OrderDocumentno", vars.getLanguage()) + ": ", orderno);
    }
    if (!(glitem == null || glitem.isEmpty())) {
      appendToDescriptions(descriptions, Utility.messageBD(conn, "APRM_GLItem", vars.getLanguage())
          + ": ", glitem);
    }

    StringBuffer bufferdescription = new StringBuffer();
    for (int i = 0; i < descriptions.size(); i++) {
      bufferdescription.append(descriptions.get(i));
      bufferdescription.append('\n');
    }

    String truncatedDescription = (bufferdescription.length() > 255) ? bufferdescription.substring(
        0, 252).concat("...") : bufferdescription.toString();
    payment.setDescription(truncatedDescription);

    // Define the payment detail

    GLItem instglitem = findDALInstance(false, GLItem.class,
        new Value(GLItem.PROPERTY_NAME, glitem));

    OBContext.setAdminMode();

    FIN_PaymentDetail paymentdetail = OBProvider.getInstance().get(FIN_PaymentDetail.class);
    paymentdetail.setOrganization(rowOrganization);
    paymentdetail.setActive(true);
    paymentdetail.setAmount(Parameter.BIGDECIMAL.parse(amount));
    paymentdetail.setGLItem(instglitem);
    paymentdetail.setFinPayment(payment);
    paymentdetail.setRefund(false);

    OBDal.getInstance().save(paymentdetail);

    FIN_PaymentScheduleDetail paymentscheduleddetail = OBProvider.getInstance().get(
        FIN_PaymentScheduleDetail.class);
    paymentscheduleddetail.setOrganization(rowOrganization);
    paymentscheduleddetail.setActive(true);
    paymentscheduleddetail.setAmount(Parameter.BIGDECIMAL.parse(amount));
    paymentscheduleddetail.setWriteoffAmount(BigDecimal.ZERO);
    paymentscheduleddetail.setPaymentDetails(paymentdetail);

    OBDal.getInstance().save(paymentscheduleddetail);

    OBContext.restorePreviousMode();

    // Set as processed
    payment.setProcessed(true);
    OBDal.getInstance().save(payment);

    OBDal.getInstance().flush();
    OBDal.getInstance().commitAndClose();

    return payment;
  }

  private void appendToDescriptions(List<String> descriptions, String prefix, String element) {

    for (int i = 0; i < descriptions.size(); i++) {
      if (descriptions.get(i).startsWith(prefix)) {
        descriptions.set(i, descriptions.get(i) + ", " + element);
        return;
      }
    }

    descriptions.add(prefix + element);
  }

  private FIN_PaymentMethod createPaymentMethod(String name, Organization org) throws Exception {
    PaymentExecutionProcess paymentExecutionProcess = findDALInstance(false,
        PaymentExecutionProcess.class, new Value(PaymentExecutionProcess.PROPERTY_NAME,
            "Simple Execution Process"));
    if (paymentExecutionProcess == null) {
      throw new Exception(Utility.messageBD(conn, "IDL_ExecutionProcess_NotFound",
          vars.getLanguage()));
    } else {
      FIN_PaymentMethod paymentMethod = OBProvider.getInstance().get(FIN_PaymentMethod.class);
      paymentMethod.setOrganization(org);
      // Set active to false, this way the payment method is used just for importing and will be no
      // longer available for future use
      paymentMethod.setActive(false);
      paymentMethod.setName(name);
      paymentMethod.setDescription("Payment Method generated automatically by IDL");
      paymentMethod.setPayinExecutionType("A");
      paymentMethod.setPayoutExecutionType("A");
      paymentMethod.setPayinExecutionProcess(paymentExecutionProcess);
      paymentMethod.setPayoutExecutionProcess(paymentExecutionProcess);
      OBDal.getInstance().save(paymentMethod);
      return paymentMethod;
    }
  }
}
