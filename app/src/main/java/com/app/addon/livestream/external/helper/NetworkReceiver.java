package com.app.addon.livestream.external.helper;

import static com.app.addon.livestream.external.helper.NetworkUtil.NOT_CONNECT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;



/**
 * Created by hitasoft on 24/1/18.
 */

public class NetworkReceiver extends BroadcastReceiver {

    /*private static final String TAG = com.app.helper.NetworkReceiver.class.getSimpleName();*/

    private static final String TAG = "NetworkReceiver";


    public static ConnectivityReceiverListener connectivityReceiverListener;
    public static Boolean connect = true;

    public NetworkReceiver() {
        super();
    }

    public static boolean isConnected() {
        return connect;
    }

    @Override
    public void onReceive(Context context, Intent arg1) {

        String status =NetworkUtil.getConnectivityStatusString(context);
        if (status.equals(NOT_CONNECT)) {
            Log.e(TAG, "NOT_CONNECT");// your code when internet lost
            connect = false;
        } else
            connect = true;

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener.onNetworkConnectionChanged(connect);
        }
    }

    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}
