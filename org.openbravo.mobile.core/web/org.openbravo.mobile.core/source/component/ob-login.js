/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

OB = window.OB || {};
OB.UTIL = window.OB.UTIL || {};
OB.OBPOSLogin = window.OB.OBPOSLogin || {};
OB.OBPOSLogin.UI = window.OB.OBPOSLogin.UI || {};

enyo.kind({
  name: 'OB.OBPOSLogin.UI.UserButton',
  classes: 'login-user-button',
  user: null,
  userImage: null,
  userConnected: null,
  showConnectionStatus: true,
  events: {
    onUserImgClick: ''
  },
  components: [{
    classes: 'login-user-button-bottom',
    components: [{
      name: 'bottomIcon',
      classes: 'login-user-button-bottom-icon',
      content: ['.']
    }, {
      name: 'bottomText',
      classes: 'login-user-button-bottom-text'
    }]
  }],
  tap: function () {
    this.doUserImgClick();
  },
  create: function () {
    var me = this;
    this.inherited(arguments);

    if (this.userImage && this.userImage !== 'none') {
      this.applyStyle('background-image', 'url(' + this.userImage + ')');
    }

    if (!this.showConnectionStatus) {
      this.$.bottomIcon.hide();
    }

    if (this.showConnectionStatus && this.userConnected === 'true') {
      this.$.bottomIcon.applyStyle('background-image', 'url(../org.openbravo.mobile.core/assets/img/iconOnlineUser.png)');
    }

    if (this.showConnectionStatus && OB.MobileApp.model.get('supportsOffline')) {
      OB.Dal.initCache(OB.Model.User, [], null, null);
      OB.Dal.find(OB.Model.User, {
        'name': this.user
      }, function (users) {
        var user;
        if (users.models.length !== 0) {
          user = users.models[0];
          OB.Dal.find(OB.Model.Session, {
            'user': user.get('id')
          }, function (sessions) {
            var session;
            if (sessions.models.length !== 0) {
              session = sessions.models[0];
              if (session.get('active') === 'Y') {
                me.$.bottomIcon.applyStyle('background-image', 'url(../org.openbravo.mobile.core/assets/img/iconAwayUser.png)');
              } else {
                me.$.bottomIcon.applyStyle('background-image', 'url(../org.openbravo.mobile.core/assets/img/iconOfflineUser.png)');
              }
            }
          }, function () {
            OB.error(arguments);
          });
        }
      }, function () {
        OB.error(arguments);
      });
    }

    if (this.user) {
      this.$.bottomText.setContent(this.user);
    }
  }
});

enyo.kind({
  name: 'OB.OBPOSLogin.UI.LoginButton',
  kind: 'OB.UI.ModalDialogButton',
  style: 'min-width: 115px;',
  id: 'loginaction',
  initComponents: function () {
    this.inherited(arguments);
    this.content = OB.I18N.getLabel('OBMOBC_LoginButton');
  }
});

enyo.kind({
  kind: 'enyo.Ajax',
  name: 'OB.OBPOSLogin.UI.LoginRequest',
  url: '../../org.openbravo.retail.posterminal.service.loginutils',
  method: 'GET',
  handleAs: 'json',
  contentType: 'application/json;charset=utf-8'
});

enyo.kind({
  name: 'OB.OBPOSLogin.UI.Login',
  tag: 'section',
  components: [{
    kind: 'OB.UI.ModalInfo',
    name: 'DatabaseDialog',
    header: 'DB version change',
    // TODO: OB.I18N.getLabel('OBPOS_DatabaseVersionChange'),
    bodyContent: {
      content: 'DB version change'
      // TODO:OB.I18N.getLabel('OBPOS_DatabaseVersionChangeLong')
    }
  }, {
    classes: 'login-header-row',
    components: [{
      name: 'loginHeaderCompany',
      classes: 'login-header-company'
    }, {
      classes: 'login-header-caption',
      name: 'appCaption'
    }, {
      classes: 'login-header-ob',
      name: 'appName'
    }]
  }, {
    style: 'height: 600px',
    components: [{
      classes: 'span6',
      components: [{
        kind: 'Scroller',
        thumb: true,
        horizontal: 'hidden',
        name: 'loginUserContainer',
        classes: 'login-user-container',
        content: ['.']
      }]
    }, {
      classes: 'span6',
      components: [{
        classes: 'login-inputs-container',
        components: [{
          name: 'loginInputs',
          classes: 'login-inputs-browser-compatible',
          components: [{
            components: [{
              classes: 'login-status-info',
              style: 'float: left;',
              name: 'connectStatus'
            }, {
              classes: 'login-status-info',
              name: 'screenLockedLbl'
            }]
          }, {
            components: [{
              kind: 'enyo.Input',
              type: 'text',
              name: 'username',
              classes: 'input-login',
              onkeydown: 'inputKeydownHandler'
            }]
          }, {
            components: [{
              kind: 'enyo.Input',
              type: 'password',
              name: 'password',
              classes: 'input-login',
              onkeydown: 'inputKeydownHandler'
            }]
          }, {
            classes: 'login-inputs-loginbutton',
            components: [{
              kind: 'OB.OBPOSLogin.UI.LoginButton',
              ontap: 'loginButtonAction'
            }]
          }]
        }, {
          name: 'loginBrowserNotSupported',
          style: 'display: none;',
          components: [{
            components: [{
              name: 'LoginBrowserNotSupported_Title_Lbl',
              classes: 'login-browsernotsupported-title'
            }]
          }, {
            components: [{
              classes: 'span8',
              components: [{
                name: 'LoginBrowserNotSupported_P1_Lbl',
                classes: 'login-browsernotsupported-content'
              }, {
                name: 'LoginBrowserNotSupported_P2_Lbl',
                classes: 'login-browsernotsupported-content'
              }, {
                name: 'LoginBrowserNotSupported_P3_Lbl',
                classes: 'login-browsernotsupported-content'
              }]
            }, {
              classes: 'span4',
              style: 'padding: 78px 0px 0px 0px',
              components: [{
                kind: 'OB.UI.Clock',
                classes: 'login-clock'
              }]
            }]
          }]
        }]
      }]
    }]
  }],

  setConnectedStatus: function () {
    var me = this;
    if (!this.$.connectStatus) {
      // don't try to set it, when it is not already built
      return;
    }
    OB.UTIL.checkConnectivityStatus(function () {
      if (me && me.$ && me.$.connectStatus) {
        me.$.connectStatus.setContent(OB.I18N.getLabel('OBMOBC_Online'));
      }
    }, function () {
      if (me && me.$ && me.$.connectStatus) {
        me.$.connectStatus.setContent(OB.I18N.getLabel('OBMOBC_Offline'));
      }
    })

  },

  initComponents: function () {
    this.inherited(arguments);

    this.$.screenLockedLbl.setContent(OB.I18N.getLabel('OBMOBC_LoginScreenLocked'));
    this.$.username.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginUserInput');
    this.$.password.attributes.placeholder = OB.I18N.getLabel('OBMOBC_LoginPasswordInput');

    this.$.LoginBrowserNotSupported_Title_Lbl.setContent(OB.I18N.getLabel('OBMOBC_LoginBrowserNotSupported'));
    this.$.LoginBrowserNotSupported_P1_Lbl.setContent(OB.I18N.getLabel('OBMOBC_LoginBrowserNotSupported_P1'));
    this.$.LoginBrowserNotSupported_P2_Lbl.setContent(OB.I18N.getLabel('OBMOBC_LoginBrowserNotSupported_P2', ['Chrome, Safari, Safari (iOS)', 'Android']));
    this.$.LoginBrowserNotSupported_P3_Lbl.setContent(OB.I18N.getLabel('OBMOBC_LoginBrowserNotSupported_P3'));

    this.$.appName.setContent(OB.MobileApp.model.get('appDisplayName'));
    this.$.appCaption.setContent(OB.appCaption || '');

    this.setConnectedStatus();

    OB.MobileApp.model.on('change:connectedToERP', function () {
      if (OB.MobileApp.model.get('supportsOffline')) {
        this.setConnectedStatus();
      }
    }, this);


    this.postRenderActions();
  },

  handlers: {
    onUserImgClick: 'handleUserImgClick'
  },

  handleUserImgClick: function (inSender, inEvent) {
    var u = inEvent.originator.user;
    this.$.username.setValue(u);
    this.$.password.setValue('');
    this.$.password.focus();
    return true;
  },

  inputKeydownHandler: function (inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    if (keyCode === 13) { //Handle ENTER key
      this.loginButtonAction();
      return true;
    }
    return false;
  },

  loginButtonAction: function () {
    var u = this.$.username.getValue(),
        p = this.$.password.getValue();
    if (!u || !p) {
      alert('Please enter your username and password');
    } else {
      OB.MobileApp.model.login(u, p);
    }
  },

  setCompanyLogo: function (inSender, inResponse) {
    if (!inResponse.logoUrl) {
      return;
    }
    if (this.$.loginHeaderCompany) {
      this.$.loginHeaderCompany.applyStyle('background-image', 'url(' + inResponse.logoUrl + ')');
    }
    return true;
  },

  setUserImages: function (inSender, inResponse) {
    var name = [],
        userName = [],
        image = [],
        connected = [],
        target = this.$.loginUserContainer,
        me = this,
        jsonImgData, i;
    if (!target) {
      return;
    }
    if (!inResponse.data) {
      OB.Dal.find(OB.Model.User, {}, function (users) {
        var i, user, session;
        for (i = 0; i < users.models.length; i++) {
          user = users.models[i];
          name.push(user.get('name'));
          userName.push(user.get('name'));
          connected.push(false);
        }
        me.renderUserButtons(name, userName, image, connected, target);
      }, function () {
        OB.error(arguments);
      });
      return true;
    }
    jsonImgData = inResponse.data;
    enyo.forEach(jsonImgData, function (v) {
      name.push(v.name);
      userName.push(v.userName);
      image.push(v.image);
      connected.push(v.connected);
    });
    this.renderUserButtons(name, userName, image, connected, target);
  },

  renderUserButtons: function (name, userName, image, connected, target) {
    var i;
    for (i = 0; i < name.length; i++) {
      target.createComponent({
        kind: 'OB.OBPOSLogin.UI.UserButton',
        user: userName[i],
        userImage: image[i],
        userConnected: connected[i]
      });
    }
    target.render();
    OB.UTIL.showLoading(false);
    return true;
  },

  postRenderActions: function () {
    var params;
    OB.UTIL.showLoggingOut(false);
    OB.MobileApp.model.on('loginfail', function (status, data) {
      var msg;
      if (data && data.messageTitle) {
        msg = data.messageTitle;
      }

      if (data && data.messageText) {
        msg += (msg ? '\n' : '') + data.messageText;
      }
      msg = msg || 'Invalid user name or password.\nPlease try again.';
      alert(msg);
      if (this.$.password) {
        this.$.password.setValue('');
      }
      if (this.$.username) {
        this.$.username.focus();
      }
    }, this);
    OB.MobileApp.model.on('loginUserImgPressfail', function (status) {
      //If the user image press (try to login with default password) fails, then no alert is shown and the focus goes directly to the password input
      if (this.$.password) {
        this.$.password.setValue('');
        this.$.password.focus();
      }
    }, this);

    if (!OB.UTIL.isSupportedBrowser()) { //If the browser is not supported, show message and finish.
      this.$.loginInputs.setStyle('display: none');
      this.$.loginBrowserNotSupported.setStyle('display: block');
      OB.UTIL.showLoading(false);
      return true;
    }

    params = OB.MobileApp.model.get('loginUtilsParams') || {};
    params.appName = OB.MobileApp.model.get('appName');

    params.command = 'companyLogo';
    new OB.OBPOSLogin.UI.LoginRequest({
      url: OB.MobileApp.model.get('loginUtilsUrl')
    }).response(this, 'setCompanyLogo').go(params);

    params.command = 'userImages';
    new OB.OBPOSLogin.UI.LoginRequest({
      url: OB.MobileApp.model.get('loginUtilsUrl')
    }).response(this, 'setUserImages').go(params);

    this.$.username.focus();
  }

});