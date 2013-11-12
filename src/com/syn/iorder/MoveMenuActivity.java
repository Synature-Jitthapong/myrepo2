package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;

import syn.pos.data.dao.MenuUtil;
import syn.pos.data.dao.Reason;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.OrderSendData;
import syn.pos.data.model.POSTable_MoveMenuData;
import syn.pos.data.model.ReasonGroups;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.WebServiceResult;
import syn.pos.data.model.ReasonGroups.ReasonDetail;
import syn.pos.data.model.TableInfo.TableName;
import syn.pos.data.model.TableInfo.TableZone;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MoveMenuActivity extends Activity {

	private GlobalVar globalVar;
	private Context context;
	private Button btnConfirm;
	private Button btnClose;
	private Button btnClear;
	private Button btnShooseMenu;
	private ListView movedMenuListView;

	private MenuUtil util;
	private ShooseMenuAdapter adapter1;
	private ShooseMenuAdapter adapter2;
	private List<OrderSendData.OrderDetail> menuData;
	private List<OrderSendData.OrderDetail> menuData2;
	private int FROM_TABLE_ID;
	private int TO_TABLE_ID;
	private String FROM_TABLE_NAME;
	private String TO_TABLE_NAME;
	
	private EditText txtMoveMenuReason;
	private ListView menuFromListView;
	private ListView menuToListView;
	private ListView listViewSourceTbName;
	private ListView listViewDestTbName;
	private Spinner spinnerMoveTbZone;
	private Spinner spinnerMoveTbZoneTo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.move_menu_layout);
		
		movedMenuListView = (ListView) findViewById(R.id.moveMenuListViewMenu);
		txtMoveMenuReason = (EditText) findViewById(R.id.moveMenuTxtReason);
		btnShooseMenu = (Button) findViewById(R.id.moveMenuBtnChooseMenu);

		globalVar = new GlobalVar(context);

		spinnerMoveTbZone = (Spinner) findViewById(R.id.spinnerSourceTableZone);
		spinnerMoveTbZoneTo = (Spinner) findViewById(R.id.spinnerDestTableZone);
		
		new LoadTableTask(context, globalVar).execute(GlobalVar.FULL_URL);

		btnShooseMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LayoutInflater factory = LayoutInflater.from(context);
				View shooseMenuLayout = factory.inflate(
						R.layout.shoose_menu_layout, null);

				TextView tvFromTableName = (TextView) shooseMenuLayout
						.findViewById(R.id.shooseMenuDialogTvFromTableName);
				TextView tvToTableName = (TextView) shooseMenuLayout
						.findViewById(R.id.shooseMenuDialogTvToTableName);

				tvFromTableName.setText(FROM_TABLE_NAME);
				tvToTableName.setText(TO_TABLE_NAME);

				menuFromListView = (ListView) shooseMenuLayout
						.findViewById(R.id.shooseMenuDialogMenuFromListView);
				menuToListView = (ListView) shooseMenuLayout
						.findViewById(R.id.shooseMenuDialogMenuToListView);

				// check moved menu
				util = new MenuUtil(context);
				if (util.listMovedMenu().size() == 0) {
					new CurrentOrderTask(context, globalVar).execute(GlobalVar.FULL_URL);
				} else {
					refreshSourceList();

					refreshDestList();
				}

				menuFromListView
						.setOnItemClickListener(new AdapterView.OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View v, int position, long id) {
								OrderSendData.OrderDetail detail = (OrderSendData.OrderDetail) parent
										.getItemAtPosition(position);
								
								util.moveMenu(detail.getiOrderID(),
										detail.getiOrderStatusID(),
										detail.getiProductID(),
										detail.getSzProductName(),
										detail.getfItemQty(), 
										detail.getiSeatID());

								int orderId = detail.getiOrderID();
								detail = util.listMenuToMove(orderId);
								if(detail.getiOrderID() != 0){
									if(detail.getiOrderStatusID() == 1 || detail.getiOrderStatusID() == 2){
										menuData.set(position, detail);
										adapter1.notifyDataSetChanged();
									}
								}else{
									refreshSourceList();
								}
								
								refreshDestList();
							}

						});
				

				menuToListView
						.setOnItemClickListener(new AdapterView.OnItemClickListener() {

							@Override
							public void onItemClick(
									AdapterView<?> parent,
									View v, int position,
									long id) {
								OrderSendData.OrderDetail detail = (OrderSendData.OrderDetail) parent
										.getItemAtPosition(position);
								util.reMoveMenu(
										detail.getiOrderID(),
										detail.getiOrderStatusID(),
										detail.getiProductID(),
										detail.getSzProductName(),
										detail.getfItemQty(),
										detail.getiSeatID());

								int orderId = detail.getiOrderID();
								detail = util.listMovedMenu(orderId);
								if(detail.getiOrderID() > 0){
									menuData2.set(position, detail);
									adapter2.notifyDataSetChanged();
								}else{
									refreshDestList();
								}
								refreshSourceList();
							}
						});
				
				TextView tvTitle = (TextView) shooseMenuLayout.findViewById(R.id.textViewTitle);
				Button btnOk = (Button) shooseMenuLayout.findViewById(R.id.buttonOk);
				Button btnCancel = (Button) shooseMenuLayout.findViewById(R.id.buttonConfirmCancel);
				
				tvTitle.setText(R.string.dialog_select_menu_title);
				btnOk.setText(R.string.dialog_select_menu_ok);
				btnCancel.setText(R.string.dialog_select_menu_cancel);
				
				final Dialog dialog = new Dialog(context, R.style.CustomDialogBottomRadius);
				dialog.setContentView(shooseMenuLayout);
				dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
				dialog.getWindow().setGravity(Gravity.TOP);
				dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
				dialog.show();
				
				btnOk.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						movedMenuListView.setAdapter(adapter2);
						if(adapter2.getCount() > 0)
							btnShooseMenu.setText(R.string.move_menu_btn_edit_menu);
							
						lockSelectedTable();
						
						dialog.dismiss();
					}
				});
				btnCancel.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						util = new MenuUtil(context);
						util.createMoveMenuTemp();
						
						clearMoveList();
						dialog.dismiss();
					}
				});

			}
		});

		// load reason
		final List<ReasonGroups.ReasonDetail> reasonDetailLst = IOrderUtility
				.loadReasonFromWs(context, globalVar, 7);
		
		final ReasonAdapter reasonAdapter = 
				new ReasonAdapter(MoveMenuActivity.this, reasonDetailLst);

		ListView reasonListView = (ListView) findViewById(R.id.moveMenuListViewReason);
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

	private void clearLockSelectedTable(){
		listViewSourceTbName.setEnabled(true);
		listViewDestTbName.setEnabled(true);
		listViewSourceTbName.clearChoices();
		listViewDestTbName.clearChoices();
		
		clear();
		
		btnClear.setVisibility(View.GONE);
	}
	
	private void lockSelectedTable(){
		listViewSourceTbName.setEnabled(false);
		listViewDestTbName.setEnabled(false);
		
		btnClear.setVisibility(View.VISIBLE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_move_menu, menu);
		View v = menu.findItem(R.id.item_confirm).getActionView();

		btnConfirm = (Button) v.findViewById(R.id.buttonConfirmOk);
		btnClose = (Button) v.findViewById(R.id.buttonConfirmCancel);
		btnClear = (Button) v.findViewById(R.id.buttonClear);
		
		btnConfirm.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Reason reason = new Reason(MoveMenuActivity.this);
				List<ReasonDetail> allReasonLst = reason.listAllReasonDetail(7);
				List<ReasonDetail> reasonLst = reason.listSelectedReasonDetail(7); 
				if(FROM_TABLE_ID == 0){
					IOrderUtility.alertDialog(context, R.string.select_source_table, 0);
				}else if(TO_TABLE_ID == 0){
					IOrderUtility.alertDialog(context, R.string.select_destination_table, 0);
				}else if(menuData2 == null){
					IOrderUtility.alertDialog(context, R.string.select_menu_to_move, 0);
				}else if(movedMenuListView.getCount()==0){
					IOrderUtility.alertDialog(context, R.string.select_menu_to_move, 0);
				}else if((allReasonLst != null && allReasonLst.size() > 0) && 
						(reasonLst != null && reasonLst.size() == 0)){
					IOrderUtility.alertDialog(context, R.string.select_reason, 0);
				}else{
					final CustomDialog cfDialog = new CustomDialog(MoveMenuActivity.this, R.style.CustomDialog);
					cfDialog.title.setVisibility(View.VISIBLE);
					cfDialog.title.setText(R.string.cf_move_menu_title);
					cfDialog.message.setText(R.string.cf_move_menu_msg);
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
							new MoveMenuTask(context, globalVar).execute(GlobalVar.FULL_URL);		
						}
						
					});
					
					cfDialog.show();
					
				}

				//reset btnShoose text
				btnShooseMenu.setText(R.string.move_menu_btn_choose_menu);
			}
		});
		btnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				MoveMenuActivity.this.finish();
			}
		});	
		
		btnClear.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				clearLockSelectedTable();
			}
			
		});
		
		return true;
	}

	private void clearMoveList() {
		menuData2 = new ArrayList<OrderSendData.OrderDetail>();

		ShooseMenuAdapter adapter = new ShooseMenuAdapter(context, globalVar,
				menuData2);
		movedMenuListView.setAdapter(adapter);
	}

	private void refreshDestList() {
		menuData2 = util.listMovedMenu();

		adapter2 = new ShooseMenuAdapter(context, globalVar,
				menuData2);
		menuToListView.setAdapter(adapter2);
	}

	private void refreshSourceList() {
		menuData = util.listMenuToMove();

		// set adapter
		adapter1 = new ShooseMenuAdapter(context, globalVar,
				menuData);
		menuFromListView.setAdapter(adapter1);
	}

	private class MoveMenuTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_TableMoveSomeMenuItemToOtherTable";
		
		public MoveMenuTask(Context c, GlobalVar gb) {
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
			property.setName("iFromTableID");
			property.setValue(FROM_TABLE_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iToTableID");
			property.setValue(TO_TABLE_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			MenuUtil menuUtil = new MenuUtil(context);
			List<OrderSendData.OrderDetail> menuData = menuUtil.listMovedMenu();
			POSTable_MoveMenuData move;
			
			if (menuData.size() > 0) {
				move = new POSTable_MoveMenuData();
				move.xListMoveMenuData = new ArrayList<POSTable_MoveMenuData.POSData_MoveMenu>();
				move.iListReasonID = new ArrayList<Integer>();

				for (OrderSendData.OrderDetail detail : menuData) {
					POSTable_MoveMenuData.POSData_MoveMenu menu = new POSTable_MoveMenuData.POSData_MoveMenu();
					menu.setiOrderId(detail.getiOrderID());
					menu.setfAddAmount(detail.getfItemQty());

					move.xListMoveMenuData.add(menu);
				}
				Gson gson = new Gson();
				String szListMoveMenuJson = gson.toJson(move.xListMoveMenuData);

				property = new PropertyInfo();
				property.setName("szJSonListMoveMenuObj");
				property.setValue(szListMoveMenuJson);
				property.setType(String.class);
				soapRequest.addProperty(property);

				property = new PropertyInfo();
				property.setName("szReasonText");
				property.setValue(txtMoveMenuReason.getText().toString());
				property.setType(String.class);
				soapRequest.addProperty(property);
				
				Reason reason = new Reason(context);

				List<ReasonDetail> reasonDetailLst = reason.listSelectedReasonDetail(7);
				List<Integer> reasonIdLst;
				String szListReasonId;
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
			}
		
			Log.d("Move menu param", soapRequest.toString());
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.move_menu_progress);
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
//					.setTitle(R.string.move_menu_dialog_title)
//					.setMessage(R.string.move_menu_result_success)
//					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							MoveMenuActivity.this.finish();
//							dialog.dismiss();
//						}
//					}).show();
					
					final CustomDialog customDialog = new CustomDialog(context, R.style.CustomDialog);
					customDialog.title.setVisibility(View.VISIBLE);
					customDialog.title.setText(R.string.move_menu_dialog_title);
					customDialog.message.setText(R.string.move_menu_result_success);
					customDialog.btnCancel.setVisibility(View.GONE);
					customDialog.btnOk.setText(R.string.global_close_dialog_btn);
					customDialog.btnOk.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							MoveMenuActivity.this.finish();
							customDialog.dismiss();
						}
					});
					customDialog.show();
					
				}else{
//					new AlertDialog.Builder(context)
//					.setTitle(R.string.move_menu_dialog_title)
//					.setMessage(wsResult.getSzResultData())
//					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							MoveMenuActivity.this.finish();
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
							MoveMenuActivity.this.finish();
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
	
	private void clear(){
		// create temptable for move menu
		util = new MenuUtil(context);
		util.createMoveMenuTemp();
		
		//clear menu to move
		clearMoveList();
		
		TextView tvTbFrom = (TextView) findViewById(R.id.tvTbFrom);
		TextView tvTbTo = (TextView) findViewById(R.id.tvTbTo);
		tvTbFrom.setText("");
		tvTbTo.setText("");
		
		// clear to tableid
		TO_TABLE_ID = 0;
		btnShooseMenu.setEnabled(false);
		
		//reset btnShoose text
		btnShooseMenu.setText(R.string.move_menu_btn_choose_menu);
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
				TableZoneSpinnerAdapter tbZoneAdapter = IOrderUtility.createTableZoneAdapter(context, tbInfo);
				spinnerMoveTbZone.setAdapter(tbZoneAdapter);
				spinnerMoveTbZoneTo.setAdapter(tbZoneAdapter);

				spinnerMoveTbZone
						.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent, View v,
									int pos, long id) {

								final TableInfo.TableZone tbZone = (TableZone) parent.getItemAtPosition(pos);
								final List<TableInfo.TableName> tbNameLst = IOrderUtility.filterTableNameHaveOrder(tbInfo, tbZone);

								listViewSourceTbName = (ListView) findViewById(R.id.listViewSorceTableName);
								listViewSourceTbName.setAdapter(IOrderUtility.createTableNameAdapter(context, globalVar, tbNameLst));
								listViewSourceTbName
										.setOnItemClickListener(new OnItemClickListener() {
											@Override
											public void onItemClick(
													AdapterView<?> parent, View v,
													int pos, long id) {
												clear();
												
												TableName tbName = (TableName) parent
														.getItemAtPosition(pos);
												FROM_TABLE_ID = tbName
														.getTableID();
												FROM_TABLE_NAME = tbName
														.getTableName();

												TextView tvTbFrom = (TextView) findViewById(R.id.tvTbFrom);
												tvTbFrom.setText(FROM_TABLE_NAME);
												
												//reset btnShoose text
												btnShooseMenu.setText(R.string.move_menu_btn_choose_menu);
												ListView listViewDestTbName = (ListView) findViewById(R.id.listViewDestTableName);
												List<TableName> tbNameLstTo = IOrderUtility.filterTableName(tbInfo, tbZone, tbName.getTableID());
												listViewDestTbName.setAdapter(IOrderUtility.createTableNameAdapter(context, globalVar, tbNameLstTo));
											}

										});
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {

							}

						});

				spinnerMoveTbZoneTo
						.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent, View v,
									int pos, long id) {

								TableInfo.TableZone tbZone = (TableZone) parent.getItemAtPosition(pos);
								final List<TableInfo.TableName> tbNameLst = IOrderUtility.filterTableName(tbInfo, tbZone, FROM_TABLE_ID);

								listViewDestTbName = (ListView) findViewById(R.id.listViewDestTableName);
								listViewDestTbName.setAdapter(IOrderUtility.createTableNameAdapter(context, globalVar, tbNameLst));

								listViewDestTbName
										.setOnItemClickListener(new OnItemClickListener() {
											@Override
											public void onItemClick(
													AdapterView<?> parent, View v,
													int pos, long id) {
												TableName tbName = (TableName) parent
														.getItemAtPosition(pos);
												TO_TABLE_ID = tbName
														.getTableID();
												TO_TABLE_NAME = tbName
														.getTableName();

												TextView tvTbTo = (TextView) findViewById(R.id.tvTbTo);
												tvTbTo.setText(tbName.getTableName());
												btnShooseMenu.setEnabled(true);
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
	
	private class CurrentOrderTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_LoadCurrentOrderFromTableID";
		
		public CurrentOrderTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);

			PropertyInfo property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(FROM_TABLE_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}
		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.load_current_order_progress);
			progress.setMessage(tvProgress.getText().toString());
			progress.show();
		}
		@Override
		protected void onPostExecute(String result) {
			if(progress.isShowing())
				progress.dismiss();
			
			GsonDeserialze gdz = new GsonDeserialze();
			try {
				OrderSendData orderData = gdz.deserializeOrderSendDataJSON(result);
//				if (orderData != null) {
//					if (orderData.xListOrderDetail != null
//							&& orderData.xListOrderDetail.size() > 0) {
						// add menu to temptable
						util = new MenuUtil(context);
						util.createMoveMenuTemp();
						util.prepareMenuForMove(orderData);

						refreshSourceList();
//					}
//				}
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
