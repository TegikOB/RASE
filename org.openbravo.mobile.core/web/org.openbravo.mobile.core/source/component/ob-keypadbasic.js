/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _ */

enyo.kind({
  name: 'OB.UI.KeypadBasic',
  padName: 'basic',
  components: [{
    classes: 'row-fluid',
    components: [{
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '/',
        command: '/',
        name: 'toolbarBtn1'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '*',
        command: '*',
        name: 'toolbarBtn2'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '%',
        command: '%',
        name: 'toolbarBtn3'
      }]
    }]
  }, {
    classes: 'row-fluid',
    components: [{
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '7',
        command: '7',
        name: 'keypadBtn7'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '8',
        command: '8',
        name: 'keypadBtn8'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '9',
        command: '9',
        name: 'keypadBtn9'
      }]
    }]
  }, {
    classes: 'row-fluid',
    components: [{
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '4',
        command: '4',
        name: 'keypadBtn4'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '5',
        command: '5',
        name: 'keypadBtn5'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '6',
        command: '6',
        name: 'keypadBtn6'
      }]
    }]
  }, {
    classes: 'row-fluid',
    components: [{
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '1',
        command: '1',
        name: 'keypadBtn1'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '2',
        command: '2',
        name: 'keypadBtn2'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '3',
        command: '3',
        name: 'keypadBtn3'
      }]
    }]
  }, {
    classes: 'row-fluid',
    components: [{
      classes: 'span8',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        label: '0',
        command: '0',
        name: 'keypadBtn0'
      }]
    }, {
      classes: 'span4',
      components: [{
        kind: 'OB.UI.ButtonKey',
        classButton: 'btnkeyboard-num',
        name: 'decimalSymbol'
      }]
    }]
  }],

  initComponents: function () {
    var decimalButton, i;


    this.inherited(arguments);

    this.label = OB.I18N.getLabel('OBMOBC_KeypadBasic');

    if (this.toolbarButtons) {
      i = 0;
      _.forEach(this.toolbarButtons, function (btn) {
        var btnName;
        i += 1;
        btnName = 'toolbarBtn' + i;
        this.$[btnName].setLabel(btn.i18nLabel ? OB.I18N.getLabel(btn.i18nLabel) : btn.label);
        if (btn.command) {
          this.$[btnName].command = btn.command;

        }
        this.$[btnName].doRegisterButton();
      }, this);
    }

    // hack to set label and command after creation
    decimalButton = this.$.decimalSymbol;
    decimalButton.label = OB.Format.defaultDecimalSymbol;
    decimalButton.command = OB.Format.defaultDecimalSymbol;
    this.keyboard.registerButton(null, {
      originator: decimalButton
    });
    decimalButton.$.button.setContent(OB.Format.defaultDecimalSymbol);
    decimalButton.$.button.setDisabled(false);
    decimalButton.$.button.removeClass('btnkeyboard-inactive');
  }

});

enyo.kind({
  name: 'OB.UI.ButtonKey',
  events: {
    onAddCommand: '',
    onAddButton: '',
    onKeyCommandPressed: '',
    onRegisterButton: ''
  },
  handlers: {
    onDisableButton: 'disableButton'
  },
  style: 'margin: 5px;',
  classButtonActive: 'btnactive',
  classButton: '',
  classButtonDisabled: 'btnkeyboard-inactive',
  command: false,
  permission: null,
  label: null,
  components: [{
    kind: 'OB.UI.Button',
    name: 'button',
    classes: 'btnkeyboard'
  }],
  disableButton: function (inSender, inEvent) {
    if (inSender === this || _.indexOf(inEvent.commands, this.command) !== -1) {
      this.$.button.setDisabled(inEvent.disabled);
      if (inEvent.disabled) {
        this.$.button.addClass(this.classButtonDisabled);
      } else {
        this.$.button.removeClass(this.classButtonDisabled);
      }
    }
    return true;
  },
  setLabel: function (lbl) {
    this.$.button.setContent(lbl);
  },
  initComponents: function () {
    var me = this;

    this.inherited(arguments);

    this.doRegisterButton();

    this.$.button.addClass(this.classButton);
    this.$.button.setContent(this.label);

    if (this.isDisabled) {
      this.$.button.setDisabled(true);
      this.$.button.addClass(this.classButtonDisabled);
    }
  }
});