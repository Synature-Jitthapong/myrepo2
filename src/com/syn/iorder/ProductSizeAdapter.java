package com.syn.iorder;

import java.util.List;

import syn.pos.data.model.ProductGroups;
import syn.pos.mobile.iordertab.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ProductSizeAdapter extends BaseAdapter {
	private List<ProductGroups.PComponentSet> sizeList;
	private LayoutInflater inflater;
	private GlobalVar globalVar;
	
	public ProductSizeAdapter(Context c, List<ProductGroups.PComponentSet> pcList){
		sizeList = pcList;
		globalVar = new GlobalVar(c);
		inflater = LayoutInflater.from(c);
	}
	
	@Override
	public int getCount() {
		return sizeList.size();
	}

	@Override
	public ProductGroups.PComponentSet getItem(int position) {
		return sizeList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ProductGroups.PComponentSet size = sizeList.get(position);
		ViewHolder holder;
		
		if(convertView == null){
			convertView = inflater.inflate(R.layout.product_size_template, null);
			
			holder = new ViewHolder();
			holder.tvSizeName = (TextView) convertView.findViewById(R.id.textViewSizeName);
			holder.tvSizePrice = (TextView) convertView.findViewById(R.id.textViewSizePrice);
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tvSizeName.setText(size.getMenuName());
		holder.tvSizePrice.setText(globalVar.decimalFormat.format(size.getPricePerUnit()));
		
		return convertView;
	}

	private class ViewHolder{
		TextView tvSizeName;
		TextView tvSizePrice;
	}
}
