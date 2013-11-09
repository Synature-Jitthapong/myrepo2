package syn.pos.mobile.iordertab;

import java.util.List;

import syn.pos.data.dao.MenuUtil;
import syn.pos.data.dao.ShopProperty;
import syn.pos.data.model.OrderSendData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class SelectOrderAdapter extends BaseAdapter {
	
	private List<OrderSendData.OrderDetail> orderLst;
	private Context context;
	private GlobalVar globalVar;
	private ShopProperty shopProperty;
	private LayoutInflater inflater;
	
	public SelectOrderAdapter(Context c, GlobalVar gb, List<OrderSendData.OrderDetail> ordLst){
		context = c;
		orderLst = ordLst;
		globalVar = gb;
		inflater = LayoutInflater.from(c);
		shopProperty = new ShopProperty(c, null);
	}
	
	@Override
	public int getCount() {
		return orderLst != null ? orderLst.size() : 0;
	}

	@Override
	public OrderSendData.OrderDetail getItem(int position) {
		return orderLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		OrderSendData.OrderDetail detail = orderLst.get(position);
		SelectOrderViewHolder holder;
		if(convertView == null){
			holder = new SelectOrderViewHolder();
			convertView = inflater.inflate(R.layout.select_order_template, null);
			holder.tvSeatName = (TextView) convertView.findViewById(R.id.textView1);
			holder.chkMenuName = (CheckedTextView) convertView.findViewById(R.id.checkedTextView1);
			holder.tvOrderQty = (TextView) convertView.findViewById(R.id.textView2);
			
			convertView.setTag(holder);
		}else{
			holder = (SelectOrderViewHolder) convertView.getTag();
		}

		// check if item is selected
		MenuUtil menuUtil = new MenuUtil(context);
		if(menuUtil.checkSelectedMenu(detail.getiOrderID(), detail.getiProductID()) != 0)
			holder.chkMenuName.setChecked(true);
		else
			holder.chkMenuName.setChecked(false);
		
		if(detail.getiSeatID() != 0){
			syn.pos.data.model.ShopData.SeatNo seat = 
					shopProperty.getSeatNo(detail.getiSeatID());
			
			holder.tvSeatName.setText(seat.getSeatName());
		}
		
		holder.chkMenuName.setText(detail.getSzProductName());
		holder.tvOrderQty.setText("x" + globalVar.qtyFormat.format(detail.getfItemQty()));
		
		return convertView;
	}

	protected static class SelectOrderViewHolder{
		CheckedTextView chkMenuName;
		TextView tvSeatName;
		TextView tvOrderQty;
	}
}
