package com.syn.iorder;

import java.io.IOException;
import java.lang.reflect.Type;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import syn.pos.data.model.WebServiceResult;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.TextView;

public abstract class WebServiceTask extends AsyncTask<String, String, String> {
	protected SoapObject soapRequest;
	private final String nameSpace = "http://tempuri.org/";
	protected String webMethod;
	protected Context context;
	protected GlobalVar globalVar;
	protected ProgressDialog progress;
	protected TextView tvProgress;
	protected int mHttpErrCode;
	protected String mHttpErrMsg;
	protected String mConnErrMsg;

	public WebServiceTask(Context c, GlobalVar gb, String method) {
		context = c;
		globalVar = gb;
		webMethod = method;

		mConnErrMsg = context.getString(R.string.network_error);
		
		soapRequest = new SoapObject(nameSpace, webMethod);

		PropertyInfo property = new PropertyInfo();
		property.setName("iShopID");
		property.setValue(GlobalVar.SHOP_ID);
		property.setType(int.class);
		soapRequest.addProperty(property);

		property = new PropertyInfo();
		property.setName("szDeviceCode");
		property.setValue(globalVar.COMPUTER_DATA.getDeviceCode());
		property.setType(String.class);
		soapRequest.addProperty(property);

		progress = new ProgressDialog(c);
		// progress.setCanceledOnTouchOutside(false);
		progress.setCancelable(false);
		tvProgress = new TextView(c);
		tvProgress.setText(R.string.loading_progress);
	}
	
	@Override
	protected void onPreExecute() {
		progress.setMessage(tvProgress.getText().toString());
		progress.show();
	}

	@Override
	protected String doInBackground(String... uri) {
		String result = "";
		String url = uri[0];// + "?WSDL";
		
		System.setProperty("http.keepAlive", "false");
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(soapRequest);
			String soapAction = nameSpace + webMethod;
			HttpTransportSE androidHttpTransport = new HttpTransportSE(url);
			androidHttpTransport.debug = true;
			try {
				androidHttpTransport.call(soapAction, envelope);
				if(envelope.bodyIn instanceof SoapObject){
					SoapObject soapResult = (SoapObject) envelope.bodyIn;
					if(soapResult != null){
						result = soapResult.getProperty(0).toString();
					}else{
						result = "No result!";
					}
				}else if(envelope.bodyIn instanceof SoapFault){
					SoapFault soapFault = (SoapFault) envelope.bodyIn;
					result = soapFault.getMessage();
				}
			} catch (IOException e) {
				result = "Connection problem! Please try again.";
			} catch (XmlPullParserException e) {
				result = e.getMessage();
			}
		}else{
			result = "Cannot connect to network!";
		}
		return result;
	}
	
	public WebServiceResult toServiceObject(String json) throws JsonSyntaxException{
		Gson gson = new Gson();
		Type type = new TypeToken<WebServiceResult>(){}.getType();
		WebServiceResult ws = gson.fromJson(json, type);
		return ws;
	}
}
