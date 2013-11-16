package com.syn.iorder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import syn.pos.data.dao.TransactionComment;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.POSData_OrderTransInfo;
import syn.pos.data.model.ProductGroups;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.WebServiceResult;
import syn.pos.data.model.TableInfo.TableName;
import syn.pos.data.model.TableInfo.TableZone;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class CommentTransactionActivity extends Activity implements OnClickListener, 
	OnItemSelectedListener, OnItemClickListener{

	private TransactionComment mTransComm;
	private List<ProductGroups.CommentTransDept> mCommDeptLst;
	private List<ProductGroups.CommentTransItem> mCommItemLst;
	private List<ProductGroups.CommentTransItem> mCommSelectedLst;
	private CommentDeptAdapter mCommDeptAdapter;
	private CommentItemAdapter mCommItemAdapter;
	private CommentSelectedAdapter mCommSelectedAdapter;
	
	private GlobalVar mGlobalVar;
	private int mSelectedTableId;
	private String mSelectedTableName;
	private TableInfo mTableInfo;
	private ListView mLvTable;
	private Spinner mSpTableZone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment_transaction);
		
		mLvTable = (ListView) findViewById(R.id.lvTable);
		mSpTableZone = (Spinner) findViewById(R.id.spTableZone);

		mTransComm = new TransactionComment(this);
		mGlobalVar = new GlobalVar(CommentTransactionActivity.this);
		mLvTable.setOnItemClickListener(this);
		mSpTableZone.setOnItemSelectedListener(this);
		
		new LoadTableTask(CommentTransactionActivity.this, mGlobalVar).execute(GlobalVar.FULL_URL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.comment_transaction, menu);
		MenuItem item = menu.findItem(R.id.item_close);
		Button btnClose = (Button) item.getActionView().findViewById(R.id.buttonClose);
		btnClose.setOnClickListener(this);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.buttonClose:
			finish();
			break;
		}
	}

	private class LoadTableTask extends WebServiceTask{
		private static final String webMethod = "WSmPOS_JSON_LoadAllTableData";
		
		public LoadTableTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);
		}

		@Override
		protected void onPostExecute(String result) {
			if(progress.isShowing())
				progress.dismiss();
			
			GsonDeserialze gdz = new GsonDeserialze();

			try {
				mTableInfo = gdz.deserializeTableInfoJSON(result);
				mSpTableZone.setAdapter(IOrderUtility.createTableZoneAdapter(context, mTableInfo));
			} catch (Exception e) {
				IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, result, 0);
			}
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.loading_progress);
			progress.setMessage(tvProgress.getText().toString());
			progress.show();
		}
		
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position,
			long id) {
		switch(parent.getId()){
		case R.id.spTableZone:
			TableInfo.TableZone tbZone = (TableZone) parent.getItemAtPosition(position);
			final List<TableInfo.TableName> tbNameLst = IOrderUtility.filterTableNameHaveOrder(mTableInfo, tbZone);
	
			mLvTable.setAdapter(IOrderUtility.createTableNameAdapter(CommentTransactionActivity.this, mGlobalVar, tbNameLst));
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void popupComment(){
		mCommDeptLst = mTransComm.listAllCommentTransDept();
//		ProductGroups.CommentTransDept cd = new ProductGroups.CommentTransDept();
//		cd.setCommentDeptID(0);
//		cd.setCommentDeptName("All");
//		mCommDeptLst.add(0, cd);
		
		mCommItemLst = mTransComm.listAllCommentTransItem();
		mCommSelectedLst = mTransComm.listTransComment(mSelectedTableId);
		
		mCommDeptAdapter = new CommentDeptAdapter();
		mCommItemAdapter = new CommentItemAdapter();
		mCommSelectedAdapter = new CommentSelectedAdapter();
		
		LayoutInflater inflater = (LayoutInflater)
				this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.comment_table_layout, null);
		Spinner spCommDept = (Spinner) v.findViewById(R.id.spCommDept);
		ListView lvCommItem = (ListView) v.findViewById(R.id.lvCommItem);
		ListView lvSelectedComm = (ListView) v.findViewById(R.id.lvSelectedComm);
		Button btnCancel = (Button) v.findViewById(R.id.btnCancel);
		Button btnOk = (Button) v.findViewById(R.id.btnOk);
		Button btnClose = (Button) v.findViewById(R.id.btnClose);
		((TextView) v.findViewById(R.id.textView1)).setText(this.getString(R.string.comment_trans) + ":" + mSelectedTableName);
		
		spCommDept.setAdapter(mCommDeptAdapter);
		lvCommItem.setAdapter(mCommItemAdapter);
		lvSelectedComm.setAdapter(mCommSelectedAdapter);
		
		spCommDept.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				ProductGroups.CommentTransDept cd = (ProductGroups.CommentTransDept)
						parent.getItemAtPosition(position);
					
					mCommItemLst = mTransComm.listCommentTransItem(cd.getCommentDeptID());
					mCommItemAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		lvCommItem.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				ProductGroups.CommentTransItem ct = (ProductGroups.CommentTransItem)
						parent.getItemAtPosition(position);
				
				CheckBox chk = (CheckBox) v.findViewById(R.id.checkBox1);
				if(chk.isChecked()){
					mTransComm.deleteTransComment(mSelectedTableId, ct.getCommentItemID());
					chk.setChecked(false);
				}else{
					mTransComm.insertTransComment(mSelectedTableId, ct.getCommentItemID());
					chk.setChecked(true);
				}
				
				mCommSelectedLst = mTransComm.listTransComment(mSelectedTableId);
				mCommSelectedAdapter.notifyDataSetChanged();
			}
			
		});
		
//		lvSelectedComm.setOnItemClickListener(new OnItemClickListener(){
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View v, int position,
//					long id) {
//				ProductGroups.CommentTransItem ct = (ProductGroups.CommentTransItem)
//						parent.getItemAtPosition(position);
//				mTransComm.deleteTransComment(mSelectedTableId, ct.getCommentItemID());
//				mCommSelectedLst = mTransComm.listAllCommentTransItem();
//				mCommItemAdapter.notifyDataSetChanged();
//			}
//			
//		});
		
		final Dialog d = new Dialog(this, R.style.CustomDialog);
		d.setContentView(v);
		d.getWindow().setBackgroundDrawableResource(R.color.grey_light);
		d.show();
		
		btnOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new SendCommentTransTask().execute(mGlobalVar.FULL_URL);
				d.dismiss();
			}
			
		});
		
		btnCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mTransComm.deleteAllTransComment(mSelectedTableId);
				d.dismiss();
			}
			
		});
		
		btnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mTransComm.deleteAllTransComment(mSelectedTableId);
				d.dismiss();
			}
			
		});
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		switch(parent.getId()){
		case R.id.lvTable:
			TableName tbName = (TableName) parent.getItemAtPosition(position);			
			mSelectedTableId = tbName.getTableID();
			mSelectedTableName = tbName.getTableName();
			
			new LoadCurrentCommentTransTask().execute(mGlobalVar.FULL_URL);
			break;
		}
	}

	private class CommentDeptAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mCommDeptLst.size();
		}

		@Override
		public ProductGroups.CommentTransDept getItem(int position) {
			return mCommDeptLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ProductGroups.CommentTransDept dept = 
					mCommDeptLst.get(position);
			
			LayoutInflater inflater = (LayoutInflater)
					CommentTransactionActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			convertView = inflater.inflate(R.layout.spinner_item, null);
			TextView tv = (TextView) convertView;
			tv.setText(dept.getCommentDeptName());
			
			return convertView;
		}
		
	}
	
	private class CommentSelectedAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mCommSelectedLst.size();
		}

		@Override
		public ProductGroups.CommentTransItem getItem(int position) {
			return mCommSelectedLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ProductGroups.CommentTransItem ci = 
					mCommSelectedLst.get(position);
			
			LayoutInflater inflater = (LayoutInflater)
					CommentTransactionActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.comment_selected_template, null);
			TextView tv = (TextView) convertView.findViewById(R.id.textView1);
			((TextView) convertView.findViewById(R.id.textView2)).setVisibility(View.GONE);
			
			tv.setText(ci.getCommentItemName());
			return convertView;
		}
		
	}
	
	private class CommentItemAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mCommItemLst.size();
		}

		@Override
		public ProductGroups.CommentTransItem getItem(int position) {
			return mCommItemLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ProductGroups.CommentTransItem transItem = 
					mCommItemLst.get(position);
			
			LayoutInflater inflater = (LayoutInflater) 
					CommentTransactionActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.list_select_template, null);
				holder = new ViewHolder();
				holder.chk = (CheckBox) convertView.findViewById(R.id.checkBox1);
				holder.tv = (TextView) convertView.findViewById(R.id.textView1);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tv.setText(transItem.getCommentItemName());
			if(mTransComm.chkTransComment(mSelectedTableId, transItem.getCommentItemID()))
				holder.chk.setChecked(true);
			else
				holder.chk.setChecked(false);
			
			return convertView;
		}
		
		private class ViewHolder{
			private CheckBox chk;
			private TextView tv;
		}
	}
	
	public class LoadCurrentCommentTransTask extends WebServiceTask{

		public LoadCurrentCommentTransTask() {
			super(CommentTransactionActivity.this, mGlobalVar, "WSiOrder_JSON_LoadCommentTransaction");
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(mSelectedTableId);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			progress.setMessage(context.getString(R.string.wait_progress));
			progress.show();
		}

		@Override
		protected void onPostExecute(String result) {
			if(progress.isShowing())
				progress.dismiss();
			
			Gson gson = new Gson();
			Type type = new TypeToken<List<ProductGroups.CommentTransItem>>() {}.getType();
			
			try {
				List<ProductGroups.CommentTransItem> commLst = 
						(List<ProductGroups.CommentTransItem>) gson.fromJson(result, type);
				
				for(ProductGroups.CommentTransItem comm : commLst){
					mTransComm.insertTransComment(mSelectedTableId, comm.getCommentItemID());
				}
				
			} catch (JsonSyntaxException e) {
				new AlertDialog.Builder(context)
				.setMessage(e.getMessage())
				.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				})
				.show();
			} catch (SQLException e) {
				new AlertDialog.Builder(context)
				.setMessage(e.getMessage())
				.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				})
				.show();
			}
			

			popupComment();
		}
	}
	
	public class SendCommentTransTask extends WebServiceTask{

		public SendCommentTransTask() {
			super(CommentTransactionActivity.this, mGlobalVar, "WSiOrder_JSON_SendCommentTransaction");
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(globalVar.COMPUTER_DATA.getComputerID());
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(globalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(mSelectedTableId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			List<POSData_OrderTransInfo.POSData_CommentInfo> commentLst =
					new ArrayList<POSData_OrderTransInfo.POSData_CommentInfo>();
			for(ProductGroups.CommentTransItem commItem : mCommSelectedLst){
				POSData_OrderTransInfo.POSData_CommentInfo comm = 
						new POSData_OrderTransInfo.POSData_CommentInfo();
				comm.setiCommentID(commItem.getCommentItemID());
				commentLst.add(comm);
			}
			
			Gson gson = new Gson();
			property = new PropertyInfo();
			property.setName("szJSon_OrderCmtTransData");
			property.setValue(gson.toJson(commentLst));
			property.setType(String.class);
			soapRequest.addProperty(property);
			
			Log.d("Send Data", gson.toJson(commentLst));
		}

		@Override
		protected void onPreExecute() {
			progress.setMessage(context.getText(R.string.wait_progress));
			progress.show();
		}

		@Override
		protected void onPostExecute(String result) {
			if(progress.isShowing())
				progress.dismiss();
			GsonDeserialze gdz = new GsonDeserialze();
			
			try {
				WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);

				if (wsResult.getiResultID() == 0) {
					
					// clear transcomment
					mTransComm.deleteAllTransComment(mSelectedTableId);
					
					new AlertDialog.Builder(CommentTransactionActivity.this)
					.setMessage(R.string.comment_table_success)
					.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						}
					})
					.show();
				}else{
					new AlertDialog.Builder(CommentTransactionActivity.this)
					.setMessage(wsResult.getSzResultData()
							.equals("") ? result : wsResult.getSzResultData())
					.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						}
					})
					.show();
				}
			} catch (Exception e) {
				new AlertDialog.Builder(CommentTransactionActivity.this)
				.setMessage(result)
				.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.show();
			}
			
		}
		
	}
}
