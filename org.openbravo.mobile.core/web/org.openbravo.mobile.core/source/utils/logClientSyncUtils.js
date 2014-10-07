/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B,_*/

(function () {

  OB = window.OB || {};
  OB.UTILS = window.OB.UTILS || {};

  OB.UTIL.processLogClientAll = function () {
    // Processes log client
    var me = this,
        criteria = {};

    if (OB.MobileApp.model.get('connectedToERP')) {
      OB.Dal.find(OB.Model.LogClient, criteria, function (logClientsNotProcessed) {
        var successCallback, errorCallback, lastrecord;
        if (!logClientsNotProcessed || logClientsNotProcessed.length === 0) {
          return;
        }
        errorCallback = function () {
          return;
        };
        OB.UTIL.processLogClients(logClientsNotProcessed, null, errorCallback);
      });
    }
  };

  OB.UTIL.processLogClientClass = 'org.openbravo.mobile.core.utils.LogClientLoader';
  OB.UTIL.processLogClients = function (logClients, successCallback, errorCallback) {
    var logClientsToJson = [];
    logClients.each(function (logClient) {
      logClientsToJson.push(logClient.serializeToJSON());
      OB.Dal.remove(logClient, null, function (tx, err) {
        OB.UTIL.showError(err);
      });
    });

    this.proc = new OB.DS.Process(OB.UTIL.processLogClientClass);
    if (OB.MobileApp.model.get('connectedToERP')) {
      this.proc.exec({
        logclient: logClientsToJson
      }, function (data, message) {
        if (data && data.exception) {
          if (errorCallback) {
            errorCallback();
          }
        } else {
          if (successCallback) {
            successCallback();
          }
        }
      }, null, null, 4000);
    }
  };
}());