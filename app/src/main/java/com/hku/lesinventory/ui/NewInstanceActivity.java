package com.hku.lesinventory.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.hku.lesinventory.BaseActivity;
import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.NewInstanceActivityBinding;
import com.hku.lesinventory.db.entity.InstanceEntity;
import com.hku.lesinventory.db.entity.LocationEntity;
import com.hku.lesinventory.viewmodel.ItemViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewInstanceActivity extends BaseActivity
        implements View.OnClickListener, NewOptionDialogFragment.NewOptionDialogListener, RFIDDataDelegate {

    static final String KEY_ITEM_ID = "item_id";
    private int mItemId;
    private NewInstanceActivityBinding mBinding;
    private ItemViewModel mItemViewModel;
    private List<InstanceEntity> mAllInstances;     // instance list used for input validation

    private RFIDScanner mRfidScanner = null;
    private boolean mScannerConnectedOnCreate = false;
    private RFIDScannerSettings mOriginalScannerSettings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.new_instance_activity);

        mScannerConnectedOnCreate = super.isCommScanner();
        super.startService();

        mBinding.collapsingToolbarLayout.setTitleEnabled(false);    // Disable custom toolbar title to display activity label in toolbar
        setSupportActionBar(mBinding.toolbar.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mItemId = getIntent().getExtras().getInt(KEY_ITEM_ID);
        // Inject itemId into view model
        ItemViewModel.Factory factory = new ItemViewModel.Factory(getApplication(), mItemId);
        mItemViewModel = new ViewModelProvider(this, factory)
                .get(ItemViewModel.class);

        mBinding.setLifecycleOwner(this);
        mBinding.setItemViewModel(mItemViewModel);
        mBinding.addLocationButton.setOnClickListener(this);
        mBinding.rfidScanButton.setOnClickListener(this);
        mBinding.saveInstanceButton.setOnClickListener(this);

        populateLocationSpinner(mBinding.locationSpinner);
        // Display item image
        mItemViewModel.getImageUriString().observe(this, imageUriString -> {
            if (imageUriString != null) {
                Uri imageUri = Uri.parse(imageUriString);
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    mBinding.itemImage.setImageBitmap(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mItemViewModel.getAllInstances().observe(this, instances -> {
            mAllInstances = instances;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mScannerConnectedOnCreate) {
            mRfidScanner = super.getCommScanner().getRFIDScanner();
            mRfidScanner.setDataDelegate(this);  // register as data listener
            configureScannerSettings();
            mBinding.rfidEdittext.setHint(getString(R.string.rfid_reader_connected,
                    super.getCommScanner().getBTLocalName()));
            mBinding.rfidScanButton.setEnabled(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRfidScanner != null && mOriginalScannerSettings != null) {
            try {
                mRfidScanner.close();
                mRfidScanner.setDataDelegate(null);
                mRfidScanner.setSettings(mOriginalScannerSettings);
            } catch (RFIDException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void populateLocationSpinner(Spinner spinner) {
        ArrayAdapter<String> locationSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        locationSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(locationSpinnerAdapter);
        // Observer location livedata from view model
        mItemViewModel.loadLocations().observe(this, locations -> {
            locationSpinnerAdapter.clear();
            for (LocationEntity location : locations) {
                locationSpinnerAdapter.add(location.getName());
            }
            locationSpinnerAdapter.notifyDataSetChanged();
        });
    }

    private boolean formIsValid() {
        String rfidUii = mBinding.rfidEdittext.getText().toString();
        if (rfidUii.isEmpty()) {
            mBinding.rfidEdittext.setError(getString(R.string.warn_empty_rfid));
            return false;
        }

        if (mAllInstances != null) {
            for (InstanceEntity instance : mAllInstances) {
                if (rfidUii.equals(instance.getRfidUii())) {
                    mBinding.rfidEdittext.setError(getString(R.string.warn_duplicate_rfid));
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onDialogPositiveClick(NewOptionDialogFragment dialog) {
        int dialogTitleId = dialog.getTitleId();
        EditText nameEditText = dialog.getDialog().findViewById(R.id.new_option_name);
        String newOptionName = nameEditText.getText().toString();

        switch (dialogTitleId) {
            case R.string.title_new_location:
                LocationEntity newLocation = new LocationEntity(newOptionName);
                mItemViewModel.insertLocation(newLocation);
                Toast.makeText(this, R.string.toast_location_saved, Toast.LENGTH_SHORT).show();
                break;

            default:

        }
    }

    @Override
    public void onDialogNegativeClick(NewOptionDialogFragment dialog) {
        // no-op
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_location_button:
                DialogFragment newLocationDialog = new NewOptionDialogFragment(R.string.title_new_location);
                newLocationDialog.show(getSupportFragmentManager(), String.valueOf(R.string.title_new_location));
                break;

            case R.id.rfid_scan_button:
                if (!mScannerConnectedOnCreate) {
                    Toast.makeText(this, R.string.toast_scanner_not_connected, Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    mRfidScanner.openInventory();
                    Toast.makeText(this, R.string.toast_scan_item_instruction, Toast.LENGTH_SHORT).show();
                } catch (RFIDException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.save_instance_button:
                if (formIsValid()) {
                    new SaveInstanceTask(this).execute();
                }
                break;

            default:

        }
    }

    @Override
    public void onRFIDDataReceived(CommScanner commScanner, RFIDDataReceivedEvent rfidDataReceivedEvent) {
        List<RFIDData> rfidDataList = rfidDataReceivedEvent.getRFIDData();
        if (rfidDataList.size() != 0) {
            RFIDData rfidData = rfidDataList.get(0);
            runOnUiThread(new uiUpdaterRfidData(rfidData));
        }
        try {
            commScanner.buzzer(CommConst.CommBuzzerType.B1);
        } catch (CommException e) {
            e.printStackTrace();
        }
    }

    private void configureScannerSettings() {
        // Configure SP1 settings
        try {
            // Save original scanner settings for restoring later
            mOriginalScannerSettings = mRfidScanner.getSettings();

            RFIDScannerSettings myScannerSettings = mRfidScanner.getSettings();
            myScannerSettings.scan.triggerMode = RFIDScannerSettings.Scan.TriggerMode.AUTO_OFF;
            myScannerSettings.scan.powerLevelRead = 10;
            mRfidScanner.setSettings(myScannerSettings);
        } catch (RFIDException e) {
            e.printStackTrace();
        }
    }


    private class uiUpdaterRfidData implements Runnable {
        private RFIDData rfidData;

        uiUpdaterRfidData(RFIDData data) { rfidData = data; }

        @Override
        public void run() {
            mBinding.rfidEdittext.setText(byteToString(rfidData.getUII()));
        }
    }

    private String byteToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02X", b));
        }
        return stringBuilder.toString();
    }



    public class SaveInstanceTask extends AsyncTask<Void, Void, Boolean> {

        private Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mBinding.loadingIndicator.setVisibility(View.VISIBLE);
        }

        public SaveInstanceTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String rfidUii = mBinding.rfidEdittext.getText().toString();
            String location = mBinding.locationSpinner.getSelectedItem().toString();
            int locationId = mItemViewModel.getLocationId(location);
            Date checkedInAt = new Date(System.currentTimeMillis());

            InstanceEntity newInstance = new InstanceEntity(mItemId, locationId, rfidUii, checkedInAt);
            mItemViewModel.insertInstance(newInstance);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mBinding.loadingIndicator.setVisibility(View.INVISIBLE);
            if (success) {
                Toast.makeText(context, R.string.toast_item_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}