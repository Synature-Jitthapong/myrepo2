package com.syn.iorder;

import java.lang.reflect.Type;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.model.OrderSendData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

public class CurrentOrderOfTable extends NewWsClient{

	public static final String TAG = CurrentOrderOfTable.class.getSimpleName();
	
	public static final String mWebMethod = "WSiOrder_JSON_LoadCurrentOrderFromTableID";
	public static final String TABLE_ID_PARAM = "iTableID"; 
	
	public CurrentOrderOfTable(Context context, int tableId, ResultReceiver receiver) {
		super(context, GlobalVar.FULL_URL, mWebMethod, receiver);
		
		PropertyInfo property = new PropertyInfo();
		property.setName(TABLE_ID_PARAM);
		property.setValue(tableId);
		property.setType(int.class);
		mSoapRequest.addProperty(property);
	}

	private CurrentOrder toCurrentOrderObj(String result) throws Exception{
		Gson gson = new Gson();
		Type type = new TypeToken<CurrentOrder>() {}.getType();
		return gson.fromJson(result, type);
	}
	
	@Override
	protected void onPostExecute(String result) {
		Log.i(TAG, result);
		CurrentOrder currentOrder = null;
		try {
			currentOrder = toCurrentOrderObj(result);
			if(mReceiver != null){
				Bundle b = new Bundle();
				b.putParcelable("currentOrder", currentOrder);
				mReceiver.send(RESULT_SUCCESS, b);
			}
		} catch (Exception e) {
			if(mReceiver != null){
				Bundle b = new Bundle();
				b.putString("msg", TextUtils.isEmpty(result) ? e.getMessage() : result);
				mReceiver.send(RESULT_ERROR, b);
			}
		}
		
	}
	
	public static class CurrentOrder extends OrderSendData implements Parcelable{

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
		}
	}
}
