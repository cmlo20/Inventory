package com.hku.lesinventory.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.res.Resources;
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
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.InstanceEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.db.entity.LocationEntity;
import com.hku.lesinventory.viewmodel.InstanceListViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


// Todo: Compare location instance list with scanned instance list to find missing instances
public class StocktakingActivity extends AppCompatActivity
        implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        ScannerAcceptStatusListener, RFIDDataDelegate {

    private StocktakingActivityBinding mBinding;

    private TagInstanceAdapter mTagInstanceAdapter;

    private InstanceListViewModel mInstanceListViewModel;

    private ReadAction nextReadAction = ReadAction.START;

    private final List<String> mScannedRfidList = new ArrayList<>();
    private LocationEntity mCurrentLocation;    // used for updating location of scanned instance
    private LocationEntity mPreviousLocation;
    private final List<InstanceEntity> mInstancesInLocation = new ArrayList<>();
    private final List<InstanceEntity> mMissingInstances = new ArrayList<>();

    // Used for showing missing instances details
    private List<ItemEntity> mItemList;
    private List<BrandEntity> mBrandList;

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

        mBinding.readToggleButton.setOnClickListener(this);
        mBinding.findMissingButton.setOnClickListener(this);
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
                mBrandList = brands;
                mTagInstanceAdapter.setBrandList(brands);
                mTagInstanceAdapter.notifyDataSetChanged();
            }
        });

        mInstanceListViewModel.loadItems().observe(this, items -> {
            if (items != null) {
                mItemList = items;
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
            case R.id.read_toggle_button:
                runReadAction();
                break;

            case R.id.find_missing_button:
                showMissingItems();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        LocationEntity location = (LocationEntity) parent.getItemAtPosition(pos);

        // Remove observers for instances in previously selected location so ui e.g. total items count won't be updated incorrectly
        if (mPreviousLocation != null)
            mInstanceListViewModel.loadInstancesInLocation(mPreviousLocation.getId()).removeObservers(this);

        // Observer instances in currently selected location
        mInstanceListViewModel.loadInstancesInLocation(location.getId())
                .observe(this, instancesInLocation -> {
                    if (instancesInLocation != null) {
                        // Update location instance list and total items count
                        mInstancesInLocation.clear();
                        mInstancesInLocation.addAll(instancesInLocation);
                        mBinding.setTotalItemsCount(mInstancesInLocation.size());
                    }
                });

        mPreviousLocation = location;

        // Clear scanned rfid list and recyclerview adapter
        mScannedRfidList.clear();
        mTagInstanceAdapter.clearInstanceList();
        mBinding.scannedItemsCount.setText(String.valueOf(mTagInstanceAdapter.getItemCount()));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void runReadAction() {
        switch (nextReadAction) {
            case START:
                if (mRfidScanner != null) {
                    mBinding.locationSpinner.setEnabled(false);
                    mBinding.findMissingButton.setEnabled(true);
                    mBinding.findMissingButton.setBackgroundColor(getColor(R.color.button_find_missing));

                    mCurrentLocation = (LocationEntity) mBinding.locationSpinner.getSelectedItem();
                    mMissingInstances.clear();
                    mMissingInstances.addAll(mInstancesInLocation);     // copy item instances in selected location for finding missing instances
                    try {
                        mRfidScanner.openInventory();
                        Toast.makeText(this, R.string.toast_stocktaking_instruction, Toast.LENGTH_SHORT).show();
                    } catch (RFIDException e) {
                        e.printStackTrace();
                    }

                    // Toggle read action and update button text
                    nextReadAction = ReadAction.RESET;
                    mBinding.readToggleButton.setText(nextReadAction.toResourceString(getResources()));

                } else {
                    Toast.makeText(this, R.string.toast_scanner_not_connected, Toast.LENGTH_SHORT).show();
                }
                break;

            case RESET:
                // Clear scanned instance list
                mScannedRfidList.clear();
                mTagInstanceAdapter.clearInstanceList();
                mBinding.scannedItemsCount.setText(String.valueOf(mTagInstanceAdapter.getItemCount()));

                mBinding.locationSpinner.setEnabled(true);
                mBinding.findMissingButton.setEnabled(false);
                mBinding.findMissingButton.setBackgroundColor(getColor(R.color.button_disabled));

                // Toggle read action and update button text
                nextReadAction = ReadAction.START;
                mBinding.readToggleButton.setText(nextReadAction.toResourceString(getResources()));


                if (mRfidScanner != null) {
                    try {
                        mRfidScanner.close();
                    } catch (RFIDException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    // Todo: Show missing instances on a dialog
    private void showMissingItems() {
        // Compare scanned instances with location instance list and display missing instances in a dialog
//        Toast.makeText(this, "Number of missing items: " + mMissingInstances.size(), Toast.LENGTH_SHORT).show();
        List<String> missingItems = new ArrayList<>();
        for (InstanceEntity instance : mMissingInstances) {
            for (ItemEntity item : mItemList) {
                if (instance.getItemId() == item.getId()) {
                    String itemName = item.getName();
                    for (BrandEntity brand : mBrandList) {
                        if (item.getBrandId() == brand.getId()) {
                            String itemBrand = brand.getName();
                            missingItems.add(itemBrand + ' ' + itemName);
                        }
                    }
                }
            }
        }
        MissingItemsDialogFragment missingItemsDialog = new MissingItemsDialogFragment(missingItems);
        missingItemsDialog.show(getSupportFragmentManager(), String.valueOf(R.string.title_missing_items));

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
                InstanceEntity scannedInstance = mInstanceListViewModel.loadInstanceByRfid(rfidUii);
                if (scannedInstance != null) {
                    // Remove scanned instance from missing instance list
                    for (Iterator<InstanceEntity> it = mMissingInstances.iterator(); it.hasNext();) {
                        InstanceEntity instance = it.next();
                        if (scannedInstance.getId() == instance.getId()) {
                            it.remove();
                        }
                    }

                    // Update instance location and check-in time
                    scannedInstance.setCheckedInAt(new Date(System.currentTimeMillis()));
                    scannedInstance.setLocationId(mCurrentLocation.getId());
                    mInstanceListViewModel.updateInstance(scannedInstance);

                    // Show instance information in recyclerview
                    runOnUiThread(new StocktakingActivity.uiUpdaterInstanceData(scannedInstance));
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


    private enum ReadAction {
        START, RESET;

        String toResourceString(Resources resources) {
            switch (this) {
                case START:
                    return resources.getText(R.string.button_start).toString();
                case RESET:
                    return resources.getText(R.string.button_reset).toString();
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private final InstanceClickCallback mInstanceClickCallback = instance -> {
        // no-op
    };
}