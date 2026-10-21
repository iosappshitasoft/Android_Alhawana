package com.app.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.addon.livestream.external.utils.StreamConstants;
import com.app.db.DBHelper;
import com.app.alhawanajd.App;
import com.app.alhawanajd.AppRTCUtils;
import com.app.alhawanajd.ChatActivity;
import com.app.alhawanajd.MainActivity;
import com.app.alhawanajd.SingleVideoActivity;
import com.app.alhawanajd.VideoCallActivity;
import com.app.alhawanajd.profile.MyProfileActivity;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.model.AddDeviceRequest;
import com.app.model.ChatResponse;
import com.app.model.GetSet;
import com.app.model.PostviedoRefresh;
import com.app.model.event.AdminMessageEvent;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DevicePref;
import com.app.utils.Logging;
import com.app.utils.SharedPref;
import com.app.utils.Util;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    ApiInterface apiInterface;
    DBHelper dbHelper;
    Context mContext;
    AppUtils appUtils;

    @Override
    public void onNewToken(String deviceToken) {
        super.onNewToken(deviceToken);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Logging.i(TAG, "onNewToken: " + deviceToken);
        DevicePref.getInstance(this).saveDeviceToken(deviceToken);
        if (SharedPref.getBoolean(SharedPref.IS_LOGGED, false)) {
            GetSet.setLogged(true);
            addDeviceId();
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Timber.tag(TAG).d("onMessageReceived getNotification: %s", new Gson().toJson(remoteMessage.getNotification()));
        Timber.tag(TAG).d("onMessageReceived: received payload: %s", new Gson().toJson(remoteMessage.getData()));
        Log.d(TAG, "onMessageReceived: "+ new Gson().toJson(remoteMessage.getData()));
        dbHelper = DBHelper.getInstance(this);
        mContext = this;
        appUtils = new AppUtils(this);
        Map<String, String> map = remoteMessage.getData(); // Get data payload

        String scope = "";

        // Try to extract and parse the nested JSON object
//        try {
//            // Convert the Map into a JSONObject (the outer object)
//            JSONObject outerJsonObject = new JSONObject(map);
//
//            // Get the "notification_data" string (escaped JSON string)
//            String notificationData = outerJsonObject.getString("notification_data");
//
//            // Convert the escaped JSON string (notificationData) into a proper JSONObject
//            JSONObject innerJsonObject = new JSONObject(notificationData);
//
//            // Now you can access the inner JSON data without escaped quotes
//            scope = innerJsonObject.getString("scope");  // Extract "scope"
//
//
//            String modifiedNotificationData = innerJsonObject.toString();
//
//            // Store the modified "notification_data" string back in the Map
//            map.put("notification_data", modifiedNotificationData);
//        } catch (JSONException e) {
//            // Handle JSON parsing errors
//            Timber.tag(TAG).e(e, "JSON parsing error");
//        }


        try {
            // Parse the incoming payload
            JSONObject jsonPayload = new JSONObject(remoteMessage.getData().toString());
            // Check if the key "notification_data" is present and not null
            if (jsonPayload.has("notification_data") && !jsonPayload.isNull("notification_data")) {
                String notificationData = jsonPayload.getString("notification_data");
                Log.d("onMessageReceived", "Notification data: " + notificationData);
                // You can now parse the notification data as required
                JSONObject notificationJson = new JSONObject(notificationData);
                String message = notificationJson.getString("message");
                scope = notificationJson.getString("scope");
                Log.d("onMessageReceived", "Message: " + message + ", Scope: " + scope);
            } else {
                scope = map.get(Constants.TAG_SCOPE);
            }
        } catch (JSONException e) {
            scope = map.get(Constants.TAG_SCOPE);
            Log.e("JSON parsing error", e.toString());
        }

        Log.d("onMessageReceived11", scope);

        if (scope != null) {
            switch (scope) {
                case Constants.TAG_VIDEO_CALL:
                    Log.d(TAG, "onMessageReceived: ");
                    onCalReceived(map, scope);
                    break;
                case Constants.TAG_ENDED:
                    onCalReceived(map, scope);
                    break;
                case Constants.TAG_TEXT_CHAT:
                    setChatNotification(map);
                    break;
                    case Constants.TAG_STREAM_INVITATION:
                        setInterestNotification(map);
                    break;
                case Constants.TAG_POSTVIDEO_NOTIFICATION:
                    setNewpostvideoNotification(map);
                    break;
                case Constants.TAG_FOLLOW:
                    if (GetSet.getFollowNotification().equals(Constants.TAG_TRUE)) {
                        setFollowNotification(map);
                    }
                    break;
                case Constants.TAG_ADMIN:
                    Timber.tag(TAG).d("onMessageReceived: admindata: %s", new Gson().toJson(map));
                    setAdminNotification(map);

                    break;
                case Constants.TAG_FOLLOWER_ON_POST:
                    setLivePostNotification(map);
                    break;
                case Constants.TAG_INTEREST:
                    if (GetSet.getInterestNotification())
                        setInterestNotification(map);
                    break;
                case Constants.TAG_MATCH:
                    if (GetSet.getInterestNotification())
                        setInterestNotification(map);
                    break;

                case Constants.TAG_FOLLOWER_ON_LIVE:
                    setNewLiveNotification(map);
                    break;
                case Constants.TAG_LIKE:
                case Constants.TAG_GIFT:
                case Constants.TAG_COMMENT:
                case Constants.TAG_MENTION:
                    setFollowNotification(map);
                    break;


            }
        }
    }

    private void setChatNotification(Map<String, String> data) {
        Intent intent = new Intent();
        intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.NOTIFICATION, Constants.TAG_PROFILE);
        intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
        intent.putExtra(Constants.TAG_PARTNER_ID, data.get(Constants.TAG_USER_ID));
        intent.putExtra(Constants.TAG_PARTNER_NAME, data.get(Constants.TAG_USER_NAME));
        intent.putExtra(Constants.TAG_PARTNER_IMAGE, data.get(Constants.TAG_USER_IMAGE));
        App.isAppOpened = true;
        String messageType = "";
        messageType = data.get(Constants.TAG_MSG_TYPE);
        if (GetSet.getChatNotification() != null) {
            if (GetSet.getChatNotification().equals(Constants.TAG_TRUE)) {
                if (App.getCurrentActivity() instanceof ChatActivity) {
                    ChatActivity activity = (ChatActivity) App.getCurrentActivity();
                    if (activity.getPartnerId() != null && !activity.getPartnerId().equals(data.get(Constants.TAG_USER_ID))) {
                        if (messageType.equals(Constants.TAG_TEXT)) {
                            sendNotification(1, data.get(Constants.TAG_USER_NAME), AppUtils.decryptMessage(data.get(Constants.TAG_MESSAGE)), intent);
                        } else if (messageType.equals(Constants.TAG_GIF)) {
                            if (LocaleManager.isRTL()) {
                                sendNotification(1, data.get(Constants.TAG_USER_NAME), "GIF", intent);
                            } else {
                                sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.gif_txt), intent);
                            }
                        } else if (messageType.equals(Constants.TAG_AUDIO)) {
                            Log.i(TAG, "setChatNotificationau: " + messageType);

                            if (LocaleManager.isRTL()) {
                                sendNotification(1, data.get(Constants.TAG_USER_NAME), "صوتي", intent);
                            } else {
                                sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.audio), intent);
                            }

                        } else if (!messageType.equals(Constants.TAG_MISSED)) {
                            if (LocaleManager.isRTL()) {
                                sendNotification(1, data.get(Constants.TAG_USER_NAME), "صورة", intent);
                            } else {
                                sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.image), intent);
                            }

                        }
                    }
                } else {
                    if (messageType.equals(Constants.TAG_TEXT)) {
                        sendNotification(1, data.get(Constants.TAG_USER_NAME), AppUtils.decryptMessage(data.get(Constants.TAG_MESSAGE)), intent);
                    } else if (messageType.equals(Constants.TAG_GIF)) {
                        if (LocaleManager.isRTL()) {
                            sendNotification(1, data.get(Constants.TAG_USER_NAME), "GIF", intent);
                        } else {
                            sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.gif_txt), intent);
                        }
                    } else if (messageType.equals(Constants.TAG_AUDIO)) {
                        if (LocaleManager.isRTL()) {
                            sendNotification(1, data.get(Constants.TAG_USER_NAME), "صوتي", intent);
                        } else {
                            sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.audio), intent);
                        }
                    } else if (!messageType.equals(Constants.TAG_MISSED)) {
                        if (LocaleManager.isRTL()) {
                            sendNotification(1, data.get(Constants.TAG_USER_NAME), "صورة", intent);
                        } else {
                            sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.image), intent);
                        }
                    }

                }
            }
        }

    }


    private void setLivePostNotification(Map<String, String> data) {
        Intent intent = new Intent();
        if (data != null) {
            final String scope = Util.getEmptyIfNull(data.get("scope"));
            final Pattern mainLaunchPattern = Pattern.compile("followerpostvideo");
            Timber.d("setPostLiveNotification: %b", scope.matches(mainLaunchPattern.pattern()));
            Timber.d("setPostLiveNotification scope: %b", scope.toString());
            if (scope.matches(mainLaunchPattern.pattern())) {
                intent = new Intent(getApplicationContext(), MainActivity.class);
               // intent = new Intent(getApplicationContext(), SingleVideoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(Constants.TAG_VIDEO_ID, data.get(Constants.TAG_STREAM_NAME));
                intent.putExtra(Constants.TAG_STREAM_NAME, data.get(Constants.TAG_STREAM_NAME));
                intent.putExtra(Constants.TAG_SCOPE, data.get(Constants.TAG_SCOPE));
                intent.putExtra(Constants.TAG_USER_ID, data.get(Constants.TAG_USER_ID));
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.NOTIFICATION, Constants.TAG_NOTIFICATION_PAGE);
            }
        }
        String appName = getString(R.string.app_name);

        String message = data.get(Constants.TAG_MESSAGE);
        String check = "";
        String post_video_username = data.get(Constants.TAG_USER_NAME);
        Log.d(TAG, "setFollowNotification_livename: " + post_video_username);
        if (LocaleManager.isRTL()) {
            if (message.equalsIgnoreCase("posted a new video")) {
                check = (post_video_username + " " + " نشر فيديو جديد");
            }
        } else {
            if (message.equalsIgnoreCase("posted a new video")) {
                check = (post_video_username + " " + data.get(Constants.TAG_MESSAGE));
            }
        }
        sendNotification(2, appName, check, intent);
    }

    //correct code for nov month delivery for exact video redirection on foryou
    /*  private void setLivePostNotification(Map<String, String> data) {
        Intent intent = new Intent();
        if (data != null) {
            final String scope = Util.getEmptyIfNull(data.get("scope"));
            final Pattern mainLaunchPattern = Pattern.compile("followerpostvideo");
            Timber.d("setPostLiveNotification: %b", scope.matches(mainLaunchPattern.pattern()));
            Timber.d("setPostLiveNotification scope: %b", scope.toString());
            if (scope.matches(mainLaunchPattern.pattern())) {
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(Constants.TAG_VIDEO_ID, data.get(Constants.TAG_VIDEO_ID));
                intent.putExtra(Constants.TAG_SCOPE, data.get(Constants.TAG_SCOPE));
                intent.putExtra(Constants.TAG_USER_ID, data.get(Constants.TAG_USER_ID));
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.NOTIFICATION, Constants.TAG_NOTIFICATION_PAGE);
            }
        }
        String appName = getString(R.string.app_name);

        String message = data.get(Constants.TAG_MESSAGE);
        String check = "";
        String post_video_username = data.get(Constants.TAG_USER_NAME);
        Log.d(TAG, "setFollowNotification_livename: " + post_video_username);
        if (LocaleManager.isRTL()) {
            if (message.equalsIgnoreCase("posted a new video")) {
                check = (post_video_username + " " + "نشر فيديو جديد");
            }
        } else {
            if (message.equalsIgnoreCase("posted a new video")) {
                check = (post_video_username + " " + data.get(Constants.TAG_MESSAGE));
            }
        }
        sendNotification(2, appName, check, intent);
    }
*/
    private void setFollowNotification(Map<String, String> data) {
        Intent intent = new Intent();
        if (data != null) {
            final String scope = Util.getEmptyIfNull(data.get("scope"));
            final Pattern mainLaunchPattern = Pattern.compile("like|gift|comment|mention|follow");
            Timber.d("setFollowNotification: %b", scope.matches(mainLaunchPattern.pattern()));
            if (scope.matches(mainLaunchPattern.pattern())) {
//                intent = new Intent(getApplicationContext(), SingleVideoActivity.class);
//                intent.putExtra(Constants.TAG_VIDEO_ID, data.get(Constants.TAG_VIDEO_ID));
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(Constants.TAG_SCOPE, data.get(Constants.TAG_SCOPE));
                if (data.containsKey(Constants.TAG_VIDEO_ID)) {
                    intent.putExtra(Constants.TAG_VIDEO_ID, data.get(Constants.TAG_VIDEO_ID));
                }
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.NOTIFICATION, Constants.TAG_NOTIFICATION_PAGE);
            }
        } else {
            intent = new Intent(getApplicationContext(), OthersProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
            intent.putExtra(Constants.NOTIFICATION, Constants.TAG_FOLLOW);
            intent.putExtra(Constants.TAG_PARTNER_ID, data.get(Constants.TAG_USER_ID));
        }
        String appName = getString(R.string.app_name);
        if (GetSet.getFollowNotification().equals(Constants.TAG_TRUE)) {
            String message = data.get(Constants.TAG_MESSAGE);
            String check = "";
            String user_name = data.get(Constants.TAG_USER_NAME);
            Log.d(TAG, "setFollowNotification_user_name: " + user_name);
            if (LocaleManager.isRTL()) {
                if (message.equalsIgnoreCase("starts following you")) {
                    check = (user_name + " " + "بدأ بمتابعتك");
                } else if (message.equalsIgnoreCase("liked your video")) {
                    check = (user_name + " " + "أعجب بالفيديو الخاص بك");
                } else if (message.equalsIgnoreCase("commented on your video")) {
                    check = (user_name + " " + "علّق على الفيديو الخاص بك");
                } else if (message.equalsIgnoreCase("sent a gift to you")) {
                    check = (user_name + " " + "أرسل هدية لك");
                } else if (message.equalsIgnoreCase("mentioned you in a video")) {
                    check = (user_name + " " + "ذكرك في فيديو");
                }
            } else {
                if (message.equalsIgnoreCase("starts following you")) {
                    check = (user_name + " " + data.get(Constants.TAG_MESSAGE));
                } else if (message.equalsIgnoreCase("liked your video")) {
                    check = (user_name + " " + data.get(Constants.TAG_MESSAGE));
                } else if (message.equalsIgnoreCase("commented on your video")) {
                    check = (user_name + " " + data.get(Constants.TAG_MESSAGE));
                } else if (message.equalsIgnoreCase("sent a gift to you")) {
                    check = (user_name + " " + data.get(Constants.TAG_MESSAGE));
                } else if (message.equalsIgnoreCase("mentioned you in a video")) {
                    check = (user_name + " " + data.get(Constants.TAG_MESSAGE));
                }
            }
            sendNotification(2, appName, check, intent);
        }
    }


    private void setAdminNotification(Map<String, String> data) {
        String message = "";
        try {
            String notificationData = data.get("notification_data");
            if (notificationData != null) {
                JSONObject jsonPayload = new JSONObject(notificationData);
                message = jsonPayload.optString("message", "No message");
            } else {
                Log.d("setAdminNotification", "No notification_data found in the map.");
            }
        } catch (JSONException e) {
            Log.e("JSON parsing error", e.toString());
        }
        Intent intent = new Intent();
        intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
        intent.putExtra(Constants.NOTIFICATION, Constants.TAG_ADMIN);
        intent.putExtra(Constants.TAG_FROM, Constants.TAG_ADMIN);
        ChatResponse adminChat = dbHelper.getAdminChat(GetSet.getUserId());
        intent.putExtra(Constants.TAG_PARTNER_NAME, adminChat.getUserName());
        Log.d(TAG, "setAdminNotification: " + adminChat.getUserName());
        App.isAppOpened = true;

        if (App.getCurrentActivity() instanceof ChatActivity) {
            ChatActivity activity = (ChatActivity) App.getCurrentActivity();
            if (!activity.isAdminChat) {
                String appName = getString(R.string.app_name);
                sendNotification(0, appName, message, intent);
            } else {
                EventBus.getDefault().post(new AdminMessageEvent(true));
            }
        } else {
            String appName = getString(R.string.app_name);
            sendNotification(0, appName, message, intent);
        }
    }

    private void setInterestNotification(Map<String, String> data) {
        Intent intent = new Intent();
        intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
        intent.putExtra(Constants.NOTIFICATION, data.get(Constants.TAG_SCOPE));
        String appName = getString(R.string.app_name);
        sendNotification(0, appName, data.get(Constants.TAG_MESSAGE), intent);
    }


    private void setNewLiveNotification(Map<String, String> data) {

        Intent intent = new Intent();
        intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.NOTIFICATION, Constants.TAG_LIVE);
        intent.putExtra(StreamConstants.TAG_STREAM_NAME, data.get(StreamConstants.TAG_STREAM_NAME));
        Log.d(TAG, "setNewLiveNotification: " + data.get(StreamConstants.TAG_STREAM_NAME));
        intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
        String appName = getString(R.string.app_name);

        String message = data.get(Constants.TAG_MESSAGE);
        String check = "";
        String liveuser_name = data.get(Constants.TAG_USER_NAME);
        Log.d(TAG, "setFollowNotification_livename: " + liveuser_name);
        if (LocaleManager.isRTL()) {
            if (message.equalsIgnoreCase("is on live now.")) {
                check = (/*liveuser_name + " " +*/ "في  البث الآن.");
            }
        } else {
            if (message.equalsIgnoreCase("is on live now.")) {
                check = (/*liveuser_name + " " +*/ data.get(Constants.TAG_MESSAGE));
            } else {
                if (message.contains("is on live now")) {
                    check = (/*liveuser_name + " " +*/ data.get(Constants.TAG_MESSAGE));
                }

            }

        }
        sendNotification(0, appName, check, intent);
    }

    private void setNewpostvideoNotification(Map<String, String> data) {
        EventBus.getDefault().post(new PostviedoRefresh(true));
        Intent intent = new Intent();
        intent = new Intent(getApplicationContext(), MyProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String appName = getString(R.string.app_name);

        String message = data.get(Constants.TAG_MESSAGE);
        String check = "";
        if (LocaleManager.isRTL()) {
            if (message.equalsIgnoreCase("Your post posted successfully")) {
                check = " تم النشر بنجاح";
            }
        } else {
            if (message.equalsIgnoreCase("Your post posted successfully")) {
                check = data.get(Constants.TAG_MESSAGE);
            }
        }
        sendNotification(0, appName, check, intent);
    }

    private void sendNotification(int notifyId, String title, String message, Intent intent) {
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        long when = System.currentTimeMillis();
        String CHANNEL_ID = getString(R.string.notification_channel_id);
        CharSequence channelName = getString(R.string.app_name);
        PendingIntent resultPendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
        } else {
            resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        }

        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            if (mNotifyManager != null) {
                mNotifyManager.createNotificationChannel(notificationChannel);
            }
        } /*else {*/
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

        mBuilder.setChannelId(CHANNEL_ID)
                .setContentText(message)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.mipmap.app_icon_new)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mBuilder.setFullScreenIntent(resultPendingIntent, true);
        }
        mNotifyManager.notify(getString(R.string.app_name), notifyId, mBuilder.build());

    }

    private void addDeviceId() {
        final String token = DevicePref.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        AddDeviceRequest request = new AddDeviceRequest();
        request.setUserId(GetSet.getUserId());
        request.setDeviceToken(token);
        request.setDeviceType(Constants.TAG_DEVICE_TYPE);
        request.setDeviceId(deviceId);
        request.setDeviceModel(AppUtils.getDeviceName());

        Call<Map<String, String>> call3 = apiInterface.pushSignIn(request);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {

            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();

            }
        });

    }

    private void onCalReceived(Map<String, String> map, String scope) {
        String userId = map.get(Constants.TAG_USER_ID);
        String userName = map.get(Constants.TAG_USER_NAME);
        String userImage = map.get(Constants.TAG_USER_IMAGE);
        String receiverId = map.get(Constants.TAG_RECEIVER_ID);
        String chatType = map.get(Constants.TAG_CHAT_TYPE);
        String utcTime = map.get(Constants.TAG_CREATED_AT);
        String callType = map.get(Constants.TAG_CALL_TYPE);
        String roomId = map.get(Constants.TAG_ROOM_ID);
        String platform = map.get(Constants.TAG_PLATFORM);

        long diffInMs = System.currentTimeMillis() - AppUtils.getTimeFromUTC(mContext, utcTime);
        long diffSeconds = diffInMs / 1000;

        if (diffSeconds < 30 && scope.equalsIgnoreCase(Constants.TAG_VIDEO_CALL)) {
            TelephonyManager telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            int isPhoneCallOn = telephony.getCallState();
            Log.d(TAG, "onCalReceived: " + (!Constants.isInRandomCall) + (!Constants.isInStream) + (!VideoCallActivity.isInCall) + (isPhoneCallOn == 0));
            if (!Constants.isInRandomCall && !Constants.isInStream && !VideoCallActivity.isInCall && isPhoneCallOn == 0) {
                VideoCallActivity.isInCall = true;
                AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                Intent intent = appRTCUtils.connectToRoom(userId, Constants.TAG_RECEIVE, chatType);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.TAG_ROOM_ID, roomId);
                intent.putExtra(Constants.TAG_PLATFORM, platform);
                intent.putExtra(Constants.TAG_USER_ID, userId);
                intent.putExtra(Constants.TAG_USER_NAME, userName);
                intent.putExtra(Constants.TAG_USER_IMAGE, userImage);
                intent.putExtra(Constants.TAG_RECEIVER_ID, receiverId);
                Log.d(TAG, "onCalReceived: " + intent);
                //   PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                startActivity(intent);
            }
        } else if (scope.equalsIgnoreCase(Constants.TAG_ENDED)) {
            Timber.i("App.getCurrentActivity %s", App.getCurrentActivity());

            /*if (App.getCurrentActivity() instanceof VideoCallActivity) {
                VideoCallActivity activity = (VideoCallActivity) App.getCurrentActivity();
                if (activity.isSender) {
                    if (activity.receiverId.equals(userId)) {
                        activity.finish();
                    }
                } else {
                    if (activity.userId.equals(userId)) {
                        activity.finish();
                    }
                }
            }*/


            if (App.getCurrentActivity() instanceof VideoCallActivity) {
                Timber.i("firebaseonCalReceived: %s", scope);
                Timber.i("firebaseonCalReceivedi: %s", userId);
                String iosUserId = userId.replaceAll("Optional", "");
                String ios = iosUserId.replace("(\")", "");
                Timber.i("firebaseonCalReceivedif: %s", iosUserId);
                Timber.i("firebaseonCalReceivediff: %s", ios);
                VideoCallActivity activity = (VideoCallActivity) App.getCurrentActivity();

                if (activity.isSender) {
                    if (activity.receiverId.equals(userId)) {
                        //activity.finishActivity();
                        activity.finish();
                    }
                } else {
                    if (activity.userId.equals(userId)) {
                        Timber.i("firebaseonCalReceivedifff: %s", userId);
                        //activity.finishActivity();
                        activity.finish();
                    }
                }
               /* if (userId.contains("Optional")){
                    Timber.i(TAG, "firebaseonCalReceivediffoptio: "+userId);
                    activity.finishActivity();
                    activity.finish();
                }*/

            } else if (App.getCurrentActivity() instanceof ChatActivity) {
                /*ChatActivity activity = (ChatActivity) App.getCurrentActivity();
                activity.finish();*/
            } else if (App.getCurrentActivity() instanceof OthersProfileActivity) {
                /*OthersProfileActivity activity = (OthersProfileActivity) App.getCurrentActivity();
                activity.finish();*/
            } else if (App.getCurrentActivity() instanceof MainActivity) {

            } else {
                //App.getCurrentActivity().finish();
            }


        }
    }

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }
}
