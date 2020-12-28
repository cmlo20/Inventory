package com.hku.lesinventory;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class SelectItemFragment extends Fragment {
    private long categoryId;
    private SQLiteDatabase db;
    private Cursor cursor;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View layout = inflater.inflate(R.layout.fragment_select_item, container, false);
        SQLiteOpenHelper inventoryDatabaseHelper = new InventoryDatabaseHelper(inflater.getContext());
        try {
            db = inventoryDatabaseHelper.getReadableDatabase();
            cursor = db.query("ITEM",
                            new String[]{"_id", "NAME"},
                            "CATEGORY = ?", new String[] {Long.toString(categoryId)},
                            null, null, "NAME ASC");
            Spinner itemTypeSpinner = layout.findViewById(R.id.item_type);
            SimpleCursorAdapter itemTypeAdapter = new SimpleCursorAdapter(
                    inflater.getContext(),
                    android.R.layout.simple_spinner_item,
                    cursor,
                    new String[]{"NAME"},
                    new int[]{android.R.id.text1}, 0);
            itemTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            itemTypeSpinner.setAdapter(itemTypeAdapter);

            itemTypeSpinner.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            AddInstanceFragment frag = new AddInstanceFragment();
                            frag.setItemType(id);
                            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                            ft.replace(R.id.edit_instance_frame, frag);
                            ft.addToBackStack(null);
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            ft.commit();
                        }

                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    }
            );

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(inflater.getContext(), "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();
    }

    public void setCategory(long categoryId) {
        this.categoryId = categoryId;
    }
}