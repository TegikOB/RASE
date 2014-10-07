/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.login;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.MobileCoreConstants;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDependency;
import org.openbravo.model.ad.system.Client;

public class LabelsComponent {
  private static final Logger log = Logger.getLogger(LabelsComponent.class);

  public static JSONObject getLabels(String languageId, String moduleId) {
    try {
      String modules = getMobileAppDependantModuleIds(moduleId);
      JSONObject labels = new JSONObject();
      String hqlLabel = "select message.searchKey, message.messageText "//
          + "from ADMessage message " //
          + "where module.id in " + modules;
      Query qryLabel = OBDal.getInstance().getSession().createQuery(hqlLabel);
      for (Object qryLabelObject : qryLabel.list()) {
        final Object[] qryLabelObjectItem = (Object[]) qryLabelObject;
        labels.put(qryLabelObjectItem[0].toString(), qryLabelObjectItem[1].toString());
      }

      String computedLanguage = "192";
      if (OBContext.getOBContext().getUser().getId().equals("0")) {
        Client sysClient = OBDal.getInstance().get(Client.class, "0");
        computedLanguage = sysClient.getLanguage().getId();
      } else {
        computedLanguage = OBContext.getOBContext().getLanguage().getId();
      }
      String langId = languageId != null ? languageId : computedLanguage;
      String hqlTrlLabels = "select trl.message.searchKey, trl.messageText from ADMessageTrl trl where trl.message.module.id in "
          + modules + " and trl.language.id='" + langId + "'";
      Query qryTrlLabels = OBDal.getInstance().getSession().createQuery(hqlTrlLabels);
      for (Object qryTrlObj : qryTrlLabels.list()) {
        final Object[] qryTrlObject = (Object[]) qryTrlObj;
        labels.put(qryTrlObject[0].toString(), qryTrlObject[1].toString());
      }
      return labels;
    } catch (Exception e) {
      log.error("There was an exception while generating the Mobile labels", e);
      return new JSONObject();
    }
  }

  public static String getMobileAppDependantModuleIds(String moduleId) {
    StringBuffer ids = new StringBuffer();

    List<Module> dependantModules = new ArrayList<Module>();
    Module theModule = OBDal.getInstance().get(Module.class, moduleId);

    // Add the module itself and mobile core as dependency
    dependantModules.add(theModule);
    dependantModules.add(OBDal.getInstance().get(Module.class, MobileCoreConstants.MODULE_ID));

    OBCriteria<ModuleDependency> allDeps = OBDal.getInstance().createCriteria(
        ModuleDependency.class);
    dependantModules.addAll(getDependantModules(theModule, allDeps.list()));

    int n = 0;
    ids.append("(");
    for (Module mod : dependantModules) {
      if (n > 0) {
        ids.append(",");
      }
      ids.append("'" + mod.getId() + "'");
      n++;
    }
    ids.append(")");
    return ids.toString();
  }

  public static List<Module> getDependantModules(Module module, List<ModuleDependency> allDeps) {
    List<Module> moduleList = new ArrayList<Module>();
    for (ModuleDependency depModule : allDeps) {
      if (depModule.getDependentModule().equals(module)
          && !moduleList.contains(depModule.getModule())) {
        moduleList.add(depModule.getModule());
        moduleList.addAll(getDependantModules(depModule.getModule(), allDeps));
      }
    }
    return moduleList;
  }

}
