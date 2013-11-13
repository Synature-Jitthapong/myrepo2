package com.syn.iorder;

import java.util.List;
import syn.pos.data.model.TableInfo.TableName;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.BaseAdapter;

public class SelectTableListAdapter extends BaseAdapter {
	private List<syn.pos.data.model.TableInfo.TableName> tbNameList;
	private Context context;
	private LayoutInflater inflater;
	private GlobalVar globalVar;
	private boolean showCapacity = true;

	public SelectTableListAdapter(Context context, GlobalVar globalVar,
			List<syn.pos.data.model.TableInfo.TableName> tbNameList) {
		this.globalVar = globalVar;
		this.tbNameList = tbNameList;
		this.context = context;
		inflater = LayoutInflater.from(context);
	}
	
	public SelectTableListAdapter(Context context, GlobalVar globalVar,
			List<syn.pos.data.model.TableInfo.TableName> tbNameList, boolean showCapacity) {
		this.globalVar = globalVar;
		this.tbNameList = tbNameList;
		this.context = context;
		inflater = LayoutInflater.from(context);
		this.showCapacity = showCapacity;
	}

	@Override
	public int getCount() {
		return tbNameList.size();
	}

	@Override
	public TableName getItem(int position) {
		return tbNameList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public static class SelectTableViewHolder{
		int lastPosition;
		int customerQty;
		String tableName;
		ImageView imgStatus;
		Button btnTbInfo;
		TextView tvStatus;
		TextView tvTableName;
		TextView tvTableCapacity;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final syn.pos.data.model.TableInfo.TableName tbName = tbNameList.get(position);
		final SelectTableViewHolder holder;
		
		if (convertView == null){
			convertView = inflater
					.inflate(R.layout.select_table_template, null);
	
			holder = new SelectTableViewHolder();
			
			holder.imgStatus = (ImageView) convertView.findViewById(R.id.imageViewStatus);
			holder.tvTableName = (TextView) convertView.findViewById(R.id.selecttable_tvname);
			holder.tvTableCapacity = (TextView) convertView.findViewById(R.id.selecttable_tvcapacity);
			holder.btnTbInfo = (Button) convertView.findViewById(R.id.button1);
			convertView.setTag(holder);
			
		}
		else{
			holder = (SelectTableViewHolder)convertView.getTag();
		}
		
		holder.lastPosition = position;
		holder.customerQty = tbName.getCapacity();
		holder.tableName = tbName.getTableName();
		
		if(showCapacity){
			holder.tvTableCapacity.setVisibility(View.VISIBLE);
		}else{
			holder.tvTableCapacity.setVisibility(View.INVISIBLE);
		}
		
		holder.tvTableCapacity.setText(String.valueOf(tbName.getCapacity()));
		if (tbName.getSTATUS() != 0){
			holder.tvTableName.setTextColor(Color.RED);
			holder.imgStatus.setImageResource(R.drawable.hasorder);
			
			holder.btnTbInfo.setVisibility(View.VISIBLE);
			
			if(tbName.getTableID() == 0)
				holder.btnTbInfo.setVisibility(View.GONE);
			
			// already checkbill
			if(tbName.getSTATUS() == 3){
				// show dolla
				holder.imgStatus.setImageResource(R.drawable.dollar);
				holder.btnTbInfo.setVisibility(View.INVISIBLE);
			}else{
				holder.btnTbInfo.setVisibility(View.VISIBLE);
			}

			holder.btnTbInfo.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					final Dialog detailDialog = new Dialog(context, R.style.CustomDialogBottomRadius);
					LayoutInflater inflater = LayoutInflater.from(context);
					View orderView = inflater.inflate(R.layout.order_list_layout, null);
					ListView lvOrder = (ListView) orderView.findViewById(R.id.listViewOrder);
					TextView tvTableName = (TextView) orderView.findViewById(R.id.textViewOrderListTitle);
					TextView tvSumText = (TextView) orderView.findViewById(R.id.textViewSumText);
					TextView tvSumPrice = (TextView) orderView.findViewById(R.id.textViewSumPrice);
					ImageButton btnClose = (ImageButton) orderView.findViewById(R.id.imageButtonCloseOrderDialog);
					
					ProgressBar progress = (ProgressBar) orderView.findViewById(R.id.progressBarOrderOfTable);
					//tvTableName.setText()
					tvTableName.setText(tbName.getTableName());
					
					//new CurrentOrderFromTableTask(context, globalVar, tbName.getTableID(), lvOrder).execute(globalVar.FULL_URL);
					new LoadBillDetailTask(context, globalVar, tbName.getTableID(), 
							lvOrder, tvSumText, tvSumPrice, progress).execute(globalVar.FULL_URL);
					detailDialog.setContentView(orderView);
					detailDialog.getWindow().setGravity(Gravity.TOP);
					detailDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
							WindowManager.LayoutParams.WRAP_CONTENT);
					detailDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);

					btnClose.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							detailDialog.dismiss();
						}
						
					});
					
					detailDialog.show();
				}
				
			});
		}
		else{
//			holder.tvStatus.setText(R.string.selecttable_tvtable_empty);
//			holder.tvStatus.setTextColor(Color.GREEN);
			holder.tvTableName.setTextColor(Color.GREEN);
			holder.imgStatus.setImageResource(R.drawable.user_green);
			if(tbName.getTableID() == 0)
				holder.imgStatus.setVisibility(View.GONE);
			else
				holder.imgStatus.setVisibility(View.VISIBLE);
			
			holder.btnTbInfo.setVisibility(View.INVISIBLE);
		}
		holder.tvTableName.setText(tbName.getTableName());
		return convertView;
	}

}
