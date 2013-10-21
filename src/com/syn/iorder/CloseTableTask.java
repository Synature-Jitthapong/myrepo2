package com.syn.iorder;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.WebServiceResult;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class CloseTableTask extends WebServiceTask{
	private WebServiceStateListener callBack;
	
	public CloseTableTask(Context c, GlobalVar gb, int tableId, WebServiceStateListener listener) {
		super(c, gb, "WSiOrder_JSON_ClosePaidTable");
		
		callBack = listener;
		
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

		tvProgress.setText(R.string.wait_progress);
		progress.setMessage(tvProgress.getText().toString());
	}

	@Override
	protected void onPreExecute() {
		progress.show();
	}

	@Override
	protected void onPostExecute(String result) {
		if(progress.isShowing())
			progress.dismiss();
		
		GsonDeserialze gdz = new GsonDeserialze();
		try {
			WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
			
			if(wsResult.getiResultID() == 0){
				final CustomDialog customDialog = new CustomDialog(context,
						R.style.CustomDialog);
				customDialog.title.setVisibility(View.VISIBLE);
				customDialog.title.setText(R.string.close_table_title);
				customDialog.message.setText(R.string.close_table_success);
				customDialog.btnCancel.setVisibility(View.GONE);
				customDialog.btnOk.setText(R.string.global_close_dialog_btn);
				customDialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						customDialog.dismiss();
						
						callBack.onSuccess();
					}
				});
				customDialog.show();
			}else{
				final CustomDialog customDialog = new CustomDialog(context,
						R.style.CustomDialog);
				customDialog.title.setVisibility(View.VISIBLE);
				customDialog.title.setText(R.string.close_table_title);
				customDialog.message.setText(result);
				customDialog.btnCancel.setVisibility(View.GONE);
				customDialog.btnOk.setText(R.string.global_close_dialog_btn);
				customDialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						customDialog.dismiss();
					}
				});
				customDialog.show();
				callBack.onNotSuccess();
			}
		} catch (Exception e) {
			final CustomDialog customDialog = new CustomDialog(context,
					R.style.CustomDialog);
			customDialog.title.setVisibility(View.VISIBLE);
			customDialog.title.setText(R.string.close_table_title);
			customDialog.message.setText(e.getMessage());
			customDialog.btnCancel.setVisibility(View.GONE);
			customDialog.btnOk.setText(R.string.global_close_dialog_btn);
			customDialog.btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					customDialog.dismiss();
				}
			});
			customDialog.show();
			e.printStackTrace();
		}
	}

	
}
