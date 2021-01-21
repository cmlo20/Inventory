package com.hku.lesinventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.densowave.scannersdk.Common.CommException;
import com.densowave.scannersdk.Common.CommManager;
import com.densowave.scannersdk.Common.CommScanner;
import com.densowave.scannersdk.Const.CommConst;
import com.densowave.scannersdk.Dto.RFIDScannerSettings;
import com.densowave.scannersdk.Listener.RFIDDataDelegate;
import com.densowave.scannersdk.Listener.ScannerAcceptStatusListener;
import com.densowave.scannersdk.RFID.RFIDData;
import com.densowave.scannersdk.RFID.RFIDDataReceivedEvent;
import com.densowave.scannersdk.RFID.RFIDException;
import com.densowave.scannersdk.RFID.RFIDScanner;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

/**
 * Add an instance of specific item type to the database.
 *
 **/
public class AddInstanceFragment extends Fragment
        implements View.OnClickListener, ScannerAcceptStatusListener, RFIDDataDelegate {
    private long itemId;
    private SQLiteDatabase db;
    private Cursor cursor;

    private CommScanner myScanner = null;
    private RFIDScanner rfidScanner = null;
    private RFIDScannerSettings orgScannerSettings = null;
    private EditText editTextReadUII;
    private Button readTagButton;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View layout = inflater.inflate(R.layout.fragment_add_instance, container, false);
        FloatingActionButton saveButton = layout.findViewById(R.id.save_button);
        ImageButton scanButton = layout.findViewById(R.id.scan_button);
        editTextReadUII = layout.findViewById(R.id.item_rfid_uii);
        readTagButton = layout.findViewById(R.id.readTag_button);
        readTagButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        scanButton.setOnClickListener(this);

        // Start waiting for SP1 to connect
        CommManager.addAcceptStatusListener(this);
        CommManager.startAccept();

        SQLiteOpenHelper inventoryDatabaseHelper = new InventoryDatabaseHelper(inflater.getContext());
        try {
            db = inventoryDatabaseHelper.getWritableDatabase();
            cursor = db.query("ITEM",
                            new String[]{"_id", "NAME", "DESCRIPTION", "IMAGE"},
                            "_id = ?", new String[] {Long.toString(itemId)},
                            null, null, null);
            if (cursor.moveToFirst()) {
                String nameText = cursor.getString(1);
                String descriptionText = cursor.getString(2);
                byte[] imageByte = cursor.getBlob(3);
                Bitmap image = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                // Populate views with data
                TextView name = layout.findViewById(R.id.item_name);
                TextView description = layout.findViewById(R.id.item_description);
                ImageView photo = layout.findViewById(R.id.item_image);
                name.setText(nameText);
                description.setText(descriptionText);
                photo.setImageBitmap(image);
                photo.setContentDescription(nameText);
            }
            cursor = db.query("LOCATION",
                            new String[]{"_id", "NAME"},
                            null, null, null, null, "NAME ASC");
            if (cursor.moveToFirst()) {
                Spinner locationSpinner = layout.findViewById(R.id.item_location);
                SimpleCursorAdapter locationAdapter = new SimpleCursorAdapter(
                        inflater.getContext(),
                        android.R.layout.simple_spinner_item,
                        cursor,
                        new String[]{"NAME"},
                        new int[]{android.R.id.text1}, 0);
                locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                locationSpinner.setAdapter(locationAdapter);
            }
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(inflater.getContext(),
                    "Database unavailable",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
        return layout;
    }

    @Override
    public void onDestroy() {
        if (rfidScanner != null && orgScannerSettings != null) {
            try {
                rfidScanner.setSettings(orgScannerSettings);
            } catch (RFIDException e) {
                e.printStackTrace();
            }
        }
        if (myScanner != null) {
            try {
                myScanner.close();
            } catch (CommException e) {
                e.printStackTrace();
            }
        }
        readTagButton.setEnabled(false);
        super.onDestroy();
        cursor.close();
        db.close();
    }

    @Override
    public void OnScannerAppeared(CommScanner commScanner) {
        myScanner = commScanner;
        try {
            myScanner.claim();
        } catch (CommException e) {
            e.printStackTrace();
        }
        CommManager.endAccept();
        CommManager.removeAcceptStatusListener(this);

        getActivity().runOnUiThread(new uiUpdaterConnected(myScanner));

        // Get RFID scanner object
        rfidScanner = myScanner.getRFIDScanner();
        rfidScanner.setDataDelegate(this);

        // Configure SP1 settings
        try {
            orgScannerSettings = rfidScanner.getSettings();

            RFIDScannerSettings myScannerSettings = rfidScanner.getSettings();
            myScannerSettings.scan.triggerMode = RFIDScannerSettings.Scan.TriggerMode.AUTO_OFF;
            myScannerSettings.scan.powerLevelRead = 10;
            rfidScanner.setSettings(myScannerSettings);
        } catch (RFIDException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRFIDDataReceived(CommScanner commScanner, RFIDDataReceivedEvent rfidDataReceivedEvent) {
        List<RFIDData> rfidDataList = rfidDataReceivedEvent.getRFIDData();
        if (rfidDataList.size() != 0) {
            RFIDData rfidData = rfidDataList.get(0);
            getActivity().runOnUiThread(new uiUpdaterRfidData(rfidData));
        }
        try {
            commScanner.buzzer(CommConst.CommBuzzerType.B1);
            rfidScanner.close();
        } catch (RFIDException | CommException e) {
            e.printStackTrace();
        }
    }

    private class uiUpdaterConnected implements Runnable {
        private CommScanner commScanner;

        uiUpdaterConnected(CommScanner scanner) {
            commScanner = scanner;
        }

        @Override
        public void run() {
            editTextReadUII.setHint(getString(R.string.rfid_reader_connected,
                    commScanner.getBTLocalName(), commScanner.getVersion()));
            readTagButton.setEnabled(true);
        }
    }

    private class uiUpdaterRfidData implements Runnable {
        private RFIDData rfidData;

        uiUpdaterRfidData(RFIDData data) { rfidData = data; }

        @Override
        public void run() {
            editTextReadUII.setText(byteToString(rfidData.getUII()));
        }
    }

    private String byteToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02X", b));
        }
        return stringBuilder.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_button:
                onClickSave(v);
                break;
            case R.id.scan_button:
                onClickScan();
            case R.id.readTag_button:
                onClickReadTag();
        }
    }

    private void onClickSave(View v) {
        View layout = v.getRootView();
        Spinner location = layout.findViewById(R.id.item_location);
        EditText barcode = layout.findViewById(R.id.item_barcode);

        ContentValues instanceValues = new ContentValues();
        instanceValues.put("ITEM", itemId);
        instanceValues.put("LOCATION", location.getSelectedItemId());
        if (!barcode.getText().toString().matches(""))
            instanceValues.put("BARCODE", Long.parseLong(barcode.getText().toString()));
        if (!editTextReadUII.getText().toString().matches(""))
            instanceValues.put("RFID_UII", editTextReadUII.getText().toString());

        try {
            db.insert("ITEMINSTANCE", null, instanceValues);
            Toast.makeText(layout.getContext(), "Item added", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Toast.makeText(layout.getContext(), "Database unavailable", Toast.LENGTH_SHORT).show();
        }
        getActivity().finish();
    }

    private void onClickScan() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setOrientationLocked(true);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.initiateScan();
    }

    private void onClickReadTag() {
        if (rfidScanner == null) return;
        try {
            rfidScanner.openInventory();
        } catch (RFIDException e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getActivity(), "Cancelled scan", Toast.LENGTH_SHORT).show();
            } else {    // scan successful
                EditText barcode = getView().findViewById(R.id.item_barcode);
                barcode.setText(result.getContents());
                //Toast.makeText(getActivity(), "Scan result: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    public void setItemType(long itemId) {
        this.itemId = itemId;
    }
}