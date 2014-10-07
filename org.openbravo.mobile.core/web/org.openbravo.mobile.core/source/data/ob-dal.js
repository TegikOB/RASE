/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global define,_,console, Backbone, enyo, Uint8Array */

enyo.kind({
  name: 'OB.Dal',
  kind: enyo.Object,

  statics: {
    EQ: '=',
    NEQ: '!=',
    CONTAINS: 'contains',
    STARTSWITH: 'startsWith',
    ENDSWITH: 'endsWith',

    get_uuid: function () {
      var array;
      var uuid = "",
          i, digit = "";
      if (window.crypto && window.crypto.getRandomValues) {
        array = new Uint8Array(16);
        window.crypto.getRandomValues(array);

        for (i = 0; i < array.length; i++) {
          digit = array[i].toString(16).toUpperCase();
          if (digit.length === 1) {
            digit = "0" + digit;
          }
          uuid += digit;
        }

        return uuid;
      }

      function S4() {
        return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1).toUpperCase();
      }
      return (S4() + S4() + S4() + S4() + S4() + S4() + S4() + S4());
    },

    transform: function (model, obj) {
      var tmp = {},
          modelProto = model.prototype,
          val, properties;
      properties = model.getProperties ? model.getProperties() : modelProto.properties;
      _.each(properties, function (property) {
        var prop;
        if (_.isString(property)) {
          prop = property;
          val = obj[modelProto.propertyMap[property]];

        } else {
          prop = property.name;
          val = obj[property.column];
        }
        if (val === 'false') {
          tmp[prop] = false;
        } else if (val === 'true') {
          tmp[prop] = true;
        } else {
          tmp[prop] = val;
        }
      });
      return new model(tmp);
    },

    getWhereClause: function (criteria, propertyMap) {
      var appendWhere = true,
          firstParam = true,
          sql = '',
          params = [],
          res = {},
          orAnd = ' AND ';
      if (criteria && !_.isEmpty(criteria)) {
        if (criteria.obdalcriteriaType) {
          orAnd = ' ' + criteria.obdalcriteriaType + ' ';
          delete criteria.obdalcriteriaType;
        }
        _.each(_.keys(criteria), function (k) {

          var undef, colName, val = criteria[k],
              operator = (val !== undef && val !== null && val.operator !== undef) ? val.operator : '=',
              value = (val !== undef && val !== null && val.value !== undef) ? val.value : val;
          if (k !== '_orderByClause' && k !== '_limit') {
            if (appendWhere) {
              sql = sql + ' WHERE ';
              params = [];
              appendWhere = false;
            }

            if (_.isArray(propertyMap)) {
              if (k === '_filter') {
                colName = '_filter';
              } else {
                colName = _.find(propertyMap, function (p) {
                  return k === p.name;
                }).column;
              }
            } else {
              colName = propertyMap[k];
            }

            sql = sql + (firstParam ? '' : orAnd) + ' ' + colName + ' ';

            if (value === null) {
              sql = sql + ' IS null ';
            } else {

              if (operator === OB.Dal.EQ) {
                sql = sql + ' = ? ';
              } else if (operator === OB.Dal.NEQ) {
                sql = sql + ' != ? ';
              } else {
                sql = sql + ' like ? ';
              }

              if (operator === OB.Dal.CONTAINS) {
                value = '%' + value + '%';
              } else if (operator === OB.Dal.STARTSWITH) {
                value = value + '%';
              } else if (operator === OB.Dal.ENDSWITH) {
                value = value + '%';
              }
              params.push(value);
            }

            if (firstParam) {
              firstParam = false;
            }

          }

        });
      }
      res.sql = sql;
      res.params = params;
      return res;
    },

    getTableName: function (model) {
      return model.getTableName ? model.getTableName() : model.prototype.tableName;
    },

    getPropertyMap: function (model) {
      return model.getProperties ? model.getProperties() : model.prototype.propertyMap;
    },

    find: function (model, whereClause, success, error, args) {
      var tableName = OB.Dal.getTableName(model),
          propertyMap = OB.Dal.getPropertyMap(model),
          sql = 'SELECT * FROM ' + tableName,
          params = null,
          appendWhere = true,
          firstParam = true,
          k, v, undef, colType, xhr, i, criteria, j, params_where, orderBy, limit;

      if (model.prototype.online) {
        colType = OB && OB.Collection && OB.Collection[model.prototype.modelName + 'List'];
        if (undef === colType) {
          throw 'There is no collection defined at: OB.Data.Collection.' + model.prototype.modelName + 'List';
        }

        xhr = new enyo.Ajax({
          url: model.prototype.source,
          method: 'POST'
        });

        xhr.response(function (inSender, inResponse) {
          //FIXME: implement error handling
          success(new colType(inResponse.response.data));
        });

        params = enyo.clone(whereClause);

        params._noCount = true;
        params._operationType = 'fetch';
        params._startRow = 0;
        params._endRow = 100;
        params._sortBy = (whereClause && whereClause._sortBy ? whereClause._sortBy : '');
        params.isc_dataFormat = 'json';
        params.isc_metaDataPrefix = '_';

        if (whereClause && whereClause._constructor) {
          for (i in whereClause) {
            if (whereClause.hasOwnProperty(i)) {
              if (i === 'criteria') {
                params.criteria = [];
                criteria = whereClause[i];
                for (j = 0; j < criteria.length; j++) {
                  params.criteria.push(JSON.stringify(criteria[j]));
                }
              } else {
                params[i] = whereClause[i];
              }
            }
          }
        } else {
          params_where = (whereClause && whereClause._where ? whereClause._where : '');
        }

        xhr.go(params);
      } else if (OB.Data.localDB) {
        // websql
        if (whereClause && whereClause._orderByClause) {
          orderBy = whereClause._orderByClause;
        }
        if (whereClause && whereClause._limit) {
          limit = whereClause._limit;
        } else {
          limit = model.prototype.dataLimit;
        }
        if (whereClause && whereClause._whereClause) {
          whereClause.sql = ' ' + whereClause._whereClause;
        } else {
          whereClause = OB.Dal.getWhereClause(whereClause, propertyMap);
        }
        sql = sql + whereClause.sql;
        params = whereClause.params;

        if (orderBy) {
          sql = sql + ' ORDER BY ' + orderBy + ' ';
        } else if (model.propertyList || model.prototype.propertyMap._idx) {
          sql = sql + ' ORDER BY _idx ';
        }

        if (limit) {
          sql = sql + ' LIMIT ' + limit;
        }

        OB.Data.localDB.readTransaction(function (tx) {
          tx.executeSql(sql, params, function (tr, result) {
            var i, collectionType = OB.Collection[model.prototype.modelName + 'List'] || Backbone.Collection,
                collection = new collectionType(),
                len = result.rows.length;
            if (len === 0) {
              success(collection, args);
            } else {
              for (i = 0; i < len; i++) {
                collection.add(OB.Dal.transform(model, result.rows.item(i)));
              }
              success(collection, args);
            }
          }, _.isFunction(error) ? error : null);
        });
      } else {
        // localStorage
        throw 'Not implemented';
      }
    },

    query: function (model, sql, params, success, error, args) {
      if (OB.Data.localDB) {
        if (model.prototype.dataLimit) {
          sql = sql + ' LIMIT ' + model.prototype.dataLimit;
        }
        OB.Data.localDB.readTransaction(function (tx) {
          tx.executeSql(sql, params, function (tr, result) {
            var i, collectionType = OB.Collection[model.prototype.modelName + 'List'] || Backbone.Collection,
                collection = new collectionType(),
                len = result.rows.length;
            if (len === 0) {
              success(collection, args);
            } else {
              for (i = 0; i < len; i++) {
                collection.add(OB.Dal.transform(model, result.rows.item(i)));
              }
              success(collection, args);
            }
          }, _.isFunction(error) ? error : null);
        });
      } else {
        // localStorage
        throw 'Not implemented';
      }
    },

    save: function (model, success, error, forceInsert) {
      var modelProto = model.constructor.prototype,
          modelDefinition = OB.Model[modelProto.modelName],
          tableName = OB.Dal.getTableName(modelDefinition),
          primaryKey, primaryKeyProperty = 'id',
          primaryKeyColumn, sql = '',
          params = null,
          firstParam = true,
          uuid, propertyName, filterVal, xhr, data = {};

      forceInsert = forceInsert || false;

      // TODO: properly check model type
      if (modelProto && modelProto.online) {
        if (!model) {
          throw 'You need to pass a Model instance to save';
        }

        xhr = new enyo.Ajax({
          url: modelProto.source,
          method: 'PUT'
        });

        xhr.response(function (inSender, inResponse) {
          success(inResponse);
        });

        data.operationType = 'update';
        data.data = model.toJSON();

        xhr.go(JSON.stringify(data));
      } else if (OB.Data.localDB) {
        // websql
        if (!tableName) {
          throw 'Missing table name in model';
        }

        if (modelDefinition.getPrimaryKey) {
          primaryKey = modelDefinition.getPrimaryKey();
          primaryKeyProperty = primaryKey.name;
          primaryKeyColumn = primaryKey.column;
        } else {
          primaryKeyColumn = modelProto.propertyMap[primaryKeyProperty];
        }

        if (model.get(primaryKeyProperty) && forceInsert === false) {
          if (modelDefinition.getUpdateStatement) {
            sql = modelDefinition.getUpdateStatement();
            params = [];
            _.each(modelDefinition.getPropertiesForUpdate(), function (property) {
              //filter doen't have name and always is the last one
              if (property.name) {
                params.push(model.get(property.name));
              }
            });
            //filter param
            if (modelDefinition.hasFilter()) {
              filterVal = '';
              _.each(modelDefinition.getFilterProperties(), function (filterProperty) {
                filterVal = filterVal + (model.get(filterProperty) ? (model.get(filterProperty) + '###') : '');
              });
              params.push(filterVal);
            }
            //Where param
            params.push(model.get(primaryKeyProperty));
          } else {
            // UPDATE
            sql = 'UPDATE ' + tableName + ' SET ';

            _.each(_.keys(modelProto.properties), function (attr) {
              propertyName = modelProto.properties[attr];
              if (attr === 'id') {
                return;
              }

              if (firstParam) {
                firstParam = false;
                params = [];
              } else {
                sql = sql + ', ';
              }

              sql = sql + modelProto.propertyMap[propertyName] + ' = ? ';
              params.push(model.get(propertyName));
            });

            if (modelProto.propertiesFilter) {
              filterVal = '';
              _.each(modelProto.propertiesFilter, function (prop) {
                filterVal = filterVal + model.get(prop) + '###';
              });
              sql = sql + ', _filter = ? ';
              params.push(filterVal);
            }
            sql = sql + ' WHERE ' + tableName + '_id = ?';
            params.push(model.get('id'));
          }
        } else {
          params = [];
          // INSERT
          sql = modelDefinition.getInsertStatement ? modelDefinition.getInsertStatement() : modelProto.insertStatement;
          if (forceInsert === false) {
            uuid = OB.Dal.get_uuid();
            params.push(uuid);
            if (model.getPrimaryKey) {
              primaryKey = model.getPrimaryKey();
              model.set(primaryKey.name, uuid);
            } else {
              model.set('id', uuid);
            }
          }
          //Set params
          if (modelDefinition.getProperties) {
            _.each(modelDefinition.getProperties(), function (property) {
              if (forceInsert === false) {
                if (property.primaryKey) {
                  return;
                }
              }
              //_filter property doesn't have name.
              //don't set the filter column. We will do it in the next step
              if (property.name) {
                params.push(model.get(property.name) === undefined ? null : model.get(property.name));
              }
            });
          } else {
            _.each(modelProto.properties, function (property) {
              if (forceInsert === false) {
                if ('id' === property) {
                  return;
                }
              }
              params.push(model.get(property) === undefined ? null : model.get(property));
            });
          }
          //set filter column
          if (modelDefinition.hasFilter) {
            if (modelDefinition.hasFilter()) {
              filterVal = '';
              _.each(modelDefinition.getFilterProperties(), function (filterProp) {
                filterVal = filterVal + (model.get(filterProp) ? (model.get(filterProp) + '###') : '');
              });
              //Include in the last position but before _idx
              params.splice(params.length - 1, 0, filterVal);
            }
          } else {
            if (modelProto.propertiesFilter) {
              filterVal = '';
              _.each(modelProto.propertiesFilter, function (prop) {
                filterVal = filterVal + model.get(prop) + '###';
              });
              //Include in the last position but before _idx
              params.splice(params.length - 1, 0, filterVal);
            }
          }
          //OB.info(params.length);
        }

        //OB.info(sql);
        //OB.info(params);
        OB.Data.localDB.transaction(function (tx) {
          tx.executeSql(sql, params, function () {
            if (success) {
              success(arguments);
            }
          }, _.isFunction(error) ? error : null);
        });
      } else {
        throw 'Not implemented';
      }
    },

    remove: function (model, success, error) {
      var modelDefinition = OB.Model[model.constructor.prototype.modelName],
          modelProto = model.constructor.prototype,
          tableName = OB.Dal.getTableName(modelDefinition),
          pk, pkProperty = 'id',
          pkColumn, sql = '',
          params = [];

      if (OB.Data.localDB) {
        // websql
        if (!tableName) {
          throw 'Missing table name in model';
        }
        if (modelDefinition.getPrimaryKey) {
          pk = modelDefinition.getPrimaryKey() ? modelDefinition.getPrimaryKey() : null;
          if (pk) {
            pkProperty = pk.name;
            pkColumn = pk.column;
          } else {
            pkColumn = modelDefinition.propertyMap[pkProperty];
          }
        }
        if (model.get(pkProperty)) {
          if (modelDefinition.getDeleteByIdStatement) {
            sql = modelDefinition.getDeleteByIdStatement();
          } else {
            sql = 'DELETE FROM ' + tableName + ' WHERE ' + modelProto.propertyMap[pkProperty] + ' = ? ';
          }
          // UPDATE
          params.push(model.get(pkProperty));
        } else {
          throw 'An object without primary key cannot be deleted';
        }

        //OB.info(sql);
        //OB.info(params);
        OB.Data.localDB.transaction(function (tx) {
          tx.executeSql(sql, params, _.isFunction(success) ? success : null, _.isFunction(error) ? error : null);
        });
      } else {
        throw 'Not implemented';
      }
    },

    removeAll: function (model, criteria, success, error) {
      var tableName = OB.Dal.getTableName(model),
          propertyMap = OB.Dal.getPropertyMap(model),
          sql, params, whereClause;
      if (OB.Data.localDB) {
        // websql
        if (!tableName) {
          throw 'Missing table name in model';
        }

        sql = 'DELETE FROM ' + tableName;
        whereClause = OB.Dal.getWhereClause(criteria, propertyMap);
        sql = sql + whereClause.sql;
        params = whereClause.params;
        OB.Data.localDB.transaction(function (tx) {
          tx.executeSql(sql, params, _.isFunction(success) ? success : null, _.isFunction(error) ? error : null);
        });
      } else {
        throw 'Not implemented';
      }
    },

    get: function (model, id, success, error, empty) {
      var tableName = OB.Dal.getTableName(model),
          sql = 'SELECT * FROM ' + tableName + ' WHERE ' + tableName + '_id = ?';

      if (OB.Data.localDB) {
        // websql
        OB.Data.localDB.readTransaction(function (tx) {
          tx.executeSql(sql, [id], function (tr, result) {
            if (result.rows.length === 0) {
              if (empty) {
                empty();
              } else {
                return null;
              }
            } else {
              success(OB.Dal.transform(model, result.rows.item(0)));
            }
          }, _.isFunction(error) ? error : null);
        });
      } else {
        // localStorage
        throw 'Not implemented';
      }
    },

    initCache: function (model, initialData, success, error, incremental) {
      if (OB.Data.localDB) {
        // error must be defined, if not it fails in some android versions
        error = error ||
        function () {};

        if (!model.propertyList && (!model.prototype.createStatement || !model.prototype.dropStatement)) {
          throw 'Model requires a create and drop statement';
        }

        if (!initialData) {
          throw 'initialData must be passed as parameter';
        }

        if (!model.prototype.local && !incremental) {
          OB.Data.localDB.transaction(function (tx) {
            var st = model.getDropStatement ? model.getDropStatement() : model.prototype.dropStatement;
            tx.executeSql(st);
          }, error);
        }

        OB.Data.localDB.transaction(function (tx) {
          var createStatement = model.getCreateStatement ? model.getCreateStatement() : model.prototype.createStatement;
          var createIndexStatement;
          tx.executeSql(createStatement, null, function () {
            //Create Index
            if (model.hasIndex && model.hasIndex()) {
              _.each(model.getIndexes(), function (indexDefinition) {
                createIndexStatement = model.getCreateIndexStatement(indexDefinition);
                tx.executeSql(createIndexStatement, null, null, function () {
                  OB.error('Error creating index ' + indexDefinition.name + ' for table ' + OB.Dal.getTableName(model));
                });
              });
            }
          }, null);
        }, error);

        if (_.isArray(initialData)) {
          OB.Data.localDB.transaction(function (tx) {
            var props = model.getProperties ? model.getProperties() : model.prototype.properties,
                filterVal, values, _idx = 0,
                updateRecord = function (tx, model, values) {
                var deleteStatement;
                deleteStatement = model.getDeleteByIdStatement ? model.getDeleteByIdStatement() : "DELETE FROM " + model.prototype.tableName + " WHERE " + model.prototype.propertyMap.id + "=?";
                tx.executeSql(deleteStatement, [values[0]], function () {
                  var insertSatement;
                  insertSatement = model.getInsertStatement ? model.getInsertStatement() : model.prototype.insertStatement;
                  tx.executeSql(insertSatement, values, null, _.isFunction(error) ? error : null);
                }, _.isFunction(error) ? error : null);
                };

            _.each(initialData, function (item) {
              var filterProps, insertStatement;
              values = [];

              _.each(props, function (prop) {
                var propName = typeof prop === 'string' ? prop : prop.name;
                if (!propName || '_idx' === propName) {
                  return;
                }
                values.push(item[propName]);
              });

              if ((model.hasFilter && model.hasFilter()) || model.prototype.propertiesFilter) {
                filterVal = '';
                filterProps = model.getFilterProperties ? model.getFilterProperties() : model.prototype.propertiesFilter;
                _.each(filterProps, function (prop) {
                  filterVal = filterVal + item[prop] + '###';
                });
                values.push(filterVal);
              }

              values.push(_idx);
              if (incremental) {
                updateRecord(tx, model, values);
              } else {
                insertStatement = model.getInsertStatement ? model.getInsertStatement() : model.prototype.insertStatement;
                tx.executeSql(insertStatement, values, null, _.isFunction(error) ? error : null);
              }
              _idx++;
            });
          }, error, function () {
            // transaction success, execute callback
            if (_.isFunction(success)) {
              success();
            }
          });
        } else { // no initial data
          throw 'initialData must be an Array';
        }
      } else {
        throw 'Not implemented';
      }

    },

    /**
     * Loads a set of models
     *
     *
     */
    loadModels: function (online, models, data, incremental) {
      function triggerReady(models) {
        if (models._LoadOnline && OB.UTIL.queueStatus(models._LoadQueue || {})) {
          // this is only triggered when all models (online and offline) are loaded.
          // offline models are loaded first but don't trigger this, it is not till
          // windowModel is going to be rendered when online models are loaded and this
          // is triggered.
          if (!OB.MobileApp.model.get('datasourceLoadFailed')) {
            models.trigger('ready');
          }
        }
      }

      var somethigToLoad = false,
          timestamp = 0;

      models._LoadOnline = online;

      if (models.length === 0) {
        triggerReady(models);
        return;
      }

      _.each(models, function (item) {
        var ds, load;

        if (item && item.generatedModel) {
          item = OB.Model[item.modelName];
        }

        load = item && ((online && item.prototype.online) || (!online && !item.prototype.online));
        //TODO: check permissions
        if (load) {
          if (item.prototype.local) {
            OB.Dal.initCache(item, [], function () {
              // OB.info('init success: ' + item.prototype.modelName);
            }, function () {
              OB.error('init error', arguments);
            });
          } else {
            // OB.info('[sdrefresh] load model ' + item.prototype.modelName + ' ' + (incremental ? 'incrementally' : 'full'));
            if (incremental && window.localStorage.getItem('lastUpdatedTimestamp' + item.prototype.modelName)) {
              timestamp = window.localStorage.getItem('lastUpdatedTimestamp' + item.prototype.modelName);
            }
            ds = new OB.DS.DataSource(new OB.DS.Request(item, timestamp));
            somethigToLoad = true;
            models._LoadQueue = models._LoadQueue || {};
            models._LoadQueue[item.prototype.modelName] = false;
            ds.on('ready', function () {
              OB.info('Loading data for ' + item.prototype.modelName);
              if (data) {
                data[item.prototype.modelName] = new Backbone.Collection(ds.cache);
              }
              models._LoadQueue[item.prototype.modelName] = true;
              if (incremental) {
                window.localStorage.setItem('POSLastIncRefresh', new Date().getTime());
              } else {
                window.localStorage.setItem('POSLastTotalRefresh', new Date().getTime());
              }
              triggerReady(models);
            });

            if (item.prototype.includeTerminalDate) {
              var currentDate = new Date();
              item.params = item.params || {};
              item.params.terminalTime = currentDate;
              item.params.terminalTimeOffset = currentDate.getTimezoneOffset();
            }
            ds.load(item.params, incremental);
          }
        }
      });
      if (!somethigToLoad) {
        triggerReady(models);
      }
    }
  }
});