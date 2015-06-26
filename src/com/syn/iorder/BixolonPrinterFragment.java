package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import syn.pos.data.model.SummaryTransaction;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.config.JposEntry;

import com.bxl.config.editor.BXLConfigLoader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BixolonPrinterFragment extends DialogFragment implements OnItemClickListener{
	
	public static final int HORIZONTAL_MAX_SPACE = 38;
	
	private static String ESCAPE_CHARACTERS = new String(new byte[] {0x1b, 0x7c});
	
	private static final String DEVICE_ADDRESS_START = " (";
	private static final String DEVICE_ADDRESS_END = ")";

	private final ArrayList<CharSequence> bondedDevices = new ArrayList<CharSequence>();
	private ArrayAdapter<CharSequence> arrayAdapter;

	private BXLConfigLoader bxlConfigLoader;
	private POSPrinter posPrinter;
	private String logicalName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBondedDevices();
		bxlConfigLoader = new BXLConfigLoader(getActivity());
		try {
			bxlConfigLoader.openFile();
		} catch (Exception e) {
			e.printStackTrace();
			bxlConfigLoader.newFile();
		}
		posPrinter = new POSPrinter(getActivity());
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.bixolon_printer_layout, null);
		ListView lvPrinter = (ListView) content.findViewById(R.id.listView1);
		
		arrayAdapter = new ArrayAdapter<CharSequence>(getActivity(),
				android.R.layout.simple_list_item_single_choice, bondedDevices);
		lvPrinter.setAdapter(arrayAdapter);
		lvPrinter.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lvPrinter.setOnItemClickListener(this);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Please select printer");
		builder.setView(content);
		builder.setNegativeButton("Close", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {}
			
		});
		builder.setPositiveButton("Print", null);
		
		final AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String textToPrint = createTextToPrint();
				try {
					posPrinter.open(logicalName);
					posPrinter.claim(0);
					posPrinter.setDeviceEnabled(true);
					posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, textToPrint);
				} catch (JposException e) {
					e.printStackTrace();
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
				} finally {
					try {
						posPrinter.close();
					} catch (JposException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					dialog.dismiss();
				}
			}
			
		});
		return dialog;
	}
	
	private String createTextToPrint(){
		SummaryTransaction sumTrans = ((CheckBillActivity)getActivity()).getSummTrans();
		String header = ((CheckBillActivity)getActivity()).getBillHeader().toString();
		String custNo = ((CheckBillActivity)getActivity()).getBillCustNo().toString();
		String tableName = ((CheckBillActivity)getActivity()).getTableName().toString();
		
		GlobalVar globalVar = new GlobalVar(getActivity());
		StringBuilder textToPrint = new StringBuilder();
		textToPrint.append(ESCAPE_CHARACTERS + "cM"); // font c
		
		// header
		textToPrint.append(adjustAlignCenter(header) + "\n");
		textToPrint.append(adjustAlignCenter(tableName + custNo) + "\n");
		textToPrint.append(createLine("-") + "\n");
		
		if(sumTrans != null){
			if(sumTrans.OrderList != null){
				for(SummaryTransaction.Order order : sumTrans.OrderList){
					String productName = globalVar.qtyFormat.format(order.fAmount) + "x" + order.szProductName;
					String productPrice = globalVar.decimalFormat.format(order.fTotalPrice);
					
					textToPrint.append(productName);
					textToPrint.append(
							createHorizontalSpace(
									calculateLength(productName) + 
									calculateLength(productPrice)));
					textToPrint.append(productPrice);
					textToPrint.append("\n");
				}
			}
			textToPrint.append(createLine("=") + "\n");
			for(syn.pos.data.model.SummaryTransaction.DisplaySummary displaySummary 
					: sumTrans.TransactionSummary.DisplaySummaryList){
				String label = displaySummary.szDisplayName;
				String value = globalVar.decimalFormat.format(displaySummary.fPriceValue);
				textToPrint.append(label);
				textToPrint.append(
						createHorizontalSpace(
								calculateLength(label) + 
								calculateLength(value)));
				textToPrint.append(value + "\n");
			}
			textToPrint.append("\n");
		}
		return textToPrint.toString();
	}
	
	protected String createLine(String sign){
		StringBuilder line = new StringBuilder();
		for(int i = 0; i <= HORIZONTAL_MAX_SPACE; i++){
			line.append(sign);
		}
		return line.toString();
	}
	
	protected String adjustAlignCenter(String text){
		int rimSpace = (HORIZONTAL_MAX_SPACE - calculateLength(text)) / 2;
		StringBuilder empText = new StringBuilder();
		for(int i = 0; i < rimSpace; i++){
			empText.append(" ");
		}
		return empText.toString() + text + empText.toString();
	}
	
	protected String createHorizontalSpace(int usedSpace){
		StringBuilder space = new StringBuilder();
		if(usedSpace > HORIZONTAL_MAX_SPACE){
			usedSpace = HORIZONTAL_MAX_SPACE - 2;
		}
		for(int i = usedSpace; i <= HORIZONTAL_MAX_SPACE; i++){
			space.append(" ");
		}
		return space.toString();
	}
	
	protected int calculateLength(String text){
		if(text == null)
			return 0;
		int length = 0;
		for(int i = 0; i < text.length(); i++){
			int code = (int) text.charAt(i);
			if(code != 3633 
					// thai
					&& code != 3636
					&& code != 3637
					&& code != 3638
					&& code != 3639
					&& code != 3640
					&& code != 3641
					&& code != 3642
					&& code != 3655
					&& code != 3656
					&& code != 3657
					&& code != 3658
					&& code != 3659
					&& code != 3660
					&& code != 3661
					&& code != 3662){
				length ++;
			}
		}
		return length == 0 ? text.length() : length;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String device = ((TextView) view).getText().toString();

		logicalName = device.substring(0, device.indexOf(DEVICE_ADDRESS_START));

		String address = device.substring(device.indexOf(DEVICE_ADDRESS_START)
				+ DEVICE_ADDRESS_START.length(),
				device.indexOf(DEVICE_ADDRESS_END));

		try {
			for (Object entry : bxlConfigLoader.getEntries()) {
				JposEntry jposEntry = (JposEntry) entry;
				bxlConfigLoader.removeEntry(jposEntry.getLogicalName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

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
	
	@Override
	public void onDestroy() {
		try {
			posPrinter.close();
		} catch (JposException e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	private void setBondedDevices() {
		logicalName = null;
		bondedDevices.clear();

		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		Set<BluetoothDevice> bondedDeviceSet = bluetoothAdapter
				.getBondedDevices();

		for (BluetoothDevice device : bondedDeviceSet) {
			bondedDevices.add(device.getName() + DEVICE_ADDRESS_START
					+ device.getAddress() + DEVICE_ADDRESS_END);
		}

		if (arrayAdapter != null) {
			arrayAdapter.notifyDataSetChanged();
		}
	}
}
