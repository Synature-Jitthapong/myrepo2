package com.syn.iorder;

import syn.pos.mobile.iordertab.R;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CustomDialog extends Dialog{
	public TextView title; 
	public TextView message;
	public Button btnCancel;
	public Button btnOk;
	
	public CustomDialog(Context context, int style) {
		super(context, style);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.custom_dialog, null);

		title = (TextView) view.findViewById(R.id.textViewTitle);
		message = (TextView) view.findViewById(R.id.textViewMessage);
		btnCancel = (Button) view.findViewById(R.id.buttonConfirmCancel);
		btnOk = (Button) view.findViewById(R.id.buttonOk);
		
		setContentView(view);
	}
	
	public CustomDialog(Context context, int style, View view){
		super(context, style);
		
		title = (TextView) view.findViewById(R.id.textViewTitle);
		message = (TextView) view.findViewById(R.id.textViewMessage);
		btnCancel = (Button) view.findViewById(R.id.buttonConfirmCancel);
		btnOk = (Button) view.findViewById(R.id.buttonOk);
		
		setContentView(view);
	}
}
