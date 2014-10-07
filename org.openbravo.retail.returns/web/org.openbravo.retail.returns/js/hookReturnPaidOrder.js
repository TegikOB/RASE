/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, $ */


(function () {

  if (OB.MobileApp.model.hookManager) {

    OB.MobileApp.model.hookManager.registerHook('OBRETUR_ReturnFromOrig', function (args, callbacks) {
      var order = args.order,
          params = args.params,
          context = args.context;
      if (params.isReturn) {
        context.doShowPopup({
          popup: 'modalReturnReceipt',
          args: {
            args: args,
            callbacks: callbacks
          }
        });
      } else {
        OB.MobileApp.model.hookManager.callbackExecutor(args, callbacks);
      }

      return;
    });
  }

}());