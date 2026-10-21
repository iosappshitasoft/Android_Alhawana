package com.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.concurrent.CountDownLatch;

public class BluetoothHelper {
    private BluetoothAdapter bluetoothAdapter;
    private Context context;

    public BluetoothHelper(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void isBluetoothConnected(final BluetoothConnectionCallback callback) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            callback.onResult(false);
            return;
        }

        final CountDownLatch latch = new CountDownLatch(3);
        final boolean[] isConnected = {false};

        checkProfileConnection(BluetoothProfile.HEADSET, isConnected, latch);
        checkProfileConnection(BluetoothProfile.A2DP, isConnected, latch);
        checkProfileConnection(BluetoothProfile.GATT, isConnected, latch);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();  // Wait for all profile checks to complete
                    callback.onResult(isConnected[0]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    callback.onResult(false);
                }
            }
        }).start();
    }

    private void checkProfileConnection(final int profile, final boolean[] isConnected, final CountDownLatch latch) {
        bluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (proxy != null) {
                    for (BluetoothDevice device : proxy.getConnectedDevices()) {
                        if (proxy.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED) {
                            isConnected[0] = true;
                            break;
                        }
                    }
                    bluetoothAdapter.closeProfileProxy(profile, proxy);
                }
                latch.countDown();
            }

            @Override
            public void onServiceDisconnected(int profile) {
                latch.countDown();
            }
        }, profile);
    }
}
