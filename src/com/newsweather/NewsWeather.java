package com.newsweather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
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
	String path="http://n.yam.com/RSS/Rss_life.xml";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);  
        li = getRss(path);
        if(li==null){Log.i("tag", "li is null");}else{Log.i("tag", String.valueOf(li.size()));}

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
    
    //按下列表所要觸發的事件
    OnItemClickListener listtener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			Log.i("what", li.get(position).getLink());
			String temp = li.get(position).getLink();
		
//			Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://c.yam.com/news/rss/r.c?http://n.yam.com/business/life/201008/20100816204762.html"));
//			Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(li.get(position).getLink()));
//			Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://n.yam.com/business/life/201008/20100816204762.html"));
			Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(temp));
			startActivity(browserIntent);


		}
    	
    };
    
    

	@Override//滑動手勢指令換頁
	public boolean onTouch(View v, MotionEvent event) {
		
		
		if(getstart==0){
			getstart = event.getX();
			Log.i("start:", String.valueOf(getstart));
		}else if(event.getAction()!=event.ACTION_MOVE){
			getend =event.getX();
			Log.i("end:", String.valueOf(getend));
			
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
	private List<News> getRss(String path){

		List<News> data = new ArrayList<News>();
		URL url = null;
		

		try{
			url = new URL(path);;				
			
			//使用sax解析
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			MyHandler myHandler = new MyHandler();
			xr.setContentHandler(myHandler);;				
			xr.parse(new InputSource(url.openStream()));
			//取得RSS標題與內容列表
			data = myHandler.getParasedData();

		}catch(Exception e){
			Log.d("tag", "wrong!");
		}
		return data;
	}

}