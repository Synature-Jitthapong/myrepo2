package com.syn.iorder;

import java.lang.reflect.Type;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.SummaryTransaction;
import syn.pos.data.model.WebServiceResult;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import android.content.Context;

public class DiscountUtils {
	
	public static class GetSummaryBillWithDiscountTask extends WebServiceTask{
		public static final String GET_SUMM_BILL_METHOD = "WSiOrder_JSON_GetSummaryBillWithDiscountItems";
		
		private GetSummaryBillWithDiscountListener mListener;
		
		public GetSummaryBillWithDiscountTask(Context c, GlobalVar gb, int tableId, String promotion,
				String promotionNoCoupon, GetSummaryBillWithDiscountListener listener) {
			super(c, gb, GET_SUMM_BILL_METHOD);

			mListener = listener;
			
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
			
			property = new PropertyInfo();
			property.setName("iTableID");
			property.setValue(tableId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szListPromotionID");
			property.setValue(promotion);
			property.setType(String.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szListNoOfCoupons");
			property.setValue(promotionNoCoupon);
			property.setType(String.class);
			soapRequest.addProperty(property);
		}

		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}

		@Override
		protected void onPostExecute(String result) {
			GsonDeserialze gdz = new GsonDeserialze();
			try {
				WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);
				if(wsResult.getiResultID() == 0){
					mListener.onPost(gdz.deserializeSummaryTransactionJSON(wsResult.getSzResultData()));
				}else{
					mListener.onError(wsResult.getSzResultData().equals("") ? 
							result : wsResult.getSzResultData());
				}
			} catch (Exception e) {
				mListener.onError(result);
				e.printStackTrace();
			}
		}
	}
	
	public static class ListButtonDiscountTask extends WebServiceTask{
		public static final String LIST_BUTTON_DISCOUNT_METHOD = "WSiOrder_JSON_ListButtonDiscountItems";
		private LoadButtonDiscountListener mListener;
		
		public ListButtonDiscountTask(Context c, GlobalVar gb, 
				LoadButtonDiscountListener listener) {
			super(c, gb, LIST_BUTTON_DISCOUNT_METHOD);
			
			mListener = listener;
			
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
		}
		
		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}
		
		@Override
		protected void onPostExecute(String result) {
			Gson gson = new Gson();
			Type type = new TypeToken<WebServiceResult>(){}.getType();
			WebServiceResult ws = null;
			
			try {
				ws=gson.fromJson(result, type);
				if(ws.getiResultID()==0){
					type = new TypeToken<List<ButtonDiscount>>(){}.getType();
					List<ButtonDiscount> btnDiscountLst = gson.fromJson(ws.getSzResultData(), type);
					mListener.onPost(btnDiscountLst);
				}else{
					mListener.onError(ws.getSzResultData().equals("") ? result : 
						ws.getSzResultData());
				}
			} catch (JsonSyntaxException e) {
				mListener.onError(result);
			}
			
		}	
	}
	
	public static interface GetSummaryBillWithDiscountListener extends ProgressListener{
		void onPost(SummaryTransaction summTrans);
	}
	
	public static interface LoadButtonDiscountListener extends ProgressListener{
		void onPost(List<ButtonDiscount> btnDiscountLst);
	}
	
	public static class ButtonDiscount{
		private String DiscountButtonName;
	    private int PromotionID;
	    private int PromotionType;
	    private int IsRequireReferenceNo;
	    private int MaxNumberCanApplied;
	    private int CurrentAppliedNumber;
	    private int[] CurrentReferenceNo;
	    private int InputDiscountNo;
	    private int noCoupone;
	    private boolean isChecked;
	    
		public int[] getCurrentReferenceNo() {
			return CurrentReferenceNo;
		}

		public void setCurrentReferenceNo(int[] currentReferenceNo) {
			CurrentReferenceNo = currentReferenceNo;
		}

		public int getIsRequireReferenceNo() {
			return IsRequireReferenceNo;
		}

		public void setIsRequireReferenceNo(int isRequireReferenceNo) {
			IsRequireReferenceNo = isRequireReferenceNo;
		}

		public int getCurrentAppliedNumber() {
			return CurrentAppliedNumber;
		}

		public void setCurrentAppliedNumber(int currentAppliedNumber) {
			CurrentAppliedNumber = currentAppliedNumber;
		}

		public int getInputDiscountNo() {
			return InputDiscountNo;
		}

		public void setInputDiscountNo(int inputDiscountNo) {
			InputDiscountNo = inputDiscountNo;
		}

		public boolean isChecked() {
			return isChecked;
		}

		public int getNoCoupone() {
			return noCoupone;
		}

		public void setNoCoupone(int noCoupone) {
			this.noCoupone = noCoupone;
		}

		public void setChecked(boolean isChecked) {
			this.isChecked = isChecked;
		}

		public String getDiscountButtonName() {
			return DiscountButtonName;
		}


		public void setDiscountButtonName(String discountButtonName) {
			DiscountButtonName = discountButtonName;
		}


		public int getPromotionID() {
			return PromotionID;
		}

		public void setPromotionID(int promotionID) {
			PromotionID = promotionID;
		}

		public int getPromotionType() {
			return PromotionType;
		}

		public void setPromotionType(int promotionType) {
			PromotionType = promotionType;
		}

		public int getMaxNumberCanApplied() {
			return MaxNumberCanApplied;
		}


		public void setMaxNumberCanApplied(int maxNumberCanApplied) {
			MaxNumberCanApplied = maxNumberCanApplied;
		}

		@Override
		public String toString() {
			return DiscountButtonName;
		}
	    
	}
}
