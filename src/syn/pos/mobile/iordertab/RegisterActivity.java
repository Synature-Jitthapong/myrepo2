package syn.pos.mobile.iordertab;

import syn.pos.data.dao.Register;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
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
		String deviceCode = syn.pos.mobile.util.SynRegisterAlghorhythm.generateDeviceCode(androidId);
		formatKeyCodeStyle(deviceCode);

		// device info
		txtProductCode.setText(android.os.Build.ID);
		txtModel.setText(android.os.Build.MODEL);
		txtManufacturer.setText(android.os.Build.MANUFACTURER);
		txtDevice.setText(android.os.Build.DEVICE);
		txtProduct.setText(android.os.Build.PRODUCT);
		txtBrand.setText(android.os.Build.BRAND);
		txtCPUHardware.setText(android.os.Build.CPU_ABI);
		txtCPUAbi.setText(android.os.Build.CPU_ABI2);
		
		getRegisterInfo();
		
	}

	private void formatKeyCodeStyle(String deviceCode){
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
		txtDeviceCode.setText(code);
	}
	
	public void getRegisterInfo(){
		Register register = new Register(RegisterActivity.this);
		txtKeyCode.setText(register.getKeyCode());
		txtRegisterCode.setText(register.getRegisterCode());
	}
	
	public void onRegisterClicked(final View v){
		Register register = new Register(RegisterActivity.this);
		register.insertRegisterInfo(txtKeyCode.getText().toString(), txtDeviceCode.getText().toString(),
					txtRegisterCode.getText().toString());
		
		Intent intent = new Intent(RegisterActivity.this, TakeOrderActivity.class);
		RegisterActivity.this.startActivity(intent);
		RegisterActivity.this.finish();
	}
	
	public void onCancelClicked(final View v){
		RegisterActivity.this.finish();
	}
	
}
