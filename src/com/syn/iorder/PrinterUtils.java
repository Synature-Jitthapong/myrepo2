package com.syn.iorder;

import java.lang.reflect.Type;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.model.WebServiceResult;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import android.content.Context;

public class PrinterUtils {

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
	
	public static interface PrintLongbillListener extends ProgressListener{
		void onPost(WebServiceResult res, String result);
	}
	
	public static interface LoadPrinterProgressListener extends ProgressListener{
		void onPost(List<Printer> printerLst, String result);
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
