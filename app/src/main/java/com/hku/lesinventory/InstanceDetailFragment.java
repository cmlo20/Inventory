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

public class InstanceDetailFragment extends Fragment {

    private int itemId, instanceNumber;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View layout = inflater.inflate(R.layout.fragment_instance_detail, container, false);

        SQLiteOpenHelper inventoryDatabaseHelper = new InventoryDatabaseHelper(inflater.getContext());
        try {
            SQLiteDatabase db = inventoryDatabaseHelper.getReadableDatabase();
            Cursor cursor = db.query("ITEMINSTANCE",
                    new String[] {"_id", "LOCATION", "BARCODE", "RFID_UII", "REMARKS"},
                    "ITEM = ?",
                    new String[] {Integer.toString(itemId)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                for (int i = 1; i <= instanceNumber; i++) {
                    if (i == instanceNumber) {
                        int locationId = cursor.getInt(1);
                        Long barcodeNumber = cursor.getLong(2);
                        String rfidUII = cursor.getString(3);
                        String remarksText = cursor.getString(4);
                        cursor = db.query("LOCATION",
                                new String[] {"_id", "NAME"},
                                "_id = ?",
                                new String[] {Integer.toString(locationId)},
                                null, null, null, null);
                        String locationText = cursor.moveToFirst() ? cursor.getString(1) : "No record";

                        TextView location = layout.findViewById(R.id.location);
                        TextView barcode = layout.findViewById(R.id.barcode);
                        TextView rfid = layout.findViewById(R.id.rfid_uii);
                        TextView remarks = layout.findViewById(R.id.remarks);
                        location.setText(locationText);
                        if (barcodeNumber == 0)
                            barcode.setText("N/A");
                        else
                            barcode.setText(Long.toString(barcodeNumber));
                        if (rfidUII.equals(""))
                            rfid.setText("N/A");
                        else
                            rfid.setText(rfidUII);
                    } else if (!cursor.moveToNext()) {
                        break;
                    }
                }
            }
            cursor.close();
            db.close();
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(inflater.getContext(), "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
        return layout;
    }

    public void setItem(int itemId) {
        this.itemId = itemId;
    }

    public void setInstance(int position) {
        this.instanceNumber = position;
    }
}