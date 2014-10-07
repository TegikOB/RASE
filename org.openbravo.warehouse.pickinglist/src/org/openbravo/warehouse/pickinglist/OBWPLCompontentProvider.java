/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.warehouse.pickinglist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;

@ApplicationScoped
@ComponentProvider.Qualifier(OBWPLCompontentProvider.PICKINGLIST_COMPONENT_TYPE)
public class OBWPLCompontentProvider extends BaseComponentProvider {
  public static final String PICKINGLIST_COMPONENT_TYPE = "OBWPL_ComponentType";

  @Override
  public Component getComponent(String componentId, Map<String, Object> parameters) {
    return null;
  }

  @Override
  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> resources = new ArrayList<ComponentResource>();
    resources.add(createStaticResource(
        "web/org.openbravo.warehouse.pickinglist/js/OBWPL_Process.js", false));
    resources.add(createStaticResource(
        "web/org.openbravo.warehouse.pickinglist/js/OBWPL_ValidateComponent.js", false));
    resources.add(createStaticResource(
        "web/org.openbravo.warehouse.pickinglist/js/OBWPL_PickEditValidations.js", false));

    resources.add(createStaticResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/"
            + "org.openbravo.warehouse.pickinglist/ob-pick-validate-process.js", false));

    resources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/"
            + "org.openbravo.warehouse.pickinglist/ob-pick-validate-process.css", false));
    return resources;
  }

}
