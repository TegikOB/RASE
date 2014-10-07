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
import org.openbravo.idl.proc.PriceListsProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.test.base.BaseTest;

public class TestPriceLists extends BaseTest {

  public void testImportPriceLists() {

    OBContext.setOBContext("8DC521270BCA499DA2E88A826D41DF18", // IDL Client Admin user
        "5232A256B80D4ACEB89EF9D3A1D1A070", // IDL Client Admin role
        "B6471BE1FE9B4CC88893B7442C687319", // IDL Client
        "42A8DA5812264B96A2B6C1FE08F6B45B" // IDL Org
    );

    PriceListsProcess plp = new PriceListsProcess();
    plp.init(new DalConnectionProvider(), null);

    plp.checkOrganization("Price List", "0");

    BaseOBObject priceList2;
    try {
      priceList2 = plp.createPriceList(null, "Second Testing Price List Sales", null, null, null,
          null, null, "Second List Version 2009", "Default Price List Schema", "2009-01-01", null,
          "fga", "78.305", "10.30", "9.80");
      assert (priceList2 != null);
      assert (priceList2.get("name").equals("Second Testing Price List Sales"));
      System.out.println(priceList2);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject priceList3 = plp.createPriceList(null, "Third Testing Price List Sales", null,
          null, null, null, null, "Third List Version 2009", "Default Price List Schema",
          "2009-01-01", null, "fga", "15.308", "10.30", "9.80");
      assert (priceList3 != null);
      assert (priceList3.get("name").equals("Third Testing Price List Sales"));
      System.out.println(priceList3);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject priceList4 = plp.createPriceList(null, "Second Testing Price List Sales", null,
          null, null, null, null, "Second List Version 2009", "Default Price List Schema",
          "2009-01-01", null, "fgb", "10.3025", "10.30", "9.80");
      assert (priceList4 != null);
      assert (priceList4.get("name").equals("Second Testing Price List Sales"));
      System.out.println(priceList4);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject priceList5 = plp.createPriceList(null, "Third Testing Price List Sales", null,
          null, null, null, null, "Third List Version 2009", "Default Price List Schema",
          "2009-01-01", null, "fgb", "52.3078", "10.30", "9.80");
      assert (priceList5 != null);
      assert (priceList5.get("name").equals("Third Testing Price List Sales"));
      System.out.println(priceList5);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject priceList6 = plp.createPriceList(null, "Second Testing Price List Sales", null,
          null, null, null, null, "Second List Version 2009", "Default Price List Schema",
          "2009-01-01", null, "fgc", "24.305", "10.30", "9.80");
      assert (priceList6 != null);
      assert (priceList6.get("name").equals("Second Testing Price List Sales"));
      System.out.println(priceList6);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject priceList7 = plp.createPriceList(null, "Third Testing Price List Sales", null,
          null, null, null, null, "Third List Version 2009", "Default Price List Schema",
          "2009-01-01", null, "fgc", "23.3078", "10.30", "9.80");
      assert (priceList7 != null);
      assert (priceList7.get("name").equals("Third Testing Price List Sales"));
      System.out.println(priceList7);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject priceList8 = plp.createPriceList(null, "Service Price list", null, null, null,
          null, null, "Service List Version 2009", "Default Price List Schema", "2009-01-01", null,
          "sva", "11.58", "10.30", "9.80");
      assert (priceList8 != null);
      assert (priceList8.get("name").equals("Service Price list"));
      System.out.println(priceList8);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject priceList9 = plp.createPriceList(null, "Service Price list", null, null, null,
          null, null, "Service List Version 2009", "Default Price List Schema", "2009-01-01", null,
          "svb", "48.56", "10.30", "9.80");
      assert (priceList9 != null);
      assert (priceList9.get("name").equals("Service Price list"));
      System.out.println(priceList9);
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

    TestPriceLists test = new TestPriceLists();

    try {
      test.publicSetUp();

      test.testImportPriceLists();

      test.publicTearDown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
