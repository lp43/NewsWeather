
package com.camangi.rssreader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.xml.sax.SAXException;
import com.camangi.rssreader.MyWidgetProvider.UpdateService;
import com.camangi.rssreader.MyWidgetProvider.WaitConnect;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * BackStage這個類別用來記錄新聞更新的最新版本號，還有path傳進來後，變成的ArrayList<NEWS>實體
 * @author simon
 */
public class BackStage extends Service{
	/**
	 * Widget的更新速度
	 */
	public static int updatespeed=1000;
	final static String tag ="tag";
	/**判斷xml文件編碼並儲存在Encode*/
//	static String Encode; 
	/**將BIG5的來源資料，用readLine()一行一行讀出來，並存成UTF-8的存放空間變數*/
	static String contentBuffer;
	/**將資料庫的name,path,int存到hashmap用的變數*/
	public static String name,path;
	/**描述 : 這個id是database裡的id,不一定會照順序*/
	int id;
	private static DB myDB;
	public static Cursor cursor;
	/**
	 * 描述 : 記錄頻道按鈕的排序位置<br/>
	 * button_order用來計算使用者總共勾選了幾筆喜好列表，
	 * 這個值是排序的，勾選了幾個頻道就有幾筆資料，從1計數。
	 * 這個值和Button.setTag()、ListView.setTag()、大容器HashMap型態的liAll的key值都是相對應的
	 */
	public static int button_order;
	/**讓Button能夠取到名字的暫存容器*/
	public static HashMap<Integer,String> rssreader_namelist;
	/**
	 * 描述 :大容器HashMap<Integer,List<News>>，Integer放的是button_order的排序，List<News>放getData<br/>
	 * 將每一筆getRSS()產生的getData容器，再放入大容器裡。因為button_order起始是1，所以資料會從index=1開始存放。
	 */
	public static HashMap<Integer,List<News>> liAll;
	private static boolean AppWidgetExist;
	private static ActivityManager activitymanager;
	public  ArrayList<News> getData;
	public static  Thread thread;
	public static final String GET_NEW_ENTITY="get_new_entity_from_backstage";
	public static final String CHANGE_LIST_IMMEDIATE="changeListimmediate";
	public static String DatabaseNumber="none";
	/**BufferDatabaseNumber是每筆資料的id值轉成String的長串數字*/
	static String BufferDatabaseNumber="";
	public static HashMap<Integer,String> backstage_widget_namelist;

	public static boolean widgetExist=false;

	/**
	 * ProgressDialog的暫存變數
	 */
	static ProgressDialog pd;
	public static ArrayList<News> bufferlist,wronglist;
	public static boolean status=true;
	public static boolean parseFinish;//在Handler裡如果有完全解析，那麼為True
	private static String Encode;//記錄著文件編碼
	public static boolean runStop;//啟動停止WIFI時，不會再啟動Service和Receiver了
	static boolean stopforce=false;//強制關閉時，就不要開第2個ProgressDialog視窗:程式關閉中
	//===========================================================================================
	
	
	@Override
	public void onCreate() {
		 Log.i(tag, "into BackStage.onCreate()");
		 super.onCreate();
		
		 
		 initialize(this);//初始化
		 
		 Log.i(tag, "BackStage.onCreate() finish");
	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		
		Log.i(tag, "================================");
		super.onStart(intent, startId);
		
//			myDB = new DB(this);//DB得重載，否則換了頻道排序會用舊資料庫，無法即時讀取新的資料庫
//			Log.i(tag, "into Backstage.onStart()");
//			cursor =myDB.getTruePath();//取得user要看的頻道的資料清單
			
			if(button_order==0){
				liAll.clear();
			}
			
			
			new Thread(){

			public void run(){
				
				myDB = new DB(BackStage.this);//DB得重載，否則換了頻道排序會用舊資料庫，無法即時讀取新的資料庫
				Log.i(tag, "into Backstage.onStart()");
				cursor =myDB.getTruePath();//取得user要看的頻道的資料清單
				
				Log.i(tag, "into Backstage.onStart() start THREAD for parse path");
				
				if(button_order<cursor.getCount()){
				cursor.moveToPosition(button_order);	
				
				Log.i(tag, "-------------<RssReader.BackStage> to NEXT cursor: "+button_order+"------------");
				
				//將資料庫內的內容取出放到Button上
				name=cursor.getString(cursor.getColumnIndex("_name"));
				path=cursor.getString(cursor.getColumnIndex("_path"));
				id = cursor.getInt(cursor.getColumnIndex("_id"));
				
				RssReader.handler1.sendEmptyMessage(RssReader.setTitleStatus);//通知主UI要更新title的更新狀態了
				
					getData = convert(path);
					liAll.put(button_order, getData);//將轉存的xml檔容器getData再放進大容器liAll

			
				rssreader_namelist.put(id,name);
				backstage_widget_namelist.put(button_order,name);
				
				cursor.close();
				myDB.close();
				
				
				Log.i(tag, "Backstage.onStart().Thread.sendBroadToRssReader()");
				sendBroadToRssReader();
				
				if(button_order==cursor.getCount()-1){
//					cursor.close();
//					myDB.close();
					Log.i(tag, "cursor path parse finish");
				}	
				button_order++;	
			}
				}
			
				

			
			}.start();
			
		
	}
	
	public static String checkDatabaseNumber(Context context){
		DB myDB=new DB(context);
		Cursor cursor=myDB.getTruePath();
		BufferDatabaseNumber="";
		
		while(cursor.moveToNext()){			
			BufferDatabaseNumber += String.valueOf(cursor.getInt(cursor.getColumnIndex("_id")));
		}
			cursor.close();
			myDB.close();
			return BufferDatabaseNumber;
	}
	
	
	public void immedParseData(Context context){
		Log.i(tag, "into BackStage.immedParseData()");
		

		initializeDatabase(context);
		initialize(context);
		
		DatabaseNumber=checkDatabaseNumber(context);
		Log.i(tag, "BackStage.DatabaseNumber is: "+DatabaseNumber);
		myDB=new DB(context);
		cursor =myDB.getTruePath();
		while(cursor.moveToNext()){
			Log.i(tag, "-------------<MyWidgetProvider.BackStage> to NEXT cursor: "+button_order+"------------");
			name=cursor.getString(cursor.getColumnIndex("_name"));
			path=cursor.getString(cursor.getColumnIndex("_path"));
			try {
				getData = convert(path);
				MyWidgetProvider.liAll.put(button_order, getData);//將轉存的xml檔容器getData再放進大容器liAll
			} catch (Exception e) {
				Log.i(tag, "convert("+path+")wrong!");
			}
			MyWidgetProvider.widget_namelist.put(button_order,name);
			button_order++;
		}
		cursor.close();
		myDB.close();
	}

	
	/** 
	 * 描述 : 一開始是沒有資料庫的,從這個method才創立起資料庫的 <br/>
	 * 因為存放網址的資料庫是程式啟動後才開啟表格並寫入的，
	 * 為了怕使用者第1次使用沒有資料庫，必須將資料庫建立
	 * 若已有資料庫，則不會寫入。
	 * 另外，在這個method裡執行checkEncode(path),encodeTransfer(path),getRss()三個method，
	 * 將來源網址轉成名為getData的HashMap實體，
	 * 好讓之後要取得各筆新聞資料時，能用HashMap.get(index)才輕鬆存取資料。
	 * @param context 程式主體
	 */
	public static void initializeDatabase(Context context){
		Log.i(tag, "into BackStage.initializeData()");
	    File file = new File(Environment.getDataDirectory().getPath()+"/data/"+context.getPackageName()+"/databases/database.db");
//	  Log.i(tag, "File pass");
		if(!file.exists()){
			Log.i(tag, "Because database exist is: "+String.valueOf(file.exists())+", so insert BackStage.initializeData() to database");
			if(getCountry(context).equals("TW")){//台灣
				myDB = new DB(context);
			      myDB.insert("yahoo!", "http://tw.news.yahoo.com/rss/realtime",true);//雅虎UTF-8
			      myDB.insert("android-tw", "http://feeds.feedburner.com/android-tw",true);//Android-台灣-程式另外解析沒有出錯,在主程式單獨執行也沒問題
			      myDB.insert("NBA", "http://www.nba.com/rss/nba_rss.xml",false);//NBA,在別的程式就先出錯了
//			      myDB.insert("上班族投資", "http://www.pjhuang.net/rss.xml",false);//上班族投資-程式另外解析沒有出錯,主程式出現NullPointerException
			      myDB.insert("天下雜誌", "http://www.cw.com.tw/RSS/cw_content.xml",true);//天下雜誌BIG5
			      myDB.insert("遊戲基地", "http://www.gamebase.com.tw/news/rss/9/0",false);//遊戲基地沒編碼，改用預設UTF-8	
			      myDB.insert("中時", "http://rss.chinatimes.com/rss/focus-u.rss",true);//中時UTF-8
			      myDB.insert("交通部公路總局", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",false);//交通部公路總局UTF8
			      myDB.insert("台東大圖書館", "http://acq.lib.nttu.edu.tw/RSS/RSS_NB.asp",false);//台東大學圖書館BIG5
			      myDB.insert("蘋果", "http://tw.nextmedia.com/rss/create/type/1077",true);//蘋果utf8
			      myDB.insert("明報", "http://inews.mingpao.com/rss/INews/gb.xml",true);//明報BIG5
			      myDB.insert("台大圖書館", "http://www.lib.ntu.edu.tw/rss/newsrss.xml",false);//台灣大學圖書館UTF8   
			      myDB.insert("Yahoo!奇摩股市", "http://tw.stock.yahoo.com/rss/url/d/e/N3.html",false);//Yahoo!奇摩股市
			    myDB.close(); 
			}else if(getCountry(context).equals("JP")){//日文
				myDB = new DB(context);
//			      myDB.insert("Yahoo!トピックス", "http://dailynews.yahoo.co.jp/fc/rss.xml",true);
			      myDB.insert("Yahoo!経済", "http://headlines.yahoo.co.jp/rss/nkbp_tren_bus.xml",false);
			      myDB.insert("Yahoo!海外", "http://headlines.yahoo.co.jp/rss/cnn_c_int.xml",false);
			      myDB.insert("全ジャンル一覧", "http://komachi-rss.yomiuri.co.jp/rss/yol/komachi/rss00",true);
			      myDB.insert("生活", "http://komachi-rss.yomiuri.co.jp/rss/yol/komachi/rss01",false);
			      myDB.insert("職場", "http://komachi-rss.yomiuri.co.jp/rss/yol/komachi/rss02",false);
			      myDB.insert("人間関係", "http://komachi-rss.yomiuri.co.jp/rss/yol/komachi/rss06",false);
			      myDB.insert("妊娠", "http://komachi-rss.yomiuri.co.jp/rss/yol/komachi/rss05",false);
			      myDB.insert("旅行", "http://komachi-rss.yomiuri.co.jp/rss/yol/komachi/rss09",false);
			      myDB.insert("主要ニュース", "http://rss.yomiuri.co.jp/rss/yol/topstories",false);
			      
			    myDB.close(); 
			
		}else if(getCountry(context).equals("FR")){//法文
			myDB = new DB(context);
			myDB.insert("BBC World", " http://feeds.bbci.co.uk/news/world/rss.xml",true);//BBC News - World
//		      myDB.insert("yahoo", "http://tw.news.yahoo.com/rss/realtime",false);//雅虎UTF-8	
//		      myDB.insert("cw", "http://www.cw.com.tw/RSS/cw_content.xml",false);//天下雜誌BIG5
//		      myDB.insert("chinatime", "http://rss.chinatimes.com/rss/focus-u.rss",false);//中時UTF-8
//		      myDB.insert("thb", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",false);//交通部公路總局UTF8
//		      myDB.insert("apple", "http://tw.nextmedia.com/rss/create/type/1077",false);//蘋果utf8
//		      myDB.insert("mingpao", "http://inews.mingpao.com/rss/INews/gb.xml",false);//明報BIG5
//		      myDB.insert("Yahoo!奇摩股市", "http://tw.stock.yahoo.com/rss/url/d/e/N3.html",false);//Yahoo!奇摩股市
		      
		      
		    myDB.close(); 
		}else if(getCountry(context).equals("ES")){//西班牙文
			myDB = new DB(context);
			myDB.insert("BBC World", " http://feeds.bbci.co.uk/news/world/rss.xml",true);//BBC News - World  
//			myDB.insert("yahoo", "http://tw.news.yahoo.com/rss/realtime",false);//雅虎UTF-8	
//		      myDB.insert("cw", "http://www.cw.com.tw/RSS/cw_content.xml",false);//天下雜誌BIG5
//		      myDB.insert("chinatime", "http://rss.chinatimes.com/rss/focus-u.rss",false);//中時UTF-8
//		      myDB.insert("thb", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",false);//交通部公路總局UTF8
//		      myDB.insert("apple", "http://tw.nextmedia.com/rss/create/type/1077",false);//蘋果utf8
//		      myDB.insert("mingpao", "http://inews.mingpao.com/rss/INews/gb.xml",false);//明報BIG5
//		      myDB.insert("wretch", "http://www.wretch.cc/blog/Thereseun&commentsRss20=1",false);//wretch;IO錯誤
		      
		    myDB.close();
		}else if(getCountry(context).equals("US")){//美國
			myDB = new DB(context);
			 myDB.insert("BBC World", "http://feeds.bbci.co.uk/news/world/rss.xml",true);//BBC News - World
			 myDB.insert("NYT > Global", "http://www.nytimes.com/services/xml/rss/nyt/GlobalHome.xml",true);//BBC News - World
			 myDB.insert("NYT > Home", "http://feeds.nytimes.com/nyt/rss/HomePage",true);//BBC News - World
			 myDB.insert("Engadget", "http://www.engadget.com/rss.xml",false);//BBC News - World
			 myDB.insert("ReadWriteWeb", "http://feeds.feedburner.com/readwriteweb",false);//BBC News - World
			 myDB.insert("TechCrunch", "http://feeds.feedburner.com/Techcrunch",false);//BBC News - World
			
			 
			 myDB.close();
		}else if(getCountry(context).equals("DE")){//德文
			myDB = new DB(context);
			  myDB.insert("BBC World", " http://feeds.bbci.co.uk/news/world/rss.xml",true);//BBC News - World
//		      myDB.insert("yahoo", "http://tw.news.yahoo.com/rss/realtime",false);//雅虎UTF-8	
//		      myDB.insert("cw", "http://www.cw.com.tw/RSS/cw_content.xml",false);
//		      myDB.insert("chinatime", "http://rss.chinatimes.com/rss/focus-u.rss",false);
//		      myDB.insert("thb", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",false);
//		      myDB.insert("apple", "http://tw.nextmedia.com/rss/create/type/1077",false);
//		      myDB.insert("mingpao", "http://inews.mingpao.com/rss/INews/gb.xml",false);
//		      myDB.insert("Yahoo!奇摩股市", "http://tw.stock.yahoo.com/rss/url/d/e/N3.html",false);
		      
		    myDB.close();
		}else{
			myDB = new DB(context);
			 myDB.insert("BBC World", "http://feeds.bbci.co.uk/news/world/rss.xml",true);//BBC News - World
			 myDB.insert("NYT > Global", "http://www.nytimes.com/services/xml/rss/nyt/GlobalHome.xml",true);//BBC News - World
			 myDB.insert("NYT > Home", "http://feeds.nytimes.com/nyt/rss/HomePage",true);//BBC News - World
			 myDB.insert("Engadget", "http://www.engadget.com/rss.xml",false);//BBC News - World
			 myDB.insert("ReadWriteWeb", "http://feeds.feedburner.com/readwriteweb",false);//BBC News - World
			 myDB.insert("TechCrunch", "http://feeds.feedburner.com/Techcrunch",false);//BBC News - World
			
		    myDB.close(); 
		}
		}
//		Log.i(tag, "BackStage.initializeDataBase() finish");
	}
	
	
	/**
	 * 描述 : 取得Android configuration裡的語系檔
	 * @param context 從該主體去取得資源(getResource())
	 * @return 回傳國家簡稱
	 */
	public static String getCountry(Context context){
        Resources rs=context.getResources();
        Configuration cf=rs.getConfiguration();
        String country=cf.locale.getCountry();
        Log.i(tag, "get current country is: "+country);
        return country;
	}
	


	/**描述 : 初始化參數 */
	//獲取RSS資料,讓大容器小容器都有資料
	private void initialize(Context context){
		Log.i(tag, "into BackStage.initialize()");
		this.button_order=0;
		 rssreader_namelist = new HashMap<Integer, String>();
		 backstage_widget_namelist = new HashMap<Integer, String>();
		 liAll= new HashMap<Integer,List<News>>();
		 rssreader_namelist.clear();
		 backstage_widget_namelist.clear();
	
		 
		 myDB = new DB(context);//先建立資料庫，若沒建立直接使用myDB.getTruePath()會出現NullPointerException
		 cursor =myDB.getTruePath();//取得user要看的頻道的資料清單
		 Log.i(tag, "cursor amount: "+cursor.getCount());
		 getData = new ArrayList<News>();
//		 Log.i(tag, "BackStage.initialize() finish");	
		 cursor.close();
		 myDB.close();
	}
	
	
    private void sendBroadToRssReader(){

	        //發送廣播來即時更改Widget
	       Intent intent = new Intent();

	       intent.putExtra("entity_name", name);
	       intent.putExtra("button_order", button_order);
	       intent.putExtra("id", id);
	       intent.putExtra("getData", getData);
	       intent.setAction(GET_NEW_ENTITY);
	       sendBroadcast(intent);
	       Log.i(tag, "=>BackStage.sendBroadToRssReader(), now send entity path is: "+ name);
	   
    }
    

    
	
	/**
	 * 描述 : 將網址丟進來後，輸出成ArrayList<News>格式，準備放入(HashMap)liAll大容器裡
	 * @param path 要解析的網址
	 * @return ArrayList<News>，此為liAll的Value值型態
	 * @throws Exception 在用Url連結和解析的過程中，會產生些許的Exception.
	 */
	public static ArrayList<News> convert(String path){


			try {
				checkEncode(path);
			} catch (MalformedURLException e) {
				appearExceptionMessage("MalformedURLException",e);
//			Log.i(path, "create wronglist finish");
			} catch (IOException e) {
				Log.i(tag, "IOException: "+e.getMessage());
				appearExceptionMessage("IOException",e);
//				Log.i(path, "create wronglist finish");
			}


//		Encode="UTF-8";
		return getRss(path);
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
    public static void checkEncode(String path) throws MalformedURLException,IOException{
    	URL url = null;
    	String encode="";
    	int a,b;

				url = new URL(path);
				Log.i(tag, "into checkEncode() path: "+path);
				HttpURLConnection uc  = (HttpURLConnection)url.openConnection();
				while((uc.getResponseCode()!=HttpURLConnection.HTTP_OK)){
					uc.disconnect();
					uc  = (HttpURLConnection)url.openConnection();
				}
				if(uc.getResponseCode()==HttpURLConnection.HTTP_OK){
					uc.setConnectTimeout(1000);
//	  				uc.setReadTimeout(1000);

	  				InputStream is = uc.getInputStream();
	  				InputStreamReader isr = new InputStreamReader(is);		
	  				BufferedReader br = new BufferedReader(isr);
//	  				
				   String buffera = br.readLine();
//				   Log.i(tag, "first line: "+buffera);
////				   Log.i(tag,path+", buffera0.indexOf(<)= "+buffera0.indexOf("<"));
				   a=buffera.indexOf("\"", 25)+1;
				   b=buffera.indexOf("\"", a+1);
				   encode = buffera.substring(a, b);
				   br.close();
//	  				isr.close();
		    	   uc.disconnect();
//				   Log.i(tag, "BackStage.checkEncode(): "+path+" -> "+encode);
//				
	    	   if(encode.equals("big5")|encode.equals("BIG5")){
	    		 Encode ="BIG5";  
			   }else if(encode.equals("utf-8")|encode.equals("UTF-8")|encode.equals("Utf-8")){
				 Encode ="UTF-8";
			   }else{
				 Encode ="UTF-8";  
			   }
//	    	  
	    	   //編碼轉換
	    	   encodeTransfer(path);
				}
//	  				
	    	   
    }
    



	/**
     * 描述 : encodeTransfer() 將新聞來源轉檔成UTF-8型態的XML檔 <br/>
     * 因XML無法解析BIG5，會出現paraexception(not-well formed(invalid tocken))
     * 所以只要網址一進來，一定存到utf-8的buffxml(列表編號(從1開始)).xml檔裡
     * @param path 傳進來的Rss來源網址
	 * @throws IOException 
	 * @see checkEncode(String path)
     * @see getRss()
     */
    public static void encodeTransfer(String path) throws IOException {
    	
//    	if(!Encode.equals("UTF-8")){

      	  URL url = null;
      	  String buffera="";
  			   
  				   Log.i(tag,"RssReader.encodeTransfer(),button_order "+button_order+": "+path+" COPY TO (String)contentBuffer");
  				   url = new URL(path);
  				 HttpURLConnection uc  = (HttpURLConnection)url.openConnection();
  				while((uc.getResponseCode()!=HttpURLConnection.HTTP_OK)){
					uc.disconnect();
					uc  = (HttpURLConnection)url.openConnection();
				}
  				if(uc.getResponseCode()==HttpURLConnection.HTTP_OK){
  					uc.setConnectTimeout(3000);
  					uc.setReadTimeout(3000);
  				InputStream is = uc.getInputStream();
  				   InputStreamReader isr = new InputStreamReader(is,Encode);
  				   BufferedReader br = new BufferedReader(isr);
  				   
  				   contentBuffer="";
  				   do{
  					   buffera = br.readLine();
	  					   if(buffera!=null){
	  						 contentBuffer+=new String(buffera.getBytes(),"UTF-8");   
	  					   } 					   
  				   } while(buffera !=null);
  				   br.close();
  				   uc.disconnect();

  			Log.i(tag,"BackStage.encodeTransfer() finish");
    	}
//    	}		
	}

	
    /**
     * 描述 : 使用解析器將XML轉成List容器 getData<br/>
     * getRss()這個method最主要將存成xml的檔案再轉成hashMap去存放，
     * 好讓之後的顯示和連結都能用get(索引)去控制每筆新聞連結
     * @throws SAXException 
     * @throws IOException 
     * @see checkEncode(String path)
     * @see encodeTransfer(String path)
     */
	public static ArrayList<News> getRss(String path){
		
		//使用android解析器
		MyHandler myHandler = new MyHandler();
		
		if(!Encode.equals("UTF-8")){
	
			Log.i(tag, "Because Encode is : "+Encode+", into (String)contentBuffer parse");
			
					
				
				/*//使用sax解析
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				MyHandler myHandler = new MyHandler();
				xr.setContentHandler(myHandler);
				FileInputStream fis = openFileInput("buffxml.xml");
				InputSource A =new InputSource(path);
			
				String encode =A.getEncoding();//返回n u l l 
				xr.parse(A);*/	
		
//				FileInputStream fis = openFileInput("buffxml"+button_order+".xml");
//				android.util.Xml.parse(fis, Xml.Encoding.UTF_8, myHandler);
				
				try {
//					Log.i(tag, "contentBuffer: "+contentBuffer);
					android.util.Xml.parse(contentBuffer, myHandler);
				} catch (SAXException e) {
					News news = new News();
					news.setDate("Error reason: "+ "SAXException" +", \n"+e.getMessage());
					news.setTitle("parse_error");
					news.setLink("http://");
					wronglist=new ArrayList<News>();
					wronglist.add(news);
					Encode="UTF-8";
					Log.i(path, "create wronglist finish");
					Log.i(tag, e.getMessage());
				}

				
		}else{
			//如果是UTF-8直接解析
			Log.i(tag, "Because Encode is: "+Encode+", into path parse directly");
			   
				try {
//					URL url = null;//編碼為UTF-8直接解析的寫法	
//					url = new URL("http://www.gamebase.com.tw/news/rss/9/0");
		
//					url = new URL(path);
//					HttpURLConnection uc=(HttpURLConnection)url.openConnection();
//					while(uc.getResponseCode()!=HttpURLConnection.HTTP_OK){
//						Log.i(tag, "Server Response: "+uc.getResponseCode());
////						
//						uc.disconnect();
//						uc=(HttpURLConnection)url.openConnection();
//					}
//					if(uc.getResponseCode()==HttpURLConnection.HTTP_OK){
//					InputStream i=uc.getInputStream();
//					InputStream i=url.openConnection().getInputStream();
//					InputStreamReader ir = new InputStreamReader(i);
//					BufferedReader br=new BufferedReader(ir);
//					Log.i(tag, "second request first line: "+br.readLine());
					
//				String s,ss="";
//					while((s=br.readLine())!=null){			
//						ss+=s;					
//					}
//					Log.i(tag, "String ss: "+ss);
					android.util.Xml.parse(contentBuffer, myHandler);
					
//					android.util.Xml.parse(/*url.openConnection().getInputStream()*/i, Xml.Encoding.UTF_8, myHandler);
//					br.close();
//					i.close();
//					uc.disconnect();
//					br.close();
//				} catch (MalformedURLException e) {
//					Log.i(tag, "MalformedURLException"+e.getMessage());
//					News news = new News();
//					news.setDate("Error reason: "+ "MalformedURLException" +", \n"+e.getMessage());
//					news.setTitle("parse_error");
//					news.setLink("http://");
//					wronglist=new ArrayList<News>();
//					wronglist.add(news);
//					Encode="UTF-8";
//					Log.i(path, "create wronglist finish");
//					}
				}catch (SAXException e) {
						Log.i(tag, "SAXException"+e.getMessage());
						News news = new News();
						news.setDate("Error reason: "+ "SAXException" +", \n"+e.getMessage());
						news.setTitle("parse_error");
						news.setLink("http://");
						wronglist=new ArrayList<News>();
						wronglist.add(news);
						Encode="UTF-8";
						Log.i(tag, "create wronglist finish");
//					}catch (IOException e) {
//						Log.i(tag, "IOException"+e.getMessage());
//						News news = new News();
//						news.setDate("Error reason: "+ "IOException" +", \n"+e.getMessage());
//						news.setTitle("parse_error");
//						news.setLink("http://");
//						wronglist=new ArrayList<News>();
//						wronglist.add(news);
//						Encode="UTF-8";
//						Log.i(path, "create wronglist finish");
					} 
				 
			}
		bufferlist = new ArrayList<News>();
		bufferlist.clear();//看看有沒有改善重覆實體的問題
		
		Log.i(tag,"BackStage.getRss() parse To GetData finish");
		bufferlist=(ArrayList<News>) myHandler.getParasedData();
		Log.i(tag, "BackStage.parseFinish= "+String.valueOf(BackStage.parseFinish));
		if(BackStage.parseFinish==false){//如果沒有資料，代表解析錯誤，傳回解析錯誤
			bufferlist=wronglist;
			Log.i(tag, "bufferlist=wronglist;");
		}
		
		return bufferlist;
		
	}

	/**
	 * 描述 : 啟動AlarmManager<br/>
	 * 由於SDK1.6以上不能再用Widget template裡updatepreoidmillis指令控制更新時間，
	 * 所以改用AlarmManager來更新
	 * @param context 傳進來的程式主體
	 * @param open 0:close AlarmManager / 1:start AlarmManager
	 */
	public static void startAlarmManager(Context context,int open){
		//使用AlarmManager的方式來控制更新時間
		 AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		 Intent intent = new Intent(context, UpdateService.class);
		 PendingIntent pintent=PendingIntent.getService(context, 0, intent, 0);
		 
		 Intent intent2 = new Intent(context, WaitConnect.class);
		 PendingIntent pintent2=PendingIntent.getService(context, 0, intent2, 0);
		 
		 
		 switch(open){
		 case 0:
			 //關閉讓Widget不斷更新的widget Service
			 alarm.cancel(pintent);
			 pintent.cancel();
			 break;
		 case 1:	 
			 alarm.setRepeating(AlarmManager.RTC, 0, updatespeed, pintent);//設定每updatespeed秒更新一次Widget
			 break;
		 case 2:
			 alarm.cancel(pintent2);//這裡原本是pintent,我改成pintent2
			 pintent2.cancel();
			 break;
		 case 3:
			 alarm.setRepeating(AlarmManager.RTC, 0, updatespeed, pintent2);
			 break;
		 }
		 
	}
	
	
/**
 * 	描述 : 顯示ProgressDialog視窗<BR/>
 * @param context 要顯示ProgressDialog的主體
 * @param open 傳進來的參數，0代表關，1代表開
 */
	public static void WifiWaitDialog(final Context context){
		runStop=false;
		
			pd =new ProgressDialog(context);
			pd.setTitle(context.getString(R.string.please_wait));
			pd.setMessage(context.getString(R.string.wifi_connecting));
			pd.setButton(-1,context.getString(R.string.cancel), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					runStop=true;
					//main Thread被拿來顯示ProgressDialog
					ProgressDialog.show(context, context.getString(R.string.please_wait), context.getString(R.string.wifi_closing));
					
					
		
					
						new Thread(){//這段讓WiFi關閉中的畫面可以持續顯示至WiFi關了為止
							

							public void run(){
								int count=0;
								Looper.prepare();
								while(Net.checkEnableingWifiStatus(context)==false){
									Log.i(tag, "because Wifi is: "+String.valueOf(Net.checkEnableingWifiStatus(context))+", let Thread sleep 1000");
									letThreadSleep(1000);//讓當下的Thread,也就是new Thread睡1秒
									count++;
									Log.i(tag, "wifi fasle count: "+count);
									
									//以下這段做到︰若一直連不上線，則強制關閉程式
									//但是還沒有實測這一段
									if(count>10){
										stopforce=true;
										Activity activity=(Activity)context;
										activity.finish();
									}
								}
								
								
								
								Runnable r=new Runnable(){
									public void run(){
										
										RssReader.handler1=new Handler();
										ProgressDialog.show(context, context.getString(R.string.please_wait), context.getString(R.string.program_finishing));
										
										new Thread(){
											public void run(){
												while(Net.checkEnableingWifiStatus(context)==true/*|stopforce==true*/){
													letThreadSleep(1000);//讓當下的Thread,也就是這個new Thread睡1秒,這個動作不影響另一個Thread關閉WiFi
												}
												Log.i(tag, "Wifi closed finish");
												//確定WiFi關了才關閉主程式
												Activity activity=(Activity)context;
												activity.finish();
												
											}
										}.start();
									}
								};
								
								Log.i(tag, "stopforce: "+stopforce);
								if(stopforce!=true){
									RssReader.handler1.post(r);//使用Post的方式，當WiFi關了以後，讓主畫面的ProgressDialog改成"程式關閉中"
								}
									
								
								
								
								
								
								//一旦偵測到WiFi打開後，就關WiFi,這個關閉的動作後續絕對不能有其它動作
								//否則會造成干擾,只能再建new Thread去做別的事
								Net.switchWifi(context, false);
								
								Log.i(tag, "close wifi");
								
								
								//以下如果再做任何動作，都會打擾到新Thead去關WiFi,會當
//								while(Net.checkEnableingWifiStatus(context)==true){
//									letThreadSleep(1000);//close wifi後好像不能直接睡,會當
//									Log.i(tag, "WiFi closing...");
//								}
								
							}
						}.start();
					
							
				}}
				
			);
			pd.show();
	
			new Thread(){
				public void run(){
					try {
					while(!Net.checkEnableingWifiStatus(context)){
						Log.i(tag, "because wifi connecting,let prgressDialog appear 1000s");
							sleep(1000);	
						}
					} catch (InterruptedException e) {
						Log.i(tag, e.getMessage());
					}finally{
						if(runStop==false){
							pd.dismiss();
							ifWifiPassThenSendMessage(context);
							Log.i(tag, "close progressDialog");
						}
						
					}
				}
			}.start();
	}

    /**
     * 描述 : 如果有連線能力了，才開始取得和解析資料<br/>
     */
    public static void ifWifiPassThenSendMessage(Context context){
    	if(Net.check3GConnectStatus(context)|Net./*checkInitWifiStatus*/checkEnableingWifiStatus(context)){
			RssReader.handler1.sendEmptyMessage(RssReader.initiateRssReaderUIthenStartBackStageService);	
		}
    }

    /**
     * 描述 : 當使用者將顯示頻道設為<0時,會跳出視窗告知使用者至少要保留一筆頻道,否則Widget可能會沒有資料,甚至出錯
     * @param context 顯示出錯視窗的主體
     */
    public static void remainOneChannel(Context context){
    	new AlertDialog.Builder(context)
		.setTitle(context.getString(R.string.error))
		.setMessage(context.getString(R.string.least_one_channel))
		.setIcon(R.drawable.warning)
		.setPositiveButton(context.getString(R.string.reset), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {}
			})
		
		.show();
    }
    
    /**
     * 讓執行緒休眠1秒
     */
	public static void letThreadSleep(int sleepTime){
		try {
			Thread.currentThread().sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * 描述 : 當使用者在還沒載入完資料前做動作，會影響到資料的載入(可能變成多筆資料重覆)<br/>
	 * 因此會先判斷若cursor還沒到最後一筆，不行Setting channel之類所有會更改到資料庫的動作
	 * @param context 顯示錯誤訊息視窗的主體
	 */
	public static void loadingCantUseDataDialog(Context context){
		
			new AlertDialog.Builder(context)
			.setTitle(context.getString(R.string.error))
			.setMessage(context.getString(R.string.loading_completely_to_action))
			.setIcon(R.drawable.warning)
			.setPositiveButton(context.getString(R.string.back), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {}
				})
			
			.show();
		
	}

	private static void appearExceptionMessage(String ExceptionName,Exception e){
		Log.i("tag", "Exception: "+e.getMessage());
		News news = new News();
		news.setDate("Error reason: "+ ExceptionName +", \n"+e.getMessage());
		news.setTitle("parse_error");
		news.setLink("http://");
		wronglist=new ArrayList<News>();
		wronglist.add(news);
		Encode="UTF-8";
		Log.i(path, "create wronglist finish");
	}
	
	public static ArrayList<News> verifyPath(Context context,String path){
		ArrayList<News> buffer = new ArrayList<News>();
		
		
		try {
			checkEncode(path);
			buffer=getRss(path);
			
		} catch (MalformedURLException e) {
			Log.i(tag, "MalformedURLException: "+e.getMessage());
			verifyErrorDialog(context,"MalformedURLException",e);
			buffer=null;//如果解析出現Exception,將回傳值傳回null
		}catch (UnsupportedEncodingException e) {
			Log.i("tag", "UnsupportedEncodingException: "+e.getMessage());
			verifyErrorDialog(context,"UnsupportedEncodingException",e);
			buffer=null;//如果解析出現Exception,將回傳值傳回null
		}catch (FileNotFoundException e) {
			Log.i("tag", "FileNotFoundException: "+e.getMessage());
			verifyErrorDialog(context,"FileNotFoundException",e);
			buffer=null;//如果解析出現Exception,將回傳值傳回null
		} catch (IOException e) {
			Log.i(tag, "IOException: "+e.getMessage());
			verifyErrorDialog(context,"IOException",e);
			buffer=null;//如果解析出現Exception,將回傳值傳回null
		}catch(Exception e){
			Log.i("tag", "Exception: "+e.getMessage());
			verifyErrorDialog(context,"Exception",e);
			buffer=null;//如果解析出現Exception,將回傳值傳回null
		}
		
		return buffer;
	}
	
	/**
	 * 描述 : 當驗證網址遇到Exception時會出現的Dialog視窗
	 * @param context 顯示錯誤訊息視窗的主體
	 */
	public static void verifyErrorDialog(Context context,String errormessage,Exception e){
		Log.i(tag, "inito VerifyErrorDialog");
		new AlertDialog.Builder(context)
		
		.setTitle(context.getString(R.string.error))
		.setMessage(context.getString(R.string.cant_parse)+"\n"+context.getString(R.string.error_reason)+" "+errormessage+", "+e.getMessage())
		.setIcon(R.drawable.warning)
		.setPositiveButton(context.getString(R.string.back), new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {}
		})

		.show();
	}
	
	
    /**
     * 這個類別用來記錄螢幕的相關資訊
     * @author simon
     */
	public static class  ScreenSize{
		
		public static int getScreenWidth(Context context){
			WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = manager.getDefaultDisplay();
			return display.getWidth();
		}
		
		public static int getScreenHeight(Context context){
			WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = manager.getDefaultDisplay();
			return display.getHeight();
		}
	}


	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	
}
