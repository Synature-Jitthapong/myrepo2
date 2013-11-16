package com.syn.iorder;

import java.util.List;

import syn.pos.data.model.TableInfo.TableZone;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TableZoneSpinnerAdapter extends BaseAdapter{
	private List<TableZone> tbZoneLst;
	private LayoutInflater inflater;
	public TableZoneSpinnerAdapter(Context c, List<TableZone> tbZoneLst){
		this.tbZoneLst = tbZoneLst;
		inflater = LayoutInflater.from(c);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return tbZoneLst != null ? tbZoneLst.size() : 0;
	}

	@Override
	public TableZone getItem(int position) {
		// TODO Auto-generated method stub
		return tbZoneLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TableZone tbZone = tbZoneLst.get(position);
		ViewHolder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.spinner_item, null);
			holder = new ViewHolder();
			holder.tvTableZone = (TextView) convertView;
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvTableZone.setText(tbZone.getZoneName());
		return convertView;
	}
	
	private class ViewHolder{
		TextView tvTableZone;
	}
	
}