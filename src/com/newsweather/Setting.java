package com.newsweather;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Setting extends Activity {

	private DB myDB;
	@Override
	protected void onPause() {
		Setting.this.finish();
		Intent intent = new Intent();
		intent.setClass(Setting.this, NewsWeather.class);
		startActivity(intent);
		super.onPause();
	}

	private Cursor cursor;
	private ListView lv;
	public CheckBox myCheckBox;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		lv=(ListView) findViewById(R.id.setView);


		myDB = new DB(this);		
 		cursor = myDB.getAll();

 		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(Setting.this, android.R.layout.simple_list_item_multiple_choice,cursor, new String[]{"_name"}, new int[]{android.R.id.text1});
		lv.setAdapter(adapter);
		while(cursor.moveToNext()){
		String a=cursor.getString(cursor.getColumnIndex("_open"));
		
			if(a.equals("0")){
				lv.setItemChecked(cursor.getPosition(), false);
			}else{
				lv.setItemChecked(cursor.getPosition(), true);
			}
		}


		lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CheckedTextView ct = (CheckedTextView) view;
				
				String name=ct.getText().toString();
				Toast.makeText(Setting.this, "你點擊的是"+name,
	                    300).show();
				
				//如果原本有被點選，再按一次就將database的資料改成false，取消點選
					if(ct.isChecked()){
						myDB.update(id,false);
					}else{
						myDB.update(id,true);
					}
				Log.i("checkedname", name);
				
			
						
				}
			
				
			
			
		});
		
	}
	
		

}
