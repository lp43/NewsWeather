package com.camangi.rssreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper{
	private static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION=2;
	private static final String DATABASE_TABLE = "allpath";
	private static final String DATABASE_CREATE=
		"create table "+ DATABASE_TABLE +" ("
		+"_id INTEGER PRIMARY KEY,"
		+"_name TEXT,"
		+"_path TEXT,"
		+"_open BOOLEAN"  
		+");";
		//0 is false
	
	
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
		
		//查詢全部資料表，供Setting讀取用
		public Cursor getAll(){
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(DATABASE_TABLE, new String[]{"_id","_name","_path","_open"}, null, null, null, null, null);
			
			return cursor;
			
		}
		
		//專門查尾巴是True的path，供動態新增NewsWeather用
		public Cursor getTruePath(){
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(DATABASE_TABLE, new String[]{"_id","_name","_path"}, new String("_open=?"), new String[]{"1"}, null, null, null);
			
			return cursor;
			
		}
		
		public void insert(String name,String path,Boolean open){
			SQLiteDatabase db = this.getWritableDatabase();
			//將新增的值放入ContentValues
			ContentValues cv = new ContentValues();
			cv.put("_name", name);
			cv.put("_path", path);
			cv.put("_open", open);
			db.insert(DATABASE_TABLE, null, cv);
		}
		
		public void delete(int id){
			SQLiteDatabase db = this.getWritableDatabase();
			db.delete(DATABASE_TABLE, new String("_id=?"), new String[]{Integer.toString(id)});
		}
		
		//勾選CheckedBox更改資料庫_open欄位的true/false
		public void channelSwitch(long id,Boolean open){
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put("_open", open);
			db.update(DATABASE_TABLE, cv, "_id=?", new String[]{String.valueOf(id)});
		}
		
		//依照id位置更改資料名稱
		public void reName(long id,String name){
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put("_name", name);
			db.update(DATABASE_TABLE, cv, "_id=?", new String[]{String.valueOf(id)});
		}
		
}
