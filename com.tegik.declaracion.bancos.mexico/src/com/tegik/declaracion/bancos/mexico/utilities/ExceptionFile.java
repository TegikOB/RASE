package com.tegik.declaracion.bancos.mexico.utilities;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Message;

public class ExceptionFile extends Exception {

  public ExceptionFile(String message) {
    super(message);
  }

  public String getMessageOpenbravo() {
    OBContext.setAdminMode(true);
    OBCriteria<Message> obMssg = OBDal.getInstance().createCriteria(Message.class);
    obMssg.add(Restrictions.eq(Message.PROPERTY_SEARCHKEY, super.getMessage()));
    List<Message> messages = obMssg.list();
    String msg = messages.get(0).getMessageText();
    OBContext.restorePreviousMode();
    return msg;

  }

}
