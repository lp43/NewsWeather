package com.newsweather;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;

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
        
        button_foucs = (Button) findViewById(R.id.button_focus);
        button_tech = (Button) findViewById(R.id.button_tech);
        button_sports = (Button) findViewById(R.id.button_sports);
        button_relax = (Button) findViewById(R.id.button_relax);
        g = (Gallery) findViewById(R.id.gallery);
        
        g.setAdapter(new NewsAdapter(this));
        
        button_foucs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(NewsWeather.this, ListContent.class);
                startActivity(i);
                NewsWeather.this.finish();
            }
        });

    }
    
    
    
    
    
    public class NewsAdapter extends BaseAdapter{
    	
    	int mGalleryItemBackground;
    	public Context mContext;
    	private Object[] mImageIds = {
//    			R.drawable.gallery_photo_1,
    			new ListContent(),
    			R.drawable.gallery_photo_2,
    			R.drawable.gallery_photo_3,
    			R.drawable.gallery_photo_4
    	};
    	
    	public NewsAdapter(Context c){
    		mContext = c;
    		
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
			return mImageIds.length;
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
//			ImageView i = new ImageView(mContext);
			ListView l = new ListView(mContext);
//			i.setImageResource(mImageIds[position]);
//			i.setScaleType(ImageView.ScaleType.FIT_XY);
//			i.setLayoutParams(new Gallery.LayoutParams(800, 480));
			
			// The preferred Gallery item background
//            i.setBackgroundResource(mGalleryItemBackground);
            
//			return i;
			
			return l;
		}
    	
    }
}