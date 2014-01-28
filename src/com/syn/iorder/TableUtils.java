package com.syn.iorder;

import java.lang.reflect.Type;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.model.WebServiceResult;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import android.content.Context;

public class TableUtils {
	
	public static class CloseTable extends WebServiceTask{
		public static final String CLOSE_TABLE_METHOD = "WSiOrder_JSON_CloseTable";
		
		private ProgressListener mListener;
		public CloseTable(Context c, GlobalVar gb, int tableId, ProgressListener listener) {
			super(c, gb, CLOSE_TABLE_METHOD);
			
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
			property.setName("iTableID");
			property.setValue(tableId);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}
		
		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}
		
		@Override
		protected void onPostExecute(String result) {
			Gson gson = new Gson();
			Type type = new TypeToken<WebServiceResult>(){}.getType();
			
			try {
				WebServiceResult ws = gson.fromJson(result, type);
				if(ws.getiResultID() == 0)
					mListener.onPost();
				else 
					mListener.onError(ws.getSzResultData().equals("") ? result : 
						ws.getSzResultData());
			} catch (JsonSyntaxException e) {
				mListener.onError(result);
				e.printStackTrace();
			}
		}
		
	}
}
