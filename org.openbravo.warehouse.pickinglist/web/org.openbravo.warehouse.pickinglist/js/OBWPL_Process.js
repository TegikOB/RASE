/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012-2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
OB.OBWPL = OB.OBWPL || {};
OB.OBWPL.Process = {
  create: function (params, view) {
    var i, selection = params.button.contextView.viewGrid.getSelectedRecords(),
        orders = [],
        callback;
    callback = function (rpcResponse, data, rpcRequest) {
      var message = data.message;
      view.activeView.messageBar.setMessage(isc.OBMessageBar[message.severity], message.title, message.text);
      // close process to refresh current view
      params.button.closeProcessPopup();
    };
    for (i = 0; i < selection.length; i++) {
      orders.push(selection[i].id);
    }

    isc.CheckIsGroupPopup.create({
      orders: orders,
      view: view,
      params: params
    }).show();
  },

  validate: function (params, view) {
    var recordId = params.button.contextView.viewGrid.getSelectedRecords()[0].id,
        processOwnerView = view.getProcessOwnerView(params.processId),
        processAction, callback;

    processAction = function () {
      OB.OBWPL.Process.process(params, view);
    };
    callback = function (rpcResponse, data, rpcRequest) {
      var processLayout, popupTitle;
      if (!data) {
        view.activeView.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, null);
        return;
      }
      if (data.message || !data.data) {
        view.activeView.messageBar.setMessage(isc.OBMessageBar[data.message.severity], null, data.message.text);
        return;
      }
      popupTitle = OB.I18N.getLabel('OBWPL_PickingList') + ' - ' + params.button.contextView.viewGrid.getSelectedRecords()[0]._identifier;
      processLayout = isc.OBPickValidateProcess.create({
        parentWindow: view,
        sourceView: view.activeView,
        buttonOwnerView: processOwnerView,
        pickGridData: data.data,
        processAction: processAction
      });
      view.openPopupInTab(processLayout, popupTitle, OB.Styles.OBWPL.PickValidateProcess.popupWidth, OB.Styles.OBWPL.PickValidateProcess.popupHeight, true, true, true, true);
    };
    OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.ValidateActionHandler', {
      recordId: recordId,
      action: 'validate'
    }, {}, callback);
  },

  cancel: function (params, view) {
    var i, selection = params.button.contextView.viewGrid.getSelectedRecords(),
        pickinglist = [],
        callback;
    callback = function (rpcResponse, data, rpcRequest) {
      var message = data.message;
      view.activeView.messageBar.setMessage(isc.OBMessageBar[message.severity], message.title, message.text);
      // close process to refresh current view
      params.button.closeProcessPopup();
    };
    for (i = 0; i < selection.length; i++) {
      pickinglist.push(selection[i].id);
    }
    isc.confirm(OB.I18N.getLabel('OBWPL_CancelConfirm'), function (clickedOK) {
      if (clickedOK) {
        OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.CancelActionHandler', {
          pickinglist: pickinglist,
          action: 'cancel'
        }, {}, callback);
      }
    });
  },

  process: function (params, view) {
    var i, selection = params.button.contextView.viewGrid.getSelectedRecords(),
        pickinglist = [],
        callback;
    callback = function (rpcResponse, data, rpcRequest) {
      var message = data.message;
      view.activeView.messageBar.setMessage(isc.OBMessageBar[message.severity], message.title, message.text);
      // close process to refresh current view
      params.button.closeProcessPopup();
    };
    for (i = 0; i < selection.length; i++) {
      pickinglist.push(selection[i].id);
    }
    isc.confirm(OB.I18N.getLabel('OBWPL_ProcessConfirm'), function (clickedOK) {
      if (clickedOK) {
        OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.ProcessActionHandler', {
          pickinglist: pickinglist,
          action: 'process'
        }, {}, callback);
      }
    });
  }
};

isc.defineClass('CheckIsGroupPopup', isc.OBPopup);

isc.CheckIsGroupPopup.addProperties({

  width: 320,
  height: 200,
  title: null,
  showMinimizeButton: false,
  showMaximizeButton: false,

  //Form
  mainform: null,

  //Button
  okButton: null,
  cancelButton: null,

  getActionList: function (form) {
    var send = {
      orders: this.orders
    },
        actionField, popup = this;
    send.action = 'getcheck';
    OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.CreateActionHandler', send, {}, function (response, data, request) {
      if (response) {
        actionField = form.getField('Action');
        if (response.data) {
          actionField.setValueMap(response.data.valuecheck.valueMap);
          actionField.setDefaultValue(response.data.valuecheck.defaultValue);
        }
      }
    });
  },

  initWidget: function () {

    var orders = this.orders,
        originalView = this.view,
        params = this.params;

    this.mainform = isc.DynamicForm.create({
      numCols: 2,
      colWidths: ['50%', '50%'],
      fields: [{
        name: 'Action',
        height: 20,
        width: 255,
        required: true,
        type: '_id_17',
        defaultToFirstOption: true
      }]
    });
    this.setTitle(OB.I18N.getLabel('OBWPL_CreatePL'));

    this.okButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE'),
      popup: this,
      action: function () {
        var callback, action;

        callback = function (rpcResponse, data, rpcRequest) {
          var status = rpcResponse.status,
              view = rpcRequest.clientContext.originalView.getView(rpcResponse.clientContext.originalView.tabId);
          if (data.message) {
            view.messageBar.setMessage(isc.OBMessageBar[data.message.severity], null, data.message.text);

          }
          rpcRequest.clientContext.popup.closeClick();
          rpcRequest.clientContext.originalView.refresh(false, false);
        };

        action = this.popup.mainform.getItem('Action').getValue();

        OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.CreateActionHandler', {
          orders: orders,
          action: 'create',

          checkvalue: action
        }, {}, callback, {
          originalView: this.popup.view,
          popup: this.popup
        });
      }
    });

    this.cancelButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
      popup: this,
      action: function () {
        this.popup.closeClick();
      }
    });

    this.getActionList(this.mainform);

    this.items = [
    isc.VLayout.create({
      defaultLayoutAlign: "center",
      align: "center",
      width: "100%",
      layoutMargin: 10,
      membersMargin: 6,
      members: [
      isc.HLayout.create({
        defaultLayoutAlign: "center",
        align: "center",
        layoutMargin: 30,
        membersMargin: 6,
        members: this.mainform
      }), isc.HLayout.create({
        defaultLayoutAlign: "center",
        align: "center",
        membersMargin: 10,
        members: [this.okButton, this.cancelButton]
      })]
    })];

    this.Super('initWidget', arguments);
  }

});