package com.syn.iorder;

import java.util.List;
import syn.pos.data.model.TableInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.BaseAdapter;

public class SelectTableListAdapter extends BaseAdapter {
	private List<TableInfo> mTableInfoLst;
	private Context mContext;
	private LayoutInflater mInflater;
	private GlobalVar mGlobalVar;
	private boolean mIsShowCapacity = true;
	private boolean mIsShowBtnInfo = true;
	
	public SelectTableListAdapter(Context context, GlobalVar globalVar,
			List<TableInfo> tbInfoLst) {
		mGlobalVar = globalVar;
		mTableInfoLst = tbInfoLst;
		mContext = context;
		mInflater = (LayoutInflater) 
				mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public SelectTableListAdapter(Context context, GlobalVar globalVar,
			List<TableInfo> tbInfoLst, boolean iSShowCapacity, boolean isShowBtnInfo) {
		mGlobalVar = globalVar;
		mTableInfoLst = tbInfoLst;
		mContext = context;
		mInflater = (LayoutInflater) 
				mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mIsShowCapacity = iSShowCapacity;
		mIsShowBtnInfo = isShowBtnInfo;
	}

	@Override
	public int getCount() {
		return mTableInfoLst.size();
	}

	@Override
	public TableInfo getItem(int position) {
		return mTableInfoLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public static class SelectTableViewHolder{
		int lastPosition;
		int customerQty;
		String tableName;
		ImageView imgStatus;
		Button btnTbInfo;
		TextView tvStatus;
		TextView tvTableName;
		TextView tvTableCapacity;
		TextView tvTbTime;
		Button btnCloseTable;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final TableInfo tbInfo = mTableInfoLst.get(position);
		final SelectTableViewHolder holder;
		
		if (convertView == null){
			convertView = mInflater
					.inflate(R.layout.select_table_template, null);
	
			holder = new SelectTableViewHolder();
			
			holder.imgStatus = (ImageView) convertView.findViewById(R.id.imageViewStatus);
			holder.tvTableName = (TextView) convertView.findViewById(R.id.selecttable_tvname);
			holder.tvTableCapacity = (TextView) convertView.findViewById(R.id.selecttable_tvcapacity);
			holder.tvTbTime = (TextView) convertView.findViewById(R.id.tvTbTime);
			holder.btnTbInfo = (Button) convertView.findViewById(R.id.button1);
			holder.btnCloseTable = (Button) convertView.findViewById(R.id.button2);
			convertView.setTag(holder);
			
		}
		else{
			holder = (SelectTableViewHolder)convertView.getTag();
		}
		
		holder.lastPosition = position;
		holder.customerQty = tbInfo.getiNoOfCustomer();
		holder.tableName = IOrderUtility.formatCombindTableName(tbInfo.isbIsCombineTable(), 
				tbInfo.getSzCombineTableName(), tbInfo.getSzTableName());
		
		if(mIsShowCapacity){
			holder.tvTableCapacity.setVisibility(View.VISIBLE);
		}else{
			holder.tvTableCapacity.setVisibility(View.INVISIBLE);
		}
		
		holder.tvTableCapacity.setText(String.valueOf(tbInfo.getiTableCapacity()));
		if (tbInfo.getTableStatus() != 0){
			holder.tvTableName.setTextColor(Color.RED);
			holder.imgStatus.setImageResource(R.drawable.hasorder);
			
			holder.btnTbInfo.setVisibility(View.VISIBLE);
			
			// already checkbill
			if(tbInfo.getTableStatus() == 3){
				// show dolla
				holder.tvTableName.setTextColor(Color.GREEN);
				holder.imgStatus.setImageResource(R.drawable.ic_action_dollar);
				holder.btnTbInfo.setVisibility(View.GONE);
				holder.btnCloseTable.setVisibility(View.VISIBLE);
				holder.btnCloseTable.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setTitle(R.string.close_table);
						builder.setMessage(R.string.close_table_confirm);
						builder.setNegativeButton(R.string.global_btn_cancel, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});
						builder.setPositiveButton(R.string.global_btn_ok, null);
						final AlertDialog d = builder.create();
						d.show();
						d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {

								final ProgressDialog progress = new ProgressDialog(mContext);
								progress.setMessage(mContext.getString(R.string.close_table_progress));
								new TableUtils.CloseTable(mContext, mGlobalVar, tbInfo.getiTableID(), 
										new ProgressListener(){

											@Override
											public void onPre() {
												progress.show();
											}

											@Override
											public void onPost() {
												if(progress.isShowing())
													progress.dismiss();
												d.dismiss();
												tbInfo.setTableStatus(0);
												notifyDataSetChanged();
											}

											@Override
											public void onError(String msg) {
												if(progress.isShowing())
													progress.dismiss();
												
												new AlertDialog.Builder(mContext)
												.setMessage(msg)
												.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
													
													@Override
													public void onClick(DialogInterface dialog, int which) {
													}
												}).show();
											}
									
								}).execute(GlobalVar.FULL_URL);
							}
							
						});
					}
					
				});
			}else{
				holder.btnTbInfo.setVisibility(View.VISIBLE);
				holder.btnCloseTable.setVisibility(View.GONE);
			}
			
			if(tbInfo.getTableStatus() == 0)
				holder.btnTbInfo.setVisibility(View.GONE);
			
			holder.btnTbInfo.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					final Dialog detailDialog = new Dialog(mContext, R.style.CustomDialog);
					LayoutInflater inflater = LayoutInflater.from(mContext);
					View orderView = inflater.inflate(R.layout.order_list_layout, null);
					ListView lvOrder = (ListView) orderView.findViewById(R.id.listViewOrder);
					TextView tvTableName = (TextView) orderView.findViewById(R.id.textViewOrderListTitle);
					TextView tvSumText = (TextView) orderView.findViewById(R.id.textViewSumText);
					TextView tvSumPrice = (TextView) orderView.findViewById(R.id.textViewSumPrice);
					ImageButton btnClose = (ImageButton) orderView.findViewById(R.id.imageButtonCloseOrderDialog);
					
					ProgressBar progress = (ProgressBar) orderView.findViewById(R.id.progressBarOrderOfTable);
					//tvTableName.setText()
					tvTableName.setText(mContext.getString(R.string.text_table) + ":" + holder.tableName);
					
					//new CurrentOrderFromTableTask(context, globalVar, tbName.getTableID(), lvOrder).execute(globalVar.FULL_URL);
					new LoadBillDetailTask(mContext, mGlobalVar, tbInfo.getiTableID(), 
							lvOrder, tvSumText, tvSumPrice, progress).execute(GlobalVar.FULL_URL);
					detailDialog.setContentView(orderView);
					detailDialog.getWindow().setGravity(Gravity.TOP);
					detailDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
							WindowManager.LayoutParams.WRAP_CONTENT);

					btnClose.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							detailDialog.dismiss();
						}
						
					});
					
					detailDialog.show();
				}
				
			});
		}else{
//			holder.tvStatus.setText(R.string.selecttable_tvtable_empty);
//			holder.tvStatus.setTextColor(Color.GREEN);
			holder.tvTableName.setTextColor(Color.GREEN);
			holder.imgStatus.setImageResource(R.drawable.user_green);
			if(tbInfo.getiTableID() == 0)
				holder.imgStatus.setVisibility(View.GONE);
			else
				holder.imgStatus.setVisibility(View.VISIBLE);
			
			holder.btnTbInfo.setVisibility(View.INVISIBLE);
			holder.btnCloseTable.setVisibility(View.GONE);
		}
		holder.tvTableName.setText(holder.tableName);
		holder.tvTableName.setSelected(true);
		

		if(mIsShowBtnInfo)
			holder.btnTbInfo.setVisibility(View.VISIBLE);
		else
			holder.btnTbInfo.setVisibility(View.GONE);
		
		// print long bill
		if(tbInfo.getiNumberPrintBill() > 0){
			holder.tvTbTime.setVisibility(View.VISIBLE);
			holder.tvTbTime.setText(IOrderUtility.formatJSONDate(mGlobalVar, tbInfo.getdEndTime()));
		}else{
			holder.tvTbTime.setVisibility(View.GONE);
		}
		
		return convertView;
	}

}
