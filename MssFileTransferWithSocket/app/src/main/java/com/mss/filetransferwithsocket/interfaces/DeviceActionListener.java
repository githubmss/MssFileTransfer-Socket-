package com.mss.filetransferwithsocket.interfaces;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;

public interface DeviceActionListener {
    void cancelDisconnect() ;
    void showDetails(WifiP2pDevice device) ;
    void connect(WifiP2pConfig config);
    void disconnect();
}
