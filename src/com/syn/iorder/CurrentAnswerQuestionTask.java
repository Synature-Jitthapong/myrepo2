package com.syn.iorder;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.ProductGroups;
import syn.pos.data.model.WebServiceResult;
import syn.pos.mobile.iordertab.R;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class CurrentAnswerQuestionTask extends WebServiceTask{
	private static final String method = "WSiOrder_JSON_GetCurrentAnswerQuestion";
	private Gson gson;
	private ICurrentAnswerListener iCurrAns;
	public CurrentAnswerQuestionTask(Context c, GlobalVar gb, int tableId, ICurrentAnswerListener iCurrAnswer) {
		super(c, gb, method);
		gson = new Gson();
		iCurrAns = iCurrAnswer;
		
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
	}

	@Override
	protected void onPreExecute() {
		tvProgress.setText(R.string.loading_progress);
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

			Type type = new TypeToken<List<ProductGroups.QuestionAnswerData>>() {}.getType();
			List<ProductGroups.QuestionAnswerData> questionLst = 
					new ArrayList<ProductGroups.QuestionAnswerData>();
					
			if(wsResult.getiResultID() == 0){
				questionLst = gson.fromJson(wsResult.getSzResultData(), type);
			}else{
				IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, 
						wsResult.getSzResultData(), 0);
			}
			iCurrAns.listQuestionAnswer(questionLst);
		} catch (Exception e) {
			IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, 
				result, 0);
			e.printStackTrace();
		}

	}
	
	public static interface ICurrentAnswerListener{
		public void listQuestionAnswer(List<ProductGroups.QuestionAnswerData> questionLst);
	}
}
