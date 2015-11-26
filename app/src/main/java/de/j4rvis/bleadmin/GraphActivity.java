package de.j4rvis.bleadmin;

import android.app.Activity;
import android.content.Intent;
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
import com.github.mikephil.charting.utils.FillFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.j4rvis.bleadmin.R;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class GraphActivity extends Activity {

    private static final String TAG = GraphActivity.class.getName();
    private UriBeacon beacon;

    ArrayList<Entry> vals = new ArrayList<Entry>();
    Comparator<Map.Entry<Long,Integer>> comparator = new Comparator<Map.Entry<Long, Integer>>() {
        @Override
        public int compare(Map.Entry<Long, Integer> lhs, Map.Entry<Long, Integer> rhs) {
            return lhs.getKey().compareTo(rhs.getKey());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        beacon = intent.getParcelableExtra("Beacon");

        Log.d(TAG, beacon.toString());
        LineChart chart = (LineChart) findViewById(R.id.chart);
        YAxis left = chart.getAxisLeft();
        YAxis right = chart.getAxisRight();
//        left.setStartAtZero(false);
//        right.setStartAtZero(false);
//        left.setAxisMaxValue(-20.0F);
//        right.setAxisMaxValue(-20.0F);
        left.setAxisMinValue(-100.0F);
        right.setAxisMinValue(-100.0F);
        TextView address = (TextView) findViewById(R.id.address);
        TextView url = (TextView) findViewById(R.id.url);
        Button button = (Button) findViewById(R.id.button);
        address.setText(beacon.getAddress());
        url.setText(beacon.getUri());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceUri = beacon.getUri();
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra("Uri", deviceUri);
                startActivity(intent);
            }
        });

        List<Map.Entry<Long,Integer>> entryList =
                new ArrayList<>(beacon.getRssiMap().entrySet());
//        Log.d(TAG, "unsorted: " + entryList.toString());
        Collections.sort(entryList, comparator);
//        Log.d(TAG, "sorted: " + entryList.toString());
        long max = -100;
        long start = entryList.get(0).getKey();
        int chartPosition = 0;
        ArrayList<String> xVals = new ArrayList<String>();

//        Collections.sort(entryList, comparator);

//        Collections.reverse(entryList);
        for(Map.Entry<Long,Integer> entry : entryList){
            if(entry.getValue() > max) max = entry.getValue();
            vals.add(new Entry((float) entry.getValue(), chartPosition));
            xVals.add((entry.getKey() - start) + "ms");
            chartPosition++;
        }

        LineDataSet setComp1 = new LineDataSet(vals, beacon.getUri());
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(setComp1);

        int axisMaxValue = (int) ((max/10) + 1) * 10;
        left.setStartAtZero(false);
        right.setStartAtZero(false);
        left.setAxisMaxValue((float) axisMaxValue);
        right.setAxisMaxValue((float) axisMaxValue);

        LineData data = new LineData(xVals, dataSets);
        chart.setData(data);
        chart.setVisibleXRangeMaximum(20.0f);
        if(entryList.size() > 20)
            chart.moveViewToX(entryList.size() - 20);
        chart.invalidate();



    }
}
