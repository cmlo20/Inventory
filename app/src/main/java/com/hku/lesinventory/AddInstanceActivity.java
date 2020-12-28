package com.hku.lesinventory;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class AddInstanceActivity extends AppCompatActivity {
    private SQLiteDatabase db;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_instance);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        SQLiteOpenHelper inventoryDatabaseHelper = new InventoryDatabaseHelper(this);
        try {
            db = inventoryDatabaseHelper.getReadableDatabase();
            cursor = db.query("CATEGORY",
                    new String[]{"_id", "NAME"},
                    null, null, null, null, "NAME ASC");
            Spinner categorySpinner = findViewById(R.id.item_category);
            SimpleCursorAdapter categoryAdapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    cursor,
                    new String[]{"NAME"},
                    new int[]{android.R.id.text1}, 0);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(categoryAdapter);

            categorySpinner.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            SelectItemFragment frag = new SelectItemFragment();
                            frag.setCategory(id);
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.select_itemtype_frame, frag);
                            ft.commit();
                        }
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    }
            );
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this,
                    "Database unavailable",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();
    }
}