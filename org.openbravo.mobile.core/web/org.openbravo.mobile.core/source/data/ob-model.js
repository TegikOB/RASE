/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */


OB = OB || {};
OB.Data = OB.Data || {};
OB.Model = OB.Model || {};
OB.Collection = OB.Collection || {};


OB.Data.BaseModel = Backbone.Model.extend();

enyo.kind({
  name: 'OB.Data.Registry',
  kind: enyo.Object,
  statics: {
    registerModel: function (modelParam) {
      var modelName, model;
      if (typeof modelParam === 'string') {
        // modelParam is the name of the entity
        modelName = modelParam;
        model = OB.Data.BaseModel.extend({
          modelName: modelName,
          source: '../../org.openbravo.service.datasource/' + modelName,
          online: true
        });
      } else if (modelParam.prototype.generatedStructure) {
        var url = '../../org.openbravo.client.kernel/OBMOBC_Main/ClientModel';
        url += '?entity=' + modelParam.prototype.entityName;
        url += '&modelName=' + modelParam.prototype.modelName;
        url += '&source=' + modelParam.prototype.source;

        new enyo.Ajax({
          url: url,
          method: 'GET',
          handleAs: 'text',
          cacheBust: false
        }).response(function (inSender, inResponse) {
          eval(inResponse);
          modelParam.prototype.structureLoaded = true;
        }).go();

        return;
      } else {
        // modelParam is a Backbone class
        modelName = modelParam.prototype.modelName;
        model = modelParam;
      }
      if (!modelName) {
        window.error('Error registering model: no modelName provided', modelParam);
      }
      OB.Model[modelName] = model;

      OB.Collection[modelName + 'List'] = Backbone.Collection.extend({
        model: OB.Model[modelName]
      });
    }
  }
});

OB.Data.ExtensibleModel = Backbone.Model.extend({}, {
  // class properties
  propertyList: [],
  indexList: [],

  addProperties: function (properties) {

    // TODO: add validations
    this.propertyList = this.propertyList.concat(properties);
  },

  addIndex: function (index) {
    this.indexList = this.indexList.concat(index);
  },

  getProperties: function (options) {
    var props = _.clone(this.propertyList);
    if (this.hasFilter()) {
      props.push({
        column: '_filter',
        type: 'TEXT'
      });
    }
    if (options && options.skipIdx) {
      return props;
    }
    props.push({
      name: '_idx',
      column: '_idx',
      type: 'NUMERIC'
    });
    return props;
  },

  getPropertiesForUpdate: function (options) {
    //all the properties except primary key and _idx
    var props = [];
    _.each(this.propertyList, function (property) {
      if (!property.primaryKey) {
        props.push(property);
      }
    });
    if (this.hasFilter()) {
      props.push({
        column: '_filter',
        type: 'TEXT'
      });
    }
    return props;
  },

  getTableName: function () {
    return this.prototype.tableName;
  },

  isOnlineModel: function () {
    return this.prototype.online;
  },

  isTerminalDateIncluded: function () {
    return this.prototype.includeTerminalDate;
  },

  getDropStatement: function () {
    return 'DROP TABLE IF EXISTS ' + this.getTableName();
  },

  getCreateStatement: function () {
    return 'CREATE TABLE IF NOT EXISTS ' + this.getTableName() + '\n' + this.getSQLColumns(true);
  },

  getCreateIndexStatement: function (indexToCreate) {
    if (indexToCreate && indexToCreate.columns) {
      var indexedColumn = [];
      _.each(indexToCreate.columns, function (col) {
        var sort = 'asc';
        if (col.sort) {
          sort = col.sort;
        }
        indexedColumn.push(col.name + ' ' + sort);
      });
      return 'CREATE INDEX IF NOT EXISTS ' + indexToCreate.name + ' ON ' + this.getTableName() + ' (' + indexedColumn.join(',') + ')';
    } else {
      OB.warn('A correct object is needed to create indexes');
      return '';
    }
  },

  getInsertStatement: function () {
    var i, statement = 'INSERT INTO ' + this.getTableName() + '\n' + this.getSQLColumns() + '\nVALUES\n(';
    for (i = 0; i < this.getProperties().length; i++) {
      statement += (i === 0 ? '' : ', ') + '?';
    }
    return statement + ')';
  },

  getUpdateStatement: function () {
    var i, j = 0,
        statement = 'UPDATE ' + this.getTableName() + '\nSET\n ';
    for (i = 0; i < this.getPropertiesForUpdate().length; i++) {
      if (!this.getPropertiesForUpdate()[i].primaryKey) {
        statement += (j === 0 ? '' : ', ');
        statement += this.getPropertiesForUpdate()[i].column + ' = ?';
        j += 1;
      }
    }
    statement += '\n WHERE ' + this.getPrimaryKey().column + ' = ?';
    return statement;
  },

  getDeleteByIdStatement: function () {
    var id;

    if (this.deleteByIdStatement) {
      // keep it cached not to compute it every time
      return this.deleteByIdStatement;
    }

    id = this.getPrimaryKey();
    //    _.find(this.getProperties(), function (p) {
    //      return p.primaryKey
    //    });
    this.deleteByIdStatement = "DELETE FROM " + this.getTableName() + " WHERE " + id.column + " = ?";
    return this.deleteByIdStatement;
  },

  getPrimaryKey: function () {
    var pk = null;
    pk = _.find(this.getProperties(), function (p) {
      return p.primaryKey;
    });
    return pk;
  },

  getFilterProperties: function () {
    var filterProps = [];

    _.each(_.filter(this.propertyList, function (p) {
      return p.filter;
    }), function (p) {
      filterProps.push(p.name);
    });
    return filterProps;
  },

  getIndexes: function () {
    var indexedProps = [];
    if (this.indexList && this.indexList.length > 0) {
      _.each(this.indexList, function (p) {
        indexedProps.push(p);
      });
      return indexedProps;
    } else {
      return [];
    }
  },

  hasFilter: function () {
    return this.getFilterProperties().length > 0;
  },

  hasIndex: function () {
    return this.getIndexes().length > 0;
  },

  getSQLColumns: function (includeType) {
    var columns = '';
    _.each(this.getProperties(), function (p) {
      if (columns !== '') {
        columns += ',\n';
      }
      columns += p.column;
      if (includeType) {
        columns += ' ' + p.type + (p.primaryKey ? ' PRIMARY KEY' : '');
      }
    });
    return '(' + columns + ')';
  }
});