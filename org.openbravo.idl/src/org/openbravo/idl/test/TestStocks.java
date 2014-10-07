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
import org.openbravo.idl.proc.StockProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.test.base.BaseTest;

/**
 * 
 * @author adrian
 */
public class TestStocks extends BaseTest {

  // private void setContext(String user, String role, String client, String org) {
  //
  // boolean isadmin = OBContext.getOBContext().isInAdministratorMode();
  // OBContext.getOBContext().setInAdministratorMode(true);
  //
  // OBContext.setOBContext(
  // findDALInstance(User.class, new Value(User.PROPERTY_NAME, user)).getId());
  //
  // OBContext.setOBContext(
  // findDALInstance(User.class, new Value(User.PROPERTY_NAME, user)).getId(),
  // findDALInstance(Role.class, new Value(Role.PROPERTY_NAME, role)).getId(),
  // findDALInstance(Client.class, new Value(Client.PROPERTY_NAME, client)).getId(),
  // findDALInstance(Organization.class, new Value(Organization.PROPERTY_NAME, org)).getId());
  //
  // OBContext.getOBContext().setInAdministratorMode(isadmin);
  // }
  //
  // private <T extends BaseOBObject> T findDALInstance(Class<T> clazz,
  // Value... values) {
  //
  // // create an OBCriteria object and add a filter
  // final OBCriteria<T> obCriteria = OBDal.getInstance().createCriteria(clazz);
  //
  // for (Value value : values) {
  // obCriteria.add(Expression.eq(value.getField(), value.getValue()));
  // }
  //
  // // perform the actual query returning a typed list
  // final List<T> listt = obCriteria.list();
  // if (listt != null && listt.size() > 0) {
  // return listt.get(0);
  // } else {
  // return null;
  // }
  // }

  public void testStocksImports() {

    // setContext("IDLAdmin", "IDL Admin", "IDL", "*");
    OBContext.setOBContext("BC64492184D54BC0AA716A1545DE6BA0", "26BB96AF82E44372B2C082432242B461",
        "BAA57A5061444E5CB69A831D98DF9D6B", "EF6321E567F449A497338032F8FD2D08");

    StockProcess stp = new StockProcess();
    stp.init(new DalConnectionProvider(), null);

    try {

      stp.checkOrganization("Stock", "*");
      stp.checkTransactionalOrganization("Stock", "Pamplona");
      // BaseOBObject stock = stp.createStock(
      // "*",
      // "Pamplona",
      // "2009-11-09",
      // "Initial import",
      // "2009-11-09",
      // "New Warehouse", "New Warehouse", "", "paloalto", "calle nada", "77777", "Pamplona",
      // "NAVARRA", "Spain",
      // "*",
      // "New Bin", "true", "60", "0", "0", "0",
      // "rmc", "12345", "100");
      BaseOBObject stock = stp.createStock("*", "Pamplona", "2009-11-08", "Initial import",
          "2009-11-08", "New Warehouse", "New Warehouse", "", "paloalto", "calle nada", "77777",
          "Pamplona", "NAVARRA", "Spain", "*", "New Bin", "true", "60", "0", "0", "0", "test",
          "Green", "150", "5");
      assert (stock != null);
      assert (stock.get("name").equals("2009-11-08"));

      System.out.println(stock);

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

    TestStocks test = new TestStocks();

    try {
      test.publicSetUp();

      test.testStocksImports();

      test.publicTearDown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
