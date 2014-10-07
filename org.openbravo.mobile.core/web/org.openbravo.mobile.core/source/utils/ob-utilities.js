/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, console, Backbone, $, _, enyo */

(function () {

  OB = window.OB || {};
  OB.UTIL = window.OB.UTIL || {};

  OB.UTIL.getParameterByName = function (name) {
    var n = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
    var regexS = '[\\?&]' + n + '=([^&#]*)';
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.search);
    return (results) ? decodeURIComponent(results[1].replace(/\+/g, ' ')) : '';
  };

  OB.UTIL.escapeRegExp = function (text) {
    return text.replace(/[\-\[\]{}()+?.,\\\^$|#\s]/g, '\\$&');
  };

  function S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1).toUpperCase();
  }

  OB.UTIL.get_UUID = function () {
    return (S4() + S4() + S4() + S4() + S4() + S4() + S4() + S4());
  };

  OB.UTIL.padNumber = function (n, p) {
    var s = n.toString();
    while (s.length < p) {
      s = '0' + s;
    }
    return s;
  };

  OB.UTIL.encodeXMLComponent = function (s, title, type) {
    return s.replace(/\&/g, '&amp;').replace(/</g, '&lt;').replace(/\>/g, '&gt;').replace(/\'/g, '&apos;').replace(/\"/g, '&quot;');
  };

  OB.UTIL.decodeXMLComponent = function (s) {
    return s.replace(/\&amp\;/g, '&').replace(/\&lt\;/g, '<').replace(/\&gt\;/g, '>').replace(/\&apos\;/g, '\'').replace(/\&quot\;/g, '\"');
  };

  /**
   * Prepares multi line string to be printed with HW Manager format
   */
  OB.UTIL.encodeXMLMultiLineComponent = function (str, width) {
    var startBlock = '<line><text>',
        endBlock = '</text></line>\n',
        lines = str.split('\n'),
        l, line, result = '';


    for (l = 0; l < lines.length; l++) {
      line = lines[l].trim();
      if (width && line.length > width) {
        result += OB.UTIL.encodeXMLMultiLineComponent(OB.UTIL.wordwrap(line, width));
      } else {
        result += startBlock + OB.UTIL.encodeXMLComponent(line) + endBlock;
      }
    }

    return result;
  };

  /**
   * Wraps words in several lines with width length
   */
  OB.UTIL.wordwrap = function (str, width) {
    if (!str || !width) {
      return str;
    }

    return str.match(RegExp('.{1,' + width + '}(\\s|$)|\\S+?(\\s|$)', 'g')).join('\n');
  };


  OB.UTIL.loadResource = function (res, callback, context) {
    var ajaxRequest = new enyo.Ajax({
      url: res,
      cacheBust: false,
      method: 'GET',
      handleAs: 'text',
      contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
      success: function (inSender, inResponse) {
        callback.call(context || this, inResponse);
      },
      fail: function (inSender, inResponse) {
        callback.call(context || this);
      }
    });
    ajaxRequest.go().response('success').error('fail');
  };

  OB.UTIL.queueStatus = function (queue) {
    // Expects an object where the value element is true/false depending if is processed or not
    if (!_.isObject(queue)) {
      throw 'Object expected';
    }
    return _.reduce(queue, function (memo, val) {
      return memo && val;
    }, true);
  };

  OB.UTIL.checkContextChange = function (oldContext, newContext, successCallback) {
    if (newContext.userId !== oldContext.user.id || newContext.orgId !== oldContext.organization.id || newContext.clientId !== oldContext.client.id || newContext.roleId !== oldContext.role.id) {
      OB.UTIL.showConfirmation.display(OB.I18N.getLabel('OBMOBC_ContextChanged'), OB.I18N.getLabel('OBMOBC_ContextChangedMessage'), [{
        isConfirmButton: true,
        label: OB.I18N.getLabel('OBMOBC_LblOk'),
        action: function () {
          OB.POS.modelterminal.logout();
          return true;
        }
      }]);
    } else {
      if (successCallback) {
        successCallback();
      }
    }
  };

  OB.UTIL.checkConnectivityStatus = function (connectedCallback, notConnectedCallback) {
    var ajaxParams, currentlyConnected = OB.MobileApp.model.get('connectedToERP');
    var oldContext = OB.MobileApp.model.get('context');
    if (currentlyConnected && oldContext) {
      new OB.DS.Request('org.openbravo.mobile.core.login.ContextInformation').exec({
        terminal: OB.MobileApp.model.get('terminalName'),
        ignoreForConnectionStatus: true
      }, function (data) {
        var newContext;
        if (data && data.exception) {
          OB.MobileApp.model.logout();
        }
        if (data[0]) {
          newContext = data[0];
          OB.UTIL.checkContextChange(oldContext, newContext, connectedCallback);
        }
      }, function () {
        if (OB.MobileApp.model && OB.MobileApp.model.get('connectedToERP')) {
          OB.MobileApp.model.triggerOffLine();
        }
        if (notConnectedCallback) {
          notConnectedCallback();
        }
      });
      return;
    } else if (navigator.onLine) {
      // It can be a false positive, make sure with the ping
      var ajaxRequest = new enyo.Ajax({
        url: '../../security/SessionActive?id=0',
        cacheBust: true,
        timeout: 5000,
        method: 'GET',
        handleAs: 'json',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        success: function (inSender, inResponse) {
          if (currentlyConnected !== true) {
            if (OB.MobileApp.model) {
              OB.MobileApp.model.triggerOnLine();
            }
          }
          if (connectedCallback) {
            connectedCallback();
          }
        },
        fail: function (inSender, inResponse) {
          if (currentlyConnected !== false) {
            if (OB.MobileApp.model) {
              OB.MobileApp.model.triggerOffLine();
            }
          }
          if (notConnectedCallback) {
            notConnectedCallback();
          }
        }
      });
      ajaxRequest.go().response('success').error('fail');
    } else {
      if (currentlyConnected) {
        if (OB.MobileApp.model) {
          OB.MobileApp.model.triggerOffLine();
        }
      }
    }
  };

  OB.UTIL.updateDocumentSequenceInDB = function (documentNo) {
    var docSeqModel, criteria = {
      'posSearchKey': OB.MobileApp.model.get('terminal').searchKey
    };
    OB.Dal.find(OB.Model.DocumentSequence, criteria, function (documentSequenceList) {
      var posDocumentNoPrefix = OB.MobileApp.model.get('terminal').docNoPrefix,
          orderDocumentSequence = parseInt(documentNo.substr(posDocumentNoPrefix.length + 1), 10) + 1,
          docSeqModel;
      if (documentSequenceList && documentSequenceList.length !== 0) {
        docSeqModel = documentSequenceList.at(0);
        if (orderDocumentSequence > docSeqModel.get('documentSequence')) {
          docSeqModel.set('documentSequence', orderDocumentSequence);
        }
      } else {
        docSeqModel = new OB.Model.DocumentSequence();
        docSeqModel.set('posSearchKey', OB.MobileApp.model.get('terminal').searchKey);
        docSeqModel.set('documentSequence', orderDocumentSequence);
      }
      OB.Dal.save(docSeqModel, null, null);
    });
  };

  OB.UTIL.isWritableOrganization = function (orgId) {
    if (OB.MobileApp.model.get('writableOrganizations')) {
      var result = false;
      result = _.find(OB.MobileApp.model.get('writableOrganizations'), function (curOrg) {
        if (orgId === curOrg) {
          return true;
        }
      });
      if (result) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  };

  OB.UTIL.getCharacteristicValues = function (characteristicDescripcion) {
    var ch_desc = '';
    _.each(characteristicDescripcion.split(','), function (character, index, collection) {
      ch_desc = ch_desc + character.substring(character.indexOf(':') + 2);
      if (index !== collection.length - 1) {
        ch_desc = ch_desc + ', ';
      }
    }, this);
    return ch_desc;
  };

  //Returns true if the value is null or undefined
  OB.UTIL.isNullOrUndefined = function (value) {
    if (_.isNull(value) || _.isUndefined(value)) {
      return true;
    }
    return false;
  };

  //Returns the first Not null and not undefined value.
  //Can be used in the same way as we use a = b || c; But using this function 0 will be a valid value for b
  OB.UTIL.getFirstValidValue = function (valuesToCheck) {
    var valueToReturn = _.find(valuesToCheck, function (value) {
      if (!OB.UTIL.isNullOrUndefined(value)) {
        return true;
      }
    });
    return valueToReturn;
  };

  OB.trace = function () {
    try {
      var msg = OB.UTIL.composeMessage(arguments);

      if (OB.UTIL.checkPermissionLog("Trace", "save")) {
        OB.UTIL.saveLogClient(msg, "Trace");
      }
      if (OB.UTIL.checkPermissionLog("Trace", "console")) {
        console.log.apply(console, OB.UTIL.argumentsWithLink(arguments));
      }
    } catch (e) {
      console.log.apply(console, arguments);
    }
  };

  OB.debug = function () {
    try {
      var msg = OB.UTIL.composeMessage(arguments);

      if (OB.UTIL.checkPermissionLog("Debug", "save")) {
        OB.UTIL.saveLogClient(msg, "Debug");
      }
      if (OB.UTIL.checkPermissionLog("Debug", "console")) {
        console.log.apply(console, OB.UTIL.argumentsWithLink(arguments));
      }
    } catch (e) {
      console.log.apply(console, arguments);
    }
  };

  OB.info = function () {
    try {
      var msg = OB.UTIL.composeMessage(arguments);

      if (OB.UTIL.checkPermissionLog("Info", "save")) {
        OB.UTIL.saveLogClient(msg, "Info");
      }
      if (OB.UTIL.checkPermissionLog("Info", "console")) {
        console.info.apply(console, OB.UTIL.argumentsWithLink(arguments));
      }
    } catch (e) {
      console.info.apply(console, arguments);
    }
  };

  OB.warn = function () {
    try {
      var msg = OB.UTIL.composeMessage(arguments);

      if (OB.UTIL.checkPermissionLog("Warn", "save")) {
        OB.UTIL.saveLogClient(msg, "Warn");
      }
      if (OB.UTIL.checkPermissionLog("Warn", "console")) {
        console.warn.apply(console, OB.UTIL.argumentsWithLink(arguments));
      }
    } catch (e) {
      console.warn.apply(console, arguments);
    }
  };

  OB.error = function () {
    try {
      var msg = OB.UTIL.composeMessage(arguments);

      if (OB.UTIL.checkPermissionLog("Error", "save")) {
        OB.UTIL.saveLogClient(msg, "Error");
      }
      if (OB.UTIL.checkPermissionLog("Error", "console")) {
        console.error.apply(console, OB.UTIL.argumentsWithLink(arguments));
      }
    } catch (e) {
      console.error.apply(console, arguments);
    }
  };

  OB.critical = function () {
    try {
      var msg = OB.UTIL.composeMessage(arguments);

      if (OB.UTIL.checkPermissionLog("Critical", "save")) {
        OB.UTIL.saveLogClient(msg, "Critical");
      }
      if (OB.UTIL.checkPermissionLog("Critical", "console")) {
        console.error.apply(console, OB.UTIL.argumentsWithLink(arguments));
      }
    } catch (e) {
      console.error.apply(console, arguments);
    }
  };

  // this function receive as arguments first the level of log, the rest of the arguments are the message
  OB.log = function () {
    var level, argsWithoutFirst, i, msg;
    try {
      level = arguments[0];
      argsWithoutFirst = [];
      for (i = 1; i < arguments.length; i++) {
        argsWithoutFirst[i - 1] = arguments[i];
      }

      msg = OB.UTIL.composeMessage(argsWithoutFirst);

      if (OB.UTIL.checkPermissionLog(level, "save")) {
        OB.UTIL.saveLogClient(msg, level);
      }
      if (OB.UTIL.checkPermissionLog(level, "console")) {
        if (level === "Info") {
          console.info.apply(console, OB.UTIL.argumentsWithLink(argsWithoutFirst));
        } else if (level === "Warn") {
          console.warn.apply(console, OB.UTIL.argumentsWithLink(argsWithoutFirst));
        } else if (level === "Error" || level === "Critical") {
          console.error.apply(console, OB.UTIL.argumentsWithLink(argsWithoutFirst));
        } else {
          console.log.apply(console, OB.UTIL.argumentsWithLink(argsWithoutFirst));
        }
      }
    } catch (e) {
      console.error.apply(console, arguments);
    }
  };

  OB.UTIL.saveLogClient = function (msg, level) {
    try {
      if (OB.POS && OB.POS.modelterminal && OB.MobileApp.model.supportLogClient()) {
        var date, json, logClientModel = new OB.Model.LogClient();
        date = new Date();

        logClientModel.set('obmobc_logclient_id', OB.UTIL.get_UUID());
        logClientModel.set('created', date.getTime());
        logClientModel.set('createdby', OB.POS.modelterminal.get('orgUserId'));
        logClientModel.set('loglevel', level);
        logClientModel.set('msg', msg);
        logClientModel.set('deviceId', OB.MobileApp.model.get('logConfiguration').deviceIdentifier);
        logClientModel.set('link', OB.UTIL.getStackLink());

        _.each(OB.MobileApp.model.get('logConfiguration').logPropertiesExtension, function (f) {
          logClientModel.set(f());
        });

        logClientModel.set('json', JSON.stringify(logClientModel.toJSON()));
        OB.Dal.save(logClientModel, null, null);
      }
    } catch (e) {
      return;
    }
  };

  OB.UTIL.composeMessage = function (args) {
    var msg = '';
    _.each(args, function (arg) {
      if (msg !== '') {
        msg += '   ';
      }
      if (!(_.isNull(arg) || _.isUndefined(arg))) {
        // comprobar si el arg es >1000. Si es mayor de 10000 -> hacer substring
        msg += ((_.isObject(arg) && JSON.stringify(arg).length < 1000) ? JSON.stringify(arg) : JSON.stringify(arg).substring(0, 1000));
      }
    });
    return msg;
  };

  // it checks if the log should be saved in backend (property 'OBMOBC_logClient.saveLog') and the level saved (property 'OBMOBC_logClient.levelLog')
  // or the level of log should be displayed in console (property 'OBMOBC_logClient.consoleLevelLog')
  // "type "will be "save" to check if the log should be saved in backend or "console" to check if the log should be displayed in console
  OB.UTIL.checkPermissionLog = function (level, type) {
    try {
      if ((OB.MobileApp && OB.MobileApp.model && OB.MobileApp.model.get('permissions') !== null) === false) {
        return true;
      }

      if (type === "save") {
        if (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.saveLog'] === false) {
          return false;
        }
        if (level === "Trace") {
          return (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Trace');
        }
        if (level === "Debug") {
          return (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Trace' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Debug');
        }
        if (level === "Info") {
          return (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Trace' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Debug' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Info');
        }
        if (level === "Warn") {
          return (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Trace' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Debug' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Info' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Warn');
        }
        if (level === "Error") {
          return (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Trace' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Debug' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Info' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Warn' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Error');
        }
        if (level === "Critical") {
          return (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Trace' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Debug' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Info' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Warn' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Error' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.levelLog'] === 'Critical');
        }
      } else {
        if (level === "Trace") {
          return (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Trace');
        }
        if (level === "Debug") {
          return (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Trace' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Debug');
        }
        if (level === "Info") {
          return (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Trace' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Debug' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Info');
        }
        if (level === "Warn") {
          return (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Trace' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Debug' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Info' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Warn');
        }
        if (level === "Error") {
          return (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Trace' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Debug' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Info' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Warn' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Error');
        }
        if (level === "Critical") {
          return (OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Trace' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Debug' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Info' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Warn' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Error' || OB.MobileApp.model.get('permissions')['OBMOBC_logClient.consoleLevelLog'] === 'Critical');
        }
      }
      return true;
    } catch (e) {
      return true;
    }
  };

  OB.UTIL.getStackLink = function () {
    try {
      var errorobj = new Error();
      var link = errorobj.stack.split('\n')[4].split('(')[1];
      link = link.substring(0, link.length - 2);
      return link;
    } catch (e) {
      return '';
    }
  };

  OB.UTIL.argumentsWithLink = function (args) {
    var arrayArgs, i;
    try {
      arrayArgs = [];
      for (i = 0; i < args.length; i++) {
        arrayArgs.push(args[i]);
      }
      arrayArgs.push(OB.UTIL.getStackLink());
      return arrayArgs;
    } catch (e) {
      return '';
    }
  };

  OB.UTIL.isIOS = function () {
    return navigator.userAgent.match(/iPhone|iPad|iPod/i);
  };
}());
