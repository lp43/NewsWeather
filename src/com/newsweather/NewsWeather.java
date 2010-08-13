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
	private Button button_foucs,//按鈕[焦點新聞]
	button_tech,//按鈕[科技]
	button_sports,//按鈕[運動]
	button_relax;//按鈕[影劇]

	private LayoutInflater mInflater;
	//先初始化4個ListView
	private ListView llv,llv2,llv3,llv4;
	//為了讓下面的新聞欄可以左右滑動，需把HorizontalScrollView宣告出來
	HorizontalScrollView slv;
	//用來存放手勢的第1個值和最後一個值，好比較是往前或往後滑
	double getend,getstart=0;

	
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
       
        llv = (ListView) findViewById(R.id.list);
        llv2 = (ListView) findViewById(R.id.list2);
        llv3 = (ListView) findViewById(R.id.list3);
        llv4 = (ListView) findViewById(R.id.list4);
        slv = (HorizontalScrollView) findViewById(R.id.hsv);
        
        //設定ListView的樣版和文字來源
        llv.setAdapter(new NewsAdapter(NewsWeather.this));
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
       
        slv.setOnTouchListener(this);
    }

	@Override
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
    
    
    
    
	//ListView使用自訂義版面
    public class NewsAdapter extends BaseAdapter{
    	
    	public Context mContext;
    	private Bitmap mIcon;
    	
    	private String[][] mDataIds = {
    			{"123456789101111","2010/08/12"},
    			{"123456789","2010/08/14"}   		
    	};
    		
    	public NewsAdapter(Context c){
    		mInflater = LayoutInflater.from(c);
    		mIcon = BitmapFactory.decodeResource(c.getResources(), R.drawable.gallery_photo_1);
    		mContext = c;
    		 Log.i("alreadyNewAdapter", "Success");
    	}
    	
		@Override
		public int getCount() {
			return mDataIds.length;//這行的數量影響了程式是否能被開啟			
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;         

			if(convertView ==null){
			//使用自定義的file_row作為Layout
			convertView = mInflater.inflate(R.layout.file_row, null);
			//初始化holder的2個text和img
			holder = new ViewHolder();
			
			
			holder.cont=(TextView) convertView.findViewById(R.id.news_cont);
			holder.info=(TextView) convertView.findViewById(R.id.news_info);
			holder.img=(ImageView) convertView.findViewById(R.id.news_img);
			
			convertView.setTag(holder);
			}else{
				holder=(ViewHolder) convertView.getTag();
			}
			Log.i("start=", String.valueOf(position));
			holder.cont.setText(mDataIds[position][0]);
			Log.i("first=", mDataIds[position][0]);
			holder.info.setText(mDataIds[position][1]);
			holder.img.setImageBitmap(mIcon);
			Log.i("second", String.valueOf(position));
		return convertView;	

		}
    	
    }
    
    //為了讓每一格LIST都有固定格式，將此設為一類別，生產出來的每行都是不同的物件實體
    private class ViewHolder{
    	
    	ListView lv;
    	TextView cont,info;
    	ImageView img;

    }
}