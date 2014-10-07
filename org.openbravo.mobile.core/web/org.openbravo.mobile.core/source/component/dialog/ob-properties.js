/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _*/

enyo.kind({
  name: 'OB.UI.ModalReceiptProperties',
  kind: 'OB.UI.ModalAction',
  handlers: {
    onApplyChanges: 'applyChanges'
  },
  bodyContent: {
    kind: 'Scroller',
    maxHeight: '225px',
    style: 'background-color: #ffffff;',
    thumb: true,
    horizontal: 'hidden',
    components: [{
      name: 'attributes'
    }]
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ReceiptPropertiesDialogApply'
    }, {
      kind: 'OB.UI.ReceiptPropertiesDialogCancel'
    }]
  },
  loadValue: function (mProperty) {
    this.waterfall('onLoadValue', {
      model: this.model,
      modelProperty: mProperty
    });
  },
  applyChanges: function (inSender, inEvent) {
    this.waterfall('onApplyChange', {});
    return true;
  },
  initComponents: function () {
    this.inherited(arguments);
    this.attributeContainer = this.$.bodyContent.$.attributes;
    enyo.forEach(this.newAttributes, function (natt) {
      this.$.bodyContent.$.attributes.createComponent({
        kind: 'OB.UI.PropertyEditLine',
        name: 'line_' + natt.name,
        newAttribute: natt
      });
    }, this);
  }
});


enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ReceiptPropertiesDialogApply',
  isDefaultAction: true,
  events: {
    onApplyChanges: ''
  },
  tap: function () {
    if (this.doApplyChanges()) {
      this.doHideThisPopup();
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBMOBC_LblApply'));
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ReceiptPropertiesDialogCancel',
  tap: function () {
    this.doHideThisPopup();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
  }
});

enyo.kind({
  name: 'OB.UI.PropertyEditLine',
  components: [{
    //style: 'border: 1px solid #F0F0F0; background-color: #E2E2E2; color: black; width: 150px; height: 40px; float: left; text-align: right;',
    classes: 'properties-label',
    name: 'label',
    components: [{
      name: 'labelLine',
      style: 'padding: 5px 8px 0px 0px; font-size: 15px;',
      content: ''
    }]
  }, {
    style: 'border: 1px solid #F0F0F0; float: left; width: 57%;',
    components: [{
      name: 'newAttribute',
      classes: 'modal-dialog-receipt-properties-text',
      style: 'width: 98%'
    }]
  }, {
    style: 'clear: both'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.propertycomponent = this.$.newAttribute.createComponent(this.newAttribute);
    if (this.propertycomponent.height) {
      this.$.label.applyStyle('height', this.propertycomponent.height);
    }
    if (this.newAttribute.i18nLabel) {
      this.$.labelLine.content = OB.I18N.getLabel(this.newAttribute.i18nLabel);
    } else {
      this.$.labelLine.content = this.newAttribute.label;
    }
  }
});

enyo.kind({
  name: 'OB.UI.renderTextProperty',
  kind: 'enyo.Input',
  type: 'text',
  classes: 'input',
  style: 'width: 100%;',
  handlers: {
    onLoadValue: 'loadValue',
    onApplyChange: 'applyChange'
  },
  events: {
    onSetProperty: '',
    onSetLineProperty: ''
  },
  loadValue: function (inSender, inEvent) {
    if (this.modelProperty === inEvent.modelProperty) {
      if (inEvent.model && inEvent.model.get(this.modelProperty)) {
        this.setValue(inEvent.model.get(this.modelProperty));
      } else {
        this.setValue('');
      }
    }
  },
  applyChange: function (inSender, inEvent) {
    return this.applyValue(inEvent.orderline);
  },
  applyValue: function (orderline) {
    if (orderline) {
      this.doSetLineProperty({
        line: orderline,
        property: this.modelProperty,
        value: this.getValue(),
        extraProperties: this.extraProperties
      });
    } else {
      this.doSetProperty({
        property: this.modelProperty,
        value: this.getValue(),
        extraProperties: this.extraProperties
      });
    }
    return true;
  },
  initComponents: function () {
    if (this.readOnly) {
      this.setAttribute('readonly', 'readonly');
    }
  }
});

enyo.kind({
  name: 'OB.UI.renderTextMultiLineProperty',
  kind: 'enyo.TextArea',
  type: 'text',
  height: '90px',

  classes: 'input',
  style: 'width: 100%; height: 80px',
  handlers: {
    onLoadValue: 'loadValue',
    onApplyChange: 'applyChange',
    onkeydown: 'keydownHandler'
  },
  events: {
    onSetProperty: '',
    onSetLineProperty: ''
  },

  loadValue: function (inSender, inEvent) {
    if (this.modelProperty === inEvent.modelProperty) {
      if (inEvent.model && inEvent.model.get(this.modelProperty)) {
        this.setValue(inEvent.model.get(this.modelProperty));
      } else {
        this.setValue('');
      }
    }
  },
  keydownHandler: function (inSender, inEvent) {
    if (inEvent.keyCode === 13) {
      // to allow multi line, prevent enter key to execute default command
      return true;
    }
  },
  applyChange: function (inSender, inEvent) {
    return this.applyValue(inEvent.orderline);
  },
  applyValue: function (orderline) {
    if (orderline) {
      this.doSetLineProperty({
        line: orderline,
        property: this.modelProperty,
        value: this.getValue(),
        extraProperties: this.extraProperties
      });
    } else {
      this.doSetProperty({
        property: this.modelProperty,
        value: this.getValue(),
        extraProperties: this.extraProperties
      });
    }
    return true;
  },
  initComponents: function () {
    if (this.readOnly) {
      this.setAttribute('readonly', 'readonly');
    }
  }
});

enyo.kind({
  name: 'OB.UI.renderBooleanProperty',
  kind: 'OB.UI.CheckboxButton',
  classes: 'modal-dialog-btn-check',
  handlers: {
    onLoadValue: 'loadValue',
    onApplyChange: 'applyChange',
    onLoadContent: 'loadContent'
  },
  events: {
    onSetProperty: '',
    onSetLineProperty: ''
  },
  loadValue: function (inSender, inEvent) {
    var i, splitResult, contentProperty, contentInModel;
    if (this.modelProperty === inEvent.modelProperty) {
      if (inEvent.model.get(this.modelProperty) !== undefined) {
        this.checked = inEvent.model.get(this.modelProperty);
      }

      if (this.checked) {
        this.addClass('active');
      } else {
        this.removeClass('active');
      }
    }

    if (this.modelContent !== undefined && this.modelContent !== "") {
      splitResult = this.modelContent.split(':');
      if (splitResult.length > 0) {
        contentProperty = splitResult[0];

        if (contentProperty === inEvent.modelProperty) {
          contentInModel = inEvent.model;
          for (i = 0; i < splitResult.length; i++) {
            contentInModel = contentInModel.get(splitResult[i]);
          }

          if (contentInModel !== undefined) {
            this.content = contentInModel;
          }
        }
      }
    }

  },
  applyChange: function (inSender, inEvent) {
    if (this.disabled) {
      return true;
    }
    if (inEvent.orderline) {
      this.doSetLineProperty({
        line: inEvent.orderline,
        property: this.modelProperty,
        value: this.checked,
        extraProperties: this.extraProperties
      });
    } else {
      this.doSetProperty({
        property: this.modelProperty,
        value: this.checked,
        extraProperties: this.extraProperties
      });
    }
  },
  initComponents: function () {
    if (this.readOnly) {
      this.setDisabled(true);
      this.setAttribute('disabled', 'disabled');
    }
  }
});

enyo.kind({
  name: 'OB.UI.renderComboProperty',
  handlers: {
    onLoadValue: 'loadValue',
    onApplyChange: 'applyChange'
  },
  events: {
    onSaveProperty: ''
  },
  components: [{
    kind: 'OB.UI.List',
    name: 'renderCombo',
    classes: 'combo',
    style: 'width: 100%; margin:0;',
    renderLine: enyo.kind({
      kind: 'enyo.Option',
      initComponents: function () {
        this.inherited(arguments);
        this.setValue(this.model.get(this.parent.parent.retrievedPropertyForValue));
        this.setContent(this.model.get(this.parent.parent.retrievedPropertyForText));
      }
    }),
    renderEmpty: 'enyo.Control'
  }],
  loadValue: function (inSender, inEvent) {
    if (this.modelProperty === inEvent.modelProperty) {
      this.$.renderCombo.setCollection(this.collection);
      this.fetchDataFunction(inEvent);
    }
  },
  dataReadyFunction: function (data, inEvent) {
    var index = 0,
        result = null;
    if (data) {
      this.collection.reset(data.models);
    } else {
      this.collection.reset(null);
      return;
    }

    result = _.find(this.collection.models, function (option) {
      if (inEvent.model) {
        //Edit: select actual value
        if (option.get(this.retrievedPropertyForValue) === inEvent.model.get(this.modelProperty)) {
          return true;
        }
      } else {
        //New: select default value
        if (option.get(this.retrievedPropertyForValue) === this.defaultValue) {
          return true;
        }
      }
      index += 1;
    }, this);
    if (result) {
      this.$.renderCombo.setSelected(index);
    } else {
      this.$.renderCombo.setSelected(0);
    }
  },
  applyChange: function (inSender, inEvent) {
    var selected = this.collection.at(this.$.renderCombo.getSelected());
    if (selected) {
      inSender.model.set(this.modelProperty, selected.get(this.retrievedPropertyForValue));
    }
    if (this.modelPropertyText) {
      inSender.model.set(this.modelPropertyText, selected.get(this.retrievedPropertyForText));
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    if (this.readOnly) {
      this.setAttribute('readonly', 'readonly');
    }
  }
});