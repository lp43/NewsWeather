package com.newsweather;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
//ListContent類別用來產生一個ListView
public class ListContent extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		ListView listView = (ListView) findViewById(R.id.listnews);
		listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new String[]{"123","456","789"}));
	}

}
