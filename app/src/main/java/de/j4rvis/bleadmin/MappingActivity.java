package de.j4rvis.bleadmin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.hadizadeh.positioning.content.Content;
import de.hadizadeh.positioning.content.ContentList;
import de.hadizadeh.positioning.content.ContentManager;
import de.hadizadeh.positioning.content.MappedContentManager;
import de.hadizadeh.positioning.content.exceptions.ContentPersistenceException;
import de.hadizadeh.positioning.controller.MappedPositionManager;
import de.hadizadeh.positioning.controller.PositionListener;
import de.hadizadeh.positioning.controller.PositionManager;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.exceptions.PositioningException;
import de.hadizadeh.positioning.exceptions.PositioningPersistenceException;
import de.hadizadeh.positioning.model.MappingPoint;
import de.hadizadeh.positioning.model.PositionInformation;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;


public class MappingActivity extends BluetoothActivity{

    private MappedPositionManager mappedPositionManager;
    private MappedContentManager mappedContentManager;
    private TextView currentPositionTv;
    private BluetoothLeTechnology technology = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        File filePosition = new File(Environment.getExternalStorageDirectory(), "positions.xml");
        File fileContent = new File(Environment.getExternalStorageDirectory(), "contents.xml");
        try {
            mappedPositionManager = new MappedPositionManager(filePosition);
            mappedContentManager = new MappedContentManager(fileContent);
        } catch (PositioningPersistenceException e) {
        } catch (ContentPersistenceException e) {
            e.printStackTrace();
        }

        final EditText mapName = (EditText) findViewById(R.id.map_name_et);
        final EditText mapCategory = (EditText) findViewById(R.id.map_category_et);
        final EditText mapPositionX = (EditText) findViewById(R.id.map_position_x_et);
        final EditText mapPositionY = (EditText) findViewById(R.id.map_position_y_et);
        Button mapBtn = (Button) findViewById(R.id.map_btn);
        Button clearFilesBtn = (Button) findViewById(R.id.clear_files_btn);
        Button removeLastBtn = (Button) findViewById(R.id.remove_last_btn);
        currentPositionTv = (TextView) findViewById(R.id.current_position_tv);

        clearFilesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mappedContentManager.removeAllContent();
                } catch (ContentPersistenceException e) {
                    e.printStackTrace();
                }
                mappedPositionManager.removeAllMappedPositions();
            }
        });

        removeLastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(mappedContentManager.getAllContents().size() > 0){
                        ContentList contents = mappedContentManager.getAllContents();
                        Content lastEntry =
                                (Content) contents.get(contents.size() - 1);
                        mappedContentManager.removeContent(Content.ContentType.DESCRIPTION,
                                lastEntry.getData());
                    }
                } catch (ContentPersistenceException e) {
                    e.printStackTrace();
                }
                if(mappedPositionManager.getMappedPositions().size() > 0){
                    List positions = mappedPositionManager.getMappedPositions();
                    String lastEntry = (String) positions.get(positions.size() - 1);
                    mappedPositionManager.removeMappedPosition(lastEntry);
                }

            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mapName.getText().toString();
                String category = mapCategory.getText().toString();
                String url = technology.getFirstBtLeDevice().getUri();
                String positionX = mapPositionX.getText().toString();
                String positionY = mapPositionY.getText().toString();

                if(name.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Name ist leer.", Toast.LENGTH_SHORT).show();
                    return;
                } else if(category.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Kategorie ist leer.", Toast.LENGTH_SHORT).show();
                    return;
                } else if(url.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "URL ist leer.", Toast.LENGTH_SHORT).show();
                    return;
                } else if(positionX.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "X-Koordinate ist leer.", Toast.LENGTH_SHORT).show();
                    return;
                } else if(positionY.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Y-Koordinate ist leer.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    MappingPoint mappingPoint = new MappingPoint(
                            Integer.parseInt(positionX),
                            Integer.parseInt(positionY),
                            0 );
                    List<MappingPoint> points = mappedPositionManager.getMappedPositionMappingPoints();
                    if(points.contains(mappingPoint)){
                        Toast.makeText(getApplicationContext(),
                                "Diese Koordinaten wurden bereits vergeben.", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        mappedPositionManager.map(mappingPoint);

                        Log.d("MappingPoint:", mappingPoint.toString());
                        Log.d("Category:", category);
                        Log.d("Name:", name);
                        Log.d("Url:", url);

                        mappedContentManager.addContent(Content.ContentType.URL, url);
                        mappedContentManager.addContent(Content.ContentType.TITLE, name);
                        mappedContentManager.addContent(Content.ContentType.DESCRIPTION, category);

                        mappedContentManager.addPosition(Content.ContentType.URL,
                                url, mappingPoint);
                        mappedContentManager.addPosition(Content.ContentType.DESCRIPTION,
                                category, mappingPoint);
                        mappedContentManager.addPosition(Content.ContentType.TITLE,
                                name, mappingPoint);

                        ContentList<Content> list = mappedContentManager.getContents(mappingPoint);
                        if(list.size() != 3){
                            mappedContentManager.removePosition(Content.ContentType.URL,
                                    url, mappingPoint);
                            mappedContentManager.removePosition(Content.ContentType.DESCRIPTION,
                                    category, mappingPoint);
                            mappedContentManager.removeContent(Content.ContentType.TITLE, name);
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();

                        }
                    }
                } catch (ContentPersistenceException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void scan() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(technology.getDeviceCount() == 0){
                    handler.postDelayed(this, 500);
                    return;
                }
                currentPositionTv.setText(technology.getFirstBtLeDevice().getUri());

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
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
            mappedPositionManager.addTechnology(technology);
        } catch (PositioningException e) {
            e.printStackTrace();
        }
        technology.startScanning();
        scan();
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
