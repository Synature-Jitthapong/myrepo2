package com.syn.iorder;
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
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class CheckBillActivity extends Activity {
	private Spinner spinnerTableZone;
	private ListView tableNameListView;
	private ListView orderDetailListView;
	private TextView tvBillHeader;
	private TextView tvBillCustNo;
	private TextView tvBillMember;
	private LinearLayout billMemberLayout;
	private TextView tvTableName;
	private ProgressBar tableProgress;
	private ProgressBar orderProgress;
	private TextView tvSummaryDisplay;
	private TextView tvPriceValue;
	
	private Button btnClose;
	private Button btnCheckbill;
	private Button btnPrint;
	private Button btnSetmember;
	private Button btnClearMember;
	private Button btnEditQuestion;
	private GlobalVar globalVar;
	private Context CONTEXT;
	
	private int CURR_TRANSACTION_ID;
	private int CURR_COMPUTER_ID;
	private int TABLE_ID;
	private int customerQty = 1;
	
	private SummaryTransaction SUMMARY_TRANS;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_bill);
		
		CONTEXT = this;
		
		globalVar = new GlobalVar(this);
		initComponent();
	}
	
	
	@Override
	protected void onResume() {
		if(TABLE_ID != 0)
			new ShowSummaryBillTask(CheckBillActivity.this, 
					globalVar).execute(globalVar.FULL_URL);
		
		super.onResume();
	}


	private void disableButton(){
		btnCheckbill.setEnabled(false);
		btnPrint.setEnabled(false);
		btnSetmember.setEnabled(false);
		btnClearMember.setEnabled(false);
		btnEditQuestion.setEnabled(false);
	}
	
	private void enableButton(){
		btnCheckbill.setEnabled(true);
		btnPrint.setEnabled(true);
		btnSetmember.setEnabled(true);
		btnClearMember.setEnabled(true);
		btnEditQuestion.setEnabled(true);
	}
	
	private void initComponent(){
		billMemberLayout = (LinearLayout) findViewById(R.id.BillMemberLayout);
		tvBillCustNo = (TextView) findViewById(R.id.textViewBillCustNo);
		tvBillMember = (TextView) findViewById(R.id.textViewBillMember);
		spinnerTableZone = (Spinner) findViewById(R.id.spinnerTableZone);
		tableNameListView = (ListView) findViewById(R.id.listViewTableName);
		btnPrint = (Button) findViewById(R.id.btnPrint);
		btnCheckbill = (Button) findViewById(R.id.btnCallCheckbill);
		btnSetmember = (Button) findViewById(R.id.buttonBillSetMember);
		btnClearMember = (Button) findViewById(R.id.buttonClearMember);
		tvBillHeader = (TextView) findViewById(R.id.tvBillHeader);
		tvTableName = (TextView) findViewById(R.id.tvTableName);
		orderDetailListView = (ListView) findViewById(R.id.orderDetailListView);
		tvSummaryDisplay = (TextView) findViewById(R.id.textViewSummaryDisplay);
		tvPriceValue = (TextView) findViewById(R.id.textViewPriceValue);
		orderProgress = (ProgressBar) findViewById(R.id.progressBarOrderDetail);
		tableProgress = (ProgressBar) findViewById(R.id.progressBarTable);
		btnEditQuestion = (Button) findViewById(R.id.buttonEditQuestion);
		
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
			btnEditQuestion.setText(R.string.button_edit_customer_qty);
			btnEditQuestion.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					LayoutInflater inflater = LayoutInflater.from(CheckBillActivity.this);
					View v = inflater.inflate(R.layout.customer_qty, null);
					TextView tvTitle = (TextView) v.findViewById(R.id.textView1);
					Button btnMinus = (Button) v.findViewById(R.id.button1);
					Button btnPlus = (Button) v.findViewById(R.id.button2);
					Button btnCancel = (Button) v.findViewById(R.id.button3);
					Button btnOk = (Button) v.findViewById(R.id.button4);
					final TextView tvCustQty = (TextView) v.findViewById(R.id.textView2);
					tvCustQty.setText(Integer.toString(customerQty));
					tvTitle.setText(tvTableName.getText());
					
					final Dialog d = new Dialog(CheckBillActivity.this, R.style.CustomDialog);
					d.setContentView(v);
					d.getWindow().setLayout(
							android.view.ViewGroup.LayoutParams.MATCH_PARENT,
							android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
					d.show();
					
					btnMinus.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							int qty = 1;
							try {
								qty = Integer.parseInt(tvCustQty.getText().toString());
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(--qty > 0){
								tvCustQty.setText(Integer.toString(qty));
								customerQty = qty;
							}
						}
						
					});
					
					btnPlus.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							int qty = 1;
							try {
								qty = Integer.parseInt(tvCustQty.getText().toString());
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							tvCustQty.setText(Integer.toString(++qty));
							customerQty = qty;
						}
						
					});
					
					btnCancel.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							d.dismiss();
						}
						
					});
					
					btnOk.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							new UpdateCustomerTask(CheckBillActivity.this, globalVar, CURR_TRANSACTION_ID,
									CURR_COMPUTER_ID, customerQty, new WebServiceTaskState(){

										@Override
										public void onSuccess() {
											d.dismiss();
											new ShowSummaryBillTask(CheckBillActivity.this, globalVar).execute(GlobalVar.FULL_URL);
										}

										@Override
										public void onNotSuccess() {

											d.dismiss();
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
						
					});
					
				}
				
			});
			
		}
		else{
			btnEditQuestion.setText(R.string.button_edit_question);
			btnEditQuestion.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					new CurrentAnswerQuestionTask(CheckBillActivity.this, globalVar, 
							TABLE_ID, new CurrentAnswerQuestionTask.ICurrentAnswerListener() {
								
								@Override
								public void listQuestionAnswer(List<QuestionAnswerData> questionLst) {
									//add answer to temp 
									final QuestionGroups qsGroup = new QuestionGroups(CheckBillActivity.this);
									qsGroup.insertCurrentAnswerQuestion(questionLst);
									
									// popup
									LayoutInflater inflater = LayoutInflater.from(CheckBillActivity.this);
									View questView = inflater.inflate(R.layout.question_list_layout, null);
									TextView tvQestionTitle = (TextView) questView.findViewById(R.id.textView1);
									tvQestionTitle.setText(tvTableName.getText());
									Button btnOk = (Button) questView.findViewById(R.id.button1);
									Button btnCancel = (Button) questView.findViewById(R.id.button2);
									final TextView tvRequire = (TextView) questView.findViewById(R.id.textView2);
									final ListView lvQuestion = (ListView) questView.findViewById(R.id.listView1);
									lvQuestion.setEnabled(false);
									
									// question adapter
									final List<ProductGroups.QuestionDetail> qsDetailLst = qsGroup.listCurrentQuestionDetail();
									
									if(qsDetailLst != null && qsDetailLst.size() > 0){
										final SelectTableQuestionAdapter qsAdapter = 
												new SelectTableQuestionAdapter(CheckBillActivity.this, globalVar, qsDetailLst);
										lvQuestion.setAdapter(qsAdapter);
										
										final Dialog dialog = new Dialog(CheckBillActivity.this, R.style.CustomDialog);
										dialog.setContentView(questView);
										dialog.setCancelable(false);
										dialog.getWindow().setLayout(
												android.view.ViewGroup.LayoutParams.MATCH_PARENT,
												android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
										dialog.show();
										
										btnOk.setOnClickListener(new OnClickListener(){
			
											@Override
											public void onClick(View v) {
												boolean isChoiceQuestion = qsGroup.checkChoiceTypeQuestion(4);
												boolean requireSelect = true;
												// check require question
												for(int i = 0; i < qsDetailLst.size(); i++){
													ProductGroups.QuestionDetail qsDetail = 
															qsDetailLst.get(i);
													
													if(qsDetail.getIsRequired() == 1){
														// check selected
														if(!qsGroup.checkAddedQuestion(qsDetail.getQuestionID())){
															if(qsDetail.getQuestionTypeID() == 2){
																if(qsGroup.getTotalAnswerQty() > 0){
																	if(!isChoiceQuestion){
																		requireSelect = false;
																		tvRequire.setVisibility(View.GONE);
																	}
																}else{
																	requireSelect = true;
																	tvRequire.setVisibility(View.VISIBLE);
																	qsDetail.setRequireSelect(requireSelect);
																	qsAdapter.notifyDataSetChanged();
																	break;
																}
															}else{
																requireSelect = true;
																tvRequire.setVisibility(View.VISIBLE);
																qsDetail.setRequireSelect(requireSelect);
																qsAdapter.notifyDataSetChanged();
																break;
															}
														}else{
															requireSelect = false;
															tvRequire.setVisibility(View.GONE);
															
															qsDetail.setRequireSelect(requireSelect);
															qsAdapter.notifyDataSetChanged();
														}
													}
												}
												
												if(!requireSelect){
													dialog.dismiss();
													
													QuestionGroups qsGroup = new QuestionGroups(CheckBillActivity.this);
													List<ProductGroups.QuestionAnswerData> selectedAnswerLst = 
															qsGroup.listAnswerQuestion();
													
													// send answer question
													new QuestionTask(CheckBillActivity.this, globalVar, TABLE_ID, 
															selectedAnswerLst, new WebServiceStateListener(){
								
																@Override
																public void onSuccess() {
																	IOrderUtility.alertDialog(CheckBillActivity.this, 
																			R.string.edit_question_title, R.string.edit_question_success, 0);
																}
								
																@Override
																public void onNotSuccess() {
																	// TODO Auto-generated method stub
																	
																}
													}).execute(GlobalVar.FULL_URL);
												}
											}
										});
										
										btnCancel.setOnClickListener(new OnClickListener(){

											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
											
										});
									}
								}
							}).execute(GlobalVar.FULL_URL);
					}
			});
		}
		
		btnPrint.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new PrintTask(CONTEXT, globalVar).execute(GlobalVar.FULL_URL);		
			}
		});
		
		btnCheckbill.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				checkBill();
			}
		});
		
		btnClearMember.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				final CustomDialog cusDialog =
						new CustomDialog(CheckBillActivity.this, R.style.CustomDialog);
				cusDialog.title.setVisibility(View.VISIBLE);
				cusDialog.title.setText(R.string.clear_member_title);
				cusDialog.message.setText(R.string.cf_clear_member);
				cusDialog.btnCancel.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						cusDialog.dismiss();
					}
					
				});
				cusDialog.btnOk.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						cusDialog.dismiss();
						new ClearMemberTask(CheckBillActivity.this, globalVar, 
								CURR_TRANSACTION_ID, CURR_COMPUTER_ID).execute(GlobalVar.FULL_URL);	
					}
					
				});
				cusDialog.show();
			}
		});
		
		btnSetmember.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CheckBillActivity.this, SearchMemberActivity.class);
				intent.putExtra("TO_TRANSACTION_ID", CURR_TRANSACTION_ID);
				intent.putExtra("TO_COMPUTER_ID", CURR_COMPUTER_ID);
				CheckBillActivity.this.startActivity(intent);
				overridePendingTransition(R.animator.slide_in_up, R.animator.slide_in_out);		
			}
		});
		 new LoadTableTask(CONTEXT, globalVar).execute(GlobalVar.FULL_URL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_summary_bill, menu);
		View v = menu.findItem(R.id.item_close).getActionView();

		btnClose = (Button) v.findViewById(R.id.buttonClose);
		
		
		btnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				CheckBillActivity.this.finish();
			}
		});
		return true;
	}

	private void checkBill(){
		if(TABLE_ID != 0)
			new CheckBillTask(CONTEXT, globalVar).execute(GlobalVar.FULL_URL);
		else{
//			new AlertDialog.Builder(CONTEXT)
//			.setTitle(R.string.call_checkbill_dialog_title)
//			.setMessage(R.string.please_select_table)
//			.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//				
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//				}
//			}).show();
			IOrderUtility.alertDialog(CONTEXT, R.string.global_dialog_title_error, R.string.please_select_table, 0);
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
			tvProgress.setText(R.string.clear_member_progress);
			progress.setMessage(tvProgress.getText());
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
					IOrderUtility.alertDialog(CheckBillActivity.this, R.string.clear_member_title, R.string.clear_member_succ, 0);

					new ShowSummaryBillTask(CheckBillActivity.this, globalVar).execute(GlobalVar.FULL_URL);
				}else{
					IOrderUtility.alertDialog(CheckBillActivity.this, R.string.global_dialog_title_error, 
							wsResult.getSzResultData().equals("") ? result : wsResult.getSzResultData(), 0);
				}
			} catch (Exception e) {
				IOrderUtility.alertDialog(CheckBillActivity.this, R.string.global_dialog_title_error, result, 0);
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
			tvProgress.setText(R.string.call_checkbill_progress);
			progress.setMessage(tvProgress.getText().toString());
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
					

//					new AlertDialog.Builder(CONTEXT)
//					.setTitle(R.string.call_checkbill_dialog_title)
//					.setMessage(R.string.call_chekcbill_success)
//					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//						}
//					}).show();
//					
					IOrderUtility.alertDialog(CONTEXT, R.string.call_checkbill_dialog_title, R.string.call_chekcbill_success, 0);
				}else{
//					new AlertDialog.Builder(CONTEXT)
//					.setTitle("Error")
//					.setMessage(wsResult.getSzResultData())
//					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//						}
//					}).show();
					IOrderUtility.alertDialog(CONTEXT, R.string.global_dialog_title_error, wsResult.getSzResultData().equals("") ? 
							result : wsResult.getSzResultData(), 0);
				}
			} catch (Exception e) {

				IOrderUtility.alertDialog(CONTEXT, R.string.global_dialog_title_error, e.getMessage(), 0);
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
			tableProgress.setVisibility(View.GONE);
			
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
								tvTableName.setText(R.string.text_table);
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
				IOrderUtility.alertDialog(CONTEXT, R.string.global_dialog_title_error, result, 0);
			}
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.load_table_progress);
			tableProgress.setVisibility(View.VISIBLE);
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
		protected void onPreExecute() {
			tvProgress.setText(R.string.loading_progress);
			orderProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			orderProgress.setVisibility(View.GONE);
			
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
					customerQty = SUMMARY_TRANS.NoCustomer;
					
					tvSummaryDisplay.setText(null);
					tvPriceValue.setText(null);
					for(syn.pos.data.model.SummaryTransaction.DisplaySummary displaySummary 
							: SUMMARY_TRANS.TransactionSummary.DisplaySummaryList){
						tvSummaryDisplay.append(displaySummary.szDisplayName + "\n");
						tvPriceValue.append(globalVar.decimalFormat.format(displaySummary.fPriceValue) + "\n");
					}
					TextView tvCallCheckBill = new TextView(context);
					tvCallCheckBill.setText(R.string.call_checkbill);
					
					// for set member
					CURR_TRANSACTION_ID = SUMMARY_TRANS.TransactionID;
					CURR_COMPUTER_ID = SUMMARY_TRANS.ComputerID;
					
					tvBillCustNo.setText("(x" + globalVar.qtyFormat.format(SUMMARY_TRANS.NoCustomer) + ")");
					
					if(!SUMMARY_TRANS.TransacionName.equals("")){
						tvBillMember.setText(SUMMARY_TRANS.TransacionName);
						billMemberLayout.setVisibility(View.VISIBLE);
						btnClearMember.setEnabled(true);
					}else{
						billMemberLayout.setVisibility(View.GONE);
						btnClearMember.setEnabled(false);
					}
					
					//tvSubmitTime.setText(SUMMARY_TRANS.TransactionSummary);
					if(SUMMARY_TRANS.CallForCheckBill > 0 && SUMMARY_TRANS.CallForCheckBill != 99){
						btnCheckbill.setEnabled(true);
						btnCheckbill.setText(tvCallCheckBill.getText().toString() + "(" + SUMMARY_TRANS.CallForCheckBill + ")");
					}else{
						btnCheckbill.setText(tvCallCheckBill.getText().toString());
					}
					
					if(SUMMARY_TRANS.CallForCheckBill == 99){
						disableButton();
					}
				}
				BillDetailAdapter billDetailAdapter = new BillDetailAdapter(CheckBillActivity.this, 
						globalVar, SUMMARY_TRANS);
				orderDetailListView.setAdapter(billDetailAdapter);
			} catch (Exception e) {
				IOrderUtility.alertDialog(CONTEXT, R.string.global_dialog_title_error, result, 0);
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
		protected void onPreExecute() {
			tvProgress.setText(R.string.print_progress);
			progress.setMessage(tvProgress.getText().toString());
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
//					new AlertDialog.Builder(context)
//					.setTitle(R.string.print_dialog_title)
//					.setMessage(R.string.print_success)
//					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//						}
//					}).show();

					IOrderUtility.alertDialog(CONTEXT, R.string.print_dialog_title, R.string.print_success, 0);
				}else{
//					new AlertDialog.Builder(context)
//					.setTitle(R.string.print_dialog_title)
//					.setMessage(wsResult.getSzResultData())
//					.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//						
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//						}
//					}).show();

					IOrderUtility.alertDialog(CONTEXT, R.string.global_dialog_title_error, wsResult.getSzResultData().equals("") ? 
							result : wsResult.getSzResultData(), 0);
				}
			} catch (Exception e) {
//				new AlertDialog.Builder(context)
//				.setTitle("Exception")
//				.setMessage(result)
//				.setNeutralButton(R.string.global_close_dialog_btn, new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				}).show();

				IOrderUtility.alertDialog(CONTEXT, R.string.global_dialog_title_error, result, 0);
			}
		}
	}
}
