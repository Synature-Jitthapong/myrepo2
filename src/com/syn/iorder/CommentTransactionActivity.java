package com.syn.iorder;

import java.util.List;
import syn.pos.data.dao.Reason;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.ReasonGroups;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableInfo.TableName;
import syn.pos.data.model.TableInfo.TableZone;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

public class CommentTransactionActivity extends Activity implements OnClickListener, OnItemSelectedListener, OnItemClickListener {

	private GlobalVar mGlobalVar;
	private Reason mReason;
	private List<ReasonGroups.ReasonDetail> mReasonDetailLst;
	private ReasonAdapter mReasonAdapter;
	private int mSelectedTableId;
	private TableInfo mTableInfo;
	private ListView mLvTable;
	private Spinner mSpTableZone;
	private ListView mLvReason;
	private EditText mTxtReason;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment_transaction);
		
		mLvTable = (ListView) findViewById(R.id.listView1);
		mLvReason = (ListView) findViewById(R.id.listView2);
		mSpTableZone = (Spinner) findViewById(R.id.spinner1);
		mTxtReason = (EditText) findViewById(R.id.editText1);
		
		mGlobalVar = new GlobalVar(CommentTransactionActivity.this);
		mLvTable.setOnItemClickListener(this);
		mLvReason.setOnItemClickListener(this);
		mSpTableZone.setOnItemSelectedListener(this);
		
		new LoadTableTask(CommentTransactionActivity.this, mGlobalVar).execute(GlobalVar.FULL_URL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.comment_transaction, menu);
		MenuItem item = menu.findItem(R.id.item_confirm);
		Button btnCancel = (Button) item.getActionView().findViewById(R.id.buttonConfirmCancel);
		Button btnOk = (Button) item.getActionView().findViewById(R.id.buttonConfirmOk);
		btnCancel.setOnClickListener(this);
		btnOk.setOnClickListener(this);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.buttonConfirmCancel:
			finish();
			break;
		case R.id.buttonConfirmOk:
			onConfirm();
			break;
		}
	}

	private void onConfirm(){
		
		
	}
	
	private class LoadTableTask extends WebServiceTask{
		private static final String webMethod = "WSmPOS_JSON_LoadAllTableData";
		
		public LoadTableTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);
		}

		@Override
		protected void onPostExecute(String result) {
			if(progress.isShowing())
				progress.dismiss();
			
			GsonDeserialze gdz = new GsonDeserialze();

			try {
				mTableInfo = gdz.deserializeTableInfoJSON(result);
				mSpTableZone.setAdapter(IOrderUtility.createTableZoneAdapter(context, mTableInfo));
			} catch (Exception e) {
				IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, result, 0);
			}
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.loading_progress);
			progress.setMessage(tvProgress.getText().toString());
			progress.show();
		}
		
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position,
			long id) {
		switch(parent.getId()){
		case R.id.spinner1:
			TableInfo.TableZone tbZone = (TableZone) parent.getItemAtPosition(position);
			final List<TableInfo.TableName> tbNameLst = IOrderUtility.filterTableNameHaveOrder(mTableInfo, tbZone);
	
			mLvTable.setAdapter(IOrderUtility.createTableNameAdapter(CommentTransactionActivity.this, mGlobalVar, tbNameLst));
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	private void loadReason(){
		mReasonDetailLst = IOrderUtility
				.loadReasonFromWs(CommentTransactionActivity.this, mGlobalVar, 7);
		
		mReasonAdapter = 
				new ReasonAdapter(CommentTransactionActivity.this, mReasonDetailLst);
		mLvReason.setAdapter(mReasonAdapter);
		
		mReason = new Reason(CommentTransactionActivity.this);
		mReason.createSelectedReasonTmp();	
		
		mTxtReason.setText(null);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		switch(parent.getId()){
		case R.id.listView1:
			TableName tbName = (TableName) parent.getItemAtPosition(position);			
			mSelectedTableId = tbName.getTableID();

			loadReason();
			break;
		case R.id.listView2:
			ReasonGroups.ReasonDetail reasonDetail = (ReasonGroups.ReasonDetail) parent
					.getItemAtPosition(position);
			mReason.addSelectedReason(reasonDetail.getReasonID(),
					reasonDetail.getReasonGroupID(),
					reasonDetail.getReasonText());

			if (reasonDetail.isChecked())
				reasonDetail.setChecked(false);
			else
				reasonDetail.setChecked(true);
			mReasonDetailLst.set(position, reasonDetail);
			mReasonAdapter.notifyDataSetChanged();
		break;
		}
	}

}
