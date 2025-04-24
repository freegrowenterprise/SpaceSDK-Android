package com.growspace.sdk.storage.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseStorage extends SQLiteOpenHelper {
    public static final String COLUMN_ALIAS = "ALIAS";
    public static final String COLUMN_MAC = "MAC";
    public static final String COLUMN_NAME = "NAME";
    private static final String DATABASE_NAME = "accessories.db";

    public DatabaseStorage(Context context) {
        super(context, DATABASE_NAME, null, 2);
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
