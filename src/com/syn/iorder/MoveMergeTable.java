package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;

import syn.pos.data.dao.Reason;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.ReasonGroups;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableName;
import syn.pos.data.model.ReasonGroups.ReasonDetail;
import syn.pos.data.model.WebServiceResult;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;
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
	
	private EditText mMoveMergeTableTxtReason;
	private Spinner mSpinnerMoveTbZone;
	private Spinner mSpinnerMoveTbZoneTo;
	private Button mBtnMoveMerge;
	private Button mBtnClose;
	private int mFromTbId;
	private int mToTbId;
	
	private boolean isLock = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		mGlobalVar = new GlobalVar(mContext);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.move_table_layout);

		Intent intent = getIntent();
		mFunc = intent.getIntExtra("func", 1); // 1=move;2=merge
		
		mMoveMergeTableTxtReason = (EditText) findViewById(R.id.moveMergeTableTxtReason);
		mSpinnerMoveTbZone = (Spinner) findViewById(R.id.spinnerSourceTableZone);
		mSpinnerMoveTbZoneTo = (Spinner) findViewById(R.id.spinnerDestTableZone);
		
//		new LoadAllTableV1(mContext, mGlobalVar, new LoadAllTableV1.LoadTableProgress() {
//			
//			@Override
//			public void onPre() {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onPost() {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onError(String msg) {
//				IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, msg, 0);
//			}
//			
//			@Override
//			public void onPost(final TableName tbName) {
				new LoadAllTableV2(mContext, mGlobalVar, new LoadAllTableV2.LoadTableProgress() {
					
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
						IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, msg, 0);
					}
					
					@Override
					public void onPost(final List<TableInfo> tbInfoLst) {
						TableZoneSpinnerAdapter tbZoneAdapter =
								IOrderUtility.createTableZoneAdapter(mContext, GlobalVar.sTbName); 
						mSpinnerMoveTbZone.setAdapter(tbZoneAdapter);
						mSpinnerMoveTbZoneTo.setAdapter(tbZoneAdapter);

						mSpinnerMoveTbZone
								.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

									@Override
									public void onItemSelected(AdapterView<?> parent, View v,
											int pos, long id) {
										final TableName.TableZone tbZone = 
												(TableName.TableZone) parent.getItemAtPosition(pos);
										final List<TableInfo> newTbInfoLst = 
												IOrderUtility.filterTableNameHaveOrder(tbInfoLst, tbZone);

										ListView listViewSourceTbName = (ListView) findViewById(R.id.listViewSorceTableName);
										listViewSourceTbName.setAdapter(IOrderUtility.createTableNameAdapter(
												mContext, mGlobalVar, newTbInfoLst, false, true));
										listViewSourceTbName
												.setOnItemClickListener(new OnItemClickListener() {
													@Override
													public void onItemClick(
															AdapterView<?> parent, View v,
															int pos, long id) {

														TableInfo tbInfo = (TableInfo) parent.getItemAtPosition(pos);
														mFromTbId = tbInfo.getiTableID();
														mToTbId = 0;
														String tbName = IOrderUtility.formatCombindTableName(tbInfo.isbIsCombineTable(), 
																tbInfo.getSzCombineTableName(), tbInfo.getSzTableName());
														TextView tvTableFrom = (TextView) findViewById(R.id.tvTbFrom);
														tvTableFrom.setText(tbName);
														TextView tvTableTo = (TextView) findViewById(R.id.tvTbTo);
														tvTableTo.setText("");
														
														List<TableInfo> tbInfoLstTo = 
																IOrderUtility.filterTableName(tbInfoLst, tbZone, tbInfo.getiTableID());
														ListView listViewDestTbName = 
																(ListView) findViewById(R.id.listViewDestTableName);
														listViewDestTbName.setAdapter(IOrderUtility.createTableNameAdapter(
																mContext, mGlobalVar, tbInfoLstTo, false, true));
														
														if(GlobalVar.sIsLockWhenPrintLongbill){
															checkTableAlreadyCallCheckBill(mFromTbId, tbName);
														}
													}

												});
									}

									@Override
									public void onNothingSelected(AdapterView<?> arg0) {

									}

								});

						mSpinnerMoveTbZoneTo
								.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

									@Override
									public void onItemSelected(AdapterView<?> parent, View v,
											int pos, long id) {

										TableName.TableZone tbZone = 
												(TableName.TableZone) parent.getItemAtPosition(pos);
										final List<TableInfo> newTbInfoLst = 
												IOrderUtility.filterTableName(tbInfoLst, tbZone, mFromTbId);

										ListView listViewDestTbName = 
												(ListView) findViewById(R.id.listViewDestTableName);
										listViewDestTbName.setAdapter(
												IOrderUtility.createTableNameAdapter(
														mContext, mGlobalVar, newTbInfoLst, false, true));

										listViewDestTbName
												.setOnItemClickListener(new OnItemClickListener() {

													@Override
													public void onItemClick(
															AdapterView<?> parent, View v,
															int pos, long id) {
														TableInfo tbInfo = (TableInfo) 
																parent.getItemAtPosition(pos);
														mToTbId = 
																tbInfo.getiTableID();
														
														TextView tvTableTo = 
																(TextView) findViewById(R.id.tvTbTo);
														String tableName = IOrderUtility.formatCombindTableName(tbInfo.isbIsCombineTable(), 
																tbInfo.getSzCombineTableName(), tbInfo.getSzTableName());
														tvTableTo.setText(tableName);
														
														if(GlobalVar.sIsLockWhenPrintLongbill){
															checkTableAlreadyCallCheckBill(mToTbId, tableName);
														}
													}

												});

									}

									@Override
									public void onNothingSelected(AdapterView<?> arg0) {

									}

								});
					}
				}).execute(GlobalVar.FULL_URL);
//			}
//		}).execute(GlobalVar.FULL_URL);
	}
	
	/**
	 * @author j1tth4
	 * Receiver for receive current order from tableId
	 */
	private class CurrentOrderReceiver extends ResultReceiver{

		private String tableName;
		
		public CurrentOrderReceiver(Handler handler, String tableName) {
			super(handler);
			this.tableName = tableName;
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case NewWsClient.RESULT_SUCCESS:
				CurrentOrderOfTable.CurrentOrder currentOrder =
					resultData.getParcelable("currentOrder");
				if(currentOrder != null){
					if(currentOrder.xTransaction != null && currentOrder.xTransaction.getiNoPrintBillDetail() > 0){
						String table = getString(R.string.table) + " " + tableName;
						String alreadyPrintLongBill = getString(R.string.already_printed_longbill);
						new AlertDialog.Builder(MoveMergeTable.this)
						.setMessage(table + " " + alreadyPrintLongBill)
						.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).show();
						isLock = true;
						mBtnMoveMerge.setEnabled(false);
					}else{
						if(isLock){
							mBtnMoveMerge.setEnabled(false);
						}else{
							mBtnMoveMerge.setEnabled(true);
						}
					}
				}
				break;
			case NewWsClient.RESULT_ERROR:
				String msg = resultData.getString("msg");
				if(!TextUtils.isEmpty(msg)){
					new AlertDialog.Builder(MoveMergeTable.this)
					.setMessage(msg)
					.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
				}
				break;
			}
		}
		
	}
	
	private void checkTableAlreadyCallCheckBill(int tableId, String tableName){
		CurrentOrderOfTable currentOrder = 
				new CurrentOrderOfTable(this, tableId, new CurrentOrderReceiver(new Handler(), tableName));
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(currentOrder);
		executor.shutdown();
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
		
		mBtnMoveMerge = (Button) v.findViewById(R.id.buttonConfirmOk);
		mBtnClose = (Button) v.findViewById(R.id.buttonConfirmCancel);
		
		ImageView imgSign2 = (ImageView) findViewById(R.id.imageViewMvMrgSign);
		if(mFunc == 1){
			setTitle(R.string.move_table_activity_title);
			mBtnMoveMerge.setText(R.string.btn_move_table);
			mBtnClose.setText(R.string.btn_cancel_move_table);
			
			mBtnMoveMerge.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Reason reason = new Reason(MoveMergeTable.this);
					List<ReasonDetail> reasonLst = reason.listSelectedReasonDetail(mReasonGroupId); 
					if(mFromTbId == 0){
						IOrderUtility.alertDialog(mContext, R.string.select_source_table, 0);
					}else if(mToTbId == 0){
						IOrderUtility.alertDialog(mContext, R.string.select_destination_table, 0);
					}else if((reasonLst != null && reasonLst.size() == 0) && mMoveMergeTableTxtReason.getText().toString().isEmpty()){
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
			mBtnMoveMerge.setText(R.string.btn_merge_table);
			mBtnClose.setText(R.string.btn_cancel_merge_table);			
			mBtnMoveMerge.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Reason reason = new Reason(MoveMergeTable.this);
					List<ReasonDetail> reasonLst = reason.listSelectedReasonDetail(mReasonGroupId); 
					if(mFromTbId == 0){
						IOrderUtility.alertDialog(mContext, R.string.select_source_table, 0);
					}else if(mToTbId == 0){
						IOrderUtility.alertDialog(mContext, R.string.select_destination_table, 0);
					}else if((reasonLst != null && reasonLst.size() == 0) && mMoveMergeTableTxtReason.getText().toString().isEmpty()){
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
			property.setValue(mFromTbId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iMergeToTableID");
			property.setValue(mToTbId);
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
			property.setValue(mMoveMergeTableTxtReason.getText().toString());
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
			property.setValue(mFromTbId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iMoveToTableID");
			property.setValue(mToTbId);
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
			property.setValue(mMoveMergeTableTxtReason.getText().toString());
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
}