package com.syn.iorder;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import syn.pos.data.model.TableName;
import android.content.Context;

public class LoadAllTableV1 extends WebServiceTask{
	public static final String LOAD_TABLE_V1_METHOD = "WSmPOS_JSON_LoadAllTableData";
	
	private LoadTableProgress mListener;
	
	public LoadAllTableV1(Context c, GlobalVar gb, LoadTableProgress listener) {
		super(c, gb, LOAD_TABLE_V1_METHOD);
		mListener = listener;
	}

	@Override
	protected void onPreExecute() {
//		progress.setMessage(context.getString(R.string.load_table_progress));
//		progress.show();
		mListener.onPre();
	}

	@Override
	protected void onPostExecute(String result) {
//		if(progress.isShowing())
//			progress.dismiss();
		
		Gson gson = new Gson();
		Type type = new TypeToken<TableName>(){}.getType();
		try {
			TableName tbName = gson.fromJson(result, type);

			TableName.TableZone tbZone = new TableName.TableZone();
			tbZone.setZoneID(0);
			tbZone.setZoneName("All Zone");
			tbName.TableZone.add(0, tbZone);
			
			mListener.onPost(tbName);
		} catch (JsonSyntaxException e) {
			mListener.onError(result);
		}
	}

	public static interface LoadTableProgress extends OnProgressListener{
		void onPost(TableName tbName);
	}
}
