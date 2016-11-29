package com.mss.filetransferwithsocket.utils;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.net.wifi.p2p.WifiP2pManager.Channel;

public class AppController extends MultiDexApplication {


	public  static Activity activity;

	@Override
	public void onCreate() {
		super.onCreate();
		MultiDex.install(this);
	}
	public static Context mainActivity;
	public static Context deviceDetailActivity;
	public  static Channel channel;
	public  static WifiP2pManager manager;
	public  static IntentFilter intentFilter;
	public static boolean screenData=false;
}
