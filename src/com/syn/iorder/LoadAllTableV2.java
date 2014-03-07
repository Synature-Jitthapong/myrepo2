package com.syn.iorder;

import java.lang.reflect.Type;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.model.TableInfo;
import syn.pos.data.model.WebServiceResult;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import android.content.Context;

public class LoadAllTableV2 extends WebServiceTask{
	public static final String LOAD_TABLE_V2_METHOD = "WSmPOS_JSON_LoadAllTableDataV2";
	
	private LoadTableProgress mListener;
	
	public LoadAllTableV2(Context c, GlobalVar globalVar, LoadTableProgress listener) {
		super(c, globalVar, LOAD_TABLE_V2_METHOD);
		
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
	}

	@Override
	protected void onPreExecute() {
		progress.setMessage(context.getString(R.string.load_table_progress));
		progress.show();
		mListener.onPre();
	}

	@Override
	protected void onPostExecute(String result) {
		if(progress.isShowing())
			progress.dismiss();
		
		Gson gson = new Gson();
		Type type = new TypeToken<WebServiceResult>(){}.getType();
		try {
			WebServiceResult ws = gson.fromJson(result, type);
			if(ws.getiResultID() == 0){
				type = new TypeToken<List<TableInfo>>(){}.getType();
				List<TableInfo> tbInfoLst = gson.fromJson(ws.getSzResultData(), type);
				mListener.onPost(tbInfoLst);
			}else{
				mListener.onError(result);
			}
		} catch (JsonSyntaxException e) {
			mListener.onError(result);
		}
	}

	public static interface LoadTableProgress extends OnProgressListener{
		void onPost(List<TableInfo> tbInfoLst);
	}
}
