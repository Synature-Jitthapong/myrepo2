package com.syn.iorder.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper{
	
	public static final String DB_NAME = "iOrder.db";
	public static final int DB_VERSION = 1;

	public static final String QUEUE_SQL = "create table QueueButton("
			+ "queue_group_id integer not null default 0 primary key,"
			+ "queue_group_name text"
			+ ");";
	public static final String TABLE_SQL = "create table TableInfo("
			+ " tb_id integer, "
			+ " tb_name text"
			+ ");";
	public static final String[] SQL_CREATE={
		TABLE_SQL,
		QUEUE_SQL
	};
	
	public SQLiteHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for(String sql : SQL_CREATE){
			db.execSQL(sql);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
