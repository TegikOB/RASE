/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global $LAB, _, $, enyo, Backbone, CryptoJS, console */

OB = window.OB || {};
OB.Model = window.OB.Model || {};

OB.MobileApp = OB.MobileApp || {};


OB.MobileApp.windowRegistry = new(Backbone.Model.extend({
  registeredWindows: [],

  registerWindow: function (window) {
    this.registeredWindows.push(window);
  }
}))();

OB.Model.Collection = Backbone.Collection.extend({
  constructor: function (data) {
    this.ds = data.ds;
    Backbone.Collection.prototype.constructor.call(this);
  },
  inithandler: function (init) {
    if (init) {
      init.call(this);
    }
  },
  exec: function (filter) {
    var me = this;
    if (this.ds) {
      this.ds.exec(filter, function (data, info) {
        var i;
        me.reset();
        me.trigger('info', info);
        if (data.exception) {
          OB.UTIL.showError(data.exception.message);
        } else {
          for (i in data) {
            if (data.hasOwnProperty(i)) {
              me.add(data[i]);
            }
          }
        }
      });
    }
  }
});

// Terminal model.
OB.Model.Terminal = Backbone.Model.extend({

  defaults: {
    terminal: null,
    context: null,
    permissions: null,
    businesspartner: null,
    location: null,
    pricelist: null,
    pricelistversion: null,
    currency: null,
    connectedToERP: null,
    loginUtilsUrl: '../../org.openbravo.mobile.core.loginutils',
    loginUtilsParams: {},
    supportsOffline: false,
    loginHandlerUrl: '../../org.openbravo.mobile.core/LoginHandler',
    applicationFormatUrl: '../../org.openbravo.client.kernel/OBCLKER_Kernel/Application',
    logConfiguration: {
      deviceIdentifier: '',
      logPropertiesExtension: []
    },
    windows: null,
    appName: 'OBMOBC',
    appDisplayName: 'Openbravo Mobile',
    useBarcode: false,
    hookManager: null,
    profileOptions: {
      showOrganization: true,
      showWarehouse: true,
      defaultProperties: {
        role: null,
        organization: null,
        warehouse: null,
        languagage: null
      }
    },
    localDB: {
      size: 4 * 1024 * 1024,
      name: 'OBMOBCDB',
      displayName: 'Mobile DB',
      version: '1'
    },
    propertiesLoaders: []
  },

  initialize: function () {
    // navigation logic
    var OBRouter = Backbone.Router.extend({
      terminal: null,
      routes: {
        // list of possible navigation routes
        // also new routes are added on the fly when registeredWindows are loaded (check: loginRegisteredWindows)
        '': 'root',
        'login': 'login',
        '*route': 'any'
      },
      any: function (route) {
        // the developer should check routes if this error shows up
        OB.error(route + ' route is missing. you should use the registerWindow method to register the window');
      },
      root: function () {
        // do nothing
      },
      login: function () {
        this.terminal.renderLogin();
      },

      initialize: function (associatedTerminal) {
        if (!associatedTerminal) {
          OB.error("The router needs to be associated to a terminal");
          return;
        }
        this.terminal = associatedTerminal;
      },

      renderGenericWindow: function (windowName, params) {
        // the registered windows are rendered within this function
        this.terminal.renderGenericWindow(windowName, params);
      }
    });

    this.hookManager = new OB.UTIL.HookManager();
    // expose terminal globally
    window.OB = OB || {};
    window.OB.MobileApp = OB.MobileApp || {};
    window.OB.MobileApp.model = this;

    this.windowRegistry = OB.MobileApp.windowRegistry;

    // DEVELOPER: activate this to see all the events that are being fired by the backbone components of the terminal
    // this.on('all', function(eventName, object, third) {
    //   if (eventName.indexOf('change') >= 0) {
    //     return;
    //   }
    //   var enyoName = object ? object.name : '';
    //   OB.info('event fired: ' + eventName + ', ' + enyoName + ', ' + third);
    // });
    // model events
    this.on('window:ready', this.renderContainerWindow); // When the active window is loaded, the window is rendered with the renderContainerWindow function
    this.on('terminalInfoLoaded', this.processPropertyLoaders);
    this.on('propertiesLoadersReady', function () {
      this.loadRegisteredWindows();
      this.renderMain();
    }, this);


    this.router = new OBRouter(this);

    // if the user refresh the page (f5, etc), be sure that the terminal navigates to the starting page
    if (window.location.toString().indexOf('#') > 0) {
      OB.info('Redirecting to the starting page');
      window.location = '';
      return;
    }

    // explicitly setting pushState to false tries to ensure...
    // that future default modes will not activate the pushState...
    // breaking the POS navigation
    Backbone.history.start({
      root: '',
      pushState: false
    });
  },

  cleanTerminalData: function () {
    _.each(this.get('propertiesLoaders'), function (curPropertiesToLoadProcess) {
      _.each(curPropertiesToLoadProcess.properties, function (curProperty) {
        this.set(curProperty, null);
      }, this);
    }, this);
  },

  returnToOnline: function () {
    //DEVELOPERS: overwrite this function to perform actions when it happens
    OB.info('The system has come back to online');
  },

  //DEVELOPER: This function allow us to execute some code to add properties to to terminal model. Each position of the array
  // must identify the properties which are set and needs to define a loadFunction which will be executed to get the data.
  // when the data is ready terminalModel.propertiesReady(properties) should be executed.
  addPropertiesLoader: function (obj) {
    this.get('propertiesLoaders').push(obj);
    this.syncAllPropertiesLoaded = _.after(this.get('propertiesLoaders').length, this.allPropertiesLoaded);
  },

  propertiesReady: function (properties) {
    OB.info(properties, 'is/are loaded');
    this.syncAllPropertiesLoaded();
  },

  processPropertyLoaders: function () {
    var termInfo;
    if (this.get('loggedOffline')) {
      termInfo = JSON.parse(this.usermodel.get('terminalinfo'));

      //Load from termInfo
      //TODO load all the recovered properties not only the array ones
      _.each(this.get('propertiesLoaders'), function (curPropertiesToLoadProcess) {
        _.each(curPropertiesToLoadProcess.properties, function (curProperty) {
          this.set(curProperty, termInfo[curProperty]);
        }, this);
      }, this);
      this.set('useBarcode', termInfo.useBarcode);
      //Not included into array
      this.set('permissions', termInfo.permissions);
      this.set('orgUserId', termInfo.orgUserId);

      this.allPropertiesLoaded();
      return;
    }
    //Set array properties as null except terminal
    this.cleanTerminalData();
    this.set('loggedOffline', false);
    //Loading the properties of the array
    OB.info('Starting to load properties based on properties loaders', this.get('propertiesLoaders'));
    if (this.get('propertiesLoaders') && this.get('propertiesLoaders').length > 0) {
      _.each(this.get('propertiesLoaders'), function (curProperty) {
        //each loadFunction will call to propertiesReady function. This function will trigger
        //allPropertiesLoaded when all of the loadFunctions are done.
        curProperty.loadFunction(this);
      }, this);
    } else {
      this.allPropertiesLoaded();
    }
  },

  //DEVELOPER: this function will be automatically called when all the properties defined in
  //this.get('propertiesLoaders') are loaded. To indicate that a property is loaded
  //me.propertiesReady(properties) should be executed by loadFunction of each property
  allPropertiesLoaded: function () {
    OB.info('properties has been loaded successfully', this.attributes);
    var undef;
    this.loggingIn = false;
    //core
    if (!this.get('loggedOffline')) {
      //In online mode, we save the terminal information in the local db
      this.usermodel.set('terminalinfo', JSON.stringify(this));
      OB.Dal.save(this.usermodel, function () {}, function () {
        OB.error(arguments);
      });
    }
    //renderMain
    if (!OB.MobileApp.model.get('datasourceLoadFailed')) {
      this.trigger('propertiesLoadersReady');
    }
  },

  navigate: function (route) {
    this.router.navigate(route, {
      trigger: true
    });
  },

  /**
   * Loads registered windows and calls renderMain method implemented by each app
   */
  renderTerminalMain: function () {
    if (this.renderMain) {
      this.loadTerminalInfo();
    } else {
      OB.error('There is no renderMain method in Terminal Model');
    }
  },

  /**
   * Loads all needed stuff for the terminal such as permissions, Application...
   */
  loadTerminalInfo: function () {
    var me = this,
        loadQueue = {};

    function triggerLoadTerminalInfoReady() {
      if (OB.UTIL.queueStatus(loadQueue)) {
        me.setUserModelOnline(function () {
          me.trigger('terminalInfoLoaded');
        });
      }
    }
    if (window.openDatabase) {
      this.initLocalDB(loadQueue, triggerLoadTerminalInfoReady);
    }
    if (OB.MobileApp.model.get('loggedOffline')) {
      triggerLoadTerminalInfoReady();
      OB.Format = JSON.parse(window.localStorage.getItem('AppFormat'));
      return;
    }

    loadQueue.preferences = false;
    new OB.DS.Request('org.openbravo.mobile.core.login.RolePermissions').exec({
      appModuleId: this.get('appModuleId')
    }, function (data) {
      var i, max, separator, permissions = {};
      if (data) {
        for (i = 0, max = data.length; i < max; i++) {
          permissions[data[i].key] = data[i].value; // Add the permission value.
          separator = data[i].key.indexOf('_');
          if (separator >= 0) {
            permissions[data[i].key.substring(separator + 1)] = data[i].value; // if key has a DB prefix, add also the permission value without this prefix
          }
        }
        me.set('permissions', permissions);
        // TODO: offline + usermodel
        loadQueue.preferences = true;
        triggerLoadTerminalInfoReady();
      }
    });

    loadQueue.application = false;
    new enyo.Ajax({
      url: this.get('applicationFormatUrl'),
      method: 'GET',
      handleAs: 'text'
    }).response(function (inSender, inResponse) {
      eval(inResponse); //...we have an OB variable local to this function
      // we want to 'export' some properties to global OB
      window.OB.Application = OB.Application;
      window.OB.Format = OB.Format;
      window.localStorage.setItem('AppFormat', JSON.stringify(OB.Format));
      window.OB.Constants = OB.Constants;
      loadQueue.application = true;
      triggerLoadTerminalInfoReady();
    }).go();
  },

  initLocalDB: function (loadQueue, triggerLoadTerminalInfoReady) {
    loadQueue.localDB = false;

    var undef, wsql = window.openDatabase !== undef,
        dbInfo = this.get('localDB'),
        db = (wsql && window.openDatabase(dbInfo.name, '', dbInfo.displayName, dbInfo.size));

    function dropTable(db, sql) {
      db.transaction(function (tx) {
        tx.executeSql(sql, {}, function () {
          OB.info('succesfully dropped table: ' + sql);
        }, function () {
          OB.error(arguments);
        });
      });
    }

    function dropTables() {
      var model;
      var modelObj;
      OB.info('Updating database model. Tables will be dropped:');
      for (model in OB.Model) {
        if (OB.Model.hasOwnProperty(model)) {
          modelObj = OB.Model[model];
          if ((modelObj.prototype && modelObj.prototype.dropStatement) || modelObj.getDropStatement) {
            //There is a dropStatement, executing it
            dropTable(db, modelObj.getDropStatement ? modelObj.getDropStatement() : modelObj.prototype.dropStatement);
          }
        }
      }
    }

    db.changeVersion(db.version, dbInfo.version, function (t) {
      var model, modelObj;
      if (db.version === dbInfo.version) {
        //Database version didn't change.
        //Will check if terminal id has changed
        if (window.localStorage.getItem('terminalName')) {
          if (window.localStorage.getItem('terminalName') !== OB.MobileApp.model.get('terminalName')) {
            OB.info('Terminal has been changed. Resetting database and local storage information.');
            dropTables();
            window.localStorage.clear();
          }
        }
        window.localStorage.setItem('terminalName', OB.MobileApp.model.get('terminalName'));
        loadQueue.localDB = true;
        triggerLoadTerminalInfoReady();
        return;
      }
      //Version of the database changed, we need to drop the tables so they can be created again
      dropTables();
      loadQueue.localDB = true;
      triggerLoadTerminalInfoReady();
    });

    // exposing DB globally
    window.OB = OB || {};
    window.OB.Data = OB.Data || {};
    window.OB.Data.localDB = db;
  },


  /**
   * Invoked from initComponents in terminal view
   */
  initializeCommonComponents: function () {
    var me = this,
        params = {};
    window.localStorage.setItem('LOGINTIMER', new Date().getTime());

    params = this.get('loginUtilsParams') || {};
    params.command = 'preRenderActions';
    // initialize labels and other common stuff
    var ajaxRequest = new enyo.Ajax({
      url: this.get('loginUtilsUrl'),
      cacheBust: false,
      timeout: 5000,
      method: 'GET',
      handleAs: 'json',
      contentType: 'application/json;charset=utf-8',
      data: params,
      success: function (inSender, inResponse) {
        OB.appCaption = inResponse.appCaption;
        if (_.isEmpty(inResponse.labels) || _.isNull(inResponse.labels) || _.isUndefined(inResponse.labels)) {
          OB.I18N.labels = JSON.parse(window.localStorage.getItem('I18NLabels'));
        } else {
          OB.I18N.labels = inResponse.labels;
          if (inResponse.activeSession) {
            // The labels will only be saved in the localStorage if they come from an active session
            // We do this to prevent the labels loaded in the login page from being stored here and overwritting
            // the labels in the correct language (see issue 23613)
            window.localStorage.setItem('I18NLabels', JSON.stringify(OB.I18N.labels));
          }
        }
        if (_.isEmpty(inResponse.dateFormats) || _.isNull(inResponse.dateFormats) || _.isUndefined(inResponse.dateFormats)) {
          OB.Format = JSON.parse(window.localStorage.getItem('AppFormat'));
        } else {
          OB.Format = inResponse.dateFormats;
        }

        me.trigger('initializedCommonComponents');

        new OB.DS.Request('org.openbravo.mobile.core.login.Context').exec({
          ignoreForConnectionStatus: true
        }, function (inResponse) {
          OB.MobileApp.model.triggerOnLine();
          if (inResponse && !inResponse.exception) {
            OB.MobileApp.model.user = inResponse[0].user.username;
            OB.MobileApp.model.set('orgUserId', inResponse[0].user.id);
            OB.UTIL.showLoading(true);

            me.set('context', inResponse[0]);
            me.renderTerminalMain();
          } else {
            // Notifying view component that I'm rendered so login page can be
            // shown if needed
            OB.MobileApp.view.terminal.trigger('viewRendered');
            me.navigate('login');
          }
        }, function () {
          // Notifying view component that I'm rendered so login page can be
          // shown if needed
          OB.MobileApp.view.terminal.trigger('viewRendered');
          me.navigate('login');
        });
      },
      fail: function (inSender, inResponse) {
        // we are likely offline. Attempt to navigate to the login page
        OB.I18N.labels = JSON.parse(window.localStorage.getItem('I18NLabels'));
        OB.Format = JSON.parse(window.localStorage.getItem('AppFormat'));
        me.trigger('initializedCommonComponents');
        // Notifying view component that I'm rendered so login page can be
        // shown if needed
        OB.MobileApp.view.terminal.trigger('viewRendered');
        OB.UTIL.showLoading(false);
        me.navigate('login');
      }
    });

    ajaxRequest.go(ajaxRequest.data).response('success').error('fail');
  },

  renderLogin: function () {
    if (!OB.Data.localDB && window.openDatabase) {
      this.initLocalDB({}, function () {});
    }
    if (!OB.MobileApp.view) {
      this.on('viewRendered', function () {
        // waiting view to be rendered before going to login page
        this.renderLogin();
      }, this);
      return;
    }
    if (!OB.MobileApp.view.$.containerWindow) {
      return;
    }

    OB.MobileApp.view.$.containerWindow.destroyComponents();
    OB.MobileApp.view.$.containerWindow.createComponent({
      kind: OB.OBPOSLogin.UI.Login
    }).render();
    OB.UTIL.showLoading(false);
  },

  loadModels: function (windowv, incremental) {
    var windows, i, windowName, windowClass, datasources, timestamp = 0,
        w, c, path;


    if (OB.MobileApp.model.get('loggedOffline')) {
      return;
    }

    if (windowv) {
      windows = [windowv];
    } else {
      windows = [];
      _.each(this.windowRegistry.registeredWindows, function (windowp) {
        windows.push(windowp);
      });
    }

    OB.info('[sdrefresh] Load models ' + (incremental ? 'incrementally' : 'full') + '.');

    for (i = 0; i < windows.length; i++) {
      windowClass = windows[i].windowClass;
      windowName = windows[i].route;
      if (OB && OB.DATA && OB.DATA[windowName]) {
        // old way of defining datasources...
        datasources = OB.DATA[windowName];
      } else if (windowClass.prototype && windowClass.prototype.windowmodel && windowClass.prototype.windowmodel.prototype && windowClass.prototype.windowmodel.prototype.models) {
        datasources = windowClass.prototype.windowmodel.prototype.models;
      } else if (typeof windowClass === 'string') {
        w = window; // global window
        path = windowClass.split('.');
        for (c = 0; c < path.length; c++) {
          w = w[path[c]];
        }

        if (w.prototype && w.prototype.windowmodel && w.prototype.windowmodel.prototype && w.prototype.windowmodel.prototype.models) {
          datasources = w.prototype.windowmodel.prototype.models;
        }
      }

      _.extend(datasources, Backbone.Events);

      OB.info('[sdrefresh] window: ' + windowName);

      OB.Dal.loadModels(false, datasources, null, incremental);
    }
  },

  cleanWindows: function () {
    this.windows = new(Backbone.Collection.extend({
      comparator: function (window) {
        // sorts by menu position, 0 if not defined
        var position = window.get('menuPosition');
        return position ? position : 0;
      }
    }))();
  },

  registerWindow: function (window) {
    this.windowRegistry.registerWindow(window);
  },

  /**
   * Iterates over all registered windows loading their models
   */
  loadRegisteredWindows: function () {
    this.cleanWindows();

    setInterval(OB.UTIL.processLogClientAll, 10000);

    var countOfLoadedWindows = 0;
    _.each(this.windowRegistry.registeredWindows, function (windowp) {
      var datasources = [],
          windowClass, windowName = windowp.route,
          minTotalRefresh, minIncRefresh, lastTotalRefresh, lastIncRefresh, intervalTotal, intervalInc, now, terminalType;
      this.windows.add(windowp);
      //    if (!OB.POS.windowObjs) {
      //      OB.POS.windowObjs = [];
      //    }
      //    OB.POS.windowObjs.push(windowp);
      //    this.loadModels(windowp, false);
      //    if (false) {
      terminalType = OB.MobileApp.model.get('terminal') && OB.MobileApp.model.get('terminal').terminalType;
      if (terminalType) {
        minTotalRefresh = OB.MobileApp.model.get('terminal').terminalType.minutestorefreshdatatotal * 60 * 1000;
        minIncRefresh = OB.MobileApp.model.get('terminal').terminalType.minutestorefreshdatainc * 60 * 1000;
        lastTotalRefresh = window.localStorage.getItem('POSLastTotalRefresh');
        lastIncRefresh = window.localStorage.getItem('POSLastIncRefresh');
      }
      if ((!minTotalRefresh && !minIncRefresh) || (!lastTotalRefresh && !lastIncRefresh)) {
        // If no configuration of the masterdata loading has been done,
        // or an initial load has not been done, then always do
        // a total refresh during the login
        this.loadModels(windowp, false);
      } else {
        now = new Date().getTime();
        intervalTotal = lastTotalRefresh ? (now - lastTotalRefresh - minTotalRefresh) : 0;
        intervalInc = lastIncRefresh ? (now - lastIncRefresh - minIncRefresh) : 0;
        if (intervalTotal > 0) {
          this.set('FullRefreshWasDone', true);

          //It's time to do a full refresh
          this.loadModels(windowp, false);
        }
      }
      //}
      this.router.route(windowName, windowName, function () {
        this.renderGenericWindow(windowName);
      });

      this.router.route(windowName + "/:params", windowName, function (params) {
        this.renderGenericWindow(windowName, params);
      });
      countOfLoadedWindows += 1;
    }, this);
    // checks if the windows loaded are more than the windows registered, which never should happen
    var countOfRegisteredWindows = this.windowRegistry.registeredWindows.length;
    console.assert(countOfRegisteredWindows === countOfLoadedWindows, 'DEVELOPER: There are ' + countOfRegisteredWindows + ' registered windows but ' + countOfLoadedWindows + ' are being loaded');
  },

  isWindowOnline: function (route) {
    var i, windows;
    windows = this.windows.toArray();
    for (i = 0; i < windows.length; i++) {
      if (windows[i].get('route') === route) {
        return windows[i].get('online');
      }
    }
    return false;
  },

  renderGenericWindow: function (windowName, params) {
    OB.UTIL.showLoading(true);
    var terminal = OB.MobileApp.model.get('terminal'),
        windowClass;

    windowClass = this.windows.where({
      route: windowName
    })[0].get('windowClass');

    OB.MobileApp.view.$.containerWindow.destroyComponents();
    OB.MobileApp.view.$.containerWindow.createComponent({
      kind: windowClass,
      params: params
    });
  },

  renderContainerWindow: function () {
    OB.MobileApp.view.$.containerWindow.render();
    OB.UTIL.showLoading(false);
    OB.MobileApp.view.$.containerWindow.resized();
    //enyo.dispatcher.capture(OB.MobileApp.view, false);
  },

  updateSession: function (user) {
    OB.Dal.find(OB.Model.Session, {
      'user': user.get('id')
    }, function (sessions) {
      var session;
      if (sessions.models.length === 0) {
        session = new OB.Model.Session();
        session.set('user', user.get('id'));
        session.set('terminal', OB.MobileApp.model.get('terminalName'));
        session.set('active', 'Y');
        OB.Dal.save(session, function () {}, function () {
          OB.error(arguments);
        });
      } else {
        session = sessions.models[0];
        session.set('active', 'Y');
        OB.Dal.save(session, function () {}, function () {
          OB.error(arguments);
        });
      }
      OB.MobileApp.model.set('session', session.get('id'));
    }, function () {
      OB.error(arguments);
    });
  },

  /**
   * This method is invoked when closing session, it is in charge
   * of dealing with all stuff needed to close session. After it is
   * done it MUST do triggerLogout
   */
  postCloseSession: function (session) {
    OB.MobileApp.model.triggerLogout();
  },

  closeSession: function () {
    var sessionId = OB.MobileApp.model.get('session'),
        me = this;
    if (!this.get('supportsOffline') || !OB.Model.Session) {
      OB.MobileApp.model.triggerLogout();
      return;
    }
    OB.Dal.get(OB.Model.Session, sessionId, function (session) {
      session.set('active', 'N');
      OB.Dal.save(session, function () {
        me.postCloseSession(session);
      }, function () {
        OB.error(arguments);
        OB.MobileApp.model.triggerLogout();
      });
    }, function () {
      OB.error(arguments);
      OB.MobileApp.model.triggerLogout();
    });
  },

  generate_sha1: function (theString) {
    return CryptoJS.enc.Hex.stringify(CryptoJS.SHA1(theString));
  },

  attemptToLoginOffline: function () {
    var me = this;
    OB.MobileApp.model.set('windowRegistered', undefined);
    OB.MobileApp.model.set('loggedOffline', true);
    OB.Dal.find(OB.Model.User, {
      'name': me.user
    }, function (users) {
      var user;
      if (users.models.length === 0) {
        alert(OB.I18N.getLabel('OBMOBC_OfflinePasswordNotCorrect'));
        window.location.reload();
      } else {
        if (users.models[0].get('password') === me.generate_sha1(me.password + users.models[0].get('created'))) {
          me.usermodel = users.models[0];
          me.set('orgUserId', users.models[0].id);
          me.updateSession(me.usermodel);
          me.renderTerminalMain();
        } else {
          alert(OB.I18N.getLabel('OBMOBC_OfflinePasswordNotCorrect'));
          window.location.reload();
        }
      }
    }, function () {});
  },

  /**
   * Prelogin actions is executed before login, it should take care of
   * removing all session values specific by application.
   *
   * Override this in case you app needs to do something special
   */
  preLoginActions: function () {

  },

  login: function (user, password, mode) {
    var params;
    OB.UTIL.showLoading(true);
    var me = this;
    me.user = user;
    me.password = password;

    // invoking app specific actions
    this.preLoginActions();
    this.set('isLoggingIn', true);

    params = this.get('loginUtilsParams') || {};
    params.user = user;
    params.password = password;
    params.Command = 'DEFAULT';
    params.IsAjaxCall = 1;
    params.appName = this.get('appName');

    var ajaxRequest = new enyo.Ajax({
      url: this.get('loginHandlerUrl'),
      cacheBust: false,
      method: 'POST',
      timeout: 5000,
      contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
      data: params,
      success: function (inSender, inResponse) {
        var pos, baseUrl;
        if (this.alreadyProcessed) {
          return;
        }
        this.alreadyProcessed = true;
        if (OB.MobileApp.model.get('timeoutWhileLogin')) {
          OB.MobileApp.model.set('timeoutWhileLogin', false);
          return;
        }
        if (inResponse && inResponse.showMessage) {
          me.triggerLoginFail(401, mode, inResponse);
          return;
        }
        OB.MobileApp.model.set('orgUserId', inResponse.userId);
        me.set('loggedOffline', false);
        if (me.get('supportsOffline')) {
          me.setUserModelOnline();
        }

        me.initializeCommonComponents();
      },
      fail: function (inSender, inResponse) {
        if (this.alreadyProcessed) {
          return;
        }
        this.alreadyProcessed = true;
        me.attemptToLoginOffline();
      }
    });
    ajaxRequest.go(ajaxRequest.data).response('success').error('fail');
  },

  setUserModelOnline: function (callback) {
    var me = this;
    OB.Dal.initCache(OB.Model.User, [], null, null);
    OB.Dal.initCache(OB.Model.LogClient, [], null, null);
    OB.Dal.initCache(OB.Model.Session, [], null, null);
    OB.Dal.find(OB.Model.User, {
      'name': me.user
    }, function (users) {
      var user, session, date, savedPass;
      if (users.models.length === 0) {
        date = new Date().toString();
        user = new OB.Model.User();
        user.set('name', me.user);
        user.set('id', OB.MobileApp.model.get('orgUserId'));
        savedPass = me.generate_sha1(me.password + date);
        user.set('password', savedPass);
        user.set('created', date);
        user.set('formatInfo', JSON.stringify(OB.Format));
        me.usermodel = user;
        OB.Dal.save(user, function () {
          if (callback) {
            callback();
          }
        }, function () {
          OB.error(arguments);
        }, true);
      } else {
        user = users.models[0];
        me.usermodel = user;
        if (me.password) {
          //The password will only be recomputed in case it was properly entered
          //(that is, if the call comes from the login page directly)
          savedPass = me.generate_sha1(me.password + user.get('created'));
          user.set('password', savedPass);
          me.usermodel = user;
        } else {
          user = users.models[0];
          me.usermodel = user;
          if (me.password) {
            //The password will only be recomputed in case it was properly entered
            //(that is, if the call comes from the login page directly)
            savedPass = me.generate_sha1(me.password + user.get('created'));
            user.set('password', savedPass);
          }
        }
        user.set('formatInfo', JSON.stringify(OB.Format));
        OB.Dal.save(user, function () {
          me.updateSession(user);
          if (callback) {
            callback();
          }
        }, function () {
          OB.error(arguments);
        });
      }
    }, function () {
      OB.error(arguments);
    });
  },

  /**
   * Set of app specific actions executed before loging out.
   *
   *  Override this method if your app needs to do something special
   */
  preLogoutActions: function () {

  },

  logout: function () {
    var me = this;

    this.preLogoutActions();

    var ajaxRequest = new enyo.Ajax({
      url: '../../org.openbravo.mobile.core.logout',
      cacheBust: false,
      method: 'GET',
      handleAs: 'json',
      timeout: 5000,
      contentType: 'application/json;charset=utf-8',
      success: function (inSender, inResponse) {
        me.closeSession();
      },
      fail: function (inSender, inResponse) {
        me.closeSession();
      }
    });
    ajaxRequest.go().response('success').error('fail');
  },

  lock: function () {
    var me = this;

    this.preLogoutActions();

    var ajaxRequest = new enyo.Ajax({
      url: '../../org.openbravo.mobile.core.logout',
      cacheBust: false,
      method: 'GET',
      handleAs: 'json',
      timeout: 5000,
      contentType: 'application/json;charset=utf-8',
      success: function (inSender, inResponse) {
        me.triggerLogout();
      },
      fail: function (inSender, inResponse) {
        me.triggerLogout();
      }
    });
    ajaxRequest.go().response('success').error('fail');
  },

  triggerLogout: function () {
    this.trigger('logout');
    var params, paramsStr = null;
    if (this.get('logoutUrlParams')) {
      var keyParams;
      params = this.get('logoutUrlParams');
      keyParams = _.keys(params);
      if (keyParams.length > 0) {
        paramsStr = '?';
        _.each(keyParams, function (key, index) {
          if (index > 0) {
            paramsStr = paramsStr + '&';
          }
          paramsStr = paramsStr + key.toString() + '=' + window.encodeURIComponent(params[key]);
        });
      }
    }
    window.location = paramsStr ? window.location.pathname + paramsStr : window.location.pathname;
  },

  triggerLoginSuccess: function () {
    this.trigger('loginsuccess');
  },

  triggerOnLine: function () {
    if (!OB.MobileApp.model.loggingIn) {
      this.set('connectedToERP', true);
      if (this.pingId) {
        clearInterval(this.pingId);
        this.pingId = null;
      }
    }
  },

  triggerOffLine: function () {
    if (!OB.MobileApp.model.loggingIn) {
      this.set('connectedToERP', false);
      if (!this.pingId) {
        this.pingId = setInterval(OB.UTIL.checkConnectivityStatus, 60000);
      }
    }
  },

  triggerLoginFail: function (e, mode, data) {
    OB.UTIL.showLoading(false);
    if (mode === 'userImgPress') {
      this.trigger('loginUserImgPressfail', e);
    } else {
      this.trigger('loginfail', e, data);
    }
  },

  hasPermission: function (p, checkForAutomaticRoles) {
    return (!checkForAutomaticRoles && !this.get('context').role.manual) || this.get('permissions')[p];
  },

  supportLogClient: function () {
    var supported = true;
    if ((OB.MobileApp && OB.MobileApp.model && OB.MobileApp.model.get('supportsOffline')) === false) {
      supported = false;
    }
    return supported;
  },

  saveTerminalInfo: function () {
    OB.info('saving terminal info:', this.attributes);
    //In online mode, we save the terminal information in the local db
    this.usermodel.set('terminalinfo', JSON.stringify(this));
    OB.Dal.save(this.usermodel, function () {}, function () {
      OB.error(arguments);
    });
  }
});