package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;

import syn.pos.data.dao.Reason;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.ReasonGroups;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.ReasonGroups.ReasonDetail;
import syn.pos.data.model.TableInfo.TableName;
import syn.pos.data.model.TableInfo.TableZone;
import syn.pos.data.model.WebServiceResult;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class MoveMergeTable extends Activity {
	private Context mContext;
	private GlobalVar mGlobalVar;
	private int mFunc;
	private int mReasonGroupId;
	
	private EditText mTxtReason;
	private Spinner mSpMoveTbZone;
	private Spinner mSpMoveTbZoneTo;
	private Button mBtnOk;
	private Button mBtnClose;
	private int mFromTableId;
	private int mToTableId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		mGlobalVar = new GlobalVar(mContext);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.move_table_layout);

		Intent intent = getIntent();
		mFunc = intent.getIntExtra("func", 1); // 1=move;2=merge
		
		mTxtReason = (EditText) findViewById(R.id.moveMergeTableTxtReason);
		mSpMoveTbZone = (Spinner) findViewById(R.id.spinnerSourceTableZone);
		mSpMoveTbZoneTo = (Spinner) findViewById(R.id.spinnerDestTableZone);
		
		new LoadTableTask(mContext, mGlobalVar).execute(GlobalVar.FULL_URL);
	}
	
	private void confirmOperation(int title, int msg){
		final CustomDialog cfDialog = new CustomDialog(MoveMergeTable.this, R.style.CustomDialog);
		cfDialog.title.setVisibility(View.VISIBLE);
		cfDialog.title.setText(title);
		cfDialog.message.setText(msg);
		cfDialog.btnCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				cfDialog.dismiss();
			}
			
		});
		cfDialog.btnOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				cfDialog.dismiss();
				if(mFunc == 1)
					new MoveTableTask(mContext, mGlobalVar).execute(GlobalVar.FULL_URL);
				else
					new MergeTableTask(mContext, mGlobalVar).execute(GlobalVar.FULL_URL);
			}
			
		});	
		cfDialog.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_move_merge_table, menu);
		View v = menu.findItem(R.id.item_confirm).getActionView();
		
		mBtnOk = (Button) v.findViewById(R.id.buttonConfirmOk);
		mBtnClose = (Button) v.findViewById(R.id.buttonConfirmCancel);
		
		ImageView imgSign2 = (ImageView) findViewById(R.id.imageViewMvMrgSign);
		if(mFunc == 1){
			setTitle(R.string.move_table_activity_title);
			mBtnOk.setText(R.string.btn_move_table);
			mBtnClose.setText(R.string.btn_cancel_move_table);
			
			mBtnOk.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Reason reason = new Reason(MoveMergeTable.this);
					List<ReasonDetail> reasonLst = reason.listSelectedReasonDetail(mReasonGroupId); 
					if(mFromTableId == 0){
						IOrderUtility.alertDialog(mContext, R.string.select_source_table, 0);
					}else if(mToTableId == 0){
						IOrderUtility.alertDialog(mContext, R.string.select_destination_table, 0);
					}else if((reasonLst != null && reasonLst.size() == 0) && mTxtReason.getText().toString().isEmpty()){
						IOrderUtility.alertDialog(mContext, R.string.select_reason, 0);
					}else{
						confirmOperation(R.string.cf_move_table_title, R.string.cf_move_table_msg);
					}
				}
			});
			mBtnClose.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					MoveMergeTable.this.finish();
				}
			});
			mReasonGroupId = 5;
		}else{
			setTitle(R.string.merge_table_activity_title);
			mBtnOk.setText(R.string.btn_merge_table);
			mBtnClose.setText(R.string.btn_cancel_merge_table);			
			mBtnOk.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Reason reason = new Reason(MoveMergeTable.this);
					List<ReasonDetail> reasonLst = reason.listSelectedReasonDetail(mReasonGroupId); 
					if(mFromTableId == 0){
						IOrderUtility.alertDialog(mContext, R.string.select_source_table, 0);
					}else if(mToTableId == 0){
						IOrderUtility.alertDialog(mContext, R.string.select_destination_table, 0);
					}else if((reasonLst != null && reasonLst.size() == 0) && mTxtReason.getText().toString().isEmpty()){
						IOrderUtility.alertDialog(mContext, R.string.select_reason, 0);
					}else{
						confirmOperation(R.string.cf_merge_table_title, R.string.cf_merge_table_msg);
					}	
				}
			});
			mBtnClose.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					MoveMergeTable.this.finish();
				}
			});
			mReasonGroupId = 6;
			imgSign2.setImageResource(R.drawable.ic_action_plus);
		}
		
		// load reason
		final List<ReasonGroups.ReasonDetail> reasonDetailLst = 
				IOrderUtility.loadReasonFromWs(mContext, mGlobalVar, mReasonGroupId);
		final ReasonAdapter reasonAdapter = 
				new ReasonAdapter(MoveMergeTable.this, reasonDetailLst);
		
		ListView reasonListView = (ListView) findViewById(R.id.moveMergeTableReasonListView);
		reasonListView.setAdapter(reasonAdapter);

		final Reason reason = new Reason(mContext);
		reason.createSelectedReasonTmp();
		
		reasonListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {
				// add selected reason
				ReasonGroups.ReasonDetail reasonDetail = (ReasonGroups.ReasonDetail)
						parent.getItemAtPosition(pos);
				reason.addSelectedReason(reasonDetail.getReasonID(), 
						reasonDetail.getReasonGroupID(), reasonDetail.getReasonText());
				
				if(reasonDetail.isChecked())
					reasonDetail.setChecked(false);
				else
					reasonDetail.setChecked(true);
				reasonDetailLst.set(pos, reasonDetail);
				reasonAdapter.notifyDataSetChanged();
			}
		});
		
		return true;
	}

	private class MergeTableTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_MergeTable";
		
		public MergeTableTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(GlobalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iCurTableID");
			property.setValue(mFromTableId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iMergeToTableID");
			property.setValue(mToTableId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			Reason reason = new Reason(context);
			List<ReasonDetail> reasonDetailLst;
			List<Integer> reasonIdLst;
			reasonDetailLst = reason.listSelectedReasonDetail(mReasonGroupId);
//			ReasonGroups reasonGroups = new ReasonGroups();
//			reasonGroups.ReasonDetail = reasonDetailLst;
			reasonIdLst = new ArrayList<Integer>();
			for(ReasonDetail detail : reasonDetailLst){
				reasonIdLst.add(detail.getReasonID());
			}
			
			Gson gson = new Gson();
			String szListReasonId = gson.toJson(reasonIdLst);
			property = new PropertyInfo();
			property.setName("szListReasonID");
			property.setValue(szListReasonId);
			property.setType(String.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("szReasonMoveTable");
			property.setValue(mTxtReason.getText().toString());
			property.setType(String.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.merge_table_progress);
			progress.setMessage(tvProgress.getText().toString());
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
//					new AlertDialog.Builder(context)
//					.setTitle(R.string.merge_table_activity_title)
//					.setMessage(R.string.merge_table_result_success)
//					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							MoveMergeTable.this.finish();
//							dialog.dismiss();
//						}
//					}).show();
					final CustomDialog customDialog = new CustomDialog(context, R.style.CustomDialog);
					customDialog.title.setVisibility(View.VISIBLE);
					customDialog.title.setText(R.string.merge_table_activity_title);
					customDialog.message.setText(R.string.merge_table_result_success);
					customDialog.btnCancel.setVisibility(View.GONE);
					customDialog.btnOk.setText(R.string.global_close_dialog_btn);
					customDialog.btnOk.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							MoveMergeTable.this.finish();
							customDialog.dismiss();
						}
					});
					customDialog.show();
				}else{
//					new AlertDialog.Builder(context)
//					.setTitle(R.string.merge_table_activity_title)
//					.setMessage(wsResult.getSzResultData().equals("") ? result : wsResult.getSzResultData())
//					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							MoveMergeTable.this.finish();
//							dialog.dismiss();
//						}
//					}).show();
					final CustomDialog customDialog = new CustomDialog(context, R.style.CustomDialog);
					customDialog.title.setVisibility(View.VISIBLE);
					customDialog.title.setText(R.string.global_dialog_title_error);
					customDialog.message.setText(wsResult.getSzResultData().equals("") ? result : wsResult.getSzResultData());
					customDialog.btnCancel.setVisibility(View.GONE);
					customDialog.btnOk.setText(R.string.global_close_dialog_btn);
					customDialog.btnOk.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							MoveMergeTable.this.finish();
							customDialog.dismiss();
						}
					});
					customDialog.show();
				}
			} catch (Exception e) {
//				new AlertDialog.Builder(context)
//				.setTitle("Exception")
//				.setMessage(result)
//				.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				}).show();
				IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, result, 0);
			}
		}
		
	}
	
	private class MoveTableTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_MoveTable";
		
		public MoveTableTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(GlobalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iCurTableID");
			property.setValue(mFromTableId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iMoveToTableID");
			property.setValue(mToTableId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			Reason reason = new Reason(context);
			List<ReasonDetail> reasonDetailLst;
			List<Integer> reasonIdLst;
			reasonDetailLst = reason.listSelectedReasonDetail(mReasonGroupId);
//			ReasonGroups reasonGroups = new ReasonGroups();
//			reasonGroups.ReasonDetail = reasonDetailLst;
			reasonIdLst = new ArrayList<Integer>();
			for(ReasonDetail detail : reasonDetailLst){
				reasonIdLst.add(detail.getReasonID());
			}
			
			Gson gson = new Gson();
			String szListReasonId = gson.toJson(reasonIdLst);
			property = new PropertyInfo();
			property.setName("szListReasonID");
			property.setValue(szListReasonId);
			property.setType(String.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("szReasonMoveTable");
			property.setValue(mTxtReason.getText().toString());
			property.setType(String.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.move_table_progress);
			progress.setMessage(tvProgress.getText().toString());
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
//					new AlertDialog.Builder(context)
//					.setTitle(R.string.move_table_dialog_title)
//					.setMessage(R.string.move_table_result_success)
//					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							MoveMergeTable.this.finish();
//							dialog.dismiss();
//						}
//					}).show();

					final CustomDialog customDialog = new CustomDialog(context, R.style.CustomDialog);
					customDialog.title.setVisibility(View.VISIBLE);
					customDialog.title.setText(R.string.move_table_dialog_title);
					customDialog.message.setText(R.string.move_table_result_success);
					customDialog.btnCancel.setVisibility(View.GONE);
					customDialog.btnOk.setText(R.string.global_close_dialog_btn);
					customDialog.btnOk.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							MoveMergeTable.this.finish();
							customDialog.dismiss();
						}
					});
					customDialog.show();
				}else{
//					new AlertDialog.Builder(context)
//					.setTitle(R.string.move_table_dialog_title)
//					.setMessage(wsResult.getSzResultData().equals("") ? result : wsResult.getSzResultData())
//					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							MoveMergeTable.this.finish();
//							dialog.dismiss();
//						}
//					}).show();
					final CustomDialog customDialog = new CustomDialog(context, R.style.CustomDialog);
					customDialog.title.setVisibility(View.VISIBLE);
					customDialog.title.setText(R.string.global_dialog_title_error);
					customDialog.message.setText(wsResult.getSzResultData().equals("") ? result : wsResult.getSzResultData());
					customDialog.btnCancel.setVisibility(View.GONE);
					customDialog.btnOk.setText(R.string.global_close_dialog_btn);
					customDialog.btnOk.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							MoveMergeTable.this.finish();
							customDialog.dismiss();
						}
					});
					customDialog.show();
				}
			} catch (Exception e) {
//				new AlertDialog.Builder(context)
//				.setTitle("Exception")
//				.setMessage(result)
//				.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				}).show();
				IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, result, 0);
			}
		}
		
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
				final TableInfo tbInfo = gdz.deserializeTableInfoJSON(result);
				TableZoneSpinnerAdapter tbZoneAdapter =IOrderUtility.createTableZoneAdapter(context, tbInfo); 
				mSpMoveTbZone.setAdapter(tbZoneAdapter);
				mSpMoveTbZoneTo.setAdapter(tbZoneAdapter);

				mSpMoveTbZone
						.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent, View v,
									int pos, long id) {
								final TableZone tbZone = (TableZone) parent
										.getItemAtPosition(pos);
								final List<TableName> tbNameLst = IOrderUtility.filterTableNameHaveOrder(tbInfo, tbZone);

								ListView listViewSourceTbName = (ListView) findViewById(R.id.listViewSorceTableName);
								listViewSourceTbName.setAdapter(IOrderUtility.createTableNameAdapter(context, globalVar, tbNameLst));
								listViewSourceTbName
										.setOnItemClickListener(new OnItemClickListener() {
											@Override
											public void onItemClick(
													AdapterView<?> parent, View v,
													int pos, long id) {

												TableName tbName = (TableName) parent
														.getItemAtPosition(pos);
												mFromTableId = tbName
														.getTableID();
												mToTableId = 0;
												
												TextView tvTableFrom = (TextView) findViewById(R.id.tvTbFrom);
												tvTableFrom.setText(tbName.getTableName());
												TextView tvTableTo = (TextView) findViewById(R.id.tvTbTo);
												tvTableTo.setText("");
												
												List<TableName> tbNameLstTo = IOrderUtility.filterTableName(tbInfo, tbZone, tbName.getTableID());
												ListView listViewDestTbName = (ListView) findViewById(R.id.listViewDestTableName);
												listViewDestTbName.setAdapter(IOrderUtility.createTableNameAdapter(context, globalVar, tbNameLstTo));
											}

										});
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {

							}

						});

				mSpMoveTbZoneTo
						.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent, View v,
									int pos, long id) {

								TableZone tbZone = (TableZone) parent
										.getItemAtPosition(pos);
								final List<TableName> tbNameLst = IOrderUtility.filterTableName(tbInfo, tbZone, mFromTableId);

								ListView listViewDestTbName = (ListView) findViewById(R.id.listViewDestTableName);
								listViewDestTbName.setAdapter(IOrderUtility.createTableNameAdapter(context, globalVar, tbNameLst));

								listViewDestTbName
										.setOnItemClickListener(new OnItemClickListener() {

											@Override
											public void onItemClick(
													AdapterView<?> parent, View v,
													int pos, long id) {
												TableName tbName = (TableName) parent
														.getItemAtPosition(pos);
												mToTableId = tbName
														.getTableID();
												
												TextView tvTableTo = (TextView) findViewById(R.id.tvTbTo);
												tvTableTo.setText(tbName.getTableName());
											}

										});

							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {

							}

						});
			} catch (Exception e) {
//				new AlertDialog.Builder(context)
//				.setTitle("Exception")
//				.setMessage(result)
//				.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				}).show();
				IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, result, 0);
			}
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.load_table_progress);
			progress.setMessage(tvProgress.getText().toString());
			progress.show();
		}
		
	}
}