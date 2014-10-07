/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.discounts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.BaseComponentProvider.ComponentResource.ComponentResourceType;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;

/**
 * @author alostale
 * 
 */
@ApplicationScoped
@ComponentProvider.Qualifier(DiscountsComponentProvider.QUALIFIER)
public class DiscountsComponentProvider extends BaseComponentProvider {

  public static final String QUALIFIER = "OBDISC";

  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  @Override
  public List<ComponentResource> getGlobalComponentResources() {

    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    final String prefix = "web/org.openbravo.retail.discounts/js/";

    final String[] resourceDependency = { "promotion-discount", "promotion-buyXgiftY",
        "promotion-XYdifferentProduts", "promotion-XYsameProduct", "promotion-pack",
        "promotion-manual"
    };

    for (String resource : resourceDependency) {
      globalResources.add(createComponentResource(ComponentResourceType.Static, prefix + resource
          + ".js", "WebPOS"));
    }

    return globalResources;
  }
}
