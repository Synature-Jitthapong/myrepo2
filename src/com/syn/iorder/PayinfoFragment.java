package com.syn.iorder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class PayinfoFragment extends DialogFragment{
	private PaymentListener mListener;
	private double totalPrice;
	private double totalPay;
	
	public static PayinfoFragment newInstance(double totalPrice){
		PayinfoFragment f = new PayinfoFragment();
		Bundle b = new Bundle();
		b.putDouble("totalPrice", totalPrice);
		f.setArguments(b);
		return f;
	}

	@Override
	public Dialog getDialog() {
		AlertDialog d = (AlertDialog)super.getDialog();
		d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mListener.onSend(totalPay);
			}
			
		});
		return d;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater)
				getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View payInfo = inflater.inflate(R.layout.call_checkbill_with_payinfo, null);
		final EditText txtTotalPay = (EditText) payInfo.findViewById(R.id.txtTotalPay);
		final ListView lvPayInfo = (ListView) payInfo.findViewById(R.id.lvPayment);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
			.setView(payInfo)
			.setNeutralButton(R.string.send, null);
		AlertDialog d = builder.create();
		return d;
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		totalPrice = getArguments().getDouble("totalPrice");
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
		private String[] mPayDetailArr = getActivity().getResources().getStringArray(R.array.price_list_arr);
		private LayoutInflater mInflater;
		
		public PayInfoAdapter(){
			mInflater = (LayoutInflater)
					getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return mPayDetailArr != null ? mPayDetailArr.length : 0;
		}

		@Override
		public String getItem(int position) {
			return mPayDetailArr[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String amount = mPayDetailArr[position];
			ViewHolder holder = null;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.payment_detail_template, null);
				holder = new ViewHolder();
				holder.tvPayName = (TextView) convertView.findViewById(R.id.textView1);
				holder.txtPayAmount = (EditText) convertView.findViewById(R.id.txtQty);
				holder.btnMinus = (Button) convertView.findViewById(R.id.btnMinus);
				holder.btnPlus = (Button) convertView.findViewById(R.id.btnPlus);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvPayName.setText(amount);
			holder.txtPayAmount.setText("1");
			holder.btnMinus.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
				}
				
			});
			holder.btnPlus.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
				}
				
			});
			return null;
		}
		
		class ViewHolder{
			TextView tvPayName;
			EditText txtPayAmount;
			Button btnMinus;
			Button btnPlus;
		}
	}
	
	public static interface PaymentListener{
		void onSend(double payAmount);
	}
}
