package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class PayInfoFragment extends DialogFragment{
	private PaymentListener mListener;
	private GlobalVar mGlobalVar;
	private int mTransactionId;
	private int mComputerId;
	private double mTotalPrice;
	private double mTotalPay;
	private double mChange;
	private PayInfoAdapter mPayInfoAdapter;
	private EditText mTxtTotalPrice;
	private TextView mTvTotalPay;
	private TextView mTvChange;
	
	public static PayInfoFragment newInstance(int transactionId, int computerId, double totalPrice){
		PayInfoFragment f = new PayInfoFragment();
		Bundle b = new Bundle();
		b.putInt("transactionId", transactionId);
		b.putInt("computerId", computerId);
		b.putDouble("totalPrice", totalPrice);
		f.setArguments(b);
		return f;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater)
				getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View payInfo = inflater.inflate(R.layout.call_checkbill_with_payinfo, null);
		mTxtTotalPrice = (EditText) payInfo.findViewById(R.id.txtTotalPrice);
		mTvTotalPay = (TextView) payInfo.findViewById(R.id.tvTotalPay);
		mTvChange = (TextView) payInfo.findViewById(R.id.tvChange);
		Button btnClose = (Button) payInfo.findViewById(R.id.button1);
		btnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
			
		});
		final ListView lvPayInfo = (ListView) payInfo.findViewById(R.id.lvPayment);
		mPayInfoAdapter = new PayInfoAdapter();
		lvPayInfo.setAdapter(mPayInfoAdapter);
		mTxtTotalPrice.setText(mGlobalVar.decimalFormat.format(mTotalPrice));
		mTvTotalPay.setText(mGlobalVar.decimalFormat.format(mTotalPrice));
		mTxtTotalPrice.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE){
					double totalPay = IOrderUtility.stringToDouble(mTxtTotalPrice.getText().toString());
					mTotalPay = totalPay;
					setPay();
					setChange();
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
						      Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(mTxtTotalPrice.getWindowToken(), 0);
					return true;
				}
				return false;
			}
			
		});
		return new AlertDialog.Builder(getActivity())
			.setView(payInfo)
			.setCancelable(false)
			.setNeutralButton(R.string.send, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mListener.onSend(mTransactionId, mComputerId, mTotalPrice, mTotalPay);
				}
			}).create();
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGlobalVar = new GlobalVar(getActivity());
		mTransactionId = getArguments().getInt("transactionId");
		mComputerId = getArguments().getInt("computerId");
		mTotalPrice = getArguments().getDouble("totalPrice");
		mTotalPay = mTotalPrice;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (PaymentListener) activity;
		} catch (Exception e) {
			throw new ClassCastException(activity.toString() + " must implement PaymentListener");
		}
	}
	
	public class PayInfoAdapter extends BaseAdapter{
		private List<PaymentInfo> mPaymentLst;
		private LayoutInflater mInflater;
		
		public PayInfoAdapter(){
			mPaymentLst = new ArrayList<PaymentInfo>();
			String[] paymentArr = getActivity().getResources().getStringArray(R.array.price_list_arr);
			double amount = mTotalPrice;
			for(String payment : paymentArr){
				PaymentInfo p = new PaymentInfo();
				Double value = IOrderUtility.stringToDouble(payment);
				int moneyQty = (int) amount / value.intValue();
				amount = amount % value.intValue();
				//Log.d("money", value + ":" + moneyQty);
				p.setPaymentValue(value);
				p.setPaymentQty(moneyQty);
				mPaymentLst.add(p);
			}
			mInflater = (LayoutInflater)
					getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return mPaymentLst != null ? mPaymentLst.size() : 0;
		}

		@Override
		public PaymentInfo getItem(int position) {
			return mPaymentLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			double totalPay = 0;
			for(PaymentInfo payment : mPaymentLst){
				if(payment.getPaymentQty() > 0)
					totalPay += payment.getPaymentValue() * payment.getPaymentQty();
			}
			mTotalPay = totalPay;
			setPay();
			setChange();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final PaymentInfo payment = mPaymentLst.get(position);
			final ViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.payment_detail_template, null);
				holder = new ViewHolder();
				holder.tvPayName = (TextView) convertView.findViewById(R.id.textView1);
				holder.txtPayQty = (EditText) convertView.findViewById(R.id.txtQty);
				holder.btnMinus = (Button) convertView.findViewById(R.id.btnMinus);
				holder.btnPlus = (Button) convertView.findViewById(R.id.btnPlus);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvPayName.setText(mGlobalVar.qtyFormat.format(payment.getPaymentValue()));
			holder.txtPayQty.setText(String.valueOf(payment.getPaymentQty()));
			holder.btnMinus.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					int qty = Integer.parseInt(holder.txtPayQty.getText().toString());
					if(--qty >= 0){
						payment.setPaymentQty(qty);
						mPayInfoAdapter.notifyDataSetChanged();
						holder.txtPayQty.setText(mGlobalVar.qtyFormat.format(qty));
					}
				}
				
			});
			holder.btnPlus.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					int qty = Integer.parseInt(holder.txtPayQty.getText().toString());
					payment.setPaymentQty(++qty);
					mPayInfoAdapter.notifyDataSetChanged();
					holder.txtPayQty.setText(mGlobalVar.qtyFormat.format(qty));
				}
				
			});
			return convertView;
		}
		
		class ViewHolder{
			TextView tvPayName;
			EditText txtPayQty;
			Button btnMinus;
			Button btnPlus;
		}
	}
	
	private void setPay(){
		mTvTotalPay.setText(mGlobalVar.decimalFormat.format(mTotalPay));
	}
	
	private void setChange(){
		if(mTotalPay > mTotalPrice){
			mChange = mTotalPay - mTotalPrice;
		}else{
			mChange = 0;
		}
		mTvChange.setText(mGlobalVar.decimalFormat.format(mChange));
	}
	
	public static class PaymentInfo{
		private String paymentName;
		private double paymentValue;
		private int paymentQty;
		
		public int getPaymentQty() {
			return paymentQty;
		}
		public void setPaymentQty(int paymentQty) {
			this.paymentQty = paymentQty;
		}
		public String getPaymentName() {
			return paymentName;
		}
		public void setPaymentName(String paymentName) {
			this.paymentName = paymentName;
		}
		public double getPaymentValue() {
			return paymentValue;
		}
		public void setPaymentValue(double paymentValue) {
			this.paymentValue = paymentValue;
		}
	}
	
	public static interface PaymentListener{
		void onSend(int transactionId, int computerId, double totalPrice, double payAmount);
	}
}
