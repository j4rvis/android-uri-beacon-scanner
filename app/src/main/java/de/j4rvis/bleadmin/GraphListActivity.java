package de.j4rvis.bleadmin;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.w3c.dom.ProcessingInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.j4rvis.bleadmin.R;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class GraphListActivity extends Activity {

    private static final String TAG = GraphListActivity.class.getName();
    private List<UriBeacon> beacons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_list);
//        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        beacons = intent.getParcelableArrayListExtra("Beacons");

        LineChart chart = (LineChart) findViewById(R.id.chart);
        YAxis left = chart.getAxisLeft();
        YAxis right = chart.getAxisRight();
        left.setAxisMinValue(-100.0F);
        right.setAxisMinValue(-100.0F);

        Comparator<Map.Entry<Long,Integer>> comparator = new Comparator<Map.Entry<Long, Integer>>() {
            @Override
            public int compare(Map.Entry<Long, Integer> lhs, Map.Entry<Long, Integer> rhs) {
                return lhs.getKey().compareTo(rhs.getKey());
            }
        };

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        ArrayList<String> xValues = new ArrayList<String>();
        ArrayList<Long> xValuesLong = new ArrayList<Long>();
        List<Map.Entry<Long,Integer>> allEntries = new ArrayList<>();
        long max = -100;
        long start = System.currentTimeMillis();
        for(UriBeacon beacon : beacons){
            List<Map.Entry<Long,Integer>> entryList =
                    new ArrayList<>(beacon.getRssiMap().entrySet());
            allEntries.addAll(entryList);
        }
        Collections.sort(allEntries, comparator);
        start = allEntries.get(0).getKey();
        for(Map.Entry<Long,Integer> entry : allEntries){
            if(entry.getValue() > max) max = entry.getValue();
            xValuesLong.add(entry.getKey());
            xValues.add((entry.getKey() - start) + "ms");
        }

        int[] colors = {
                R.color.blue,
                R.color.green,
                R.color.purple,
                R.color.red,
                R.color.aqua,
                R.color.cyan,
                R.color.darkpurple,
                R.color.darkblue,
                R.color.darkgreen,
                R.color.darkred,
                R.color.yellow,
        };
        int colorIndex = 0;

        for(UriBeacon beacon : beacons){
            ArrayList<Entry> vals = new ArrayList<Entry>();
            List<Map.Entry<Long,Integer>> entryList =
                    new ArrayList<>(beacon.getRssiMap().entrySet());

            Collections.sort(entryList, comparator);
            for(Map.Entry<Long,Integer> entry : entryList){
//                Log.d("INDEX",""+xValuesLong.indexOf(entry.getKey()) + " " + (entry.getKey()-start));
                vals.add(new Entry((float) entry.getValue(), xValuesLong.indexOf(entry.getKey())));
            }
            LineDataSet setComp = new LineDataSet(vals, beacon.getUri());
            setComp.setColor(getResources().getColor(colors[colorIndex]));
            setComp.setCircleColor(getResources().getColor(colors[colorIndex++]));
            setComp.setAxisDependency(YAxis.AxisDependency.LEFT);
            dataSets.add(setComp);
        }

        int axisMaxValue = (int) ((max/10) + 1) * 10;
        Log.d(TAG, "" + (int) (max / 10));
        Log.d(TAG, axisMaxValue + " " + max);
        left.setStartAtZero(false);
        right.setStartAtZero(false);
        left.setAxisMaxValue((float) axisMaxValue);
        right.setAxisMaxValue((float) axisMaxValue);

        LineData data = new LineData(xValues, dataSets);

        chart.setData(data);
        chart.setVisibleXRangeMaximum(20.0f);
        if(allEntries.size() > 20)
            chart.moveViewToX(allEntries.size() - 20);
        chart.invalidate();
    }

}
