package com.mss.filetransferwithsocket.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class AppPreferences {
    SharedPreferences pref;
    Editor editor;
    public static Context mContext;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "AndroidMSSNative_Prefrence";

    @SuppressLint("CommitPrefEdits")
    public AppPreferences(Context context) {
        this.mContext = context;
        pref = AppPreferences.mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public static void init(Context context) {
        AppPreferences.mContext = context;

    }

    public static String getLastModified() {
        return PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext).getString("lastModified", "");
    }

    public static void setLastModified(String timeStamp) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext).edit();
        editor.putString("lastModified", timeStamp);
        editor.commit();
    }

    public static void setPreferenceRelaod(String key, int value) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext);
        Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getPreferenceRelaod(String key, int defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext);
        return preferences.getInt(key, defaultValue);
    }

    public boolean getPrefrenceBoolean(String keyName) {
        return pref.getBoolean(keyName, false);
    }

    public void setPrefrenceBoolean(String keyName, Boolean booleanValue) {
        editor.putBoolean(keyName, booleanValue);
        editor.commit();
    }

    public String getPrefrenceString(String keyName) {
        return pref.getString(keyName, "");
    }

    public void setPrefrenceString(String keyName, String stringValue) {
        editor.putString(keyName, stringValue);
        editor.commit();
    }

    public int getPrefrenceInt(String keyName) {
        return pref.getInt(keyName, 0);
    }

    public void setPrefrenceInt(String keyName, int intValue) {
        editor.putInt(keyName, intValue);
        editor.commit();
    }

    public long getPrefrenceLong(String keyName) {
        return pref.getLong(keyName, 0);
    }

    public void setPrefrenceLong(String keyName, Long intValue) {
        editor.putLong(keyName, intValue);
        editor.commit();
    }

    public void setPreferenceNotificationCount(String key, int value) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext);
        Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int getPreferenceNotificationCount(String key, int defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppPreferences.mContext);
        return preferences.getInt(key, defaultValue);
    }
}
