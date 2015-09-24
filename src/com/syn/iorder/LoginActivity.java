package com.syn.iorder;

import java.util.List;

import syn.pos.data.dao.SyncDataLog;
import syn.pos.data.model.TableName;
import syn.pos.data.model.ShopData.StaffPermission;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	public static final int NUM_CLICK_FOR_SETTING = 3;
	
	private int numClick = 0;
	
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
	
	Toast toast;
	CountDownTimer timer;

	@Override
	public void onCreate(Bundle savedInstanceState){
		context = this;
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		tvDeviceCode = (TextView) findViewById(R.id.textViewDeviceCode);
		tvComputer = (TextView) findViewById(R.id.textViewComputer);
		tvLastUpdate = (TextView) findViewById(R.id.textViewUpdateLog);
		tvWsVersion = (TextView) findViewById(R.id.textViewWsVersion);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		txtUserName = (EditText) findViewById(R.id.txtUserName);
		txtPassWord = (EditText) findViewById(R.id.txtPassword);
		
		globalVar = new GlobalVar(context);
		// check config
		if (IOrderUtility.checkConfig(this)) {			
			txtUserName.setSelectAllOnFocus(true);
			txtPassWord.setSelectAllOnFocus(true);
			txtUserName.clearFocus();
			txtPassWord.clearFocus();
			loadDeviceData();
			
			final ProgressDialog progress = new ProgressDialog(LoginActivity.this);
			progress.setTitle(R.string.check_license);
			progress.setCancelable(false);
			IOrderUtility.checkRegister(LoginActivity.this, globalVar, 
					new IOrderUtility.CheckLicense.CheckLicenseListener() {
				
				@Override
				public void onSuccess() {
					if(progress.isShowing())
						progress.dismiss();
//					ShopProperty shopProp = new ShopProperty(LoginActivity.this, null);
//					ShopData.Language lang = shopProp.getLanguage();
					SharedPreferences sharedPref = getSharedPreferences(GlobalVar.PREF_LANG, MODE_PRIVATE);
					int langId = sharedPref.getInt("langId", 1);
					IOrderUtility.switchLanguage(context, langId);
					
					txtPassWord.setOnEditorActionListener(new OnEditorActionListener() {
			
						@Override
						public boolean onEditorAction(TextView v, int actionId,
								KeyEvent event) {
							if (actionId == EditorInfo.IME_ACTION_DONE) {
								doLogin();
								return true;
							}
							return false;
						}
			
					});
			
					btnLogin.setOnClickListener(new Button.OnClickListener() {
			
						@Override
						public void onClick(View v) {
							doLogin();
						}
					});
				}
				
				@Override
				public void onNetworkDown(String msg){
					if(progress.isShowing())
						progress.dismiss();
					new AlertDialog.Builder(LoginActivity.this)
					.setCancelable(false)
					.setTitle(R.string.global_network_connection_problem)
					.setMessage(msg)
					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
				}
				
				@Override
				public void onPre() {
					progress.setMessage(
							LoginActivity.this.getString(R.string.check_license_progress));
					progress.show();
				}
				
				@Override
				public void onFail(String msg) {
					if(progress.isShowing())
						progress.dismiss();
					new AlertDialog.Builder(LoginActivity.this)
					.setCancelable(false)
					.setTitle(R.string.register)
					.setMessage(msg)
					.setNegativeButton(R.string.global_btn_cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
							finish();
						}
					}).show();
				}
			});
		} else {
			Intent intent = new Intent(this, AppConfigLayoutActivity.class);
			startActivity(intent);
			finish();
		}
	}

	private void doLogin(){
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

				checkPermission(staffObj.getStaffID(), staffObj.getStaffCode(), staffObj.getStaffName());
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
						checkPermission(staffObj.getStaffID(), staffObj.getStaffCode(), staffObj.getStaffName());
					}

					@Override
					public void onNotSuccess() {
						
					}
				});
			}
		}).execute(GlobalVar.FULL_URL);
		
		// prepare tablename data
		preparedTableData();
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
	
	private void preparedTableData(){
		new LoadAllTableV1(this, globalVar, new LoadAllTableV1.LoadTableProgress() {
			
			@Override
			public void onPre() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPost() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(String msg) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPost(TableName tbName) {
				GlobalVar.sTbName = tbName;
			}
		}).execute(GlobalVar.FULL_URL);
	}
	
	private void checkPermission(int staffId, String staffCode, String staffName){
		if (staffId != 0) {
			GlobalVar.STAFF_ID = staffId;
			GlobalVar.STAFF_NAME = staffCode + ":"
					+ staffName;

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(
					txtPassWord.getWindowToken(), 0);
			
			
			new PermissionCheckingTask(LoginActivity.this, globalVar, new PermissionCheckingTask.IPermissionChecking() {
				
				@Override
				public void onSuccess(List<StaffPermission> permissionLst) {
					syn.pos.data.dao.ShopProperty shopProperty = 
							new syn.pos.data.dao.ShopProperty(LoginActivity.this, null);
					shopProperty.insertStaffPermissionData(permissionLst);
					
					if(!shopProperty.chkAccessPocketPermission()){
						final CustomDialog dialog = new CustomDialog(LoginActivity.this, R.style.CustomDialog);
						dialog.message.setText(R.string.not_access_pocket);
						dialog.btnCancel.setVisibility(View.GONE);
						dialog.btnOk.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View arg0) {
								btnLogin.setEnabled(true);
								dialog.dismiss();
							}
							
						});
						dialog.show();
					}else{
						Intent intent = new Intent(LoginActivity.this,
								TakeOrderActivity.class);
						startActivity(intent);
						finish();
					}
				}
				
				@Override
				public void onError(String msg) {
					syn.pos.data.dao.ShopProperty shopProperty = 
							new syn.pos.data.dao.ShopProperty(LoginActivity.this, null);
					if(!shopProperty.chkAccessPocketPermission()){
						final CustomDialog dialog = new CustomDialog(LoginActivity.this, R.style.CustomDialog);
						dialog.message.setText(R.string.not_access_pocket);
						dialog.btnCancel.setVisibility(View.GONE);
						dialog.btnOk.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View arg0) {
								btnLogin.setEnabled(true);
								dialog.dismiss();
							}
							
						});
						dialog.show();
					}else{
						Intent intent = new Intent(LoginActivity.this,
								TakeOrderActivity.class);
						startActivity(intent);
						finish();
					}
				}
			}).execute(globalVar.FULL_URL);
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
	public boolean onOptionsItemSelected(final MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.menu_settings:
			if(timer == null){
				timer = new CountDownTimer(2000, 1000){
	
					@Override
					public void onFinish() {
						numClick = 0;
						toast = null;
					}
	
					@Override
					public void onTick(long millisUntilFinished) {
					}
					
				}.start();
			}
			
			++numClick;
			makeToast();
			if(numClick == NUM_CLICK_FOR_SETTING){
				numClick = 0;
				toast = null;
				startActivity(new Intent(LoginActivity.this,
						AppConfigLayoutActivity.class));
				finish();
			}
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
					finish();
					startActivity(new Intent(LoginActivity.this, LoginActivity.class));
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
		case R.id.itemAbout:
			intent = new Intent(LoginActivity.this,
					AboutActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void makeToast(){
		try{
			toast.getView().isShown();
			toast.setText(String.valueOf(numClick));
		}catch(Exception ex){
			toast = Toast.makeText(LoginActivity.this, String.valueOf(numClick), Toast.LENGTH_SHORT);
			toast.show();
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
				finish();
				cusDialog.dismiss();
			}
		});
		cusDialog.show();
	}
}
