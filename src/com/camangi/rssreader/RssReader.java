package com.camangi.rssreader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import com.camangi.rssreader.MyWidgetProvider.mReceiver;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;


public class RssReader extends Activity implements OnTouchListener {
	/**
	 * 顯示在"關於"Menu的版本編號
	 */
	private String softVersion="v1.0045b7";
	/**
	 * 描述 : 這個變數用來進階控制上面按鈕的被選取狀態<br/>
	 * 依照滑動的動作，和最左邊x軸的位置，去判斷現在是在第幾個畫面，
	 */
	int currentView;
	/**
	 * 當解析出多少筆資料，就記錄出多少筆的id值所形成的資料庫版本號
	 * 最後再讓他覆寫BackStage.DatabaseNum，
	 * 好讓什麼時候要重覆更新的動作做的更準確
	 */
	public String currentDatabaseNum=""; //我還沒有真正用到這個變數,只有在每筆解析完時做記錄
	/**
	 * 因為Button已經是動態產生，所以只要宣告一個變數，
	 * 之後各個按鈕的控制都靠Button.getId()去的值去決定
	 */
	private static Button button,first_button;
	/**為了讓下面的新聞欄可以左右滑動，需把HorizontalScrollView宣告出來*/
	private static HorizontalScrollView slv,up_lv;
	/**用來存放手勢的第1個值和最後一個值，好比較是往前或往後滑*/
	double getend,getstart=0;
	/**List小容器，一個getData放了一個頻道的資訊,索引值從0開始，
	 *後面的資料放的是News實體。所以可以用News類別的方法將其值取出 
	 */
	private static ArrayList<News> getData;
//	/**判斷xml文件編碼並儲存在Encode*/
//	String Encode;  
	/**描述 : bufferb用來存放從xml複製下來，每一行從BIG5轉成UTF-8的String空間*/
	String bufferb;  
	/**資料載入中的等待視窗*/
	public ProgressDialog myDialog;
	/**用來檢查資料庫在不在，不在才要重建*/
	File file;
	/**現在的畫面，起始為1*/
	private static DB myDB;
	private Cursor cursor;
	/**將資料庫的name,path,int存到hashmap用的變數*/
	static String name,path;
	/**描述 : 這個id是database裡的id,不一定會照順序*/
	static int id;
	/**
	 * 描述 : 記錄頻道按鈕的排序位置<br/>
	 * button_order用來計算使用者總共勾選了幾筆喜好列表，
	 * 這個值是排序的，勾選了幾個頻道就有幾筆資料，從1計數。
	 * 這個值和Button.setTag()、ListView.setTag()、大容器HashMap型態的liAll的key值都是相對應的
	 */
	static int button_order;
	static LinearLayout up_layout,down_layout;//定義上下佈局
	/**
	 * 描述 : 讓Button能夠取到名字的暫存容器<br/>
	 * 專門用來放每一筆的name，好讓刪除視窗出現時，能對應到
	 */
	private static HashMap<Integer,String> namelist;
	/**
	 * 描述 :大容器HashMap<Integer,List<News>>，Integer放的是button_order的排序，List<News>放getData<br/>
	 * 將每一筆getRSS()產生的getData容器，再放入大容器裡。因為button_order起始是1，所以資料會從index=1開始存放。
	 */
	public HashMap<Integer,List<News>> liAll;
	final static String tag ="tag";


	/**取出該Apk的套件名稱	 */
	String packageName;
	private mReceiver Rreceiver_Widget;
	public GetBackStageData Rreceiver_getData;
	/**NewsWeather專屬的更新記錄*/
	public static int updateVersion=0;
	static ActivityManager  activitymanager;
	/**如果MyWidgetProvider.class確實有被開啟的記錄參數 */
	static boolean AppWidgetExist;
	String contentBuffer;
	private Intent intent;
	private static Intent intent2;
	IntentFilter mFilter1,mFilter2;
	int screen_width;
	public static Handler handler1;
	private static final int open=1;
	private static final int close=0;
	private static boolean receiver_getData_status=false;
	public static String pathyouwanttoadd,nameyouwanttoadd="";

	EditText newname;
	EditText newpath;
	
	int curChoiceButton;
	private boolean service_getData_status=false;
	
	
	public final static int initiateRssReaderUIthenStartBackStageService=0;
	public final static int setTitleStatus=1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(tag,"into RssReader.onCreate()");
        
        Log.i(tag, "Language is: "+BackStage.getCountry(RssReader.this));
        
        packageName=this.getPackageName();    
       
        BackStage.initializeDatabase(RssReader.this);
        BackStage.DatabaseNumber="none";//背景資料庫編號預設為0才會重新解析頻道 

        

        screen_width=BackStage.ScreenSize.getScreenWidth(this);  
        Log.i(tag, "Screen Size is: "+String.valueOf(screen_width)+"*"+String.valueOf(BackStage.ScreenSize.getScreenHeight(this)));
        
    	//每onResume啟動時馬上註冊廣播，不能寫在onCreate()，因為每個函式間都有各自的生命週期
    	//向系統註冊Receiver1，讓MyWidgetProvider.mReceiver產生功能
        mFilter1=new IntentFilter(BackStage.CHANGE_LIST_IMMEDIATE);
        Rreceiver_Widget = new MyWidgetProvider.mReceiver();
        registerReceiver(Rreceiver_Widget,mFilter1);//MyWidgetProvider.mReceiver()的<IntentFilter>是CHANGE_LIST_IMMEDIATE
        Log.i(tag, "registerReceiverIntentFilter 1 is: CHANGE_LIST_IMMEDIATE");
        
        
        ;
     	
        handler1=new Handler(){

		

			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case initiateRssReaderUIthenStartBackStageService:
					/**
					 * handler1甪來初始化畫面，並產生first_button，
					 */
					Log.i(tag, "into handler1");
					   up_layout =(LinearLayout) findViewById(R.id.up_layout);//找出主畫面上方的水平scrollbar的id位置
					   down_layout = (LinearLayout) findViewById(R.id.down_layout);//找出主畫面下方的水平scrollbar的id位置		    
				
					   
						//滑動選單的初始設定
				       up_lv = (HorizontalScrollView) findViewById(R.id.up_sv);
				       slv = (HorizontalScrollView) findViewById(R.id.hsv);
				       slv.setOnTouchListener(RssReader.this);
				       
//				       BackStage.button_order=0;
				       String buffer="";
				       buffer=BackStage.checkDatabaseNumber(RssReader.this);
				       
				       if(!BackStage.DatabaseNumber.equals(String.valueOf(buffer))){
				    	   Log.i(tag, "checkDatabaseNumber is:"+buffer+", BackStage.DatabaseNumber is: "+BackStage.DatabaseNumber+", StartService..");
				    	   BackStage.DatabaseNumber=buffer;
				    	   
				    	   //一開始先清空所有的view，避免每次都重覆創建子view
				    	   up_layout.removeAllViews();
				    	   down_layout.removeAllViews();
				    	   
				    	   
				    	   if(BackStage.BufferDatabaseNumber!=BackStage.checkDatabaseNumber(RssReader.this)){
				    		 //程式一開始,第1個按鈕顯示為[取消載入]按鈕
					    	   first_button = new Button(RssReader.this);
					    	   first_button.setTextSize(16);
					    	   first_button.setPadding(20, 0, 20, 0);
					    	   first_button.setText(R.string.cancel_loading);
					    	   first_button.setEnabled(false);//還沒載入至少1筆資料,不能執行[取消載入]功能
					    	  

					    	   LinearLayout.LayoutParams param =new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,65);
						       up_layout.addView(first_button,param);
						       
						       //這個時候還是[取消載入]的字樣
						       first_button.setOnClickListener(new View.OnClickListener() {
					    		
									@Override
									public void onClick(View v) {
//										
										Log.i(tag, "you press Button Stop!");
										
								          
										switchBroadcaast_getData(false);//告訴系統將receiver_getData_status關閉
										 Log.i(tag, "stopReceiver: getData");
								           Toast.makeText(RssReader.this, getString(R.string.cancel_loading)+"...", Toast.LENGTH_SHORT).show();
								           //接著把Widget打開
								           sendBroadForSwitchWidget(open);
								           Log.i(tag, "start WidgetService");
								           //在Title告知使用者在什麼時候沒有載入資料了
								           setTitle((getString(R.string.from_num_no_load).replace("#title",(BackStage.button_order+1)+"/"+BackStage.cursor.getCount()+" "+BackStage.name)));
								           Log.i(tag, "setTitle tell user not finish");
								        	   
								        	   
								        		 //關閉解析的Service
								           		switch_Service_getData(false,RssReader.this);
									           Log.i(tag, "stopService: BackStage");
								        	   
									       
									     
									        	   
									        	   first_button.setClickable(false);


								           
								           first_button.setPadding(30, 0, 30, 0);
								           //現在是[重新載入]:當使用者按下[取消載入]按鈕後,第1個按鈕馬上變成[重新載入]按鈕,讓使用者可以重載看看
								           first_button.setText(R.string.reloading);
										   first_button.setOnClickListener(new View.OnClickListener() {
											
											@Override
											public void onClick(View v) {
												Log.i(tag, "you press reload!");
												BackStage.letThreadSleep(1000);
												//要先將資料庫的版本設回none，重新載入時才會更新
												BackStage.DatabaseNumber="none";
												Log.i(tag, "BackStage.databasenum set to none");
												BackStage.button_order=0;
												Log.i(tag,"BackStage.button_order set to 0");
//												sendBroadForSwitchWidget(close);
												
												onResume();
												Log.i(tag, "start onResume()");
											}
										});
									}
								});
				    	   }
				    	   
				    	   
				    	   
				    	   
				    	   
				    	   sendBroadForSwitchWidget(close);//關閉Service
				    	   Log.i(tag, "stop Widget Service");

				    	   
				    	 //啟動Service以解析資料
				    	   if(service_getData_status==false){
				    		   switch_Service_getData(true,RssReader.this);
					           Log.i(tag, "start BackStage.service()");
				    	   }
				    	   
				           
				    	   
				           
				       }else{
				    	   Log.i(tag, "checkDatabaseNumber is:"+buffer+", BackStage.DatabaseNumber is: "+BackStage.DatabaseNumber+", doing nothing..");
				       }
					break;
				case setTitleStatus:
					Log.i(tag, "into handler2: setTitle");
					setTitle(getString(R.string.loading)+" "+(BackStage.button_order+1)+"/"+BackStage.cursor.getCount()+": "+BackStage.name);
					break;
				}
				super.handleMessage(msg);
			}
    		
    	};
    }
    
    
    @Override
	protected void onStart() {
		super.onStart();
		myDB = new DB(RssReader.this);
		cursor =myDB.getTruePath();
		//如果使用者按了Home鍵再返回到RssReader,Title會抓出Service還在解析中的頻道狀態
		if/*(BackStage.button_order!=0)*/(getData!=null&receiver_getData_status==true&BackStage.button_order!=cursor.getCount()){
			handler1.sendEmptyMessage(setTitleStatus);
		}
		cursor.close();
		myDB.close();
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
    Log.i(tag, "=====================================");
	Log.i(tag, "into RssReader.onResume()");
	super.onResume();
	
/*	 up_layout =(LinearLayout) findViewById(R.id.up_layout);//找出主畫面上方的水平scrollbar的id位置
	if(up_layout.getChildCount()==0){
		//啟動getData的Service以解析資料
		   if(service_getData_status==false){
			   switch_Service_getData(true,RssReader.this);
	        Log.i(tag, "start BackStage.service()");
		   }
	}*/
	 //啟動getData的Receiver以接收解析資料
	if(receiver_getData_status==false){
		switchBroadcaast_getData(true);
	}
	
	
	myDB = new DB(RssReader.this);
	cursor =myDB.getTruePath();
	
	if(getData!=null&receiver_getData_status==true&BackStage.button_order!=cursor.getCount()){
		Log.i(tag, "into onResume().setTitle");
		Log.i(tag, "BackStage.button_order= "+BackStage.button_order);
		Log.i(tag, "cursor.getCount(): "+cursor.getCount());
		setTitle(getString(R.string.loading)+" "+(BackStage.button_order+1)+"/"+BackStage.cursor.getCount()+": "+BackStage.name);
	}
	cursor.close();
	myDB.close();
	

		final Thread t =new Thread(){
			
			public void run(){
				Looper.prepare();
				
				Net.switchWifi(RssReader.this,true);
				Log.i(tag, "WifiStatus: "+Net./*checkInitWifiStatus*/checkEnableingWifiStatus(RssReader.this));

			}
		};

		if(!(Net.check3GConnectStatus(RssReader.this)|Net.checkEnableingWifiStatus(RssReader.this))){
			Log.i(tag, "into if");
//			
			new AlertDialog.Builder(RssReader.this)
    		
    		.setTitle(R.string.no_internet_what_do_you_want)
    		.setIcon(R.drawable.question)
    		.setItems(new String[]{getString(R.string.connect_to_default_WIFI),getString(R.string.back_to_set)}, new DialogInterface.OnClickListener(){
    			
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				switch(which){
    				case 0:
    					t.start();
    					
    					Log.i(tag, "start ProgressDialog");
    					BackStage.WifiWaitDialog(RssReader.this);//開啟ProgressDialog視窗
    					
    					break;
    							
    				case 1:		
    					finish();
    					break;
    					
    				
    				}
    				
    			}
    			
    		})

    		.show();
         }
		setTitle(getString(R.string.app_name));
		BackStage.ifWifiPassThenSendMessage(this);
	}
	


	@Override
	protected void onDestroy() {
		Log.i(tag, "into RssReader.onDestroy()");
		
		switch_Service_getData(false,RssReader.this);
		
		unregisterReceiver(Rreceiver_Widget);
		Log.i(tag, "unregisterR_W");
		switchBroadcaast_getData(false);
		BackStage.button_order=0;
		
		Log.i(tag, "Because Destroy, close 2 Receiver & BackStage.service");
		finish();
		super.onDestroy();
	}
	
	@Override  /**描述 : 滑動手勢指令換頁*/
	public boolean onTouch(View v, MotionEvent event) {
//		Log.i(tag, String.valueOf(v.get));
		HorizontalScrollView horizontalScrollView=(HorizontalScrollView)v;
		LinearLayout linearLayout=(LinearLayout) horizontalScrollView.getChildAt(0);
		

		int a=horizontalScrollView.getScrollX()/BackStage.ScreenSize.getScreenWidth(RssReader.this);

		
		if(getstart==0){
			getstart = event.getX();
//			Log.i(tag, "start: "+String.valueOf(getstart));
		}else if(event.getAction()!=event.ACTION_MOVE){
			getend =event.getX();
//			Log.i(tag, "end: "+String.valueOf(getend));
			
			//向左移
			if (getstart-getend >/*50*/0 & getstart>(BackStage.ScreenSize.getScreenWidth(RssReader.this)/2) && getend<(BackStage.ScreenSize.getScreenWidth(RssReader.this)/2)){			
				slv.smoothScrollBy(screen_width, 0);		
				Log.i(tag, "turn left");
				
				
				if(a==0){
					currentView=1;
				}else{
					if(a<linearLayout.getChildCount()-1){
						currentView=a+1;	
					}
					
				}
				Log.i(tag, "current view: "+currentView);
				
				//一滑動時，將所有的Button都先預設回沒有被選取的狀態
				for(int i=0;i<linearLayout.getChildCount()-1;i++){
					up_layout.findViewWithTag(i).setBackgroundResource(R.drawable.button);	
				}
				//然後再將已經選取的按鈕設為被選取的狀態
				up_layout.findViewWithTag(currentView).setBackgroundResource(R.drawable.button_press);
				
				//當該頻道的Button顯示在螢幕外時,往內移動並顯示在螢幕
				Log.i(tag, "button location: "+String.valueOf(up_layout.findViewWithTag(currentView).getRight()));
				Log.i(tag, "layout right is: "+up_layout.getRight());
				//找到正要出現的頻道Button,然後移動這個button的距離,好讓button是存在在使用者看的見的位置
				up_lv.scrollBy(up_layout.findViewWithTag(currentView).getRight()-up_layout.findViewWithTag(currentView).getLeft(),0);
				
			//向右移	
			}else if(getstart-getend </*-50*/0 && getstart<(BackStage.ScreenSize.getScreenWidth(RssReader.this)/2) && getend>(BackStage.ScreenSize.getScreenWidth(RssReader.this)/2)){
				slv.smoothScrollBy(-screen_width, 0);		
				Log.i(tag, "turn right");
				
				if(a>0){
					currentView=a-1;
				}else{
					currentView=0;		
				}
				
				Log.i(tag, "current view: "+currentView);
				
				//一滑動時，將所有的Button都先預設回沒有被選取的狀態
				for(int i=0;i<linearLayout.getChildCount();i++){
					up_layout.findViewWithTag(i).setBackgroundResource(R.drawable.button);	
				}
				//一滑動時，將所有的Button都先預設回沒有被選取的狀態
				up_layout.findViewWithTag(currentView).setBackgroundResource(R.drawable.button_press);
				
				//當該頻道的Button顯示在螢幕外時,往內移動並顯示在螢幕
				Log.i(tag, "button location: "+String.valueOf(up_layout.findViewWithTag(currentView).getLeft()));

				//一旦往左移動時,上面的滑動頻道是隨著"剛剛"的Button大小去移動現在的大小
				up_lv.scrollBy(up_layout.findViewWithTag(currentView+1).getLeft()-up_layout.findViewWithTag(currentView+1).getRight(),0);

				
			}

			getstart=0;
		}
		return true;
	}

	/**
	 * 描述 : 若呼叫此方法，會寄出廣播讓Widget的Service停止<br/>
	 * 但是UpdateService因為是Widget，不會真的完全停止。
	 * 而是下次執行時，UpdateService又會重onCreate()啟動
	 */
    private void sendBroadForSwitchWidget(int status){	
    	
    	  if(status==close){
	    	   Log.i(tag, "==>RssReader.sendBroadForStopWidget(), status is close");
	       }else{
	    	   Log.i(tag, "==>RssReader.sendBroadForStartWidget(), status is opne");
	    	   MyWidgetProvider.liAll=BackStage.liAll;
	    	   MyWidgetProvider.widget_namelist=BackStage.backstage_widget_namelist;
	       }
    	  
	        //發送廣播來即時更改Widget
	       Intent intent = new Intent();
	       intent.putExtra("status", status);
	       intent.setAction(BackStage.CHANGE_LIST_IMMEDIATE);
	       sendBroadcast(intent);	       

    }
    
    


    
	private void createNewChannelButton(){

		//生產一個新增頻道按鈕

		 LayoutInflater factory = LayoutInflater.from(RssReader.this);
         View addchannel_layout = factory.inflate(R.layout.alert_dialog_newchannel, null);

          newpath=(EditText) addchannel_layout.findViewById(R.id.new_channel_path);
          newpath.setFocusable(true);
          newpath.setText(R.string.type_path_here);
         
         
				new AlertDialog.Builder(RssReader.this)
				.setTitle(R.string.add_channel)
				.setView(addchannel_layout)
				.setIcon(R.drawable.add_channel)
				.setPositiveButton(R.string.verify, new DialogInterface.OnClickListener() {
		
				@Override
				public void onClick(DialogInterface dialog, int which) {

					String newchannelpath=newpath.getText().toString();
					pathyouwanttoadd=newchannelpath;
					nameyouwanttoadd="";

					
//						if(newchannelpath.equals("")){
//						new AlertDialog.Builder(RssReader.this)
//						.setTitle(R.string.error)
//						.setMessage(R.string.enter_completely_to_verify)
//						.setIcon(R.drawable.warning)
//						.setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
//				
//						@Override
//						public void onClick(DialogInterface dialog, int which) {}
//						})
//						.show();	
//						}
						
//						//如果在Handler裡的endDoucument()將checkParse值改成true，代表解析成功，那麼就跑這段
						if(BackStage.verifyPath(RssReader.this,pathyouwanttoadd)!=null&BackStage.parseFinish==true){
							

								//當驗證完畢回剛剛的新增訊息視窗,此時的頻道名稱已從剛剛解析的過程中抓出來,現在也可以被編輯了
								LayoutInflater factory = LayoutInflater.from(RssReader.this);
					            View addchannel_layout = factory.inflate(R.layout.alert_dialog_verified, null);
					            final EditText newname=(EditText) addchannel_layout.findViewById(R.id.new_channel_name);
					            newname.setText(nameyouwanttoadd);
					            newname.setFocusable(true);
					            EditText newpath=(EditText) addchannel_layout.findViewById(R.id.new_channel_path);
					            newpath.setText(pathyouwanttoadd);
					            newpath.setFocusable(false);
					            
					            new AlertDialog.Builder(RssReader.this)
								.setTitle(R.string.verification_established)
								.setIcon(R.drawable.verify)
								.setView(addchannel_layout)
								.setMessage(R.string.click_Add_continue)
								.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
						
								@Override
								public void onClick(DialogInterface dialog, int which) {
						
//									if(newname.equals("") ||pathyouwanttoadd.equals("")){
//										new AlertDialog.Builder(RssReader.this)
//										.setTitle(R.string.error)
//										.setMessage(R.string.add_when_completely)
//										.setIcon(R.drawable.warning)
//										.setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
//								
//										@Override
//										public void onClick(DialogInterface dialog, int which) {}
//										})
//							
//										.show();
//									}
									
									myDB=new DB(RssReader.this);
									myDB.insert(newname.getText().toString(), pathyouwanttoadd, true);
									myDB.close();
									
									myDB=new DB(RssReader.this);
									cursor=myDB.getTruePath();
									cursor.moveToLast();
									name=cursor.getString(cursor.getColumnIndex("_name"));
									path=cursor.getString(cursor.getColumnIndex("_path"));
									button = new Button(RssReader.this);
									id = cursor.getInt(cursor.getColumnIndex("_id"));
//									
									ArrayList<News> buffer=BackStage.verifyPath(RssReader.this,path);
									BackStage.liAll.put(BackStage.button_order, buffer);

									BackStage.rssreader_namelist.put(id,name);
									BackStage.backstage_widget_namelist.put(BackStage.button_order,name);
									
							        button.setText(name);
							        button.setTextSize(16);
							        button.setTextColor(Color.YELLOW);        
							        button.setBackgroundResource(R.drawable.main_button_background);

							        button.setId(id);/*setId和namelist的key值、database的_id相對應，這個id值可能不會照順序而會跳號 */
//							        Log.i(tag, "move to last id: "+id);
//							        Log.i(tag, "setting tag is: "+BackStage.button_order);
							        

							        LinearLayout.LayoutParams param =new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,65);
							        button.setPadding(20, 0, 20, 0);
							        up_layout.addView(button,param);
							        
							        button.setTag(BackStage.button_order);//setTag是依照使用者的喜好頻道從1設到總筆數,每個button有各自的button_order
							        buttonClickListener(button,RssReader.this);
							        buttonLongClickListener(button,RssReader.this);
							        
							        
							        //動態新增ListView
								    ListView newlv = new ListView(RssReader.this);
								    LinearLayout.LayoutParams param2 =new LinearLayout.LayoutParams(screen_width,LinearLayout.LayoutParams.FILL_PARENT);
								    down_layout.addView(newlv,param2);
								    
								    newlv.setTag(BackStage.button_order);
							        newlv.setAdapter(new NewsAdapter(RssReader.this,buffer));
							        ListViewListener(newlv,RssReader.this);
							        
							        BackStage.DatabaseNumber+=id;//後台的資料庫版本要順便更新,免得新增頻道後看新聞又返回一直重載入
							        BackStage.button_order++;
									
							        cursor.close();//補加了這2行，但沒測能不能通
							        myDB.close();
							        
							        //將2筆剛剛暫存的新增頻道和名稱的變數清空
							        nameyouwanttoadd="";
							        pathyouwanttoadd="";
//							       BackStage.bufferlist=null;
							        
								}
								})
								.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
									})
								.show(); 
					            
					            
						}else{
							//如果在Handler裡的endDoucument()沒有將checkParse值改成true，代表沒有真正解析成功，那麼就跑這段
							Toast.makeText(RssReader.this, R.string.cant_parse, Toast.LENGTH_SHORT).show();
							
							
						}
            
					}	
				})
				
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
				})
				.show(); 	

	}

	private void buttonClickListener(final Button button, final Context context) {
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

       		int a=Integer.parseInt(v.getTag().toString());
       		Log.i(tag, "tag is: "+v.getTag());
           	slv.smoothScrollTo((a*screen_width),0);//因為getTag()取出的值button_order是從1開始，而螢幕起始點是(0,0)
           	
           	Log.i(tag, "up_layout count: "+up_layout.getChildCount());
           	
           	//以下這段判斷如果1個按鈕被點擊了,另一個按鈕就還原,不要被點擊
           	for(int i=0;i<up_layout.getChildCount()-1;i++){
//           		Log.i(tag, "i:"+i);
				up_layout.findViewWithTag(i).setBackgroundResource(R.drawable.button);	
			}
			up_layout.findViewWithTag(a).setBackgroundResource(R.drawable.button_press);
			         	 	
 
            }
        });
		
	}
	
	
	private void buttonLongClickListener(Button button, final Context context){
		
		button.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(final View v) {
            	if(BackStage.button_order<BackStage.cursor.getCount()-1){
            		BackStage.loadingCantUseDataDialog(RssReader.this);
            	}else{
        		myDB=new DB(context);


       		new AlertDialog.Builder(context)
       		.setTitle(getString(R.string.what_do_you_want).replace("#title",BackStage.rssreader_namelist.get(v.getId())))
       		.setIcon(R.drawable.question)
       		.setItems(new String[]{getString(R.string.hide),getString(R.string.rename),getString(R.string.delete)}, new DialogInterface.OnClickListener(){
       			
       			@Override
       			public void onClick(DialogInterface dialog, int which) {
       				switch(which){
       				case 0:
       					if(up_layout.getChildCount()==1){
       						BackStage.remainOneChannel(RssReader.this);//跳出至少要保留一筆頻道的視窗
       					}else{
       							if(BackStage.button_order<BackStage.cursor.getCount()-1){
			 					BackStage.loadingCantUseDataDialog(RssReader.this);
       							}else{
	       					    myDB.channelSwitch(v.getId(), false); 
	       					    myDB.close();
	       				        onResume();
			 				    }
       					}
       					break;
       							
       				case 1:		
       					if(BackStage.button_order<BackStage.cursor.getCount()-1){
			 					
			 					BackStage.loadingCantUseDataDialog(RssReader.this);
			 				}else{
			 					LayoutInflater factory = LayoutInflater.from(context);
			 					
			 					final View rename_layout = factory.inflate(R.layout.alert_dialog_rename, null);
			 					EditText et = (EditText) rename_layout.findViewById(R.id.edit_rename);
			 					et.setText(BackStage.rssreader_namelist.get(v.getId()));
			 					

			 					
			 					
       							new AlertDialog.Builder(context)
       							.setTitle(getString(R.string.rename_for).replace("#title", BackStage.rssreader_namelist.get(v.getId())))
       							.setView(rename_layout)
       							
       							
       							.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

       									@Override
       									public void onClick(DialogInterface dialog, int which) {	
       											
       											EditText edit_rename = (EditText) rename_layout.findViewById(R.id.edit_rename);
       											String rename=edit_rename.getText().toString();
       											
       											//如果新名稱仍是原來的名字,則不做作何動作 
       											if(rename.equals(BackStage.rssreader_namelist.get(v.getId()))){
       												
       											}else if(rename.equals("")){//如果是空白就另開視窗告知錯誤
       											
       												
       												String back = (String) getResources().getText(R.string.back);
       												new AlertDialog.Builder(context)
       												.setTitle(getString(R.string.error))
       												.setMessage(getString(R.string.enter_completely_to_rename))
       												.setIcon(R.drawable.warning)
       												.setPositiveButton(back, new DialogInterface.OnClickListener() {

       													@Override
       													public void onClick(DialogInterface dialog, int which) {}
       													})
       												
       												.show();
       												
       												
       											}else{//如果真的輸入名稱了,就重新載入主UI
	        											myDB.reName(v.getId(), rename);
	        											myDB.close();
	        											Log.i(tag, "intoONRESUME()");
	        											BackStage.DatabaseNumber="none";//要將資料庫重設,否則不會即時更新資料
	        											onResume();
       											}

       									}
       									})
       							
       							.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
       								
       									@Override
       									public void onClick(DialogInterface dialog, int which) {
       									}
       							})
       							.show(); 

			 				}
       					break;
       					
       				case 2:
       					if(BackStage.button_order<BackStage.cursor.getCount()-1){
			 					
			 					BackStage.loadingCantUseDataDialog(RssReader.this);
			 				}else{
			 					if(up_layout.getChildCount()==1){
		       						BackStage.remainOneChannel(RssReader.this);//跳出至少要保留一筆頻道的視窗
		       					}else{
       					try{

       						
       							new AlertDialog.Builder(context)
       							.setIcon(R.drawable.warning)
       							.setMessage(getString(R.string.are_you_sure_delete).replace("#title", BackStage.rssreader_namelist.get(v.getId())))
       							.setTitle(getString(R.string.attention))
       							
       							.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
       								
       								@Override
       								public void onClick(DialogInterface dialog, int which) {
       									myDB.delete(v.getId());
       									myDB.close();
       									onResume();
       								}
       							})
       							
       							.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
       								
       								@Override
       								public void onClick(DialogInterface dialog, int which) {
       								}
       							})
       							
       							.show(); 
       					
       					}catch(Exception e){
       						
   							
   							
       							new AlertDialog.Builder(context)
       							
       								.setMessage("程式出錯了，將返回！")
       								.setTitle(getString(R.string.attention))
       								
       								.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
       							
       							@Override
       							public void onClick(DialogInterface dialog, int which) {
       								onResume();
       							}
       							})
       							.show();
       					}
			 				}
			 				}
       					break;
       				}
       				
       			}
       			
       		})
       		.setPositiveButton(getString(R.string.back), new DialogInterface.OnClickListener() {
       					
       					@Override
       					public void onClick(DialogInterface dialog, int which) {
       					}})
       		.show();
       		
            	}
       		return false;
            }					        
       });
	}
	
	private void ListViewListener(ListView newlv, final Context context){
        newlv.setOnItemClickListener(new OnItemClickListener(){
        	
        	/**描述 : ListView的觸發事件*/
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//將大容器liAll裡的小容器getData裡的Link依照parent.getTag(),也就是該ListView所設的tag(同button_order)的值取出
				
				Log.i(tag, "parent.getTag(): "+parent.getTag().toString());
				Log.i(tag, "ListView getLink: "+BackStage.liAll.get(Integer.parseInt(parent.getTag().toString())).get(position).getLink());
				Intent browserIntent1 = new Intent("android.intent.action.VIEW", Uri.parse(BackStage.liAll.get(Integer.parseInt(parent.getTag().toString())).get(position).getLink()));
				context.startActivity(browserIntent1);
				
			}
        	
        });
	}
	
	


	


	/**
	 * 描述 : 如果資料還沒有載入完就讓會影響到資料庫的相關指令變成失效
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(receiver_getData_status==false){//如果按了[取消載入],退回桌面再進入,為了讓Menu變回原樣,特設此判斷
			 BackStage.button_order=BackStage.cursor.getCount()-1;
		}
		
		if(BackStage.button_order<BackStage.cursor.getCount()-1){
			menu.getItem(0).setEnabled(false);
			menu.getItem(1).setEnabled(false);
			menu.getItem(2).setEnabled(false);
		}else{
			menu.getItem(0).setEnabled(true);
			menu.getItem(1).setEnabled(true);
			menu.getItem(2).setEnabled(true);
		}
	
		return super.onPrepareOptionsMenu(menu);
	}


	/**描述 : 建立Menu清單*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0, 0, 0, R.string.add_channel);
		menu.add(0, 1, 1, R.string.channel_list);
		menu.add(0, 2, 2, R.string.reloading);
		menu.add(0, 3, 3, R.string.contact_author);
		menu.getItem(0).setIcon(R.drawable.add_channel);
		menu.getItem(1).setIcon(R.drawable.setting);
		menu.getItem(2).setIcon(R.drawable.refresh);
		menu.getItem(3).setIcon(R.drawable.about);
		
		return super.onCreateOptionsMenu(menu);
	}
	
/*	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_MENU){
			Log.i(tag, "press_keyMenu");

		}
		return super.onKeyDown(keyCode, event);
	}*/

	/**描述 : 建立Menu清單的觸發事件*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case 0:
				Log.i(tag, "now button order is: "+BackStage.button_order);

					createNewChannelButton();	

				break;
			
			case 1:
				Intent intent = new Intent();
				intent.setClass(RssReader.this, Setting.class);
				startActivity(intent);

				break;
				
			case 2:
			
				//要先將資料庫的版本設回none，重新載入時才會更新
				BackStage.DatabaseNumber="none";
				onResume();

				break;
				
			case 3:
				new AlertDialog.Builder(RssReader.this)
				.setMessage(getString(R.string.app_name)+" "+ softVersion +"\n"+getString(R.string.author)+"：Camangi Corporation\n\n"+getString(R.string.copyright)+" 2010")
				.setIcon(R.drawable.icon)
				.setTitle(getString(R.string.about))
				.setPositiveButton(getString(R.string.report_problem), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent sendIntent = new Intent(Intent.ACTION_SEND);
						sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"simon@camangi.com"}); 
						sendIntent.putExtra(Intent.EXTRA_TEXT, "write your suggestion here");
						sendIntent.putExtra(Intent.EXTRA_SUBJECT, "RssReader issue report");
						sendIntent.setType("message/rfc822");
						startActivity(Intent.createChooser(sendIntent, "Title:"));
					}
				})
				.setNeutralButton(getString(R.string.back), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
		
					}
				})
				.show();
				break;
		
		}
		return super.onOptionsItemSelected(item);
	}


	
	
	private void switchBroadcaast_getData(boolean open){
		if(open){
			//向系統註冊Receiver2，讓RssReader.GetBackStageData產生功能，專收從BackStage來的實體
	           mFilter2=new IntentFilter(BackStage.GET_NEW_ENTITY);
	           Rreceiver_getData=new RssReader.GetBackStageData();
	           registerReceiver(Rreceiver_getData,mFilter2);
	           Log.i(tag, "start Receiver Receiver_get_Data");
	           receiver_getData_status=true;        
		}else{
				unregisterReceiver(Rreceiver_getData);
		    	receiver_getData_status=false;
		    	Log.i(tag, "close Receiver_get_Data");
		}
	}
	
	private void switch_Service_getData(boolean open,Context context){
		if(open){
			   intent2 = new Intent(context, BackStage.class);
			   context.startService(intent2);
			   service_getData_status=true;
			   Log.i(tag, "start Service_getData");
		}else{
	    	   intent = new Intent(RssReader.this, BackStage.class);
	           RssReader.this.stopService(intent);
	           service_getData_status=false;
	           Log.i(tag, "close Service_getData");
		}
	}



	public class GetBackStageData extends BroadcastReceiver{
//		HashMap<Integer,List<News>> liAll = new HashMap<Integer,List<News>>();

		@Override
		public void onReceive(final Context context, Intent intent) {
			Log.i(tag, ">=RssReader.GetBackStageData.onReceive(), get entity name:"+intent.getExtras().getString("entity_name"));
				
			
		    
			   name = intent.getExtras().getString("entity_name");
			   button_order=intent.getExtras().getInt("button_order");
			   Log.i(tag, "get button_order: "+button_order);
			   id = intent.getExtras().getInt("id");
			   getData=(ArrayList<News>) intent.getSerializableExtra("getData");

			   
			   first_button.setEnabled(true);//資料還沒撈出第1筆,先把[取消載入]的功能關閉
			  
			   BackStage.liAll.put(button_order, getData);
			  
				//開始動態新增按鈕
				button = new Button(context);
				button.setTextColor(Color.WHITE);
				button.setTextSize(16);
				button.setBackgroundResource(R.drawable.main_button_background);
		        button.setText(name);
		        button.setPadding(20, 0, 20, 0);
//		        Log.i(tag, "setname pass");
		        button.setId(id);/*setId和namelist的key值、database的_id相對應，這個id值可能不會照順序而會跳號 */
		        
		        LinearLayout.LayoutParams param =new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,65);
//		        Log.i(tag, "setlinearlayout pass");
		        up_layout.addView(button,param);
//		        Log.i(tag, "set up_layout pass");
		        
		        buttonLongClickListener(button, RssReader.this);
		        buttonClickListener(button,RssReader.this);

		        button.setTag(button_order);//setTag是依照使用者的喜好頻道從1設到總筆數,每個button有各自的button_order
		        if(button_order==0)button.setBackgroundResource(R.drawable.button_press);
		        
		        //動態新增ListView
			    ListView newlv = new ListView(context);
			    LinearLayout.LayoutParams param2 =new LinearLayout.LayoutParams(screen_width,LinearLayout.LayoutParams.FILL_PARENT);
			    down_layout.addView(newlv,param2);
			    newlv.setTag(button_order);
		        newlv.setAdapter(new NewsAdapter(context,getData));
		        ListViewListener(newlv,RssReader.this);
				
//		        currentDatabaseNum+=id;//當確定解析完也增加出Button和ListView後，整理出現有的資料庫編號，為防之後判斷何時要重整UI能更準確
		        
//		        Log.i(tag, "currentDatabaseNum= "+currentDatabaseNum);
					        
					      //啟動Service以解析資料
		        Log.i(tag, "service_getdATA_STATUS: "+service_getData_status);
//		        		if(service_getData_status==false){
		        			switch_Service_getData(true,RssReader.this);
//		        		}
		        		

					        
							   Log.i(tag, "BackStage.cursor count= "+BackStage.cursor.getCount()+", BackStage.liAll.size()= "+BackStage.liAll.size());
					        if(BackStage.cursor.isLast()){
					        	switch_Service_getData(false,context);
					        
								   Log.i(tag, "Data load finish, stop (Service)BackStage");
								   
								   Toast.makeText(context, getString(R.string.loading_completed), Toast.LENGTH_SHORT).show();
								   
								   //當資料都下載完了,[取消載入]就變成[新增頻道]的功能
								   first_button.setTextColor(Color.WHITE);
								   first_button.setText(R.string.add_channel);
								   first_button.setBackgroundResource(R.drawable.newchannel_button_background);
								   first_button.setOnClickListener(new View.OnClickListener() {
									
									@Override
									public void onClick(View v) {
										createNewChannelButton();
									}
								});
								   
								   
								   setTitle(getString(R.string.app_name));
								   sendBroadForSwitchWidget(open);					  
							   }
					        
							   
						}

		

		}


}
