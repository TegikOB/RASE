/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, enyo, _ */

// Percentage discount per total amount
OB.Model.Discounts.discountRules['9707DE71F91549DB80CCB2F094E951EA'] = {
  async: false,
  implementation: function (discountRule, receipt, line) {

    // Calculate total without my discounts...
    var total = 0;
    receipt.get('lines').each(function (l) {
      total = OB.DEC.add(total, l.getGross());
      _.each(l.get('promotions'), function (p) {
        if (p.ruleId === '9707DE71F91549DB80CCB2F094E951EA') {
          total = OB.DEC.add(total, p.amt);
        }
      });
    });
    // reset promotion in all lines
    receipt.get('lines').each(function (l) {
      receipt.removePromotion(l, discountRule);
    });

    if (total >= discountRule.get('obdiscTotalreceipt')) {
      // Add promotion in all lines 
      receipt.get('lines').each(function (l) {

        var discount = OB.DEC.toBigDecimal(l.getGross()).multiply(OB.DEC.toBigDecimal(discountRule.get('obdiscTotalpercdisc')).divide(new BigDecimal('100'), 20, OB.DEC.getRoundingMode()));
        discount = OB.DEC.toNumber(discount);

        receipt.addPromotion(l, discountRule, {
          amt: discount
        });
      });
    }
  }
};