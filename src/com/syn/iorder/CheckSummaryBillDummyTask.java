package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.dao.POSOrdering;
import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.MenuDataItem;
import syn.pos.data.model.MenuGroups;
import syn.pos.data.model.POSData_OrderTransInfo;
import syn.pos.data.model.ProductGroups;
import syn.pos.data.model.SummaryTransaction;
import syn.pos.data.model.WebServiceResult;

import com.google.gson.Gson;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CheckSummaryBillDummyTask extends WebServiceTask {
	protected SummaryTransaction summaryTrans;
	protected ListView lvBill;
	protected TextView tvSumText;
	protected TextView tvSumPriceVal;
	protected ProgressBar progress;
	
	public CheckSummaryBillDummyTask(Context c, GlobalVar gb, ListView lvBill, TextView tvSumText, 
			TextView tvSumVal, ProgressBar progress) {
		super(c, gb, "WSiOrder_JSON_CheckSummaryBillDummy");
		
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
		property.setName("iMemberID");
		property.setValue(GlobalVar.MEMBER_ID);
		property.setType(int.class);
		soapRequest.addProperty(property);

		// sendorder json
		POSData_OrderTransInfo orderTrans = new POSData_OrderTransInfo();
		orderTrans.setSzTransNote("");

		orderTrans.xListPaymentAmount = new ArrayList<POSData_OrderTransInfo.POSData_PaymentAmount>();

		POSOrdering posOrder = new POSOrdering(context);
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
				if (mi.menuCommentList != null && mi.menuCommentList.size() > 0) {

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
		property.setName("szJSon_OrderTransData");
		property.setValue(jsonToSend);
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
						if(productSetType == 0 || productSetType == 15){
							orderList.add(order);
						}
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

				BillDetailAdapter billDetailAdapter = new BillDetailAdapter(context, 
						globalVar, summaryTrans);
				lvBill.setAdapter(billDetailAdapter);
			}else{
				IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, result, 0);
			}
		} catch (Exception e) {
			IOrderUtility.alertDialog(context, R.string.global_dialog_title_error, result, 0);
		}
	}
}
