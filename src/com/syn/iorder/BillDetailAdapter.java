package com.syn.iorder;

import syn.pos.data.model.SummaryTransaction;
import syn.pos.mobile.iordertab.R;
import syn.pos.mobile.util.Log;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

// bill detail adapter
public class BillDetailAdapter extends BaseAdapter{
	private Context context;
	private GlobalVar globalVar;
	private SummaryTransaction summaryTrans;
	private LayoutInflater inflater;
	
	public BillDetailAdapter(Context context, GlobalVar globalVar, SummaryTransaction summaryTrans){
		this.globalVar = globalVar;
		this.summaryTrans = summaryTrans;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return summaryTrans.OrderList != null ? summaryTrans.OrderList.size() : 0;
	}

	@Override
	public SummaryTransaction.Order getItem(int position) {
		return summaryTrans.OrderList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		SummaryTransaction.Order order = summaryTrans.OrderList.get(position);

		if(convertView == null){
			convertView = inflater.inflate(R.layout.bill_detail_layout, null);
			holder = new ViewHolder();
			holder.imgWaitPrint = (ImageView) convertView.findViewById(R.id.imageViewWaitPrint);
			holder.tvOrderName = (TextView) convertView.findViewById(R.id.textViewOrderName);
			holder.tvOrderQty = (TextView) convertView.findViewById(R.id.textViewOrderQty);
			holder.tvOrderTotalPrice = (TextView) convertView.findViewById(R.id.textViewOrderTotalPrice);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		try {

			int productSetType = order.iProductSetType;
			LinearLayout.LayoutParams param = 
					new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			if(productSetType == 14 || productSetType == 15){
				param.setMargins(20, 0, 0, 0);
			}else{
				param.setMargins(0, 0, 0, 0);
			}
			
			holder.tvOrderQty.setLayoutParams(param);
			holder.tvOrderName.setText(order.szProductName);
			holder.tvOrderQty.setText(globalVar.qtyFormat.format(order.fAmount) + "x");
			holder.tvOrderTotalPrice.setText(globalVar.decimalFormat.format(order.fTotalPrice));
			
			// printed
			if(order.iOrderStatus == 2){
				holder.imgWaitPrint.setVisibility(View.VISIBLE);
				if(productSetType == 14 || productSetType == 15)
					holder.imgWaitPrint.setVisibility(View.GONE);
			}else{
				holder.imgWaitPrint.setVisibility(View.GONE);
			}
			
			if(order.iOrderStatus == 3 || order.iOrderStatus == 4){
				holder.tvOrderQty.setTextColor(Color.RED);
				holder.tvOrderName.setTextColor(Color.RED);
				holder.tvOrderTotalPrice.setTextColor(Color.RED);
				
				holder.tvOrderQty.setPaintFlags(holder.tvOrderQty.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				holder.tvOrderName.setPaintFlags(holder.tvOrderName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				holder.tvOrderTotalPrice.setPaintFlags(holder.tvOrderTotalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}else{
				holder.tvOrderQty.setTextColor(Color.BLACK);
				holder.tvOrderName.setTextColor(Color.BLACK);
				holder.tvOrderTotalPrice.setTextColor(Color.BLACK);

				holder.tvOrderQty.setPaintFlags(holder.tvOrderQty.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
				holder.tvOrderName.setPaintFlags(holder.tvOrderName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
				holder.tvOrderTotalPrice.setPaintFlags(holder.tvOrderTotalPrice.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			}
		} catch (Exception e) {
			Log.appendLog(context, "Error at " + CheckBillActivity.class + " Message " + e.getMessage());
		}
		return convertView;
	}
	
	private class ViewHolder{
		public ImageView imgWaitPrint;
		public TextView tvOrderName;
		public TextView tvOrderQty;
		public TextView tvOrderTotalPrice;
	}
	
}
