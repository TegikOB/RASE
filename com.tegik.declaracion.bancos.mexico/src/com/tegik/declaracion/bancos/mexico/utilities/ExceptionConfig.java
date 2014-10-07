package com.tegik.declaracion.bancos.mexico.utilities;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Message;

public class ExceptionConfig extends Exception {
  private static final long serialVersionUID = 1L;
  public String extra = "";

  public ExceptionConfig(String message) {
    super(message);
    this.extra = "";
  }

  public ExceptionConfig(String message, String extra) {
    super(message);
    this.extra = extra;
  }

  public String getMessageOpenbravo() {
    OBContext.setAdminMode(true);
    OBCriteria<Message> obMssg = OBDal.getInstance().createCriteria(Message.class);
    obMssg.add(Restrictions.eq(Message.PROPERTY_SEARCHKEY, super.getMessage()));
    List<Message> messages = obMssg.list();
    String msg = messages.get(0).getMessageText();
    if (!extra.equals("")) {
      msg = msg + " " + getCorresponde();
    }
    OBContext.restorePreviousMode();

    return msg;

  }

  public String getCorresponde() {
    OBContext.setAdminMode(true);

    OBCriteria<org.openbravo.model.ad.domain.List> obMssg = OBDal.getInstance().createCriteria(
        org.openbravo.model.ad.domain.List.class);
    obMssg.add(Restrictions.eq(org.openbravo.model.ad.domain.List.PROPERTY_SEARCHKEY, this.extra));
    List<org.openbravo.model.ad.domain.List> messages = obMssg.list();
    String msg = messages.get(0).getName();

    OBContext.restorePreviousMode();

    return msg;
  }

}
