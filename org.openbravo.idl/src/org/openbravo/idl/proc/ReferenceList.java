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
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

/**
 * 
 * @author adrian
 */
public class ReferenceList {

  private Map<String, String> valuestrl;
  private Map<String, String> values;

  private ReferenceList(Map<String, String> values, Map<String, String> valuestrl) {
    this.values = values;
    this.valuestrl = valuestrl;
  }

  public static ReferenceList getReferenceListByName(String name, String language) {

    OBContext.setAdminMode(true);

    final OBCriteria<org.openbravo.model.ad.domain.Reference> obCriteria = OBDal.getInstance()
        .createCriteria(org.openbravo.model.ad.domain.Reference.class);
    obCriteria.add(Restrictions.eq("name", name));

    java.util.List<org.openbravo.model.ad.domain.Reference> data = obCriteria.list();

    OBContext.restorePreviousMode();

    if (data.size() == 1) {
      return getReferenceList(data.get(0).getId(), language);
    } else {
      return null;
    }
  }

  public static ReferenceList getReferenceList(org.openbravo.model.ad.domain.Reference r,
      String language) {

    return getReferenceList(r.getId(), language);
  }

  public static ReferenceList getReferenceList(String id, String language) {

    final Session session = OBDal.getInstance().getSession();
    SQLQuery query = session
        .createSQLQuery("SELECT AD_REF_LIST.VALUE AS VALUE, AD_REF_LIST.NAME AS LISTNAME, TRL.NAME AS TRLNAME "
            + "FROM AD_REF_LIST LEFT OUTER JOIN "
            + "(SELECT AD_REF_LIST_ID, NAME FROM AD_REF_LIST_TRL WHERE AD_REF_LIST_TRL.AD_LANGUAGE = ?) TRL "
            + "ON AD_REF_LIST.AD_REF_LIST_ID = TRL.AD_REF_LIST_ID "
            + "WHERE AD_REF_LIST.AD_REFERENCE_ID = ?");
    query.setString(0, language);
    query.setString(1, id);

    java.util.List<Object[]> l = query.list();
    Map<String, String> m = new HashMap<String, String>();
    Map<String, String> mtrl = new HashMap<String, String>();
    for (Object[] row : l) {
      if (row[1] != null) {
        m.put((String) row[1], (String) row[0]);
      }
      if (row[2] != null) {
        mtrl.put((String) row[2], (String) row[0]);
      }
    }
    return new ReferenceList(m, mtrl);
  }

  public String getReferenceValue(String name) {
    String valtrl = valuestrl.get(name);
    if (valtrl == null) {
      return values.get(name);
    } else {
      return valtrl;
    }
  }

  @Override
  public String toString() {
    return toString(valuestrl) + " -> " + toString(values);
  }

  private String toString(Map<String, String> map) {
    StringBuffer s = new StringBuffer();
    s.append("[");
    for (Entry<String, String> e : map.entrySet()) {
      s.append("(");
      s.append(e.getKey());
      s.append(", ");
      s.append(e.getValue());
      s.append("), ");
    }
    s.append("]");
    return s.toString();
  }
}
