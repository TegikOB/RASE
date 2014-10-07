/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.mobile.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.BaseComponentProvider.ComponentResource.ComponentResourceType;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.mobile.core.model.ClientModelComponent;

@ApplicationScoped
@ComponentProvider.Qualifier(MobileCoreConstants.COMPONENT_TYPE)
public class MobileCoreComponentProvider extends BaseComponentProvider {

  static {
    // Set dependency on Mobile Core app
    BaseComponentProvider.setAppDependencies(MobileCoreConstants.RETAIL_CORE,
        Arrays.asList(MobileCoreConstants.APP_IDENTIFIER));
  }

  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    if (MobileCoreConstants.LIBRARIES_COMPONENT.equals(componentId)) {
      final LibraryResource c = getComponent(LibraryResource.class);
      c.setParameters(parameters);
      c.setId(componentId);
      return c;
    } else if (KernelConstants.RESOURCE_COMPONENT_ID.equals(componentId)) {
      final MobileStaticResourceComponent c = getComponent(MobileStaticResourceComponent.class);
      c.setParameters(parameters);
      c.setId(componentId);
      return c;
    } else if (MobileCoreConstants.CLIENT_MODEL_COMPONENT.equals(componentId)) {
      final ClientModelComponent component = getComponent(ClientModelComponent.class);
      component.setId(componentId);
      component.setParameters(parameters);
      return component;
    }
    // else if ("Proxy".equals(componentId)) {
    // return getProxiedComponent(parameters);
    // }
    throw new IllegalStateException("Component not supported");
  }

  @Override
  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    final String prefix = "web/" + MobileCoreConstants.MODULE_JAVAPACKAGE;

    final String[] jsDependency = { "data/ob-datasource", "data/ob-dal", "data/ob-model",
        "data/ob-windowmodel", "component/ob-terminal-component", "model/ob-terminal-model",
        "model/logclient", "component/ob-commonbuttons", "component/ob-login",
        "utils/logClientSyncUtils", "utils/ob-testregistry", "utils/ob-utilitiesui",
        "utils/ob-arithmetic", "utils/ob-utilities", "utils/ob-hooks", "component/ob-clock",
        "component/ob-windowview", "component/ob-layout", "component/ob-scrollabletable",
        "utils/ob-i18n", "../../org.openbravo.client.application/js/utilities/ob-utilities-number",
        "../../org.openbravo.client.application/js/utilities/ob-utilities-date",
        "component/ob-keyboard", "component/ob-context-menu", "component/ob-keypadbasic",
        "component/ob-table", "component/dialog/ob-profile", "component/dialog/ob-modalonline",
        "component/dialog/ob-logout", "component/ob-menu", "component/dialog/ob-properties",
        "offline/ob-session", "offline/ob-user" };

    final String[] jsRetailDependency = { "component/ob-retail-product-browser",
        "component/ob-retail-searchproducts", "component/ob-retail-searchproductcharacteristic" };

    final String[] cssDependency = { "css/ob-login", "css/ob-standard" };

    for (String resource : jsDependency) {
      globalResources.add(createComponentResource(ComponentResourceType.Static, prefix + "/source/"
          + resource + ".js", MobileCoreConstants.APP_IDENTIFIER));
    }

    for (String resource : jsRetailDependency) {
      globalResources.add(createComponentResource(ComponentResourceType.Static, prefix
          + "/source/retail/" + resource + ".js", MobileCoreConstants.RETAIL_CORE));
    }

    for (String resource : cssDependency) {
      globalResources.add(createComponentResource(ComponentResourceType.Stylesheet, prefix
          + "/assets/" + resource + ".css", MobileCoreConstants.APP_IDENTIFIER));
    }

    return globalResources;
  }
}
