package com.frsarker.newsapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    public static final String databseName = "NewsAppBookmarks.db";
    public static final String tableName = "bookmarks";

    public DBHelper(Context context) {
        super(context, databseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "CREATE TABLE " + tableName + "(id INTEGER PRIMARY KEY, post_id TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db);
    }

    public void insertBookmark(String post_id) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("post_id", post_id);
        db.insert(tableName, null, contentValues);
    }

    public Boolean isBookmarked(String post_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + tableName + " WHERE post_id = '" + post_id + "' order by id desc", null);
        if(res.getCount() > 0) {
            return true;
        }else{
            return false;
        }
    }

    public void deleteBookmark(Context c, String post_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, "post_id = ? ", new String[]{post_id});
        try {
            BookmarksActivity.getInstance().reloadListView();
        }catch (Exception e){}
    }

    public String getAllBookmarks() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select post_id from " + tableName + " order by id desc", null);
        cursor.moveToFirst();

        ArrayList<String> mylist = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            mylist.add(cursor.getString(cursor.getColumnIndex("post_id")));
            cursor.moveToNext();
        }
        return android.text.TextUtils.join(",", mylist);
    }
}