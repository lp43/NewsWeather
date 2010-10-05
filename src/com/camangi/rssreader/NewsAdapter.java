package com.camangi.rssreader;

import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

//ListView使用自訂義版面
public class NewsAdapter extends BaseAdapter{
	
	public Context mContext;
	private Bitmap mIcon;
	private LayoutInflater mInflater;
	private List<News> items;

		
	public NewsAdapter(Context c, List<News> it){
		mInflater = LayoutInflater.from(c);
		mIcon = BitmapFactory.decodeResource(c.getResources(), R.drawable.quill);
		 items=it;
	}
	
	@Override
	public int getCount() {
		return items.size();//這行的數量影響了程式是否能被開啟			
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
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
		
		
		holder.title=(TextView) convertView.findViewById(R.id.news_title);
		holder.date=(TextView) convertView.findViewById(R.id.news_date);
		holder.img=(ImageView) convertView.findViewById(R.id.news_img);
		
		convertView.setTag(holder);
		}else{
			holder=(ViewHolder) convertView.getTag();
		}
		News tmpN = items.get(position);
		holder.title.setText(tmpN.getTitle());
		holder.date.setText(tmpN.getDate());
		holder.img.setImageBitmap(mIcon);
	
	return convertView;	

	}
	
}

//為了讓每一格LIST都有固定格式，將此設為一類別，生產出來的每行都是不同的物件實體
class ViewHolder{
	
	TextView title,date;
	ImageView img;

}
