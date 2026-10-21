package com.app.helper;

import static android.content.Intent.EXTRA_CHOSEN_COMPONENT;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

public class ShareBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = ShareBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName clickedComponent = intent.getParcelableExtra(EXTRA_CHOSEN_COMPONENT);
//        Log.d(TAG, "onReceive: " + new Gson().toJson(clickedComponent));
//        Log.d(TAG, "onReceive: " + clickedComponent.getPackageName());
    }
}
