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
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;


public class NewsWeather extends Activity implements OnTouchListener,OnClickListener,OnLongClickListener,OnItemClickListener {
	
	/**
	 * 因為Button已經是動態產生，所以只要宣告一個變數，
	 * 之後各個按鈕的控制都靠Button.getId()去的值去決定
	 */
	private Button button;
	/**為了讓下面的新聞欄可以左右滑動，需把HorizontalScrollView宣告出來*/
	private HorizontalScrollView slv;
	/**用來存放手勢的第1個值和最後一個值，好比較是往前或往後滑*/
	double getend,getstart=0;
	/**List小容器，一個getData放了一個頻道的資訊,索引值從0開始，
	 *後面的資料放的是News實體。所以可以用News類別的方法將其值取出 
	 */
	private ArrayList<News> getData;
	/**判斷xml文件編碼並儲存在Encode*/
	String Encode;  
	/**描述 : bufferb用來存放從xml複製下來，每一行從BIG5轉成UTF-8的String空間*/
	String bufferb;  
	/**資料載入中的等待視窗*/
	public ProgressDialog myDialog;
	/**用來檢查資料庫在不在，不在才要重建*/
	File file;
	/**現在的畫面，起始為1*/
	int nowview=1;
	private Handler handler,handler2;
	private DB myDB;
	private Cursor cursor;
	/**將資料庫的name,path,int存到hashmap用的變數*/
	String name,path;
	/**描述 : 這個id是database裡的id,不一定會照順序*/
	int id;
	/**
	 * 描述 : 記錄頻道按鈕的排序位置<br/>
	 * button_order用來計算使用者總共勾選了幾筆喜好列表，
	 * 這個值是排序的，勾選了幾個頻道就有幾筆資料，從1計數。
	 * 這個值和Button.setTag()、ListView的nowview、大容器HashMap型態的liAll的key值都是相對應的
	 */
	int button_order;
	LinearLayout up_layout,down_layout;//定義上下佈局
	/**
	 * 描述 : 讓Button能夠取到名字的暫存容器<br/>
	 * 專門用來放每一筆的name，好讓刪除視窗出現時，能對應到
	 */
	private HashMap<Integer,String> namelist;
	/**
	 * 描述 :大容器HashMap<Integer,List<News>>，Integer放的是button_order的排序，List<News>放getData<br/>
	 * 將每一筆getRSS()產生的getData容器，再放入大容器裡。因為button_order起始是1，所以資料會從index=1開始存放。
	 */
	public static HashMap<Integer,List<News>> liAll;
	final static String tag ="tag";
	public static final String CHANGE_CHANNEL_LIST="changeChannelList";
	/**取出該Apk的套件名稱	 */
	String packageName;
	/**NewsWeather專屬的更新記錄*/
	public static int updateVersion=0;
	static ActivityManager  activitymanager;
	/**如果MyWidgetProvider.class確實有被開啟的記錄參數 */
	static boolean AppWidgetExist;
	
	
	/** 描述 : 一開始是沒有資料庫的,從這個method才創立起資料庫的 */
	public void getDefaultData(){
		myDB = new DB(this);
	      myDB.insert("yahoo", "http://tw.news.yahoo.com/rss/realtime",true);//雅虎UTF-8	
	      myDB.insert("cw", "http://www.cw.com.tw/RSS/cw_content.xml",true);//天下雜誌BIG5
	      myDB.insert("chinatime", "http://rss.chinatimes.com/rss/focus-u.rss",true);//中時UTF-8
	      myDB.insert("thb", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",true);//交通部公路總局UTF8
	      myDB.insert("apple", "http://tw.nextmedia.com/rss/create/type/1077",false);//蘋果utf8
	      myDB.insert("mingpao", "http://inews.mingpao.com/rss/INews/gb.xml",false);//明報BIG5
	      myDB.insert("台大圖書館", "http://www.lib.ntu.edu.tw/rss/newsrss.xml",false);//台灣大學圖書館UTF8
	      myDB.insert("台東大圖書館", "http://www.thb.gov.tw/tm/Menus/Menu04/Trss/rss1_xml.aspx",false);//台東大學圖書館BIG5
		myDB.close();
		
	}
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i("startProgress", "start");
        packageName=this.getPackageName();
        //如果沒有資料庫，才建立預設資料
        File file = new File(Environment.getDataDirectory().getPath()+"/data/"+packageName+"/databases/database.db");
        if(!file.exists())getDefaultData();//取得預設的新聞資料
        Log.i("checkdatabase", "pass");

//        progressDialog();
        
                      
            
/*
        //寄簡訊請主執行緒的Handler繼續處理事件
        Message message = handler.obtainMessage(1);
        handler.sendMessage(message);
        Log.i("handlestatus", "endsendmess");
        	}
    };
    Log.i("startbrother", "start");
    brother.start();*/
   /* 
    handler = new Handler(){
    	
	    @Override 
	    public void handleMessage(Message msg){ 
	    	switch(msg.what){

 
	        
	    /*
	        //把ProgressDialog關掉
	        myDialog.dismiss();
	        Log.i("prepareToClosePD", "closed");
	    	break;*/
	    	
	/*    	}
	    
	    }
	    
      };*/
    }
    
    
    /**
     *描述 : 系統在onPause()返回或onCreate()時都必經的一個生命週期，藉此週期中將layout的佈局作id關聯和畫面呈現<br/> 
     * 利用上述的這2點特性，讓資料不管進入Setting頻道時，或者是重新開啟時，
     * 都能夠用這個時候去建立id關聯，並在這個時候將資料庫裡_open為True的Cursor，
     * 利用迴圈將其網址解析成List型的getData，最後再放入大容器lliAll裡，
     * 並且每將一個頻道轉成一個getData後，馬上將其內容輸出成主UI的Button和ListView內容，
     * 一個一個的輸出出來。
     * 最後，為了讓使用者能夠在所有頻道的最後面"新增頻道"，
     * 另外又寫了一段生成的按鈕。
     */
    @Override
	protected void onResume() {
	Log.i("onResum", "into");
	super.onResume();
	
	
	if(NewsWeather.updateVersion<BackStage.updateVersion){

		    up_layout =(LinearLayout) findViewById(R.id.up_layout);//找出主畫面上方的水平scrollbar的id位置
		    down_layout = (LinearLayout) findViewById(R.id.down_layout);//找出主畫面下方的水平scrollbar的id位置
		    
		
			myDB = new DB(this);//先建立資料庫，若沒建立直接使用myDB.getTruePath()會出現NullPointerException	
			cursor =myDB.getTruePath();//取得user要看的頻道的資料清單
			
			//一開始先清空所有的view，避免每次都重覆創建子view
			up_layout.removeAllViews();
			down_layout.removeAllViews();
			
			button_order=1;
		
			namelist = new HashMap();
		
			liAll= new HashMap<Integer,List<News>>();
			
				while(cursor.moveToNext()){
					
					//將資料庫內的內容取出放到Button上
					name=cursor.getString(cursor.getColumnIndex("_name"));
					path=cursor.getString(cursor.getColumnIndex("_path"));
					id = cursor.getInt(cursor.getColumnIndex("_id"));
					
					//動態新增按鈕
					button = new Button(NewsWeather.this);
		            button.setText(name);
		            LinearLayout.LayoutParams param =new LinearLayout.LayoutParams(110,65);
		            up_layout.addView(button,param);
		            button.setOnLongClickListener(this);
		            button.setId(id);/*setId和namelist的key值、database的_id相對應，這個id值可能不會照順序而會跳號 */
		            button.setOnClickListener(this);
		            button.setTag(button_order);//setTag是依照使用者的喜好頻道從1設到總筆數,每個button有各自的button_order
		            
		            
		            //動態新增ListView
		            ListView newlv = new ListView(NewsWeather.this);
		            LinearLayout.LayoutParams param2 =new LinearLayout.LayoutParams(800,440);
		            down_layout.addView(newlv,param2);
		            
		            
		            
		            activitymanager=(ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
					Log.i(tag, "getSystemService finish");
					List<ActivityManager.RunningTaskInfo> a=activitymanager.getRunningTasks(10);
					Log.i(tag, "getRunningTasks finish");
					for(ActivityManager.RunningTaskInfo j:a){
						Log.i(tag, "intoFor-loop");
						if(j.baseActivity.getClassName().equals(packageName+".NewsWeather")){
							AppWidgetExist=true;
							boolean d=AppWidgetExist;
							boolean q =d;
							Log.i(tag, "getClassName finish");
						}
					}
					
					
					
				 if(NewsWeather.updateVersion<BackStage.updateVersion){
					if(AppWidgetExist){
						if(MyWidgetProvider.updateVersion==BackStage.updateVersion){
		            		NewsWeather.liAll=MyWidgetProvider.liAll;
		            	}else{
		    		        //開始對每一行的Cursor的網址做解析
		    		        checkEncode(path);//檢查這行Cursor的網址編碼
		    		        encodeTransfer(path);//對檢查出來的編碼做另存檔
		    		        getRss();
		    		            
		    		        liAll.put(button_order, getData);//將轉存的xml檔容器getData再放進大容器liAll
		    				}    
						}	
		            }
		
		            newlv.setAdapter(new NewsAdapter(NewsWeather.this,liAll.get(button_order)));
		            newlv.setOnItemClickListener(this);
		            newlv.setId(button_order);
		            
		            //將取出來的name存入容器
		            namelist.put(id,name);
		            
		            button_order++;
				}
					//更新完才將版本號調為最新號
					NewsWeather.updateVersion=BackStage.updateVersion;
				
				
					//最後生產一個新增頻道按鈕
					button = new Button(NewsWeather.this);
					button.setText("新增頻道");
					LinearLayout.LayoutParams param =new LinearLayout.LayoutParams(110,65);
					up_layout.addView(button,param);
					button.setOnClickListener(this);
					button.setId(9999);
		        
					//滑動選單的初始設定
			        slv = (HorizontalScrollView) findViewById(R.id.hsv);
			        slv.setOnTouchListener(NewsWeather.this);

	        
	        
		}
	}

	//ProgressDialog對話框
    private void progressDialog(){
    	final CharSequence strDialogTitle = getString(R.string.str_dialog_title);
    	final CharSequence strDialogBody = getString(R.string.str_dialog_body);
    	
    	//顯示Progress對話方塊
    	myDialog = ProgressDialog.show(NewsWeather.this, strDialogTitle,strDialogBody);
    	 Log.i("startProgressThread", "start");
    	 
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
    private void checkEncode(String path){
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
    
    
    /**
     * 描述 : encodeTransfer() 將新聞來源轉檔成UTF-8型態的XML檔 <br/>
     * 因XML無法解析BIG5，會出現paraexception(not-well formed(invalid tocken))
     * 所以只要網址一進來，一定存到utf-8的buffxml(列表編號(從1開始)).xml檔裡
     * @param path 傳進來的Rss來源網址
     * @see checkEncode(String path)
     * @see getRss()
     */
    private void encodeTransfer(String path) {
    	
    	  URL url = null;
    	  String buffera="";
			   try {
				   Log.i(tag,"TransferToXML: "+button_order);
				   url = new URL(path);
				   InputStream is = url.openConnection().getInputStream();
				   InputStreamReader isr = new InputStreamReader(is,Encode);
				   BufferedReader br = new BufferedReader(isr);
				   FileOutputStream fos = openFileOutput("buffxml"+button_order+".xml", Context.MODE_PRIVATE);
				   
				   
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

    
	/**描述 : 滑動手勢指令換頁*/
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		
		if(getstart==0){
			getstart = event.getX();
		}else if(event.getAction()!=event.ACTION_MOVE){
			getend =event.getX();
			
			if (getstart-getend >0){
				slv.smoothScrollBy(800, 0);
				
					if(nowview<=button_order){
						nowview++;
					}else{
						nowview=button_order;//button_order最後是一個呈獻筆數總值
					}
				
			}else{
				slv.smoothScrollBy(-800, 0);
				if(nowview>1)nowview--;
			}
			
			getstart=0;
		}		
		return true;
	}
  
	
	
    /**
     * 描述 : 使用解析器將XML轉成List容器 getData<br/>
     * getRss()這個method最主要將存成xml的檔案再轉成hashMap去存放，
     * 好讓之後的顯示和連結都能用get(索引)去控制每筆新聞連結
     * @see checkEncode(String path)
     * @see encodeTransfer(String path)
     */
	private void getRss(){

		
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
			
			/*//編碼為UTF-8直接解析的寫法
//			android.util.Xml.parse(url.openConnection().getInputStream(), Xml.Encoding.UTF_8, myHandler);*/
			
			
	
			FileInputStream fis = openFileInput("buffxml"+button_order+".xml");
			android.util.Xml.parse(fis, Xml.Encoding.UTF_8, myHandler);
			//取得RSS標題與內容列表
			getData = new ArrayList<News>();
			getData = (ArrayList<News>) myHandler.getParasedData();
			
			/*//發送廣播
			Intent i = new Intent(CHANGE_CHANNEL_LIST);
			i.putParcelableArrayListExtra(String.valueOf(button_order), getData);
			sendBroadcast(i);*/
			
			Log.i(tag,"XMLtoGetData_finish");
		}catch(Exception e){
			Log.i("tag", "wrong! "+e.getMessage());
		}
	}
	
	/**描述 : 建立Menu清單*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
		menu.add(0, 0, 0, "頻道清單");
		menu.add(0, 1, 1, "關於");
		menu.getItem(0).setIcon(R.drawable.setting);
		menu.getItem(1).setIcon(R.drawable.about);
		return super.onCreateOptionsMenu(menu);
	}

	/**描述 : 建立Menu清單的觸發事件*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
			case 0:
				Intent intent = new Intent();
				intent.setClass(NewsWeather.this, Setting.class);
				startActivity(intent);

				break;
			case 1:
				new AlertDialog.Builder(NewsWeather.this)
				.setMessage("RssReader v1.0024\n作者：Camangi Corporation\n\n版權所有 2010")
				.setIcon(R.drawable.icon)
				.setTitle("關於")
				
				.setPositiveButton("確認", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
		
					}
				})
				.show();
		}
		return super.onOptionsItemSelected(item);
	}

	/**描述 : Button的觸發事件*/
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		
		case 9999:
			Log.i("into", "99");
			LayoutInflater factory = LayoutInflater.from(NewsWeather.this);
            final View addchannel_layout = factory.inflate(R.layout.alert_dialog_newchannel, null);
				new AlertDialog.Builder(NewsWeather.this)
				.setTitle("新增頻道")
				.setView(addchannel_layout)
				.setPositiveButton("確認", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {	
							
								
								EditText new_channel_name = (EditText) addchannel_layout.findViewById(R.id.new_channel_name);
								String newchannelname=new_channel_name.getText().toString();
								EditText new_channel_path = (EditText) addchannel_layout.findViewById(R.id.new_channel_path);
								String newchannelpath=new_channel_path.getText().toString();
								
								if(newchannelname.equals("") ||newchannelpath.equals("")){
									new AlertDialog.Builder(NewsWeather.this)
									.setTitle("錯誤！")
									.setMessage("請輸入完整方可新增...")
									.setIcon(R.drawable.warning01)
									.setPositiveButton("返回", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {}
										})
									
									.show();
									
									
								}else{
									myDB=new DB(NewsWeather.this);
									myDB.insert(newchannelname, newchannelpath, true);
									onResume();
	
								}
								
						}
						})
				
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
				})
				.show(); 
			break;
			
		default:
			Log.i("into", "default");
		int a=Integer.parseInt(v.getTag().toString());
    	slv.smoothScrollTo(((a-1)*800),0);//因為getTag()取出的值button_order是從1開始，而螢幕起始點是(0,0)
    	nowview=a;
//		v.setBackgroundResource(R.color.brown);//試圖改變背景顏色，結果...	]
    		break;
		}
	}
	
	/**描述 : Button的長按事件*/
	@Override
	public boolean onLongClick(final View v) {
		
		myDB=new DB(NewsWeather.this);
		
		new AlertDialog.Builder(NewsWeather.this)
		
		.setTitle("對於 "+namelist.get(v.getId())+" 頻道，你想要...？")
		.setIcon(R.drawable.q01)
		.setItems(new String[]{"隱藏","重新命名","刪除"}, new DialogInterface.OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which){
				case 0:
					myDB.channelSwitch(v.getId(), false);
					onResume();
					break;
							
				case 1:		
					LayoutInflater factory = LayoutInflater.from(NewsWeather.this);
			            final View rename_layout = factory.inflate(R.layout.alert_dialog_rename, null);
							new AlertDialog.Builder(NewsWeather.this)
							.setTitle("替 "+namelist.get(v.getId())+" 重新命名")
							.setView(rename_layout)
							.setPositiveButton("確認", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {	
											
											EditText edit_rename = (EditText) rename_layout.findViewById(R.id.edit_rename);
											String rename=edit_rename.getText().toString();
											
											if(rename.equals("")){
												new AlertDialog.Builder(NewsWeather.this)
												.setTitle("錯誤！")
												.setMessage("請輸入完整才能更名...")
												.setIcon(R.drawable.warning01)
												.setPositiveButton("返回", new DialogInterface.OnClickListener() {

													@Override
													public void onClick(DialogInterface dialog, int which) {}
													})
												
												.show();
												
												
											}else{
											myDB.reName(v.getId(), rename);
											onResume();
											}

									}
									})
							
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
							})
							.show(); 
							
					break;
					
				case 2:
					try{
							new AlertDialog.Builder(NewsWeather.this)
							.setIcon(R.drawable.warning01)
							.setMessage("這樣會刪除頻道 "+namelist.get(v.getId())+"\n確定嗎？")
							.setTitle("注意！")
							
							.setPositiveButton("確認", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									myDB.delete(v.getId());
									onResume();
								}
							})
							
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							})
							
							.show(); 
					
					}catch(Exception e){
							new AlertDialog.Builder(NewsWeather.this)
						
						
								.setMessage("程式出錯了，將返回！")
								.setTitle("注意！")
								
								.setPositiveButton("確認", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								onResume();
							}
							})
							.show();
					}
					break;
				}
				
			}
			
		})
		.setPositiveButton("返回", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}})
		.show();
		

		return false;
	}

	/**描述 : ListView的觸發事件*/
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		//將大容器liAll裡的小容器getData裡的Link依照nowview(同button_order)的值取出
		String temp1 = liAll.get(nowview).get(position).getLink();
		Intent browserIntent1 = new Intent("android.intent.action.VIEW", Uri.parse(temp1));
		startActivity(browserIntent1);
		
	}
	


}