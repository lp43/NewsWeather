package com.newsweather;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import android.app.Activity;
import android.content.Context;
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
	//先初始化4個ListView
	private ListView llv,llv2,llv3,llv4;
	//為了讓下面的新聞欄可以左右滑動，需把HorizontalScrollView宣告出來
	HorizontalScrollView slv;
	//用來存放手勢的第1個值和最後一個值，好比較是往前或往後滑
	double getend,getstart=0;
	private List<News> li = new ArrayList<News>();
/*	
	String path="http://tw.news.yahoo.com/rss/realtime";//雅虎UTF-8*/
	
	String path="http://www.cw.com.tw/RSS/cw_content.xml";//天下雜誌BIG5
	String b;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //進行big5→utf-8轉碼
        big52utf8(path);
        
        li = getRss();
           

        button_foucs = (Button) findViewById(R.id.button_focus);
        button_tech = (Button) findViewById(R.id.button_tech);
        button_sports = (Button) findViewById(R.id.button_sports);
        button_relax = (Button) findViewById(R.id.button_relax);
       
        llv = (ListView) findViewById(R.id.list);
        llv2 = (ListView) findViewById(R.id.list2);
        llv3 = (ListView) findViewById(R.id.list3);
        llv4 = (ListView) findViewById(R.id.list4);
        slv = (HorizontalScrollView) findViewById(R.id.hsv);
        
        //設定ListView的樣版和文字來源
        llv.setAdapter(new NewsAdapter(NewsWeather.this,li));
        llv2.setAdapter(new ArrayAdapter(NewsWeather.this,android.R.layout.simple_list_item_1,new String[]{"阿姆斯狀其實沒有登陸月球","456","456","456","456","456","456","456","456","456","456","456","456","456","456"}));
        llv3.setAdapter(new ArrayAdapter(NewsWeather.this,android.R.layout.simple_list_item_1,new String[]{"王建民輸球","456","456","456","456","456","456","456","456","456","456","456","456","456","456"}));
        llv4.setAdapter(new ArrayAdapter(NewsWeather.this,android.R.layout.simple_list_item_1,new String[]{"小s想再生第3胎","456","456","456","456","456","456","456","456","456","456","456","456","456","456"}));
       
        //讓列表有點選的功能
        llv.setOnItemClickListener(listtener);
        llv2.setOnItemClickListener(listtener);
        llv3.setOnItemClickListener(listtener);
        llv4.setOnItemClickListener(listtener);
        
        
        button_foucs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	slv.smoothScrollTo(0,0);
            	
            }
        });
        button_tech.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	slv.smoothScrollTo(800,0);
            }
        });
        button_sports.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	slv.smoothScrollTo(1600,0);
            }
        });
        button_relax.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	slv.smoothScrollTo(2400,0);
            }
        });
        button_relax.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	slv.smoothScrollTo(2400,0);
            }
        });
       
        slv.setOnTouchListener(this);
    }
    
    
    /*因XML無法解析BIG5，會出現paraexception(not-well formed(invalid tocken))
      所以只要網址一進來，一定存到utf-8的buffxml.xml檔裡*/
    private void big52utf8(String path) {
    	
    	  URL url = null;
			   try {
				   url = new URL(path);
				   InputStream is = url.openConnection().getInputStream();
				   InputStreamReader isr = new InputStreamReader(is,"BIG5");
				   BufferedReader br = new BufferedReader(isr);
				   FileOutputStream fos = openFileOutput("buffxml.xml", Context.MODE_PRIVATE);
				   
				   String a = br.readLine();
				   while(a !=null){
					   a = br.readLine();
					   b = new String(a.getBytes(),"UTF-8");
					   fos.write(b.getBytes());
					   fos.write('\r');
				   }
				    fos.flush();
				    fos.close();
				    
				}  catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();	
		} 
				
		
	}

	//按下列表所要觸發的事件
    OnItemClickListener listtener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String temp = li.get(position).getLink();
			Log.i("what", temp);
			
			Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(temp));
			startActivity(browserIntent);


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
			}else{
				slv.smoothScrollBy(-800, 0);
			}
			
			getstart=0;
		}		
		return true;
	}
    
	//使用XML解析器
	private List<News> getRss(){

		List<News> data = new ArrayList<News>();
//		URL url = null;
		

		try{
//			url = new URL(path);				
			
			/*//使用sax解析
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			MyHandler myHandler = new MyHandler();
			xr.setContentHandler(myHandler);;				
			xr.parse(new InputSource(url.openStream()));*/
			
			//使用android解析器
			MyHandler myHandler = new MyHandler();
			
			/*android.util.Xml.parse(url.openConnection().getInputStream(), Xml.Encoding.UTF_8, myHandler);*/
			
			FileInputStream fis = openFileInput("buffxml.xml");
			android.util.Xml.parse(fis, Xml.Encoding.UTF_8, myHandler);
			
			//取得RSS標題與內容列表
			data = myHandler.getParasedData();

		}catch(Exception e){
			Log.d("tag", "wrong! "+e.getMessage());
		}
		return data;
	}

}