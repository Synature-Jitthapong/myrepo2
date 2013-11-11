package com.syn.iorder;

import java.util.List;

import syn.pos.data.model.MenuGroups;
import syn.pos.data.model.ProductGroups;
import syn.pos.data.model.MenuGroups.MenuComment;
import syn.pos.mobile.iordertab.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckBox;

public class OrderSetMenuCommentAdapter extends MenuCommentListAdapter{
    
	private ProductGroups.PComponentSet pcs;
	
	public OrderSetMenuCommentAdapter(Context context, GlobalVar globalVar, int transactionId,
			List<MenuComment> mcl, ProductGroups.PComponentSet pcs, ListView selectedMenuCommentListView) {
		super(context, globalVar, mcl, transactionId, pcs.getOrderDetailId(), selectedMenuCommentListView);
    	this.pcs = pcs;
	}

	@Override
	protected void updateSelectedListView(){
		List<MenuGroups.MenuComment> mcLst
		= posOrder.listOrderSetComment(transactionId, orderId, pcs.getOrderSetID());
		
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
					if(qty >= 0)
						holder.tvMenuCommentQty.setText(globalVar.qtyFormat.format(qty));
					
					if(qty <= 0)
						holder.checkBox1.setChecked(false);
					
					posOrder.updateOrderSetComment(transactionId, orderId, pcs.getOrderSetID(), mc.getMenuCommentID(), qty);
					updateSelectedListView();
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

					posOrder.updateOrderSetComment(transactionId, orderId, pcs.getOrderSetID(), mc.getMenuCommentID(), qty);
					updateSelectedListView();
				}
			});
			convertView.setTag(holder);
			
		}else{
			holder = (MenuCommentViewHolder) convertView.getTag();
		}

		
		holder.tvMenuCommentName.setText(mc.getMenuCommentName_0());
		holder.tvMenuCommentPrice.setText(globalVar.decimalFormat.format(mc
				.getProductPricePerUnit()));

		// checked if comment
		if (posOrder.chkOrderSetComment(transactionId, orderId,
				pcs.getOrderSetID(), mc.getMenuCommentID())) {
			holder.checkBox1.setChecked(true);

			holder.btnMenuCommentMinus.setEnabled(true);
			holder.btnMenuCommentPlus.setEnabled(true);

			double qty = posOrder.getOrderSetCommentQty(transactionId, orderId,
					pcs.getOrderSetID(), mc.getMenuCommentID());
			if (qty > 0)
				holder.tvMenuCommentQty
						.setText(globalVar.qtyFormat.format(qty));
		} else {
			holder.checkBox1.setChecked(false);
			holder.tvMenuCommentQty.setText("0");

			holder.btnMenuCommentMinus.setEnabled(false);
			holder.btnMenuCommentPlus.setEnabled(false);
		}

		if (mc.getProductPricePerUnit() <= 0) {
			holder.btnMenuCommentPlus.setVisibility(View.INVISIBLE);
			holder.btnMenuCommentMinus.setVisibility(View.GONE);
			holder.tvMenuCommentQty.setVisibility(View.GONE);
			holder.tvMenuCommentPrice.setVisibility(View.GONE);
		} else {
			holder.btnMenuCommentPlus.setVisibility(View.VISIBLE);
			holder.btnMenuCommentMinus.setVisibility(View.VISIBLE);
			holder.tvMenuCommentQty.setVisibility(View.VISIBLE);
			holder.tvMenuCommentPrice.setVisibility(View.VISIBLE);

			mc.setCommentQty(1);
		}

		convertView.setTag(holder);

		return convertView;
	}
}
