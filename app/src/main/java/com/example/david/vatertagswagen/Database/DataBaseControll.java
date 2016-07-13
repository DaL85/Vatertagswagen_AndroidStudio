package com.example.david.vatertagswagen.Database;


import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;


public class DataBaseControll {
	private SQLiteDatabase database;
	private MySQLite sqlite;
	private String[] datacolumns = { "ID", "DeviceName", "DeviceMAC"};
	public DataBaseControll (Context context) {
		sqlite = MySQLite.getInstance(context);
	}
	public void open() throws SQLException {
		database = sqlite.getWritableDatabase();
	}
	public void close() {
		sqlite.close();
	}
	public Entry createEntry (String DeviceName, String DeviceMAC)//, boolean full)
	{
		ContentValues values = new ContentValues();
		values.put ("DeviceName", DeviceName);
		values.put ("DeviceMAC", DeviceMAC);
		long insertID = database.insert ("DATA", null, values);
		Cursor cursor = database.query("DATA", datacolumns, "ID = " + insertID, null, null, null, null);
		cursor.moveToFirst();
		return cursorToEntry(cursor);
	}
	private Entry cursorToEntry (Cursor cursor){
		Entry e = new Entry();
		e.setID(cursor.getLong(0));
		e.setDeviceName(cursor.getString(1));
		e.setDeviceMAC(cursor.getString(2));
		return e;
	}
	//private GPS_Point cursorToGPS_Point (Cursor cursor){
	//GPS_Point c = new GPS_Point();
	//c.setID(cursor.getLong(0));
	//c.setLatitude(cursor.getDouble(1));
	//c.setLongitude(cursor.getDouble(2));
	////c.setfull(cursor.getInt(3)>0);
	//return c;
	//}
	protected List<Entry> getAllEntries() {
		List<Entry> EntryList = new ArrayList<Entry>();
		Cursor cursor = database.query("DATA", datacolumns, null, null, null, null, null);
		cursor.moveToFirst();
		if(cursor.getCount() == 0) return EntryList;
		while (cursor.isAfterLast() == false) {
			Entry entry = cursorToEntry(cursor);
			EntryList.add(entry);
			cursor.moveToNext();
		}
		cursor.close();
		return EntryList;
	}
	public ArrayList<Entry> getEntryByDeviceName(String DeviceName){
		ArrayList<Entry> list = new ArrayList<Entry>();
		String[] DeviceNames = { DeviceName };
		Cursor cursor = database.rawQuery("SELECT * from DATA where Latitude = ?;", DeviceNames);
		cursor.moveToFirst();
		while(cursor.isAfterLast() == false){
		list.add(cursorToEntry(cursor));
		cursor.moveToNext();
		}
		return list;
	}
	public void deleteEntry (long ID){
		database.delete("DATA", "ID=" + ID, null);
	}
	public void deleteAllEntries(){
		List<Entry> all= this.getAllEntries();
		for(int i=1;i<=all.size();i++)
		{
			this.deleteEntry(i);
	}
	}
	public void updateEntry (Entry e){
		ContentValues values = new ContentValues();
		values.put ("ID", e.getID());
		values.put ("DeviceName", e.getDeviceName());
		values.put ("DeviceMAC", e.getDeviceMAC());
		database.update("DATA", values, "ID=+" + e.getID(), null);
	}
	public Entry getEntry (long ID){
		Cursor cursor = database.query("DATA", datacolumns, "ID=" + ID, null, null, null, null);
		cursor.moveToFirst();
		while(cursor.isAfterLast() == false){
			if(cursor.getLong(0) == ID){
				return cursorToEntry(cursor);
			}
			cursor.moveToNext();
		}
		return null;
	}
	public int Size(){
		Cursor cursor = database.query("DATA", datacolumns, null, null, null, null, null);
		cursor.moveToLast();
		return cursor.getCount();
	}
	public Entry getLastEntry(){
		Cursor cursor = database.query("DATA", datacolumns, null, null, null, null, null);
		cursor.moveToLast();
		if(cursor.getCount() == 0)
			return null;
		else
		{
			Entry entry=cursorToEntry(cursor);
			cursor.close();
			return entry;
		}
	}
	
	public long getEntryID(String DeviceName, String DeviceMAC){
		String[] s= {String.valueOf(DeviceName),String.valueOf(DeviceMAC)};
		Cursor cursor = database.rawQuery("SELECT * from DATA where DeviceName = ? AND DeviceMAC = ?;",s);
		cursor.moveToFirst();
		if(cursor.getCount() > 1)
		{
			long id =cursor.getLong(0);
			return id;
		}
		else
			return -1;
	}
}