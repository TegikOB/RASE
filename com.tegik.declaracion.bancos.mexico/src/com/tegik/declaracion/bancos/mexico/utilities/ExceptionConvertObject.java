package com.tegik.declaracion.bancos.mexico.utilities;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Message;

public class ExceptionConvertObject extends Exception {

  public String classname = "";
  public String dato = "";
  public long linea = 0;;

  public ExceptionConvertObject(String searchkey, String dato, String classname) {
    super(searchkey);
    this.classname = classname;
    this.dato = dato;

  }

  public long getLinea() {
    return this.linea;
  }

  public void setLinea(long linea) {
    this.linea = linea;
  }

  public String getMessageOpenbravo() {
    OBContext.setAdminMode(true);
    OBCriteria<Message> obMssg = OBDal.getInstance().createCriteria(Message.class);
    obMssg.add(Restrictions.eq(Message.PROPERTY_SEARCHKEY, super.getMessage()));
    List<Message> messages = obMssg.list();
    String msg = messages.get(0).getMessageText() + " " + getTipoDato() + " " + "linea: "
        + this.linea + " dato: " + dato;
    OBContext.restorePreviousMode();

    return msg;

  }

  public String getTipoDato() {
    OBContext.setAdminMode(true);

    OBCriteria<org.openbravo.model.ad.domain.List> obMssg = OBDal.getInstance().createCriteria(
        org.openbravo.model.ad.domain.List.class);
    obMssg.add(Restrictions.eq(org.openbravo.model.ad.domain.List.PROPERTY_SEARCHKEY,
        this.classname));
    List<org.openbravo.model.ad.domain.List> messages = obMssg.list();
    String msg = messages.get(0).getName();

    OBContext.restorePreviousMode();

    return msg;
  }

}
