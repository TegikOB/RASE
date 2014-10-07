/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package com.openbravo.gps.GoogleMapLocalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelConstants;

/**
 * ComponentProvider for declaring static resources
 * 
 * 
 */
@ApplicationScoped
@ComponentProvider.Qualifier(GoogleCollectionCompomentProvider.COMPONENT_TYPE)
public class GoogleCollectionCompomentProvider extends BaseComponentProvider {
  public static final String COMPONENT_TYPE = "OBGMPSL_Map";

  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  @Override
  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    // globalResources.add(createStaticResource("web/com.openbravo.gps.GoogleMapLocalization/js/MapLocalization.js",
    // false));
    globalResources.add(createStaticResource(
        "web/com.openbravo.gps.GoogleMapLocalization/js/GoogleMapLocalization-toolbar-button.js",
        false));
    globalResources.add(createStaticResource(
        "web/com.openbravo.gps.GoogleMapLocalization/js/GoogleMapLocalization-generate-popup.js",
        false));
    globalResources
        .add(createStaticResource(
            "web/com.openbravo.gps.GoogleMapLocalization/js/GoogleMapShowLocalizations-toolbar-button.js",
            false));
    globalResources
        .add(createStaticResource(
            "web/com.openbravo.gps.GoogleMapLocalization/js/GoogleMapShowLocalizations-generate-popup.js",
            false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_PARAMETER
            + "/com.openbravo.gps.GoogleMapLocalization/GoogleMapLocalization-styles.css", false));
    return globalResources;
  }

  @Override
  public List<String> getTestResources() {
    return Collections.emptyList();
  }
}
