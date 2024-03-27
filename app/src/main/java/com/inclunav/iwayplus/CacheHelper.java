package com.inclunav.iwayplus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CacheHelper extends SQLiteOpenHelper {

    //this database stores the cached jsonarray to string recieved from server
    public static final String TABLE_NAME = "cache_table";
    private static final String COL1 = "key_string";
    private static final String COL2 = "data";
    private static final String COL3 = "building_name";
    private static final String COL4 = "insert_time"; //time in currentTimeMillis




    public CacheHelper(Context context){
        super(context,TABLE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL1 +" TEXT, "+ COL2  +" TEXT, "+ COL3  +" TEXT, "+ COL4  +" INTEGER )";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//        onCreate(db);
    }

    public void insertData(String key,String buildingName, String data, Long time){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1,key);
        contentValues.put(COL2,data);
        contentValues.put(COL3,buildingName);
        contentValues.put(COL4,time);

        db.insert(TABLE_NAME,null,contentValues);

    }

    public String dataGivenKey(String key){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " where "+ COL1 +" = "+ "'" +key+ "'";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            //no tuple exists so create
            cursor.close();
            return null;
        }
        else{
           return cursor.getString(2);
        }

    }

    //returns only if the stored data is not older then cache_time_millis
    public String dataGivenBuilding(String buildingName){

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " where "+ COL3 +" = "+ "'" +buildingName+ "'";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            //no tuple exists so create
            cursor.close();
            return null;
        }
        else{
            cursor.moveToLast();
            if(System.currentTimeMillis()-cursor.getLong(4) < Utils.cache_time_millis){
                return cursor.getString(2);
            }
            return null;
        }
    }



    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
    }

    public void deleteOldData() {
        long currentTime = System.currentTimeMillis();
        long expiryTime = currentTime - 15*86400000;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME+" where "+COL4+"<"+expiryTime);
    }

}

