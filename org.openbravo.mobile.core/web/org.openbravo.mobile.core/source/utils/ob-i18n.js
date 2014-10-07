/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, moment */

(function () {

  // Mockup for OB.I18N
  OB = window.OB || {};
  OB.I18N = window.OB.I18N || {};

  // Quantity scale.
  OB.I18N.qtyScale = function () {
    return OB.Format.formats.qtyEdition.length - OB.Format.formats.qtyEdition.indexOf('.') - 1;
  };

  OB.I18N.formatCurrency = function (number) {
    var maskNumeric = OB.Format.formats.priceInform,
        decSeparator = OB.Format.defaultDecimalSymbol,
        groupSeparator = OB.Format.defaultGroupingSymbol,
        groupInterval = OB.Format.defaultGroupingSize;

    maskNumeric = maskNumeric.replace(',', 'dummy').replace('.', decSeparator).replace('dummy', groupSeparator);

    return OB.Utilities.Number.JSToOBMasked(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
  };

  OB.I18N.formatCurrencyWithSymbol = function (number, symbol, currencySymbolToTheRight) {
    if (currencySymbolToTheRight) {
      return OB.I18N.formatCurrency(number) + symbol;
    } else {
      return symbol + OB.I18N.formatCurrency(number);
    }
  };

  OB.I18N.formatCoins = function (number) {
    var val = OB.I18N.formatCurrency(number);
    var decSeparator = OB.Format.defaultDecimalSymbol;
    return val.replace(new RegExp('[' + decSeparator + '][0]+$'), '');
  };

  OB.I18N.formatRate = function (number) {
    var symbol = '%',
        maskNumeric = OB.Format.formats.taxInform || OB.Format.formats.euroEdition,
        decSeparator = OB.Format.defaultDecimalSymbol,
        groupSeparator = OB.Format.defaultGroupingSymbol,
        groupInterval = OB.Format.defaultGroupingSize;

    maskNumeric = maskNumeric.replace(',', 'dummy').replace('.', decSeparator).replace('dummy', groupSeparator);

    var formattedNumber = OB.Utilities.Number.JSToOBMasked(number, maskNumeric, decSeparator, groupSeparator, groupInterval);
    formattedNumber = formattedNumber + symbol;
    return formattedNumber;
  };

  OB.I18N.formatDate = function (JSDate) {
    var dateFormat = OB.Format.date;
    if (OB.Utilities && OB.Utilities.Date) {
      return OB.Utilities.Date.JSToOB(JSDate, dateFormat);
    } else {
      return JSDate;
    }
  };

  OB.I18N.formatDateISO = function (d) {
    var curr_date = d.getDate();
    var curr_month = d.getMonth();
    var curr_year = d.getFullYear();
    var curr_hour = d.getHours();
    var curr_min = d.getMinutes();
    var curr_sec = d.getSeconds();
    var curr_mill = d.getMilliseconds();

    return OB.UTIL.padNumber(curr_year, 4) + '-' + OB.UTIL.padNumber(curr_month + 1, 2) + '-' + OB.UTIL.padNumber(curr_date, 2) + ' ' + OB.UTIL.padNumber(curr_hour, 2) + ':' + OB.UTIL.padNumber(curr_min, 2) + ':' + OB.UTIL.padNumber(curr_sec, 2) + '.' + OB.UTIL.padNumber(curr_mill, 3);
  };

  OB.I18N.formatHour = function (d, includeSeconds) {
    var curr_date = d.getDate();
    var curr_month = d.getMonth();
    var curr_year = d.getFullYear();
    var curr_hour = d.getHours();
    var curr_min = d.getMinutes();
    var curr_sec = d.getSeconds();
    var formattedHour = OB.UTIL.padNumber(curr_hour, 2) + ':' + OB.UTIL.padNumber(curr_min, 2);
    if (includeSeconds) {
      formattedHour += ":" + OB.UTIL.padNumber(curr_sec, 2);
    }
    return formattedHour;
  };

  OB.I18N.parseServerDate = function (d) {
    // for example if server and client are in different time zones
    // parserServerDate("2014-02-05T00:00:00+01:00") returns Wed Feb 05 2014 00:00:00 GMT-0500 (CET)
    return moment(d, "YYYY-MM-DD").toDate();
  };


  OB.I18N.parseNumber = function (s) {
    if (OB.Format.defaultDecimalSymbol !== '.') {
      s = s.toString();
      while (s.indexOf(OB.Format.defaultDecimalSymbol) !== -1) {
        s = s.replace(OB.Format.defaultDecimalSymbol, '.');
      }
    }
    return parseFloat(s, 10);
  };

  OB.I18N.getLabel = function (key, params, object, property) {
    if (key === '') {
      return '';
    }

    if (key.indexOf('OBUIAPP_GroupBy') === 0) {
      // Don't show error for GroupBy* labels, they are used by core's decimal
      // but don't really needed
      return '';
    }
    if (!OB.I18N.labels) {
      return 'UNDEFINED ' + key;
    }
    if (!OB.I18N.labels[key]) {
      if (object && property) {
        OB.I18N.getLabelFromServer(key, params, object, property);
      }
      OB.warn('not found label', key);
      return 'UNDEFINED ' + key;
    }
    var label = OB.I18N.labels[key],
        i;
    if (params && params.length && params.length > 0) {
      for (i = 0; i < params.length; i++) {
        label = label.replace("%" + i, params[i]);
      }
    }
    if (object && property) {
      if (Object.prototype.toString.call(object[property]) === '[object Function]') {
        object[property](label);
      } else {
        object[property] = label;
      }
    }
    return label;
  };

  OB.I18N.hasLabel = function (key, params, object, property) {
    return OB.I18N.labels[key] ? true : false;
  };

  OB.I18N.getDateFormatLabel = function () {
    var year, month, day, label = '',
        i, char, format;
    year = OB.I18N.getLabel('OBMOBC_YearCharLbl');
    month = OB.I18N.getLabel('OBMOBC_MonthCharLbl');
    day = OB.I18N.getLabel('OBMOBC_DayCharLbl');
    format = OB.Format.date.toLowerCase();
    for (i = 0; i < format.length; i++) {
      char = format[i];
      switch (char) {
      case 'y':
        label += year;
        break;
      case 'm':
        label += month;
        break;
      case 'd':
        label += day;
        break;
      default:
        label += char;
      }
    }
    return label;
  };
}());