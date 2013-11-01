package com.syn.iorder;

import syn.pos.data.dao.DataBaseHelper;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.itemClose:
			finish();
			return true;
		case R.id.itemDeleteDb:
			new AlertDialog.Builder(AboutActivity.this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Clear iOrder data")
			.setMessage("Are you sure you want to clear all iOrder data?")
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			})
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DataBaseHelper dbHelper = new DataBaseHelper(AboutActivity.this);
					dbHelper.deleteDbFile();
					
					new AlertDialog.Builder(AboutActivity.this)
					.setTitle("Clear iOrder data")
					.setMessage("Clear data successfully.")
					.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(AboutActivity.this, AppConfigLayoutActivity.class);
							startActivity(intent);
							finish();
						}
					}).show();
				}
			})
			.show();
			return true;
		default:
		return super.onOptionsItemSelected(item);
		}
	}
}
