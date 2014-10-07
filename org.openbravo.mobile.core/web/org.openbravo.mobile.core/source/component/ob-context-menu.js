/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

enyo.kind({
  name: 'OB.UI.ButtonContextMenu',
  kind: 'OB.UI.ToolbarButton',
  icon: 'btn-icon btn-icon-menu',
  handlers: {
    onLeftToolbarDisabled: 'disabledButton'
  },
  disabledButton: function (inSender, inEvent) {
    this.setDisabled(inEvent.status);
  },
  components: [{
    name: 'leftIcon'
  }, {
    tag: 'span',
    style: 'display: inline-block;'
  }, {
    name: 'rightIcon'
  }],
  ontap: 'onButtonTap'
});

enyo.kind({
  name: 'OB.UI.ToolbarMenu',
  components: [{
    kind: 'onyx.MenuDecorator',
    name: 'btnContextMenu',
    components: [{
      kind: 'OB.UI.ButtonContextMenu',
      name: 'toolbarButton'
    }, {
      kind: 'onyx.Menu',
      classes: 'dropdown',
      name: 'menu',
      maxHeight: 600,
      scrolling: false,
      floating: true
    }]
  }],
  onButtonTap: function () {
    if (this.$.toolbarButton.hasClass('btn-over')) {
      this.$.toolbarButton.removeClass('btn-over');
    }
  },
  initComponents: function () {
    this.inherited(arguments);

    enyo.forEach(this.menuEntries, function (entry) {
      this.$.menu.createComponent(entry);
    }, this);
  }
});