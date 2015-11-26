package de.j4rvis.bleadmin;

/**
 * Created by j4rvis on 20.06.15.
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.util.Log;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.model.SignalInformation;

public class BluetoothLeTechnology extends Technology {

    private static final String TAG = BluetoothLeTechnology.class.getName();
    private static BluetoothLeTechnology mInstance = null;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private Map<String, SignalInformation> signalData;

    private UriBeaconAdapter adapter = null;
    private TextView textView = null;

    private int cacheSize = 10;
    private LinkedList<UriBeacon> btLeDevices;
    private boolean isScanning = false;

    private ScanCallback leScanCallback = null;

    private ScanCallback leScanCallbackAdapter = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            UriBeacon beacon = new UriBeacon(result);

            if(adapter!=null){
                Log.d(TAG, "Adapter");
                adapter.add(beacon);
            }
        }
    };
    private ScanCallback leScanCallbackMapping = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            UriBeacon beacon = new UriBeacon(result);
            if (textView!=null){
                if (beacon.getRssi() > -85) {
                    textView.setText(beacon.getUri());
//                    Log.d(TAG, beacon.toString());
                    btLeDevices.add(beacon);
                }
                while (btLeDevices.size() > cacheSize) {
                    btLeDevices.removeFirst();
                }
            }
        }
    };

    private BluetoothLeTechnology(String name) {
        super(name, null);
        initTechnology();
    }

    public static BluetoothLeTechnology getInstance(){
        Log.d(TAG, "getInstance");
        if(mInstance == null) {
            mInstance = new BluetoothLeTechnology("BLE");
        }
        return mInstance;
    }

    public void activateCallback(int value){
        switch (value){
            case 0:
                leScanCallback = leScanCallbackAdapter;
                break;
            case 1:
                leScanCallback = leScanCallbackMapping;
                break;
            default:
                leScanCallback = leScanCallbackAdapter;
                break;
        }
    }

    @Override
    public Map<String, SignalInformation> getSignalData() {
        if (btLeDevices.size() > 0) {
            UriBeacon bestDevice = Collections.max(btLeDevices);
            Log.d(TAG,bestDevice.toString());
            signalData.clear();
            signalData.put(bestDevice.getUri(), new SignalInformation(1.0));
            return signalData;
        }
        return null;
    }

    public void setAdapter(UriBeaconAdapter adapter){
        this.adapter = adapter;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    private void initTechnology(){
        Log.d(TAG, "INIT");
        providesExactlyPosition();
        btLeDevices = new LinkedList<>();
        signalData = new HashMap<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        scanner = bluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
        filters = new ArrayList<>();
    }

    @Override
    public void startScanning(){
        if(!isScanning){
            super.startScanning();
            Log.d(TAG, "start scanning...");
            scanner.startScan(filters, settings, leScanCallback);
            isScanning = true;
        }
    }

    @Override
    public void stopScanning() {
        if(isScanning){
            super.stopScanning();
            scanner.stopScan(leScanCallback);
            isScanning = false;
        }
    }
}