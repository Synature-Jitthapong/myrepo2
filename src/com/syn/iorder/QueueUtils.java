package com.syn.iorder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.syn.iorder.util.SQLiteHelper;

import syn.pos.data.model.WebServiceResult;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class QueueUtils {
	
	public static List<QueueButton> getQueueBtnLst(Context c){
		List<QueueButton> queueBtnLst = new ArrayList<QueueButton>();
		SQLiteDatabase sqlite = getDatabase(c);
		Cursor cursor = sqlite.query("QueueButton", 
				new String[]{"queue_group_id", "queue_group_name"}, 
				null, null, null, null, "queue_group_id");
		if(cursor.moveToFirst()){
			do{
				QueueButton btn = new QueueButton();
				btn.setQueueGroupId(cursor.getInt(cursor.getColumnIndex("queue_group_id")));
				btn.setQueueGroupName(cursor.getString(cursor.getColumnIndex("queue_group_name")));
				queueBtnLst.add(btn);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return queueBtnLst;
	}
	
	public static void insertQueueButton(Context c, int groupNumber){
		String[] groupName = {"A", "B", "C", "D", "E", "F","G","H"};
		SQLiteDatabase sqlite = getDatabase(c);
		sqlite.delete("QueueButton", null, null);
		for(int i = 0; i < groupNumber; i++){
			ContentValues cv = new ContentValues();
			cv.put("queue_group_id", (i+1));
			cv.put("queue_group_name", groupName[i]);
			sqlite.insert("QueueButton", null, cv);
		}
	}
	
	public static SQLiteDatabase getDatabase(Context c){
		SQLiteHelper sqliteHelper = new SQLiteHelper(c);
		return sqliteHelper.getWritableDatabase();
	}
	
	public static class GenerateQueue extends WebServiceTask{
		public static final String GEN_QUEUE_METHOD = 
				"WSiQueue_JSON_GenerateNewQueueWithSelectQueuePrinter";
		
		private ProgressListener mListener;
		
		public GenerateQueue(Context c, GlobalVar gb, int queueGroupId, 
				int customerQty, String customerName, int printerId, ProgressListener listener) {
			super(c, gb, GEN_QUEUE_METHOD);
			
			mListener = listener;
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iQueueGroupID");
			property.setValue(queueGroupId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iCustQty");
			property.setValue(customerQty);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szCustName");
			property.setValue(customerName);
			property.setType(String.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iPrinterID");
			property.setValue(printerId);
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
					mListener.onError(ws.getSzResultData().equals("") ? 
							result : ws.getSzResultData());
			} catch (JsonSyntaxException e) {
				mListener.onError(result);
				e.printStackTrace();
			}
		}
	}
	
	public static class GenerateAutoQueue extends WebServiceTask{
		public static final String GEN_AUTO_QUEUE_METHOD = 
				"WSiQueue_JSON_CreateNewQueueAutoGroupWithSelectQueuePrinter";
		
		private ProgressListener mListener;
		
		public GenerateAutoQueue(Context c, GlobalVar gb, int customerQty, 
				String customerName, int printerId, ProgressListener listener) {
			super(c, gb, GEN_AUTO_QUEUE_METHOD);
			
			mListener = listener;
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iCustQty");
			property.setValue(customerQty);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szCustName");
			property.setValue(customerName);
			property.setType(String.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iPrinterID");
			property.setValue(printerId);
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
					mListener.onError(ws.getSzResultData().equals("") ? 
							result : ws.getSzResultData());
			} catch (JsonSyntaxException e) {
				mListener.onError(result);
				e.printStackTrace();
			}
		}
		
		
	}
	
	public static class GetPrinterForPrintQueue extends WebServiceTask{
		public static final String GET_QUEUE_PRINTER_METHOD = 
				"WSiQueue_JSON_GetPrinterForPrintQueue";
		
		private GetPrinterListener mListener;
		
		public GetPrinterForPrintQueue(Context c, GlobalVar gb, GetPrinterListener listener) {
			super(c, gb, GET_QUEUE_PRINTER_METHOD);
			
			mListener = listener;
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
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
			Type type = new TypeToken<List<PrinterUtils.Printer>>(){}.getType();
			
			try {
				List<PrinterUtils.Printer> printerLst = gson.fromJson(result, type);
				mListener.onPost(printerLst);
			} catch (JsonSyntaxException e) {
				mListener.onError(result);
				e.printStackTrace();
			}
		}
		
	}

	public static interface GetPrinterListener extends ProgressListener{
		void onPost(List<PrinterUtils.Printer> printerLst);
	}
	
	public static class QueueButton{
		private int queueGroupId;
		private String queueGroupName;
		
		public int getQueueGroupId() {
			return queueGroupId;
		}
		public void setQueueGroupId(int queueGroupId) {
			this.queueGroupId = queueGroupId;
		}
		public String getQueueGroupName() {
			return queueGroupName;
		}
		public void setQueueGroupName(String queueGroupName) {
			this.queueGroupName = queueGroupName;
		}
	}
}
