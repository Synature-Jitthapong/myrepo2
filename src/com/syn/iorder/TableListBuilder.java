package com.syn.iorder;

import java.util.ArrayList;
import java.util.List;

import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableInfo.TableName;
import syn.pos.data.model.TableInfo.TableZone;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
	
	public TableListBuilder(final Context context, final GlobalVar globalVar, final TableInfo tbInfo) {
		super(context);	
		
		LayoutInflater inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View closeTableView = inflater.inflate(R.layout.close_table_layout, null);
		final ListView lvTable = (ListView) closeTableView.findViewById(R.id.lvTable);
		Spinner spTableZone = (Spinner) closeTableView.findViewById(R.id.spTableZone);
		mBtnClose = (Button) closeTableView.findViewById(R.id.btnClose);
		setView(closeTableView);
		
		spTableZone.setAdapter(IOrderUtility.createTableZoneAdapter(context, tbInfo));
		spTableZone.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				
				TableZone tbZone = (TableZone) parent.getItemAtPosition(position);
				List<TableName> tbNameLst = new ArrayList<TableName>();
				for (TableName tbName : tbInfo.TableName) {
					if (tbName.getTableStatus() == 3) {
						if (tbZone.getZoneID() != 0) {
							if (tbZone.getZoneID() == tbName.getZoneID()) {
								tbNameLst.add(tbName);
							}
						} else {
							tbNameLst.add(tbName);
						}
					}
				}
				
				if(tbNameLst.size() == 0){
					tbInfo.TableName = new ArrayList<TableInfo.TableName>();
					TableInfo.TableName tbName = new TableInfo.TableName();
					tbName.setTableID(0);
					tbName.setTableName(context.getString(R.string.no_table));
					tbNameLst.add(tbName);
				}
				
				lvTable.setAdapter(new SelectTableListAdapter(context, globalVar, tbNameLst, false));
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
