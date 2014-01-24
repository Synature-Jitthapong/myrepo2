package com.syn.iorder;

import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.dao.MenuItem;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.KdsOrderInfo;
import syn.pos.data.model.OrderSendData;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.WebServiceResult;
import syn.pos.data.model.TableInfo.TableName;
import syn.pos.data.model.TableInfo.TableZone;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class KdsInfoActivity extends Activity {
	private Context CONTEXT;
	private Spinner spinnerTableZone;
	private TextView tvKdsSelTable;
	private ListView tableListView;
	private ListView kdsListView;
	private ProgressBar kdsProgress;
	private ProgressBar tableProgress;
	private Button btnKdsClose;
	private Button btnSortByTime;
	private Button btnSortByItem;
	private Button btnSortByItemName;
	private GlobalVar globalVar;
	private int SELECTED_TABLEID;
	private int SORT_BY = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kds_info_layout);

		spinnerTableZone = (Spinner) findViewById(R.id.spinnerTableZone);
		tvKdsSelTable = (TextView) findViewById(R.id.textViewKdsSelTable);
		tableListView = (ListView) findViewById(R.id.listViewTable);
		kdsListView = (ListView) findViewById(R.id.listViewKdsInfo);
		tableProgress = (ProgressBar) findViewById(R.id.progressBarTable);
		kdsProgress = (ProgressBar) findViewById(R.id.progressBarKds);
		btnSortByTime = (Button) findViewById(R.id.buttonKdsOrderByTime);
		btnSortByItem = (Button) findViewById(R.id.buttonKdsOrderByFinishTime);
		btnSortByItemName = (Button) findViewById(R.id.buttonKdsOrderByName);
		
		//btnKdsClose = (Button) findViewById(R.id.buttonKdsClose);
		
		CONTEXT = this;
		globalVar = new GlobalVar(CONTEXT);

		btnSortByTime.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				v.setSelected(true);
				btnSortByItem.setSelected(false);
				btnSortByItemName.setSelected(false);
				SORT_BY = 0;
				
				if(SELECTED_TABLEID != 0)
					new KdsOrderInfoTask(CONTEXT, globalVar,
							SELECTED_TABLEID, SORT_BY).execute(GlobalVar.FULL_URL);
				else
					new CurrentOrderFromTableTask(KdsInfoActivity.this, globalVar).execute(GlobalVar.FULL_URL);
			}
			
		});
		btnSortByItem.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				v.setSelected(true);
				btnSortByTime.setSelected(false);
				btnSortByItemName.setSelected(false);
				SORT_BY = 1;
				
				if(SELECTED_TABLEID != 0)
					new KdsOrderInfoTask(CONTEXT, globalVar,
							SELECTED_TABLEID, SORT_BY).execute(GlobalVar.FULL_URL);
				else
					new CurrentOrderFromTableTask(KdsInfoActivity.this, globalVar).execute(GlobalVar.FULL_URL);
			}
			
		});
		btnSortByItemName.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				v.setSelected(true);
				btnSortByTime.setSelected(false);
				btnSortByItem.setSelected(false);
				SORT_BY = 2;
				
				if(SELECTED_TABLEID != 0)
					new KdsOrderInfoTask(CONTEXT, globalVar,
							SELECTED_TABLEID, SORT_BY).execute(GlobalVar.FULL_URL);
				else
					new CurrentOrderFromTableTask(KdsInfoActivity.this, globalVar).execute(GlobalVar.FULL_URL);
			}
			
		});
		
		new LoadTableTask(CONTEXT, globalVar).execute(GlobalVar.FULL_URL);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_kds, menu);
		View v = menu.findItem(R.id.item_close).getActionView();
		btnKdsClose = (Button) v.findViewById(R.id.buttonClose);
		btnKdsClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				finish();
			}
			
		});
		return true;
	}


	private class LoadTableTask extends WebServiceTask{
		private static final String webMethod = "WSmPOS_JSON_LoadAllTableData";
		
		public LoadTableTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);
		}

		@Override
		protected void onPostExecute(String result) {
			tableProgress.setVisibility(View.GONE);
			tableListView.setVisibility(View.VISIBLE);
			
			GsonDeserialze gdz = new GsonDeserialze();

			try {
				final TableInfo tbInfo = gdz.deserializeTableInfoJSON(result);
				TableZoneSpinnerAdapter tbZoneAdapter =IOrderUtility.createTableZoneAdapter(context, tbInfo); 
				spinnerTableZone.setAdapter(tbZoneAdapter);

				spinnerTableZone
						.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent, View v,
									int pos, long id) {
								final TableZone tbZone = (TableZone) parent
										.getItemAtPosition(pos);
								final List<TableName> tbNameLst = IOrderUtility.filterTableNameHaveOrder(tbInfo, tbZone);
								TableName tbName = new TableName();
								tbName.setTableID(0);
								tbName.setTableName("All table");
								tbName.setSTATUS(1);
								tbNameLst.add(0, tbName);
								
								tableListView.setAdapter(IOrderUtility.createTableNameAdapter(context, globalVar, tbNameLst));
								tableListView
										.setOnItemClickListener(new OnItemClickListener() {
											@Override
											public void onItemClick(
													AdapterView<?> parent, View v,
													int pos, long id) {

												TableName tbName = (TableName) parent
														.getItemAtPosition(pos);
												
												// set selected table name
												tvKdsSelTable.setText(tbName.getTableName());
												
												SELECTED_TABLEID = tbName.getTableID();
												if(tbName.getTableID() == 0){
													// selected btn sort by time
													btnSortByTime.setSelected(false);
													btnSortByItem.setSelected(false);
													btnSortByItemName.setSelected(false);
													
													new CurrentOrderFromTableTask(KdsInfoActivity.this, globalVar).execute(GlobalVar.FULL_URL);
												}else{
													// selected btn sort by time
													btnSortByTime.setSelected(true);
													btnSortByItem.setSelected(false);
													btnSortByItemName.setSelected(false);
													
													new KdsOrderInfoTask(CONTEXT, globalVar,
															SELECTED_TABLEID, SORT_BY).execute(GlobalVar.FULL_URL);
												}
											}

										});
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {

							}

						});
			} catch (Exception e) {
				IOrderUtility.alertDialog(CONTEXT, R.string.global_dialog_title_error, result, 0);
			}
		}

		@Override
		protected void onPreExecute() {
			tableProgress.setVisibility(View.VISIBLE);
			tableListView.setVisibility(View.INVISIBLE);
		}
		
	}
	// kds webservice
	private class KdsOrderInfoTask extends WebServiceTask{

		public KdsOrderInfoTask(Context c, GlobalVar gb, int tableId, int sortBy) {
			super(c, gb, "WSiOrder_JSON_KdsGetKitchenOrderInfoOfTable");
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(tableId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iSortBy");
			property.setValue(tableId);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			kdsProgress.setVisibility(View.VISIBLE);
			kdsListView.setVisibility(View.INVISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			Log.w("KdsActivity", result);
			kdsProgress.setVisibility(View.GONE);
			kdsListView.setVisibility(View.VISIBLE);
			
			GsonDeserialze gdz = new GsonDeserialze();
			WebServiceResult wsResult;
			
			try {
				wsResult = gdz.deserializeWsResultJSON(result);
				if(wsResult.getiResultID() == 0){
					try {
						List<KdsOrderInfo> kdsOrderList = gdz.deserializeKdsInfoJSON(wsResult.getSzResultData());
						KdsListAdapter kdsAdapter = new KdsListAdapter(kdsOrderList);
						kdsListView.setAdapter(kdsAdapter);
					} catch (Exception e) {
						e.printStackTrace();
						IOrderUtility.alertDialog(CONTEXT, R.string.global_dialog_title_error, result, 0);
					}
				}else{
					IOrderUtility.alertDialog(CONTEXT, R.string.global_dialog_title_error, 
							wsResult.getSzResultData() != "" ? wsResult.getSzResultData() : result, 0);
				}
			} catch (Exception e) {
				e.printStackTrace();
				IOrderUtility.alertDialog(CONTEXT, R.string.global_dialog_title_error, result, 0);
			}	
		}
	}
	
	public class CurrentOrderFromTableTask extends WebServiceTask {
		private static final String webMethod = "WSiOrder_JSON_LoadCurrentOrderFromTableID";
		public CurrentOrderFromTableTask(Context context, GlobalVar gb) {
			super(context, gb, webMethod);

			PropertyInfo property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(0);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			kdsProgress.setVisibility(View.VISIBLE);
			kdsListView.setVisibility(View.INVISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {

			kdsProgress.setVisibility(View.GONE);
			kdsListView.setVisibility(View.VISIBLE);

			GsonDeserialze gdz = new GsonDeserialze();
			try {
				OrderSendData order = gdz.deserializeOrderSendDataJSON(result);
				KdsListAdapterAllTable kdsAllTableAdapter = 
						new KdsListAdapterAllTable(order.xListOrderDetail);
				kdsListView.setAdapter(kdsAllTableAdapter);
			} catch (Exception e) {
				IOrderUtility.alertDialog(context,
						R.string.global_dialog_title_error, result, 0);
			}
		}
	}
	
	private class KdsListAdapterAllTable extends BaseAdapter{
		protected ImageLoader imgLoader;
		protected LayoutInflater inflater;
		protected MenuItem mi;
		protected List<OrderSendData.OrderDetail> orderList;
		
		public KdsListAdapterAllTable(List<OrderSendData.OrderDetail> orderList){
			inflater = LayoutInflater.from(CONTEXT);
			this.orderList = orderList;
			mi = new MenuItem(CONTEXT);
			imgLoader = new ImageLoader(CONTEXT, ImageLoader.IMAGE_SIZE.SMALL);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return orderList != null ? orderList.size() : 0;
		}

		@Override
		public OrderSendData.OrderDetail getItem(int position) {
			// TODO Auto-generated method stub
			return orderList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			OrderSendData.OrderDetail order = orderList.get(position);
			
			ViewHolder holder;
			
			if(convertView == null){
				convertView = inflater.inflate(R.layout.kds_info_template, null);
				
				holder = new ViewHolder();
				holder.imgKdsMenuImg = (ImageView) convertView.findViewById(R.id.imageViewKdsMenuImg);
				holder.tvKdsMenuName = (TextView) convertView.findViewById(R.id.textViewKdsMenuName);
				holder.tvKdsStaff = (TextView) convertView.findViewById(R.id.textViewKdsStaff);
				holder.tvKdsSubmitTime = (TextView) convertView.findViewById(R.id.textViewKdsSubmitTime);
				holder.tvKdsFinishTime = (TextView) convertView.findViewById(R.id.textViewKdsFinishTime);
				holder.tvKdsWaitTime = (TextView) convertView.findViewById(R.id.textViewKdsWait);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			imgLoader.DisplayImage(GlobalVar.IMG_URL + mi.getImageUrl(order.getiProductID()), holder.imgKdsMenuImg);
			holder.tvKdsMenuName.setText(order.getSzProductName());
			holder.tvKdsStaff.setText(order.getSzOrderStaffBy());
			holder.tvKdsSubmitTime.setText(order.getSzOrderTime());
			holder.tvKdsFinishTime.setText(order.getSzFinishTime());
			if(order.getiOrderWaitMinTime() > -1){
				holder.tvKdsWaitTime.setText(String.valueOf(order.getiOrderWaitMinTime()));
				holder.tvKdsWaitTime.append("min.");
			}else{
				holder.tvKdsWaitTime.setText("");
			}
			return convertView;
		}
		
		private class ViewHolder{
			ImageView imgKdsMenuImg;
			TextView tvKdsMenuName;
			TextView tvKdsStaff;
			TextView tvKdsSubmitTime;
			TextView tvKdsFinishTime;
			TextView tvKdsWaitTime;
		}
		
	}
	
	// kds adapter
	private class KdsListAdapter extends BaseAdapter{
		protected ImageLoader imgLoader;
		protected LayoutInflater inflater;
		protected List<KdsOrderInfo> kdsList;
		protected MenuItem mi;
		
		public KdsListAdapter(List<KdsOrderInfo> kdsList){
			inflater = LayoutInflater.from(CONTEXT);
			mi = new MenuItem(CONTEXT);
			this.kdsList = kdsList;
			imgLoader = new ImageLoader(CONTEXT, ImageLoader.IMAGE_SIZE.SMALL);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return kdsList != null ? kdsList.size() : 0;
		}

		@Override
		public KdsOrderInfo getItem(int position) {
			// TODO Auto-generated method stub
			return kdsList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			KdsOrderInfo kdsOrderInfo = kdsList.get(position);
			
			ViewHolder holder;
			
			if(convertView == null){
				convertView = inflater.inflate(R.layout.kds_info_template, null);
				
				holder = new ViewHolder();
				holder.imgKdsMenuImg = (ImageView) convertView.findViewById(R.id.imageViewKdsMenuImg);
				holder.tvKdsMenuName = (TextView) convertView.findViewById(R.id.textViewKdsMenuName);
				holder.tvKdsStaff = (TextView) convertView.findViewById(R.id.textViewKdsStaff);
				holder.tvKdsSubmitTime = (TextView) convertView.findViewById(R.id.textViewKdsSubmitTime);
				holder.tvKdsFinishTime = (TextView) convertView.findViewById(R.id.textViewKdsFinishTime);
				holder.tvKdsWaitTime = (TextView) convertView.findViewById(R.id.textViewKdsWait);
				holder.tvSlash = (TextView) convertView.findViewById(R.id.textView1);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			imgLoader.DisplayImage(GlobalVar.IMG_URL + mi.getImageUrl(kdsOrderInfo.getiProductID()), holder.imgKdsMenuImg);
			holder.tvKdsMenuName.setText(kdsOrderInfo.getSzProductName());
			holder.tvKdsStaff.setText(kdsOrderInfo.getSzStaffOrder());
			holder.tvKdsSubmitTime.setText(kdsOrderInfo.getSzSubmitOrderDateTime());
			holder.tvKdsFinishTime.setText(kdsOrderInfo.getSzFinishOrderDateTime());
			
			if(GlobalVar.sIsEnableChecker){
				holder.tvKdsFinishTime.setVisibility(View.VISIBLE);
				holder.tvSlash.setVisibility(View.VISIBLE);
			}else{
				holder.tvKdsFinishTime.setVisibility(View.GONE);
				holder.tvSlash.setVisibility(View.GONE);
			}
			
			if(kdsOrderInfo.getiWaitMinTime() > -1){
				holder.tvKdsWaitTime.setText(String.valueOf(kdsOrderInfo.getiWaitMinTime()));
				holder.tvKdsWaitTime.append("min.");
			}else{
				holder.tvKdsWaitTime.setText("");
			}
			return convertView;
		}
		
		private class ViewHolder{
			ImageView imgKdsMenuImg;
			TextView tvKdsMenuName;
			TextView tvKdsStaff;
			TextView tvKdsSubmitTime;
			TextView tvKdsFinishTime;
			TextView tvKdsWaitTime;
			TextView tvSlash;
		}
		
	}
}
