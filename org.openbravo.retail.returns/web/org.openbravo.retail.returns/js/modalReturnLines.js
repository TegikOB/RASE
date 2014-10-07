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

  enyo.kind({
    name: 'OB.UI.CheckboxButtonAll',
    kind: 'OB.UI.CheckboxButton',
    classes: 'modal-dialog-btn-check span1',
    style: 'width: 8%',
    events: {
      onCheckedAll: ''
    },
    handlers: {
      onAllSelected: 'allSelected'
    },
    allSelected: function (inSender, inEvent) {
      if (inEvent.allSelected) {
        this.check();
      } else {
        this.unCheck();
      }
      return true;
    },
    tap: function () {
      this.inherited(arguments);
      this.doCheckedAll({
        checked: this.checked
      });
    }
  });

  enyo.kind({
    name: 'OB.UI.CheckboxButtonReturn',
    kind: 'OB.UI.CheckboxButton',
    classes: 'modal-dialog-btn-check span1',
    style: 'width: 8%',
    handlers: {
      onCheckAll: 'checkAll'
    },
    events: {
      onLineSelected: ''
    },
    checkAll: function (inSender, inEvent) {
      if (inEvent.checked) {
        this.check();
      } else {
        this.unCheck();
      }
      this.parent.$.quantity.setDisabled(!inEvent.checked);
      this.parent.$.qtyplus.setDisabled(!inEvent.checked);
      this.parent.$.qtyminus.setDisabled(!inEvent.checked);
    },
    tap: function () {
      this.inherited(arguments);
      this.parent.$.quantity.setDisabled(!this.checked);
      this.parent.$.qtyplus.setDisabled(!this.checked);
      this.parent.$.qtyminus.setDisabled(!this.checked);

      if (this.checked) {
        this.parent.$.quantity.focus();
      }
      this.doLineSelected({
        selected: this.checked
      });
    }
  });

  enyo.kind({
    name: 'OB.UI.EditOrderLine',
    style: 'border-bottom: 1px solid #cccccc; text-align: center; color: black; padding-top: 9px;',
    handlers: {
      onApplyChange: 'applyChange'
    },
    events: {
      onCorrectQty: ''
    },
    applyChange: function (inSender, inEvent) {
      var me = this;
      var index = inEvent.lines.indexOf(this.newAttribute);
      var line, promotionsfactor;
      if (index !== -1) {
        if (this.$.checkboxButtonReturn.checked) {
          var initialQty = inEvent.lines[index].quantity;
          inEvent.lines[index].quantity = this.$.quantity.getValue();
          // update promotions amount to the quantity returned
          enyo.forEach(this.newAttribute.promotions, function (p) {
            if (!OB.UTIL.isNullOrUndefined(p)) {
              p.amt = OB.DEC.mul(p.amt, (me.$.quantity.getValue() / initialQty));
              p.actualAmt = OB.DEC.mul(p.actualAmt, (me.$.quantity.getValue() / initialQty));
            }
          });
        } else {
          inEvent.lines.splice(index, 1);
        }
      }
    },
    components: [{
      kind: 'OB.UI.CheckboxButtonReturn'
    }, {
      name: 'product',
      classes: 'span4',
      style: 'line-height: 40px; font-size: 17px;'
    }, {
      name: 'maxQuantity',
      classes: 'span2',
      style: 'line-height: 40px; font-size: 17px; width: 70px;'
    }, {
      name: 'qtyminus',
      kind: 'OB.UI.SmallButton',
      style: 'width: 40px',
      classes: 'btnlink-gray btnlink-cashup-edit span1',
      content: '-',
      ontap: 'subUnit'
    }, {
      kind: 'enyo.Input',
      type: 'text',
      classes: 'input span1',
      style: 'margin-right: 2px; text-align: center;',
      name: 'quantity',
      isFirstFocus: true,
      selectOnFocus: true,
      autoKeyModifier: 'num-lock',
      onchange: 'validate'
    }, {
      name: 'qtyplus',
      kind: 'OB.UI.SmallButton',
      style: 'width: 40px',
      classes: 'btnlink-gray btnlink-cashup-edit span1',
      content: '+',
      ontap: 'addUnit'
    }, {
      name: 'price',
      classes: 'span2',
      style: 'line-height: 40px; font-size: 17px;'
    }, {
      style: 'clear: both;'
    }],
    addUnit: function (inSender, inEvent) {
      var units = parseInt(this.$.quantity.getValue(), 10);
      if (!isNaN(units) && units < this.$.quantity.getAttribute('max')) {
        this.$.quantity.setValue(units + 1);
        this.validate();
      }

    },
    subUnit: function (inSender, inEvent) {
      var units = parseInt(this.$.quantity.getValue(), 10);
      if (!isNaN(units) && units > this.$.quantity.getAttribute('min')) {
        this.$.quantity.setValue(units - 1);
        this.validate();
      }
    },
    validate: function () {
      var value, maxValue;
      value = this.$.quantity.getValue();
      try {
        value = parseFloat(this.$.quantity.getValue());
      } catch (e) {
        this.addStyles('background-color: red');
        this.doCorrectQty({
          correctQty: false
        });
        return true;
      }
      maxValue = OB.DEC.toNumber(OB.DEC.toBigDecimal(this.$.quantity.getAttribute('max')));

      if (!_.isNumber(value) || _.isNaN(value)) {
        this.addStyles('background-color: red');
        this.doCorrectQty({
          correctQty: false
        });
        return true;
      }

      value = OB.DEC.toNumber(OB.DEC.toBigDecimal(value));
      this.$.quantity.setValue(value);


      if (value > maxValue || value <= 0) {
        this.addStyles('background-color: red');
        this.doCorrectQty({
          correctQty: false
        });
      } else {
        this.addStyles('background-color: white');
        this.doCorrectQty({
          correctQty: true
        });
        return true;
      }
    },
    initComponents: function () {
      this.inherited(arguments);

      this.$.product.setContent(this.newAttribute.name);
      this.$.maxQuantity.setContent(this.newAttribute.quantity);
      this.$.quantity.setDisabled(true);
      this.$.qtyplus.setDisabled(true);
      this.$.qtyminus.setDisabled(true);
      this.$.quantity.setValue(this.newAttribute.quantity);
      this.$.quantity.setAttribute('max', this.newAttribute.quantity);
      this.$.quantity.setAttribute('min', OB.DEC.One);
      this.$.price.setContent(this.newAttribute.priceIncludesTax ? this.newAttribute.unitPrice : this.newAttribute.baseNetUnitPrice);
      if (this.newAttribute.promotions.length > 0) {
        this.$.quantity.addStyles('margin-bottom:0px');
        enyo.forEach(this.newAttribute.promotions, function (d) {
          if (d.hidden) {
            // continue
            return;
          }
          this.createComponent({
            style: 'display: block; color:gray; font-size:13px; line-height: 20px;',
            components: [{
              content: '-- ' + d.name,
              attributes: {
                style: 'float: left; width: 60%;'
              }
            }, {
              content: OB.I18N.formatCurrency(-d.amt),
              attributes: {
                style: 'float: left; width: 31%; text-align: right;'
              }
            }, {
              style: 'clear: both;'
            }]
          });
        }, this);

      }
      // shipment info
      if (!OB.UTIL.isNullOrUndefined(this.newAttribute.shiplineNo)) {
        this.createComponent({
          style: 'display: block; color:gray; font-size:13px; line-height: 20px;',
          components: [{
            content: this.newAttribute.shipment + ' - ' + this.newAttribute.shiplineNo,
            attributes: {
              style: 'float: left; width: 60%;'
            }
          }, {
            style: 'clear: both;'
          }]
        });
      }
    }
  });

  enyo.kind({
    kind: 'OB.UI.ModalDialogButton',
    name: 'OB.UI.ReturnReceiptDialogApply',
    events: {
      onApplyChanges: '',
      onCallbackExecutor: '',
      onCheckQty: ''
    },
    tap: function () {
      if (this.doCheckQty()) {
        return true;
      }
      if (this.doApplyChanges()) {
        this.doCallbackExecutor();
        this.doHideThisPopup();
      }
    },
    initComponents: function () {
      this.inherited(arguments);
      this.setContent(OB.I18N.getLabel('OBMOBC_LblApply'));
    }
  });

  enyo.kind({
    kind: 'OB.UI.ModalDialogButton',
    name: 'OB.UI.ReturnReceiptDialogCancel',
    tap: function () {
      this.doHideThisPopup();
    },
    initComponents: function () {
      this.inherited(arguments);
      this.setContent(OB.I18N.getLabel('OBMOBC_LblCancel'));
    }
  });

  enyo.kind({
    name: 'OB.UI.ModalReturnReceipt',
    kind: 'OB.UI.ModalAction',
    correctQty: true,
    handlers: {
      onApplyChanges: 'applyChanges',
      onCallbackExecutor: 'callbackExecutor',
      onCheckedAll: 'checkedAll',
      onCheckQty: 'checkQty',
      onCorrectQty: 'changeCorrectQty',
      onLineSelected: 'lineSelected'
    },
    bodyContent: {
      kind: 'Scroller',
      maxHeight: '225px',
      style: 'background-color: #ffffff;',
      thumb: true,
      horizontal: 'hidden',
      components: [{
        name: 'attributes'
      }]
    },
    bodyButtons: {
      components: [{
        kind: 'OB.UI.ReturnReceiptDialogApply'
      }, {
        kind: 'OB.UI.ReturnReceiptDialogCancel'
      }]
    },
    applyChanges: function (inSender, inEvent) {
      this.waterfall('onApplyChange', {
        lines: this.args.args.order.receiptLines
      });
      return true;
    },
    callbackExecutor: function (inSender, inEvent) {
      var me = this;
      var nameLocation = "";
      OB.Dal.get(OB.Model.BPLocation, me.args.args.order.bpLocId, function (bpLoc) {
        me.nameLocation = bpLoc.get('name');
      });

      OB.Dal.get(OB.Model.BusinessPartner, this.args.args.order.bp, function (bp) {
        _.each(me.args.args.order.receiptLines, function (line) {
          _.each(line.promotions, function (promotion) {
            promotion.amt = -promotion.amt;
            promotion.actualAmt = -promotion.actualAmt;
          });
          OB.Dal.get(OB.Model.Product, line.id, function (prod) {
            prod.set('ignorePromotions', true);
            prod.set('standardPrice', line.priceIncludesTax ? line.unitPrice : line.baseNetUnitPrice);
            me.args.args.context.model.get('order').createLine(prod, -line.quantity, null, {
              'originalOrderLineId': line.lineId,
              'originalDocumentNo': me.args.args.order.documentNo,
              'skipApplyPromotions': true,
              'promotions': line.promotions,
              'shipmentlineId': line.shipmentlineId
            });
            me.args.args.cancelOperation = true;
            bp.set('locId', me.args.args.order.bpLocId);
            bp.set('locName', me.nameLocation);
            me.args.args.context.model.get('order').set('bp', bp);
            me.args.args.context.model.get('order').calculateGross();
            me.args.args.context.model.get('order').save();
            me.args.args.context.model.get('orderList').saveCurrent();
            OB.MobileApp.model.hookManager.callbackExecutor(me.args.args, me.args.callbacks);
          });
        });
      });
    },
    checkedAll: function (inSender, inEvent) {
      if (inEvent.checked) {
        this.selectedLines = this.numberOfLines;
        this.allSelected = true;
      } else {
        this.selectedLines = 0;
        this.allSelected = false;
      }
      this.waterfall('onCheckAll', {
        checked: inEvent.checked
      });

      return true;
    },
    checkQty: function (inSender, inEvent) {
      if (!this.correctQty) {
        return true;
      }
    },
    changeCorrectQty: function (inSender, inEvent) {
      this.correctQty = inEvent.correctQty;
    },
    lineSelected: function (inSender, inEvent) {
      if (inEvent.selected) {
        this.selectedLines += 1;
      } else {
        this.selectedLines -= 1;
      }
      if (this.selectedLines === this.numberOfLines) {
        this.allSelected = true;
        this.waterfall('onAllSelected', {
          allSelected: this.allSelected
        });
        return true;
      } else {
        if (this.allSelected) {
          this.allSelected = false;
          this.waterfall('onAllSelected', {
            allSelected: this.allSelected
          });
        }
      }
    },
    splitShipmentLines: function (lines) {
      var newlines = [];

      enyo.forEach(lines, function (line) {
        if (line.shipmentlines && line.shipmentlines.length > 1) {
          enyo.forEach(line.shipmentlines, function (sline) {
            var attr, splitline = {};
            for (attr in line) {
              if (line.hasOwnProperty(attr) && !OB.UTIL.isNullOrUndefined(line[attr]) && typeof line[attr] !== 'object') {
                splitline[attr] = line[attr];
              }
            }
            splitline.compname = line.lineId + sline.shipLineId;
            splitline.quantity = sline.qty;
            splitline.shiplineNo = sline.shipmentlineNo;
            splitline.shipment = sline.shipment;
            splitline.shipmentlineId = sline.shipLineId;
            // delete confusing properties
            delete splitline.linegrossamount;
            delete splitline.warehouse;
            delete splitline.warehousename;
            // split promotions
            splitline.promotions = [];
            if (line.promotions.length > 0) {
              enyo.forEach(line.promotions, function (p) {
                if (!OB.UTIL.isNullOrUndefined(p)) {
                  var attr, splitpromo = {};
                  for (attr in p) {
                    if (p.hasOwnProperty(attr) && !OB.UTIL.isNullOrUndefined(p[attr]) && typeof p[attr] !== 'object') {
                      splitpromo[attr] = p[attr];
                    }
                  }
                  splitpromo.amt = OB.DEC.mul(p.amt, (splitline.quantity / line.quantity));
                  splitpromo.actualAmt = OB.DEC.mul(p.actualAmt, (splitline.quantity / line.quantity));
                  splitline.promotions.push(splitpromo);
                }
              });
            }

            newlines.push(splitline);
          }, this);
        } else {
          line.compname = line.lineId;
          if (line.shipmentlines.length === 1) {
            line.shipmentlineId = line.shipmentlines[0].shipLineId;
          }
          // delete confusing properties
          delete line.linegrossamount;
          delete line.warehouse;
          delete line.warehousename;
          newlines.push(line);
        }
      }, this);
      return newlines;
    },
    executeOnShow: function () {
      var me = this,
          newArray = [];
      this.$.bodyContent.$.attributes.destroyComponents();
      this.$.header.destroyComponents();
      this.$.header.createComponent({
        name: 'CheckAllHeaderDocNum',
        style: 'text-align: center; color: white;',
        components: [{
          content: me.args.args.order.documentNo,
          name: 'documentNo',
          classes: 'span12',
          style: 'line-height: 40px; font-size: 24px;'
        }, {
          style: 'clear: both;'
        }]
      });
      if (!this.$.header.$.checkboxButtonAll) {
        this.$.header.addStyles('padding-bottom: 0px; margin: 0px; height: 103px;');

        this.$.header.createComponent({
          name: 'CheckAllHeader',
          style: 'padding-top: 5px; border-bottom: 3px solid #cccccc; text-align: center; color: black; margin-top: 10px; padding-bottom: 8px;  font-weight: bold; background-color: white',
          components: [{
            kind: 'OB.UI.CheckboxButtonAll'
          }, {
            content: OB.I18N.getLabel('OBRETUR_LblProductName'),
            name: 'productNameLbl',
            classes: 'span4',
            style: 'line-height: 40px; font-size: 17px;'
          }, {
            name: 'maxQtyLbl',
            content: OB.I18N.getLabel('OBRETUR_LblMaxQty'),
            classes: 'span2',
            style: 'line-height: 40px; font-size: 17px; width: 70px;'
          }, {
            content: OB.I18N.getLabel('OBRETUR_LblQty'),
            name: 'qtyLbl',
            classes: 'span3',
            style: 'line-height: 40px; font-size: 17px;'
          }, {
            content: OB.I18N.getLabel('OBRETUR_LblPrice'),
            name: 'priceLbl',
            classes: 'span2',
            style: 'line-height: 40px; font-size: 17px;'
          }, {
            style: 'clear: both;'
          }]
        });
      } else {
        this.$.header.$.checkboxButtonAll.unCheck();
      }
      newArray = _.filter(this.args.args.order.receiptLines, function (line) {
        return line.quantity > 0;
      });
      newArray = this.splitShipmentLines(newArray);
      this.args.args.order.receiptLines = newArray;
      this.numberOfLines = 0;
      this.selectedLines = 0;
      this.allSelected = false;
      enyo.forEach(this.args.args.order.receiptLines, function (line) {
        this.$.bodyContent.$.attributes.createComponent({
          kind: 'OB.UI.EditOrderLine',
          name: 'line_' + line.compname,
          newAttribute: line
        });
        this.numberOfLines += 1;
      }, this);

      this.$.bodyContent.$.attributes.render();
      this.$.header.render();
    },
    initComponents: function () {
      this.inherited(arguments);
      this.attributeContainer = this.$.bodyContent.$.attributes;
    }
  });
  OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
    kind: 'OB.UI.ModalReturnReceipt',
    name: 'modalReturnReceipt'
  });

}());