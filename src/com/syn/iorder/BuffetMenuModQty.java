package com.syn.iorder;

import org.ksoap2.serialization.PropertyInfo;

import android.content.Context;
import android.text.TextUtils;

public class BuffetMenuModQty extends WebServiceTask{

	public static final String METHOD = "WSiOrder_JSON_ChangeBuffetItemQtyV1";
	
	private BuffetMenuModQtyListener mListener;
	
	public BuffetMenuModQty(Context c, GlobalVar gb, int staffId, int tableId, 
			String jsonBuffetOrder, BuffetMenuModQtyListener listener) {
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

		property = new PropertyInfo();
		property.setName("szJSonListBuffetItems");
		property.setValue(jsonBuffetOrder);
		property.setType(String.class);
		soapRequest.addProperty(property);
		
		mListener = listener;
	}

	@Override
	protected void onPostExecute(String result) {
		if(progress.isShowing())
			progress.dismiss();
		if(!TextUtils.isEmpty(result)){
			mListener.onPost(result);
		}else{
			mListener.onError("Unknown error!");
		}
	}

	public static interface BuffetMenuModQtyListener{
		void onPost(String msg);
		void onError(String msg);
	}
}
