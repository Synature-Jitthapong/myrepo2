package com.syn.iorder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class NetworkUtility {
	public static boolean isOnline(Context c) {
	    ConnectivityManager cm =
	        (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	public static int ping(String url){
		int httpCode = 0;
	    HttpURLConnection connection = null;
	    try {
	        URL u = new URL(url);
	        connection = (HttpURLConnection) u.openConnection();
	        connection.setRequestMethod("HEAD");
	        connection.connect();
	        httpCode = connection.getResponseCode();
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if (connection != null) {
	            connection.disconnect();
	        }
	    }
	    return httpCode;
	}
}
