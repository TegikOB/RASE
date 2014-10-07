/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone console _*/

// Promotion rules module depends directly on client kernel module so it is not necessary
// to install Web POS to work with it, this makes dependency chain not to be as it should:
// this code depending on WebPOS that provides OB.Model.Discounts. Hacking it here to allow
// this dependency.
window.OB = window.OB || {};
OB.Model = OB.Model || {};
OB.Model.Discounts = OB.Model.Discounts || {};
OB.Model.Discounts.discountRules = OB.Model.Discounts.discountRules || {};

//Fixed Percentage Discount
OB.Model.Discounts.discountRules['697A7AB9FD9C4EE0A3E891D3D3CCA0A7'] = {
  async: false,
  implementation: function (discountRule, receipt, line) {
    var linePrice, totalDiscount, qty = OB.DEC.toBigDecimal(line.get('qty')),
        discount = OB.DEC.toBigDecimal(discountRule.get('discount')),
        discountedLinePrice, oldDiscountedLinePrice;

    linePrice = OB.DEC.toBigDecimal(line.get('discountedLinePrice') || line.get('price'));

    discountedLinePrice = linePrice.multiply(new BigDecimal('100').subtract(OB.DEC.toBigDecimal(discountRule.get('discount'))).divide(new BigDecimal('100'), 20, OB.DEC.getRoundingMode()));
    discountedLinePrice = OB.DEC.toNumber(discountedLinePrice);

    totalDiscount = OB.DEC.sub(OB.DEC.mul(OB.DEC.toNumber(linePrice), qty), OB.DEC.mul(discountedLinePrice, qty));
    oldDiscountedLinePrice = !_.isNull(line.get('discountedLinePrice')) ? line.get('discountedLinePrice') : line.get('price');
    if (oldDiscountedLinePrice < OB.DEC.div(totalDiscount, line.getQty())) {
      totalDiscount = OB.DEC.mul(oldDiscountedLinePrice, line.getQty());
    }
    receipt.addPromotion(line, discountRule, {
      amt: OB.DEC.toNumber(totalDiscount)
    });

    line.set('discountedLinePrice', discountedLinePrice);
  }
};