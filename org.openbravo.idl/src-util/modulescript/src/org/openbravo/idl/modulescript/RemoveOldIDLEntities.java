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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.idl.modulescript;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class RemoveOldIDLEntities extends ModuleScript {
  
  @Override
  // Remove UIDL entities not used in version for Openbravo 3
  // Open Payables, Open Receivables and Bank Accounts
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      RemoveOldIDLEntitiesData[] data = RemoveOldIDLEntitiesData.select(cp);
      
      for (RemoveOldIDLEntitiesData client : data) {
        RemoveOldIDLEntitiesData[] total = RemoveOldIDLEntitiesData.totalImported(cp, client.adClientId);
        // Prevent error of more than 1000 elements in sql IN clause
        // It is possible if the idl dataset was imported for 334 organizations in the same client
        if (total != null && total.length > 0 && total.length < 999) {
          // On delete cascade idl_fields will be removed
          RemoveOldIDLEntitiesData.removeEntities(cp, client.adClientId);
          // Delete ref data loaded entries
          RemoveOldIDLEntitiesData.deleteRefDataLoaded(cp, client.adClientId);
        }
      }
    } catch (Exception e) {
      handleError(e);
    }
  }
}
