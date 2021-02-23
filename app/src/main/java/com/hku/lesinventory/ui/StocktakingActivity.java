package com.hku.lesinventory.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.densowave.scannersdk.Common.CommException;
import com.densowave.scannersdk.Common.CommManager;
import com.densowave.scannersdk.Common.CommScanner;
import com.densowave.scannersdk.Dto.RFIDScannerSettings;
import com.densowave.scannersdk.Listener.RFIDDataDelegate;
import com.densowave.scannersdk.Listener.ScannerAcceptStatusListener;
import com.densowave.scannersdk.RFID.RFIDData;
import com.densowave.scannersdk.RFID.RFIDDataReceivedEvent;
import com.densowave.scannersdk.RFID.RFIDException;
import com.densowave.scannersdk.RFID.RFIDScanner;
import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.StocktakingActivityBinding;
import com.hku.lesinventory.db.entity.InstanceEntity;
import com.hku.lesinventory.db.entity.LocationEntity;
import com.hku.lesinventory.viewmodel.InstanceListViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


// Todo: Compare location instance list with scanned instance list
public class StocktakingActivity extends AppCompatActivity
        implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        ScannerAcceptStatusListener, RFIDDataDelegate {

    private StocktakingActivityBinding mBinding;

    private TagInstanceAdapter mTagInstanceAdapter;

    private InstanceListViewModel mInstanceListViewModel;

    private List<String> mScannedRfidList = new ArrayList<>();
    private LocationEntity mCurrentLocation;    // used for updating scanned instance location
    private List<InstanceEntity> mLocationInstancesList = new ArrayList<>();

    private CommScanner mCommScanner = null;
    private RFIDScanner mRfidScanner = null;
    private RFIDScannerSettings mOriginalScannerSettings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.stocktaking_activity);
        setSupportActionBar(mBinding.toolbar.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTagInstanceAdapter = new TagInstanceAdapter(mInstanceClickCallback);
        mBinding.tagInstanceList.setAdapter(mTagInstanceAdapter);

        mBinding.startButton.setOnClickListener(this);
        mBinding.clearButton.setOnClickListener(this);
        mBinding.setLifecycleOwner(this);

        mInstanceListViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(InstanceListViewModel.class);
        subscribeToModel();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start waiting for SP1 to connect
        CommManager.addAcceptStatusListener(this);
        CommManager.startAccept();
    }

    @Override
    public void onStop() {
        super.onStop();
        closeScanner();
    }

    private void subscribeToModel() {
        // Create adapters for location spinners
        ArrayAdapter<LocationEntity> locationSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        locationSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.locationSpinner.setAdapter(locationSpinnerAdapter);
        mBinding.locationSpinner.setOnItemSelectedListener(this);

        mInstanceListViewModel.loadLocations().observe(this, locations -> {
            locationSpinnerAdapter.clear();
            for (LocationEntity location : locations) {
                locationSpinnerAdapter.add(location);
            }
            locationSpinnerAdapter.notifyDataSetChanged();
        });

        mInstanceListViewModel.loadBrands().observe(this, brands -> {
            if (brands != null) {
                mTagInstanceAdapter.setBrandList(brands);
                mTagInstanceAdapter.notifyDataSetChanged();
            }
        });

        mInstanceListViewModel.loadItems().observe(this, items -> {
            if (items != null) {
                mTagInstanceAdapter.setItemList(items);
                mTagInstanceAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.start_button:
                if (mRfidScanner != null) {
                    mBinding.clearButton.setEnabled(true);
                    mBinding.startButton.setEnabled(false);
                    mBinding.locationSpinner.setEnabled(false);
                    mCurrentLocation = (LocationEntity) mBinding.locationSpinner.getSelectedItem();
//                    mBinding.clearButton.setTextColor(getColor(R.color.text_default_disabled));
                    try {
                        mRfidScanner.openInventory();
                        Toast.makeText(this, R.string.toast_stocktaking_instruction, Toast.LENGTH_SHORT).show();
                    } catch (RFIDException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, R.string.toast_scanner_not_connected, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.clear_button:
                mBinding.startButton.setEnabled(true);
                mBinding.locationSpinner.setEnabled(true);
                mBinding.clearButton.setEnabled(false);
                mScannedRfidList.clear();
                mTagInstanceAdapter.clearInstanceList();
                mBinding.scannedItemsCount.setText(String.valueOf(mTagInstanceAdapter.getItemCount()));
                if (mRfidScanner != null) {
                    try {
                        mRfidScanner.close();
                    } catch (RFIDException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        LocationEntity location = (LocationEntity) parent.getItemAtPosition(pos);
        mInstanceListViewModel.loadInstancesInLocation(location.getId())
                .observe(this, instances -> {
                    if (instances != null) {
                        // Update instance list and total item count
                        mLocationInstancesList.clear();
                        mLocationInstancesList.addAll(instances);
                        mBinding.setTotalItemsCount(instances.size());
                    }
                });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void OnScannerAppeared(CommScanner commScanner) {
        mCommScanner = commScanner;
        try {
            mCommScanner.claim();
        } catch (CommException e) {
            e.printStackTrace();
        }
        CommManager.endAccept();
        CommManager.removeAcceptStatusListener(this);

        runOnUiThread(new StocktakingActivity.uiUpdaterConnected(mCommScanner));

        // Get RFID scanner object
        mRfidScanner = mCommScanner.getRFIDScanner();
        mRfidScanner.setDataDelegate(this);

        // Configure SP1 settings
        try {
            mOriginalScannerSettings = mRfidScanner.getSettings();

            RFIDScannerSettings myScannerSettings = mRfidScanner.getSettings();
            myScannerSettings.scan.triggerMode = RFIDScannerSettings.Scan.TriggerMode.ALTERNATE;
            myScannerSettings.scan.powerLevelRead = 30;
            mRfidScanner.setSettings(myScannerSettings);
        } catch (RFIDException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRFIDDataReceived(CommScanner commScanner, RFIDDataReceivedEvent rfidDataReceivedEvent) {  // Background thread
        List<RFIDData> rfidDataList = rfidDataReceivedEvent.getRFIDData();
        if (rfidDataList.size() != 0) {
            RFIDData rfidData = rfidDataList.get(0);
            String rfidUii = byteToString(rfidData.getUII());
            if (!mScannedRfidList.contains(rfidUii)) {
                mScannedRfidList.add(rfidUii);
                InstanceEntity instance = mInstanceListViewModel.loadInstanceByRfid(rfidUii);
                if (instance != null) {
                    // Update instance location and check-in time
                    instance.setCheckedInAt(new Date(System.currentTimeMillis()));
                    instance.setLocationId(mCurrentLocation.getId());
                    mInstanceListViewModel.updateInstance(instance);


                    runOnUiThread(new StocktakingActivity.uiUpdaterInstanceData(instance));
//                    try {
//                        commScanner.buzzer(CommConst.CommBuzzerType.B1);
//                    } catch (CommException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }
    }

    private class uiUpdaterConnected implements Runnable {
        private CommScanner commScanner;

        uiUpdaterConnected(CommScanner scanner) {
            this.commScanner = scanner;
        }

        @Override
        public void run() {
            mBinding.readerConnectionStatus.setText(getString(R.string.rfid_reader_connected,
                    commScanner.getBTLocalName()));
        }
    }

    private class uiUpdaterInstanceData implements Runnable {
        private InstanceEntity instance;

        uiUpdaterInstanceData(InstanceEntity instance) { this.instance = instance; }

        @Override
        public void run() {
            mBinding.setHasTagInstance(true);
            mTagInstanceAdapter.addInstance(instance);
            mBinding.scannedItemsCount.setText(String.valueOf(mTagInstanceAdapter.getItemCount()));
        }
    }

    private String byteToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02X", b));
        }
        return stringBuilder.toString();
    }

    private void closeScanner() {
        if (mRfidScanner != null && mOriginalScannerSettings != null) {
            try {
                mRfidScanner.setSettings(mOriginalScannerSettings);
                mRfidScanner.close();
            } catch (RFIDException e) {
                e.printStackTrace();
            }
        }
        if (mCommScanner != null) {
            try {
                mCommScanner.close();
            } catch (CommException e) {
                e.printStackTrace();
            }
        }
    }

    private final InstanceClickCallback mInstanceClickCallback = instance -> {
        // no-op
    };
}