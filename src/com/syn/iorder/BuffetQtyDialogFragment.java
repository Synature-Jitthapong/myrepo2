package com.syn.iorder;

import java.text.NumberFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class BuffetQtyDialogFragment extends DialogFragment{
	
	private GlobalVar mGlobalVar;
	
	private int mStaffId;
	private int mTableId;
	private int mOrderId;
	private String mTableName;
	private String mItemName;
	private int mItemQty;
	
	public static BuffetQtyDialogFragment newInstance(int staffId, int tableId, String tableName, int orderId, String itemName, int itemQty){
		BuffetQtyDialogFragment f = new BuffetQtyDialogFragment();
		Bundle b = new Bundle();
		b.putInt("staffId", staffId);
		b.putInt("tableId", tableId);
		b.putInt("orderId", orderId);
		b.putString("tableName", tableName);
		b.putString("itemName", itemName);
		b.putInt("itemQty", itemQty);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGlobalVar = new GlobalVar(getActivity());
		mStaffId = getArguments().getInt("staffId");
		mTableId = getArguments().getInt("tableId");
		mOrderId = getArguments().getInt("orderId");
		mTableName = getArguments().getString("tableName");
		mItemName = getArguments().getString("itemName");
		mItemQty = getArguments().getInt("itemQty");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.buffet_qty, null);
		final TextView tvQty = (TextView) content.findViewById(R.id.tvQty);
		final Button btnMinus = (Button) content.findViewById(R.id.btnMinus);
		final Button btnPlus = (Button) content.findViewById(R.id.btnPlus);
		tvQty.setText(NumberFormat.getInstance().format(mItemQty));
		btnMinus.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(mItemQty > 0)
					mItemQty--;
				tvQty.setText(NumberFormat.getInstance().format(mItemQty));
			}
			
		});
		btnPlus.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				tvQty.setText(NumberFormat.getInstance().format(++mItemQty));
			}
		});
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(mTableName + ": " + mItemName);
		builder.setView(content);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {	
			}
		});
		builder.setPositiveButton(android.R.string.ok, null);
		final AlertDialog d = builder.create();
		d.show();
		d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				new BuffetMenuModQty(getActivity(), mGlobalVar, mStaffId, mTableId, mOrderId, mItemQty, new BuffetMenuModQty.BuffetMenuModQtyListener() {
					
					@Override
					public void onPost(String msg) {
						new AlertDialog.Builder(getActivity())
						.setMessage(msg)
						.setCancelable(false)
						.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								d.dismiss();
							}
						}).show();
					}
					
					@Override
					public void onError(String msg) {
						new AlertDialog.Builder(getActivity())
						.setMessage(msg)
						.setCancelable(false)
						.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								d.dismiss();
							}
						}).show();
						
					}
				}).execute(GlobalVar.FULL_URL);
			}
		});
		return d;
	}
}
