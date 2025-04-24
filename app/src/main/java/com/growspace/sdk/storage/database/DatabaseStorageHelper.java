package com.growspace.sdk.storage.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.growspace.sdk.model.Accessory;

import java.util.ArrayList;
import java.util.List;

public class DatabaseStorageHelper {
    private DatabaseStorage mDatabaseStorage;
    private SQLiteDatabase mSQLiteDatabase;

    public DatabaseStorageHelper(Context context) {
        this.mDatabaseStorage = new DatabaseStorage(context);
    }

    private void openWritableDatabase() {
        this.mSQLiteDatabase = this.mDatabaseStorage.getWritableDatabase();
    }

    private void openReadableDatabase() {
        this.mSQLiteDatabase = this.mDatabaseStorage.getReadableDatabase();
    }

    private void closeDatabase() {
        this.mDatabaseStorage.close();
        if (this.mSQLiteDatabase.isOpen()) {
            this.mSQLiteDatabase.close();
        }
    }

    public void deleteTableAccessory() {
        openWritableDatabase();
        this.mSQLiteDatabase.execSQL("DELETE FROM ACCESSORY");
        closeDatabase();
    }

    public List<Accessory> getAccessories() {
        ArrayList arrayList = new ArrayList();
        openReadableDatabase();
        Cursor rawQuery = this.mSQLiteDatabase.rawQuery("SELECT * FROM ACCESSORY", null);
        while (rawQuery.moveToNext()) {
            Accessory accessory = new Accessory();
            accessory.setName(rawQuery.getString(rawQuery.getColumnIndexOrThrow(DatabaseStorage.COLUMN_NAME)));
            accessory.setMac(rawQuery.getString(rawQuery.getColumnIndexOrThrow(DatabaseStorage.COLUMN_MAC)));
            accessory.setAlias(rawQuery.getString(rawQuery.getColumnIndexOrThrow(DatabaseStorage.COLUMN_ALIAS)));
            arrayList.add(accessory);
        }
        rawQuery.close();
        closeDatabase();
        return arrayList;
    }

    public Accessory getAccessory(String str) {
        Accessory accessory;
        openReadableDatabase();
        Cursor rawQuery = this.mSQLiteDatabase.rawQuery("SELECT * FROM ACCESSORY WHERE MAC = ?", new String[]{str});
        if (rawQuery.moveToFirst()) {
            accessory = new Accessory();
            accessory.setName(rawQuery.getString(rawQuery.getColumnIndexOrThrow(DatabaseStorage.COLUMN_NAME)));
            accessory.setMac(rawQuery.getString(rawQuery.getColumnIndexOrThrow(DatabaseStorage.COLUMN_MAC)));
            accessory.setAlias(rawQuery.getString(rawQuery.getColumnIndexOrThrow(DatabaseStorage.COLUMN_ALIAS)));
        } else {
            accessory = null;
        }
        rawQuery.close();
        closeDatabase();
        return accessory;
    }

    public void insertAccessory(Accessory accessory) {
        openWritableDatabase();
        SQLiteStatement compileStatement = this.mSQLiteDatabase.compileStatement("INSERT INTO ACCESSORY (NAME, MAC, ALIAS) VALUES (?, ?, ?)");
        compileStatement.bindString(1, accessory.getName());
        compileStatement.bindString(2, accessory.getMac());
        compileStatement.bindString(3, accessory.getAlias() != null ? accessory.getAlias() : "");
        compileStatement.executeInsert();
        compileStatement.close();
        closeDatabase();
    }

    public void deleteAccessory(Accessory accessory) {
        openWritableDatabase();
        SQLiteStatement compileStatement = this.mSQLiteDatabase.compileStatement("DELETE FROM ACCESSORY WHERE MAC = ?");
        compileStatement.bindString(1, accessory.getMac());
        compileStatement.execute();
        compileStatement.close();
        closeDatabase();
    }

    public void updateAccessoryAlias(Accessory accessory, String str) {
        openWritableDatabase();
        SQLiteStatement compileStatement = this.mSQLiteDatabase.compileStatement("UPDATE ACCESSORY SET ALIAS = ? WHERE MAC = ?");
        compileStatement.bindString(1, str);
        compileStatement.bindString(2, accessory.getMac());
        compileStatement.execute();
        compileStatement.close();
        closeDatabase();
    }
}
