package de.j4rvis.bleadmin;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends BluetoothActivity{

    private boolean isScanning;
    private ListView deviceList;
    private UriBeaconAdapter adapter;
    private Context context = this;
    private TextView scanStatus;
    private Context that = this;
    private BluetoothLeTechnology technology = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new UriBeaconAdapter(context);
        deviceList = (ListView) findViewById(R.id.listView);
        deviceList.setAdapter(adapter);
        final Button startScanning = (Button) findViewById(R.id.button_start);
        Button stopScanning = (Button) findViewById(R.id.button_stop);
        this.scanStatus = (TextView) findViewById(R.id.scan_status);
        Log.d(TAG,"INIT");

        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UriBeacon beacon = adapter.getItem(position);
                String deviceUri = beacon.getUri();
                Log.d(TAG, "URI: "+deviceUri);
                Intent intent = new Intent(that, WebViewActivity.class);
                intent.putExtra("Uri", deviceUri);
                startActivity(intent);
            }
        });

        startScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning)
                    startScanning();
            }
        });

        stopScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScanning();
            }
        });
    }

    public void startScanning(){
        Log.d(TAG, "Start Scanning...");
        isScanning = true;
        scanStatus.setText("Scanning");
        technology.startScanning();
    }

    private void stopScanning(){
        Log.d(TAG, "Stop Scanning...");
        isScanning = false;
        scanStatus.setText("Stopped");
        technology.stopScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        stopScanning();
    }

    @Override
    protected void onBluetoothActivated() {
        super.onBluetoothActivated();
        technology = BluetoothLeTechnology.getInstance();
        technology.setAdapter(adapter);
        technology.activateCallback(0);
        startScanning();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_show_mapping) {
            Intent intent = new Intent(this, MappingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
