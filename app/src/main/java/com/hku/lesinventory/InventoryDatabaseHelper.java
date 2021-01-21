package com.hku.lesinventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InventoryDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "les_inventory";
    private static final int DB_VERSION = 3;

    InventoryDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateMyDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db, oldVersion, newVersion);
    }

    private static void insertCategory(SQLiteDatabase db, String name) {
        ContentValues categoryValues = new ContentValues();
        categoryValues.put("NAME", name);
        db.insert("CATEGORY", null, categoryValues);
    }

    private static void insertLocation(SQLiteDatabase db, String name) {
        ContentValues locationValues = new ContentValues();
        locationValues.put("NAME", name);
        db.insert("LOCATION", null, locationValues);
    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {   // New user

            db.execSQL("CREATE TABLE CATEGORY ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "NAME TEXT UNIQUE);");

            db.execSQL("CREATE TABLE LOCATION ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "NAME TEXT UNIQUE);");

            db.execSQL("CREATE TABLE ITEM ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "NAME TEXT UNIQUE, "
                    + "DESCRIPTION TEXT, "
                    + "IMAGE BLOB, "
                    + "CATEGORY INTEGER NOT NULL, "
                    + "FOREIGN KEY(CATEGORY) REFERENCES CATEGORY(_id));");

            db.execSQL("CREATE TABLE ITEMINSTANCE ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "BARCODE INTEGER UNIQUE, "
                    + "REMARKS TEXT, "
                    + "LOCATION INTEGER NOT NULL, "
                    + "ITEM INTEGER NOT NULL, "
                    + "FOREIGN KEY(LOCATION) REFERENCES LOCATION(_id),"
                    + "FOREIGN KEY(ITEM) REFERENCES ITEM(_id));");

            insertLocation(db, "CPD-2.74");
            insertLocation(db, "CPD-2.76");

            insertCategory(db, "Extender");
            insertCategory(db, "DI box");
            insertCategory(db, "Mic");
            insertCategory(db, "Interface");
            insertCategory(db, "Mixer");
            insertCategory(db, "Amp");
            insertCategory(db, "AVIP/EDID");
            insertCategory(db, "Scaler/Converter");
            insertCategory(db, "Tool");
            insertCategory(db, "Switcher");
            insertCategory(db, "Recorder");
            insertCategory(db, "Camera");
        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE ITEMINSTANCE ADD COLUMN RFID_UII TEXT");
        }
    }
}
