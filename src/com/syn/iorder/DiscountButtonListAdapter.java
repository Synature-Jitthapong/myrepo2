package com.syn.iorder;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class DiscountButtonListAdapter extends BaseAdapter{
	private List<DiscountUtils.ButtonDiscount> mDiscountLst;
	private LayoutInflater mInflater;
	
	public DiscountButtonListAdapter(Context c, List<DiscountUtils.ButtonDiscount> discountLst){
		mDiscountLst = discountLst;
		mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mDiscountLst != null ? mDiscountLst.size() : 0;
	}

	@Override
	public DiscountUtils.ButtonDiscount getItem(int position) {
		return mDiscountLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.list_selected_template, null);
		}
		TextView tvNo = (TextView) convertView.findViewById(R.id.tvNo);
		tvNo.setText(null);
		CheckedTextView chk = (CheckedTextView) convertView.findViewById(R.id.checkedTextView1);
		chk.setText(mDiscountLst.get(position).getDiscountButtonName());
		chk.setChecked(mDiscountLst.get(position).isChecked());
		return convertView;
	}

}
