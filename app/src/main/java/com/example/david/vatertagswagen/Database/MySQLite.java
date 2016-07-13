package com.example.david.vatertagswagen.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class MySQLite extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "led_database.db";
	private static final int DATABASE_VERSION = 1;
	private static MySQLite mInstance = null;
	private static final String TABLE_CREATE_DATA = "" + "create table DATA("
	+ " ID integer primary key autoincrement, "
	+ "DeviceName string, "
	+ "DeviceMAC string"
	+ ")";
	
	public static MySQLite getInstance(Context context){
		if (mInstance == null){
			mInstance = new MySQLite(context.getApplicationContext());
		}
		return mInstance;
	}
	
	private MySQLite(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(TABLE_CREATE_DATA);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if ((newVersion == 2) && (oldVersion == 1)){
			db.execSQL("ALTER TABLE DATA ADD COLUMN full bool DEFAULT FALSE;");
		} 
		else if ((newVersion == 3) && (oldVersion == 2)){
			db.execSQL(TABLE_CREATE_DATA);
		} 
		else {
		}
	}
}