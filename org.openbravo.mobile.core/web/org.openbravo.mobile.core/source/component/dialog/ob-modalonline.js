/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OB.UI.ModalOnline',
  bodyContent: {
    content: ''
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ModalDialogButton',
      content: '',
      isDefaultAction: true,
      tap: function () {
        this.doHideThisPopup();
        OB.MobileApp.model.logout();
      }
    }, {
      kind: 'OB.UI.ModalDialogButton',
      content: '',
      tap: function () {
        this.doHideThisPopup();
      }
    }]
  },
  initComponents: function () {
    this.header = OB.I18N.getLabel('OBMOBC_Online');
    this.bodyContent.content = OB.I18N.getLabel('OBMOBC_OnlineConnectionHasReturned');
    this.bodyButtons.components[0].content = OB.I18N.getLabel('OBMOBC_LblLoginAgain');
    this.bodyButtons.components[1].content = OB.I18N.getLabel('OBMOBC_LblCancel');
    this.inherited(arguments);
  }
});