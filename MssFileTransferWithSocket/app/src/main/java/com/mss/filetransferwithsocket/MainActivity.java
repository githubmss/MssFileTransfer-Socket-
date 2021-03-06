package com.mss.filetransferwithsocket;

import android.*;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mss.filetransferwithsocket.adapter.WiFiPeerListAdapter;
import com.mss.filetransferwithsocket.interfaces.OnDeviceStatusChange;
import com.mss.filetransferwithsocket.interfaces.OnfriendDivceStatus;
import com.mss.filetransferwithsocket.services.GPSTracker;
import com.mss.filetransferwithsocket.utils.AppController;
import com.mss.filetransferwithsocket.utils.AppPreferences;
import com.mss.filetransferwithsocket.utils.Session;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ChannelListener, PeerListListener, View.OnClickListener, OnDeviceStatusChange, AdapterView.OnItemClickListener, OnfriendDivceStatus, ConnectionInfoListener {

    public static final String TAG = "wifidirectdemo";
    private WifiP2pManager mManager;
    private boolean mIsWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private List<WifiP2pDevice> mPeers = new ArrayList<WifiP2pDevice>();
    private final IntentFilter intentFilter = new IntentFilter();
    private Channel mChannel;
    private BroadcastReceiver mReceiver = null;
    ProgressDialog progressDialog = null;
    PeerListListener peerListListener;
    private TextView txtDeviceName, txtDeviceStatus, txtHeader;
    private ListView lvDeviceList;
    ImageView imgReload, menuItem;
    LinearLayout llReload;
    Toolbar toolbar;
    private WiFiPeerListAdapter mAdapter;
    public static String AVAILABLE = "Available";
    public static String UNAVAILABLE = "Unavailable";
    public static String CONNECTED = "Connected";
    public static String INVITED = "Invited";
    public static String FAILED = "failed";
    public static String UNKNOWN = "Unknown";
    Context mContext;
    private GPSTracker gps;
    private AppPreferences mSession;

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.mIsWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mSession = new AppPreferences(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView imgIcon = (ImageView) findViewById(R.id.mss);
        imgIcon.setImageResource(R.drawable.app_icon);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            gps = new GPSTracker(mContext, MainActivity.this);
            // Check if GPS enabled
            if (gps.canGetLocation()) {
                double latitude = gps.getLatitude();
                mSession.setPrefrenceString(Constants.CURRENT_LATITUDE, "" + latitude);
                double longitude = gps.getLongitude();
                mSession.setPrefrenceString(Constants.CURRENT_LONGITUDE, "" + longitude);
            } else {
                gps.showSettingsAlert();
            }
        }
        initUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.contact_us) {
            Intent intent = new Intent(MainActivity.this, ContactUsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.about_us) {
            Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initUi() {
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        imgReload = (ImageView) findViewById(R.id.img_reload);
        imgReload.setImageResource(R.mipmap.reload);
        llReload = (LinearLayout) findViewById(R.id.ll_right);
        txtHeader = (TextView) findViewById(R.id.toolbar_title);
        llReload.setVisibility(View.VISIBLE);
        AppController.channel = mChannel;
        AppController.manager = mManager;
        AppController.intentFilter = intentFilter;
        AppController.screenData = false;
        Session.setsUpdateSpeed(this);
        Session.setOnfriendDivceStatus(this);
        txtDeviceName = (TextView) findViewById(R.id.txt_device_name);
        txtDeviceStatus = (TextView) findViewById(R.id.txt_device_available);
        lvDeviceList = (ListView) findViewById(R.id.lv_friend_device);
        mAdapter = new WiFiPeerListAdapter(MainActivity.this, R.layout.adapter_friend_device, mPeers);
        lvDeviceList.setAdapter(mAdapter);
        lvDeviceList.setOnItemClickListener(this);
        imgReload.setOnClickListener(this);
        txtHeader.setText(R.string.txt_header);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    gps = new GPSTracker(mContext, MainActivity.this);
                    if (gps.canGetLocation()) {
                        double latitude = gps.getLatitude();
                        mSession.setPrefrenceString(Constants.CURRENT_LATITUDE, "" + latitude);
                        double longitude = gps.getLongitude();
                        mSession.setPrefrenceString(Constants.CURRENT_LONGITUDE, "" + longitude);
                    } else {
                        gps.showSettingsAlert();
                    }
                } else {
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            AppController.screenData = false;
            mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, this);
            registerReceiver(mReceiver, intentFilter);
            mManager.discoverPeers(mChannel, new ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reasonCode) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.txt_discover_failed) + reasonCode,
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChannelDisconnected() {
        if (mManager != null && !retryChannel) {
            Toast.makeText(this, getResources().getString(R.string.chanel_lost), Toast.LENGTH_LONG).show();
            retryChannel = true;
            mManager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    getResources().getString(R.string.txt_lost_permanent),
                    Toast.LENGTH_LONG).show();
        }

    }


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

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        mPeers.clear();
        mPeers.addAll(peerList.getDeviceList());
        // ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (mPeers.size() == 0) {
            Log.d(MainActivity.TAG, getResources().getString(R.string.txt_no_device));
            return;
        }
        mAdapter.notifyDataSetChanged();

    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.img_reload:

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.txt_press_back), getResources().getString(R.string.txt_find_peers), true,
                        true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {

                            }
                        });

                mManager.discoverPeers(mChannel, new ActionListener() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.txt_discover_failed) + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });

                break;

        }
    }


    @Override
    public void onDevicceStatusUpdate(WifiP2pDevice device) {
        txtDeviceName.setText(device.deviceName);
        txtDeviceStatus.setText(getDeviceStatus(device.status));

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(MainActivity.this, DeviceDetailActivity.class);
        intent.putExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE, mPeers.get(i));
        startActivity(intent);
    }

    @Override
    public void onFriendDeviceStatusUpdate(WifiP2pDevice device) {
        if (!AppController.screenData) {
            AppController.screenData = true;
            Intent intent = new Intent(MainActivity.this, DeviceDetailActivity.class);
            intent.putExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE, device);
            startActivity(intent);
        }

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        if (DeviceDetailActivity.progressDialog != null && DeviceDetailActivity.progressDialog.isShowing())
            DeviceDetailActivity.progressDialog.dismiss();
        Session.getOnConnecte(wifiP2pInfo);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
