package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;
import org.ksoap2.serialization.PropertyInfo;
import com.google.gson.Gson;
import syn.pos.data.dao.MenuComment;
import syn.pos.data.dao.POSOrdering;
import syn.pos.data.dao.QuestionGroups;
import syn.pos.data.dao.SaleMode;
import syn.pos.data.dao.ShopProperty;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.MenuDataItem;
import syn.pos.data.model.MenuGroups;
import syn.pos.data.model.OrderHold;
import syn.pos.data.model.OrderTransaction;
import syn.pos.data.model.POSData_OrderTransInfo;
import syn.pos.data.model.ProductGroups;
import syn.pos.data.model.QueueInfo;
import syn.pos.data.model.ShopData;
import syn.pos.data.model.ShopData.SeatNo;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableInfo.TableName;
import syn.pos.data.model.TableInfo.TableZone;
import syn.pos.data.model.WebServiceResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.RadioGroup.LayoutParams;
import android.widget.TextView.OnEditorActionListener;
import android.view.inputmethod.EditorInfo;

public class TakeOrderActivity extends Activity{
	private Spinner mMenuGroupSpinner;
	private Spinner mMenuDeptSpinner;
	private GridView mMenuItemGridView;
	private ExpandableListView mOrderListView;
	private TextView mTvTotalPrice;
	private TextView mTvTotalQty;
	private TextView mTvTotalSeat;
	private Button mBtnSendByQueue;
	private Button mBtnSendOrder;
	private Button mBtnCheckDummyBill;
	private Button mBtnSetQueue;
	private Button mBtnSetTable;
	private Button mBtnSetmember;
	private LinearLayout mNotificationLayout;
	private TextView mTvNotification;
	private TextView mTvNotification2;
	private LinearLayout mPluLayout;
	private Button mBtnToggleMax;
	private LinearLayout mMenuItemLayout;
	private LinearLayout mSaleModeSwLayout;
	private Button mBtnPlu;
	private Button mBtnSeat;
	RelativeLayout mSaleModeTextLayout;

	private List<syn.pos.data.model.MenuDataItem> mOrderLst;
	private OrderListExpandableAdapter mOrderLstAdapter;
	private MenuGroupAdapter mMenuGroupAdapter;
	private MenuDeptAdapter mMenuDeptAdapter;
	private List<MenuGroups.MenuDept> mMenuDeptLst;

	private boolean mIsMaximize = false;
	private int mCurrTableId = 0; // selected tableId
	private String mCurrTableName = ""; // selected tableName
	private int mCustomerQty = 1; // selected customer qty
	private GlobalVar mGlobalVar;

	private int mCurrQueueId = 0;
	private String mCurrQueueName = "";

	private int mTransSaleMode = 1;
	private int mSaleMode = 1; // default Eat In
	private int mSeatId = 0;
	private String mSeatName = "";
	private int mCurrSeatId = mSeatId;
	private String mCurrSeatName = mSeatName;
	private int mCourseId = 0;
	private String mCourseName = "";
	private int mCurrCourseId = mCourseId;
	private String mCurrCourseName = mCourseName;
	private String mSaleModeWord = "";
	private String mSaleModeText = "";
	private int mSaleModePrefix = 0;
	private boolean mIsEnableQueue = false;
	private boolean mIsEnableSeat = false;
	private boolean mIsEnableSalemode = false;
	private boolean mIsEnableCourse = false;
	private String mSearchColumn = "";
	private boolean mAddOnlyOneItem = false;
	private int mCommentType = 0; // global

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take_order);
		setTitle("");
		
		mGlobalVar = new GlobalVar(this);
		// check register
		//if (IOrderUtility.checkRegister(TakeOrderActivity.this)) {
			// check config
			if (IOrderUtility.checkConfig(TakeOrderActivity.this)) {

				// check login
				if (GlobalVar.STAFF_ID != 0) {
					// initial component
					initComponent();

					// check feature
					checkProgramFeature();

					// check permissiom
					permissionChecking();
					
					// list menu
					listAllMenuItem();

					// load salemode
					createSwSaleMode();

				} else {
					Intent intent = new Intent(TakeOrderActivity.this,
							LoginActivity.class);
					TakeOrderActivity.this.startActivity(intent);
					TakeOrderActivity.this.finish();
				}
			} else {
				Intent intent = new Intent(TakeOrderActivity.this,
						AppConfigLayoutActivity.class);
				TakeOrderActivity.this.startActivity(intent);
				TakeOrderActivity.this.finish();
			}
//		} else {
//			Intent intent = new Intent(TakeOrderActivity.this,
//					RegisterActivity.class);
//			TakeOrderActivity.this.startActivity(intent);
//			TakeOrderActivity.this.finish();
//		}
	}

	@SuppressLint("NewApi")
	private void initComponent(){
		mMenuGroupSpinner = (Spinner) findViewById(R.id.spinnerMenuGroup);
		mMenuDeptSpinner = (Spinner) findViewById(R.id.spinnerMenuDept);
		mMenuItemGridView = (GridView) findViewById(R.id.gridViewMenuItem);
		mOrderListView = (ExpandableListView) findViewById(R.id.listViewQueueList);
		mNotificationLayout = (LinearLayout) findViewById(R.id.LinearLayoutNotification);
		mTvNotification = (TextView) findViewById(R.id.textViewNotification);
		mTvNotification2 = (TextView) findViewById(R.id.textViewNotification2);
		mTvTotalPrice = (TextView) findViewById(R.id.txtTotalSalePrice);
		mTvTotalQty = (TextView) findViewById(R.id.tvOrderQty);
		mTvTotalSeat = (TextView) findViewById(R.id.textViewTotalSeat);
		mBtnSetTable = (Button) findViewById(R.id.buttonSetTable);
		mBtnSetQueue = (Button) findViewById(R.id.buttonSetQueue);
		mBtnSendOrder = (Button) findViewById(R.id.btnSendOrder);
		mBtnSendByQueue = (Button) findViewById(R.id.buttonSendByQueue);
		mBtnCheckDummyBill = (Button) findViewById(R.id.buttonCheckDummbyBill);
		mBtnSetmember = (Button) findViewById(R.id.buttonSetMember);
		mBtnToggleMax = (Button) findViewById(R.id.btnToggleMax);
		mMenuItemLayout = (LinearLayout) findViewById(R.id.MenuItemLayout);
		mBtnPlu = (Button) findViewById(R.id.btnPlu);
		mPluLayout = (LinearLayout) findViewById(R.id.PLULayout); 
		mSaleModeSwLayout = (LinearLayout) findViewById(R.id.layoutSwSaleMode);
		mBtnSeat = (Button) findViewById(R.id.buttonSeat);
		mSaleModeTextLayout = (RelativeLayout) findViewById(R.id.saleModeTextLayout);
		
		mBtnSeat.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				popupSeat();
			}
			
		});

		mOrderListView.setGroupIndicator(null);
		mOrderListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {

				parent.setItemChecked(groupPosition, true);
				return false;
			}

		});
		
		mOrderListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				v.setSelected(true);
				return false;
			}

		});
		
		mBtnToggleMax.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsMaximize == false) {
					mMenuItemLayout.setVisibility(View.GONE);
					mPluLayout.setVisibility(View.GONE);
					mBtnToggleMax.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_find_previous_holo_light, 0, 0, 0);
					mIsMaximize = true;
				} else {
					mMenuItemLayout.setVisibility(View.VISIBLE);
					mBtnToggleMax.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_find_next_holo_light, 0, 0, 0);
					mIsMaximize = false;
				}
			}

		});

		mBtnSetmember.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TakeOrderActivity.this,
						SetMemberFromMain.class);
				intent.putExtra("TO_TRANSACTION_ID", mGlobalVar.TRANSACTION_ID);
				intent.putExtra("TO_COMPUTER_ID", mGlobalVar.COMPUTER_ID);
				TakeOrderActivity.this.startActivity(intent);
				overridePendingTransition(R.animator.slide_in_up,
						R.animator.slide_in_out);
			}

		});

		// shoptype fassfood
		if (mGlobalVar.SHOP_DATA.getShopType() == 2) {
			mBtnSetTable.setVisibility(View.GONE);
			mBtnSetQueue.setVisibility(View.GONE);
			mBtnSendByQueue.setVisibility(View.GONE);
			mBtnSeat.setVisibility(View.GONE);
		} else {
			mBtnSeat.setVisibility(View.VISIBLE);
			mBtnCheckDummyBill.setVisibility(View.GONE);
		}
		
		mBtnSetQueue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCurrTableId != 0) {
					final CustomDialog cusDialog = new CustomDialog(
							TakeOrderActivity.this, R.style.CustomDialog);
					cusDialog.title.setVisibility(View.VISIBLE);
					cusDialog.title
							.setText(R.string.global_dialog_title_warning);
					TextView tvMsg1 = new TextView(TakeOrderActivity.this);
					tvMsg1.setText(R.string.msg_already_set_table);
					TextView tvMsg2 = new TextView(TakeOrderActivity.this);
					tvMsg2.setText(mCurrTableName);
					TextView tvMsg3 = new TextView(TakeOrderActivity.this);
					tvMsg3.setText(R.string.cf_change_queue);
					cusDialog.message.setText(tvMsg1.getText().toString() + " "
							+ tvMsg2.getText().toString() + "\n"
							+ tvMsg3.getText().toString());
					cusDialog.btnCancel
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									cusDialog.dismiss();
								}

							});
					cusDialog.btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							cusDialog.dismiss();
							new SetQueueTask(TakeOrderActivity.this, mGlobalVar)
									.execute(GlobalVar.FULL_URL);
						}

					});
					cusDialog.show();
				} else {
					new SetQueueTask(TakeOrderActivity.this, mGlobalVar)
							.execute(GlobalVar.FULL_URL);
				}
			}

		});
		
		mBtnSetTable.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCurrQueueId != 0) {
					final CustomDialog cusDialog = new CustomDialog(
							TakeOrderActivity.this, R.style.CustomDialog);
					
					cusDialog.title.setVisibility(View.VISIBLE);
					cusDialog.title
							.setText(R.string.global_dialog_title_warning);
					TextView tvMsg1 = new TextView(TakeOrderActivity.this);
					tvMsg1.setText(R.string.msg_already_set_queue);
					TextView tvMsg2 = new TextView(TakeOrderActivity.this);
					tvMsg2.setText(mCurrQueueName);
					TextView tvMsg3 = new TextView(TakeOrderActivity.this);
					tvMsg3.setText(R.string.cf_change_table);
					cusDialog.message.setText(tvMsg1.getText().toString() + " "
							+ tvMsg2.getText().toString() + "\n"
							+ tvMsg3.getText().toString());
					cusDialog.btnCancel
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									cusDialog.dismiss();
								}

							});
					cusDialog.btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							cusDialog.dismiss();
							new SelectTableTask(TakeOrderActivity.this,
									mGlobalVar).execute(GlobalVar.FULL_URL);
						}

					});
					cusDialog.show();
				} else {
					new SelectTableTask(TakeOrderActivity.this, mGlobalVar)
							.execute(GlobalVar.FULL_URL);
				}
			}

		});

		mBtnPlu.setOnClickListener(new Button.OnClickListener() {
			EditText txtPluCode;

			@Override
			public void onClick(View v) {
				txtPluCode = (EditText) findViewById(R.id.txtPluCode);
				txtPluCode.setImeOptions (EditorInfo.IME_ACTION_SEARCH);
				ImageButton btnClose = (ImageButton) findViewById(R.id.imageButtonPluClose);
				Button btnPlu0 = (Button) findViewById(R.id.btnPlu0);
				Button btnPlu1 = (Button) findViewById(R.id.btnPlu1);
				Button btnPlu2 = (Button) findViewById(R.id.btnPlu2);
				Button btnPlu3 = (Button) findViewById(R.id.btnPlu3);
				Button btnPlu4 = (Button) findViewById(R.id.btnPlu4);
				Button btnPlu5 = (Button) findViewById(R.id.btnPlu5);
				Button btnPlu6 = (Button) findViewById(R.id.btnPlu6);
				Button btnPlu7 = (Button) findViewById(R.id.btnPlu7);
				Button btnPlu8 = (Button) findViewById(R.id.btnPlu8);
				Button btnPlu9 = (Button) findViewById(R.id.btnPlu9);
				Button btnPluEnter = (Button) findViewById(R.id.btnPluEnter);
				Button btnPluDash = (Button) findViewById(R.id.btnPluDash);
				Button btnPluClear = (Button) findViewById(R.id.btnPluClear);
				Button btnPluDelete = (Button) findViewById(R.id.btnPluDel);

				txtPluCode.setOnEditorActionListener(new OnEditorActionListener() {
				    @Override
				    public boolean onEditorAction(TextView v, int keyId, KeyEvent event) {
				       if(keyId == EditorInfo.IME_ACTION_SEARCH){
							new PluSearchTask().execute("");
							return true;
				       }
				       return false;
				    }
				});
				
				btnPlu0.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.append("0");
					}
				});
				btnPlu1.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.append("1");
					}
				});
				btnPlu2.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.append("2");
					}
				});
				btnPlu3.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.append("3");
					}
				});
				btnPlu4.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.append("4");
					}
				});
				btnPlu5.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.append("5");
					}
				});
				btnPlu6.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.append("6");
					}
				});
				btnPlu7.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.append("7");
					}
				});
				btnPlu8.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.append("8");
					}
				});
				btnPlu9.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.append("9");
					}
				});
				btnPluEnter.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!txtPluCode.getText().toString().equals("")) {
							new PluSearchTask().execute("");
						}
					}
				});
				btnPluDelete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String pluCode = txtPluCode.getText().toString();
						if (pluCode.length() > 0) {
							pluCode = pluCode.substring(0, pluCode.length() - 1);
							txtPluCode.setTextKeepState(pluCode);
						}
					}
				});
				btnPluClear.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.setText("");
					}
				});
				btnPluDash.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.append("-");
					}
				});

				btnClose.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mPluLayout.setVisibility(View.GONE);
						mMenuItemLayout.setVisibility(View.VISIBLE);
					}

				});

				mPluLayout.setVisibility(View.VISIBLE);
				mMenuItemLayout.setVisibility(View.GONE);
			}
		});

		mBtnCheckDummyBill.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mOrderLst.size() > 0) {
					final Dialog detailDialog = new Dialog(
							TakeOrderActivity.this,
							R.style.CustomDialogBottomRadius);
					LayoutInflater inflater = LayoutInflater
							.from(TakeOrderActivity.this);
					View orderView = inflater.inflate(
							R.layout.order_list_layout, null);
					ListView lvOrder = (ListView) orderView
							.findViewById(R.id.listViewOrder);
					TextView tvTitle = (TextView) orderView
							.findViewById(R.id.textViewOrderListTitle);
					TextView tvSumText = (TextView) orderView
							.findViewById(R.id.textViewSumText);
					TextView tvSumPrice = (TextView) orderView
							.findViewById(R.id.textViewSumPrice);
					ImageButton btnClose = (ImageButton) orderView
							.findViewById(R.id.imageButtonCloseOrderDialog);
					Button btnSendOrderFromSumm = (Button) orderView
							.findViewById(R.id.buttonSendFromSummary);
					btnSendOrderFromSumm.setVisibility(View.VISIBLE);

					ProgressBar progress = (ProgressBar) orderView
							.findViewById(R.id.progressBarOrderOfTable);

					new CheckSummaryBillDummyTask(TakeOrderActivity.this,
							mGlobalVar, lvOrder, tvSumText, tvSumPrice, progress)
							.execute(mGlobalVar.FULL_URL);
					tvTitle.setText(R.string.button_check_price);
					detailDialog.setContentView(orderView);
					detailDialog.getWindow().setGravity(Gravity.TOP);
					detailDialog.getWindow().setLayout(
							WindowManager.LayoutParams.MATCH_PARENT,
							WindowManager.LayoutParams.WRAP_CONTENT);
					detailDialog.getWindow().setWindowAnimations(
							R.style.DialogAnimation);

					btnClose.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							detailDialog.dismiss();
						}

					});

					btnSendOrderFromSumm
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									mBtnSendOrder.callOnClick();
									detailDialog.dismiss();
								}

							});

					detailDialog.show();
				} else {
					IOrderUtility.alertDialog(TakeOrderActivity.this,
							R.string.global_dialog_title_warning,
							R.string.no_order_msg, 0);
				}
			}

		});

		mBtnSendOrder.setOnClickListener(new Button.OnClickListener() {
			private void send(){
				POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
				List<syn.pos.data.model.MenuDataItem> ml = posOrder.listOrder(
						GlobalVar.TRANSACTION_ID, GlobalVar.COMPUTER_ID);

				if (ml.size() > 0) {
					/*
					 * check shop type shop type = 1 table shop type = 2 fast
					 * food
					 */
					if (mGlobalVar.SHOP_DATA.getShopType() == 2) {
						final SendOrderKeypadDialog dialog = new SendOrderKeypadDialog(
								mGlobalVar, TakeOrderActivity.this,
								R.style.CustomDialogBottomRadius);

						dialog.btnConfirm
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										if (!dialog.txtFastRef.getText()
												.toString().equals("")) {
											dialog.dismiss();
											if (mGlobalVar.MEMBER_ID == 0) {
												new SubmitSendOrder(
														TakeOrderActivity.this,
														mGlobalVar,
														"WSiOrder_JSON_SendFastFoodOrderTransactionData",
														dialog.txtFastRef
																.getText()
																.toString(),
														Integer.parseInt(dialog.tvCustQty
																.getText()
																.toString()))
														.execute(GlobalVar.FULL_URL);
											} else {
												new SubmitSendOrder(
														TakeOrderActivity.this,
														mGlobalVar,
														"WSiOrder_JSON_SendFastFoodOrderTransactionDataWithMemberID",
														dialog.txtFastRef
																.getText()
																.toString(),
														Integer.parseInt(dialog.tvCustQty
																.getText()
																.toString()))
														.execute(GlobalVar.FULL_URL);
											}
										} else {
											final CustomDialog customDialog = new CustomDialog(
													TakeOrderActivity.this,
													R.style.CustomDialog);
											customDialog.title
													.setVisibility(View.VISIBLE);
											customDialog.title
													.setText(R.string.global_dialog_title_error);
											customDialog.message
													.setText(R.string.cf_fastfood_title);
											customDialog.btnCancel
													.setVisibility(View.GONE);
											customDialog.btnOk
													.setText(R.string.global_close_dialog_btn);
											customDialog.btnOk
													.setOnClickListener(new OnClickListener() {

														@Override
														public void onClick(
																View v) {
															customDialog
																	.dismiss();
														}
													});
											customDialog.show();
										}
									}

								});
						dialog.btnCancel
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
										imm.hideSoftInputFromWindow(
												dialog.txtFastRef
														.getWindowToken(), 0);

										dialog.dismiss();
									}

								});

						dialog.getWindow().setGravity(Gravity.TOP);
						// dialog.getWindow()
						// .setSoftInputMode(
						// WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
						dialog.getWindow()
								.setLayout(
										android.view.ViewGroup.LayoutParams.MATCH_PARENT,
										android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
						dialog.getWindow().setWindowAnimations(
								R.style.DialogAnimation);

						dialog.show();
					}else if(mTransSaleMode != 1){
						final Dialog smDialog = new Dialog(TakeOrderActivity.this, R.style.CustomDialog);
						LayoutInflater inflater = LayoutInflater.from(TakeOrderActivity.this);
						View v = inflater.inflate(R.layout.send_sale_mode, null);
						TextView title = (TextView) v.findViewById(R.id.textView1);
						final EditText txtRef = (EditText) v.findViewById(R.id.editTextRef);
						final EditText txtMobile = (EditText) v.findViewById(R.id.editTextMobile);
						final TextView tvCustQty = (TextView) v.findViewById(R.id.textViewSaleModeCust);
						Button btnMinus = (Button) v.findViewById(R.id.buttonSaleModeMinus);
						Button btnPlus = (Button) v.findViewById(R.id.buttonSaleModePlus);
						Button btnCancel = (Button) v.findViewById(R.id.buttonSaleModeCancel);
						Button btnOk = (Button) v.findViewById(R.id.buttonSaleModeOk);
						
						title.setText(mSaleModeText);
						txtRef.requestFocus();
						
						btnMinus.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								int qty = Integer.parseInt(tvCustQty.getText().toString());
								if(--qty > 0){
									tvCustQty.setText(Integer.toString(qty));
									mCustomerQty = qty;
								}
							}
							
						});

						btnPlus.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								int qty = Integer.parseInt(tvCustQty.getText().toString());
								tvCustQty.setText(Integer.toString(++qty));
								mCustomerQty = qty;
							}
							
						});
						
						btnCancel.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								smDialog.dismiss();
							}
							
						});
						
						btnOk.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								mCustomerQty = 1;
								String ref = txtRef.getText().toString();
								String mobile = txtMobile.getText().toString();
								
								if(!mobile.isEmpty())
									ref += ":" + mobile;
								
								new SubmitSendOrder(TakeOrderActivity.this, mGlobalVar, "WSiOrder_JSON_SendSaleModeOrderTransactionData", 
										mTransSaleMode, ref, mCustomerQty).execute(GlobalVar.FULL_URL);
								smDialog.dismiss();
							}
							
						});
						
						smDialog.setContentView(v);
						smDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
								WindowManager.LayoutParams.WRAP_CONTENT);
						smDialog.show();
					}else {
						if (mCurrQueueId != 0) {
							final CustomDialog cusDialog = new CustomDialog(
									TakeOrderActivity.this,
									R.style.CustomDialog);
							cusDialog.title.setVisibility(View.VISIBLE);
							cusDialog.title
									.setText(R.string.global_dialog_title_warning);
							TextView tvMsg1 = new TextView(
									TakeOrderActivity.this);
							tvMsg1.setText(R.string.msg_already_set_queue);
							TextView tvMsg2 = new TextView(
									TakeOrderActivity.this);
							tvMsg2.setText(mCurrQueueName);
							TextView tvMsg3 = new TextView(
									TakeOrderActivity.this);
							tvMsg3.setText(R.string.cf_change_table);
							cusDialog.message.setText(tvMsg1.getText()
									.toString()
									+ " "
									+ tvMsg2.getText().toString()
									+ "\n"
									+ tvMsg3.getText().toString());
							cusDialog.btnCancel
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											cusDialog.dismiss();
										}

									});
							cusDialog.btnOk
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											cusDialog.dismiss();
											new LoadTableTaskQuestion(
													TakeOrderActivity.this,
													mGlobalVar)
													.execute(GlobalVar.FULL_URL);
										}

									});
							cusDialog.show();
						} else {
							new LoadTableTaskQuestion(TakeOrderActivity.this, mGlobalVar).execute(GlobalVar.FULL_URL);
						}
					}
				} else {
					IOrderUtility.alertDialog(TakeOrderActivity.this,
							R.string.global_dialog_title_warning,
							R.string.no_order_msg, 0);
				}

				// update IsOutOfStock
				new IOrderUtility.CheckOutOfProductTask(TakeOrderActivity.this,
						mGlobalVar).execute(GlobalVar.FULL_URL);
			}
			
			@Override
			public void onClick(View v) {
				new IOrderUtility.CompareSaleDateTask(TakeOrderActivity.this,
						mGlobalVar, new WebServiceStateListener() {

							@Override
							public void onSuccess() {
								send();
							}

							@Override
							public void onNotSuccess() {
								final CustomDialog cusDialog = new CustomDialog(TakeOrderActivity.this, R.style.CustomDialog);
								cusDialog.title.setVisibility(View.VISIBLE);
								cusDialog.title.setText(R.string.global_dialog_title_warning);
								cusDialog.message.setText(R.string.session_expire);
								cusDialog.btnCancel.setVisibility(View.GONE);
								//cusDialog.btnOk.setBackgroundResource(R.drawable.green_button);
								cusDialog.btnOk.setOnClickListener(new OnClickListener(){

									@Override
									public void onClick(View v) {
										cusDialog.dismiss();
										Intent intent = new Intent(
												TakeOrderActivity.this,
												LoginActivity.class);
										TakeOrderActivity.this
												.startActivity(intent);
										TakeOrderActivity.this.finish();
									}});
								cusDialog.show();
								
							}
						}).execute(GlobalVar.FULL_URL);
			}
		});

		mBtnSendByQueue.setOnClickListener(new Button.OnClickListener() {
			private void send(){
				POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
				List<syn.pos.data.model.MenuDataItem> ml = posOrder.listOrder(
						GlobalVar.TRANSACTION_ID, GlobalVar.COMPUTER_ID);

				if (ml.size() > 0) {
					if (mCurrTableId != 0) {
						final CustomDialog cusDialog = new CustomDialog(
								TakeOrderActivity.this, R.style.CustomDialog);
						cusDialog.title.setVisibility(View.VISIBLE);
						cusDialog.title
								.setText(R.string.global_dialog_title_warning);
						TextView tvMsg1 = new TextView(TakeOrderActivity.this);
						tvMsg1.setText(R.string.msg_already_set_table);
						TextView tvMsg2 = new TextView(TakeOrderActivity.this);
						tvMsg2.setText(mCurrTableName);
						tvMsg2.setTextSize(42);
						TextView tvMsg3 = new TextView(TakeOrderActivity.this);
						tvMsg3.setText(R.string.cf_change_queue);
						cusDialog.message.setText(tvMsg1.getText().toString()
								+ " " + tvMsg2.getText().toString() + "\n"
								+ tvMsg3.getText().toString());
						cusDialog.btnCancel
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										cusDialog.dismiss();
									}

								});
						cusDialog.btnOk
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										cusDialog.dismiss();
										new LoadQueueTask(
												TakeOrderActivity.this,
												mGlobalVar)
												.execute(GlobalVar.FULL_URL);
									}

								});
						cusDialog.show();
					} else {
						new LoadQueueTask(TakeOrderActivity.this, mGlobalVar)
								.execute(GlobalVar.FULL_URL);
					}
				} else {
					IOrderUtility.alertDialog(TakeOrderActivity.this,
							R.string.global_dialog_title_warning,
							R.string.no_order_msg, 0);
				}

				// update IsOutOfStock
				new IOrderUtility.CheckOutOfProductTask(TakeOrderActivity.this,
						mGlobalVar).execute(GlobalVar.FULL_URL);
				
			}
			
			@Override
			public void onClick(View v) {
				new IOrderUtility.CompareSaleDateTask(TakeOrderActivity.this,
						mGlobalVar, new WebServiceStateListener() {

							@Override
							public void onSuccess() {
								send();
							}

							@Override
							public void onNotSuccess() {
								final CustomDialog cusDialog = new CustomDialog(TakeOrderActivity.this, R.style.CustomDialog);
								cusDialog.title.setVisibility(View.VISIBLE);
								cusDialog.title.setText(R.string.global_dialog_title_warning);
								cusDialog.message.setText(R.string.session_expire);
								cusDialog.btnCancel.setVisibility(View.GONE);
								//cusDialog.btnOk.setBackgroundResource(R.drawable.green_button);
								cusDialog.btnOk.setOnClickListener(new OnClickListener(){

									@Override
									public void onClick(View v) {
										cusDialog.dismiss();
										Intent intent = new Intent(
												TakeOrderActivity.this,
												LoginActivity.class);
										TakeOrderActivity.this
												.startActivity(intent);
										TakeOrderActivity.this.finish();
									}});
								cusDialog.show();
								
							}
						}).execute(GlobalVar.FULL_URL);
			}
		});
	}
	
	@Override
	protected void onResume() {
		iOrderInit();
		countHoldOrder();
		
		// param from QueueActivity
		Intent intent = getIntent();
		if (intent.getIntExtra("QUEUE_ID", 0) != 0) {
			
			int queueId = intent.getIntExtra("QUEUE_ID", 0);
			String queueName = intent
					.getStringExtra("QUEUE_NAME");
			int queueQty = intent
					.getIntExtra("CUSTOMER_QTY", 1);

			new LoadPreOrderTask(TakeOrderActivity.this,
					mGlobalVar, queueId, queueName, queueQty)
					.execute(GlobalVar.FULL_URL);
		}
		
		intent.removeExtra("QUEUE_ID");
		intent.removeExtra("QUEUE_NAME");
		intent.removeExtra("CUSTOMER_QTY");

		// set member name to button setmember
		if (mGlobalVar.MEMBER_NAME != "") {
			setSelectedMember();
		} else {
			clearSelectedMember();
		}
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Handle the back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	private void checkProgramFeature() {
		// show by config
		List<ShopData.ProgramFeature> featureList = mGlobalVar.PROGRAM_FEATURE;
		if (featureList.size() > 0) {
			for (ShopData.ProgramFeature feature : featureList) {
				switch (feature.getFeatureID()) {
				// queue feature
				case 1:
					if (feature.getFeatureValue() == 1) {
						mIsEnableQueue = true;
						mBtnSetQueue.setVisibility(View.VISIBLE);
						mBtnSendByQueue.setVisibility(View.VISIBLE);
					} else {
						mIsEnableQueue = false;
						mBtnSetQueue.setVisibility(View.GONE);
						mBtnSendByQueue.setVisibility(View.GONE);
					}

					break;
				// search column feature
				case 2:
					if (feature.getFeatureValue() == 1) {
						if (feature.getFeatureText() != "") {
							mSearchColumn = feature.getFeatureText();
						}
					}
					break;

				// add every one item 
				case 3:
					if(feature.getFeatureValue() == 1){
						mAddOnlyOneItem = true;
					}else{
						mAddOnlyOneItem = false;
					}
					break;
				case 4:
					if(feature.getFeatureValue() != 0){
						GlobalVar.isEnableTableQuestion = true;
					}else{
						GlobalVar.isEnableTableQuestion = false;
					}
					break;
				case 5:
					mCommentType = feature.getFeatureValue();
					break;
				case 6:
					if(feature.getFeatureValue() == 1){
						if(mGlobalVar.SHOP_DATA.getShopType() == 1){
							mBtnSeat.setVisibility(View.VISIBLE);
							mTvTotalSeat.setVisibility(View.VISIBLE);
							mIsEnableSeat = true;
							clearSeat();
						}
					}else{
						mIsEnableSeat = false;
						mBtnSeat.setVisibility(View.GONE);
						mTvTotalSeat.setVisibility(View.GONE);
					}
					break;
				case 7:
					if(feature.getFeatureValue() == 1){
						mIsEnableSalemode = true;
					}else{
						mIsEnableSalemode = false;
					}
					break;
				case 8:
					if(feature.getFeatureValue() == 1){
						mIsEnableCourse = true;
					}else{
						mIsEnableCourse = false;
					}
					break;
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_take_order, menu);

		SubMenu sub = menu.findItem(R.id.item_other).getSubMenu();
		SaleMode saleMode = new SaleMode(TakeOrderActivity.this);
		int[] saleModeId = {1,2};
		List<ProductGroups.SaleMode> saleModeLst = saleMode.listSaleMode(saleModeId);
		
		if(saleModeLst != null && saleModeLst.size() > 0){
			
			SubMenu subSaleMode = sub.addSubMenu("SaleMode");
			for(final ProductGroups.SaleMode s : saleModeLst){
				subSaleMode.addSubMenu(0, s.getSaleModeID(), 
						0, s.getSaleModeName());
			}
		}
		
		SubMenu subStaff = sub.addSubMenu(0, R.id.menu_logout, 0, "Log Out | " + GlobalVar.STAFF_NAME);
		//subStaff.setIcon(R.drawable.ic_action_staff);
		
		// fast food type
		if (mGlobalVar.SHOP_DATA.getShopType() == 2) {
			menu.findItem(R.id.table_util).setVisible(false);
			menu.findItem(R.id.menu_util).setVisible(false);
			menu.findItem(R.id.checkbill).setVisible(false);
			menu.findItem(R.id.menu_manage_queue).setVisible(false);
		}

		if (mIsEnableQueue) {
			menu.findItem(R.id.menu_manage_queue).setVisible(true);
		}else{
			menu.findItem(R.id.menu_manage_queue).setVisible(false);
		}
		
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {

		case R.id.action_moveTable:
			intent = new Intent(TakeOrderActivity.this, MoveMergeTable.class);
			intent.putExtra("func", 1); // 1=move
			TakeOrderActivity.this.startActivity(intent);
			overridePendingTransition(R.animator.slide_in_up,
					R.animator.slide_in_out);
			return true;
		case R.id.action_mergeTable:
			intent = new Intent(TakeOrderActivity.this, MoveMergeTable.class);
			intent.putExtra("func", 2); // 1=merge
			TakeOrderActivity.this.startActivity(intent);
			overridePendingTransition(R.animator.slide_in_up,
					R.animator.slide_in_out);
			return true;
		case R.id.action_comment_trans:
			intent = new Intent(TakeOrderActivity.this, CommentTransactionActivity.class);
			startActivity(intent);
			overridePendingTransition(R.animator.slide_in_up,
					R.animator.slide_in_out);
			return true;
		case R.id.action_move_menu:
			// moveMenu();
			intent = new Intent(TakeOrderActivity.this, MoveMenuActivity.class);
			TakeOrderActivity.this.startActivity(intent);
			overridePendingTransition(R.animator.slide_in_up,
					R.animator.slide_in_out);
			return true;
		case R.id.action_cancel_menu:
			intent = new Intent(TakeOrderActivity.this,
					CancelMenuActivity.class);
			TakeOrderActivity.this.startActivity(intent);
			overridePendingTransition(R.animator.slide_in_up,
					R.animator.slide_in_out);
			return true;
		case R.id.action_reprint:
			intent = new Intent(TakeOrderActivity.this,
					ReprintMenuActivity.class);
			TakeOrderActivity.this.startActivity(intent);
			overridePendingTransition(R.animator.slide_in_up,
					R.animator.slide_in_out);
			return true;
		case R.id.menu_logout:
			logOut();
			return true;
		case R.id.checkbill:
			intent = new Intent(TakeOrderActivity.this, CheckBillActivity.class);
			TakeOrderActivity.this.startActivity(intent);
			overridePendingTransition(R.animator.slide_in_up,
					R.animator.slide_in_out);
			return true;
		case R.id.menu_manage_queue:
			intent = new Intent(TakeOrderActivity.this, QueueActivity.class);
			TakeOrderActivity.this.startActivity(intent);
			overridePendingTransition(R.animator.slide_in_up,
					R.animator.slide_in_out);
			return true;
		case R.id.action_kds:
			intent = new Intent(TakeOrderActivity.this, KdsInfoActivity.class);
			TakeOrderActivity.this.startActivity(intent);
			overridePendingTransition(R.animator.slide_in_up,
					R.animator.slide_in_out);
			return true;
		case R.id.hold_order:
			holdOrder();
			return true;
		case R.id.order_hold:
			displayHoldOrder();
			return true;
		case R.id.menu_clear_order:
			clearOrder();
			return true;
		case 1: // salemode1
			switchSaleMode(1);
			return true;
		case 2: // salemode2
			switchSaleMode(2);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void switchSaleMode(int saleModeId){
		
		SaleMode saleMode = new SaleMode(TakeOrderActivity.this);
		final ProductGroups.SaleMode s = saleMode.getSaleMode(saleModeId);

		if(s.getSaleModeID() != mTransSaleMode){
			if(mOrderLst.size() > 0){
				final CustomDialog cusDialog = new CustomDialog(TakeOrderActivity.this, R.style.CustomDialog);
				cusDialog.title.setVisibility(View.VISIBLE);
				cusDialog.title.setText(R.string.global_dialog_title_warning);
				cusDialog.message.setText("Are you sure you want to change sale mode?");
				cusDialog.btnCancel.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						cusDialog.dismiss();
					}
					
				});
				cusDialog.btnOk.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						clearOrderList();
						setTransSaleMode(s);
						
						cusDialog.dismiss();
					}
					
				});
				cusDialog.show();
			}else{
				setTransSaleMode(s);
			}

			if(mTransSaleMode != 1){
				clearSetQueue();
				clearSetTable();
			}
			
			refreshPluResult();
		}
	}
	
	private void clearOrder() {
		LayoutInflater inflater = LayoutInflater.from(TakeOrderActivity.this);
		View v = inflater.inflate(R.layout.custom_dialog, null);

		TextView tvTitle = (TextView) v.findViewById(R.id.textViewTitle);
		TextView tvMessage = (TextView) v.findViewById(R.id.textViewMessage);
		Button btnOk = (Button) v.findViewById(R.id.buttonOk);
		Button btnCancel = (Button) v.findViewById(R.id.buttonConfirmCancel);

		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText(R.string.clear_order_title);
		tvMessage.setText(R.string.msg_clear_order);
		btnOk.setText(R.string.global_btn_yes);
		btnCancel.setText(R.string.global_btn_no);

		final Dialog dialog = new Dialog(TakeOrderActivity.this,
				R.style.CustomDialog);
		dialog.setContentView(v);
		dialog.show();

		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				POSOrdering pos = new POSOrdering(TakeOrderActivity.this);
				// pos.clearOrder();
				pos.deleteOrderDetail(GlobalVar.TRANSACTION_ID);

				countHoldOrder();

				GlobalVar.TRANSACTION_ID = 0;
				iOrderInit();

				dialog.dismiss();
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	private void logOut() {
		LayoutInflater inflater = LayoutInflater.from(TakeOrderActivity.this);
		View v = inflater.inflate(R.layout.custom_dialog, null);

		TextView tvTitle = (TextView) v.findViewById(R.id.textViewTitle);
		TextView tvMessage = (TextView) v.findViewById(R.id.textViewMessage);
		Button btnOk = (Button) v.findViewById(R.id.buttonOk);
		Button btnCancel = (Button) v.findViewById(R.id.buttonConfirmCancel);

		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText(R.string.dialog_logout_title);
		tvMessage.setText(R.string.dialog_logout_message);
		btnOk.setText(R.string.btn_ok_logout);
		btnCancel.setText(R.string.btn_cancel_logout);

		final Dialog dialog = new Dialog(TakeOrderActivity.this,
				R.style.CustomDialog);
		dialog.setContentView(v);
		dialog.show();

		btnOk.setOnClickListener(new OnClickListener() {

			private void clearOrder(){
				POSOrdering pos = new POSOrdering(TakeOrderActivity.this);
				pos.deleteOrderDetail(GlobalVar.TRANSACTION_ID);
				
				GlobalVar.STAFF_ID = 0;
				
				Intent intent = new Intent(TakeOrderActivity.this,
						LoginActivity.class);
				TakeOrderActivity.this.startActivity(intent);
				TakeOrderActivity.this.finish();
			}
			
			@Override
			public void onClick(View v) {
				if(mOrderLst.size() > 0){
					final CustomDialog cusDialog = 
							new CustomDialog(TakeOrderActivity.this, R.style.CustomDialog);
					cusDialog.title.setVisibility(View.VISIBLE);
					cusDialog.title.setText(R.string.dialog_logout_title);
					cusDialog.message.setText(R.string.warn_clear_order);
					cusDialog.btnCancel.setVisibility(View.VISIBLE);
					cusDialog.btnCancel.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							cusDialog.dismiss();
						}
						
					});
					cusDialog.btnOk.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							clearOrder();
							cusDialog.dismiss();
						}
					});
					cusDialog.show();
				}else{
					clearOrder();
				}
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	private class ViewOrderListAdapter extends BaseAdapter {
		private List<OrderHold> holdLst;
		private LayoutInflater inflater = null;
		private Dialog holdDialog;

		public ViewOrderListAdapter(List<OrderHold> holdList, Dialog holdDialog) {
			holdLst = holdList;
			inflater = LayoutInflater.from(TakeOrderActivity.this);
			this.holdDialog = holdDialog;
		}

		@Override
		public int getCount() {
			return holdLst != null ? holdLst.size() : 0;
		}

		@Override
		public OrderHold getItem(int position) {
			return holdLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View v, ViewGroup viewGroup) {
			final OrderHold hold = holdLst.get(position);
			ViewHolder holder;
			if (v == null) {
				holder = new ViewHolder();
				v = inflater.inflate(R.layout.pending_list_template, null);

				holder.tvExtraRemark = (TextView) v
						.findViewById(R.id.textViewExtraRemark);
				holder.tvPendingNo = (TextView) v
						.findViewById(R.id.textViewPendingNo);
				holder.tvPendingDate = (TextView) v
						.findViewById(R.id.textViewPendingDate);
				holder.tvPendingOrderQty = (TextView) v
						.findViewById(R.id.textViewPendingOrderQty);
				holder.tvPendingRemark = (TextView) v
						.findViewById(R.id.textViewPendingRemark);

				holder.btnPendingEdit = (Button) v
						.findViewById(R.id.buttonPendingEdit);
				holder.btnPendingSend = (Button) v
						.findViewById(R.id.buttonPendingSend);

				v.setTag(holder);
			} else {
				holder = (ViewHolder) v.getTag();
			}

			holder.btnPendingEdit.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mOrderLst.size() > 0) {
						IOrderUtility.alertDialog(TakeOrderActivity.this,
								R.string.global_dialog_title_warning,
								R.string.hold_curr_order_msg, 0);
					} else {
						holdDialog.dismiss();

						GlobalVar.TRANSACTION_ID = hold.getTransactionID();

						POSOrdering pos = new POSOrdering(
								TakeOrderActivity.this);
						pos.prepareTransaction(GlobalVar.TRANSACTION_ID,
								GlobalVar.COMPUTER_ID, GlobalVar.STAFF_ID, "");

						clearSeat();
						listAllOrder();
						countHoldOrder();
					}
				}

			});
			holder.btnPendingSend.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

				}

			});

			holder.tvPendingNo.setText(position + 1 + ".");
			holder.tvPendingDate.setText(hold.getUpdateDate());
			holder.tvPendingRemark.setText(hold.getTransactionNote());
			holder.tvPendingOrderQty.setText(mGlobalVar.qtyDecimalFormat.format(hold
					.getHoldQty()));

			if (!hold.getQueueName().equals("")) {
				holder.tvExtraRemark.setVisibility(View.VISIBLE);
				holder.tvExtraRemark.setText("Queue:" + hold.getQueueName());
			}
			if (!hold.getTableName().equals("")) {
				holder.tvExtraRemark.setVisibility(View.VISIBLE);
				holder.tvExtraRemark.setText("Table:" + hold.getTableName());
			}
			return v;
		}

		private class ViewHolder {
			TextView tvPendingNo;
			TextView tvPendingDate;
			TextView tvPendingOrderQty;
			TextView tvPendingRemark;
			TextView tvExtraRemark;

			Button btnPendingEdit;
			Button btnPendingSend;
		}

	}

	private void displayHoldOrder() {
		LayoutInflater factory = LayoutInflater.from(TakeOrderActivity.this);
		View v = factory.inflate(R.layout.pending_list_layout, null);
		Button btnPendingSendAll = (Button) v
				.findViewById(R.id.buttonSendPendingAll);
		ImageButton btnPendingClose = (ImageButton) v
				.findViewById(R.id.imageButtonPendingClose);

		final POSOrdering pos = new POSOrdering(TakeOrderActivity.this);
		List<OrderHold> holdLst = pos.listHoldOrder();

		final Dialog orderHoldDialog = new Dialog(TakeOrderActivity.this,
				R.style.CustomDialogBottomRadius);
		orderHoldDialog.setContentView(v);
		orderHoldDialog.getWindow()
				.setWindowAnimations(R.style.DialogAnimation);
		orderHoldDialog.getWindow().setGravity(Gravity.TOP);
		orderHoldDialog.getWindow().setLayout(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		orderHoldDialog.show();

		btnPendingSendAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}

		});

		btnPendingClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				orderHoldDialog.dismiss();
			}

		});
		ListView orderListView = (ListView) v
				.findViewById(R.id.listViewPending);
		orderListView.setAdapter(new ViewOrderListAdapter(holdLst,
				orderHoldDialog));
	}

	private void countHoldOrder() {
		POSOrdering pos = new POSOrdering(TakeOrderActivity.this);
		int totalHold = pos.countHoldOrder(GlobalVar.COMPUTER_ID);
	}

	protected void iOrderInit() {
		POSOrdering pos = new POSOrdering(TakeOrderActivity.this);
		GlobalVar.TRANSACTION_ID = pos
				.getCurrentTransaction(GlobalVar.COMPUTER_ID);

		if (GlobalVar.TRANSACTION_ID == 0) {
			GlobalVar.TRANSACTION_ID = pos.openTransaction(
					GlobalVar.COMPUTER_ID, GlobalVar.SHOP_ID,
					GlobalVar.STAFF_ID, 0, 0, 0);
		}
		listAllOrder();
	}

	public void listAllOrder() {
		POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
		mOrderLst = posOrder.listAllOrder(GlobalVar.TRANSACTION_ID,
				GlobalVar.COMPUTER_ID, mSeatId);

		mOrderLstAdapter = new OrderListExpandableAdapter();
		mOrderListView.setAdapter(mOrderLstAdapter);
		// orderListView.setSelected(true);
		// orderListView.setItemChecked(orderListView.getCount(), true);
		mOrderListView.setSelection(mOrderLst.size());

		getTableName();
		getQueueName();
		// orderListView.smoothScrollToPosition(orderListView.getCount());
		summaryTotalSalePrice();
	}

	private class SummaryTotalSaleTask extends
			AsyncTask<String, Boolean, double[]> {

		@Override
		protected void onPostExecute(double[] result) {
			double totalQty = result[0];
			double totalSalePrice = result[1];
			Log.d("sendorder clear total", "" + result[0]);
			mTvTotalQty.setText(mGlobalVar.qtyDecimalFormat.format(totalQty));
			mTvTotalPrice
					.setText(mGlobalVar.decimalFormat.format(totalSalePrice));
		}

		@Override
		protected double[] doInBackground(String... params) {
			double totalQty = 0.0d;
			double totalPrice = 0.0d;

			// POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
			// totalQty = posOrder.summaryTotalQty(GlobalVar.TRANSACTION_ID,
			// GlobalVar.COMPUTER_ID);
			// totalPrice = posOrder.summaryTotalPrice(GlobalVar.TRANSACTION_ID,
			// GlobalVar.COMPUTER_ID);

			if (mOrderLst != null && mOrderLst.size() > 0) {
				for (MenuDataItem order : mOrderLst) {
					double qty = order.getProductQty();
					double productPrice = order.getPricePerUnit();

					totalQty += qty;
					totalPrice += productPrice * qty;

					if (order.menuCommentList != null
							&& order.menuCommentList.size() > 0) {
						double totalCommentPrice = 0.0d;

						for (MenuGroups.MenuComment mc : order.menuCommentList) {
							double commentQty = mc.getCommentQty();
							double commentPrice = mc.getProductPricePerUnit();

							totalCommentPrice += (commentPrice * commentQty)
									* qty;
						}
						totalPrice += totalCommentPrice;
					}

					if (order.pCompSetLst != null
							&& order.pCompSetLst.size() > 0) {
						double totalSetPrice = 0.0d;
						for (ProductGroups.PComponentSet pcs : order.pCompSetLst) {
							double setQty = pcs.getProductQty();
							double setPrice = pcs.getPricePerUnit();

							totalSetPrice += setPrice * setQty;

							if (pcs.menuCommentList != null
									&& pcs.menuCommentList.size() > 0) {
								double totalSetCommentPrice = 0.0d;
								for (MenuGroups.MenuComment setMc : pcs.menuCommentList) {
									double setCommentQty = setMc
											.getCommentQty();
									double setCommentPrice = setMc
											.getProductPricePerUnit();

									totalSetCommentPrice += (setCommentPrice * setCommentQty)
											* setQty;
								}
								totalSetPrice += totalSetCommentPrice;
							}

						}
						totalPrice += totalSetPrice;
					}
				}
			}

			double[] totalSale = { totalQty, totalPrice };
			return totalSale;
		}

	}

	public void summaryTotalSalePrice() {
		new SummaryTotalSaleTask().execute("");

	}

	private class MenuDeptAdapter extends BaseAdapter{
		private LayoutInflater inflater;
		public MenuDeptAdapter(){
			inflater = LayoutInflater.from(TakeOrderActivity.this);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mMenuDeptLst != null ? mMenuDeptLst.size() : 0;
		}

		@Override
		public MenuGroups.MenuDept getItem(int position) {
			// TODO Auto-generated method stub
			return mMenuDeptLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MenuGroups.MenuDept md = mMenuDeptLst.get(position);
			ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.spinner_item, null);
				holder = new ViewHolder();
				holder.tvDeptName = (TextView) convertView;
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvDeptName.setText(md.getMenuDeptName_0());
			
			return convertView;
		}
		
		private class ViewHolder{
			TextView tvDeptName;
		}
	}
	
	private class MenuGroupAdapter extends BaseAdapter{
		private List<MenuGroups.MenuGroup> mgl;
		private LayoutInflater inflater;
		public MenuGroupAdapter(List<MenuGroups.MenuGroup> mgl){
			this.mgl = mgl;
			inflater = LayoutInflater.from(TakeOrderActivity.this);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mgl != null ? mgl.size() : 0;
		}

		@Override
		public MenuGroups.MenuGroup getItem(int position) {
			return mgl.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MenuGroups.MenuGroup mg = mgl.get(position);
			ViewHolder holder;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.spinner_item, null);
				holder = new ViewHolder();
				holder.tvGroupName = (TextView) convertView;
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag(); 
			}
			holder.tvGroupName.setText(mg.getMenuGroupName_0());
			return convertView;
		}
		
		private class ViewHolder{
			TextView tvGroupName;
		}
		
	}
	
	private void clearOrderList(){
		POSOrdering pos = new POSOrdering(TakeOrderActivity.this);
		// pos.clearOrder();
		pos.deleteOrderDetail(GlobalVar.TRANSACTION_ID);

		countHoldOrder();

		GlobalVar.TRANSACTION_ID = 0;
		iOrderInit();

	}
	
	private void refreshMenu(){
		int selMenuDeptPos = mMenuDeptSpinner
				.getSelectedItemPosition();
		
		MenuGroups.MenuDept md = 
				(MenuGroups.MenuDept) mMenuDeptSpinner.getItemAtPosition(selMenuDeptPos);
		
		// load menu task
		new LoadMenuTask(md.getMenuDeptID()).execute("");
	}
	
	private void setSaleMode(ProductGroups.SaleMode s){
		mSaleMode = s.getSaleModeID();
		mSaleModeWord = s.getPrefixText();
		mSaleModePrefix = s.getPositionPrefix();
		mSaleModeText = s.getSaleModeName();
		
		Button btnEatIn = (Button) mSaleModeSwLayout.findViewById(1);
		Button btnTakeAway = (Button) mSaleModeSwLayout.findViewById(2);
		if(s.getSaleModeID() == 1)
		{
			if(btnEatIn != null)
				btnEatIn.setSelected(true);
			if(btnTakeAway != null)
				btnTakeAway.setSelected(false);
		}else if(s.getSaleModeID() == 2){
			if(btnEatIn != null)
				btnEatIn.setSelected(false);
			if(btnTakeAway != null)
				btnTakeAway.setSelected(true);
		}
		refreshMenu();
	}
	
	private void setTransSaleMode(ProductGroups.SaleMode s){
		mTransSaleMode = s.getSaleModeID();
		mSaleMode = s.getSaleModeID();
		mSaleModeWord = s.getPrefixText();
		mSaleModePrefix = s.getPositionPrefix();
		mSaleModeText = s.getSaleModeName();

		showSaleModeText(s.getSaleModeName());
		
		Button btnEatIn = (Button) mSaleModeSwLayout.findViewById(1);
		Button btnTakeAway = (Button) mSaleModeSwLayout.findViewById(2);

		if(mTransSaleMode != 1){
			if(!mIsEnableSalemode){
				try {
					btnEatIn.setEnabled(false);
					btnTakeAway.setEnabled(false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				btnEatIn.setSelected(false);
				btnTakeAway.setSelected(true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(mTransSaleMode == 1){
			if(btnEatIn != null){
				btnEatIn.setEnabled(true);
				btnEatIn.setSelected(true);
			}
			if(btnTakeAway != null){
				btnTakeAway.setEnabled(true);
				btnTakeAway.setSelected(false);
			}
			if(!mIsEnableSalemode){
				hideSaleModeText();
			}
		}
		refreshMenu();
	}
	
	private void listAllMenuItem() {
		syn.pos.data.dao.MenuGroup mg = 
				new syn.pos.data.dao.MenuGroup(TakeOrderActivity.this);

		List<MenuGroups.MenuGroup> mgl = mg.listAllMenuGroup();

		mMenuGroupAdapter = new MenuGroupAdapter(mgl);
		
		mMenuGroupSpinner.setAdapter(mMenuGroupAdapter);

		mMenuGroupSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent, View v,
							int position, long id) {
						MenuGroups.MenuGroup mg = (MenuGroups.MenuGroup) parent
								.getItemAtPosition(position);
						syn.pos.data.dao.MenuDept md = new syn.pos.data.dao.MenuDept(
								TakeOrderActivity.this, mg.getMenuGroupID());

						mMenuDeptLst = md.listMenuDept();
						mMenuDeptAdapter = new MenuDeptAdapter();

						mMenuDeptSpinner.setAdapter(mMenuDeptAdapter);
						mMenuDeptSpinner
								.setOnItemSelectedListener(new OnItemSelectedListener() {

									@Override
									public void onItemSelected(
											AdapterView<?> parent, View v,
											int position, long id) {

										MenuGroups.MenuDept md = (MenuGroups.MenuDept) parent
												.getItemAtPosition(position);

										// load menu task
										new LoadMenuTask(md.getMenuDeptID()).execute("");
									}

									@Override
									public void onNothingSelected(
											AdapterView<?> arg0) {
									}
								});
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}
				});

	}
	
	private class LoadMenuTask extends AsyncTask<String, Boolean, List<MenuDataItem>>{
		private int menuDeptId;
		private ProgressBar progress;
		public LoadMenuTask(int menuDeptId){
			this.menuDeptId = menuDeptId;
			progress = (ProgressBar) findViewById(R.id.progressBarLoadMenu);
		}
		
		@Override
		protected void onPostExecute(List<MenuDataItem> dataItem) {
			progress.setVisibility(View.GONE);
			mMenuItemGridView.setVisibility(View.VISIBLE);
			
			mMenuItemGridView
					.setAdapter(new MenuItemAdapter(
							dataItem));
		}

		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
			mMenuItemGridView.setVisibility(View.GONE);
		}

		@Override
		protected List<MenuDataItem> doInBackground(String... params) {
			syn.pos.data.dao.MenuItem item = new syn.pos.data.dao.MenuItem(
					TakeOrderActivity.this, 0, menuDeptId, mSaleMode);
			List<MenuDataItem> dataItem = item
					.getMenuItem();

			return dataItem;
		}
		
	}

	private void refreshPluResult(){
		LinearLayout pluResultLayout = (LinearLayout) findViewById(R.id.PluResultLayout);
		pluResultLayout.removeAllViews();
	}
	
	private void pluResult(List<MenuDataItem> mdl) {
		LinearLayout pluResultLayout = (LinearLayout) findViewById(R.id.PluResultLayout);
		LayoutInflater inflater = LayoutInflater.from(TakeOrderActivity.this);

		ImageLoader imgLoader = new ImageLoader(TakeOrderActivity.this);
		LayoutParams layoutParam = new LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1f);
		layoutParam.width = 230;
		layoutParam.setMargins(1, 1, 1, 1);
		pluResultLayout.removeAllViews();
		if (mdl.size() > 1) {
			for (final MenuDataItem mi : mdl) {
				View menuItemView = inflater.inflate(R.layout.menu_item_layout,
						null);
				//menuItemView.setBackgroundResource(R.drawable.dark_blue_button);
				ImageView imgMenu = (ImageView) menuItemView
						.findViewById(R.id.menuitem_img);
				TextView tvMenuName = (TextView) menuItemView
						.findViewById(R.id.menuitem_tvMenuName);
				TextView tvMenuCode = (TextView) menuItemView
						.findViewById(R.id.tvMenuCode);

				if(GlobalVar.DISPLAY_MENU_IMG == 1){
					imgMenu.setVisibility(View.VISIBLE);
					// load image
					imgLoader.DisplayImage(GlobalVar.IMG_URL + mi.getImgUrl(),
							imgMenu);
				}else{
					imgMenu.setVisibility(View.GONE);
				}

				String menuName = mi.getMenuName();
				
				try {
					switch(GlobalVar.SHOW_MENU_COLUMN){
					case 1:
						menuName = mi.getMenuName1().equals("") ? mi.getMenuName() : mi.getMenuName1();
						break;
					case 2:
						menuName = mi.getMenuName2().equals("") ? mi.getMenuName() : mi.getMenuName2();
						break;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				String extraMenuName = menuName;
				
				if(mSaleModePrefix == 0)
					extraMenuName = mSaleModeWord + menuName;
				else
					extraMenuName = menuName + mSaleModeWord;

				tvMenuName.setText(extraMenuName);
				tvMenuCode.setVisibility(View.VISIBLE);
				tvMenuCode.setText(!mi.getProductBarcode().equals("") ? mi.getProductBarcode() : mi.getProductCode());

				if (mi.getIsOutOfStock() == 1) {
					menuItemView.setEnabled(false);
					TextView tvOutOfStock = (TextView) menuItemView
							.findViewById(R.id.textViewMenuItemOutOfStock);
					tvOutOfStock.setVisibility(View.VISIBLE);
				} else {
					menuItemView.setEnabled(true);
					TextView tvOutOfStock = (TextView) menuItemView
							.findViewById(R.id.textViewMenuItemOutOfStock);
					tvOutOfStock.setVisibility(View.GONE);
				}

				menuItemView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						addOrderItem(mi);
					}

				});

				pluResultLayout.addView(menuItemView, layoutParam);
			}
		}else{
			pluResultLayout.removeAllViews();
		}
	}

	private class PluSearchTask extends
			AsyncTask<String, Boolean, List<syn.pos.data.model.MenuDataItem>> {

		private LinearLayout layoutPluProgress;
		private LinearLayout pluResultLayout;
		private EditText txtPluCode;

		public PluSearchTask() {
			txtPluCode = (EditText) findViewById(R.id.txtPluCode);
		}

		@Override
		protected void onPreExecute() {
			layoutPluProgress = (LinearLayout) findViewById(R.id.layoutPluProgress);
			pluResultLayout = (LinearLayout) findViewById(R.id.PluResultLayout);
			pluResultLayout.setVisibility(View.GONE);
			layoutPluProgress.setVisibility(View.VISIBLE);
		}

		private void addPluOrder(final MenuDataItem mi) {
			addOrderItem(mi);
		}

		@Override
		protected void onPostExecute(List<MenuDataItem> mdl) {
			layoutPluProgress.setVisibility(View.GONE);
			pluResultLayout.setVisibility(View.VISIBLE);

			if (mdl.size() > 0) {
				if (mdl.size() == 1) {
					MenuDataItem mi = mdl.get(0);
					if (mi.getProductCode().equals(
							txtPluCode.getText().toString())
							|| mi.getProductBarcode().equals(
									txtPluCode.getText().toString())) {

						if (mi.getProductID() != 0) {
							if (mi.getIsOutOfStock() == 0) {								
								addPluOrder(mi);
							} else {
								TextView tvOutOfStock = new TextView(
										TakeOrderActivity.this);
								tvOutOfStock.setText(R.string.out_of_stock);

								IOrderUtility.alertDialog(
										TakeOrderActivity.this,
										R.string.global_dialog_title_error,
										mi.getMenuName()
												+ " "
												+ tvOutOfStock.getText()
														.toString(), 0);
							}
						}
					}

				}
			} else {
				LayoutInflater inflater = LayoutInflater
						.from(TakeOrderActivity.this);
				View v = inflater.inflate(R.layout.custom_dialog, null);

				TextView tvTitle = (TextView) v
						.findViewById(R.id.textViewTitle);
				TextView tvMessage = (TextView) v
						.findViewById(R.id.textViewMessage);
				Button btnOk = (Button) v.findViewById(R.id.buttonOk);
				Button btnCancel = (Button) v
						.findViewById(R.id.buttonConfirmCancel);

				tvTitle.setVisibility(View.VISIBLE);
				tvTitle.setText(R.string.dialog_plu_search_notfound_title);
				tvMessage.setText(R.string.dialog_plu_search_notfound_msg);
				btnOk.setText(R.string.dialog_plu_search_btn_close);
				btnCancel.setVisibility(View.GONE);

				final Dialog dialog = new Dialog(TakeOrderActivity.this,
						R.style.CustomDialog);
				dialog.setContentView(v);
				dialog.show();

				btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						txtPluCode.setText("");
						dialog.dismiss();
					}
				});
			}

			pluResult(mdl);
			txtPluCode.setText("");
		}

		@Override
		protected List<syn.pos.data.model.MenuDataItem> doInBackground(
				String... arg0) {
			syn.pos.data.dao.MenuItem mi = new syn.pos.data.dao.MenuItem(
					TakeOrderActivity.this);

			List<syn.pos.data.model.MenuDataItem> mdl = null;

			if (mSearchColumn != "") {
				String[] columnToSearch = mSearchColumn.split(",");
				if (columnToSearch.length == 1)
					mdl = mi.pluSearchListItem(txtPluCode.getText().toString(),
							columnToSearch[0], mSaleMode);
				else if (columnToSearch.length == 2)
					mdl = mi.pluSearchListItem(txtPluCode.getText().toString(),
							columnToSearch[0], columnToSearch[1], mSaleMode);
			} else {
				mdl = mi.pluSearchListItem(txtPluCode.getText().toString(),
						"ProductCode", mSaleMode);
			}
			return mdl;
		}

	}

	// orderlist adapter
	private class OrderListExpandableAdapter extends BaseExpandableListAdapter {
		private LayoutInflater inflater = null;
		private ImageLoader imgLoader;
		private SaleMode saleMode;

		private List<MenuGroups.MenuComment> menuCommentList;
		private MenuCommentListAdapter menuCommentAdapter;
		

		public OrderListExpandableAdapter() {
			inflater = LayoutInflater.from(TakeOrderActivity.this);
			imgLoader = new ImageLoader(TakeOrderActivity.this,
					ImageLoader.IMAGE_SIZE.SMALL);
			saleMode = new SaleMode(TakeOrderActivity.this);
		}

		@Override
		public MenuDataItem getChild(int groupPosition, int childPosition) {
			return mOrderLst.get(groupPosition).pCompSetLst.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

			final ProductGroups.PComponentSet pcs = mOrderLst
					.get(groupPosition).pCompSetLst.get(childPosition);
			final MenuDataItem mi = mOrderLst.get(groupPosition);

			ChildOrderLinkType7ViewHolder holder;
			if (convertView == null) {
				convertView = inflater
						.inflate(R.layout.child_order_type7, null);

				holder = new ChildOrderLinkType7ViewHolder();
				holder.imgMenuSet = (ImageView) convertView
						.findViewById(R.id.imageViewMenuPic);
				holder.tvOrderNo = (TextView) convertView
						.findViewById(R.id.tvChildOrderLinkType7No);
				holder.tvMenuName = (TextView) convertView
						.findViewById(R.id.tvChildOrderLinkType7MenuName);
				holder.tvMenuComment = (TextView) convertView
						.findViewById(R.id.tvChildOrderLinkType7Comment);
				holder.tvMenuQty = (TextView) convertView
						.findViewById(R.id.tvChildOrderLinkType7Qty);
				holder.tvMenuPrice = (TextView) convertView
						.findViewById(R.id.tvChildOrderLinkType7Price);

				holder.btnMinus = (Button) convertView
						.findViewById(R.id.btnChildOrderLinkType7Minus);
				holder.btnPlus = (Button) convertView
						.findViewById(R.id.btnChildOrderLinkType7Plus);
				holder.btnDelete = (Button) convertView
						.findViewById(R.id.btnChildOrderLinkType7Delete);
				holder.btnComment = (Button) convertView
						.findViewById(R.id.btnChildOrderLinkType7Comment);

				holder.tvOrderNo.setPadding(20, 0, 0, 0);
				holder.tvMenuComment.setPadding(30, 0, 0, 0);
				convertView.setTag(holder);
			} else {
				holder = (ChildOrderLinkType7ViewHolder) convertView.getTag();
			}

			if(GlobalVar.DISPLAY_MENU_IMG == 1){
				holder.imgMenuSet.setVisibility(View.VISIBLE);
				imgLoader.DisplayImage(GlobalVar.IMG_URL + mi.getImgUrl(),
						holder.imgMenuSet);
			}else{
				holder.imgMenuSet.setVisibility(View.GONE);
			}

			String menuName = pcs.getMenuName();
			try {
				switch(GlobalVar.SHOW_MENU_COLUMN){
				case 1:
					menuName = pcs.getMenuName1().equals("") ? pcs.getMenuName() : pcs.getMenuName1();
					break;
				case 2:
					menuName = pcs.getMenuName2().equals("") ? pcs.getMenuName() : pcs.getMenuName2();
					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			holder.tvOrderNo.setText(Integer.toString(childPosition + 1) + ".");
			holder.tvMenuName.setText(menuName);

			holder.tvMenuComment.setText("");
			holder.tvMenuPrice.setText(pcs.getPricePerUnit() != 0 ? 
					mGlobalVar.decimalFormat.format(pcs.getPricePerUnit()) : "");

//			LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(
//					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
//					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
//			layoutParam.setMargins(0, 0, 30, 0);
//			holder.tvMenuQty.setLayoutParams(layoutParam);
			holder.tvMenuQty.setText(mGlobalVar.qtyFormat.format(pcs
					.getProductQty()));

			// order comment
			String menuComment = "";
			if (pcs.menuCommentList != null && pcs.menuCommentList.size() > 0) {
				holder.tvMenuComment.setVisibility(View.VISIBLE);
				for (MenuGroups.MenuComment mc : pcs.menuCommentList) {
					menuComment += mc.getMenuCommentName_0() + " ";

					if (mc.getProductPricePerUnit() > 0) {
						menuComment += " "
								+ mGlobalVar.qtyDecimalFormat
										.format(mc.getCommentQty());
						menuComment += " x "
								+ mGlobalVar.decimalFormat.format(mc
										.getProductPricePerUnit());
						menuComment += " = "
								+ mGlobalVar.decimalFormat.format(mc
										.getCommentQty()
										* mc.getProductPricePerUnit()) + " ";
					} else {
						menuComment += ", ";
					}
				}
				holder.tvMenuComment.setText(menuComment);
			} else {
				holder.tvMenuComment.setVisibility(View.GONE);
			}

			if (!pcs.getOrderComment().equals("")) {
				menuComment += pcs.getOrderComment();

				holder.tvMenuComment.setText(menuComment);
				holder.tvMenuComment.setVisibility(View.VISIBLE);
			}

			holder.btnMinus.setVisibility(View.GONE);
			holder.btnPlus.setVisibility(View.GONE);
			holder.btnDelete.setVisibility(View.GONE);
			holder.btnComment.setVisibility(View.GONE);

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mOrderLst.get(groupPosition).pCompSetLst != null ? mOrderLst
					.get(groupPosition).pCompSetLst.size() : 0;
		}

		@Override
		public MenuDataItem getGroup(int groupPosition) {
			return mOrderLst.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return mOrderLst.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {

			final OrderListViewHolder holder;
			final MenuDataItem mi = mOrderLst.get(groupPosition);
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.order_listview, null);
				holder = new OrderListViewHolder();

				holder.imgOrder = (ImageView) convertView
						.findViewById(R.id.imageViewMenuPic);
				holder.tvOrderNo = (TextView) convertView
						.findViewById(R.id.tvOrderNo);
				holder.tvOrderListMenuName = (TextView) convertView
						.findViewById(R.id.tvOrderListMenuName);
				holder.tvOrderListMenuQty = (TextView) convertView
						.findViewById(R.id.tvOrderListMenuQty);
				holder.tvOrderListMenuPrice = (TextView) convertView
						.findViewById(R.id.tvOrderListMenuPrice);
				holder.tvOrderListMenuComment = (TextView) convertView
						.findViewById(R.id.tvOrderListMenuComment);
				holder.btnMinus = (Button) convertView
						.findViewById(R.id.btnOrderMinus);
				holder.btnPlus = (Button) convertView
						.findViewById(R.id.btnOrderPlus);
				holder.btnComment = (Button) convertView
						.findViewById(R.id.btnOrderComment);
				holder.btnDelete = (Button) convertView
						.findViewById(R.id.btnOrderDelete);
				holder.btnEdit = (Button) convertView
						.findViewById(R.id.buttonOrderEdit);

				convertView.setTag(holder);
			} else {
				holder = (OrderListViewHolder) convertView.getTag();
			}

			if(GlobalVar.DISPLAY_MENU_IMG == 1){
				holder.imgOrder.setVisibility(View.VISIBLE);
				imgLoader.DisplayImage(GlobalVar.IMG_URL + mi.getImgUrl(),
						holder.imgOrder);
			}else{
				holder.imgOrder.setVisibility(View.GONE);
			}

			String extra = "";
			if(mi.getCourseId() != 0 && mi.getSeatId() != 0){
				extra = mi.getCourseName() + "-" + mi.getSeatName();
			}else{
				if(mi.getCourseId() != 0){
					extra = mi.getCourseName();
				}
				if(mi.getSeatId() != 0){
					extra = mi.getSeatName();
				}
			}
			
			holder.tvOrderNo.setText(extra);
			
			String menuName = mi.getMenuName();
			
			try {
				switch(GlobalVar.SHOW_MENU_COLUMN){
				case 1:
					menuName = mi.getMenuName1().equals("") ? mi.getMenuName() : mi.getMenuName1();
					break;
				case 2:
					menuName = mi.getMenuName2().equals("") ? mi.getMenuName() : mi.getMenuName2();
					break;
					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String extraMenuName = menuName;
			
			if(mi.getSaleMode() != 1){
				ProductGroups.SaleMode s = saleMode.getSaleMode(mi.getSaleMode());
				if(s.getPositionPrefix() == 0){
					extraMenuName = s.getPrefixText() + menuName;
				}else{
					extraMenuName = menuName + s.getPrefixText();
				}
			}

			holder.tvOrderListMenuName.setText(extraMenuName);
			holder.tvOrderListMenuQty.setText(mGlobalVar.qtyDecimalFormat.format(mi
					.getProductQty()));
			holder.tvOrderListMenuPrice.setText(mGlobalVar.decimalFormat
					.format(mi.getPricePerUnit()));

			String menuComment = "";
			if (mi.menuCommentList != null && mi.menuCommentList.size() > 0) {
				holder.tvOrderListMenuComment.setVisibility(View.VISIBLE);
				for (MenuGroups.MenuComment mc : mi.menuCommentList) {
					menuComment += mc.getMenuCommentName_0() + " ";

					if (mc.getProductPricePerUnit() > 0) {
						menuComment += " "
								+ mGlobalVar.qtyDecimalFormat
										.format(mc.getCommentQty());
						menuComment += " x "
								+ mGlobalVar.decimalFormat.format(mc
										.getProductPricePerUnit());
						menuComment += " = "
								+ mGlobalVar.decimalFormat.format(mc
										.getCommentQty()
										* mc.getProductPricePerUnit()) + " ";
					} else {
						menuComment += ", ";
					}
				}
				holder.tvOrderListMenuComment.setText(menuComment);
			} else {
				holder.tvOrderListMenuComment.setVisibility(View.GONE);
			}

			if (!mi.getOrderComment().equals("")) {
				menuComment += mi.getOrderComment();

				holder.tvOrderListMenuComment.setText(menuComment);
				holder.tvOrderListMenuComment.setVisibility(View.VISIBLE);
			}

			holder.position = groupPosition;
			holder.orderDetailId = mi.getOrderDetailId();

			holder.btnMinus.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					double qty = mi.getProductQty();// Double.parseDouble(holder.tvOrderListMenuQty.getText().toString());
					if (--qty > 0) {
						// holder.tvOrderListMenuQty.setText(globalVar.qtyFormat.format(qty));
						// updateOrder(mi.getOrderDetailId(), qty);
						//
						// mi.setProductQty(qty);
						// mi.setOrderComment("");
						POSOrdering posOrder = new POSOrdering(
								TakeOrderActivity.this);
						posOrder.updateOrderDetail(GlobalVar.TRANSACTION_ID,
								mi.getOrderDetailId(), qty);

						MenuDataItem menuItem = posOrder.listOrder(
								GlobalVar.TRANSACTION_ID,
								GlobalVar.COMPUTER_ID, mi.getOrderDetailId(), mSeatId);

						mOrderLst.set(groupPosition, menuItem);
						mOrderLstAdapter.notifyDataSetChanged();
						mOrderListView.setItemChecked(groupPosition, true);

						summaryTotalSalePrice();
					}
				}
			});

			holder.btnPlus.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					double qty = mi.getProductQty();// Double.parseDouble(holder.tvOrderListMenuQty.getText().toString());
					++qty;
					// holder.tvOrderListMenuQty.setText(globalVar.qtyFormat.format(qty));

					POSOrdering posOrder = new POSOrdering(
							TakeOrderActivity.this);
					posOrder.updateOrderDetail(GlobalVar.TRANSACTION_ID,
							mi.getOrderDetailId(), qty);

					MenuDataItem menuItem = posOrder.listOrder(
							GlobalVar.TRANSACTION_ID, GlobalVar.COMPUTER_ID,
							mi.getOrderDetailId(), mSeatId);

					mOrderLst.set(groupPosition, menuItem);
					mOrderLstAdapter.notifyDataSetChanged();
					mOrderListView.setItemChecked(groupPosition, true);

					summaryTotalSalePrice();
				}
			});

			holder.btnComment.setOnClickListener(new View.OnClickListener() {

				int modSaleMode = mi.getSaleMode();
				int modSeatId = mi.getSeatId();
				String modSeatName = mi.getSeatName();
				int modCourseId = mi.getCourseId();
				String modCourseName = mi.getCourseName();
				
				private void loadCommentGroup(MenuComment mc, Spinner spMcg){
					//menu comment group 
					List<MenuGroups.MenuCommentGroup> mcgLst = 
							mc.listMenuCommentGroups();
					
					MenuCommentGroupAdapter mcgAdapter = 
							new MenuCommentGroupAdapter(TakeOrderActivity.this, mcgLst);
					spMcg.setAdapter(mcgAdapter);
				}
				
				class UpdateSelectedCommentTask extends AsyncTask<Void, Void, List<MenuGroups.MenuComment>>{
					ListView selectedMenuCommentListView;
					
					public UpdateSelectedCommentTask(ListView lv){
						selectedMenuCommentListView = lv;
					}
					
					
					@Override
					protected void onPostExecute(List<MenuGroups.MenuComment> mcLst) {
						MenuCommentSelectedAdapter selectedCommentAdapter = 
								new MenuCommentSelectedAdapter(TakeOrderActivity.this, mGlobalVar, mcLst);
						selectedMenuCommentListView.setAdapter(selectedCommentAdapter);
					}


					@Override
					protected void onPreExecute() {
						// TODO Auto-generated method stub
						super.onPreExecute();
					}


					@Override
					protected List<MenuGroups.MenuComment> doInBackground(Void... params) {
						POSOrdering posOrdering = new POSOrdering(
								TakeOrderActivity.this);
						
						List<MenuGroups.MenuComment> mcLst
							= posOrdering.listOrderCommentTemp(GlobalVar.TRANSACTION_ID, mi.getOrderDetailId());
						return mcLst;
					}
					
				}
				
				@Override
				public void onClick(View view) {
					mOrderListView.setItemChecked(groupPosition, true);
					//
					POSOrdering posOrdering = new POSOrdering(
							TakeOrderActivity.this);
					// create OrderCommentTemp
					posOrdering.createOrderCommentTmp(GlobalVar.TRANSACTION_ID,
							mi.getOrderDetailId());

					LayoutInflater factory = LayoutInflater.from(TakeOrderActivity.this);
					View v = factory.inflate(R.layout.menu_comment_layout2, null);

					TextView tvTitle = (TextView) v.findViewById(R.id.textViewMenuCommentTitle);
					LinearLayout seatLayout = (LinearLayout) v.findViewById(R.id.seatContent);
					final Spinner spMcg = (Spinner) v.findViewById(R.id.spinnerMcg);
					Button btnCancel = (Button) v.findViewById(R.id.buttonCancelComment);
					Button btnOk = (Button) v.findViewById(R.id.buttonOkComment);
					final Button btnModSeat = (Button) v.findViewById(R.id.buttonModSeat);
					
					if(mIsEnableSeat){
						seatLayout.setVisibility(View.VISIBLE);
						
						final ShopProperty shopProperty = new ShopProperty(TakeOrderActivity.this, null);
						
						String extra = "...";
						if(modSeatId != 0 && modCourseId != 0)
						{
							extra = modCourseName + "-" + modSeatName; 
						}else{
							if(modSeatId != 0)
								extra = modSeatName;
							if(modCourseId != 0)
								extra = modCourseName;
						}
						
						btnModSeat.setText(extra);
						
						btnModSeat.setOnClickListener(new OnClickListener(){
	
							@Override
							public void onClick(View v) {
								LayoutInflater inflater = LayoutInflater.from(TakeOrderActivity.this);
								v = inflater.inflate(R.layout.seat_template, null);
								LinearLayout courseContent = (LinearLayout) v.findViewById(R.id.courseContent);
								final GridView gvSeat = (GridView) v.findViewById(R.id.gridView1);
								final Button btnClose = (Button) v.findViewById(R.id.btnClose);
								final Button btnCancel = (Button) v.findViewById(R.id.button1);
								final Button btnOk = (Button) v.findViewById(R.id.button2);
										
								if(mIsEnableCourse){
									courseContent.setVisibility(View.VISIBLE);
									HorizontalScrollView hView = (HorizontalScrollView) 
											courseContent.findViewById(R.id.horizontalScrollView1);
									new Course(TakeOrderActivity.this, modCourseId, hView, new Course.OnCourseClickedListener() {
										
										@Override
										public void onClick(int courseId, String courseName, String courseShortName) {
											modCourseId = courseId;
											modCourseName = courseShortName;
										}
									}).createCourseView();
									
								}else{
									courseContent.setVisibility(View.GONE);
								}
								
								final Dialog d = new Dialog(TakeOrderActivity.this, R.style.CustomDialog);
								d.setContentView(v);
								d.getWindow().setGravity(Gravity.TOP);
								d.show();
								
								List<ShopData.SeatNo> seatLst = new ArrayList<ShopData.SeatNo>();
								seatLst = shopProperty.getSeatNo();
								
								ShopData.SeatNo seat = new ShopData.SeatNo();
								seat.setSeatID(0);
								seat.setSeatName("All");
								seatLst.add(0, seat);
								
								SeatModAdapter seatAdapter = new SeatModAdapter(seatLst, modSeatId, new OnSeatClickListener(){

									@Override
									public void onClick(int id, String name) {
										modSeatId = id;
										modSeatName = name;
									}
								});
								gvSeat.setAdapter(seatAdapter);
						
								btnOk.setOnClickListener(new OnClickListener(){

									@Override
									public void onClick(View v) {
										POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
										posOrder.updateSeatOrderDetail(GlobalVar.TRANSACTION_ID, 
												mi.getOrderDetailId(), modSeatId, modSeatName, 
												modCourseId, modCourseName);

										String extra = "...";
										if(modSeatId != 0 && modCourseId != 0){
											extra = modCourseName + "-" + modSeatName;
										}else{
											if(modSeatId != 0)
												extra = modSeatName;
											if(modCourseId != 0)
												extra = modCourseName;
										}
										btnModSeat.setText(extra);
										
										// reset main seat
//										mSeatId = 0;
//										mBtnSeat.setText("All");
										
										d.dismiss();
									}
									
								});
								
								btnCancel.setOnClickListener(new OnClickListener(){

									@Override
									public void onClick(View v) {
										modSeatId = mi.getSeatId();
										modSeatName = mi.getSeatName();
										modCourseId = mi.getCourseId();
										modCourseName = mi.getCourseName();
										d.dismiss();
									}
								});
								
								btnClose.setOnClickListener(new OnClickListener(){

									@Override
									public void onClick(View v) {
										modSeatId = mi.getSeatId();
										modSeatName = mi.getSeatName();
										modCourseId = mi.getCourseId();
										modCourseName = mi.getCourseName();
										d.dismiss();
									}
									
								});
							}
						});
					}else{
						seatLayout.setVisibility(View.GONE);
					}
					
					final ListView menuCommentListView = (ListView) v.findViewById(R.id.menuCommentListView);
					final ListView selectedMenuCommentListView = (ListView) v.findViewById(R.id.listViewCommentSelected);
					final EditText txtComment = (EditText) v.findViewById(R.id.txt_menu_comment);
					tvTitle.setText(R.string.title_menu_comment);

					SaleMode saleMode = new SaleMode(TakeOrderActivity.this);
					List<ProductGroups.SaleMode> saleModeLst = saleMode.listSaleMode(new int[]{1,2});
					if(saleModeLst != null && saleModeLst.size() > 0)
					{
						// sale mode mod layout
						final LinearLayout saleModeModSwLayout = (LinearLayout) v.findViewById(R.id.saleModeModSwLayout);
						for(final ProductGroups.SaleMode s : saleModeLst){
							Button btnSwSaleMode = (Button) factory.inflate(R.layout.button_sale_mode, null);
							btnSwSaleMode.setId(s.getSaleModeID());
							btnSwSaleMode.setText(s.getSaleModeName());
							
							if(s.getSaleModeID() == 1)
								btnSwSaleMode.setSelected(true);
							
							btnSwSaleMode.setOnClickListener(new OnClickListener(){

								@Override
								public void onClick(View arg0) {
									modSaleMode = s.getSaleModeID();
									
									Button btnEatIn = (Button) saleModeModSwLayout.findViewById(1);
									Button btnTakeAway = (Button) saleModeModSwLayout.findViewById(2);
									
									if(s.getSaleModeID() == 1){
										if(btnEatIn != null){
											btnEatIn.setSelected(true);
										}
										if(btnTakeAway != null){
											btnTakeAway.setSelected(false);
										}
									}else if(s.getSaleModeID() == 2){
										if(btnEatIn != null){
											btnEatIn.setSelected(false);
										}
										if(btnTakeAway != null){
											btnTakeAway.setSelected(true);
										}
									}
								}
								
							});
							
//							if(s.getSaleModeID() == 1){
//								btnSwSaleMode.setText("DI");
//							}
//							else if(s.getSaleModeID() == 2){
//								btnSwSaleMode.setText("TW");
//							}
							
							saleModeModSwLayout.addView(btnSwSaleMode);
						}

						Button btnEatIn = (Button) saleModeModSwLayout.findViewById(1);
						Button btnTakeAway = (Button) saleModeModSwLayout.findViewById(2);
						
						if(modSaleMode == 1){
							if(btnEatIn != null){
								btnEatIn.setSelected(true);
							}
							if(btnTakeAway != null){
								btnTakeAway.setSelected(false);
							}
						}else if(modSaleMode == 2){
							if(btnEatIn != null){
								btnEatIn.setSelected(false);
							}
							if(btnTakeAway != null){
								btnTakeAway.setSelected(true);
							}
						}
						
						if(mTransSaleMode == 2){
							if(btnEatIn != null){
								btnEatIn.setEnabled(false);
							}
							if(btnTakeAway != null){
								btnTakeAway.setEnabled(false);
							}
						}
						
						if(saleModeLst.size() == 1){
							if(saleModeLst.get(0).getSaleModeID() == 1)
								saleModeModSwLayout.setVisibility(View.INVISIBLE);
						}
					}
					
					final MenuComment mc = new MenuComment(TakeOrderActivity.this);
					loadCommentGroup(mc, spMcg);
					
					spMcg.setOnItemSelectedListener(new OnItemSelectedListener(){

						@Override
						public void onItemSelected(AdapterView<?> parent,
								View v, int position, long id) {
							MenuGroups.MenuCommentGroup mg = 
									(MenuGroups.MenuCommentGroup)parent.getItemAtPosition(position);
							
							if(mCommentType == 1 || mCommentType == 2){
								menuCommentList = mc.listFixMenuComment(mi.getProductID());
								if(menuCommentList.size() == 0){
									menuCommentList = mc.listMenuComments(mg.getMenuCommentGroupID());
								}
								menuCommentAdapter = new MenuCommentListAdapter(
								TakeOrderActivity.this, mGlobalVar, menuCommentList,
								GlobalVar.TRANSACTION_ID, mi.getOrderDetailId(), selectedMenuCommentListView);
								menuCommentListView.setAdapter(menuCommentAdapter);
							}else{
								menuCommentList = mc.listMenuComments(mg.getMenuCommentGroupID());
								menuCommentAdapter = new MenuCommentListAdapter(
								TakeOrderActivity.this, mGlobalVar, menuCommentList,
								GlobalVar.TRANSACTION_ID, mi.getOrderDetailId(), selectedMenuCommentListView);
								menuCommentListView.setAdapter(menuCommentAdapter);
							}
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
							
						}
						
					});

					selectedMenuCommentListView.setOnItemClickListener(new OnItemClickListener(){

						@Override
						public void onItemClick(final AdapterView<?> parent, View v,
								final int position, long id) {
							
							final CustomDialog cusDialog = new CustomDialog(TakeOrderActivity.this, R.style.CustomDialog);
							cusDialog.title.setVisibility(View.VISIBLE);
							cusDialog.title.setText(R.string.delete_menu_comment_title);
							cusDialog.message.setText(R.string.delete_menu_comment_cf);
							cusDialog.btnCancel.setVisibility(View.VISIBLE);
							cusDialog.btnCancel.setOnClickListener(new OnClickListener(){

								@Override
								public void onClick(View v) {
									cusDialog.dismiss();
								}
								
							});
							cusDialog.btnOk.setOnClickListener(new OnClickListener(){

								@Override
								public void onClick(View v) {
									MenuGroups.MenuComment mcData = 
											(MenuGroups.MenuComment) parent.getItemAtPosition(position);
									
									POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
									posOrder.deleteOrderComment(GlobalVar.TRANSACTION_ID, 
											mi.getOrderDetailId(), mcData.getMenuCommentID());
									
									new UpdateSelectedCommentTask(selectedMenuCommentListView).execute();

									menuCommentAdapter.notifyDataSetInvalidated();
									
									cusDialog.dismiss();
								}
								
							});
							cusDialog.show();
						}
						
					});

					new UpdateSelectedCommentTask(selectedMenuCommentListView).execute();
					// set adapter view click listenner
					menuCommentListView
							.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent,
										View v, int position, long id) {

									MenuCommentListAdapter.MenuCommentViewHolder holder = 
											(MenuCommentListAdapter.MenuCommentViewHolder) v.getTag();

									MenuGroups.MenuComment mc = (MenuGroups.MenuComment) parent
											.getItemAtPosition(position);

									POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
									
									if (!posOrder.chkOrderComment(GlobalVar.TRANSACTION_ID, 
											mi.getOrderDetailId(), mc.getMenuCommentID())) {
										holder.checkBox1.setChecked(true);
										holder.tvMenuCommentQty.setText("1");
										holder.btnMenuCommentMinus
												.setEnabled(true);
										holder.btnMenuCommentPlus
												.setEnabled(true);

										posOrder.addOrderComment(
												GlobalVar.TRANSACTION_ID,
												mi.getOrderDetailId(),
												mc.getMenuCommentID(),
												mc.getCommentQty(),
												mc.getProductPricePerUnit());

									} else {
										holder.checkBox1.setChecked(false);

										holder.tvMenuCommentQty.setText("0");
										holder.btnMenuCommentMinus
												.setEnabled(false);
										holder.btnMenuCommentPlus
												.setEnabled(false);

										posOrder.deleteOrderComment(
												GlobalVar.TRANSACTION_ID,
												mi.getOrderDetailId(),
												mc.getMenuCommentID());
									}
									new UpdateSelectedCommentTask(selectedMenuCommentListView).execute();
								}

							});
					
					txtComment.append(mi.getOrderComment());
					// List dialog menucomment
					final Dialog dialog = new Dialog(TakeOrderActivity.this,
							R.style.CustomDialogBottomRadius);
					// .setTitle(R.string.title_menu_comment)
					dialog.setContentView(v);
					dialog.getWindow().setWindowAnimations(
							R.style.DialogAnimation);
					dialog.getWindow().setGravity(Gravity.TOP);
					dialog.getWindow()
							.setSoftInputMode(
									WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
									WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
					dialog.getWindow().setLayout(
							android.view.ViewGroup.LayoutParams.MATCH_PARENT,
							android.view.ViewGroup.LayoutParams.MATCH_PARENT);
					dialog.show();

					btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							POSOrdering posOrdering = new POSOrdering(
									TakeOrderActivity.this);
							posOrdering.confirmOrderComment(
									GlobalVar.TRANSACTION_ID,
									mi.getOrderDetailId());
							posOrdering.updateOrderDetail(
									GlobalVar.TRANSACTION_ID,
									mi.getOrderDetailId(), modSaleMode,
									txtComment.getText().toString());

//							MenuDataItem menuDataItem = posOrdering.listOrder(
//									GlobalVar.TRANSACTION_ID,
//									GlobalVar.COMPUTER_ID,
//									mi.getOrderDetailId(), modSeatId);
//							ORDER_LIST.set(groupPosition, menuDataItem);
//							ORDER_LIST_ADAPTER.notifyDataSetChanged();
							
							listAllOrder();
							mOrderListView.setSelection(groupPosition);
							mOrderListView.setItemChecked(groupPosition, true);

							//summaryTotalSalePrice();

							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									txtComment.getWindowToken(), 0);

							dialog.dismiss();
						}
					});

					btnCancel.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									txtComment.getWindowToken(), 0);

							dialog.dismiss();
						}
					});
				}
			});

			holder.btnDelete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					final CustomDialog customDialog = new CustomDialog(
							TakeOrderActivity.this, R.style.CustomDialog);
					customDialog.title.setVisibility(View.VISIBLE);
					customDialog.title
							.setText(R.string.dialog_delete_item_title);
					customDialog.message
							.setText(R.string.dialog_delete_item_message);
					customDialog.btnCancel
							.setText(R.string.dialog_delete_item_btn_cancel);
					customDialog.btnOk
							.setText(R.string.dialog_delete_item_btn_ok);
					customDialog.btnOk
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									POSOrdering posOrder = new POSOrdering(
											TakeOrderActivity.this);
									posOrder.deleteOrderDetail(
											GlobalVar.TRANSACTION_ID,
											mi.getOrderDetailId());

									listAllOrder();
									customDialog.dismiss();
								}
							});
					customDialog.btnCancel
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									customDialog.dismiss();
								}
							});

					customDialog.show();
				}
			});

			holder.btnEdit.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(TakeOrderActivity.this,
							MenuSetActivity.class);
					intent.putExtra("EDIT_MODE", 1);
					intent.putExtra("transactionId", GlobalVar.TRANSACTION_ID);
					intent.putExtra("orderId", mi.getOrderDetailId());
					intent.putExtra("productId", mi.getProductID());
					intent.putExtra("commentType", mCommentType);
					TakeOrderActivity.this.startActivity(intent);
					overridePendingTransition(R.animator.slide_left_in,
							R.animator.slide_left_out);
				}
			});

			// hide btn when order is type 7
			if (mi.getProductTypeID() == 7) {
				holder.btnMinus.setVisibility(View.GONE);
				holder.btnPlus.setVisibility(View.GONE);
				holder.btnComment.setVisibility(View.GONE);
				holder.btnEdit.setVisibility(View.VISIBLE);

				// show /
				holder.tvOrderListMenuQty.setVisibility(View.INVISIBLE);
				// holder.tvOrderListMenuPrice.setText(mi.getCurrencySymbol() +
				// globalVar.decimalFormat.format(mi.getPricePerUnit()));
			} else {
				holder.btnMinus.setVisibility(View.VISIBLE);
				holder.btnPlus.setVisibility(View.VISIBLE);
				holder.btnComment.setVisibility(View.VISIBLE);
				holder.tvOrderListMenuQty.setVisibility(View.VISIBLE);
				holder.btnEdit.setVisibility(View.GONE);
				
				if(mAddOnlyOneItem){
					holder.btnPlus.setVisibility(View.GONE);
					holder.btnMinus.setVisibility(View.GONE);
				}else{
					holder.btnPlus.setVisibility(View.VISIBLE);
					holder.btnMinus.setVisibility(View.VISIBLE);
				}
			}
			
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return true;
		}
	}

	private class ResizeMenuImage extends ResizeImage {
		public ResizeMenuImage(Context context, int theme, ImageView img) {
			super(context, theme, img);
		}

	}

	// menuitem adapter
	private class MenuItemAdapter extends BaseAdapter {
		private List<MenuDataItem> menuDataItemLst;
		private LayoutInflater inflater;
		private ImageLoader imageLoader;

		private double detailQty = 1.0d;
		private double detailTotalPrice = 0.0d;

		public MenuItemAdapter(List<MenuDataItem> menuDataItemLst) {
			this.menuDataItemLst = menuDataItemLst;
			this.inflater = LayoutInflater.from(TakeOrderActivity.this);
			imageLoader = new ImageLoader(TakeOrderActivity.this, ImageLoader.IMAGE_SIZE.MEDIUM);
		}

		@Override
		public int getCount() {
			return menuDataItemLst.size();
		}

		@Override
		public MenuDataItem getItem(int position) {
			return menuDataItemLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			MenuItemViewHolder holder;
			final MenuDataItem mi = menuDataItemLst.get(position);
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.menu_item_layout, null);

				holder = new MenuItemViewHolder();
				holder.tvMenuCode = (TextView) convertView
						.findViewById(R.id.tvMenuCode);
				holder.menuImg = (ImageView) convertView
						.findViewById(R.id.menuitem_img);
				holder.tvMenuName = (TextView) convertView
						.findViewById(R.id.menuitem_tvMenuName);
				holder.btnSpeak = (ImageButton) convertView
						.findViewById(R.id.imageButtonSpeak);

				convertView.setTag(holder);
			} else {
				holder = (MenuItemViewHolder) convertView.getTag();
			}
			
			String menuName = mi.getMenuName();
			
			try {
				switch(GlobalVar.SHOW_MENU_COLUMN){
				case 1:
					menuName = mi.getMenuName1().equals("") ? mi.getMenuName() : mi.getMenuName1();
					break;
				case 2:
					menuName = mi.getMenuName2().equals("") ? mi.getMenuName() : mi.getMenuName2();
					break;
					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String extraMenuName = menuName;
			
			if (mSaleModePrefix == 0)
				extraMenuName = mSaleModeWord + menuName;
			else
				extraMenuName = menuName + mSaleModeWord;

			if(GlobalVar.DISPLAY_MENU_IMG == 1){
				holder.menuImg.setVisibility(View.VISIBLE);
				imageLoader.DisplayImage(GlobalVar.IMG_URL + mi.getImgUrl(),
						holder.menuImg);
			}
			else{
				holder.menuImg.setVisibility(View.GONE);
			}
			
			holder.tvMenuCode.setText(mi.getProductBarcode().equals("") ? mi.getProductCode() : mi.getProductBarcode());
			holder.tvMenuCode.setVisibility(View.GONE);
			holder.tvMenuName.setText(extraMenuName);
			holder.btnSpeak.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					SpeakText sp = new SpeakText(TakeOrderActivity.this, mi.getMenuName());
					sp.speak();
				}
				
			});
			
			if (mi.getIsOutOfStock() == 1) {
				convertView.setEnabled(false);
				TextView tvOutOfStock = (TextView) convertView
						.findViewById(R.id.textViewMenuItemOutOfStock);
				tvOutOfStock.setVisibility(View.VISIBLE);
			} else {
				convertView.setEnabled(true);

				TextView tvOutOfStock = (TextView) convertView
						.findViewById(R.id.textViewMenuItemOutOfStock);
				tvOutOfStock.setVisibility(View.GONE);

				// item click handler
				convertView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						addOrderItem(mi);
						
						// speak
//						SpeakText sp = new SpeakText(TakeOrderActivity.this, mi.getMenuName());
//						sp.speak();
					}

				});
				
				if(GlobalVar.DISPLAY_MENU_IMG == 1){
					convertView.setOnLongClickListener(new OnLongClickListener(){

						@Override
						public boolean onLongClick(View v) {
							LayoutInflater factory = LayoutInflater
									.from(TakeOrderActivity.this);
							final View viewDetail = factory.inflate(
									R.layout.menu_detail_layout, null);
							TextView tvDetailTitle = (TextView) viewDetail
									.findViewById(R.id.textViewMenuDetailTitle);
							ImageView imgDetail = (ImageView) viewDetail
									.findViewById(R.id.menuItemMordetailImg);
							final TextView tvDetailQty = (TextView) viewDetail
									.findViewById(R.id.tvQty);
							TextView tvDetailPrice = (TextView) viewDetail
									.findViewById(R.id.textViewPrice);
							final TextView tvDetailTotalPrice = (TextView) viewDetail
									.findViewById(R.id.textViewTotalPrice);
							Button btnDetailClose = (Button) viewDetail
									.findViewById(R.id.buttonClose);
							Button btnDetailMinus = (Button) viewDetail
									.findViewById(R.id.buttonMinus);
							Button btnDetailPlus = (Button) viewDetail
									.findViewById(R.id.buttonPlus);
							Button btnDetailOrder = (Button) viewDetail
									.findViewById(R.id.buttonDetailOrder);

							ImageLoader imgLoaderLargeSize = new ImageLoader(
									TakeOrderActivity.this,
									ImageLoader.IMAGE_SIZE.LARGE);
							imgLoaderLargeSize.DisplayImage(
									GlobalVar.IMG_URL + mi.getImgUrl(), imgDetail);

							tvDetailTitle.setText(mi.getMenuName());
							
							double detailPrice = mi.getPricePerUnit() == -1 ? 0 : mi.getPricePerUnit();
							tvDetailPrice.setText(mGlobalVar.decimalFormat.format(detailPrice));
							
							tvDetailTotalPrice.setText(mGlobalVar.decimalFormat
									.format(detailPrice));

							final ResizeMenuImage dialog = new ResizeMenuImage(
									TakeOrderActivity.this, R.style.CustomDialog,
									imgDetail);
							dialog.setContentView(viewDetail);

							dialog.getWindow()
									.setLayout(
											android.view.ViewGroup.LayoutParams.MATCH_PARENT,
											android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

							// dialog
							dialog.show();

							btnDetailClose
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											dialog.dismiss();
										}
									});

							if(mi.getProductTypeID() != 0){
								btnDetailMinus.setEnabled(false);
								btnDetailPlus.setEnabled(false);
							}else{
								btnDetailMinus.setEnabled(true);
								btnDetailPlus.setEnabled(true);
								
								if(mAddOnlyOneItem){
									btnDetailPlus.setEnabled(false);
									btnDetailMinus.setEnabled(false);
								}else{
									btnDetailPlus.setEnabled(true);
									btnDetailMinus.setEnabled(true);
								}
							}
							
							btnDetailMinus
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											detailQty = Double
													.parseDouble(tvDetailQty
															.getText().toString());
											if (mi.getProductTypeID() == 0) {
												--detailQty;
												if (detailQty > 0) {
													tvDetailQty
															.setText(mGlobalVar.qtyDecimalFormat
																	.format(detailQty));
													detailTotalPrice = mi
															.getPricePerUnit()
															* detailQty;
													tvDetailTotalPrice
															.setText(mGlobalVar.decimalFormat
																	.format(detailTotalPrice));
												}
											}
										}
									});
							btnDetailPlus.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									detailQty = Double.parseDouble(tvDetailQty
											.getText().toString());
									if (mi.getProductTypeID() == 0) {
										++detailQty;
										tvDetailQty.setText(mGlobalVar.qtyDecimalFormat
												.format(detailQty));
										detailTotalPrice = mi.getPricePerUnit()
												* detailQty;

										tvDetailTotalPrice
												.setText(mGlobalVar.decimalFormat
														.format(detailTotalPrice));
									}
								}
							});
							
							btnDetailOrder
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											mi.setProductQty(detailQty);
											addOrderItem(mi);
											detailQty = 1;
											dialog.dismiss();
										}
									});
							return true;
						}
						
					});
				}
				

				// img click handler
//				holder.menuImg.setOnLongClickListener(new OnLongClickListener() {
//					@Override
//					public boolean onLongClick(View v) {
//						LayoutInflater factory = LayoutInflater
//								.from(TakeOrderActivity.this);
//						final View viewDetail = factory.inflate(
//								R.layout.menu_detail_layout, null);
//						TextView tvDetailTitle = (TextView) viewDetail
//								.findViewById(R.id.textViewMenuDetailTitle);
//						ImageView imgDetail = (ImageView) viewDetail
//								.findViewById(R.id.menuItemMordetailImg);
//						final TextView tvDetailQty = (TextView) viewDetail
//								.findViewById(R.id.tvQty);
//						TextView tvDetailPrice = (TextView) viewDetail
//								.findViewById(R.id.textViewPrice);
//						final TextView tvDetailTotalPrice = (TextView) viewDetail
//								.findViewById(R.id.textViewTotalPrice);
//						Button btnDetailClose = (Button) viewDetail
//								.findViewById(R.id.buttonClose);
//						Button btnDetailMinus = (Button) viewDetail
//								.findViewById(R.id.buttonMinus);
//						Button btnDetailPlus = (Button) viewDetail
//								.findViewById(R.id.buttonPlus);
//						Button btnDetailOrder = (Button) viewDetail
//								.findViewById(R.id.buttonDetailOrder);
//
//						ImageLoader imgLoaderLargeSize = new ImageLoader(
//								TakeOrderActivity.this,
//								ImageLoader.IMAGE_SIZE.LARGE);
//						imgLoaderLargeSize.DisplayImage(
//								GlobalVar.IMG_URL + mi.getImgUrl(), imgDetail);
//
//						tvDetailTitle.setText(mi.getMenuName());
//						
//						double detailPrice = mi.getPricePerUnit() == -1 ? 0 : mi.getPricePerUnit();
//						tvDetailPrice.setText(mGlobalVar.decimalFormat.format(detailPrice));
//						
//						tvDetailTotalPrice.setText(mGlobalVar.decimalFormat
//								.format(detailPrice));
//
//						final ResizeMenuImage dialog = new ResizeMenuImage(
//								TakeOrderActivity.this, R.style.CustomDialog,
//								imgDetail);
//						dialog.setContentView(viewDetail);
//
//						dialog.getWindow()
//								.setLayout(
//										android.view.ViewGroup.LayoutParams.MATCH_PARENT,
//										android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
//
//						// dialog
//						dialog.show();
//
//						btnDetailClose
//								.setOnClickListener(new OnClickListener() {
//
//									@Override
//									public void onClick(View v) {
//										dialog.dismiss();
//									}
//								});
//
//						if(mi.getProductTypeID() != 0){
//							btnDetailMinus.setEnabled(false);
//							btnDetailPlus.setEnabled(false);
//						}else{
//							btnDetailMinus.setEnabled(true);
//							btnDetailPlus.setEnabled(true);
//							
//							if(mAddOnlyOneItem){
//								btnDetailPlus.setEnabled(false);
//								btnDetailMinus.setEnabled(false);
//							}else{
//								btnDetailPlus.setEnabled(true);
//								btnDetailMinus.setEnabled(true);
//							}
//						}
//						
//						btnDetailMinus
//								.setOnClickListener(new OnClickListener() {
//
//									@Override
//									public void onClick(View v) {
//										detailQty = Double
//												.parseDouble(tvDetailQty
//														.getText().toString());
//										if (mi.getProductTypeID() == 0) {
//											--detailQty;
//											if (detailQty > 0) {
//												tvDetailQty
//														.setText(mGlobalVar.qtyDecimalFormat
//																.format(detailQty));
//												detailTotalPrice = mi
//														.getPricePerUnit()
//														* detailQty;
//												tvDetailTotalPrice
//														.setText(mGlobalVar.decimalFormat
//																.format(detailTotalPrice));
//											}
//										}
//									}
//								});
//						btnDetailPlus.setOnClickListener(new OnClickListener() {
//
//							@Override
//							public void onClick(View v) {
//								detailQty = Double.parseDouble(tvDetailQty
//										.getText().toString());
//								if (mi.getProductTypeID() == 0) {
//									++detailQty;
//									tvDetailQty.setText(mGlobalVar.qtyDecimalFormat
//											.format(detailQty));
//									detailTotalPrice = mi.getPricePerUnit()
//											* detailQty;
//
//									tvDetailTotalPrice
//											.setText(mGlobalVar.decimalFormat
//													.format(detailTotalPrice));
//								}
//							}
//						});
//						
//						btnDetailOrder
//								.setOnClickListener(new OnClickListener() {
//
//									@Override
//									public void onClick(View v) {
//										mi.setProductQty(detailQty);
//										addOrderItem(mi);
//										detailQty = 1;
//										dialog.dismiss();
//									}
//								});
//						return true;
//					}
//				});
			}
			return convertView;
		}

		
	}
	
	private void addOrderItem(final MenuDataItem mi){
		if(mi.getProductTypeID() == 1){
			// add order detail
			POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
			int orderId = posOrder.addOrderDetail(GlobalVar.TRANSACTION_ID,
					GlobalVar.COMPUTER_ID, GlobalVar.SHOP_ID, mi.getProductID(),
					mi.getMenuName(), mi.getProductTypeID(), mSaleMode, 
					1, mi.getPricePerUnit(), mi.getVatAmount(), 0,
					0, 0, 0, mSeatId, mSeatName, mCourseId, mCourseName);
			
			// list set of product
			List<ProductGroups.PComponentSet> pCompSetLst = 
					IOrderUtility.listPComSetOfProduct(TakeOrderActivity.this, mi.getProductID());
			
			if(pCompSetLst != null){
				if(pCompSetLst.size() > 0){
					for(ProductGroups.PComponentSet pCompSet : pCompSetLst){
						addOrderSetOfProduct(orderId, pCompSet.getProductID(), 
								pCompSet.getPricePerUnit(), pCompSet.getChildProductAmount());
					}
				}
			}
			listAllOrder();
		}else if (mi.getProductTypeID() == 2) { // size

			ProductSizeAdapter sizeAdapter = IOrderUtility
					.getProductSize(TakeOrderActivity.this,mi.getProductID());
			// size dialog
			final Dialog sizeDialog = new Dialog(
					TakeOrderActivity.this,
					R.style.CustomDialog);

			LayoutInflater inflater = LayoutInflater
					.from(TakeOrderActivity.this);
			View sizeView = inflater.inflate(
					R.layout.product_size_layout, null);

			final ImageButton btnCloseDialog = (ImageButton) 
					sizeView.findViewById(R.id.imageButtonCloseSize);
			btnCloseDialog.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					sizeDialog.dismiss();		
				}
				
			});
			
			ListView sizeListView = (ListView) sizeView
					.findViewById(R.id.listViewProductSize);

			sizeListView.setAdapter(sizeAdapter);

			sizeListView
					.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(
								AdapterView<?> parent, View v,
								int position, long id) {
							ProductGroups.PComponentSet pc = (ProductGroups.PComponentSet) parent
									.getItemAtPosition(position);

							addOrder(pc.getProductID(), pc.getMenuName(), pc.getProductTypeID(),
									mSaleMode, 1, pc.getPricePerUnit(), pc.getVatAmount());
							
							sizeDialog.dismiss();
						}

					});

			sizeDialog.setContentView(sizeView);
			sizeDialog.show();
		} else if(mi.getProductTypeID() == 5){
			addOrderType5(mi.getProductID(), mi.getMenuName(), mi.getProductTypeID(),
					mSaleMode, 1, mi.getPricePerUnit(), mi.getVatAmount());
		}else{
			// if order from menu detail
			// can edit qty
			double menuQty = mi.getProductQty() == 0 ? 1 : mi.getProductQty();
			
			addOrder(mi.getProductID(), mi.getMenuName(), mi.getProductTypeID(),
					mSaleMode, menuQty, mi.getPricePerUnit(), mi.getVatAmount());
		}
	}
	
	private void addOrderSetOfProduct(int orderDetailId, int productId, double productPrice, double productQty){
		POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
		posOrder.addOrderSetOfProduct(GlobalVar.TRANSACTION_ID, orderDetailId, 
				productId, productPrice, productQty);
	}
	
	private void addOrderType5(final int productId, final String menuName, final int productType,
			final int saleMode, final double qty, final double price, final double vatAmount){
		
		final POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
		
		final Dialog dialog = new Dialog(TakeOrderActivity.this, R.style.CustomDialog);
		LayoutInflater inflater = LayoutInflater.from(TakeOrderActivity.this);
		View v = inflater.inflate(R.layout.open_price_dialog, null);
		TextView textView1 = (TextView) v.findViewById(R.id.textView1);
		textView1.setText(R.string.enter_product_amount);
		final EditText txtAmount = (EditText) v.findViewById(R.id.editTextDetailPrice);
		Button btnCancel = (Button) v.findViewById(R.id.button1);
		Button btnOk = (Button) v.findViewById(R.id.button2);
		
		btnCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(txtAmount.getWindowToken(), 0);
				dialog.dismiss();
			}
			
		});
		btnOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(!txtAmount.equals("")){
					try {
						double openQty = Double.parseDouble(txtAmount.getText().toString());
						int orderId = posOrder.addOrderDetail(GlobalVar.TRANSACTION_ID,
								GlobalVar.COMPUTER_ID, GlobalVar.SHOP_ID, productId,
								menuName, productType, saleMode, openQty, price, vatAmount, 0,
								0, 0, 0, mSeatId, mSeatName, mCourseId, mCourseName);
					
						MenuDataItem menuDataItem = posOrder.listOrder(GlobalVar.TRANSACTION_ID,
										GlobalVar.COMPUTER_ID, orderId, mSeatId);
						mOrderLst.add(menuDataItem);
						mOrderLstAdapter.notifyDataSetChanged();

						summaryTotalSalePrice();
						mOrderListView.setSelection(mOrderLst.size());
	
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(txtAmount.getWindowToken(), 0);
						dialog.dismiss();	
					} catch (NumberFormatException e) {
						IOrderUtility.alertDialog(TakeOrderActivity.this, 
								R.string.global_dialog_title_error, "Please enter number.", 0);
					}
				}else{
					IOrderUtility.alertDialog(TakeOrderActivity.this, 
							R.string.global_dialog_title_error, R.string.enter_open_price, 0);
				}
			}
			
		});
		dialog.setContentView(v);

		dialog.getWindow()
				.setSoftInputMode(
						WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		dialog.getWindow().setLayout(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		dialog.show();
	}
	
	private void addOrder(final int productId, final String menuName, final int productType,
			final int saleMode, final double qty, final double price, final double vatAmount) {

		final POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);

		// open price
		if(price == -1){
			final Dialog dialog = new Dialog(TakeOrderActivity.this, R.style.CustomDialog);
			LayoutInflater inflater = LayoutInflater.from(TakeOrderActivity.this);
			View v = inflater.inflate(R.layout.open_price_dialog, null);
			final EditText txtPrice = (EditText) v.findViewById(R.id.editTextDetailPrice);
			Button btnCancel = (Button) v.findViewById(R.id.button1);
			Button btnOk = (Button) v.findViewById(R.id.button2);
			
			btnCancel.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(txtPrice.getWindowToken(), 0);
					dialog.dismiss();
				}
				
			});
			btnOk.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if(!txtPrice.equals("")){
						try {
							double openPrice = Double.parseDouble(txtPrice.getText().toString());
							int orderId = posOrder.addOrderDetail(GlobalVar.TRANSACTION_ID,
									GlobalVar.COMPUTER_ID, GlobalVar.SHOP_ID, productId,
									menuName, productType, saleMode, qty, openPrice, vatAmount, 0,
									0, 0, 0, mSeatId, mSeatName, mCourseId, mCourseName);
						
							// produce order set
							if (productType == 7) {
								Intent intent = new Intent(
										TakeOrderActivity.this,
										MenuSetActivity.class);
								intent.putExtra("transactionId",
										GlobalVar.TRANSACTION_ID);
								intent.putExtra("productId", productId);
								intent.putExtra("orderId", orderId);
								intent.putExtra("commentType", mCommentType);
								TakeOrderActivity.this.startActivity(intent);
							}
								
							MenuDataItem menuDataItem = 
									posOrder.listOrder(GlobalVar.TRANSACTION_ID,GlobalVar.COMPUTER_ID,
											orderId, mSeatId);
							mOrderLst.add(menuDataItem);
							mOrderLstAdapter.notifyDataSetChanged();
	
							summaryTotalSalePrice();
							mOrderListView.setSelection(mOrderLst.size());
		
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(txtPrice.getWindowToken(), 0);
							dialog.dismiss();	
						} catch (NumberFormatException e) {
							IOrderUtility.alertDialog(TakeOrderActivity.this, 
									R.string.global_dialog_title_error, "Please enter number.", 0);
						}
					}else{
						IOrderUtility.alertDialog(TakeOrderActivity.this, 
								R.string.global_dialog_title_error, R.string.enter_open_price, 0);
					}
				}
				
			});
			dialog.setContentView(v);

			dialog.getWindow()
					.setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			dialog.getWindow().setLayout(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			//dialog.getWindow().setGravity(Gravity.BOTTOM);
			dialog.show();
		}else{
			int orderId = posOrder.addOrderDetail(GlobalVar.TRANSACTION_ID,
					GlobalVar.COMPUTER_ID, GlobalVar.SHOP_ID, productId,
					menuName, productType, saleMode, qty, price, vatAmount, 0,
					0, 0, 0, mSeatId, mSeatName, mCourseId, mCourseName);
			
			// produce order set
			if (productType == 7) {
				Intent intent = new Intent(
						TakeOrderActivity.this,
						MenuSetActivity.class);
				intent.putExtra("transactionId",
						GlobalVar.TRANSACTION_ID);
				intent.putExtra("productId", productId);
				intent.putExtra("orderId", orderId);
				intent.putExtra("commentType", mCommentType);
				TakeOrderActivity.this.startActivity(intent);
			}
			
			MenuDataItem menuDataItem = 
					posOrder.listOrder(GlobalVar.TRANSACTION_ID,GlobalVar.COMPUTER_ID,
							orderId, mSeatId);
			mOrderLst.add(menuDataItem);
			mOrderLstAdapter.notifyDataSetChanged();
	
			summaryTotalSalePrice();
			mOrderListView.setSelection(mOrderLst.size());
		}
		
	}
	
	private void holdOrder() {
		if (mOrderLst.size() > 0) {
			LayoutInflater inflater = LayoutInflater
					.from(TakeOrderActivity.this);
			View holdView = inflater.inflate(R.layout.hold_layout, null);

			final EditText txtRemark = (EditText) holdView
					.findViewById(R.id.editTextHoldRemark);
			final TextView tvCustQty = (TextView) holdView
					.findViewById(R.id.textViewHoldCustQty);
			final TextView tvHoldTableName = (TextView) holdView
					.findViewById(R.id.textViewHoldTable);
			final TextView tvHoldQueueName = (TextView) holdView
					.findViewById(R.id.textViewHoldQueue);
			Button btnMinus = (Button) holdView
					.findViewById(R.id.buttonHoldCustMinus);
			Button btnPlus = (Button) holdView
					.findViewById(R.id.buttonHoldCustPlus);
			LinearLayout holdTableLayout = (LinearLayout) holdView
					.findViewById(R.id.linearLayout2);
			LinearLayout holdQueueLayout = (LinearLayout) holdView
					.findViewById(R.id.linearLayout1);
			Button btnClose = (Button) holdView.findViewById(R.id.buttonClose);
			Button btnYes = (Button) holdView.findViewById(R.id.buttonOk);

			if (mCustomerQty > 0)
				tvCustQty.setText(mGlobalVar.qtyFormat.format(mCustomerQty));

			IOrderUtility.touchQty(mGlobalVar, btnMinus, btnPlus, tvCustQty);

			if(mCurrTableId != 0){
				holdTableLayout.setVisibility(View.VISIBLE);
			}else{
				holdTableLayout.setVisibility(View.GONE);
			}
			
			if(mCurrQueueId != 0){
				holdQueueLayout.setVisibility(View.VISIBLE);
			}else{
				holdQueueLayout.setVisibility(View.GONE);
			}
			tvHoldTableName.setText(mCurrTableName);
			tvHoldQueueName.setText(mCurrQueueName);
			
			final Dialog holdDialog = new Dialog(TakeOrderActivity.this,
					R.style.CustomDialogBottomRadius);
			holdDialog.setContentView(holdView);
			holdDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
			holdDialog.getWindow().setLayout(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			holdDialog.getWindow().setGravity(Gravity.TOP);
			holdDialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			holdDialog.show();

			btnClose.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(txtRemark.getWindowToken(), 0);

					holdDialog.dismiss();
				}

			});

			btnYes.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(txtRemark.getWindowToken(), 0);

					POSOrdering pos = new POSOrdering(TakeOrderActivity.this);
					pos.holdTransaction(GlobalVar.TRANSACTION_ID,
							GlobalVar.COMPUTER_ID, GlobalVar.STAFF_ID,
							mCurrTableId, mCurrTableName, mCurrQueueId,
							mCurrQueueName,
							Integer.parseInt(tvCustQty.getText().toString()),
							txtRemark.getText().toString());

					countHoldOrder();

					GlobalVar.TRANSACTION_ID = 0;
					iOrderInit();

					clearSetQueue();
					clearSetTable();

					holdDialog.dismiss();
				}

			});
		}

	}

	// send order from queue task
	private class SendOrderFromQueueTask extends SubmitSendOrder {
		public SendOrderFromQueueTask(Context c, GlobalVar gb, int queueId) {
			super(c, gb,
					"WSiOrder_JSON_SendPreOrderTableTransactionFromQueueID",
					queueId);
		}
	}

	// submit send order task
	private class SubmitSendOrder extends WebServiceTask {
		// protected static final String webMethod =
		// "WSiOrder_JSON_SendTableOrderTransactionData";

		// answer question
		private List<ProductGroups.QuestionAnswerData> qsAnsLst;
		
		public SubmitSendOrder(Context c, GlobalVar gb, String method,
				String queueName, int custQty) {
			super(c, gb, method);

			PropertyInfo property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(GlobalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			if (globalVar.MEMBER_ID != 0) {
				property = new PropertyInfo();
				property.setName("iMemberID");
				property.setValue(GlobalVar.MEMBER_ID);
				property.setType(int.class);
				soapRequest.addProperty(property);
			}

			// sendorder json
			POSData_OrderTransInfo orderTrans = new POSData_OrderTransInfo();
			orderTrans.setSzTransNote("");

			orderTrans.xListPaymentAmount = new ArrayList<POSData_OrderTransInfo.POSData_PaymentAmount>();

			POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
			List<syn.pos.data.model.MenuDataItem> ml = posOrder.listOrder(
					GlobalVar.TRANSACTION_ID, GlobalVar.COMPUTER_ID);

			if (ml.size() > 0) {
				orderTrans.xListOrderItem = new ArrayList<POSData_OrderTransInfo.POSData_OrderItemInfo>();
				for (MenuDataItem mi : ml) {
					POSData_OrderTransInfo.POSData_OrderItemInfo orderItem = new POSData_OrderTransInfo.POSData_OrderItemInfo();
					orderItem.setiProductID(mi.getProductID());
					orderItem.setfProductQty(mi.getProductQty());
					orderItem.setfProductPrice(mi.getPricePerUnit());
					orderItem.setSzOrderComment(mi.getOrderComment());
					orderItem.setiSaleMode(mi.getSaleMode());
					orderItem.setiSeatID(mi.getSeatId());
					orderItem.setiCourseID(mi.getCourseId());

					// type7
					if (mi.pCompSetLst != null && mi.pCompSetLst.size() > 0) {
						orderItem.xListChildOrderSetLinkType7 = new ArrayList<POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info>();

						for (ProductGroups.PComponentSet pcs : mi.pCompSetLst) {
							POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info type7 = new POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info();
							type7.setiPGroupID(pcs.getPGroupID());
							type7.setiProductID(pcs.getProductID());
							type7.setfProductQty(pcs.getProductQty());
							type7.setfProductPrice(pcs.getPricePerUnit());
							type7.setiSetGroupNo(pcs.getSetGroupNo());
							type7.setSzOrderComment(pcs.getOrderComment());

							// comment of type 7
							if (pcs.menuCommentList != null
									&& pcs.menuCommentList.size() > 0) {
								type7.xListCommentInfo = new ArrayList<POSData_OrderTransInfo.POSData_CommentInfo>();

								for (MenuGroups.MenuComment mc : pcs.menuCommentList) {
									POSData_OrderTransInfo.POSData_CommentInfo comment = new POSData_OrderTransInfo.POSData_CommentInfo();
									comment.setiCommentID(mc.getMenuCommentID());
									comment.setfCommentQty(mc.getCommentQty());
									comment.setfCommentPrice(mc
											.getProductPricePerUnit());

									type7.xListCommentInfo.add(comment);
								}
							}

							orderItem.xListChildOrderSetLinkType7.add(type7);
						}
					}

					// type7
					if (mi.menuCommentList != null
							&& mi.menuCommentList.size() > 0) {

						orderItem.xListCommentInfo = new ArrayList<POSData_OrderTransInfo.POSData_CommentInfo>();

						for (MenuGroups.MenuComment mc : mi.menuCommentList) {
							POSData_OrderTransInfo.POSData_CommentInfo orderComment = new POSData_OrderTransInfo.POSData_CommentInfo();

							orderComment.setiCommentID(mc.getMenuCommentID());
							orderComment.setfCommentQty(mc.getCommentQty());
							orderComment.setfCommentPrice(mc
									.getProductPricePerUnit());

							orderItem.xListCommentInfo.add(orderComment);
						}
					}
					orderTrans.xListOrderItem.add(orderItem);
				}
			}

			Gson gson = new Gson();
			String jsonToSend = gson.toJson(orderTrans);
			Log.d("Log order data", jsonToSend);

			property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iCustQty");
			property.setValue(custQty);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("szQueueName");
			property.setValue(queueName);
			property.setType(String.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("szJSon_OrderTransData");
			property.setValue(jsonToSend);
			property.setType(String.class);
			soapRequest.addProperty(property);
		}
		
		public SubmitSendOrder(Context c, GlobalVar gb, String method, int saleMode, String ref, int custQty) {
			super(c, gb, method);

			PropertyInfo property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(GlobalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			if (globalVar.MEMBER_ID != 0) {
				property = new PropertyInfo();
				property.setName("iMemberID");
				property.setValue(GlobalVar.MEMBER_ID);
				property.setType(int.class);
				soapRequest.addProperty(property);
			}

			// sendorder json
			POSData_OrderTransInfo orderTrans = new POSData_OrderTransInfo();
			orderTrans.setSzTransNote(ref);

			orderTrans.xListPaymentAmount = new ArrayList<POSData_OrderTransInfo.POSData_PaymentAmount>();

			POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
			List<syn.pos.data.model.MenuDataItem> ml = posOrder.listOrder(
					GlobalVar.TRANSACTION_ID, GlobalVar.COMPUTER_ID);

			if (ml.size() > 0) {
				orderTrans.xListOrderItem = new ArrayList<POSData_OrderTransInfo.POSData_OrderItemInfo>();
				for (MenuDataItem mi : ml) {
					POSData_OrderTransInfo.POSData_OrderItemInfo orderItem = new POSData_OrderTransInfo.POSData_OrderItemInfo();
					orderItem.setiProductID(mi.getProductID());
					orderItem.setfProductQty(mi.getProductQty());
					orderItem.setfProductPrice(mi.getPricePerUnit());
					orderItem.setSzOrderComment(mi.getOrderComment());
					orderItem.setiSaleMode(mi.getSaleMode());
					orderItem.setiSeatID(mi.getSeatId());
					orderItem.setiCourseID(mi.getCourseId());

					// type7
					if (mi.pCompSetLst != null && mi.pCompSetLst.size() > 0) {
						orderItem.xListChildOrderSetLinkType7 = new ArrayList<POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info>();

						for (ProductGroups.PComponentSet pcs : mi.pCompSetLst) {
							POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info type7 = new POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info();
							type7.setiPGroupID(pcs.getPGroupID());
							type7.setiProductID(pcs.getProductID());
							type7.setfProductQty(pcs.getProductQty());
							type7.setfProductPrice(pcs.getPricePerUnit());
							type7.setiSetGroupNo(pcs.getSetGroupNo());
							type7.setSzOrderComment(pcs.getOrderComment());

							// comment of type 7
							if (pcs.menuCommentList != null
									&& pcs.menuCommentList.size() > 0) {
								type7.xListCommentInfo = new ArrayList<POSData_OrderTransInfo.POSData_CommentInfo>();

								for (MenuGroups.MenuComment mc : pcs.menuCommentList) {
									POSData_OrderTransInfo.POSData_CommentInfo comment = new POSData_OrderTransInfo.POSData_CommentInfo();
									comment.setiCommentID(mc.getMenuCommentID());
									comment.setfCommentQty(mc.getCommentQty());
									comment.setfCommentPrice(mc
											.getProductPricePerUnit());

									type7.xListCommentInfo.add(comment);
								}
							}

							orderItem.xListChildOrderSetLinkType7.add(type7);
						}
					}

					// type7
					if (mi.menuCommentList != null
							&& mi.menuCommentList.size() > 0) {

						orderItem.xListCommentInfo = new ArrayList<POSData_OrderTransInfo.POSData_CommentInfo>();

						for (MenuGroups.MenuComment mc : mi.menuCommentList) {
							POSData_OrderTransInfo.POSData_CommentInfo orderComment = new POSData_OrderTransInfo.POSData_CommentInfo();

							orderComment.setiCommentID(mc.getMenuCommentID());
							orderComment.setfCommentQty(mc.getCommentQty());
							orderComment.setfCommentPrice(mc
									.getProductPricePerUnit());

							orderItem.xListCommentInfo.add(orderComment);
						}
					}
					orderTrans.xListOrderItem.add(orderItem);
				}
			}

			Gson gson = new Gson();
			String jsonToSend = gson.toJson(orderTrans);
			Log.d("Log order data", jsonToSend);

			property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iCustomerQty");
			property.setValue(custQty);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iSaleMode");
			property.setValue(saleMode);
			property.setType(String.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("szJSon_OrderTransData");
			property.setValue(jsonToSend);
			property.setType(String.class);
			soapRequest.addProperty(property);
		}

		public SubmitSendOrder(Context c, GlobalVar gb, String method,
				int queueId) {
			super(c, gb, method);

			PropertyInfo property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(GlobalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			if (globalVar.MEMBER_ID != 0) {
				property = new PropertyInfo();
				property.setName("iMemberID");
				property.setValue(GlobalVar.MEMBER_ID);
				property.setType(int.class);
				soapRequest.addProperty(property);
			}

			// sendorder json
			POSData_OrderTransInfo orderTrans = new POSData_OrderTransInfo();
			orderTrans.setSzTransNote("");

			orderTrans.xListPaymentAmount = new ArrayList<POSData_OrderTransInfo.POSData_PaymentAmount>();

			POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
			List<syn.pos.data.model.MenuDataItem> ml = posOrder.listOrder(
					GlobalVar.TRANSACTION_ID, GlobalVar.COMPUTER_ID);

			if (ml.size() > 0) {
				orderTrans.xListOrderItem = new ArrayList<POSData_OrderTransInfo.POSData_OrderItemInfo>();
				for (MenuDataItem mi : ml) {
					POSData_OrderTransInfo.POSData_OrderItemInfo orderItem = new POSData_OrderTransInfo.POSData_OrderItemInfo();
					orderItem.setiProductID(mi.getProductID());
					orderItem.setfProductQty(mi.getProductQty());
					orderItem.setfProductPrice(mi.getPricePerUnit());
					orderItem.setSzOrderComment(mi.getOrderComment());
					orderItem.setiSaleMode(mi.getSaleMode());
					orderItem.setiSeatID(mi.getSeatId());
					orderItem.setiCourseID(mi.getCourseId());

					// type7
					if (mi.pCompSetLst != null && mi.pCompSetLst.size() > 0) {
						orderItem.xListChildOrderSetLinkType7 = new ArrayList<POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info>();

						for (ProductGroups.PComponentSet pcs : mi.pCompSetLst) {
							POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info type7 = new POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info();
							type7.setiPGroupID(pcs.getPGroupID());
							type7.setiProductID(pcs.getProductID());
							type7.setfProductQty(pcs.getProductQty());
							type7.setfProductPrice(pcs.getPricePerUnit());
							type7.setiSetGroupNo(pcs.getSetGroupNo());
							type7.setSzOrderComment(pcs.getOrderComment());

							// comment of type 7
							if (pcs.menuCommentList != null
									&& pcs.menuCommentList.size() > 0) {
								type7.xListCommentInfo = new ArrayList<POSData_OrderTransInfo.POSData_CommentInfo>();

								for (MenuGroups.MenuComment mc : pcs.menuCommentList) {
									POSData_OrderTransInfo.POSData_CommentInfo comment = new POSData_OrderTransInfo.POSData_CommentInfo();
									comment.setiCommentID(mc.getMenuCommentID());
									comment.setfCommentQty(mc.getCommentQty());
									comment.setfCommentPrice(mc
											.getProductPricePerUnit());

									type7.xListCommentInfo.add(comment);
								}
							}

							orderItem.xListChildOrderSetLinkType7.add(type7);
						}
					}

					// type7
					if (mi.menuCommentList != null
							&& mi.menuCommentList.size() > 0) {

						orderItem.xListCommentInfo = new ArrayList<POSData_OrderTransInfo.POSData_CommentInfo>();

						for (MenuGroups.MenuComment mc : mi.menuCommentList) {
							POSData_OrderTransInfo.POSData_CommentInfo orderComment = new POSData_OrderTransInfo.POSData_CommentInfo();

							orderComment.setiCommentID(mc.getMenuCommentID());
							orderComment.setfCommentQty(mc.getCommentQty());
							orderComment.setfCommentPrice(mc
									.getProductPricePerUnit());

							orderItem.xListCommentInfo.add(orderComment);
						}
					}
					orderTrans.xListOrderItem.add(orderItem);
				}
			}

			Gson gson = new Gson();
			String jsonToSend = gson.toJson(orderTrans);
			Log.d("Log order data", jsonToSend);

			property = new PropertyInfo();
			property.setName("iQueueID");
			property.setValue(queueId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("szJSon_OrderTransData");
			property.setValue(jsonToSend);
			property.setType(String.class);
			soapRequest.addProperty(property);
		}

		public SubmitSendOrder(Context c, GlobalVar gb, String webMethod) {
			super(c, gb, webMethod);

			PropertyInfo property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(GlobalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			if (globalVar.MEMBER_ID != 0) {
				property = new PropertyInfo();
				property.setName("iMemberID");
				property.setValue(GlobalVar.MEMBER_ID);
				property.setType(int.class);
				soapRequest.addProperty(property);
			}

			// sendorder json
			POSData_OrderTransInfo orderTrans = new POSData_OrderTransInfo();
			orderTrans.setSzTransNote("");

			orderTrans.xListPaymentAmount = new ArrayList<POSData_OrderTransInfo.POSData_PaymentAmount>();

			POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
			List<syn.pos.data.model.MenuDataItem> ml = posOrder.listOrder(
					GlobalVar.TRANSACTION_ID, GlobalVar.COMPUTER_ID);

			if (ml.size() > 0) {
				orderTrans.xListOrderItem = new ArrayList<POSData_OrderTransInfo.POSData_OrderItemInfo>();
				for (MenuDataItem mi : ml) {
					POSData_OrderTransInfo.POSData_OrderItemInfo orderItem = new POSData_OrderTransInfo.POSData_OrderItemInfo();
					orderItem.setiProductID(mi.getProductID());
					orderItem.setfProductQty(mi.getProductQty());
					orderItem.setfProductPrice(mi.getPricePerUnit());
					orderItem.setSzOrderComment(mi.getOrderComment());
					orderItem.setiSaleMode(mi.getSaleMode());
					orderItem.setiSeatID(mi.getSeatId());
					orderItem.setiCourseID(mi.getCourseId());

					// type7
					if (mi.pCompSetLst != null && mi.pCompSetLst.size() > 0) {
						orderItem.xListChildOrderSetLinkType7 = new ArrayList<POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info>();

						for (ProductGroups.PComponentSet pcs : mi.pCompSetLst) {
							POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info type7 = new POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info();
							type7.setiPGroupID(pcs.getPGroupID());
							type7.setiProductID(pcs.getProductID());
							type7.setfProductQty(pcs.getProductQty());
							type7.setfProductPrice(pcs.getPricePerUnit());
							type7.setiSetGroupNo(pcs.getSetGroupNo());
							type7.setSzOrderComment(pcs.getOrderComment());

							// comment of type 7
							if (pcs.menuCommentList != null
									&& pcs.menuCommentList.size() > 0) {
								type7.xListCommentInfo = new ArrayList<POSData_OrderTransInfo.POSData_CommentInfo>();

								for (MenuGroups.MenuComment mc : pcs.menuCommentList) {
									POSData_OrderTransInfo.POSData_CommentInfo comment = new POSData_OrderTransInfo.POSData_CommentInfo();
									comment.setiCommentID(mc.getMenuCommentID());
									comment.setfCommentQty(mc.getCommentQty());
									comment.setfCommentPrice(mc
											.getProductPricePerUnit());

									type7.xListCommentInfo.add(comment);
								}
							}

							orderItem.xListChildOrderSetLinkType7.add(type7);
						}
					}

					// type7
					if (mi.menuCommentList != null
							&& mi.menuCommentList.size() > 0) {

						orderItem.xListCommentInfo = new ArrayList<POSData_OrderTransInfo.POSData_CommentInfo>();

						for (MenuGroups.MenuComment mc : mi.menuCommentList) {
							POSData_OrderTransInfo.POSData_CommentInfo orderComment = new POSData_OrderTransInfo.POSData_CommentInfo();

							orderComment.setiCommentID(mc.getMenuCommentID());
							orderComment.setfCommentQty(mc.getCommentQty());
							orderComment.setfCommentPrice(mc
									.getProductPricePerUnit());

							orderItem.xListCommentInfo.add(orderComment);
						}
					}
					orderTrans.xListOrderItem.add(orderItem);
				}
			}

			Gson gson = new Gson();
			String jsonToSend = gson.toJson(orderTrans);
			Log.d("Log order data", jsonToSend);

			property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(mCurrTableId);
			property.setType(String.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iCustomerQty");
			property.setValue(mCustomerQty);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("szJSon_OrderTransData");
			property.setValue(jsonToSend);
			property.setType(String.class);
			soapRequest.addProperty(property);
		}
		
		public SubmitSendOrder(Context c, GlobalVar gb, String webMethod, List<ProductGroups.QuestionAnswerData> qsAnsLst) {
			super(c, gb, webMethod);

			this.qsAnsLst = qsAnsLst;
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(GlobalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			if (globalVar.MEMBER_ID != 0) {
				property = new PropertyInfo();
				property.setName("iMemberID");
				property.setValue(GlobalVar.MEMBER_ID);
				property.setType(int.class);
				soapRequest.addProperty(property);
			}

			// sendorder json
			POSData_OrderTransInfo orderTrans = new POSData_OrderTransInfo();
			orderTrans.setSzTransNote("");

			orderTrans.xListPaymentAmount = new ArrayList<POSData_OrderTransInfo.POSData_PaymentAmount>();

			POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
			List<syn.pos.data.model.MenuDataItem> ml = posOrder.listOrder(
					GlobalVar.TRANSACTION_ID, GlobalVar.COMPUTER_ID);

			if (ml.size() > 0) {
				orderTrans.xListOrderItem = new ArrayList<POSData_OrderTransInfo.POSData_OrderItemInfo>();
				for (MenuDataItem mi : ml) {
					POSData_OrderTransInfo.POSData_OrderItemInfo orderItem = new POSData_OrderTransInfo.POSData_OrderItemInfo();
					orderItem.setiProductID(mi.getProductID());
					orderItem.setfProductQty(mi.getProductQty());
					orderItem.setfProductPrice(mi.getPricePerUnit());
					orderItem.setSzOrderComment(mi.getOrderComment());
					orderItem.setiSaleMode(mi.getSaleMode());
					orderItem.setiSeatID(mi.getSeatId());
					orderItem.setiCourseID(mi.getCourseId());

					// type7
					if (mi.pCompSetLst != null && mi.pCompSetLst.size() > 0) {
						orderItem.xListChildOrderSetLinkType7 = new ArrayList<POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info>();

						for (ProductGroups.PComponentSet pcs : mi.pCompSetLst) {
							POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info type7 = new POSData_OrderTransInfo.POSData_ChildOrderSetLinkType7Info();
							type7.setiPGroupID(pcs.getPGroupID());
							type7.setiProductID(pcs.getProductID());
							type7.setfProductQty(pcs.getProductQty());
							type7.setfProductPrice(pcs.getPricePerUnit());
							type7.setiSetGroupNo(pcs.getSetGroupNo());
							type7.setSzOrderComment(pcs.getOrderComment());

							// comment of type 7
							if (pcs.menuCommentList != null
									&& pcs.menuCommentList.size() > 0) {
								type7.xListCommentInfo = new ArrayList<POSData_OrderTransInfo.POSData_CommentInfo>();

								for (MenuGroups.MenuComment mc : pcs.menuCommentList) {
									POSData_OrderTransInfo.POSData_CommentInfo comment = new POSData_OrderTransInfo.POSData_CommentInfo();
									comment.setiCommentID(mc.getMenuCommentID());
									comment.setfCommentQty(mc.getCommentQty());
									comment.setfCommentPrice(mc
											.getProductPricePerUnit());

									type7.xListCommentInfo.add(comment);
								}
							}

							orderItem.xListChildOrderSetLinkType7.add(type7);
						}
					}

					// type7
					if (mi.menuCommentList != null
							&& mi.menuCommentList.size() > 0) {

						orderItem.xListCommentInfo = new ArrayList<POSData_OrderTransInfo.POSData_CommentInfo>();

						for (MenuGroups.MenuComment mc : mi.menuCommentList) {
							POSData_OrderTransInfo.POSData_CommentInfo orderComment = new POSData_OrderTransInfo.POSData_CommentInfo();

							orderComment.setiCommentID(mc.getMenuCommentID());
							orderComment.setfCommentQty(mc.getCommentQty());
							orderComment.setfCommentPrice(mc
									.getProductPricePerUnit());

							orderItem.xListCommentInfo.add(orderComment);
						}
					}
					orderTrans.xListOrderItem.add(orderItem);
				}
			}

			Gson gson = new Gson();
			String jsonToSend = gson.toJson(orderTrans);
			Log.d("Log order data", jsonToSend);

			property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(mCurrTableId);
			property.setType(String.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iCustomerQty");
			property.setValue(mCustomerQty);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("szJSon_OrderTransData");
			property.setValue(jsonToSend);
			property.setType(String.class);
			soapRequest.addProperty(property);
		}

		private void sendSuccess(){
			final CustomDialog customDialog = new CustomDialog(
					TakeOrderActivity.this, R.style.CustomDialog);
			customDialog.setCancelable(false);
			customDialog.title.setVisibility(View.VISIBLE);
			customDialog.title
					.setText(R.string.send_order_dialog_title);
			customDialog.message
					.setText(R.string.send_order_dialog_success_msg);

			customDialog.btnCancel.setVisibility(View.GONE);
			customDialog.btnOk
					.setText(R.string.global_close_dialog_btn);
//			customDialog.btnOk
//					.setBackgroundResource(R.drawable.green_button);
			customDialog.btnOk
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							
							customDialog.dismiss();
						}
					});

			customDialog.show();

			POSOrdering posOrder = new POSOrdering(
					TakeOrderActivity.this);

			posOrder.deleteTransaction(GlobalVar.TRANSACTION_ID,
					GlobalVar.COMPUTER_ID);			

			// clear
			clearTransaction();
			clearSetTable();
			clearSetQueue();
			clearSeat();

			iOrderInit();	
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (progress.isShowing())
				progress.dismiss();

			GsonDeserialze gdz = new GsonDeserialze();
			try {
				WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);

				if (wsResult.getiResultID() == 0) {
					
					if(GlobalVar.isEnableTableQuestion){
						if(qsAnsLst != null && qsAnsLst.size() > 0){
							// send answer question
							new QuestionTask(TakeOrderActivity.this, globalVar, mCurrTableId, 
									this.qsAnsLst, new WebServiceStateListener(){
		
										@Override
										public void onSuccess() {
											sendSuccess();
										}
		
										@Override
										public void onNotSuccess() {
											// TODO Auto-generated method stub
											
										}
							}).execute(GlobalVar.FULL_URL);
						}else{
							sendSuccess();
						}
					}else{
						sendSuccess();
					}

				} else {
					clearTable();
					clearQueue();
					
					final CustomDialog customDialog = new CustomDialog(
							TakeOrderActivity.this, R.style.CustomDialog);
					customDialog.title.setVisibility(View.VISIBLE);
					customDialog.title
							.setText(R.string.global_dialog_title_error);
					customDialog.message.setText(wsResult.getSzResultData()
							.equals("") ? result : wsResult.getSzResultData());

					customDialog.btnCancel.setVisibility(View.GONE);
					customDialog.btnOk
							.setText(R.string.global_close_dialog_btn);
					customDialog.btnOk
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									customDialog.dismiss();
								}
							});
					customDialog.show();
				}
			} catch (Exception e) {
				final CustomDialog customDialog = new CustomDialog(
						TakeOrderActivity.this, R.style.CustomDialog);
				customDialog.title.setVisibility(View.VISIBLE);
				customDialog.title.setText(R.string.global_dialog_title_error);
				customDialog.message.setText(result);

				customDialog.btnCancel.setVisibility(View.GONE);
				customDialog.btnOk.setText(R.string.global_close_dialog_btn);
				customDialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						customDialog.dismiss();
					}
				});
				customDialog.show();
			}
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.send_order_progress);
			progress.setMessage(tvProgress.getText().toString());
			progress.show();
		}
	}

	private class SelectTableTask extends LoadTableTaskQuestion {

		public SelectTableTask(Context c, GlobalVar gb) {
			super(c, gb);

			tvTitle.setText(R.string.select_table_title);
			
			btnConfirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (tableId != 0) {
						mCurrTableId = tableId;
						mCurrTableName = tbName.getTableName();
					}
					setSelectedTable();
					dialogSelectTable.dismiss();
				}
			});
			
			if(mCurrTableId != 0)
				btnClearTable.setVisibility(View.VISIBLE);
			
			btnClearTable.setOnClickListener(new OnClickListener() {
	
				@Override
				public void onClick(View v) {
					final CustomDialog cfDialog = 
							new CustomDialog(TakeOrderActivity.this, R.style.CustomDialog);
					cfDialog.title.setVisibility(View.VISIBLE);
					cfDialog.title.setText(R.string.clear_table_title);
					cfDialog.message.setText(R.string.cf_clear_table);
					cfDialog.btnCancel.setOnClickListener(new OnClickListener(){
	
						@Override
						public void onClick(View v) {
							cfDialog.dismiss();
						}
						
					});
					cfDialog.btnOk.setOnClickListener(new OnClickListener(){
	
						@Override
						public void onClick(View v) {
							clearSetTable();	
							cfDialog.dismiss();
							dialogSelectTable.dismiss();
						}
						
					});
					cfDialog.show();
				}
			});
		}

		@Override
		protected void popupQuestion() {
			btnConfirm.setEnabled(false);
			if(tbName.getSTATUS() == 0){
				// add answer to temp 
				final QuestionGroups qsGroup = new QuestionGroups(TakeOrderActivity.this);
				qsGroup.insertCurrentAnswerQuestion(new ArrayList<ProductGroups.QuestionAnswerData>());
				
				// popup
				LayoutInflater inflater = LayoutInflater.from(TakeOrderActivity.this);
				View questView = inflater.inflate(R.layout.question_list_layout, null);
				TextView tvQestionTitle = (TextView) questView.findViewById(R.id.textView1);
				tvQestionTitle.setText("Table: " + tbName);
				Button btnOk = (Button) questView.findViewById(R.id.button1);
				Button btnCancel = (Button) questView.findViewById(R.id.button2);
				final TextView tvRequire = (TextView) questView.findViewById(R.id.textView2);
				final ListView lvQuestion = (ListView) questView.findViewById(R.id.listView1);
				lvQuestion.setEnabled(false);
				
				// question adapter
				final List<ProductGroups.QuestionDetail> qsDetailLst = qsGroup.listQuestionDetail();
				
				if(qsDetailLst != null && qsDetailLst.size() > 0){
					final SelectTableQuestionAdapter qsAdapter = 
							new SelectTableQuestionAdapter(context, globalVar, qsDetailLst, tvSelectTableCusNo);
					lvQuestion.setAdapter(qsAdapter);
					
					final Dialog dialog = new Dialog(TakeOrderActivity.this, R.style.CustomDialog);
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
								dialogSelectTable.dismiss();
								dialog.dismiss();
								btnConfirm.setEnabled(true);
								// submit send order 
								
								QuestionGroups qsGroup = new QuestionGroups(TakeOrderActivity.this);
								selectedAnswerLst = qsGroup.listAnswerQuestion();
								
								if (tableId != 0) {
									mCurrTableId = tableId;
									mCurrTableName = tbName.getTableName();
								}
								
								mCustomerQty = Integer.parseInt(tvSelectTableCusNo.getText().toString());

								setSelectedTable();
							}
						}
					});
					
					btnCancel.setOnClickListener(new OnClickListener(){
		
						@Override
						public void onClick(View v) {
							btnConfirm.setEnabled(false);
							dialog.dismiss();
						}
						
					});
				}
			}else{
				btnConfirm.setEnabled(true);
			}
		}
	}

	// queue adapter
	private class QueueListAdapter extends BaseAdapter {
		private List<QueueInfo> queueList;

		public QueueListAdapter(List<QueueInfo> qInfoLst) {
			queueList = qInfoLst;
		}

		@Override
		public int getCount() {
			return queueList.size();
		}

		@Override
		public QueueInfo getItem(int position) {
			return queueList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater
						.from(TakeOrderActivity.this);
				convertView = inflater.inflate(
						R.layout.queue_listview_template, null);

				holder = new ViewHolder();
				holder.tvQueueGroup = (TextView) convertView
						.findViewById(R.id.textViewQueue);
				holder.tvQueueName = (TextView) convertView
						.findViewById(R.id.textViewCustomerName);
				holder.tvCustQty = (TextView) convertView
						.findViewById(R.id.textViewCustQty);
				holder.tvWait = (TextView) convertView
						.findViewById(R.id.textViewCustWait);
				holder.btnCall = (Button) convertView
						.findViewById(R.id.buttonCall);
				holder.btnCancel = (Button) convertView
						.findViewById(R.id.buttonConfirmCancel);
				holder.btnPreOrder = (Button) convertView
						.findViewById(R.id.buttonPreOrder);
				holder.imgHasOrder = (ImageView) convertView
						.findViewById(R.id.imageViewQueueHasPre);

				holder.btnCall.setVisibility(View.GONE);
				holder.btnPreOrder.setVisibility(View.GONE);
				holder.btnCancel.setVisibility(View.GONE);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final QueueInfo queueInfo = queueList.get(position);

			if (queueInfo != null) {
				holder.tvQueueGroup.setText(queueInfo.getSzQueueName());
				holder.tvQueueName.setText(queueInfo.getSzCustomerName());
				holder.tvQueueName.append("(x"
						+ mGlobalVar.qtyFormat.format(queueInfo
								.getiCustomerQty()) + ")");
				holder.tvWait.setText(mGlobalVar.qtyFormat.format(queueInfo
						.getiWaitQueueMinTime()) + "min.");

				if (queueInfo.getiHasPreOrderList() == 1) {
					// convertView.setBackgroundResource(R.drawable.list_light_green_bg);
					holder.imgHasOrder.setVisibility(View.VISIBLE);
				}
			}
			return convertView;
		}

		private class ViewHolder {
			private TextView tvQueueGroup;
			private TextView tvQueueName;
			private TextView tvCustQty;
			private TextView tvWait;
			private Button btnCall;
			private Button btnCancel;
			private Button btnPreOrder;
			private ImageView imgHasOrder;
		}
	}

	// set queue
	private class SetQueueTask extends LoadQueueTask {

		public SetQueueTask(Context c, GlobalVar gb) {
			super(c, gb);
			if(mCurrQueueId != 0)
				btnClearQueue.setVisibility(View.VISIBLE);
			
			btnClearQueue.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					final CustomDialog cfDialog = 
							new CustomDialog(TakeOrderActivity.this, R.style.CustomDialog);
					cfDialog.title.setVisibility(View.VISIBLE);
					cfDialog.title.setText(R.string.clear_queue_title);
					cfDialog.message.setText(R.string.cf_clear_queue);
					cfDialog.btnCancel.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							cfDialog.dismiss();
						}
						
					});
					cfDialog.btnOk.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							clearSetQueue();	
							cfDialog.dismiss();
							dialog.dismiss();
						}
						
					});
					cfDialog.show();
				}

			});
			
			btnCancelQueue.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}

			});
			btnOkQueue.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new LoadPreOrderTask(TakeOrderActivity.this, globalVar,
							queueId, queueInfo.getSzQueueName(), queueInfo
									.getiCustomerQty())
							.execute(GlobalVar.FULL_URL);

					dialog.dismiss();
				}

			});
		}
	}

	// load preorder task
	private class LoadPreOrderTask extends WebServiceTask {
		private int queueId;
		private String queueName;
		private int queueQty;

		public LoadPreOrderTask(Context c, GlobalVar gb, int queueId,
				String queueName, int queueQty) {
			super(c, gb, "WSiQueue_JSON_GetPreOrderListOfQueueID");

			if (queueId != 0) {
				this.queueId = queueId;
				this.queueName = queueName;
				this.queueQty = queueQty;
			} else {
				this.queueId = mCurrQueueId;
				this.queueName = mCurrQueueName;
				this.queueQty = mCustomerQty;
			}

			PropertyInfo property = new PropertyInfo();
			property.setName("iQueueID");
			property.setValue(this.queueId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(GlobalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}

		private void addPreOrder(
				List<POSData_OrderTransInfo.POSData_OrderItemInfo> orderItemLst) {
			// add pre order to db
			POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
			if (orderItemLst != null && orderItemLst.size() > 0) {
				posOrder.addOrderDetail(GlobalVar.TRANSACTION_ID,
						GlobalVar.COMPUTER_ID, GlobalVar.SHOP_ID, orderItemLst);

				listAllOrder();
			}

			mCurrQueueId = queueId;
			mCurrQueueName = queueName;
			mCustomerQty = queueQty;

			setSelectedQueue();
		}

		@Override
		protected void onPostExecute(String result) {
			 if(progress.isShowing())
				 progress.dismiss();

			GsonDeserialze gdz = new GsonDeserialze();
			try {
				WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
				if (wsResult.getiResultID() == 0) {
					try {
						final List<POSData_OrderTransInfo.POSData_OrderItemInfo> orderItemLst = gdz
								.deserializeOrderTransInfoJSON(wsResult
										.getSzResultData());
						// check curr order
						if (mOrderLst.size() > 0) {
							if (orderItemLst != null && orderItemLst.size() > 0) {
								final CustomDialog cfDialog = new CustomDialog(
										TakeOrderActivity.this,
										R.style.CustomDialog);
								cfDialog.setCanceledOnTouchOutside(false);
								cfDialog.title.setVisibility(View.VISIBLE);
								cfDialog.title
										.setText(R.string.global_dialog_title_warning);
								cfDialog.message
										.setText(R.string.text_warn_replace_preorder);
								cfDialog.btnCancel
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												cfDialog.dismiss();
											}

										});
								cfDialog.btnOk
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												addPreOrder(orderItemLst);
												cfDialog.dismiss();
											}

										});
								cfDialog.show();
							} else {
								addPreOrder(orderItemLst);
							}

						} else {
							addPreOrder(orderItemLst);
						}
					} catch (Exception e) {
						IOrderUtility.alertDialog(TakeOrderActivity.this,
								R.string.global_dialog_title_error,
								e.getMessage(), 0);
					}
				} else {
					IOrderUtility.alertDialog(TakeOrderActivity.this,
							R.string.global_dialog_title_error, wsResult
									.getSzResultData().equals("") ? result
									: wsResult.getSzResultData(), 0);
				}
			} catch (Exception e) {
				IOrderUtility.alertDialog(TakeOrderActivity.this,
						R.string.global_dialog_title_error, result, 0);

				syn.pos.mobile.util.Log.appendLog(TakeOrderActivity.this,
						result);
			}
		}
	}

	// queue task
	private class LoadQueueTask extends WebServiceTask {
		private static final String webMethod = "WSiQueue_JSON_GetCurrentQueueInfo";
		protected int queueId;
		protected Dialog dialog;
		protected View queueView;
		protected ListView queueListView;
		protected TextView tvSelectedQueue;
		protected ProgressBar progressBarLoadQueue;
		protected Button btnCancelQueue;
		protected Button btnOkQueue;
		protected Button btnClearQueue;
		protected QueueInfo queueInfo;
		protected int selectedIdx = -1;

		public LoadQueueTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);
			LayoutInflater inflater = LayoutInflater
					.from(TakeOrderActivity.this);
			queueView = inflater.inflate(R.layout.queue_list_layout, null);

			queueListView = (ListView) queueView
					.findViewById(R.id.listViewQueueList);
			tvSelectedQueue = (TextView) queueView
					.findViewById(R.id.textViewSelectedQueue);
			progressBarLoadQueue = (ProgressBar) queueView
					.findViewById(R.id.progressBarListQueue);
			btnCancelQueue = (Button) queueView
					.findViewById(R.id.buttonQueueCancel);
			btnClearQueue = (Button) queueView.findViewById(R.id.buttonClearSelectedQueue);
			btnOkQueue = (Button) queueView.findViewById(R.id.buttonQueueOk);
			tvSelectedQueue.setText(mCurrQueueName);

			if (mCurrQueueId != 0)
				btnOkQueue.setEnabled(true);

			queueInfo = new QueueInfo();
			queueListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> adapter, View v,
						final int position, long id) {
					queueInfo = (QueueInfo) adapter.getItemAtPosition(position);
					queueId = queueInfo.getiQueueID();

					if (mCurrQueueId != 0 && mCurrQueueId != queueId) {
						final CustomDialog cusDialog = new CustomDialog(
								TakeOrderActivity.this, R.style.CustomDialog);
						cusDialog.setCanceledOnTouchOutside(false);
						cusDialog.title.setVisibility(View.VISIBLE);
						cusDialog.title
								.setText(R.string.global_dialog_title_warning);
						TextView tvMsg = new TextView(TakeOrderActivity.this);
						tvMsg.setText(R.string.msg_already_set_queue);
						TextView tvMsg2 = new TextView(TakeOrderActivity.this);
						tvMsg2.setText(mCurrQueueName);
						tvMsg2.setTextSize(42);

						TextView tvMsg3 = new TextView(TakeOrderActivity.this);
						tvMsg3.setText(R.string.cf_change_queue);
						cusDialog.message.setText(tvMsg.getText().toString()
								+ " " + tvMsg2.getText().toString() + " \n "
								+ tvMsg3.getText().toString());
						cusDialog.btnCancel
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										// clear to use CURR_QUEUE_ID
										queueId = 0;
										
										cusDialog.dismiss();
										queueListView.setItemChecked(
												selectedIdx, true);
									}

								});
						cusDialog.btnOk
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										cusDialog.dismiss();
										tvSelectedQueue.setText(queueInfo
												.getSzQueueName());
										queueListView.setItemChecked(position,
												true);
									}

								});
						cusDialog.show();
					} else {
						tvSelectedQueue.setText(queueInfo.getSzQueueName());
					}

					btnOkQueue.setEnabled(true);
				}

			});
			
			btnCancelQueue.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}

			});

			btnOkQueue.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (queueId == 0) {
						queueId = mCurrQueueId;
					}

					dialog.dismiss();
					new SendOrderFromQueueTask(TakeOrderActivity.this,
							globalVar, queueId).execute(GlobalVar.FULL_URL);
				}

			});
			dialog = new Dialog(TakeOrderActivity.this,
					R.style.CustomDialogBottomRadius);
			dialog.setContentView(queueView);
			dialog.getWindow().setLayout(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.MATCH_PARENT);
			dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
			dialog.show();

			PropertyInfo property = new PropertyInfo();
			property.setName("iQueueGroupID");
			property.setValue(-1);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iOrderBy");
			property.setValue(0);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			progressBarLoadQueue.setVisibility(View.VISIBLE);
			queueListView.setVisibility(View.INVISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			progressBarLoadQueue.setVisibility(View.INVISIBLE);

			queueListView.setVisibility(View.VISIBLE);

			GsonDeserialze gdz = new GsonDeserialze();
			try {
				List<QueueInfo> queueList = gdz
						.deserializeQueueInfoListJSON(result);
				QueueListAdapter queueAdapter = new QueueListAdapter(queueList);

				queueListView.setAdapter(queueAdapter);
				queueListView.setSelection(queueAdapter.getCount());

				selectedIdx = IOrderUtility.indexOfQueueList(queueList,
						mCurrQueueId);
				queueListView.setItemChecked(selectedIdx, true);
				queueListView.setSelection(selectedIdx);

			} catch (Exception e) {
				e.printStackTrace();
				syn.pos.mobile.util.Log.appendLog(context, result);

				IOrderUtility.alertDialog(context,
						R.string.global_dialog_title_error, result, 0);
			}
		}

	}

	private class LoadTableTaskQuestion extends WebServiceTask{
		private final static String webMethod = "WSmPOS_JSON_LoadAllTableData";
		protected View view;
		protected LinearLayout layoutTotalCust;
		protected ListView tbListView;
		protected TextView tvTitle, tvSelectTableCusNo, tvSelectTableName;
		protected Button btnCancel, btnConfirm, btnClearTable;
		protected Button btnSelectTableMinus;
		protected Button btnSelectTablePlus;
		protected Spinner spinnerTbZone;
		protected ProgressBar progressBar;
		protected Dialog dialogSelectTable;
		protected ProgressBar progressQty;
		
		protected List<ProductGroups.QuestionAnswerData> selectedAnswerLst;
		protected TableInfo.TableName tbName;
		protected int tableId, selectedIdx = -1;
		
		public LoadTableTaskQuestion(Context c, GlobalVar gb) {
			super(c, gb, webMethod);
			
			selectedAnswerLst = new ArrayList<ProductGroups.QuestionAnswerData>();
			
			LayoutInflater inflater = LayoutInflater
					.from(TakeOrderActivity.this);
			view = inflater.inflate(R.layout.select_table_layout_with_qs, null);
			
			layoutTotalCust = (LinearLayout) view.findViewById(R.id.LinearLayoutTotalCust);
			tvTitle = (TextView) view.findViewById(R.id.textViewTitle);
			btnCancel = (Button) view.findViewById(R.id.buttonConfirmCancel);
			btnConfirm = (Button) view.findViewById(R.id.buttonConfirm);
			btnClearTable = (Button) view.findViewById(R.id.buttonClearSelectedTable);

			tvSelectTableCusNo = (TextView) view.findViewById(R.id.select_table_txtcusno);
			tvSelectTableName = (TextView) view.findViewById(R.id.select_table_cusno_tvname);
			btnSelectTableMinus = (Button) view.findViewById(R.id.select_table_cusno_btnminus);
			btnSelectTablePlus = (Button) view.findViewById(R.id.select_table_cusno_btnplus);
			
			spinnerTbZone = (Spinner) view.findViewById(R.id.spinner_table_zone);
			tbListView = (ListView) view.findViewById(R.id.tableList);

			progressBar = (ProgressBar) view.findViewById(R.id.loadTableProgress);	
			progressQty = (ProgressBar) view.findViewById(R.id.progressBar1);
			
			// hide layoutTotalCust by question config
			if(GlobalVar.isEnableTableQuestion){
				layoutTotalCust.setVisibility(View.GONE);
			}else{
				layoutTotalCust.setVisibility(View.VISIBLE);
			}
			
			dialogSelectTable = new Dialog(TakeOrderActivity.this,
					R.style.CustomDialogBottomRadius);
			tvTitle.setText(R.string.dialog_title_sendorder);
			dialogSelectTable.setContentView(view);
			dialogSelectTable.getWindow().setLayout(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.MATCH_PARENT);
			dialogSelectTable.getWindow().setWindowAnimations(R.style.DialogAnimation);

			tbName = new TableInfo.TableName();
			if (mCurrTableId != 0) {
				tvSelectTableName.setText(mCurrTableName);
				tvSelectTableCusNo.setText(globalVar.qtyFormat
						.format(mCustomerQty));
				
				if(!GlobalVar.isEnableTableQuestion)
					btnConfirm.setEnabled(true);
			}

			btnConfirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (tableId != 0) {
						mCurrTableId = tableId;
						mCurrTableName = tbName.getTableName();
					}

					if (mCurrTableId != 0) {
						if (GlobalVar.MEMBER_ID == 0) {
							new SubmitSendOrder(TakeOrderActivity.this,
									globalVar, "WSiOrder_JSON_SendTableOrderTransactionData")
									.execute(GlobalVar.FULL_URL);
						} else {
							new SubmitSendOrder(TakeOrderActivity.this,
									globalVar,
									"WSiOrder_JSON_SendTableOrderTransactionDataWithMemberID")
									.execute(GlobalVar.FULL_URL);
						}
					} else {
						final CustomDialog customDialog = new CustomDialog(
								context, R.style.CustomDialog);
						customDialog.title.setVisibility(View.VISIBLE);
						customDialog.title.setText(R.string.select_table_title);
						customDialog.message
								.setText(R.string.select_table_warning);
						customDialog.btnCancel.setVisibility(View.GONE);
						customDialog.btnOk
								.setText(R.string.global_close_dialog_btn);
						customDialog.btnOk
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										customDialog.dismiss();
									}
								});
						customDialog.show();
					}
					dialogSelectTable.dismiss();
				}
			});

			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialogSelectTable.dismiss();
				}
			});
		}
		
		@Override
		protected void onPostExecute(String result) {
			GsonDeserialze gdz = new GsonDeserialze();
			try {
				final TableInfo tbInfo = gdz.deserializeTableInfoJSON(result);

				spinnerTbZone.setAdapter(IOrderUtility.createTableZoneAdapter(
						TakeOrderActivity.this, tbInfo));
				spinnerTbZone
						.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent,
									View v, int pos, long id) {
								TableZone tbZone = (TableZone) parent
										.getItemAtPosition(pos);

								List<TableName> tbNameList = IOrderUtility
										.filterTableName(tbInfo, tbZone);

								SelectTableListAdapter adapter = new SelectTableListAdapter(
										TakeOrderActivity.this, globalVar,
										tbNameList);

								tbListView.setAdapter(adapter);
								selectedIdx = IOrderUtility.indexOfTbList(
										tbNameList, mCurrTableId);
								tbListView.setItemChecked(selectedIdx, true);
								tbListView.setSelection(selectedIdx);							
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {
								// TODO Auto-generated method stub

							}
						});

				tbListView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View v,
							final int position, long id) {

						tbName = (TableName) parent.getItemAtPosition(position);

						if(tbName.getTableStatus() != 3){
							
							if(tbName.getSTATUS() == 0){
								btnSelectTableMinus.setEnabled(true);
								btnSelectTablePlus.setEnabled(true);
							}else{
								btnSelectTableMinus.setEnabled(false);
								btnSelectTablePlus.setEnabled(false);
							}
							
							btnSelectTableMinus
									.setOnClickListener(new OnClickListener() {
	
										@Override
										public void onClick(View v) {
											int capacity = mCustomerQty;
											--capacity;
											if (capacity > 0) {
												tvSelectTableCusNo
														.setText(globalVar.qtyFormat
																.format(capacity));
												mCustomerQty = capacity;
											}
										}
	
									});
	
							btnSelectTablePlus
									.setOnClickListener(new OnClickListener() {
	
										@Override
										public void onClick(View v) {
											int capacity = mCustomerQty;
											++capacity;
											tvSelectTableCusNo
													.setText(globalVar.qtyFormat
															.format(capacity));
											mCustomerQty = capacity;
										}
	
									});
							
							if (mCurrTableId != 0
									&& mCurrTableId != tbName.getTableID()) {
								final CustomDialog cusDialog = new CustomDialog(
										TakeOrderActivity.this,
										R.style.CustomDialog);
								cusDialog.setCanceledOnTouchOutside(false);
								cusDialog.title.setVisibility(View.VISIBLE);
								cusDialog.title
										.setText(R.string.global_dialog_title_warning);
								TextView tvMsg1 = new TextView(
										TakeOrderActivity.this);
								tvMsg1.setText(R.string.msg_already_set_table);
								TextView tvMsg2 = new TextView(
										TakeOrderActivity.this);
								tvMsg2.setText(mCurrTableName);
								TextView tvMsg3 = new TextView(
										TakeOrderActivity.this);
								tvMsg3.setText(R.string.cf_change_table);
								cusDialog.message.setText(tvMsg1.getText()
										.toString()
										+ " "
										+ tvMsg2.getText().toString()
										+ " \n "
										+ tvMsg3.getText().toString());
								cusDialog.btnCancel
										.setOnClickListener(new OnClickListener() {
	
											@Override
											public void onClick(View v) {
												tbListView.setItemChecked(
														selectedIdx, true);
												cusDialog.dismiss();
											}
	
										});
								cusDialog.btnOk
										.setOnClickListener(new OnClickListener() {
	
											@Override
											public void onClick(View v) {
												tbListView.setItemChecked(position,
														true);
												cusDialog.dismiss();
												tableId = tbName.getTableID();
												tvSelectTableName.setText(tbName
														.getTableName());
												tvSelectTableCusNo
														.setText(globalVar.qtyFormat.format(tbName
																.getCapacity()));
												mCustomerQty = tbName.getCapacity();
	
												// popup question
												if(GlobalVar.isEnableTableQuestion)
													popupQuestion();
											}
	
										});
								cusDialog.show();
							} else {
								tableId = tbName.getTableID();
								tvSelectTableName.setText(tbName.getTableName());
								tvSelectTableCusNo.setText(globalVar.qtyFormat
										.format(tbName.getCapacity()));
								mCustomerQty = tbName.getCapacity();
	
								// popup question
								if(GlobalVar.isEnableTableQuestion)
									popupQuestion();
							}
							if(!GlobalVar.isEnableTableQuestion)
								btnConfirm.setEnabled(true);
						}else{
							btnSelectTableMinus.setEnabled(false);
							btnSelectTablePlus.setEnabled(false);
							btnConfirm.setEnabled(false);
						}
					}

				});
			} catch (Exception e) {
				final CustomDialog customDialog = new CustomDialog(context,
						R.style.CustomDialog);
				customDialog.title.setVisibility(View.VISIBLE);
				customDialog.title.setText(R.string.global_dialog_title_error);
				customDialog.message.setText(result);
				customDialog.btnCancel.setVisibility(View.GONE);
				customDialog.btnOk.setText(R.string.global_close_dialog_btn);
				customDialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						customDialog.dismiss();
					}
				});
				customDialog.show();
			}

			progressBar.setVisibility(View.GONE);
		}

		protected void popupQuestion(){
			btnConfirm.setEnabled(false);
			if(tbName.getSTATUS() == 0){
				// add answer to temp 
				final QuestionGroups qsGroup = new QuestionGroups(TakeOrderActivity.this);
				qsGroup.insertCurrentAnswerQuestion(new ArrayList<ProductGroups.QuestionAnswerData>());
				
				// popup
				LayoutInflater inflater = LayoutInflater.from(TakeOrderActivity.this);
				View questView = inflater.inflate(R.layout.question_list_layout, null);
				TextView tvQestionTitle = (TextView) questView.findViewById(R.id.textView1);
				tvQestionTitle.setText("Table: " + tbName);
				Button btnOk = (Button) questView.findViewById(R.id.button1);
				Button btnCancel = (Button) questView.findViewById(R.id.button2);
				final TextView tvRequire = (TextView) questView.findViewById(R.id.textView2);
				final ListView lvQuestion = (ListView) questView.findViewById(R.id.listView1);
				lvQuestion.setEnabled(false);
				
				// question adapter
				final List<ProductGroups.QuestionDetail> qsDetailLst = qsGroup.listQuestionDetail();
				
				if(qsDetailLst != null && qsDetailLst.size() > 0){
					final SelectTableQuestionAdapter qsAdapter = 
							new SelectTableQuestionAdapter(context, globalVar, qsDetailLst, tvSelectTableCusNo);
					lvQuestion.setAdapter(qsAdapter);
					
					final Dialog dialog = new Dialog(TakeOrderActivity.this, R.style.CustomDialog);
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
								dialogSelectTable.dismiss();
								dialog.dismiss();
								btnConfirm.setEnabled(true);
								// submit send order 
								
								QuestionGroups qsGroup = new QuestionGroups(TakeOrderActivity.this);
								selectedAnswerLst = qsGroup.listAnswerQuestion();
								
								if (tableId != 0) {
									mCurrTableId = tableId;
									mCurrTableName = tbName.getTableName();
								}
								
								mCustomerQty = Integer.parseInt(tvSelectTableCusNo.getText().toString());
								if (GlobalVar.MEMBER_ID == 0) {
									new SubmitSendOrder(TakeOrderActivity.this,
											globalVar, "WSiOrder_JSON_SendTableOrderTransactionData", selectedAnswerLst).execute(GlobalVar.FULL_URL);
								} else {
									new SubmitSendOrder(TakeOrderActivity.this,
											globalVar,
											"WSiOrder_JSON_SendTableOrderTransactionDataWithMemberID", selectedAnswerLst)
											.execute(GlobalVar.FULL_URL);
								}
							}
						}
					});
					
					btnCancel.setOnClickListener(new OnClickListener(){
		
						@Override
						public void onClick(View v) {
							btnConfirm.setEnabled(false);
							dialog.dismiss();
						}
						
					});
				}
			}else{
				btnConfirm.setEnabled(true);
			}
		}
		
		@Override
		protected void onPreExecute() {
			dialogSelectTable.show();
			progressBar.setVisibility(View.VISIBLE);
		}
		
	}

	private void getQueueName() {
		POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
		OrderTransaction tran = posOrder.getQueuNameFromTransaction(
				GlobalVar.TRANSACTION_ID, GlobalVar.COMPUTER_ID);

		if (tran.getQueueID() != 0) {
			mCurrQueueName = tran.getQueueName();
			mCurrQueueId = tran.getQueueID();
			mCustomerQty = tran.getNoCustomer();

			setQueueNameText();
		}
	}

	private void getTableName() {
		POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
		OrderTransaction tran = posOrder.getTableNameFromTransaction(
				GlobalVar.TRANSACTION_ID, GlobalVar.COMPUTER_ID);

		if (tran.getTableID() != 0) {
			mCurrTableName = tran.getTableName();
			mCurrTableId = tran.getTableID();
			mCustomerQty = tran.getNoCustomer();

			setTableNameText();
		}
	}

	private void setQueueNameText() {
		mNotificationLayout.setVisibility(View.VISIBLE);
		
		mBtnSendByQueue.setVisibility(View.VISIBLE);
		mBtnSendOrder.setVisibility(View.GONE);
		mBtnSetTable.setVisibility(View.GONE);
		ImageView imgIcTable = (ImageView) findViewById(R.id.imageViewIcTable);
		ImageView imgIcQueue = (ImageView) findViewById(R.id.imageViewIcQueue);
		imgIcTable.setVisibility(View.GONE);
		imgIcQueue.setVisibility(View.VISIBLE);

		mBtnSetQueue.setSelected(true);

		mTvNotification.setText(R.string.button_set_queue);
		mTvNotification.append(mCurrQueueName);
		mTvNotification.append("(x" + mGlobalVar.qtyFormat.format(mCustomerQty)
				+ ")");
	}

	private void setTableNameText() {
		mNotificationLayout.setVisibility(View.VISIBLE);
		
		mBtnSendByQueue.setVisibility(View.GONE);
		mBtnSendOrder.setVisibility(View.VISIBLE);
		mBtnSetTable.setVisibility(View.VISIBLE);
		mBtnSetQueue.setVisibility(View.GONE);
		ImageView imgIcTable = (ImageView) findViewById(R.id.imageViewIcTable);
		ImageView imgIcQueue = (ImageView) findViewById(R.id.imageViewIcQueue);
		imgIcTable.setVisibility(View.VISIBLE);
		imgIcQueue.setVisibility(View.GONE);

		mBtnSetTable.setSelected(true);
		
		mTvNotification.setText(R.string.button_set_table);
		mTvNotification.append(mCurrTableName);
		mTvNotification.append("(x" + mGlobalVar.qtyFormat.format(mCustomerQty) + ")");
	}

	private void setSelectedQueue() {
		// set tableId to transaction
		POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
		posOrder.setQueueIdToTransaction(GlobalVar.TRANSACTION_ID,
				GlobalVar.COMPUTER_ID, mCurrQueueId, mCurrQueueName,
				mCustomerQty);

		mCurrTableId = 0;
		mCurrTableName = "";

		setQueueNameText();
	}

	private void setSelectedTable() {
		// set tableId to transaction
		POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
		posOrder.setTableIdToTransaction(GlobalVar.TRANSACTION_ID,
				GlobalVar.COMPUTER_ID, mCurrTableId, mCurrTableName,
				mCustomerQty);

		mCurrQueueId = 0;
		mCurrQueueName = "";

		setTableNameText();
	}

	private void clearSelectedMember(){
		if(mCurrQueueName.equals("") && mCurrTableName.equals(""))
			mNotificationLayout.setVisibility(View.GONE);

		mGlobalVar.MEMBER_ID = 0;
		mGlobalVar.MEMBER_NAME = "";
		mTvNotification2.setText("");
		mBtnSetmember.setSelected(false);
		ImageView imgIcMember = (ImageView) findViewById(R.id.imageViewIcMember);
		imgIcMember.setVisibility(View.GONE);
	}
	
	private void setSelectedMember(){
		mTvNotification2.setText(mGlobalVar.MEMBER_NAME);

		ImageView imgIcMember = (ImageView) findViewById(R.id.imageViewIcMember);
		imgIcMember.setVisibility(View.VISIBLE);
		mNotificationLayout.setVisibility(View.VISIBLE);
		mBtnSetmember.setSelected(true);
	}

	private void clearSaleMode() {
		SaleMode saleMode = new SaleMode(TakeOrderActivity.this);
		ProductGroups.SaleMode s = saleMode.getSaleMode(1);
		setTransSaleMode(s);
		
		//listSaleMode();
	}

	private void clearTransaction() {
		GlobalVar.TRANSACTION_ID = 0;

		clearSaleMode();
		clearSelectedMember();
	}

	private void clearTable(){
		mCurrTableId = 0;
		mCustomerQty = 0;
	}
	
	private void clearQueue(){
		mCurrQueueId = 0;
	}
	
	private void clearSetQueue() {
		POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
		posOrder.setQueueIdToTransaction(GlobalVar.TRANSACTION_ID,
				GlobalVar.COMPUTER_ID, 0, "", 0);

		// clear order of queue
		// posOrder.deleteOrderDetail(GlobalVar.TRANSACTION_ID);
		// listAllOrder();

		mCurrQueueId = 0;
		mCustomerQty = 0;
		mCurrQueueName = "";

		mBtnSetQueue.setSelected(false);
		mBtnSetTable.setSelected(false);
		mBtnSendByQueue.setVisibility(View.VISIBLE);
		mBtnSendOrder.setVisibility(View.VISIBLE);
		
		// table type
		if (mGlobalVar.SHOP_DATA.getShopType() == 1) {
			mBtnSetQueue.setVisibility(View.VISIBLE);
			mBtnSetTable.setVisibility(View.VISIBLE);
		}

		// enable queue
		if (mIsEnableQueue) {
			mBtnSetQueue.setVisibility(View.VISIBLE);
			mBtnSendByQueue.setVisibility(View.VISIBLE);
		} else {
			mBtnSetQueue.setVisibility(View.GONE);
			mBtnSendByQueue.setVisibility(View.GONE);
		}

		ImageView imgIcQueue = (ImageView) findViewById(R.id.imageViewIcQueue);
		imgIcQueue.setVisibility(View.GONE);
		mTvNotification.setText("");
		mNotificationLayout.setVisibility(View.GONE);
	}

	private void clearSetTable() {
		POSOrdering posOrder = new POSOrdering(TakeOrderActivity.this);
		posOrder.setTableIdToTransaction(GlobalVar.TRANSACTION_ID,
				GlobalVar.COMPUTER_ID, 0, "", 0);

		mCurrTableId = 0;
		mCurrTableName = "";
		mCustomerQty = 0;

		mBtnSetQueue.setSelected(false);
		mBtnSetTable.setSelected(false);
		mBtnSendOrder.setVisibility(View.VISIBLE);
		mBtnSendByQueue.setVisibility(View.VISIBLE);

		// table type
		if (mGlobalVar.SHOP_DATA.getShopType() == 1) {
			mBtnSetQueue.setVisibility(View.VISIBLE);
			mBtnSetTable.setVisibility(View.VISIBLE);
		}

		// enable queue
		if (mIsEnableQueue) {
			mBtnSetQueue.setVisibility(View.VISIBLE);
			mBtnSendByQueue.setVisibility(View.VISIBLE);
		} else {
			mBtnSetQueue.setVisibility(View.GONE);
			mBtnSendByQueue.setVisibility(View.GONE);
		}
		
		ImageView imgIcTable = (ImageView) findViewById(R.id.imageViewIcTable);
		imgIcTable.setVisibility(View.GONE);
		mTvNotification.setText("");
		mNotificationLayout.setVisibility(View.GONE);
	}

	private void hideSaleModeText(){
		mSaleModeTextLayout.setVisibility(View.GONE);
	}
	
	private void showSaleModeText(String saleModeText){
		mSaleModeTextLayout.setVisibility(View.VISIBLE);
		TextView tvSaleMode = (TextView) findViewById(R.id.tvSaleModeText);
		tvSaleMode.setText(saleModeText);
	}
	
	private void createSwSaleMode(){
		SaleMode saleMode = new SaleMode(TakeOrderActivity.this);
		int[] saleModeId = {1,2}; 
		List<ProductGroups.SaleMode> saleModeLst = saleMode.listSaleMode(saleModeId);
		
		if(saleModeLst != null && saleModeLst.size() > 0){
			for(final ProductGroups.SaleMode s : saleModeLst){
				LayoutInflater inflater = LayoutInflater.from(TakeOrderActivity.this);
				final Button btnSwSaleMode = (Button)inflater.inflate(R.layout.button_sale_mode, null);
				btnSwSaleMode.setId(s.getSaleModeID());
				btnSwSaleMode.setText(s.getSaleModeName());
				// default Eat In
				if(s.getSaleModeID() == 1){
					btnSwSaleMode.setSelected(true);

					if(mIsEnableSalemode){
						showSaleModeText(s.getSaleModeName());
					}else{
						hideSaleModeText();
					}
				}
				
				btnSwSaleMode.setOnClickListener(new OnClickListener(){
		
					@Override
					public void onClick(View v) {
						if(mIsEnableSalemode){
							switchSaleMode(s.getSaleModeID());
						}else{
							setSaleMode(s);
							btnSwSaleMode.setSelected(true);
						}
						refreshPluResult();
					}
					
				});
//				if(s.getSaleModeID() == 1){
//					btnSwSaleMode.setText("DI");
//				}
//				else if(s.getSaleModeID() == 2){
//					btnSwSaleMode.setText("TW");
//				}
				mSaleModeSwLayout.addView(btnSwSaleMode);
			}
			
			if(saleModeLst.size() == 1){
				if(saleModeLst.get(0).getSaleModeID() == 1)
					mSaleModeSwLayout.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	private void clearSeat(){
		mSeatId = 0;
		mSeatName = "All";
		mBtnSeat.setText(mSeatName);
		mTvTotalSeat.setText("");
	}

	private void popupSeat(){
		LayoutInflater inflater = LayoutInflater.from(TakeOrderActivity.this);
		final View v = inflater.inflate(R.layout.seat_template, null);
		LinearLayout courseContent = (LinearLayout) v.findViewById(R.id.courseContent);
		final GridView gvSeat = (GridView) v.findViewById(R.id.gridView1);
		final Button btnClose = (Button) v.findViewById(R.id.btnClose);
		final Button btnCancel = (Button) v.findViewById(R.id.button1);
		final Button btnOk = (Button) v.findViewById(R.id.button2);
				
		if(mIsEnableCourse){
			courseContent.setVisibility(View.VISIBLE);
			HorizontalScrollView hView = (HorizontalScrollView) 
					courseContent.findViewById(R.id.horizontalScrollView1);
			new Course(this, mCourseId, hView, new Course.OnCourseClickedListener() {
				
				@Override
				public void onClick(int courseId, String courseName, String courseShortName) {
					mCurrCourseId = courseId;
					mCurrCourseName = courseShortName;
				}
			}).createCourseView();
			
		}else{
			courseContent.setVisibility(View.GONE);
		}
		
		final Dialog d = new Dialog(TakeOrderActivity.this, R.style.CustomDialog);
		d.setContentView(v);
		d.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, 
				WindowManager.LayoutParams.WRAP_CONTENT);
		d.show();
		
		List<ShopData.SeatNo> seatLst = new ArrayList<ShopData.SeatNo>();
		syn.pos.data.dao.ShopProperty shopProp = 
				new syn.pos.data.dao.ShopProperty(TakeOrderActivity.this, null);
		
		seatLst = shopProp.getSeatNo();
		
		ShopData.SeatNo seat = new ShopData.SeatNo();
		seat.setSeatID(0);
		seat.setSeatName("All");
		seatLst.add(0, seat);
		
		SeatAdapter seatAdapter = new SeatAdapter(seatLst, new OnSeatClickListener(){

			@Override
			public void onClick(int id, String name) {
				mCurrSeatId = id;
				mCurrSeatName = name;
			}

			
		});
		gvSeat.setAdapter(seatAdapter);
		
		btnOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String extra = "...";
				
				mSeatId = mCurrSeatId;
				mSeatName = mCurrSeatName;
				mCourseId = mCurrCourseId;
				mCourseName = mCurrCourseName;
				
				if(mSeatId != 0 && mCourseId != 0){
					extra = mCourseName + "-" + mSeatName;
				}else{
					if(mSeatId != 0)
						extra = mSeatName;
					if(mCourseId != 0)
						extra = mCourseName;
				}
				
				mBtnSeat.setText(extra);

//				if(mSeatId == 0)
//					mTvTotalSeat.setText("");
//				else
//					mTvTotalSeat.setText(mSeatName + ":");
				
				listAllOrder();
				d.dismiss();
			}
			
		});
		
		btnCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				d.dismiss();
			}
			
		});
		
		btnClose.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				d.dismiss();
			}
			
		});

	}
	
	private class SeatModAdapter extends SeatAdapter{
		public SeatModAdapter(List<SeatNo> seatLst, int currSeatId, OnSeatClickListener onClick) {
			super(seatLst, onClick);
			selectedSeatId = currSeatId;
		}
	}
	
	private class SeatAdapter extends BaseAdapter{

		private List<ShopData.SeatNo> seatLst;
		private LayoutInflater inflater;
		private OnSeatClickListener listener;
		protected int selectedSeatId;
		protected int lastBtnId = -1;
		
		public SeatAdapter(List<ShopData.SeatNo> seatLst, OnSeatClickListener onClick){
			this.seatLst = seatLst;
			inflater = LayoutInflater.from(TakeOrderActivity.this);
			this.listener = onClick;
			selectedSeatId = mSeatId;
		}
		
		@Override
		public int getCount() {
			return seatLst != null ? seatLst.size() : 0;
		}

		@Override
		public ShopData.SeatNo getItem(int arg0) {
			return seatLst.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {
			
			convertView = inflater.inflate(R.layout.seat_button, null);
			
			final Button btnSeat = (Button) convertView;
			
			btnSeat.setText(seatLst.get(position).getSeatName());
			btnSeat.setId(seatLst.get(position).getSeatID());
			btnSeat.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					if(getItemId(position) == lastBtnId){
						btnSeat.setActivated(true);
					}else{
						btnSeat.setActivated(true);
						try {
							((Button) parent.findViewById(lastBtnId)).setActivated(false);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					lastBtnId = seatLst.get(position).getSeatID();
						
					listener.onClick(seatLst.get(position).getSeatID(), seatLst.get(position).getSeatName());
				}});
			
			if(selectedSeatId == seatLst.get(position).getSeatID()){
				btnSeat.setActivated(true);
				lastBtnId = (int) getItemId(position);
			}
			else{
				btnSeat.setActivated(false);
			}
			
			return convertView;
		}
	}
		
	public interface OnSeatClickListener{
		public void onClick(int id, String name);
	}
	
	private void permissionChecking(){
		// some permission ??
	}
}
