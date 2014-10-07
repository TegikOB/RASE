/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.idl.initial_data_load.projectjob_0_1.ProjectJob;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.project.Project;
import org.openbravo.model.project.ProjectType;

public class ProjectProcess extends IdlServiceETL {

  final ProjectJob job = new ProjectJob();

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
        new Parameter("ProjectType", Parameter.STRING),
        new Parameter("ProjectStatus", Parameter.STRING),
        new Parameter("Processed", Parameter.STRING), new Parameter("StartDate", Parameter.STRING),
        new Parameter("DateFinish", Parameter.STRING),
        new Parameter("DateContract", Parameter.STRING),
        new Parameter("Responsible", Parameter.STRING),
        new Parameter("BusinessPartner", Parameter.STRING),
        new Parameter("BPLocation", Parameter.STRING), new Parameter("User", Parameter.STRING),
        new Parameter("PaymentMethod", Parameter.STRING),
        new Parameter("PaymentTerm", Parameter.STRING),
        new Parameter("Commitment", Parameter.STRING),
        new Parameter("CommitCeiling", Parameter.STRING),
        new Parameter("CommitedAmt", Parameter.STRING),
        new Parameter("CommitedQty", Parameter.STRING),
        new Parameter("ServRevenue", Parameter.STRING),
        new Parameter("ExpExpenses", Parameter.STRING),
        new Parameter("ServSerCost", Parameter.STRING),
        new Parameter("ServOutCost", Parameter.STRING),
        new Parameter("ServCost", Parameter.STRING),
        new Parameter("ExpReinvoicing", Parameter.STRING),
        new Parameter("ServMargin", Parameter.STRING),
        new Parameter("ExpMargin", Parameter.STRING),
        new Parameter("SalesRepresentative", Parameter.STRING),
        new Parameter("Warehouse", Parameter.STRING), new Parameter("PriceList", Parameter.STRING),
        new Parameter("POReference", Parameter.STRING), new Parameter("Summary", Parameter.STRING),
        new Parameter("Description", Parameter.STRING) };
  }

  @Override
  protected BaseOBObject internalProcess(Object... values) throws Exception {

    return createProject((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4], (String) values[5], (String) values[6],
        (String) values[7], (String) values[8], (String) values[9], (String) values[10],
        (String) values[11], (String) values[12], (String) values[13], (String) values[14],
        (String) values[15], (String) values[16], (String) values[17], (String) values[18],
        (String) values[19], (String) values[20], (String) values[21], (String) values[22],
        (String) values[23], (String) values[24], (String) values[25], (String) values[26],
        (String) values[27], (String) values[28], (String) values[29], (String) values[30],
        (String) values[31], (String) values[32]);
  }

  public BaseOBObject createProject(String strOrg, String strSearchkey, String strName,
      String strProjectType, String strProjectStatus, String strProcessed, String strDateStart,
      String strDateFinish, String strDateContract, String strResponsible,
      String strBusinessPartner, String strBPLocation, String strUser, String strPaymentMethod,
      String strPaymentTerm, String strCommitment, String strCommitCeiling, String strCommitedAmt,
      String strCommitedQty, String strServRevenue, String strExpExpenses, String strServSerCost,
      String strServOutCost, String strServCost, String strExpReinvoicing, String strServMargin,
      String strExpMargin, String strSalesRepresentative, String strWarehouse, String strPriceList,
      String strPOReference, String strSummary, String strDescription) throws Exception {

    // Business Partner
    Project project = findDALInstance(false, Project.class, new Value("searchKey", strSearchkey));

    if (project != null) {
      throw new OBException(OBMessageUtils.messageBD("IDL_PROJECT_EXISTS") + strSearchkey);
    }

    project = OBProvider.getInstance().get(Project.class);
    project.setOrganization(rowTransactionalOrg);
    project.setActive(true);
    project.setSearchKey(strSearchkey);
    project.setName(strName);
    if (StringUtils.isNotEmpty(strProjectType)) {
      ProjectType projectType = findDALInstance(false, ProjectType.class, new Value(
          ProjectType.PROPERTY_NAME, strProjectType));
      project.setProjectType(projectType);
    }
    project.setProjectStatus(getReferenceValue("ProjectStatus", strProjectStatus));
    project.setProcessed(DALUtils.getBoolean(strProcessed));
    project.setStartingDate(Parameter.DATE.parse(strDateStart));
    project.setEndingDate(Parameter.DATE.parse(strDateFinish));
    project.setContractDate(Parameter.DATE.parse(strDateContract));
    if (StringUtils.isNotEmpty(strResponsible)) {
      BusinessPartner responsible = findDALInstance(false, BusinessPartner.class, new Value(
          BusinessPartner.PROPERTY_NAME, strResponsible), new Value(
          BusinessPartner.PROPERTY_EMPLOYEE, true));
      project.setPersonInCharge(responsible);
    }
    if (StringUtils.isNotEmpty(strBusinessPartner)) {
      BusinessPartner bp = findDALInstance(false, BusinessPartner.class, new Value(
          BusinessPartner.PROPERTY_NAME, strBusinessPartner));
      project.setBusinessPartner(bp);
      if (StringUtils.isNotEmpty(strBPLocation)) {
        Location bpLocation = findDALInstance(false, Location.class, new Value(
            Location.PROPERTY_NAME, strBPLocation));
        project.setPartnerAddress(bpLocation);
      }
      if (StringUtils.isNotEmpty(strUser)) {
        User user = findDALInstance(false, User.class, new Value(User.PROPERTY_NAME, strUser));
        project.setUserContact(user);
      }
    }
    if (StringUtils.isNotEmpty(strPaymentMethod)) {
      FIN_PaymentMethod paymentMethod = findDALInstance(false, FIN_PaymentMethod.class, new Value(
          FIN_PaymentMethod.PROPERTY_NAME, strPaymentMethod));
      project.setPaymentMethod(paymentMethod);
    }
    if (StringUtils.isNotEmpty(strPaymentTerm)) {
      PaymentTerm paymentTerm = findDALInstance(false, PaymentTerm.class, new Value(
          PaymentTerm.PROPERTY_NAME, strPaymentTerm));
      project.setPaymentTerms(paymentTerm);
    }
    project.setLegallyBindingContract(DALUtils.getBoolean(strCommitment) ? true : false);
    project.setPriceCeiling(DALUtils.getBoolean(strCommitCeiling) ? true : false);
    project.setContractAmount(StringUtils.isNotEmpty(strCommitedAmt) ? Parameter.BIGDECIMAL
        .parse(strCommitedAmt) : BigDecimal.ZERO);
    project.setContractQuantity(StringUtils.isNotEmpty(strCommitedQty) ? Parameter.BIGDECIMAL
        .parse(strCommitedQty) : BigDecimal.ZERO);
    project.setServiceRevenue(Parameter.BIGDECIMAL.parse(strServRevenue));
    project.setPlannedExpenses(Parameter.BIGDECIMAL.parse(strExpExpenses));
    BigDecimal servSerCost = Parameter.BIGDECIMAL.parse(strServSerCost);
    project.setServicesProvidedCost(servSerCost);
    BigDecimal servOutCost = Parameter.BIGDECIMAL.parse(strServOutCost);
    project.setOutsourcedCost(servOutCost);
    BigDecimal servCost = Parameter.BIGDECIMAL.parse(strServCost);
    project.setServiceCost(servCost);
    if (servCost == null && (servSerCost != null || servOutCost != null)) {
      servSerCost = servSerCost == null ? BigDecimal.ZERO : servSerCost;
      servOutCost = servOutCost == null ? BigDecimal.ZERO : servOutCost;
      servCost = servSerCost.add(servOutCost);
      project.setServiceCost(servCost);
    }
    project.setReinvoicedExpenses(Parameter.BIGDECIMAL.parse(strExpReinvoicing));
    project.setServiceMargin(Parameter.BIGDECIMAL.parse(strServMargin));
    project.setExpensesMargin(Parameter.BIGDECIMAL.parse(strExpMargin));
    if (StringUtils.isNotEmpty(strSalesRepresentative)) {
      User salesRep = findDALInstance(false, User.class, new Value(User.PROPERTY_NAME,
          strSalesRepresentative));
      project.setSalesRepresentative(salesRep);
    }
    if (StringUtils.isNotEmpty(strWarehouse)) {
      Warehouse wh = findDALInstance(false, Warehouse.class, new Value(Warehouse.PROPERTY_NAME,
          strWarehouse));
      project.setWarehouse(wh);
    }
    if (StringUtils.isNotEmpty(strPriceList)) {
      PriceList pl = findDALInstance(false, PriceList.class, new Value(PriceList.PROPERTY_NAME,
          strPriceList));
      project.setPriceList(pl);
      project.setCurrency(pl.getCurrency());
    } else {
      project.setCurrency(FinancialUtils.getLegalEntityCurrency(rowTransactionalOrg));
    }
    project.setOrderReference(strPOReference);
    project.setSummaryLevel(DALUtils.getBoolean(strSummary) ? true : false);
    project.setDescription(strDescription);

    project.setProjectCategory("S");

    OBDal.getInstance().save(project);
    // End process
    OBDal.getInstance().commitAndClose();

    return project;
  }
}