package com.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class DevicePref {
    private static final String SHARED_PREF_NAME = "TokenPref";
    private static final String TAG_DEVICE_TOKEN = "device_token";
    public static final String SCREEN_WIDTH = "width";
    public static final String SCREEN_HEIGHT = "height";

    private static DevicePref mInstance;
    private static Context mCtx;

    private DevicePref(Context context) {
        mCtx = context;
    }

    public static synchronized DevicePref getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DevicePref(context);
        }
        return mInstance;
    }

    //this method will save the device token to shared preferences
    public boolean saveDeviceToken(String token) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TAG_DEVICE_TOKEN, token);
        editor.apply();
        return true;
    }

    //this method will fetch the device token from shared preferences
    public String getDeviceToken() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(TAG_DEVICE_TOKEN, null);
    }

    public static void putInt(String key, int val) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, val).commit();
        editor.apply();
    }

    public int getInt(String key, int defaultValue) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defaultValue);
    }
}
