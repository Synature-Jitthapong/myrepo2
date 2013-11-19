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

	private GlobalVar mGlobalVar;
	private Context mContext;
	private Button mBtnConfirm;
	private Button mBtnClose;
	private Button mBtnClear;
	private Button mBtnShooseMenu;
	private ListView mMovedMenuListView;

	private MenuUtil mMenuUtil;
	private ShooseMenuAdapter mAdapter1;
	private ShooseMenuAdapter mAdapter2;
	private List<OrderSendData.OrderDetail> mMenuData;
	private List<OrderSendData.OrderDetail> mMenuData2;
	private int mFromTableId;
	private int mToTableId;
	private String mFromTableName;
	private String mToTableName;
	
	private EditText mTxtMoveMenuReason;
	private ListView mMenuFromListView;
	private ListView mMenuToListView;
	private ListView mLvSourceTbName;
	private ListView mLvDestTbName;
	private Spinner mSpMoveTbZone;
	private Spinner mSpMoveTbZoneTo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.move_menu_layout);
		
		mMovedMenuListView = (ListView) findViewById(R.id.moveMenuListViewMenu);
		mTxtMoveMenuReason = (EditText) findViewById(R.id.moveMenuTxtReason);
		mBtnShooseMenu = (Button) findViewById(R.id.moveMenuBtnChooseMenu);
		mSpMoveTbZone = (Spinner) findViewById(R.id.spinnerSourceTableZone);
		mSpMoveTbZoneTo = (Spinner) findViewById(R.id.spinnerDestTableZone);

		mGlobalVar = new GlobalVar(mContext);

		new LoadTableTask(mContext, mGlobalVar).execute(GlobalVar.FULL_URL);

		mBtnShooseMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LayoutInflater factory = LayoutInflater.from(mContext);
				View shooseMenuLayout = factory.inflate(
						R.layout.shoose_menu_layout, null);

				TextView tvFromTableName = (TextView) shooseMenuLayout
						.findViewById(R.id.shooseMenuDialogTvFromTableName);
				TextView tvToTableName = (TextView) shooseMenuLayout
						.findViewById(R.id.shooseMenuDialogTvToTableName);

				tvFromTableName.setText(mFromTableName);
				tvToTableName.setText(mToTableName);

				mMenuFromListView = (ListView) shooseMenuLayout
						.findViewById(R.id.shooseMenuDialogMenuFromListView);
				mMenuToListView = (ListView) shooseMenuLayout
						.findViewById(R.id.shooseMenuDialogMenuToListView);

				// check moved menu
				mMenuUtil = new MenuUtil(mContext);
				if (mMenuUtil.listMovedMenu().size() == 0) {
					new CurrentOrderTask(mContext, mGlobalVar).execute(GlobalVar.FULL_URL);
				} else {
					refreshSourceList();

					refreshDestList();
				}

				mMenuFromListView
						.setOnItemClickListener(new AdapterView.OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View v, int position, long id) {
								OrderSendData.OrderDetail detail = (OrderSendData.OrderDetail) parent
										.getItemAtPosition(position);
								
								mMenuUtil.moveMenu(detail.getiOrderID(),
										detail.getiOrderStatusID(),
										detail.getiProductID(),
										detail.getSzProductName(),
										detail.getfItemQty(), 
										detail.getiSeatID());

								int orderId = detail.getiOrderID();
								detail = mMenuUtil.listMenuToMove(orderId);
								if(detail.getiOrderID() != 0){
									if(detail.getiOrderStatusID() == 1 || detail.getiOrderStatusID() == 2){
										mMenuData.set(position, detail);
										mAdapter1.notifyDataSetChanged();
									}
								}else{
									refreshSourceList();
								}
								
								refreshDestList();
							}

						});
				

				mMenuToListView
						.setOnItemClickListener(new AdapterView.OnItemClickListener() {

							@Override
							public void onItemClick(
									AdapterView<?> parent,
									View v, int position,
									long id) {
								OrderSendData.OrderDetail detail = (OrderSendData.OrderDetail) parent
										.getItemAtPosition(position);
								mMenuUtil.reMoveMenu(
										detail.getiOrderID(),
										detail.getiOrderStatusID(),
										detail.getiProductID(),
										detail.getSzProductName(),
										detail.getfItemQty(),
										detail.getiSeatID());

								int orderId = detail.getiOrderID();
								detail = mMenuUtil.listMovedMenu(orderId);
								if(detail.getiOrderID() > 0){
									mMenuData2.set(position, detail);
									mAdapter2.notifyDataSetChanged();
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
				
				final Dialog dialog = new Dialog(mContext, R.style.CustomDialog);
				dialog.setContentView(shooseMenuLayout);
				dialog.getWindow().setGravity(Gravity.TOP);
				dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
				dialog.show();
				
				btnOk.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						mMovedMenuListView.setAdapter(mAdapter2);
						if(mAdapter2.getCount() > 0)
							mBtnShooseMenu.setText(R.string.move_menu_btn_edit_menu);
							
						lockSelectedTable();
						
						dialog.dismiss();
					}
				});
				btnCancel.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						mMenuUtil = new MenuUtil(mContext);
						mMenuUtil.createMoveMenuTemp();
						
						clearMoveList();
						dialog.dismiss();
					}
				});

			}
		});

		// load reason
		final List<ReasonGroups.ReasonDetail> reasonDetailLst = IOrderUtility
				.loadReasonFromWs(mContext, mGlobalVar, 7);
		
		final ReasonAdapter reasonAdapter = 
				new ReasonAdapter(MoveMenuActivity.this, reasonDetailLst);

		ListView reasonListView = (ListView) findViewById(R.id.moveMenuListViewReason);
		reasonListView.setAdapter(reasonAdapter);

		final Reason reason = new Reason(mContext);
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
		mLvSourceTbName.setEnabled(true);
		mLvDestTbName.setEnabled(true);
		mLvSourceTbName.clearChoices();
		mLvDestTbName.clearChoices();
		
		clear();
		
		mBtnClear.setVisibility(View.GONE);
	}
	
	private void lockSelectedTable(){
		mLvSourceTbName.setEnabled(false);
		mLvDestTbName.setEnabled(false);
		
		mBtnClear.setVisibility(View.VISIBLE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_move_menu, menu);
		View v = menu.findItem(R.id.item_confirm).getActionView();

		mBtnConfirm = (Button) v.findViewById(R.id.buttonConfirmOk);
		mBtnClose = (Button) v.findViewById(R.id.buttonConfirmCancel);
		mBtnClear = (Button) v.findViewById(R.id.buttonClear);
		
		mBtnConfirm.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Reason reason = new Reason(MoveMenuActivity.this);
				List<ReasonDetail> reasonLst = reason.listSelectedReasonDetail(7); 
				if(mFromTableId == 0){
					IOrderUtility.alertDialog(mContext, R.string.select_source_table, 0);
				}else if(mToTableId == 0){
					IOrderUtility.alertDialog(mContext, R.string.select_destination_table, 0);
				}else if(mMenuData2 == null){
					IOrderUtility.alertDialog(mContext, R.string.select_menu_to_move, 0);
				}else if(mMovedMenuListView.getCount()==0){
					IOrderUtility.alertDialog(mContext, R.string.select_menu_to_move, 0);
				}else if((reasonLst != null && reasonLst.size() == 0) && mTxtMoveMenuReason.getText().toString().isEmpty()){
					IOrderUtility.alertDialog(mContext, R.string.select_reason, 0);
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
							new MoveMenuTask(mContext, mGlobalVar).execute(GlobalVar.FULL_URL);		
						}
						
					});
					
					cfDialog.show();
					
				}

				//reset btnShoose text
				mBtnShooseMenu.setText(R.string.move_menu_btn_choose_menu);
			}
		});
		mBtnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				MoveMenuActivity.this.finish();
			}
		});	
		
		mBtnClear.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				clearLockSelectedTable();
			}
			
		});
		
		return true;
	}

	private void clearMoveList() {
		mMenuData2 = new ArrayList<OrderSendData.OrderDetail>();

		ShooseMenuAdapter adapter = new ShooseMenuAdapter(mContext, mGlobalVar,
				mMenuData2);
		mMovedMenuListView.setAdapter(adapter);
	}

	private void refreshDestList() {
		mMenuData2 = mMenuUtil.listMovedMenu();

		mAdapter2 = new ShooseMenuAdapter(mContext, mGlobalVar,
				mMenuData2);
		mMenuToListView.setAdapter(mAdapter2);
	}

	private void refreshSourceList() {
		mMenuData = mMenuUtil.listMenuToMove();

		// set adapter
		mAdapter1 = new ShooseMenuAdapter(mContext, mGlobalVar,
				mMenuData);
		mMenuFromListView.setAdapter(mAdapter1);
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
			property.setValue(mFromTableId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iToTableID");
			property.setValue(mToTableId);
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
				property.setValue(mTxtMoveMenuReason.getText().toString());
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
		mMenuUtil = new MenuUtil(mContext);
		mMenuUtil.createMoveMenuTemp();
		
		//clear menu to move
		clearMoveList();
		
		TextView tvTbFrom = (TextView) findViewById(R.id.tvTbFrom);
		TextView tvTbTo = (TextView) findViewById(R.id.tvTbTo);
		tvTbFrom.setText("");
		tvTbTo.setText("");
		
		// clear to tableid
		mFromTableId = 0;
		mToTableId = 0;
		mBtnShooseMenu.setEnabled(false);
		
		//reset btnShoose text
		mBtnShooseMenu.setText(R.string.move_menu_btn_choose_menu);
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
				mSpMoveTbZone.setAdapter(tbZoneAdapter);
				mSpMoveTbZoneTo.setAdapter(tbZoneAdapter);

				mSpMoveTbZone
						.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent, View v,
									int pos, long id) {

								final TableInfo.TableZone tbZone = (TableZone) parent.getItemAtPosition(pos);
								final List<TableInfo.TableName> tbNameLst = IOrderUtility.filterTableNameHaveOrder(tbInfo, tbZone);

								mLvSourceTbName = (ListView) findViewById(R.id.listViewSorceTableName);
								mLvSourceTbName.setAdapter(IOrderUtility.createTableNameAdapter(context, globalVar, tbNameLst));
								mLvSourceTbName
										.setOnItemClickListener(new OnItemClickListener() {
											@Override
											public void onItemClick(
													AdapterView<?> parent, View v,
													int pos, long id) {
												clear();
												
												TableName tbName = (TableName) parent
														.getItemAtPosition(pos);
												mFromTableId = tbName
														.getTableID();
												mFromTableName = tbName
														.getTableName();

												TextView tvTbFrom = (TextView) findViewById(R.id.tvTbFrom);
												tvTbFrom.setText(mFromTableName);
												
												//reset btnShoose text
												mBtnShooseMenu.setText(R.string.move_menu_btn_choose_menu);
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

				mSpMoveTbZoneTo
						.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent, View v,
									int pos, long id) {

								TableInfo.TableZone tbZone = (TableZone) parent.getItemAtPosition(pos);
								final List<TableInfo.TableName> tbNameLst = IOrderUtility.filterTableName(tbInfo, tbZone, mFromTableId);

								mLvDestTbName = (ListView) findViewById(R.id.listViewDestTableName);
								mLvDestTbName.setAdapter(IOrderUtility.createTableNameAdapter(context, globalVar, tbNameLst));

								mLvDestTbName
										.setOnItemClickListener(new OnItemClickListener() {
											@Override
											public void onItemClick(
													AdapterView<?> parent, View v,
													int pos, long id) {
												TableName tbName = (TableName) parent
														.getItemAtPosition(pos);
												mToTableId = tbName
														.getTableID();
												mToTableName = tbName
														.getTableName();

												TextView tvTbTo = (TextView) findViewById(R.id.tvTbTo);
												tvTbTo.setText(tbName.getTableName());
												mBtnShooseMenu.setEnabled(true);
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
			property.setValue(mFromTableId);
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
						mMenuUtil = new MenuUtil(context);
						mMenuUtil.createMoveMenuTemp();
						mMenuUtil.prepareMenuForMove(orderData);

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
