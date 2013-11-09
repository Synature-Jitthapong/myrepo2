package syn.pos.mobile.iordertab;

import java.util.List;
import java.util.Locale;

import syn.pos.data.dao.ShopProperty;
import syn.pos.data.dao.ShowMenuColumnName;
import syn.pos.data.dao.SyncDataLog;
import syn.pos.data.model.ShopData;
import syn.pos.data.model.SyncDataLogModel;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
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
	private EditText txtBackoffice;
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
//			TextView tvMessage = (TextView) findViewById(R.id.textViewMessage);
//			tvMessage.setVisibility(View.VISIBLE);
//			tvMessage.setText(message);
		}
		
		txtServerIp = (EditText) findViewById(R.id.txtServerIp);
		txtBackoffice = (EditText) findViewById(R.id.txtBackoffice);
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

		Button btnSave = (Button) v.findViewById(R.id.btnSave);
		Button btnClose = (Button) v.findViewById(R.id.btnCancel);
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
		String backOffice = txtBackoffice.getText().toString();
		
		if(!serverIp.equals("") && !backOffice.equals("")){
			// delete dbfile
//			syn.pos.data.dao.DataBaseHelper dbHelper
//				= new syn.pos.data.dao.DataBaseHelper(getApplicationContext());
//			dbHelper.deleteDbFile();
//			Log.d("delete dbfile", "delete database file");
			
			syn.pos.data.dao.AppConfig setting = new syn.pos.data.dao.AppConfig(
					context);
			
			setting.addAppConfig(serverIp, backOffice, GlobalVar.DISPLAY_MENU_IMG);

			ShowMenuColumnName showColName = 
					new ShowMenuColumnName(AppConfigLayoutActivity.this);
			ShowMenuColumnName.MenuColumn mc = (ShowMenuColumnName.MenuColumn)
					spinnerShowMenuField.getItemAtPosition(spinnerShowMenuField.getSelectedItemPosition());
			
			showColName.saveUseColumn(mc.getShowMenuColId());

			ShopData.Language lang = (ShopData.Language) 
					spinnerLanguage.getItemAtPosition(spinnerLanguage.getSelectedItemPosition());
			
			ShopProperty shopProp = new ShopProperty(AppConfigLayoutActivity.this, null);
			shopProp.setSelectedLanguage(lang.getLangID());
			IOrderUtility.switchLanguage(AppConfigLayoutActivity.this, lang.getLangCode());
			
		}
	}

	protected void loadSetting() {
		syn.pos.data.dao.AppConfig setting = new syn.pos.data.dao.AppConfig(
				context);

		syn.pos.data.model.AppConfigModel appModel = setting.getConfigs();

		txtServerIp.setText(appModel.getServerIP());
		txtBackoffice.setText(appModel.getWebServiceUrl());
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
		
//		ShowMenuColumnAdapter menuColAdapter =
//				new ShowMenuColumnAdapter(menuColLst);
		
		ArrayAdapter<ShowMenuColumnName.MenuColumn> menuColAdapter = 
				new ArrayAdapter<ShowMenuColumnName.MenuColumn>(this,
						android.R.layout.simple_spinner_dropdown_item, menuColLst);
		
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
		//LanguageAdapter langAdapter = new LanguageAdapter(langLst);
		ArrayAdapter<ShopData.Language> langAdapter = 
				new ArrayAdapter<ShopData.Language>(this, 
						android.R.layout.simple_spinner_dropdown_item, langLst);
		spinnerLanguage.setAdapter(langAdapter);
		
		for(int i = 0; i < langLst.size(); i++){
			if(langLst.get(i).getIsDefault() == 1){
				spinnerLanguage.setSelection(i);
				break;
			}
		}
	}
	
//	private class LanguageAdapter extends BaseAdapter{
//		private List<ShopData.Language> langLst;
//		private LayoutInflater inflater;
//		
//		public LanguageAdapter(List<ShopData.Language> langList){
//			this.langLst = langList;
//			inflater = LayoutInflater.from(AppConfigLayoutActivity.this);
//		}
//		
//		@Override
//		public int getCount() {
//			return langLst != null ? langLst.size(): 0;
//		}
//
//		@Override
//		public ShopData.Language getItem(int position) {
//			return langLst.get(position);
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			ShopData.Language lang = langLst.get(position);
//			ViewHolder holder;
//			if(convertView == null){
//				convertView = inflater.inflate(R.layout.spinner_item, null);
//				holder = new ViewHolder();
//				holder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
//				convertView.setTag(holder);
//			}else{
//				holder = (ViewHolder) convertView.getTag();
//			}
//			holder.textView1.setText(lang.getLangName());
//			return convertView;
//		}
//		
//		private class ViewHolder{
//			public TextView textView1;
//		}
//	}
//	
//	private class ShowMenuColumnAdapter extends BaseAdapter{
//		private List<ShowMenuColumnName.MenuColumn> menuColumnLst;
//		private LayoutInflater inflater;
//		
//		public ShowMenuColumnAdapter(List<ShowMenuColumnName.MenuColumn> colLst){
//			this.menuColumnLst = colLst;
//			this.inflater = LayoutInflater.from(AppConfigLayoutActivity.this);
//		}
//		
//		@Override
//		public int getCount() {
//			return menuColumnLst != null ? menuColumnLst.size() : 0;
//		}
//
//		@Override
//		public ShowMenuColumnName.MenuColumn getItem(int position) {
//			return menuColumnLst.get(position);
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			ShowMenuColumnName.MenuColumn col = menuColumnLst.get(position);
//			ViewHolder holder;
//			if(convertView == null){
//				holder = new ViewHolder();
//				convertView = inflater.inflate(R.layout.spinner_item, null);
//				holder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
//				convertView.setTag(holder);
//			}else{
//				holder = (ViewHolder) convertView.getTag();
//			}
//			holder.textView1.setText(col.getShowMenuColName());
//			return convertView;
//		}
//		
//		private class ViewHolder{
//			private TextView textView1;
//		}
//	}
}
