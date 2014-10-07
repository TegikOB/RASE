/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, $, _ */


(function () {

  if (OB.MobileApp.model.hookManager) {

    OB.MobileApp.model.hookManager.registerHook('OBPOS_GroupedProductPreCreateLine', function (args, callbacks) {
      if (OB.UTIL.isNullOrUndefined(args.line) && !OB.UTIL.isNullOrUndefined(args.allLines)) {
        args.line = args.allLines.find(function (l) {
          if (l.get('product').id === args.p.id && (l.get('qty') > 0 || OB.UTIL.isNullOrUndefined(l.get('originalOrderLineId')))) {
            affectedByPack = l.isAffectedByPack();
            if (!affectedByPack) {
              return true;
            } else if ((args.options && args.options.packId === affectedByPack.ruleId) || !(args.options && args.options.packId)) {
              return true;
            }
          }
        });
      }

      if (!OB.UTIL.isNullOrUndefined(args.line) && !OB.UTIL.isNullOrUndefined(args.line.get('originalOrderLineId'))) {
        args.cancelOperation = true;
        args.receipt.createLine(args.p, args.qty, args.options, args.attrs);
        args.receipt.save();
      }
      OB.MobileApp.model.hookManager.callbackExecutor(args, callbacks);

      return;
    });
  }

}());