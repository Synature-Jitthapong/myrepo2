package com.syn.iorder;

import java.util.List;

import syn.pos.data.dao.POSOrdering;
import syn.pos.data.model.MenuDataItem;
import syn.pos.data.model.MenuGroups;
import syn.pos.data.model.Order;
import syn.pos.data.model.MenuGroups.MenuComment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.BaseAdapter;

public class MenuCommentListAdapter extends BaseAdapter {
	protected List<MenuGroups.MenuComment> mcl;
	protected Context context;
	protected GlobalVar globalVar;
	protected ListView selectedMenuCommentListView;
	protected LayoutInflater inflater;
	protected POSOrdering posOrder;
	protected int transactionId;
	protected int orderId;
	
	public MenuCommentListAdapter(Context context, GlobalVar gb, List<MenuGroups.MenuComment> mcl, 
			int transId, int ordId, ListView selectedMenuCommentListView){
		this.mcl = mcl;
		this.context = context;
		inflater = LayoutInflater.from(context);
		globalVar = gb;
		this.selectedMenuCommentListView = selectedMenuCommentListView;
		transactionId = transId;
		orderId = ordId;
		posOrder = new POSOrdering(context);
	}
	
	@Override
	public int getCount() {
		return mcl.size();
	}

	@Override
	public MenuGroups.MenuComment getItem(int position) {
		return mcl.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public static class MenuCommentViewHolder {
		CheckBox checkBox1;
		TextView tvMenuCommentName;
		TextView tvMenuCommentQty;
		TextView tvMenuCommentPrice;
		
		Button btnMenuCommentMinus;
		Button btnMenuCommentPlus;
	}

	protected void updateSelectedListView(){
		List<MenuGroups.MenuComment> mcLst
			= posOrder.listOrderCommentTemp(transactionId, orderId);
		
		MenuCommentSelectedAdapter selectedCommentAdapter = 
				new MenuCommentSelectedAdapter(context, globalVar, mcLst);
		selectedMenuCommentListView.setAdapter(selectedCommentAdapter);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final MenuComment mc = mcl.get(position);
		
		final MenuCommentViewHolder holder;
		
		if(convertView == null){
			convertView = inflater.inflate(R.layout.menu_comment_template, null);
			
			holder = new MenuCommentViewHolder();
			holder.checkBox1 = (CheckBox) convertView.findViewById(R.id.checkBox1);
			
			holder.tvMenuCommentName = (TextView) convertView.findViewById(R.id.tvMenuCommentName);
			holder.tvMenuCommentPrice = (TextView) convertView.findViewById(R.id.tvMenuCommentPrice);
			holder.tvMenuCommentQty = (TextView) convertView.findViewById(R.id.tvMenuCommentQty);
			
			holder.btnMenuCommentMinus = (Button) convertView.findViewById(R.id.btnMenuCommentMinus);
			holder.btnMenuCommentPlus = (Button) convertView.findViewById(R.id.btnMenuCommentPlus);
			

			holder.btnMenuCommentMinus.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					double qty = 0;
					if(!holder.tvMenuCommentQty.getText().toString().equals(""))
						qty = Double.parseDouble(holder.tvMenuCommentQty.getText().toString());
	
					--qty;
					if(qty > 0){
						holder.tvMenuCommentQty.setText(globalVar.qtyFormat.format(qty));
						posOrder.updateOrderComment(transactionId, orderId, mc.getMenuCommentID(), 
								qty, mc.getProductPricePerUnit());
						
						updateSelectedListView();
					}
				}
			});
			
			holder.btnMenuCommentPlus.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					double qty = 0;
					if(!holder.tvMenuCommentQty.getText().toString().equals(""))
						qty = Double.parseDouble(holder.tvMenuCommentQty.getText().toString());
					
					++qty;
					holder.tvMenuCommentQty.setText(globalVar.qtyFormat.format(qty));
					
					holder.checkBox1.setChecked(true);
					
					posOrder.updateOrderComment(transactionId, orderId, mc.getMenuCommentID(), 
							qty, mc.getProductPricePerUnit());
					
					updateSelectedListView();
					
				}
			});
			
			convertView.setTag(holder);
			
		}else{
			holder = (MenuCommentViewHolder) convertView.getTag();
		}
		
		holder.tvMenuCommentName.setText(mc.getMenuCommentName_0());
		holder.tvMenuCommentPrice.setText(globalVar.decimalFormat.format(mc.getProductPricePerUnit()));

		// checked if comment
		if(posOrder.chkOrderComment(transactionId, orderId, mc.getMenuCommentID())){
			holder.checkBox1.setChecked(true);
			
			holder.btnMenuCommentMinus.setEnabled(true);
			holder.btnMenuCommentPlus.setEnabled(true);
			
			double qty =posOrder.getOrderCommentQty(transactionId, orderId, mc.getMenuCommentID()); 
			if( qty > 0)
				holder.tvMenuCommentQty.setText(globalVar.qtyFormat.format(qty));
		}
		else{
			holder.checkBox1.setChecked(false);
			holder.tvMenuCommentQty.setText("0");
			
			holder.btnMenuCommentMinus.setEnabled(false);
			holder.btnMenuCommentPlus.setEnabled(false);
		}
		
		if(mc.getProductPricePerUnit() <= 0)
		{
			holder.btnMenuCommentPlus.setVisibility(View.INVISIBLE);
			holder.btnMenuCommentMinus.setVisibility(View.GONE);
			holder.tvMenuCommentQty.setVisibility(View.GONE);
			holder.tvMenuCommentPrice.setVisibility(View.GONE);
		}else{
			holder.btnMenuCommentPlus.setVisibility(View.VISIBLE);
			holder.btnMenuCommentMinus.setVisibility(View.VISIBLE);
			holder.tvMenuCommentQty.setVisibility(View.VISIBLE);
			holder.tvMenuCommentPrice.setVisibility(View.VISIBLE);
			
			mc.setCommentQty(1);
		}
		return convertView;
	}

}
