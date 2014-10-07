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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.initial_data_load.journalentryjob_0_1.JournalEntryJob;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaElement;
import org.openbravo.model.financialmgmt.accounting.coa.ElementValue;
import org.openbravo.model.financialmgmt.gl.GLBatch;
import org.openbravo.model.financialmgmt.gl.GLCategory;
import org.openbravo.model.financialmgmt.gl.GLJournal;
import org.openbravo.model.financialmgmt.gl.GLJournalLine;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.financialmgmt.tax.Withholding;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.model.sales.SalesRegion;

/**
 * 
 * @author mirurita
 */
public class JournalEntriesProcess extends IdlServiceETL {

  final String ELEMENTTYPE_ACCOUNT = "AC";
  final JournalEntryJob job = new JournalEntryJob();
  GLBatch glBatchGeneral = null;
  ArrayList<GLBatch> glBatchList = new ArrayList<GLBatch>();

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
        new Parameter("TransactionalOrg", Parameter.STRING),
        new Parameter("JournalHeader", Parameter.STRING),
        new Parameter("JournalLine", Parameter.STRING),
        new Parameter("Currency", Parameter.STRING),
        new Parameter("AccountingSchema", Parameter.STRING),
        new Parameter("GLCategory", Parameter.STRING),
        new Parameter("AccountingDate", Parameter.STRING),
        new Parameter("Opening", Parameter.STRING), new Parameter("Alias", Parameter.STRING),
        new Parameter("Account", Parameter.STRING), new Parameter("Product", Parameter.STRING),
        new Parameter("BusinessPartner", Parameter.STRING),
        new Parameter("Project", Parameter.STRING), new Parameter("Campaign", Parameter.STRING),
        new Parameter("SalesRegion", Parameter.STRING),
        new Parameter("DebitAmount", Parameter.STRING),
        new Parameter("CreditAmount", Parameter.STRING), new Parameter("Tax", Parameter.STRING),
        new Parameter("Withholding", Parameter.STRING), new Parameter("Activity", Parameter.STRING) };
  }

  @Override
  protected BaseOBObject internalProcess(Object... values) throws Exception {

    return createJournalEntries((String) values[0],

    (String) values[1], (String) values[2], (String) values[3], (String) values[4],
        (String) values[5], (String) values[6],

        (String) values[7], (String) values[8], (String) values[9], (String) values[10],
        (String) values[11], (String) values[12], (String) values[13],

        (String) values[14], (String) values[15], (String) values[16], (String) values[17],
        (String) values[18], (String) values[19], (String) values[20]

    );

  }

  public BaseOBObject createJournalEntries(
      final String organization,
      final String transactionalOrg,

      // GL Journal Batch
      final String journalName,

      final String journalLineDescription, final String currency, final String acctSchema,
      final String glCategory, final String accountingDate,
      final String opening,

      // Account combination
      final String alias, final String account, final String product, final String businessPartner,
      final String project, final String campaign, final String salesRegion,

      // GL journal line
      final String debitAmount, final String creditAmount, final String tax,
      final String withholding, final String activity

  ) throws Exception {
    /*
     * STEP 1: Create the batch
     */
    try {

      String defCurrency = searchDefaultValue("Price List", "Currency", currency);
      Currency currencyinst = findDALInstance(true, Currency.class, new Value("iSOCode",
          defCurrency));

      if (rowOrganization.getSearchKey().equals("0")) {
        throw new OBException(Utility.messageBD(conn, "IDL_NOTVALID_ORGANIZATION",
            vars.getLanguage())
            + rowOrganization.getName());
      }

      GLBatch glBatch = null;
      String batchDescription = searchDefaultValue("JournalEntry", "BatchDescription", null);
      if (batchDescription == null) {
        batchDescription = "Initial Data Load Batch";
      }

      Date validAccountingDate = Parameter.DATE.parse(accountingDate);

      if (this.glBatchGeneral == null
          || !(glBatchGeneral.getOrganization().getSearchKey().equals(organization))) {

        glBatch = createBatch(batchDescription, glCategory, validAccountingDate, currencyinst);
        this.glBatchGeneral = glBatch;
        this.glBatchList.add(glBatch);

      } else if (glBatchGeneral.getOrganization().getSearchKey().equals(organization)) {
        glBatch = findDALInstance(true, GLBatch.class, new Value(GLBatch.PROPERTY_DESCRIPTION,
            batchDescription), new Value(GLBatch.PROPERTY_PERIOD, glBatchGeneral.getPeriod()),
            new Value(GLBatch.PROPERTY_DOCUMENTNO, glBatchGeneral.getDocumentNo()));
      }
      /*
       * STEP 2: General Ledger Journal
       */

      GLJournal glJournal = findDALInstance(true, GLJournal.class, new Value(
          GLJournal.PROPERTY_DESCRIPTION, journalName), new Value(GLJournal.PROPERTY_JOURNALBATCH,
          glBatch));

      if (glJournal == null) {

        glJournal = OBProvider.getInstance().get(GLJournal.class);
        glJournal.setActive(true);
        glJournal.setOrganization(rowOrganization);
        glJournal.setJournalBatch(glBatch);
        AcctSchema acccSche = findDALInstance(true, AcctSchema.class, new Value(
            AcctSchema.PROPERTY_NAME, acctSchema));
        glJournal.setAccountingSchema(acccSche);

        DocumentType docType = findDALInstance(true, DocumentType.class, new Value(
            DocumentType.PROPERTY_DOCUMENTCATEGORY, "GLJ"));

        // Document number
        Sequence seq2 = docType.getDocumentSequence();
        glJournal.setDocumentNo("*IDL*" + seq2.getNextAssignedNumber().toString());
        seq2.setNextAssignedNumber(seq2.getNextAssignedNumber() + seq2.getIncrementBy());
        OBDal.getInstance().save(seq2);
        OBDal.getInstance().flush();

        glJournal.setDescription(journalName); // Description
        glJournal.setDocumentType(docType); // Document type

        GLCategory glCat = findDALInstance(true, GLCategory.class, new Value(
            GLCategory.PROPERTY_NAME, glCategory));
        glJournal.setGLCategory(glCat); // GL Category

        glJournal.setDocumentDate(new Date());
        glJournal.setAccountingDate(validAccountingDate);

        glJournal.setPeriod(DALUtils.getPeriodByDate(validAccountingDate, rowOrganization)); // Period
        glJournal.setCurrency(currencyinst);

        glJournal.setOpening(DALUtils.getBoolean(opening)); // Is opening

        glJournal.setPostingType("A");

        OBDal.getInstance().save(glJournal);
        OBDal.getInstance().flush();
      }

      /*
       * STEP 3: General Ledger Journal line
       */

      GLJournalLine glJournalLine = OBProvider.getInstance().get(GLJournalLine.class);
      glJournalLine.setActive(true);
      glJournalLine.setOrganization(rowTransactionalOrg);

      final Session session = OBDal.getInstance().getSession();
      StringBuilder hql = new StringBuilder();
      hql.append(" SELECT max(jl.lineNo)");
      hql.append(" FROM FinancialMgmtGLJournalLine as jl inner join jl.journalEntry as j");
      hql.append(" WHERE j.id = ?");
      final Query query = session.createQuery(hql.toString());
      query.setParameter(0, glJournal.getId());
      List<Long> list = query.list();
      Long lineNo = new Long(10);
      if (list != null && list.size() > 0 && list.get(0) != null) {
        lineNo = (Long) list.get(0) + 10;
      }

      glJournalLine.setLineNo(lineNo);
      glJournalLine.setJournalEntry(glJournal);
      glJournalLine.setDescription(journalLineDescription);
      glJournalLine.setCurrency(currencyinst);
      glJournalLine.setAccountingDate(validAccountingDate);
      glJournalLine.setGenerated(false);
      glJournalLine.setDebit(BigDecimal.ZERO);
      glJournalLine.setCredit(BigDecimal.ZERO);

      glJournalLine.setForeignCurrencyDebit((debitAmount == null) ? BigDecimal.ZERO
          : new BigDecimal(debitAmount));

      glJournalLine.setForeignCurrencyCredit((creditAmount == null) ? BigDecimal.ZERO
          : new BigDecimal(creditAmount));

      AcctSchema oboAcctSchema = findDALInstance(true, AcctSchema.class, new Value(
          AcctSchema.PROPERTY_NAME, acctSchema));

      OBCriteria<AcctSchemaElement> criteriaAcctSchemaElement = OBDal.getInstance().createCriteria(
          AcctSchemaElement.class);
      criteriaAcctSchemaElement.add(Restrictions.eq(AcctSchemaElement.PROPERTY_ACCOUNTINGSCHEMA,
          oboAcctSchema));
      criteriaAcctSchemaElement.add(Restrictions.eq(AcctSchemaElement.PROPERTY_TYPE,
          ELEMENTTYPE_ACCOUNT));

      criteriaAcctSchemaElement.uniqueResult();

      ElementValue oboAccount = findDALInstanceOrderByOrg(true, ElementValue.class, new Value(
          ElementValue.PROPERTY_SEARCHKEY, account), new Value(ElementValue.PROPERTY_ELEMENTLEVEL,
          "S"), new Value(ElementValue.PROPERTY_ACCOUNTINGELEMENT + ".id",
          criteriaAcctSchemaElement.list().get(0).getAccountingElement().getId()));

      BusinessPartner oboBusinessPartner = findDALInstance(true, BusinessPartner.class, new Value(
          BusinessPartner.PROPERTY_SEARCHKEY, businessPartner));
      Product oboProduct = findDALInstance(true, Product.class, new Value(
          Product.PROPERTY_SEARCHKEY, product));
      Project oboProject = findDALInstance(true, Project.class, new Value(
          Project.PROPERTY_SEARCHKEY, project));
      Campaign oboSalesCampaign = findDALInstance(true, Campaign.class, new Value(
          Campaign.PROPERTY_SEARCHKEY, campaign));
      SalesRegion oboSalesRegion = findDALInstance(true, SalesRegion.class, new Value(
          SalesRegion.PROPERTY_SEARCHKEY, salesRegion));
      ABCActivity oboActivity = findDALInstance(true, ABCActivity.class, new Value(
          ABCActivity.PROPERTY_SEARCHKEY, activity));

      AccountingCombination acctCombi = findDALInstance(true, AccountingCombination.class,
          new Value(AccountingCombination.PROPERTY_ACCOUNT, oboAccount), new Value(
              AccountingCombination.PROPERTY_ACCOUNTINGSCHEMA, oboAcctSchema));

      if (acctCombi == null) {
        hql = new StringBuilder();
        hql.append(" SELECT FMA.accountingElement.id");
        hql.append(" FROM FinancialMgmtAcctSchemaElement FMA");
        hql.append(" WHERE accountingSchema.id =? AND type='AC'");
        Query query2 = session.createQuery(hql.toString());
        query2.setParameter(0, oboAcctSchema.getId());
        list = query2.list();
        BaseOBObject acctTree = OBDal.getInstance().get("FinancialMgmtElement", list.get(0));
        OBCriteria<ElementValue> criteria = OBDal.getInstance().createCriteria(ElementValue.class);
        criteria.add(Restrictions.eq(ElementValue.PROPERTY_ACCOUNTINGELEMENT, acctTree));
        criteria.add(Restrictions.eq(ElementValue.PROPERTY_ID, oboAccount.getId()));
        List<ElementValue> listAccount = criteria.list();
        if (!listAccount.isEmpty()) {
          acctCombi = OBProvider.getInstance().get(AccountingCombination.class);
          acctCombi.setActive(true);
          acctCombi.setOrganization(rowTransactionalOrg);
          acctCombi.setFullyQualified(true);
          acctCombi.setAccount(oboAccount);
          acctCombi.setAccountingSchema(oboAcctSchema);
          acctCombi.setClient(rowTransactionalOrg.getClient());

          OBDal.getInstance().save(acctCombi);
          OBDal.getInstance().flush();
        } else {
          throw new Exception(Utility
              .messageBD(conn, "IDL_AccountNotInAccountTree", vars.getLanguage())
              .replace("%0", oboAccount.getIdentifier()).replace("%1", acctTree.getIdentifier()));
        }
      }
      glJournalLine.setAccountingCombination(acctCombi);

      // Tax
      TaxRate tr = findDALInstance(true, TaxRate.class, new Value(TaxRate.PROPERTY_NAME, tax));
      glJournalLine.setTax(tr);

      // Withholding
      Withholding with = findDALInstance(true, Withholding.class, new Value(
          Withholding.PROPERTY_NAME, withholding));
      glJournalLine.setWithholding(with);

      // Accounting Dimensions
      // Product
      glJournalLine.setProduct(oboProduct);
      // Business Partner
      glJournalLine.setBusinessPartner(oboBusinessPartner);
      // Project
      glJournalLine.setProject(oboProject);
      // Activity
      glJournalLine.setActivity(oboActivity);
      // Sales Campaign
      glJournalLine.setSalesCampaign(oboSalesCampaign);
      // Sales Region
      glJournalLine.setSalesRegion(oboSalesRegion);

      OBDal.getInstance().save(glJournalLine);
      OBDal.getInstance().flush();

      // Commit after the post process

      return glBatch;

    } catch (Exception e) {
      this.glBatchGeneral = null;
      throw e;
    }

  }

  protected GLBatch createBatch(String batchDescription, String glCategory,
      Date validAccountingDate, Currency currencyinst) throws Exception {

    GLBatch glBatch = OBProvider.getInstance().get(GLBatch.class);
    glBatch.setActive(true);
    glBatch.setOrganization(rowOrganization); // Transactional Org

    glBatch.setDescription(batchDescription); // Description (NOT NULL)

    // Document number
    Sequence seq = findDALInstance(true, Sequence.class, new Value(Sequence.PROPERTY_NAME,
        "DocumentNo_GL_JournalBatch"));
    glBatch.setDocumentNo("*IDL*" + seq.getNextAssignedNumber().toString());
    seq.setNextAssignedNumber(seq.getNextAssignedNumber() + seq.getIncrementBy());
    OBDal.getInstance().save(seq);
    OBDal.getInstance().flush();

    GLCategory glCat = findDALInstance(true, GLCategory.class, new Value(GLCategory.PROPERTY_NAME,
        glCategory));
    glBatch.setGLCategory(glCat); // GL Category
    glBatch.setDocumentDate(new Date()); // Document Date
    glBatch.setAccountingDate(validAccountingDate); // Accounting Date

    glBatch.setPeriod(DALUtils.getPeriodByDate(validAccountingDate, rowOrganization)); // Period

    glBatch.setCurrency(currencyinst); // Currency

    glBatch.setTotalCreditAmount(BigDecimal.ZERO);
    glBatch.setTotalDebitAmount(BigDecimal.ZERO);

    OBDal.getInstance().save(glBatch);
    OBDal.getInstance().flush();

    return glBatch;
  }

  @Override
  protected void postProcess() throws Exception {
    ListIterator<GLBatch> iterador = this.glBatchList.listIterator();
    // Execute
    try {

      while (iterador.hasNext()) {
        callProcess("GL_JournalBatch_Post", iterador.next().getId());
      }
      // End process
      OBDal.getInstance().commitAndClose();
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      throw e;

    } finally {
      this.glBatchList.clear();
      this.glBatchGeneral = null;

    }

  }
}
