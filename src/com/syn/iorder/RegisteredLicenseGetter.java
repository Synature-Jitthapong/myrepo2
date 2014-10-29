package com.syn.iorder;

import java.lang.reflect.Type;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.text.TextUtils;

public class RegisteredLicenseGetter extends WebServiceTask{

	public static final String TAG = RegisteredLicenseGetter.class.getSimpleName();
	
	public static final String METHOD = "WSiOrder_JSON_GetRegisterLicense";
	
	private RegisteredListener mListener;
	
	public RegisteredLicenseGetter(Context c, GlobalVar gb, RegisteredListener listener) {
		super(c, gb, METHOD);
		
		PropertyInfo property = new PropertyInfo();
		property.setName("szDeviceUUID");
		property.setValue(globalVar.COMPUTER_DATA.getDeviceCode());
		property.setType(String.class);
		soapRequest.addProperty(property);
		
		mListener = listener;
	}
	
	@Override
	protected void onPreExecute() {
		mListener.onPre();
	}

	@Override
	protected void onPostExecute(String result) {
		if(!TextUtils.isEmpty(result)){
			try {
				mListener.onPost(toRegisterObject(result));
			} catch (Exception e) {
				mListener.onError("This devices hasn't registered.");
			}
		}else{
			mListener.onError(result);
		}
	}
	
	public RegisterLicense toRegisterObject(String json) throws Exception{
		Gson gson = new Gson();
		Type type = new TypeToken<RegisterLicense>(){}.getType();
		return gson.fromJson(json, type);
	}
	
	public static interface RegisteredListener{
		void onPre();
		void onPost(RegisterLicense register);
		void onError(String err);
	}
	
	public static class RegisterLicense{
		private String szLicenseCode;
		private String szDeviceKeyCode;
		private String szRegisterCode;
		private int iTotalLicense;
		private String szMerchantName;
		private String szBrandName;
		private String szShopName;
		public String getSzLicenseCode() {
			return szLicenseCode;
		}
		public void setSzLicenseCode(String szLicenseCode) {
			this.szLicenseCode = szLicenseCode;
		}
		public String getSzDeviceKeyCode() {
			return szDeviceKeyCode;
		}
		public void setSzDeviceKeyCode(String szDeviceKeyCode) {
			this.szDeviceKeyCode = szDeviceKeyCode;
		}
		public String getSzRegisterCode() {
			return szRegisterCode;
		}
		public void setSzRegisterCode(String szRegisterCode) {
			this.szRegisterCode = szRegisterCode;
		}
		public int getiTotalLicense() {
			return iTotalLicense;
		}
		public void setiTotalLicense(int iTotalLicense) {
			this.iTotalLicense = iTotalLicense;
		}
		public String getSzMerchantName() {
			return szMerchantName;
		}
		public void setSzMerchantName(String szMerchantName) {
			this.szMerchantName = szMerchantName;
		}
		public String getSzBrandName() {
			return szBrandName;
		}
		public void setSzBrandName(String szBrandName) {
			this.szBrandName = szBrandName;
		}
		public String getSzShopName() {
			return szShopName;
		}
		public void setSzShopName(String szShopName) {
			this.szShopName = szShopName;
		}
	}
}
