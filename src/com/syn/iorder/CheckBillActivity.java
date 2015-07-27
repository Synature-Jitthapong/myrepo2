package com.syn.iorder;
/*
 * set member 
 */
import java.util.ArrayList;
import java.util.List;

import jpos.POSPrinter;

import org.ksoap2.serialization.PropertyInfo;

import com.bxl.config.editor.BXLConfigLoader;
import com.syn.iorder.DiscountUtils.ButtonDiscount;
import com.syn.iorder.PrinterUtils.Printer;

import syn.pos.data.dao.QuestionGroups;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.ProductGroups;
import syn.pos.data.model.ProductGroups.QuestionAnswerData;
import syn.pos.data.model.SummaryTransaction;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableName;
import syn.pos.data.model.WebServiceResult;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class CheckBillActivity extends Activity implements PayInfoFragment.PaymentListener {
	private boolean isSearchMember = true;
	
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
	private Button btnCalDiscount;
	private Button btnPrint;
	private Button btnSetmember;
	private Button btnEditQuestion;
	private GlobalVar globalVar;
	private Context mContext;	
	
	private int mTransactionId;
	private int mComputerId;
	private int mTableId;
	private int mCustomerQty = 1;
	
	private SummaryTransaction mSummaryTrans;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_bill);
		
		mContext = CheckBillActivity.this;
		
		globalVar = new GlobalVar(this);
		initComponent();
	}
	
	public SummaryTransaction getSummTrans(){
		return mSummaryTrans;
	}
	
	public CharSequence getTableName(){
		return tvTableName.getText();
	}
	
	public CharSequence getBillCustNo(){
		return tvBillCustNo.getText();
	}
	
	public CharSequence getBillHeader(){
		return tvBillHeader.getText();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(mTableId != 0)
			new ShowSummaryBillTask(mContext, 
					globalVar).execute(GlobalVar.FULL_URL);
		
	}

	private void disableButton(){
		btnCalDiscount.setEnabled(false);
		btnCheckbill.setEnabled(false);
		btnPrint.setEnabled(false);
		btnSetmember.setEnabled(false);
		btnEditQuestion.setEnabled(false);
	}
	
	private void enableButton(){
		btnCalDiscount.setEnabled(true);
		btnCheckbill.setEnabled(true);
		btnPrint.setEnabled(true);
		btnSetmember.setEnabled(true);
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
		btnCalDiscount = (Button) findViewById(R.id.btnCalDiscount);
		btnSetmember = (Button) findViewById(R.id.buttonBillSetMember);
		tvBillHeader = (TextView) findViewById(R.id.tvBillHeader);
		tvTableName = (TextView) findViewById(R.id.tvTableName);
		orderDetailListView = (ListView) findViewById(R.id.orderDetailListView);
		tvSummaryDisplay = (TextView) findViewById(R.id.textViewSummaryDisplay);
		tvPriceValue = (TextView) findViewById(R.id.textViewPriceValue);
		orderProgress = (ProgressBar) findViewById(R.id.progressBarOrderDetail);
		tableProgress = (ProgressBar) findViewById(R.id.progressBarTable);
		btnEditQuestion = (Button) findViewById(R.id.buttonEditQuestion);
		
		tvBillHeader.setText(globalVar.SHOP_DATA.getShopName());
		
		if(mTableId==0){
			disableButton();
		}
		else{
			enableButton();
		}
		
		if(GlobalVar.sIsCalculateDiscount){
			btnCalDiscount.setVisibility(View.VISIBLE);
		}else{
			btnCalDiscount.setVisibility(View.GONE);
		}
		/* change btnCallCheckBill text to Print long bill
		 * if feature is enabled
		 */
		if(GlobalVar.sIsEnableCallCheckBill)
			btnCheckbill.setText(R.string.call_checkbill);
		else if(GlobalVar.sIsEnablePrintLongBill)
			btnCheckbill.setText(R.string.print_long_bill);
		else if(GlobalVar.sIsEnableCallCheckbillPayCashDetail)
			btnCheckbill.setText(R.string.call_checkbill);
		else
			btnCheckbill.setVisibility(View.GONE);
		
		/* set text button edit question or customer qty.
		 * and set event by feature
		 */
		if(!GlobalVar.sIsEnableTableQuestion){
			btnEditQuestion.setText(R.string.button_edit_customer_qty);
			btnEditQuestion.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					LayoutInflater inflater = LayoutInflater.from(mContext);
					View v = inflater.inflate(R.layout.customer_qty, null);
					Button btnMinus = (Button) v.findViewById(R.id.button1);
					Button btnPlus = (Button) v.findViewById(R.id.button2);
					final TextView tvCustQty = (TextView) v.findViewById(R.id.textView2);
					tvCustQty.setText(Integer.toString(mCustomerQty));
					
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle(tvTableName.getText());
					builder.setNegativeButton(R.string.global_btn_cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					builder.setPositiveButton(R.string.global_btn_ok, null);
					builder.setView(v);
					final AlertDialog dialog = builder.create();
					dialog.show();
					
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
								mCustomerQty = qty;
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
							mCustomerQty = qty;
						}
						
					});
					
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							new UpdateCustomerTask(mContext, globalVar, mTransactionId,
									mComputerId, mCustomerQty, new WebServiceTaskState(){

										@Override
										public void onSuccess() {
											dialog.dismiss();
											new ShowSummaryBillTask(mContext, globalVar).execute(GlobalVar.FULL_URL);
										}

										@Override
										public void onNotSuccess() {
											dialog.dismiss();
										}

										@Override
										public void onProgress() {
											// TODO Auto-generated method stub
											
										}

										@Override
										public void onSuccess(int arg) {
											// TODO Auto-generated method stub
											
										}
								
							}).execute(GlobalVar.FULL_URL);
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
					new CurrentAnswerQuestionTask(mContext, globalVar, 
							mTableId, new CurrentAnswerQuestionTask.ICurrentAnswerListener() {
								
								@Override
								public void listQuestionAnswer(List<QuestionAnswerData> questionLst) {
									//add answer to temp 
									final QuestionGroups qsGroup = new QuestionGroups(mContext);
									qsGroup.insertCurrentAnswerQuestion(questionLst);
									
									// popup
									LayoutInflater inflater = LayoutInflater.from(mContext);
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
												new SelectTableQuestionAdapter(mContext, globalVar, qsDetailLst);
										lvQuestion.setAdapter(qsAdapter);
										
										final Dialog dialog = new Dialog(mContext, R.style.CustomDialog);
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
													
													QuestionGroups qsGroup = new QuestionGroups(mContext);
													List<ProductGroups.QuestionAnswerData> selectedAnswerLst = 
															qsGroup.listAnswerQuestion();
													
													// send answer question
													new QuestionTask(mContext, globalVar, mTableId, 
															selectedAnswerLst, new WebServiceStateListener(){
								
																@Override
																public void onSuccess() {
																	IOrderUtility.alertDialog(mContext, 
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
		
		btnCalDiscount.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				calculateDiscount();
			}
			
		});
		
		btnPrint.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new PrintTask(mContext, globalVar).execute(GlobalVar.FULL_URL);		
			}
		});
		
		btnCheckbill.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(GlobalVar.sIsEnablePrintLongBill)
					printLongbill();
				else if(GlobalVar.sIsEnableCallCheckBill)
					checkBill();
				else if(GlobalVar.sIsEnableCallCheckbillPayCashDetail)
					checkBillSetPaydetail();
			}
		});
		
		btnSetmember.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(isSearchMember){
					Intent intent = new Intent(mContext, SearchMemberActivity.class);
					intent.putExtra("TO_TRANSACTION_ID", mTransactionId);
					intent.putExtra("TO_COMPUTER_ID", mComputerId);
					mContext.startActivity(intent);
				}else{
					final CustomDialog cusDialog =
							new CustomDialog(mContext, R.style.CustomDialog);
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
							btnSetmember.setText(R.string.call_checkbill_setmember);
							isSearchMember = true;
							cusDialog.dismiss();
							new ClearMemberTask(mContext, globalVar, 
									mTransactionId, mComputerId).execute(GlobalVar.FULL_URL);	
						}
						
					});
					cusDialog.show();
				}
			}
		});
		
//		 new LoadAllTableV1(this, globalVar, new LoadAllTableV1.LoadTableProgress() {
//			
//			@Override
//			public void onPre() {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onPost() {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onError(String msg) {
//				IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, msg, 0);
//			}
//			
//			@Override
//			public void onPost(final TableName tbName) {
				new LoadAllTableV2(mContext, globalVar, new LoadAllTableV2.LoadTableProgress() {
					
					@Override
					public void onPre() {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onPost() {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onError(String msg) {
						IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, msg, 0);
					}
					
					@Override
					public void onPost(final List<TableInfo> tbInfoLst) {
						spinnerTableZone.setAdapter(IOrderUtility.createTableZoneAdapter(mContext, GlobalVar.sTbName));
						spinnerTableZone.setOnItemSelectedListener(new OnItemSelectedListener(){

							@Override
							public void onItemSelected(AdapterView<?> parent, View v,
									int position, long id) {
								TableName.TableZone tbZone = (TableName.TableZone) parent.getItemAtPosition(position);
								
								final List<TableInfo> newTbInfoLst =
										IOrderUtility.filterTableNameHaveOrder(tbInfoLst, tbZone);
								
								tableNameListView.setAdapter(IOrderUtility.createTableNameAdapter(
										mContext, globalVar, newTbInfoLst, false, false));
								tableNameListView.setOnItemClickListener(new OnItemClickListener(){

									@Override
									public void onItemClick(AdapterView<?> parent, View v,
											int position, long id) {
										TableInfo tbInfo = (TableInfo) parent.getItemAtPosition(position);
												
										mTableId = tbInfo.getiTableID();
										mTransactionId = tbInfo.getiTransactionID();
										mComputerId = tbInfo.getiComputerID();
										String tableName = IOrderUtility.formatCombindTableName(tbInfo.isbIsCombineTable(), 
												tbInfo.getSzCombineTableName(), tbInfo.getSzTableName());
										tvTableName.setText(R.string.text_table);
										tvTableName.append(":" + tableName);
										
										disableButton();
										
										new ShowSummaryBillTask(mContext, globalVar).execute(GlobalVar.FULL_URL);
									}
									
								});
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {
								// TODO Auto-generated method stub
								
							}
							
						});
					}
				}).execute(GlobalVar.FULL_URL);
//			}
//		}).execute(GlobalVar.FULL_URL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_summary_bill, menu);
		View v = menu.findItem(R.id.item_close).getActionView();

		btnClose = (Button) v.findViewById(R.id.buttonClose);
		
		
		btnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		return true;
	}

	// calculate discount
	private void calculateDiscount(){
		final List<ButtonDiscount> promotionLst = 
				new ArrayList<ButtonDiscount>();
		
		final LayoutInflater inflater = (LayoutInflater)
				mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View discountView = inflater.inflate(R.layout.bill_summary_with_discount_layout, null);
		final ListView lvDiscount = (ListView) discountView.findViewById(R.id.lvDiscount);
		final ProgressBar progress = (ProgressBar) discountView.findViewById(R.id.progressBar1);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.select_promotion);
		builder.setView(discountView);
		builder.setNegativeButton(R.string.global_btn_cancel, null);
		builder.setPositiveButton(R.string.apply_discount, null);
		final AlertDialog dialogSelPromo = builder.create();
		dialogSelPromo.show();
		
		/// bill summar
//		View orderView = inflater.inflate(R.layout.order_list_layout, null);
//		final ListView lvOrder = (ListView) orderView.findViewById(R.id.listViewOrder);
//		final TextView tvSumText = (TextView) orderView.findViewById(R.id.textViewSumText);
//		final TextView tvSumPrice = (TextView) orderView.findViewById(R.id.textViewSumPrice);
//		ImageButton btnClose = (ImageButton) orderView.findViewById(R.id.imageButtonCloseOrderDialog);
//		final ProgressBar progressSumm = (ProgressBar) orderView.findViewById(R.id.progressBarOrderOfTable);
//		
//		final Dialog detailDialog = new Dialog(mContext, R.style.CustomDialog);
//		detailDialog.setContentView(orderView);
//		detailDialog.getWindow().setGravity(Gravity.TOP);
//		detailDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
//				WindowManager.LayoutParams.WRAP_CONTENT);
//
//		btnClose.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				detailDialog.dismiss();
//			}
//			
//		});
		
		final ProgressDialog applyPromoProgress = new ProgressDialog(mContext);
		final DiscountUtils.GetSummaryBillWithDiscountListener getSummListener =
				new DiscountUtils.GetSummaryBillWithDiscountListener() {
					
					@Override
					public void onPre() {
//						progressSumm.setVisibility(View.VISIBLE);
//						detailDialog.show();
						applyPromoProgress.setMessage(mContext.getString(R.string.loading_progress));
						applyPromoProgress.show();
					}
					
					@Override
					public void onPost() {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onError(String msg) {
						//progressSumm.setVisibility(View.GONE);
						if(applyPromoProgress.isShowing())
							applyPromoProgress.dismiss();
						
						new AlertDialog.Builder(mContext)
						.setMessage(msg)
						.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						}).show();
					}
					
					@Override
					public void onPost(SummaryTransaction summTrans) {
						//progressSumm.setVisibility(View.GONE);
						
//						if(summTrans != null){
//							tvSumText.setText(null);
//							tvSumPrice.setText(null);
//							for(syn.pos.data.model.SummaryTransaction.DisplaySummary displaySummary 
//									: summTrans.TransactionSummary.DisplaySummaryList){
//								tvSumText.append(displaySummary.szDisplayName + "\n");
//								tvSumPrice.append(globalVar.decimalFormat.format(displaySummary.fPriceValue) + "\n");
//							}
//						}
//						lvOrder.setAdapter(new BillDetailAdapter(mContext, 
//								globalVar, summTrans));

						if(applyPromoProgress.isShowing())
							applyPromoProgress.dismiss();
						dialogSelPromo.dismiss();
						new ShowSummaryBillTask(mContext, 
								globalVar).execute(GlobalVar.FULL_URL);
					}
				};
				
		dialogSelPromo.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(promotionLst.size() > 0){
					new AlertDialog.Builder(mContext)
					.setTitle(android.R.string.cancel)
					.setMessage(R.string.confirm_cancel)
					.setNegativeButton(R.string.global_btn_no, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					})
					.setPositiveButton(R.string.global_btn_yes, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new DiscountUtils.ApplayDiscountWithTransaction(mContext, globalVar, 
									mTableId, "", "", getSummListener).execute(GlobalVar.FULL_URL);
						}
					}).show();
				}else{
					new DiscountUtils.ApplayDiscountWithTransaction(mContext, globalVar, 
							mTableId, "", "", getSummListener).execute(GlobalVar.FULL_URL);
				}
			}
			
		});
		
		dialogSelPromo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				//if(promotionLst.size() != 0){
					String promotions = "";
					String promotionsRefNo = "";
					for(int i = 0; i < promotionLst.size(); i++){
						DiscountUtils.ButtonDiscount promotion = 
								promotionLst.get(i);
						int promotionId = promotion.getPromotionID();
						int applyNumber = promotion.getCurrentAppliedNumber();
						int refNo = promotion.getReferenceNo(); 
						promotions += DiscountUtils.toPromotionSeperate(promotionId, applyNumber);
						promotionsRefNo += DiscountUtils.toPromotionRefNoSeperate(refNo, applyNumber);
						if(i < promotionLst.size() - 1){
							promotions += ",";
							promotionsRefNo += ",";
						}
					}
					new DiscountUtils.ApplayDiscountWithTransaction(mContext, globalVar, 
							mTableId, promotions, promotionsRefNo, getSummListener).execute(GlobalVar.FULL_URL);
//				}else{
//					new AlertDialog.Builder(mContext)
//					.setMessage(R.string.select_promotion)
//					.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener(){
//
//						@Override
//						public void onClick(DialogInterface dialog,
//								int which) {
//						}
//						
//					})
//					.show();
//				}
			}
		});
				
		DiscountUtils.LoadButtonDiscountListener loadDiscountListener
			= new DiscountUtils.LoadButtonDiscountListener() {
				
				@Override
				public void onPre() {
					lvDiscount.setVisibility(View.GONE);
					progress.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onPost() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onError(String msg) {
					lvDiscount.setVisibility(View.VISIBLE);
					progress.setVisibility(View.GONE);
					
					new AlertDialog.Builder(mContext)
					.setMessage(msg)
					.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
				}
				
				@Override
				public void onPost(final List<ButtonDiscount> btnDiscountLst) {
					lvDiscount.setVisibility(View.VISIBLE);
					progress.setVisibility(View.GONE);
					
					final DiscountButtonListAdapter discountAdapter = 
							new DiscountButtonListAdapter(mContext, btnDiscountLst);
					
					for(DiscountUtils.ButtonDiscount discount : btnDiscountLst){
						if(discount.getCurrentAppliedNumber() > 0){
							discount.setChecked(true);
							promotionLst.add(discount);
						}
					}
					discountAdapter.notifyDataSetChanged();
					
					lvDiscount.setOnItemClickListener(new OnItemClickListener(){

						@Override
						public void onItemClick(AdapterView<?> parent, View v,
								int position, long id) {
							final DiscountUtils.ButtonDiscount btnDiscount = 
									(DiscountUtils.ButtonDiscount) parent.getItemAtPosition(position);
						
							if(btnDiscount.getCurrentAppliedNumber() == 0)
								btnDiscount.setCurrentAppliedNumber(1);
							
							// popup for enter reference number if required
							if(btnDiscount.getIsRequireReferenceNo() == 1 && !btnDiscount.isChecked()){
								AlertDialog.Builder builder = 
										new AlertDialog.Builder(mContext);
								final EditText txtRefNo = new EditText(mContext);
								txtRefNo.setInputType(InputType.TYPE_CLASS_NUMBER);
								builder.setTitle(R.string.enter_ref_no);
								builder.setView(txtRefNo);
								builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										btnDiscount.setChecked(false);
										btnDiscount.setCurrentAppliedNumber(0);
										discountAdapter.notifyDataSetChanged();
									}
								});
								builder.setPositiveButton(android.R.string.ok, null);
								final AlertDialog d = builder.create();
								d.show();

								txtRefNo.requestFocus();
								d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

									@Override
									public void onClick(View v) {
										try {
											btnDiscount.setReferenceNo(Integer.parseInt(txtRefNo.getText().toString()));
											btnDiscount.setChecked(true);
											discountAdapter.notifyDataSetChanged();
											d.dismiss();
										} catch (NumberFormatException e) {
											e.printStackTrace();
										}
									}
									
								});
							}
							
							// popup for enter number promotion
							if(btnDiscount.getMaxNumberCanApplied() > 1 && !btnDiscount.isChecked()){
								AlertDialog.Builder builder = 
										new AlertDialog.Builder(mContext);
								View editQtyView = inflater.inflate(R.layout.edit_qty_layout, null);
								final EditText txtQty = (EditText) editQtyView.findViewById(R.id.txtQty);
								final Button btnMinus = (Button) editQtyView.findViewById(R.id.btnMinus);
								final Button btnPlus = (Button) editQtyView.findViewById(R.id.btnPlus);
								
								btnPlus.setOnClickListener(new OnClickListener(){

									@Override
									public void onClick(View v) {
										try {
											int qty = Integer.parseInt(txtQty.getText().toString());
											if(++qty <= btnDiscount.getMaxNumberCanApplied()){
												txtQty.setText(String.valueOf(qty));
												btnDiscount.setCurrentAppliedNumber(qty);
												discountAdapter.notifyDataSetChanged();
											}
										} catch (NumberFormatException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									
								});
								btnMinus.setOnClickListener(new OnClickListener(){

									@Override
									public void onClick(View v) {
										try {
											int qty = Integer.parseInt(txtQty.getText().toString());
											if(--qty > 0){
												txtQty.setText(String.valueOf(qty));
												btnDiscount.setCurrentAppliedNumber(qty);
												discountAdapter.notifyDataSetChanged();
											}
										} catch (NumberFormatException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									
								});
								builder.setTitle(mContext.getString(R.string.enter_amount) + ", " + 
										mContext.getString(R.string.limit) + " " + 
										btnDiscount.getMaxNumberCanApplied());
								builder.setView(editQtyView);
								builder.setNegativeButton(R.string.global_btn_cancel, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										btnDiscount.setChecked(false);
										btnDiscount.setCurrentAppliedNumber(0);
										discountAdapter.notifyDataSetChanged();
									}
								});
								builder.setPositiveButton(R.string.global_btn_ok, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										if(Integer.parseInt(txtQty.getText().toString()) >
											btnDiscount.getMaxNumberCanApplied()){

											btnDiscount.setChecked(false);
											btnDiscount.setCurrentAppliedNumber(0);
											
											new AlertDialog.Builder(mContext)
											.setMessage(mContext.getString(R.string.promotion_limit) +
													" " + btnDiscount.getMaxNumberCanApplied())
											.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which) {
												}
											})
											.show();
										}else{
											btnDiscount.setChecked(true);
										}
										discountAdapter.notifyDataSetChanged();
									}
								});
								AlertDialog d = builder.create();
								d.show(); 
							}
							
							if(btnDiscount.isChecked()){
								btnDiscount.setChecked(false);
								btnDiscount.setCurrentAppliedNumber(0);
								promotionLst.remove(btnDiscount);
							}else{
								btnDiscount.setChecked(true);
								promotionLst.add(btnDiscount);
							}
							discountAdapter.notifyDataSetChanged();
						}
						
					});
					lvDiscount.setAdapter(discountAdapter);
				}
			};
			
		new DiscountUtils.ListButtonDiscountTask(mContext, 
				globalVar, mTransactionId, mComputerId, loadDiscountListener).execute(GlobalVar.FULL_URL);
	}
	
	private void checkBillSetPaydetail(){
		new AlertDialog.Builder(mContext)
		.setTitle(R.string.input_money)
		.setCancelable(false)
		.setMessage(R.string.confirm_input_money)
		.setNegativeButton(R.string.global_btn_no, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {	
				checkBill();
			}
		})
		.setPositiveButton(R.string.global_btn_yes, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PayInfoFragment f = PayInfoFragment.newInstance(mTransactionId, mComputerId, mSummaryTrans.TransactionSummary.fTotalSalePrice);
				f.show(getFragmentManager(), "payinfo");
			}
		}).show();
	}
	
	private void printLongbill(){
		
		boolean isEnableBtPrinter = AppConfigLayoutActivity.isEnableBtPrinter(mContext);
		
		if(isEnableBtPrinter){
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
			    // Device does not support Bluetooth 
			} else{
				LongBillPrintFragment f = LongBillPrintFragment.newInstance(mTableId, GlobalVar.STAFF_ID);
				f.show(this.getFragmentManager(), LongBillPrintFragment.TAG);
			}
		}else{
			final ProgressDialog progress = new ProgressDialog(this);
			
			final PrinterUtils.PrintLongbillListener printListener = 
					new PrinterUtils.PrintLongbillListener() {
						
						@Override
						public void onPre() {
							progress.setMessage(mContext.getString(R.string.print_progress));
							progress.show();
						}
						
						@Override
						public void onPost() {
						}
						
						@Override
						public void onError(String msg) {
							if(progress.isShowing())
								progress.dismiss();
							
							AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
							builder.setMessage(msg);
							builder.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});
							
							AlertDialog d = builder.create();
							d.show();
						}
						
						@Override
						public void onPost(WebServiceResult res, String result) {
							if(progress.isShowing())
								progress.dismiss();
							
							String msg = mContext.getString(R.string.print_longbill_success);
							
							if(res.getiResultID() != 0)
								msg = res.getSzResultData().equals("") ? result : res.getSzResultData();
							
							AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
							builder.setMessage(msg);
							builder.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									new ShowSummaryBillTask(mContext, globalVar).execute(GlobalVar.FULL_URL);
								}
							});
							
							AlertDialog d = builder.create();
							d.show();
						}
				};
					
			final PrinterUtils.LoadPrinterProgressListener loadPrinterListener = 
					new PrinterUtils.LoadPrinterProgressListener() {
						
						@Override
						public void onPre() {
							progress.setMessage(mContext.getString(R.string.loading_progress));
							progress.show();
						}
						
						@Override
						public void onPost() {
						}
						
						@Override
						public void onError(String msg) {
							if(progress.isShowing())
								progress.dismiss();
							
							AlertDialog.Builder builder = 
									new AlertDialog.Builder(mContext);
							builder.setMessage(msg);
							builder.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});
							AlertDialog d = builder.create();
							d.show();
						}
						
						@Override
						public void onPost(final List<Printer> printerLst, String result) {
							if(progress.isShowing())
								progress.dismiss();
							
							final PrinterListBuilder builder = 
									new PrinterListBuilder(mContext, printerLst);
							builder.setTitle(R.string.select_printer);
							builder.setNegativeButton(R.string.global_btn_close, 
									new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});
							builder.setPositiveButton(R.string.global_btn_ok, null);
							
							final AlertDialog d = builder.create();
							d.show();
							d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

								@Override
								public void onClick(View v) {
									if(builder.getPrinterData().getPrinterID() != 0){
										d.dismiss();
										// print
										new PrinterUtils.PrintTransToPrinterTask(mContext, 
												globalVar, mTransactionId, mComputerId, 
												builder.getPrinterData().getPrinterID(), GlobalVar.STAFF_ID, printListener).execute(GlobalVar.FULL_URL);
									}else{
										new AlertDialog.Builder(mContext)
										.setMessage(R.string.please_select_printer)
										.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
											}
										})
										.show();
									}
								}
								
							});
						}
					};
					
			new PrinterUtils.LoadPrinterTask(this, globalVar, 
					GlobalVar.STAFF_ID, loadPrinterListener).execute(GlobalVar.FULL_URL);	
		}
	}
	
	private void checkBill(){
		if(mTableId != 0)
			new CheckBillTask(mContext, globalVar).execute(GlobalVar.FULL_URL);
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
			IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, R.string.please_select_table, 0);
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
					IOrderUtility.alertDialog(mContext, R.string.clear_member_title, R.string.clear_member_succ, 0);

					new ShowSummaryBillTask(mContext, globalVar).execute(GlobalVar.FULL_URL);
				}else{
					IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, 
							wsResult.getSzResultData().equals("") ? result : wsResult.getSzResultData(), 0);
				}
			} catch (Exception e) {
				IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, result, 0);
			}
		}
		
		
	}
	
	private class CheckBillTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_CallCheckBillFromTableID";
		
		public CheckBillTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);

			PropertyInfo property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(mTableId);
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
					IOrderUtility.alertDialog(mContext, R.string.call_checkbill_dialog_title, R.string.call_chekcbill_success, 0);
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
					IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, wsResult.getSzResultData().equals("") ? 
							result : wsResult.getSzResultData(), 0);
				}
			} catch (Exception e) {

				IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, e.getMessage(), 0);
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
			property.setValue(mTableId);
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
				mSummaryTrans = gdz.deserializeSummaryTransactionJSON(wsResult.getSzResultData());
				
				if(mSummaryTrans != null){
					if(mSummaryTrans.OrderList != null){
						List<SummaryTransaction.Order> orderList = 
								new ArrayList<SummaryTransaction.Order>();
						
						for(SummaryTransaction.Order order : mSummaryTrans.OrderList){
							int productSetType = order.iProductSetType;
							//if(productSetType == 0 || productSetType == 15){
								orderList.add(order);
							//}
						}
						
						mSummaryTrans.OrderList = orderList;
					}
					
					// enable button
					enableButton();
					
					// set customer qty
					mCustomerQty = mSummaryTrans.NoCustomer;
					
					tvSummaryDisplay.setText(null);
					tvPriceValue.setText(null);
					for(syn.pos.data.model.SummaryTransaction.DisplaySummary displaySummary 
							: mSummaryTrans.TransactionSummary.DisplaySummaryList){
						tvSummaryDisplay.append(displaySummary.szDisplayName + "\n");
						tvPriceValue.append(globalVar.decimalFormat.format(displaySummary.fPriceValue) + "\n");
					}
					
					// for set member
					mTransactionId = mSummaryTrans.TransactionID;
					mComputerId = mSummaryTrans.ComputerID;
					
					tvBillCustNo.setText("(x" + globalVar.qtyFormat.format(mSummaryTrans.NoCustomer) + ")");
					
					if(!mSummaryTrans.TransacionName.equals("")){
						tvBillMember.setText(mSummaryTrans.TransacionName);
						billMemberLayout.setVisibility(View.VISIBLE);
						btnSetmember.setText(R.string.btn_clear_member);
						isSearchMember = false;
					}else{
						billMemberLayout.setVisibility(View.GONE);
					}
					
					//tvSubmitTime.setText(SUMMARY_TRANS.TransactionSummary);
					if(mSummaryTrans.CallForCheckBill > 0 && mSummaryTrans.CallForCheckBill != 99){
						btnCheckbill.setEnabled(true);
						if(GlobalVar.sIsEnablePrintLongBill)
							btnCheckbill.setText(R.string.print_long_bill);
						else if(GlobalVar.sIsEnableCallCheckBill)
							btnCheckbill.setText(mContext.getString(R.string.call_checkbill) + "(" + mSummaryTrans.CallForCheckBill + ")");
					}else{
						if(GlobalVar.sIsEnablePrintLongBill)
							btnCheckbill.setText(R.string.print_long_bill);
						else if(GlobalVar.sIsEnableCallCheckBill)
							btnCheckbill.setText(R.string.call_checkbill);
					}
					
					if(mSummaryTrans.CallForCheckBill == 99){
						if(!GlobalVar.sIsEnableBuffetType)
							disableButton();

						if(AppConfigLayoutActivity.isEnableBtPrinter(mContext))
							btnCheckbill.setEnabled(true);
					}
				}
				BillDetailAdapter billDetailAdapter = new BillDetailAdapter(mContext, 
						globalVar, mSummaryTrans);
				orderDetailListView.setAdapter(billDetailAdapter);
			} catch (Exception e) {
				IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, result, 0);
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
			property.setValue(mTableId);
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

					IOrderUtility.alertDialog(mContext, R.string.print_dialog_title, R.string.print_success, 0);
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

					IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, wsResult.getSzResultData().equals("") ? 
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

				IOrderUtility.alertDialog(mContext, R.string.global_dialog_title_error, result, 0);
			}
		}
	}

	@Override
	public void onSend(int transactionId, int computerId, double totalPrice, double payAmount) {
		if(payAmount >= mSummaryTrans.TransactionSummary.fTotalSalePrice){
			final ProgressDialog progress = new ProgressDialog(this);
			new PaymentCashDetail(mContext, globalVar, mTransactionId, mComputerId, String.valueOf(payAmount), 
					new ProgressListener(){

				@Override
				public void onPre() {
					progress.setMessage(mContext.getString(R.string.loading_progress));
					progress.show();
				}

				@Override
				public void onPost() {
					if(progress.isShowing())
						progress.dismiss();
					new AlertDialog.Builder(mContext)
					.setMessage(R.string.call_chekcbill_success)
					.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					})
					.show();
				}

				@Override
				public void onError(String msg) {
					if(progress.isShowing())
						progress.dismiss();
					new AlertDialog.Builder(mContext)
						.setMessage(msg)
						.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.show();
				}
				
			}).execute(GlobalVar.FULL_URL);
		}else{
			new AlertDialog.Builder(mContext)
			.setTitle(R.string.input_money)
			.setMessage(R.string.enter_enough_money)
			.setNeutralButton(R.string.global_btn_close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PayInfoFragment f = PayInfoFragment.newInstance(mTransactionId, mComputerId, mSummaryTrans.TransactionSummary.fTotalSalePrice);
					f.show(getFragmentManager(), "payinfo");
				}
			}).show();
		}
	}
}
