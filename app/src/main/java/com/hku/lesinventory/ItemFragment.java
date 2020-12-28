package com.hku.lesinventory;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

/* Display a list of inventory items of a specific category */
public class ItemFragment extends Fragment {
    private int categoryId;
    private SQLiteDatabase db;
    private Cursor cursor;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        RecyclerView itemRecycler = (RecyclerView) inflater.inflate(
                R.layout.fragment_item, container, false);

        SQLiteOpenHelper inventoryDatabaseHelper = new InventoryDatabaseHelper(inflater.getContext());
        try {   // use AsyncTask to read database in background!
            db = inventoryDatabaseHelper.getReadableDatabase();
            cursor = db.query("ITEM",
                              new String[]{"_id", "NAME", "DESCRIPTION", "IMAGE"},
                              "CATEGORY = ?", new String[] {Integer.toString(categoryId)},
                              null, null, "NAME ASC");
            int numItems = cursor.getCount();
            int[] itemIds = new int[numItems];
            String[] itemNames = new String[numItems];
            String[] itemDescriptions = new String[numItems];
            Bitmap[] itemImages = new Bitmap[numItems];

            if (cursor.moveToFirst()) {
                for (int i = 0; i < numItems; i++) {
                    itemIds[i] = cursor.getInt(0);
                    itemNames[i] = cursor.getString(1);
                    itemDescriptions[i] = cursor.getString(2);
                    byte[] imageByte = cursor.getBlob(3);
                    itemImages[i] = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                    if (!cursor.moveToNext())   break;
                }
            }
            // bind adapter to the recycler view
            ThumbnailItemAdapter adapter = new ThumbnailItemAdapter(itemIds, itemNames, itemDescriptions, itemImages);
            itemRecycler.setAdapter(adapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            itemRecycler.setLayoutManager(layoutManager);

            adapter.setListener(new ThumbnailItemAdapter.Listener() {
                @Override
                public void onClick(int itemId) {
                    Intent intent = new Intent(getActivity(), ItemDetailActivity.class);
                    intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, itemId);
                    getActivity().startActivity(intent);
                }
            });
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(inflater.getContext(),
                                    "Database unavailable",
                                         Toast.LENGTH_SHORT);
            toast.show();
        }
        return itemRecycler;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();
    }

    public void setCategory(int categoryId) {
        this.categoryId = categoryId;
    }

}