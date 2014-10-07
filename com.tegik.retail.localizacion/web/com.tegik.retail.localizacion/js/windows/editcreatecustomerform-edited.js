  OB.OBPOSPointOfSale.UI.customers.edit_createcustomers_impl.prototype.newAttributes.splice(3,2);
OB.OBPOSPointOfSale.UI.customers.edit_createcustomers_impl.prototype.newAttributes.splice(3,0,
  /*{
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerName',
    modelProperty: 'name',
    isFirstFocus: true,
    i18nLabel: 'OBPOS_LblName',
    maxlength: 60
  }, {
    kind: 'OB.UI.CustomerComboProperty',
    name: 'customerCategory',
    modelProperty: 'businessPartnerCategory',
    //Required: property where the selected value will be get and where the value will be saved
    modelPropertyText: 'businessPartnerCategory_name',
    //optional: When saving, the property which will store the selected text
    collection: new OB.Collection.BPCategoryList(),
    // defaultValue: OB.POS.modelterminal.get('terminal').defaultbp_bpcategory,
    //Default value for new lines
    retrievedPropertyForValue: 'id',
    //property of the retrieved model to get the value of the combo item
    retrievedPropertyForText: '_identifier',
    //property of the retrieved model to get the text of the combo item
    //function to retrieve the data
    fetchDataFunction: function (args) {
      var me = this;
      OB.Dal.find(OB.Model.BPCategory, null, function (data, args) {
        //This function must be called when the data is ready
        me.dataReadyFunction(data, args);
      }, function (error) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingBPCategories'));
        //This function must be called when the data is ready
        me.dataReadyFunction(null, args);
      }, args);
    },
    i18nLabel: 'OBPOS_BPCategory',
    displayLogic: function () {
      return OB.MobileApp.model.get('terminal').bp_showcategoryselector;
    }
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerTaxId',
    modelProperty: 'taxID',
    i18nLabel: 'OBPOS_LblTaxId',
    displayLogic: function () {
      return OB.MobileApp.model.get('terminal').bp_showtaxid;
    },
    maxlength: 20
  },*/ 
   {
    kind: 'OB.UI.CustomerComboProperty',
    name: 'customerLocEstado',
    modelProperty: 'region',
    //Required: property where the selected value will be get and where the value will be saved
    modelPropertyText: 'region_name',
    //optional: When saving, the property which will store the selected text
    collection: new OB.Collection.RegionList(),
    // defaultValue: OB.POS.modelterminal.get('terminal').defaultbp_bpcategory,
    //Default value for new lines
    retrievedPropertyForValue: 'id',
    //property of the retrieved model to get the value of the combo item
    retrievedPropertyForText: '_identifier',
    //property of the retrieved model to get the text of the combo item
    //function to retrieve the data
    fetchDataFunction: function (args) {
      var me = this;
      OB.Dal.find(OB.Model.Region, null, function (data, args) {
        //This function must be called when the data is ready
        me.dataReadyFunction(data, args);
      }, function (error) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingBPCategories'));
        //This function must be called when the data is ready
        me.dataReadyFunction(null, args);
      }, args);
    },
    i18nLabel: 'OBLOC_LblEstado'
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'obposCity',
    modelProperty: 'cityName',
    i18nLabel: 'OBPOS_LblCity',
    maxlength: 60
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocColonia',
    modelProperty: 'addressLine2',
    i18nLabel: 'OBLOC_LblColonia',
    maxlength: 60
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocCalle',
    modelProperty: 'locName',
    i18nLabel: 'OBLOC_LblCalle',
    maxlength: 60
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocNoExt',
    modelProperty: 'tdirmNumex',
    i18nLabel: 'OBLOC_LblNoExt',
    maxlength: 60
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocNoInt',
    modelProperty: 'tdirmNumin',
    i18nLabel: 'OBLOC_LblNoInt',
    maxlength: 60
  });
  
OB.OBPOSPointOfSale.UI.customers.editcustomers_impl.prototype.newAttributes.splice(3,2);
OB.OBPOSPointOfSale.UI.customers.editcustomers_impl.prototype.newAttributes.splice(3,0, 
  {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocEstado',
    modelProperty: 'region_name',
    i18nLabel: 'OBLOC_LblEstado',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'obposCity',
    modelProperty: 'cityName',
    i18nLabel: 'OBPOS_LblCity',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocColonia',
    modelProperty: 'addressLine2',
    i18nLabel: 'OBLOC_LblColonia',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocCalle',
    modelProperty: 'locName',
    i18nLabel: 'OBLOC_LblCalle',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocNoExt',
    modelProperty: 'tdirmNumex',
    i18nLabel: 'OBLOC_LblNoExt',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocNoInt',
    modelProperty: 'tdirmNumin',
    i18nLabel: 'OBLOC_LblNoInt',
    readOnly: true
  });

OB.OBPOSPointOfSale.UI.customeraddr.editcustomers_impl.prototype.newAttributes.splice(1,1);
OB.OBPOSPointOfSale.UI.customeraddr.editcustomers_impl.prototype.newAttributes.splice(2,1);
OB.OBPOSPointOfSale.UI.customeraddr.editcustomers_impl.prototype.newAttributes.splice(1,0,
   {
    kind: 'OB.UI.CustomerAddrTextProperty',
    name: 'customerAddrName',
    modelProperty: 'name',
    i18nLabel: 'OBLOC_LblCalle',
    readOnly: true
  },{
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocEstado',
    modelProperty: 'region_name',
    i18nLabel: 'OBLOC_LblEstado',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'obposAddrCity',
    modelProperty: 'cityName',
    i18nLabel: 'OBPOS_LblCity',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocColonia',
    modelProperty: 'addressLine2',
    i18nLabel: 'OBLOC_LblColonia',
    readOnly: true
  }, /*{
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocCalle',
    modelProperty: 'locName',
    i18nLabel: 'OBLOC_LblCalle',
    readOnly: true
  },*/ {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocNoExt',
    modelProperty: 'tdirmNumex',
    i18nLabel: 'OBLOC_LblNoExt',
    readOnly: true
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocNoInt',
    modelProperty: 'tdirmNumin',
    i18nLabel: 'OBLOC_LblNoInt',
    readOnly: true
  });

OB.OBPOSPointOfSale.UI.customeraddr.edit_createcustomers_impl.prototype.newAttributes.splice(1,1);
OB.OBPOSPointOfSale.UI.customeraddr.edit_createcustomers_impl.prototype.newAttributes.splice(2,1);
OB.OBPOSPointOfSale.UI.customeraddr.edit_createcustomers_impl.prototype.newAttributes.splice(1,0,
 {
    kind: 'OB.UI.CustomerAddrTextProperty',
    name: 'customerAddrName',
    modelProperty: 'name',
    i18nLabel: 'OBLOC_LblCalle',
    maxlength: 60
  },{
    kind: 'OB.UI.CustomerComboProperty',
    name: 'customerLocEstado',
    modelProperty: 'region',
    //Required: property where the selected value will be get and where the value will be saved
    modelPropertyText: 'region_name',
    //optional: When saving, the property which will store the selected text
    collection: new OB.Collection.RegionList(),
    // defaultValue: OB.POS.modelterminal.get('terminal').defaultbp_bpcategory,
    //Default value for new lines
    retrievedPropertyForValue: 'id',
    //property of the retrieved model to get the value of the combo item
    retrievedPropertyForText: '_identifier',
    //property of the retrieved model to get the text of the combo item
    //function to retrieve the data
    fetchDataFunction: function (args) {
      var me = this;
      OB.Dal.find(OB.Model.Region, null, function (data, args) {
        //This function must be called when the data is ready
        me.dataReadyFunction(data, args);
      }, function (error) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_ErrorGettingBPCategories'));
        //This function must be called when the data is ready
        me.dataReadyFunction(null, args);
      }, args);
    },
    i18nLabel: 'OBLOC_LblEstado'
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'obposAddrCity',
    modelProperty: 'cityName',
    i18nLabel: 'OBPOS_LblCity',
    maxlength: 60
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocColonia',
    modelProperty: 'addressLine2',
    i18nLabel: 'OBLOC_LblColonia',
    maxlength: 60
  }, /*{
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocCalle',
    modelProperty: 'locName',
    i18nLabel: 'OBLOC_LblCalle',
    readOnly: true
  },*/ {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocNoExt',
    modelProperty: 'tdirmNumex',
    i18nLabel: 'OBLOC_LblNoExt',
    maxlength: 60
  }, {
    kind: 'OB.UI.CustomerTextProperty',
    name: 'customerLocNoInt',
    modelProperty: 'tdirmNumin',
    i18nLabel: 'OBLOC_LblNoInt',
    maxlength: 60
  });