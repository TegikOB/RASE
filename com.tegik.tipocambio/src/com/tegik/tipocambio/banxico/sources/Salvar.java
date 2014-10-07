package com.tegik.tipocambio.banxico.sources;

import java.math.BigDecimal;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;

public class Salvar {

  public static void salvarConversion(Currency moneda, Currency monedaMeta, Moneda equi) {

    ConversionRate conversion = new ConversionRate();

    conversion.setClient(OBContext.getOBContext().getCurrentClient());
    conversion.setDivideRateBy(equi.getCantidad());
    BigDecimal multipleRateBy = BigDecimal.ONE.divide(equi.getCantidad(), 4,
        BigDecimal.ROUND_HALF_EVEN);
    conversion.setMultipleRateBy(multipleRateBy);
    conversion.setValidFromDate(equi.getFecha());
    conversion.setValidToDate(equi.getFecha());
    Organization org = Finder.findOrganizationByName("*");
    conversion.setOrganization(org);
    conversion.setCurrency(moneda);
    conversion.setToCurrency(monedaMeta);

    OBDal.getInstance().save(conversion);
    OBDal.getInstance().flush();

  }

  public static void salvarConversionInversa(Currency moneda, Currency monedaMeta, Moneda equi) {

    ConversionRate conversion = new ConversionRate();

    conversion.setClient(OBContext.getOBContext().getCurrentClient());
    conversion.setMultipleRateBy(equi.getCantidad());
    BigDecimal divideRateBy = BigDecimal.ONE.divide(equi.getCantidad(), 4,
        BigDecimal.ROUND_HALF_EVEN);
    conversion.setDivideRateBy(divideRateBy);
    conversion.setValidFromDate(equi.getFecha());
    conversion.setValidToDate(equi.getFecha());
    Organization org = Finder.findOrganizationByName("*");
    conversion.setOrganization(org);
    conversion.setCurrency(moneda);
    conversion.setToCurrency(monedaMeta);

    OBDal.getInstance().save(conversion);
    OBDal.getInstance().flush();

  }

}
