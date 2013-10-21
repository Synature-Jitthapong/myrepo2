package com.syn.iorder;

import org.ksoap2.serialization.PropertyInfo;
import android.content.Context;

public class SaleDateTask extends WebServiceTask{
	protected static final String method = "WSiOrder_JSON_GetCurrentSaleDate";
	
	public SaleDateTask(Context c, GlobalVar gb) {
		super(c, gb, method);
		
		PropertyInfo property = new PropertyInfo();
		property.setName("iComputerID");
		property.setValue(GlobalVar.COMPUTER_ID);
		property.setType(int.class);
		soapRequest.addProperty(property);

	}
	
	@Override
	protected void onPreExecute() {
		progress.dismiss();
	}
	
}