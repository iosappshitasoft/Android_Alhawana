package com.app.helper;

import android.content.Context;
import android.util.Log;

import com.app.alhawanajd.App;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class OnesignalNotificationService implements OneSignal.OSRemoteNotificationReceivedHandler {
    private static final String LOG_TAG_CALL = "Fundoo.OneSignal";
    private static final String TAG = OnesignalNotificationService.class.getSimpleName();
    Context mContext;
    String userId, userName, userImage, receiverId, chatType, utcTime, callType, roomId, platform, scope;


    public OnesignalNotificationService() {
    }

    @Override
    public void remoteNotificationReceived(Context context, OSNotificationReceivedEvent osNotificationReceivedEvent) {
        mContext = App.getInstance().getApplicationContext();
        OSNotification notification = osNotificationReceivedEvent.getNotification();
        Log.d(TAG, "remoteNotificationReceived: " + notification);
        JSONObject data = notification.getAdditionalData();
        Log.i(TAG, "remoteNotificationReceived: " + data);
        try {
            userId = data.getString("user_id");
            userImage = data.getString("user_image");
            userName = data.getString("user_name");
            receiverId = data.getString("receiver_id");
            chatType = data.getString("chat_type");
            utcTime = data.getString("created_at");
            callType = data.getString("call_type");
            roomId = data.getString("room_id");
            platform = data.getString("platform");
            scope = data.getString("scope");
        } catch (JSONException e) {
            Log.d(TAG, "remoteNotificationReceived: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
