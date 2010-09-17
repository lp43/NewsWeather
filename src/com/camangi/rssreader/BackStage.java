
package com.camangi.rssreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.xml.sax.SAXException;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * BackStage這個類別用來記錄新聞更新的最新版本號，還有path傳進來後，變成的ArrayList<NEWS>實體
 * @author simon
 */
public class BackStage extends Service{

	/**記錄了RSS目前的最新更新編號*/
	public static int updateVersion=1;
	final static String tag ="tag";
	/**判斷xml文件編碼並儲存在Encode*/
	static String Encode; 
	/**將BIG5的來源資料，用readLine()一行一行讀出來，並存成UTF-8的存放空間變數*/
	static String contentBuffer;
	/**將資料庫的name,path,int存到hashmap用的變數*/
	public static String name,path;
	/**描述 : 這個id是database裡的id,不一定會照順序*/
	int id;
	private DB myDB;
	public static Cursor cursor;
	/**
	 * 描述 : 記錄頻道按鈕的排序位置<br/>
	 * button_order用來計算使用者總共勾選了幾筆喜好列表，
	 * 這個值是排序的，勾選了幾個頻道就有幾筆資料，從1計數。
	 * 這個值和Button.setTag()、ListView.setTag()、大容器HashMap型態的liAll的key值都是相對應的
	 */
	public static int button_order;
	/**讓Button能夠取到名字的暫存容器*/
	public static HashMap<Integer,String> rssreader_namelist,widget_namelist;
	/**
	 * 描述 :大容器HashMap<Integer,List<News>>，Integer放的是button_order的排序，List<News>放getData<br/>
	 * 將每一筆getRSS()產生的getData容器，再放入大容器裡。因為button_order起始是1，所以資料會從index=1開始存放。
	 */
	public static HashMap<Integer,List<News>> liAll;
	private static boolean AppWidgetExist;
	private static ActivityManager activitymanager;
	public  ArrayList<News> getData;
	public static final String GET_NEW_ENTITY="get_new_entity_from_backstage";
	public static final String CHANGE_LIST_IMMEDIATE="changeListimmediate";
	public static String DatabaseNumber="none";
	static String BufferDatabaseNumber="";
	//===========================================================================================
	
	
	@Override
	public void onCreate() {
		 Log.i(tag, "into BackStage.onCreate()");
		 super.onCreate();
		
		 
		 initialize(this);
		 
//		 Log.i(tag, "BackStage.onCreate() finish");
	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.i(tag, "into Backstage.onStart()");
		cursor =myDB.getTruePath();//取得user要看的頻道的資料清單
		
		new Thread(){
			
		  public void run(){
			Log.i(tag, "into Backstage.onStart().Thread");
			Log.i(tag, "cursor amount: "+cursor.getCount());
			if(button_order<cursor.getCount()){
			cursor.moveToPosition(button_order);	
			
			Log.i(tag, "-------------<RssReader.BackStage> to NEXT cursor: "+button_order+"------------");
			
			//將資料庫內的內容取出放到Button上
			name=cursor.getString(cursor.getColumnIndex("_name"));
			path=cursor.getString(cursor.getColumnIndex("_path"));
			id = cursor.getInt(cursor.getColumnIndex("_id"));
			
			try {
				getData = convert(path);
				liAll.put(button_order, getData);//將轉存的xml檔容器getData再放進大容器liAll
			} catch (Exception e) {
				Log.i(tag, "convert("+path+")wrong!");
			}
			
			rssreader_namelist.put(id,name);
			widget_namelist.put(button_order,name);
			
			sendBroadToRssReader();
			button_order++;
			if(button_order==cursor.getCount()-1){
				cursor.close();
				myDB.close();
			}
			
		}
			
		}
		}.start();
		
	}

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

	
	
	public static String checkDatabaseNumber(Context context){
		DB myDB=new DB(context);
		Cursor cursor=myDB.getTruePath();
		BufferDatabaseNumber="";
		
		while(cursor.moveToNext()){			
			BufferDatabaseNumber += String.valueOf(cursor.getInt(cursor.getColumnIndex("_id")));
		}
			myDB.close();
			return BufferDatabaseNumber;
	}
	
	public void immedParseData(Context context){
		Log.i(tag, "into BackStage.immedParseData()");
		
		initializeDatabase(context);
		initialize(context);
		
		DatabaseNumber=checkDatabaseNumber(context);
		Log.i(tag, "BackStage.DatabaseNumber is: "+DatabaseNumber);
		
		while(cursor.moveToNext()){
			Log.i(tag, "-------------<MyWidgetProvider.BackStage> to NEXT cursor: "+button_order+"------------");
			name=cursor.getString(cursor.getColumnIndex("_name"));
			path=cursor.getString(cursor.getColumnIndex("_path"));
			try {
				getData = convert(path);
				liAll.put(button_order, getData);//將轉存的xml檔容器getData再放進大容器liAll
			} catch (Exception e) {
				Log.i(tag, "convert("+path+")wrong!");
			}
			widget_namelist.put(button_order,name);
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
	public void initializeDatabase(Context context){
		Log.i(tag, "into BackStage.initializeData()");
	  File file = new File(Environment.getDataDirectory().getPath()+"/data/"+context.getPackageName()+"/databases/database.db");
//	  Log.i(tag, "File pass");
		if(!file.exists()){
			Log.i(tag, "Because database is "+String.valueOf(file.exists())+"exist, so insert BackStage.initializeData() to database");
			myDB = new DB(context);
		      myDB.insert("yahoo", "http://tw.news.yahoo.com/rss/realtime",true);//雅虎UTF-8	
		      myDB.insert("cw", "http://www.cw.com.tw/RSS/cw_content.xml",true);//天下雜誌BIG5
		      myDB.insert("chinatime", "http://rss.chinatimes.com/rss/focus-u.rss",true);//中時UTF-8
		      myDB.insert("thb", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",true);//交通部公路總局UTF8
		      myDB.insert("apple", "http://tw.nextmedia.com/rss/create/type/1077",false);//蘋果utf8
		      myDB.insert("mingpao", "http://inews.mingpao.com/rss/INews/gb.xml",false);//明報BIG5
		      myDB.insert("台大圖書館", "http://www.lib.ntu.edu.tw/rss/newsrss.xml",false);//台灣大學圖書館UTF8
		      myDB.insert("台東大圖書館", "http://acq.lib.nttu.edu.tw/RSS/RSS_NB.asp",false);//台東大學圖書館BIG5
		    myDB.close(); 
		}
//		Log.i(tag, "BackStage.initializeDataBase() finish");
	}
	

	/**描述 : 初始化參數 */
	//獲取RSS資料,讓大容器小容器都有資料
	private void initialize(Context context){
		Log.i(tag, "into BackStage.initialize()");
		this.button_order=0;
	
		 rssreader_namelist = new HashMap<Integer, String>();
		 widget_namelist = new HashMap<Integer, String>();
		 liAll= new HashMap<Integer,List<News>>();
			rssreader_namelist.clear();
			widget_namelist.clear();
			liAll.clear();
		 
		 myDB = new DB(context);//先建立資料庫，若沒建立直接使用myDB.getTruePath()會出現NullPointerException
		 cursor =myDB.getTruePath();//取得user要看的頻道的資料清單
		 getData = new ArrayList<News>();
//		 Log.i(tag, "BackStage.initialize() finish");	
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
	public static ArrayList<News> convert(String path) throws Exception{
		checkEncode(path);
		encodeTransfer(path);
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
    public static void checkEncode(String path) throws Exception{
    	URL url = null;
    	String encode="";
    	int a,b;
    	

			   url = new URL(path);

			   InputStream is = url.openConnection().getInputStream();
			   InputStreamReader isr = new InputStreamReader(is);
			   BufferedReader br = new BufferedReader(isr);
			   String buffera = br.readLine();
			   br.close();
			   
				   Log.i(tag,path+", buffera.indexOf(<)= "+buffera.indexOf("<"));
				   a=buffera.indexOf("\"", 25)+1;
				   b=buffera.indexOf("\"", a+1);
				   encode = buffera.substring(a, b);
				   Log.i(tag, "BackStage.checkEncode(): "+path+" -> "+encode);
			
 		 
    	 
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
    public static void encodeTransfer(String path) throws UnsupportedEncodingException,FileNotFoundException,IOException{
    	
    	if(!Encode.equals("UTF-8")){

      	  URL url = null;
      	  String buffera="";
  			   
//  				   Log.i(tag,"RssReader.encodeTransfer(),button_order "+button_order+": "+path+" COPY TO (String)contentBuffer");
  				   url = new URL(path);
  				   InputStream is = url.openConnection().getInputStream();

  				   InputStreamReader isr = new InputStreamReader(is,Encode);
  				   BufferedReader br = new BufferedReader(isr);
  				   
  				   contentBuffer="";
  				   do{
  					   buffera = br.readLine();
	  					   if(buffera!=null){
	  						 contentBuffer+=new String(buffera.getBytes(),"UTF-8");   
	  					   } 					   
  				   } while(buffera !=null);

  			Log.i(tag,"BackStage.encodeTransfer() finish");
    	}
	}

	
    /**
     * 描述 : 使用解析器將XML轉成List容器 getData<br/>
     * getRss()這個method最主要將存成xml的檔案再轉成hashMap去存放，
     * 好讓之後的顯示和連結都能用get(索引)去控制每筆新聞連結
     * @see checkEncode(String path)
     * @see encodeTransfer(String path)
     */
	public static ArrayList<News> getRss(String path){
		
		//使用android解析器
		MyHandler myHandler = new MyHandler();
		
		if(!Encode.equals("UTF-8")){
	
			Log.i(tag, "Because Encode is : "+Encode+", into (String)contentBuffer parse");
			try{
					
				
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
				
				android.util.Xml.parse(contentBuffer, myHandler);
				//取得RSS標題與內容列表
				
			}catch(Exception e){
				Log.i("tag", "wrong! "+e.getMessage());
			}
		}else{
			Log.i(tag, "Because Encode is: "+Encode+", into path parse directly");
			    URL url = null;//編碼為UTF-8直接解析的寫法	
	        try {
				url = new URL(path);//編碼為UTF-8直接解析的寫法
				
				//編碼為UTF-8直接解析的寫法
				android.util.Xml.parse(url.openConnection().getInputStream(), Xml.Encoding.UTF_8, myHandler);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
		  }
		}
		
		
		Log.i(tag,"BackStage.getRss() parse To GetData finish");
		return (ArrayList<News>) myHandler.getParasedData();
	}



	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
}
