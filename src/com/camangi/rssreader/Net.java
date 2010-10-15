package com.camangi.rssreader;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class Net extends Service{

	final static String tag ="tag";
	static WifiManager wm;
	static ConnectivityManager cm;
	
	
	@Override
	public void onCreate() {
		if(!(Net.check3GConnectStatus(Net.this)|Net./*checkInitWifiStatus*/checkEnableingWifiStatus(Net.this))){
			switchWifi(Net.this,true);
		}
	
		super.onCreate();
	}

	
	/**
	 * 描述 : 檢查IP位址來確認連線狀態<br/>
	 * @return 如果有IP位址，返回true
	 */
	public static boolean check3GConnectStatus(Context context){
		boolean net3g_status=false;
		
		cm= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni=cm.getActiveNetworkInfo();
		Log.i(tag, "NetworkInfo status: "+cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState());
		if(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState()==NetworkInfo.State.DISCONNECTED|cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState()==NetworkInfo.State.UNKNOWN){		
			net3g_status=false;
		}else{
			net3g_status=true;
		}
		return net3g_status;
	}

	
    /**	
	 * 描述 : 檢查WIFI的連線狀態，並以Toast返回<br/>
	 * @param context 顯示Toast的Context主體
     * @return if wifi connected retun true, else teturn false;
     */
//	public static boolean checkInitWifiStatus(Context context){
//		wm =(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//		boolean wifistatus =wm.isWifiEnabled();
//		Log.i(tag, "WIFI status: "+wifistatus);
//		return wifistatus;
//	}
	
	public static boolean checkEnableingWifiStatus(Context context){
		wm =(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if(wm.getWifiState()==wm.WIFI_STATE_DISABLED|wm.getConnectionInfo().getIpAddress()==0){
			return false;
		}else{
			return true;
		}	
	}
	
	
	/**
	 * 描述 : 自動連線至WIFI<br/>
	 * 這個Method被引用在RssReader主程式和Widget在更新以前的WIFI連線狀態的檢查
	 * @param context 程式主體，用來顯示Toast
	 * @return 連線完成後，回傳連線狀態為True
	 */
	public static void switchWifi(Context context,boolean open){
		Log.i(tag, "into autoWifi");
//		if(!/*checkInitWifiStatus*/checkEnableingWifiStatus(context)){
//
//			wm.setWifiEnabled(true);
//			
//		}	
		if(open==true){
			if(!/*checkInitWifiStatus*/checkEnableingWifiStatus(context)){
			wm.setWifiEnabled(true);
			}
		}else{
			wm.setWifiEnabled(false);
		}
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}


}
