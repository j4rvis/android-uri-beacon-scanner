package de.j4rvis.bleadmin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import de.hadizadeh.positioning.content.Content;
import de.hadizadeh.positioning.content.ContentList;
import de.hadizadeh.positioning.content.ContentManager;
import de.hadizadeh.positioning.content.exceptions.ContentPersistenceException;
import de.hadizadeh.positioning.controller.PositionListener;
import de.hadizadeh.positioning.controller.PositionManager;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.exceptions.PositioningException;
import de.hadizadeh.positioning.exceptions.PositioningPersistenceException;
import de.hadizadeh.positioning.model.PositionInformation;


public class MappingActivity extends BluetoothActivity{

    private PositionManager positionManager;
    private TextView currentPositionTv;
    private BluetoothLeTechnology technology = null;
    private ContentManager contentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        File filePosition = new File(Environment.getExternalStorageDirectory(), "positions.xml");
        try {
            positionManager = new PositionManager(filePosition);
            Log.d("positionManager", "initialized");
        } catch (PositioningPersistenceException e) {
            e.printStackTrace();
        }
        File fileContent = new File(Environment.getExternalStorageDirectory(), "contents.xml");
        try {
            contentManager= new ContentManager(fileContent);
            Log.d("positionManager", "initialized");
        } catch (ContentPersistenceException e) {
            e.printStackTrace();
        }

        final EditText mapName = (EditText) findViewById(R.id.map_name_et);
        final EditText mapCategory = (EditText) findViewById(R.id.map_category_et);
        Button mapBtn = (Button) findViewById(R.id.map_btn);
        Button clearFilesBtn = (Button) findViewById(R.id.clear_files_btn);
        Button removeLastBtn = (Button) findViewById(R.id.remove_last_btn);
        currentPositionTv = (TextView) findViewById(R.id.current_position_tv);

        clearFilesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    contentManager.removeAllContent();
                } catch (ContentPersistenceException e) {
                    e.printStackTrace();
                }
                positionManager.removeAllMappedPositions();
            }
        });

        removeLastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(contentManager.getAllContents().size() > 0){
                        ContentList contents = contentManager.getAllContents();
                        Content lastEntry =
                                (Content) contents.get(contents.size() - 1);
                        contentManager.removeContent(Content.ContentType.DESCRIPTION,
                                lastEntry.getData());
                    }
                } catch (ContentPersistenceException e) {
                    e.printStackTrace();
                }
                if(positionManager.getMappedPositions().size() > 0){
                    List positions = positionManager.getMappedPositions();
                    String lastEntry = (String) positions.get(positions.size() - 1);
                    positionManager.removeMappedPosition(lastEntry);
                }

            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Category: " + mapCategory.getText().toString());
                Log.d(TAG, "Name: " + mapName.getText().toString());

                try {
                    contentManager.addContent(Content.ContentType.DESCRIPTION,
                            mapCategory.getText().toString());
                    positionManager.map(mapName.getText().toString());
                } catch (ContentPersistenceException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onBluetoothActivated() {
        super.onBluetoothActivated();
        initializeMapping();
    }

    @Override
    protected void onPause() {
        super.onPause();
        technology.stopScanning();
    }

    private void initializeMapping() {

        technology = BluetoothLeTechnology.getInstance();
        try {
            positionManager.addTechnology(technology);
        } catch (PositioningException e) {
            e.printStackTrace();
        }
        technology.setTextView(currentPositionTv);
        technology.activateCallback(1);
        technology.startScanning();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mapping, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_position_xml) {
            Intent intent = new Intent(this, XmlActivity.class);
            intent.putExtra("xml", "positions.xml");
            startActivity(intent);
            return true;
        } else if (id == R.id.action_content_xml) {
            Intent intent = new Intent(this, XmlActivity.class);
            intent.putExtra("xml", "contents.xml");
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
