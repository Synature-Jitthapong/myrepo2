package com.syn.iorder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.model.WebServiceResult;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class PrinterUtils {

	public static class LoadLongBillRealFormat extends WebServiceTask{

		public static final String GET_FORMAT_METHOD = "WSiOrder_JSON_GetFormatPrintBillDetail";
		
		private OnLoadFormatPrintBillDetailListener mListener;
		
		public LoadLongBillRealFormat(Context c, GlobalVar gb, int tableId, int staffId,
				OnLoadFormatPrintBillDetailListener listener) {
			super(c, gb, GET_FORMAT_METHOD);

			mListener = listener;
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(staffId);
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
		}

		@Override
		protected void onPreExecute() {
			mListener.onPre();
		}

		@Override
		protected void onPostExecute(String result) {
			Gson gson = new Gson();
			WebServiceResult wsResult;
			try {
				wsResult = toServiceObject(result);
				if(wsResult.getiResultID() == 0){
					Type type = new TypeToken<ArrayList<PrintUtilLine>>(){}.getType();
					ArrayList<PrintUtilLine> lines = null;
					try {
						lines = gson.fromJson(wsResult.getSzResultData(), type);
						mListener.onPost(lines, result);
					} catch (JsonSyntaxException e) {
						mListener.onError(result);
					}
				}else{
					mListener.onError(!TextUtils.isEmpty(wsResult.getSzResultData()) ?
							wsResult.getSzResultData() : result);
				}
			} catch (JsonSyntaxException e1) {
				mListener.onError(result);
			}
		}
	}
			
	public static class PrintTransToPrinterTask extends WebServiceTask{
		public static final String PRINT_TRANS_METHOD = "WSiOrder_JSON_PrintTransactionBillDetailToPrinter";
		private PrintLongbillListener mListener;
		
		public PrintTransToPrinterTask(Context c, GlobalVar gb, 
				int transactionId, int computerId, int printerId, 
				int staffId, PrintLongbillListener listener) {
			super(c, gb, PRINT_TRANS_METHOD);
			mListener = listener;

			PropertyInfo property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(staffId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iComputerID");
			property.setValue(GlobalVar.COMPUTER_ID);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iDbTransID");
			property.setValue(transactionId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iDbCompID");
			property.setValue(computerId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iPrinterID");
			property.setValue(printerId);
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
			WebServiceResult res = null;
			
			try {
				res = gson.fromJson(result, type);
				mListener.onPost(res, result);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				mListener.onError(result);
			}
		}
		
	}
	
	public static class LoadPrinterTask extends WebServiceTask{
		public static final String GET_PRINTER_METHOD = "WSiOrder_JSON_GetPrinterForPrintBillDetail";
		private LoadPrinterProgressListener mListener;
		
		public LoadPrinterTask(Context c, GlobalVar gb, int staffId, LoadPrinterProgressListener listener) {
			super(c, gb, GET_PRINTER_METHOD);
			mListener = listener;
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iStaffID");
			property.setValue(staffId);
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
			Type type = new TypeToken<List<Printer>>(){}.getType();
			List<Printer> printerLst = null;
			try {
				printerLst = gson.fromJson(result, type);
				mListener.onPost(printerLst, result);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				mListener.onError(result);
			}
		}		
	}
	
	public static interface OnLoadFormatPrintBillDetailListener extends ProgressListener{
		void onPost(ArrayList<PrintUtilLine> lines, String result);
	}
	
	public static interface PrintLongbillListener extends ProgressListener{
		void onPost(WebServiceResult res, String result);
	}
	
	public static interface LoadPrinterProgressListener extends ProgressListener{
		void onPost(List<Printer> printerLst, String result);
	}
	
	public static class PrintUtilLine implements Parcelable{

		public static final int PRINT_DEFAULT = 0;
		public static final int PRINT_LEFT = 1;
		public static final int PRINT_RIGHT = 2;
		public static final int PRINT_CENTER = 3;
		public static final int PRINT_THREE_COLUMN = 4;
		public static final int PRINT_LEFT_RIGHT = 5;
		public static final int PRINT_BLANK = 6;
		
		private String PrintFont;
		private String LeftText;
		private int PrintLineType;
		private String CenterText;
		private String RightText;
		private boolean IsUnderLine;

		public void setPrintFont(String PrintFont) {
			this.PrintFont = PrintFont;
		}

		public void setLeftText(String LeftText) {
			this.LeftText = LeftText;
		}

		public void setPrintLineType(int PrintLineType) {
			this.PrintLineType = PrintLineType;
		}

		public void setCenterText(String CenterText) {
			this.CenterText = CenterText;
		}

		public void setRightText(String RightText) {
			this.RightText = RightText;
		}

		public void setIsUnderLine(boolean IsUnderLine) {
			this.IsUnderLine = IsUnderLine;
		}

		public String getPrintFont() {
			return PrintFont;
		}

		public String getLeftText() {
			return LeftText;
		}

		public int getPrintLineType() {
			return PrintLineType;
		}

		public String getCenterText() {
			return CenterText;
		}

		public String getRightText() {
			return RightText;
		}

		public boolean isIsUnderLine() {
			return IsUnderLine;
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public static class Printer{
		private int PrinterID;
		private String PrinterName;
		private boolean isChecked;
		
		public boolean isChecked() {
			return isChecked;
		}
		public void setChecked(boolean isChecked) {
			this.isChecked = isChecked;
		}
		public int getPrinterID() {
			return PrinterID;
		}
		public void setPrinterID(int printerID) {
			PrinterID = printerID;
		}
		public String getPrinterName() {
			return PrinterName;
		}
		public void setPrinterName(String printerName) {
			PrinterName = printerName;
		}
	}
}
