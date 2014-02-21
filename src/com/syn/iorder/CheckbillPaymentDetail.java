package com.syn.iorder;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class CheckbillPaymentDetail extends AlertDialog.Builder{
	private LayoutInflater mInflater;
	private String[] mPaymentVals;
	private PaymentDetailAdapter mPaymentAdapter;
	private EditText mTxtTotalPay;
	private ListView mLvPaymentDetail;
	
	public CheckbillPaymentDetail(Context context) {
		super(context);
		mInflater = (LayoutInflater) 
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = mInflater.inflate(R.layout.call_checkbill_with_payinfo, null);
		mTxtTotalPay = (EditText) v.findViewById(R.id.txtTotalPay);
		mLvPaymentDetail = (ListView) v.findViewById(R.id.lvPayment);
		
		mPaymentVals = context.getResources().getStringArray(R.array.price_list_arr);
		mPaymentAdapter = new PaymentDetailAdapter();
		mLvPaymentDetail.setAdapter(mPaymentAdapter);
	}

	private class PaymentDetailAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mPaymentVals != null ? mPaymentVals.length : 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
