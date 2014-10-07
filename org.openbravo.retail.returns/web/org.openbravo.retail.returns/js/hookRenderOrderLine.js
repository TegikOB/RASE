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

    OB.MobileApp.model.hookManager.registerHook('OBPOS_RenderOrderLine', function (args, callbacks) {
      var orderline = args.orderline;
      if (orderline.model.get('originalDocumentNo')) {
        orderline.createComponent({
          style: 'display: block;',
          components: [{
            content: '** ' + OB.I18N.getLabel('OBRETUR_LblLineFromOriginal') + orderline.model.get('originalDocumentNo') + ' **',
            attributes: {
              style: 'float: left; width: 80%;'
            }
          }, {
            content: '',
            attributes: {
              style: 'float: right; width: 20%; text-align: right;'
            }
          }, {
            style: 'clear: both;'
          }]
        });
      }
      OB.MobileApp.model.hookManager.callbackExecutor(args, callbacks);
      return;
    });
  }

}());