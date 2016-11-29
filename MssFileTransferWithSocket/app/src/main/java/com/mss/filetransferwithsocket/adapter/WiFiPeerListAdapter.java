package com.mss.filetransferwithsocket.adapter;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mss.filetransferwithsocket.MainActivity;
import com.mss.filetransferwithsocket.R;
import com.mss.filetransferwithsocket.utils.AppController;
import com.mss.filetransferwithsocket.utils.Session;

import java.util.List;

public class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

    private List<WifiP2pDevice> items;
    private Context mContext;

    public static String AVAILABLE = "Available";
    public static String UNAVAILABLE = "Unavailable";
    public static String CONNECTED = "Connected";
    public static String INVITED = "Invited";
    public static String FAILED = "failed";
    public static String UNKNOWN = "Unknown";


    public WiFiPeerListAdapter(Context context, int textViewResourceId,
                               List<WifiP2pDevice> objects) {
        super(context, textViewResourceId, objects);
        this.mContext = context;
        items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.adapter_friend_device, null);
        }
        WifiP2pDevice device = items.get(position);
        if (device != null) {
            TextView top = (TextView) v.findViewById(R.id.txt_device_friend_name);
            TextView bottom = (TextView) v.findViewById(R.id.txt_device_friend_available);
            ImageView imgStatus = (ImageView) v.findViewById(R.id.img_avail);
            top.setText(device.deviceName);
            bottom.setText(getDeviceStatus(device.status));

            if (getDeviceStatus(device.status).equals(CONNECTED)) {
                if (!AppController.screenData) {
                    Session.getOnfriendDivceStatus(device);
                }
            }
            if (getDeviceStatus(device.status).equals(AVAILABLE)) {
                imgStatus.setImageResource(R.mipmap.available);
            }
            if (getDeviceStatus(device.status).equals(UNAVAILABLE)) {
                imgStatus.setImageResource(R.mipmap.unavailable);
            }
            if (getDeviceStatus(device.status).equals(CONNECTED)) {
                imgStatus.setImageResource(R.mipmap.connect);
            }
            if (getDeviceStatus(device.status).equals(INVITED)) {
                imgStatus.setImageResource(R.mipmap.invite);
            }
            if (getDeviceStatus(device.status).equals(FAILED)) {
                imgStatus.setImageResource(R.mipmap.fail);
            }

        }
        return v;
    }


    // tell devices status
    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return AVAILABLE;
            case WifiP2pDevice.INVITED:
                return INVITED;
            case WifiP2pDevice.CONNECTED:
                return CONNECTED;
            case WifiP2pDevice.FAILED:
                return FAILED;
            case WifiP2pDevice.UNAVAILABLE:
                return UNAVAILABLE;
            default:
                return UNKNOWN;

        }
    }

}
