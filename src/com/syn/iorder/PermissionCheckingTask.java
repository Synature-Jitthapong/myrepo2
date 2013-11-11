package com.syn.iorder;

import java.lang.reflect.Type;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import syn.pos.data.model.ShopData;
import syn.pos.mobile.iordertab.R;

import android.content.Context;

public class PermissionCheckingTask extends WebServiceTask {
	private IPermissionChecking listener;
	
	public PermissionCheckingTask(Context c, GlobalVar gb, IPermissionChecking checking) {
		super(c, gb, "WSiOrder_JSON_GetStaffPermission");

		listener = checking;
		
		PropertyInfo property = new PropertyInfo();
		property.setName("iStaffID");
		property.setValue(GlobalVar.STAFF_ID);
		property.setType(int.class);
		soapRequest.addProperty(property);
	}

	@Override
	protected void onPreExecute() {
		tvProgress.setText(R.string.check_permission_progress);
		progress.setMessage(tvProgress.getText());
		progress.show();
	}

	@Override
	protected void onPostExecute(String result) {
		if(progress.isShowing())
			progress.dismiss();
		
		try {
			Gson gson = new Gson();
			Type type = new TypeToken<List<ShopData.StaffPermission>>() {}.getType();
			
			List<ShopData.StaffPermission> permissionLst = gson.fromJson(result, type); 
			
			if(permissionLst != null && permissionLst.size() > 0){
				listener.onSuccess(permissionLst);
			}else{
				listener.onError("Not have permission.");
			}
		} catch (JsonSyntaxException e) {
			listener.onError(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static interface IPermissionChecking{
		public void onSuccess(List<ShopData.StaffPermission> permissionLst);
		public void onError(String msg);
	}
}
