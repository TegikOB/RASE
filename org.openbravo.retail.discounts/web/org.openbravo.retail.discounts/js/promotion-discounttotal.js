/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, enyo, _ */

// Discount per total amount
OB.Model.Discounts.discountRules['4183C8EB7CDA472D9E64521DC2504B15'] = {
  async: false,
  implementation: function (discountRule, receipt, line) {

    // Calculate total without my discounts...
    var total = 0;
    receipt.get('lines').each(function (l) {
      total = OB.DEC.add(total, l.getGross());
      _.each(l.get('promotions'), function (p) {
        if (p.ruleId === '4183C8EB7CDA472D9E64521DC2504B15') {
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
      var accumdiscount = 0;
      var totaldiscount = discountRule.get('obdiscTotalamountdisc');
      receipt.get('lines').each(function (l, index, list) {
        var discount;
        if (index < list.length - 1) {
          discount = OB.DEC.toNumber(OB.DEC.toBigDecimal(l.getGross()).multiply(OB.DEC.toBigDecimal(totaldiscount).divide(OB.DEC.toBigDecimal(total), 20, OB.DEC.getRoundingMode())));
        } else {
          discount = OB.DEC.sub(totaldiscount, accumdiscount);
        }
        accumdiscount = OB.DEC.add(accumdiscount, discount);
        receipt.addPromotion(l, discountRule, {
          amt: discount
        });
      });
    }
  }
};