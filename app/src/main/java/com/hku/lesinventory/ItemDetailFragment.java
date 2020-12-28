package com.hku.lesinventory;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ItemDetailFragment extends Fragment {
    private int itemId;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View layout = inflater.inflate(R.layout.fragment_item_detail, container, false);
        SQLiteOpenHelper inventoryDatabaseHelper = new InventoryDatabaseHelper(inflater.getContext());
        try {
            SQLiteDatabase db = inventoryDatabaseHelper.getReadableDatabase();
            Cursor cursor = db.query("ITEM",
                                    new String[] {"_id", "NAME", "DESCRIPTION", "CATEGORY"},
                                    "_id = ?",
                                    new String[] {Integer.toString(itemId)},
                                    null, null, null);
            if (cursor.moveToFirst()) {
                String nameText = cursor.getString(1);
                String descriptionText = cursor.getString(2);
                int categoryId = cursor.getInt(3);

                cursor = db.query("CATEGORY",
                                new String[] {"_id", "NAME"},
                                "_id = ?",
                                new String[] {Integer.toString(categoryId)},
                                null, null, null, null);
                String categoryText = cursor.moveToFirst() ? cursor.getString(1) : "";

                cursor = db.query("ITEMINSTANCE",
                                new String[] {"_id", "ITEM"},
                                "ITEM = ?",
                                new String[] {Integer.toString(itemId)},
                                null, null, null);
                int instanceQuantity = cursor.moveToFirst() ? cursor.getCount() : 0;

                TextView name = layout.findViewById(R.id.name);
                TextView description = layout.findViewById(R.id.description);
                TextView category = layout.findViewById(R.id.category);
                TextView quantity = layout.findViewById(R.id.quantity);
                name.setText(nameText);
                description.setText(descriptionText);
                category.setText(categoryText);
                quantity.setText(Integer.toString(instanceQuantity));

                cursor.close();
                db.close();
            }
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(inflater.getContext(), "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
        return layout;
    }

    public void setItem(int itemId) {
        this.itemId = itemId;
    }
}