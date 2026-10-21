package com.app.helper;

import android.view.View;
import android.view.ViewConfiguration;

public abstract class OnDoubleTapListener implements View.OnClickListener {

    private static final long DEFAULT_DOUBLE_TAP_TIME_DELTA_MILLIS = 300;

    private long doubleTapTimeout = DEFAULT_DOUBLE_TAP_TIME_DELTA_MILLIS;
    private long lastClickTime = 0;

    public OnDoubleTapListener() {
        doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout();
    }

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < doubleTapTimeout) {
            onDoubleTap(v);
            lastClickTime = 0;
        } else {
            onSingleClick(v);
        }
        lastClickTime = clickTime;
    }

    public abstract void onSingleClick(View v);
    public abstract void onDoubleTap(View v);
}