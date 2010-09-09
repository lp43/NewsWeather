
package com.camangi.rssreader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.util.Xml;

public class BackStage {

	
	public static int updateVersion=1;
	final static String tag ="tag";
	/**判斷xml文件編碼並儲存在Encode*/
	static String Encode; 
	static String contentBuffer;
	static String bufferb; 
	/**將資料庫的name,path,int存到hashmap用的變數*/
	static String name;
	
	
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
//  				   FileOutputStream fos = openFileOutput("buffxml"+button_order+".xml", Context.MODE_PRIVATE);
  				   
  				   contentBuffer="";
  				   do{
  					   buffera = br.readLine();
  					   if(buffera!=null){
  					   bufferb = new String(buffera.getBytes(),"UTF-8");   
  					   contentBuffer+=bufferb;
//  					   fos.write(bufferb.getBytes());
//  					   fos.write('\r');
  					   }else{/*else這段避免XML原文最下面有一行空白行，卻還要for.write(b.getBytes())給出值的冏境
  					       導致造成NullPointerException*/
  					   bufferb="";
  				   }
  					   
  				   } while(buffera !=null);
  					  
  				   
//  				    fos.flush();			    
//  				    fos.close();
  				
  				
  			Log.i(tag,"BackStage.encodeTransfer(): run to end");
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
	
	
}
