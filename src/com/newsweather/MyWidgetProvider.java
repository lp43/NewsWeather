package com.newsweather;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import android.util.Xml;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	
	private static List<News> getData;//容器
	static String Encode;  //判斷xml文件編碼並儲存在Encode
	static String bufferb;  //bufferb用來存放從xml複製下來，每一行從BIG5轉成UTF-8的String空間
	File file;//用來檢查資料庫在不在
	private DB myDB;
	private static Cursor cursor;
	static String name;//將資料庫的name,path,int存到hashmap用的變數
	static String path;
	int id;//這個id是database裡的id,不一定會照順序
	static int button_order=0;//記錄頻道按鈕的排序位置
	private static HashMap<Integer,String> namelist;//讓Button能夠取到名字的暫存容器
	private static HashMap<Integer,List<News>> liAll;//將每一筆getRSS()產生的getData容器，再放入大容器裡
	final static String tag ="tag";
	Intent intent;
	

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.i(tag, "ProviderOnUpdate");	
		
		intent = new Intent(context, UpdateService.class);
	    context.startService(intent);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	
	
	
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.i(tag, "ProviderOnDiasabled");
		namelist.clear();
		liAll.clear();
		intent = new Intent(context, UpdateService.class);
		context.stopService(intent);
	}




	public static class UpdateService extends Service {

		
		@Override
		public void onCreate() {
			// TODO Auto-generated method stub
			Log.i(tag, "ServiceOnCreate");
			File file = new File("/data/data/com.newsweather/databases/database.db");
			DB myDB= new DB(this);
			namelist = new HashMap<Integer,String>();
			liAll=new HashMap<Integer,List<News>>();
			
			 if(!file.exists()){//取得預設的新聞資料		        	
			      myDB.insert("yahoo", "http://tw.news.yahoo.com/rss/realtime",true);//雅虎UTF-8	
			      myDB.insert("天下雜誌", "http://www.cw.com.tw/RSS/cw_content.xml",true);//天下雜誌BIG5
			      myDB.insert("中時", "http://rss.chinatimes.com/rss/focus-u.rss",true);//中時UTF-8
			      myDB.insert("公路總局", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",true);//交通部公路總局UTF8
			      myDB.insert("蘋果日報", "http://tw.nextmedia.com/rss/create/type/1077",false);//蘋果utf8
			      myDB.insert("明報", "http://inews.mingpao.com/rss/INews/gb.xml",false);//明報BIG5
			      myDB.insert("台大圖書館", "http://www.lib.ntu.edu.tw/rss/newsrss.xml",false);//台灣大學圖書館UTF8
			      myDB.insert("台東大圖書館", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",false);//台東大學圖書館BIG5
				myDB.close();
		}
			 
			 cursor=myDB.getTruePath();
				while(cursor.moveToNext()){
					//將資料庫內的內容取出放到Button上
					name=cursor.getString(cursor.getColumnIndex("_name"));
					path=cursor.getString(cursor.getColumnIndex("_path"));
					
					//開始對每一行的Cursor的網址做解析
		            checkEncode(path);//檢查這行Cursor的網址編碼
		            encodeTransfer(path);//對檢查出來的編碼做另存檔
		            getRss();
		            
		            button_order++;
		            liAll.put(button_order, getData);//將轉存的xml檔容器getData再放進大容器liAll
		            namelist.put(button_order,name);
		            
				}
			
				
			 
			super.onCreate();
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
			
		}

		@Override
		public void onStart(Intent intent, int startId) {
			Log.i(tag, "ServiceonStart");
			super.onStart(intent, startId);
			
			int i=button_order;
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

	
			RemoteViews updateViews = new RemoteViews(this.getPackageName(),
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
	
	public static class currentnews{
		
		static int news_channel=1;//頻道從1開始放
		static int news_number=0;//新聞內容從0放的
		static String content,source;
	}
	
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
				   Log.i("test", "test");
	    	 }catch (Exception e) {
					Log.i("Exception+", e.getMessage());	
					
	 		} 
	    	 
		    	   if(encode.equals("big5")|encode.equals("BIG5")){
		    		 Encode ="BIG5";  
				   }else if(encode.equals("utf-8")|encode.equals("UTF-8")|encode.equals("Utf-8")){
					 Encode ="UTF-8";
				   }	
		    	   
	    }
	    
	    
	    
	    /*因XML無法解析BIG5，會出現paraexception(not-well formed(invalid tocken))
	      所以只要網址一進來，一定存到utf-8的buffxml.xml檔裡*/
	    private static void encodeTransfer(String path) {
	    	
	    	  URL url = null;
	    	  String buffera="";
				   try {
					   Log.i("intoencodeTransfer:"+button_order, "pass");
					   url = new URL(path);
					   InputStream is = url.openConnection().getInputStream();
					   InputStreamReader isr = new InputStreamReader(is,Encode);
					   BufferedReader br = new BufferedReader(isr);
					   FileOutputStream fos = new FileOutputStream("/data/data/com.newsweather/files/buffxml"+button_order+".xml");
					   
					   
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
					    Log.i("fos.close()+", "pass");
					}  catch (UnsupportedEncodingException e) {
					Log.i("unsupportex", e.getMessage());
				} catch (FileNotFoundException e) {
					Log.i("FileNotFoundException", e.getMessage());
				}   catch (IOException e) {
					Log.i("IOException+", e.getMessage());
			} 
					
				Log.i("big52utf8()+", "pass");
		}
	




		//使用XML解析器
		private static void getRss(){

			try{
				//使用android解析器
				MyHandler myHandler = new MyHandler();
				Log.i("myHandler", "pass");
				
		
				FileInputStream fis = new FileInputStream("/data/data/com.newsweather/files/buffxml"+button_order+".xml");
				android.util.Xml.parse(fis, Xml.Encoding.UTF_8, myHandler);
				Log.i("parse", "pass");
				//取得RSS標題與內容列表
				getData = new ArrayList<News>();
				getData = myHandler.getParasedData();
				Log.i("getParasedData", "pass");
			}catch(Exception e){
				Log.i("tag", "wrong! "+e.getMessage());
			}
		}
	
	
	
}
