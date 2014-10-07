package com.tegik.declaracion.bancos.mexico.utilities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;

import com.tegik.declaracion.bancos.mexico.sources.DeclaracionBancos;

public class Convertidor {
  private static final Logger log = Logger.getLogger(Convertidor.class);

  public static Date formatoDate(String dato, String formato) {
    if (formato.equals("DD/MM/AAAA")) {
      String datosFecha[] = dato.split("/");
      int days = Integer.parseInt(datosFecha[0]);
      int month = Integer.parseInt(datosFecha[1]);
      int year = Integer.parseInt(datosFecha[2]);
      Date date = new Date();
      date.setDate(days);
      date.setMonth(month - 1);
      date.setYear(year - 1900);

      return date;

    }

    return null;

  }

  public static void guardarDeclaracion(DeclaracionBancos decl, Object obj, String corresponde)
      throws ExceptionConfig {

    if (obj.getClass().getName().equals("java.lang.String")) {
      if (corresponde.equals("description")) {
        decl.setDescription((java.lang.String) (obj));
      } else if (corresponde.equals("referenceNo")) {
        decl.setReferenceNo((java.lang.String) (obj));

      } else {
        throw new ExceptionConfig("BANMEX_ConfigColumnas", corresponde);
      }

    } else if (obj.getClass().getName().equals("java.lang.Integer")) {
      throw new ExceptionConfig("BANMEX_ConfigColumnas", corresponde);

    } else if (obj.getClass().getName().equals("java.lang.Long")) {
      throw new ExceptionConfig("BANMEX_ConfigColumnas", corresponde);

    } else if (obj.getClass().getName().equals("java.math.BigDecimal")) {
      if (corresponde.equals("cramount")) {
        decl.setCramount((java.math.BigDecimal) (obj));
      } else if (corresponde.equals("dramount")) {
        decl.setDramount((java.math.BigDecimal) (obj));
      } else {
        throw new ExceptionConfig("BANMEX_ConfigColumnas", corresponde);

      }

    } else if (obj.getClass().getName().equals("java.util.Date")) {
      if (corresponde.equals("transactionDate")) {
        decl.setTransactionDate((java.util.Date) (obj));
      } else {
        throw new ExceptionConfig("BANMEX_ConfigColumnas", corresponde);
      }

    } else if (obj.getClass().getName()
        .matches("org\\.openbravo\\.model\\.common\\.businesspartner\\.BusinessPartner.*")) {
      if (corresponde.equals("businessPartner")) {
        decl.setBp((org.openbravo.model.common.businesspartner.BusinessPartner) obj);

      } else {
        throw new ExceptionConfig("BANMEX_ConfigColumnas", corresponde);

      }
    }

  }

  public static Object obtenerObjecto(String dato, String classname, String formato)
      throws ExceptionConvertObject {
    if (dato == null || dato.equals("")) {
      return null;
    }
    dato = dato.replaceAll("\"", "");

    try {

      if (classname.equals("java.lang.String")) {
        return dato;
      } else if (classname.equals("java.lang.Integer")) {
        return Integer.parseInt(dato);
      } else if (classname.equals("java.lang.Long")) {
        return new Long(Integer.parseInt(dato));
      } else if (classname.equals("java.math.BigDecimal")) {
        return new BigDecimal(dato);
      } else if (classname.equals("java.util.Date")) {
        return formatoDate(dato, formato);
      } else if (classname.equals("org.openbravo.model.common.businesspartner.BusinessPartner")) {
        return findBPartner(dato);
      }

    } catch (Exception e) {
      throw new ExceptionConvertObject("BANMEX_ConverDato", dato, classname);
    }
    return null;
  }

  public static BusinessPartner findBPartner(String value) {

    value = value.trim();
    OBContext.setAdminMode(true);
    OBCriteria<BusinessPartner> obBP = OBDal.getInstance().createCriteria(BusinessPartner.class);

    // perform the actual query returning a typed list
    List<BusinessPartner> bpList = obBP.list();

    if (bpList.isEmpty())
      return null;
    if (value != null && !(value.equals(""))) {

      for (BusinessPartner bp : bpList) {
        if (bp.getTaxID() != null) {
          if (bp.getTaxID().equals(value)) {
            return bp;
          }
        } else if (bp.getName() != null) {
          if (bp.getName().contains(value)) {
            return bp;
          }
        } else if (bp.getIdentifier() != null) {
          if (bp.getIdentifier().contains(value)) {
            return bp;
          }
        }
      }

    }

    return null;

  }
}
