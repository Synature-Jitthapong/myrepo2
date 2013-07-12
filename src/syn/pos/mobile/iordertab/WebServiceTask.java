package syn.pos.mobile.iordertab;

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
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public abstract class WebServiceTask extends AsyncTask<String, String, String> {

	protected SoapObject soapRequest;
	private final String nameSpace = "http://tempuri.org/";
	protected String webMethod;
	protected Context context;
	protected GlobalVar globalVar;
	protected ProgressDialog progress;
	protected TextView tvProgress;

	public WebServiceTask(Context c, GlobalVar gb, String method) {
		context = c;
		globalVar = gb;
		webMethod = method;

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
		//progress.setCanceledOnTouchOutside(false);
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

		TextView tvError = new TextView(context);
		tvError.setText(R.string.network_error);
		Log.d("Param to send", soapRequest.toString());
		
		syn.pos.mobile.util.Log.appendLog(context, "SEND JSON \n" + soapRequest.toString());
		
		SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);

		envelope.dotNet = true;
		envelope.setOutputSoapObject(soapRequest);

		System.setProperty("http.keepAlive", "false");
		HttpTransportSE androidHttpTransport = new HttpTransportSE(url, 30000);
		String soapAction = nameSpace + webMethod;
		
		try {
			Thread.sleep(1000);
			try {
				androidHttpTransport.call(soapAction, envelope);
				try {
					Log.i("result", envelope.getResponse().toString());
					result = envelope.getResponse().toString();
				} catch (SoapFault e) {
					result = e.getMessage();
					e.printStackTrace();
				}
			}catch (IOException e) {
				result = tvError.getText().toString();
				e.printStackTrace();
			}catch (XmlPullParserException e) {
				result = tvError.getText().toString();
				e.printStackTrace();
			}
		} catch (InterruptedException e1) {
			tvError.setText(R.string.msg_connection_timeout);
			result = tvError.getText().toString();
			e1.printStackTrace();
		}
		
		return result;
	}

}
