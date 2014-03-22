package com.syn.iorder;

import java.lang.reflect.Type;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.model.TableInfo;
import syn.pos.data.model.WebServiceResult;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.syn.iorder.util.SQLiteHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TableUtils {
	
	public static int getMappingTableId(Context c, String tableName){
		int tableId = 0;
		if(tableName.equals(""))
			return 0;
		SQLiteHelper sqliteHelper = new SQLiteHelper(c);
		SQLiteDatabase sqlite = sqliteHelper.getReadableDatabase();
		Cursor cursor = sqlite.query("TableInfo", 
				new String[]{"tb_id", "tb_name"}, 
				"tb_name" + "=?", new String[]{tableName}, null, null, null);
		if(cursor.moveToFirst()){
			tableId = cursor.getInt(cursor.getColumnIndex("tb_id"));
		}
		cursor.close();
		return tableId;
	}
	
	public static void insertTableInfoTableInfo(Context c, List<TableInfo> tbInfLst){
		try {
			SQLiteHelper sqliteHelper = new SQLiteHelper(c);
			SQLiteDatabase sqlite = sqliteHelper.getWritableDatabase();
			sqlite.delete("TableInfo", null, null);
			for(TableInfo tbInf : tbInfLst){
				ContentValues cv = new ContentValues();
				cv.put("tb_id", tbInf.getiTableID());
				cv.put("tb_name", tbInf.getSzTableName());
				sqlite.insert("TableInfo", null, cv);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static class LoadAllTableNoProgress extends LoadAllTableV2{

		public LoadAllTableNoProgress(Context c, GlobalVar globalVar,
				LoadTableProgress listener) {
			super(c, globalVar, listener);
		}

		@Override
		protected void onPreExecute() {
			progress.dismiss();
		}
	}
	
	public static class SplitMultiTableService extends WebServiceTask{
		public static final String SPLIT_MULTI_TABLE_METHOD = "WSiOrder_JSON_SplitMultiTable";
		private ProgressListener mListener;
		
		public SplitMultiTableService(Context c, GlobalVar gb,
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

	public static class LoadMergedTable extends WebServiceTask{
		public static final String METHOD = "WSiOrder_JSON_GetListTablesOfMergeTable";
		private LoadMergeTableProgressListener mListener;
		
		public LoadMergedTable(Context c, GlobalVar gb, int transId, int compId, LoadMergeTableProgressListener listener) {
			super(c, gb, METHOD);
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
		}

		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}

		@Override
		protected void onPostExecute(String result) {
			Gson gson = new Gson();
			Type type = new TypeToken<int[]>(){}.getType();
			try {
				int[] tbIds = gson.fromJson(result, type);
				mListener.onPost(tbIds);
			} catch (JsonSyntaxException e) {
				mListener.onError(result);
			}
		}
	}
	
	public static interface LoadMergeTableProgressListener extends ProgressListener{
		void onPost(int[] tbIds);
	}
	
	public static interface LoadTableProgressListener extends ProgressListener{
		void onPost(TableInfo tbInfo);
	}
}
