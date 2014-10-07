/*
 ************************************************************************************
 * Copyright (C) 2009-2010 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.idl.proc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.model.financialmgmt.calendar.PeriodControl;

public class DALUtils {

  // private static Logger log4j = Logger.getLogger(DALUtils.class);

  public static Organization findOrganization(String orgName) {
    if (orgName != null) {
      OBCriteria<Organization> obc = OBDal.getInstance().createCriteria(Organization.class);
      obc.add(Restrictions.eq("searchKey", orgName));
      // Asterisk org
      if (!orgName.equals("0")) {
        obc.add(Restrictions.eq("client", OBContext.getOBContext().getCurrentClient()));
      }
      final List<Organization> listt = obc.list();
      if (listt != null && listt.size() > 0) {
        return listt.get(0);
      } else {
        return null;
      }

    } else {
      return null;
    }

  }

  public static boolean getBoolean(String value) {
    return "TRUE".equalsIgnoreCase(value) || "YES".equalsIgnoreCase(value)
        || "Y".equalsIgnoreCase(value);

  }

  private static Organization getCalendarOwner(Organization org) throws SQLException {
    // call the Stored Function through Dal connection
    ResultSet rs = null;
    // first get a connection
    final Connection connection = OBDal.getInstance().getConnection(false);
    final PreparedStatement ps = connection
        .prepareStatement("SELECT AD_ORG_GETCALENDAROWNER(?) FROM DUAL");
    try {
      // Set Parameters
      ps.setString(1, org.getId());

      rs = ps.executeQuery();
      String ownerOrgId = null;
      if (rs.next()) {
        ownerOrgId = rs.getString(1);
      }

      Organization ownerOrg = OBDal.getInstance().get(Organization.class, ownerOrgId);

      return ownerOrg;

    } catch (Exception e) {
      throw new IllegalStateException(e);
    } finally {
      ps.close();
      rs.close();
    }
  }

  public static Period getPeriodByDate(Date date, Organization org) {

    Organization ownerOrg = null;
    try {
      ownerOrg = getCalendarOwner(org);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    Period period = null;

    if (ownerOrg != null) {

      OBCriteria<Period> obc = OBDal.getInstance().createCriteria(Period.class);
      obc.add(Restrictions.le(Period.PROPERTY_STARTINGDATE, date));
      obc.add(Restrictions.ge(Period.PROPERTY_ENDINGDATE, date));
      obc.add(Restrictions.in("organization.id", OBContext.getOBContext()
          .getOrganizationStructureProvider().getNaturalTree(org.getId())));
      obc.setFilterOnReadableOrganization(false);
      obc.setMaxResults(1);
      final List<Period> periodList = obc.list();

      if (obc.count() > 0) {

        // Check if the period belongs to the Organization calendar
        if (periodList.get(0).getYear().getCalendar().getId()
            .equals(ownerOrg.getCalendar().getId())) {

          // Check if the period is opened
          OBCriteria<PeriodControl> obc2 = OBDal.getInstance().createCriteria(PeriodControl.class);
          obc2.add(Restrictions.eq(PeriodControl.PROPERTY_PERIOD, periodList.get(0)));
          obc2.add(Restrictions.eq(PeriodControl.PROPERTY_PERIODSTATUS, "O"));
          obc2.add(Restrictions.in("organization.id", OBContext.getOBContext()
              .getOrganizationStructureProvider().getNaturalTree(org.getId())));
          obc2.setFilterOnReadableOrganization(false);
          obc2.setMaxResults(1);

          if (obc2.count() > 0) {
            period = periodList.get(0);
          }

        }
      }

    }

    return period;
  }

  public static Country getCountryByIban(String iban) {
    String isoCode = iban.substring(0, 2);
    OBCriteria<Country> obc = OBDal.getInstance().createCriteria(Country.class);
    obc.add(Restrictions.eq(Country.PROPERTY_ISOCOUNTRYCODE, isoCode));
    if (obc.list() != null && obc.list().size() > 0) {
      return obc.list().get(0);
    } else {
      return null;
    }
  }

  public static DocumentType getDocumentTypeRecursive(Organization org, String docCategory) {
    Client client;
    OBCriteria<DocumentType> obcDoc = OBDal.getInstance().createCriteria(DocumentType.class);
    obcDoc.setFilterOnReadableOrganization(false);
    obcDoc.setFilterOnReadableClients(false);
    obcDoc.add(Restrictions.eq(DocumentType.PROPERTY_ORGANIZATION, org));
    if ("0".equals(org.getId())) {
      client = OBContext.getOBContext().getCurrentClient();
      if ("0".equals(client.getId())) {
        return null;
      }
    } else {
      client = org.getClient();
    }
    obcDoc.add(Restrictions.eq(DocumentType.PROPERTY_CLIENT, client));
    obcDoc.add(Restrictions.eq(DocumentType.PROPERTY_DOCUMENTCATEGORY, docCategory));
    List<DocumentType> docTypeList = obcDoc.list();
    if (docTypeList != null && docTypeList.size() > 0) {
      return docTypeList.get(0);
    } else {
      if ("0".equals(org.getId())) {
        return null;
      } else {
        return getDocumentTypeRecursive(new OrganizationStructureProvider().getParentOrg(org),
            docCategory);
      }
    }
  }

}
