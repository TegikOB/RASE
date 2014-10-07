/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.idl.IDL_Entities;

public class IdlIntService {

  private static class ThreadLocalService extends ThreadLocal<IdlIntService> {

    @Override
    public IdlIntService initialValue() {
      return new IdlIntService();
    }
  }

  private static ThreadLocalService thservice = new ThreadLocalService();

  public static IdlIntService getInstance() {
    return thservice.get();
  }

  private Map<String, String> idlservice = new HashMap<String, String>();
  private Map<String, IdlService> servicecache = new HashMap<String, IdlService>();

  private IdlIntService() {
//    idlservice.put("Bank Account", "org.openbravo.idl.proc.BankAccountsProcess");
//    idlservice.put("Business Partner", "org.openbravo.idl.proc.BusinessPartnersProcess");
//    idlservice.put("Product", "org.openbravo.idl.proc.ProductsProcess");
//    idlservice.put("Price List", "org.openbravo.idl.proc.PriceListsProcess");
//    idlservice.put("Open Payable", "org.openbravo.idl.proc.OpenPayablesProcess");
//    idlservice.put("Open Receivable", "org.openbravo.idl.proc.OpenReceivablesProcess");
//    idlservice.put("Asset", "org.openbravo.idl.proc.AssetsProcess");
//    idlservice.put("Stock", "org.openbravo.idl.proc.StockProcess");
//    idlservice.put("JournalEntry", "org.openbravo.idl.proc.JournalEntriesProcess");
//    idlservice.put("Costing", "org.openbravo.idl.proc.CostingsProcess");
  }

  public IdlService getService(String s) {

      if (idlservice.containsKey(s)) {
          return getServiceByClassName(idlservice.get(s));
      } else {
          OBCriteria<IDL_Entities> obc = OBDal.getInstance().createCriteria(IDL_Entities.class);
          obc.add(Restrictions.eq("name", s));

          final List<IDL_Entities> listt = obc.list();
          String classname = (listt != null && listt.size() > 0)
                  ? listt.get(0).getJavaClassName()
                  : null;
          idlservice.put(s, classname);
          return getServiceByClassName(classname);
      }
  }

  private IdlService getServiceByClassName(String clazz) {

    IdlService retservice = servicecache.get(clazz);

    if (retservice == null) {
      try {
        retservice = (IdlService) Class.forName(clazz).newInstance();
        servicecache.put(clazz, retservice);

        // } catch (IllegalAccessException ex) {
        // } catch (ClassNotFoundException ex) {
        // } catch (InstantiationException ex) {
        // } catch (ClassCastException ex) {
      } catch (Exception ex) {
        retservice = null;
      }
    }

    return retservice;
  }
}
