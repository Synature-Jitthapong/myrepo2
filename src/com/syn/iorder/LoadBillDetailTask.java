package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.SummaryTransaction;
import syn.pos.data.model.WebServiceResult;
import syn.pos.mobile.iordertab.R;
import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadBillDetailTask extends WebServiceTask{
		private static final String webMethod = "WSiOrder_JSON_ShowSummaryBillDetail";
		protected SummaryTransaction summaryTrans;
		protected ListView lvBill;
		protected TextView tvSumText;
		protected TextView tvSumPriceVal;
		protected ProgressBar progress;
		
		public LoadBillDetailTask(Context c, GlobalVar gb, int tableId, 
				ListView lvBill, TextView tvSumText, TextView tvSumVal, ProgressBar progress) {
			super(c, gb, webMethod);
			
			this.lvBill = lvBill;
			this.tvSumText = tvSumText;
			this.tvSumPriceVal = tvSumVal;
			
			this.progress = progress;
			
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
			property.setValue(tableId);
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
			this.progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			this.progress.setVisibility(View.GONE);
			
			GsonDeserialze gdz = new GsonDeserialze();
			try {
				WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
				summaryTrans = gdz.deserializeSummaryTransactionJSON(wsResult.getSzResultData());
				
				if(summaryTrans != null){
					if(summaryTrans.OrderList != null){
						List<SummaryTransaction.Order> orderList = 
								new ArrayList<SummaryTransaction.Order>();
						
						for(SummaryTransaction.Order order : summaryTrans.OrderList){
							int productSetType = order.iProductSetType;
							//if(productSetType == 0 || productSetType == 15){
								orderList.add(order);
							//}
						}
						
						summaryTrans.OrderList = orderList;
					}
					
					tvSumText.setText(null);
					tvSumPriceVal.setText(null);
					for(syn.pos.data.model.SummaryTransaction.DisplaySummary displaySummary 
							: summaryTrans.TransactionSummary.DisplaySummaryList){
						tvSumText.append(displaySummary.szDisplayName + "\n");
						tvSumPriceVal.append(globalVar.decimalFormat.format(displaySummary.fPriceValue) + "\n");
					}
					TextView tvCallCheckBill = new TextView(context);
					tvCallCheckBill.setText(R.string.call_checkbill);
				}
				BillDetailAdapter billDetailAdapter = new BillDetailAdapter(context, 
						globalVar, summaryTrans);
				lvBill.setAdapter(billDetailAdapter);
			} catch (Exception e) {
				IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, result, 0);
			}
		}
}
