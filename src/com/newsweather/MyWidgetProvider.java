/**
   * Copy Right Information : Camangi coporation
   * Project : RssReader
   * Android version used : SDK1.6
   * Version : 1.0024
   * Modification history :2010.9.2
   * Modified By : Simon
   */
package com.newsweather;

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
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.util.Xml;
import android.widget.RemoteViews;

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
	private DB myDB;
	private static Cursor cursor;
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
	Intent intent;
	static BufferedWriter ProgramWithWIFI;
	/**記錄PackageName*/
	static String packageName;
	static ActivityManager  m;
	static boolean NewsWeatherExist;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(tag, "Provider_OnReceive");
		super.onReceive(context, intent);
//		ArrayList n=intent.getExtras().getParcelableArrayList("1");
//		Log.i(tag, "getExtra"+n.get(0));
	}
	
	
	
	/**
	 * 描述 : appWidget.class一啟動時先跑的method<br/>
	 * 目的是呼叫出Service
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.i(tag, "Provider_OnUpdate");	
		intent = new Intent(context, UpdateService.class);
	    context.startService(intent);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	

	
	
	/**
	 * 描述 : Widget刪除時引用到的函式<br/>
	 * 當Home Screen的Widget刪掉後，會跑這個appWidget的這個method。
	 * 為防user再度建立該Widget時資料錯亂，
	 * 所有的設定都是將值初始化
	 */
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.i(tag, "Provider_OnDisabled");
		namelist.clear();
		liAll.clear();
		intent = new Intent(context, UpdateService.class);
		context.stopService(intent);
		button_order=0;
		currentnews.news_channel=1;//頻道從1開始放
		currentnews.news_number=0;//新聞內容從0放的
	}




	public static class UpdateService extends Service {

		/**
		 * 描述 : Service服務第1次運行跑的第1個函式<br/>
		 * Service會先跑onCreate()。
		 * 當程式已經是第2次更新，就不會跑onCreate()而直接跑onStart()
		 */
		@Override
		public void onCreate() {
			Log.i(tag, "Service_OnCreate");
			packageName=this.getPackageName();
			Log.i(tag, "packageName: "+packageName);
			
		
			m=(ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
			Log.i(tag, "getSystemService finish");
			List<ActivityManager.RunningTaskInfo> a=m.getRunningTasks(10);
			Log.i(tag, "getRunningTasks finish");
			for(ActivityManager.RunningTaskInfo j:a){
				Log.i(tag, "intoFor-loop");
				if(j.baseActivity.getClassName().equals(packageName+".NewsWeather")){
					NewsWeatherExist=true;
					Log.i(tag, "getClassName finish");
				}
			}
			
			//把所有RSS資料都載入進來
			initialize(this);
			Log.i(tag, "initialize_finish");
			super.onCreate();
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
			
		}
		
		
		/**
		 * 描述 : Service服務第1次運行跑的第2個函式，但也是第2次跑的第1個函式<br/>
		 * Service會先跑onCreate()再跑onStart()。
		 * 當程式已經是第2次更新，就不會跑onCreate()而直接跑onStart()
		 */
		@Override
		public void onStart(Intent intent, int startId) {
			
			Log.i(tag, "Service_OnStart, "+"NOW_channel_is:"+namelist.get(currentnews.news_channel));
			super.onStart(intent, startId);
			
			currentnews.content=liAll.get(currentnews.news_channel).get(currentnews.news_number).getTitle();	
			currentnews.source=namelist.get(currentnews.news_channel);
			
			int channelTotal=liAll.size();//算出大容器liAll的總頻道數
			int newsTotal=liAll.get(currentnews.news_channel).size();//算出指定的小容器getData的總新聞數
			
		
			//輪播新聞公式
			if(currentnews.news_number<newsTotal-1/*小容器從0開始放,若共有3筆,最後一筆索引值會是2*/){
				currentnews.news_number++;
			}else{
				if(currentnews.news_channel>=channelTotal){
					currentnews.news_channel=1;
					currentnews.news_number=0;
				}else{
					currentnews.news_channel++;
					currentnews.news_number=0;	
				}
				
			}		

	
			RemoteViews updateViews = new RemoteViews(packageName,
			          R.layout.widget);
			updateViews.setTextViewText(R.id.widgetContent, currentnews.content);
			updateViews.setTextViewText(R.id.widgetSource, currentnews.source);
			
			//點下Widget可以進入APK
			intent = new Intent(this, NewsWeather.class);  
			PendingIntent pendingIntent = PendingIntent.getActivity(this,0 /* no requestCode */, intent, 0 /* no flags */);  
			updateViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
			
			
			ComponentName thisWidget = new ComponentName(this, MyWidgetProvider.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
		    manager.updateAppWidget(thisWidget, updateViews);		
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
		static int news_channel=1;//頻道從1開始放
		static int news_number=0;//新聞內容從0放的
		static String content,source;
	}
	
	
    /**
     * 描述︰檢查新聞來源的原本編碼 <br/>
     * 因XML無法解析BIG5，會出現paraexception(not-well formed(invalid tocken))，
     * 所以只要網址一進來，設定將資料轉存到utf-8的buffxml(列表編號(從1開始)).xml檔裡，
     * 這個method目的為判別資料來源的編碼格式，
     * 因為JAVA要轉碼，必須先給定初始格式，才可轉檔。
     * @param path 傳進來的Rss來源網址
     * @see encodeTransfer(String path)
     * @see getRss()
     */
	 private static void checkEncode(String path){//判斷此xml的格式
	    	URL url = null;
	    	String encode="";
	    	int a,b;
	    	 try {
				   url = new URL(path);

				   InputStream is = url.openConnection().getInputStream();
				   InputStreamReader isr = new InputStreamReader(is);
				   BufferedReader br = new BufferedReader(isr);
				   String buffera = br.readLine();
				   br.close();
				   a=buffera.indexOf("\"", 25)+1;
				   b=buffera.indexOf("\"", a+1);
				   encode = buffera.substring(a, b);
				   Log.i(tag, "checkEncode_finish");
	    	 }catch (Exception e) {
					Log.i("Exception+", e.getMessage());	
					
	 		} 
	    	 
		    	   if(encode.equals("big5")|encode.equals("BIG5")){
		    		 Encode ="BIG5";  
				   }else if(encode.equals("utf-8")|encode.equals("UTF-8")|encode.equals("Utf-8")){
					 Encode ="UTF-8";
				   }	
		    	   
	    }
	    
	    
	    
	    /**
	     * 描述 : encodeTransfer() 將新聞來源轉檔成UTF-8型態的XML檔 <br/>
	     * 因XML無法解析BIG5，會出現paraexception(not-well formed(invalid tocken))
	     * 所以只要網址一進來，一定存到utf-8的buffxml(列表編號(從1開始)).xml檔裡
	     * @param path 傳進來的Rss來源網址
	     * @see checkEncode(String path)
	     * @see getRss()
	     */
	    private static void encodeTransfer(String path) {
	    	
	    	  URL url = null;
	    	  String buffera="";
				   try {
					   Log.i(tag,"TransferToXML: "+button_order);
					   url = new URL(path);
					   InputStream is = url.openConnection().getInputStream();
					   InputStreamReader isr = new InputStreamReader(is,Encode);
					   BufferedReader br = new BufferedReader(isr);
					   FileOutputStream fos = new FileOutputStream(Environment.getDataDirectory().getPath()+"/data/"+packageName+"/files/buffxml"+button_order+".xml");					   
					   
					   do{
						   buffera = br.readLine();
						   if(buffera!=null){
						   bufferb = new String(buffera.getBytes(),"UTF-8");
						   fos.write(bufferb.getBytes());
						   fos.write('\r');
						   }else{/*else這段避免XML原文最下面有一行空白行，卻還要for.write(b.getBytes())給出值的冏境
						       導致造成NullPointerException*/
						   bufferb="";
					   }
						   
					   } while(buffera !=null);
						  
					   
					    fos.flush();			    
					    fos.close();
					}  catch (UnsupportedEncodingException e) {
					Log.i("unsupportex", e.getMessage());
				} catch (FileNotFoundException e) {
					Log.i("FileNotFoundException", e.getMessage());
				}   catch (IOException e) {
					Log.i("IOException+", e.getMessage());
			} 
					
				Log.i(tag,"big5TOutf8_finish");
		}
	




		
	    /**
	     * 描述 : 使用解析器將XML轉成List容器 getData<br/>
	     * getRss()這個method最主要將存成xml的檔案再轉成hashMap去存放，
	     * 好讓之後的顯示和連結都能用get(索引)去控制每筆新聞連結
	     * @see checkEncode(String path)
	     * @see encodeTransfer(String path)
	     */
		private static void getRss(){

			try{
				
				//使用android解析器
				MyHandler myHandler = new MyHandler();
			
		
				FileInputStream fis = new FileInputStream(Environment.getDataDirectory().getPath()+"/data/"+packageName+"/files/buffxml"+button_order+".xml");
				android.util.Xml.parse(fis, Xml.Encoding.UTF_8, myHandler);
//				Log.i(tag,"parse_pass");
				//取得RSS標題與內容列表
				getData = new ArrayList<News>();
				getData = (ArrayList<News>) myHandler.getParasedData();
				Log.i(tag,"XMLtoGetData_finish");
			}catch(Exception e){
				Log.i("tag", "wrong! "+e.getMessage());
			}
		}
	
		
		
		/**
		 * 描述 : 為防初始無資料的基本設定<br/>
		 * 因為存放網址的資料庫是程式啟動後才開啟表格並寫入的，
		 * 為了怕使用者第1次使用沒有資料庫，必須將資料庫建立
		 * 若已有資料庫，則不會寫入。
		 * 另外，在這個method裡執行checkEncode(path),encodeTransfer(path),getRss()三個method，
		 * 將來源網址轉成名為getData的HashMap實體，
		 * 好讓之後要取得各筆新聞資料時，能用HashMap.get(index)才輕鬆存取資料。
		 * @param context 程式主體
		 */
		//獲取RSS資料,讓大容器小容器都有資料
		private static void initialize(Context context){
			Log.i(tag, "into initialize");
			File file = new File(Environment.getDataDirectory().getPath()+"/data/"+packageName+"/databases/database.db");
			DB myDB= new DB(context);
			namelist = new HashMap<Integer,String>();
			liAll=new HashMap<Integer,List<News>>();
			Log.i(tag, "namelist,liAll finish");
			
			 if(!file.exists()){//取得預設的新聞資料
			      myDB.insert("yahoo", "http://tw.news.yahoo.com/rss/realtime",true);//雅虎UTF-8	
			      myDB.insert("天下雜誌", "http://www.cw.com.tw/RSS/cw_content.xml",true);//天下雜誌BIG5
			      myDB.insert("中時", "http://rss.chinatimes.com/rss/focus-u.rss",true);//中時UTF-8
			      myDB.insert("公路總局", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",true);//交通部公路總局UTF8
			      myDB.insert("蘋果日報", "http://tw.nextmedia.com/rss/create/type/1077",false);//蘋果utf8
			      myDB.insert("明報", "http://inews.mingpao.com/rss/INews/gb.xml",false);//明報BIG5
			      myDB.insert("台大圖書館", "http://www.lib.ntu.edu.tw/rss/newsrss.xml",false);//台灣大學圖書館UTF8
			      myDB.insert("台東大圖書館", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",false);//台東大學圖書館BIG5
				
		}

			 Log.i(tag, "myDB insert finish");
			 Log.i(tag, "NewsWeatherExist: " + String.valueOf(NewsWeatherExist));
			 if(NewsWeatherExist){//如果Android程序中的NewsWeather有被開啟
				 if(NewsWeather.liAll.get(1)!=null){//如果NewsWeather.liAll的第1筆也有值,才將liAll實體拷貝到Widget這裡用
					 Log.i(tag, "CHECK NewsWeather.liAll is null?: "+String.valueOf(NewsWeather.liAll.get(1)==null));
					 //之所以用get(1)來判斷，是因為如果只用NewsWeather.liAll判斷，易發生誤判有實體卻沒內容
						 cursor=myDB.getTruePath();
						 while(cursor.moveToNext()){
								//將資料庫內的內容取出放到Button上
								name=cursor.getString(cursor.getColumnIndex("_name"));
								path=cursor.getString(cursor.getColumnIndex("_path"));
							
								button_order++;
								Log.i(tag, "In_Unull_Cursor_loop: now_button_order: "+button_order+", name= "+ name);
									            				
								namelist.put(button_order,name);			            
							}
						 	liAll=NewsWeather.liAll;//既然主UI已經更新了liAll，使用者後來才建立Ｗidget，就把它直接拿來用，不重新解析了
						 	
						 	Log.i(tag, "COPY_liAll_to_HashMap: "+liAll.get(button_order).get(0).getTitle());
							myDB.close();
							cursor.close();
				 }
			 }else{	 
				 Log.i(tag, "into else");
					 cursor=myDB.getTruePath();
						while(cursor.moveToNext()){
							//將資料庫內的內容取出放到Button上
							name=cursor.getString(cursor.getColumnIndex("_name"));
							path=cursor.getString(cursor.getColumnIndex("_path"));
						
							button_order++;
							Log.i(tag, "In_NULL_Cursor_loop: now_button_order: "+button_order+", name= "+ name);
							
							//開始對每一行的Cursor的網址做解析
				            checkEncode(path);//檢查這行Cursor的網址編碼
				            encodeTransfer(path);//對檢查出來的編碼做另存檔
				            getRss();
				            
				            liAll.put(button_order, getData);//將轉存的xml檔容器getData再放進大容器liAll
				            namelist.put(button_order,name);
				            Log.i(tag, "PUT_to_HashMap: "+name);
				            
						}
						myDB.close();
						cursor.close();
			 }
				
		}
	
}
