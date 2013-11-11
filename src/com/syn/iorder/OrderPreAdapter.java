package com.syn.iorder;

import java.util.List;

import syn.pos.data.dao.MenuItem;
import syn.pos.data.model.POSData_OrderTransInfo;
import syn.pos.mobile.iordertab.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OrderPreAdapter extends BaseAdapter {
	private List<POSData_OrderTransInfo.POSData_OrderItemInfo> orderLst;
	private MenuItem menuItem;
	private GlobalVar globalVar;
	private LayoutInflater inflater;
	
	public OrderPreAdapter(Context context, GlobalVar globalVar, 
			List<POSData_OrderTransInfo.POSData_OrderItemInfo> orderLst){
		this.inflater = LayoutInflater.from(context);
		this.menuItem = new MenuItem(context);
		this.globalVar = globalVar;
		this.orderLst = orderLst;
	}
	
	@Override
	public int getCount() {
		return orderLst == null ? 0 : orderLst.size();
	}

	@Override
	public POSData_OrderTransInfo.POSData_OrderItemInfo getItem(int position) {
		return orderLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		POSData_OrderTransInfo.POSData_OrderItemInfo order = 
				orderLst.get(position);
		ViewHolder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.order_pre_template, null);
			holder = new ViewHolder();
			holder.tvOrderPreQty = (TextView) convertView.findViewById(R.id.textViewOrderListTitle);
			holder.tvOrderPreMenuName = (TextView) convertView.findViewById(R.id.textViewOrderPreMenuName);
			holder.tvOrderPreMenuPrice = (TextView) convertView.findViewById(R.id.textViewOrderPreMenuPrice);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tvOrderPreQty.setText("x" + globalVar.qtyFormat.format(order.getfProductQty()));
		holder.tvOrderPreMenuName.setText(menuItem.getMenuItem(order.getiProductID()).getMenuName());
		holder.tvOrderPreMenuPrice.setText(globalVar.decimalFormat.format(order.getfProductPrice()
				* order.getfProductQty()));
		
		return convertView;
	}
	
	private class ViewHolder{
		TextView tvOrderPreQty;
		TextView tvOrderPreMenuName;
		TextView tvOrderPreMenuPrice;
	}
}
