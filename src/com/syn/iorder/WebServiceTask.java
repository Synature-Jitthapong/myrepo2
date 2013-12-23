package com.syn.iorder;

import java.io.IOException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;
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
		String url = uri[0];

		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// fetch data
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			
			// tell server not keep connection
			System.setProperty("http.keepAlive", "false");
			
			envelope.dotNet = true;
			envelope.setOutputSoapObject(soapRequest);

			HttpTransportSE androidHttpTransport = new HttpTransportSE(url, 30000);

			String soapAction = nameSpace + webMethod;
			try {
				androidHttpTransport.call(soapAction, envelope);
				try {
					result = envelope.getResponse().toString();
				} catch (SoapFault e) {
					result = mConnErrMsg + "\n" + e.getMessage();
					e.printStackTrace();
				}
			} catch (IOException e) {
				result = mConnErrMsg + "\n" + e.getMessage();
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				result = mConnErrMsg + "\n" + e.getMessage();
				e.printStackTrace();
			}

			if(result == null || result.equals("")){
				result = mConnErrMsg;
			}
		} else {
			// display error
			result = mConnErrMsg;
		}
		return result;
	}

}
