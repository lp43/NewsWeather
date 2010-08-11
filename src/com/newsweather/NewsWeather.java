package com.newsweather;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NewsWeather extends Activity {
	
	private Button button_foucs,
	button_tech,
	button_sports,
	button_relax;
	
	private Gallery g;
	

	

	
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
        g = (Gallery) findViewById(R.id.gallery);
        Log.i("findgallery", "Success");
        g.setAdapter(new NewsAdapter(this));
        Log.i("gallerysetToNewsadapter", "Success");
    }
    
    
    
    
    
    public class NewsAdapter extends BaseAdapter{
    	
    	int mGalleryItemBackground;
    	public Context mContext;
    	private LayoutInflater mInflater;
    	private Bitmap mIcon;
    	
//    	private Integer[] mImageIds = {
//    			R.drawable.gallery_photo_1,
//    			R.drawable.gallery_photo_2,
//    			R.drawable.gallery_photo_3,
//    			R.drawable.gallery_photo_4
//    	};
    	
    	private String[][] mDataIds = {
    			{"1234567","2010/08/12"},
    			{"123456789","2010/08/14"}
    			
    	};
    	
    	
    	public NewsAdapter(Context c){
    		mInflater = LayoutInflater.from(c);
    		mIcon = BitmapFactory.decodeResource(c.getResources(), R.drawable.gallery_photo_1);
//    		mContext = c;
    		 Log.i("alreadyNewAdapter", "Success");
    	/*使用在res/values/attrs.xml中的<declare-styleable>定義的
    	 * Gallery屬性
    	 */
    		TypedArray a = obtainStyledAttributes(R.styleable.Gallery);
        //取得Gallery屬性的Index id
    		mGalleryItemBackground = a.getResourceId(R.styleable.Gallery_android_galleryItemBackground, 0);
    	//讓物件的styleable屬性能夠反覆使用
    		a.recycle();
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
			
//			ImageView i = new ImageView(mContext);
//			
//			i.setImageResource(mImageIds[position]);
//			i.setScaleType(ImageView.ScaleType.FIT_XY);
//			i.setLayoutParams(new Gallery.LayoutParams(800, 480));
			
			// The preferred Gallery item background
//            i.setBackgroundResource(mGalleryItemBackground);
            
//			return i;
			
			if(convertView ==null){
			//使用自定義的file_rwo作為Layout
			convertView = mInflater.inflate(R.layout.listcontent, null);
			//初始化holder的2個text和icon
			holder = new ViewHolder();
			holder.bigtext=(TextView) convertView.findViewById(R.id.bigtext);
			holder.smalltext=(TextView) convertView.findViewById(R.id.smalltext);
			holder.icon=(ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
			}else{
				holder=(ViewHolder) convertView.getTag();
			}
			holder.bigtext.setText(mDataIds[0][0]);//程式在這裡出現NullPointerException,檢查一下holder實體還在不在
			holder.smalltext.setText(mDataIds[0][1]);
			holder.icon.setImageBitmap(mIcon);
		return convertView;	
		}
    	
    }
    
    //為了讓每一格LIST都有固定格式，將此設為一類別，生產出來的每行都是不同的物件實體
    private class ViewHolder{
    	TextView bigtext,smalltext;
    	ImageView icon;
    }
}