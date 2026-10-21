package com.app.addon.livestream.external.helper;

import android.content.Context;
import android.util.Log;

import com.app.addon.livestream.external.utils.StreamConstants;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

public class SocketIO {
    private static final String TAG = SocketIO.class.getSimpleName();
    public static Socket mSocket;
    private Context mContext;
    private SocketEvents events;
    private SocketIO socketIO;

    public SocketIO(Context mContext) {
        this.mContext = mContext;
        if (mSocket == null) {
            try {
                IO.Options opts = new IO.Options();
                opts.transports = new String[]{WebSocket.NAME};
                opts.reconnection = true;
                mSocket = IO.socket(StreamConstants.STREAM_SOCKET_IO_URL, opts);
                mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.v(TAG, "EVENT_CONNECT");
                    }
                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.e(TAG, "EVENT_DISCONNECT");
                    }
                });
                mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.e(TAG, "EVENT_CONNECT_ERROR: " + args[0]);
                    }
                });
                mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.e("EVENT_CONNECT_TIMEOUT", "EVENT_CONNECT_TIMEOUT");
                    }
                });
                mSocket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.e("EVENT_ERROR", "EVENT_ERROR");
                    }
                });
            } catch (URISyntaxException e) {
                Log.e(TAG, "SocketIO: " + e.getMessage());
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
