package com.example.sicurashare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DatabaseHelper extends SQLiteOpenHelper
{

    public static final String database_name = "Database.db";
    public static final String table_name="datatable";
    public static final String col1="id";
    public static final String col2="entry";
    public static final String col3="datetime";

    public DatabaseHelper(Context context) {
        super(context, database_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+table_name+"(id integer primary key autoincrement, entry text,datetime text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + table_name);
    }

    public void insertData(String entry,String date)
    {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv =new ContentValues();
        cv.put(col2,entry);
        cv.put(col3,date);
        long result= db.insert(table_name,null,cv);

    }

    public Cursor showData(){
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor cursor =db.rawQuery("select * from "+table_name,null);
        return cursor;
    }

    public void deleteALL()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + table_name);
        db.execSQL("create table "+table_name+"(id integer primary key autoincrement, entry text,datetime text)");
    }
}
