package syn.pos.mobile.iordertab;

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
import syn.pos.data.model.WebServiceResult;
import syn.pos.data.model.ReasonGroups.ReasonDetail;
import syn.pos.data.model.TableInfo.TableName;
import syn.pos.data.model.TableInfo.TableZone;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
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
	OrderSendData detail;
	SelectOrderAdapter menuAdapter;
	private List<OrderSendData.OrderDetail> orderDetailLst;
	
	private int TABLE_ID;
	private String CANCEL_MENU_REASON;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context = this;
		globalVar = new GlobalVar(context);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cancel_menu);
		
		spinnerTableZone = (Spinner) findViewById(R.id.spinner1);
		listViewTableName = (ListView) findViewById(R.id.listView1);
		listViewMenu = (ListView) findViewById(R.id.listView2);
		reasonListView = (ListView) findViewById(R.id.listView3);
		txtCancelMenuReason = (EditText) findViewById(R.id.editText1);
		
		new LoadTableTask(context, globalVar).execute(GlobalVar.FULL_URL);

		// load reason
		final List<ReasonGroups.ReasonDetail> reasonDetailLst = IOrderUtility
				.loadReasonFromWs(context, globalVar, 2);
		
		final ReasonAdapter reasonAdapter = new ReasonAdapter(CancelMenuActivity.this, reasonDetailLst);
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
		btnConfirm = (Button) v.findViewById(R.id.btnConfirm);
		btnCancel = (Button) v.findViewById(R.id.btnCancel);
		
		btnConfirm.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Reason reason = new Reason(CancelMenuActivity.this);
				List<ReasonDetail> allReasonLst = reason.listAllReasonDetail(2);
				List<ReasonDetail> reasonLst = reason.listSelectedReasonDetail(2); 
				if(TABLE_ID != 0){
					MenuUtil menuUtil = new MenuUtil(context);
					orderDetailLst = menuUtil
							.listMenuToCancel();
					
					 if(orderDetailLst == null){
						new AlertDialog.Builder(CancelMenuActivity.this)
						.setMessage(R.string.select_menu)
						.show();
					 }else if(orderDetailLst != null && orderDetailLst.size() == 0){
						 new AlertDialog.Builder(CancelMenuActivity.this)
							.setMessage(R.string.select_menu)
							.show();
					 }else if((allReasonLst != null && allReasonLst.size() > 0) && 
							 (reasonLst != null && reasonLst.size() == 0)){
						 new AlertDialog.Builder(CancelMenuActivity.this)
							.setMessage(R.string.select_reason)
							.show();
					 }else{
						CANCEL_MENU_REASON = txtCancelMenuReason.getText().toString();
						
						new AlertDialog.Builder(CancelMenuActivity.this)
						.setMessage(R.string.confirm_cancel_menu)
						.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
							}
						})
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								new CancelMenuTask(context, globalVar).execute(GlobalVar.FULL_URL);
							}
						})
						.show();
					 }
				}else{
					new AlertDialog.Builder(CancelMenuActivity.this)
					.setMessage(R.string.select_source_table)
					.show();
				}
			}
		});	
		btnCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
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
			property.setValue(TABLE_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("szReasonDelete");
			property.setValue(CANCEL_MENU_REASON);
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
			progress.setMessage(context.getString(R.string.loading_progress));
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
					new AlertDialog.Builder(context)
					.setMessage(R.string.success)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).show();
				}else{
					new AlertDialog.Builder(context)
					.setMessage(wsResult.getSzResultData().equals("") ? result : wsResult.getSzResultData())
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).show();
				}
			} catch (Exception e) {
				new AlertDialog.Builder(context)
				.setMessage(result)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).show();
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
				spinnerTableZone.setAdapter(IOrderUtility.createTableZoneAdapter(context, tbInfo));
				spinnerTableZone.setOnItemSelectedListener(new OnItemSelectedListener(){

					@Override
					public void onItemSelected(AdapterView<?> parent, View v,
							int position, long id) {
						TableInfo.TableZone tbZone = (TableZone) parent.getItemAtPosition(position);
						final List<TableInfo.TableName> tbNameLst = IOrderUtility.filterTableNameHaveOrder(tbInfo, tbZone);

						listViewTableName.setAdapter(IOrderUtility.createTableNameAdapter(context, globalVar, tbNameLst));
						listViewTableName.setOnItemClickListener(new OnItemClickListener(){

							@Override
							public void onItemClick(AdapterView<?> parent, View v,
									int position, long id) {
								TableName tbName = (TableName) parent.getItemAtPosition(position);
										
								TABLE_ID = tbName.getTableID();

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
			} catch (Exception e) {
				new AlertDialog.Builder(context)
				.setMessage(result)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).show();
			}
		}

		@Override
		protected void onPreExecute() {
			progress.setMessage(context.getString(R.string.loading_progress));
			progress.show();
		}
		
	}
	
	private class CurrentOrderTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_LoadCurrentOrderFromTableID";
		
		public CurrentOrderTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);

			PropertyInfo property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(TABLE_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(progress.isShowing())
				progress.dismiss();
			
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
							
							CheckedTextView chkMenuName = (CheckedTextView) v.findViewById(R.id.checkedTextView1);
							// check if item is selected
							menuUtil = new MenuUtil(context);
							if(menuUtil.checkSelectedMenu(data.getiOrderID(), data.getiProductID()) != 0)
								chkMenuName.setChecked(true);
							else
								chkMenuName.setChecked(false);
						}
					}
				});
				
			} catch (Exception e) {
				new AlertDialog.Builder(context)
				.setMessage(result)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
			}
			
		}
	}
}
