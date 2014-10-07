/* 
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
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
 ************************************************************************
 **/

// Creates an object that encapsulates functions used in jasper reports in hyperlinks
OB.AGING = {

  // Shows a new popup with content
  //
  // Parameters:
  // * {{{name}}} type: String - the name of the window. Used to manage
  // window.open target
  // * {{{width}}} type: String | Number - the width of the popup. It can be
  // set as a String as a %
  // * {{{height}}} type: String | Number - the height of the popup. It can
  // be set as a String as a %
  // * {{{url}}} type: String - the url to be opened in the popup
  // * {{{title}}} type: String - the title to be displayed in the popup
  // * {{{theOpener}}} type: Window Object - the window object of the opener of the popup. Used in window.open to allow IE know which is the opener
  // * {{{showMinimizeControl}}} type: Boolean - to specify if the popup should show the minimize control or not. The default value is "true" if it is not specified
  // * {{{showMaximizeControl}}} type: Boolean - to specify if the popup should show the maximize control or not. The default value is "true" if it is not specified
  // * {{{showCloseControl}}} type: Boolean - to specify if the popup should show the close control or not. The default value is "true" if it is not specified
  // * {{{postParams}}} type: Object - parameters to be sent to the url using POST instead of GET
  // * {{{isModal}}} type: Boolean - to specify if the popup should be modal or not. The default value is "true" if it is not specified
  // 
  // Opens the PopUp and returns Undefined, this way it can work both in Chrome and in Firefox
  openPopup: function (name, width, height, url, title, theOpener, showMinimizeControl, showMaximizeControl, showCloseControl, postParams) {
    OB.Layout.ClassicOBCompatibility.Popup.open(name, width, height, OB.Application.contextUrl + url, title, theOpener, showMinimizeControl, showMaximizeControl, showCloseControl, postParams);
  }
};