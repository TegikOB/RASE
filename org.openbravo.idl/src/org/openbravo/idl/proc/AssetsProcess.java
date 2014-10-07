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

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_process.assets.AssetLinearDepreciationMethodProcess;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.idl.initial_data_load.assetjob_0_1.AssetJob;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.assetmgmt.Asset;
import org.openbravo.model.financialmgmt.assetmgmt.AssetGroup;
import org.openbravo.service.db.DbUtility;

/**
 * 
 * @author mirurita
 */
public class AssetsProcess extends IdlServiceETL {

  final AssetJob job = new AssetJob();

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
    return new Parameter[] { new Parameter("TransactionalOrg", Parameter.STRING),
        new Parameter("SearchKey", Parameter.STRING), new Parameter("Name", Parameter.STRING),
        new Parameter("Description", Parameter.STRING), new Parameter("Product", Parameter.STRING),
        new Parameter("AssetCategory", Parameter.STRING),
        new Parameter("Quantity", Parameter.STRING),
        new Parameter("CalculateType", Parameter.STRING),
        new Parameter("AmortizeSchedule", Parameter.STRING),
        new Parameter("UsableLifeYears", Parameter.STRING),
        new Parameter("UsableLifeMonths", Parameter.STRING),
        new Parameter("AnnualDepreciation", Parameter.STRING),
        new Parameter("Currency", Parameter.STRING),
        new Parameter("PurchaseDate", Parameter.STRING),
        new Parameter("CancellationDate", Parameter.STRING),
        new Parameter("DepreciationStartDate", Parameter.STRING),
        new Parameter("DepreciationEndDate", Parameter.STRING),
        new Parameter("AssetValue", Parameter.STRING),
        new Parameter("ResidualAssetValue", Parameter.STRING),
        new Parameter("DepreciationAmount", Parameter.STRING),
        new Parameter("PreviouslyDepreciatedAmount", Parameter.STRING),
        new Parameter("DepreciatedValue", Parameter.STRING),
        new Parameter("DepreciatedPlan", Parameter.STRING) };
  }

  @Override
  protected BaseOBObject internalProcess(Object... values) throws Exception {

    return createAssets((String) values[0], (String) values[1], (String) values[2],
        (String) values[3], (String) values[4], (String) values[5], (String) values[6],
        (String) values[7], (String) values[8], (String) values[9], (String) values[10],
        (String) values[11], (String) values[12], (String) values[13], (String) values[14],
        (String) values[15], (String) values[16], (String) values[17], (String) values[18],
        (String) values[19], (String) values[20], (String) values[21], (String) values[22]);

  }

  public BaseOBObject createAssets(final String transactionalOrg, final String searchkey,
      final String name, final String description, final String productName,
      final String assetCategory, final String quantity, final String calculateType,
      final String amortizeType, final String usableLifeYears, final String usableLifeMonths,
      final String annualDepreciation, final String currency, final String purchaseDate,
      final String cancellationDate, final String DepreciationStartDate,
      final String depreciationEndDate, final String assetValue, final String residualAssetValue,
      final String depreciationAmount, final String previousleDepreciatedAmount,
      final String depreciatedValue, final String depreciatedPlan) throws Exception {

    Currency currencyinst = findDALInstance(true, Currency.class, new Value("iSOCode", currency));

    Asset asset = OBProvider.getInstance().get(Asset.class);
    asset.setActive(true);
    if (rowTransactionalOrg.getSearchKey().equals("0")) {
      throw new OBException(
          Utility.messageBD(conn, "IDL_NOTVALID_ORGANIZATION", vars.getLanguage())
              + rowTransactionalOrg.getName());
    }
    asset.setOrganization(rowTransactionalOrg);

    // Document number
    Sequence seq = findDALInstance(true, Sequence.class, new Value(Sequence.PROPERTY_NAME,
        "DocumentNo_A_Asset"));
    asset.setDocumentNo("*IDL*" + seq.getNextAssignedNumber().toString());
    seq.setNextAssignedNumber(seq.getNextAssignedNumber() + seq.getIncrementBy());
    OBDal.getInstance().save(seq);
    OBDal.getInstance().flush();

    // SearchKey and name
    asset.setSearchKey(searchkey);
    asset.setName(name);
    asset.setDescription(description);
    asset.setCurrency(currencyinst);

    Product product = findDALInstance(true, Product.class, new Value(Product.PROPERTY_SEARCHKEY,
        productName));
    asset.setProduct(product);

    AssetGroup assetGroup = findDALInstance(true, AssetGroup.class, new Value(
        AssetGroup.PROPERTY_NAME, assetCategory));
    if (assetGroup == null) {
      assetGroup = OBProvider.getInstance().get(AssetGroup.class);
      assetGroup.setActive(true);
      Organization org = DALUtils
          .findOrganization(searchDefaultValue("Asset", "Organization", null));
      assetGroup.setOrganization(org);
      assetGroup.setName(assetCategory);
      OBDal.getInstance().save(assetGroup);
      OBDal.getInstance().flush();
    }
    asset.setAssetCategory(assetGroup);

    if (quantity != null) {
      asset.setQuantity(new BigDecimal(quantity));
    }

    asset.setDepreciate(true);

    asset.setDepreciationType("LI"); // Linear
    asset.setCalculateType(getReferenceValue("AmortizationCalcType", calculateType));

    String aux = getReferenceValue("Amortization schedule", amortizeType);
    String schedule = (aux == null) ? "MO" : aux;
    asset.setAmortize(schedule);

    if (usableLifeMonths != null) {
      asset.setUsableLifeMonths(new Long(usableLifeMonths));
    }

    if (usableLifeYears != null) {
      asset.setUsableLifeYears(new Long(usableLifeYears));
    }

    if (annualDepreciation != null) {
      asset.setAnnualDepreciation(Parameter.BIGDECIMAL.parse(annualDepreciation));
    }

    asset.setPurchaseDate(Parameter.DATE.parse(purchaseDate));

    asset.setCancellationDate(Parameter.DATE.parse(cancellationDate));

    asset.setDepreciationStartDate(Parameter.DATE.parse(DepreciationStartDate));

    asset.setDepreciationEndDate(Parameter.DATE.parse(depreciationEndDate));

    if (assetValue != null) {
      asset.setAssetValue(new BigDecimal(assetValue));
    }

    if (residualAssetValue != null) {
      asset.setResidualAssetValue(new BigDecimal(residualAssetValue));
    }

    if (depreciationAmount != null) {
      asset.setDepreciationAmt(new BigDecimal(depreciationAmount));
    }

    if (previousleDepreciatedAmount != null) {
      asset.setPreviouslyDepreciatedAmt(new BigDecimal(previousleDepreciatedAmount));
    }

    if (depreciatedValue != null) {
      asset.setDepreciatedValue(new BigDecimal(depreciatedValue));
    }

    if (depreciatedPlan != null) {
      asset.setDepreciatedPlan(new BigDecimal(depreciatedPlan));
    }

    OBDal.getInstance().save(asset);
    OBDal.getInstance().flush();

    // Execute
    OBError msg = new OBError();
    try {
      AssetLinearDepreciationMethodProcess assetProcess = new AssetLinearDepreciationMethodProcess();
      msg = assetProcess.generateAmortizationPlan(asset);
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();

      Throwable ex = DbUtility.getUnderlyingSQLException(e);
      String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(message);
      throw new OBException(msg.getMessage());
    }
    if (msg.getType().equals("Error")) {
      OBDal.getInstance().rollbackAndClose();
      throw new OBException(msg.getMessage());
    }

    // End process
    OBDal.getInstance().commitAndClose();

    return asset;

  }

}
