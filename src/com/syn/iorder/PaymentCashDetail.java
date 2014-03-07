package com.syn.iorder;

import java.lang.reflect.Type;
import org.ksoap2.serialization.PropertyInfo;
import syn.pos.data.model.WebServiceResult;
import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class PaymentCashDetail extends WebServiceTask{
	public static final String METHOD = "WSiOrder_JSON_SetPaymentCashDetail";
	private ProgressListener mListener;
	
	public PaymentCashDetail(Context c, GlobalVar globalVar, 
			int transId, int compId, String totalPay, ProgressListener listener) {
		super(c, globalVar, METHOD);
		mListener = listener;
		
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
		property.setValue(compId);
		property.setType(int.class);
		soapRequest.addProperty(property);
		
		property = new PropertyInfo();
		property.setName("fPayAmount");
		property.setValue(totalPay);
		property.setType(String.class);
		soapRequest.addProperty(property);
	}
	
	@Override
	protected void onPreExecute() {
		mListener.onPre();
	}
	
	@Override
	protected void onPostExecute(final String result) {
		Gson gson = new Gson();
		Type type = new TypeToken<WebServiceResult>(){}.getType();
		try {
			final WebServiceResult ws = gson.fromJson(result, type);
			if(ws.getiResultID() == 0){
				mListener.onPost();
			}else{
				mListener.onError(ws.getSzResultData().equals("") ? result : ws.getSzResultData());
			}
		} catch (JsonSyntaxException e) {
			mListener.onError(result);
		}
	}
}