

package com.mss.filetransferwithsocket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;

import com.mss.filetransferwithsocket.utils.Session;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;
    private DeviceDetailActivity mDetailActivity;
    PeerListListener mPeerListListener;
    ConnectionInfoListener connectionInfoListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       MainActivity activity, PeerListListener peerListListener) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
        this.mPeerListListener = peerListListener;
    }

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       DeviceDetailActivity activity, ConnectionInfoListener peerListListener) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mDetailActivity = activity;
        this.connectionInfoListener = peerListListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                if (mDetailActivity != null) {
                    mDetailActivity.setIsWifiP2pEnabled(true);

                } else {
                    mActivity.setIsWifiP2pEnabled(true);

                }
            } else {

                if (mDetailActivity != null) {
                    mDetailActivity.setIsWifiP2pEnabled(true);

                } else {
                    mActivity.setIsWifiP2pEnabled(true);

                }
                //  mActivity.resetData();

            }
            Log.d(MainActivity.TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p mManager. This is an
            // asynchronous call and the calling mActivity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, mPeerListListener);
            }


            Log.d(MainActivity.TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                mManager.requestConnectionInfo(mChannel, connectionInfoListener);


            } else {
                Session.getDeviceActionListenerDisconnect();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Session.getUpdateSpeed(device);

        }
    }
}
