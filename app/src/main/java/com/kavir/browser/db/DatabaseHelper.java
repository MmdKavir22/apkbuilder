package com.kavir.browser.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "kavir_browser.db";
    private static final int DB_VERSION = 2;
    public static final String TABLE_HISTORY = "history";
    public static final String TABLE_BOOKMARKS = "bookmarks";
    public static final String TABLE_CONFIGS = "configs";

    public static class ConfigItem {
        public long id;
        public String title;
        public String content;
        public boolean active;
        public ConfigItem(long id, String title, String content, boolean active) {
            this.id=id; this.title=title; this.content=content; this.active=active;
        }
    }

    public DatabaseHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE history (id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,url TEXT,timestamp LONG)");
        db.execSQL("CREATE TABLE bookmarks (id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,url TEXT)");
        db.execSQL("CREATE TABLE configs (id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,content TEXT,active INTEGER DEFAULT 0)");
    }

    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) {
        if (oldVersion < 2) db.execSQL("CREATE TABLE IF NOT EXISTS configs (id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,content TEXT,active INTEGER DEFAULT 0)");
    }

    public void addHistory(String title,String url) {
        ContentValues v=new ContentValues(); v.put("title",title==null?"":title); v.put("url",url); v.put("timestamp",System.currentTimeMillis());
        getWritableDatabase().insert(TABLE_HISTORY,null,v);
    }
    public void clearHistory(){ getWritableDatabase().delete(TABLE_HISTORY,null,null); }

    public long addConfig(String title,String content) {
        ContentValues v=new ContentValues(); v.put("title",title); v.put("content",content); v.put("active",0);
        return getWritableDatabase().insert(TABLE_CONFIGS,null,v);
    }
    public List<ConfigItem> getConfigs() {
        List<ConfigItem> out=new ArrayList<>();
        Cursor c=getReadableDatabase().query(TABLE_CONFIGS,null,null,null,null,null,"active DESC,id DESC");
        try { while(c.moveToNext()) out.add(new ConfigItem(c.getLong(0),c.getString(1),c.getString(2),c.getInt(3)==1)); }
        finally { c.close(); }
        return out;
    }
    public ConfigItem getActiveConfig() {
        Cursor c=getReadableDatabase().query(TABLE_CONFIGS,null,"active=1",null,null,null,"id DESC","1");
        try { return c.moveToFirst()?new ConfigItem(c.getLong(0),c.getString(1),c.getString(2),true):null; }
        finally { c.close(); }
    }
    public void setActiveConfig(long id) {
        SQLiteDatabase db=getWritableDatabase();
        db.beginTransaction();
        try { db.execSQL("UPDATE configs SET active=0"); db.execSQL("UPDATE configs SET active=1 WHERE id=?",new Object[]{id}); db.setTransactionSuccessful(); }
        finally { db.endTransaction(); }
    }
    public void deleteConfig(long id){ getWritableDatabase().delete(TABLE_CONFIGS,"id=?",new String[]{String.valueOf(id)}); }
}
