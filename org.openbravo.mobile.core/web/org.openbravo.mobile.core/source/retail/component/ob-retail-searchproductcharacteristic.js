/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _ */

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.BrandButton',
  style: 'width: 86%; padding: 0px;',
  classes: 'btnlink-white-simple',
  events: {
    onShowPopup: ''
  },
  tap: function () {
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalproductbrand'
      });
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel('OBMOBC_LblBrand'));
  }
});

enyo.kind({
  name: 'OB.UI.SearchProductCharacteristicHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: ''
  },
  handlers: {
    onFiltered: 'searchAction',
    onClearAllAction: 'clearAllAction'
  },
  components: [{
    style: 'padding: 10px 10px 5px 10px;',
    components: [{
      style: 'display: table;  width: 100%;',
      components: [{
        style: 'display: table-cell; width: 100%;',
        components: [{
          kind: 'OB.UI.SearchInputAutoFilter',
          name: 'productname',
          style: 'width: 100%;'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-gray btn-icon-small btn-icon-clear',
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          ontap: 'clearAllAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-yellow btn-icon-small btn-icon-search',
          style: 'width: 100px; margin: 0px 0px 8px 5px;',
          ontap: 'searchAction'
        }]
      }]
    }, {
      style: 'margin: 5px 0px 0px 0px;',
      components: [{
        kind: 'OB.UI.List',
        name: 'productcategory',
        classes: 'combo',
        style: 'width: 100%',
        renderHeader: enyo.kind({
          kind: 'enyo.Option',
          initComponents: function () {
            this.inherited(arguments);
            this.setValue('__all__');
            this.setContent(OB.I18N.getLabel('OBMOBC_SearchAllCategories'));
          }
        }),
        renderLine: enyo.kind({
          kind: 'enyo.Option',
          initComponents: function () {
            this.inherited(arguments);
            this.setValue(this.model.get('id'));
            this.setContent(this.model.get('_identifier'));
          }
        }),
        renderEmpty: 'enyo.Control'
      }]
    }, {
      name: 'filteringBy',
      style: 'text-align: left; font-weight: bold; font-size: 15px; color: #aaaaaa'
    }]
  }],
  setHeaderCollection: function (valueToSet) {
    this.$.productcategory.setCollection(valueToSet);
  },
  searchAction: function () {
    this.doSearchAction({
      productCat: this.$.productcategory.getValue(),
      productName: this.$.productname.getValue(),
      skipProduct: false,
      skipProductCharacteristic: false
    });
    return true;
  },
  clearAllAction: function () {
    this.$.productname.setValue('');
    this.$.productcategory.setSelected(0);
    this.$.filteringBy.setContent('');
    this.parent.$.brandButton.removeClass('btnlink-yellow-bold');
    this.parent.model.set('filter', []);
    this.parent.model.set('brandFilter', []);
    this.parent.genericParent = null;
    this.parent.products.reset();
    this.doSearchAction({
      productCat: this.$.productcategory.getValue(),
      productName: this.$.productname.getValue(),
      skipProduct: true,
      skipProductCharacteristic: false
    });
  },
  init: function () {
    var me = this;
    this.inherited(arguments);
    this.categories = new OB.Collection.ProductCategoryList();
    this.products = new OB.Collection.ProductList();

    //first the main collection of the component
    //    this.$.products.setCollection(this.products);
    this.setHeaderCollection(this.categories);

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackCategories(dataCategories, me) {
      if (dataCategories && dataCategories.length > 0) {
        me.categories.reset(dataCategories.models);
      } else {
        me.categories.reset();
      }
    }

    this.products.on('click', function (model) {
      this.doAddProduct({
        product: model
      });
    }, this);

    OB.Dal.find(OB.Model.ProductCategory, null, successCallbackCategories, errorCallback, this);
  }
});

enyo.kind({
  name: 'OB.UI.SearchProductCharacteristic',
  classes: 'span12',
  style: 'background-color: #ffffff; color: black; ',
  published: {
    receipt: null,
    genericParent: null
  },
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction',
    onUpdateFilter: 'filterUpdate',
    onUpdateBrandFilter: 'brandFilterUpdate'
  },
  events: {
    onAddProduct: '',
    onSearchAction: '',
    onClearAction: '',
    onTabChange: ''
  },
  filterUpdate: function (inSender, inEvent) {
    var i, j, index, chValue = inEvent.value.value;
    for (j = 0; j < chValue.length; j++) {
      index = this.model.get('filter').map(function (e) {
        return e.id;
      }).indexOf(chValue[j].get('id'));
      if (index === -1) {
        if (chValue[j].get('checked')) {
          this.model.get('filter').push({
            characteristic_id: chValue[j].get('characteristic_id'),
            id: chValue[j].get('id'),
            name: chValue[j].get('name'),
            checked: chValue[j].get('checked'),
            selected: chValue[j].get('selected')
          });
        }
      } else {
        if (!chValue[j].get('checked')) {
          this.model.get('filter').splice(index, 1);
        } else {
          this.model.get('filter')[index] = {
            characteristic_id: chValue[j].get('characteristic_id'),
            id: chValue[j].get('id'),
            name: chValue[j].get('name'),
            checked: chValue[j].get('checked'),
            selected: chValue[j].get('selected')
          };
        }

      }
    }
    this.model.set('filter', _.sortBy(this.model.get('filter'), function (e) {
      return e.characteristic_id;
    }));
    this.filteringBy();
    this.doSearchAction({
      productCat: this.$.searchProductCharacteristicHeader.$.productcategory.getValue(),
      productName: this.$.searchProductCharacteristicHeader.$.productname.getValue(),
      filter: this.model.get('filter'),
      skipProduct: false,
      skipProductCharacteristic: false
    });
    return true;
  },
  brandFilterUpdate: function (inSender, inEvent) {
    var i, j, index, brandValue = inEvent.value.value;
    for (j = 0; j < brandValue.length; j++) {
      index = this.model.get('brandFilter').map(function (e) {
        return e.id;
      }).indexOf(brandValue[j].get('id'));
      if (index === -1 && brandValue[j].get('checked')) {
        this.model.get('brandFilter').push({
          id: brandValue[j].get('id'),
          name: brandValue[j].get('name')
        });
      } else if (index !== -1 && (_.isUndefined(brandValue[j].get('checked')) || !brandValue[j].get('checked'))) {
        this.model.get('brandFilter').splice(index, 1);
      }
    }
    this.model.set('filter', _.sortBy(this.model.get('filter'), function (e) {
      return e.characteristic_id;
    }));
    this.filteringBy();
    if (this.model.get('brandFilter').length > 0) {
      this.$.brandButton.addClass('btnlink-yellow-bold');
    } else {
      this.$.brandButton.removeClass('btnlink-yellow-bold');
    }
    this.doSearchAction({
      productCat: this.$.searchProductCharacteristicHeader.$.productcategory.getValue(),
      productName: this.$.searchProductCharacteristicHeader.$.productname.getValue(),
      filter: this.model.get('filter'),
      skipProduct: false,
      skipProductCharacteristic: false
    });
    return true;
  },
  filteringBy: function () {
    var filteringBy = OB.I18N.getLabel('OBMOBC_FilteringBy'),
        selectedItems, i;
    selectedItems = _.compact(this.model.get('filter').map(function (e) {
      if (e.selected) {
        return e.name;
      }
    }));
    if ((_.isUndefined(this.genericParent) || _.isNull(this.genericParent)) && selectedItems.length === 0 && this.model.get('brandFilter').length === 0) {
      this.$.searchProductCharacteristicHeader.$.filteringBy.setContent('');
      return true;
    }
    if (!_.isUndefined(this.genericParent) && !_.isNull(this.genericParent)) {
      filteringBy = filteringBy + ' ' + this.genericParent.get('_identifier');
      if (selectedItems.length + this.model.get('brandFilter').length > 0) {
        filteringBy = filteringBy + ', ';
      }
    }
    for (i = 0; i < selectedItems.length; i++) {
      filteringBy = filteringBy + ' ' + selectedItems[i];
      if (i !== selectedItems.length - 1 || (i === selectedItems.length - 1 && this.model.get('brandFilter').length > 0)) {
        filteringBy = filteringBy + ', ';
      }
    }
    for (i = 0; i < this.model.get('brandFilter').length; i++) {
      filteringBy = filteringBy + ' ' + this.model.get('brandFilter')[i].name;
      if (i !== this.model.get('brandFilter').length - 1) {
        filteringBy = filteringBy + ', ';
      }
    }
    this.$.searchProductCharacteristicHeader.$.filteringBy.setContent(filteringBy);
  },
  executeOnShow: function (model) {
    var me = this,
        criteria = {};
    this.doClearAction();
    this.genericParent = model;
    this.doSearchAction({
      productCat: this.$.searchProductCharacteristicHeader.$.productcategory.getValue(),
      productName: this.$.searchProductCharacteristicHeader.$.productname.getValue(),
      filter: this.model.get('filter'),
      skipProduct: !this.genericParent,
      skipProductCharacteristic: false
    });
    this.filteringBy();
    setTimeout(function () {
      me.parent.$.searchCharacteristic.$.searchProductCharacteristicHeader.$.productname.focus();
    }, 200);
  },
  components: [{
    kind: 'OB.UI.SearchProductCharacteristicHeader',
    classes: 'span12',
    style: 'display: table-cell; '
  }, {
    style: 'display: table; width:100%',
    components: [{
      style: 'display: table-cell; width:30%',
      classes: 'row-fluid',
      components: [{
        components: [{
          classes: 'row-fluid',
          components: [{
            classes: 'row-fluid',
            components: [{
              components: [{
                name: 'brandButton',
                kind: 'OB.UI.BrandButton'
              }, {
                kind: 'OB.UI.ScrollableTable',
                name: 'productsCh',
                scrollAreaMaxHeight: '482px',
                renderEmpty: 'OB.UI.RenderEmptyCh',
                renderLine: 'OB.UI.RenderProductCh'
              }]
            }]
          }]
        }]
      }]
    }, {
      style: 'display: table-cell; width:70%; padding-right:5px',
      classes: 'row-fluid ',
      components: [{
        components: [{
          classes: 'row-fluid',
          style: 'border-bottom: 1px solid #cccccc;',
          components: [{
            classes: 'row-fluid',
            components: [{
              components: [{
                kind: 'OB.UI.ScrollableTable',
                name: 'products',
                scrollAreaMaxHeight: '482px',
                renderEmpty: 'OB.UI.RenderEmpty',
                renderLine: 'OB.UI.RenderProduct'
              }]
            }]
          }]
        }]
      }]
    }]
  }],
  init: function (model) {
    this.model = model;
    var me = this,
        params = [],
        whereClause = '';
    this.inherited(arguments);
    this.categories = new OB.Collection.ProductCategoryList();
    this.products = new OB.Collection.ProductList();
    this.productsCh = new OB.Collection.ProductCharacteristicList();
    //first the main collection of the component
    this.$.products.setCollection(this.products);
    this.$.productsCh.setCollection(this.productsCh);
    //    this.$.products.getHeader().setHeaderCollection(this.categories);

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackCategories(dataCategories, me) {
      if (dataCategories && dataCategories.length > 0) {
        me.categories.reset(dataCategories.models);
      } else {
        me.categories.reset();
      }
    }

    function successCallbackProductCh(dataProductCh, me) {
      if (dataProductCh && dataProductCh.length > 0) {
        me.productsCh.reset(dataProductCh.models);
      } else {
        me.productsCh.reset();
      }
    }

    this.products.on('click', function (model) {
      if (!model.get('isGeneric')) {
        me.doAddProduct({
          product: model
        });
      } else {
        me.doTabChange({
          tabPanel: 'searchCharacteristic',
          keyboard: false,
          edit: false,
          options: model
        });
      }
    }, this);

    OB.Dal.find(OB.Model.ProductCategory, null, successCallbackCategories, errorCallback, this);
    OB.Dal.query(OB.Model.ProductCharacteristic, 'select distinct(characteristic_id), _identifier from m_product_ch order by UPPER(_identifier) asc', [], successCallbackProductCh, errorCallback, this);
  },
  receiptChanged: function () {
    this.receipt.on('clear', function () {
      this.$.searchProductCharacteristicHeader.$.productname.setContent('');
      this.$.searchProductCharacteristicHeader.$.productcategory.setContent('');
      //A filter should be set before show products. -> Big data!!
      //this.products.exec({priceListVersion: OB.POS.modelterminal.get('pricelistversion').id, product: {}});
    }, this);
  },
  clearAction: function (inSender, inEvent) {
    this.waterfall('onClearAllAction');
  },
  addWhereFilter: function (values) {
    if (values.productName) {
      this.whereClause = this.whereClause + ' and _filter like ?';
      this.params.push('%' + values.productName + '%');
    }
    if (values.productCat && values.productCat !== '__all__') {
      this.whereClause = this.whereClause + ' and m_product_category_id = ?';
      this.params.push(values.productCat);
    }
  },
  searchAction: function (inSender, inEvent) {
    this.params = [];
    this.whereClause = '';

    var criteria = {},
        me = this,
        filterWhereClause = '',
        valuesString = '',
        brandString = '',
        i, j;


    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    // Initializing combo of categories without filtering

    function successCallbackProducts(dataProducts) {
      if (dataProducts && dataProducts.length > 0) {
        me.products.reset(dataProducts.models);
        me.products.trigger('reset');
      } else {
        OB.UTIL.showWarning("No products found");
        me.products.reset();
      }
    }

    function successCallbackProductCh(dataProductCh, me) {
      if (dataProductCh && dataProductCh.length > 0) {
        for (i = 0; i < dataProductCh.length; i++) {
          for (j = 0; j < me.model.get('filter').length; j++) {
            if (dataProductCh.models[i].get('characteristic_id') === me.model.get('filter')[j].characteristic_id) {
              dataProductCh.models[i].set('filtering', true);
            }
          }
        }
        me.productsCh.reset(dataProductCh.models);
      } else {
        me.productsCh.reset();
      }
    }
    this.whereClause = this.whereClause + " where isGeneric = 'false'";

    this.addWhereFilter(inEvent);

    if (this.genericParent) {
      this.whereClause = this.whereClause + ' and generic_product_id = ?';
      this.params.push(this.genericParent.get('id'));
    }
    if (this.model.get('filter').length > 0) {
      for (i = 0; i < this.model.get('filter').length; i++) {
        if (i !== 0 && (this.model.get('filter')[i].characteristic_id !== this.model.get('filter')[i - 1].characteristic_id)) {
          filterWhereClause = filterWhereClause + ' and exists (select * from m_product_ch as char where ch_value_id in (' + valuesString + ') and char.m_product = product.m_product_id)';
          valuesString = '';
        }
        if (valuesString !== '') {
          valuesString = valuesString + ', ' + "'" + this.model.get('filter')[i].id + "'";
        } else {
          valuesString = "'" + this.model.get('filter')[i].id + "'";
        }
        if (i === this.model.get('filter').length - 1) { //last iteration
          filterWhereClause = filterWhereClause + ' and exists (select * from m_product_ch as char where ch_value_id in (' + valuesString + ') and char.m_product = product.m_product_id)';
          valuesString = '';
        }
      }
    }
    if (this.model.get('brandFilter').length > 0) {
      for (i = 0; i < this.model.get('brandFilter').length; i++) {
        brandString = brandString + "'" + this.model.get('brandFilter')[i].id + "'";
        if (i !== this.model.get('brandFilter').length - 1) {
          brandString = brandString + ', ';
        }
      }
      filterWhereClause = filterWhereClause + ' and product.brand in (' + brandString + ')';
    }
    if (!inEvent.skipProduct) {
      OB.Dal.query(OB.Model.Product, 'select * from m_product as product' + this.whereClause + filterWhereClause, this.params, successCallbackProducts, errorCallback, this);
    }
    if (!inEvent.skipProductCharacteristic) {
      if (this.model.get('filter').length > 0) {
        OB.Dal.query(OB.Model.ProductCharacteristic, 'select distinct(characteristic_id), _identifier from m_product_ch as prod_ch where exists (select * from m_product as product where 1=1 ' + filterWhereClause + ' and prod_ch.m_product = product.m_product_id) order by UPPER(_identifier) asc', [], successCallbackProductCh, errorCallback, this);
      } else {
        OB.Dal.query(OB.Model.ProductCharacteristic, 'select distinct(characteristic_id), _identifier from m_product_ch as prod_ch where exists (select * from m_product as product' + this.whereClause + ' and prod_ch.m_product = product.m_product_id) order by UPPER(_identifier) asc', this.params, successCallbackProductCh, errorCallback, this);
      }

    }
  }
});