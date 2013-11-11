package com.syn.iorder;

import java.util.List;

import syn.pos.data.dao.QuestionGroups;
import syn.pos.data.model.ProductGroups;
import syn.pos.data.model.ProductGroups.QuestionDetail;
import syn.pos.mobile.iordertab.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SelectTableQuestionAdapter extends BaseAdapter {
	private List<ProductGroups.QuestionDetail> qsLst;
	private LayoutInflater inflater;
	private TextView tvTotalCust;
	private int totalCust;
	private Context context;
	private GlobalVar globalVar;
	
	public SelectTableQuestionAdapter(Context c, GlobalVar globalVar, List<ProductGroups.QuestionDetail> qsLst){
		this.context = c;
		this.globalVar = globalVar;
		this.qsLst = qsLst;
		inflater = LayoutInflater.from(c);
		this.tvTotalCust = new TextView(c);
		this.totalCust = 0;
	}
	
	public SelectTableQuestionAdapter(Context c, GlobalVar globalVar, List<ProductGroups.QuestionDetail> qsLst, 
			TextView tvTotalCust){
		this.context = c;
		this.globalVar = globalVar;
		this.qsLst = qsLst;
		inflater = LayoutInflater.from(c);
		this.tvTotalCust = tvTotalCust;
		this.totalCust = 0;
	}
	
	@Override
	public int getCount() {
		return qsLst == null ? 0 : qsLst.size();
	}

	@Override
	public ProductGroups.QuestionDetail getItem(int position) {
		return qsLst.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ProductGroups.QuestionDetail qsDetail = qsLst.get(position);
		final QuestionGroups qsGroup = new QuestionGroups(context);
		final ViewHolder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.select_table_ques_template, null);
			holder = new ViewHolder();
			holder.layoutType2 = (LinearLayout) convertView.findViewById(R.id.layoutType2);
			holder.layoutType4 = (HorizontalScrollView) convertView.findViewById(R.id.layoutType4);
			holder.optLayout = (LinearLayout) convertView.findViewById(R.id.optionLayout);
			holder.tvRequire = (TextView) convertView.findViewById(R.id.textViewRequire);
			holder.tvQsName = (TextView) convertView.findViewById(R.id.textView1);
			holder.tvQsQty = (TextView) convertView.findViewById(R.id.textViewQuestQty);
			holder.btnQsMinus = (Button) convertView.findViewById(R.id.buttonQuestMinus);
			holder.btnQsPlus = (Button) convertView.findViewById(R.id.buttonQuestPlus);
			
			if(qsDetail.getQuestionTypeID() == 2){
				holder.layoutType2.setVisibility(View.VISIBLE);
				holder.layoutType4.setVisibility(View.GONE);
			}else if(qsDetail.getQuestionTypeID() == 4){
				holder.layoutType2.setVisibility(View.GONE);
				holder.layoutType4.setVisibility(View.VISIBLE);
				
				// list answer option
				final List<ProductGroups.AnswerOption> optLst = qsGroup.listAnswerOption(qsDetail.getQuestionID());
				if(optLst != null){
					int i = 0;
					
					//holder.optLayout.removeAllViews();
					for(final ProductGroups.AnswerOption opt : optLst){
						final View optView = inflater.inflate(R.layout.answer_option_template, null);
						final Button btnOpt = (Button) optView.findViewById(R.id.button1);
						btnOpt.setId(opt.getAnswerID());
						btnOpt.setText(opt.getAnswerName());
						
						int addedOptId = qsGroup.checkAddedOpt(qsDetail.getQuestionID());
						
						if(addedOptId == opt.getAnswerID()){
							btnOpt.setSelected(true);
						}else{
							btnOpt.setSelected(false);
						}
						
						btnOpt.setOnClickListener(new OnClickListener(){
							
							@Override
							public void onClick(View v) {
								int addedOptId = qsGroup.checkAddedOpt(qsDetail.getQuestionID());

								if(addedOptId == opt.getAnswerID()){
									qsGroup.deleteAnswerQuestion(qsDetail.getQuestionID());
									btnOpt.setSelected(false);
								}else{
									qsGroup.addAnswerQuestion(qsDetail.getQuestionID(), opt.getAnswerID(), opt.getAnswerName());
									btnOpt.setSelected(true);
									
								}
								try {
									for(ProductGroups.AnswerOption opt2 : optLst){
										if(opt.getAnswerID() != opt2.getAnswerID()){
											Button btn = (Button) holder.optLayout.findViewById(opt2.getAnswerID());
											btn.setSelected(false);
										}
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
						});
//						if(i == 0)
//							btnOpt.setBackgroundResource(R.drawable.grey_button_left);
//						else if(i == optLst.size() - 1)
//							btnOpt.setBackgroundResource(R.drawable.grey_button_right);
						holder.optLayout.addView(optView);
						i++;
					}
				}
			}
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		if(qsDetail.isRequireSelect())
			holder.tvRequire.setVisibility(View.VISIBLE);
		else
			holder.tvRequire.setVisibility(View.GONE);
		
		holder.tvQsName.setText(qsDetail.getQuestionName());
		holder.tvQsQty.setText(globalVar.qtyFormat.format(qsDetail
				.getQuestionValue()));

		totalCust = qsGroup.getTotalAnswerQty();
		tvTotalCust.setText(Integer.toString(totalCust));

		holder.btnQsMinus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int qty = Integer.parseInt(holder.tvQsQty.getText().toString());
				if (--qty >= 0) {
					holder.tvQsQty.setText(Integer.toString(qty));
					qsDetail.setQuestionValue(qty);
					qsGroup.addAnswerQuestion(qsDetail.getQuestionID(), 0, qty,
							qsDetail.getQuestionName());

					if (qty == 0) {
						qsGroup.deleteAnswerQuestion(qsDetail.getQuestionID());
					}
				}

				totalCust = qsGroup.getTotalAnswerQty();
				tvTotalCust.setText(Integer.toString(totalCust));
			}
		});
		holder.btnQsPlus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int qty = Integer.parseInt(holder.tvQsQty.getText().toString());
				holder.tvQsQty.setText(Integer.toString(++qty));
				qsDetail.setQuestionValue(qty);
				qsGroup.addAnswerQuestion(qsDetail.getQuestionID(), 0, qty,
						qsDetail.getQuestionName());

				totalCust = qsGroup.getTotalAnswerQty();
				tvTotalCust.setText(Integer.toString(totalCust));
			}
		});
		return convertView;
	}

	private class ViewHolder{
		public LinearLayout layoutType2;
		public HorizontalScrollView layoutType4;
		public LinearLayout optLayout;
		public TextView tvRequire;
		public TextView tvQsName;
		public TextView tvQsQty;
		public Button btnQsMinus;
		public Button btnQsPlus;
	}
}
