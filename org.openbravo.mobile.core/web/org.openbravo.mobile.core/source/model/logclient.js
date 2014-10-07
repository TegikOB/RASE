/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone */

(function () {

  var LogClient = OB.Data.ExtensibleModel.extend({
    modelName: 'LogClient',
    tableName: 'obmobc_logclient',
    entityName: 'LogClient',
    source: 'org.openbravo.mobile.core.master.LogClient',
    local: true,
    createStatement: 'CREATE TABLE IF NOT EXISTS obmobc_logclient (obmobc_logclient_id TEXT PRIMARY KEY, deviceId TEXT,  msg TEXT, json CLOB, created TEXT, createdby TEXT)',
    dropStatement: 'DROP TABLE IF EXISTS obmobc_logclient',
    insertStatement: 'INSERT INTO obmobc_logclient (obmobc_logclient_id, deviceId, msg, json, created, createdby) VALUES (?,?,?,?,?,?)',
    serializeToJSON: function () {
      return JSON.parse(JSON.stringify(this.toJSON()));
    }
  });


  LogClient.addProperties([{
    name: 'id',
    column: 'obmobc_logclient_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'deviceId',
    column: 'deviceId',
    type: 'TEXT'
  }, {
    name: 'msg',
    column: 'msg',
    type: 'TEXT'
  }, {
    name: 'json',
    column: 'json',
    type: 'CLOB'
  }, {
    name: 'created',
    column: 'created',
    type: 'TEXT'
  }, {
    name: 'createdby',
    column: 'createdby',
    type: 'TEXT'
  }]);


  OB.Data.Registry.registerModel(LogClient);
}());