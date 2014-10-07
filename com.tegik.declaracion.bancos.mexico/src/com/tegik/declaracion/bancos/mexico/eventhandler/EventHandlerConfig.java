package com.tegik.declaracion.bancos.mexico.eventhandler;

import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.service.db.DalConnectionProvider;

import com.tegik.declaracion.bancos.mexico.BanMexConfiguracion;

public class EventHandlerConfig extends EntityPersistenceEventObserver {
  private static Entity[] entities = { ModelProvider.getInstance().getEntity(
      BanMexConfiguracion.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());
  private String language = OBContext.getOBContext().getLanguage().getLanguage();
  private ConnectionProvider conn = new DalConnectionProvider(false);

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final BanMexConfiguracion configColumnas = (BanMexConfiguracion) event.getTargetInstance();

  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final BanMexConfiguracion config = (BanMexConfiguracion) event.getTargetInstance();

  }

  public void onDelete(@Observes EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
  }

}