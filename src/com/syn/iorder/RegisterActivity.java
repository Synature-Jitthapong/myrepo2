package com.syn.iorder;

import syn.pos.data.dao.Register;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends Activity {
	private EditText txtProductCode;
	private EditText txtKeyCode;
	private EditText txtDeviceCode;
	private EditText txtRegisterCode;
	private EditText txtUUID;
	private EditText txtModel;
	private EditText txtManufacturer;
	private EditText txtDevice;
	private EditText txtProduct;
	private EditText txtBrand;
	private EditText txtCPUHardware;
	private EditText txtCPUAbi;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		txtProductCode = (EditText) findViewById(R.id.editTextProductCode);
		txtKeyCode = (EditText) findViewById(R.id.EditTextKeyCode);
		txtDeviceCode = (EditText) findViewById(R.id.EditTextDeviceCode);
		txtRegisterCode = (EditText) findViewById(R.id.EditTextRegisterCode);
		txtUUID = (EditText) findViewById(R.id.EditTextUUID);
		txtModel = (EditText) findViewById(R.id.EditTextModel);
		txtManufacturer = (EditText) findViewById(R.id.EditTextMenuFecture);
		txtDevice = (EditText) findViewById(R.id.EditTextDevice);
		txtProduct = (EditText) findViewById(R.id.EditTextProduct);
		txtBrand = (EditText) findViewById(R.id.EditTextBrand);
		txtCPUHardware = (EditText) findViewById(R.id.EditTextCPUHardware);
		txtCPUAbi = (EditText) findViewById(R.id.EditTextCPUAbi);
		
		// get android id
		String androidId = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		txtUUID.setText(androidId);
		
		// convert android id to numberic by synature register algorhythm
		String deviceCode = com.syn.iorder.util.SynRegisterAlghorhythm.generateDeviceCode(androidId);
		try {
			txtDeviceCode.setText(formatKeyCodeStyle(deviceCode));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// device info
		txtProductCode.setText(Integer.toString(com.syn.iorder.util.SynRegisterAlghorhythm.PRODUCT_CODE));
		txtModel.setText(android.os.Build.MODEL);
		txtManufacturer.setText(android.os.Build.MANUFACTURER);
		txtDevice.setText(android.os.Build.DEVICE);
		txtProduct.setText(android.os.Build.PRODUCT);
		txtBrand.setText(android.os.Build.BRAND);
		txtCPUHardware.setText(android.os.Build.CPU_ABI);
		txtCPUAbi.setText(android.os.Build.CPU_ABI2);
		
		getRegisterInfo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.register, menu);
		View v = menu.findItem(R.id.item_confirm).getActionView();

		Button btnConfirm = (Button) v.findViewById(R.id.buttonConfirmOk);
		Button btnClose = (Button) v.findViewById(R.id.buttonConfirmCancel);
		btnConfirm.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				onRegisterClicked();
			}
			
		});
		
		btnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				onCancelClicked();
			}
			
		});
		
		return true;
	}

	private String formatKeyCodeStyle(String deviceCode) throws Exception{
		StringBuilder code = new StringBuilder();
		for(int i = 0; i < deviceCode.length(); i++){
			code.append(deviceCode.charAt(i));
			if(i == 3)
				code.append('-');
			else{
				if(i > 3){
					if((i + 1) % 4 == 0){
						if(i + 1 < deviceCode.length())
							code.append('-');
					}
				}
			}
		}
		return code.toString();
	}
	
	public void getRegisterInfo(){
		Register register = new Register(RegisterActivity.this);
		register.getRegisterInfo();
		try {
			txtKeyCode.setText(formatKeyCodeStyle(register.getKeyCode()));
			txtRegisterCode.setText(formatKeyCodeStyle(register.getRegisterCode()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onRegisterClicked(){
		String keyCode = txtKeyCode.getText().toString().replace("-", "");
		String deviceCode = txtDeviceCode.getText().toString().replace("-", "");
		String regCode = txtRegisterCode.getText().toString().replace("-", "");
		
		if(!keyCode.equals("") && !regCode.equals("")){
			// check product code
			
			try {
				int checkProCode = -1;
				checkProCode = com.syn.iorder.util.SynRegisterAlghorhythm.checkProductCode(keyCode);
				if(checkProCode == 0){
					try {
						int compare = -1;
						compare = com.syn.iorder.util.SynRegisterAlghorhythm.comparison(keyCode,
								deviceCode, regCode);

						if(compare == 0){
							Register register = new Register(RegisterActivity.this);
							register.insertRegisterInfo(keyCode, deviceCode, regCode);

							successPopup("Register", "Register successfully.",
									new OnClickListener() {

										@Override
										public void onClick(View arg0) {
											Intent intent = new Intent(
													RegisterActivity.this,
													TakeOrderActivity.class);
											RegisterActivity.this
													.startActivity(intent);
											RegisterActivity.this.finish();
										}
									});
						}else{
							errorPopup("Product code is incorrect.");
						}
					} catch (Exception e) {
						errorPopup("Key Code or Register Code is incorrect.");
					}

				}else{
					errorPopup("Product code is incorrect.");
				}
			} catch (Exception e1) {
				errorPopup("Key Code or Register Code is incorrect.");
			}
			
		}else{
			errorPopup(keyCode.equals("") ? "Please enter Key Code." : "Please enter Register Code.");
		}
	}
	
	private void successPopup(String title, String msg, OnClickListener listener){
		final CustomDialog d = new CustomDialog(RegisterActivity.this, R.style.CustomDialog);
		d.title.setVisibility(View.VISIBLE);
		d.title.setText(title);
		d.message.setText(msg);
		d.btnCancel.setVisibility(View.GONE);
		d.btnOk.setOnClickListener(listener);
		d.show();
	}
	
	private void errorPopup(String msg){
		final CustomDialog d = new CustomDialog(RegisterActivity.this, R.style.CustomDialog);
		d.title.setVisibility(View.VISIBLE);
		d.title.setText(R.string.global_dialog_title_error);
		d.message.setText(msg);
		d.btnCancel.setVisibility(View.GONE);
		d.btnOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				d.dismiss();
			}
			
		});
		d.show();
	}
	
	public void onCancelClicked(){
		RegisterActivity.this.finish();
	}
}
