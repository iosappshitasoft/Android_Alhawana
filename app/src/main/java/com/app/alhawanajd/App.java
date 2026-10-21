package com.app.alhawanajd;

import static com.app.Zego.ApiBase.IPAddressUtils.getIPAddress;
import static com.app.utils.Constants.KEY_LAST_BG_TIME;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.app.Zego.ApiBase.XDeviceInfo;
import com.app.Zego.activity.PkLiveStreamingActivity;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.app.Zego.ZEGOSDKKeyCenter;
import com.app.Zego.internal.sdk.ZEGOSDKManager;
import com.app.db.DBHelper;
import com.app.helper.AppWebSocket;
import com.app.helper.LocaleManager;
import com.app.helper.StorageUtils;
import com.app.logging.FileTree;
import com.app.model.GetSet;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DevicePref;
import com.app.utils.Logging;
import com.app.utils.SharedPref;
import com.app.utils.api.CommonApi;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;


public class App extends android.app.Application implements LifecycleObserver {


    private static final String TAG = App.class.getSimpleName();

    private static App instance;
    private static Activity mCurrentActivity = null;
    private Locale locale;
    public static boolean onAppForegrounded = false, isAppOpened = false;
    public static boolean isCacheEnabled = true;
    private HttpProxyCacheServer proxy;
    public static RequestOptions profileImageRequest;
    private StorageUtils storageUtils;

    public static SimpleCache simpleCache = null;
    LeastRecentlyUsedCacheEvictor leastRecentlyUsedCacheEvictor = null;
    StandaloneDatabaseProvider databaseProvider;
    long exoPlayerCacheSize = 500 * 1024 * 1024;

    private static long mLastClicked = 0;
    private static Gson sGson;

    //Added for sep9 livestreaming
    public static String deviceID;
    public static String deviceIP;


    private DefaultLifecycleObserver lifecycleObserver = new DefaultLifecycleObserver() {
        @Override
        public void onCreate(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onCreate(owner);
        }

        @Override
        public void onStart(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onStart(owner);
        }

        @Override
        public void onResume(@NonNull LifecycleOwner owner) {
            Log.d(TAG, "onAppForegrounded: ");
            onAppForegrounded = true;
            if (!(App.getCurrentActivity() instanceof SplashActivity)) {
                AppWebSocket.mInstance = null;
                AppWebSocket.getInstance(instance);
            }


//            if (isSystemLanguageArabic()) {
//                Log.d("LangCheck", "Device language is Arabic");
//                setNewLocale(getApplicationContext(),"ar","العربية");
//            } else {
//                Log.d("LangCheck", "Device language is NOT Arabic");
//                setNewLocale(getApplicationContext(),"en","English");
//            }


           /* if (SharedPref.getBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false)) {
                DialogAppLock dialogAppLock = new DialogAppLock();
                dialogAppLock.setCallBack(new FingerPrintCallBack() {
                    @Override
                    public void onPurchased(boolean withFingerprint, @Nullable FingerprintManager.CryptoObject cryptoObject) {

                    }

                    @Override
                    public void onError(String errString) {

                    }
                });
                dialogAppLock.setCancelable(false);
                dialogAppLock.show(get());
            }*/
        }

        @Override
        public void onPause(@NonNull LifecycleOwner owner) {
            onAppForegrounded = false;
            DefaultLifecycleObserver.super.onPause(owner);
        }

        @Override
        public void onStop(@NonNull LifecycleOwner owner) {
            Log.d(TAG, "onAppBackgrounded: ");
            onAppForegrounded = false;
            if (GetSet.isIsLogged() && AppWebSocket.getInstance(instance) != null) {
                AppWebSocket.getInstance(instance).disconnect();
            }

        /*if (GetSet.isIsLogged() && Constants.RANDOU_ENABLED && RandouWebSocket.getInstance(instance) != null) {
            if (RandouCallActivity.isInCall) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TAG_TYPE, Constants.TAG_NOTIFY_USER);
                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                    jsonObject.put(Constants.TAG_PARTNER_ID, RandouCallActivity.partnerId);
                    jsonObject.put(Constants.TAG_MSG_TYPE, Constants.OFFLINE);
                    Log.i(TAG, "onAppBackgrounded: " + jsonObject);
                    RandouWebSocket.getInstance(instance).send(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }*/

        }

        @Override
        public void onDestroy(@NonNull LifecycleOwner owner) {
            Log.d(TAG, "onDestroy: ");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

       /* if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }
*/
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        deviceIP = getIPAddress(this);

        storageUtils = StorageUtils.getInstance(this);
        EventBus.getDefault().removeAllStickyEvents();
        SharedPref.initPref(instance);
        isAppOpened = false;
        CommonApi.getAppDefaultData(this);

        Timber.plant(new Timber.DebugTree(), new FileTree(this));
        //OneSignal notification
        //initOneSignal();

        ZEGOSDKManager.getInstance().initSDK(this, ZEGOSDKKeyCenter.appID, ZEGOSDKKeyCenter.appSign);

        if (isCacheEnabled) {
            initCache();
        }

        profileImageRequest = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .dontAnimate()
                .circleCrop();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleObserver);
        AppUtils.resetFilter();
        if (SharedPref.getBoolean(SharedPref.IS_LOGGED, false)) {
            AppWebSocket.getInstance(this);
            GetSet.setLogged(true);
            GetSet.setUserId(SharedPref.getString(SharedPref.USER_ID, null));
            GetSet.setLoginId(SharedPref.getString(SharedPref.LOGIN_ID, null));
            GetSet.setLoginType(SharedPref.getString(SharedPref.LOGIN_TYPE, null));
            GetSet.setAuthToken(SharedPref.getString(SharedPref.AUTH_TOKEN, null));
            GetSet.setName(SharedPref.getString(SharedPref.NAME, null));
            GetSet.setUserName(SharedPref.getString(SharedPref.USER_NAME, null));
            GetSet.setUserImage(SharedPref.getString(SharedPref.USER_IMAGE, null));
            GetSet.setAge(SharedPref.getString(SharedPref.AGE, null));
            GetSet.setDob(SharedPref.getString(SharedPref.DOB, null));
            GetSet.setGender(SharedPref.getString(SharedPref.GENDER, null));
            /*GetSet.setLocation(SharedPref.getString(SharedPref.LOCATION, null));*/

            GetSet.setGems(SharedPref.getFloat(SharedPref.GEMS, 0F));
            GetSet.setGifts(SharedPref.getLong(SharedPref.GIFTS, 0L));
            GetSet.setVideos(SharedPref.getLong(SharedPref.VIDEOS, 0L));
            GetSet.setVideosHistory(SharedPref.getLong(SharedPref.VIDEOS_HISTORY, 0L));
            GetSet.setFollowersCount(SharedPref.getString(SharedPref.FOLLOWERS_COUNT, "0"));
            GetSet.setFollowingCount(SharedPref.getString(SharedPref.FOLLOWINGS_COUNT, "0"));
            GetSet.setFriendsCount(SharedPref.getInt(SharedPref.FRIENDS_COUNT, 0));
            GetSet.setInterestsCount(SharedPref.getInt(SharedPref.INTEREST_COUNT, 0));
            GetSet.setUnlocksLeft(SharedPref.getInt(SharedPref.UNLOCKS_LEFT, 0));
            GetSet.setPremiumMember(SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, Constants.TAG_FALSE));
            GetSet.setPremiumExpiry(SharedPref.getString(SharedPref.PREMIUM_EXPIRY, Constants.TAG_FALSE));
            GetSet.setPrivacyAge(SharedPref.getString(SharedPref.PRIVACY_AGE, Constants.TAG_FALSE));
            GetSet.setPrivacyContactMe(SharedPref.getString(SharedPref.PRIVACY_CONTACT_ME, Constants.TAG_FALSE));
            GetSet.setShowNotification(SharedPref.getString(SharedPref.SHOW_NOTIFICATION, Constants.TAG_FALSE));
            GetSet.setChatNotification(SharedPref.getString(SharedPref.CHAT_NOTIFICATION, Constants.TAG_FALSE));
            GetSet.setFollowNotification(SharedPref.getString(SharedPref.FOLLOW_NOTIFICATION, Constants.TAG_FALSE));
            GetSet.setInterestNotification(SharedPref.getBoolean(SharedPref.INTEREST_NOTIFICATION, false));
            GetSet.setGiftEarnings(SharedPref.getString(SharedPref.GIFT_EARNINGS, "0"));
            GetSet.setReferalLink(SharedPref.getString(SharedPref.REFERAL_LINK, ""));
            GetSet.setCreatedAt(SharedPref.getString(SharedPref.CREATED_AT, ""));
            SharedPref.putString(SharedPref.POST_COMMAND, "Everyone");

            GetSet.setPostCommand(SharedPref.getString(SharedPref.POST_COMMAND, "Everyone"));

            GetSet.setSendMessage(SharedPref.getString(SharedPref.SEND_MESSAGE, "Everyone"));

            if (GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                GetSet.setOncePurchased(true);
            } else {
                GetSet.setOncePurchased(SharedPref.getBoolean(SharedPref.ONCE_PAID, false));
            }
        }
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        App app = (App) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }
    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }


    private void initOneSignal() {
        /*OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(Constants.ONESIGNAL_APP_ID);*/
    }

    public static void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }

    public static Activity getCurrentActivity() {
        return mCurrentActivity;
    }


    @SuppressLint("ResourceAsColor")
    public static void dialog(Context ctx, String title, String content, int color_green) {

        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        dialog.getWindow().setLayout(DevicePref.getInstance(ctx).getInt(DevicePref.SCREEN_WIDTH, 0) * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        MaterialTextView alertTitle = (MaterialTextView) dialog.findViewById(R.id.alert_title);
        MaterialTextView alertMsg = (MaterialTextView) dialog.findViewById(R.id.alert_msg);
        MaterialButton alertOk = (MaterialButton) dialog.findViewById(R.id.alert_button);

        alertMsg.setTextColor(color_green);

        alertTitle.setText(title);
        alertMsg.setText(content);

        alertOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        } else dialog.dismiss();


    }

    public static Gson getGsonPrettyInstance() {
        if (sGson == null) {
            sGson = new GsonBuilder().setLenient().setPrettyPrinting().create();
        }
        return sGson;
    }


    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static synchronized App getInstance() {
        return instance;
    }

    public static void makeToast(String message) {
        Toast.makeText(instance.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void makeCustomToast(String message) {

        /*Toast toast = Toast.makeText(instance, message, Toast.LENGTH_SHORT);
        LayoutInflater li = (LayoutInflater) instance.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
        View toastView = li.inflate(R.layout.toast_hint_layout, null);
        TextView text = (TextView) toastView.findViewById(R.id.hint_text_tv);
        text.setText(message);
        text.setTextColor(instance.getResources().getColor(R.color.colorWhite));
        toastView.setBackgroundTintList(ColorStateList.valueOf(instance.getResources().getColor(R.color.colorBlack)));
        toastView.setBackgroundColor(Color.BLACK);
        toast.setView(toastView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();*/

        Toast toast = Toast.makeText(instance, message, Toast.LENGTH_SHORT);
        toast.getView()
                .setBackgroundTintList(ColorStateList.valueOf(instance.getResources().getColor(R.color.colorBlack)));
        ((TextView) toast.getView().findViewById(android.R.id.message))
                .setTextColor(instance.getResources().getColor(R.color.colorWhite));
        toast.getView().setBackgroundTintList(ColorStateList.valueOf(instance.getResources().getColor(R.color.colorBlack)));
        ((TextView) toast.getView().findViewById(android.R.id.message)).setTextColor(instance.getResources().getColor(R.color.colorWhite));
        toast.show();



      /*  Toast toast = Toast.makeText(instance, message, Toast.LENGTH_SHORT);
        toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));*/
    /*    toast.getView()
                .setBackgroundTintList(ColorStateList.valueOf(instance.getResources().getColor(R.color.colorBlack)));
        ((TextView) toast.getView().findViewById(android.R.id.message))
                .setTextColor(instance.getResources().getColor(R.color.colorWhite));
*/        //toast.show();
    }

    private void initCache() {
        Timber.d("initCache: %s", bytesIntoHumanReadable(exoPlayerCacheSize));
        if (leastRecentlyUsedCacheEvictor == null) {
            leastRecentlyUsedCacheEvictor = new LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize);
        }

        if (databaseProvider != null) {
            databaseProvider = new StandaloneDatabaseProvider(this);
        }
        File cacheDir = StorageUtils.getInstance(this).getVideoCacheDir(true);
        if (!cacheDir.exists()) cacheDir.mkdirs();
        if (simpleCache == null) {
            simpleCache = new SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, databaseProvider);
        }
    }

    public static SimpleCache getSimpleCache() {
        return simpleCache;
    }
    public static void showErrorDialog(Context context, String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                GetSet.reset();
                SharedPref.clearAll();
                DBHelper.getInstance(context).clearDB();
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            }
        });
        alertDialog.show();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
        fix();
    }

    public static void fix() {
        try {
            Class clazz = Class.forName("java.lang.Daemons$FinalizerWatchdogDaemon");
            Method method = clazz.getSuperclass().getDeclaredMethod("stop");
            method.setAccessible(true);

            Field field = clazz.getDeclaredField("INSTANCE");
            field.setAccessible(true);

            method.invoke(field.get(null));

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private String bytesIntoHumanReadable(long bytes) {
        long kilobyte = 1024;
        long megabyte = kilobyte * 1024;
        long gigabyte = megabyte * 1024;
        long terabyte = gigabyte * 1024;

        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + " B";

        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return (bytes / kilobyte) + " KB";

        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return (bytes / megabyte) + " MB";

        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return (bytes / gigabyte) + " GB";

        } else if (bytes >= terabyte) {
            return (bytes / terabyte) + " TB";

        } else {
            return bytes + " Bytes";
        }
    }

    @SuppressLint("DefaultLocale")
    public static String fmtFloat(float d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.format("%s", d);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }

    public static boolean isOnAppForegrounded() {
        return onAppForegrounded;
    }

    public void changeLocale(Context context) {
        Configuration config = context.getResources().getConfiguration();
        String lang = SharedPref
                .getString(Constants.TAG_LANGUAGE_CODE, Constants.DEFAULT_LANGUAGE_CODE);
        if (!(config.locale.getLanguage().equals(lang))) {
            locale = new Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            Logging.i(TAG, "changeLocale: " + lang);
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        }
    }

    // TODO: 26/10/21 @VishnuKumar
    public static boolean isPreventMultipleClick() {
        Log.e("lastElap", "-" + mLastClicked);
        if (SystemClock.elapsedRealtime() - mLastClicked < 1000) {
            return true;
        }
        mLastClicked = SystemClock.elapsedRealtime();
        return false;
    }

    public static void preventMultipleClick(final View view) {
        view.setEnabled(false);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setEnabled(true);
            }
        }, 1000);
    }

    public static RequestOptions getProfileImageRequest() {
        return profileImageRequest;
    }

    public static int getScreenOrientation(Activity context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int orientation;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        orientation = context.getResources().getConfiguration().orientation;
        return orientation;
    }

    public static void hideKeyboardFragment(Context context, View view) {

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        /*imm.hideSoftInputFromWindow(context.getWindow().getDecorView().getWindowToken(), 0);*/
        imm.hideSoftInputFromWindow(view.getRootView().getWindowToken(), 0);
    }

    public static void hideKeyboardActivity(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    public static void pauseExternalAudio(Context mContext) {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            AppUtils.isExternalPlay = true;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            mContext.sendBroadcast(i);
        }
    }

    public static String milliSecondsToTimer(long milliseconds) {
        if (milliseconds > 0) {
            String finalTimerString = "";
            String secondsString = "";

            // Convert total duration into time
            int hours = (int) (milliseconds / (1000 * 60 * 60));
            int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
            int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
            // Add hours if there
            if (hours > 0) {
                finalTimerString = hours + ":";
            }

            // Prepending 0 to seconds if it is one digit
            if (seconds < 10) {
                secondsString = "0" + seconds;
            } else {
                secondsString = "" + seconds;
            }
            finalTimerString = finalTimerString + minutes + ":" + secondsString;

//        Timber.d("milliSecondsToTimer: %s", finalTimerString);
            // return timer string
            return finalTimerString;
        } else {
            return "00:00";
        }
    }

    public static void openImage(Context context, String url, String from, ImageView view) {
        preventMultipleClick(view);
        Intent intent = new Intent(context, ImageViewActivity.class);
        intent.putExtra(Constants.TAG_USER_IMAGE, url);
        intent.putExtra(Constants.TAG_FROM, from);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }

    public static void openImage(Context context, Uri uri, String from, ImageView view) {
        preventMultipleClick(view);
        Intent intent = new Intent(context, ImageViewActivity.class);
        intent.setData(uri);
        intent.putExtra(Constants.TAG_FROM, from);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }

    public static class AppLifecycleObserver {

        private static final String TAG = "AppLifecycleObserver";
        private static AppBackgroundListener listener;

        private final DefaultLifecycleObserver lifecycleObserver = new DefaultLifecycleObserver() {

            @Override
            public void onCreate(@NonNull LifecycleOwner owner) {
                DefaultLifecycleObserver.super.onCreate(owner);
            }

            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                DefaultLifecycleObserver.super.onStart(owner);
                Log.d("AppLifecycle", "onStart - App came to foreground");

                Activity currentActivity = App.getCurrentActivity();

                if (currentActivity instanceof PkLiveStreamingActivity) {
                    long lastBgTime = SharedPref.getLong(KEY_LAST_BG_TIME, 0);
                    long now = System.currentTimeMillis();
                    Log.d("AppLifecycle", "lastBgTime - " +lastBgTime +"test");
                    if (lastBgTime > 0) {
                        long diff = now - lastBgTime;
                        Log.d("AppLifecycle", "PkLiveStreamingActivity was in background for " + diff + " ms");

                        if (diff > Constants.TIMEOUT_MS) {
                            Log.d("AppLifecycle", "Timeout exceeded. Restarting MainActivity");

                            Intent intent = new Intent(currentActivity, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            currentActivity.startActivity(intent);
                        }
                    }
                }

            }

            @Override
            public void onResume(@NonNull LifecycleOwner owner) {
                Log.d("AppLifecycle", "onResume");
                onAppForegrounded = true;

                if (!(App.getCurrentActivity() instanceof SplashActivity)) {
                    AppWebSocket.mInstance = null;
                    AppWebSocket.getInstance(instance);
                }
            }

            @Override
            public void onPause(@NonNull LifecycleOwner owner) {
                onAppForegrounded = false;
                Log.d("AppLifecycle", "onPause");
                DefaultLifecycleObserver.super.onPause(owner);
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                Log.d("AppLifecycle", "onStop - App went to background");
                onAppForegrounded = false;

                if (GetSet.isIsLogged() && AppWebSocket.getInstance(instance) != null) {
                    AppWebSocket.getInstance(instance).disconnect();

                    if (listener != null) {
                        listener.onAppBackgrounded(); // Execute only if a listener is set
                    }

                    if (App.getCurrentActivity() instanceof PkLiveStreamingActivity) {
                        Log.e(TAG, "onStop: ::::::");
                    }
                }
            }

            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                Log.d("AppLifecycle", "onDestroy");
            }
        };

        // Set listener for a specific activity
        public static void setAppBackgroundListener(AppBackgroundListener backgroundListener) {
            listener = backgroundListener;
        }

        // Remove listener when activity is destroyed
        public static void removeAppBackgroundListener() {
            listener = null;
        }

        public DefaultLifecycleObserver getLifecycleObserver() {
            return lifecycleObserver;
        }
    }


    public interface AppBackgroundListener {
        void onAppBackgrounded();
    }

    public boolean isSystemLanguageArabic() {
        Locale systemLocale = Resources.getSystem().getConfiguration().locale;
        Log.e("LangCheck", "System language: " + systemLocale.getLanguage());
        return "ar".equalsIgnoreCase(systemLocale.getLanguage());
    }

    private void setNewLocale(Context context, String languageCode, String languageName) {
        SharedPref.putString(Constants.TAG_LANGUAGE_CODE, languageCode);
        LocaleManager.setNewLocale(context, languageCode, languageName);
        Log.i(TAG, "setNewLocale: " + languageCode + "" + languageName);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

//Added sep9 for livestreaming
public static String getXDeviceInfo() {
    XDeviceInfo deviceInfo = new XDeviceInfo(
            deviceID,
            "Android",
            Build.MODEL,
            Build.VERSION.RELEASE,
            BuildConfig.VERSION_NAME
    );

       /* XUserInfo userInfo = new XUserInfo(
                com.cashgate.chobi.addon.livestream.external.utils.GetSet.getUserId(),""
        );

        XCompleteInfo completeInfo = new XCompleteInfo(deviceInfo, userInfo);*/
    //Making it manual because of named cache issue in Gson
    return  "{" + "\"device_id\":\""+ deviceID + "\"," + "\"device_name\":\"" + deviceInfo.getDeviceName() + "\"," +
            "\"device_type\":\"" + deviceInfo.getDeviceType() + "\"," +
            "\"device_model\":\"" + deviceInfo.getDeviceModel() + "\"," +
            "\"device_os\": \"" + deviceInfo.getDeviceName() + "\"," +
            "\"app_version\":\"" + deviceInfo.getAppVersion() + "\"," +
            "\"ip\":\"" + deviceIP + "\"" + "}";


}

    // Add this at top inside App class:
    private static final List<Activity> activityStack = new CopyOnWriteArrayList<>();

    public static void register(Activity activity) {
        activityStack.add(activity);
    }

    public static void unregister(Activity activity) {
        activityStack.remove(activity);
    }

    public static void recreateAllActivities() {
        for (Activity act : activityStack) {
            if (act != null && !act.isFinishing()) {
                act.runOnUiThread(() -> {
                    try {
                        act.recreate();
                        act.overridePendingTransition(0,0);
                    } catch (Exception e) {
                        Intent i = act.getIntent();
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        act.finish();
                        act.startActivity(i);
                    }
                });
            }
        }
    }


}

