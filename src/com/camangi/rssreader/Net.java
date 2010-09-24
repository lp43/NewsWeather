package com.camangi.rssreader;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class Net {
	
	final static String tag ="tag";
	
	/**
	 * 描述 : 自動連線至WIFI<br/>
	 * 這個Method被引用在RssReader主程式和Widget在更新以前的WIFI連線狀態的檢查
	 * @param context 程式主體，用來顯示Toast
	 * @return 連線完成後，回傳連線狀態為True
	 */
	public static boolean autoWifi(Context context){
		WifiManager wm =(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		Toast.makeText(context, "Was Wifi open? "+String.valueOf(wm.isWifiEnabled()), Toast.LENGTH_SHORT).show();
		if(!wm.isWifiEnabled()){
//			Toast.makeText(context, "Wifi connecting...", Toast.LENGTH_SHORT).show();
			wm.setWifiEnabled(false);
/*			這段本來是要幫WS的異常關閉WIFI做處理用的
                try {
				Thread.currentThread().sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			wm.setWifiEnabled(true);
			
		}
		
		while(wm.getWifiState()==wm.WIFI_STATE_DISABLED|wm.getConnectionInfo().getIpAddress()==0){
    		try {
    			Log.i(tag, "Thread sleep: 1000");
				Thread.currentThread().sleep(1000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
		
		return wm.isWifiEnabled();
		
	}
}
