/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone*/

// Buy X pay Y of different product
OB.Model.Discounts.discountRules['312D41071ED34BA18B748607CA679F44'] = {
  async: false,
  implementation: function (discountRule, receipt, line) {
    var qty = BigDecimal.prototype.ZERO,
        x, y, chunks, i, ln, price, subtype, totalPrice = BigDecimal.prototype.ZERO,
        distributed, amt, distributionAmt = BigDecimal.prototype.ZERO,
        distributionQty = 0,
        distribution = [],
        applyingToLinesCollection = Backbone.Collection.extend({
        comparator: function (line) {
          // To group sort by price desc, so we generate the best (for client) possible group
          return -(line.get('discountedLinePrice') || line.get('price'));
        }
      }),
        group, lQty, checkedQty, nextY, chunksInThisGroup, alerts, unitsToDiscount, unitsToCheck, discountedQty, avgPrice, applyingToLines = new applyingToLinesCollection(),
        promotionCandidates = line.get('promotionCandidates') || [];

    // This line is candidate for this promotion
    promotionCandidates.push(discountRule.id);
    line.set('promotionCandidates', promotionCandidates);

    // look for other lines that are candidates to apply this same rule
    receipt.get('lines').forEach(function (l) {
      if (l.get('promotionCandidates')) {
        l.get('promotionCandidates').forEach(function (candidateRule) {
          if (candidateRule === discountRule.id) {
            applyingToLines.add(l);
            qty = qty.add(OB.DEC.toBigDecimal(l.get('qty')));
          }
        });
      }
    });

    // apply the rule
    x = discountRule.get('oBDISCX');
    y = discountRule.get('oBDISCY');
    subtype = discountRule.get('oBDISCSubtype') || 'CHEAPEST';

    if (OB.DEC.toNumber(qty) >= x) {
      // Enforce stop cascade after applying this rule
      discountRule.set('applyNext', false, {
        silent: true
      });

      chunks = OB.DEC.toNumber(qty.divideInteger(OB.DEC.toBigDecimal(x)));
      // Do the group
      unitsToCheck = chunks * x;
      group = new applyingToLinesCollection();
      for (i = 0; i < applyingToLines.length; i++) {
        ln = applyingToLines.at(i);

        if (unitsToCheck > 0) {
          if (ln.get('qty') > unitsToCheck) {
            discountedQty = unitsToCheck;
          } else {
            discountedQty = ln.get('qty');
          }
          unitsToCheck -= discountedQty;
          totalPrice = totalPrice.add(OB.DEC.toBigDecimal(ln.get('discountedLinePrice') || ln.get('price')).multiply(OB.DEC.toBigDecimal(discountedQty)));
          group.add(ln);
        } else {
          // discount has been completely applied, now ensure there's no other lines with it
          receipt.removePromotion(ln, discountRule);
        }
      }

      // calculate discount
      unitsToDiscount = chunks * (x - y);

      if (subtype === 'AVG') {
        avgPrice = totalPrice.divide(OB.DEC.toBigDecimal(chunks * x));
        unitsToCheck = chunks * x;
      }

      distributed = discountRule.get('oBDISCDistribute');
      nextY = x;
      checkedQty = BigDecimal.prototype.ZERO;
      for (i = 0; i < group.length; i++) {
        ln = group.at(i);
        price = OB.DEC.toBigDecimal(ln.get('discountedLinePrice') || ln.get('price'));
        lQty = OB.DEC.toBigDecimal(ln.get('qty'));
        checkedQty = checkedQty.add(lQty);

        if (subtype === 'CHEAPEST') {
          if (OB.DEC.toNumber(checkedQty) < nextY) {
            // Create a fake discount to mark this line as used by a discount
            // preventing cascade in this way
            receipt.addPromotion(ln, discountRule, {
              actualAmt: 0,
              hidden: true
            });
          } else {
            chunksInThisGroup = 0;
            while (nextY <= OB.DEC.toNumber(checkedQty)) {
              chunksInThisGroup += x - y;
              nextY += x;
            }
            amt = price.multiply(OB.DEC.toBigDecimal(chunksInThisGroup));
            if (distributed) {
              distributionAmt = distributionAmt.add(amt);
              distributionQty += chunksInThisGroup;
              distribution[i] = OB.DEC.toNumber(amt);
            } else {
              receipt.addPromotion(ln, discountRule, {
                amt: OB.DEC.toNumber(amt)
              });
            }
          }
        } else { // Discount AVG
          if (ln.get('qty') > unitsToCheck) {
            discountedQty = unitsToCheck;
          } else {
            discountedQty = ln.get('qty');
          }
          unitsToCheck -= discountedQty;
          discountedQty = OB.DEC.toBigDecimal(discountedQty);

          receipt.addPromotion(ln, discountRule, {
            amt: OB.DEC.toNumber(discountedQty.multiply(price).multiply(OB.DEC.toBigDecimal(unitsToDiscount)).multiply(avgPrice).divide(totalPrice))
          });
        }
      }
      // Distribute the discount among all lines, but display to the user the same info as it was not distributed
      // distribution is just to be internally managed
      if (distributed && (subtype === 'CHEAPEST')) {
        unitsToDiscount = chunks * (x - y);
        for (i = 0; i < applyingToLines.length; i++) {
          ln = applyingToLines.at(i);
          price = OB.DEC.toBigDecimal(ln.get('discountedLinePrice') || ln.get('price'));
          amt = OB.DEC.toNumber(OB.DEC.toBigDecimal(ln.get('qty')).multiply(price).multiply(distributionAmt).multiply(OB.DEC.toBigDecimal(unitsToDiscount)).divide(totalPrice.multiply(OB.DEC.toBigDecimal(distributionQty))));
          receipt.addPromotion(ln, discountRule, {
            actualAmt: amt,
            amt: distribution[i]
          });
        }
      }
    } else {
      // Cannot apply discount, not enough qty, ensure there is no line with this discount
      applyingToLines.forEach(function (ln) {
        receipt.removePromotion(ln, discountRule);
      });
    }

    if (OB.DEC.toNumber(qty) % x !== 0) {
      alerts = OB.I18N.getLabel('OBPOS_DiscountAlert', [line.get('product').get('_identifier'), discountRule.get('printName') || discountRule.get('name')]);
    }

    return {
      alerts: alerts
    };
  }
};