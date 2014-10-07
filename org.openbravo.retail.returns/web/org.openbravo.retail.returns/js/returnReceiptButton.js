/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, $ */

(function () {

  enyo.kind({
    name: 'OBRETUR.UI.MenuReturn',
    kind: 'OB.UI.MenuAction',
    permission: 'OBRETUR_Return',
    i18nLabel: 'OBRETUR_LblReturn',
    events: {
      onPaidReceipts: ''
    },
    tap: function () {
      if (this.disabled) {
        return true;
      }
      this.inherited(arguments); // Manual dropdown menu closure
      if (!OB.POS.modelterminal.get('connectedToERP')) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline'));
        return;
      }
      if (OB.POS.modelterminal.hasPermission(this.permission)) {
        this.doPaidReceipts({
          isQuotation: false,
          isReturn: true
        });
      }
    }
  });

  // Register the menu...
  OB.OBPOSPointOfSale.UI.LeftToolbarImpl.prototype.menuEntries.push({
    kind: 'OBRETUR.UI.MenuReturn'
  });
}());