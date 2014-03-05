package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;

import syn.pos.data.model.ReasonGroups;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableName;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class MergeMultiTableActivity extends Activity{
	private Context mContext;
	private GlobalVar mGlobalVar;
	private List<TableInfo> mSelTbLst;
	private List<ReasonGroups.ReasonDetail> mSelReasonLst;
	private int mTbFromId;
	private int mReasonGroupId = 6;
	private Spinner mSpZoneFrom;
	private Spinner mSpZoneTo;
	private ListView mLvFrom;
	private ListView mLvTo;
	private ListView mLvReason;
	private EditText mTxtReason;
	private TextView mTvFrom;
	private TextView mTvTo;
	private ProgressDialog mProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_merge_multi_table);
		mSpZoneFrom = (Spinner) findViewById(R.id.spZoneFrom);
		mSpZoneTo = (Spinner) findViewById(R.id.spZoneTo);
		mLvFrom = (ListView) findViewById(R.id.lvTbFrom);
		mLvTo = (ListView) findViewById(R.id.lvTbTo);
		mLvReason = (ListView) findViewById(R.id.lvReason);
		mTxtReason = (EditText) findViewById(R.id.txtReason);
		mTvFrom = (TextView) findViewById(R.id.tvFrom);
		mTvTo = (TextView) findViewById(R.id.tvTo);
		mContext = this;
		mGlobalVar = new GlobalVar(this);
		mProgress = new ProgressDialog(this);
		mSelTbLst = new ArrayList<TableInfo>();
		mSelReasonLst = new ArrayList<ReasonGroups.ReasonDetail>();
		loadTable();
		loadReason();
	}

	private void mergeMultiTable(){
		String tableIds = "";
		String reasonIds = "";
		
		for(int i = 0; i < mSelTbLst.size(); i++){
			TableInfo tbInfo = mSelTbLst.get(i);
			tableIds += tbInfo.getiTableID();
			if(i < mSelTbLst.size() - 1)
				tableIds += ",";
		}
		for(int i = 0; i < mSelReasonLst.size(); i ++){
			ReasonGroups.ReasonDetail reason = mSelReasonLst.get(i);
			reasonIds += reason.getReasonID();
			if(i < mSelReasonLst.size() - 1)
				reasonIds += ",";
		}
		
		new TableUtils.MergeMultiTableService(mContext, mGlobalVar, mTbFromId, 
				tableIds, reasonIds, mTxtReason.getText().toString(), new ProgressListener(){

					@Override
					public void onPre() {
						mProgress.setMessage(mContext.getString(R.string.merge_table_progress));
						mProgress.show();
					}

					@Override
					public void onPost() {
						if(mProgress.isShowing())
							mProgress.dismiss();
						new AlertDialog.Builder(mContext)
						.setTitle(R.string.merge_table_activity_title)
						.setMessage(R.string.merge_table_result_success)
						.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						}).show();
					}

					@Override
					public void onError(String msg) {
						if(mProgress.isShowing())
							mProgress.dismiss();
						new AlertDialog.Builder(mContext)
						.setTitle(R.string.merge_table_activity_title)
						.setMessage(msg)
						.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
							}
						}).show();
					}
			
		}).execute(GlobalVar.FULL_URL);
	}
	
	private void loadReason(){
		// load reason
		final List<ReasonGroups.ReasonDetail> reasonDetailLst = 
				IOrderUtility.loadReasonFromWs(mContext, mGlobalVar, mReasonGroupId);
		final ReasonAdapter reasonAdapter = 
				new ReasonAdapter(mContext, reasonDetailLst);
		mLvReason.setAdapter(reasonAdapter);
		mLvReason.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {
				// add selected reason
				ReasonGroups.ReasonDetail reasonDetail = (ReasonGroups.ReasonDetail)
						parent.getItemAtPosition(pos);
				
				if(reasonDetail.isChecked()){
					reasonDetail.setChecked(false);
					mSelReasonLst.remove(reasonDetail);
				}else{
					reasonDetail.setChecked(true);
					mSelReasonLst.add(reasonDetail);
				}
				reasonDetailLst.set(pos, reasonDetail);
				reasonAdapter.notifyDataSetChanged();
			}
		});	
	}
	
	private void loadTable(){
		new LoadAllTableV1(this, mGlobalVar, new LoadAllTableV1.LoadTableProgress() {
			
			@Override
			public void onPre() {
				mProgress.setMessage(mContext.getString(R.string.load_table_progress));
				mProgress.show();
			}
			
			@Override
			public void onPost() {
			}
			
			@Override
			public void onError(String msg) {
				if(mProgress.isShowing())
					mProgress.dismiss();
				IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, msg, 0);
			}
			
			@Override
			public void onPost(final TableName tbName) {
				new LoadAllTableV2(mContext, mGlobalVar, new LoadAllTableV2.LoadTableProgress() {
					
					@Override
					public void onPre() {
					}
					
					@Override
					public void onPost() {
					}
					
					@Override
					public void onError(String msg) {
						if(mProgress.isShowing())
							mProgress.dismiss();
						IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, msg, 0);
					}
					
					@Override
					public void onPost(final List<TableInfo> tbInfoLst) {
						if(mProgress.isShowing())
							mProgress.dismiss();
						TableZoneSpinnerAdapter tbZoneAdapter =
								IOrderUtility.createTableZoneAdapter(mContext, tbName); 
						mSpZoneFrom.setAdapter(tbZoneAdapter);
						mSpZoneTo.setAdapter(tbZoneAdapter);

						mSpZoneFrom.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

									@Override
									public void onItemSelected(AdapterView<?> parent, View v,
											int pos, long id) {
										final TableName.TableZone tbZone = 
												(TableName.TableZone) parent.getItemAtPosition(pos);
										final List<TableInfo> newTbInfoLst = 
												IOrderUtility.filterTableNameHaveOrder(tbInfoLst, tbZone);

										mLvFrom.setAdapter(IOrderUtility.createTableNameAdapter(
												mContext, mGlobalVar, newTbInfoLst, false, true));
										mLvFrom.setOnItemClickListener(new OnItemClickListener() {
													@Override
													public void onItemClick(
															AdapterView<?> parent, View v,
															int pos, long id) {

														TableInfo tbInfo = (TableInfo) parent.getItemAtPosition(pos);
														mTbFromId = tbInfo.getiTableID();
														mSelTbLst.clear();
														String tbName = IOrderUtility.formatCombindTableName(tbInfo.isbIsCombineTable(), 
																tbInfo.getSzCombineTableName(), tbInfo.getSzTableName());
														
														mTvFrom.setText(tbName);
														mTvFrom.setSelected(true);
														mTvTo.setText("");
														
														List<TableInfo> tbInfoLstTo = 
																IOrderUtility.filterTableName(tbInfoLst, tbZone, tbInfo.getiTableID());
														
														mLvTo.setAdapter(IOrderUtility.createTableNameAdapter(
																mContext, mGlobalVar, tbInfoLstTo, false, true));
													}

												});
									}

									@Override
									public void onNothingSelected(AdapterView<?> arg0) {

									}

								});

						mSpZoneTo.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

									@Override
									public void onItemSelected(AdapterView<?> parent, View v,
											int pos, long id) {

										TableName.TableZone tbZone = 
												(TableName.TableZone) parent.getItemAtPosition(pos);
										final List<TableInfo> newTbInfoLst = 
												IOrderUtility.filterTableName(tbInfoLst, tbZone, mTbFromId);

										mLvTo.setAdapter(IOrderUtility.createTableNameAdapter(
														mContext, mGlobalVar, newTbInfoLst, false, true));

										mLvTo.setOnItemClickListener(new OnItemClickListener() {

													@Override
													public void onItemClick(
															AdapterView<?> parent, View v,
															int pos, long id) {
														TableInfo tbInfo = (TableInfo) parent.getItemAtPosition(pos);
														if(tbInfo.isChecked()){
															tbInfo.setChecked(false);
															mSelTbLst.remove(tbInfo);
														}else{
															tbInfo.setChecked(true);
															mSelTbLst.add(tbInfo);	
														}
														String tableName = "";
														for(int i = 0; i < mSelTbLst.size(); i++){
															TableInfo selTbInfo = mSelTbLst.get(i);
															tableName += IOrderUtility.formatCombindTableName(selTbInfo.isbIsCombineTable(), 
																	selTbInfo.getSzCombineTableName(), selTbInfo.getSzTableName());
															if(i < mSelTbLst.size() - 1)
																tableName += ", ";
														}
														mTvTo.setText(tableName);
														mTvTo.setSelected(true);
													}

												});

									}

									@Override
									public void onNothingSelected(AdapterView<?> arg0) {

									}

								});
					}
				}).execute(GlobalVar.FULL_URL);
			}
		}).execute(GlobalVar.FULL_URL);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_move_merge_table, menu);
		View v = menu.findItem(R.id.item_confirm).getActionView();
		
		((Button) v.findViewById(R.id.buttonConfirmOk)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(mTbFromId == 0){
					IOrderUtility.alertDialog(mContext, R.string.select_source_table, 0);
				}else if(mSelTbLst.size() == 0){
					IOrderUtility.alertDialog(mContext, R.string.select_destination_table, 0);
				}else if((mSelReasonLst != null && mSelReasonLst.size() == 0) && mTxtReason.getText().toString().isEmpty()){
					IOrderUtility.alertDialog(mContext, R.string.select_reason, 0);
				}else{
					mergeMultiTable();
				}
			}
			
		});
		((Button) v.findViewById(R.id.buttonConfirmCancel)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		return true;
	}
}
