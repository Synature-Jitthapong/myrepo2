package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.config.JposEntry;

import com.bxl.config.editor.BXLConfigLoader;
import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BluetoothPrinter extends TextPrintBase{

	public static final int EPSON = 0;
	public static final int BIXOLON = 1;
	public static final String EPSON_PRINTER_MODEL = "TM-P20";
	
	public static final String PREF_BT_PRINTER_VENDOR = "pref_bt_printer_vendor";
	public static final String PREF_BT_PRINTER_MAC_ADDRESS = "pref_bt_printer_mac_address";
	
	private static String ESCAPE_CHARACTERS = new String(new byte[] {0x1b, 0x7c});

	private static final String DEVICE_ADDRESS_START = " (";
	private static final String DEVICE_ADDRESS_END = ")";
	
	private ArrayList<PrinterUtils.PrintUtilLine> mPrintFormatLst;
	
	private OnPrinterWorkingListener mOnPrinterWorkingListener;
	
	private Context mContext;
	private Print mEpsonPrinter;
	private BXLConfigLoader mBxlConfigLoader;
	private POSPrinter mBixolonPrinter;
	
	public BluetoothPrinter(Context context, ArrayList<PrinterUtils.PrintUtilLine> printFormatLst, 
			OnPrinterWorkingListener listener){
		mContext = context;
		mPrintFormatLst = printFormatLst;
		mOnPrinterWorkingListener = listener;
	}
	
	public void print(){
		int printerVendor = getPrinterVendor();
		if(printerVendor == EPSON){
			printToEpsonPrinter();
		}else if(printerVendor == BIXOLON){
			initConfigLoader();
			printToBixolonPrinter();
		}else{
			mOnPrinterWorkingListener.onPrinterError("");
		}
	}
	
	private void printToEpsonPrinter() {
		mEpsonPrinter = new Print(mContext);
		final SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String mac = sharedPref.getString(PREF_BT_PRINTER_MAC_ADDRESS, null);
		final int interval = 1000;
		final int statusMonitor = Print.TRUE;
		if(mac != null){
			try {
				mEpsonPrinter.openPrinter(Print.DEVTYPE_BLUETOOTH, mac, statusMonitor, interval);
				com.epson.eposprint.Builder builder = createEpsonBuilder();
				new EPSONPrintTask().execute(builder);
			} catch (EposException e) {
				if(mOnPrinterWorkingListener != null)
					mOnPrinterWorkingListener.onPrinterError(e.getMessage());
			}
		}else{
			BluetoothPrinterListDialogFragment f = 
					new BluetoothPrinterListDialogFragment();
			
			Activity host = (Activity) mContext;
			f.show(host.getFragmentManager(), BluetoothPrinterListDialogFragment.TAG);
			f.setOnSelectedPrinterListener(new BluetoothPrinterListDialogFragment.OnSelectedPrinterListener() {
				
				@Override
				public void onSelectedPrinter() {
					try {
						final String mac = sharedPref.getString(PREF_BT_PRINTER_MAC_ADDRESS, null);
						mEpsonPrinter.openPrinter(Print.DEVTYPE_BLUETOOTH, mac, statusMonitor, interval);
						com.epson.eposprint.Builder builder = createEpsonBuilder();
						new EPSONPrintTask().execute(builder);
					} catch (EposException e) {
						if(mOnPrinterWorkingListener != null)
							mOnPrinterWorkingListener.onPrinterError(e.getMessage());
					}
				}
			});
		}
	}

	private com.epson.eposprint.Builder createEpsonBuilder() throws EposException{
		com.epson.eposprint.Builder builder = new com.epson.eposprint.Builder(
				EPSON_PRINTER_MODEL, Builder.MODEL_ANK, mContext);
		builder.addTextFont(Builder.FONT_C);
		builder.addText(createTextToPrint(EPSON));
		return builder;
	}
	
	private void printToBixolonPrinter(){
		mBixolonPrinter = new POSPrinter(mContext);
		List<JposEntry> savedPrinters = getSavedPrinters();
		if(savedPrinters.isEmpty()){
			// show printer list for select
			BluetoothPrinterListDialogFragment f = 
					new BluetoothPrinterListDialogFragment();
			
			Activity host = (Activity) mContext;
			f.show(host.getFragmentManager(), BluetoothPrinterListDialogFragment.TAG);
			f.setOnSelectedPrinterListener(new BluetoothPrinterListDialogFragment.OnSelectedPrinterListener() {
				
				@Override
				public void onSelectedPrinter() {
					String textToPrint = createTextToPrint(BIXOLON);
					new BixolonPrintTask().execute(textToPrint);
				}
			});
		}else{
			String textToPrint = createTextToPrint(BIXOLON);
			new BixolonPrintTask().execute(textToPrint);
		}	
	}
	
	private void initConfigLoader(){
		mBxlConfigLoader = new BXLConfigLoader(mContext);
		try {
			mBxlConfigLoader.openFile();
		} catch (Exception e) {
			e.printStackTrace();
			mBxlConfigLoader.newFile();
		}
	}
	
	public int getPrinterVendor(){
		int printerVendor = 0;
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		printerVendor = sharedPref.getInt(PREF_BT_PRINTER_VENDOR, -1);
		
		return printerVendor;
	}
	
	private List<JposEntry> getSavedPrinters(){
		List<JposEntry> jposEntry = null;
		try {
			jposEntry = mBxlConfigLoader.getEntries();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jposEntry;
	}
	
	private class EPSONPrintTask extends AsyncTask<com.epson.eposprint.Builder, Integer, Integer> implements StatusChangeEventListener, 
		BatteryStatusChangeEventListener{

		public static final int SUCCESS = 0;
		public static final int ERROR = -1;
		
		private CharSequence message;
		
		public EPSONPrintTask(){
			mEpsonPrinter.setBatteryStatusChangeEventCallback(this);
			mEpsonPrinter.setStatusChangeEventCallback(this);
		}
		
		@Override
		protected void onPreExecute() {
			if(mOnPrinterWorkingListener != null)
				mOnPrinterWorkingListener.onPrintStart();
		}

		@Override
		protected void onPostExecute(Integer result) {
			if(result == SUCCESS){
				if(mOnPrinterWorkingListener != null)
					mOnPrinterWorkingListener.onPrintFinish();
			}else if (result == ERROR){
				if(mOnPrinterWorkingListener != null)
					mOnPrinterWorkingListener.onPrinterError(message);
			}
		}

		@Override
		public void onBatteryStatusChangeEvent(String arg0, int arg1) {
		}

		@Override
		public void onStatusChangeEvent(String arg0, int arg1) {
		}

		@Override
		protected Integer doInBackground(com.epson.eposprint.Builder... params) {
			int status = SUCCESS;
			com.epson.eposprint.Builder builder = params[0];
			try {
				mEpsonPrinter.sendData(builder, 10 * 1000, new int[1], new int[1]);
			} catch (EposException e) {
				status = ERROR;
				message = e.getMessage();
			} finally{
				try {
					builder.clearCommandBuffer();
					builder = null;
					mEpsonPrinter.closePrinter();
				} catch (Exception e) {
					builder = null;
				}
			}
			return status;
		}
		
	}
	
	private class BixolonPrintTask extends AsyncTask<String, Integer, Integer>{

		public static final int SUCCESS = 0;
		public static final int ERROR = -1;
		
		private CharSequence message;
		
		@Override
		protected void onPreExecute() {
			if(mOnPrinterWorkingListener != null)
				mOnPrinterWorkingListener.onPrintStart();
		}

		@Override
		protected void onPostExecute(Integer result) {
			if(result == SUCCESS){
				if(mOnPrinterWorkingListener != null)
					mOnPrinterWorkingListener.onPrintFinish();
			}else if (result == ERROR){
				if(mOnPrinterWorkingListener != null)
					mOnPrinterWorkingListener.onPrinterError(message);
			}
		}

		@Override
		protected Integer doInBackground(String... params) {
			int statusCode = SUCCESS;
			String textToPrint = params[0];
			try {
				List<JposEntry> savedPrinters = getSavedPrinters();
				if(savedPrinters != null && !savedPrinters.isEmpty()){
					String logicalName = savedPrinters.get(0).getLogicalName();
					mBixolonPrinter.open(logicalName);
					mBixolonPrinter.claim(0);
					mBixolonPrinter.setDeviceEnabled(true);
					mBixolonPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, textToPrint);
				}
			} catch (JposException e) {
				e.printStackTrace();
				statusCode = ERROR;
				message = e.getMessage();
			} finally {
				try {
					if(mBixolonPrinter != null){
						mBixolonPrinter.close();
					}
				} catch (JposException e) {
					e.printStackTrace();
				}
			}
			return statusCode;
		}
		
	}
	
	private String createTextToPrint(int printerVendor){
		ArrayList<PrinterUtils.PrintUtilLine> lines = mPrintFormatLst;
		
		StringBuilder textToPrint = new StringBuilder();
		
		if(printerVendor == BIXOLON){
			textToPrint.append(ESCAPE_CHARACTERS + "cM"); // font c
		}
		
		for(PrinterUtils.PrintUtilLine line : lines){
			int lineType = line.getPrintLineType();
			if(lineType == PrinterUtils.PrintUtilLine.PRINT_DEFAULT){
				textToPrint.append(line.getLeftText());
			}else if(lineType == PrinterUtils.PrintUtilLine.PRINT_LEFT){
				textToPrint.append(line.getLeftText());
			}else if(lineType == PrinterUtils.PrintUtilLine.PRINT_THREE_COLUMN){
				String left = line.getLeftText() + " ";
				String center = line.getCenterText();
				String right = line.getRightText();
				
				int leftLength = calculateLength(left + center);
				int rightLength = calculateLength(right);
				textToPrint.append(left + center);
				textToPrint.append(createHorizontalSpace(leftLength + rightLength));
				textToPrint.append(right);
			}else if(lineType == PrinterUtils.PrintUtilLine.PRINT_CENTER){
				String left = line.getLeftText() + " ";
				String right = line.getRightText();

				int leftLength = calculateLength(left);
				int rightLength = calculateLength(right);
				textToPrint.append(left);
				textToPrint.append(createHorizontalSpace(leftLength + rightLength));
				textToPrint.append(right);
			}else if(lineType == PrinterUtils.PrintUtilLine.PRINT_LEFT_RIGHT){
				textToPrint.append(adjustAlignCenter(line.getCenterText()));
			}else if(lineType == PrinterUtils.PrintUtilLine.PRINT_RIGHT){
				String left = "";
				String right = line.getRightText();
				int leftLength = calculateLength(left);
				int rightLength = calculateLength(right);
				textToPrint.append(left);
				textToPrint.append(createHorizontalSpace(leftLength + rightLength));
				textToPrint.append(right);
			}else if(lineType == PrinterUtils.PrintUtilLine.PRINT_BLANK){
				//textToPrint.append("\n");
			}
			textToPrint.append("\n");
		}
		textToPrint.append("\n\n");
		return textToPrint.toString();
	}
	
	public static interface OnPrinterWorkingListener{
		void onPrintStart();
		void onPrintFinish();
		void onPrinterError(CharSequence message);
	}
	
	public static class BluetoothPrinterListDialogFragment extends DialogFragment implements OnItemClickListener{

		public static final String TAG = BluetoothPrinterListDialogFragment.class.getSimpleName();
		public static final int REQUEST_FOR_BLUETOOTH_SETTING = 1;
		
		private final ArrayList<CharSequence> bondedDevices = new ArrayList<CharSequence>();
		private ArrayAdapter<CharSequence> arrayAdapter;
		
		private OnSelectedPrinterListener onSelectedPrinterListener;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			setBondedDevices();
			arrayAdapter = new ArrayAdapter<CharSequence>(getActivity(),
					android.R.layout.simple_list_item_single_choice, bondedDevices);
		}

		public void setOnSelectedPrinterListener(OnSelectedPrinterListener listener){
			this.onSelectedPrinterListener = listener;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View content = inflater.inflate(R.layout.bixolon_printer_layout, null);
			ListView lvPrinter = (ListView) content.findViewById(R.id.listView1);
			
			lvPrinter.setAdapter(arrayAdapter);
			lvPrinter.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			lvPrinter.setOnItemClickListener(this);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.please_select_printer);
			builder.setView(content);
			builder.setPositiveButton(R.string.global_btn_close, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(onSelectedPrinterListener != null)
						onSelectedPrinterListener.onSelectedPrinter();
				}
				
			});
			AlertDialog dialog = builder.create();
			dialog.show();
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			return dialog;
		}
		
		private void setSelectedPrinter(final int position){
			String device = (String) arrayAdapter.getItem(position);
			
			String logicalName = device.substring(0, device.indexOf(DEVICE_ADDRESS_START));
			String address = device.substring(device.indexOf(DEVICE_ADDRESS_START)
					+ DEVICE_ADDRESS_START.length(),
					device.indexOf(DEVICE_ADDRESS_END));
			
			int vendor = getPrinterVendor(logicalName);
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			sharedPref.edit().putInt(PREF_BT_PRINTER_VENDOR, vendor).commit();
			if(vendor == 0){
				sharedPref.edit().putString(PREF_BT_PRINTER_MAC_ADDRESS, address).commit();
			}else{
				clearEntry();
				addEntry(logicalName, address);
			}
		}

		private int getPrinterVendor(String deviceName){
			int vendor = EPSON;
			if(deviceName.matches(".*SPP.*"))
				vendor = BIXOLON;
			else if(deviceName.matches(".*TM-P20.*"))
				vendor = EPSON;
			return vendor;
		}
		
		public void addEntry(String logicalName, String address){
			BXLConfigLoader bxlConfigLoader = new BXLConfigLoader(getActivity());
			try {
				bxlConfigLoader.addEntry(logicalName,
						BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
						logicalName,
						BXLConfigLoader.DEVICE_BUS_BLUETOOTH, address);
				
				bxlConfigLoader.saveFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void clearEpson(){
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			sharedPref.edit().putInt(PREF_BT_PRINTER_VENDOR, -1).commit();
		}
		
		public void clearEntry(){
			BXLConfigLoader bxlConfigLoader = new BXLConfigLoader(getActivity());
			try {
				for (Object entry : bxlConfigLoader.getEntries()) {
					JposEntry jposEntry = (JposEntry) entry;
					bxlConfigLoader.removeEntry(jposEntry.getLogicalName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private void setBondedDevices() {
			bondedDevices.clear();

			BluetoothAdapter bluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			
			boolean isRequestSetting = false;
			
			if(bluetoothAdapter.isEnabled()){
				Set<BluetoothDevice> bondedDeviceSet = bluetoothAdapter
						.getBondedDevices();
	
				if(!bondedDeviceSet.isEmpty()){
					for (BluetoothDevice device : bondedDeviceSet) {
						bondedDevices.add(device.getName() + DEVICE_ADDRESS_START
								+ device.getAddress() + DEVICE_ADDRESS_END);
					}
		
					if (arrayAdapter != null) {
						arrayAdapter.notifyDataSetChanged();
					}
				}else{
					isRequestSetting = true;
				}
			}else{
				isRequestSetting = true;
			}
			
			if(isRequestSetting){
				Intent intentBluetooth = new Intent();
		        intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
		        startActivityForResult(intentBluetooth, REQUEST_FOR_BLUETOOTH_SETTING);
			}
		}
		
		
		@Override
		public void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			if(requestCode == REQUEST_FOR_BLUETOOTH_SETTING){
				setBondedDevices();
			}
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			setSelectedPrinter(position);
		}
		
		public static interface OnSelectedPrinterListener{
			void onSelectedPrinter();
		}
	}
}
