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
  name: 'OB.UI.RenderProduct',

  kind: 'OB.UI.SelectButton',
  components: [{
    style: 'float: left; width: 25%',
    components: [{
      kind: 'OB.UI.Thumbnail',
      name: 'thumbnail'
    }]
  }, {
    style: 'float: left; width: 55%;',
    components: [{
      name: 'identifier',
      style: 'padding-left: 5px;'
    }]
  }, {
    name: 'price',
    style: 'float: left; width: 20%; text-align: right; font-weight:bold;'
  }, {
    style: 'clear:both;'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('_identifier'));
    this.$.price.setContent(OB.I18N.formatCurrency(this.model.get('standardPrice')));
    var image;
    if (OB.MobileApp.model.get('permissions')["OBPOS_retail.productImages"]) {
      image = OB.UTIL.getImageURL(this.model.get('id'));
      this.$.thumbnail.setImgUrl(image);
    } else {
      image = this.model.get('img');
      this.$.thumbnail.setImg(image);
    }
  }
});

enyo.kind({
  name: 'OB.UI.RenderCategory',
  kind: 'OB.UI.SelectButton',
  components: [{
    style: 'float: left; width: 25%',
    components: [{
      kind: 'OB.UI.Thumbnail',
      name: 'thumbnail'
    }]
  }, {
    style: 'float: left; width: 75%;',
    components: [{
      name: 'identifier',
      style: 'padding-left: 5px;'
    }, {
      style: 'clear:both;'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.addClass('btnselect-browse');
    this.$.identifier.setContent(this.model.get('_identifier'));
    this.$.thumbnail.setImg(this.model.get('img'));
  }
});

enyo.kind({
  name: 'OB.UI.ProductBrowser',
  useCharacteristics: true,
  classes: 'row-fluid',
  components: [{
    classes: 'span6',
    components: [{
      kind: 'OB.UI.BrowseProducts',
      name: 'browseProducts'
    }]
  }, {
    classes: 'span6',
    components: [{
      kind: 'OB.UI.BrowseCategories',
      name: 'browseCategories'
    }]
  }],
  init: function () {
    this.$.browseProducts.$.listProducts.useCharacteristics = this.useCharacteristics;
    this.$.browseCategories.$.listCategories.categories.on('selected', function (category) {
      this.$.browseProducts.$.listProducts.loadCategory(category);
    }, this);
  }
});

enyo.kind({
  name: 'OB.UI.BrowseCategories',
  style: 'overflow:auto; height: 612px; margin: 5px;',
  components: [{
    style: 'background-color: #ffffff; color: black; padding: 5px',
    components: [{
      kind: 'OB.UI.ListCategories',
      name: 'listCategories'
    }]
  }]
});

enyo.kind({
  name: 'OB.UI.BrowseProducts',
  style: 'margin: 5px;',
  components: [{
    style: 'background-color: #ffffff; color: black; padding: 5px',
    components: [{
      kind: 'OB.UI.ListProducts',
      name: 'listProducts'
    }]
  }]
});

enyo.kind({
  kind: 'OB.UI.ScrollableTableHeader',
  name: 'OB.UI.CategoryListHeader',
  style: 'padding: 10px; border-bottom: 1px solid #cccccc;',
  components: [{
    style: 'line-height: 27px; font-size: 18px; font-weight: bold;',
    name: 'title',
    content: ''
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.title.setContent(OB.I18N.getLabel('OBMOBC_LblCategories'));
  }
});

enyo.kind({
  name: 'OB.UI.ListCategories',
  components: [{
    name: 'categoryTable',
    scrollAreaMaxHeight: '574px',
    listStyle: 'list',
    kind: 'OB.UI.ScrollableTable',
    renderHeader: 'OB.UI.CategoryListHeader',
    renderEmpty: 'OB.UI.RenderEmpty',
    renderLine: 'OB.UI.RenderCategory',
    columns: ['thumbnail', 'identifier']
  }],

  init: function () {
    var me = this;
    this.categories = new OB.Collection.ProductCategoryList();
    this.$.categoryTable.setCollection(this.categories);

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackBestSellerProducts(products, context) {
      if (products && products.length > 0) {
        var virtualBestSellerCateg = new OB.Model.ProductCategory();
        virtualBestSellerCateg.createBestSellerCategory();
        context.categories.add(virtualBestSellerCateg, {
          at: 0
        });
      }
      context.me.categories.reset(context.categories.models);
    }

    function successCallbackCategories(dataCategories, me) {
      var bestSellerCriteria, context;
      if (dataCategories && dataCategories.length > 0) {
        //search products
        bestSellerCriteria = {
          'bestseller': 'true'
        };
        context = {
          me: me,
          categories: dataCategories
        };
        OB.Dal.find(OB.Model.Product, bestSellerCriteria, successCallbackBestSellerProducts, errorCallback, context);
      } else {
        me.categories.reset();
      }
    }

    OB.Dal.find(OB.Model.ProductCategory, null, successCallbackCategories, errorCallback, this);
  }
});

//This header is set dynamically
//use scrollableTableHeaderChanged_handler method of scrollableTable to manage changes
//me.$.productTable.setHeaderText(category.get('_identifier'));
enyo.kind({
  kind: 'OB.UI.ScrollableTableHeader',
  name: 'OB.UI.ProductListHeader',
  style: 'padding: 10px; border-bottom: 1px solid #cccccc;',
  components: [{
    style: 'line-height: 27px; font-size: 18px; font-weight: bold;',
    name: 'title'
  }],
  setHeader: function (valueToSet) {
    this.$.title.setContent(valueToSet);
  }
});

enyo.kind({
  name: 'OB.UI.ListProducts',
  events: {
    onAddProduct: '',
    onShowLeftSubWindow: '',
    onTabChange: ''
  },
  components: [{
    kind: 'OB.UI.ScrollableTable',
    name: 'productTable',
    scrollAreaMaxHeight: '574px',
    renderHeader: 'OB.UI.ProductListHeader',
    renderEmpty: 'OB.UI.RenderEmpty',
    renderLine: 'OB.UI.RenderProduct',
    columns: ['thumbnail','identifier','price', 'generic']
  }],
  init: function () {
    var me = this;
    this.inherited(arguments);
    this.products = new OB.Collection.ProductList();
    this.$.productTable.setCollection(this.products);
    this.products.on('click', function (model) {
      if (this.useCharacteristics) {
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
      } else {
        me.doAddProduct({
          product: model
        });
      }
    }, this);
  },

  loadCategory: function (category) {
    var criteria, me = this;

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackProducts(dataProducts) {
      if (dataProducts && dataProducts.length > 0) {
        me.products.reset(dataProducts.models);
      } else {
        me.products.reset();
      }
      //      TODO
      me.$.productTable.getHeader().setHeader(category.get('_identifier'));
    }

    if (category) {
      if (category.get('id') === 'OBPOS_bestsellercategory') {
        criteria = {
          'bestseller': 'true'
        };
        if (this.useCharacteristics) {
          criteria.generic_product_id = null;
        }
      } else {
        criteria = {
          'productCategory': category.get('id')
        };
        if (this.useCharacteristics) {
          criteria.generic_product_id = null;
        }
      }
      criteria._orderByClause = 'upper(_identifier) asc';
      OB.Dal.find(OB.Model.Product, criteria, successCallbackProducts, errorCallback);
    } else {
      this.products.reset();
      this.title.text(OB.I18N.getLabel('OBMOBC_LblNoCategory'));
    }
  }
});