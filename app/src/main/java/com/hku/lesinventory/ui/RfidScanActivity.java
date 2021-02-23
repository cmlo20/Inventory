package com.hku.lesinventory.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.RfidScanActivityBinding;

import java.util.List;

public class RfidScanActivity extends AppCompatActivity
        implements ScannerAcceptStatusListener, RFIDDataDelegate {

    private RfidScanActivityBinding mBinding;

    private CommScanner mCommScanner = null;
    private RFIDScanner mRfidScanner = null;
    private RFIDScannerSettings mOriginalScannerSettings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = RfidScanActivityBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBinding.rfidScanButton.setOnClickListener(view -> {
            if (mRfidScanner != null) {
                try {
                    mRfidScanner.openInventory();
                    Toast.makeText(this, R.string.toast_scan_item_instruction, Toast.LENGTH_SHORT).show();
                } catch (RFIDException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, R.string.toast_scanner_not_connected, Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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

        runOnUiThread(new uiUpdaterConnected(mCommScanner));

        // Get RFID scanner object
        mRfidScanner = mCommScanner.getRFIDScanner();
        mRfidScanner.setDataDelegate(this);

        // Configure SP1 settings
        try {
            mOriginalScannerSettings = mRfidScanner.getSettings();

            RFIDScannerSettings myScannerSettings = mRfidScanner.getSettings();
            myScannerSettings.scan.triggerMode = RFIDScannerSettings.Scan.TriggerMode.AUTO_OFF;
            myScannerSettings.scan.powerLevelRead = 20;
            mRfidScanner.setSettings(myScannerSettings);
        } catch (RFIDException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRFIDDataReceived(CommScanner commScanner, RFIDDataReceivedEvent rfidDataReceivedEvent) {
        List<RFIDData> rfidDataList = rfidDataReceivedEvent.getRFIDData();
        if (rfidDataList.size() != 0) {
            RFIDData rfidData = rfidDataList.get(0);
            runOnUiThread(new uiUpdaterInstanceData(rfidData));
        }
        try {
            commScanner.buzzer(CommConst.CommBuzzerType.B1);
            mRfidScanner.close();
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
            mBinding.rfidEdittext.setHint(getString(R.string.rfid_reader_connected,
                    commScanner.getBTLocalName()));
        }
    }

    private class uiUpdaterInstanceData implements Runnable {
        private RFIDData rfidData;

        uiUpdaterInstanceData(RFIDData rfidData) { this.rfidData = rfidData; }

        @Override
        public void run() {
            String rfidUii = byteToString(rfidData.getUII());
            mBinding.rfidEdittext.setText(rfidUii);

            InstanceFragment instanceFragment = InstanceFragment.forInstance(rfidUii);
            getSupportFragmentManager()
                    .beginTransaction()
//                    .addToBackStack("instance")
                    .replace(R.id.fragment_container, instanceFragment, null)
                    .commit();
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
}