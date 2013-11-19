package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;
import org.ksoap2.serialization.PropertyInfo;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.POSData_OrderTransInfo;
import syn.pos.data.model.QueueInfo;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableInfo.TableName;
import syn.pos.data.model.TableInfo.TableZone;
import syn.pos.data.model.WebServiceResult;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class QueueActivity extends Activity {
	private TextView tvCustomerQty;
	private TextView tvTotalQueue;
	private EditText editTextCustomerName;
	private EditText editTextCustomerMobile;
	private ProgressBar progressQueue;
	private Button btnQueueRefresh;
	private Button btnActivityClose;
	private Button btnQueueMinus;
	private Button btnQueuePlus;
	private Button btnGroupA;
	private Button btnGroupB;
	private Button btnGroupC;
	private Button btnSortByTime;
	private Button btnSortByGroup;
	private Button btnEditQueue;
	private ListView queueListView;
	private List<QueueInfo> queueList;
	private QueueListAdapter queueAdapter;

	private boolean isRun = true;
	private Handler handler;
	private final int updateInterval = 30000;
	private int queueGroupId;
	private int custQty = 1;
	private String custName;
	private String custMobile;

	private int loadQueueGroupBy = -1; // all
	private int loadQueueOrderBy = 1; // group
	private boolean isShowQueueCtrl = true;

	private GlobalVar globalVar;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.queue_layout);

		context = this;
		globalVar = new GlobalVar(context);

		// find view
		progressQueue = (ProgressBar) findViewById(R.id.progressBarQueue);
		tvCustomerQty = (TextView) findViewById(R.id.textViewCustomerQty);
		tvTotalQueue = (TextView) findViewById(R.id.textViewTotalQueue);
		editTextCustomerName = (EditText) findViewById(R.id.editTextCustomerName);
		editTextCustomerMobile = (EditText) findViewById(R.id.editTextCustomerMobile);
		btnQueueRefresh = (Button) findViewById(R.id.buttonQueueRefresh);
		btnQueueMinus = (Button) findViewById(R.id.buttonQueueMinus);
		btnQueuePlus = (Button) findViewById(R.id.buttonQueuePlus);
		btnGroupA = (Button) findViewById(R.id.buttonQueueGroup1);
		btnGroupB = (Button) findViewById(R.id.buttonQueueGroup2);
		btnGroupC = (Button) findViewById(R.id.buttonQueueGroup3);
		queueListView = (ListView) findViewById(R.id.listViewQueue);
		btnSortByTime = (Button) findViewById(R.id.buttonSortQueueByTime);
		btnSortByGroup = (Button) findViewById(R.id.buttonSortQueueByGroup);
		btnEditQueue = (Button) findViewById(R.id.buttonQueueEdit);

		editTextCustomerName.requestFocus();
		// refresh queue list interval
		handler = new Handler();
		handler.post(updateQueueList);

		btnEditQueue.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(isShowQueueCtrl){
					btnEditQueue.setSelected(false);
				}else{
					btnEditQueue.setSelected(true);
				}
				
				isShowQueueCtrl = !isShowQueueCtrl;
				
				queueAdapter.notifyDataSetInvalidated();
			}
			
		});
		
		btnSortByTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				v.setSelected(true);
				btnSortByGroup.setSelected(false);
				loadQueueOrderBy = 0;
				new LoadQueueTask(context, globalVar)
						.execute(GlobalVar.FULL_URL);
			}

		});
		btnSortByGroup.setSelected(true);

		btnSortByGroup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				v.setSelected(true);
				btnSortByTime.setSelected(false);
				loadQueueOrderBy = 1;
				new LoadQueueTask(context, globalVar)
						.execute(GlobalVar.FULL_URL);
			}

		});

		btnQueueRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new LoadQueueTask(context, globalVar)
						.execute(GlobalVar.FULL_URL);
			}

		});

		btnGroupA.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				queueGroupId = 1;
				excuteGenerateQueue();
			}

		});

		btnGroupB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				queueGroupId = 2;
				excuteGenerateQueue();
			}

		});

		btnGroupC.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				queueGroupId = 3;
				excuteGenerateQueue();
			}

		});

		btnQueueMinus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int qty = Integer.parseInt(tvCustomerQty.getText().toString());
				if (qty > 1) {
					--qty;
					tvCustomerQty.setText(globalVar.qtyFormat.format(qty));
					custQty = qty;
				}
			}

		});

		btnQueuePlus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int qty = Integer.parseInt(tvCustomerQty.getText().toString());
				++qty;
				tvCustomerQty.setText(globalVar.qtyFormat.format(qty));
				custQty = qty;
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_queue, menu);
		View v = menu.findItem(R.id.item_close).getActionView();

		btnActivityClose = (Button) v.findViewById(R.id.buttonClose);
		btnActivityClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(
						editTextCustomerName.getWindowToken(), 0);
				isRun = false;

				QueueActivity.this.finish();
			}

		});

		return true;
	}

	private Runnable updateQueueList = new Runnable() {

		@Override
		public void run() {
			if (isRun) {
				try {
					queueList = new ArrayList<QueueInfo>();
					queueAdapter = new QueueListAdapter();
					queueListView.setAdapter(queueAdapter);
					new LoadQueueTask(context, globalVar)
							.execute(GlobalVar.FULL_URL);

					handler.postDelayed(this, updateInterval);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	};

	private void excuteGenerateQueue() {
		if (!editTextCustomerName.getText().toString().equals("")) {
			custName = editTextCustomerName.getText().toString();
			new GenerateNewQueueTask(context, globalVar)
					.execute(GlobalVar.FULL_URL);
		} else {
			IOrderUtility.alertDialog(context,
					R.string.global_dialog_title_warning,
					R.string.msg_input_customer_name, 0);
		}
	}

	private class QueueListAdapter extends BaseAdapter {

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
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(
						R.layout.queue_listview_template, null);

				holder = new ViewHolder();
				holder.tvQueueGroup = (TextView) convertView
						.findViewById(R.id.textViewQueue);
				holder.tvQueueName = (TextView) convertView
						.findViewById(R.id.textViewCustomerName);
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
				holder.layoutCtrl = (LinearLayout) convertView.findViewById(R.id.layoutQueueCtrl);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final QueueInfo queueInfo = queueList.get(position);

			if(isShowQueueCtrl){
				holder.layoutCtrl.setVisibility(View.VISIBLE);
			}else{
				holder.layoutCtrl.setVisibility(View.GONE);
			}
			
			if (queueInfo != null) {
				holder.tvQueueGroup.setText(queueInfo.getSzQueueName());
				holder.tvQueueName.setText(queueInfo.getSzCustomerName());
				holder.tvQueueName.append("(x"
						+ globalVar.qtyFormat.format(queueInfo
								.getiCustomerQty()) + ")");
				holder.tvWait.setText(globalVar.qtyFormat.format(queueInfo
						.getiWaitQueueMinTime()) + "min.");

				if (queueInfo.getiHasPreOrderList() == 1) {
					// convertView.setBackgroundResource(R.drawable.list_light_green_bg);
					holder.imgHasOrder.setVisibility(View.VISIBLE);
				} else {
					holder.imgHasOrder.setVisibility(View.GONE);
				}

				holder.btnPreOrder.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						new LoadPreOrderTask(QueueActivity.this, globalVar,
								queueInfo.getiQueueID(), queueInfo
										.getSzQueueName(), queueInfo
										.getiCustomerQty())
								.execute(globalVar.FULL_URL);
					}

				});
				holder.btnCall.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						new CallQueueTask(context, globalVar, queueInfo
								.getiQueueID(), queueInfo.getiCustomerQty(),
								queueInfo.getSzQueueName(), queueInfo.getiHasPreOrderList())
								.execute(GlobalVar.FULL_URL);
					}

				});

				holder.btnCancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						final CustomDialog customDialog = new CustomDialog(
								context, R.style.CustomDialog);
						customDialog.title.setVisibility(View.VISIBLE);
						customDialog.title
								.setText(R.string.cancel_queue_dialog_title);
						customDialog.message
								.setText(R.string.cancel_queue_confirm);
						customDialog.btnCancel.setText(R.string.global_btn_no);
						customDialog.btnOk.setText(R.string.global_btn_yes);
						customDialog.btnOk
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										new CancelQueueTask(context, globalVar,
												queueInfo.getiQueueID())
												.execute(GlobalVar.FULL_URL);
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
			}
			return convertView;
		}

		private class ViewHolder {
			private TextView tvQueueGroup;
			private TextView tvQueueName;
			private TextView tvWait;
			private Button btnCall;
			private Button btnCancel;
			private Button btnPreOrder;
			private ImageView imgHasOrder;
			private LinearLayout layoutCtrl;
		}
	}

	// load preorder
	private class LoadPreOrderTask extends WebServiceTask {
		private int queueId;
		private String queueName;
		private int queueQty;

		public LoadPreOrderTask(Context c, GlobalVar gb, int queueId,
				String queueName, int queueQty) {
			super(c, gb, "WSiQueue_JSON_GetPreOrderListOfQueueID");

			this.queueId = queueId;
			this.queueName = queueName;
			this.queueQty = queueQty;

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

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.loading_progress);
			progress.setMessage(tvProgress.getText());
			progress.show();
		}

		@Override
		protected void onPostExecute(String result) {
			if (progress.isShowing())
				progress.dismiss();

			GsonDeserialze gdz = new GsonDeserialze();
			try {
				WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
				if (wsResult.getiResultID() == 0) {
					List<POSData_OrderTransInfo.POSData_OrderItemInfo> orderItemLst = gdz
							.deserializeOrderTransInfoJSON(wsResult
									.getSzResultData());

					if (orderItemLst != null && orderItemLst.size() > 0) {
						OrderPreAdapter adapter = new OrderPreAdapter(context,
								globalVar, orderItemLst);

						LayoutInflater inflater = LayoutInflater.from(context);
						View orderView = inflater.inflate(
								R.layout.order_list_layout, null);
						TextView tvOrderListTitle = (TextView) orderView
								.findViewById(R.id.textViewOrderListTitle);
						TextView tvOrderListMsg = (TextView) orderView
								.findViewById(R.id.textViewOrderListMsg);
						ListView lvOrderPre = (ListView) orderView
								.findViewById(R.id.listViewOrder);
						Button btnClose = (Button) orderView
								.findViewById(R.id.buttonOrderListClose);
						Button btnOk = (Button) orderView
								.findViewById(R.id.buttonOrderListOk);
						btnClose.setVisibility(View.VISIBLE);
						btnOk.setVisibility(View.VISIBLE);
						ImageButton btnCloseDialog = (ImageButton) orderView
								.findViewById(R.id.imageButtonCloseOrderDialog);

						tvOrderListTitle.setText(queueName);
						tvOrderListMsg.setVisibility(View.VISIBLE);
						tvOrderListMsg.setText(R.string.text_order_of_queue);

						lvOrderPre.setAdapter(adapter);

						final Dialog dialog = new Dialog(context,
								R.style.CustomDialog);
						dialog.setContentView(orderView);
						dialog.getWindow().setGravity(Gravity.TOP);
						dialog.getWindow().setLayout(
								WindowManager.LayoutParams.MATCH_PARENT,
								WindowManager.LayoutParams.WRAP_CONTENT);

						btnCloseDialog
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										dialog.dismiss();
									}

								});

						btnClose.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}

						});
						btnOk.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent intent = new Intent(QueueActivity.this,
										TakeOrderActivity.class);
								intent.putExtra("QUEUE_ID", queueId);
								intent.putExtra("QUEUE_NAME", queueName);
								intent.putExtra("CUSTOMER_QTY", queueQty);
								QueueActivity.this.startActivity(intent);
								QueueActivity.this.finish();
							}

						});

						dialog.show();
					} else {
						Intent intent = new Intent(QueueActivity.this,
								TakeOrderActivity.class);
						intent.putExtra("QUEUE_ID", queueId);
						intent.putExtra("QUEUE_NAME", queueName);
						intent.putExtra("CUSTOMER_QTY", queueQty);
						QueueActivity.this.startActivity(intent);
						QueueActivity.this.finish();
					}
				} else {
					IOrderUtility.alertDialog(context,
							R.string.global_dialog_title_error, wsResult
									.getSzResultData().equals("") ? result
									: wsResult.getSzResultData(), 0);
				}
			} catch (Exception e) {
				IOrderUtility.alertDialog(context,
						R.string.global_dialog_title_error, result, 0);
			}
		}
	}

	// generate new queue
	private class GenerateNewQueueTask extends WebServiceTask {
		private static final String webMethod = "WSiQueue_JSON_GenerateNewQueue";

		public GenerateNewQueueTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);

			PropertyInfo property = new PropertyInfo();
			property.setName("iQueueGroupID");
			property.setValue(queueGroupId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iCustQty");
			property.setValue(custQty);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("szCustName");
			property.setValue(custName);
			property.setType(String.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.generate_new_queue_progress);
			progress.setMessage(tvProgress.getText());
			progress.show();
		}

		@Override
		protected void onPostExecute(String result) {
			if (progress.isShowing())
				progress.dismiss();

			GsonDeserialze gdz = new GsonDeserialze();

			try {
				// QueueInfo queueInfo = gdz.deserializeQueueInfoJSON(result);
				// queueList.add(queueInfo);
				// queueAdapter.notifyDataSetChanged();
				// queueListView.setSelection(queueAdapter.getCount());

				clearTextBox();
				new LoadQueueTask(context, globalVar)
						.execute(GlobalVar.FULL_URL);

			} catch (Exception e) {
				e.printStackTrace();
				syn.pos.mobile.util.Log.appendLog(context, result);

				IOrderUtility.alertDialog(context,
						R.string.global_dialog_title_error, result, 0);
			}

		}
	}

	// call queue
	private class CallQueueTask extends WebServiceTask {
		private static final String webMethod = "WSiQueue_JSON_CallQueue";
		private int queueId;
		private String queueName;
		private int queueQty;
		private int isHavePreOrder;

		public CallQueueTask(Context c, GlobalVar gb, int queueId,
				int queueQty, String queueName, int isHavePreOrder) {
			super(c, gb, webMethod);

			PropertyInfo property = new PropertyInfo();
			property.setName("iQueueID");
			property.setValue(queueId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			this.queueId = queueId;
			this.queueName = queueName;
			this.queueQty = queueQty;
			this.isHavePreOrder = isHavePreOrder;
		}

		@Override
		protected void onPreExecute() {
			// tvProgress.setText(R.string.call_queue_progress);
			// progress.setMessage(tvProgress.getText());
			// progress.show();
			progress.dismiss();
		}

		private void popupDialogKeyPad(){
			final SendOrderKeypadDialog dialog = new SendOrderKeypadDialog(
					globalVar, QueueActivity.this,
					R.style.CustomDialog);
			
			dialog.ffSendCustLayout.setVisibility(View.GONE);
			dialog.tvDialogTitle.setText(R.string.checkin_queue_title);
			dialog.btnConfirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!dialog.txtFastRef.getText().toString()
							.equals("")) {
						
						new CheckInQueueFastFood(QueueActivity.this,
								globalVar, queueId, dialog.txtFastRef
										.getText().toString(), globalVar.MEMBER_ID)
								.execute(globalVar.FULL_URL);
						
						dialog.dismiss();

					} else {
						final CustomDialog customDialog = new CustomDialog(
								QueueActivity.this,
								R.style.CustomDialog);
						customDialog.title.setVisibility(View.VISIBLE);
						customDialog.title
								.setText(R.string.global_dialog_title_error);
						customDialog.message
								.setText(R.string.cf_fastfood_title);
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
				}

			});
			dialog.btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(
							dialog.txtFastRef.getWindowToken(), 0);

					dialog.dismiss();
				}

			});

			dialog.getWindow().setGravity(Gravity.TOP);
//			dialog.getWindow()
//					.setSoftInputMode(
//							WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			dialog.getWindow().setLayout(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

			dialog.show();
		}
		
		@Override
		protected void onPostExecute(String result) {
			// if (progress.isShowing())
			// progress.dismiss();

			// final CustomDialog customDialog = new CustomDialog(context,
			// R.style.CustomDialog);
			// customDialog.title.setVisibility(View.VISIBLE);
			// customDialog.title.setText(R.string.call_queue_dialog_title);
			// customDialog.message.setText(R.string.call_queue_success);
			// customDialog.btnCancel.setVisibility(View.GONE);
			// customDialog.btnOk.setText(R.string.global_close_dialog_btn);
			// customDialog.btnOk.setOnClickListener(new OnClickListener() {
			//
			// @Override
			// public void onClick(View v) {
			// new LoadQueueTask(context, globalVar)
			// .execute(GlobalVar.FULL_URL);
			// customDialog.dismiss();
			// }
			// });
			// customDialog.show();

			// table type
			if (globalVar.SHOP_DATA.getShopType() == 1) {
				new SelectTableTask(context, globalVar, queueId, queueQty,
						queueName).execute(GlobalVar.FULL_URL);
			} else if (globalVar.SHOP_DATA.getShopType() == 2) {
				if (globalVar.SHOP_DATA.getFastFoodType() == 1) { // manual queue
//					if(isHavePreOrder == 1){
//						final Dialog detailDialog = new Dialog(QueueActivity.this, R.style.CustomDialogBottomRadius);
//						LayoutInflater inflater = LayoutInflater.from(QueueActivity.this);
//						View orderView = inflater.inflate(R.layout.order_list_layout, null);
//						ListView lvOrder = (ListView) orderView.findViewById(R.id.listViewOrder);
//						TextView tvTitle = (TextView) orderView.findViewById(R.id.textViewOrderListTitle);
//						TextView tvSumText = (TextView) orderView.findViewById(R.id.textViewSumText);
//						TextView tvSumPrice = (TextView) orderView.findViewById(R.id.textViewSumPrice);
//						ImageButton btnClose = (ImageButton) orderView.findViewById(R.id.imageButtonCloseOrderDialog);
//						Button btnCheckInFromSummary = (Button) orderView.findViewById(R.id.buttonSendFromSummary);
//						btnCheckInFromSummary.setText(R.string.queue_btn_call);
//						btnCheckInFromSummary.setVisibility(View.VISIBLE);
//						
//						ProgressBar progress = (ProgressBar) orderView.findViewById(R.id.progressBarOrderOfTable);
//						
//						new CheckSummaryBillDummyTask(QueueActivity.this, globalVar, 
//								lvOrder, tvSumText, tvSumPrice, progress).execute(globalVar.FULL_URL);
//						tvTitle.setText(R.string.button_check_price);
//						detailDialog.setContentView(orderView);
//						detailDialog.getWindow().setGravity(Gravity.TOP);
//						detailDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
//								WindowManager.LayoutParams.WRAP_CONTENT);
//						detailDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
//
//						btnClose.setOnClickListener(new OnClickListener(){
//
//							@Override
//							public void onClick(View v) {
//								detailDialog.dismiss();
//							}
//							
//						});
//						
//						btnCheckInFromSummary.setOnClickListener(new OnClickListener(){
//
//							@Override
//							public void onClick(View v) {
//								popupDialogKeyPad();
//								detailDialog.dismiss();
//							}
//							
//						});
//						
//						detailDialog.show();
//					}else{
						popupDialogKeyPad();
//					}
				}
			} else {
				QueueActivity.this.finish();
			}
		}
	}

	// cancel queue
	private class CancelQueueTask extends WebServiceTask {
		private static final String webMethod = "WSiQueue_JSON_CancelQueue";

		public CancelQueueTask(Context c, GlobalVar gb, int queueId) {
			super(c, gb, webMethod);

			PropertyInfo property = new PropertyInfo();
			property.setName("iQueueID");
			property.setValue(queueId);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.cancel_queue_progress);
			progress.setMessage(tvProgress.getText());
			progress.show();
		}

		@Override
		protected void onPostExecute(String result) {
			if (progress.isShowing())
				progress.dismiss();

			final CustomDialog customDialog = new CustomDialog(context,
					R.style.CustomDialog);
			customDialog.title.setVisibility(View.VISIBLE);
			customDialog.title.setText(R.string.cancel_queue_dialog_title);
			customDialog.message.setText(R.string.cancel_queue_success);
			customDialog.btnCancel.setVisibility(View.GONE);
			customDialog.btnOk.setText(R.string.global_close_dialog_btn);
			customDialog.btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new LoadQueueTask(context, globalVar)
							.execute(GlobalVar.FULL_URL);
					customDialog.dismiss();
				}
			});
			customDialog.show();
		}
	}

	private void clearTextBox() {
		custQty = 1;
		custName = "";
		custMobile = "";

		tvCustomerQty.setText("1");
		editTextCustomerName.setText("");
		editTextCustomerMobile.setText("");
	}

	// get queue
	private class LoadQueueTask extends WebServiceTask {
		private static final String webMethod = "WSiQueue_JSON_GetCurrentQueueInfo";

		public LoadQueueTask(Context c, GlobalVar gb) {
			super(c, gb, webMethod);

			PropertyInfo property = new PropertyInfo();
			property.setName("iQueueGroupID");
			property.setValue(loadQueueGroupBy);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iOrderBy");
			property.setValue(loadQueueOrderBy);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			if(progress.isShowing())
				progress.dismiss();
			// tvProgress.setText(R.string.queue_refresh_progress);
			// progress.setMessage(tvProgress.getText());
			// progress.show();
			// progressQueue.setVisibility(View.VISIBLE);
			// queueListView.setVisibility(View.INVISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			// if(progress.isShowing())
			// progress.dismiss();
			// progressQueue.setVisibility(View.INVISIBLE);

			// queueListView.setVisibility(View.VISIBLE);

			GsonDeserialze gdz = new GsonDeserialze();
			try {
				queueList = gdz.deserializeQueueInfoListJSON(result);

				try {
					queueAdapter.notifyDataSetChanged();
					// queueListView.setSelection(queueList.size());

					tvTotalQueue.setText(globalVar.qtyFormat.format(queueList
							.size()));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (Exception e) {
				e.printStackTrace();
				try {
					syn.pos.mobile.util.Log.appendLog(context, result);

					IOrderUtility.alertDialog(context,
							R.string.global_dialog_title_error, result, 0);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// setTitle(R.string.global_network_connection_problem);
			}
		}

	}

	private class CheckInQueueFastFood extends WebServiceTask {
		private static final String method = "WSiQueue_JSON_CheckInQueueFromFastFoodNo";

		public CheckInQueueFastFood(Context c, GlobalVar gb, int queueId,
				String ref, int memberId) {
			super(c, gb, method);

			PropertyInfo property = new PropertyInfo();
			property.setName("iQueueID");
			property.setValue(queueId);
			property.setType(int.class);

			soapRequest.addProperty(property);
			property = new PropertyInfo();
			property.setName("szFastFoodNo");
			property.setValue(ref);
			property.setType(String.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iMemberID");
			property.setValue(memberId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(gb.COMPUTER_DATA.getComputerID());
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.checkin_queue_progress);
			progress.setMessage(tvProgress.getText());
			progress.show();
		}

		@Override
		protected void onPostExecute(String result) {
			if (progress.isShowing())
				progress.dismiss();

			GsonDeserialze gdz = new GsonDeserialze();

			try {
				WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
				if (wsResult.getiResultID() == 0) {
					final CustomDialog dialog = new CustomDialog(
							QueueActivity.this, R.style.CustomDialog);
					dialog.title.setVisibility(View.VISIBLE);
					dialog.title.setText(R.string.checkin_queue_title);
					dialog.message.setText(R.string.checkin_success);
					dialog.btnCancel.setVisibility(View.GONE);
					dialog.btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							new LoadQueueTask(QueueActivity.this, globalVar)
									.execute(GlobalVar.FULL_URL);
							dialog.dismiss();
						}

					});
					dialog.show();

				} else {
					IOrderUtility.alertDialog(
							QueueActivity.this,
							R.string.global_dialog_title_error,
							wsResult.getSzResultData() != "" ? wsResult
									.getSzResultData() : result, 0);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private class CheckInFromQueue extends WebServiceTask {
		private static final String webMethod = "WSiQueue_JSON_CheckInTableFromQueueID";

		public CheckInFromQueue(Context c, GlobalVar gb, int queueId,
				int tableId) {
			super(c, gb, webMethod);

			PropertyInfo property = new PropertyInfo();
			property.setName("iQueueID");
			property.setValue(queueId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(tableId);
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(gb.COMPUTER_DATA.getComputerID());
			property.setType(int.class);
			soapRequest.addProperty(property);

			property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(GlobalVar.STAFF_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			tvProgress.setText(R.string.checkin_queue_progress);
			progress.setMessage(tvProgress.getText());
			progress.show();
		}

		@Override
		protected void onPostExecute(String result) {
			if (progress.isShowing())
				progress.dismiss();

//			IOrderUtility.alertDialog(QueueActivity.this,
//					R.string.unknown_error_title, result, 0);
			int resStat = -1;
			try {
				resStat = Integer.parseInt(result);
				if (resStat == 0) {
					// IOrderUtility.alertDialog(context,
					// R.string.checkin_queue_title,
					// R.string.checkin_success, R.drawable.green_button);
					//
					final CustomDialog dialog = new CustomDialog(
							QueueActivity.this, R.style.CustomDialog);
					dialog.title.setVisibility(View.VISIBLE);
					dialog.title.setText(R.string.checkin_queue_title);
					dialog.message.setText(R.string.checkin_success);
					dialog.btnCancel.setVisibility(View.GONE);
					dialog.btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							new LoadQueueTask(QueueActivity.this, globalVar)
									.execute(GlobalVar.FULL_URL);
							dialog.dismiss();
						}

					});
					dialog.show();

				} else {
					IOrderUtility.alertDialog(QueueActivity.this,
							R.string.unknown_error_title, result, 0);
				}
			} catch (NumberFormatException e) {

			}
		}

	}

	/*
	 * Load only empty table
	 */
	private class SelectTableTask extends WebServiceTask {
		protected int queueId;
		protected int tableId;
		protected int queueQty;
		protected String queueName;

		protected View view;
		protected LinearLayout tableListLayout;
		protected TextView tvTitle;
		protected Button btnCancel;
		protected Button btnConfirm;
		private Button btnCancelQueue;
		protected TextView tvSelectTableCusNo;
		protected TextView tvSelectTableName;
		protected Button btnSelectTableMinus;
		protected Button btnSelectTablePlus;

		Spinner spinnerTbZone;
		private ListView tbListView;

		protected ProgressBar progressBar;

		public SelectTableTask(Context c, GlobalVar gb, int qId, int qQty,
				String qName) {
			super(c, gb, "WSmPOS_JSON_LoadAllTableData");

			queueId = qId;
			queueQty = qQty;
			queueName = qName;

			LayoutInflater factory = LayoutInflater.from(context);
			view = factory.inflate(R.layout.select_table_layout_with_qs, null);

			tableListLayout = (LinearLayout) view
					.findViewById(R.id.layoutTableList);
			tvTitle = (TextView) view.findViewById(R.id.textViewTitle);
			btnCancel = (Button) view.findViewById(R.id.buttonConfirmCancel);
			btnCancel.setText(R.string.global_btn_close);
			btnConfirm = (Button) view.findViewById(R.id.buttonConfirm);
			btnCancelQueue = (Button) view
					.findViewById(R.id.buttonCancelQueueFromCheckin);
			btnCancelQueue.setVisibility(View.VISIBLE);

			tvSelectTableCusNo = (TextView) view
					.findViewById(R.id.select_table_txtcusno);
			tvSelectTableName = (TextView) view
					.findViewById(R.id.select_table_cusno_tvname);
			btnSelectTableMinus = (Button) view
					.findViewById(R.id.select_table_cusno_btnminus);
			btnSelectTablePlus = (Button) view
					.findViewById(R.id.select_table_cusno_btnplus);

			tvSelectTableCusNo.setText(gb.qtyFormat.format(queueQty));
			// btnSelectTableMinus.setVisibility(View.INVISIBLE);
			// btnSelectTablePlus.setVisibility(View.INVISIBLE);

			spinnerTbZone = (Spinner) view
					.findViewById(R.id.spinner_table_zone);
			tbListView = (ListView) view.findViewById(R.id.tableList);

			progressBar = (ProgressBar) view
					.findViewById(R.id.loadTableProgress);

		}

		@Override
		protected void onPostExecute(String result) {

			GsonDeserialze gdz = new GsonDeserialze();
			try {
				final TableInfo tbInfo = gdz.deserializeTableInfoJSON(result);

				spinnerTbZone.setAdapter(IOrderUtility.createTableZoneAdapter(
						context, tbInfo));
				spinnerTbZone
						.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent,
									View v, int pos, long id) {
								TableZone tbZone = (TableZone) parent
										.getItemAtPosition(pos);

								List<TableName> tbNameList = IOrderUtility
										.filterEmptyTableName(tbInfo, tbZone);

								SelectTableListAdapter adapter = new SelectTableListAdapter(
										context, globalVar, tbNameList);

								tbListView.setAdapter(adapter);
							}

							@Override
							public void onNothingSelected(AdapterView<?> arg0) {
								// TODO Auto-generated method stub

							}
						});

				tbListView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {
						// set list selected background

						final SelectTableListAdapter.SelectTableViewHolder holder = (SelectTableListAdapter.SelectTableViewHolder) v
								.getTag();

						 //enable ctrl
						 tvSelectTableCusNo.setEnabled(true);
						 btnSelectTableMinus.setEnabled(true);
						 btnSelectTablePlus.setEnabled(true);

						btnSelectTableMinus
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										int capacity = queueQty;
										--capacity;
										if (capacity > 0) {
											tvSelectTableCusNo
													.setText(globalVar.qtyFormat
															.format(capacity));
											queueQty = capacity;
										}
									}

								});

						btnSelectTablePlus
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										int capacity = queueQty;
										++capacity;
										tvSelectTableCusNo
												.setText(globalVar.qtyFormat
														.format(capacity));
										queueQty = capacity;
									}

								});

						TableName tbName = (TableName) parent
								.getItemAtPosition(position);
						tvSelectTableName.setText(holder.tableName);
						tableId = tbName.getTableID();
						btnConfirm.setEnabled(true);
					}

				});
			} catch (Exception e) {

				final CustomDialog customDialog = new CustomDialog(context,
						R.style.CustomDialog);
				customDialog.title.setVisibility(View.VISIBLE);
				customDialog.title
						.setText(R.string.global_dialog_title_error);
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
			tableListLayout.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPreExecute() {
			final Dialog dialog = new Dialog(context, R.style.CustomDialog);
			//tvTitle.setTextSize(48);
			tvTitle.setText("Queue : " + queueName);
			dialog.setContentView(view);
			dialog.show();

			btnConfirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (tableId != 0) {
						new CheckInFromQueue(context, globalVar, queueId,
								tableId).execute(GlobalVar.FULL_URL);
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
					dialog.dismiss();
				}
			});

			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			btnCancelQueue.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					final CustomDialog customDialog = new CustomDialog(context,
							R.style.CustomDialog);
					customDialog.title.setVisibility(View.VISIBLE);
					customDialog.title
							.setText(R.string.cancel_queue_dialog_title);
					customDialog.message.setText(R.string.cancel_queue_confirm);
					customDialog.btnCancel.setText(R.string.global_btn_no);
					customDialog.btnOk.setText(R.string.global_btn_yes);
					customDialog.btnOk
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									new CancelQueueTask(context, globalVar,
											queueId)
											.execute(GlobalVar.FULL_URL);
									customDialog.dismiss();
									dialog.dismiss();
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

			progressBar.setVisibility(View.VISIBLE);
		}

	}
}
