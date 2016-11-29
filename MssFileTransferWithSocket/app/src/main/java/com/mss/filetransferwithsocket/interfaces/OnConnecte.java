package com.mss.filetransferwithsocket.interfaces;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pInfo;

/**
 * Created by ravishankar on 23/11/16.
 */

public interface OnConnecte {
    void onConnectionUpdate(WifiP2pInfo config);
}
