/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.test;

import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.idl.proc.AssetsProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.test.base.BaseTest;

public class TestAssets extends BaseTest {
  public void testAssetsImport() {

    // TransactionalOrg=Pamplona&Name=Asset1&SearchKey=Asset1&Description=My first
    // asset&ProductName=rma
    // &AssetCategory=General&Quantity=5.0&CalculateType=Time&AmortizeType=Monthly&UsableLifeYears=
    // &UsableLifeMonths=2&AnnualDepreciation=&Currency=EUR&PurchaseDate=2009-11-20
    // &CancellationDate=&DepreciationStartDate=2009-11-23&DepreciationEndDate=&AssetValue=1
    // &ResidualAssetValue=2&DepreciationAmount=3&PreviousleDepreciatedAmount=4&DepreciatedValue=5&DepreciatedPlan=6

    OBContext.setOBContext("8DC521270BCA499DA2E88A826D41DF18", // IDL Client Admin user
        "5232A256B80D4ACEB89EF9D3A1D1A070", // IDL Client Admin role
        "B6471BE1FE9B4CC88893B7442C687319", // IDL Client
        "42A8DA5812264B96A2B6C1FE08F6B45B" // IDL Org
    );

    AssetsProcess ap = new AssetsProcess();
    ap.init(new DalConnectionProvider(), null);

    ap.checkOrganization("Asset", "0");
    ap.checkTransactionalOrganization("Asset", "Pamplona");

    try {
      BaseOBObject asset1 = ap.createAssets("Pamplona", "Asset1", "Asset1", "My first asset",
          "rma", "General", "5.0", "Time", "Monthly", "", "2", "", "EUR", "2009-05-25",
          "2010-10-25", "2009-08-25", "2010-05-25", "1", "2", "3", "4", "5", "6");
      assert (asset1 != null);
      assert (asset1.get("name").equals("Asset1"));
      System.out.println(asset1);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject asset2 = ap.createAssets("Pamplona", "Asset2", "Asset2", "My second asset",
          "rmb", "General", "5.0", "Percentage", "", "", "", "12", "EUR", "2009-05-25",
          "2010-10-25", "2009-08-25", "2010-05-25", "1", "2", "3", "4", "5", "6");
      assert (asset2 != null);
      assert (asset2.get("name").equals("Asset2"));
      System.out.println(asset2);
    } catch (Exception e) {
      fail(e.toString());
    }

  }

  public void publicSetUp() throws Exception {
    setUp();
  }

  public void publicTearDown() throws Exception {
    tearDown();
  }

  public static void main(String[] args) {

    TestAssets test = new TestAssets();

    try {
      test.publicSetUp();

      test.testAssetsImport();

      test.publicTearDown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
