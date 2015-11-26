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
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    private LinkedList<UriBeacon> btLeDevices;
    private boolean isScanning = false;

    Comparator<UriBeacon> beaconComparator = new Comparator<UriBeacon>() {
        public int compare(UriBeacon obj1,UriBeacon obj2) {
            return obj1.compareTo(obj2);
        }
    };

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            UriBeacon beacon = new UriBeacon(result);

            if (!beacon.isUriBeacon()){ return; }
            if(btLeDevices.isEmpty()){
                btLeDevices.add(beacon);
            } else if(!btLeDevices.contains(beacon)){
                btLeDevices.add(beacon);
            } else {
                int position = btLeDevices.indexOf(beacon);
                btLeDevices.get(position).addRssiValue(result.getRssi());
//                btLeDevices.set(position, beacon);
            }
            Collections.sort(btLeDevices,beaconComparator);
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

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public LinkedList<UriBeacon> getBtLeDevices() {
        return btLeDevices;
    }

    public UriBeacon getFirstBtLeDevice() {
        return btLeDevices.getLast();
    }

    public int getDeviceCount() {
        return btLeDevices.size();
    }

    @Override
    public Map<String, SignalInformation> getSignalData() {
        if (btLeDevices.size() > 0) {
            UriBeacon bestDevice = Collections.max(btLeDevices);
//            Log.d(TAG,bestDevice.toString());
            signalData.clear();
            signalData.put(bestDevice.getAddress(), new SignalInformation(1.0));
            return signalData;
        }
        return null;
    }

    private void initTechnology(){
        Log.d(TAG, "INIT");
//        providesExactlyPosition();
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
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            scanner.startScan(filters, settings, leScanCallback);
            isScanning = true;
        }
    }

    @Override
    public void stopScanning() {
        if(isScanning){
            super.stopScanning();
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            scanner.stopScan(leScanCallback);
            isScanning = false;
        }
    }
}