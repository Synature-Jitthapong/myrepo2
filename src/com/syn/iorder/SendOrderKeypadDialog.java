package com.syn.iorder;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SendOrderKeypadDialog extends Dialog {
	protected TextView tvDialogTitle;
	protected Button btnConfirm;
	protected Button btnCancel;
	protected EditText txtFastRef;
	protected TextView tvCustQty;
	protected LinearLayout ffSendCustLayout;

	public SendOrderKeypadDialog(final GlobalVar globalVar, Context context,
			int theme) {
		super(context, theme);
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.cf_fast_food_layout, null);

		tvDialogTitle = (TextView) view.findViewById(R.id.textViewFFTitle);
		tvCustQty = (TextView) view.findViewById(R.id.textViewCustQty);

		btnConfirm = (Button) view.findViewById(R.id.buttonFastFoodOk);
		btnCancel = (Button) view.findViewById(R.id.buttonFastFoodCancel);
		Button btnMinus = (Button) view.findViewById(R.id.buttonFastFoodMinus);
		Button btnPlus = (Button) view.findViewById(R.id.buttonFastFoodPlus);

		ffSendCustLayout = (LinearLayout) view
				.findViewById(R.id.ffSendCustLayout);

		txtFastRef = (EditText) view.findViewById(R.id.editTextFFRef);
		txtFastRef.requestFocus();

		Button btnFast0 = (Button) view.findViewById(R.id.buttonFast0);
		Button btnFast1 = (Button) view.findViewById(R.id.buttonFast1);
		Button btnFast2 = (Button) view.findViewById(R.id.buttonFast2);
		Button btnFast3 = (Button) view.findViewById(R.id.buttonFast3);
		Button btnFast4 = (Button) view.findViewById(R.id.buttonFast4);
		Button btnFast5 = (Button) view.findViewById(R.id.buttonFast5);
		Button btnFast6 = (Button) view.findViewById(R.id.buttonFast6);
		Button btnFast7 = (Button) view.findViewById(R.id.buttonFast7);
		Button btnFast8 = (Button) view.findViewById(R.id.buttonFast8);
		Button btnFast9 = (Button) view.findViewById(R.id.buttonFast9);
		Button btnFastDot = (Button) view.findViewById(R.id.buttonFastDot);
		Button btnFastDel = (Button) view.findViewById(R.id.buttonFastDel);
		Button btnFastClear = (Button) view.findViewById(R.id.buttonFastClear);

		btnFast0.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				txtFastRef.append("0");
			}

		});
		btnFast1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				txtFastRef.append("1");
			}

		});
		btnFast2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				txtFastRef.append("2");
			}

		});
		btnFast3.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				txtFastRef.append("3");
			}

		});
		btnFast4.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				txtFastRef.append("4");
			}

		});
		btnFast5.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				txtFastRef.append("5");
			}

		});
		btnFast6.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				txtFastRef.append("6");
			}

		});
		btnFast7.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				txtFastRef.append("7");
			}

		});
		btnFast8.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				txtFastRef.append("8");
			}

		});
		btnFast9.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				txtFastRef.append("9");
			}

		});
		btnFastDot.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				txtFastRef.append(".");
			}

		});
		
		btnFastDel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String txtRef = txtFastRef.getText().toString();
				if (txtRef.length() > 0) {
					txtRef = txtRef.substring(0, txtRef.length() - 1);
					txtFastRef.setTextKeepState(txtRef);
				}
			}

		});

		btnFastClear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				txtFastRef.setText("");
			}

		});

		btnMinus.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int custQty = Integer.parseInt(tvCustQty.getText().toString());
				if (--custQty > 0)
					tvCustQty.setText(globalVar.qtyFormat.format(custQty));
			}

		});

		btnPlus.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int custQty = Integer.parseInt(tvCustQty.getText().toString());
				tvCustQty.setText(globalVar.qtyFormat.format(++custQty));
			}

		});

		this.setContentView(view);
	}

}
