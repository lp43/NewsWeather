package com.newsweather;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;

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
    }
    
    
    
    
    
    public class NewsAdapter extends BaseAdapter{
    	
    	int mGalleryItemBackground;
    	public Context mContext;
    	
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
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
    	
    }
}