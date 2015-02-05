package com.syn.iorder;

import java.util.List;

import syn.pos.data.dao.ShopProperty;
import syn.pos.data.dao.ShowMenuColumnName;
import syn.pos.data.dao.SyncDataLog;
import syn.pos.data.model.ShopData;
import syn.pos.data.model.SyncDataLogModel;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class AppConfigLayoutActivity extends Activity {
	private Context context;
	private EditText txtServerIp;
	private EditText txtWsName;
	private Switch swDisplayMenuImage;
	private Spinner spinnerShowMenuField;
	private Spinner spinnerLanguage;
	private TextView tvWsVersion;
	private String message;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		context = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_setting_layout);

		Intent intent = getIntent();
		message = intent.getStringExtra("message");
		
		if(message != null)
		{
			TextView tvMessage = (TextView) findViewById(R.id.textViewMessage);
			tvMessage.setVisibility(View.VISIBLE);
			tvMessage.setText(message);
		}
		
		txtServerIp = (EditText) findViewById(R.id.txtServerIp);
		txtWsName = (EditText) findViewById(R.id.txtWsName);
		tvWsVersion = (TextView) findViewById(R.id.textView2);
		swDisplayMenuImage = (Switch) findViewById(R.id.switchDisplayMenuImage);
		spinnerShowMenuField = (Spinner) findViewById(R.id.spinnerShowMenuField);
		spinnerLanguage = (Spinner) findViewById(R.id.spinnerLanguage);
		
		swDisplayMenuImage.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					GlobalVar.DISPLAY_MENU_IMG = 1;
				}else{
					GlobalVar.DISPLAY_MENU_IMG = 0;
				}
			}
			
		});

		loadSetting();
		loadLanguage();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_app_setting, menu);
		View v = menu.findItem(R.id.item_setting_control).getActionView();

		Button btnSave = (Button) v.findViewById(R.id.buttonConfirmOk);
		btnSave.setText(R.string.app_config_btn_save);
		//btnSave.setBackgroundResource(R.drawable.green_button);
		Button btnClose = (Button) v.findViewById(R.id.buttonConfirmCancel);
		btnClose.setText(R.string.global_btn_close);
		btnSave.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				saveSetting();
			}

		});
		btnClose.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				//if(IOrderUtility.checkRegister(AppConfigLayoutActivity.this)){
					Intent intent = new Intent(AppConfigLayoutActivity.this, LoginActivity.class);
					AppConfigLayoutActivity.this.startActivity(intent);
					AppConfigLayoutActivity.this.finish();
//				}else{
//					Intent intent = new Intent(AppConfigLayoutActivity.this, RegisterActivity.class);
//					AppConfigLayoutActivity.this.startActivity(intent);
//					AppConfigLayoutActivity.this.finish();
//				}
			}

		});
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Handle the back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	protected void saveSetting() {
		String serverIp = txtServerIp.getText().toString();
		String wsName = txtWsName.getText().toString();
		
		if(!serverIp.equals("") && !wsName.equals("")){
			// delete dbfile
//			syn.pos.data.dao.DataBaseHelper dbHelper
//				= new syn.pos.data.dao.DataBaseHelper(getApplicationContext());
//			dbHelper.deleteDbFile();
//			Log.d("delete dbfile", "delete database file");
			
			syn.pos.data.dao.AppConfig setting = new syn.pos.data.dao.AppConfig(
					context);
			
			setting.addAppConfig(serverIp, wsName, GlobalVar.DISPLAY_MENU_IMG);

			ShowMenuColumnName showColName = 
					new ShowMenuColumnName(AppConfigLayoutActivity.this);
			ShowMenuColumnName.MenuColumn mc = (ShowMenuColumnName.MenuColumn)
					spinnerShowMenuField.getItemAtPosition(spinnerShowMenuField.getSelectedItemPosition());
			
			showColName.saveUseColumn(mc.getShowMenuColId());

			ShopData.Language lang = (ShopData.Language) 
					spinnerLanguage.getItemAtPosition(spinnerLanguage.getSelectedItemPosition());
			
//			ShopProperty shopProp = new ShopProperty(AppConfigLayoutActivity.this, null);
//			shopProp.setSelectedLanguage(lang.getLangID());
			
			SharedPreferences sharedPref = getSharedPreferences(GlobalVar.PREF_LANG, MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt("langId", lang.getLangID());
			editor.commit();
			IOrderUtility.switchLanguage(AppConfigLayoutActivity.this, lang.getLangID());
			
			//loadSetting();
			
//			new AlertDialog.Builder(context)
//					.setIcon(android.R.drawable.ic_dialog_alert)
//					.setTitle("Setting")
//					.setMessage("Save setting success.")
//					.setNeutralButton(R.string.global_close_dialog_btn,
//							new DialogInterface.OnClickListener() {
//	
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//									IOrderUtility util = new IOrderUtility();
//									util.syncDataWebService(context, globalVar);
//								}
//	
//							}).show();
			
			final CustomDialog customDialog = new CustomDialog(context, R.style.CustomDialog);
			customDialog.title.setVisibility(View.VISIBLE);
			customDialog.title.setText(R.string.app_setting_dialog_title);
			customDialog.message.setText(R.string.app_setting_save_success);
			customDialog.btnCancel.setVisibility(View.GONE);
			customDialog.btnOk.setText(R.string.global_close_dialog_btn);
			//customDialog.btnOk.setBackgroundResource(R.drawable.green_button);
			customDialog.btnOk.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
//					IOrderUtility util = new IOrderUtility();
//					util.syncDataWebService(context, globalVar, new WebServiceStateListener(){
//
//						@Override
//						public void onSuccess() {
//							new IOrderUtility.UpdateWebServiceVersion(AppConfigLayoutActivity.this, globalVar, 
//									new WebServiceStateListener(){
//
//										@Override
//										public void onSuccess() {
//											SyncDataLog syncLog = new SyncDataLog(AppConfigLayoutActivity.this);
//											SyncDataLogModel syncData = syncLog.getSyncTimeStamp();
//											tvWsVersion.setText(syncData.getWebServiceVersion());
//										}
//
//										@Override
//										public void onNotSuccess() {
//											
//										}
//								
//							}).execute(GlobalVar.FULL_URL);
//						}
//
//						@Override
//						public void onNotSuccess() {
//							
//						}
//					});
					customDialog.dismiss();
					//refreshActivity();
				}
			});
			customDialog.show();
		}else{
//			new AlertDialog.Builder(context)
//			.setIcon(android.R.drawable.ic_dialog_alert)
//			.setTitle("Setting")
//			.setMessage("Please input server IP and webservice name")
//			.setNeutralButton(R.string.global_close_dialog_btn,
//					new DialogInterface.OnClickListener() {
//
//						@Override
//						public void onClick(DialogInterface dialog,
//								int which) {
//							IOrderUtility util = new IOrderUtility();
//							util.syncDataWebService(context, globalVar);
//						}
//
//					}).show();
			
			final CustomDialog customDialog = new CustomDialog(context, R.style.CustomDialog);
			customDialog.title.setVisibility(View.VISIBLE);
			customDialog.title.setText(R.string.app_setting_dialog_title);
			customDialog.message.setText(R.string.app_setting_input_data);
			customDialog.btnCancel.setVisibility(View.GONE);
			customDialog.btnOk.setText(R.string.global_close_dialog_btn);
			customDialog.btnOk.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					customDialog.dismiss();
				}
			});
			customDialog.show();
		}
	}

	protected void loadSetting() {
		syn.pos.data.dao.AppConfig setting = new syn.pos.data.dao.AppConfig(
				context);

		syn.pos.data.model.AppConfigModel appModel = setting.getConfigs();

		txtServerIp.setText(appModel.getServerIP());
		txtWsName.setText(appModel.getWebServiceUrl());
		GlobalVar.DISPLAY_MENU_IMG = appModel.getDisplayImageMenu();
		
		if(GlobalVar.DISPLAY_MENU_IMG == 1){
			swDisplayMenuImage.setChecked(true);
		}else{
			swDisplayMenuImage.setChecked(false);
		}

		GlobalVar.SERVER_IP = "http://" + appModel.getServerIP() + "/";
		GlobalVar.WS_URL = appModel.getWebServiceUrl() + "/ws_mpos.asmx";
		GlobalVar.FULL_URL = GlobalVar.SERVER_IP + GlobalVar.WS_URL;
		
		SyncDataLog syncLog = new SyncDataLog(AppConfigLayoutActivity.this);
		SyncDataLogModel syncData = syncLog.getSyncTimeStamp();
		
		tvWsVersion.setText(syncData.getWebServiceVersion());

		loadShowMenuColumn();
	}
	
	public void loadShowMenuColumn(){
		ShowMenuColumnName showColName = 
				new ShowMenuColumnName(AppConfigLayoutActivity.this);
		List<ShowMenuColumnName.MenuColumn> menuColLst = 
				showColName.listMenuColumn();
		
		ShowMenuColumnAdapter menuColAdapter =
				new ShowMenuColumnAdapter(menuColLst);
		
		spinnerShowMenuField.setAdapter(menuColAdapter);
		ShowMenuColumnName.MenuColumn mc = showColName.getShowMenuColumnObj();
		for(int i = 0; i< menuColLst.size(); i++){
			if(menuColLst.get(i).getShowMenuColId() == mc.getShowMenuColId()){
				spinnerShowMenuField.setSelection(i);
				break;
			}
		}
	}
	
	private void refreshActivity(){
		finish();
		startActivity(getIntent());
	}
	
	private void loadLanguage(){
		ShopProperty sp = new ShopProperty(AppConfigLayoutActivity.this, null);
		
		List<ShopData.Language> langLst = sp.listLanguage();
		LanguageAdapter langAdapter = new LanguageAdapter(langLst);
		spinnerLanguage.setAdapter(langAdapter);
		
		SharedPreferences sharedPref = getSharedPreferences(GlobalVar.PREF_LANG, MODE_PRIVATE);
		int selectedLangId = sharedPref.getInt("langId", 1);
		for(int i = 0; i < langLst.size(); i++){
			if(langLst.get(i).getLangID() == selectedLangId){
				spinnerLanguage.setSelection(i);
				break;
			}
		}
	}
	
	private class LanguageAdapter extends BaseAdapter{
		private List<ShopData.Language> langLst;
		private LayoutInflater inflater;
		
		public LanguageAdapter(List<ShopData.Language> langList){
			this.langLst = langList;
			inflater = LayoutInflater.from(AppConfigLayoutActivity.this);
		}
		
		@Override
		public int getCount() {
			return langLst != null ? langLst.size(): 0;
		}

		@Override
		public ShopData.Language getItem(int position) {
			return langLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ShopData.Language lang = langLst.get(position);
			ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.spinner_item, null);
				holder = new ViewHolder();
				holder.textView1 = (TextView) convertView;
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textView1.setText(lang.getLangName());
			return convertView;
		}
		
		private class ViewHolder{
			public TextView textView1;
		}
	}
	
	private class ShowMenuColumnAdapter extends BaseAdapter{
		private List<ShowMenuColumnName.MenuColumn> menuColumnLst;
		private LayoutInflater inflater;
		
		public ShowMenuColumnAdapter(List<ShowMenuColumnName.MenuColumn> colLst){
			this.menuColumnLst = colLst;
			this.inflater = LayoutInflater.from(AppConfigLayoutActivity.this);
		}
		
		@Override
		public int getCount() {
			return menuColumnLst != null ? menuColumnLst.size() : 0;
		}

		@Override
		public ShowMenuColumnName.MenuColumn getItem(int position) {
			return menuColumnLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ShowMenuColumnName.MenuColumn col = menuColumnLst.get(position);
			ViewHolder holder;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.spinner_item, null);
				holder.textView1 = (TextView) convertView;
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textView1.setText(col.getShowMenuColName());
			return convertView;
		}
		
		private class ViewHolder{
			private TextView textView1;
		}
	}
}
