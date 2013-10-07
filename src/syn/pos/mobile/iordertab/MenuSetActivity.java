package syn.pos.mobile.iordertab;

import java.util.List;

import syn.pos.data.dao.MenuComment;
import syn.pos.data.dao.PComponentGroup;
import syn.pos.data.dao.PComponentSet;
import syn.pos.data.dao.POSOrdering;
import syn.pos.data.model.MenuDataItem;
import syn.pos.data.model.MenuGroups;
import syn.pos.data.model.ProductGroups;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup.LayoutParams;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

@SuppressLint("NewApi")
public class MenuSetActivity extends Activity {
	private GlobalVar globalVar;
	private ExpandableListView childOrderListView;
	private TextView tvMenuSetReqAmount;
	private Button btnConfirm;
	private Button btnCancel;

	private int TRANSACTION_ID;
	private int ORDER_ID;
	private int PRODUCT_ID;
	private Context CONTEXT;
	int lastId = -1;
	private Activity SET_ACTIVITY;
	private GridView gvPCompSet;
	private int EDIT_MODE = 0; // 0 default, 1 edit
	private int commentType = 0; // global

	private ChildOrderLinkType7ExpandListAdapter orderSetAdapter;
	private List<ProductGroups.PComponentGroup> orderSetLst;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		CONTEXT = this;
		SET_ACTIVITY = MenuSetActivity.this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_set);
		// getActionBar().setDisplayHomeAsUpEnabled(true);
		// setTitle("");

		gvPCompSet = (GridView) findViewById(R.id.gvSetMenuItem);

		globalVar = new GlobalVar(CONTEXT);

		// get operation mode
		Intent intent = getIntent();
		EDIT_MODE = intent.getIntExtra("EDIT_MODE", 0);
		// get require params
		TRANSACTION_ID = intent.getIntExtra("transactionId", 0);
		ORDER_ID = intent.getIntExtra("orderId", 0);
		PRODUCT_ID = intent.getIntExtra("productId", 0);
		commentType = intent.getIntExtra("commentType", 0);

		POSOrdering posOrder = new POSOrdering(CONTEXT);
		posOrder.createOrderSetTmp(TRANSACTION_ID, ORDER_ID);

		childOrderListView = (ExpandableListView) findViewById(R.id.orderSetListView);
		childOrderListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				v.setSelected(true);
				return false;
			}
		});
		childOrderListView.setGroupIndicator(null);

		PComponentGroup pcg = new PComponentGroup(this);
		List<ProductGroups.PComponentGroup> pcgLst = pcg
				.getPcomponentGroup(PRODUCT_ID);

		ViewGroup layout = (ViewGroup) findViewById(R.id.layout);
		LayoutInflater inflater = LayoutInflater.from(this);
		ImageLoader imgLoader = new ImageLoader(this);

		if (pcgLst.size() > 0) {
			int i = 0;
			for (final ProductGroups.PComponentGroup pcgData : pcgLst) {
				final View v = inflater.inflate(
						R.layout.pcomponentgroup_template, null);
				v.setId(pcgData.getPGroupID());

				ImageView imgMenuSet = (ImageView) v
						.findViewById(R.id.pCompGroupImg);
				TextView tvMenuSetName = (TextView) v
						.findViewById(R.id.tvMenuSetName);
				tvMenuSetReqAmount = (TextView) v
						.findViewById(R.id.tvMenuSetReqAmount);

				tvMenuSetName.setText(pcgData.getSetGroupName());

				// check balance
				if (pcgData.getRequireAmount() == 0) {
					tvMenuSetReqAmount.setText("...");
				} else {
					double balance = calculateSetRequireAmount(
							pcgData.getPGroupID(), pcgData.getRequireAmount());
				}

				if(GlobalVar.DISPLAY_MENU_IMG == 1){
					imgMenuSet.setVisibility(View.VISIBLE);
					// load image
					imgLoader.DisplayImage(
							GlobalVar.IMG_URL + pcgData.getMenuImageLink(),
							imgMenuSet);
				}else{
					imgMenuSet.setVisibility(View.GONE);
				}

				// if setgroup 0
				if (pcgData.getSetGroupNo() == 0) {
					v.setEnabled(false);
					// v.setBackgroundResource(R.drawable.btn_default_disabled_holo_light);

					if (posOrder.checkAddedSetGroup0(TRANSACTION_ID, ORDER_ID,
							pcgData.getPGroupID()) == 0) {
						PComponentSet pComponentSet = new PComponentSet(this);
						List<ProductGroups.PComponentSet> componentSetLst = pComponentSet
								.getPComponentSet(pcgData.getPGroupID());
						if (componentSetLst.size() > 0) {
							for (ProductGroups.PComponentSet pSet : componentSetLst) {
								double flexiblePrice = pSet
										.getFlexibleIncludePrice() == 1 ? pSet
										.getFlexibleProductPrice() : 0;

								posOrder.addOrderSet(TRANSACTION_ID, ORDER_ID,
										pSet.getPGroupID(),
										pcgData.getSetGroupNo(),
										pSet.getProductID(), flexiblePrice,
										pSet.getChildProductAmount(), 
										pSet.getChildProductAmount(), "");
							}

							listAllSetOrder();
						}
					}

				} else {
					// set click event
					v.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View view) {
							// view.setBackgroundResource(R.drawable.gradient_bg_hover);
							view.setSelected(true);
							if (lastId != -1 && lastId != view.getId()) {
								View lastView = findViewById(lastId);
								lastView.setSelected(false);
								// lastView.setBackgroundResource(R.drawable.btn_selector);
							}

							lastId = view.getId();

							// load menuset task
							new LoadSetMenuTask(pcgData.getPGroupID(), pcgData.getSetGroupNo(),
									pcgData.getSetGroupName(), pcgData.getRequireAmount()).execute("");
						}

					});

				}

				if (i == 0){
					v.callOnClick();
					v.setBackgroundResource(R.drawable.blue_button_left);
				}else if (i == pcgLst.size() - 1){
					v.setBackgroundResource(R.drawable.blue_button_right);
				}else{
					v.setBackgroundResource(R.drawable.blue_button_center);
				}
				LayoutParams layoutParam = new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,
						1f);
				//layoutParam.width = 250;
				v.setMinimumWidth(200);
				layout.addView(v, layoutParam);

				i++;
			}
		}

		listAllSetOrder();
	}

	private class LoadSetMenuTask extends AsyncTask<String, Boolean, List<ProductGroups.PComponentSet>>{
		private int pGroupId;
		private int groupNo;
		private String groupName;
		private double requireAmount;
		private ProgressBar progress;
		
		public LoadSetMenuTask(int pGroupId, int groupNo, String groupName, double requireAmount){
			this.pGroupId = pGroupId;
			this.groupNo = groupNo;
			this.groupName = groupName;
			this.requireAmount = requireAmount;
			progress = (ProgressBar) findViewById(R.id.progressBarLoadMenuSet);
		}
		
		@Override
		protected void onPostExecute(
				List<syn.pos.data.model.ProductGroups.PComponentSet> componentSetLst) {
			progress.setVisibility(View.GONE);
			gvPCompSet.setVisibility(View.VISIBLE);
			
			PComponentSetAdapter adapter = new PComponentSetAdapter(
					groupNo, groupName, requireAmount,componentSetLst);

			gvPCompSet.setAdapter(adapter);
		}

		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
			gvPCompSet.setVisibility(View.GONE);
		}

		@Override
		protected List<syn.pos.data.model.ProductGroups.PComponentSet> doInBackground(
				String... params) {
			PComponentSet pComponentSet = new PComponentSet(
					CONTEXT);

			List<ProductGroups.PComponentSet> componentSetLst = pComponentSet
					.getPComponentSet(pGroupId);

			return componentSetLst;
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_menu_set, menu);
		View v = menu.findItem(R.id.item_confirm).getActionView();

		btnConfirm = (Button) v.findViewById(R.id.buttonConfirmOk);
		btnCancel = (Button) v.findViewById(R.id.buttonConfirmCancel);
		btnConfirm.setText(R.string.menu_setmenu_confirm);
		btnCancel.setText(R.string.menu_setmenu_cancel);

		btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				done();
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LayoutInflater inflater = LayoutInflater.from(CONTEXT);
				v = inflater.inflate(R.layout.custom_dialog, null);
				TextView tvTitle = (TextView) v.findViewById(R.id.textViewTitle);
				TextView tvMessage = (TextView) v
						.findViewById(R.id.textViewMessage);
				Button btnOk = (Button) v.findViewById(R.id.buttonOk);
				Button btnCancel = (Button) v
						.findViewById(R.id.buttonConfirmCancel);

				btnOk.setText(R.string.btn_global_yes);
				btnCancel.setText(R.string.btn_global_no);

				tvTitle.setVisibility(View.VISIBLE);
				tvTitle.setText(R.string.dialog_cancel_title_setmenu);
				tvMessage.setText(R.string.dialog_cancel_msg_setmenu);

				final Dialog dialog = new Dialog(CONTEXT, R.style.CustomDialog);
				dialog.setContentView(v);
				dialog.show();

				btnCancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						POSOrdering posOrder = new POSOrdering(CONTEXT);

						if (EDIT_MODE == 1)
							posOrder.cancelOrderSet();
						else
							posOrder.deleteOrderDetail(TRANSACTION_ID, ORDER_ID);

						dialog.dismiss();

						// NavUtils.navigateUpFromSameTask(SET_ACTIVITY);
						gotoTakeOrder();
						// MenuSetActivity.this.finish();
					}
				});
			}
		});

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Handle the back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	private void gotoTakeOrder() {
		finish();
	}

	public void listAllSetOrder() {
		new UpdateOrderListTask().execute("");
		// childOrderListView.smoothScrollToPosition(orderSetAdapter.getGroupCount());
	}

	private void done() {
		boolean canDone = true;
		PComponentGroup pcg = new PComponentGroup(this);
		List<ProductGroups.PComponentGroup> pcgLst = pcg
				.getPcomponentGroup(PRODUCT_ID);
		String pGroupName = "";
		if (pcgLst.size() > 0) {
			POSOrdering posOrder = new POSOrdering(CONTEXT);
			for (ProductGroups.PComponentGroup pg : pcgLst) {
				if (pg.getRequireAmount() > 0) {
					pGroupName = pg.getSetGroupName();
					double count = posOrder.countOrderSet(TRANSACTION_ID,
							ORDER_ID, pg.getPGroupID());
					double balance = pg.getRequireAmount() - count;

					if (balance != 0) {
						canDone = false;
						break;
					}
				}
			}
		}
		if (canDone) {
			POSOrdering posOrder = new POSOrdering(CONTEXT);
			posOrder.confirmOrderSet(TRANSACTION_ID, ORDER_ID);

			// NavUtils.navigateUpFromSameTask(this);
			gotoTakeOrder();
			// MenuSetActivity.this.finish();
		} else {
			// new AlertDialog.Builder(this)
			// .setIcon(android.R.drawable.ic_dialog_alert)
			// .setTitle(R.string.menuset_dialog_done_title)
			// .setMessage("Please select " + pGroupName)
			// .setPositiveButton(R.string.menuset_dialog_done_close,
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(
			// DialogInterface dialog,
			// int which) {
			// dialog.dismiss();
			// }
			//
			// }).show();
			LayoutInflater inflater = LayoutInflater.from(CONTEXT);
			View v = inflater.inflate(R.layout.custom_dialog, null);

			TextView tvTitle = (TextView) v.findViewById(R.id.textViewTitle);
			TextView tvMessage = (TextView) v
					.findViewById(R.id.textViewMessage);
			Button btnOk = (Button) v.findViewById(R.id.buttonOk);
			Button btnCancel = (Button) v
					.findViewById(R.id.buttonConfirmCancel);

			tvTitle.setVisibility(View.VISIBLE);
			tvTitle.setText(R.string.global_dialog_title_warning);
			tvMessage.setText(R.string.menuset_dialog_done_msg);
			tvMessage.append(" " + pGroupName);
			btnOk.setText(R.string.menuset_dialog_done_close);
			btnCancel.setVisibility(View.GONE);

			final Dialog dialog = new Dialog(CONTEXT, R.style.CustomDialog);
			dialog.setContentView(v);
			dialog.show();

			btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
	}

	protected double calculateSetRequireAmount(int pgId, double requireAmount) {
		POSOrdering posOrder = new POSOrdering(CONTEXT);
		double count = posOrder.countOrderSet(TRANSACTION_ID, ORDER_ID, pgId);
		double balance = requireAmount - count;

		tvMenuSetReqAmount.setText(globalVar.qtyFormat.format(balance));
		return balance;
	}

	private class PComponentSetAdapter extends BaseAdapter {
		private List<ProductGroups.PComponentSet> pcsLst;
		private LayoutInflater inflater;
		private ImageLoader imgLoader;
		private int pGroupNo;
		private double requireAmount;
		private String groupName;

		public PComponentSetAdapter(int groupNo, String gpName,
				double reqAmount, List<ProductGroups.PComponentSet> pCompSetLst) {
			pcsLst = pCompSetLst;
			requireAmount = reqAmount;
			groupName = gpName;
			pGroupNo = groupNo;
			inflater = LayoutInflater.from(CONTEXT);
			imgLoader = new ImageLoader(CONTEXT);
		}

		public int getCount() {
			return pcsLst.size();
		}

		public ProductGroups.PComponentSet getItem(int position) {
			return pcsLst.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			MenuItemViewHolder holder;
			final ProductGroups.PComponentSet pcs = pcsLst.get(position);

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.menu_item_layout, null);

				holder = new MenuItemViewHolder();
				holder.menuImg = (ImageView) convertView
						.findViewById(R.id.menuitem_img);
				holder.tvMenuCode = (TextView) convertView
						.findViewById(R.id.tvMenuCode);
				holder.tvMenuName = (TextView) convertView
						.findViewById(R.id.menuitem_tvMenuName);
				convertView.setTag(holder);
			} else {
				holder = (MenuItemViewHolder) convertView.getTag();
			}
			
			holder.tvMenuCode.setVisibility(View.GONE);
			
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
			holder.tvMenuName.setText(menuName);

			if(GlobalVar.DISPLAY_MENU_IMG == 1){
				holder.menuImg.setVisibility(View.VISIBLE);
				imgLoader.DisplayImage(GlobalVar.IMG_URL + pcs.getMenuImageLink(),
						holder.menuImg);
			}else{
				holder.menuImg.setVisibility(View.GONE);
			}

			if(pcs.getIsOutOfStock() == 1){
				convertView.setEnabled(false);
				  TextView tvOutOfStock = (TextView) 
						  convertView.findViewById(R.id.textViewMenuItemOutOfStock);
				  tvOutOfStock.setVisibility(View.VISIBLE);
			}else{
				convertView.setEnabled(true);
				  TextView tvOutOfStock = (TextView) 
						  convertView.findViewById(R.id.textViewMenuItemOutOfStock);
				  tvOutOfStock.setVisibility(View.GONE);
				  
				  convertView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (requireAmount > 0) {
						double balance = recalculateSetRequireAmount(
								pcs.getPGroupID(), requireAmount);

						if (balance > 0 && balance >= pcs.getChildProductAmount()) {
							POSOrdering posOrder = new POSOrdering(CONTEXT);

							double flexiblePrice = pcs
									.getFlexibleIncludePrice() == 1 ? pcs
									.getFlexibleProductPrice() : 0;
							posOrder.addOrderSet(TRANSACTION_ID, ORDER_ID,
									pcs.getPGroupID(), pGroupNo,
									pcs.getProductID(), flexiblePrice, 1, 
									pcs.getChildProductAmount(), "");

							balance = recalculateSetRequireAmount(
									pcs.getPGroupID(), requireAmount);

							listAllSetOrder();
						}

					} else {
						POSOrdering posOrder = new POSOrdering(CONTEXT);

						double flexiblePrice = pcs.getFlexibleIncludePrice() == 1 ? pcs
								.getFlexibleProductPrice() : 0;
						posOrder.addOrderSet(TRANSACTION_ID, ORDER_ID,
								pcs.getPGroupID(), pGroupNo,
								pcs.getProductID(), flexiblePrice, 1, 
								pcs.getChildProductAmount(), "");

						listAllSetOrder();
					}
				}
			});
			}
			

			return convertView;
		}

	}

	protected double recalculateSetRequireAmount(int pgId, double reqAmount) {
		View v = SET_ACTIVITY.findViewById(pgId);
		TextView tvReqAmount = (TextView) v
				.findViewById(R.id.tvMenuSetReqAmount);

		POSOrdering posOrder = new POSOrdering(CONTEXT);
		double count = posOrder.countOrderSet(TRANSACTION_ID, ORDER_ID, pgId);
		double balance = 0.0d;// Double.parseDouble(tvReqAmount.getText().toString());

		balance = reqAmount - count;

		if (balance >= 0) {
			tvReqAmount.setText(globalVar.qtyFormat.format(balance));
		} else {
			balance = 0;
		}
		return balance;
	}

	private class ChildOrderLinkType7ExpandListAdapter extends
			BaseExpandableListAdapter {
		private List<ProductGroups.PComponentGroup> pCompGroupLst;
		private LayoutInflater inflater;
		private ImageLoader imgLoader;

		private List<MenuGroups.MenuComment> menuCommentList;
		private MenuCommentListAdapter menuCommentAdapter;
		
		public ChildOrderLinkType7ExpandListAdapter(Context c, int ordId,
				List<ProductGroups.PComponentGroup> pCompGroupList) {
			pCompGroupLst = pCompGroupList;
			inflater = LayoutInflater.from(CONTEXT);
			imgLoader = new ImageLoader(CONTEXT, ImageLoader.IMAGE_SIZE.SMALL);
		}

		@Override
		public ProductGroups.PComponentGroup getChild(int groupPosition,
				int childPosition) {
			return pCompGroupLst.get(groupPosition).pCompSetLst
					.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(final int groupPosition,
				final int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {

			final ProductGroups.PComponentGroup pcg = pCompGroupLst
					.get(groupPosition);

			final ProductGroups.PComponentSet pcs = pCompGroupLst
					.get(groupPosition).pCompSetLst.get(childPosition);

			final ChildOrderLinkType7ViewHolder holder;

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
				holder.layoutComment = (LinearLayout) convertView
						.findViewById(R.id.LinearLayout3);

				holder.btnMinus = (Button) convertView
						.findViewById(R.id.btnChildOrderLinkType7Minus);
				holder.btnPlus = (Button) convertView
						.findViewById(R.id.btnChildOrderLinkType7Plus);
				holder.btnDelete = (Button) convertView
						.findViewById(R.id.btnChildOrderLinkType7Delete);
				holder.btnComment = (Button) convertView
						.findViewById(R.id.btnChildOrderLinkType7Comment);

				convertView.setTag(holder);
			} else {
				holder = (ChildOrderLinkType7ViewHolder) convertView.getTag();
			}

			if(GlobalVar.DISPLAY_MENU_IMG == 1){
				holder.imgMenuSet.setVisibility(View.VISIBLE);
				imgLoader.DisplayImage(GlobalVar.IMG_URL + pcs.getMenuImageLink(),
						holder.imgMenuSet);
			}else{
				holder.imgMenuSet.setVisibility(View.GONE);
			}

			holder.tvOrderNo.setText(globalVar.qtyFormat
					.format(childPosition + 1) + ".");
			
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
			
			holder.tvMenuName.setText(menuName);
			holder.tvMenuComment.setText("");
		
			holder.tvMenuPrice.setText(pcs.getPricePerUnit() != 0 ? 
					globalVar.decimalFormat.format(pcs.getPricePerUnit()) : "");

			holder.tvMenuQty.setText(globalVar.qtyFormat.format(pcs
					.getProductQty()));

			// order comment
			String menuComment = "";
			if (pcs.menuCommentList != null && pcs.menuCommentList.size() > 0) {
				holder.layoutComment.setVisibility(View.VISIBLE);
				for (MenuGroups.MenuComment mc : pcs.menuCommentList) {
					menuComment += mc.getMenuCommentName_0() + " ";

					if (mc.getProductPricePerUnit() > 0) {
						menuComment += " "
								+ globalVar.qtyFormat
										.format(mc.getCommentQty());
						menuComment += " x "
								+ globalVar.decimalFormat.format(mc
										.getProductPricePerUnit());
						menuComment += " = "
								+ globalVar.decimalFormat.format(mc
										.getCommentQty()
										* mc.getProductPricePerUnit()) + " ";
					} else {
						menuComment += ", ";
					}
				}
				holder.tvMenuComment.setText(menuComment);
			} else {
				holder.layoutComment.setVisibility(View.GONE);
			}

			if (!pcs.getOrderComment().equals("")) {
				menuComment += pcs.getOrderComment();

				holder.tvMenuComment.setText(menuComment);
				holder.layoutComment.setVisibility(View.VISIBLE);
			}

			holder.btnMinus.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					double qty = Double.parseDouble(holder.tvMenuQty.getText()
							.toString());
					PComponentSet pCompSet = new PComponentSet(MenuSetActivity.this);
					double childAmount = pCompSet.getChildProductAmount(pcs.getPGroupID(), pcs.getProductID());
					
					POSOrdering posOrder = new POSOrdering(CONTEXT);
					double weightAmount = posOrder.countOrderSet(TRANSACTION_ID, ORDER_ID, pcs.getPGroupID());
					
					--qty;
					if(childAmount > 1)
						weightAmount = weightAmount - childAmount;
					else
						weightAmount = qty;
					
					if (qty > 0) {
						posOrder.updateOrderSet(TRANSACTION_ID, ORDER_ID,
								pcs.getOrderSetID(), qty, weightAmount);

						if (pcg.getRequireAmount() > 0)
							recalculateSetRequireAmount(pcg.getPGroupID(),
									pcg.getRequireAmount());

						holder.tvMenuQty.setText(globalVar.qtyFormat
								.format(qty));
					}
				}

			});

			holder.btnPlus.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					double qty = Double.parseDouble(holder.tvMenuQty.getText()
							.toString());
					PComponentSet pCompSet = new PComponentSet(MenuSetActivity.this);
					double childAmount = pCompSet.getChildProductAmount(pcs.getPGroupID(), 
							pcs.getProductID());
					
					POSOrdering posOrder = new POSOrdering(CONTEXT);
					double weightAmount = posOrder.countOrderSet(TRANSACTION_ID, 
							ORDER_ID, pcs.getPGroupID());
					
					++qty;
					if(childAmount > 1)
						weightAmount = weightAmount + childAmount;
					else
						weightAmount = qty;

					if (pcg.getRequireAmount() > 0) {
						double balance = recalculateSetRequireAmount(
								pcg.getPGroupID(), pcg.getRequireAmount());
						if (balance > 0 && balance >= pcs.getChildProductAmount()) {
							posOrder.updateOrderSet(TRANSACTION_ID, ORDER_ID,
									pcs.getOrderSetID(), qty, weightAmount);

							holder.tvMenuQty.setText(globalVar.qtyFormat
									.format(qty));
							balance = recalculateSetRequireAmount(
									pcg.getPGroupID(), pcg.getRequireAmount());
						} 
					} else {
						posOrder.updateOrderSet(TRANSACTION_ID, ORDER_ID,
								pcs.getOrderSetID(), qty, qty);
						holder.tvMenuQty.setText(globalVar.qtyFormat
								.format(qty));
					}
				}

			});

			holder.btnDelete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					LayoutInflater inflater = LayoutInflater.from(CONTEXT);
					v = inflater.inflate(R.layout.custom_dialog, null);

					TextView tvTitle = (TextView) v
							.findViewById(R.id.textViewTitle);
					TextView tvMessage = (TextView) v
							.findViewById(R.id.textViewMessage);
					Button btnOk = (Button) v.findViewById(R.id.buttonOk);
					Button btnCancel = (Button) v
							.findViewById(R.id.buttonConfirmCancel);

					tvTitle.setVisibility(View.VISIBLE);
					tvTitle.setText(R.string.dialog_delete_item_title);
					tvMessage.setText(R.string.dialog_delete_item_message);
					btnOk.setText(R.string.dialog_delete_item_btn_ok);
					btnCancel.setText(R.string.dialog_delete_item_btn_cancel);

					final Dialog dialog = new Dialog(CONTEXT,
							R.style.CustomDialog);
					dialog.setContentView(v);
					dialog.show();

					btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							POSOrdering posOrder = new POSOrdering(CONTEXT);
							posOrder.deleteOrderSet(TRANSACTION_ID, ORDER_ID,
									pcs.getOrderSetID());

							if (pcg.getRequireAmount() != 0)
								recalculateSetRequireAmount(pcg.getPGroupID(),
										pcg.getRequireAmount());
							listAllSetOrder();

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

			});

			holder.btnComment.setOnClickListener(new View.OnClickListener() {
				
				private void loadCommentGroup(MenuComment mc, Spinner spMcg){
					//menu comment group 
					List<MenuGroups.MenuCommentGroup> mcgLst = 
							mc.listMenuCommentGroups();
					
					MenuCommentGroupAdapter mcgAdapter = 
							new MenuCommentGroupAdapter(MenuSetActivity.this, mcgLst);
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
								new MenuCommentSelectedAdapter(MenuSetActivity.this, globalVar, mcLst);
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
								MenuSetActivity.this);
						
						List<MenuGroups.MenuComment> mcLst
							= posOrdering.listOrderSetComment(TRANSACTION_ID, ORDER_ID, pcs.getOrderSetID());
						return mcLst;
					}
					
				}
				
				@Override
				public void onClick(View view) {
					final MenuComment mc = new MenuComment(CONTEXT);

					LayoutInflater factory = LayoutInflater.from(CONTEXT);
					View v = factory.inflate(R.layout.menu_comment_layout2, null);
					
					TextView tvTitle = (TextView) v.findViewById(R.id.textViewMenuCommentTitle);
					TableRow tbRowSaleMode = (TableRow) v.findViewById(R.id.tableRowSaleMode);
					tbRowSaleMode.setVisibility(View.GONE);
					final Spinner spMcg = (Spinner) v.findViewById(R.id.spinnerMcg);
					final ListView menuCommentListView = (ListView) v.findViewById(R.id.menuCommentListView);
					final ListView selectedMenuCommentListView = (ListView) v.findViewById(R.id.listViewCommentSelected);
					final EditText txtComment = (EditText) v.findViewById(R.id.txt_menu_comment);
					Button btnCancel = (Button) v.findViewById(R.id.buttonCancelComment);
					Button btnOk = (Button) v.findViewById(R.id.buttonOkComment);
					
					tvTitle.setText(R.string.title_menu_comment);
					txtComment.append(pcs.getOrderComment());

					loadCommentGroup(mc, spMcg);
					
					spMcg.setOnItemSelectedListener(new OnItemSelectedListener(){

						@Override
						public void onItemSelected(AdapterView<?> parent,
								View v, int position, long id) {
							MenuGroups.MenuCommentGroup mg = 
									(MenuGroups.MenuCommentGroup)parent.getItemAtPosition(position);
							
							if(commentType == 1 || commentType == 2){
								menuCommentList = mc.listFixMenuComment(pcs.getProductID());
								if(menuCommentList.size() == 0){
									menuCommentList = mc.listMenuComments(mg.getMenuCommentGroupID());
								}
								menuCommentAdapter = new OrderSetMenuCommentAdapter(CONTEXT, globalVar, 
										TRANSACTION_ID, menuCommentList, pcs, selectedMenuCommentListView);
								menuCommentListView.setAdapter(menuCommentAdapter);
							}else{
								menuCommentList = mc.listMenuComments(mg.getMenuCommentGroupID());
								menuCommentAdapter = new OrderSetMenuCommentAdapter(CONTEXT, globalVar, 
										TRANSACTION_ID, menuCommentList, pcs, selectedMenuCommentListView);
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
							
							final CustomDialog cusDialog = new CustomDialog(MenuSetActivity.this, R.style.CustomDialog);
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
									
									POSOrdering posOrder = new POSOrdering(MenuSetActivity.this);
									posOrder.deleteOrderSetComment(TRANSACTION_ID, ORDER_ID, 
											pcs.getOrderSetID(), mcData.getMenuCommentID());
									
									new UpdateSelectedCommentTask(selectedMenuCommentListView).execute();
									menuCommentAdapter.notifyDataSetInvalidated();
									cusDialog.dismiss();
								}
								
							});
							cusDialog.show();
						}
						
					});

					new UpdateSelectedCommentTask(selectedMenuCommentListView).execute();
					
					menuCommentListView
							.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent,
										View v, int position, long id) {

									MenuCommentListAdapter.MenuCommentViewHolder holder = 
											(MenuCommentListAdapter.MenuCommentViewHolder) v.getTag();

									syn.pos.data.model.MenuGroups.MenuComment mc = 
											(MenuGroups.MenuComment) parent.getItemAtPosition(position);

									POSOrdering posOrder = new POSOrdering(MenuSetActivity.this);
									
									if (!posOrder.chkOrderSetComment(GlobalVar.TRANSACTION_ID, 
											pcs.getOrderDetailId(), pcs.getOrderSetID(), mc.getMenuCommentID())) {
										holder.checkBox1.setChecked(true);
										holder.tvMenuCommentQty.setText("1");
										holder.btnMenuCommentMinus
												.setEnabled(true);
										holder.btnMenuCommentPlus
												.setEnabled(true);

										posOrder.addOrderSetComment(
												TRANSACTION_ID, ORDER_ID,
												pcs.getOrderSetID(),
												pcg.getPGroupID(),
												pcs.getProductID(),
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

										posOrder.deleteOrderSetComment(
												TRANSACTION_ID, ORDER_ID,
												pcs.getOrderSetID(),
												mc.getMenuCommentID());

									}
									new UpdateSelectedCommentTask(selectedMenuCommentListView).execute();
								}

							});

					final Dialog dialog = new Dialog(CONTEXT,
							R.style.CustomDialogBottomRadius);
					dialog.setContentView(v);
					dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
					dialog.getWindow().setGravity(Gravity.TOP);
					dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
							WindowManager.LayoutParams.WRAP_CONTENT);
					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
							WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
					dialog.show();

					btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							POSOrdering posOrdering = new POSOrdering(CONTEXT);
							posOrdering.updateOrderSet(TRANSACTION_ID,
									ORDER_ID, pcs.getOrderSetID(), txtComment
											.getText().toString());
//							posOrdering.confirmOrderSetComment(TRANSACTION_ID,
//									ORDER_ID, pcs.getOrderSetID());

							ProductGroups.PComponentGroup orderSetData = posOrdering
									.getOrderSet(TRANSACTION_ID, ORDER_ID,
											pcs.getPGroupID());
							orderSetLst.set(groupPosition, orderSetData);
							orderSetAdapter.notifyDataSetChanged();

							childOrderListView.setSelectedChild(groupPosition,
									childPosition, true);
							// listAllSetOrder();
							dialog.dismiss();
						}
					});

					btnCancel.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});

					// List dialog menucomment
					// new AlertDialog.Builder(CONTEXT)
					// .setTitle(R.string.title_menu_comment)
					// .setView(v)
					// .setPositiveButton(R.string.btn_ok_comment, new
					// DialogInterface.OnClickListener() {
					// // public void onClick(DialogInterface dialog, int
					// whichButton) {
					// // POSOrdering posOrdering = new POSOrdering(CONTEXT);
					// // posOrdering.updateOrderSet(TRANSACTION_ID, ORDER_ID,
					// pcs.getOrderSetID(), txtComment.getText().toString());
					// // posOrdering.confirmOrderSetComment(TRANSACTION_ID,
					// ORDER_ID, pcs.getOrderSetID());
					// // listAllSetOrder();
					// // dialog.dismiss();
					// // }
					// // })
					// // .setNegativeButton(R.string.btn_cancel_comment, new
					// DialogInterface.OnClickListener() {
					// // public void onClick(DialogInterface dialog, int
					// whichButton) {
					// // dialog.dismiss();
					// // }
					// // }).show();
					// childOrderListView.setItemChecked(childPosition, true);

					// MenuComment mc = new MenuComment(CONTEXT, 30048);
					//
					// final List<MenuGroups.MenuComment> mcl = mc
					// .menuCommentList();
					//
					// OrderSetMenuCommentAdapter adapter = new
					// OrderSetMenuCommentAdapter(
					// CONTEXT, TRANSACTION_ID, mcl, pcs);
					//
					// Button btnCancel = (Button)
					// findViewById(R.id.buttonCancelComment);
					// Button btnOk = (Button)
					// findViewById(R.id.buttonOkComment);
					// final EditText txtComment = (EditText)
					// findViewById(R.id.txt_menu_comment);
					// ListView menuCommentListView = (ListView)
					// findViewById(R.id.menuCommentListView);
					//
					// txtComment.setText(pcs.getOrderComment());
					//
					// // set adapter view click listenner
					// POSOrdering posOrdering = new POSOrdering(CONTEXT);
					// posOrdering.createOrderSetComment(TRANSACTION_ID,
					// ORDER_ID,
					// pcs.getOrderSetID());
					//
					// menuCommentListView
					// .setOnItemClickListener(new OnItemClickListener() {
					//
					// @Override
					// public void onItemClick(AdapterView<?> parent,
					// View v, int position, long id) {
					//
					// MenuCommentListAdapter.MenuCommentViewHolder holder =
					// (MenuCommentListAdapter.MenuCommentViewHolder) v
					// .getTag();
					//
					// syn.pos.data.model.MenuGroups.MenuComment mc = mcl
					// .get(position);
					//
					// if (holder.isChecked == false) {
					// holder.chkMenuComment
					// .setImageResource(R.drawable.btn_check_buttonless_on);
					// holder.isChecked = true;
					// holder.tvMenuCommentQty.setText("1");
					// holder.btnMenuCommentMinus
					// .setEnabled(true);
					// holder.btnMenuCommentPlus
					// .setEnabled(true);
					//
					// POSOrdering posOrdering = new POSOrdering(
					// CONTEXT);
					// posOrdering.addOrderSetComment(
					// TRANSACTION_ID, ORDER_ID,
					// pcs.getOrderSetID(),
					// pcg.getPGroupID(),
					// pcs.getProductID(),
					// mc.getMenuCommentID(),
					// mc.getCommentQty(),
					// mc.getProductPricePerUnit());
					// } else {
					// holder.chkMenuComment
					// .setImageResource(R.drawable.btn_check_buttonless_off);
					// holder.isChecked = false;
					// holder.tvMenuCommentQty.setText("0");
					// holder.btnMenuCommentMinus
					// .setEnabled(false);
					// holder.btnMenuCommentPlus
					// .setEnabled(false);
					//
					// POSOrdering posOrdering = new POSOrdering(
					// CONTEXT);
					// posOrdering.deleteOrderSetComment(
					// TRANSACTION_ID, ORDER_ID,
					// pcs.getOrderSetID(),
					// mc.getMenuCommentID());
					//
					// }
					// }
					//
					// });
					//
					// // set adapter
					// menuCommentListView.setAdapter(adapter);
					//
					// btnOk.setOnClickListener(new OnClickListener() {
					//
					// @Override
					// public void onClick(View v) {
					// POSOrdering posOrdering = new POSOrdering(CONTEXT);
					// posOrdering.updateOrderSet(TRANSACTION_ID,
					// ORDER_ID, pcs.getOrderSetID(), txtComment
					// .getText().toString());
					// posOrdering.confirmOrderSetComment(TRANSACTION_ID,
					// ORDER_ID, pcs.getOrderSetID());
					//
					// ProductGroups.PComponentGroup orderSetData = posOrdering
					// .getOrderSet(TRANSACTION_ID, ORDER_ID,
					// pcs.getPGroupID());
					// orderSetLst.set(groupPosition, orderSetData);
					// orderSetAdapter.notifyDataSetChanged();
					//
					// childOrderListView.setSelectedChild(groupPosition,
					// childPosition, true);
					// // listAllSetOrder();
					//
					// InputMethodManager imm = (InputMethodManager)
					// getSystemService(Context.INPUT_METHOD_SERVICE);
					// imm.hideSoftInputFromWindow(
					// txtComment.getWindowToken(), 0);
					//
					// anim = AnimationUtils.loadAnimation(CONTEXT,
					// R.animator.slide_down_up);
					// menuCommentLayout.setAnimation(anim);
					// menuCommentLayout.setVisibility(View.GONE);
					// }
					// });
					//
					// btnCancel.setOnClickListener(new OnClickListener() {
					//
					// @Override
					// public void onClick(View v) {
					// InputMethodManager imm = (InputMethodManager)
					// getSystemService(Context.INPUT_METHOD_SERVICE);
					// imm.hideSoftInputFromWindow(
					// txtComment.getWindowToken(), 0);
					//
					// anim = AnimationUtils.loadAnimation(CONTEXT,
					// R.animator.slide_down_up);
					// menuCommentLayout.setAnimation(anim);
					// menuCommentLayout.setVisibility(View.GONE);
					// }
					// });
					//
					// anim = AnimationUtils.loadAnimation(CONTEXT,
					// R.animator.slide_down_in);
					// menuCommentLayout.setAnimation(anim);
					// menuCommentLayout.setVisibility(View.VISIBLE);
				}

			});

			// set group 0
			if (pcs.getSetGroupNo() == 0) {
				holder.btnMinus.setVisibility(View.GONE);
				holder.btnPlus.setVisibility(View.GONE);
				holder.btnComment.setVisibility(View.GONE);
				holder.btnDelete.setVisibility(View.INVISIBLE);
			} else {
				holder.btnMinus.setVisibility(View.VISIBLE);
				holder.btnPlus.setVisibility(View.VISIBLE);
				holder.btnComment.setVisibility(View.VISIBLE);
				holder.btnDelete.setVisibility(View.VISIBLE);
			}

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return pCompGroupLst.get(groupPosition).pCompSetLst.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return pCompGroupLst.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return pCompGroupLst.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {

			OrderSetViewHolder holder;
			if (convertView == null) {
				holder = new OrderSetViewHolder();
				LayoutInflater inflater = LayoutInflater.from(CONTEXT);
				convertView = inflater.inflate(
						R.layout.order_set_group_template, null);

				holder.tvOrderSetGroupName = (TextView) convertView
						.findViewById(R.id.textViewOrderGroupName);
				holder.btnDeleteAll = (Button) convertView
						.findViewById(R.id.buttonDeleteAll);

				convertView.setTag(holder);

			} else {
				holder = (OrderSetViewHolder) convertView.getTag();
			}

			holder.tvOrderSetGroupName.setText(pCompGroupLst.get(groupPosition)
					.getSetGroupName());

			holder.btnDeleteAll.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					LayoutInflater inflater = LayoutInflater.from(CONTEXT);
					v = inflater.inflate(R.layout.custom_dialog, null);

					TextView tvTitle = (TextView) v
							.findViewById(R.id.textViewTitle);
					TextView tvMessage = (TextView) v
							.findViewById(R.id.textViewMessage);
					Button btnOk = (Button) v.findViewById(R.id.buttonOk);
					Button btnCancel = (Button) v
							.findViewById(R.id.buttonConfirmCancel);

					tvTitle.setVisibility(View.VISIBLE);
					tvTitle.setText(R.string.dialog_delete_item_title);
					tvMessage.setText(R.string.dialog_delete_all_item);
					btnOk.setText(R.string.dialog_delete_item_btn_ok);
					btnCancel.setText(R.string.dialog_delete_item_btn_cancel);

					final Dialog dialog = new Dialog(CONTEXT,
							R.style.CustomDialog);
					dialog.setContentView(v);
					dialog.show();

					btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							POSOrdering posOrder = new POSOrdering(CONTEXT);
							posOrder.deleteOrderGroupSet(TRANSACTION_ID,
									ORDER_ID, pCompGroupLst.get(groupPosition)
											.getPGroupID());

							if (pCompGroupLst.get(groupPosition)
									.getRequireAmount() != 0)
								recalculateSetRequireAmount(
										pCompGroupLst.get(groupPosition)
												.getPGroupID(), pCompGroupLst
												.get(groupPosition)
												.getRequireAmount());
							listAllSetOrder();
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
			});

			if (pCompGroupLst.get(groupPosition).getSetGroupNo() == 0)
				holder.btnDeleteAll.setVisibility(View.INVISIBLE);
			else
				holder.btnDeleteAll.setVisibility(View.VISIBLE);

			// ExpandableListView elv = (ExpandableListView) parent;
			// elv.expandGroup(groupPosition);

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		protected class OrderSetViewHolder {
			TextView tvOrderSetGroupName;
			Button btnDeleteAll;
		}
	}

	private class UpdateOrderListTask extends
			AsyncTask<String, Boolean, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			orderSetAdapter = new ChildOrderLinkType7ExpandListAdapter(CONTEXT,
					ORDER_ID, orderSetLst);
			childOrderListView.setAdapter(orderSetAdapter);

			// childOrderListView.smoothScrollToPosition(orderSetAdapter.getChildrenCount(orderSetAdapter.getGroupCount()
			// - 1));

			if (orderSetLst.size() > 0) {
				for (int i = 0; i < orderSetLst.size(); i++) {
					try {
						childOrderListView.expandGroup(i);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			childOrderListView.setSelection(childOrderListView.getCount());
		}

		@Override
		protected Boolean doInBackground(String... params) {
			POSOrdering posOrder = new POSOrdering(CONTEXT);
			orderSetLst = posOrder.listAllGrouperOrderSet(TRANSACTION_ID,
					ORDER_ID);
			return null;
		}
	}
}
