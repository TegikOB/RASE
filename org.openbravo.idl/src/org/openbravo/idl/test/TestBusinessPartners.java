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
import org.openbravo.idl.proc.BusinessPartnersProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.test.base.BaseTest;

public class TestBusinessPartners extends BaseTest {

  public void testImportBusinessPartners() {

    OBContext.setOBContext("8DC521270BCA499DA2E88A826D41DF18", // IDL Client Admin user
        "5232A256B80D4ACEB89EF9D3A1D1A070", // IDL Client Admin role
        "B6471BE1FE9B4CC88893B7442C687319", // IDL Client
        "42A8DA5812264B96A2B6C1FE08F6B45B" // IDL Org
    );

    BusinessPartnersProcess bpp = new BusinessPartnersProcess();
    bpp.init(new DalConnectionProvider(), null);

    bpp.checkOrganization("Business Partner", "0");

    try {
      BaseOBObject businessPartner1 = bpp.createBusinessPartner("Pamplona", "va", "Vendor A",
          "Testing purposes", "44626500V", "False", null, "Vendors", null, "Enorme", "11 2º A",
          "26564", "Pamplona", "NAVARRA", "Spain", "678894111", "948784222", "948784333", "true",
          "true", "true", "true", "150", "True", "False", "12345678912345678912", null, "Alfred",
          "Alfred", "Lujan", "Testing", "alu@testing.com", "Chief officer provider", "678705552",
          "948255042", "654336688", "True", "Bank Deposit", "90D", "Default Purchase Price List",
          null, "Vendor National Testing... 5543-5343-64-1212121212", "False", null, null, null,
          null, null, null, null, "False", "False");
      assert (businessPartner1 != null);
      assert (businessPartner1.get("name").equals("Vendor A"));
      System.out.println(businessPartner1);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject businessPartner2 = bpp.createBusinessPartner("Pamplona", "vb", "Vendor B",
          "Testing purposes", "44626509V", "False", null, "Vendors", null, "Inmensa", "11 2º A",
          "26564", "Pamplona", "NAVARRA", "Spain", "678894523", "948784523", "948784524", "true",
          "true", "true", "true", "300", "True", "False", "12342228912345678912", null, "Paul",
          "Paul", "Argal", "Testing", "par@testing.com", "Chief officer provider", "678705111",
          "948255222", "654336333", "True", "Cash", "90D", "Default Purchase Price List", null,
          "Vendor National Testing... 5543-5343-64-1212121212", "False", null, null, null, null,
          null, null, null, "False", "False");
      assert (businessPartner2 != null);
      assert (businessPartner2.get("name").equals("Vendor B"));
      System.out.println(businessPartner2);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject businessPartner3 = bpp.createBusinessPartner("Pamplona", "vc", "Vendor C",
          "Testing purposes", "44626590V", "False", null, "Vendors", null, "Pequeña", "11 2º A",
          "26564", "Pamplona", "NAVARRA", "Spain", "678894524", "948784525", "948784526", "true",
          "true", "true", "true", "450", "True", "False", "12342228988885678912", null, "John",
          "John", "Wall", "Testing", "jwa@testing.com", "Chief officer provider", "678705123",
          "948255123", "654336123", "True", "Bank Deposit", "90D", "Default Purchase Price List",
          null, "Vendor National Testing... 5543-5343-64-1212121212", "False", null, null, null,
          null, null, null, null, "False", "False");
      assert (businessPartner3 != null);
      assert (businessPartner3.get("name").equals("Vendor C"));
      System.out.println(businessPartner3);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject businessPartner4 = bpp.createBusinessPartner("Pamplona", "cusa", "Customer A",
          "Testing purposes", "44626567V", "False", null, "Customers", null, "Little", "11 2º A",
          "26564", "Pamplona", "NAVARRA", "Spain", "678894524", "948784525", "948784526", "true",
          "true", "true", "true", "560", "True", "False", "56321459875236547852", null, "Toño",
          "Toño", "Soles", "Testing", "tso@testing.com", "Chief officer sales", "678705123",
          "948255123", "654336123", "False", null, null, null, null, null, "True", "Bank Deposit",
          "30D/5", "Second Testing Price List Sales", null,
          "Customer National Testi... 1234-4321-24-1234567890", "Monthly the first",
          "Customer Schedule after Delivery", "False", "False");
      assert (businessPartner4 != null);
      assert (businessPartner4.get("name").equals("Customer A"));
      System.out.println(businessPartner4);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject businessPartner5 = bpp.createBusinessPartner("Pamplona", "cusb", "Customer B",
          "Testing purposes", "44626523V", "False", null, "Customers", null, "Small", "11 2º A",
          "26564", "Pamplona", "NAVARRA", "Spain", "678894555", "948784588", "948784533", "true",
          "true", "true", "true", "230", "True", "False", "56321459875231117521", null, "Elena",
          "Elena", "martin", "Testing", "ema@testing.com", "Chief officer sales", "678705100",
          "948255100", "654336100", "False", null, null, null, null, null, "True", "Cash", "30D/5",
          "Second Testing Price List Sales", null,
          "Customer National Testi... 1234-4321-24-1234567890", null, "After Delivery", "False",
          "False");
      assert (businessPartner5 != null);
      assert (businessPartner5.get("name").equals("Customer B"));
      System.out.println(businessPartner5);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject businessPartner6 = bpp.createBusinessPartner("Pamplona", "cusc", "Customer C",
          "Testing purposes", "44626555V", "False", null, "Customers", null, "Smaller", "11 2º A",
          "26564", "Pamplona", "NAVARRA", "Spain", "678894524", "948784525", "948784526", "true",
          "true", "true", "true", "584", "True", "False", "56321459875231100521", null, "Gorka",
          "Gorka", "Lales", "Testing", "gla@testing.com", "Chief officer sales", "678705123",
          "948255123", "654336123", "False", null, null, null, null, null, "True", "Wire Transfer",
          "30D/5", "Second Testing Price List Sales", null,
          "Customer National Testi... 1234-4321-24-1234567890", null, "After Delivery", "False",
          "False");
      assert (businessPartner6 != null);
      assert (businessPartner6.get("name").equals("Customer C"));
      System.out.println(businessPartner6);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject businessPartner7 = bpp.createBusinessPartner(null, "empla", "Employee A",
          "Testing purposes", "44626588V", "False", null, "Employee", null, "Very small",
          "11 2º A", "26564", "Pamplona", "NAVARRA", "Spain", null, null, null, "true", "true",
          "true", "true", null, null, null, null, null, "Paul", "Sirof", "Sirof", "Testing",
          "psi@testing.com", "Chief officer Operations", "678705125", "948255155", "654336155",
          "False", null, null, null, null, null, "False", null, null, null, null, null, null, null,
          "True", "False");
      assert (businessPartner7 != null);
      assert (businessPartner7.get("name").equals("Employee A"));
      System.out.println(businessPartner7);
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

    TestBusinessPartners test = new TestBusinessPartners();

    try {
      test.publicSetUp();

      test.testImportBusinessPartners();

      test.publicTearDown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
