package com.app.addon.livestream.socket;

import android.content.Context;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.app.utils.Constants;
import com.app.utils.Logging;

import java.net.URISyntaxException;

public class SocketIO {
    private static final String TAG = SocketIO.class.getSimpleName();
    private static Socket mSocket;
    private Context mContext;
    private SocketEvents events;
    private SocketIO socketIO;

    public SocketIO(Context mContext) {
        this.mContext = mContext;
        if (mSocket == null) {
            try {
                mSocket = IO.socket(Constants.SOCKET_IO_URL);
            } catch (URISyntaxException e) {
                Logging.e(TAG, "SocketIO: "+e.getMessage());
            }
        }
    }

    public Socket getInstance() {
        return mSocket;
    }

    public void setSocketEvents(SocketEvents events) {
        this.events = events;
    }

    public interface SocketEvents {

    }

}
