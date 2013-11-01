package com.syn.iorder;

import java.util.Locale;

import syn.pos.data.dao.ShopProperty;
import syn.pos.data.dao.SyncDataLog;
import syn.pos.data.model.ShopData;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private TextView tvLastUpdate;
	private TextView tvWsVersion;
	private TextView tvDeviceCode;
	private TextView tvComputer;
	private EditText txtUserName;
	private EditText txtPassWord;
	private Button btnLogin;
	private Context context;
	private GlobalVar globalVar;
	private String deviceCode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		context = this;
		super.onCreate(savedInstanceState);

		ShopProperty shopProp = new ShopProperty(LoginActivity.this, null);
		ShopData.Language lang = shopProp.getLanguage();
		IOrderUtility.switchLanguage(context, lang.getLangCode());
		
		setContentView(R.layout.activity_login);

		//
		// getWindow().getDecorView().setSystemUiVisibility
		// (View.SYSTEM_UI_FLAG_LOW_PROFILE);
		//
		//
		// getWindow().getDecorView().setSystemUiVisibility
		// (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		//

		globalVar = new GlobalVar(context);

		tvDeviceCode = (TextView) findViewById(R.id.textViewDeviceCode);
		tvComputer = (TextView) findViewById(R.id.textViewComputer);
		tvLastUpdate = (TextView) findViewById(R.id.textViewUpdateLog);
		tvWsVersion = (TextView) findViewById(R.id.textViewWsVersion);
		loadDeviceData();
		
		txtUserName = (EditText) findViewById(R.id.txtUserName);
		txtPassWord = (EditText) findViewById(R.id.txtPassword);

		txtUserName.clearFocus();
		txtPassWord.clearFocus();
		
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new IOrderUtility.CompareSaleDateTask(LoginActivity.this, globalVar, 
						new WebServiceStateListener() {
					
					@Override
					public void onSuccess() {
						updateWebserviceVersion();
						
						btnLogin.setEnabled(false);
						
						syn.pos.data.dao.Staffs staff = new syn.pos.data.dao.Staffs(
								context, txtUserName.getText().toString(),
								txtPassWord.getText().toString());

						syn.pos.data.model.ShopData.Staff staffObj = staff
								.checkLogin();

						takeLogin(staffObj.getStaffID(), staffObj.getStaffCode(), staffObj.getStaffName());
					}

					@Override
					public void onNotSuccess() {
						IOrderUtility util = new IOrderUtility();
						util.syncDataWebService(context, globalVar, new WebServiceStateListener() {
							
							@Override
							public void onSuccess() {
								updateWebserviceVersion();
								
								syn.pos.data.dao.Staffs staff = new syn.pos.data.dao.Staffs(
									context, txtUserName.getText().toString(),
									txtPassWord.getText().toString());
	
								syn.pos.data.model.ShopData.Staff staffObj = staff
									.checkLogin();
								takeLogin(staffObj.getStaffID(), staffObj.getStaffCode(), staffObj.getStaffName());
							}

							@Override
							public void onNotSuccess() {
								
							}
						});
					}
				}).execute(GlobalVar.FULL_URL);
			}
		});
	}
	
	private void updateWebserviceVersion(){
		new IOrderUtility.UpdateWebServiceVersion(LoginActivity.this, globalVar, 
				new WebServiceStateListener(){

					@Override
					public void onSuccess() {

						SyncDataLog syncLog = new SyncDataLog(context);
						syn.pos.data.model.SyncDataLogModel syncData = syncLog
								.getSyncTimeStamp();
						tvLastUpdate.setText(syncData.getSyncTime());
						tvWsVersion.setText(syncData.getWebServiceVersion());
					}

					@Override
					public void onNotSuccess() {
						
					}
			
		}).execute(GlobalVar.FULL_URL);	
	}
	
	private void loadDeviceData(){
		deviceCode = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		tvDeviceCode.setText(deviceCode);
		tvComputer
				.setText(globalVar.COMPUTER_DATA.getComputerID() + " "
						+ globalVar.COMPUTER_DATA.getComputerName() != null ? globalVar.COMPUTER_DATA
						.getComputerName() : "-");

		TextView tvShopName = (TextView) findViewById(R.id.textViewShopName);
		tvShopName.setText(globalVar.SHOP_DATA.getShopName());

		TextView tvVersionName = (TextView) findViewById(R.id.textViewVersionName);
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			tvVersionName.setText(pInfo.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SyncDataLog syncLog = new SyncDataLog(context);
		syn.pos.data.model.SyncDataLogModel syncData = syncLog
				.getSyncTimeStamp();
		tvLastUpdate.setText(syncData.getSyncTime());
		tvWsVersion.setText(syncData.getWebServiceVersion());
	}
	
	private void takeLogin(int staffId, String staffCode, String staffName){
		if (staffId != 0) {
			GlobalVar.STAFF_ID = staffId;
			GlobalVar.STAFF_NAME = staffCode + ":"
					+ staffName;

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(
					txtPassWord.getWindowToken(), 0);
			
			Intent intent = new Intent(LoginActivity.this,
					MainActivity.class);
			LoginActivity.this.startActivity(intent);
//			overridePendingTransition(R.animator.slide_left_in,
//					R.animator.slide_left_out);
			LoginActivity.this.finish();
		} else {
			btnLogin.setEnabled(true);
			TextView tvMsg = new TextView(getApplicationContext());
			tvMsg.setText(R.string.login_fail);
			Toast.makeText(getApplicationContext(),
					tvMsg.getText(), Toast.LENGTH_SHORT).show();
		}	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			LoginActivity.this.finish();

			Intent intent = new Intent(LoginActivity.this,
					AppConfigLayoutActivity.class);
			LoginActivity.this.startActivity(intent);
			return true;
		case R.id.login_menu_exit:
			exitApplication();
			return true;
		case R.id.Update:
			IOrderUtility util = new IOrderUtility();
			util.syncDataWebService(context, globalVar, new WebServiceStateListener(){

				@Override
				public void onSuccess() {
					updateWebserviceVersion();
				}

				@Override
				public void onNotSuccess() {
					// TODO Auto-generated method stub
					
				}
				
			});

			return true;
//		case R.id.log:
//			intent = new Intent(LoginActivity.this, LogActivity.class);
//			LoginActivity.this.startActivity(intent);
//			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Handle the back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitApplication();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	private void exitApplication() {
		final CustomDialog cusDialog = new CustomDialog(LoginActivity.this,
				R.style.CustomDialog);
		cusDialog.title.setVisibility(View.VISIBLE);
		cusDialog.title.setText(R.string.login_dialog_title_cfm_exit);
		cusDialog.message.setText(R.string.login_dialog_msg_cfm_exit);
		cusDialog.btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cusDialog.dismiss();
			}
		});
		cusDialog.btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cusDialog.dismiss();
				LoginActivity.this.finish();
			}
		});
		cusDialog.show();
	}
}
