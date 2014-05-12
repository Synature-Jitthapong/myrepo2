package com.syn.iorder;

import syn.pos.data.dao.Register;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends Activity implements TextWatcher{
	private String mDeviceCode;
	private EditText txtProductCode;
	private EditText txtLicenceCodeSeq1;
	private EditText txtLicenceCodeSeq2;
	private EditText txtLicenceCodeSeq3;
	private EditText txtLicenceCodeSeq4;
	private EditText txtDeviceCode;
	private EditText txtRegCodeSeq1;
	private EditText txtRegCodeSeq2;
	private EditText txtRegCodeSeq3;
	private EditText txtRegCodeSeq4;
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
		txtLicenceCodeSeq1 = (EditText) findViewById(R.id.txtLicenceCodeSeq1);
		txtLicenceCodeSeq2 = (EditText) findViewById(R.id.txtLicenceCodeSeq2);
		txtLicenceCodeSeq3 = (EditText) findViewById(R.id.txtLicenceCodeSeq3);
		txtLicenceCodeSeq4 = (EditText) findViewById(R.id.txtLicenceCodeSeq4);
		txtDeviceCode = (EditText) findViewById(R.id.txtDeviceCode);
		txtRegCodeSeq1 = (EditText) findViewById(R.id.txtRegCodeSeq1);
		txtRegCodeSeq2 = (EditText) findViewById(R.id.txtRegCodeSeq2);
		txtRegCodeSeq3 = (EditText) findViewById(R.id.txtRegCodeSeq3);
		txtRegCodeSeq4 = (EditText) findViewById(R.id.txtRegCodeSeq4);
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
				Secure.ANDROID_ID).toUpperCase();
		txtUUID.setText(androidId);
		
		// convert android id to numberic by synature register algorhythm
		mDeviceCode = com.syn.iorder.util.SynRegisterAlghorhythm.generateDeviceCode(androidId);
		try {
			txtDeviceCode.setText(mDeviceCode.substring(0, 4) + "-" +
					mDeviceCode.substring(4, 8) + "-" +
					mDeviceCode.substring(8, 12) + "-" +
					mDeviceCode.substring(12, 16));
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
		
		txtLicenceCodeSeq1.addTextChangedListener(this);
		txtLicenceCodeSeq2.addTextChangedListener(this);
		txtLicenceCodeSeq3.addTextChangedListener(this);
		txtLicenceCodeSeq4.addTextChangedListener(this);
		txtRegCodeSeq1.addTextChangedListener(this);
		txtRegCodeSeq2.addTextChangedListener(this);
		txtRegCodeSeq3.addTextChangedListener(this);
		txtRegCodeSeq4.addTextChangedListener(this);
		
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

	public void getRegisterInfo(){
		Register register = new Register(RegisterActivity.this);
		register.getRegisterInfo();
		try {
			txtLicenceCodeSeq1.setText(register.getKeyCode().substring(0,4));
			txtLicenceCodeSeq2.setText(register.getKeyCode().substring(4,8));
			txtLicenceCodeSeq3.setText(register.getKeyCode().substring(8,12));
			txtLicenceCodeSeq4.setText(register.getKeyCode().substring(12,16));
			txtRegCodeSeq1.setText(register.getRegisterCode().substring(0,4));
			txtRegCodeSeq2.setText(register.getRegisterCode().substring(4,8));
			txtRegCodeSeq3.setText(register.getRegisterCode().substring(8,12));
			txtRegCodeSeq4.setText(register.getRegisterCode().substring(12,16));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean checkRegisterCodeDigit(){
		boolean pass = false;
		if(!txtRegCodeSeq1.getText().toString().equals("") && 
				!txtRegCodeSeq2.getText().toString().equals("") &&
				!txtRegCodeSeq3.getText().toString().equals("") &&
				!txtRegCodeSeq4.getText().toString().equals("")){
			if(txtRegCodeSeq1.getText().toString().length() < 4){
				txtRegCodeSeq1.requestFocus();
				pass = false;
			}else if(txtRegCodeSeq2.getText().toString().length() < 4){
				txtRegCodeSeq2.requestFocus();
				pass = false;
			}else if(txtRegCodeSeq3.getText().toString().length() < 4){
				txtRegCodeSeq3.requestFocus();
				pass = false;
			}else if(txtRegCodeSeq4.getText().toString().length() < 4){
				txtRegCodeSeq4.requestFocus();
				pass = false;
			}else{
				pass = true;
			}
		}
		return pass;
	}
	
	private boolean checkLicenceCodeDigit(){
		boolean pass = false;
		if(!txtLicenceCodeSeq1.getText().toString().equals("") && 
				!txtLicenceCodeSeq2.getText().toString().equals("") &&
				!txtLicenceCodeSeq3.getText().toString().equals("") &&
				!txtLicenceCodeSeq4.getText().toString().equals("")){
			if(txtLicenceCodeSeq1.getText().toString().length() < 4){
				txtLicenceCodeSeq1.requestFocus();
				pass = false;
			}else if(txtLicenceCodeSeq2.getText().toString().length() < 4){
				txtLicenceCodeSeq2.requestFocus();
				pass = false;
			}else if(txtLicenceCodeSeq3.getText().toString().length() < 4){
				txtLicenceCodeSeq3.requestFocus();
				pass = false;
			}else if(txtLicenceCodeSeq4.getText().toString().length() < 4){
				txtLicenceCodeSeq4.requestFocus();
				pass = false;
			}else{
				pass = true;
			}
		}
		return pass;
	}
	
	public void onRegisterClicked(){
		String licenceCode = txtLicenceCodeSeq1.getText().toString() +
				txtLicenceCodeSeq2.getText().toString() +
				txtLicenceCodeSeq3.getText().toString() +
				txtLicenceCodeSeq4.getText().toString();
		
		String regCode = txtRegCodeSeq1.getText().toString() +
				txtRegCodeSeq2.getText().toString() +
				txtRegCodeSeq3.getText().toString() +
				txtRegCodeSeq4.getText().toString();
		
		if(!licenceCode.equals("") && !regCode.equals("")){
			if(checkLicenceCodeDigit()){
				if(checkRegisterCodeDigit()){
					try {
						int checkProCode = -1;
						checkProCode = com.syn.iorder.util.SynRegisterAlghorhythm.
								checkProductCode(licenceCode);
						if(checkProCode == 0){
							try {
								int compare = -1;
								compare = com.syn.iorder.util.SynRegisterAlghorhythm.comparison(licenceCode,
										mDeviceCode, regCode);

								if(compare == 0){
									Register register = new Register(RegisterActivity.this);
									register.insertRegisterInfo(licenceCode, mDeviceCode, regCode);

									new AlertDialog.Builder(RegisterActivity.this)
									.setTitle(R.string.register)
									.setMessage(R.string.register_success)
									.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											Intent intent = new Intent(
													RegisterActivity.this,
													TakeOrderActivity.class);
											RegisterActivity.this
													.startActivity(intent);
											RegisterActivity.this.finish();
										}
									}).show();
								}else{
									alertDialog(R.string.register_code_incorrect);
								}
							} catch (Exception e) {
								alertDialog(R.string.licence_or_register_incorrect);
							}

						}else{
							alertDialog(R.string.licence_code_incorrect);
						}
					} catch (Exception e1) {
						alertDialog(R.string.licence_or_register_incorrect);
					}
				}else{
					alertDialog(R.string.enter_register_code);
				}
			}else{
				alertDialog(R.string.enter_licence_code);
			}
		}else{
			alertDialog(licenceCode.equals("") ? R.string.enter_licence_code : 
				R.string.enter_register_code);
		}
	}
	
	private void alertDialog(int msg){
		new AlertDialog.Builder(RegisterActivity.this)
		.setTitle(R.string.register)
		.setMessage(msg)
		.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).show();
	}
	
	public void onCancelClicked(){
		RegisterActivity.this.finish();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		View v = getCurrentFocus();
		if(v instanceof EditText){
			switch(v.getId()){
			case R.id.txtLicenceCodeSeq1:
				if(txtLicenceCodeSeq1.getText().toString().length() == 4)
					txtLicenceCodeSeq2.requestFocus();
			case R.id.txtLicenceCodeSeq2:
				if(txtLicenceCodeSeq2.getText().toString().length() == 4)
					txtLicenceCodeSeq3.requestFocus();
			case R.id.txtLicenceCodeSeq3:
				if(txtLicenceCodeSeq3.getText().toString().length() == 4)
					txtLicenceCodeSeq4.requestFocus();
			case R.id.txtLicenceCodeSeq4:
				if(txtLicenceCodeSeq4.getText().toString().length() == 4)
					txtRegCodeSeq1.requestFocus();
			case R.id.txtRegCodeSeq1:
				if(txtRegCodeSeq1.getText().toString().length() == 4)
					txtRegCodeSeq2.requestFocus();
			case R.id.txtRegCodeSeq2:
				if(txtRegCodeSeq2.getText().toString().length() == 4)
					txtRegCodeSeq3.requestFocus();
			case R.id.txtRegCodeSeq3:
				if(txtRegCodeSeq3.getText().toString().length() == 4)
					txtRegCodeSeq4.requestFocus();
			}
		}
	}
}
