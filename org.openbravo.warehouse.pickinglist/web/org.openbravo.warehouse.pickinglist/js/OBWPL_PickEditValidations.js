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
OB.OBWPL = OB.OBWPL || {};
OB.OBWPL.QuantityValidate = function (item, validator, value, record) {
  var availableQty = isc.isA.Number(record.availableQty) ? new BigDecimal(String(record.availableQty)) : BigDecimal.prototype.ZERO,
      deliveredQuantity = isc.isA.Number(record.deliveredQuantity) ? new BigDecimal(String(record.deliveredQuantity)) : BigDecimal.prototype.ZERO,
      orderedQuantity = isc.isA.Number(record.orderedQuantity) ? new BigDecimal(String(record.orderedQuantity)) : BigDecimal.prototype.ZERO,
      reservedinothersQty = isc.isA.Number(record.reservedInOthers) ? new BigDecimal(String(record.reservedInOthers)) : BigDecimal.prototype.ZERO,
      quantity = null,
      totalQty = BigDecimal.prototype.ZERO,
      selectedRecords = item.grid.getSelectedRecords(),
      selectedRecordsLength = selectedRecords.length,
      editedRecord = null,
      i;
  if (!isc.isA.Number(value)) {
    return false;
  }
  if (value === null || value < 0) {
    return false;
  }
  quantity = new BigDecimal(String(value));
  if (quantity.compareTo(availableQty.subtract(reservedinothersQty)) > 0) {
    isc.warn(OB.I18N.getLabel('OBWPL_MoreQtyThanAvailable', [record.availableQty, record.reservedinothers]));
    return false;
  }
  for (i = 0; i < selectedRecordsLength; i++) {
    editedRecord = isc.addProperties({}, selectedRecords[i], item.grid.getEditedRecord(selectedRecords[i]));
    if (isc.isA.Number(editedRecord.quantity)) {
      totalQty = totalQty.add(new BigDecimal(String(editedRecord.quantity)));
    }
  }
  if (totalQty.compareTo(orderedQuantity.subtract(deliveredQuantity)) > 0) {
    isc.warn(OB.I18N.getLabel('OBWPL_Qty_MoreThanPendingOrdered', [orderedQuantity.subtract(deliveredQuantity).toString()]));
    return false;
  }
  return true;
};