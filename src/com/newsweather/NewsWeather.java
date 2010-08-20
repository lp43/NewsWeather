package com.newsweather;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;


public class NewsWeather extends Activity implements OnTouchListener {
	
	//宣告最上面的5個新聞標題按鈕
	private Button button_foucs,//按鈕[焦點新聞]
	button_tech,//按鈕[科技]
	button_sports,//按鈕[運動]
	button_relax;//按鈕[影劇]
	private ListView llv1,llv2,llv3,llv4;//先初始化4個ListView
	private HorizontalScrollView slv;//為了讓下面的新聞欄可以左右滑動，需把HorizontalScrollView宣告出來
	double getend,getstart=0;//用來存放手勢的第1個值和最後一個值，好比較是往前或往後滑
	private List<News> li1,li2,li3,li4 = new ArrayList<News>();//容器
	String Encode;  //判斷xml文件編碼並儲存在Encode
	String bufferb;  //bufferb用來存放從xml複製下來，每一行從BIG5轉成UTF-8的String空間
	
	private HashMap<String,String> path;
	String path1="http://tw.news.yahoo.com/rss/realtime";//雅虎UTF-8	
	String path2="http://www.cw.com.tw/RSS/cw_content.xml";//天下雜誌BIG5	
	String path3="http://rss.chinatimes.com/rss/focus-u.rss"; //中時UTF-8
//	String path4="http://www.zdnet.com.tw/rss/news_daily.htm";  ////中時BIG5
//	String path="http://tw.nextmedia.com/rss/create/type/1077";//蘋果utf8
//	String path="http://inews.mingpao.com/rss/INews/gb.xml";//明報BIG5
//	String path="http://www.lib.ntu.edu.tw/rss/newsrss.xml";//台灣大學圖書館UTF8
//	String path="http://www.ait.org.tw/zh/press-releases.rss";//美國在台協會UTF8→有編碼的問題(來源檔有亂碼)
//	String path="http://acq.lib.nttu.edu.tw/RSS/RSS_NB.asp";//台東大學圖書館BIG5
	String path4="http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx";//交通部公路總局UTF8
	int frequently;
	int nowview=1;
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        path=new HashMap<String,String>();
        	path.put("path1", "http://tw.news.yahoo.com/rss/realtime");
        	path.put("path2", "http://www.cw.com.tw/RSS/cw_content.xml");
        	path.put("path3", "http://rss.chinatimes.com/rss/focus-u.rss");
        	path.put("path4", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx");
        
        	
            for(int i =1; i<5;i++){
            	Log.i("foreach", String.valueOf(i));
            	frequently=i;
            	
            		checkEncode(path.get("path"+frequently));  //先判斷xml的編碼格式
                    encodeTransfer(path.get("path"+frequently));  //進行big5→utf-8轉碼
                    switch(frequently){
                    	case 1:
                    		li1 = getRss();  //將複製並轉碼完的檔案做解析，並存放到li容器裡   
                    		break;
                    	case 2:
                    		li2 = getRss();  //將複製並轉碼完的檔案做解析，並存放到li容器裡   
                    		break;
                    	case 3:
                    		li3 = getRss();  //將複製並轉碼完的檔案做解析，並存放到li容器裡   
                    		break;
                    	case 4:
                    		li4 = getRss();  //將複製並轉碼完的檔案做解析，並存放到li容器裡   
                    		break;
                    }
                           
            }
        	    	
        	
        	

        button_foucs = (Button) findViewById(R.id.button_focus);
        button_tech = (Button) findViewById(R.id.button_tech);
        button_sports = (Button) findViewById(R.id.button_sports);
        button_relax = (Button) findViewById(R.id.button_relax);
       
        llv1 = (ListView) findViewById(R.id.list);
        llv2 = (ListView) findViewById(R.id.list2);
        llv3 = (ListView) findViewById(R.id.list3);
        llv4 = (ListView) findViewById(R.id.list4);
        slv = (HorizontalScrollView) findViewById(R.id.hsv);
        
        //設定ListView的樣版和文字來源
        llv1.setAdapter(new NewsAdapter(NewsWeather.this,li1));
        llv2.setAdapter(new NewsAdapter(NewsWeather.this,li2));
        llv3.setAdapter(new NewsAdapter(NewsWeather.this,li3));
        llv4.setAdapter(new NewsAdapter(NewsWeather.this,li4));
       
        //讓列表有點選的功能
        llv1.setOnItemClickListener(listtener);
        llv2.setOnItemClickListener(listtener);
        llv3.setOnItemClickListener(listtener);
        llv4.setOnItemClickListener(listtener);
        
        
        button_foucs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	slv.smoothScrollTo(0,0);
            	nowview=1;
            }
        });
        button_tech.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	slv.smoothScrollTo(800,0);
            	nowview=2;
            }
        });
        button_sports.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	slv.smoothScrollTo(1600,0);
            	nowview=3;
            }
        });
        button_relax.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	slv.smoothScrollTo(2400,0);
            	nowview=4;
            }
        });
       
        slv.setOnTouchListener(this);
    }
    
    private void checkEncode(String path){//判斷此xml的格式
    	URL url = null;
    	String encode="";
    	int a,b;
    	 try {
    		   Log.i("intoCeckEncode:"+frequently, "pass");
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
    	 }catch (IOException e) {
				Log.i("IOException+", e.getMessage());
				
				new AlertDialog.Builder(NewsWeather.this)
				.setTitle("很抱歉！")
				.setMessage("由於目前沒有針測到網路，暫不提供新聞服務...")
				.setPositiveButton("確認", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												NewsWeather.this.finish();
											}
										}).show();
				
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
				   Log.i("intoencodeTransfer:"+frequently, "pass");
				   url = new URL(path);
				   InputStream is = url.openConnection().getInputStream();
				   InputStreamReader isr = new InputStreamReader(is,Encode);
				   BufferedReader br = new BufferedReader(isr);
				   FileOutputStream fos = openFileOutput("buffxml"+frequently+".xml", Context.MODE_PRIVATE);
				   
				   
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

	//按下列表所要觸發的事件
    OnItemClickListener listtener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch(nowview){
				case 1:
					String temp1 = li1.get(position).getLink();
					Intent browserIntent1 = new Intent("android.intent.action.VIEW", Uri.parse(temp1));
					startActivity(browserIntent1);
					break;
				case 2:
					String temp2 = li2.get(position).getLink();
					Intent browserIntent2 = new Intent("android.intent.action.VIEW", Uri.parse(temp2));
					startActivity(browserIntent2);
					break;
				case 3:
					String temp3 = li3.get(position).getLink();
					Intent browserIntent3 = new Intent("android.intent.action.VIEW", Uri.parse(temp3));
					startActivity(browserIntent3);
					break;
				case 4:
					String temp4 = li4.get(position).getLink();
					Intent browserIntent4 = new Intent("android.intent.action.VIEW", Uri.parse(temp4));
					startActivity(browserIntent4);
					break;
					
			}
			


		}
    	
    };
    
    

	@Override//滑動手勢指令換頁
	public boolean onTouch(View v, MotionEvent event) {
		
		
		if(getstart==0){
			getstart = event.getX();
		}else if(event.getAction()!=event.ACTION_MOVE){
			getend =event.getX();
			
			if (getstart-getend >0){
				slv.smoothScrollBy(800, 0);
				
					if(nowview<=4){
						nowview++;
					}else{
						nowview=4;
					}
				
			}else{
				slv.smoothScrollBy(-800, 0);
				if(nowview>1)nowview--;
			}
			
			getstart=0;
		}		
		return true;
	}
    
	//使用XML解析器
	private List<News> getRss(){

		List<News> data = new ArrayList<News>();
//		URL url = null;//編碼為UTF-8直接解析的寫法		

		try{
//            url = new URL(path);//編碼為UTF-8直接解析的寫法				
			
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
			
			//使用android解析器
			MyHandler myHandler = new MyHandler();
			Log.i("myHandler", "pass");
			
			/*//編碼為UTF-8直接解析的寫法
//			android.util.Xml.parse(url.openConnection().getInputStream(), Xml.Encoding.UTF_8, myHandler);*/
			
			
	
			FileInputStream fis = openFileInput("buffxml"+frequently+".xml");
			android.util.Xml.parse(fis, Xml.Encoding.UTF_8, myHandler);
			Log.i("parse", "pass");
			//取得RSS標題與內容列表
			data = myHandler.getParasedData();
			Log.i("getParasedData", "pass");
		}catch(Exception e){
			Log.i("tag", "wrong! "+e.getMessage());
		}
		return data;
	}
	


}