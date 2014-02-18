package com.syn.iorder;

/*
 * order status = 1,2 สามารถแก้ไขได้
 */
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;

import syn.pos.data.dao.MenuUtil;
import syn.pos.data.dao.Reason;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.OrderSendData;
import syn.pos.data.model.ReasonGroups;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableName;
import syn.pos.data.model.WebServiceResult;
import syn.pos.data.model.ReasonGroups.ReasonDetail;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

public class CancelMenuActivity extends Activity {
	private Context context;
	private GlobalVar globalVar;
	private Spinner spinnerTableZone;
	private ListView listViewTableName;
	private ListView listViewMenu;
	private ListView reasonListView;
	private EditText txtCancelMenuReason;
	private Button btnConfirm;
	private Button btnCancel;
	private ProgressBar progressBar;
	OrderSendData detail;
	SelectOrderAdapter menuAdapter;
	private List<OrderSendData.OrderDetail> orderDetailLst;
	
	private int mTableId;
	private String mCancelReason;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context = this;
		globalVar = new GlobalVar(context);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cancel_menu);
		
		spinnerTableZone = (Spinner) findViewById(R.id.spinnerCancelMenuTableZone);
		listViewTableName = (ListView) findViewById(R.id.listViewCancelMenuTableName);
		listViewMenu = (ListView) findViewById(R.id.listViewCancelMenu);
		reasonListView = (ListView) findViewById(R.id.listViewCancelMenuReason);
//		btnConfirm = (Button) findViewById(R.id.buttonConfirm);
//		btnCancel = (Button) findViewById(R.id.buttonConfirmCancel);
		
		progressBar = (ProgressBar) findViewById(R.id.progressBarShooseMenu);
		txtCancelMenuReason = (EditText) findViewById(R.id.txtCancelMenuReason);
		
		new LoadAllTableV1(context, globalVar, new LoadAllTableV1.LoadTableProgress() {
			
			@Override
			public void onPre() {
				
			}
			
			@Override
			public void onPost() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(String msg) {
				IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, msg, 0);
			}
			
			@Override
			public void onPost(final TableName tbName) {
				new LoadAllTableV2(context, globalVar, new LoadAllTableV2.LoadTableProgress() {
					
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
						IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, msg, 0);
					}
					
					@Override
					public void onPost(final List<TableInfo> tbInfoLst) {
						spinnerTableZone.setAdapter(IOrderUtility.createTableZoneAdapter(context, tbName));
						spinnerTableZone.setOnItemSelectedListener(new OnItemSelectedListener(){

							@Override
							public void onItemSelected(AdapterView<?> parent, View v,
									int position, long id) {
								TableName.TableZone tbZone = (TableName.TableZone) parent.getItemAtPosition(position);
								final List<TableInfo> newTbInfoLst = 
										IOrderUtility.filterTableNameHaveOrder(tbInfoLst, tbZone);

								listViewTableName.setAdapter(IOrderUtility.createTableNameAdapter(context, globalVar, newTbInfoLst));
								listViewTableName.setOnItemClickListener(new OnItemClickListener(){

									@Override
									public void onItemClick(AdapterView<?> parent, View v,
											int position, long id) {
										TableInfo tbInfo = (TableInfo) parent.getItemAtPosition(position);
												
										mTableId = tbInfo.getiTableID();

										detail = new OrderSendData();
										detail.xListOrderDetail = new ArrayList<OrderSendData.OrderDetail>();
										menuAdapter = new SelectOrderAdapter(context, globalVar, detail.xListOrderDetail);
										menuAdapter.notifyDataSetChanged();
										
										// create menu temptable
										MenuUtil menuUtil = new MenuUtil(context);
										menuUtil.createMenuTemp();
										
										// load current order
										new CurrentOrderTask(context, globalVar).execute(GlobalVar.FULL_URL);
									}
									
								});
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {
								// TODO Auto-generated method stub
								
							}
							
						});
					}
				}).execute(GlobalVar.FULL_URL);
			}
		}).execute(GlobalVar.FULL_URL);

		// load reason
		final List<ReasonGroups.ReasonDetail> reasonDetailLst = IOrderUtility
				.loadReasonFromWs(context, globalVar, 2);
		
		final ReasonAdapter reasonAdapter = new ReasonAdapter(CancelMenuActivity.this, reasonDetailLst);

		reasonListView = (ListView) findViewById(R.id.listViewCancelMenuReason);
		reasonListView.setAdapter(reasonAdapter);

		final Reason reason = new Reason(context);
		reason.createSelectedReasonTmp();

		reasonListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {
				// add selected reason
				ReasonGroups.ReasonDetail reasonDetail = (ReasonGroups.ReasonDetail) parent
						.getItemAtPosition(pos);
				reason.addSelectedReason(reasonDetail.getReasonID(),
						reasonDetail.getReasonGroupID(),
						reasonDetail.getReasonText());
				
				if(reasonDetail.isChecked())
					reasonDetail.setChecked(false);
				else
					reasonDetail.setChecked(true);
				reasonDetailLst.set(pos, reasonDetail);
				reasonAdapter.notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_cancel_menu, menu);
		View v = menu.findItem(R.id.item_confirm).getActionView();
		btnConfirm = (Button) v.findViewById(R.id.buttonConfirmOk);
		btnCancel = (Button) v.findViewById(R.id.buttonConfirmCancel);
		
		btnConfirm.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Reason reason = new Reason(CancelMenuActivity.this);
				List<ReasonDetail> reasonLst = reason.listSelectedReasonDetail(2); 
				if(mTableId != 0){
					MenuUtil menuUtil = new MenuUtil(context);
					orderDetailLst = menuUtil
							.listMenuToCancel();
					
					 if(orderDetailLst == null){
						IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, R.string.select_menu_to_cancel, 0);
					 }else if(orderDetailLst != null && orderDetailLst.size() == 0){
						 IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, R.string.select_menu_to_cancel, 0);
					 }else if((reasonLst != null && reasonLst.size() == 0) && txtCancelMenuReason.getText().toString().isEmpty()){
						 IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, R.string.select_reason, 0);
					 }else{
						mCancelReason = txtCancelMenuReason.getText().toString();
						
						final CustomDialog cfDialog = new CustomDialog(CancelMenuActivity.this, R.style.CustomDialog);
						cfDialog.title.setVisibility(View.VISIBLE);
						cfDialog.title.setText(R.string.cf_cancel_menu_title);
						cfDialog.message.setText(R.string.cf_cancel_menu_msg);
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
								new CancelMenuTask(context, globalVar).execute(GlobalVar.FULL_URL);
							}
							
						});	
						cfDialog.show();
					 }
				}else{
					IOrderUtility.alertDialog(context,R.string.global_dialog_title_error, R.string.select_source_table, 0);
				}
			}
		});	
		btnCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				CancelMenuActivity.this.finish();
			}
		});
		return true;
	}

	private class CancelMenuTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_TableDeleteMenuItems";
		public CancelMenuTask(Context c, GlobalVar gb) {
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
			property.setName("iTableID");
			property.setValue(mTableId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("szReasonDelete");
			property.setValue(mCancelReason);
			property.setType(String.class);
			soapRequest.addProperty(property);

			//List<CancelOrder> orderIdLst = new ArrayList<CancelOrder>();
			List<Integer> orderIdLst = new ArrayList<Integer>();
			if (orderDetailLst.size() > 0) {
				for (OrderSendData.OrderDetail detail : orderDetailLst) {
//					CancelOrder cancelOrder = new CancelOrder();
//					cancelOrder.setiOrderID(detail.getiOrderID());
//					orderIdLst.add(cancelOrder);
					orderIdLst.add(detail.getiOrderID());
				}
			}
			Gson gson = new Gson();
			String szListOrderId = gson.toJson(orderIdLst);

			property = new PropertyInfo();
			property.setName("szListOrderID");
			property.setValue(szListOrderId);
			property.setType(String.class);
			soapRequest.addProperty(property);
			
			Reason reason = new Reason(context);
			List<Integer> reasonIdLst;
			String szListReasonId;
			List<ReasonDetail> reasonDetailLst = reason.listSelectedReasonDetail(2);
			reasonIdLst = new ArrayList<Integer>();
			for(ReasonDetail detail : reasonDetailLst){
				reasonIdLst.add(detail.getReasonID());
			}
			gson = new Gson();
			szListReasonId = gson.toJson(reasonIdLst);
			property = new PropertyInfo();
			property.setName("szListReasonID");
			property.setValue(szListReasonId);
			property.setType(String.class);
			soapRequest.addProperty(property);
			
			Log.d("Cancel menu param ", soapRequest.toString());
		}
		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.cancel_menu_progress);
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
//					.setTitle(R.string.cancel_menu_dialog_title)
//					.setMessage(R.string.cancel_menu_result_success)
//					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							CancelMenuActivity.this.finish();
//							dialog.dismiss();
//						}
//					}).show();
					final CustomDialog customDialog = new CustomDialog(context, R.style.CustomDialog);
					customDialog.title.setVisibility(View.VISIBLE);
					customDialog.title.setText(R.string.cancel_menu_dialog_title);
					customDialog.message.setText(R.string.cancel_menu_result_success);
					customDialog.btnCancel.setVisibility(View.GONE);
					customDialog.btnOk.setText(R.string.global_close_dialog_btn);
					customDialog.btnOk.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							CancelMenuActivity.this.finish();
							customDialog.dismiss();
						}
					});
					customDialog.show();
				}else{
//					new AlertDialog.Builder(context)
//					.setTitle(R.string.cancel_menu_dialog_title)
//					.setMessage(wsResult.getSzResultData())
//					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							CancelMenuActivity.this.finish();
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
							CancelMenuActivity.this.finish();
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

	private class CurrentOrderTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_LoadCurrentOrderFromTableID";
		
		public CurrentOrderTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);

			PropertyInfo property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(mTableId);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}
		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.load_current_order_progress);
			progressBar.setVisibility(View.VISIBLE);
		}
		@Override
		protected void onPostExecute(String result) {
			progressBar.setVisibility(View.GONE);
			
			GsonDeserialze gdz = new GsonDeserialze();
			try {
				detail = gdz.deserializeOrderSendDataJSON(result);
				List<OrderSendData.OrderDetail> orderFilter = IOrderUtility.filterProductType(detail.xListOrderDetail);
				menuAdapter = new SelectOrderAdapter(context, globalVar, orderFilter);
				listViewMenu.setAdapter(menuAdapter);
				
				listViewMenu.setOnItemClickListener(new OnItemClickListener(){
					
					@Override
					public void onItemClick(
							AdapterView<?> parent, View v,
							int position, long id) {
						OrderSendData.OrderDetail data = (OrderSendData.OrderDetail) 
								parent.getItemAtPosition(position);//detail.xListOrderDetail.get(position);
						if(data.getiOrderStatusID() == 1 || data.getiOrderStatusID() == 2){
							MenuUtil menuUtil = new MenuUtil(context);
							menuUtil.prepareMenuForCacnel(data);
							
							CheckBox chkBox = (CheckBox) v.findViewById(R.id.checkBox1);
							// check if item is selected
							menuUtil = new MenuUtil(context);
							if(menuUtil.checkSelectedMenu(data.getiOrderID(), data.getiProductID()) != 0)
								chkBox.setChecked(true);
							else
								chkBox.setChecked(false);
						}
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
	}
}
