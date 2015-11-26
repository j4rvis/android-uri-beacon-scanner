package de.j4rvis.bleadmin;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import de.hadizadeh.positioning.controller.PositionListener;
import de.hadizadeh.positioning.controller.PositionManager;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.exceptions.PositioningException;
import de.hadizadeh.positioning.exceptions.PositioningPersistenceException;
import de.hadizadeh.positioning.model.PositionInformation;


public class PositioningActivity extends BluetoothActivity implements PositionListener{

    private PositionManager positionManager;
    private TextView currentPositionTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);
    }

    @Override
    protected void onBluetoothActivated() {
        super.onBluetoothActivated();
        initializePositioning();
    }
    @Override
    public void positionReceived(PositionInformation positionInformation) { }

    @Override
    public void positionReceived(final List<PositionInformation> positionInformation) {
        currentPositionTv.post(new Runnable() {
            public void run() {
            String positioningText = "";
            for (int i = 0; i < positionInformation.size(); i++) {
                positioningText += i + ".: "
                        + positionInformation.get(i).getName() + " "
                        +positionInformation.get(i).getSignalInformation()
                        + System.getProperty("line.separator");
            }
            Log.d("Position received", "" + positioningText);
            currentPositionTv.setText(positioningText);
            }
        });
    }

    private void initializePositioning() {
        File file = new File(Environment.getExternalStorageDirectory(), "positioningPersistence.xml");
        try {
            positionManager = new PositionManager(file);
            Log.d("positionManager", "initialized");
        } catch (PositioningPersistenceException e) {
            e.printStackTrace();
        }

        Technology bleTechnology = BluetoothLeTechnology.getInstance();

        try {
            positionManager.addTechnology(bleTechnology);
        } catch (PositioningException e) {
            e.printStackTrace();
        }

        bleTechnology.startScanning();
        positionManager.registerPositionListener(this);

        final EditText mapName = (EditText) findViewById(R.id.mapname_et);
        Button mapBtn = (Button) findViewById(R.id.map_btn);
        Button startBtn = (Button) findViewById(R.id.start_btn);
        Button stopBtn = (Button) findViewById(R.id.stop_btn);
        currentPositionTv = (TextView) findViewById(R.id.current_position_tv);

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positionManager.map(mapName.getText().toString());
                Log.d("mapBnt", "" +mapName.getText().toString());
            }
        });
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positionManager.startPositioning(500);
                Log.d("StartBtn", "Start");
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positionManager.stopPositioning();
            }
        });
    }
}
