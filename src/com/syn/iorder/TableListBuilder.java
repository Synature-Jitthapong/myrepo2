package com.syn.iorder;

import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableInfo.TableZone;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.Spinner;

public class TableListBuilder extends AlertDialog.Builder{

	public TableListBuilder(final Context context, final GlobalVar globalVar, final TableInfo tbInfo) {
		super(context);	
		
		LayoutInflater inflater = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View closeTableView = inflater.inflate(R.layout.close_table_layout, null);
		final ListView lvTable = (ListView) closeTableView.findViewById(R.id.lvTable);
		Spinner spTableZone = (Spinner) closeTableView.findViewById(R.id.spTableZone);
		setView(closeTableView);
		
		spTableZone.setAdapter(IOrderUtility.createTableZoneAdapter(context, tbInfo));
		spTableZone.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				
				TableZone tbZone = (TableZone) parent.getItemAtPosition(position);
				lvTable.setAdapter(new SelectTableListAdapter(context, globalVar, 
						IOrderUtility.filterCloseTableName(tbInfo, tbZone)));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
			
		});
	}
}
