/* The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.gps.copyrole;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.Role;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.utils.Replace;

public class CopyRoleProcess implements org.openbravo.scheduling.Process {

  private static final Logger log = Logger.getLogger(CopyRoleProcess.class);

  /**
   * Executes the Copy Role process. The expected parameter in the bundle is newRoleName.
   */
  public void execute(ProcessBundle bundle) throws Exception {

    try {
      final String newrolename = (String) bundle.getParams().get("newrolename");
      final String strRoleId = (String) bundle.getParams().get("AD_Role_ID");
      log.debug("Copying Role with new name: " + newrolename);

      // Get sourcerole Role Object
      final Role sourceRole = OBDal.getInstance().get(Role.class, strRoleId);

      if (sourceRole.isManual() == false) {
        throw new IllegalArgumentException("@OBGPSCR_RoleNotManual@");
      }
      // Create targetrole Role Object copying sourcerole and child objects, setting ID's to null
      final Role targetRole = (Role) DalUtil.copy(sourceRole, true, true);

      targetRole.setName(newrolename);

      // Remove Assigned Users and Assigned Orgs
      targetRole.setADRoleOrganizationList(null);
      targetRole.setADUserRolesList(null);

      // Save all new created object
      OBDal.getInstance().save(targetRole);

      final OBError msg = new OBError();
      msg.setType("Success");

      // Compose messagestring to get the proper link in the message
      String messagestring = Utility.messageBD(bundle.getConnection(), "obgpscr_CopyRoleSuccess",
          bundle.getContext().getLanguage());
      messagestring = Replace.replace(messagestring, "&quot;", "\"");
      messagestring = messagestring.replace("@role_id@", targetRole.getId());
      messagestring = messagestring.replace("@role_name@", newrolename);

      msg.setMessage(messagestring);

      bundle.setResult(msg);
    } catch (final Exception e) {
      log.error(e);
      e.printStackTrace(System.err);
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("Oops! An Error has occurred");
      bundle.setResult(msg);
    }
  }

}
