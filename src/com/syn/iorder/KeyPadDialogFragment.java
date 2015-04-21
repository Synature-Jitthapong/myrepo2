package com.syn.iorder;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class KeyPadDialogFragment extends DialogFragment{

	public static final String TAG = KeyPadDialogFragment.class.getSimpleName();
	
	private OnKeyPadListener mCallback;
	
	private int mOrderId;
	private String mMenuName;
	private double mCurrQty;
	private int mPosition;
	private GlobalVar mGlobalVar;
	private double mTotalQty;
	private StringBuilder mStrQty;
	private EditText mTxtKeyPadDisplay;
	
	
	public static interface OnKeyPadListener{
		void onEnter(int orderId, int position, double val);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof OnKeyPadListener){
			mCallback = (OnKeyPadListener) activity;
		}
	}

	public static KeyPadDialogFragment newInstance(int orderId, int position, String menuName, double currQty){
		KeyPadDialogFragment f = new KeyPadDialogFragment();
		Bundle b = new Bundle();
		b.putInt("orderId", orderId);
		b.putInt("position", position);
		b.putString("menuName", menuName);
		b.putDouble("currQty", currQty);
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mOrderId = getArguments().getInt("orderId");
		mPosition = getArguments().getInt("position");
		mMenuName = getArguments().getString("menuName");
		mCurrQty = getArguments().getDouble("currQty");
		mTotalQty = mCurrQty;
		
		mGlobalVar = new GlobalVar(getActivity());
		mStrQty = new StringBuilder();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getDialog().setTitle(mMenuName);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.keypad_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mTxtKeyPadDisplay = (EditText) view.findViewById(R.id.txtKeyPadDisplay);
		Button btn1 = (Button) view.findViewById(R.id.btn1);
		Button btn2 = (Button) view.findViewById(R.id.btn2);
		Button btn3 = (Button) view.findViewById(R.id.btn3);
		Button btn4 = (Button) view.findViewById(R.id.btn4);
		Button btn5 = (Button) view.findViewById(R.id.btn5);
		Button btn6 = (Button) view.findViewById(R.id.btn6);
		Button btn7 = (Button) view.findViewById(R.id.btn7);
		Button btn8 = (Button) view.findViewById(R.id.btn8);
		Button btn9 = (Button) view.findViewById(R.id.btn9);
		Button btn0 = (Button) view.findViewById(R.id.btn0);
		Button btnDel = (Button) view.findViewById(R.id.btnDel);
		Button btnClear = (Button) view.findViewById(R.id.btnClear);
		Button btnDot = (Button) view.findViewById(R.id.btnDot);
		Button btnEnter = (Button) view.findViewById(R.id.btnEnter);
		btn1.setOnClickListener(mOnClick);
		btn2.setOnClickListener(mOnClick);
		btn3.setOnClickListener(mOnClick);
		btn4.setOnClickListener(mOnClick);
		btn5.setOnClickListener(mOnClick);
		btn6.setOnClickListener(mOnClick);
		btn7.setOnClickListener(mOnClick);
		btn8.setOnClickListener(mOnClick);
		btn9.setOnClickListener(mOnClick);
		btn0.setOnClickListener(mOnClick);
		btnDel.setOnClickListener(mOnClick);
		btnClear.setOnClickListener(mOnClick);
		btnDot.setOnClickListener(mOnClick);
		btnEnter.setOnClickListener(mOnClick);

		mTxtKeyPadDisplay.setText(mGlobalVar.qtyDecimalFormat.format(mTotalQty));
	}
	
	private void display(){
		try {
			mTotalQty = Double.parseDouble(mStrQty.toString());
		} catch (NumberFormatException e) {
			mTotalQty = 1;
		}
		mTxtKeyPadDisplay.setText(mGlobalVar.qtyDecimalFormat.format(mTotalQty));
	}
	
	private View.OnClickListener mOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.btn1:
				mStrQty.append("1");
				display();
				break;
			case R.id.btn2:
				mStrQty.append("2");
				display();
				break;
			case R.id.btn3:
				mStrQty.append("3");
				display();
				break;
			case R.id.btn4:
				mStrQty.append("4");
				display();
				break;
			case R.id.btn5:
				mStrQty.append("5");
				display();
				break;
			case R.id.btn6:
				mStrQty.append("6");
				display();
				break;
			case R.id.btn7:
				mStrQty.append("7");
				display();
				break;
			case R.id.btn8:
				mStrQty.append("8");
				display();
				break;
			case R.id.btn9:
				mStrQty.append("9");
				display();
				break;
			case R.id.btn0:
				mStrQty.append("0");
				display();
				break;
			case R.id.btnDel:
				try {
					mStrQty.deleteCharAt(mStrQty.length() - 1);
				} catch (Exception e) {
					mStrQty = new StringBuilder();
				}
				display();
				break;
			case R.id.btnDot:
				mStrQty.append(".");
				display();
				break;
			case R.id.btnClear:
				mStrQty = new StringBuilder();
				display();
				break;
			case R.id.btnEnter:
				if(mTotalQty > 0){
					mCallback.onEnter(mOrderId, mPosition, mTotalQty);
					getDialog().dismiss();
				}
				break;
			}
		}
	};
}
