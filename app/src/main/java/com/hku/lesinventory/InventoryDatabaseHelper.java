package com.hku.lesinventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InventoryDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "les_inventory";
    private static final int DB_VERSION = 2;

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

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 1) {

            db.execSQL("CREATE TABLE Category ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "Name TEXT UNIQUE);");

            db.execSQL("CREATE TABLE Location ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "Name TEXT UNIQUE);");

            db.execSQL("CREATE TABLE Brand ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "Name TEXT UNIQUE);");

            db.execSQL("CREATE TABLE Item ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "Name TEXT UNIQUE, "
                    + "Description TEXT, "
                    + "Image BLOB, "
                    + "Brand INTEGER NOT NULL, "
                    + "Category INTEGER NOT NULL, "
                    + "FOREIGN KEY(Brand) REFERENCES Brand(_id), "
                    + "FOREIGN KEY(Category) REFERENCES Category(_id));");

            db.execSQL("CREATE TABLE ItemInstance ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "Rfid_Uii TEXT UNIQUE, "
                    + "Barcode TEXT UNIQUE, "
                    + "Location INTEGER NOT NULL, "
                    + "Item INTEGER NOT NULL, "
                    + "FOREIGN KEY(Location) REFERENCES Location(_id),"
                    + "FOREIGN KEY(Item) REFERENCES Item(_id));");
        }

        if (oldVersion < 2) {
            insertSampleData(db);
        }
    }

    private void insertSampleData(SQLiteDatabase db) {
        insertBrand(db, "Behringer");
        insertBrand(db, "Radial");
        insertBrand(db, "Sennheiser");
        insertBrand(db, "Shure");
        insertBrand(db, "Soundcraft");

        insertCategory(db, "Camera");
        insertCategory(db, "DI Box");
        insertCategory(db, "Interface");
        insertCategory(db, "Mic");
        insertCategory(db, "Mixer");
        insertCategory(db, "Tool");

        insertLocation(db, "CPD-2.74");
        insertLocation(db, "CPD-2.76");
    }

    private static void insertCategory(SQLiteDatabase db, String name) {
        ContentValues categoryValues = new ContentValues();
        categoryValues.put("Name", name);
        db.insert("Category", null, categoryValues);
    }

    private static void insertLocation(SQLiteDatabase db, String name) {
        ContentValues locationValues = new ContentValues();
        locationValues.put("Name", name);
        db.insert("Location", null, locationValues);
    }

    private static void insertBrand(SQLiteDatabase db, String name) {
        ContentValues brandValues = new ContentValues();
        brandValues.put("Name", name);
        db.insert("Brand", null, brandValues);
    }
}
