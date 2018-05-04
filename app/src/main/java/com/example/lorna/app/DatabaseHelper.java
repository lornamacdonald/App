package com.example.lorna.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by User on 2/28/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "ClientDB";
    public static final String TABLE_NAME = "client_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "CLIENTID";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    /**
     * On create method
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,CLIENTID)");
    }

    /**
     * On upgrade method
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    /**
     * Insert method
     * @param clientID
     * @return
     */
    public boolean insertID(String clientID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,clientID);
        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    /**
     * Get all data method
     * @return
     */
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }

    /**
     * Count method
     * @return
     */
    public int count() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res.getCount();
    }

    /**
     * Delete method
     * @param id
     * @return
     */
    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "CLIENTID = ?",new String[] {id});
    }
}