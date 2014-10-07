/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.test;

import org.openbravo.idl.proc.ReferenceList;
import org.openbravo.test.base.BaseTest;

/**
 * 
 * @author adrian
 */
public class TestReferenceLists extends BaseTest {

  public void testReferenceLists() {

    ReferenceList r = ReferenceList.getReferenceListByName("AmortizationCalcType", "es_ES");
    // ReferenceList r = ReferenceList.getReferenceList("216", "es_ES");
    System.out.println(r.toString());
    System.out.println(r.getReferenceValue("Time"));
    System.out.println(r.getReferenceValue("Ahorros"));
    System.out.println(r.getReferenceValue("Dummy"));
    r = ReferenceList.getReferenceList("216", "en_US");
    System.out.println(r.toString());
    System.out.println(r.getReferenceValue("Savings"));
    r = ReferenceList.getReferenceList("215", "es_ES");
    System.out.println(r.toString());
    r = ReferenceList.getReferenceList("215", "en_US");
    System.out.println(r.toString());

    r = ReferenceList.getReferenceListByName("All_Payment Rule", "es_ES");
    System.out.println(r.toString());
    r = ReferenceList.getReferenceListByName("C_Order InvoiceRule", "es_ES");
    System.out.println(r.toString());
    r = ReferenceList.getReferenceListByName("M_Product_ProductType", "es_ES");
    System.out.println(r.toString());
    r = ReferenceList.getReferenceListByName("Cost Type", "es_ES");
    System.out.println(r.toString());
  }

  public void publicSetUp() throws Exception {
    setUp();
  }

  public void publicTearDown() throws Exception {
    tearDown();
  }

  public static void main(String[] args) {

    TestReferenceLists test = new TestReferenceLists();

    try {
      test.publicSetUp();

      test.testReferenceLists();

      test.publicTearDown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
