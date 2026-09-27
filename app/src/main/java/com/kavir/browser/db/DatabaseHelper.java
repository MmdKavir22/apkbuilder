package com.kavir.browser.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "kavir_browser.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_HISTORY = "history";
    public static final String TABLE_BOOKMARKS = "bookmarks";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + " (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, timestamp LONG)");
        db.execSQL("CREATE TABLE " + TABLE_BOOKMARKS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
        onCreate(db);
    }

    public void addHistory(String title, String url) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("url", url);
        cv.put("timestamp", System.currentTimeMillis());
        db.insert(TABLE_HISTORY, null, cv);
    }

    public void clearHistory() {
        getWritableDatabase().delete(TABLE_HISTORY, null, null);
    }
}
