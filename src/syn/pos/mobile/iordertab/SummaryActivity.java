package syn.pos.mobile.iordertab;
/*
 * set member 
 */
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.dao.QuestionGroups;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.ProductGroups;
import syn.pos.data.model.ProductGroups.QuestionAnswerData;
import syn.pos.data.model.SummaryTransaction;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableInfo.TableName;
import syn.pos.data.model.TableInfo.TableZone;
import syn.pos.data.model.WebServiceResult;
import syn.pos.mobile.util.Log;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class SummaryActivity extends Activity {
	private Spinner spinnerTableZone;
	private ListView tableNameListView;
	private ListView lvOrder;
	private TextView tvBillHeader;
	private TextView tvSummary;
	private TextView tvTableName;
	private TableLayout tbSummary;
	
	private Button btnClose;
	private Button btnCheckbill;
	private Button btnSetmember;
	private Button btnEditQuestion;
	private GlobalVar globalVar;
	private Context CONTEXT;
	
	private int CURR_TRANSACTION_ID;
	private int CURR_COMPUTER_ID;
	private int TABLE_ID;
	private int mCustomerQty = 1;
	
	private SummaryTransaction SUMMARY_TRANS;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_summary);
		
		CONTEXT = this;
		
		globalVar = new GlobalVar(this);
		initComponent();
	}
	
	
	@Override
	protected void onResume() {
		if(TABLE_ID != 0)
			new ShowSummaryBillTask(SummaryActivity.this, 
					globalVar).execute(globalVar.FULL_URL);
		
		super.onResume();
	}


	private void disableButton(){
		btnCheckbill.setEnabled(false);
		btnSetmember.setEnabled(false);
		btnEditQuestion.setEnabled(false);
	}
	
	private void enableButton(){
		btnCheckbill.setEnabled(true);
		btnSetmember.setEnabled(true);
		btnEditQuestion.setEnabled(true);
	}
	
	private void initComponent(){
		tvSummary = (TextView) findViewById(R.id.tvBillSummary);
		spinnerTableZone = (Spinner) findViewById(R.id.spTableZone);
		tableNameListView = (ListView) findViewById(R.id.lvTable);
		btnCheckbill = (Button) findViewById(R.id.btnCallCheckbill);
		btnSetmember = (Button) findViewById(R.id.btnSetMember);
		tvBillHeader = (TextView) findViewById(R.id.tvBillHeader);
		lvOrder = (ListView) findViewById(R.id.lvOrder);
		btnEditQuestion = (Button) findViewById(R.id.btnEditCustomer);
		tbSummary = (TableLayout) findViewById(R.id.tbSummary);
		
		tvBillHeader.setText(globalVar.SHOP_DATA.getShopName());
		
		if(TABLE_ID==0){
			disableButton();
		}
		else{
			enableButton();
		}
		
		/* set text button edit question or customer qty.
		 * and set event by feature
		 */
		if(!GlobalVar.isEnableTableQuestion){
			btnEditQuestion.setText(R.string.edit_customer);
			btnEditQuestion.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					LayoutInflater inflater = LayoutInflater.from(SummaryActivity.this);
					View v = inflater.inflate(R.layout.edit_customer_layout, null);
					final NumberPicker picker = (NumberPicker) v.findViewById(R.id.numberPicker1);
					picker.setValue(mCustomerQty);
					
					new AlertDialog.Builder(SummaryActivity.this)
					.setView(v)
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					})
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mCustomerQty = picker.getValue();
							
							new UpdateCustomerTask(SummaryActivity.this, globalVar, CURR_TRANSACTION_ID,
									CURR_COMPUTER_ID, mCustomerQty, new WebServiceTaskState(){

										@Override
										public void onSuccess() {
											new ShowSummaryBillTask(SummaryActivity.this, globalVar).execute(GlobalVar.FULL_URL);
										}

										@Override
										public void onNotSuccess() {
										}

										@Override
										public void onProgress() {
											// TODO Auto-generated method stub
											
										}

										@Override
										public void onSuccess(int arg) {
											// TODO Auto-generated method stub
											
										}
								
							}).execute(globalVar.FULL_URL);
						}
					})
					.show();

				}
				
			});
			
		}
		else{
			btnEditQuestion.setText(R.string.edit_question);
//			btnEditQuestion.setOnClickListener(new OnClickListener(){
//
//				@Override
//				public void onClick(View v) {
//					new CurrentAnswerQuestionTask(SummaryActivity.this, globalVar, 
//							TABLE_ID, new CurrentAnswerQuestionTask.ICurrentAnswerListener() {
//								
//								@Override
//								public void listQuestionAnswer(List<QuestionAnswerData> questionLst) {
//									//add answer to temp 
//									final QuestionGroups qsGroup = new QuestionGroups(SummaryActivity.this);
//									qsGroup.insertCurrentAnswerQuestion(questionLst);
//									
//									// popup
//									LayoutInflater inflater = LayoutInflater.from(SummaryActivity.this);
//									View questView = inflater.inflate(R.layout.question_list_layout, null);
//									TextView tvQestionTitle = (TextView) questView.findViewById(R.id.textView1);
//									tvQestionTitle.setText(tvTableName.getText());
//									Button btnOk = (Button) questView.findViewById(R.id.button1);
//									Button btnCancel = (Button) questView.findViewById(R.id.button2);
//									final TextView tvRequire = (TextView) questView.findViewById(R.id.textView2);
//									final ListView lvQuestion = (ListView) questView.findViewById(R.id.listView1);
//									lvQuestion.setEnabled(false);
//									
//									// question adapter
//									final List<ProductGroups.QuestionDetail> qsDetailLst = qsGroup.listCurrentQuestionDetail();
//									
//									if(qsDetailLst != null && qsDetailLst.size() > 0){
//										final SelectTableQuestionAdapter qsAdapter = 
//												new SelectTableQuestionAdapter(SummaryActivity.this, globalVar, qsDetailLst);
//										lvQuestion.setAdapter(qsAdapter);
//										
//										final Dialog dialog = new Dialog(SummaryActivity.this, R.style.CustomDialog);
//										dialog.setContentView(questView);
//										dialog.setCancelable(false);
//										dialog.getWindow().setLayout(
//												android.view.ViewGroup.LayoutParams.MATCH_PARENT,
//												android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
//										dialog.show();
//										
//										btnOk.setOnClickListener(new OnClickListener(){
//			
//											@Override
//											public void onClick(View v) {
//												boolean isChoiceQuestion = qsGroup.checkChoiceTypeQuestion(4);
//												boolean requireSelect = true;
//												// check require question
//												for(int i = 0; i < qsDetailLst.size(); i++){
//													ProductGroups.QuestionDetail qsDetail = 
//															qsDetailLst.get(i);
//													
//													if(qsDetail.getIsRequired() == 1){
//														// check selected
//														if(!qsGroup.checkAddedQuestion(qsDetail.getQuestionID())){
//															if(qsDetail.getQuestionTypeID() == 2){
//																if(qsGroup.getTotalAnswerQty() > 0){
//																	if(!isChoiceQuestion){
//																		requireSelect = false;
//																		tvRequire.setVisibility(View.GONE);
//																	}
//																}else{
//																	requireSelect = true;
//																	tvRequire.setVisibility(View.VISIBLE);
//																	qsDetail.setRequireSelect(requireSelect);
//																	qsAdapter.notifyDataSetChanged();
//																	break;
//																}
//															}else{
//																requireSelect = true;
//																tvRequire.setVisibility(View.VISIBLE);
//																qsDetail.setRequireSelect(requireSelect);
//																qsAdapter.notifyDataSetChanged();
//																break;
//															}
//														}else{
//															requireSelect = false;
//															tvRequire.setVisibility(View.GONE);
//															
//															qsDetail.setRequireSelect(requireSelect);
//															qsAdapter.notifyDataSetChanged();
//														}
//													}
//												}
//												
//												if(!requireSelect){
//													dialog.dismiss();
//													
//													QuestionGroups qsGroup = new QuestionGroups(SummaryActivity.this);
//													List<ProductGroups.QuestionAnswerData> selectedAnswerLst = 
//															qsGroup.listAnswerQuestion();
//													
//													// send answer question
//													new QuestionTask(SummaryActivity.this, globalVar, TABLE_ID, 
//															selectedAnswerLst, new WebServiceStateListener(){
//								
//																@Override
//																public void onSuccess() {
//																	IOrderUtility.alertDialog(SummaryActivity.this, 
//																			R.string.edit_question_title, R.string.edit_question_success, 0);
//																}
//								
//																@Override
//																public void onNotSuccess() {
//																	// TODO Auto-generated method stub
//																	
//																}
//													}).execute(GlobalVar.FULL_URL);
//												}
//											}
//										});
//										
//										btnCancel.setOnClickListener(new OnClickListener(){
//
//											@Override
//											public void onClick(View v) {
//												dialog.dismiss();
//											}
//											
//										});
//									}
//								}
//							}).execute(GlobalVar.FULL_URL);
//					}
//			});
		}
		
//		btnPrint.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				new PrintTask(CONTEXT, globalVar).execute(GlobalVar.FULL_URL);		
//			}
//		});
		
		btnCheckbill.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				checkBill();
			}
		});
		
//		btnClearMember.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				final CustomDialog cusDialog =
//						new CustomDialog(SummaryActivity.this, R.style.CustomDialog);
//				cusDialog.title.setVisibility(View.VISIBLE);
//				cusDialog.title.setText(R.string.clear_member_title);
//				cusDialog.message.setText(R.string.cf_clear_member);
//				cusDialog.btnCancel.setOnClickListener(new OnClickListener(){
//
//					@Override
//					public void onClick(View v) {
//						cusDialog.dismiss();
//					}
//					
//				});
//				cusDialog.btnOk.setOnClickListener(new OnClickListener(){
//
//					@Override
//					public void onClick(View v) {
//						cusDialog.dismiss();
//						new ClearMemberTask(SummaryActivity.this, globalVar, 
//								CURR_TRANSACTION_ID, CURR_COMPUTER_ID).execute(GlobalVar.FULL_URL);	
//					}
//					
//				});
//				cusDialog.show();
//			}
//		});
		
		btnSetmember.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SummaryActivity.this, SearchMemberActivity.class);
				intent.putExtra("TO_TRANSACTION_ID", CURR_TRANSACTION_ID);
				intent.putExtra("TO_COMPUTER_ID", CURR_COMPUTER_ID);
				SummaryActivity.this.startActivity(intent);	
			}
		});
		 new LoadTableTask(CONTEXT, globalVar).execute(GlobalVar.FULL_URL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_summary_bill, menu);
		View v = menu.findItem(R.id.item_close).getActionView();

		btnClose = (Button) v.findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				SummaryActivity.this.finish();
			}
		});
		return true;
	}

	private void checkBill(){
		if(TABLE_ID != 0)
			new CheckBillTask(CONTEXT, globalVar).execute(GlobalVar.FULL_URL);
		else{
			new AlertDialog.Builder(CONTEXT)
			.setMessage(R.string.select_table)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			}).show();
		}
	}

	private class ClearMemberTask extends WebServiceTask{

		public ClearMemberTask(Context c, GlobalVar gb, int transactionId, int computerId) {
			super(c, gb, "WSiOrder_JSON_ClearMemberOfTransaction");
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iToTransactionID");
			property.setValue(transactionId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iToComputerID");
			property.setValue(computerId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iFromShopID");
			property.setValue(globalVar.SHOP_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iFromStaffID");
			property.setValue(globalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iFromComputerID");
			property.setValue(globalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			progress.setMessage(context.getString(R.string.loading_progress));
			progress.show();
		}

		@Override
		protected void onPostExecute(String result) {
			if(progress.isShowing())
				progress.dismiss();
			
			GsonDeserialze gdz = new GsonDeserialze();
			try {
				WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
				if(wsResult.getiResultID() == 0){
					new AlertDialog.Builder(CONTEXT)
					.setMessage(R.string.success)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}).show();
					new ShowSummaryBillTask(SummaryActivity.this, globalVar).execute(GlobalVar.FULL_URL);
				}else{
					new AlertDialog.Builder(CONTEXT)
					.setTitle(R.string.error)
					.setMessage(wsResult.getSzResultData().equals("") ? result : wsResult.getSzResultData())
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}).show();
				}
			} catch (Exception e) {
				new AlertDialog.Builder(CONTEXT)
				.setTitle(R.string.error)
				.setMessage(result)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
			}
		}
		
		
	}
	
	private class CheckBillTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_CallCheckBillFromTableID";
		
		public CheckBillTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);

			PropertyInfo property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(TABLE_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			progress.setMessage(context.getString(R.string.loading_progress));
			progress.show();
		}

		@Override
		protected void onPostExecute(String result) {
			if(progress.isShowing())
				progress.dismiss();
			
			GsonDeserialze gdz = new GsonDeserialze();
			try {
				WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
				if(wsResult.getiResultID() == 0){
					// refresh
					new ShowSummaryBillTask(context, globalVar).execute(GlobalVar.FULL_URL);
					

					new AlertDialog.Builder(CONTEXT)
					.setMessage(R.string.success)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}).show();
				}else{
					new AlertDialog.Builder(CONTEXT)
					.setTitle(R.string.error)
					.setMessage(wsResult.getSzResultData().equals("") ? 
							result : wsResult.getSzResultData())
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						}
					}).show();
				}
			} catch (Exception e) {
				new AlertDialog.Builder(CONTEXT)
				.setTitle(R.string.error)
				.setMessage(result)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).show();
			}
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
				final TableInfo tbInfo = gdz.deserializeTableInfoJSON(result);

				spinnerTableZone.setAdapter(IOrderUtility.createTableZoneAdapter(CONTEXT, tbInfo));
				spinnerTableZone.setOnItemSelectedListener(new OnItemSelectedListener(){

					@Override
					public void onItemSelected(AdapterView<?> parent, View v,
							int position, long id) {
						TableInfo.TableZone tbZone = (TableZone) parent.getItemAtPosition(position);
						
						final List<TableInfo.TableName> tbNameLst =
								IOrderUtility.filterTableNameHaveOrder(tbInfo, tbZone);
						
						tableNameListView.setAdapter(IOrderUtility.createTableNameAdapter(CONTEXT, globalVar, tbNameLst));
						tableNameListView.setOnItemClickListener(new OnItemClickListener(){

							@Override
							public void onItemClick(AdapterView<?> parent, View v,
									int position, long id) {
								TableName tbName = (TableName) parent.getItemAtPosition(position);
										
								TABLE_ID = tbName.getTableID();
								tvTableName.setText(R.string.table);
								tvTableName.append(":" +tbName.getTableName());
								
								disableButton();
								
								new ShowSummaryBillTask(context, globalVar).execute(GlobalVar.FULL_URL);
							}
							
						});
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
						
					}
					
				});
			} catch (Exception e) {
				new AlertDialog.Builder(CONTEXT)
				.setTitle(R.string.error)
				.setMessage(result)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).show();
			}
		}
	}
	
	private class ShowSummaryBillTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_ShowSummaryBillDetail";
		
		public ShowSummaryBillTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(GlobalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(TABLE_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szReason");
			property.setValue("");
			property.setType(String.class);
			soapRequest.addProperty(property);
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(progress.isShowing())
				progress.dismiss();
			
			GsonDeserialze gdz = new GsonDeserialze();
			try {
				WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
				SUMMARY_TRANS = gdz.deserializeSummaryTransactionJSON(wsResult.getSzResultData());
				
				if(SUMMARY_TRANS != null){
					if(SUMMARY_TRANS.OrderList != null){
						List<SummaryTransaction.Order> orderList = 
								new ArrayList<SummaryTransaction.Order>();
						
						for(SummaryTransaction.Order order : SUMMARY_TRANS.OrderList){
							int productSetType = order.iProductSetType;
							//if(productSetType == 0 || productSetType == 15){
								orderList.add(order);
							//}
						}
						
						SUMMARY_TRANS.OrderList = orderList;
					}
					
					// enable button
					enableButton();
					
					// set customer qty
					mCustomerQty = SUMMARY_TRANS.NoCustomer;
					
					TableRow summaryRow = new TableRow(SummaryActivity.this);
					TextView tvSummLabel = new TextView(SummaryActivity.this);
					TextView tvSummVal = new TextView(SummaryActivity.this);
					for(syn.pos.data.model.SummaryTransaction.DisplaySummary displaySummary 
							: SUMMARY_TRANS.TransactionSummary.DisplaySummaryList){
						tvSummLabel.append(displaySummary.szDisplayName + "\n");
						tvSummVal.append(globalVar.decimalFormat.format(displaySummary.fPriceValue) + "\n");
					}
					// for set member
					CURR_TRANSACTION_ID = SUMMARY_TRANS.TransactionID;
					CURR_COMPUTER_ID = SUMMARY_TRANS.ComputerID;
					
					tvSummary.append("(x" + globalVar.qtyFormat.format(SUMMARY_TRANS.NoCustomer) + ")");
					
					if(!SUMMARY_TRANS.TransacionName.equals("")){
						tvSummary.setText(SUMMARY_TRANS.TransacionName);
					}else{
					}
					
					//tvSubmitTime.setText(SUMMARY_TRANS.TransactionSummary);
					if(SUMMARY_TRANS.CallForCheckBill > 0 && SUMMARY_TRANS.CallForCheckBill != 99){
						btnCheckbill.setEnabled(true);
						btnCheckbill.setText(context.getString(R.string.call_check_bill) + "(" + SUMMARY_TRANS.CallForCheckBill + ")");
					}else{
						btnCheckbill.setText(context.getString(R.string.call_check_bill));
					}
					
					if(SUMMARY_TRANS.CallForCheckBill == 99){
						disableButton();
					}
				}
				BillDetailAdapter billDetailAdapter = new BillDetailAdapter(SummaryActivity.this, 
						globalVar, SUMMARY_TRANS);
				lvOrder.setAdapter(billDetailAdapter);
			} catch (Exception e) {
				new AlertDialog.Builder(CONTEXT)
				.setTitle(R.string.error)
				.setMessage(result)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).show();
			}
		}
	}
	
	private class PrintTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_TablePrintLongBill";
		
		public PrintTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(GlobalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(TABLE_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szReason");
			property.setValue("");
			property.setType(String.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPostExecute(String result) {
			if(progress.isShowing())
				progress.dismiss();
			
			GsonDeserialze gdz = new GsonDeserialze();
			try {
				WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
				if(wsResult.getiResultID() == 0){
					new AlertDialog.Builder(context)
					.setMessage(R.string.success)
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						}
					}).show();
				}else{
					new AlertDialog.Builder(context)
					.setMessage(wsResult.getSzResultData().equals("") ? 
							result : wsResult.getSzResultData())
					.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
						}
					}).show();
				}
			} catch (Exception e) {
				new AlertDialog.Builder(context)
				.setTitle(R.string.error)
				.setMessage(result)
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
			}
		}
	}
}
