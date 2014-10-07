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
  name: 'OB.UI.ModalLogout',
  kind: 'OB.UI.ModalAction',

  bodyContent: {
    kind: 'enyo.Control',
    name: 'label'
  },

  bodyButtons: {
    components: [{
      kind: 'OB.UI.LogoutDialogLogout'
    },
    //,{ kind: 'OB.UI.LogoutDialogLock' //Disabled until feature be ready}
    {
      kind: 'OB.UI.LogoutDialogCancel'
    }]
  },

  initComponents: function () {
    this.inherited(arguments);

    this.setHeader(OB.I18N.getLabel('OBMOBC_LogoutDialogLogout'));
    this.$.bodyContent.$.label.setContent(OB.I18N.getLabel('OBMOBC_LogoutDialogText'));
  }

});

enyo.kind({
  name: 'OB.UI.LogoutDialogCancel',
  kind: 'OB.UI.ModalDialogButton',
  tap: function () {
    this.doHideThisPopup();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
  }
});

enyo.kind({
  name: 'OB.UI.LogoutDialogLogout',
  kind: 'OB.UI.ModalDialogButton',
  isDefaultAction: true,
  tap: function () {
    this.doHideThisPopup();
    OB.UTIL.showLoggingOut(true);
    OB.MobileApp.model.logout();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBMOBC_LogoutDialogLogout'));
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.LogoutDialogLock',
  tap: function () {
    this.doHideThisPopup();
    OB.MobileApp.model.lock();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBMOBC_LogoutDialogLock'));
  }
});