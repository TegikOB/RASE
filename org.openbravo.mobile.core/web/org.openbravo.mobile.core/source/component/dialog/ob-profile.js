/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */

enyo.kind({
  name: 'OB.UI.ModalProfile',
  kind: 'OB.UI.ModalAction',

  handlers: {
    onSelectRole: 'roleSelected',
    onSelectOrganization: 'orgSelected'
  },

  bodyContent: {
    style: 'background-color: #ffffff;',
    components: [{
      components: [{
        classes: 'properties-label',
        components: [{
          name: 'roleLbl',
          style: 'padding: 5px 8px 0px 0px; font-size: 15px;'
        }]
      }, {
        kind: 'OB.UI.ModalProfile.Role',
        classes: 'properties-field',
        name: 'roleList'
      }]
    }, {
      style: 'clear: both'
    },

    {
      name: 'orgSelector',
      components: [{
        classes: 'properties-label',
        components: [{
          style: 'padding: 5px 8px 0px 0px; font-size: 15px;',
          name: 'orgLbl'
        }]
      }, {
        kind: 'OB.UI.ModalProfile.Organization',
        classes: 'properties-field',
        name: 'orgList'
      }]
    },

    {
      style: 'clear: both'
    }, {
      name: 'warehouseSelector',
      components: [{
        classes: 'properties-label',
        components: [{
          style: 'padding: 5px 8px 0px 0px; font-size: 15px;',
          name: 'warehouseLbl'
        }]
      }, {
        components: [{
          kind: 'OB.UI.List',
          name: 'warehouseList',
          tag: 'select',
          classes: 'modal-dialog-profile-combo',
          renderEmpty: enyo.Control,
          renderLine: enyo.kind({
            kind: 'enyo.Option',
            initComponents: function () {
              this.inherited(arguments);
              this.setValue(this.model.get('id'));
              this.setContent(this.model.get('_identifier'));
              if (OB.MobileApp.model.get('context') && OB.MobileApp.model.get('context').user && OB.MobileApp.model.get('context').user.defaultWarehouse && this.model.get('id') === OB.MobileApp.model.get('context').user.defaultWarehouse) {
                this.parent.setSelected(this.parent.children.length - 1);
              }
            }
          })
        }]
      }]
    }, {
      style: 'clear: both'
    }, {
      components: [{
        classes: 'properties-label',
        components: [{
          style: 'padding: 5px 8px 0px 0px; font-size: 15px;',
          name: 'langLbl'
        }]
      }, {
        name: 'lang',
        components: [{
          kind: 'OB.UI.List',
          name: 'langList',
          tag: 'select',
          classes: 'modal-dialog-profile-combo',
          renderEmpty: enyo.Control,
          renderLine: enyo.kind({
            kind: 'enyo.Option',
            initComponents: function () {
              this.inherited(arguments);
              this.setValue(this.model.get('id'));
              this.setContent(this.model.get('_identifier'));
              if (OB.Application && this.model.get('id') === OB.Application.language) {
                this.parent.setSelected(this.parent.children.length - 1);
              }
            }
          })
        }]
      }]
    }, {
      style: 'clear: both'
    }, {
      components: [{
        classes: 'properties-label',
        components: [{
          style: 'padding: 5px 8px 0px 0px; font-size: 15px;',
          name: 'setAsDefaultLbl'
        }]
      }, {
        components: [{
          classes: 'modal-dialog-profile-checkbox',
          components: [{
            kind: 'OB.UI.CheckboxButton',
            name: 'defaultBox',
            classes: 'modal-dialog-btn-check'
          }]
        }]
      }]
    }, {
      style: 'clear: both'
    }]
  },
  bodyButtons: {
    components: [{
      style: 'clear: both'
    }, {
      kind: 'OB.UI.ProfileDialogApply'
    }, {
      kind: 'OB.UI.ProfileDialogCancel',
      name: 'profileCancelButton'
    }]
  },

  roleSelected: function (inSender, inEvent) {
    // This code can be used to replace _.filter when underscore is upgraded to 1.4.4
    //    this.selectedRoleOptions = _.findWhere(this.availableRoles, {
    //      id: inEvent.newRoleId
    //    });
    this.selectedRoleOptions = _.filter(this.availableRoles, function (role) {
      return role.id === inEvent.newRoleId;
    })[0];

    this.organizations.reset(this.selectedRoleOptions.organizationValueMap);
    if (this.$.bodyContent) {
      this.$.bodyContent.$.orgList.changeOrganization(); // force redraw of warehouse list
    }
  },

  orgSelected: function (inSender, inEvent) {
    // This code can be used to replace _.filter when underscore is upgraded to 1.4.4
    //    var whMap = _.findWhere(this.selectedRoleOptions.warehouseOrgMap, {
    //      orgId: inEvent.newOrgId
    //    }).warehouseMap;
    var whMap = _.filter(this.selectedRoleOptions.warehouseOrgMap, function (orgMap) {
      return orgMap.orgId === inEvent.newOrgId;
    })[0].warehouseMap;

    this.warehouses.reset(whMap);
  },

  initComponents: function () {
    var proc, languages = new Backbone.Collection(),
        options, profileHandler = OB.MobileApp.model.get('profileHandlerUrl') || 'org.openbravo.mobile.core.login.ProfileUtils';

    proc = new OB.DS.Process(profileHandler);

    this.inherited(arguments);

    this.roles = new Backbone.Collection();
    this.organizations = new Backbone.Collection();
    this.warehouses = new Backbone.Collection();

    this.$.bodyContent.$.roleList.setCollection(this.roles);
    this.$.bodyContent.$.orgList.setCollection(this.organizations);
    this.$.bodyContent.$.warehouseList.setCollection(this.warehouses);
    this.$.bodyContent.$.langList.setCollection(languages);

    proc.exec({}, enyo.bind(this, function (response) {
      if (response.exception) {
        // we are offline...
        return;
      }
      this.availableRoles = response.role.roles;
      this.roles.reset(response.role.valueMap);
      if (this.$.bodyContent) {
        this.$.bodyContent.$.roleList.changeRole(); // force redraw of org list
      }
      languages.reset(response.language.valueMap);
    }));

    this.ctx = OB.MobileApp.model.get('context');

    // labels
    this.$.bodyContent.$.roleLbl.setContent(OB.I18N.getLabel('OBMOBC_Role'));
    this.$.bodyContent.$.orgLbl.setContent(OB.I18N.getLabel('OBMOBC_Org'));
    this.$.bodyContent.$.warehouseLbl.setContent(OB.I18N.getLabel('OBMOBC_Warehouse'));
    this.$.bodyContent.$.langLbl.setContent(OB.I18N.getLabel('OBMOBC_Language'));
    this.$.bodyContent.$.setAsDefaultLbl.setContent(OB.I18N.getLabel('OBMOBC_SetAsDefault'));


    options = OB.MobileApp.model.get('profileOptions');
    if (options) {
      this.$.bodyContent.$.orgSelector.setShowing(options.showOrganization);
      this.$.bodyContent.$.warehouseSelector.setShowing(options.showWarehouse);
    }
    this.setHeader(OB.I18N.getLabel('OBMOBC_ProfileDialogTitle'));
  },
  show: function () {
    this.inherited(arguments);
    // Focus will be set on a button to avoid a redraw which will make
    // the focuskeeper visible
    this.$.bodyButtons.$.profileCancelButton.focus();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ProfileDialogApply',
  isActive: true,
  isDefaultAction: true,
  tap: function () {
    var ajaxRequest, widgetForm = this.owner.owner.$.bodyContent.$,
        options = OB.MobileApp.model.get('profileOptions'),
        newLanguageId = widgetForm.langList.getValue(),
        newRoleId = widgetForm.roleList.getValue(),
        isDefault = widgetForm.defaultBox.checked,
        actionURL = '../../org.openbravo.client.kernel?command=save&_action=org.openbravo.client.application.navigationbarcomponents.UserInfoWidgetActionHandler',
        postData = {
        language: newLanguageId,
        role: newRoleId,
        'default': isDefault
        };

    if (!this.isActive) {
      return;
    }
    this.isActive = false;

    if (options) {
      if (options.showOrganization) {
        postData.organization = widgetForm.orgList.getValue();
      }
      if (options.showWarehouse) {
        postData.warehouse = widgetForm.warehouseList.getValue();
      }
      if (isDefault) {
        postData.defaultProperties = options.defaultProperties;
      }
    }

    window.localStorage.setItem('POSlanguageId', newLanguageId);
    ajaxRequest = new enyo.Ajax({
      url: actionURL,
      cacheBust: false,
      method: 'POST',
      handleAs: 'json',
      contentType: 'application/json;charset=utf-8',
      data: JSON.stringify(postData),
      success: function (inSender, inResponse) {
        if (inResponse.result === 'success') {
          window.location.reload();
        } else {
          OB.UTIL.showError(inResponse.result);
        }
        this.isActive = true;
      },
      fail: function (inSender, inResponse) {
        OB.UTIL.showError(inResponse);
        this.isActive = true;
      }
    });
    ajaxRequest.go(ajaxRequest.data).response('success').error('fail');
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBMOBC_LblApply'));
  }

});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ProfileDialogCancel',
  tap: function () {
    this.doHideThisPopup();
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
  }
});

enyo.kind({
  name: 'OB.UI.ModalProfile.Role',
  kind: 'OB.UI.List',
  classes: 'modal-dialog-profile-combo',

  events: {
    onSelectRole: ''
  },

  handlers: {
    onchange: 'changeRole'
  },

  renderEmpty: enyo.Control,
  renderLine: enyo.kind({
    kind: 'enyo.Option',
    initComponents: function () {
      this.inherited(arguments);
      this.setValue(this.model.get('id'));
      this.setContent(this.model.get('_identifier'));
      if (OB.MobileApp.model.get('context') && OB.MobileApp.model.get('context').role && this.model.get('id') === OB.MobileApp.model.get('context').role.id) {
        this.parent.setSelected(this.parent.children.length - 1);
      }
    }
  }),

  changeRole: function (inSender, inEvent) {
    this.doSelectRole({
      newRoleId: this.children[this.getSelected()].getValue()
    });
  }
});

enyo.kind({
  name: 'OB.UI.ModalProfile.Organization',
  kind: 'OB.UI.List',
  classes: 'modal-dialog-profile-combo',

  events: {
    onSelectOrganization: ''
  },

  handlers: {
    onchange: 'changeOrganization'
  },

  renderEmpty: enyo.Control,
  renderLine: enyo.kind({
    kind: 'enyo.Option',
    initComponents: function () {
      this.inherited(arguments);
      this.setValue(this.model.get('id'));
      this.setContent(this.model.get('_identifier'));
      if (OB.MobileApp.model.get('context') && OB.MobileApp.model.get('context').organization && this.model.get('id') === OB.MobileApp.model.get('context').organization.id) {
        this.parent.setSelected(this.parent.children.length - 1);
      }
    }
  }),

  changeOrganization: function (inSender, inEvent) {
    this.doSelectOrganization({
      newOrgId: this.children[this.getSelected()].getValue()
    });
  }
});

enyo.kind({
  name: 'OB.UI.Profile.SessionInfo',
  kind: 'OB.UI.Popup',
  classes: 'modal',
  components: [{
    classes: 'widget-profile-title widget-profile-title-connection',
    name: 'connStatus'
  }, {
    style: 'height: 5px;'
  }, {
    kind: 'OB.UI.MenuAction',
    name: 'lockOption',
    allowHtml: true,
    tap: function () {
      this.owner.hide();
      OB.MobileApp.model.lock();
    }
  }, {
    style: 'height: 5px;'
  }, {
    kind: 'OB.UI.MenuAction',
    name: 'endSessionButton',
    i18nLabel: 'OBMOBC_EndSession',
    tap: function () {
      var me = this;
      if (this.owner.args && (this.owner.args.model.get('order').get('lines').length > 0 || this.owner.args.model.get('orderList').length > 1)) {
        this.owner.hide();
        OB.UTIL.Approval.requestApproval(
        this.owner.args.model, 'OBPOS_approval.removereceipts', function (approved, supervisor, approvalType) {
          if (approved) {
            me.owner.hide();
            OB.MobileApp.view.$.dialogsContainer.$.logoutDialog.show();
          }
        });
      } else {
        this.owner.hide();
        OB.MobileApp.view.$.dialogsContainer.$.logoutDialog.show();
      }
    }
  }, {
    style: 'height: 5px;'
  }],

  setConnectionStatus: function () {
    var connected = OB.MobileApp.model.get('connectedToERP');
    this.$.connStatus.setContent(OB.I18N.getLabel(connected ? 'OBMOBC_Online' : 'OBMOBC_Offline'));
    this.$.connStatus.addRemoveClass('onlineicon', connected);
    this.$.connStatus.addRemoveClass('offlineicon', !connected);
  },

  initComponents: function () {
    this.inherited(arguments);
    this.$.lockOption.setShowing(OB.MobileApp.model.get('supportsOffline'));
    this.$.lockOption.setLabel(OB.I18N.getLabel('OBMOBC_LogoutDialogLock') + '<span style="padding-left:190px">0\u21B5</span>');

    OB.MobileApp.model.on('change:connectedToERP', function () {
      this.setConnectionStatus();
    }, this);
    this.setConnectionStatus();

  }
});

enyo.kind({
  name: 'OB.UI.Profile.UserInfo',
  kind: 'OB.UI.Popup',
  classes: 'modal',
  components: [{
    name: 'userWidget',
    kind: 'OB.UI.Profile.UserWidget'
  }, {
    style: 'height: 5px;'
  }, {
    kind: 'OB.UI.MenuAction',
    i18nLabel: 'OBMOBC_Profile',
    name: 'profileButton',
    tap: function () {
      this.owner.hide(); // Manual dropdown menu closure
      OB.MobileApp.view.$.dialogsContainer.$.profileDialog.show();
    }
  }],

  show: function () {
    this.$.profileButton.setShowing(OB.MobileApp.model.hasPermission('OBMOBC_ChangeProfile'));
    this.inherited(arguments);
  },

  initComponents: function () {
    this.inherited(arguments);
    OB.MobileApp.model.on('change:context', function () {
      var ctx = OB.MobileApp.model.get('context');
      if (!ctx) {
        return;
      }
      this.$.userWidget.setUserName(ctx.user._identifier);
      this.$.userWidget.setUserRole(ctx.role._identifier);
      this.$.userWidget.setUserImg(ctx.img);
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.Profile.UserWidget',
  style: 'width: 100%',
  published: {
    userName: '',
    userRole: '',
    userImg: ''
  },
  components: [{
    //style: 'height: 60px; background-color: #FFF899;',
    classes: 'widget-profile-title ',
    components: [{
      style: 'float: left; width: 55px; margin: -15px 0px 0px 6px;',
      components: [{
        kind: 'OB.UI.Thumbnail',
        'default': '../org.openbravo.mobile.core/assets/img/anonymous-icon.png',
        name: 'img'
      }]
    }, {
      style: 'float: left; margin: 6px 0px 0px 0px; line-height: 150%;',
      components: [{
        components: [{
          components: [{
            tag: 'span',
            style: 'font-weight: 800; margin: 0px 0px 0px 5px;',
            name: 'username'
          }]
        }]
      }]
    }, {
      style: 'float: left; margin: 6px 0px 0px 0px; line-height: 150%;',
      components: [{
        components: [{
          components: [{
            tag: 'span',
            style: 'font-weight: 800; margin: 0px 0px 0px 5px;',
            content: '-'
          }]
        }]
      }]
    }, {
      style: 'float: left; margin: 6px 0px 0px 0px; line-height: 150%;',
      components: [{
        components: [{
          components: [{
            tag: 'span',
            style: 'font-weight: 800; margin: 0px 0px 0px 5px;',
            name: 'role'
          }]
        }]
      }]
    }]
  }],

  userNameChanged: function () {
    this.$.username.setContent(this.userName);
  },

  userRoleChanged: function () {
    this.$.role.setContent(this.userRole);
  },

  userImgChanged: function () {
    this.$.img.setImg(this.userImg);
  }
});