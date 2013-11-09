package syn.pos.mobile.iordertab;

import org.ksoap2.serialization.PropertyInfo;

import syn.pos.data.json.GsonDeserialze;
import syn.pos.data.model.ProductGroups;
import syn.pos.data.model.WebServiceResult;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class CurrentAnswerQuestionTask extends WebServiceTask{
	private static final String method = "WSiOrder_JSON_GetCurrentAnswerQuestion";
	private Gson gson;
	private ICurrentAnswerListener iCurrAns;
	public CurrentAnswerQuestionTask(Context c, GlobalVar gb, int tableId, ICurrentAnswerListener iCurrAnswer) {
		super(c, gb, method);
		gson = new Gson();
		iCurrAns = iCurrAnswer;
		
		PropertyInfo property = new PropertyInfo();
		property.setName("iComputerID");
		property.setValue(GlobalVar.COMPUTER_ID);
		property.setType(int.class);
		soapRequest.addProperty(property);
		
		property = new PropertyInfo();
		property.setName("iStaffID");
		property.setValue(GlobalVar.STAFF_ID);
		property.setType(int.class);
		soapRequest.addProperty(property);
		
		property = new PropertyInfo();
		property.setName("iTableID");
		property.setValue(tableId);
		property.setType(int.class);
		soapRequest.addProperty(property);
	}

	@Override
	protected void onPostExecute(String result) {
		if(progress.isShowing())
			progress.dismiss();
		
		GsonDeserialze gdz = new GsonDeserialze();
		try {
			WebServiceResult wsResult = gdz.deserializeWsResultJSON(result);

			Type type = new TypeToken<List<ProductGroups.QuestionAnswerData>>() {}.getType();
			List<ProductGroups.QuestionAnswerData> questionLst = 
					new ArrayList<ProductGroups.QuestionAnswerData>();
					
			if(wsResult.getiResultID() == 0){
				questionLst = gson.fromJson(wsResult.getSzResultData(), type);
			}else{
				new AlertDialog.Builder(context)
				.setTitle(R.string.error)
				.setMessage(wsResult.getSzResultData())
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				})
				.show();
			}
			iCurrAns.listQuestionAnswer(questionLst);
		} catch (Exception e) {
			new AlertDialog.Builder(context)
			.setTitle(R.string.error)
			.setMessage(result)
			.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			})
			.show();
			e.printStackTrace();
		}

	}
	
	public static interface ICurrentAnswerListener{
		public void listQuestionAnswer(List<ProductGroups.QuestionAnswerData> questionLst);
	}
}
