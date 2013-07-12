package syn.pos.mobile.iordertab;

/*
 * order status = 1,2 สามารถแก้ไขได้
 */
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;

import syn.pos.data.dao.MenuUtil;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.OrderSendData;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.WebServiceResult;
import syn.pos.data.model.TableInfo.TableName;
import syn.pos.data.model.TableInfo.TableZone;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

public class ReprintMenuActivity extends Activity {
	private Context context;
	private GlobalVar globalVar;
	
	private Spinner spinnerTableZone;
	private ListView listViewTableName;
	private ListView listViewMenu;
	private Button btnConfirm;
	private Button btnCancel;
	private ProgressBar progressBar;
	OrderSendData detail;
	SelectOrderAdapter menuAdapter;
	private List<OrderSendData.OrderDetail> orderDetailLst;
	
	private int TABLE_ID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context = this;
		globalVar = new GlobalVar(context);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reprint_menu);
		
		spinnerTableZone = (Spinner) findViewById(R.id.spinnerCancelMenuTableZone);
		listViewTableName = (ListView) findViewById(R.id.listViewCancelMenuTableName);
		listViewMenu = (ListView) findViewById(R.id.listViewCancelMenu);
		progressBar = (ProgressBar) findViewById(R.id.progressBarShooseMenu);
		
		new LoadTableTask(context, globalVar).execute(GlobalVar.FULL_URL);
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
				if(TABLE_ID != 0){
					MenuUtil menuUtil = new MenuUtil(context);
					orderDetailLst = menuUtil
							.listMenuToCancel();
					
					 if(orderDetailLst == null){
						IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, R.string.select_menu_to_cancel, 0);
					 }else if(orderDetailLst != null && orderDetailLst.size() == 0){
						 IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, R.string.select_menu_to_cancel, 0);
					 }else{
						new ReprintMenuTask(context, globalVar).execute(GlobalVar.FULL_URL);
					 }
				}else{
					IOrderUtility.alertDialog(context,R.string.global_dialog_title_error, R.string.select_source_table, 0);
				}
			}
		});	
		btnCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				ReprintMenuActivity.this.finish();
			}
		});
		return true;
	}

	private class ReprintMenuTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_ReprintQuickenOrder";
		public ReprintMenuTask(Context c, GlobalVar gb) {
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

			List<Integer> orderIdLst = new ArrayList<Integer>();
			if (orderDetailLst.size() > 0) {
				for (OrderSendData.OrderDetail detail : orderDetailLst) {
					orderIdLst.add(detail.getiOrderID());
				}
			}
			Gson gson = new Gson();
			String szListOrderId = gson.toJson(orderIdLst);

			property = new PropertyInfo();
			property.setName("szAryOrderID");
			property.setValue(szListOrderId);
			property.setType(String.class);
			soapRequest.addProperty(property);
		}
		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.wait_progress);
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
					final CustomDialog customDialog = new CustomDialog(context, R.style.CustomDialog);
					customDialog.title.setVisibility(View.VISIBLE);
					customDialog.title.setText(R.string.reprint_title);
					customDialog.message.setText(R.string.reprint_success);
					customDialog.btnCancel.setVisibility(View.GONE);
					customDialog.btnOk.setText(R.string.global_close_dialog_btn);
					customDialog.btnOk.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							ReprintMenuActivity.this.finish();
							customDialog.dismiss();
						}
					});
					customDialog.show();
				}else{
					final CustomDialog customDialog = new CustomDialog(context, R.style.CustomDialog);
					customDialog.title.setVisibility(View.VISIBLE);
					customDialog.title.setText(R.string.global_dialog_title_error);
					customDialog.message.setText(wsResult.getSzResultData().equals("") ? result : wsResult.getSzResultData());
					customDialog.btnCancel.setVisibility(View.GONE);
					customDialog.btnOk.setText(R.string.global_close_dialog_btn);
					customDialog.btnOk.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							ReprintMenuActivity.this.finish();
							customDialog.dismiss();
						}
					});
					customDialog.show();
				}
			} catch (Exception e) {
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
				IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, result, 0);
			}
			
		}
	}
}
