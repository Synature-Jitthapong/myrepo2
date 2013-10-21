package com.syn.iorder;

import java.util.List;

import syn.pos.data.model.MenuGroups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MenuCommentSelectedAdapter extends BaseAdapter{
	private List<MenuGroups.MenuComment> mcLst;
	private LayoutInflater inflater;
	private GlobalVar globalVar;
	
	public MenuCommentSelectedAdapter(Context c, GlobalVar gb, List<MenuGroups.MenuComment> mcLst){
		this.mcLst = mcLst;
		this.globalVar = gb;
		inflater = LayoutInflater.from(c);
	}
	
	@Override
	public int getCount() {
		return mcLst != null ? mcLst.size() : 0;
	}

	@Override
	public MenuGroups.MenuComment getItem(int position) {
		return mcLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MenuGroups.MenuComment mc = mcLst.get(position);
		ViewHolder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.comment_selected_template, null);
			holder = new ViewHolder();
			holder.tvCommentName = (TextView) convertView.findViewById(R.id.textView1);
			holder.tvCommentQty = (TextView) convertView.findViewById(R.id.textView2);
				
			holder.tvCommentName.setText(mc.getMenuCommentName_0());

			if(mc.getCommentQty() > 0){
				holder.tvCommentQty.setVisibility(View.VISIBLE);

				holder.tvCommentQty.setText(globalVar.qtyFormat.format(mc.getCommentQty()));
				holder.tvCommentQty.append(
						" x " + globalVar.decimalFormat.format(mc.getProductPricePerUnit()));
			}
			else{
				holder.tvCommentQty.setVisibility(View.GONE);
			}
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		return convertView;
	}

	private class ViewHolder{
		public TextView tvCommentName;
		public TextView tvCommentQty;
	}
}
