  
(function () {

  var Region = OB.Data.ExtensibleModel.extend({
    modelName: 'Region',
    tableName: 'c_region',
    entityName: 'Region',
    source: 'com.tegik.retail.localizacion.Region',
    dataLimit: 300
  });

  Region.addProperties([{
    name: 'id',
    column: 'c_region_id',
    primaryKey: true,
    type: 'TEXT'
  }, {
    name: 'name',
    column: 'name',
    filter: true,
    type: 'TEXT'
  }, {
    name: '_identifier',
    column: '_identifier',
    filter: true,
    type: 'TEXT'
  }]);
  
  window.OB = window.OB || {};
  window.OB.Model = window.OB.Model || {};
  window.OB.Collection = window.OB.Collection || {};

  window.OB.Model.Region = Region;
  
  OB.Data.Registry.registerModel(Region);
  
  OB.OBPOSPointOfSale.Model.PointOfSale.prototype.models.push(OB.Model.Region);

}());