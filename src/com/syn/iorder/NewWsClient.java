package com.syn.iorder;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.ResultReceiver;

public abstract class NewWsClient implements Runnable{

	public static final String NAME_SPACE = "http://tempuri.org/";
	
	public static final int RESULT_ERROR = 0;
	public static final int RESULT_SUCCESS = 1;
	
	protected Context mContext;
	protected String mUrl;
	protected SoapObject mSoapRequest;
	protected int mTimeOut = 30 * 1000;
	protected String mWebMethod;
	protected PropertyInfo mProperty;
	protected String mResult;
	protected ResultReceiver mReceiver;
	
	public NewWsClient(Context context, String url, String method, ResultReceiver receiver){
		mContext = context;
		mUrl = url;
		mWebMethod = method;
		mReceiver = receiver;
		mSoapRequest = new SoapObject(NAME_SPACE, mWebMethod);
		
		PropertyInfo property = new PropertyInfo();
		property.setName("iShopID");
		property.setValue(GlobalVar.SHOP_ID);
		property.setType(int.class);
		mSoapRequest.addProperty(property);

		property = new PropertyInfo();
		property.setName("szDeviceCode");
		property.setValue(GlobalVar.getDeviceCode(mContext));
		property.setType(String.class);
		mSoapRequest.addProperty(property);
	}
	
	@Override
	public void run() {
		System.setProperty("http.keepAlive", "false");
		ConnectivityManager connMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(mSoapRequest);
			String soapAction = NAME_SPACE + mWebMethod;
			HttpTransportSE androidHttpTransport = new HttpTransportSE(mUrl, mTimeOut);
			//androidHttpTransport.debug = true;
			try {
				androidHttpTransport.call(soapAction, envelope);
				if(envelope.bodyIn instanceof SoapObject){
					SoapObject soapResult = (SoapObject) envelope.bodyIn;
					if(soapResult != null){
						mResult = soapResult.getProperty(0).toString();
					}else{
						mResult = "No result!";
					}
				}else if(envelope.bodyIn instanceof SoapFault){
					SoapFault soapFault = (SoapFault) envelope.bodyIn;
					mResult = soapFault.getMessage();
				}
			} catch (IOException e) {
				mResult = e.getMessage();
			} catch (XmlPullParserException e) {
				mResult = e.getMessage();
			}
		}else{
			mResult = mContext.getString(R.string.network_error);
		}
		onPostExecute(mResult);
	}

	protected abstract void onPostExecute(String result);
}
