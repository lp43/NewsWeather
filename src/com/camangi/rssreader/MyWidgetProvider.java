/**
   * Copy Right Information : Camangi coporation
   * Project : RssReader
   * Android version used : SDK1.6
   * Version : 1.0024
   * Modification history :2010.9.2
   * Modified By : Simon
   */
package com.camangi.rssreader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.util.Xml;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 *程式的flow是MyWidgetProvider.onUpdate()->UpdateService.onCreate()->UpdateService.onCreate.onStart()
 *之後就變成MyWidgetProvider.onUpdate()->->UpdateService.onCreate.onStart()
 */
public class MyWidgetProvider extends AppWidgetProvider {
	
	/**專放News類的容器，幾個channel就有幾個getData，因為這個List是即時存進大容器lliAll，
	 * 所以資料一旦存進大容器，就會被下一筆channel的資料替掉*/
	private static ArrayList<News> getData;
	/**判斷xml文件編碼並儲存在Encode*/
	static String Encode; 
	/**bufferb用來存放從xml複製下來，每一行從BIG5轉成UTF-8的String空間*/
	static String bufferb;
	/**用來檢查資料庫在不在*/
	File file;
	/**將資料庫的name,path,int存到hashmap用的變數*/
	static String name;
	/**從資料庫查詢回來_open為"true"的網址,每個迴圈存一次，所以會一直被替代掉*/
	static String path;
	/**這個id是database裡的id,不一定會照順序*/
	int id;
	/**記錄頻道按鈕的排序位置,雖然是從0開始,但因為appWidget的程式流程是myWidgetProvider的onUpdate()、Service的onCreate()、onStart()。
	而在onCreate()時我就會馬上+1，所以第一筆放進大容器liAll的button_order仍是1開始，沒有變。*/
	static int button_order=0;
	/**讓Button能夠取到名字的暫存容器*/
	private static HashMap<Integer,String> namelist;
	/**將每一筆getRSS()產生的getData容器，再放入大容器裡*/
	public static HashMap<Integer,List<News>> liAll;
	/**設定logcat的tag標籤名稱*/
	final static String tag ="tag";
//	Intent intent;
	private static ActivityManager activitymanager;
	private static boolean RssliAllExist;
	/**記錄PackageName*/
	static String packageName;
	/**MyWidgetProvider專屬的更新記錄*/
	public static int updateVersion=0;	
	AlarmManager alarm;
	PendingIntent pintent;
	public static String WidgetDatabaseNum="none";
	public static HashMap<Integer,String> widget_namelist;
	
	
	
	/**
	 * 描述 : appWidget.class一啟動時先跑的method<br/>
	 * 目的是呼叫出Service
	 */
	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		Log.i(tag, "Provider_OnUpdate");
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		if(!(Net.check3GConnectStatus(context)|Net./*checkInitWifiStatus*/checkEnableingWifiStatus(context))){
			//如果沒有連線上網就啟動等待上網模式
			BackStage.startAlarmManager(context, 3);
			Log.i(tag, "BackStage.startAlarmManager(context, 3);");
		}else{
			//使用AlarmManager的方式來控制更新時間
			BackStage.startAlarmManager(context, 1);
			Log.i(tag, "BackStage.startAlarmManager(context, 1);");
		}

		


	}
	/**
	 * 描述 : Widget刪除時引用到的函式<br/>
	 * 當Home Screen的Widget刪掉後，會跑這個appWidget的這個method。
	 * 為防user再度建立該Widget時資料錯亂，
	 * 所有的設定都是將值初始化
	 */
	@Override
	public void onDisabled(Context context) {
		
		Log.i(tag, "Provider_OnDisabled");

		
		Intent intent = new Intent(context, UpdateService.class);
		AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		 PendingIntent pintent=PendingIntent.getService(context, 0, intent, 0);
		 alarm.cancel(pintent);
		 
		BackStage.widgetExist=false; 
		currentnews.news_channel=0;//頻道從0開始放
		currentnews.news_number=0;//新聞內容從0放的
		super.onDisabled(context);
	}

	public static class WaitConnect extends Service{

		
		@Override
		public void onStart(Intent intent, int startId) {
			packageName=this.getPackageName();
			if(!(Net.check3GConnectStatus(this)|Net./*checkInitWifiStatus*/checkEnableingWifiStatus(this))){
				Log.i(tag, "Test.service if");
				RemoteViews updateViews = new RemoteViews(packageName,R.layout.widget);
				updateViews.setTextViewText(R.id.widgetContent, getString(R.string.click_to_wifi));
				updateViews.setTextViewText(R.id.widgetSource, getString(R.string.cant_detect_internet));

				Log.i(tag, "updateViews.set finish");
				//按Widget後會發生的事
				intent = new Intent(this, Net.class);  
				PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);  
				updateViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
				
				Log.i(tag, "setOnClick finish");
				//讓Widget能更新的基本程式
				ComponentName thisWidget = new ComponentName(this, MyWidgetProvider.class);
				AppWidgetManager manager = AppWidgetManager.getInstance(this);
			    manager.updateAppWidget(thisWidget, updateViews);
				Log.i(tag, "manager finish");
			}else{
				BackStage.startAlarmManager(this, 2);
				BackStage.startAlarmManager(this, 1);
			}
			super.onStart(intent, startId);
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}
		
	}

	public static class UpdateService extends Service {
		MyWidgetProvider.mReceiver Rreceiver1;


		/**
		 * 描述 : Service服務第1次運行跑的第1個函式<br/>
		 * Service會先跑onCreate()。
		 * 當程式已經是第2次更新，就不會跑onCreate()而直接跑onStart()
		 */
		@Override
		public void onCreate() {
			//如果wifi和3g都沒有連線，就讓widget進入等待連線模式
			while(!(Net.checkEnableingWifiStatus(this)|Net.check3GConnectStatus(this)|Net.checkEnableingWifiStatus(this))){
				BackStage.letThreadSleep(1000);
				Log.i(tag, "waiting for connect...");
			}
			
			
			Log.i(tag, "into UpdateService.OnCreate()");
			packageName=this.getPackageName();
			liAll = new HashMap<Integer,List<News>>();
			widget_namelist = new HashMap<Integer, String>();
			widget_namelist.clear();
			liAll.clear();
			

			currentnews.news_channel=0;//頻道從0開始放
			currentnews.news_number=0;//新聞內容從0放的
			
			if(!checkRssliAllExist()){
				
				Log.i(tag, "<MyWidgetProvider$UpdateService>.onCreate(): Because checkRssReaderExist= "+String.valueOf(checkRssliAllExist())+", into BackStage.immedParseData()");
				BackStage bs =new BackStage();
				bs.immedParseData(UpdateService.this);
				
			}else{
				Log.i(tag, "<MyWidgetProvider$UpdateService>.onCreate(): Because checkRssReaderExist= "+String.valueOf(checkRssliAllExist())+", load Data directly");
				MyWidgetProvider.liAll=BackStage.liAll;
				MyWidgetProvider.widget_namelist=BackStage.backstage_widget_namelist;
			}

			Log.i(tag, "MyWidgetProvider$UpdateService.initialize() finish");
			super.onCreate();
		}

		/**
		 * 描述 : Service服務第1次運行跑的第2個函式，但也是第2次跑的第1個函式<br/>
		 * Service會先跑onCreate()再跑onStart()。
		 * 當程式已經是第2次更新，就不會跑onCreate()而直接跑onStart()
		 */
		@Override
		public void onStart(Intent intent, int startId) {
			
			
			super.onStart(intent, startId);
			Log.i(tag, "into MyWidgetProvider$UpdateService.onStart()");		
		
			BackStage.widgetExist=true;

			try{//為防解析出空值的getData實體,包住這段的Exception
				Log.i(tag, "UpdateService.OnStart(), "+"NOW_channel_is:"+widget_namelist.get(currentnews.news_channel)+"..."+(currentnews.news_number+1)+"/"+((liAll.get(currentnews.news_channel).size())-1));
				currentnews.content=MyWidgetProvider.liAll.get(currentnews.news_channel).get(currentnews.news_number).getTitle();	
				currentnews.source=widget_namelist.get(currentnews.news_channel);					
			}catch(IndexOutOfBoundsException e){
				Log.i(tag, "into IndexOutOfBoundsException");
				currentnews.news_number=0;
				currentnews.news_channel++;
			}
				
			int channelTotal=MyWidgetProvider.liAll.size();//算出大容器liAll的總頻道數
			int newsTotal=MyWidgetProvider.liAll.get(currentnews.news_channel).size();//算出指定的小容器getData的總新聞數
			
		
					//輪播新聞的計算公式
						if(newsTotal>currentnews.news_number){
							currentnews.news_number++;
							if(currentnews.news_number==newsTotal){
								currentnews.news_number=0;
								currentnews.news_channel++;
								if(currentnews.news_channel==channelTotal){
									currentnews.news_number=0;
									currentnews.news_channel=0;
								}
							}
						}

						
					RemoteViews updateViews = new RemoteViews(packageName,R.layout.widget);
					updateViews.setTextViewText(R.id.widgetContent, currentnews.content);
					updateViews.setTextViewText(R.id.widgetSource, currentnews.source+"..."+String.valueOf(currentnews.news_number)+"/"+String.valueOf(newsTotal-1));
					
			
			//點下Widget可以進入APK
			intent = new Intent(this, RssReader.class);  
			PendingIntent pendingIntent = PendingIntent.getActivity(this,0 /* no requestCode */, intent, 0 /* no flags */);  
			updateViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
			

			//讓Widget能更新的基本程式
			ComponentName thisWidget = new ComponentName(this, MyWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
		    manager.updateAppWidget(thisWidget, updateViews);
		    
		   

//		    Log.i(tag,"MyWidgetProvider.UpdateService.onStart() finish");
		}
		
		
		public boolean checkRssliAllExist(){
			RssliAllExist=false;
			//從Task清單裡去查明有開啟Widget，就將AppWidgetExist設為True，以成為之後複製檔案的判斷條件
		    activitymanager=(ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
//			Log.i(tag, "getSystemService finish");
			List<ActivityManager.RunningTaskInfo> a=activitymanager.getRunningTasks(10);
//			Log.i(tag, "getRunningTasks finish");
			for(ActivityManager.RunningTaskInfo j:a){
//				Log.i(tag, "intoFor-loop");
				if(j.baseActivity.getClassName().equals(packageName+".RssReader")){
					if(BackStage.liAll!=null){
						RssliAllExist=true;
						MyWidgetProvider.WidgetDatabaseNum=BackStage.checkDatabaseNumber(this);
					}
//					Log.i(tag, "getClassName finish");
				}
			}
			return RssliAllExist;	
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
			
		}
		
		
	
	  }
	
	/**
	 * 描述 : 放現在播放的新聞的值，好讓大小容器在取資料時能夠替換。<br/>
	 * 為了讓appWidget(Reciever)的onUpdate()和UpdateService(Service)的onCreate()和onStart()不會干擾每一次廣播後的數字加總，
	 * 特別開了這個類才存放要播放的新聞的值，每一次都會加1來輪播。
	 * @param news_channel 現在的頻道，預設從1開始播放(也是liAll這個hashMap的第1筆索引值(沒有0索引,為了和主程式相對應))
	 * @param news_number 現在頻道裡的新聞，預設從0開始播放(getData實體是從0開始存放的關係)
	 * @param content 用來存放新聞的標題內容
	 * @param source 用來存放該筆資料的新聞來源
	 */
	public static class currentnews{	
		static int news_channel=0;//頻道從0開始放
		static int news_number=0;//新聞內容從0放的
		static String content,source;
	}
	
	
	/**
	 * 描述 ： 此靜態類別專收從主程式寄來的廣播<br/>
	 * 一旦收到了廣播，馬上將Service停止，然後將currentnews.news_channel，currentnews.news_number，button_order恢復為預設狀態，
	 *  最後再將MyWidgetProvider.updateVersion的版本改成和後臺的版本一致
     */
	public static class mReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
		
			
			
			//當收到廣播時，將Service停止;
			int status =intent.getExtras().getInt("status");
			
			Log.i(tag, ">==MyWidgetProvider.mReceiver.onReceive(), status is: "+status);
			
			
			switch(status){
				case 0:	 //status=0時，停止update widget
					Log.i(tag, "stop AppWidgetProvier$UpdateService");
					BackStage.startAlarmManager(context, 0);
					break;
					
				case 1:  //status=1時，恢復update widget
					
					if(BackStage.widgetExist){
						Log.i(tag, "BackStage.widgetExist= "+BackStage.widgetExist+", start AppWidgetProvier$UpdateService");
						BackStage.startAlarmManager(context, 1);
					}else{
						Log.i(tag, "BackStage.widgetExist= "+BackStage.widgetExist+", ignore BroadCastReceiver");
					}
					break;
			}
			
			 
			//將值恢復預設
			currentnews.news_channel=0;
			currentnews.news_number=0;
			button_order=0;

			
			Log.i(tag, "=====================================");
		}
		
	}
		

}
