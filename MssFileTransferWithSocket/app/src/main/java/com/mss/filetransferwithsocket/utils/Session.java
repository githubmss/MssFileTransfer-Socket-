package com.mss.filetransferwithsocket.utils;


import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

import com.mss.filetransferwithsocket.interfaces.DeviceActionListener;
import com.mss.filetransferwithsocket.interfaces.OnConnecte;
import com.mss.filetransferwithsocket.interfaces.OnDeviceStatusChange;
import com.mss.filetransferwithsocket.interfaces.OnDisConnect;
import com.mss.filetransferwithsocket.interfaces.OnfriendDivceStatus;

public class Session {
    private static OnDeviceStatusChange sUpdateCheckBox;
    private static DeviceActionListener sDeviceActionListener;
    private static OnfriendDivceStatus onfriendDivceStatus;
    private static OnConnecte onConnecte;
    private static OnDisConnect onDisConnect;


    public static void getOnfriendDivceStatus(WifiP2pDevice device ) {
        if (onfriendDivceStatus != null) {
            onfriendDivceStatus.onFriendDeviceStatusUpdate(device);
        }
    }

    public static void setOnfriendDivceStatus(OnfriendDivceStatus listner) {
        if (listner != null) {
            onfriendDivceStatus = listner;
        }
    }


    public static void getOnConnecte(WifiP2pInfo config ){

        if (onConnecte != null) {
            onConnecte.onConnectionUpdate(config);
        }
    }

    public static void setOnConnecte(OnConnecte listner) {
        if (listner != null) {
            onConnecte = listner;
        }
    }

    public static void getUpdateSpeed(WifiP2pDevice device) {

        if (sUpdateCheckBox != null) {
            sUpdateCheckBox.onDevicceStatusUpdate(device);
        }
    }
    public static void setsUpdateSpeed(OnDeviceStatusChange listner) {
        if (listner != null) {
            sUpdateCheckBox = listner;
        }

    }


    public static void getDeviceActionListenerConnect( WifiP2pConfig config) {
        if (sDeviceActionListener != null) {
            sDeviceActionListener.connect(config);
        }
    }
    public static void setDeviceActionListenerConnect(DeviceActionListener listner) {
        if (listner != null) {
            sDeviceActionListener = listner;
        }

    }

    public static void getDeviceActionListenerDisconnect( ) {
        if (onDisConnect != null) {
            onDisConnect.onDisconnectStatus();
        }
    }
    public static void setDeviceActionListenerDisconnect(OnDisConnect listner) {
        if (listner != null) {
            onDisConnect = listner;
        }

    }

}
