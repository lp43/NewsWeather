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


public class RssReader extends Activity implements OnTouchListener,OnClickListener,OnLongClickListener,OnItemClickListener {
	
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
//	/**判斷xml文件編碼並儲存在Encode*/
//	String Encode;  
	/**描述 : bufferb用來存放從xml複製下來，每一行從BIG5轉成UTF-8的String空間*/
	String bufferb;  
	/**資料載入中的等待視窗*/
	public ProgressDialog myDialog;
	/**用來檢查資料庫在不在，不在才要重建*/
	File file;
	/**現在的畫面，起始為1*/
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
	 * 這個值和Button.setTag()、ListView.setTag()、大容器HashMap型態的liAll的key值都是相對應的
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
	public static final String CHANGE_LIST_IMMEDIATE="changeListimmediate";
	public static final String GET_NEW_ENTITY="get_new_entity_from_backstage";
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
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i("startProgress", "start");
        
        packageName=this.getPackageName();


        
        //向系統註冊Receiver，讓MyWidgetProvider.mReceiver產生功能
        IntentFilter mFilter,mFilter2;
        /*mFilter=new IntentFilter(CHANGE_LIST_IMMEDIATE);
        Rreceiver1 = new MyWidgetProvider.mReceiver();
        registerReceiver(Rreceiver1,mFilter);//MyWidgetProvider.mReceiver()的<IntentFilter>是CHANGE_LIST_IMMEDIATE
        Log.i(tag, "registerReceiver1,IntentFilter is: CHANGE_LIST_IMMEDIATE");*/
        
      //向系統註冊Receiver，讓RssReader.GetBackStageData產生功能，專收從BackStage來的實體
        mFilter2=new IntentFilter(GET_NEW_ENTITY);
        Rreceiver2=new RssReader.GetBackStageData();
        registerReceiver(Rreceiver2,mFilter2);
        Log.i(tag, "registerReceiver2,IntentFilter is: GET_NEW_ENTITY");

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
	
	   up_layout =(LinearLayout) findViewById(R.id.up_layout);//找出主畫面上方的水平scrollbar的id位置
	   down_layout = (LinearLayout) findViewById(R.id.down_layout);//找出主畫面下方的水平scrollbar的id位置		    
		
       //一開始先清空所有的view，避免每次都重覆創建子view
	   up_layout.removeAllViews();
	   down_layout.removeAllViews();
	   
		//滑動選單的初始設定
       slv = (HorizontalScrollView) findViewById(R.id.hsv);
       slv.setOnTouchListener(RssReader.this);


		//啟動Service以解析資料
	   intent = new Intent(this, BackStage.class);
       startService(intent);
	
       
//       unregisterReceiver(Rreceiver2);
       //寄出廣播讓Widget的Service停止
//       sendBroadToStopService();
       Log.i(tag, "=====================================");
       
//	if(RssReader.updateVersion<BackStage.updateVersion){
     //從Task清單裡去查明有開啟Widget，就將AppWidgetExist設為True，以成為之後複製檔案的判斷條件
//		            activitymanager=(ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
////					Log.i(tag, "getSystemService finish");
//					List<ActivityManager.RunningTaskInfo> a=activitymanager.getRunningTasks(10);
////					Log.i(tag, "getRunningTasks finish");
//					for(ActivityManager.RunningTaskInfo j:a){
////						Log.i(tag, "intoFor-loop");
//						if(j.baseActivity.getClassName().equals(packageName+".RssReader")){
//							AppWidgetExist=true;
////							Log.i(tag, "getClassName finish");
//						}
//					}
					
					
//						 if(RssReader.updateVersion<BackStage.updateVersion){
//							if(AppWidgetExist){//之所以要先判斷AppWidgetExist，是因為若接在BackStage取liAll，
								//可能會因為並沒有建立AppWidget而造成NullPointerException
       
				/*				if(MyWidgetProvider.updateVersion==BackStage.updateVersion){
				            		RssReader.liAll=MyWidgetProvider.liAll;
				            	}else{
						    		    try{
									    	   liAll.put(button_order, BackStage.convert(path));//將轉存的xml檔容器getData再放進大容器liAll	 
										} catch (Exception e) {
											Log.i("Exception+", e.getMessage());    		    		     
					    				} 	  
							    }	*/
					//更新完才將版本號調為最新號
//					RssReader.updateVersion=BackStage.updateVersion;
        
		}
    
	
	private void createNewChannelButton(){
		//最後生產一個新增頻道按鈕
		button = new Button(RssReader.this);
		button.setText("新增頻道");
		LinearLayout.LayoutParams param3 =new LinearLayout.LayoutParams(110,65);
		up_layout.addView(button,param3);
		button.setOnClickListener(this);
		button.setId(9999);
	}



	@Override
	protected void onDestroy() {

		super.onDestroy();
		   unregisterReceiver(Rreceiver1);
		   Log.i(tag, "RssReader onDestroy()=>unregisterReceiver");
		   MyWidgetProvider.updateVersion=0;
		   RssReader.updateVersion=0;
		   RssReader.updateVersion=1;
	}

    
	/**
	 * 描述 : 若呼叫此方法，會寄出廣播讓Widget的Service停止
	 */
    private void sendBroadToStopService(){
	    if(BackStage.updateVersion>MyWidgetProvider.updateVersion){
	        //發送廣播來即時更改Widget
	       Intent intent = new Intent();
	       intent.putExtra("now channel", name);
	       
	       intent.setAction(CHANGE_LIST_IMMEDIATE);
	       sendBroadcast(intent);
	       Log.i(tag, "RssReader.sendBroadToStopService()=>sendBroadcast, now channel is: "+name);
	    }
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
			}else{
				slv.smoothScrollBy(-800, 0);
			}
			
			getstart=0;
		}		
		return true;
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
				intent.setClass(RssReader.this, Setting.class);
				startActivity(intent);

				break;
			case 1:
				new AlertDialog.Builder(RssReader.this)
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
			Log.i(tag,  "you press button: 9999");
			LayoutInflater factory = LayoutInflater.from(RssReader.this);
            final View addchannel_layout = factory.inflate(R.layout.alert_dialog_newchannel, null);
				new AlertDialog.Builder(RssReader.this)
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
									new AlertDialog.Builder(RssReader.this)
									.setTitle("錯誤！")
									.setMessage("請輸入完整方可新增...")
									.setIcon(R.drawable.warning01)
									.setPositiveButton("返回", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {}
										})
									
									.show();
									
									
								}else{
									myDB=new DB(RssReader.this);
									myDB.insert(newchannelname, newchannelpath, true);
									onResume();
	
								}
								
						}
						})
				
				.setNeutralButton("找網址", new DialogInterface.OnClickListener() {
					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
							startActivity(i);
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
    	slv.smoothScrollTo(((a-1)*800),0);//因為getTag()取出的值button_order是從1開始，而螢幕起始點是(0,0)
//		v.setBackgroundResource(R.color.brown);//試圖改變背景顏色，結果...	]
    		break;
		}
	}
	
	/**描述 : Button的長按事件*/
	@Override
	public boolean onLongClick(final View v) {
		
		myDB=new DB(RssReader.this);
		
		new AlertDialog.Builder(RssReader.this)
		
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
					LayoutInflater factory = LayoutInflater.from(RssReader.this);
			            final View rename_layout = factory.inflate(R.layout.alert_dialog_rename, null);
							new AlertDialog.Builder(RssReader.this)
							.setTitle("替 "+namelist.get(v.getId())+" 重新命名")
							.setView(rename_layout)
							.setPositiveButton("確認", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {	
											
											EditText edit_rename = (EditText) rename_layout.findViewById(R.id.edit_rename);
											String rename=edit_rename.getText().toString();
											
											if(rename.equals("")){
												new AlertDialog.Builder(RssReader.this)
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
							new AlertDialog.Builder(RssReader.this)
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
							new AlertDialog.Builder(RssReader.this)
						
						
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
		
		//將大容器liAll裡的小容器getData裡的Link依照parent.getTag(),也就是該ListView所設的tag(同button_order)的值取出
		String temp1 = liAll.get(Integer.parseInt(parent.getTag().toString())).get(position).getLink();
		Intent browserIntent1 = new Intent("android.intent.action.VIEW", Uri.parse(temp1));
		startActivity(browserIntent1);
		
	}
	public static class GetBackStageData extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(tag, "GetBackStageData.onReceive() get entity name:"+intent.getExtras().getString("entity_name"));
			//啟動Service以解析資料
			   intent = new Intent(context, BackStage.class);
			   context.startService(intent);
//			
//			
//			button_order++;
//			//動態新增按鈕
//			button = new Button(RssReader.this);
//	        button.setText(name);
//	        LinearLayout.LayoutParams param =new LinearLayout.LayoutParams(110,65);
//	        up_layout.addView(button,param);
//	        button.setOnLongClickListener(RssReader.this);
//	        button.setId(id);/*setId和namelist的key值、database的_id相對應，這個id值可能不會照順序而會跳號 */
//	        button.setOnClickListener(RssReader.this);
//	        button.setTag(button_order);//setTag是依照使用者的喜好頻道從1設到總筆數,每個button有各自的button_order
//	        
//	        
//	        //動態新增ListView
//		    ListView newlv = new ListView(RssReader.this);
//		    LinearLayout.LayoutParams param2 =new LinearLayout.LayoutParams(800,440);
//		    down_layout.addView(newlv,param2);
//		    newlv.setTag(button_order);
//	        newlv.setAdapter(new NewsAdapter(RssReader.this,liAll.get(button_order)));
//	        newlv.setOnItemClickListener(RssReader.this);
	        
	        
		}
	

}

	
}
