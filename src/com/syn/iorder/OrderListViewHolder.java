package com.syn.iorder;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OrderListViewHolder {
	int position;
	int orderDetailId;
	ImageView imgOrder;
	TextView tvOrderNo;
	TextView tvOrderListMenuName;
	TextView tvOrderListMenuComment;
	TextView tvOrderListMenuPrice;
	TextView tvOrderListMenuQty;
	LinearLayout layoutComment;
	Button btnPlus;
	Button btnMinus;
	Button btnComment;
	Button btnDelete;
	Button btnEdit;
}
