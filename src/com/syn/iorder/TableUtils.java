package com.syn.iorder;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.model.TableInfo;
import syn.pos.data.model.WebServiceResult;

import com.google.gson.JsonSyntaxException;

import android.content.Context;

public class TableUtils {
	
	public static class SplitMultiTableService extends WebServiceTask{
		public static final String SPLIT_MULTI_TABLE_METHOD = "WSiOrder_JSON_SplitMultiTable";
		private ProgressListener mListener;
		
		public SplitMultiTableService(Context c, GlobalVar gb, int staffId, 
				int transId, int compId, String tbIds, String reasonIds, 
				String reason, ProgressListener listener) {
			super(c, gb, SPLIT_MULTI_TABLE_METHOD);
			
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
			property.setName("szListSplitTableID");
			property.setValue(tbIds);
			property.setType(String.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szListReasonID");
			property.setValue(reasonIds);
			property.setType(String.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szReasonSplitTable");
			property.setValue(reason);
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
					mListener.onPost();
				}else{
					mListener.onError(!ws.getSzResultData().equals("") ? 
							ws.getSzResultData() : result);
				}
			} catch (JsonSyntaxException e) {
				mListener.onError(result);
			}
		}
		
	}
	
	public static class MergeMultiTableService extends WebServiceTask{
		public static final String MERGE_MULTI_TABLE_METHOD = "WSiOrder_JSON_MergeMultiTable";
		private ProgressListener mListener;
		
		public MergeMultiTableService(Context c, GlobalVar gb, int currTableId, 
				String tableIds, String reasonIds, String reason, ProgressListener listener) {
			super(c, gb, MERGE_MULTI_TABLE_METHOD);
			
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
			property.setName("iCurTableID");
			property.setValue(currTableId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szListMergeTableID");
			property.setValue(tableIds);
			property.setType(String.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szListReasonID");
			property.setValue(reasonIds);
			property.setType(String.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szReasonMergeTable");
			property.setValue(reason);
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
					mListener.onPost();
				}else{
					mListener.onError(!ws.getSzResultData().equals("") ? 
							ws.getSzResultData() : result);
				}
			} catch (JsonSyntaxException e) {
				mListener.onError(result);
			}
		}
		
	}
	
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
			try {
				WebServiceResult ws = toServiceObject(result);
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

	public static interface LoadTableProgressListener extends ProgressListener{
		void onPost(TableInfo tbInfo);
	}
}
