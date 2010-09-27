package com.camangi.rssreader;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class Net{
	final static String tag ="tag";
	static WifiManager wm;
	

    /**	
	 * 描述 : 檢查WIFI的連線狀態，並以Toast返回<br/>
	 * @param context 顯示Toast的Context主體
     * @return if wifi connected retun true, else teturn false;
     */
	public static boolean checkInitWifiStatus(Context context){
		wm =(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		boolean wifistatus =wm.isWifiEnabled();
//		Toast.makeText(context, "Was Wifi open? "+String.valueOf(wifistatus), Toast.LENGTH_SHORT).show();
		return wifistatus;
	}
	
	public static boolean checkEnableingWifiStatus(){
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
	public static void autoWifi(Context context){
		Log.i(tag, "into autoWifi");
		if(!checkInitWifiStatus(context)){

			wm.setWifiEnabled(false);
			wm.setWifiEnabled(true);
			
		}	
	}


}
