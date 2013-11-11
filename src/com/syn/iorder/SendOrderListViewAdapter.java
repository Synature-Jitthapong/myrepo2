package com.syn.iorder;

import syn.pos.mobile.iordertab.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SendOrderListViewAdapter extends BaseAdapter {

	private syn.pos.data.model.OrderSendData sendOrderData;
	private LayoutInflater inflater = null;
	private Context context;
	private GlobalVar globalVar;
	
	public SendOrderListViewAdapter(Context c, syn.pos.data.model.OrderSendData sendOrderData){
		this.sendOrderData = sendOrderData;
		context = c;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		globalVar = new GlobalVar(c);
	}
	
	@Override
	public int getCount() {
		return sendOrderData.xListOrderDetail != null ? sendOrderData.xListOrderDetail.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		syn.pos.data.model.OrderSendData.OrderDetail orderDetail = 
				sendOrderData.xListOrderDetail.get(position);
		
		if(v == null){
			v = inflater.inflate(R.layout.send_order_listview_template, null);
		}

		TextView tvCurrOrderNo = (TextView) v.findViewById(R.id.tvCurrOrderNo);
		TextView tvMenuName = (TextView) v.findViewById(R.id.tvMenuName);
		TextView tvMenuQty = (TextView) v.findViewById(R.id.tvMenuQty);
		TextView tvMenuPrice = (TextView) v.findViewById(R.id.tvMenuPrice);
		TextView tvMenuTotalPrice = (TextView) v.findViewById(R.id.tvMenuTotalPrice);
		
		tvCurrOrderNo.setText(position + 1 + ".");
		tvMenuName.setText(orderDetail.getSzProductName());
		tvMenuQty.setText(globalVar.qtyFormat.format(orderDetail.getfItemQty()));
		tvMenuPrice.setText(globalVar.decimalFormat.format(orderDetail.getfItemPrice()));
		tvMenuTotalPrice.setText(globalVar.decimalFormat.format(orderDetail.getfTotalPrice()));
		
		return v;
	}

}
