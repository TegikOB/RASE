/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
/*global enyo, _*/

/**
 * Multi (2) columns layout. Each of this columns consists on a toolbar and a
 * panel. When the screen is narrow, the layout shows only one column and adds
 * a button to switch between them
 */
enyo.kind({
  name: 'OB.UI.MultiColumn',

  isRightSideShown: true,

  handlers: {
    onSwitchColumn: 'switchColumn',
    onpostresize: 'panelsResized',
    onShowColumn: 'showColumn'
  },
  events: {
    onNarrowChanged: ''
  },

  published: {
    leftToolbar: null,
    leftPanel: null,
    rightToolbar: null,
    rightPanel: null
  },
  components: [{
    kind: 'Panels',
    name: 'panels',
    arrangerKind: 'CollapsingArranger',
    style: 'height: 690px;',
    //TODO: height is needed, how to calculate??
    components: [{
      name: 'left',
      classes: 'menu-option',
      components: [{
        classes: 'row',
        style: 'margin-bottom: 5px;',
        name: 'leftToolbar'
      }, {
        classes: 'span12',
        name: 'leftPanel'
      }]
    }, {
      name: 'right',
      classes: 'menu-option',
      components: [{
        classes: 'row',
        style: 'margin-bottom: 5px;',
        name: 'rightToolbar'
      }, {
        classes: 'span12',
        name: 'rightPanel'
      }]
    }]
  }],
  statics: {
    isSingleColumn: function () {
      return enyo.Panels.isScreenNarrow();
    }
  },

  menuChanged: function () {
    this.$.leftMenu.setMenu(this.menu);
  },

  panelsResized: function () {
    var newNarrow = enyo.Panels.isScreenNarrow(),
        i, panels, zoom;

    if (window.innerHeight) {
      // resetting height to prevent scrollbar depending on resolution
      zoom = document.body.style.zoom || 1;
      this.$.panels.applyStyle('height', (window.innerHeight - 14) / zoom + 'px');
    }

    if (this.narrow === newNarrow) {
      // no change in window size, do nothing
      return false;
    }

    this.narrow = newNarrow;

    if (newNarrow) {
      this.$.panels.draggable = this.isRightSideShown;
    } else {
      this.$.panels.draggable = false;
      this.$.panels.setIndex(0);
    }

    panels = this.$.panels.getPanels();
    for (i = 0; i < panels.length; i++) {
      if (newNarrow) {
        panels[i].removeClass('menu-option-transparent');
        panels[i].addClass('menu-option');

      } else {
        panels[i].removeClass('menu-option');
        panels[i].addClass('menu-option-transparent');
      }
    }

    this.waterfall('onNarrowChanged');
  },

  switchColumn: function () {
    var idx = this.$.panels.getIndex();
    if (!this.isRightSideShown) {
      return;
    }

    idx = idx === 0 ? 1 : 0;
    this.$.panels.setIndex(idx);
  },

  showColumn: function (inSender, inEvent) {
    var idx = inEvent.colNum;
    if (!enyo.Panels.isScreenNarrow()) {
      // no columns to navigate
      return;
    }
    if (!idx) {
      this.switchColumn();
    } else {
      this.$.panels.setIndex(idx);
    }
  },

  setRightShowing: function (showing) {
    this.isRightSideShown = showing;
    this.$.rightToolbar.setShowing(showing);
    this.$.rightPanel.setShowing(showing);

    this.$.panels.draggable = showing;
    if (!showing) {
      this.$.panels.setIndex(0);
    }

    if (this.$.leftToolbar.$.leftToolbar) {
      this.$.leftToolbar.$.leftToolbar.$.rightHolder.$.colswitcher.$.button.setDisabled(!showing);
    }
  },

  initComponents: function () {
    var leftToolbar, rightToolbar, emptyPanel = {
      kind: 'Component'
    },
        emptyToolbar = {
        kind: 'OB.UI.MultiColumn.Toolbar'
        };

    this.leftToolbar = this.leftToolbar || emptyToolbar;
    this.leftPanel = this.leftPanel || emptyPanel;
    this.rightToolbar = this.rightToolbar || emptyToolbar;
    if (!this.rightPanel) {
      this.rightPanel = emptyPanel;
      this.leftToolbar.emptyRightPanel = true;
    }

    leftToolbar = _.clone(this.leftToolbar);
    rightToolbar = _.clone(this.rightToolbar);
    this.inherited(arguments);

    leftToolbar.position = 'left';
    rightToolbar.position = 'right';
    this.$.leftToolbar.createComponent(leftToolbar);
    this.$.rightToolbar.createComponent(rightToolbar);
    this.$.leftPanel.createComponent(this.leftPanel);
    this.$.rightPanel.createComponent(this.rightPanel);
    this.setRightShowing(this.isRightSideShown);
  }
});

/**
 * Toolbar for OB.UI.MultiColumn layout. Handles column switcher button that
 * is shown when in narrow screen
 */
enyo.kind({
  name: 'OB.UI.MultiColumn.Toolbar',
  handlers: {
    onNarrowChanged: 'toolbarResized'
  },
  components: [{
    classes: 'span12',
    components: [{
      name: 'theToolbar',
      components: [{
        name: 'leftHolder'
      }, {
        name: 'standardToolbar',
        components: [{
          name: 'toolbar',
          tag: 'ul',
          classes: 'unstyled nav-pos row-fluid'
        }]
      }, {
        name: 'rightHolder'
      }]
    }]
  }],

  initComponents: function () {
    this.inherited(arguments);
    if (this.position === 'left') {
      if (!this.emptyRightPanel) {
        this.$.rightHolder.createComponent({
          kind: 'OB.UI.OB.UI.MultiColumn.Toolbar.ColumnSwitcher',
          name: 'colswitcher'
        });
      }

      if (this.showMenu) {
        this.$.leftHolder.createComponent({
          kind: 'OB.UI.MainMenu',
          customMenuEntries: this.menuEntries,
          showWindowsMenu: this.showWindowsMenu
        });
      }
    }
    enyo.forEach(this.buttons, function (btn) {
      this.$.toolbar.createComponent({
        kind: 'OB.UI.OB.UI.MultiColumn.Toolbar.Button',
        button: btn
      });
    }, this);

    if (this.position === 'right') {
      this.$.leftHolder.createComponent({
        kind: 'OB.UI.OB.UI.MultiColumn.Toolbar.ColumnSwitcher',
        name: 'colswitcher'
      });
    }
  },
  /**
   * If it is narrow screen, column switcher button is shown and the toolbar is
   * resized to fit it.
   */
  toolbarResized: function () {
    var isNarrow = enyo.Panels.isScreenNarrow(),
        hasMenu = this.position === 'left' && this.showMenu;

    if (isNarrow && !this.emptyRightPanel) {
      if (hasMenu) {
        this.$.standardToolbar.removeClass('span10');
        this.$.standardToolbar.addClass('span8');
      } else {
        this.$.standardToolbar.removeClass('span12');
        this.$.standardToolbar.addClass('span10');
      }
      if (this.$.leftHolder.$.colswitcher) {
        this.$.leftHolder.$.colswitcher.$.button.show();
      }
      if (this.$.rightHolder.$.colswitcher) {
        this.$.rightHolder.$.colswitcher.$.button.show();
      }
    } else {
      if (hasMenu) {
        this.$.standardToolbar.removeClass('span8');
        this.$.standardToolbar.addClass('span10');
      } else {
        this.$.standardToolbar.removeClass('span10');
        this.$.standardToolbar.addClass('span12');
      }
      if (this.$.leftHolder.$.colswitcher) {
        this.$.leftHolder.$.colswitcher.$.button.hide();
      }
      if (this.$.rightHolder.$.colswitcher) {
        this.$.rightHolder.$.colswitcher.$.button.hide();
      }
    }
    return true;
  }
});


/**
 * Button to switch between columns when in narrow screen
 */
enyo.kind({
  name: 'OB.UI.OB.UI.MultiColumn.Toolbar.ColumnSwitcher',
  events: {
    onSwitchColumn: ''
  },
  classes: 'span2',
  components: [{
    // column switcher
    components: [{
      attributes: {
        style: 'margin: 0px 5px 0px 5px;'
      },
      components: [{
        kind: 'OB.UI.ToolbarButton',
        name: 'button',
        icon: 'btn-icon btn-icon-switchColumn',
        tap: function () {
          this.bubble('onSwitchColumn');
        }
      }]
    }]
  }]
});

enyo.kind({
  name: 'OB.UI.OB.UI.MultiColumn.Toolbar.Button',
  tag: 'li',
  components: [{
    name: 'theButton',
    attributes: {
      style: 'margin: 0px 5px 0px 5px;'
    }
  }],
  initComponents: function () {
    var span = this.button.span || (this.button.kind === 'OB.UI.MultiColumn.EmptyToolbar' ? '12' : '4');
    this.inherited(arguments);

    this.addClass('span' + span);
    this.$.theButton.createComponent(this.button);
  }
});

enyo.kind({
  name: 'OB.UI.MultiColumn.EmptyToolbar',
  kind: 'OB.UI.ToolbarButton',
  classes: 'btnlink-gray',
  style: 'font-weight: bold; font-size: 130%;',
  initComponents: function () {
    this.inherited(arguments);
  }
});