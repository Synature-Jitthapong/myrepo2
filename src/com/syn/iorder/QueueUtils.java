package com.syn.iorder;

import java.lang.reflect.Type;
import java.util.List;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import syn.pos.data.model.WebServiceResult;
import android.content.Context;

public class QueueUtils {
	
	public static class GenerateQueue extends WebServiceTask{
		public static final String GEN_QUEUE_METHOD = 
				"WSiQueue_JSON_GenerateNewQueueWithSelectQueuePrinter";
		
		private ProgressListener mListener;
		
		public GenerateQueue(Context c, GlobalVar gb, int queueGroupId, 
				int customerQty, String customerName, int printerId, ProgressListener listener) {
			super(c, gb, GEN_QUEUE_METHOD);
			
			mListener = listener;
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iQueueGroupID");
			property.setValue(queueGroupId);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("iCustQty");
			property.setValue(customerQty);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szCustName");
			property.setValue(customerName);
			property.setType(String.class);
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
			
			try {
				WebServiceResult ws = gson.fromJson(result, type);
				if(ws.getiResultID() == 0)
					mListener.onPost();
				else
					mListener.onError(ws.getSzResultData().equals("") ? 
							result : ws.getSzResultData());
			} catch (JsonSyntaxException e) {
				mListener.onError(result);
				e.printStackTrace();
			}
		}
	}
	
	public static class GenerateAutoQueue extends WebServiceTask{
		public static final String GEN_AUTO_QUEUE_METHOD = 
				"WSiQueue_JSON_CreateNewQueueAutoGroupWithSelectQueuePrinter";
		
		private ProgressListener mListener;
		
		public GenerateAutoQueue(Context c, GlobalVar gb, int customerQty, 
				String customerName, int printerId, ProgressListener listener) {
			super(c, gb, GEN_AUTO_QUEUE_METHOD);
			
			mListener = listener;
			
			PropertyInfo property = new PropertyInfo();
			property.setName("iCustQty");
			property.setValue(customerQty);
			property.setType(int.class);
			soapRequest.addProperty(property);
			
			property = new PropertyInfo();
			property.setName("szCustName");
			property.setValue(customerName);
			property.setType(String.class);
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
			
			try {
				WebServiceResult ws = gson.fromJson(result, type);
				if(ws.getiResultID() == 0)
					mListener.onPost();
				else
					mListener.onError(ws.getSzResultData().equals("") ? 
							result : ws.getSzResultData());
			} catch (JsonSyntaxException e) {
				mListener.onError(result);
				e.printStackTrace();
			}
		}
		
		
	}
	
	public static class GetPrinterForPrintQueue extends WebServiceTask{
		public static final String GET_QUEUE_PRINTER_METHOD = 
				"WSiQueue_JSON_GetPrinterForPrintQueue";
		
		private GetPrinterListener mListener;
		
		public GetPrinterForPrintQueue(Context c, GlobalVar gb, GetPrinterListener listener) {
			super(c, gb, GET_QUEUE_PRINTER_METHOD);
			
			mListener = listener;
			
			PropertyInfo property = new PropertyInfo();
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
			Type type = new TypeToken<List<PrinterUtils.Printer>>(){}.getType();
			
			try {
				List<PrinterUtils.Printer> printerLst = gson.fromJson(result, type);
				mListener.onPost(printerLst);
			} catch (JsonSyntaxException e) {
				mListener.onError(result);
				e.printStackTrace();
			}
		}
		
	}

	public static interface GetPrinterListener extends ProgressListener{
		void onPost(List<PrinterUtils.Printer> printerLst);
	}
}
