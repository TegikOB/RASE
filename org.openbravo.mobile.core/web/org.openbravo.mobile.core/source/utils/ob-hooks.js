/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _, enyo */

/**
 * HookManager (available at OB.MobileApp.model.hookManager) is a lightweight way of
 * allow extension points in the code. These extensions points will be used by external
 * modules to inject their code.
 *
 * Extensions points are identified by a qualifier string. Modules register the function
 * to execute at this point.
 */
OB.UTIL.HookManager = Backbone.Model.extend({
  /**
   * External modules can define the hook to execute (func) for a concrete extension
   * point (qualifier).
   *
   * The function has always 2 parameters:
   *   - args: it is any javascript object the base module can send as parameter
   *   - callback: it is an array of callback functions, it should be managed by
   *       invoking to OB.MobileApp.model.hookManager.executeHooks(callback) function
   *       once the execution is finished
   */
  registerHook: function (qualifier, func) {
    var qualifierFuncs;
    if (!enyo.isFunction(func)) {
      window.error('Error trying to register hook for', qualifier, 'func', func, 'is not a function');
      return;
    }
    qualifierFuncs = this.get(qualifier) || [];
    qualifierFuncs.unshift(func);
    this.set(qualifier, qualifierFuncs);
  },

  /**
   * Base modules execute hooks for a qualifier, after execution of all hooks, callback
   * function will be invoked
   */
  executeHooks: function (qualifier, args, callback) {
    var hooks;
    if (callback && !enyo.isFunction(callback)) {
      window.error('Error while executing hooks for', qualifier, 'callback is not a function', callback);
      return;
    }
    hooks = _.clone(this.get(qualifier)) || [];
    if (callback) {
      hooks.unshift(callback);
    }
    this.callbackExecutor(args, hooks);
  },

  /**
   * Convenience method that should be invoked by hook implementors once they are done
   * in order to continue with the hook chain
   */
  callbackExecutor: function (args, callbacks) {
    var func;
    func = callbacks.pop();
    if (func) {
      func(args, callbacks);
    }
  }
});