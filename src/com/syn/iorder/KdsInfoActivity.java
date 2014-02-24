package com.syn.iorder;

import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.dao.MenuItem;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.KdsOrderInfo;
import syn.pos.data.model.OrderSendData;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableName;
import syn.pos.data.model.WebServiceResult;
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
	private Context mContext;
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
	private int mSelTableId;
	private int mSortBy = 0;
	
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
		
		mContext = this;
		globalVar = new GlobalVar(mContext);

		btnSortByTime.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				v.setSelected(true);
				btnSortByItem.setSelected(false);
				btnSortByItemName.setSelected(false);
				mSortBy = 0;
				
				if(mSelTableId != 0)
					new KdsOrderInfoTask(mContext, globalVar,
							mSelTableId, mSortBy).execute(GlobalVar.FULL_URL);
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
				mSortBy = 1;
				
				if(mSelTableId != 0)
					new KdsOrderInfoTask(mContext, globalVar,
							mSelTableId, mSortBy).execute(GlobalVar.FULL_URL);
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
				mSortBy = 2;
				
				if(mSelTableId != 0)
					new KdsOrderInfoTask(mContext, globalVar,
							mSelTableId, mSortBy).execute(GlobalVar.FULL_URL);
				else
					new CurrentOrderFromTableTask(KdsInfoActivity.this, globalVar).execute(GlobalVar.FULL_URL);
			}
			
		});
		
		new LoadAllTableV1(mContext, globalVar, new LoadAllTableV1.LoadTableProgress() {
			
			@Override
			public void onPre() {
				tableProgress.setVisibility(View.VISIBLE);
				tableListView.setVisibility(View.INVISIBLE);
			}
			
			@Override
			public void onPost() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(String msg) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPost(final TableName tbName) {
				new LoadAllTableV2(mContext, globalVar, new LoadAllTableV2.LoadTableProgress() {
					
					@Override
					public void onPre() {
					}
					
					@Override
					public void onPost() {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onError(String msg) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onPost(final List<TableInfo> tbInfoLst) {
						tableProgress.setVisibility(View.GONE);
						tableListView.setVisibility(View.VISIBLE);
						
						TableZoneSpinnerAdapter tbZoneAdapter =IOrderUtility.createTableZoneAdapter(mContext, tbName); 
						spinnerTableZone.setAdapter(tbZoneAdapter);

						spinnerTableZone
								.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

									@Override
									public void onItemSelected(AdapterView<?> parent, View v,
											int pos, long id) {
										TableName.TableZone tbZone = (TableName.TableZone) parent
												.getItemAtPosition(pos);
										final List<TableInfo> newTbInfoLst
											= IOrderUtility.filterTableNameHaveOrder(tbInfoLst, tbZone);
										TableInfo tbInfo = new TableInfo();
										tbInfo.setiTableID(0);
										tbInfo.setSzTableName("All table");
										tbInfo.setTableStatus(1);
										newTbInfoLst.add(0, tbInfo);
										
										tableListView.setAdapter(IOrderUtility.createTableNameAdapter(
												mContext, globalVar, newTbInfoLst, false, true));
										tableListView
												.setOnItemClickListener(new OnItemClickListener() {
													@Override
													public void onItemClick(
															AdapterView<?> parent, View v,
															int pos, long id) {

														TableInfo tbInfo = (TableInfo) parent
																.getItemAtPosition(pos);
														
														// set selected table name
														String tbName = tbInfo.isbIsCombineTable() ? tbInfo.getSzCombineTableName() :
															tbInfo.getSzTableName();
														tvKdsSelTable.setText(tbName);
														
														mSelTableId = tbInfo.getiTableID();
														if(tbInfo.getTableStatus() == 0){
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
															
															new KdsOrderInfoTask(mContext, globalVar,
																	mSelTableId, mSortBy).execute(GlobalVar.FULL_URL);
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
			}
		}).execute(GlobalVar.FULL_URL);
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
						IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, result, 0);
					}
				}else{
					IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, 
							wsResult.getSzResultData() != "" ? wsResult.getSzResultData() : result, 0);
				}
			} catch (Exception e) {
				e.printStackTrace();
				IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, result, 0);
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
			inflater = LayoutInflater.from(mContext);
			this.orderList = orderList;
			mi = new MenuItem(mContext);
			imgLoader = new ImageLoader(mContext, ImageLoader.IMAGE_SIZE.SMALL);
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
			inflater = LayoutInflater.from(mContext);
			mi = new MenuItem(mContext);
			this.kdsList = kdsList;
			imgLoader = new ImageLoader(mContext, ImageLoader.IMAGE_SIZE.SMALL);
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
