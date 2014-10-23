package com.syn.iorder;

import java.text.NumberFormat;
import java.util.List;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class BuffetQtyDialogFragment extends DialogFragment{
	
	public static final String TAG = BuffetQtyDialogFragment.class.getSimpleName();
	
	private GlobalVar mGlobalVar;
	
	private BuffetListAdapter mBuffetAdapter;
	private List<BuffetMenuLoader.BuffetOrder> mBuffetLst;
	private int mStaffId;
	private int mTableId;
	private String mTableName;

	public BuffetQtyDialogFragment(int staffId, int tableId, String tableName,
			List<BuffetMenuLoader.BuffetOrder> buffetLst){
		mStaffId = staffId;
		mTableId = tableId;
		mTableName = tableName;
		mBuffetLst = buffetLst;
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
		mBuffetAdapter = new BuffetListAdapter();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.buffet_qty, null);
		ListView lvBuffet = (ListView) content.findViewById(R.id.lvBuffet);
		lvBuffet.setAdapter(mBuffetAdapter);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(mTableName);
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
				sendModifyBuffetMenu();
			}
		});
		return d;
	}
	
	private void sendModifyBuffetMenu(){
		Gson gson = new Gson();
		String jsonBuffetOrder = gson.toJson(mBuffetLst);
		Log.i(TAG, jsonBuffetOrder);
		new BuffetMenuModQty(getActivity(), mGlobalVar, mStaffId, mTableId, jsonBuffetOrder, new BuffetMenuModQty.BuffetMenuModQtyListener() {
			
			@Override
			public void onPost(String msg) {
				Log.i(TAG, msg);
				new AlertDialog.Builder(getActivity())
				.setMessage(msg)
				.setCancelable(false)
				.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						getDialog().dismiss();
					}
				}).show();
			}
			
			@Override
			public void onError(String msg) {
				Log.e(TAG, msg);
				new AlertDialog.Builder(getActivity())
				.setMessage(msg)
				.setCancelable(false)
				.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						getDialog().dismiss();
					}
				}).show();
				
			}
		}).execute(GlobalVar.FULL_URL);	
	}
	
	private class BuffetListAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater = getActivity().getLayoutInflater();
		
		@Override
		public int getCount() {
			return mBuffetLst != null ? mBuffetLst.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return mBuffetLst.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			ViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.buffet_mod_item, container, false);
				holder = new ViewHolder();
				holder.tvBuffetName = (TextView) convertView.findViewById(R.id.tvBuffetName);
				holder.tvBuffetQty = (TextView) convertView.findViewById(R.id.tvBuffetQty);
				holder.btnMinus = (Button) convertView.findViewById(R.id.btnMinus);
				holder.btnPlus = (Button) convertView.findViewById(R.id.btnPlus);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			final BuffetMenuLoader.BuffetOrder buffet = mBuffetLst.get(position);
			holder.tvBuffetName.setText(buffet.getSzItemName());
			holder.tvBuffetQty.setText(NumberFormat.getInstance().format(buffet.getfItemQty()));
			holder.btnMinus.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					double qty = buffet.getfItemQty();
					if(qty > 1){
						buffet.setfItemQty(--qty);
						notifyDataSetChanged();
					}
				}
				
			});
			holder.btnPlus.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					double qty = buffet.getfItemQty();
					buffet.setfItemQty(++qty);
					notifyDataSetChanged();
				}
				
			});
			return convertView;
		}
		
		private class ViewHolder{
			TextView tvBuffetName;
			TextView tvBuffetQty;
			Button btnMinus;
			Button btnPlus;
		}
	}
}
