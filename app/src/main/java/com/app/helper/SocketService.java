package com.app.helper;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

public class SocketService extends IntentService {

    public SocketService() {
        super("SocketService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppWebSocket.mInstance = null;
        AppWebSocket.getInstance(this);
       /* if (VideoCallActivity.isInCall && !RandouCallActivity.isInCall) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Constants.TAG_TYPE, Constants.TAG_NOTIFY_USER);
                jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                jsonObject.put(Constants.TAG_RECEIVER_ID, VideoCallActivity.receiverId.equals(GetSet.getUserId())
                        ? VideoCallActivity.userId : VideoCallActivity.receiverId);
                jsonObject.put(Constants.TAG_MSG_TYPE, Constants.ONLINE);
                appWebSocket.send(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (RandouCallActivity.isInCall && !VideoCallActivity.isInCall) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Constants.TAG_TYPE, Constants.TAG_NOTIFY_USER);
                jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                jsonObject.put(Constants.TAG_RECEIVER_ID, RandouCallActivity.partnerId);
                jsonObject.put(Constants.TAG_MSG_TYPE, Constants.ONLINE);
                appWebSocket.send(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/
    }
}
