package com.syn.iorder;

import java.lang.reflect.Type;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import syn.pos.data.model.SummaryTransaction;
import syn.pos.data.model.WebServiceResult;
import android.content.Context;

public class TableSummaryBill extends WebServiceTask {
	public static final String SUMMARY_METHOD = "WSiOrder_JSON_ShowSummaryBillDetail";
	private LoadBillSummaryListener mListener;
	
	public TableSummaryBill(Context c, GlobalVar gb, int tableId, LoadBillSummaryListener listener) {
		super(c, gb, SUMMARY_METHOD);
		mListener = listener;
		
		PropertyInfo property = new PropertyInfo();
		property.setName("iStaffID");
		property.setValue(GlobalVar.STAFF_ID);
		property.setType(int.class);
		soapRequest.addProperty(property);
		
		property = new PropertyInfo();
		property.setName("iComputerID");
		property.setValue(GlobalVar.COMPUTER_ID);
		property.setType(int.class);
		soapRequest.addProperty(property);
		
		property = new PropertyInfo();
		property.setName("iTableID");
		property.setValue(tableId);
		property.setType(int.class);
		soapRequest.addProperty(property);
		
		property = new PropertyInfo();
		property.setName("szReason");
		property.setValue("");
		property.setType(String.class);
		soapRequest.addProperty(property);
	}

	@Override
	protected void onPreExecute() {
		mListener.onPre();
	}

	@Override
	protected void onPostExecute(String result) {
		try {
			WebServiceResult ws = toServiceObject(result);
			if(ws.getiResultID() == 0){
				Gson gson = new Gson();
				Type type = new TypeToken<SummaryTransaction>(){}.getType();
				SummaryTransaction summaryTrans = gson.fromJson(ws.getSzResultData(), type);
				mListener.onPost(summaryTrans);
			}else{
				mListener.onError(ws.getSzResultData().equals("") ? result : ws.getSzResultData());
			}
		} catch (JsonSyntaxException e) {
			mListener.onError(result);
		}
	}
	
	public static interface LoadBillSummaryListener extends ProgressListener{
		void onPost(SummaryTransaction summaryTrans);
	}
}
