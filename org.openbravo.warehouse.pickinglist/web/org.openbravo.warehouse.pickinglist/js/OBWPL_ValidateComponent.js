/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

isc.defineClass('OBPickValidateProcess', isc.VLayout);

isc.OBPickValidateProcess.addProperties({
  width: '100%',
  height: '100%',
  overflow: 'auto',
  autoSize: false,
  waitForFocus: [],
  setWaitForFocus: function (time) {
    var me = this;
    if (!time) {
      time = 2500;
    }
    this.waitForFocus.push(true);
    setTimeout(function () {
      me.waitForFocus.splice(0, 1);
    }, time);
  },
  barcodeForm: null,
  forceFocusToBarcode: true,
  initWidget: function () {
    var pickGrid, validateButton, validateButtonLayout, processButton, cancelButton, buttonLayout = [],
        me = this;

    this.focusCheckInterval = setInterval(function () {
      var activeTab;
      if (!me || !me.parentWindow || !me.parentWindow.parentElement) {
        return false;
      }
      activeTab = me.parentWindow.parentElement.parentElement.getSelectedTab();
      if (me.forceFocusToBarcode && activeTab && activeTab.windowId === me.parentWindow.windowId && me.waitForFocus.length === 0) {
        me.barcodeForm.focusInItem('Barcode');
      }
    }, 500);

    this.messageBar = isc.OBMessageBar.create({
      visibility: 'hidden',
      view: this
    });

    pickGrid = isc.OBPickValidateProcessGrid.create({
      ID: OB.Utilities.generateRandomString(10, true, false, false, false),
      //Random ID to avoid Smartclient error when loading the process popup the second time is invoked
      data: this.pickGridData,
      theLayout: this
    });

    this.barcodeForm = isc.DynamicForm.create({
      titleOrientation: 'top',
      draw: function () {
        this.Super('draw', arguments);
        this.focusInItem('Barcode');
      },
      fields: [{
        name: 'Quantity',
        title: OB.I18N.getLabel('OBWPL_Quantity'),
        type: 'OBSpinnerItem',
        width: OB.Styles.OBWPL.PickValidateProcess.quantityWidth,
        required: true,
        showErrorIcon: false,
        defaultValue: 1,
        keyPressFilter: '[0-9]',
        min: 1,
        max: 9999,
        focus: function () {
          me.setWaitForFocus(2500);
          return this.Super('focus', arguments);
        },
        mouseDown: function () {
          var value = this.getValue(),
              thisItem = this;
          setTimeout(function () {
            if (value !== thisItem.getValue()) {
              me.barcodeForm.focusInItem('Barcode');
            }
          }, 10);
          return this.Super('mouseDown', arguments);
        },
        keyDown: function () {
          if (isc.EH.getKey() === 'Enter' && !isc.EH.ctrlKeyDown() && !isc.EH.altKeyDown() && !isc.EH.shiftKeyDown()) {
            me.barcodeForm.focusInItem('Barcode');
            return false;
          } else {
            me.setWaitForFocus(1500);
            return this.Super('keyDown', arguments);
          }
        }
      }, {
        name: 'Barcode',
        title: OB.I18N.getLabel('OBWPL_Barcode'),
        type: 'OBTextItem',
        width: OB.Styles.OBWPL.PickValidateProcess.barcodeWidth,
        required: true,
        showErrorIcon: false,
        defaultValue: '',
        keyDown: function () {
          if (isc.EH.getKey() === 'Enter' && !isc.EH.ctrlKeyDown() && !isc.EH.altKeyDown() && !isc.EH.shiftKeyDown()) {
            validateButton.click();
            return false;
          }
        }
      }]
    });

    this.firstFocusedItem = this.barcodeForm;

    validateButton = isc.OBLinkButtonItem.create({
      layoutTopMargin: OB.Styles.OBWPL.PickValidateProcess.validateButtonTopMargin,
      title: '[ ' + OB.I18N.getLabel('OBWPL_ValidateBarcode') + ' ]',
      click: function () {
        var code, qty, validated = true;
        if (me.barcodeForm.getItem('Quantity').getValue() !== null && me.barcodeForm.getItem('Quantity').getValue() !== '' && me.barcodeForm.getItem('Barcode').getValue() !== null && me.barcodeForm.getItem('Barcode').getValue() !== '') {
          code = me.barcodeForm.getItem('Barcode').getValue();
          qty = me.barcodeForm.getItem('Quantity').getValue();
          me.barcodeForm.getItem('Quantity').setValue('1');
          me.barcodeForm.getItem('Barcode').setValue('');
          validated = pickGrid.validateCode(code, qty);
        }
        if (validated === false) {
          me.waitForFocus.push(true);
          me.addMembers(isc.HTMLFlow.create({
            width: 1,
            contents: '<audio controls="controls" autoplay="true" style="display: none;"><source src="' + OB.Styles.OBWPL.PickValidateProcess.errorSound + '.mp3" type="audio/mpeg"><source src="' + OB.Styles.OBWPL.PickValidateProcess.errorSound + '.ogg" type="audio/ogg"><embed src="' + OB.Styles.OBWPL.PickValidateProcess.errorSound + '.mp3" hidden="true" autostart="true" loop="false" /></audio>'
          }));
          isc.warn(OB.I18N.getLabel('OBWPL_Alert_WrongBarcode'), function () {
            me.waitForFocus.splice(0, 1);
            return true;
          }, {
            icon: '[SKINIMG]Dialog/error.png',
            title: OB.I18N.getLabel('OBUIAPP_Error')
          });
        }
      },
      keyUp: function () {
        if (isc.EH.getKey() === 'Enter' && !isc.EH.ctrlKeyDown() && !isc.EH.altKeyDown() && !isc.EH.shiftKeyDown()) {
          me.barcodeForm.focusInItem('Barcode');
        } else if (isc.EH.getKey() === 'Tab') {
          me.setWaitForFocus(1200);
        }
        return this.Super('keyUp', arguments);
      }
    });

    validateButtonLayout = isc.Layout.create({
      layoutTopMargin: OB.Styles.OBWPL.PickValidateProcess.validateButtonLayoutTopMargin,
      members: [validateButton]
    });

    processButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBWPL_Process'),
      click: function () {
        var areErrors = false,
            i;

        for (i = 0; i < pickGrid.getTotalRows(); i++) {
          pickGrid.updateRecordStyle(null, i, true);
          if (pickGrid.getRecord(i).qtyPending !== 0) {
            areErrors = true;
          }
        }

        if (areErrors) {
          isc.warn(OB.I18N.getLabel('OBWPL_Alert_PendingToValidate'), function () {
            return true;
          }, {
            icon: '[SKINIMG]Dialog/error.png',
            title: OB.I18N.getLabel('OBUIAPP_Error')
          });
        } else {
          me.closePopup();
          me.processAction();
          return true;
        }
      },
      keyUp: function () {
        if (isc.EH.getKey() === 'Tab') {
          me.setWaitForFocus(1200);
        }
        return this.Super('keyUp', arguments);
      }
    });

    cancelButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
      click: function () {
        me.closePopup();
      },
      keyUp: function () {
        if (isc.EH.getKey() === 'Tab') {
          me.setWaitForFocus(1200);
        }
        return this.Super('keyUp', arguments);
      }
    });

    buttonLayout.push(this.barcodeForm);
    buttonLayout.push(validateButtonLayout);

    buttonLayout.push(isc.HLayout.create({
      width: OB.Styles.OBWPL.PickValidateProcess.bottomComponentsSeparatorWidth
    }));

    if (Object.prototype.toString.call(me.processAction) === '[object Function]') {
      buttonLayout.push(processButton);
    }
    buttonLayout.push(isc.HLayout.create({
      width: OB.Styles.OBWPL.PickValidateProcess.bottomRightButtonsSeparatorWidth
    }));
    buttonLayout.push(cancelButton);

    this.members = [this.messageBar, pickGrid, isc.HLayout.create({
      align: 'center',
      width: '100%',
      height: OB.Styles.Process.PickAndExecute.buttonLayoutHeight,
      members: [isc.HLayout.create({
        width: 1,
        layoutTopMargin: OB.Styles.OBWPL.PickValidateProcess.bottomRightButtonsLayoutTopMargin,
        overflow: 'visible',
        styleName: this.buttonBarStyleName,
        height: OB.Styles.OBWPL.PickValidateProcess.bottomRightButtonsLayoutHeight,
        defaultLayoutAlign: OB.Styles.OBWPL.PickValidateProcess.bottomRightButtonsLayoutAlign,
        members: buttonLayout
      })]
    })];

    this.Super('initWidget', arguments);
  },
  destroy: function () {
    clearInterval(this.focusCheckInterval);
    return this.Super('destroy', arguments);
  },
  closePopup: function () {
    this.parentElement.parentElement.closeClick(); // Super call
  },
  closeClick: function () {
    return true;
  }
});


isc.defineClass('OBPickValidateProcessGrid', isc.OBGrid);

isc.OBPickValidateProcessGrid.addProperties({
  enforceVClipping: true,
  selectionType: 'single',
  fixedRecordHeights: true,
  cellHeight: OB.Styles.Process.PickAndExecute.gridCellHeight,
  width: '100%',
  height: '100%',
  canReorderFields: true,
  //showAllRecords: true,
  drawAheadRatio: 6,
  showRecordComponents: true,
  showRecordComponentsByCell: true,
  initWidget: function () {
    this.fields = [{
      name: 'barcode',
      title: OB.I18N.getLabel('OBWPL_Barcode'),
      width: OB.Styles.OBWPL.PickValidateProcessGrid.barcodeColumnWidth
    }, {
      name: 'product',
      title: OB.I18N.getLabel('OBWPL_Product'),
      width: OB.Styles.OBWPL.PickValidateProcessGrid.productColumnWidth
    }, {
      name: 'quantity',
      title: OB.I18N.getLabel('OBWPL_Quantity'),
      width: OB.Styles.OBWPL.PickValidateProcessGrid.quantityColumnWidth
    }, {
      name: 'qtyVerified',
      title: OB.I18N.getLabel('OBWPL_QtyVerified'),
      width: OB.Styles.OBWPL.PickValidateProcessGrid.qtyVerifiedColumnWidth
    }, {
      name: 'qtyPending',
      title: OB.I18N.getLabel('OBWPL_QtyPending'),
      width: OB.Styles.OBWPL.PickValidateProcessGrid.qtyPendingColumnWidth
    }, {
      name: 'iconStatus',
      showTitle: false,
      canSort: false,
      canReorder: false,
      canHide: false,
      width: OB.Styles.OBWPL.PickValidateProcessGrid.iconStatusColumnWidth
    }];
    this.Super('initWidget', arguments);
  },
  createRecordComponent: function (record, colNum) {
    var theGrid = this,
        fieldName = this.getFieldName(colNum),
        item = null,
        qtyItemType = (isc.Browser.isMoz ? 'OBTextItem' : 'OBSpinnerItem'); // To avoid UI problem with Smartclient + Firefox rendering the OBSpinnerItem as a record component in the grid
    if (fieldName === 'qtyVerified') {
      item = isc.DynamicForm.create({
        width: OB.Styles.OBWPL.PickValidateProcessGrid.qtyVerifiedInputWidth,
        fields: [{
          name: 'Quantity',
          type: qtyItemType,
          showTitle: false,
          changeOnKeypress: true,
          cellStyle: '',
          //To avoid use default OBFormItem that adds a padding
          width: OB.Styles.OBWPL.PickValidateProcessGrid.qtyVerifiedInputWidth,
          required: true,
          showErrorIcon: false,
          keyPressFilter: '[0-9]',
          min: 0,
          change: function (form, item, value, oldValue) {
            theGrid.updateQuantity(record, null, value, true);
          },
          init: function () {
            this.Super('init', arguments);
            if (!record.qtyVerified) {
              record.qtyVerified = 0;
            }
            this.setValue(record.qtyVerified);
            record.qtyPending = record.quantity - record.qtyVerified;
          },
          focus: function () {
            theGrid.selectSingleRecord(record);
            theGrid.theLayout.setWaitForFocus(2500);
            return this.Super('focus', arguments);
          },
          mouseDown: function () {
            var value = this.getValue(),
                thisItem = this;
            setTimeout(function () {
              if (value !== thisItem.getValue()) {
                theGrid.theLayout.barcodeForm.focusInItem('Barcode');
              }
            }, 10);
            return this.Super('mouseDown', arguments);
          },
          keyDown: function () {
            if (isc.EH.getKey() === 'Enter' && !isc.EH.ctrlKeyDown() && !isc.EH.altKeyDown() && !isc.EH.shiftKeyDown()) {
              theGrid.theLayout.barcodeForm.focusInItem('Barcode');
              return false;
            } else {
              theGrid.theLayout.setWaitForFocus(1500);
              return this.Super('keyDown', arguments);
            }
          }
        }]
      });
    } else if (fieldName === 'iconStatus') {
      item = isc.Img.create({
        width: OB.Styles.OBWPL.PickValidateProcessGrid.iconStatusImgWidth,
        height: OB.Styles.OBWPL.PickValidateProcessGrid.iconStatusImgHeight,
        imageType: 'normal',
        setIcon: function (status) {
          if (status === 'success') {
            this.setSrc(OB.Styles.OBWPL.PickValidateProcessGrid.iconStatusSuccessSrc);
          } else if (status === 'error') {
            this.setSrc(OB.Styles.OBWPL.PickValidateProcessGrid.iconStatusErrorSrc);
          } else {
            this.setSrc(OB.Styles.OBWPL.PickValidateProcessGrid.iconStatusBlankSrc);
          }
        },
        initWidget: function () {
          if (record.qtyPending === 0) {
            this.setIcon('success');
          } else if (record.qtyPending < 0 || (record._baseStyle && record._baseStyle.indexOf(OB.Styles.OBWPL.PickValidateProcessGrid.cellErrorStyle) !== -1)) {
            this.setIcon('error');
          } else {
            this.setIcon('');
          }
          this.Super('initWidget', arguments);
        }
      });
    }
    return item;
  },
  getCellVAlign: function () {
    return 'center';
  },
  getRecordComponentsCustom: function (record) {
    if (this.getRecordComponents) {
      return this.getRecordComponents(record);
    }
    var ids = record['_recordComponents_' + this.ID],
        key, components = {};
    if (ids) {
      for (key in ids) {
        if (ids.hasOwnProperty(key)) {
          if (ids[key].isNullMarker) {
            components[key] = ids[key];
          } else {
            components[key] = isc.Canvas.getById(ids[key]);
          }
        }
      }
    }
    return components;
  },
  updateRecordStyle: function (record, rowNum, isStrict) {
    var iconStatus, oldBaseStyle, newBaseStyle, newIconType, i;

    if (!rowNum && rowNum !== 0) {
      for (i = 0; i < this.data.length; i++) {
        if (!record.barcode && record.barcode !== '' && this.data[i].barcode === record.barcode) {
          rowNum = i;
          break;
        }
        if (record.productId === this.data[i].productId) {
          rowNum = i;
          break;
        }
      }
    }
    if (!record) {
      record = this.data.get(rowNum);
    }
    iconStatus = this.getRecordComponentsCustom(record).iconStatus;
    oldBaseStyle = record._baseStyle;

    if (record.qtyPending === 0) {
      //this.deselectRecord(rowNum);
      newBaseStyle = OB.Styles.OBWPL.PickValidateProcessGrid.cellSuccessStyle + ' ' + this.baseStyle;
      newIconType = 'success';
    } else if (record.qtyPending < 0 || isStrict) {
      //this.deselectRecord(rowNum);
      newBaseStyle = OB.Styles.OBWPL.PickValidateProcessGrid.cellErrorStyle + ' ' + this.baseStyle;
      newIconType = 'error';
    } else {
      newBaseStyle = this.baseStyle;
      newIconType = '';
    }
    if (newBaseStyle !== oldBaseStyle) {
      record._baseStyle = newBaseStyle;
      if (iconStatus) {
        iconStatus.setIcon(newIconType);
      }
      for (i = 0; i < 6; i++) {
        this.refreshCellStyle(rowNum, i);
      }
    }
  },
  updateQuantity: function (record, rowNum, qty, updatedFromRecordComponent) {
    var i;
    if (!rowNum && rowNum !== 0) {
      for (i = 0; i < this.data.length; i++) {
        if (!record.barcode && record.barcode !== '' && this.data[i].barcode === record.barcode) {
          rowNum = i;
          break;
        }
        if (record.productId === this.data[i].productId) {
          rowNum = i;
          break;
        }
      }
    }
    if (!record) {
      record = this.data.get(rowNum);
    }
    if (!record.qtyVerified) {
      record.qtyVerified = 0;
    }
    if ( !! updatedFromRecordComponent) {
      record.qtyVerified = qty;
    } else {
      record.qtyVerified = record.qtyVerified + qty;
    }

    record.qtyPending = record.quantity - record.qtyVerified;
    if (this.getRecordComponentsCustom(record).qtyVerified) { //In case the row is visible (and rendered)
      if (!updatedFromRecordComponent) {
        this.getRecordComponentsCustom(record).qtyVerified.getItem('Quantity').setValue(record.qtyVerified);
      }
      this.refreshCell(rowNum, this.getFieldNum('qtyPending'));
    }
    this.scrollCellIntoView(rowNum, null, true, true);
    this.selectSingleRecord(rowNum);
    this.updateRecordStyle(record, rowNum);
  },
  validateCode: function (code, qty) {
    var record, rowNum, i;
    for (i = 0; i < this.data.length; i++) {
      if (this.data[i].barcode === code) {
        record = this.data[i];
        rowNum = i;
        break;
      }
    }
    if (record) {
      this.updateQuantity(record, rowNum, qty);
      return true;
    } else {
      return false;
    }
  }
});