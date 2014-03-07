package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;

import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableName;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class SplitTableActivity extends Activity {
	private Context mContext;
	private GlobalVar mGlobalVar;
	private List<TableInfo> mTbInfoLst;
	private Spinner mSpZone;
	private ListView mLvTable;
	private ProgressDialog mProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_split_table);
		mSpZone = (Spinner) findViewById(R.id.spZone);
		mLvTable = (ListView) findViewById(R.id.lvTable);
		mContext = SplitTableActivity.this;
		mGlobalVar = new GlobalVar(mContext);
		mProgress = new ProgressDialog(mContext);
		mProgress.setCancelable(false);
		loadTable();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_move_merge_table, menu);
		View v = menu.findItem(R.id.item_confirm).getActionView();
		((Button) v.findViewById(R.id.buttonConfirmOk)).setVisibility(View.GONE);
		Button btnClose = (Button) v.findViewById(R.id.buttonConfirmCancel);
		btnClose.setText(R.string.global_btn_close);
		btnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	public List<TableInfo> getTbInfoLst(){
		return mTbInfoLst;
	}
	
	public GlobalVar getGlobalVar(){
		return mGlobalVar;
	}
	
	private void loadTable(){
		new LoadAllTableV1(mContext, mGlobalVar, new LoadAllTableV1.LoadTableProgress() {

					@Override
					public void onPre() {
						mProgress.setMessage(mContext
								.getString(R.string.load_table_progress));
						mProgress.show();
					}

					@Override
					public void onPost() {
					}

					@Override
					public void onError(String msg) {
						if (mProgress.isShowing())
							mProgress.dismiss();
						new AlertDialog.Builder(mContext)
								.setTitle(R.string.table)
								.setMessage(msg)
								.setNeutralButton(R.string.global_btn_close,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
											}
										}).show();
					}

					@Override
					public void onPost(final TableName tbName) {
						new LoadAllTableV2(mContext, mGlobalVar,
								new LoadAllTableV2.LoadTableProgress() {

									@Override
									public void onPre() {
									}

									@Override
									public void onPost() {
									}

									@Override
									public void onError(String msg) {
										if (mProgress.isShowing())
											mProgress.dismiss();
										new AlertDialog.Builder(mContext)
												.setTitle(R.string.table)
												.setMessage(msg)
												.setNeutralButton(
														R.string.global_btn_close,
														new DialogInterface.OnClickListener() {

															@Override
															public void onClick(
																	DialogInterface dialog,
																	int which) {
															}
														}).show();
									}

									@Override
									public void onPost(final List<TableInfo> tbInfoLst) {
										if (mProgress.isShowing())
											mProgress.dismiss();
										mTbInfoLst = tbInfoLst;
										TableZoneSpinnerAdapter tbZoneAdapter =
												IOrderUtility.createTableZoneAdapter(mContext, tbName); 
										mSpZone.setAdapter(tbZoneAdapter);
										mSpZone.setOnItemSelectedListener(new OnItemSelectedListener(){

											@Override
											public void onItemSelected(
													AdapterView<?> parent,
													View v, int position,
													long id) {
												final TableName.TableZone tbZone = 
														(TableName.TableZone) parent.getItemAtPosition(position);
												final List<TableInfo> newTbInfoLst = 
														IOrderUtility.filterTableNameHaveOrder(mTbInfoLst, tbZone);
												mLvTable.setAdapter(IOrderUtility.createTableNameAdapter(
												mContext, mGlobalVar, newTbInfoLst, false, true));
												mLvTable.setOnItemClickListener(new OnItemClickListener(){

													@Override
													public void onItemClick(
															AdapterView<?> parent,
															View v,
															int position, long id) {
														TableInfo tbInfo = (TableInfo) parent.getItemAtPosition(position);
														MergedTableFragment f = 
																MergedTableFragment.newInstance(tbInfo.getiTableID(),
																		IOrderUtility.formatCombindTableName(tbInfo.isbIsCombineTable(), 
																		tbInfo.getSzCombineTableName(), tbInfo.getSzTableName()), 
																		tbInfo.getiTransactionID(), tbInfo.getiComputerID());
														f.show(getFragmentManager(), "MergedTableFragment");
													}
													
												});
											}

											@Override
											public void onNothingSelected(
													AdapterView<?> arg0) {
											}
											
										});
									}
								}).execute(GlobalVar.FULL_URL);
					}
				}).execute(GlobalVar.FULL_URL);
	}
	
	public void onSplitTable(final int transId, final int compId, final List<TableInfo> selTbLst){
		if(selTbLst.size() > 0){
			new AlertDialog.Builder(mContext)
			.setTitle(R.string.split_table)
			.setMessage(R.string.confirm_split_table)
			.setNegativeButton(R.string.global_btn_no, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).setPositiveButton(R.string.global_btn_yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String tbIds = "";
					for(int i = 0; i < selTbLst.size(); i++){
						TableInfo tbInfo = selTbLst.get(i);
						tbIds += tbInfo.getiTableID();
						if(i < selTbLst.size() - 1)
							tbIds += ",";
					}
					new TableUtils.SplitMultiTableService(mContext, mGlobalVar, transId, compId, tbIds, 
							"", "Split Table From Android", new ProgressListener() {
								
								@Override
								public void onPre() {
									mProgress.setMessage(mContext.getString(R.string.loading_progress));
									mProgress.show();
								}
								
								@Override
								public void onPost() {
									if(mProgress.isShowing())
										mProgress.dismiss();
									new AlertDialog.Builder(mContext)
									.setTitle(R.string.split_table)
									.setMessage(R.string.split_table_success)
									.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											loadTable();
										}
									}).show();
								}
								
								@Override
								public void onError(String msg) {
									if(mProgress.isShowing())
										mProgress.dismiss();
									new AlertDialog.Builder(mContext)
									.setTitle(R.string.split_table)
									.setMessage(msg)
									.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
										}
									}).show();
								}
							}).execute(GlobalVar.FULL_URL);
				}
			}).show();
		}
	}
	
	public static class MergedTableFragment extends DialogFragment{
		private MergedTableAdapter mMergedTableAdapter;
		private int mCurrTableId;
		private String mTbName;
		private List<TableInfo> mSelTbLst; 
		private int mTransactionId;
		private int mComputerId;
		
		public static MergedTableFragment newInstance(int currTableId, String tbName, int transId, int compId){
			MergedTableFragment f = new MergedTableFragment();
			Bundle b = new Bundle();
			b.putInt("currTableId", currTableId);
			b.putString("tableName", tbName);
			b.putInt("transactionId", transId);
			b.putInt("computerId", compId);
			f.setArguments(b);
			return f;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mCurrTableId = getArguments().getInt("currTableId");
			mTbName = getArguments().getString("tableName");
			mTransactionId = getArguments().getInt("transactionId");
			mComputerId = getArguments().getInt("computerId");
			mSelTbLst = new ArrayList<TableInfo>();
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = (LayoutInflater) 
					getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View v = inflater.inflate(R.layout.merged_table_layout, null);
			final TextView tvTitle = (TextView) v.findViewById(R.id.textView1);
			final ImageButton btnClose = (ImageButton) v.findViewById(R.id.imageButton1);
			final ListView lvTable = (ListView) v.findViewById(R.id.lvMergedTable);
			tvTitle.setText(mTbName);
			btnClose.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					getDialog().dismiss();
				}
				
			});
			lvTable.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					TableInfo tbInfo = (TableInfo) parent.getItemAtPosition(position);
					if(tbInfo.isChecked()){
						tbInfo.setChecked(false);
						mSelTbLst.remove(tbInfo);
					}else{
						tbInfo.setChecked(true);
						mSelTbLst.add(tbInfo);
					}
					mMergedTableAdapter.notifyDataSetChanged();
				}
				
			});
			
			final GlobalVar globalVar = ((SplitTableActivity) getActivity()).getGlobalVar();
			final List<TableInfo> tbInfoLst = ((SplitTableActivity) getActivity()).getTbInfoLst();
			final ProgressDialog progress = new ProgressDialog(getActivity());
			progress.setCancelable(false);
			new TableUtils.LoadMergedTable(getActivity(), globalVar, 
					mTransactionId, mComputerId, new TableUtils.LoadMergeTableProgressListener() {
						
						@Override
						public void onPre() {
							progress.setMessage(getActivity().getString(R.string.load_table_progress));
							progress.show();
						}
						
						@Override
						public void onPost() {
						}
						
						@Override
						public void onError(String msg) {
							if(progress.isShowing())
								progress.dismiss();
							
							new AlertDialog.Builder(getActivity())
							.setTitle(R.string.table)
							.setMessage(msg)
							.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
								}
							}).show();
						}
						
						@Override
						public void onPost(int[] tbIds) {
							if(progress.isShowing())
								progress.dismiss();
							
							List<TableInfo> mergedTbLst = new ArrayList<TableInfo>();
							if(tbIds.length > 0){
								for(int tbId : tbIds){
									for(TableInfo tbInfo : tbInfoLst){
										if(tbId == tbInfo.getiTableID() && tbId != mCurrTableId){
											mergedTbLst.add(tbInfo);
										}
									}
								}
								mMergedTableAdapter = new MergedTableAdapter(mergedTbLst);  
								lvTable.setAdapter(mMergedTableAdapter);
								if(mergedTbLst.size() == 0){
									new AlertDialog.Builder(getActivity())
									.setTitle(R.string.table)
									.setMessage(R.string.not_found_table)
									.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											
										}
									}).show();
								}
							}else{
								new AlertDialog.Builder(getActivity())
								.setTitle(R.string.table)
								.setMessage(R.string.not_found_table)
								.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										
									}
								}).show();
							}
						}
					}).execute(GlobalVar.FULL_URL);
			return new AlertDialog.Builder(getActivity())
				.setView(v)
				.setNeutralButton(R.string.split_table, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						((SplitTableActivity) getActivity()).onSplitTable(mTransactionId, mComputerId, mSelTbLst);
					}
				})
				.create();
		}
		
		private class MergedTableAdapter extends BaseAdapter{
			private List<TableInfo> mTbInfoLst;
			private LayoutInflater mInflater;
			
			public MergedTableAdapter(List<TableInfo> tbInfoLst){
				mTbInfoLst = tbInfoLst;
				mInflater = (LayoutInflater)
						getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
			
			@Override
			public int getCount() {
				return mTbInfoLst == null ? 0 : mTbInfoLst.size();
			}

			@Override
			public TableInfo getItem(int position) {
				return mTbInfoLst.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder;
				if(convertView == null){
					convertView = mInflater.inflate(R.layout.list_selected_template, null);
					holder = new ViewHolder();
					holder.tvNo = (TextView) convertView.findViewById(R.id.tvNo);
					holder.chkTv = (CheckedTextView) convertView.findViewById(R.id.checkedTextView1);
					convertView.setTag(holder);
				}else{
					holder = (ViewHolder) convertView.getTag();
				}
				TableInfo tbInfo = mTbInfoLst.get(position);;
				holder.tvNo.setVisibility(View.GONE);
				holder.chkTv.setText(tbInfo.getSzTableName());
				holder.chkTv.setChecked(tbInfo.isChecked());
				return convertView;
			}
			
			private class ViewHolder{
				TextView tvNo;
				CheckedTextView chkTv;
			}
		}
	}
}
