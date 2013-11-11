package com.syn.iorder;

import java.util.List;

import syn.pos.data.model.ReasonGroups;
import syn.pos.mobile.iordertab.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class ReasonAdapter extends BaseAdapter{
	private List<ReasonGroups.ReasonDetail> reasonLst;
	private LayoutInflater inflater;
	
	public ReasonAdapter(Context c, List<ReasonGroups.ReasonDetail> reasonLst){
		this.reasonLst = reasonLst;
		inflater = LayoutInflater.from(c);
	}
	
	@Override
	public int getCount() {
		return reasonLst != null ? reasonLst.size() : 0;
	}

	@Override
	public ReasonGroups.ReasonDetail getItem(int position) {
		return reasonLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ReasonGroups.ReasonDetail reason = reasonLst.get(position);
		ViewHolder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.list_select_template, null);
			holder = new ViewHolder();
			holder.checkBox1 = (CheckBox) convertView.findViewById(R.id.checkBox1);
			holder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.checkBox1.setChecked(reason.isChecked());
		holder.textView1.setText(reason.getReasonText());
		return convertView;
	}
	
	private class ViewHolder{
		CheckBox checkBox1;
		TextView textView1;
	}
}
