package de.j4rvis.bleadmin;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;


public class MainActivity extends BluetoothActivity{

    private boolean isScanning;
    private ListView deviceList;
    private UriBeaconAdapter adapter;
    private Context context = this;
    private TextView scanStatus;
    private BluetoothLeTechnology technology = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        adapter = new UriBeaconAdapter(context);
        deviceList = (ListView) findViewById(R.id.listView);
        deviceList.setAdapter(adapter);
        Button showGraphs = (Button) findViewById(R.id.button);

        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UriBeacon beacon = adapter.getItem(position);
                String deviceUri = beacon.getUri();
                Log.d(TAG, "URI: "+deviceUri);
                Intent intent = new Intent(context, GraphActivity.class);
                intent.putExtra("Beacon", beacon);
                startActivity(intent);
            }
        });

        showGraphs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GraphListActivity.class);
                ArrayList beacons = new ArrayList<UriBeacon>();
                beacons.addAll(technology.getBtLeDevices());
                intent.putParcelableArrayListExtra("Beacons", beacons);
                startActivity(intent);
            }
        });

    }

    public void scan(){
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(isScanning){
                    adapter.setList(technology.getBtLeDevices());
                    handler.postDelayed(this, 1000);
                } else
                    return;
            }
        };
        handler.post(runnable);
    }

    public void startScanning(){
        Log.d(TAG, "Start Scanning...");
        isScanning = true;
        technology = BluetoothLeTechnology.getInstance();
        technology.startScanning();
        scan();
    }

    private void stopScanning(){
        Log.d(TAG, "Stop Scanning...");
        isScanning = false;
        technology = BluetoothLeTechnology.getInstance();
        technology.stopScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScanning();
    }

    @Override
    protected void onBluetoothActivated() {
        super.onBluetoothActivated();
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
