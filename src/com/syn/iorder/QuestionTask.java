package com.syn.iorder;

import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.ProductGroups;
import syn.pos.data.model.WebServiceResult;
import syn.pos.mobile.iordertab.R;
import syn.pos.mobile.util.Log;

import android.content.Context;

public class QuestionTask extends WebServiceTask {
	private static final String method = "WSiOrder_JSON_SendAnswerQuestion";
	private Gson gson;
	private WebServiceStateListener serviceState;
	
	public QuestionTask(Context c, GlobalVar gb, int tableId, 
			List<ProductGroups.QuestionAnswerData> questionLst, WebServiceStateListener state) {
		super(c, gb, method);
		gson = new Gson();
		this.serviceState = state;
		
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
		property.setName("iTableID");
		property.setValue(tableId);
		property.setType(int.class);
		soapRequest.addProperty(property);
		
		property = new PropertyInfo();
		property.setName("szJSon_ListAnswerQuestion");
		property.setValue(gson.toJson(questionLst));
		property.setType(String.class);
		soapRequest.addProperty(property);
	}

	@Override
	protected void onPreExecute() {
		tvProgress.setText(R.string.wait_progress);
		progress.setMessage(tvProgress.getText().toString());
		progress.show();
	}

	@Override
	protected void onPostExecute(String result) {
		if(progress.isShowing())
			progress.dismiss();
		
		GsonDeserialze gdz = new GsonDeserialze();
		try {
			WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
			if(wsResult.getiResultID() == 0){
				serviceState.onSuccess();
			}else{
				IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, 
						wsResult.getSzResultData().equals("") ? result : wsResult.getSzResultData(), 0);
			}
		} catch (Exception e) {
			IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, 
					result, 0);
			e.printStackTrace();
		}
	}

}
