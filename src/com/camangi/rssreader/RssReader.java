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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.SAXException;

import com.camangi.rssreader.MyWidgetProvider.UpdateService;
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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
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
import android.widget.Toast;


public class RssReader extends Activity implements OnTouchListener {
	/**
	 * 顯示在"關於"Menu的版本編號
	 */
	private String softVersion="v1.0033";
	/**
	 * 因為Button已經是動態產生，所以只要宣告一個變數，
	 * 之後各個按鈕的控制都靠Button.getId()去的值去決定
	 */
	private static Button button;
	/**為了讓下面的新聞欄可以左右滑動，需把HorizontalScrollView宣告出來*/
	private static HorizontalScrollView slv;
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
	private mReceiver Rreceiver1;
	public GetBackStageData Rreceiver2;
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




	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(tag,"into RssReader.onCreate()");
        
        
        packageName=this.getPackageName();
        BackStage bs = new BackStage();
        bs.initializeDatabase(RssReader.this);
        bs.DatabaseNumber="none";
        

        screen_width=BackStage.ScreenSize.getScreenWidth(this);  
        Log.i(tag, "Screen Size is: "+String.valueOf(screen_width)+"*"+String.valueOf(BackStage.ScreenSize.getScreenHeight(this)));
        
        
        	
       
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
	
//	Net.autoWifi(this);
	
	//每onResume啟動時馬上註冊廣播，不能寫在onCreate()，因為每個函式間都有各自的生命週期
	   //向系統註冊Receiver1，讓MyWidgetProvider.mReceiver產生功能
    mFilter1=new IntentFilter(BackStage.CHANGE_LIST_IMMEDIATE);
    Rreceiver1 = new MyWidgetProvider.mReceiver();
    registerReceiver(Rreceiver1,mFilter1);//MyWidgetProvider.mReceiver()的<IntentFilter>是CHANGE_LIST_IMMEDIATE
    Log.i(tag, "registerReceiverIntentFilter 1 is: CHANGE_LIST_IMMEDIATE");
    
  //向系統註冊Receiver2，讓RssReader.GetBackStageData產生功能，專收從BackStage來的實體
    mFilter2=new IntentFilter(BackStage.GET_NEW_ENTITY);
    Rreceiver2=new RssReader.GetBackStageData();
    registerReceiver(Rreceiver2,mFilter2);
    Log.i(tag, "registerReceiver IntentFilter 2 is: GET_NEW_ENTITY");
    
		
	   up_layout =(LinearLayout) findViewById(R.id.up_layout);//找出主畫面上方的水平scrollbar的id位置
	   down_layout = (LinearLayout) findViewById(R.id.down_layout);//找出主畫面下方的水平scrollbar的id位置		    
		
    
	   
		//滑動選單的初始設定
       slv = (HorizontalScrollView) findViewById(R.id.hsv);
       slv.setOnTouchListener(RssReader.this);

       BackStage.button_order=0;
       String buffer="";
       buffer=BackStage.checkDatabaseNumber(this);
       
       if(!BackStage.DatabaseNumber.equals(String.valueOf(buffer))){
    	   Log.i(tag, "checkDatabaseNumber is:"+buffer+", BackStage.DatabaseNumber is: "+BackStage.DatabaseNumber+", StartService..");
    	   BackStage.DatabaseNumber=buffer;
    	   
    	   //一開始先清空所有的view，避免每次都重覆創建子view
    	   up_layout.removeAllViews();
    	   down_layout.removeAllViews();
    	   
    	   sendBroadForSwitchWidget(0);//0代表關閉Service
    	   

    	   
    	 //啟動Service以解析資料
    	   intent = new Intent(this, BackStage.class);
    	   intent.putExtra("callfrom", "RssReader");
           startService(intent); 

    	   
          
       }else{
    	   Log.i(tag, "checkDatabaseNumber is:"+buffer+", BackStage.DatabaseNumber is: "+BackStage.DatabaseNumber+", doing nothing..");
       }
      
	}
   
    
	@Override
	protected void onPause() {
		unregisterReceiver(Rreceiver1);
		unregisterReceiver(Rreceiver2);//離開畫面仍更新的問題沒有處理掉
		super.onPause();
		 Log.i(tag, "RssReader.onPause()=>unregisterReceiver: RssReader.GetBackStageData & WidgetReceiver");
	}
	
	
	@Override
	protected void onDestroy() {
		Log.i(tag, "into RssReader.onDestroy()");
		super.onDestroy();
	}


	/**
	 * 描述 : 若呼叫此方法，會寄出廣播讓Widget的Service停止<br/>
	 * 但是UpdateService因為是Widget，不會真的完全停止。
	 * 而是下次執行時，UpdateService又會重onCreate()啟動
	 */
    private void sendBroadForSwitchWidget(int status){

    	
    	
    	  if(status==0){
	    	   Log.i(tag, "==>RssReader.sendBroadForStopWidget(), status is: "+status);
	       }else{
	    	   Log.i(tag, "==>RssReader.sendBroadForStartWidget(), status is: "+status);
	    	   MyWidgetProvider.liAll=BackStage.liAll;
	    	   MyWidgetProvider.widget_namelist=BackStage.backstage_widget_namelist;
	       }
    	  
	        //發送廣播來即時更改Widget
	       Intent intent = new Intent();
//	       intent.putExtra("now channel", BackStage.name);
	       intent.putExtra("status", status);
	       intent.setAction(BackStage.CHANGE_LIST_IMMEDIATE);
	       sendBroadcast(intent);
	       
	     
	       

    }
    
    

    
	private void createNewChannelButton(){
		//最後生產一個新增頻道按鈕
		button = new Button(RssReader.this);
		button.setText("新增頻道");
		LinearLayout.LayoutParams param3 =new LinearLayout.LayoutParams(120,65);
		up_layout.addView(button,param3);
		button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
			
		});
		button.setId(9999);
	}


	/**描述 : 滑動手勢指令換頁*/
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		
		if(getstart==0){
			getstart = event.getX();
		}else if(event.getAction()!=event.ACTION_MOVE){
			getend =event.getX();
			
			if (getstart-getend >0){
				slv.smoothScrollBy(screen_width, 0);				
			}else{
				slv.smoothScrollBy(-screen_width, 0);
			}
			
			getstart=0;
		}		
		return true;
	}
	
	
	/**描述 : 建立Menu清單*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0, 0, 0, "頻道清單");
		menu.add(0, 1, 1, "關於");
		menu.getItem(0).setIcon(R.drawable.setting);
		menu.getItem(1).setIcon(R.drawable.about);
		return super.onCreateOptionsMenu(menu);
	}

	/**描述 : 建立Menu清單的觸發事件*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case 0:
				Intent intent = new Intent();
				intent.setClass(RssReader.this, Setting.class);
				startActivity(intent);
				BackStage.button_order=0;
				break;
			case 1:
				new AlertDialog.Builder(RssReader.this)
				.setMessage("RssReader"+ softVersion +"\n作者：Camangi Corporation\n\n版權所有 2010")
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

	



	public class GetBackStageData extends BroadcastReceiver{
		HashMap<Integer,List<News>> liAll = new HashMap<Integer,List<News>>();

		@Override
		public void onReceive(final Context context, Intent intent) {
			Log.i(tag, ">=RssReader.GetBackStageData.onReceive(), get entity name:"+intent.getExtras().getString("entity_name"));
			

		    
			   name = intent.getExtras().getString("entity_name");
			   button_order=intent.getExtras().getInt("button_order");
			   Log.i(tag, "get button_order: "+button_order);
			   id = intent.getExtras().getInt("id");
			   getData=(ArrayList<News>) intent.getSerializableExtra("getData");
			   Toast.makeText(RssReader.this, "資料處理中..."+String.valueOf(button_order+1)+"/"+String.valueOf(BackStage.cursor.getCount()), Toast.LENGTH_SHORT).show();
			   
			  
			   liAll.put(button_order, getData);
			  
					
					
							//動態新增按鈕
							button = new Button(context);
//							Log.i(tag, "new button pass");
					        button.setText(name);
					        button.setEllipsize(TextUtils.TruncateAt.MARQUEE);//太長就縮小文字
//					        Log.i(tag, "setname pass");
					        button.setId(id);/*setId和namelist的key值、database的_id相對應，這個id值可能不會照順序而會跳號 */
					        LinearLayout.LayoutParams param =new LinearLayout.LayoutParams(120,65);
//					        Log.i(tag, "setlinearlayout pass");
					        up_layout.addView(button,param);
//					        Log.i(tag, "set up_layout pass");
					        button.setOnLongClickListener(new View.OnLongClickListener() {
					             public boolean onLongClick(final View v) {
					         		myDB=new DB(context);
					         		
					        		new AlertDialog.Builder(context)
					        		
					        		.setTitle("對於 "+BackStage.rssreader_namelist.get(v.getId())+" 頻道，你想要...？")
					        		.setIcon(R.drawable.q01)
					        		.setItems(new String[]{"隱藏","重新命名","刪除"}, new DialogInterface.OnClickListener(){
					        			
					        			@Override
					        			public void onClick(DialogInterface dialog, int which) {
					        				switch(which){
					        				case 0:
					        					myDB.channelSwitch(v.getId(), false); 
					        					myDB.close();
					        				       onResume();
					        					break;
					        							
					        				case 1:		
					        					LayoutInflater factory = LayoutInflater.from(context);
					        			            final View rename_layout = factory.inflate(R.layout.alert_dialog_rename, null);
					        							new AlertDialog.Builder(context)
					        							.setTitle("替 "+BackStage.rssreader_namelist.get(v.getId())+" 重新命名")
					        							.setView(rename_layout)
					        							.setPositiveButton("確認", new DialogInterface.OnClickListener() {

					        									@Override
					        									public void onClick(DialogInterface dialog, int which) {	
					        											
					        											EditText edit_rename = (EditText) rename_layout.findViewById(R.id.edit_rename);
					        											String rename=edit_rename.getText().toString();
					        											
					        											if(rename.equals("")){
					        												new AlertDialog.Builder(context)
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
						        											myDB.close();
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
					        							new AlertDialog.Builder(context)
					        							.setIcon(R.drawable.warning01)
					        							.setMessage("這樣會刪除頻道 "+BackStage.rssreader_namelist.get(v.getId())+"\n確定嗎？")
					        							.setTitle("注意！")
					        							
					        							.setPositiveButton("確認", new DialogInterface.OnClickListener() {
					        								
					        								@Override
					        								public void onClick(DialogInterface dialog, int which) {
					        									myDB.delete(v.getId());
					        									myDB.close();
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
					        							new AlertDialog.Builder(context)
					        						
					        						
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
					        });
//					        Log.i(tag, "set buttonOnlongclicklisterner pass");
					        
					        button.setOnClickListener(new View.OnClickListener() {
					             public void onClick(View v) {
					         		switch(v.getId()){
					        		
					        		case 9999:
					        			Log.i(tag,  "you press button: 9999");
					        			LayoutInflater factory = LayoutInflater.from(context);
					                    final View addchannel_layout = factory.inflate(R.layout.alert_dialog_newchannel, null);
					        				new AlertDialog.Builder(context)
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
					        									new AlertDialog.Builder(context)
					        									.setTitle("錯誤！")
					        									.setMessage("請輸入完整方可新增...")
					        									.setIcon(R.drawable.warning01)
					        									.setPositiveButton("返回", new DialogInterface.OnClickListener() {

					        										@Override
					        										public void onClick(DialogInterface dialog, int which) {}
					        										})
					        									
					        									.show();
					        									
					        									
					        								}else{
					        									myDB=new DB(context);
					        									myDB.insert(newchannelname, newchannelpath, true);
					        									myDB.close();
					        									onResume();
					        	
					        								}
					        								
					        						}
					        						})
					        				
					        				.setNeutralButton("找網址", new DialogInterface.OnClickListener() {
					        					
					        						@Override
					        						public void onClick(DialogInterface dialog, int which) {
					        							Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
					        							context.startActivity(i);
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
					        			Log.i(tag, "you press button: default");
					        		int a=Integer.parseInt(v.getTag().toString());
					            	slv.smoothScrollTo((a*screen_width),0);//因為getTag()取出的值button_order是從1開始，而螢幕起始點是(0,0)
//					        		v.setBackgroundResource(R.color.brown);//試圖改變背景顏色，結果...	]
					            		break;
					        		}
					             }
					         });
					        button.setTag(button_order);//setTag是依照使用者的喜好頻道從1設到總筆數,每個button有各自的button_order
					        
					        
					        //動態新增ListView
						    ListView newlv = new ListView(context);
						    LinearLayout.LayoutParams param2 =new LinearLayout.LayoutParams(screen_width,LinearLayout.LayoutParams.FILL_PARENT);
						    down_layout.addView(newlv,param2);
						    newlv.setTag(button_order);
					        newlv.setAdapter(new NewsAdapter(context,getData));
					        newlv.setOnItemClickListener(new OnItemClickListener(){
					        	
					        	/**描述 : ListView的觸發事件*/
								@Override
								public void onItemClick(AdapterView<?> parent, View view,
										int position, long id) {
									//將大容器liAll裡的小容器getData裡的Link依照parent.getTag(),也就是該ListView所設的tag(同button_order)的值取出
									
									Log.i(tag, "parent.getTag(): "+parent.getTag().toString());
									Intent browserIntent1 = new Intent("android.intent.action.VIEW", Uri.parse(liAll.get(Integer.parseInt(parent.getTag().toString())).get(position).getLink()));
									context.startActivity(browserIntent1);
									
								}
					        	
					        });
					        
					      //啟動Service以解析資料
							   intent2 = new Intent(context, BackStage.class);
							   intent2.putExtra("callfrom", "RssReader");
							   context.startService(intent2); 

					        
							   Log.i(tag, "BackStage.cursor count= "+BackStage.cursor.getCount()+", BackStage.liAll.size()= "+BackStage.liAll.size());
					        if(BackStage.cursor.isLast()){
					        	intent2 = new Intent(context, BackStage.class);
								   context.stopService(intent2);
								   Log.i(tag, "Data load finish, stop (Service)BackStage");
								   
								   sendBroadForSwitchWidget(1);					  
							   }
							   
						}

		}



	
}
