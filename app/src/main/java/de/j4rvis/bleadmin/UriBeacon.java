package de.j4rvis.bleadmin;

/**
 * Created by j4rvis on 20.06.15.
 */
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.URLUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UriBeacon implements Comparable<UriBeacon>, Parcelable {

    private final static String TAG = UriBeacon.class.getName();

    private String address;
    private String companyId;
    private String uri;
    Map<Long, Integer> rssiMap;

    private static final int DATA_TYPE_SERVICE_DATA = 0x16;
    private static final byte[] URI_SERVICE_16_BIT_UUID_BYTES = {(byte) 0xd8, (byte) 0xfe};
    public static final String NO_URI = "";

    /**
     * URI Scheme maps a byte code into the scheme and an optional scheme specific prefix.
     */
    private static final SparseArray<String> URI_SCHEMES = new SparseArray<String>() {{
        put((byte) 0, "http://www.");
        put((byte) 1, "https://www.");
        put((byte) 2, "http://");
        put((byte) 3, "https://");
        put((byte) 4, "urn:uuid:");    // RFC 2141 and RFC 4122};
    }};
    /**
     * Expansion strings for "http" and "https" schemes. These contain strings appearing anywhere in a
     * URL. Restricted to Generic TLDs. <p/> Note: this is a scheme specific encoding.
     */
    private static final SparseArray<String> URL_CODES = new SparseArray<String>() {{
        put((byte) 0, ".com/");
        put((byte) 1, ".org/");
        put((byte) 2, ".edu/");
        put((byte) 3, ".net/");
        put((byte) 4, ".info/");
        put((byte) 5, ".biz/");
        put((byte) 6, ".gov/");
        put((byte) 7, ".com");
        put((byte) 8, ".org");
        put((byte) 9, ".edu");
        put((byte) 10, ".net");
        put((byte) 11, ".info");
        put((byte) 12, ".biz");
        put((byte) 13, ".gov");
    }};

    @Override
    public String toString() {
        return "UriBeaconDevice{" +
                "address='" + address + '\'' +
                ", companyId='" + companyId + '\'' +
                ", uri='" + uri + '\'' +
                ", rssi=" + getRssi() +
                '}';
    }

    public Boolean isUriBeacon(){
        return getUri() != null ;
    }

    public String getAddress() {
        return address;
    }

    public String getUri() {
        return uri;
    }

    public Map<Long, Integer> getRssiMap() {
        return rssiMap;
    }

    public int getRssi() {
        List<Map.Entry<Long,Integer>> entryList =
                new ArrayList<>(rssiMap.entrySet());
        return entryList.get(entryList.size()-1).getValue();
    }

    public void addRssiValue(int rssi) {
        rssiMap.put(System.currentTimeMillis(), rssi);
        Log.d(TAG, "Map size: " + rssiMap.size());
    }

    public UriBeacon(Parcel parcel){
        this.address = parcel.readString();
        this.companyId = parcel.readString();
        this.uri = parcel.readString();
        this.rssiMap = parcel.readHashMap(ClassLoader.getSystemClassLoader());
//        this.txPower = parcel.readInt();
    }

    public UriBeacon(ScanResult result){
        ScanRecord scanRecord = result.getScanRecord();
        byte[] scanRecordBytes = scanRecord.getBytes();
        rssiMap = new TreeMap<>();
        rssiMap.put(System.currentTimeMillis(), result.getRssi());
        address = result.getDevice().getAddress();
        companyId = String.format("%02x", scanRecordBytes[5])
                + String.format("%02x", scanRecordBytes[6]);
        byte[] serviceData = parseServiceDataFromBytes(result.getScanRecord().getBytes());
        // Minimum UriBeacon consists of flags, TxPower
        if (serviceData != null && serviceData.length >= 2) {
            int currentPos = 0;
            byte flags = serviceData[currentPos++];
            byte txPowerLevel = serviceData[currentPos++];
            uri = decodeUri(serviceData, currentPos);
        }
    }

    private static String decodeUri(byte[] serviceData, int offset) {
        if (serviceData.length == offset) {
            return NO_URI;
        }
        StringBuilder uriBuilder = new StringBuilder();
        if (offset < serviceData.length) {
            byte b = serviceData[offset++];
            String scheme = URI_SCHEMES.get(b);
            if (scheme != null) {
                uriBuilder.append(scheme);
                if (URLUtil.isNetworkUrl(scheme)) {
                    return decodeUrl(serviceData, offset, uriBuilder);
                }
            }
            Log.w(TAG, "decodeUri unknown Uri scheme code=" + b);
        }
        return null;
    }

    private static String decodeUrl(byte[] serviceData, int offset, StringBuilder urlBuilder) {
        while (offset < serviceData.length) {
            byte b = serviceData[offset++];
            String code = URL_CODES.get(b);
            if (code != null) {
                urlBuilder.append(code);
            } else {
                urlBuilder.append((char) b);
            }
        }
        return urlBuilder.toString();
    }

    private static byte[] parseServiceDataFromBytes(byte[] scanRecord) {
        int currentPos = 0;
        try {
            while (currentPos < scanRecord.length) {
                int fieldLength = scanRecord[currentPos++] & 0xff;
                if (fieldLength == 0) {
                    break;
                }
                int fieldType = scanRecord[currentPos] & 0xff;
                if (fieldType == DATA_TYPE_SERVICE_DATA) {
                    // The first two bytes of the service data are service data UUID.
                    if (scanRecord[currentPos + 1] == URI_SERVICE_16_BIT_UUID_BYTES[0]
                            && scanRecord[currentPos + 2] == URI_SERVICE_16_BIT_UUID_BYTES[1]) {
                        // jump to data
                        currentPos += 3;
                        // length includes the length of the field type and ID
                        byte[] bytes = new byte[fieldLength - 3];
                        System.arraycopy(scanRecord, currentPos, bytes, 0, fieldLength - 3);
                        return bytes;
                    }
                }
                // length includes the length of the field type
                currentPos += fieldLength;
            }
        } catch (Exception e) {
            Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord), e);
        }
        return null;
    }

    @Override
    public int compareTo(UriBeacon another) {
        if(getRssi() > another.getRssi()) {
            return 1;
        } else if(getRssi() < another.getRssi()) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UriBeacon)) return false;
        UriBeacon that = (UriBeacon) o;
        return !(address != null ? !address.equals(that.address) : that.address != null);
    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(companyId);
        dest.writeString(uri);
        dest.writeMap(rssiMap);
    }

    public static final Parcelable.Creator<UriBeacon> CREATOR =
        new Parcelable.Creator<UriBeacon>(){

            @Override
            public UriBeacon createFromParcel(Parcel source) {
                return new UriBeacon(source);
            }

            @Override
            public UriBeacon[] newArray(int size) {
                return new UriBeacon[size];
            }
        };
}