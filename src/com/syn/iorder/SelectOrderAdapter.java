package com.syn.iorder;

import java.util.List;

import syn.pos.data.dao.MenuUtil;
import syn.pos.data.dao.ShopProperty;
import syn.pos.data.model.OrderSendData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class SelectOrderAdapter extends BaseAdapter {
	
	private List<OrderSendData.OrderDetail> orderLst;
	private Context context;
	private GlobalVar globalVar;
	private ShopProperty shopProperty;
	private LayoutInflater inflater;
	
	public SelectOrderAdapter(Context c, GlobalVar gb, List<OrderSendData.OrderDetail> ordLst){
		context = c;
		orderLst = ordLst;
		globalVar = gb;
		inflater = LayoutInflater.from(c);
		shopProperty = new ShopProperty(c, null);
	}
	
	@Override
	public int getCount() {
		return orderLst != null ? orderLst.size() : 0;
	}

	@Override
	public OrderSendData.OrderDetail getItem(int position) {
		return orderLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		OrderSendData.OrderDetail detail = orderLst.get(position);
		SelectOrderViewHolder holder;
		if(convertView == null){
			holder = new SelectOrderViewHolder();
			convertView = inflater.inflate(R.layout.select_order_template, null);
			
			holder.checkBox1 = (CheckBox) convertView.findViewById(R.id.checkBox1);
			holder.tvExtra = (TextView) convertView.findViewById(R.id.tvExtra);
			holder.tvOrderName = (TextView) convertView.findViewById(R.id.tvSelectOrderOrderName);
			holder.tvOrderQty = (TextView) convertView.findViewById(R.id.tvSelectOrderQty);
			
			convertView.setTag(holder);
		}else{
			holder = (SelectOrderViewHolder) convertView.getTag();
		}

		// check if item is selected
		MenuUtil menuUtil = new MenuUtil(context);
		if(menuUtil.checkSelectedMenu(detail.getiOrderID(), detail.getiProductID()) != 0)
			holder.checkBox1.setChecked(true);
		else
			holder.checkBox1.setChecked(false);
		
		String extra = "";
		
		if(detail.getiSeatID() != 0 && detail.getiCourseID() != 0){
			syn.pos.data.model.ShopData.SeatNo seat = 
					shopProperty.getSeatNo(detail.getiSeatID());
			syn.pos.data.model.ShopData.CourseInfo course = 
					shopProperty.getCourse(detail.getiCourseID());
			
			extra = course.getCourseShortName() + "-" + seat.getSeatName();
		}else{
			if(detail.getiSeatID() != 0){
				syn.pos.data.model.ShopData.SeatNo seat = 
						shopProperty.getSeatNo(detail.getiSeatID());
				
				extra = seat.getSeatName();
			}
			
			if(detail.getiCourseID() != 0){
				syn.pos.data.model.ShopData.CourseInfo course = 
						shopProperty.getCourse(detail.getiCourseID());
				
				extra = course.getCourseShortName();
			}
		}

		holder.tvExtra.setText(extra);
		holder.tvOrderName.setText(detail.getSzProductName());
		holder.tvOrderQty.setText("x" + globalVar.qtyFormat.format(detail.getfItemQty()));
		
		if(detail.getiProductType() == 14 || detail.getiProductType() == 15){
			holder.tvOrderName.setText("   " + detail.getSzProductName());
		}
		return convertView;
	}

	protected static class SelectOrderViewHolder{
		CheckBox checkBox1;
		TextView tvExtra;
		TextView tvOrderName;
		TextView tvOrderQty;
	}
}
