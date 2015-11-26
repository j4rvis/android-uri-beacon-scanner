package de.j4rvis.bleadmin;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.model.SignalInformation;

/**
 * Created by j4rvis on 21.07.15.
 */


public class BluetoothActivity extends Activity{

    final String TAG = this.getClass().getName();
    private static final int REQUEST_ENABLE_BT = 0;

    @Override
    protected void onResume() {
        super.onResume();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        ensureBluetoothIsEnabled();
    }

    private void ensureBluetoothIsEnabled() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Log.d(TAG, "BT already active.");
            onBluetoothActivated();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_CANCELED && data != null){
            if (requestCode == REQUEST_ENABLE_BT){
                if (resultCode == RESULT_OK){
                    onBluetoothActivated();
                    Log.d(TAG, "BT activated.");
                }
            }
        }
    }

    protected void onBluetoothActivated(){
        Log.d(TAG, "Call onBluetoothActivated.");
    }
}
