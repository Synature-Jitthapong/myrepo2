package com.syn.iorder;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.WebServiceResult;
import syn.pos.mobile.iordertab.R;

import android.content.Context;
import android.view.View;

public class UpdateCustomerTask extends WebServiceTask {
	private WebServiceTaskState state;
	public UpdateCustomerTask(Context c, GlobalVar gb,  int transId,
			int computerId, int custNo, WebServiceTaskState listener) {
		super(c, gb, "WSiOrder_JSON_UpdateCustomerNoFromTransaction");
		
		state = listener;
		
		PropertyInfo property = new PropertyInfo();
		property.setName("iComputerID");
		property.setValue(GlobalVar.COMPUTER_ID);
		property.setType(int.class);
		soapRequest.addProperty(property);
	
		property = new PropertyInfo();
		property.setName("iStaffID");
		property.setValue(GlobalVar.STAFF_ID);
		property.setType(int.class);
		soapRequest.addProperty(property);

		property = new PropertyInfo();
		property.setName("iDbTransID");
		property.setValue(transId);
		property.setType(int.class);
		soapRequest.addProperty(property);
		
		property = new PropertyInfo();
		property.setName("iDbCompID");
		property.setValue(computerId);
		property.setType(int.class);
		soapRequest.addProperty(property);
		
		property = new PropertyInfo();
		property.setName("iNoCustomer");
		property.setValue(custNo);
		property.setType(int.class);
		soapRequest.addProperty(property);
		
		tvProgress.setText(R.string.wait_progress);
		progress.setMessage(tvProgress.getText());
	}
	@Override
	protected void onPreExecute() {
		progress.show();
		state.onProgress();
	}
	
	@Override
	protected void onPostExecute(String result) {
		if(progress.isShowing())
			progress.dismiss();
		
		GsonDeserialze gdz = new GsonDeserialze();
		WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
		if(wsResult.getiResultID() == 0){
			state.onSuccess();
		}else{
			state.onNotSuccess();
		}
	}
}
