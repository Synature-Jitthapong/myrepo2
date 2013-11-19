package com.syn.iorder;

import java.util.List;
import syn.pos.data.dao.ShopProperty;
import syn.pos.data.model.OrderSendData;
import syn.pos.mobile.util.Log;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ShooseMenuAdapter extends BaseAdapter {
	private List<OrderSendData.OrderDetail> orderData;
	private LayoutInflater inflater;
	private GlobalVar globalVar;
	private ShopProperty shopProperty;
	
	public ShooseMenuAdapter(Context c, GlobalVar gb, List<OrderSendData.OrderDetail> data){
		orderData = data;
		globalVar = gb;
		inflater = LayoutInflater.from(c);
		shopProperty = new ShopProperty(c, null);
	}
	
	@Override
	public int getCount() {
		return orderData.size();
	}

	@Override
	public OrderSendData.OrderDetail getItem(int position) {
		return orderData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		OrderSendData.OrderDetail data = orderData.get(position);
		MoveMenuHolder holder;
		
		if(convertView == null){
			convertView = inflater.inflate(R.layout.shoose_menu_template, null);
			holder = new MoveMenuHolder();
			
			holder.tvExtra = (TextView) convertView.findViewById(R.id.tvExtra);
			holder.tvMenuName = (TextView) convertView.findViewById(R.id.shooseMenuTvMenuName);
			holder.tvMenuQty = (TextView) convertView.findViewById(R.id.shooseMenuTvMenuQty);
			
			convertView.setTag(holder);
			
		}else{
			holder = (MoveMenuHolder) convertView.getTag();
		}
		
		String extra = "";
		if(data.getiSeatID() != 0 && data.getiCourseID() != 0){
			syn.pos.data.model.ShopData.SeatNo seat = shopProperty.getSeatNo(data.getiSeatID());
			syn.pos.data.model.ShopData.CourseInfo course = shopProperty.getCourse(data.getiCourseID());
			extra = course.getCourseShortName() + "-" + seat.getSeatName();
		}else{
			if(data.getiSeatID() != 0){
				syn.pos.data.model.ShopData.SeatNo seat = shopProperty.getSeatNo(data.getiSeatID());
				extra = seat.getSeatName();
			}
			
			if(data.getiCourseID() != 0){
				syn.pos.data.model.ShopData.CourseInfo course = shopProperty.getCourse(data.getiCourseID());
				extra = course.getCourseShortName();
			}
		}
		
		holder.tvExtra.setText(extra);
		holder.tvMenuName.setText(data.getSzProductName());
		holder.tvMenuQty.setText("x" + globalVar.qtyFormat.format(data.getfItemQty()));

		return convertView;
	}
	
	public static class MoveMenuHolder{
		TextView tvExtra;
		TextView tvMenuName;
		TextView tvMenuQty;
	}
	
}
