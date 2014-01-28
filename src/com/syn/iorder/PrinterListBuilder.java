package com.syn.iorder;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PrinterListBuilder extends AlertDialog.Builder implements OnItemClickListener{
	
	private PrinterUtils.Printer mPrinterData;
	private List<PrinterUtils.Printer> mPrinterLst;
	private PrinterListAdapter mPrinterAdapter;
	
	protected PrinterListBuilder(Context context, List<PrinterUtils.Printer> printerLst) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View printerView = inflater.inflate(R.layout.select_printer_layout, null);
		ListView lvPrinter = (ListView) printerView.findViewById(R.id.lvPrinter);
		setView(printerView);

		mPrinterLst = printerLst;
		mPrinterAdapter = 
				new PrinterListAdapter(context, printerLst);
		mPrinterData = new PrinterUtils.Printer();
		lvPrinter.setAdapter(mPrinterAdapter);
		lvPrinter.setOnItemClickListener(this);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		PrinterUtils.Printer printer = (PrinterUtils.Printer)
				parent.getItemAtPosition(position);
		
		if(printer.isChecked()){
			printer.setChecked(false);
			mPrinterData.setPrinterID(0);
		}else{
			printer.setChecked(true);
			mPrinterData.setPrinterID(printer.getPrinterID());
		}
		
		try {
			for(PrinterUtils.Printer p : mPrinterLst){
				if(p.getPrinterID() != printer.getPrinterID()){
					p.setChecked(false);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mPrinterAdapter.notifyDataSetChanged();
	}

	public PrinterUtils.Printer getPrinterData() {
		return mPrinterData;
	}

	public PrinterListAdapter getPrinterAdapter() {
		return mPrinterAdapter;
	}

}
