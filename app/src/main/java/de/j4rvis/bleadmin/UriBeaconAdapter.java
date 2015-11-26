package de.j4rvis.bleadmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by j4rvis on 26.06.15.
 */
public class UriBeaconAdapter extends BaseAdapter{

    private static final String TAG = UriBeaconAdapter.class.getName();

    private List<UriBeacon> beaconList = null;
    private Context context;

    public UriBeaconAdapter(Context context) {
        this.context = context;
        this.beaconList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return beaconList.size();
    }

    @Override
    public UriBeacon getItem(int position) {
        return beaconList.get(getCount() - position - 1);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    Comparator<UriBeacon> beaconComparator = new Comparator<UriBeacon>() {
        public int compare(UriBeacon obj1,UriBeacon obj2) {
            return obj1.compareTo(obj2);
        }
    };

    public void add(UriBeacon beacon){
        if (!beacon.isUriBeacon()){
            return;
        }

        if(beaconList.isEmpty()){
//            Log.d(TAG, "List is empty.");
            beaconList.add(beacon);
        } else if(!beaconList.contains(beacon)){
//            Log.d(TAG, "Beacon is not yet in the list.");
            beaconList.add(beacon);
        } else {
//            Log.d(TAG,"Beacon is already in the list."+" "+beacon.getUri());
            int position = beaconList.indexOf(beacon);
            beaconList.set(position, beacon);
        }
        notifyDataSetChanged();
        Collections.sort(beaconList,beaconComparator);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UriBeacon device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null)
            convertView = LayoutInflater.from(this.context)
                    .inflate(R.layout.list_item_uribeacon, parent, false);
        TextView deviceRSSI = (TextView) convertView.findViewById(R.id.deviceRSSI);
        TextView deviceUri = (TextView) convertView.findViewById(R.id.deviceUri);
        deviceRSSI.setText(""+device.getRssi());
        deviceUri.setText(""+device.getUri());
        return convertView;
    }
}
