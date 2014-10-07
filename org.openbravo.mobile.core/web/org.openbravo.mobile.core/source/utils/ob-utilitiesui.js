/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, console, _ */

OB = window.OB || {};
OB.UTIL = window.OB.UTIL || {};

enyo.kind({
  name: 'OB.UI.Thumbnail',
  published: {
    img: null,
    imgUrl: null
  },
  tag: 'div',
  classes: 'image-wrap',
  contentType: 'image/png',
  width: '49px',
  height: '49px',
  'default': '../org.openbravo.mobile.core/assets/img/box.png',
  components: [{
    tag: 'div',
    name: 'image',
    style: 'margin: auto; height: 100%; width: 100%; background-size: contain;'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.applyStyle('height', this.height);
    this.applyStyle('width', this.width);
    this.imgChanged();
  },
  drawImage: function () {
    var url;
    if (this.img || this.imgUrl) {
      if (this.img === 'iconBestSellers') {
        url = 'img/iconBestSellers.png';
      } else if (this.img) {
        url = 'data:' + this.contentType + ';base64,' + this.img;
      } else if (this.imgUrl) {
        url = this.imgUrl;
      }
    } else {
      url = this['default'];
    }
    this.$.image.applyStyle('background', '#ffffff url(' + url + ') center center no-repeat');
    this.$.image.applyStyle('background-size', 'contain');
  },
  imgChanged: function () {
    this.drawImage();
  },
  imgUrlChanged: function () {
    this.drawImage();
  }
});

enyo.kind({
  name: 'OB.UTIL.showAlert',
  classes: 'alert alert-fade',
  style: 'right:0; top:0; padding:2px; margin:0; cursor:pointer; border:0;',
  tap: function () {
    this.hide();
  },
  statics: {
    /**
     * Adds a new message to the bubble of messages
     * @param  {[type]} txt         the message to be shown
     * @param  {[type]} title       the title (deprecated)
     * @param  {[type]} type        alert-success, alert-warning, alert-error
     * @param  {[type]} keepVisible false: the message will be hiden after some time. true: you will execute the hide() function of the returned object when the line must dissapear
     * @return {[type]}             the OB.UTIL.alertLine created and added to the bubble of messages
     */
    display: function (txt, title, type, keepVisible) {
      if (!type) {
        type = 'alert-warning';
      }

      // we don't want to annoy the user... do not repeat error and warning messages in the bubble
      var componentsArray = OB.MobileApp.view.$.alertQueue.getComponents(),
          i;
      for (i = 0; i < componentsArray.length; i++) {
        var element = componentsArray[i];
        if (type === 'alert-warning' || type === 'alert-error') {
          if (element.txt === txt) {
            element.destroy();
          }
        }
      }

      // add a new line to the bubble of messages
      OB.MobileApp.view.$.alertQueue.show();
      var alertLine = OB.MobileApp.view.$.alertQueue.createComponent({
        kind: 'OB.UTIL.alertLine',
        title: title,
        txt: txt,
        type: type,
        keepVisible: keepVisible
      }).render();

      OB.UTIL.showAlert.showOrHideMessagesBubble();

      return alertLine;
    },
    showOrHideMessagesBubble: function () {
      var componentsArray = OB.MobileApp.view.$.alertQueue.getComponents();
      if (componentsArray.length === 0) {
        OB.MobileApp.view.$.alertQueue.removeClass('alert-fade-in');
      } else {
        OB.MobileApp.view.$.alertQueue.addClass('alert-fade-in');
      }
    }
  }
});

enyo.kind({
  name: 'OB.UTIL.alertLine',
  classes: '',
  style: 'padding:2px;',
  components: [{
    name: 'title',
    style: 'display:none; float: left; margin-right:10px;' // hiden because is obvious info but still there for future use
  }, {
    name: 'txt'
  }],
  initComponents: function () {
    var me = this,
        destroyTimeout;
    this.inherited(arguments);

    this.$.title.setContent(this.title);
    this.$.txt.setContent(this.txt);

    this.addClass(this.type);

    // Define the timeout of the message. These values should be moved taken from global settings
    switch (this.type) {
    case 'alert-success':
      destroyTimeout = 2000;
      break;
    case 'alert-warning':
      destroyTimeout = 2000;
      break;
    case 'alert-error':
      destroyTimeout = 4000;
      break;
    default:
      OB.error("DEVELOPER: The message of type '" + this.type + "' needs more code to be processed");
    }

    if (!this.keepVisible) {
      setTimeout(function () {
        me.hide();
        OB.UTIL.showAlert.showOrHideMessagesBubble();
      }, destroyTimeout);
    }
  },
  /**
   * Hides this particular message of the bubble of messages
   * @return undefined
   */
  hide: function () {
    this.destroy();
  }
});

/**
 * Shows a confirmation box.
 *
 * arguments:
 *   title: string with the title of the dialog
 *   text: string with the text of the dialog
 *   buttons: (optional) array of buttons. If this parameter is not present,
 *            a OK button will be shown, clicking on it will just close the
 *            confirmation dialog box.
 *            Each button in the array should have these attributes:
 *               label: text to be shown within the button
 *               action: (optional) function to be executed when the button
 *                       is tapped. If this attribute is not specified, the
 *                       action will be just to close the popup.
 */
enyo.kind({
  name: 'OB.UTIL.showConfirmation',
  statics: {
    display: function (title, text, buttons, options) {
      var container = OB.MobileApp.view.$.confirmationContainer,
          components = container.getComponents(),
          i, dialog;

      function getDialog() {

        // Allow display in a confirmation message a literal or a list of components
        var bodyContent;
        if (Array.isArray(text)) {
          bodyContent = {
            kind: 'enyo.Control',
            components: text
          };
        } else {
          bodyContent = {
            kind: 'enyo.Control',
            content: text
          };
        }

        var box = {
          kind: 'OB.UI.ModalAction',
          header: title,
          bodyContent: bodyContent,
          autoDismiss: !OB.UTIL.isNullOrUndefined(options) && !OB.UTIL.isNullOrUndefined(options.autoDismiss) ? options.autoDismiss : true,
          executeOnShow: function () {
            if (options && options.onShowFunction) {
              options.onShowFunction(this);
            }
            return true;
          },
          executeOnHide: function () {
            //the hide function only will be executed when
            //a button without action is used or when popup
            //is closed using background or x button
            if (options && !this.args.actionExecuted && options.onHideFunction) {
              options.onHideFunction(this);
            }
            return true;
          },
          bodyButtons: {}
        };

        if (options && options.style) {
          box.style = options.style;
        }

        if (!buttons) {
          box.bodyButtons = {
            kind: 'OB.UI.ModalDialogButton',
            content: OB.I18N.getLabel('OBMOBC_LblOk'),
            tap: function () {
              this.doHideThisPopup();
            }
          };
        } else {
          box.bodyButtons.components = [];
          _.forEach(buttons, function (btn) {
            var button = {
              kind: 'OB.UI.ModalDialogButton',
              content: btn.label,
              tap: function () {
                var params = {
                  actionExecuted: false
                };
                if (btn.action) {
                  params.actionExecuted = true;
                  btn.action();
                }
                this.doHideThisPopup({
                  args: params
                });
              }
            };
            box.bodyButtons.components.push(button);
          }, this);
        }
        return box;
      }

      // remove old confirmation box
      for (i = 0; i < components.length; i++) {
        components[i].destroy();
      }

      dialog = OB.MobileApp.view.$.confirmationContainer.createComponent(getDialog());

      dialog.show();
    }
  }
});

OB.UTIL.isSupportedBrowser = function () {
  if (navigator.userAgent.toLowerCase().indexOf('webkit') !== -1 && window.openDatabase) { // If the browser is not supported, show
    // message and finish.
    return true;
  } else {
    return false;
  }
};

OB.UTIL.showLoading = function (value) {
  if (value) {
    OB.MobileApp.view.$.containerWindow.hide();
    OB.MobileApp.view.$.containerLoading.show();
  } else {
    OB.MobileApp.view.$.containerLoading.hide();
    OB.MobileApp.view.$.containerWindow.show();
  }
};

OB.UTIL.showLoggingOut = function (value) {
  if (value) {
    OB.MobileApp.view.$.containerWindow.hide();
    OB.MobileApp.view.$.containerLoggingOut.show();
  } else {
    OB.MobileApp.view.$.containerLoggingOut.hide();
    OB.MobileApp.view.$.containerWindow.show();
  }
};

OB.UTIL.showLoggingOut = function (value) {
  if (value) {
    OB.MobileApp.view.$.containerWindow.hide();
    OB.MobileApp.view.$.containerLoggingOut.show();
  } else {
    OB.MobileApp.view.$.containerLoggingOut.hide();
    OB.MobileApp.view.$.containerWindow.show();
  }
};

OB.UTIL.showLoggingOut = function (value) {
  if (value) {
    OB.MobileApp.view.$.containerWindow.hide();
    OB.MobileApp.view.$.containerLoggingOut.show();
  } else {
    OB.MobileApp.view.$.containerLoggingOut.hide();
    OB.MobileApp.view.$.containerWindow.show();
  }
};

OB.UTIL.showSuccess = function (s) {
  return OB.UTIL.showAlert.display(s, OB.I18N.getLabel('OBMOBC_LblSuccess'), 'alert-success');
};

OB.UTIL.showWarning = function (s) {
  return OB.UTIL.showAlert.display(s, OB.I18N.getLabel('OBMOBC_LblWarning'), 'alert-warning');
};

OB.UTIL.showStatus = function (s) {
  return OB.UTIL.showAlert.display(s, 'Wait', '');
};

OB.UTIL.showError = function (s) {
  OB.UTIL.showLoading(false);
  return OB.UTIL.showAlert.display(s, OB.I18N.getLabel('OBMOBC_LblError'), 'alert-error');
};

// This funtion returns an array of Enyo components (the object and its
// children) sorted
// in the same way it they are rendered in the html
OB.UTIL.getAllChildsSorted = function (component, result) {
  var i;
  if (Object.prototype.toString.call(component) !== '[object Array]') {
    component = [component];
  }
  if (!result) {
    result = [];
  }
  for (i = 0; i < component.length; i++) {
    result.push(component[i]);
    if (typeof component[i].children !== 'undefined' && component[i].children.length !== 0) {
      result = OB.UTIL.getAllChildsSorted(component[i].children, result);
    }
  }
  return result;
};