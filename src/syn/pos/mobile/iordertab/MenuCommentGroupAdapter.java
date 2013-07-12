package syn.pos.mobile.iordertab;

import java.util.List;

import syn.pos.data.model.MenuGroups;
import syn.pos.data.model.MenuGroups.MenuCommentGroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MenuCommentGroupAdapter extends BaseAdapter{
	private List<MenuGroups.MenuCommentGroup> mcgLst;
	private LayoutInflater inflater;
	
	public MenuCommentGroupAdapter(Context c, List<MenuGroups.MenuCommentGroup> mcgLst){
		inflater = LayoutInflater.from(c);
		this.mcgLst = mcgLst;
	}
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mcgLst != null ? mcgLst.size() : 0;
	}

	@Override
	public MenuGroups.MenuCommentGroup getItem(int position) {
		// TODO Auto-generated method stub
		return mcgLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MenuGroups.MenuCommentGroup mg = mcgLst.get(position);
		ViewHolder holder;
		
		if(convertView == null){
			convertView = inflater.inflate(R.layout.spinner_item, null);
			holder = new ViewHolder();
			holder.tvCommentGroupName = (TextView) convertView.findViewById(R.id.textView1);
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tvCommentGroupName.setText(mg.getMenuCommentGroupName_0());
		
		return convertView;
	}
	
	private class ViewHolder{
		public TextView tvCommentGroupName;
	}

}
