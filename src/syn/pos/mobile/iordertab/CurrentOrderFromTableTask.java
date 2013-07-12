package syn.pos.mobile.iordertab;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.OrderSendData;
import android.content.Context;
import android.widget.ListView;

public class CurrentOrderFromTableTask extends WebServiceTask {
	private static final String webMethod = "WSiOrder_JSON_LoadCurrentOrderFromTableID";
	private ListView lv;

	public CurrentOrderFromTableTask(Context c, GlobalVar gb, int tableId,
			ListView listView) {
		super(c, gb, webMethod);
		lv = listView;

		PropertyInfo property = new PropertyInfo();
		property.setName("iTableID");
		property.setValue(tableId);
		property.setType(int.class);
		soapRequest.addProperty(property);
	}

	@Override
	protected void onPostExecute(String result) {
		if (progress.isShowing())
			progress.dismiss();

		GsonDeserialze gdz = new GsonDeserialze();
		try {
			OrderSendData currData = gdz.deserializeOrderSendDataJSON(result);
			CurrOrderAdapter currOrderAdapter = 
					new CurrOrderAdapter(context, globalVar, currData);
			lv.setAdapter(currOrderAdapter);
		} catch (Exception e) {
			IOrderUtility.alertDialog(context,
					R.string.global_dialog_title_error, result, 0);
		}
	}
}
