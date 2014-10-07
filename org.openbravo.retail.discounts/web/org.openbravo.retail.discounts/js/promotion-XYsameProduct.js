/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

// Buy X pay Y of same product
OB.Model.Discounts.discountRules['E08EE3C23EBA49358A881EF06C139D63'] = {
  async: false,
  implementation: function (discountRule, receipt, line) {
    var alerts, qty, x, y, mod, chunks, price, finalPrice;

    x = discountRule.get('oBDISCX');
    y = discountRule.get('oBDISCY');

    if (!x || !y || x === 0) {
      window.console.warn('Discount incorrectly defined, missing x or y', discountRule);
    }
    qty = line.get('qty');

    mod = qty % x;

    if (mod !== 0) {
      alerts = OB.I18N.getLabel('OBPOS_DISCAlertXYSameProduct', [x - mod, line.get('product').get('_identifier'), discountRule.get('printName') || discountRule.get('name')]);
    }

    if (qty >= x) {
      chunks = Math.floor(qty / x);
      price = line.get('discountedLinePrice') || line.get('price');
      // note discountedLinePrice is not updated since this rule shouldn't change it
      finalPrice = OB.DEC.add(OB.DEC.mul(OB.DEC.mul(chunks, y), price), OB.DEC.mul(mod, price));
      receipt.addPromotion(line, discountRule, {
        amt: OB.DEC.sub(OB.DEC.mul(qty, price), finalPrice)
      });
    }

    return {
      alerts: alerts
    };
  }
};