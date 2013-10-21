package com.syn.iorder;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

public class SetMemberFromMain extends SearchMemberActivity{
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_menu_set, menu);
		View v = menu.findItem(R.id.item_confirm).getActionView();
		
		btnSearchMemberCancel = (Button) v.findViewById(R.id.buttonConfirmCancel);
		btnSearchMemberOk = (Button) v.findViewById(R.id.buttonConfirmOk);
		
		btnSearchMemberCancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(
						txtMemberCode.getWindowToken(), 0);
				
				SetMemberFromMain.this.finish();
			}
			
		});
		btnSearchMemberOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// set to local trnasaction
				globalVar.MEMBER_ID = MEMBER_ID;
				globalVar.MEMBER_NAME = MEMBER_NAME;
				
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(
						txtMemberCode.getWindowToken(), 0);
				
				SetMemberFromMain.this.finish();
			}
			
		});

		btnSearchMemberOk.setEnabled(false);
		return true;
	}

}
