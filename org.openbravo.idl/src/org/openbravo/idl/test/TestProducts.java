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
import org.openbravo.idl.proc.ProductsProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.test.base.BaseTest;

public class TestProducts extends BaseTest {

  public void testImportProducts() {

    OBContext.setOBContext("8DC521270BCA499DA2E88A826D41DF18", // IDL Client Admin user
        "5232A256B80D4ACEB89EF9D3A1D1A070", // IDL Client Admin role
        "B6471BE1FE9B4CC88893B7442C687319", // IDL Client
        "42A8DA5812264B96A2B6C1FE08F6B45B" // IDL Org
    );

    ProductsProcess p = new ProductsProcess();
    p.init(new DalConnectionProvider(), null);

    p.checkOrganization("Product", "0");

    try {
      BaseOBObject product1 = p.createProduct(null, "rma", "Raw material A",
          "Raw material for purchase", "3344567", "Raw material", "Bg", "Item", "False", "False",
          "FALSE", "Average", "Serial Number", null, "True", "True", "False", "IVA 4%", null, null,
          "2.5");
      assert (product1 != null);
      assert (product1.get("name").equals("Raw material A"));
      System.out.println(product1);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject product2 = p.createProduct(null, "rmb", "Raw material B",
          "Raw material for purchase", "3344534", "Raw material", "Bg", "Item", "False", "False",
          "FALSE", "Average", "Serial Number", null, "True", "True", "False", "IVA 4%", null, null,
          "3.5");
      assert (product2 != null);
      assert (product2.get("name").equals("Raw material B"));
      System.out.println(product2);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject product3 = p.createProduct(null, "rmc", "Raw material C",
          "Raw material for purchase", "3344523", "Raw material", "Bg", "Item", "False", "False",
          "FALSE", "Average", "Serial Number", null, "True", "True", "False", "IVA 4%", null, null,
          "4.5");
      assert (product3 != null);
      assert (product3.get("name").equals("Raw material C"));
      System.out.println(product3);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject product4 = p.createProduct(null, "fga", "Final good A",
          "Final goods for selling", "3344567", "Final goods", "Kg", "Item", "False", "False",
          "FALSE", "Average", "Lots", null, "True", "False", "True", "IVA 16%", null, "15.5", null);
      assert (product4 != null);
      assert (product4.get("name").equals("Final good A"));
      System.out.println(product4);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject product5 = p.createProduct(null, "fgb", "Final good B",
          "Final goods for selling", "3344567", "Final goods", "Kg", "Item", "False", "False",
          "FALSE", "Average", "Lots", null, "True", "False", "True", "IVA 16%", null, "16.5", null);
      assert (product5 != null);
      assert (product5.get("name").equals("Final good B"));
      System.out.println(product5);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject product6 = p.createProduct(null, "fgc", "Final good C",
          "Final goods for selling", "3344567", "Final goods", "Kg", "Item", "False", "False",
          "FALSE", "Average", "Lots", null, "True", "False", "True", "IVA 16%", null, "17.5", null);
      assert (product6 != null);
      assert (product6.get("name").equals("Final good C"));
      System.out.println(product6);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject product7 = p.createProduct(null, "sva", "Service A", "Consultant services",
          "3344567", "Services", "HR", "Service", "False", "False", "FALSE", null, null, null,
          "False", "False", "True", "IVA 16%", null, "52.5", null);
      assert (product7 != null);
      assert (product7.get("name").equals("Service A"));
      System.out.println(product7);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject product8 = p.createProduct(null, "svb", "Service B", "Consultant services",
          "3344567", "Services", "HR", "Service", "False", "False", "FALSE", null, null, null,
          "False", "False", "True", "IVA 16%", null, "58.5", null);
      assert (product8 != null);
      assert (product8.get("name").equals("Service B"));
      System.out.println(product8);
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

    TestProducts test = new TestProducts();

    try {
      test.publicSetUp();

      test.testImportProducts();

      test.publicTearDown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
