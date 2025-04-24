package com.growspace.sdk.storage.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseStorage extends SQLiteOpenHelper {
    public static final String COLUMN_ALIAS = "ALIAS";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_MAC = "MAC";
    public static final String COLUMN_NAME = "NAME";
    public static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String DATABASE_NAME = "accessories.db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_ACCESSORY = "ACCESSORY";
    private final String TABLE_ACCESSORY_CREATE;

    public DatabaseStorage(Context context) {
        super(context, DATABASE_NAME, null, 2);
        this.TABLE_ACCESSORY_CREATE = "CREATE TABLE ACCESSORY(ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME VARCHAR, MAC VARCHAR, ALIAS VARCHAR);";
    }

    @Override
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE ACCESSORY(ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME VARCHAR, MAC VARCHAR, ALIAS VARCHAR);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS ACCESSORY");
        onCreate(sQLiteDatabase);
    }
}
