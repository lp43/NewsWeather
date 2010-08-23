package com.newsweather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper{
	private static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION=1;
	private static final String DATABASE_TABLE = "defaultpath";
	private static final String DATABASE_CREATE=
		"create table defaultpath("
		+"id INTEGER PRIMARY KEY,"
		+"sourcename TEXT,"
		+"path TEXT"
		+");";
		
		public DB(Context context){
		super(context, DATABASE_NAME,null,DATABASE_VERSION);	
		}
	

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}


		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS"+DATABASE_TABLE);
			onCreate(db);
		}
		
		public Cursor getSourcename(){
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(DATABASE_TABLE, new String[]{"sourcename"}, null, null, null, null, null);
			return cursor;
		}
		
		public void insert(String source,String path){
			SQLiteDatabase db = this.getWritableDatabase();
			//將新增的值放入ContentValues
			ContentValues cv = new ContentValues();
			cv.put("sourcename", source);
			cv.put("path", path);
			db.insert(DATABASE_TABLE, null, cv);
		}
		
		public void delete(){
			SQLiteDatabase db = this.getWritableDatabase();
			//更多程式碼
		}
		
}
