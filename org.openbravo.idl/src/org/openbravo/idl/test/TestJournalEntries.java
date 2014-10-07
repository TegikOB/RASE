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
import org.openbravo.idl.proc.JournalEntriesProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.test.base.BaseTest;

public class TestJournalEntries extends BaseTest {
  public void testJournalEntriesImport() {

    OBContext.setOBContext("8DC521270BCA499DA2E88A826D41DF18", // IDL Client Admin user
        "5232A256B80D4ACEB89EF9D3A1D1A070", // IDL Client Admin role
        "B6471BE1FE9B4CC88893B7442C687319", // IDL Client
        "42A8DA5812264B96A2B6C1FE08F6B45B" // IDL Org
    );

    JournalEntriesProcess jep = new JournalEntriesProcess();
    jep.init(new DalConnectionProvider(), null);

    jep.checkOrganization("JournalEntry", "0");
    jep.checkTransactionalOrganization("JournalEntry", "Pamplona");

    try {
      BaseOBObject journalEntry1 = jep.createJournalEntries(null, "Pamplona", "Journal1", "Line1",
          "EUR", "IDL US/A/Euro", "Manual", "2009-11-10", "false", "1115", "102", "rma", "cusa",
          null, null, null, "15.23", "0", "Exento Internacional", null, null);
      assert (journalEntry1 != null);
      System.out.println(journalEntry1);
    } catch (Exception e) {
      fail(e.toString());
    }

    try {
      BaseOBObject journalEntry2 = jep.createJournalEntries(null, "Pamplona", "Journal1", "Line2",
          "EUR", "IDL US/A/Euro", "Manual", "2009-11-06", "false", "1116", "10340", "rmb", "cusb",
          null, null, null, "0", "15.23", "Entregas intracomunitarias 7% (+7%)", null, null);
      assert (journalEntry2 != null);
      System.out.println(journalEntry2);
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

    TestJournalEntries test = new TestJournalEntries();

    try {
      test.publicSetUp();

      test.testJournalEntriesImport();

      test.publicTearDown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
