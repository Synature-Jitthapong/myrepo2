package com.syn.iorder;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings.Secure;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import syn.pos.data.dao.ProgramFeature;
import syn.pos.data.dao.ShowMenuColumnName;
import syn.pos.data.model.ShopData;
import syn.pos.data.model.ShopData.ComputerProperty;
import syn.pos.data.model.ShopData.GlobalProperty;
import syn.pos.data.model.ShopData.ShopProperty;
import syn.pos.data.model.TableInfo;
import syn.pos.data.model.TableName;

public class GlobalVar {
	public static final String PREF_LANG = "pref_lang";
	public static final String PREF_REASON_CONFIG = "pref_reason_config";
	public static final String KEY_PREF_REQ_REASON_MOVE_TAฺBLE = "ask_when_move_table";
	public static final String KEY_PREF_REQ_REASON_REPRINT_TRANS = "ask_when_reprint_trans";
	public static final String KEY_PREF_REQ_REASON_VOID_TRANS = "ask_when_void_trans";
	public static final String KEY_PREF_REQ_REASON_VOID_ORDER = "ask_when_void_order";
	public static final String KEY_PREF_REQ_REASON_PRINT_BILL_DETAIL = "ask_when_print_bill_detail";
	
	public static boolean sIsEnableChecker = false;
	public static boolean sIsEnableCallCheckBill = false;
	public static boolean sIsEnablePrintLongBill = false;
	public static boolean sIsEnableTableQuestion = false;
	public static boolean sIsEnableQueue = false;
	public static boolean sIsEnableSeat = false;
	public static boolean sIsEnableSaleMode = false;
	public static boolean sIsEnableCourse = false;
	public static boolean sIsAddOnlyOneItem = false;
	public static boolean sIsCalculateDiscount = false;
	public static boolean sIsEnableGenQueueWithSelectPrinter = false;
	public static boolean sIsEnableMaxCharFormergeTable = false;
	public static boolean sIsEnableCallCheckbillPayCashDetail = false;
	public static boolean sIsEnableBuffetType = false;
	public static boolean sIsEnableFastFoodMapTable = false;
	public static boolean sIsAddSameItem = false;
	public static boolean sIsPopupWhenTableNotEmpty = false;
	public static boolean sIsLockWhenPrintLongbill = false;

	public static int STAFF_ID = 0;
	public static String STAFF_NAME = "";
	public static String CURRENCY_SYMBOL = "";

	public static int TRANSACTION_ID;
	public static int COMPUTER_ID;
	public static int SHOP_ID;
	public static int MEMBER_ID;
	public static String MEMBER_NAME = "";

	public static String SERVER_IP = "http://1.1.0.101/";
	public static String WS_NAME = "ws_mpos.asmx";
	public static String WS_URL = "webservice_mpos11/";
	private static String IMG_PATH = "/Resources/Shop/MenuImage/";
	private static String WELCOME_IMG_PATH = "Resources/Shop/WelcomeImage/";
	public static String FULL_URL = SERVER_IP + WS_URL;
	public static String IMG_URL = SERVER_IP + WS_URL + IMG_PATH;
	public static String WELCOME_IMG_URL = SERVER_IP + WS_URL
			+ WELCOME_IMG_PATH;
	public static String WS_NAMESPACE = "http://tempuri.org/";
	public static int DISPLAY_MENU_IMG = 0;
	public static int SHOW_MENU_COLUMN = 1;
	
	public ShopProperty SHOP_DATA;
	public List<ShopData.ProgramFeature> PROGRAM_FEATURE;
	public GlobalProperty GLOBAL_PROP;
	public ComputerProperty COMPUTER_DATA;
	public DecimalFormat decimalFormat;
	public DecimalFormat qtyFormat;
	public DecimalFormat qtyDecimalFormat;
	public SimpleDateFormat mTimeFormat;
	static TableInfo TBINFO = null;

	public static TableName sTbName;
	
	public GlobalVar(Context context) {
		syn.pos.data.dao.ComputerProperty comProp = new syn.pos.data.dao.ComputerProperty(
				context);

		syn.pos.data.dao.ShopProperty sp = new syn.pos.data.dao.ShopProperty(
				context, null);
		SHOP_DATA = sp.getShopProper();
		SHOP_ID = SHOP_DATA.getShopID();

		ProgramFeature feature = new ProgramFeature(context);
		PROGRAM_FEATURE = feature.listProgramFeature();

		syn.pos.data.dao.GlobalProperty globalProp = new syn.pos.data.dao.GlobalProperty(
				context, null);

		String decimalPatern = globalProp.getGlobalProperty()
				.getCurrencyFormat() == null ? "#,###.00" : globalProp
				.getGlobalProperty().getCurrencyFormat();
		String qtyPatern = globalProp.getGlobalProperty().getQtyFormat() == null ? "#,###"
				: globalProp.getGlobalProperty().getQtyFormat();
		String qtyDecimalPatern = "#,###.####";
		String timeFormat = globalProp.getGlobalProperty().getTimeFormat() == null ? "HH:mm:ss" :
				globalProp.getGlobalProperty().getTimeFormat().equals("") ? 
				"HH:mm:ss" : globalProp.getGlobalProperty().getTimeFormat();

		decimalFormat = new DecimalFormat(decimalPatern);
		qtyFormat = new DecimalFormat(qtyPatern);
		qtyDecimalFormat = new DecimalFormat(qtyDecimalPatern);
		mTimeFormat = new SimpleDateFormat(timeFormat, Locale.getDefault());

		GLOBAL_PROP = globalProp.getGlobalProperty();

		String currencyCodeISO4217 = "THB";
		Currency currency = Currency.getInstance(currencyCodeISO4217);
		CURRENCY_SYMBOL = currency.getSymbol();// globalProp.getGlobalProperty().getCurrencyCode();

		COMPUTER_DATA = comProp.getComputerData();
		COMPUTER_ID = COMPUTER_DATA.getComputerID();

		String deviceCode = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
		COMPUTER_DATA.setDeviceCode(deviceCode);
		// COMPUTER_DATA.setDeviceCode("2e8752a898cb3c94");

		syn.pos.data.dao.AppConfig setting = new syn.pos.data.dao.AppConfig(
				context);

		syn.pos.data.model.AppConfigModel appModel = setting.getConfigs();
		SERVER_IP = "http://" + appModel.getServerIP() + "/";
		WS_URL = appModel.getWebServiceUrl() + "/ws_mpos.asmx";
		FULL_URL = SERVER_IP + WS_URL;
		IMG_URL = SERVER_IP + appModel.getWebServiceUrl() + IMG_PATH;
		WELCOME_IMG_URL = SERVER_IP + appModel.getWebServiceUrl()
				+ WELCOME_IMG_PATH;
		DISPLAY_MENU_IMG = appModel.getDisplayImageMenu();

		ShowMenuColumnName showColName = new ShowMenuColumnName(context);
		SHOW_MENU_COLUMN = showColName.getShowMenuColumn();
	}
	
	public static void hideKeyboard(Context c, EditText edt){
		InputMethodManager imm = (InputMethodManager) c.getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);	
	}
	
	public static String getDeviceCode(Context context){
		return Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
	}
	
	public static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
}
