package com.tegik.declaracion.bancos.mexico.eventhandler;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.DalConnectionProvider;

import com.tegik.declaracion.bancos.mexico.BanMexColumnas;

public class EventHandlerColumnas extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      BanMexColumnas.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final BanMexColumnas configColumnas = (BanMexColumnas) event.getTargetInstance();
    if (checkIgnoreDatos(configColumnas)) {
      String language = OBContext.getOBContext().getLanguage().getLanguage();
      ConnectionProvider conn = new DalConnectionProvider(false);
      throw new OBException(Utility.messageBD(conn, "@BANMEX_OnlyOne@", language));
    }
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final BanMexColumnas configColumnas = (BanMexColumnas) event.getTargetInstance();
    if (checkIgnoreDatos(configColumnas)) {
      String language = OBContext.getOBContext().getLanguage().getLanguage();
      ConnectionProvider conn = new DalConnectionProvider(false);
      throw new OBException(Utility.messageBD(conn, "@BANMEX_OnlyOne@", language));
    }

  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
  }

  public boolean checkIgnoreDatos(BanMexColumnas configCol) {

    if (configCol.getCantidad() != null) {
      if (configCol.getCantidad() > 0) {
        if (configCol.getPrefijo() != null)
          return true;
      }
    }

    return false;
  }
}
