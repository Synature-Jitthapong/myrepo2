package com.syn.iorder;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class PrinterListAdapter extends BaseAdapter{
	private List<PrinterUtils.Printer> mPrinterLst;
	private LayoutInflater mInflater;
	
	public PrinterListAdapter(Context c, List<PrinterUtils.Printer> printerLst){
		mInflater = (LayoutInflater)
				c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPrinterLst = printerLst;
	}
	
	@Override
	public int getCount() {
		return mPrinterLst != null ? mPrinterLst.size() : 0;
	}

	@Override
	public PrinterUtils.Printer getItem(int position) {
		return mPrinterLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(R.layout.printer_list_template, null);
		TextView tvNo = (TextView) convertView.findViewById(R.id.tvPrinterNo);
		CheckedTextView chPrinterName = (CheckedTextView) convertView.findViewById(R.id.chPrinterName);
		
		tvNo.setText(String.valueOf(position + 1) + ".");
		chPrinterName.setText(mPrinterLst.get(position).getPrinterName());
		chPrinterName.setChecked(mPrinterLst.get(position).isChecked());	
		return convertView;
	}

}
