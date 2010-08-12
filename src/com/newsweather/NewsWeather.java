package com.newsweather;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


public class NewsWeather extends Activity implements OnTouchListener {
	//宣告最上面的5個新聞標題按鈕
	private Button button_foucs,
	button_tech,
	button_sports,
	button_relax,
	button_backto,
	button_nextto;
	private LayoutInflater mInflater;
	//先初始化4個ListView
	private ListView llv,llv2,llv3,llv4;
	//為了讓下面的新聞欄可以左右滑動，需把HorizontalScrollView宣告出來
	HorizontalScrollView slv;
	//用來存放手勢的第1個值和最後一個值，好比較是往前或往後滑
	double getend,getstart=0;
	//宣告一個陣列來存手勢的值
	private List list = new ArrayList();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);      

        Log.i("Create Android", "Success create NewsWeather");
        button_foucs = (Button) findViewById(R.id.button_focus);
        button_tech = (Button) findViewById(R.id.button_tech);
        button_sports = (Button) findViewById(R.id.button_sports);
        button_relax = (Button) findViewById(R.id.button_relax);
        button_backto = (Button) findViewById(R.id.button_backto);
        button_nextto = (Button) findViewById(R.id.button_nextto);
        llv = (ListView) findViewById(R.id.list);
        llv2 = (ListView) findViewById(R.id.list2);
        llv3 = (ListView) findViewById(R.id.list3);
        llv4 = (ListView) findViewById(R.id.list4);
        slv = (HorizontalScrollView) findViewById(R.id.hsv);
        
        llv.setAdapter(new ArrayAdapter(NewsWeather.this,android.R.layout.simple_list_item_1,new String[]{"王建民輸球","阿姆斯狀其實沒有登陸月球","小s想再生第3胎","456","456","456","456","456","456","456","456","456","456","456","456"}));
        llv2.setAdapter(new ArrayAdapter(NewsWeather.this,android.R.layout.simple_list_item_1,new String[]{"阿姆斯狀其實沒有登陸月球","456","456","456","456","456","456","456","456","456","456","456","456","456","456"}));
        llv3.setAdapter(new ArrayAdapter(NewsWeather.this,android.R.layout.simple_list_item_1,new String[]{"王建民輸球","456","456","456","456","456","456","456","456","456","456","456","456","456","456"}));
        llv4.setAdapter(new ArrayAdapter(NewsWeather.this,android.R.layout.simple_list_item_1,new String[]{"小s想再生第3胎","456","456","456","456","456","456","456","456","456","456","456","456","456","456"}));
       
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
        button_backto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	slv.smoothScrollBy(-800, 0);
            }
        });
        button_nextto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	slv.smoothScrollBy(800, 0);
            }
        });
       
        slv.setOnTouchListener(this);
    }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		list.add(event.getX());
		Log.i("end:", String.valueOf(list));
		Log.i("end:", String.valueOf(list.get(0)));
//		if(list.get(list.))
//		if(event.getAction()!=event.ACTION_MOVE){
//			getend = event.getX();
//			Log.i("end:", String.valueOf(getend));
//
//		}else{
//			getstart = event.getX();
//			Log.i("start:", String.valueOf(getstart));
//		}
//		
//		if(getend>getstart){
//		slv.smoothScrollBy(800, 0);
//		} else{slv.smoothScrollBy(-800, 0);}
		
		return true;
	}
    
    
    
    
    
//    public class NewsAdapter extends BaseAdapter{
//    	
//    	int mGalleryItemBackground;
//    	public Context mContext;
//    	
//    	private Bitmap mIcon;
    	
    	
//    	private String[][] mDataIds = {
//    			{"1234567","2010/08/12"},
//    			{"123456789","2010/08/14"}   		
//    	};
//    	
    	
//    	public NewsAdapter(Context c){
//    		mInflater = LayoutInflater.from(c);
//    		mIcon = BitmapFactory.decodeResource(c.getResources(), R.drawable.gallery_photo_1);
//    		mContext = c;
//    		 Log.i("alreadyNewAdapter", "Success");
//    	}
//    	
//		@Override
//		public int getCount() {
//			return 5;//這行的數量影響了程式是否能被開啟			
//		}
//
//		@Override
//		public Object getItem(int position) {
//			return position;
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//
//			ViewHolder holder;         
//
//			if(convertView ==null){
////			//使用自定義的file_rwo作為Layout
//			convertView = mInflater.inflate(R.layout.listcontent, null);
////			//初始化holder的2個text和icon
//			holder = new ViewHolder();
//			holder.lv= (ListView) convertView.findViewById(R.id.list);
//			holder.testtext=(TextView)convertView.findViewById(R.id.testtext);
//////			holder.tag=(TextView) convertView.findViewById(R.id.text_cont);
//////			holder.info=(TextView) convertView.findViewById(R.id.text_info);
//////			holder.icon=(ImageView) convertView.findViewById(R.id.icon);
//			
//			convertView.setTag(holder);
//			}else{
//				holder=(ViewHolder) convertView.getTag();
//			}
////			holder.tag.setText(mDataIds[0][0]);//程式在這裡出現NullPointerException,檢查一下holder實體還在不在
//////			holder.info.setText(mDataIds[0][1]);
//////			holder.icon.setImageBitmap(mIcon);
//			holder.lv.setAdapter(new ArrayAdapter(NewsWeather.this,android.R.layout.simple_list_item_1,new String[]{"123","456","456","456","456","456","456","456","456","456","456","456","456","456","456"}));
////			holder.testtext.setText("1234");
////			
//		return convertView;	
//
//		}
//    	
//    }
//    
//    //為了讓每一格LIST都有固定格式，將此設為一類別，生產出來的每行都是不同的物件實體
//    private class ViewHolder{
//    	ListView lv;
//    	TextView tag,info;
//    	ImageView icon;
//    	TextView testtext;
//    }
}