package com.camangi.rssreader;

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
import java.util.List;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

public class BackStage extends SQLiteOpenHelper{

	private static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION=2;
	private DB myDB;
	private Cursor cursor;
	String name,path;//將資料庫的name,path,int存到hashmap用的變數
	int id;//這個id是database裡的id,不一定會照順序
	int button_order;//記錄頻道按鈕的排序位置
	String Encode;  //判斷xml文件編碼並儲存在Encode
	private List<News> getData;//容器
	String bufferb;  //bufferb用來存放從xml複製下來，每一行從BIG5轉成UTF-8的String空間
	public static int updateVersion=1;
	
	public BackStage(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * 這個method用來取得此apk的Data資料夾路徑名稱
	 * @return 此apk的Data資料夾路徑名稱
	 */
	public static String get_DataDir_Path(){
		return Environment.getDataDirectory().getPath();
	}
	
	//一開始是沒有資料庫的,從這個method才創立起資料庫的
	public void getDefaultData(){
//		myDB = new DB(this);
	      myDB.insert("yahoo", "http://tw.news.yahoo.com/rss/realtime",true);//雅虎UTF-8	
	      myDB.insert("天下雜誌", "http://www.cw.com.tw/RSS/cw_content.xml",true);//天下雜誌BIG5
	      myDB.insert("中時", "http://rss.chinatimes.com/rss/focus-u.rss",true);//中時UTF-8
	      myDB.insert("公路總局", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",true);//交通部公路總局UTF8
	      myDB.insert("蘋果日報", "http://tw.nextmedia.com/rss/create/type/1077",false);//蘋果utf8
	      myDB.insert("明報", "http://inews.mingpao.com/rss/INews/gb.xml",false);//明報BIG5
	      myDB.insert("台大圖書館", "http://www.lib.ntu.edu.tw/rss/newsrss.xml",false);//台灣大學圖書館UTF8
	      myDB.insert("台東大圖書館", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",false);//台東大學圖書館BIG5
//		myDB.close();
	}
	
	//檢查資料庫是否存在
	public void checkDatabase(){
        //如果沒有資料庫，才建立預設資料
        File file = new File("/data/data/com.newsweather/databases/database.db");
        if(!file.exists())getDefaultData();//取得預設的新聞資料
	}
	
	//給定網址取回每個channel的getData實體
	public List<News> getChannelEntity(String path){
		checkEncode(path);
		encodeTransfer(path);
		getRss();
		return getData;
	}
	
	
	private void checkEncode(String path){//判斷此xml的格式
    	URL url = null;
    	String encode="";
    	int a,b;
    	 try {
    		   Log.i("intoCeckEncode: ",button_order+ "pass");
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
    private void encodeTransfer(String path) {
    	
    	  URL url = null;
    	  String buffera="";
			   try {
				   Log.i("intoencodeTransfer:"+button_order, "pass");
				   url = new URL(path);
				   InputStream is = url.openConnection().getInputStream();
				   InputStreamReader isr = new InputStreamReader(is,Encode);
				   BufferedReader br = new BufferedReader(isr);
				   FileOutputStream fos = new FileOutputStream("buffxml"+button_order+".xml");
   
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
	private void getRss(){

		try{
		
			//使用android解析器
			MyHandler myHandler = new MyHandler();
			Log.i("myHandler", "pass");
			

			FileInputStream fis = new FileInputStream("buffxml"+button_order+".xml");
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

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}
