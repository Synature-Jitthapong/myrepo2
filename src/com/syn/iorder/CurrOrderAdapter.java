package com.syn.iorder;

import syn.pos.data.model.OrderSendData;
import syn.pos.mobile.iordertab.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CurrOrderAdapter extends BaseAdapter {
	private OrderSendData currOrder;
	private GlobalVar globalVar;
	private LayoutInflater inflater;
	
	public CurrOrderAdapter(Context context, GlobalVar globalVar, OrderSendData currData){
		this.globalVar = globalVar;
		this.currOrder = currData;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return currOrder.xListOrderDetail == null ? 0 : currOrder.xListOrderDetail.size();
	}

	@Override
	public OrderSendData.OrderDetail getItem(int position) {
		return currOrder.xListOrderDetail.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		OrderSendData.OrderDetail order = currOrder.xListOrderDetail.get(position);
		convertView = inflater.inflate(R.layout.order_list_template, null);
		TextView tvMenuQty = (TextView) convertView.findViewById(R.id.textViewMenuQty);
		TextView tvMenuName = (TextView) convertView.findViewById(R.id.textViewMenuName);
		
		tvMenuQty.setText("x" + globalVar.qtyFormat.format(order.getfItemQty()));
		tvMenuName.setText(order.getSzProductName());
		return convertView;
	}

}
