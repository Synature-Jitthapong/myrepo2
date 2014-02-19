package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableName;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

public class TableListBuilder extends AlertDialog.Builder{

	public Button mBtnClose;
	
	public TableListBuilder(final Context context, final GlobalVar globalVar, 
			final TableName tbName, final List<TableInfo> tbInfoLst) {
		super(context);	
		
		LayoutInflater inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View closeTableView = inflater.inflate(R.layout.close_table_layout, null);
		final ListView lvTable = (ListView) closeTableView.findViewById(R.id.lvTable);
		Spinner spTableZone = (Spinner) closeTableView.findViewById(R.id.spTableZone);
		mBtnClose = (Button) closeTableView.findViewById(R.id.btnClose);
		setView(closeTableView);
		
		spTableZone.setAdapter(IOrderUtility.createTableZoneAdapter(context, tbName));
		spTableZone.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				
				TableName.TableZone tbZone = (TableName.TableZone) parent.getItemAtPosition(position);
				List<TableInfo> newTbInfoLst = new ArrayList<TableInfo>();
				for (TableInfo tbInfo : tbInfoLst) {
					if (tbInfo.getTableStatus() == 3) {
						if (tbZone.getZoneID() != 0) {
							if (tbZone.getZoneID() == tbInfo.getiTableZoneID()) {
								newTbInfoLst.add(tbInfo);
							}
						} else {
							newTbInfoLst.add(tbInfo);
						}
					}
				}
				
				if(newTbInfoLst.size() == 0){
					newTbInfoLst = new ArrayList<TableInfo>();
					TableInfo tbInfo = new TableInfo();
					tbInfo.setiTableID(0);
					tbInfo.setSzTableName(context.getString(R.string.no_table));
					newTbInfoLst.add(tbInfo);
				}
				
				lvTable.setAdapter(new SelectTableListAdapter(context, globalVar, newTbInfoLst, false));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
			
		});
	}

	public void setCloseButton(CharSequence text, OnClickListener listener) {
		mBtnClose.setOnClickListener(listener);
	}
	
}
