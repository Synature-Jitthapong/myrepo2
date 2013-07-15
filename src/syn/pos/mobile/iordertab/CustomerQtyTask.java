package syn.pos.mobile.iordertab;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.WebServiceResult;

import android.content.Context;

public class CustomerQtyTask extends WebServiceTask{
	private WebServiceTaskState wsState;
	public CustomerQtyTask(Context c, GlobalVar gb, int tableId, WebServiceTaskState listener) {
		super(c, gb, "WSiOrder_JSON_GetToalCustomerForTableID");
		
		wsState = listener;
		
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
		wsState.onProgress();
	}
	@Override
	protected void onPostExecute(String result) {
		GsonDeserialze gdz = new GsonDeserialze();
		
		try {
			WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
			if(wsResult.getiResultID() == 0){
				int totalCust = 0;
				try {
					totalCust = Integer.parseInt(wsResult.getSzResultData());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				wsState.onSuccess(totalCust);
			}else{
				wsState.onNotSuccess();
			}
		} catch (Exception e) {
			wsState.onNotSuccess();
			e.printStackTrace();
		}
	}

	
}
