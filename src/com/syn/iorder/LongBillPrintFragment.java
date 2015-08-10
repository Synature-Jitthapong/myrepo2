package com.syn.iorder;

import java.util.ArrayList;

import com.syn.iorder.PrinterUtils.PrintUtilLine;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LongBillPrintFragment extends DialogFragment{
	
	public static final String TAG = LongBillPrintFragment.class.getSimpleName();

	private int mStaffId;
	private int mTableId;
	
	private ArrayList<PrinterUtils.PrintUtilLine> mPrintUtilLineLst;
	private LongBillListAdapter mLongBillAdapter;
	
	private LinearLayout mProgressContainer;
	private ProgressBar mLoadLongBillProgress;
	private ListView mLvLongBill;
	private TextView mTvMesg;
	private TextView mLoadLongBillProgressText;
	
	public static LongBillPrintFragment newInstance(int tableId, int staffId){
		LongBillPrintFragment f = new LongBillPrintFragment();
		Bundle b = new Bundle();
		b.putInt("tableId", tableId);
		b.putInt("staffId", staffId);
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mTableId = getArguments().getInt("tableId");
		mStaffId = getArguments().getInt("staffId");
		
		mPrintUtilLineLst = new ArrayList<PrinterUtils.PrintUtilLine>();
		mLongBillAdapter = new LongBillListAdapter();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadLongBill();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View contentView = getActivity().getLayoutInflater().inflate(R.layout.longbill_list_layout, null);
		mProgressContainer = (LinearLayout) contentView.findViewById(R.id.progressContainer);
		mTvMesg = (TextView) contentView.findViewById(R.id.tvMesg);
		mLoadLongBillProgress = (ProgressBar) contentView.findViewById(R.id.loadLongBillProgress);
		mLoadLongBillProgressText = (TextView) contentView.findViewById(R.id.loadLongBillProgressText);
		mLvLongBill = (ListView) contentView.findViewById(R.id.lvLongBill);
		mLvLongBill.setAdapter(mLongBillAdapter);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(contentView);
//		builder.setNegativeButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//			}
//		});
		builder.setPositiveButton(R.string.global_btn_close, null);
		
		final AlertDialog dialog = builder.create();
		dialog.show();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		
		Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		btnPositive.setEnabled(false);
		btnPositive.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
			
		});
		return dialog;
	}
	
	private Button getButtonFromDialog(int buttonType){
		AlertDialog dialog = (AlertDialog) getDialog();
		return dialog.getButton(buttonType);
	}
	
	private void loadLongBill(){
		new PrinterUtils.LoadLongBillRealFormat(getActivity(), new GlobalVar(getActivity()), 
				mTableId, mStaffId, mOnLoadFormatPrintListener).execute(GlobalVar.FULL_URL);
	}
	
	@SuppressLint("NewApi")
	private PrinterUtils.OnLoadFormatPrintBillDetailListener mOnLoadFormatPrintListener = 
			new PrinterUtils.OnLoadFormatPrintBillDetailListener() {
				
				@Override
				public void onPre() {
					mTvMesg.setText(null);
					mProgressContainer.setVisibility(View.VISIBLE);
					Button btnPositive = getButtonFromDialog(AlertDialog.BUTTON_POSITIVE);
					btnPositive.setEnabled(false);
				}
				
				@Override
				public void onPost() {
				}
				
				@Override
				public void onError(String msg) {
					mTvMesg.setText(msg);
					mProgressContainer.setVisibility(View.GONE);

					Button btnPositive = getButtonFromDialog(AlertDialog.BUTTON_POSITIVE);
					btnPositive.setEnabled(true);
				}
				
				@Override
				public void onPost(ArrayList<PrintUtilLine> lines, String result) {
					mTvMesg.setText(null);
					//mProgressContainer.setVisibility(View.GONE);
					
					Button btnPositive = getButtonFromDialog(AlertDialog.BUTTON_POSITIVE);
					btnPositive.setEnabled(true);
					
//					mPrintUtilLineLst = lines;
//					mLongBillAdapter.notifyDataSetChanged();
					
					BluetoothPrinter btPrinter = new BluetoothPrinter(getActivity(), 
							lines, mOnPrinterWorkingListener);
					btPrinter.print();
				}
			};
			
	private BluetoothPrinter.OnPrinterWorkingListener mOnPrinterWorkingListener
		= new BluetoothPrinter.OnPrinterWorkingListener() {
			
			@Override
			public void onPrintStart() {
				mLoadLongBillProgressText.setText(R.string.print_progress);
			}
			
			@Override
			public void onPrintFinish() {
				dismiss();
			}

			@Override
			public void onPrinterError(CharSequence message) {
				new AlertDialog.Builder(getActivity())
				.setCancelable(false)
				.setMessage(getString(R.string.connect_to_bt_printer_fail) + (TextUtils.isEmpty(message) ? "" : "\n >>" + message + "<<"))
				.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
					}
				}).show();
			}
		};
	
	private class LongBillListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mPrintUtilLineLst != null ? mPrintUtilLineLst.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return mPrintUtilLineLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PrintUtilLineViewHolder holder;
			if(convertView == null){
				convertView = getActivity().getLayoutInflater().inflate(R.layout.print_line_item, parent, false);
				holder = new PrintUtilLineViewHolder();
				holder.mTvTextLeft = (TextView) convertView.findViewById(R.id.tvTextLeft);
				holder.mTvTextCenter = (TextView) convertView.findViewById(R.id.tvTextCenter);
				holder.mTvTextRight = (TextView) convertView.findViewById(R.id.tvTextRight);
				convertView.setTag(holder);
			}else{
				holder = (PrintUtilLineViewHolder) convertView.getTag();
			}
			
			PrinterUtils.PrintUtilLine line = mPrintUtilLineLst.get(position);
			holder.mTvTextLeft.setText(line.getLeftText());
			holder.mTvTextCenter.setText(line.getCenterText());
			holder.mTvTextRight.setText(line.getRightText());
			
			if(line.getPrintLineType() == PrinterUtils.PrintUtilLine.PRINT_DEFAULT){
				setLayoutWeight(holder.mTvTextLeft, 1);
				setLayoutWeight(holder.mTvTextCenter, 0);
				setLayoutWeight(holder.mTvTextRight, 0);
			}else if(line.getPrintLineType() == PrinterUtils.PrintUtilLine.PRINT_THREE_COLUMN){
				setLayoutWeight(holder.mTvTextLeft, 0.3f);
				setLayoutWeight(holder.mTvTextCenter, 1);
				setLayoutWeight(holder.mTvTextRight, 1);
			}else if(line.getPrintLineType() == PrinterUtils.PrintUtilLine.PRINT_LEFT_RIGHT){
				setLayoutWeight(holder.mTvTextLeft, 1);
				setLayoutWeight(holder.mTvTextCenter, 0);
				setLayoutWeight(holder.mTvTextRight, 1);
			}
			return convertView;
		}
		
		private void setLayoutWeight(TextView tv, float weight){
			LinearLayout.LayoutParams params 
				= new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
			tv.setLayoutParams(params);
		}
	}
	
	public static class PrintUtilLineViewHolder{
		public TextView mTvTextLeft;
		public TextView mTvTextCenter;
		public TextView mTvTextRight;
	}
}
