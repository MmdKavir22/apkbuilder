package com.kavir.browser.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME="kavir_browser.db"; private static final int DB_VERSION=2;
    public static final String TABLE_CONFIGS="configs";
    public static class ConfigItem { public long id; public String title,content; public boolean active; public ConfigItem(long i,String t,String c,boolean a){id=i;title=t;content=c;active=a;} }
    public DatabaseHelper(Context c){super(c,DB_NAME,null,DB_VERSION);}
    public void onCreate(SQLiteDatabase db){db.execSQL("CREATE TABLE history (id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,url TEXT,timestamp LONG)");db.execSQL("CREATE TABLE bookmarks (id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,url TEXT)");db.execSQL("CREATE TABLE configs (id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,content TEXT,active INTEGER DEFAULT 0)");}
    public void onUpgrade(SQLiteDatabase db,int o,int n){if(o<2)db.execSQL("CREATE TABLE IF NOT EXISTS configs (id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,content TEXT,active INTEGER DEFAULT 0)");}
    public void addHistory(String t,String u){ContentValues v=new ContentValues();v.put("title",t==null?"":t);v.put("url",u);v.put("timestamp",System.currentTimeMillis());getWritableDatabase().insert("history",null,v);}
    public void clearHistory(){getWritableDatabase().delete("history",null,null);}
    public long addConfig(String t,String c){ContentValues v=new ContentValues();v.put("title",t);v.put("content",c);return getWritableDatabase().insert(TABLE_CONFIGS,null,v);}
    public List<ConfigItem> getConfigs(){List<ConfigItem> l=new ArrayList<>();Cursor c=getReadableDatabase().query(TABLE_CONFIGS,null,null,null,null,null,"active DESC,id DESC");try{while(c.moveToNext())l.add(new ConfigItem(c.getLong(0),c.getString(1),c.getString(2),c.getInt(3)==1));}finally{c.close();}return l;}
    public ConfigItem getActiveConfig(){Cursor c=getReadableDatabase().query(TABLE_CONFIGS,null,"active=1",null,null,null,"id DESC","1");try{return c.moveToFirst()?new ConfigItem(c.getLong(0),c.getString(1),c.getString(2),true):null;}finally{c.close();}}
    public void setActiveConfig(long id){SQLiteDatabase db=getWritableDatabase();db.beginTransaction();try{db.execSQL("UPDATE configs SET active=0");db.execSQL("UPDATE configs SET active=1 WHERE id=?",new Object[]{id});db.setTransactionSuccessful();}finally{db.endTransaction();}}
    public void deleteConfig(long id){getWritableDatabase().delete(TABLE_CONFIGS,"id=?",new String[]{String.valueOf(id)});}
}