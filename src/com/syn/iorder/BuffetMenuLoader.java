package com.syn.iorder;

import java.lang.reflect.Type;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.WebServiceResult;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class BuffetMenuLoader extends WebServiceTask{

	public static final String TAG = BuffetMenuLoader.class.getSimpleName();
	
	public static final String METHOD = "WSiOrder_JSON_GetBuffetMenuFromTableID";
	
	private GetBuffetOrderListener mListener;
	
	public BuffetMenuLoader(Context c, GlobalVar gb, int tableId, GetBuffetOrderListener listener) {
		super(c, gb, METHOD);
		
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
		
		mListener = listener;
	}

	@Override
	protected void onPostExecute(String result) {
		Log.i(TAG, result);
		if(progress.isShowing())
			progress.dismiss();
		try {
			GsonDeserialze gdz = new GsonDeserialze();
			WebServiceResult ws = gdz.deserializeWsResultJSON(result);
			if(ws != null){
				if(ws.getiResultID() == 0){
					if(!TextUtils.isEmpty(ws.getSzResultData())){
						Gson gson = new Gson();
						Type type = new TypeToken<List<BuffetOrder>>(){}.getType();
						List<BuffetOrder> buffetLst = gson.fromJson(ws.getSzResultData(), type);
						if(buffetLst != null){
							mListener.onPost(buffetLst);
						}
					}
				}else{
					mListener.onError(TextUtils.isEmpty(ws.getSzResultData()) ? result : ws.getSzResultData());
				}
			}
		} catch (JsonSyntaxException e) {
			mListener.onError(result);
			Log.e(TAG, e.getMessage());
		}
	}
	
	public static interface GetBuffetOrderListener{
		void onPre();
		void onPost(List<BuffetOrder> buffetOrder);
		void onError(String msg);
	}
	
	public static class BuffetOrder{
		private int iOrderID;
	    private int iItemID;
	    private String szItemName;
	    private double fItemQty;
	    private double fItemPrice;
		public int getiOrderID() {
			return iOrderID;
		}
		public void setiOrderID(int iOrderID) {
			this.iOrderID = iOrderID;
		}
		public int getiItemID() {
			return iItemID;
		}
		public void setiItemID(int iItemID) {
			this.iItemID = iItemID;
		}
		public String getSzItemName() {
			return szItemName;
		}
		public void setSzItemName(String szItemName) {
			this.szItemName = szItemName;
		}
		public double getfItemQty() {
			return fItemQty;
		}
		public void setfItemQty(double fItemQty) {
			this.fItemQty = fItemQty;
		}
		public double getfItemPrice() {
			return fItemPrice;
		}
		public void setfItemPrice(double fItemPrice) {
			this.fItemPrice = fItemPrice;
		}
	}
}
