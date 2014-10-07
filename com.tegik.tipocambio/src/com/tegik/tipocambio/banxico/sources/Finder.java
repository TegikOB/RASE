package com.tegik.tipocambio.banxico.sources;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;

import com.tegik.tipocambio.banxico.LeerXML;

public class Finder {
  private static final Logger log = Logger.getLogger(LeerXML.class);

  public static Currency findCurrencyByISOCode(String value) {

    OBContext.setAdminMode(true);
    OBCriteria<Currency> obCurr = OBDal.getInstance().createCriteria(Currency.class);
    obCurr.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, value));
    List<Currency> monedas = obCurr.list();
    if (monedas.isEmpty()) {
      return null;
    }

    OBContext.restorePreviousMode();

    return monedas.get(0);

  }

  public static Organization findOrganizationByName(String name) {

    OBContext.setAdminMode(true);
    OBCriteria<Organization> obOrg = OBDal.getInstance().createCriteria(Organization.class);
    obOrg.add(Restrictions.eq(Organization.PROPERTY_NAME, name));
    List<Organization> org = obOrg.list();
    if (org.isEmpty()) {
      return null;
    }

    OBContext.restorePreviousMode();

    return org.get(0);

  }

  public static boolean existeConversion(Currency moneda, Currency monedaObj, Date fecha) {

    OBContext.setAdminMode(true);
    OBCriteria<ConversionRate> obCon = OBDal.getInstance().createCriteria(ConversionRate.class);
    obCon.add(Restrictions.eq(ConversionRate.PROPERTY_VALIDFROMDATE, fecha));
    obCon.add(Restrictions.eq(ConversionRate.PROPERTY_CURRENCY, moneda));
    obCon.add(Restrictions.eq(ConversionRate.PROPERTY_TOCURRENCY, monedaObj));

    List<ConversionRate> conv = obCon.list();
    if (!(conv.isEmpty())) {
      log.info("Entro aqui 1");
      return true;
    }

    obCon = OBDal.getInstance().createCriteria(ConversionRate.class);
    obCon.add(Restrictions.gt(ConversionRate.PROPERTY_VALIDTODATE, fecha));
    obCon.add(Restrictions.eq(ConversionRate.PROPERTY_CURRENCY, moneda));
    obCon.add(Restrictions.eq(ConversionRate.PROPERTY_TOCURRENCY, monedaObj));

    conv = obCon.list();
    if (!(conv.isEmpty())) {
      log.info("Entro aqui 2");
      return true;
    }

    return false;

  }
}
