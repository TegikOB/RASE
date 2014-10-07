/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */

// Container for the whole application
enyo.kind({
  name: 'OB.UI.Terminal',

  events: {
    onDestroyMenu: ''
  },
  published: {
    scanning: false
  },

  classes: 'pos-container',
  style: 'height:700px',
  components: [{
    style: 'height: 10px;'
  }, {
    components: [{
      name: 'containerLoading',
      components: [{
        classes: 'POSLoadingCenteredBox',
        components: [{
          classes: 'POSLoadingPromptLabel',
          content: 'Loading...' //OB.I18N.getLabel('OBPOS_LblLoading')
        }, {
          classes: 'POSLoadingProgressBar',
          components: [{
            classes: 'POSLoadingProgressBarImg'
          }]
        }]
      }]
    }, {
      name: 'containerLoggingOut',
      showing: false,
      components: [{
        classes: 'POSLoadingCenteredBox',
        components: [{
          classes: 'POSLoadingPromptLabel',
          content: 'Logging out...' //OB.I18N.getLabel('OBPOS_LblLoggingOut')
        }, {
          classes: 'POSLoadingProgressBar',
          components: [{
            classes: 'POSLoadingProgressBarImg'
          }]
        }]
      }]
    }, {
      name: 'containerWindow',
      handlers: {
        onShowPopup: 'showPopupHandler',
        onHidePopup: 'hidePopupHandler'
      },
      getRoot: function () {
        return this.getComponents()[0];
      },
      showPopupHandler: function (inSender, inEvent) {
        if (inEvent.popup) {
          this.showPopup(inEvent.popup, inEvent.args);
        }
      },
      showPopup: function (popupName, args) {
        var componentsArray, i;
        if (OB.MobileApp.view.$[popupName]) {
          OB.MobileApp.view.$[popupName].show(args);
          return true;
        }
        componentsArray = this.getComponents();
        for (i = 0; i < componentsArray.length; i++) {
          if (componentsArray[i].$[popupName]) {
            componentsArray[i].$[popupName].show(args);
            break;
          }
        }
        return true;
      },
      hidePopupHandler: function (inSender, inEvent) {
        if (inEvent.popup) {
          this.hidePopup(inEvent.popup, inEvent.args);
        }
      },
      hidePopup: function (popupName, args) {
        var componentsArray, i;
        if (OB.MobileApp.view.$[popupName]) {
          OB.MobileApp.view.$[popupName].hide(args);
          return true;
        }
        componentsArray = this.getComponents();
        for (i = 0; i < componentsArray.length; i++) {
          if (componentsArray[i].$[popupName]) {
            componentsArray[i].$[popupName].hide(args);
            break;
          }
        }
        return true;
      }
    }]
  }, {
    name: 'dialogsContainer'
  }, {
    name: 'alertContainer',
    components: [{
      kind: 'OB.UTIL.showAlert',
      name: 'alertQueue'
    }]
  }, {
    name: 'confirmationContainer'
  }, {
    kind: 'OB.UI.Profile.SessionInfo',
    name: 'profileSessionInfo'
  }, {
    kind: 'OB.UI.Profile.UserInfo',
    name: 'profileUserInfo'
  }, {
    // To globaly handle keypup events when not using focusKeeper
    kind: enyo.Signals,
    onkeyup: "keypressHandler"
  }],

  /**
   * When using focus keeper for barcode scanners, keep always the focus there
   */
  keeperBlur: function (inSender, inEvent) {
    OB.MobileApp.view.scanningFocus(this.scanMode);
  },

  /**
   * Key up on focus keeper. We are using key up event because keydown doesn't work
   * properly on Android Chrome 26
   */
  keeperKey: function (k) {
    OB.MobileApp.view.waterfall('onGlobalKeypress', {
      keyboardEvent: k
    });
  },

  /**
   * Checks whether focus should be places on focusKeeper and puts it there if needed
   */
  scanningFocus: function (scanMode, forced) {
    var keeper;
    if (!OB.MobileApp.model.get('useBarcode')) {
      return;
    }

    if (scanMode !== undefined) {
      this.scanMode = scanMode;
    }

    // This shouldn't be done in iOS, if done the focuskeeper loses the focus
    if (this.scanMode && navigator.userAgent.match(/iPhone|iPad|iPod/i)) {
      keeper = document.getElementById('_focusKeeper');
      keeper.disabled = false;
      keeper.focus();
    }

    if (!this.scanMode && forced) {
      return;
    }

    // using timeout due problems in android stock browser because of 2 reasons:
    //   1. need to set the focus back to keeper even if not in scanMode, if not,
    //      when in a non scanMode tab using scanner writes in the URL tab. So 
    //      it is not keep in keeper only in case the destination element is a input
    //      to know the destination timeout is needed
    //   2. if timeout is not used, on-screen keyboard is shown when settin focus in
    //      keeper
    setTimeout(function () {
      if (this.scanMode || (document.activeElement.tagName !== 'INPUT' && document.activeElement.tagName !== 'SELECT')) {
        keeper = document.getElementById('_focusKeeper');
        keeper.focus();
      }
    }, 500);
  },

  /**
   * Global key up event. Used when not using focusKeeper
   */
  keypressHandler: function (inSender, inEvent) {
    if (OB.MobileApp.model.get('useBarcode') && this.scanMode) {
      return;
    }
    //Issue 25013. This flag is set by globalKeypressHandler function in ob-keyboard.js
    if (OB.MobileApp.model.get('useBarcode') && OB.MobileApp.keyPressProcessed) {
      delete OB.MobileApp.keyPressProcessed;
      return;
    }

    this.waterfall('onGlobalKeypress', {
      keyboardEvent: inEvent
    });
  },

  resizeWindow: function () {
    var winHeight = window.innerHeight,
        winWidth = window.innerWidth,
        percentage, appHeight = 700;


    // resize in case of window rotation but not if virtual keyboard appears
    // hack: virtual keyboard is detected because only y is resized but not x
    if (!this.sizeProperties || (this.sizeProperties.x !== winWidth && this.sizeProperties.y !== winHeight)) {
      //When the window is resized we need to close the menu
      //to avoid a strange problem when rotating mobile devices
      //See issue https://issues.openbravo.com/view.php?id=23669
      this.waterfall('onDestroyMenu');
      this.sizeProperties = {
        x: window.innerWidth,
        y: window.innerHeight
      };

      percentage = window.innerHeight * 100 / appHeight;
      percentage = Math.floor(percentage) / 100;
      document.body.style.zoom = percentage;

      // after zooming, force render again
      // if this is not done, iPad's Safari messes the layout when going from 1 to 2 columns
      this.render();
    }
  },

  rendered: function () {
    var keeper;
    this.inherited(arguments);

    // Creating focus keeper. This needs to be done in this way instead of using enyo components
    // because currently, it is not possible to place focus (in iOS)
    keeper = document.getElementById('_focusKeeper');
    if (!keeper) {
      keeper = document.createElement('input');
      keeper.setAttribute('id', '_focusKeeper');
      keeper.setAttribute('style', 'position: fixed; top: -100px; left: -100px;');
      keeper.setAttribute('onblur', 'OB.MobileApp.view.keeperBlur()');
      // Fixed 25288: key up is not working fine in IOS7. Because of that we are
      // listening to keypress and then sending it to the standard flow.
      if (OB.UTIL.isIOS()) {
        keeper.addEventListener('keypress', function (k) {
          if (k.keyCode === 13) {
            OB.MobileApp.view.keeperKey(k);
          }
        });
      }
      keeper.addEventListener('keyup', function (k) {
        if (OB.UTIL.isIOS() && k.keyCode === 13) {
          return;
        }
        OB.MobileApp.view.keeperKey(k);
      });
      document.body.appendChild(keeper);
    }
    this.scanningFocus();
  },

  initComponents: function () {
    window.OB = OB || {};
    window.OB.MobileApp = OB.MobileApp || {};
    window.OB.MobileApp.view = this;

    var args = arguments,
        initialized = false;

    //this.inherited(args);
    OB.MobileApp.model.initializeCommonComponents();
    this.terminal.on('initializedCommonComponents', function () {
      var me = this;

      if (!initialized) {
        this.inherited(args);
        initialized = true;

        this.terminal.on('change:context', function () {
          var ctx = this.terminal.get('context');
          if (!ctx) {
            return;
          }
          if (this.$.dialogsContainer.$.profileDialog) {
            this.$.dialogsContainer.$.profileDialog.destroy();
          }
          if (OB.UI.ModalProfile) {
            this.$.dialogsContainer.createComponent({
              kind: 'OB.UI.ModalProfile',
              name: 'profileDialog'
            });
          }
        }, this);

        this.terminal.on('change:connectedToERP', function (model) {
          if (model.get('loggedOffline') !== undefined) {
            if (model.get('connectedToERP')) {
              //We will do an additional request to verify whether the connection is still active or not
              new OB.DS.Request('org.openbravo.mobile.core.login.Context').exec({}, function (inResponse) {
                if (inResponse && !inResponse.exception) {
                  OB.MobileApp.model.returnToOnline();
                } else {
                  if (model.get('supportsOffline')) {
                    if (!OB.MobileApp.view.$.dialogsContainer.$.modalOnline) {
                      OB.MobileApp.view.$.dialogsContainer.createComponent({
                        kind: 'OB.UI.ModalOnline',
                        name: 'modalOnline'
                      }).render();
                    }
                    OB.MobileApp.view.$.dialogsContainer.$.modalOnline.show();
                  } else {
                    OB.MobileApp.model.triggerLogout();
                  }
                }
              });
            } else {
              if (model.get('supportsOffline')) {
                OB.UTIL.showWarning(OB.I18N.getLabel('OBMOBC_OfflineModeWarning'));
              }
            }
          }
        }, this);

        // TODO: POS specific, move it to somewhere else
        this.terminal.on('change:terminal change:bplocation change:location change:pricelist change:pricelistversion', function () {
          var name = '';
          var clientname = '';
          var orgname = '';
          var pricelistname = '';
          var currencyname = '';
          var locationname = '';

          if (this.terminal.get('terminal')) {
            name = this.terminal.get('terminal')._identifier;
            clientname = this.terminal.get('terminal')['client' + OB.Constants.FIELDSEPARATOR + '_identifier'];
            orgname = this.terminal.get('terminal')['organization' + OB.Constants.FIELDSEPARATOR + '_identifier'];
          }
          if (this.terminal.get('pricelist')) {
            pricelistname = this.terminal.get('pricelist')._identifier;
            currencyname = this.terminal.get('pricelist')['currency' + OB.Constants.FIELDSEPARATOR + '_identifier'];
          }
          if (this.terminal.get('location')) {
            locationname = this.terminal.get('location')._identifier;
          }

          // TODO: POS specific widget: move it
          //        this.$.terminal.setContent(name);
          //        this.$.yourcompany.setContent(orgname);
          //        this.$.yourcompanyproperties.destroyComponents();
          //        this.$.yourcompanyproperties.createComponent({
          //          kind: 'OB.UI.Terminal.CompanyWidget',
          //          clientName: clientname,
          //          orgName: orgname,
          //          priceName: pricelistname,
          //          currencyName: currencyname,
          //          locationName: locationname
          //        }).render();
        }, this);
      }
      this.$.dialogsContainer.destroyComponents();
      if (OB.UI.ModalLogout) {
        this.$.dialogsContainer.createComponent({
          kind: 'OB.UI.ModalLogout',
          name: 'logoutDialog'
        });
      }

      this.render();

      this.resizeWindow();
      window.onresize = function () {
        me.resizeWindow();
      };

      // forced trigger to show online status
      this.terminal.trigger('change:connectedToERP', this.terminal);
    }, this);
  }
});