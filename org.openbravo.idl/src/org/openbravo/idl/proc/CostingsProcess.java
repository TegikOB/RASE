/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.idl.initial_data_load.costingjob_0_1.CostingJob;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.Costing;

/**
 * 
 * @author adrian
 */
public class CostingsProcess extends IdlServiceETL {

  final CostingJob job = new CostingJob();
  private static Boolean isMigrated = false;

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
  protected BaseOBObject internalProcess(Object... values) throws Exception {

    return createCosting((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4], (String) values[5], (String) values[6],
        (String) values[7], (String) values[8], (String) values[9], (String) values[10]);

  }

  @Override
  public Parameter[] getParameters() {
    return new Parameter[] { new Parameter("Organization", Parameter.STRING),
        new Parameter("Product", Parameter.STRING), new Parameter("CostType", Parameter.STRING),
        new Parameter("Cost", Parameter.STRING), new Parameter("StartingDate", Parameter.STRING),
        new Parameter("EndingDate", Parameter.STRING), new Parameter("Quantity", Parameter.STRING),
        new Parameter("TotalMovementQuantity", Parameter.STRING),
        new Parameter("Price", Parameter.STRING), new Parameter("Warehouse", Parameter.STRING),
        new Parameter("Currency", Parameter.STRING) };
  }

  public BaseOBObject createCosting(final String org, final String product, final String costtype,
      final String cost, final String startingdate, final String endingdate, final String quantity,
      final String totalmovementquantity, final String price, final String warehouse,
      final String currency) throws Exception {

    Costing costing = OBProvider.getInstance().get(Costing.class);
    // costing.setActive(true);
    costing.setOrganization(rowOrganization);
    costing.setProduct(findDALInstance(false, Product.class, new Value("searchKey", product)));
    costing.setCostType(getReferenceValue("Cost Type", costtype));
    costing.setCost(Parameter.BIGDECIMAL.parse(cost));

    costing.setStartingDate(Parameter.DATE.parse(startingdate));
    costing.setEndingDate(Parameter.DATE.parse(endingdate));
    costing.setQuantity(Parameter.BIGDECIMAL.parse(quantity));
    costing.setTotalMovementQuantity(Parameter.BIGDECIMAL.parse(totalmovementquantity));
    costing.setPrice(Parameter.BIGDECIMAL.parse(price));
    costing
        .setWarehouse(findDALInstance(false, Warehouse.class, new Value("searchKey", warehouse)));
    Currency currencyObj = findDALInstance(true, Currency.class, new Value("iSOCode", currency));
    if (currencyObj == null) {
      Organization legalEntity = OBContext.getOBContext().getOrganizationStructureProvider()
          .getLegalEntity(rowOrganization);
      currencyObj = legalEntity.getCurrency();
      if (currencyObj == null) {
        Client client = OBDal.getInstance().get(Client.class,
            OBContext.getOBContext().getCurrentClient().getId());
        currencyObj = client.getCurrency();
      }
    }
    costing.setCurrency(findDALInstance(true, Currency.class,
        new Value("iSOCode", currencyObj.getISOCode())));

    costing.setProduction(Boolean.FALSE);
    costing.setManual(Boolean.TRUE);
    costing.setPermanent(Boolean.TRUE);

    OBDal.getInstance().save(costing);
    OBDal.getInstance().flush();

    // OBDal.getInstance().commitAndClose();

    return costing;
  }

  @Override
  protected void postProcess() throws Exception {
    if (!isCostingEngineMigrated()) {
      try {
        callProcess("M_Generate_Average_Costs", null);
      } catch (Exception e) {
        OBDal.getInstance().rollbackAndClose();
        throw e;
      }
    }
    OBDal.getInstance().commitAndClose();
  }

  private Boolean isCostingEngineMigrated() {
    if (isMigrated == null || isMigrated == false) {
      OBContext.setAdminMode(false);
      try {
        //
        OBQuery<Preference> crQry = OBDal.getInstance().createQuery(Preference.class,
            Preference.PROPERTY_ATTRIBUTE + " ='Cost_Eng_Ins_Migrated'");
        crQry.setFilterOnReadableClients(false);
        crQry.setFilterOnReadableOrganization(false);
        crQry.setMaxResult(1);
        isMigrated = crQry.uniqueResult() != null;
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    return isMigrated;
  }

}
