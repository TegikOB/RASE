/*
 ************************************************************************************
 * Copyright (C) 2012-2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _, $ */
enyo.kind({
  name: 'OB.UI.Button',
  kind: 'enyo.Button',
  handlers: {
    onkeydown: 'keydownHandler'
  },
  keydownHandler: function (inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    if (keyCode === 13 || keyCode === 32) { //Handle ENTER and SPACE keys in buttons
      this.executeTapAction();
      return true;
    }
    return false;
  },
  executeTapAction: function () {
    if (this && this.ontap && this.owner && this.owner[this.ontap]) {
      this.owner[this.ontap]();
    } else {
      this.tap();
    }
  },
  focus: function () { // Enyo doesn't have "focus" function in buttons, so it has to be implemented
    if (this.hasNode()) {
      this.hasNode().focus();
    }
  },

  initComponents: function () {
    this.inherited(arguments);
    if (this.i18nContent) {
      this.setContent(OB.I18N.getLabel(this.i18nContent));
    } else if (this.i18nLabel) {
      this.setContent(OB.I18N.getLabel(this.i18nLabel));
    } else if (this.label) {
      this.setContent(this.label);
    }
  }
/*, Removed to investigate if btn-over/btn-down are still needed due to enyo/onyx adoption
  handlers: {
    onmouseover: 'mouseOverOut',
    onmouseout: 'mouseOverOut'
  },
  //TODO: support windows 7  setTimeout(function() { me.$el.removeClass('btn-down'); }, 125);
  mouseOverOut: function (inSender, inEvent) {
    this.addRemoveClass('btn-over', inEvent.type === 'mouseover');
  }*/
});

enyo.kind({
  name: 'OB.UI.RegularButton',
  kind: 'OB.UI.Button',
  icon: '',
  iconright: '',
  label: '',
  classes: 'btnlink'
});

enyo.kind({
  name: 'OB.UI.SmallButton',
  kind: 'OB.UI.RegularButton',
  classes: 'btnlink-small'
});

enyo.kind({
  name: 'OB.UI.MediumButton',
  kind: 'OB.UI.RegularButton',
  classes: 'btnlink-medium'
});

enyo.kind({
  name: 'OB.UI.ModalDialogButton',
  kind: 'OB.UI.RegularButton',
  events: {
    onHideThisPopup: ''
  },
  classes: 'btnlink-gray modal-dialog-button'
});

enyo.kind({
  name: 'OB.UI.Popup',
  kind: "onyx.Popup",
  centered: true,
  floating: true,
  closeOnEscKey: true,
  scrim: true,
  autoDismiss: true,
  handlers: {
    onkeydown: 'keydownHandler',
    onHideThisPopup: 'hideFromInside'
  },
  keydownHandler: function (inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    if (keyCode === 27 && this.showing && this.closeOnEscKey) { //Handle ESC key to hide the popup
      this.hide();
      return true;
    } else if (keyCode === 13 && this.defaultActionButton) { //Handle ENTER key to execute the default action (if exists)
      this.defaultActionButton.executeTapAction();
      return true;
    } else {
      return false;
    }
  },
  _addArgsToComponent: function (args) {
    if (args) {
      this.args = this.args || {};
      _.each(args, function (arg, key) {
        this.args[key] = arg;
      }, this);
    }
  },
  showingChanged: function () {
    this.inherited(arguments);
    if (this.showing) {
      OB.MobileApp.view.openedPopup = this;
    } else {
      OB.MobileApp.view.openedPopup = null;
    }
  },
  show: function (args) {
    var resp = true;

    // While in poup, disable focusKeeper
    this.originalScanMode = OB.MobileApp.view.scanMode;
    OB.MobileApp.view.scanningFocus(false);

    this.args = {}; //reset;
    this._addArgsToComponent(args);
    if (this.executeOnShow) {
      resp = this.executeOnShow();
      if (_.isUndefined(resp) || _.isNull(resp)) {
        resp = true;
      }
    }
    if (resp) {
      var me = this;
      if (me.autoDismiss) {
        me.autoDismiss = false;
        this.inherited(arguments);
        setTimeout(function () {
          //The autoDismiss is activated only after a small delay, to prevent issue 24582 from happening
          me.autoDismiss = true;
        }, 100);
      } else {
        this.inherited(arguments);
      }
      this.setDefaultActionButton();
      this.focusInPopup();
      if (this.executeOnShown) {
        this.executeOnShown();
      }
    }
  },
  hideFromInside: function (inSender, inEvent) {
    this.hide(inEvent.args);
  },
  hide: function (args) {
    var resp = true;
    this._addArgsToComponent(args);
    if (this.executeBeforeHide) {
      resp = this.executeBeforeHide();
      if (_.isUndefined(resp) || _.isNull(resp)) {
        resp = true;
      }
    }
    if (resp) {

      this.inherited(arguments);
      // This line will remove the popup in case hasNode doesn't return a valid component.
      // This is needed because after a resize, if not done, the popups stop working correctly
      if (!this.hasNode()) {
        var element = document.getElementById(this.id);
        element.parentNode.removeChild(element);
      }
      // Restore focus keeper
      OB.MobileApp.view.scanningFocus(this.originalScanMode);
      if (this.executeOnHide) {
        this.executeOnHide();
      }
    }
    delete this.originalScanMode;
  },
  focusInPopup: function () {
    var allChildsArray = OB.UTIL.getAllChildsSorted(this),
        isFirstFocusableElementObtained = false,
        tagName, element, i;
    for (i = 0; i < allChildsArray.length; i++) {
      if (allChildsArray[i].hasNode() && allChildsArray[i].hasNode().tagName) {
        tagName = allChildsArray[i].hasNode().tagName.toUpperCase();
      } else {
        tagName = '';
      }
      if ((tagName === 'INPUT' || tagName === 'SELECT' || tagName === 'TEXTAREA' || tagName === 'BUTTON') && allChildsArray[i].showing && !isFirstFocusableElementObtained) {
        element = allChildsArray[i];
        isFirstFocusableElementObtained = true;
      } else if (allChildsArray[i].isFirstFocus) {
        element = allChildsArray[i];
        break;
      }
    }
    if (element) {
      element.focus();
    }
    return true;
  },
  setDefaultActionButton: function (element) {
    var allChildsArray, tagName, i;
    if (element) {
      allChildsArray = [element];
    } else {
      allChildsArray = OB.UTIL.getAllChildsSorted(this);
    }

    for (i = 0; i < allChildsArray.length; i++) {
      if (allChildsArray[i].hasNode() && allChildsArray[i].hasNode().tagName) {
        tagName = allChildsArray[i].hasNode().tagName.toUpperCase();
      } else {
        tagName = '';
      }
      if (tagName === 'BUTTON' && allChildsArray[i].isDefaultAction) {
        element = allChildsArray[i];
        break;
      }
    }
    if (element) {
      this.defaultActionButton = element;
    }
    return true;
  },
  updatePosition: function () {
    if (!this.centered || !this.floating) {
      this.inherited(arguments);
      return true;
    }
    // Improve of enyo "updatePosition" function to proper manage of % and absolute top and left positions
    var top, left, t = this.getBounds();
    if (this.topPosition) {
      top = this.topPosition.toString();
    } else if (this.centered) {
      top = '50%';
    } else {
      top = '0';
    }
    if (top.indexOf('px') !== -1) {
      top = top.replace('px', '');
    }

    if (this.leftPosition) {
      left = this.leftPosition.toString();
    } else if (this.centered) {
      left = '50%';
    } else {
      left = '0';
    }
    if (left.indexOf('px') !== -1) {
      left = left.replace('px', '');
    }

    if (top.indexOf('%') !== -1) {
      this.addStyles('top: ' + top);
      this.addStyles('margin-top: -' + Math.max(t.height / 2, 0).toString() + 'px;');
    } else {
      this.addStyles('top: ' + top + 'px');
    }

    if (left.indexOf('%') !== -1) {
      this.addStyles('left: ' + left);
      this.addStyles('margin-left: -' + Math.max(t.width / 2, 0).toString() + 'px;');
    } else {
      this.addStyles('left: ' + left + 'px');
    }

    return true;
  }
});

enyo.kind({
  //TODO: maxheight
  name: 'OB.UI.Modal',
  kind: 'OB.UI.Popup',
  classes: 'modal modal-poup',
  published: {
    header: ''
  },
  components: [{
    tag: 'div',
    classes: 'modal-header',
    components: [{
      name: 'closebutton',
      tag: 'div',
      classes: 'modal-header-closebutton',
      components: [{
        tag: 'span',
        ontap: 'hide',
        allowHtml: true,
        content: '&times;'
      }]
    }, {
      name: 'header',
      classes: 'modal-header-text'
    }]
  }, {
    tag: 'div',
    name: 'body',
    classes: 'modal-body'
  }],
  headerChanged: function () {
    this.$.header.setContent(this.i18nHeader ? OB.I18N.getLabel(this.i18nHeader) : this.header);
  },
  initComponents: function () {
    this.inherited(arguments);
    if (this.modalClass) {
      this.addClass(this.modalClass);
    }

    this.$.header.setContent(this.i18nHeader ? OB.I18N.getLabel(this.i18nHeader) : this.header);

    if (this.bodyClass) {
      this.$.body.addClass(this.bodyClass);
    }
    this.$.body.createComponent(this.body);
  }
});

enyo.kind({
  name: 'OB.UI.RenderEmpty',
  style: 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc',
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(this.label || OB.I18N.getLabel('OBMOBC_SearchNoResults'));
  }
});


enyo.kind({
  name: 'OB.UI.SelectButton',
  kind: 'OB.UI.Button',
  classes: 'btnselect',
  tap: function () {
    this.model.trigger('selected', this.model);
    this.model.trigger('click', this.model);
  }
});

//Automated test. new kind to extend list item buttons.
enyo.kind({
  kind: 'OB.UI.SelectButton',
  name: 'OB.UI.listItemButton',
  classes: 'btnselect',
  tap: function () {
    this.model.trigger('selected', this.model);
    this.model.trigger('click', this.model);
  },
  getValueByFieldName: function (fieldName) {
    if (this.$[fieldName] && this.$[fieldName].getValue) {
      return this.$[fieldName].getValue();
    } else if (this.$[fieldName] && this.$[fieldName].content) {
      return this.$[fieldName].content;
    } else {
      OB.warn('No way found to return field name. Overwrite this method in the component to do it');
    }
    return null;
  },
  getValueByIndex: function (index) {
    if (this.columns) {
      if (this.getValueByFieldName) {
        return this.getValueByFieldName(this.columns[index]);
      } else {
        OB.warn(this.name + ' must implement getValueByFieldName');
      }
    } else {
      OB.warn('columns array is not present in ' + this.name + ' And it is needed to execute getValueByIndex');
    }
    return null;
  },
  getValue: function (arg) {
    if (_.isNumber(arg)) {
      if (this.getValueByIndex) {
        return this.getValueByIndex(arg);
      } else {
        OB.warn('getValueByIndex is not present in ' + this.name);
        return null;
      }
    } else if (_.isString(arg)) {
      if (this.getValueByFieldName) {
        return this.getValueByFieldName(arg);
      } else {
        OB.warn('getValueByFieldName is not present in ' + this.name);
        return null;
      }
    }
  }
});

enyo.kind({
  name: 'OB.UI.CancelButton',
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-white btnlink-fontgray',
  events: {
    onShowPopup: ''
  },
  tap: function () {
    this.doShowPopup({
      popup: 'modalCancel'
    });
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(this.label || OB.I18N.getLabel('OBMOBC_LblCancel'));
  }
});

enyo.kind({
  name: 'OB.UI.RadioButton',
  kind: "onyx.Checkbox",
  classes: 'btn btn-radio',
  style: 'padding: 0px 0px 0px 40px; margin: 10px;',
  activeRadio: function () {
    this.addClass('active');
    this.setChecked(true);
  },
  disableRadio: function () {
    this.setNodeProperty('checked', false);
    this.setAttribute('checked', '');
    this.removeClass('active');
    this.active = false;
    this.checked = false;
  },
  tap: function () {
    this.inherited(arguments);
    this.activeRadio();
  },
  initComponents: function () {
    this.inherited(arguments);
  }
});



// Toolbar Button
enyo.kind({
  name: 'OB.UI.ToolbarButton',
  kind: 'OB.UI.RegularButton',
  classes: 'btnlink-toolbar',
  initComponents: function () {
    this.inherited(arguments);
    if (this.icon) {
      this.addClass(this.icon);
    }
  }
});

enyo.kind({
  name: 'OB.UI.CheckboxButton',
  kind: 'OB.UI.Button',
  classes: 'btn-check',
  checked: false,
  tap: function () {
    if (this.readOnly) {
      return;
    }
    this.checked = !this.checked;
    this.addRemoveClass('active', this.checked);
  },
  toggle: function () {
    this.checked = !this.checked;
    this.addRemoveClass('active', this.checked);
  },
  check: function () {
    this.addClass('active');
    this.checked = true;
  },
  unCheck: function () {
    this.removeClass('active');
    this.checked = false;
  }
});

// Order list
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.ButtonTab',
  initComponents: function () {
    var label;
    this.inherited(arguments);
    this.addClass('btnlink btnlink-gray');
    label = this.i18nLabel ? OB.I18N.getLabel(this.i18nLabel) : this.label;
    if (label) {
      this.createComponent({
        name: 'lbl',
        tag: 'span',
        content: label
      });
    }
    //TODO
    //this.receipt.on('change:gross', function() {
    //  this.render();
    //}, this)
  }
});



// Order list
enyo.kind({
  name: 'OB.UI.ToolbarButtonTab',
  kind: 'OB.UI.ButtonTab',
  events: {
    onTabChange: ''
  },
  initComponents: function () {
    this.inherited(arguments);
    this.addClass('btnlink-toolbar');
  }
});


enyo.kind({
  name: 'OB.UI.TabPane',
  classes: 'postab-pane',
  executeOnShow: function (params) {
    var components = this.getComponents();
    if (components.length > 0) {
      if (this.$[components[0].getName()].executeOnShow) {
        this.$[components[0].getName()].executeOnShow(params);
      }
    }
  }
});


enyo.kind({
  //TODO: maxheight
  name: 'OB.UI.ModalAction',
  kind: 'OB.UI.Popup',
  classes: 'modal-dialog',
  bodyContentClass: 'modal-dialog-body-content',
  bodyButtonsClass: 'modal-dialog-body-buttons',
  components: [{
    classes: 'modal-dialog-header',
    components: [{
      name: 'headerCloseButton',
      classes: 'modal-dialog-header-closebutton',
      components: [{
        tag: 'span',
        ontap: 'hide',
        allowHtml: true,
        content: '&times'
      }]
    }, {
      name: 'header',
      classes: 'modal-dialog-header-text'
    }]
  }, {
    classes: 'modal-dialog-body',
    name: 'bodyParent',
    components: [{
      name: 'bodyContent'
    }, {
      name: 'bodyButtons'
    }]
  }],
  setHeader: function (headerText) {
    this.$.header.setContent(headerText);
  },
  initComponents: function () {
    this.inherited(arguments);

    if (this.i18nHeader) {
      this.$.header.setContent(OB.I18N.getLabel(this.i18nHeader));
    } else {
      this.$.header.setContent(this.header);
    }

    if (this.maxheight) {
      this.$.bodyParent.setStyle('max-height: ' + this.maxheight + ';');
    }

    this.$.bodyContent.setClasses(this.bodyContentClass);
    if (this.bodyContent && this.bodyContent.i18nContent) {
      this.$.bodyContent.setContent(OB.I18N.getLabel(this.bodyContent.i18nContent));
    } else {
      this.$.bodyContent.createComponent(this.bodyContent);
    }

    this.$.bodyButtons.setClasses(this.bodyButtonsClass);
    this.$.bodyButtons.createComponent(this.bodyButtons);
  }
});

enyo.kind({
  //TODO: maxheight
  name: 'OB.UI.ActionPopup',
  kind: 'OB.UI.Popup',
  classes: 'modal-dialog',
  bodyContentClass: 'modal-dialog-body-content',
  bodyButtonsClass: 'modal-dialog-body-buttons',
  components: [{
    classes: 'modal-dialog-header',
    components: [{
      name: 'headerCloseButton',
      classes: 'modal-dialog-header-closebutton',
      components: [{
        tag: 'span',
        ontap: 'hide',
        allowHtml: true,
        content: '&times'
      }]
    }, {
      name: 'header',
      classes: 'modal-dialog-header-text'
    }]
  }, {
    classes: 'modal-dialog-body',
    name: 'bodyParent',
    components: [{
      name: 'bodyContent'
    }, {
      name: 'bodyButtons'
    }]
  }],
  setHeader: function (headerText) {
    this.$.header.setContent(headerText);
  },
  initComponents: function () {
    this.inherited(arguments);

    if (this.i18nHeader) {
      this.$.header.setContent(OB.I18N.getLabel(this.i18nHeader));
    } else {
      this.$.header.setContent(this.header);
    }

    if (this.maxheight) {
      this.$.bodyParent.setStyle('max-height: ' + this.maxheight + ';');
    }

    this.$.bodyContent.setClasses(this.bodyContentClass);
    if (this.bodyContent && this.bodyContent.i18nContent) {
      this.$.bodyContent.setContent(OB.I18N.getLabel(this.bodyContent.i18nContent));
    } else {
      this.$.bodyContent.createComponent(this.bodyContent, {owner: this});
    }

    this.$.bodyButtons.setClasses(this.bodyButtonsClass);
    this.$.bodyButtons.createComponent(this.bodyButtons, {owner: this});
  }
});

enyo.kind({
  name: 'OB.UI.SearchInput',
  kind: 'enyo.Input',
  type: 'text',
  classes: 'input',
  attributes: {
    'x-webkit-speech': 'x-webkit-speech'
  }
});

enyo.kind({
  name: 'OB.UI.SearchInputAutoFilter',
  kind: 'enyo.Input',
  type: 'text',
  classes: 'input',
  attributes: {
    'x-webkit-speech': 'x-webkit-speech'
  },

  events: {
    onFiltered: ''
  },

  minLengthToSearch: 0,

  handlers: {
    onblur: 'blur'
  },
  blur: function () {
    // blurring from input: check if it needed to set focus keeper in order
    // focus not to be out of an input tag
    // This shouldn't be done in iOS, if done the focuskeeper loses the focus
    if (!navigator.userAgent.match(/iPhone|iPad|iPod/i)) {
      OB.MobileApp.view.scanningFocus();
    }
  },
  input: function () {
    var temp = this.getValue(),
        me = this;
    if (temp.length >= this.minLengthToSearch || this.minLengthToSearch === 0) {
      setTimeout(function () {
        if (temp === me.getValue()) {
          me.doFiltered();
        }
      }, 1000);
    }
  }
});

enyo.kind({
  name: 'OB.UI.ModalInfo',
  kind: 'OB.UI.ModalAction',
  bodyButtons: {
    components: [{
      kind: 'OB.UI.AcceptDialogButton'
    }]
  },
  events: {
    onShowPopup: ''
  },
  closeOnAcceptButton: true,
  initComponents: function () {
    this.inherited(arguments);
    this.$.bodyButtons.$.acceptDialogButton.dialogContainer = this;

  }
});

enyo.kind({
  name: 'OB.UI.InfoPopup',
  kind: 'OB.UI.ActionPopup',
  bodyButtons: {
    components: [{
      kind: 'OB.UI.AcceptDialogButton'
    }]
  },
  events: {
    onShowPopup: ''
  },
  closeOnAcceptButton: true,
  initComponents: function () {
    this.inherited(arguments);
    this.$.acceptDialogButton.dialogContainer = this;
  }
});

enyo.kind({
  name: 'OB.UI.AcceptDialogButton',
  kind: 'OB.UI.ModalDialogButton',
  classes: 'btnlink btnlink-gray modal-dialog-button',
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    if (this.dialogContainer.acceptCallback) {
      this.dialogContainer.acceptCallback();
    }
    if (this.dialogContainer.closeOnAcceptButton) {
      this.doHideThisPopup();
    }
  },
  initComponents: function () {
    if (this.label) {
      this.setContent(this.label);
    } else {
      this.setContent(OB.I18N.getLabel('OBMOBC_LblOk'));
    }
    this.inherited(arguments);
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.CancelDialogButton',
  tap: function () {
    this.doHideThisPopup();
  },
  initComponents: function () {
    this.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
    this.inherited(arguments);

  }
});