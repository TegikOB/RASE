(function () {
	var buttonProps = {
		      action: function(){
		    	  var callback, line, 
		    	  i, view = this.view, 
		    	  grid = view.viewGrid, 
		    	  
		    	  line = grid.getSelectedRecord().id;
		    	  
		    	  OB.OBGMPSL_selectedRecord = grid.getSelectedRecord();
		    	  
		    	  OB.OBGMPSL_selectedTabId = grid.getData().context.params.tabId;
		    	  
		    	// define the callback function which shows the result to the user
		          callback = function (rpcResponse, data, rpcRequest) {
		            //isc.say(OB.I18N.getLabel('obgmpsl_Localization_Registered'));
		            rpcRequest.clientContext.view.refresh(false, false);
		          };
		    	  
		       // and call the server
		          isc.obgmpsl_Localization_Popup.create({
		              line: line,
		              originalview: view
		            }).show();
		      },
		      buttonType: 'obgmpsl_pointer',
		      prompt: OB.I18N.getLabel('obgmpsl_Localization'),
		      updateState: function(){
		          var view = this.view, form = view.viewForm, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords();
		          if (view.isShowingForm && form.isNew) {
		            this.setDisabled(true);
		          } else if (view.isEditingGrid && grid.getEditForm().isNew) {
		            this.setDisabled(true);
		          } else {
		            this.setDisabled(selectedRecords.length !== 1);
		          }
		      }
	};
	OB.ToolbarRegistry.registerButton(buttonProps.buttonType, isc.OBToolbarIconButton, buttonProps, 500, ['220', '294', '186', '177', '257']);
	//var send = {};
	//send.accion = 'data';
	//OB.RemoteCallManager.call('com.openbravo.gps.GoogleMapLocalization.process.ButtonTabsActionHandler', send, {}, function (response, data, request) {
	  	    //OB.ToolbarRegistry.registerButton(buttonProps.buttonType, isc.OBToolbarIconButton, buttonProps, 500, data.tabs);
	//});
}());
