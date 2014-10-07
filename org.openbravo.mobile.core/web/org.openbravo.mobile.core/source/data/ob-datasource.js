/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, Backbone, _, enyo */

(function () {
  OB = window.OB || {};
  OB.DS = window.OB.DS || {};

  OB.DS.MAXSIZE = 100;

  function serviceSuccess(inResponse, callback) {
    if (inResponse._entityname) {
      callback([inResponse]);
    } else {
      var response = inResponse.response;
      var status = response.status;
      if (response.contextInfo && OB.MobileApp.model.get('context')) {
        OB.UTIL.checkContextChange(OB.MobileApp.model.get('context'), response.contextInfo);
      }
      if (status === 0) {
        callback(response.data, response.message, response.lastUpdated);
      } else if (response.errors) {
        if (OB.MobileApp.model.get('isLoggingIn')) {
          OB.MobileApp.model.set("datasourceLoadFailed", true);
          OB.UTIL.showConfirmation.display('Error', response.errors.substring(0, 400), [{
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            action: function () {
              OB.MobileApp.model.logout();
              OB.UTIL.showLoading(true);
            }
          }], {
            onHideFunction: function () {
              OB.UTIL.showLoading(true);
              OB.MobileApp.model.logout();
            }
          });
        }
        OB.error(response);
        callback({
          exception: {
            message: response.errors.id,
            status: response
          }
        });
      } else {
        OB.error(response);
        OB.UTIL.showLoading(false);
        if (OB.MobileApp.model.get('isLoggingIn')) {
          OB.MobileApp.model.set("datasourceLoadFailed", true);
          OB.UTIL.showConfirmation.display('Error', response.error.message.substring(0, 400), [{
            label: OB.I18N.getLabel('OBMOBC_LblOk'),
            action: function () {
              OB.MobileApp.model.logout();
              OB.UTIL.showLoading(true);
            }
          }], {
            onHideFunction: function () {
              OB.UTIL.showLoading(true);
              OB.MobileApp.model.logout();
            }
          });
        }
        if (response.error && response.error.message) {
          callback({
            exception: {
              message: response.error.message,
              status: response
            }
          });
        } else {
          callback({
            exception: {
              message: 'Unknow error',
              status: response
            }
          });
        }
      }
    }
  }

  function serviceError(inResponse, callback, callbackError) {
    if (callbackError) {
      callbackError({
        exception: {
          message: OB.I18N.getLabel('OBPOS_MsgApplicationServerNotAvailable'),
          status: inResponse
        }
      });
    } else {
      callback({
        exception: {
          message: OB.I18N.getLabel('OBPOS_MsgApplicationServerNotAvailable'),
          status: inResponse
        }
      });
    }
  }

  function servicePOST(source, dataparams, callback, callbackError, async, timeout) {
    if (async !== false) {
      async = true;
    }
    var ajaxRequest = new enyo.Ajax({
      url: '../../org.openbravo.mobile.core.service.jsonrest/' + source,
      cacheBust: false,
      sync: !async,
      timeout: timeout,
      method: 'POST',
      handleAs: 'json',
      contentType: 'application/json;charset=utf-8',
      ignoreForConnectionStatus: dataparams.parameters && dataparams.parameters.ignoreForConnectionStatus ? dataparams.parameters.ignoreForConnectionStatus : false,
      data: JSON.stringify(dataparams),
      success: function (inSender, inResponse) {
        if (this.processHasFailed) {
          return;
        }
        serviceSuccess(inResponse, callback);
      },
      fail: function (inSender, inResponse) {
        this.processHasFailed = true;
        inResponse = inResponse || {};
        if (inSender && inSender === "timeout") {
          inResponse.timeout = true;
        }
        if (!this.ignoreForConnectionStatus) {
          OB.MobileApp.model.triggerOffLine();
        }
        serviceError(inResponse, callback, callbackError);
      }
    });
    ajaxRequest.go(ajaxRequest.data).response('success').error('fail');
  }

  function serviceGET(source, dataparams, callback, callbackError, async) {
    if (async !== false) {
      async = true;
    }
    var ajaxRequest = new enyo.Ajax({
      url: '../../org.openbravo.mobile.core.service.jsonrest/' + source + '/' + encodeURI(JSON.stringify(dataparams)),
      cacheBust: false,
      sync: !async,
      method: 'GET',
      handleAs: 'json',
      ignoreForConnectionStatus: dataparams.parameters && dataparams.parameters.ignoreForConnectionStatus ? dataparams.parameters.ignoreForConnectionStatus : false,
      contentType: 'application/json;charset=utf-8',
      success: function (inSender, inResponse) {
        serviceSuccess(inResponse, callback);
      },
      fail: function (inSender, inResponse) {
        if (!this.ignoreForConnectionStatus) {
          OB.MobileApp.model.triggerOffLine();
        }

        serviceError(inResponse, callback, callbackError);
      }
    });
    ajaxRequest.go().response('success').error('fail');
  }

  // Process object
  OB.DS.Process = function (source) {
    this.source = source;
  };

  OB.DS.Process.prototype.exec = function (params, callback, callbackError, async, timeout) {
    var data = {},
        i, attr;

    for (attr in params) {
      if (params.hasOwnProperty(attr)) {
        data[attr] = params[attr];
      }
    }

    if (OB.DS.commonParams) {
      for (i in OB.DS.commonParams) {
        if (OB.DS.commonParams.hasOwnProperty(i)) {
          data[i] = OB.DS.commonParams[i];
        }
      }
    }

    data.appName = OB.MobileApp.model.get('appName') || 'OBMOBC';

    servicePOST(this.source, data, callback, callbackError, async, timeout);
  };

  // Source object
  OB.DS.Request = function (source, lastUpdated) {
    this.model = source && source.prototype && source.prototype.modelName && source; // we're using a Backbone.Model as source
    this.source = (this.model && this.model.prototype.source) || source; // we're using a plain String as source
    if (!this.source) {
      throw 'A Request must have a source';
    }

    this.lastUpdated = lastUpdated;
  };

  OB.DS.Request.prototype.exec = function (params, callback, callbackError, async) {
    var p, i;
    var data = {};

    if (params) {
      p = {};
      for (i in params) {
        if (params.hasOwnProperty(i)) {
          if (typeof params[i] === 'string') {
            p[i] = {
              value: params[i],
              type: 'string'
            };
          } else if (typeof params[i] === 'number') {
            if (params[i] === Math.round(params[i])) {
              p[i] = {
                value: params[i],
                type: 'long'
              };
            } else {
              p[i] = {
                value: params[i],
                type: 'bigdecimal'
              };
            }
          } else if (typeof params[i] === 'boolean') {
            p[i] = {
              value: params[i],
              type: 'boolean'
            };
          } else {
            p[i] = params[i];
          }
        }
      }
      data.parameters = p;
    }

    if (OB.DS.commonParams) {
      for (i in OB.DS.commonParams) {
        if (OB.DS.commonParams.hasOwnProperty(i)) {
          data[i] = OB.DS.commonParams[i];
        }
      }
    }
    if (this.lastUpdated) {
      data.lastUpdated = this.lastUpdated;
    }


    data.appName = OB.MobileApp.model.get('appName') || 'OBMOBC';

    serviceGET(this.source, data, callback, callbackError, async);
  };

  function check(elem, filter) {
    var p;

    for (p in filter) {
      if (filter.hasOwnProperty(p)) {
        if (typeof (filter[p]) === 'object') {
          return check(elem[p], filter[p]);
        } else {
          if (filter[p].substring(0, 2) === '%i') {
            if (!new RegExp(filter[p].substring(2), 'i').test(elem[p])) {
              return false;
            }
          } else if (filter[p].substring(0, 2) === '%%') {
            if (!new RegExp(filter[p].substring(2)).test(elem[p])) {
              return false;
            }
          } else if (filter[p] !== elem[p]) {
            return false;
          }
        }
      }
    }
    return true;
  }

  function findInData(data, filter) {
    var i, max;

    if ($.isEmptyObject(filter)) {
      return {
        exception: 'filter not defined'
      };
    } else {
      for (i = 0, max = data.length; i < max; i++) {
        if (check(data[i], filter)) {
          return data[i];
        }
      }
      return null;
    }
  }

  function execInData(data, filter, filterfunction) {
    var newdata, info, i, max, f, item;

    if ($.isEmptyObject(filter) && !filterfunction) {
      return {
        data: data.slice(0, OB.DS.MAXSIZE),
        info: (data.length > OB.DS.MAXSIZE ? 'OBPOS_DataMaxReached' : null)
      };
    } else {
      f = filterfunction ||
      function (item) {
        return item;
      };
      newdata = [];
      info = null;
      for (i = 0, max = data.length; i < max; i++) {
        if (check(data[i], filter)) {
          item = f(data[i]);
          if (item) {
            if (newdata.length >= OB.DS.MAXSIZE) {
              info = 'OBPOS_DataMaxReached';
              break;
            }
            newdata.push(data[i]);
          }
        }
      }
      return {
        data: newdata,
        info: info
      };
    }
  }

  // DataSource objects
  // OFFLINE GOES HERE
  OB.DS.DataSource = function (request) {
    this.request = request;
    this.cache = null;
  };
  _.extend(OB.DS.DataSource.prototype, Backbone.Events);

  OB.DS.DataSource.prototype.load = function (params, incremental) {
    var me = this;
    this.cache = null;

    this.request.exec(params, function (data, message, lastUpdated) {

      if (data.exception) {
        OB.error('Error in datasource', data);
        if (data.exception.message) {
          throw data.exception.message;
        } else {
          throw data.exception;
        }
      }

      if (lastUpdated) {
        window.localStorage.setItem('lastUpdatedTimestamp' + me.request.model.prototype.modelName, lastUpdated);
      }

      me.cache = data;

      if (me.request.model && !me.request.model.prototype.online) {
        OB.Dal.initCache(me.request.model, data, function () {
          me.trigger('ready');
        }, function () {
          OB.error(arguments, me.request.model.prototype);
        }, incremental);
      } else {
        me.trigger('ready');
      }
    });
  };

  OB.DS.DataSource.prototype.find = function (filter, callback) {
    if (this.cache) {
      callback(findInData(this.cache, filter));
    } else {
      this.on('ready', function () {
        callback(findInData(this.cache, filter));
      }, this);
    }
  };

  OB.DS.DataSource.prototype.exec = function (filter, callback) {
    if (this.cache) {
      var result1 = execInData(this.cache, filter);
      callback(result1.data, result1.info);
    } else {
      this.on('ready', function () {
        var result2 = execInData(this.cache, filter);
        callback(result2.data, result2.info);
      }, this);
    }
  };
}());