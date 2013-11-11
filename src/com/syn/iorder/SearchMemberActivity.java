package com.syn.iorder;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.Members;
import syn.pos.data.model.WebServiceResult;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SearchMemberActivity extends Activity {
	protected GlobalVar globalVar;
	protected int TO_TRANSACTION_ID;
	protected int TO_COMPUTER_ID;
	protected int MEMBER_ID;
	protected String MEMBER_NAME;
	
	protected EditText txtMemberCode;
	protected TextView tvMemberCode;
	protected TextView tvMemberName;
	protected TextView tvMemberGroup;
	protected TextView tvMemberBirthday;
	protected TextView tvMemberExpire;
	protected TextView tvMemberMobile;
	
	protected ProgressBar loadMemberProgress;
	
	protected Button btnQrCode;
	protected Button btnBarCode;
	protected Button btnSearchMember;
	protected Button btnSearchMemberCancel;
	protected Button btnSearchMemberOk;
	
	protected Members member;
	
	protected String SCAN_MODE = "QR_CODE_MODE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_detail_layout);
		
		// clear member
		globalVar.MEMBER_ID = 0;
		globalVar.MEMBER_NAME = "";
		
		Intent intent = getIntent();
		TO_TRANSACTION_ID = intent.getIntExtra("TO_TRANSACTION_ID", 0);
		TO_COMPUTER_ID = intent.getIntExtra("TO_COMPUTER_ID", 0);
		globalVar = new GlobalVar(SearchMemberActivity.this);
		
		txtMemberCode = (EditText) findViewById(R.id.editTextMemberCode);
		tvMemberCode = (TextView) findViewById(R.id.textViewMemberCode);
		tvMemberName = (TextView) findViewById(R.id.textViewMemberName);
		tvMemberBirthday = (TextView) findViewById(R.id.textViewMemberBirthday);
		tvMemberGroup = (TextView) findViewById(R.id.textViewMemberGroup);
		tvMemberExpire = (TextView) findViewById(R.id.textViewMemberExpire);
		tvMemberMobile = (TextView) findViewById(R.id.textViewMemberMobile);
		btnQrCode = (Button) findViewById(R.id.buttonQrCode);
		btnBarCode = (Button) findViewById(R.id.buttonBarCode);
		
		loadMemberProgress = (ProgressBar) findViewById(R.id.progressBarSearchMember);
		
		btnSearchMember = (Button) findViewById(R.id.buttonSearchMember);
		
		btnBarCode.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent scanIntent = new Intent("com.google.zxing.client.android.SCAN");
				scanIntent.putExtra("SCAN_MODE", "BAR_CODE_MODE");
				startActivityForResult(scanIntent, 0);
			}
			
		});
		
		btnQrCode.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent scanIntent = new Intent("com.google.zxing.client.android.SCAN");
				scanIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(scanIntent, 0);
			}
			
		});
		
		btnSearchMember.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// execute webservice serch member
				if(!txtMemberCode.getText().toString().equals("")){
					new GetMemberInfoTask(SearchMemberActivity.this, globalVar, 
							txtMemberCode.getText().toString()).execute(globalVar.FULL_URL);
				}else{
					IOrderUtility.alertDialog(SearchMemberActivity.this, R.string.global_dialog_title_error, R.string.input_member_code_msg, 0);
				}
			}
			
		});
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_menu_set, menu);
		View v = menu.findItem(R.id.item_confirm).getActionView();
		
		btnSearchMemberCancel = (Button) v.findViewById(R.id.buttonConfirmCancel);
		btnSearchMemberOk = (Button) v.findViewById(R.id.buttonConfirmOk);
		
		btnSearchMemberCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				SearchMemberActivity.this.finish();
			}
			
		});
		btnSearchMemberOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				//SearchMemberActivity.this.finish();
				new SetMemberToTransaction(SearchMemberActivity.this, globalVar).execute(globalVar.FULL_URL);
			}
			
		});
		btnSearchMemberOk.setEnabled(false);
		
		return true;
	}


	@SuppressLint("NewApi")
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		   if (requestCode == 0) {
		      if (resultCode == RESULT_OK) {
		         String contents = intent.getStringExtra("SCAN_RESULT");
		         String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
		         // Handle successful scan
		         /* set membercode to txtMemberCode and then
		          * call onclick btnSearchMember
		          */
		         txtMemberCode.setText(contents);
		         btnSearchMember.callOnClick();
		         
		         // getMemberInfo
		      } else if (resultCode == RESULT_CANCELED) {
		         // Handle cancel
		      }
		   }
	}
	
	protected class SetMemberToTransaction extends WebServiceTask{

		public SetMemberToTransaction(Context c, GlobalVar gb) {
			super(c, gb, "WSiOrder_JSON_SetMemberIDToTransaction");
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iMemberID");
			property.setValue(member.getiMemberID());
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iToTransactionID");
			property.setValue(TO_TRANSACTION_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iToComputerID");
			property.setValue(TO_COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iFromShopID");
			property.setValue(globalVar.SHOP_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iFromComputerID");
			property.setValue(globalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iFromStaffID");
			property.setValue(globalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.wait_progress);
			progress.setMessage(tvProgress.getText());
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
					SearchMemberActivity.this.finish();
					
					InputMethodManager imm = (InputMethodManager)getSystemService(
						      Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(txtMemberCode.getWindowToken(), 0);
				}else{
					IOrderUtility.alertDialog(SearchMemberActivity.this, R.string.global_dialog_title_error,
							wsResult.getSzResultData().equals("") ? result : wsResult.getSzResultData(), 0);
				}
			} catch (Exception e) {
				IOrderUtility.alertDialog(SearchMemberActivity.this, R.string.global_dialog_title_error, 
						result, 0);
				e.printStackTrace();
			}
			
		}
	}
	
	protected class GetMemberInfoTask extends WebServiceTask{

		public GetMemberInfoTask(Context c, GlobalVar gb, String memberCode) {
			super(c, gb, "WSiOrder_JSON_GetMemberInfoFromMemberCode");
			
			member = new Members();
			
			PropertyInfo property = new PropertyInfo();
			property.setName("szMemberCode");
			property.setValue(memberCode);
			property.setType(String.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.progress_searching_member);
			btnSearchMemberOk.setEnabled(false);
			
			loadMemberProgress.setVisibility(View.VISIBLE);
			btnSearchMember.setVisibility(View.GONE);
		}

		@Override
		protected void onPostExecute(String result) {
			loadMemberProgress.setVisibility(View.GONE);
			btnSearchMember.setVisibility(View.VISIBLE);
			
			GsonDeserialze gdz = new GsonDeserialze();
			
			try {
				WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
				if(wsResult.getiResultID() == 0){
					member = gdz.deserializeMembersInfoJSON(wsResult.getSzResultData());
					tvMemberCode.setText(member.getSzMemberCode());
					tvMemberName.setText(member.getSzMemberFullName());
					tvMemberGroup.setText(member.getSzMemberGroupName());
					tvMemberBirthday.setText(member.getSzMemberBirthday());
					tvMemberExpire.setText(member.getSzMemberExpiration());
					tvMemberMobile.setText(member.getSzMemberMobile());
					btnSearchMemberOk.setEnabled(true);
					
					MEMBER_ID = member.getiMemberID();
					MEMBER_NAME = member.getSzMemberFullName();
					
					InputMethodManager imm = (InputMethodManager)getSystemService(
						      Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(txtMemberCode.getWindowToken(), 0);
				}else{
					tvMemberCode.setText("-");
					tvMemberName.setText("-");
					tvMemberBirthday.setText("-");
					tvMemberGroup.setText("-");
					tvMemberExpire.setText("-");
					tvMemberMobile.setText("-");
					
					IOrderUtility.alertDialog(SearchMemberActivity.this, R.string.global_dialog_title_error, 
							wsResult.getSzResultData().equals("") ? result : wsResult.getSzResultData(),0);
				}
			} catch (Exception e) {
				IOrderUtility.alertDialog(SearchMemberActivity.this, R.string.global_dialog_title_error, 
						result, 0);
				e.printStackTrace();
			}
		}
	}
}
