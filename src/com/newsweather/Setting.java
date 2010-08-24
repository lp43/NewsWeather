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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class Setting extends Activity {

	private DB myDB;
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

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(Setting.this, android.R.layout.simple_list_item_multiple_choice,cursor, new String[]{"_name"}, new int[]{android.R.id.text1});

		lv.setAdapter(adapter);  
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SparseBooleanArray d=null;
				
				if(position==0){
					 d=lv.getCheckedItemPositions();
						boolean /e=d.get(0);
				
						e=d.get(3);
						e=d.get(4);
						e=d.get(5);
						e=d.get(6);
						e=d.get(7);
						e=d.get(8);
						e=d.get(9);
						e=d.get(10);
						e=d.get(11);
						e=d.get(12);
						e=d.get(13);
						e=d.get(14);
						e=d.get(15);
				}
			
				
			}
			
		});
		
	}
	
		

}
