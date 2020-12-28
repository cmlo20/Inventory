package com.hku.lesinventory;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddCategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void onClickSave(View view) {
        EditText newCategory = findViewById(R.id.item_category);
        ContentValues cv = new ContentValues();
        cv.put("NAME", newCategory.getText().toString());

        SQLiteOpenHelper inventoryDatabaseHelper = new InventoryDatabaseHelper(AddCategoryActivity.this);
        try {
            SQLiteDatabase db = inventoryDatabaseHelper.getWritableDatabase();
            db.insert("CATEGORY", null, cv);
            db.close();
            Toast.makeText(AddCategoryActivity.this, "Category added", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Toast.makeText(AddCategoryActivity.this, "Database unavailable", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}