/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone _*/

// Buy X and get Y as gift
OB.Model.Discounts.discountRules['94AEA884F5AD4EABB72322832B9C5172'] = {
  async: true,
  implementation: function (discountRule, receipt, line, listener) {
    var applyingToLines = new Backbone.Collection(),
        criteria, promotionCandidates = line.get('promotionCandidates') || [];

    // This line is candidate for this promotion
    promotionCandidates.push(discountRule.id);
    line.set('promotionCandidates', promotionCandidates);

    // look for other lines that are candidates to apply this same rule
    receipt.get('lines').forEach(function (l) {
      if (l.get('promotionCandidates')) {
        l.get('promotionCandidates').forEach(function (candidateRule) {
          if (candidateRule === discountRule.id) {
            applyingToLines.add(l);
          }
        });
      }
    });

    // Query local DB to know detail about the rule (products and quantities) 
    criteria = {
      priceAdjustment: discountRule.id
    };

    OB.Dal.find(OB.Model.DiscountFilterProduct, criteria, function (products) {
      var chunks, distributed, totalAmt, promotionAmt = 0,
          alerts = '';

      if (applyingToLines.length !== products.length) {
        // rule can't be applied because not all products are matched
        listener.trigger('completed');
        return;
      }

      products.forEach(function (product) {
        var i, line, applyNtimes, qtyToGetGift, qtyAsGift;
        if (chunks === 0) {
          // already decided not to apply
          return;
        }
        for (i = 0; i < applyingToLines.length; i++) {
          if (applyingToLines.at(i).get('product').id === product.get('product')) {
            line = applyingToLines.at(i);
            break;
          }
        }
        if (!line) {
          // cannot apply the rule
          chunks = 0;
          return;
        }
        product.set('receiptLine', line);
        if (_.isUndefined(product.get('obdiscQty')) || _.isNull(product.get('obdiscQty'))) {
          qtyToGetGift = 0;
        } else {
          qtyToGetGift = product.get('obdiscQty');
        }
        if (product.get('obdiscIsGift')) {
          if (_.isUndefined(product.get('obdiscGifqty')) || _.isNull(product.get('obdiscGifqty'))) {
            qtyAsGift = 0;
          } else {
            qtyAsGift = product.get('obdiscGifqty');
          }

          applyNtimes = Math.floor(line.get('qty') / (qtyToGetGift + qtyAsGift || 1));
        } else {
          applyNtimes = Math.floor(line.get('qty') / (qtyToGetGift || 1));
        }
        if (!chunks || applyNtimes < chunks) {
          chunks = applyNtimes;
        }
      });

      if (chunks === 0) {
        // Cannot apply, ensure the promotion is not applied
        applyingToLines.forEach(function (ln) {
          receipt.removePromotion(ln, discountRule);
        });
        listener.trigger('completed', {
          alerts: OB.I18N.getLabel('OBPOS_DiscountAlert', [line.get('product').get('_identifier'), discountRule.get('printName') || discountRule.get('name')])
        });
        return;
      }

      // There are products enough to apply the rule
      // Enforce stop cascade after applying this rule
      discountRule.set('applyNext', false, {
        silent: true
      });


      distributed = discountRule.get('oBDISCDistribute');

      if (distributed) {
        totalAmt = products.reduce(function (total, product) {
          var l = product.get('receiptLine');
          return OB.DEC.add(total, OB.DEC.mul(l.get('qty'), l.get('discountedLinePrice') || l.get('price')));
        }, OB.DEC.Zero);


        // Apply N chunks to the free products
        products.where({
          obdiscIsGift: true
        }).forEach(function (product) {
          var l = product.get('receiptLine'),
              lineAmt;

          lineAmt = OB.DEC.mul(OB.DEC.mul((l.get('discountedLinePrice') || l.get('price')), chunks), (product.get('obdiscQty') || 1));
          if (distributed) {
            promotionAmt += lineAmt;
          }
        });
      }

      // first loop calculated the total discount, now let's apply it
      products.forEach(function (product) {
        var l = product.get('receiptLine'),
            price = l.get('discountedLinePrice') || l.get('price'),
            actualAmt, giftQty;

        if (distributed) {
          actualAmt = l.get('qty') * price * promotionAmt / (totalAmt);
          if (product.get('obdiscIsGift')) {
            giftQty = product.get('obdiscGifqty');
            if (giftQty) {
              // gift products are shown to user as free
              receipt.addPromotion(l, discountRule, {
                amt: OB.DEC.mul(OB.DEC.mul(price, chunks), giftQty),
                actualAmt: actualAmt
              });
            } else {
              window.console.warn(OB.I18N.getLabel('OBPOS_DISCbuyXgiftYNotDefineGiftQty', [discountRule.get('printName') || discountRule.get('name'), l.get('product').get('_identifier')]));
              alerts += '\n' + OB.I18N.getLabel('OBPOS_DISCbuyXgiftYNotDefineGiftQty', [discountRule.get('printName') || discountRule.get('name'), l.get('product').get('_identifier')]);
            }
          } else {
            receipt.addPromotion(l, discountRule, {
              actualAmt: actualAmt
            });
          }
        } else {
          // not distributed
          if (product.get('obdiscIsGift')) {
            giftQty = product.get('obdiscGifqty');
            if (giftQty) {
              // apply just to free products N chunks
              receipt.addPromotion(l, discountRule, {
                amt: OB.DEC.mul(OB.DEC.mul(price, chunks), (product.get('obdiscGifqty') || 0))
              });
            } else {
              window.console.warn(OB.I18N.getLabel('OBPOS_DISCbuyXgiftYNotDefineGiftQty', [discountRule.get('printName') || discountRule.get('name'), l.get('product').get('_identifier')]));
              alerts += '\n' + OB.I18N.getLabel('OBPOS_DISCbuyXgiftYNotDefineGiftQty', [discountRule.get('printName') || discountRule.get('name'), l.get('product').get('_identifier')]);
            }
          } else {
            // create a fake discount to prevent cascade
            receipt.addPromotion(l, discountRule, {
              actualAmt: 0,
              hidden: true
            });
          }
        }
      });
      if (alerts) {
        listener.trigger('completed', {
          alerts: alerts
        });
      } else {
        listener.trigger('completed');
      }
    }, function () {
      window.console.error('Error querying for products', discountRule, receipt, line, arguments);
      listener.trigger('completed');
    });
  }
};