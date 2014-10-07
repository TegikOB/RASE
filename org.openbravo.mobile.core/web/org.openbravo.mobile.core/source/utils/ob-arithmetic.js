/*
 ************************************************************************************
 * Copyright (C) 2012-2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $, _, console */

(function () {

  OB = window.OB || {};
  OB.DEC = window.OB.DEC || {};

  var scale = 2;
  var roundingmode = BigDecimal.prototype.ROUND_HALF_UP;

  var toBigDecimal = function (a) {
      return new BigDecimal(a.toString());
      };

  var toNumber = function (big, arg_scale) {
      var localscale = arg_scale || scale;
      if (big.scale) {
        return parseFloat(big.setScale(localscale, roundingmode).toString(), 10);
      } else {
        if (_.isNumber(big)) {
          return big;
        } else {
          OB.error("toNumber: Argument cannot be converted toNumber", big);
        }
      }
      };

  OB.DEC.Zero = toNumber(BigDecimal.prototype.ZERO);
  OB.DEC.One = toNumber(BigDecimal.prototype.ONE);

  OB.DEC.getScale = function () {
    return scale;
  };

  OB.DEC.getRoundingMode = function () {
    return roundingmode;
  };

  OB.DEC.isNumber = function (a) {
    return typeof (a) === 'number' && !isNaN(a);
  };

  OB.DEC.add = function (a, b, arg_scale) {
    return toNumber(toBigDecimal(a).add(toBigDecimal(b)), arg_scale);
  };

  OB.DEC.sub = function (a, b, arg_scale) {
    return toNumber(toBigDecimal(a).subtract(toBigDecimal(b)), arg_scale);
  };

  OB.DEC.mul = function (a, b, arg_scale) {
    return toNumber(toBigDecimal(a).multiply(toBigDecimal(b)), arg_scale);
  };

  OB.DEC.div = function (a, b, arg_scale) {
    return toNumber(toBigDecimal(a).divide(toBigDecimal(b), scale, roundingmode), arg_scale);
  };

  OB.DEC.compare = function (a) {
    return toBigDecimal(a).compareTo(BigDecimal.prototype.ZERO);
  };

  OB.DEC.number = function (jsnumber) {
    return jsnumber; // toNumber(toBigDecimal(jsnumber));
  };

  OB.DEC.setContext = function (s, r) {
    scale = s;
    roundingmode = r;
  };

  OB.DEC.toBigDecimal = function (a) {
    return toBigDecimal(a);
  };

  OB.DEC.toNumber = function (a, arg_scale) {
    return toNumber(a, arg_scale);
  };

  OB.DEC.abs = function (a, arg_scale) {
    return toNumber(toBigDecimal(a).abs(), arg_scale);
  };

}());