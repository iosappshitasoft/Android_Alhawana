package com.app.alhawanajd;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.app.Zego.Model.TokenResponse;
import com.app.Zego.internal.sdk.ZEGOSDKManager;
import com.app.Zego.internal.sdk.basic.ZEGOSDKCallBack;
import com.app.Zego.pkbattle.activity.SwipeLiveActivity;


import com.app.alhawanajd.home.eventbus.VideoZoomEvent;
import com.app.alhawanajd.profile.NewProfileFragment;
import com.app.alhawanajd.search.SearchActivity;
import com.app.camera.CameraLiveChooserActivity;
import com.app.external.MediaPlayerUtils;
import com.app.helper.LocaleManager;
import com.app.model.AppDefaultResponse;
import com.app.model.CommonResponse;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
//import com.app.hiliteapp.BuildConfig;
import com.app.DialogOverLayPermission;
import com.app.Zego.internal.ZEGOLiveStreamingManager;
import com.app.addon.livestream.PlayerActivity;
import com.app.addon.livestream.external.utils.StreamConstants;
import com.app.db.DBHelper;
import com.app.alhawanajd.home.CustomViewPager;
import com.app.alhawanajd.home.HomeFragment;
import com.app.alhawanajd.home.eventbus.FollowingSwipePrevent;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.alhawanajd.search.SearchFragment;
import com.app.helper.AppWebSocket;
import com.app.helper.NetworkReceiver;
import com.app.helper.OkayCancelCallback;
import com.app.helper.PermissionsUtils;
import com.app.helper.callback.CompositeOnNetworkChangedListener;
import com.app.helper.callback.FingerPrintCallBack;
import com.app.helper.callback.OnOkCancelClickListener;
import com.app.model.AddDeviceRequest;
import com.app.model.AdminMessageResponse;
import com.app.model.ChatResponse;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.model.ReferFriendResponse;
import com.app.model.SignInRequest;
import com.app.model.SignInResponse;
import com.app.addon.livestream.external.model.StreamDetails;
import com.app.model.event.FollowingScrollEvent;
import com.app.model.event.ForYouScrollEvent;
import com.app.model.event.GetProfileEvent;
import com.app.model.event.GetSearchEvent;
import com.app.utils.AdminData;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DevicePref;
import com.app.utils.Logging;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.api.CommonApi;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import com.app.Zego.activity.StartLiveActivity;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class MainActivity extends BaseFragmentActivity implements HomeFragment.OnFragmentInteractionListener/*implements OSSubscriptionObserver*/ {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final Logger log = LoggerFactory.getLogger(MainActivity.class);
    /*CoordinatorLayout*/ LinearLayout parentLay;
    public CustomViewPager viewPager;
    BottomNavigationView bottomNavigation;
    RelativeLayout splashLay;
    public ImageView menuAddStory, btnHome, btnSearch, btnChat, btnProfile;
    View bottomViewGroup;
    LinearLayout bottom_navigation_custom;

   // ProfileFragment profileFragment = new ProfileFragment();
    NewProfileFragment profileFragment = new NewProfileFragment();
    HomeFragment homeFragment = new HomeFragment();
    SearchFragment searchFragment = new SearchFragment();
    NotificationFragment notificationFragment = new NotificationFragment();
    public String[] permissionArray;
    private MenuItem prevMenuItem;
    private ApiInterface apiInterface;
    DialogProfile profileDialog;
    DialogProfileImage imageDialog;
    DialogFreeGems gemsDialog;
    DialogInterest interestDialog;
    private ActivityResultLauncher<String[]> galleryResultLauncher;
    boolean doubleBackToExit = false;

    public static Boolean opendialog = false;

    Handler onlineHandler = new Handler();
    int delay = 5000; //milliseconds
    private int previousPosition;
    DBHelper dbHelper;
    List<String> skuList = new ArrayList<>();
    private String from = null, scope = null;
    private BiometricPrompt.PromptInfo promptInfo;
    private ExecutorService executor;
    private BiometricPrompt biometricPrompt;
    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    Cipher defaultCipher;
    Cipher cipherNotInvalidated;
    private AppUtils appUtils;

    SignInRequest signUpRequest;

    Boolean isSettinsIn = false;
    Boolean isAutoStartIn = false;
    String oneSignalId = null;
    private ActivityResultLauncher<String[]> storagePermissionResult;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    DialogOverLayPermission dialogOverLayPermission;
    private static final int PHONE_STATE_PERMISSION_REQUEST_CODE = 100;

    public CompositeOnNetworkChangedListener compositeOnNetworkChangedListener = new CompositeOnNetworkChangedListener();

    Runnable onlineRunnable = new Runnable() {
        @Override
        public void run() {
            if (NetworkReceiver.isConnected()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(Constants.TAG_TYPE, Constants.TAG_UPDATE_LIVE);
                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                    jsonObject.put(Constants.TAG_TIMESTAMP, AppUtils.getCurrentUTCTime(getApplicationContext()));
                    AppWebSocket.getInstance(MainActivity.this).send(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            onlineHandler.postDelayed(onlineRunnable, delay);
        }
    };
    private static final int REQUEST_CALL_PHONE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make the status bar transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancelAll();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        dbHelper = DBHelper.getInstance(this);
        appUtils = new AppUtils(this);
        executor = Executors.newSingleThreadExecutor();

        updateWatchCount();
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_main);
        CommonApi.getAppDefaultDataForGift(MainActivity.this);

   /*     Window window = getWindow();
       // Tells Android: "I'll handle padding and bar styling myself"
        WindowCompat.setDecorFitsSystemWindows(window, false);

   // Set your custom color (use from colors.xml or hardcoded)
        int customColor = ContextCompat.getColor(this, R.color.colorBlack);

    // Apply custom color to status and navigation bars
        window.setStatusBarColor(customColor);
        window.setNavigationBarColor(customColor);

     // Ensure icons on status/nav bars are white (dark background)
        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(window, window.getDecorView());
        insetsController.setAppearanceLightStatusBars(false);
        insetsController.setAppearanceLightNavigationBars(false);
*/
        findViews();

        checkUserIsActive();
        initPermissions();
        registerNetworkReceiver();
        if (getIntent().hasExtra(Constants.NOTIFICATION)) {
            from = getIntent().getStringExtra(Constants.NOTIFICATION);
            Log.d(TAG, "onCreate11111111111111111: " + from);
            if (getIntent().hasExtra(Constants.TAG_SCOPE)) {
                scope = getIntent().getStringExtra(Constants.TAG_SCOPE);
            }
            if (getIntent().getBooleanExtra(Constants.TAG_FROM_FOREGROUND, false)) {
                checkFromNotification();
            } else {
                if (App.isAppOpened) {
                    checkFromNotification();
                } else {
                    if (SharedPref.getBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false) && appUtils.checkIsDeviceEnabled(this)) {
                        splashLay.setVisibility(View.VISIBLE);
                        bottomNavigation.setVisibility(View.INVISIBLE);
                        checkFingerPrintEnabled();
                    } else {
                        SharedPref.putBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false);
                        checkFromNotification();
                    }
                }
            }
            if (from.equals("gift")) {
                viewPager.setCurrentItem(3);
            }
        }
        else if (getIntent().hasExtra(Constants.TAG_FROM)) {
            Log.d(TAG, "onCreate: from  push" + getIntent().getStringExtra(Constants.TAG_FROM));
            if (getIntent().getStringExtra(Constants.TAG_FROM).equals(Constants.TAG_LIVE)) {
                getStreamDetails(getIntent().getStringExtra(StreamConstants.TAG_STREAM_NAME));
            }
            if (getIntent().getStringExtra(Constants.TAG_FROM).equals(Constants.TAG_PROFILE)) {
                bottomNavigation.setSelectedItemId(R.id.menuProfile);
            }
            if (getIntent().getStringExtra(Constants.TAG_FROM).equals(Constants.TAG_VIDEO)) {
//                getStreamDetails(getIntent().getStringExtra(StreamConstants.TAG_STREAM_NAME));
            }
        }

        /*setupViewPager();
        setupBottomNavigation();*/

        //OCT 14
        handleShareIntent(getIntent());

        sendPing();

        if (getIntent().getStringExtra("skipvalue") != null && getIntent().getStringExtra("skipvalue").equals("true")) {
            setupViewPager();
            setupBottomNavigation();
        } else {
            if (GetSet.getName() == null) {
                showProfileDialog();
            } else {
                setupViewPager();
                setupBottomNavigation();
                getUserProfile(GetSet.getUserId());
            }
        }

        getAppDefaultData();
        signInZEGOSDK(GetSet.getUserId(), GetSet.getUserName(), (errorCode, message) -> {
            Log.e(TAG, "Error code : " + errorCode + "," + message);
            if (errorCode == 0) {
            }
        });

        getstreamToken();

        Log.d(TAG, "onCreate: TAG_IS_FROM_LINK "+ getIntent().hasExtra(Constants.TAG_IS_FROM_LINK) +" ttt "+ getIntent().getStringExtra(Constants.TAG_IS_FROM_LINK));
        if (getIntent().hasExtra(Constants.TAG_IS_FROM_LINK)){
            if (getIntent().getStringExtra(Constants.TAG_IS_FROM_LINK).equals(Constants.TAG_TRUE)){
                if (getIntent().getStringExtra(Constants.TAG_FROM).equals(Constants.TAG_VIDEO)) {
                    Intent intent = new Intent(this, SingleVideoActivity.class);
                    intent.putExtra(Constants.TAG_VIDEO_ID, getIntent().getStringExtra(StreamConstants.TAG_STREAM_NAME));
//                intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, listNotification.get(getAbsoluteAdapterPosition()).getVideoThumbnail());
                    intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, "");
                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                    startActivityForResult(intent, Constants.NOTIFICATION_ACTIVITY);
                }
            }
        }

        ColorStateList navColor = ContextCompat.getColorStateList(this, R.color.nav_item_color);
        bottomNavigation.setItemTextColor(navColor);

        // In your Activity or Fragment
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigation.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
            if (bottomNavigation.getMenu().getItem(i).getItemId() == R.id.menuLiveStream) {
                TextView textView = itemView.findViewById(com.google.android.material.R.id.navigation_bar_item_small_label_view);
                textView.setPadding(textView.getPaddingLeft(), 50, textView.getPaddingRight(), textView.getPaddingBottom()); // Increase top padding
            }
        }

    }

    public void getAppDefaultData() {
        ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
        if (NetworkReceiver.isConnected()) {
            mExecutorService.execute(new Runnable() {
                public void run() {
                    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                    String lang = LocaleManager.getLanguageCode(MainActivity.this);
                    Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID,lang);
                    call.enqueue(new Callback<AppDefaultResponse>() {
                        @Override
                        public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                            if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                AppDefaultResponse defaultData = response.body();
//                                Timber.d("getAppDefaultData: %s", App.getGsonPrettyInstance().toJson(defaultData));
                                AdminData.resetData();
                                AdminData.freeGems = defaultData.getFreeGems();
                                AdminData.giftList = defaultData.getGifts();
                                AdminData.giftsDetails = defaultData.getGiftsDetails();
                                AdminData.primeDetails = defaultData.getPrimeDetails();

//                                GetSet.setPrimeBenefit(defaultData.getPrimeDetails());
                                AdminData.reportList = defaultData.getReports();
                                Log.d(TAG, "onResponse_Report: "+AdminData.reportList);
                                /*AdminData.userReportList = defaultData.getUserReports();*/
                                AdminData.userReportList = defaultData.getUserReportsNew();
                                /*Add first item as Select all location filter*/
                                /*AdminData.locationList = new ArrayList<>();
                                AdminData.locationList.add(getString(R.string.select_all));
                                AdminData.locationList.addAll(defaultData.getLocations());*/
                                AdminData.membershipList = defaultData.getMembershipPackages();
                                AdminData.filterGems = defaultData.getFilterGems();
                                AdminData.filterOptions = defaultData.getFilterOptions();
                                AdminData.inviteCredits = defaultData.getInviteCredits();
                                AdminData.showAds = defaultData.getShowAds();
                                AdminData.showVideoAd = defaultData.getVideoAds();
                                AdminData.welcomeMessage = defaultData.getWelcomeMessage();
                                AdminData.videoAdsDuration = defaultData.getVideoAdsDuration();
                                AdminData.postAuthorizeMessage = defaultData.getPostAuthorizeMessage();
                                AdminData.galleryUpload = defaultData.isGalleryUpload();

                                Log.d("", "onResponse:13" + defaultData.isGalleryUpload());
                                AdminData.downloadVideo = defaultData.isDownloadVideo();
                                AdminData.uploadSound = defaultData.isUploadSound();
                                AdminData.contactEmail = defaultData.getContactEmail();
                                AdminData.showMoneyConversion = defaultData.getShowMoneyConversion();
                                AdminData.audioCategory = defaultData.getAudioCategory();
                                AdminData.max_sound_duration = defaultData.getMaxSoundDuration();
                                AdminData.max_video_duration = defaultData.getMaxVideoDuration();
                                AdminData.arabic_ad_key = defaultData.isArabic_ad_key();
                                AdminData.english_ad_key = defaultData.isEnglish_ad_key();
                                AdminData.ad_status = defaultData.isad_status();

                                GetSet.setMax_sound_duration(defaultData.getMaxSoundDuration());
                                // GetSet.setMax_video_duration(defaultData.getMaxVideoDuration());

                                if (AdminData.membershipList != null && AdminData.membershipList.size() > 0) {
                                    SharedPref.putString(SharedPref.DEFAULT_SUBS_SKU, AdminData.membershipList.get(0).getSubsTitle());
                                } else {
                                    SharedPref.putString(SharedPref.DEFAULT_SUBS_SKU, Constants.DEFAULT_SUBS_SKU);
                                }

                                //Addon Live Stream
                                if (Constants.ADDON_LIVE_STREAM) {
                                    AdminData.streamDetails = defaultData.getStreamConnectionInfo();
                                    GetSet.setStreamBaseUrl(defaultData.getStreamConnectionInfo().getStreamBaseUrl());
                                    SharedPref.putString(SharedPref.STREAM_BASE_URL, defaultData.getStreamConnectionInfo().getStreamBaseUrl());
                                    SharedPref.putString(SharedPref.STREAM_WEBSOCKET_URL, defaultData.getStreamConnectionInfo().getWebSocketUrl());
                                    SharedPref.putString(SharedPref.STREAM_VOD_URL, defaultData.getStreamConnectionInfo().getStreamVodUrl());
                                    SharedPref.putString(SharedPref.STREAM_API_URL, defaultData.getStreamConnectionInfo().getStreamApiUrl());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                            call.cancel();
                        }
                    });
                }
            });
        }
    }

    private void signInZEGOSDK(String userID, String userName, ZEGOSDKCallBack callback) {
        ZEGOSDKManager.getInstance().connectUser(userID, userName, callback);
    }

    private void getstreamToken() {
        Call<TokenResponse> call = apiInterface.getStreamToken(GetSet.getUserId());
        call.enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                TokenResponse tokenResponse = response.body();
                if (response.isSuccessful()) {
                    Log.e(TAG, "streamToken : " + new Gson().toJson(response.body()));

                    GetSet.setStreamToken(tokenResponse.getStream_token());
                    // com.app.numediatech.addon.livestream.external.utils.GetSet.setStreamToken(tokenResponse.getStream_token());
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {

            }
        });
    }

    public void enablePostNotification() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        android.Manifest.permission.POST_NOTIFICATIONS, READ_PHONE_STATE}, 103);
            }
        }
    }

    private void findViews() {
        parentLay = findViewById(R.id.parentLay);
        viewPager = findViewById(R.id.fragment_home_viewpager);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomViewGroup = findViewById(R.id.constraintLayout);
        splashLay = findViewById(R.id.splashLay);
        menuAddStory = findViewById(R.id.menuAddStory);
        bottom_navigation_custom = findViewById(R.id.bottom_navigation_custom);
        btnHome = findViewById(R.id.btnHome);
        btnSearch = findViewById(R.id.btnSearch);
        btnChat = findViewById(R.id.btnChat);
        btnProfile = findViewById(R.id.btnProfile);

        // Remove the global tint first
        bottomNavigation.setItemIconTintList(null);




        galleryResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {
                    if (homeFragment != null && homeFragment.homeForYou != null)
                        homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
                    Intent newIntent = new Intent(MainActivity.this, CameraActivity.class);
                    startActivityForResult(newIntent, Constants.MAIN_ACTIVITY_TO_CAMERA__REQUEST_CODE);
                    animateZoom(MainActivity.this);
                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, x.getKey())) {
                                galleryResultLauncher.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                openPermissionsDialog(MainActivity.this, new OkayCancelCallback() {
                                    @Override
                                    public void onOkayClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, MainActivity.this.getString(R.string.need_video_permission_to_access));
                                Log.d(TAG, "onActivityResult: permisstion check2222");
                            }
                            break;
                        }
                    }
                }

            }
        });


    }

    public static void openPermissionsDialog(Context mContext, OkayCancelCallback callback, String description) {
        new AlertDialog.Builder(mContext)
                .setMessage(description)
                .setPositiveButton(mContext.getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onOkayClicked("");
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(mContext.getString(R.string.cancel_txt), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onCancelClicked("");
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: requestcode "+requestCode );

        if (requestCode == PHONE_STATE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with getting the phone state
//                getPhoneState();
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 100) {
            if (checkPermissions(permissions, MainActivity.this)) {
                //  ImagePicker.pickImage(this, getString(R.string.select_your_image));
            } else {
                if (shouldShowRationale(permissions, MainActivity.this)) {
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, 100);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 100);
                }
            }
        } else if (requestCode == 103) {
            if (checkPermissions(permissions, MainActivity.this)) {
                //  ImagePicker.pickImage(this, getString(R.string.select_your_image));
            } else {
                if (shouldShowRationale(permissions, MainActivity.this)) {
//                    ActivityCompat.requestPermissions(MainActivity.this, permissions, 103);
                    showSettingsPermissionDialog(getString(R.string.need_permission_to_notify_access));
                } else {
                    showSettingsPermissionDialog(getString(R.string.need_permission_to_notify_access));
//                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_notify_access), Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                    Uri uri = Uri.fromParts("package", getPackageName(), null);
//                    intent.setData(uri);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    startActivityForResult(intent, 103);
                }
            }
        }
        switch (requestCode) {
            case 11:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }

    private boolean checkPermissions(String[] permissionList, MainActivity activity) {
        boolean isPermissionsGranted = false;
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                isPermissionsGranted = true;
            } else {
                isPermissionsGranted = false;
                break;
            }
        }
        return isPermissionsGranted;
    }

    private boolean shouldShowRationale(String[] permissions, MainActivity activity) {
        boolean showRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showRationale = true;
            } else {
                showRationale = false;
                break;
            }
        }
        return showRationale;
    }


    private void checkFromNotification() {
        App.isAppOpened = true;
        splashLay.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);
        Log.d(TAG, "checkFromNotification: " + from);
        switch (from) {
            case Constants.TAG_FOLLOW: {
                Intent profileIntent = new Intent(getApplicationContext(), OthersProfileActivity.class);
                profileIntent.putExtra(Constants.TAG_USER_ID, getIntent().getStringExtra(Constants.TAG_PARTNER_ID));
                profileIntent.putExtra(Constants.TAG_USER_IMAGE, getIntent().getStringExtra(Constants.TAG_PARTNER_IMAGE));
                profileIntent.putExtra(Constants.TAG_FROM, Constants.NOTIFICATION);
                startActivity(profileIntent);
                break;
            }
            case Constants.TAG_PROFILE: {
                Intent profileIntent = new Intent(getApplicationContext(), ChatActivity.class);
                profileIntent.putExtra(Constants.TAG_PARTNER_ID, getIntent().getStringExtra(Constants.TAG_PARTNER_ID));
                profileIntent.putExtra(Constants.TAG_PARTNER_NAME, getIntent().getStringExtra(Constants.TAG_PARTNER_NAME));
                profileIntent.putExtra(Constants.TAG_PARTNER_IMAGE, getIntent().getStringExtra(Constants.TAG_PARTNER_IMAGE));
                startActivity(profileIntent);
                break;
            }
            case Constants.TAG_ADMIN: {
                Intent profileIntent = new Intent(getApplicationContext(), ChatActivity.class);
                ChatResponse adminChat = dbHelper.getAdminChat(GetSet.getUserId());
                profileIntent.putExtra(Constants.TAG_FROM, Constants.TAG_ADMIN);
                profileIntent.putExtra(Constants.TAG_PARTNER_NAME, adminChat.getUserName());
                startActivity(profileIntent);
                break;
            }
            case Constants.TAG_NOTIFICATION_PAGE:
                if (scope != null) {
                    final Pattern mainLaunchPattern = Pattern.compile("like|gift|comment|mention|follow");
                    if (scope.matches(mainLaunchPattern.pattern())) {
//                         bottomNavigation.setSelectedItemId(R.id.menuChat);
                    } else if (scope.equals(Constants.TAG_FOLLOWER_ON_LIVE)) {
                        Intent intent = new Intent(this, SingleVideoActivity.class);
                        intent.putExtra(Constants.TAG_VIDEO_ID, getIntent().getStringExtra(Constants.TAG_VIDEO_ID));
                        intent.putExtra(Constants.TAG_NAME, getIntent().getStringExtra(Constants.TAG_NAME));
                        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, "");
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                        startActivity(intent);
                    }
                    else if (scope.equals("followerpostvideo")) {
                        Log.d(TAG, "checkFromNotification: "+getIntent().getStringExtra(Constants.TAG_VIDEO_ID)+"  "+getIntent().getStringExtra(Constants.TAG_STREAM_NAME));
                        Intent intent = new Intent(this, SingleVideoActivity.class);
                        intent.putExtra(Constants.TAG_VIDEO_ID, getIntent().getStringExtra(Constants.TAG_VIDEO_ID)!=null?getIntent().getStringExtra(Constants.TAG_VIDEO_ID):getIntent().getStringExtra(Constants.TAG_STREAM_NAME));
                        intent.putExtra(Constants.TAG_NAME, getIntent().getStringExtra(Constants.TAG_NAME));
                        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, "");
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                        startActivity(intent);
                    }
                }
                //startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                break;
           /* case Constants.TAG_LIVE: {
                Intent intent = new Intent(this, StreamListActivity.class);
                intent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                intent.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                intent.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                intent.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                startActivity(intent);
                break;
            }*/
            case Constants.TAG_LIVE: {
                getStreamDetails(getIntent().getStringExtra(StreamConstants.TAG_STREAM_NAME));
                break;
            }
        }
    }

    private void checkUserIsActive() {
        Call<HashMap<String, String>> call = apiInterface.isActive(GetSet.getUserId());
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<HashMap<String, String>> call, @NotNull Response<HashMap<String, String>> response) {
                HashMap<String, String> Response = response.body();

                Timber.i("checkUserIsActive=> %s", new Gson().toJson(response.body()));

                if (response.isSuccessful()) {
                    if (Objects.requireNonNull(Response.get(Constants.TAG_STATUS)).equals(Constants.TAG_FALSE)) {

                        App.makeToast(getString(R.string.account_deactivated_by_admin));
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(MainActivity.this).clearDB();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }

    private void login() {
        GetSet.reset();
        SharedPref.clearAll();
        DBHelper.getInstance(MainActivity.this).clearDB();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        Timber.i("onNetworkChanged isConnected %s", isConnected);

        compositeOnNetworkChangedListener.onNetworkChanged(isConnected);

        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
        CommonApi.getAppDefaultData(MainActivity.this);
        if (isConnected && GetSet.isIsLogged()) {
            AppWebSocket.mInstance = null;
            AppWebSocket.getInstance(MainActivity.this);
            onlineHandler.post(onlineRunnable);

        } else {
            onlineHandler.removeCallbacks(onlineRunnable);
            AppWebSocket.getInstance(this).disconnect();
        }

    }

    private void sendPing() {
        onlineHandler.post(onlineRunnable);
    }

    private void removePing() {
        onlineHandler.removeCallbacks(onlineRunnable);
    }

    public void onClickAddStory(View view) {

        if (Constants.Login_method.equals("skip")) {
            login();
        } else {
            uploadvideo();
        }

    }

    public void uploadvideo() {
        Log.e(TAG,"Audicategory : " + AdminData.audioCategory.size());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
//                    ContextCompat.checkSelfPermission(getApplicationContext(), MICROPHONE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {

                storagePermissionResult.launch(new String[]{/*MICROPHONE,*/CAMERA, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, RECORD_AUDIO, READ_MEDIA_AUDIO});

            } else {

                if (homeFragment != null && homeFragment.homeForYou != null)
                    homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
                Intent newIntent = new Intent(MainActivity.this, CameraLiveChooserActivity.class);
                startActivityForResult(newIntent, Constants.MAIN_ACTIVITY_TO_CAMERA__REQUEST_CODE);
                animateZoom(MainActivity.this);

            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{CAMERA, RECORD_AUDIO}, 100);
            } else {
                if (homeFragment != null && homeFragment.homeForYou != null)
                    homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
                Intent newIntent = new Intent(MainActivity.this, CameraLiveChooserActivity.class);
                startActivityForResult(newIntent, Constants.MAIN_ACTIVITY_TO_CAMERA__REQUEST_CODE);
                animateZoom(MainActivity.this);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{CAMERA, RECORD_AUDIO}, 100);
            } else {
                if (homeFragment != null && homeFragment.homeForYou != null)
                    homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
                Intent newIntent = new Intent(MainActivity.this, CameraLiveChooserActivity.class);
                startActivityForResult(newIntent, Constants.MAIN_ACTIVITY_TO_CAMERA__REQUEST_CODE);
                animateZoom(MainActivity.this);
            }
        } else {
            if (ContextCompat.checkSelfPermission(MainActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{CAMERA, RECORD_AUDIO}, 100);
            } else {
                if (homeFragment != null && homeFragment.homeForYou != null)
                    homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
                Intent newIntent = new Intent(MainActivity.this, CameraLiveChooserActivity.class);
                startActivityForResult(newIntent, Constants.MAIN_ACTIVITY_TO_CAMERA__REQUEST_CODE);
                animateZoom(MainActivity.this);
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VideoZoomEvent event) {
        //   Timber.d("onMessageEvent zoomscale: %s", (event.getCurrentZoomScale()));
        // if (event.getCurrentZoomScale() >1){
        if (event.currentZoomScale >1){
            bottomViewGroup.setVisibility(View.GONE);
        }
        else {
            bottomViewGroup.setVisibility(View.VISIBLE);
        }

    }

    public static void animateZoom(Context context) {
        ((Activity) context).overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_zoom_out);
    }

    private void requestCallPermission() {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            // Request permission
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PHONE_PERMISSION
            );
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Bundle extras = intent.getExtras();
        viewPager.setCurrentItem(2, false);

        //oct 14
        handleShareIntent(intent);
    }

    //oct14
    private void handleShareIntent(Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        String type = intent.getType();

        // Only handle if the intent is SEND or SEND_MULTIPLE
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("text/")) {
                // Shared text (like links or messages)
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    CommonApi.openChatWithSharedContent(this,"text", sharedText, null);
                }
            } else if (type.startsWith("image/")) {
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    CommonApi.openChatWithSharedContent(this,"image", null, imageUri);
                }
            } else if (type.startsWith("video/")) {
                Uri videoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (videoUri != null) {
                    CommonApi.openChatWithSharedContent(this,"video", null, videoUri);
                }
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (uris != null && !uris.isEmpty()) {
                CommonApi.openChatWithMultipleContent(this, type, uris);
            }
        } else {
            // Not a share intent → normal app launch, do nothing
        }
    }


    @Override
    public void onFragmentEvent() {
        EventBus.getDefault().post(new FollowingSwipePrevent(true));
        // viewPager.setCurrentItem(2, false); //myself commented on oct 8
        if(GetSet.isLogged()) {
            Intent intent = new Intent(this, SearchFragmentActivity.class);
            intent.putExtra(Constants.TAG_FROM, Constants.TAG_BROADCAST);
            startActivity(intent);
        }
    }

    public void checkFingerPrintEnabled() {
        if (SharedPref.getBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false) && appUtils.checkIsDeviceEnabled(this)) {
            try {
                mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            } catch (KeyStoreException e) {
                throw new RuntimeException("Failed to get an instance of KeyStore", e);
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mKeyGenerator = KeyGenerator
                            .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                }
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                    cipherNotInvalidated = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                }
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException("Failed to get an instance of Cipher", e);
            }

            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (BiometricManager.from(this).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    createKey(Constants.DEFAULT_KEY_NAME, true);
                    createKey(Constants.KEY_NAME_NOT_INVALIDATED, false);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    openBioMetricDialog();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    openFingerPrintDialog();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);

                if (fingerprintManager != null && fingerprintManager.hasEnrolledFingerprints()) {
                    createKey(Constants.DEFAULT_KEY_NAME, true);
                    createKey(Constants.KEY_NAME_NOT_INVALIDATED, false);
                    openFingerPrintDialog();
                } else if (km != null && km.isKeyguardSecure()) {
                    Intent i = km.createConfirmDeviceCredentialIntent(getString(R.string.authentication_required), getString(R.string.password));
                    startActivityForResult(i, Constants.DEVICE_LOCK_REQUEST_CODE);
                } else {
                    checkFromNotification();
                }
            }
        } else {
            SharedPref.putBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false);
            checkFromNotification();
        }
    }

    private void openBioMetricDialog() {
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.i(TAG, "onAuthenticationError: " + errorCode);
                if (errorCode == 7) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            App.makeToast(getString(R.string.too_many_attempts));
                            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                            if (km != null && km.isKeyguardSecure()) {
                                Intent i = km.createConfirmDeviceCredentialIntent(getString(R.string.authentication_required), getString(R.string.password));
                                startActivityForResult(i, Constants.DEVICE_LOCK_REQUEST_CODE);
                            }
                        }
                    });
                } else if (errorCode == 13) {
                    Logging.i(TAG, "onAuthenticationError: " + "Cancel");
                }
                finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkFromNotification();
                    }
                });
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.security))
                .setSubtitle("")
                .setDescription(getString(R.string.touch_fingerprint_description))
                .setNegativeButtonText(getString(R.string.cancel_txt))
                .build();
        biometricPrompt.authenticate(promptInfo);


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openFingerPrintDialog() {
        FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);
        if (fingerprintManager != null && !fingerprintManager.hasEnrolledFingerprints()) {
            checkFromNotification();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                initCipher(defaultCipher, Constants.DEFAULT_KEY_NAME);
            }
            DialogFingerPrint dialogFingerPrint = new DialogFingerPrint();
            dialogFingerPrint.setContext(MainActivity.this);
            dialogFingerPrint.setCallBack(new FingerPrintCallBack() {
                @Override
                public void onPurchased(boolean withFingerprint, @Nullable FingerprintManager.CryptoObject cryptoObject) {
                    if (withFingerprint) {
                        // If the user has authenticated with fingerprint, verify that using cryptography and
                        // then show the confirmation message.
                        assert cryptoObject != null;
                        tryEncrypt(cryptoObject.getCipher());
                    } else {
                        // Authentication happened with backup password. Just show the confirmation message.

                    }
                }

                @Override
                public void onError(String errorMsg) {
                    App.makeToast(errorMsg);
                }
            });
            dialogFingerPrint.setCryptoObject(new FingerprintManager.CryptoObject(defaultCipher));
            dialogFingerPrint.show(getSupportFragmentManager(), TAG);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean initCipher(Cipher cipher, String keyName) {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(keyName, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                 | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     *
     * @param keyName                          the name of the key to be created
     * @param invalidatedByBiometricEnrollment if {@code false} is passed, the created key will not
     *                                         be invalidated even if a new fingerprint is enrolled.
     *                                         The default value is {@code true}, so passing
     *                                         {@code true} doesn't change the behavior
     *                                         (the key will be invalidated if a new fingerprint is
     *                                         enrolled.). Note that this parameter is only valid if
     *                                         the app works on Android N developer preview.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }
            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                 | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Tries to encrypt some data with the generated key in {@link #createKey} which is
     * only works if the user has just authenticated via fingerprint.
     */
    private void tryEncrypt(Cipher cipher) {
        try {
            byte[] encrypted = cipher.doFinal(Constants.SECRET_MESSAGE.getBytes());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkFromNotification();
                }
            });
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            App.makeToast("Failed to encrypt the data with the generated key. " + "Retry the purchase");
            Log.e(TAG, "Failed to encrypt the data with the generated key." + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.DEVICE_LOCK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                checkFromNotification();
            } else {
                App.makeToast(getString(R.string.unable_to_verify));
                finish();
            }
        } else if (requestCode == Constants.MAIN_ACTIVITY_TO_CAMERA__REQUEST_CODE) {
            //Timber.d("onActivityResult: %s" , HomeForYou home = new HomeForYou().viewPager.getCurrentItem());
        } else {
            if (requestCode == Constants.OVERLAY_REQUEST_CODE) {

                if (!checkOverLayPermission()) {
                    openPermissionDialog();
                } else {
                    opendialog = false;
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
//        enablePostNotification();

        // Resume only if we are on Home tab
        if (viewPager != null && viewPager.getCurrentItem() == 0) {
            resumeVideoPlayed();
        }

        if (checkOverLayPermission()) {
            if (Build.BRAND.equalsIgnoreCase("TECNO")) {
//                enableProtectesApps();
            }
        }

        if (isSettinsIn) {
            if (Build.BRAND.equalsIgnoreCase("TECNO")) {
//                enableAutoStart();
            }
        }
        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }


        if (checkOverLayPermission()) {
            if (Build.BRAND.equalsIgnoreCase("TECNO")) {
//                enableProtectesApps();
            }
        }
        if (!checkOverLayPermission()) {
            openPermissionDialog();
        }
        if (checkOverLayPermission()) {
            if (Build.BRAND.equalsIgnoreCase("TECNO")) {
//                enableProtectesApps();
            }
        } else {
            opendialog = false;
        }


    }

    private void pauseVideoPlayed() {

        try{
            appUtils.pauseExternalAudio();
            if (MediaPlayerUtils.isPlaying()) {
                MediaPlayerUtils.pauseMediaPlayer();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if (homeFragment != null) {
            if (homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 0) {
                if (homeFragment.homeForYou != null)
                    homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
            } else if (homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 1) {
                if (homeFragment.homeFollowing != null)
                    homeFragment.homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(false);
            }
        }
    }

    private void resumeVideoPlayed() {
        if (!Constants.overLaysDialogShown) {
            if (homeFragment != null) {
                if (homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 0) {
                    if (homeFragment.homeForYou != null)
                        homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(true);
                } else if (homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 1) {
                    if (homeFragment.homeFollowing != null)
                        homeFragment.homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(true);
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        enablePostNotification();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private boolean checkOverLayPermission() {
        //Android M Or Over
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isOverLayEnabled = Settings.canDrawOverlays(this);
            Log.d(TAG, "checkOverLayPermission: " + isOverLayEnabled);
            SharedPref.putBoolean(SharedPref.POP_UP_WINDOW_PERMISSION, !isOverLayEnabled);
            return isOverLayEnabled;
        }
        return true;
    }

    private void enableAutoStart() {
        Log.d(TAG, "enableAutoStart: " + Build.BRAND);
        pref = getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        isAutoStartIn = pref.getBoolean("isAutoStartIn", false);
        if (!isAutoStartIn) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            ViewGroup viewGroup = findViewById(android.R.id.content);
            View dialogView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_overlay_permission_autostart, viewGroup, false);
            builder.setView(dialogView);
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));
            Button allowButton = dialogView.findViewById(R.id.btnAllow);
            Button denyButton = dialogView.findViewById(R.id.btnDeny);
            allowButton.setOnClickListener(v -> {
                editor.putBoolean("isAutoStartIn", true);
                editor.apply();
                Intent intent = new Intent();
                intent.setClassName("com.transsion.phonemaster",
                        "com.cyin.himgr.widget.activity.MainActivity");
                startActivity(intent);
                alertDialog.dismiss();
            });
            denyButton.setOnClickListener(v -> {
                alertDialog.dismiss();
            });
            alertDialog.show();
        }
    }


    private void enableProtectesApps() {
        Log.d(TAG, "enableAutoStart: " + Build.BRAND);
        SharedPreferences pref = getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        isSettinsIn = pref.getBoolean("isSettingsIn", false);
        if (!isSettinsIn) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            ViewGroup viewGroup = findViewById(android.R.id.content);
            View dialogView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_overlay_permission_call, viewGroup, false);
            builder.setView(dialogView);
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));
            Button allowButton = dialogView.findViewById(R.id.btnAllow);
            Button denyButton = dialogView.findViewById(R.id.btnDeny);
            allowButton.setOnClickListener(v -> {
                editor.putBoolean("isSettingsIn", true);
                editor.apply();
                Intent intent = new Intent();
                intent.setClassName("com.transsion.phonemaster",
                        "com.cyin.himgr.widget.activity.MainSettingGpActivity");
                startActivity(intent);
                alertDialog.dismiss();
            });
            denyButton.setOnClickListener(v -> {
                alertDialog.dismiss();
            });
            alertDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        AppUtils.resetFilter();
        removePing();
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("onPause: ");

        // Pause videos whenever MainActivity is not in foreground
        pauseVideoPlayed();
    }

    private void showProfileDialog() {
        Log.d(TAG, "showProfileDialog11111111111: ");

        profileDialog = new DialogProfile();
        profileDialog.setContext(MainActivity.this);

        profileDialog.setCallBack((object, imageBase64) -> {
            Log.d(TAG, "sssss: ");
            if (NetworkReceiver.isConnected()) {
                signUpRequest = (SignInRequest) object;
                signUpRequest.setEmail(Constants.Mailid);
//                signUpRequest.setImage(imageBase64); // base64 string
                InputStream inputStream = AppUtils.convertBase64ToInputStream(imageBase64);
                signUp(signUpRequest, inputStream);
            }
        });

        profileDialog.setCancelable(false);
        profileDialog.show(getSupportFragmentManager(), TAG);
    }

    private void signUp(SignInRequest request, InputStream imageStream) {
        Timber.d("onResponse: signup %s", App.getGsonPrettyInstance().toJson(request));
        Call<SignInResponse> call = apiInterface.callSignUp(request);
        call.enqueue(new Callback<SignInResponse>() {
            @Override
            public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                SignInResponse signin = response.body();
                Log.d(TAG, "onResponse_response: " + response);

                if (response.isSuccessful() && signin != null && Constants.TAG_TRUE.equals(signin.getStatus())) {
                    Log.d(TAG, "onResponse: email.." + signin.getEmail());

                    String lang = LocaleManager.getLanguageCode(getApplicationContext());
                    String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                    if(lang.equalsIgnoreCase("en")){
                        changeLangugae("English", deviceId, getApplicationContext(), lang, lang);
                    }else{
                        changeLangugae("Arabic", deviceId,  getApplicationContext(), lang, lang);
                    }

                    GetSet.setUserId(signin.getUserId());
                    GetSet.setLoginId(signin.getLoginId());
                    GetSet.setName(signin.getName());
                    GetSet.setUserName(signin.getUserName());
                    GetSet.setDob(signin.getDob());
                    GetSet.setAge(signin.getAge());
                    GetSet.setEmail(signin.getEmail());
                    GetSet.setGender(signin.getGender());
                    GetSet.setAuthToken(signin.getAuthToken());
                    GetSet.setFollowersCount("0");
                    GetSet.setFollowingCount("0");
                    GetSet.setGems(0F);
                    GetSet.setGifts(0L);
                    GetSet.setPremiumMember(Constants.TAG_FALSE);
                    GetSet.setPremiumExpiry("");
                    GetSet.setPrivacyAge(signin.getPrivacyAge());
                    GetSet.setPrivacyContactMe(signin.getPrivacyContactMe());
                    GetSet.setFollowNotification(signin.getFollowNotification());
                    GetSet.setChatNotification(signin.getChatNotification());
                    GetSet.setShowNotification(signin.getShowNotification());

                    SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                    SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());
                    SharedPref.putString(SharedPref.NAME, GetSet.getName());
                    SharedPref.putString(SharedPref.USER_NAME, GetSet.getUserName());
                    SharedPref.putString(SharedPref.DOB, GetSet.getDob());
                    SharedPref.putString(SharedPref.AGE, GetSet.getAge());
                    SharedPref.putString(SharedPref.GENDER, GetSet.getGender());
                    SharedPref.putString(SharedPref.AUTH_TOKEN, GetSet.getAuthToken());
                    SharedPref.putString(SharedPref.FOLLOWERS_COUNT, GetSet.getFollowersCount());
                    SharedPref.putString(SharedPref.FOLLOWINGS_COUNT, GetSet.getFollowingCount());
                    SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                    SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                    SharedPref.putLong(SharedPref.VIDEOS, GetSet.getVideos());
                    SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                    SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                    SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                    SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                    SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                    SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                    SharedPref.putString(SharedPref.SHOW_NOTIFICATION, GetSet.getShowNotification());

                    apiInterface = ApiClient.getClient().create(ApiInterface.class);

                    if (!TextUtils.isEmpty(SharedPref.getString(SharedPref.REFERAL_CODE, ""))) {
                        updateGems(SharedPref.getString(SharedPref.REFERAL_CODE, ""));
                    }

                    AppUtils.callerList.clear();
                    AppUtils.callerList.add(GetSet.getUserId());
                    addAdminMessages();

                    if (imageStream == null) {
                        addDeviceID();
                    } else {

                    }

                    try {
                        if (imageStream != null) {
                            uploadImage(AppUtils.getBytes(imageStream));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    addDeviceID();
                } else {
                    profileDialog.hideLoading();

                    if (signin != null) {
                        App.makeToast(signin.getMessage());
                    } else {
                        App.makeToast("Something went wrong. Please try again.");
                        Log.e(TAG, "Signup failed: response body is null");
                        try {
                            if (response.errorBody() != null) {
                                Log.e(TAG, "Raw Error: " + response.errorBody().string());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SignInResponse> call, Throwable t) {
                call.cancel();
                profileDialog.hideLoading();
                Log.e(TAG, "API failure: " + t.getMessage(), t);
            }
        });
    }

    public void getStreamDetails(String streamName) {

        ApiInterface apiInterfacelive;
        apiInterfacelive = ApiClient.getClient().create(ApiInterface.class);
        Call<StreamDetails> call3 = apiInterfacelive.getStreamDetails(SharedPref.getString(SharedPref.AUTH_TOKEN, ""), SharedPref.getString(SharedPref.USER_ID, ""), streamName);
        call3.enqueue(new Callback<StreamDetails>() {
            @Override
            public void onResponse(Call<StreamDetails> call, Response<StreamDetails> response) {
                if (response.isSuccessful()) {

                    if (response.body().getStatus().equals(com.app.addon.livestream.external.utils.Constants.TAG_TRUE)) {
                        StreamDetails streamData = response.body();
                        Log.e(TAG,"StreamDetails : " + new Gson().toJson(streamData));
                        if (!streamData.getType().equals(com.app.addon.livestream.external.utils.Constants.TAG_LIVE)) {
                            appUtils.pauseExternalAudio();
                            Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                            intent.putExtra("stream_data", streamData);
                            intent.putExtra("from", Constants.TAG_HOME);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {

                            try{
                                appUtils.pauseExternalAudio();
                                if (MediaPlayerUtils.isPlaying()) {
                                    MediaPlayerUtils.pauseMediaPlayer();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            signInZEGOSDK(GetSet.getUserId(), GetSet.getName(), (errorCode, message) -> {
                                Log.e(TAG, errorCode +"," + message);
                            });

                            List<String> permissions = Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);
                            requestPermissionIfNeeded(permissions, (allGranted, grantedList, deniedList) -> {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ZEGOLiveStreamingManager.getInstance().init();
                                        Intent intent = new Intent(MainActivity.this, SwipeLiveActivity.class);
                                        intent.putExtra("from", "viewer"); //from this page
                                        intent.putExtra("type", streamData.getStreamType());
                                        intent.putExtra("publisherName", streamData.getPostedBy());
                                        intent.putExtra(StreamConstants.TAG_TITLE, streamData.getTitle());
                                        Log.d(TAG, "onResponse:77 " + streamData.getName());
                                        intent.putExtra(StreamConstants.TAG_STREAM_NAME, streamData.getName());
                                        intent.putExtra(StreamConstants.TAG_INVITE_LINK, streamData.getInviteLink());
                                        intent.putExtra(StreamConstants.TAG_STREAM_TOKEN, "");
                                        intent.putExtra("hostId", streamData.publisherId);
                                        intent.putExtra("hostName", streamData.postedBy);
                                        intent.putExtra("hostImage", streamData.publisherImage);
                                        intent.putExtra("host", false);
                                        intent.putExtra("liveID", streamData.getName());
                                        intent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                                        intent.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                                        intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getImageUrl());
                                        intent.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());

                                        Log.d(TAG, "onResponse:123 " + GetSet.getImageUrl());
                                        Log.d(TAG, "onResponse:123 " + GetSet.getUserImage());
                                        intent.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                                        Bundle arguments = new Bundle();
                                        arguments.putSerializable(StreamConstants.TAG_STREAM_DATA, streamData);
                                        intent.putExtras(arguments);
                                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_SUBSCRIBE);
                                        startActivity(intent);
                                    }
                                }, 100);

                            });

                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<StreamDetails> call, Throwable t) {
                call.cancel();
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });

    }

    private void addDeviceID() {
        final String token = DevicePref.getInstance(getApplicationContext()).getDeviceToken();
        @SuppressLint("HardwareIds") final String deviceId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);

        AddDeviceRequest request = new AddDeviceRequest();
        request.setUserId(GetSet.getUserId());
        request.setDeviceToken(token);
        request.setDeviceType(Constants.TAG_DEVICE_TYPE);
        request.setDeviceId(deviceId);
        request.setDeviceModel(AppUtils.getDeviceName());
        /*request.setOneSignalId(oneSignalId);*/
        Call<Map<String, String>> call3 = apiInterface.pushSignIn(request);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {

                if (GetSet.getUserId() != null) {
                    AppWebSocket.mInstance = null;
                    AppWebSocket.getInstance(MainActivity.this);
                }
                if (homeFragment != null) {
                    /*RecentStreamEvent streamEvent = new RecentStreamEvent();
                    streamEvent.setUserId(GetSet.getUserId());
                    EventBus.getDefault().post(streamEvent);*/
                }
                if (searchFragment != null) {
                    GetSearchEvent searchEvent = new GetSearchEvent();
                    searchEvent.setUserId(GetSet.getUserId());
                    EventBus.getDefault().post(searchEvent);
                }

                if (SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null) != null) {
                    try {
                        Logging.e(TAG, "onResponse:fbImage " + SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null));
                        InputStream imageStream = new URL(SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null)).openStream();
                        uploadImage(AppUtils.getBytes(imageStream));
                    } catch (Exception e) {
                        Logging.e(TAG, "onResponse: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {

                    profileDialog.hideLoading();
                    getUserProfile(GetSet.getUserId());
                    showInterest();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void addAdminMessages() {
        AdminMessageResponse.MessageData messageData = new AdminMessageResponse().new MessageData();
        messageData.setMsgFrom(Constants.TAG_ADMIN);
        messageData.setMsgId("0");
        messageData.setMsgType(Constants.TAG_TEXT);
        messageData.setMsgData(AdminData.welcomeMessage);
        String msgAt = "" + (System.currentTimeMillis() / 1000);
        String createdAt = AppUtils.getCurrentUTCTime(getApplicationContext());
        messageData.setMsgAt(msgAt);
        messageData.setCreateaAt(createdAt);
        dbHelper.addAdminMessage(messageData);
        messageData.setMsgAt(createdAt);
        messageData.setCreateaAt(msgAt);
        int unseenCount = dbHelper.getUnseenMessagesCount(GetSet.getUserId());
        dbHelper.addAdminRecentMessage(messageData, unseenCount);
    }

    private void updateGems(String referalCode) {
        Log.e(TAG, "updateGems: " + referalCode);
        Call<ReferFriendResponse> call = apiInterface.updateReferal(referalCode);
        call.enqueue(new Callback<ReferFriendResponse>() {
            @Override
            public void onResponse(Call<ReferFriendResponse> call, Response<ReferFriendResponse> response) {
                if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                    SharedPref.putString(SharedPref.REFERAL_CODE, "");
                }
            }

            @Override
            public void onFailure(Call<ReferFriendResponse> call, Throwable t) {

            }
        });
    }

    private void showProfileImageDialog(SignInRequest signUpRequest) {
        imageDialog = new DialogProfileImage();
        imageDialog.setContext(MainActivity.this);

        imageDialog.setCallBack((object, bio) -> {
            InputStream imageStream = (InputStream) object;
            imageDialog.showLoading();
            signUpRequest.setBio(bio); // Added bio here
            signUpRequest.setEmail(Constants.Mailid);
//            signUp(signUpRequest, imageStream);
            signUp(signUpRequest, null);

        });

        imageDialog.setCancelable(false);
        imageDialog.show(getSupportFragmentManager(), TAG);
        /*profileDialog.dismiss();*/
    }

    private void uploadImage(byte[] imageBytes) {
        Log.d(TAG, "uploadImage1111111111111111111: ");
        RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_PROFILE_IMAGE, "image.jpg", requestFile);
        RequestBody userid = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
        ApiInterface apiInterface = ApiClient.getUploadClient().create(ApiInterface.class);
        Call<Map<String, String>> call3 = apiInterface.uploadProfileImage(body, userid);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
//                    Timber.d("data username onResponse: %s", data.get(Constants.TAG_USER_IMAGE));
                    GetSet.setUserImage(data.get(Constants.TAG_USER_IMAGE));
                    SharedPref.putString(Constants.TAG_USER_IMAGE, data.get(Constants.TAG_USER_IMAGE));

                    if (imageDialog != null) {
                        imageDialog.hideLoading();
                    }

                    if (profileDialog != null) {
                        profileDialog.hideLoading();
                        //  profileDialog.dismiss();
                    }

                    getUserProfile(GetSet.getUserId());
                    showInterest();

                } else if (data.get(Constants.TAG_STATUS) != null && Objects.equals(data.get(Constants.TAG_STATUS), Constants.TAG_REJECTED)) {
                    if (imageDialog != null) {
                        imageDialog.hideLoading();
                    }
                    if (profileDialog != null) {
                        profileDialog.dismiss();
                    }
                    App.makeToast(data.get(Constants.TAG_MESSAGE));
                } else {
                    App.makeToast(data.get(Constants.TAG_MESSAGE));
                    if (imageDialog != null) {
                        imageDialog.hideLoading();
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<Map<String, String>> call, Throwable t) {
                if (imageDialog != null) {
                    imageDialog.hideLoading();
                }
                Timber.e("uploadImage: %s", t.getMessage());
                call.cancel();
            }
        });
    }

    private void showGemsDialog() {
        gemsDialog = new DialogFreeGems();
        gemsDialog.setContext(MainActivity.this);
        gemsDialog.setCallBack((o, trim) -> gemsDialog.dismiss());
        gemsDialog.setCancelable(false);
        gemsDialog.show(getSupportFragmentManager(), TAG);
    }

    private void showInterest() {

        interestDialog = new DialogInterest();
        interestDialog.setContext(MainActivity.this);
        interestDialog.setFrom(Constants.TAG_SIGNUP);
        interestDialog.setCallBack((o, trim) -> {
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            finish();

            profileDialog.dismiss();
//            imageDialog.dismiss();
            if (imageDialog != null) {
                imageDialog.dismiss();
            }

            interestDialog.dismiss();

/*            if (trim.equals("skip")) {

            } else {

                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
            }*/

        });
        interestDialog.setCancelable(false);
        interestDialog.show(getSupportFragmentManager(), TAG);
    }


    private void getUserProfile(String userId) {
//        initPermissionDialog();
        if (NetworkReceiver.isConnected()) {
            if (GetSet.getUserId() != null) {
                ProfileRequest request = new ProfileRequest();
                request.setUserId(userId);
                request.setProfileId(userId);
                Call<ProfileResponse> call = apiInterface.getProfile(request);
                call.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        ProfileResponse profile = response.body();
                        if(profile != null)
                            if (profile.getStatus() != null && profile.getStatus().equals(Constants.TAG_TRUE)) {


                                GetSet.setUserId(profile.getUserId());
                                GetSet.setName(profile.getName());
                                GetSet.setUserName(profile.getUserName()); //sep12 i commented
                                GetSet.setEmail(profile.getEmail());
                                GetSet.setDob(profile.getDob());

                                GetSet.setGender(profile.getGender());
                                GetSet.setUserImage(profile.getUserImage());
                                /*GetSet.setLocation(profile.getLocation());*/
                                /*GetSet.setLoginId(profile.getLoginId());*/
                            /*GetSet.setPrivacyAge(profile.getPrivacyAge());
                            GetSet.setPrivacyContactMe(profile.getPrivacyContactMe());*/

                                GetSet.setPrivacyContactMe(profile.getBlockedByMe());

                                GetSet.setAge(String.valueOf(profile.getAge()));
                                GetSet.setFollowingCount(String.valueOf(profile.getFollowings()));
                                GetSet.setFollowersCount(String.valueOf(profile.getFollowers()));
                                GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(String.valueOf(profile.getAvailableGifts())) : 0L);
                                GetSet.setGems(profile.getAvailableGems() != null ? profile.getAvailableGems() : 0F);

                                GetSet.setShowNotification(String.valueOf(profile.getShowNotification()));
                                GetSet.setFollowNotification(String.valueOf(profile.getFollowNotification()));
                                GetSet.setChatNotification(String.valueOf(profile.getChatNotification()));
                                GetSet.setGiftEarnings(String.valueOf(profile.getGiftEarnings()));

                                GetSet.setPremiumMember(profile.getPremiumMember());
                                GetSet.setPremiumExpiry(profile.getPremiumExpiryDate());

                                GetSet.setReferalLink(profile.getReferalLink());
                                GetSet.setCreatedAt(profile.getCreatedAt());
                                GetSet.setBio(profile.getBio());


                                // Livza
                                GetSet.setBio(profile.getBio());
                                GetSet.setLikes(profile.getLikes());
                                GetSet.setPostCommand(profile.getCommentPrivacy());
                                GetSet.setSendMessage(profile.getMessagePrivacy());

                                SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                                /*SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());*/
                                SharedPref.putString(SharedPref.NAME, GetSet.getName());
                                SharedPref.putString(SharedPref.USER_NAME, GetSet.getUserName());
                                SharedPref.putString(SharedPref.DOB, GetSet.getDob());
                                SharedPref.putString(SharedPref.AGE, GetSet.getAge());
                                SharedPref.putString(SharedPref.GENDER, GetSet.getGender());
                                SharedPref.putString(SharedPref.USER_IMAGE, GetSet.getUserImage());
                                /*SharedPref.putString(SharedPref.LOCATION, GetSet.getLocation());*/
                                SharedPref.putString(SharedPref.FOLLOWINGS_COUNT, GetSet.getFollowingCount());
                                SharedPref.putString(SharedPref.FOLLOWERS_COUNT, GetSet.getFollowersCount());
                                SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                                SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                                SharedPref.putLong(SharedPref.VIDEOS, GetSet.getVideos());
                                SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                                SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                                SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                                SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                                SharedPref.putString(SharedPref.SHOW_NOTIFICATION, GetSet.getShowNotification());
                                SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                                SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                                SharedPref.putString(SharedPref.GIFT_EARNINGS, GetSet.getGiftEarnings());
                                SharedPref.putString(SharedPref.REFERAL_LINK, GetSet.getReferalLink());
                                SharedPref.putString(SharedPref.CREATED_AT, GetSet.getCreatedAt());
                                SharedPref.putString(SharedPref.BIO, GetSet.getBio());
                                SharedPref.putString(SharedPref.INTEREST_LIST, profile.getInterestList());


                                SharedPref.putString(SharedPref.POST_COMMAND, GetSet.getPostCommand());
                                SharedPref.putString(SharedPref.SEND_MESSAGE, GetSet.getSendMessage());


                                if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                                    AppUtils.filterLocation = new ArrayList<>();
                                }

                                /*Message Event*/
                                if (notificationFragment != null) {
                                    EventBus.getDefault().postSticky(GetSet.getUserId());
                                }

                                /*New Streams Event*/
                                if (homeFragment != null) {
                                /*NewStreamEvent event = new NewStreamEvent();
                                event.setHasNewStreams(profile.hasUnReadBroadcasts());
                                EventBus.getDefault().postSticky(event);*/
                                }

                                /*Profile Update Event*/
                                if (profileFragment != null) {
                                    GetProfileEvent profileEvent = new GetProfileEvent();
                                    profileEvent.setProfileResponse(profile);
                                    EventBus.getDefault().postSticky(profileEvent);
                                }
                            }
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {

                    }
                });
            }
        } else {
//            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    @Override
    public void onBackPressed() {
        //boolean consumed = tellFragments();
        // Timber.d("onBack: consumed %s", consumed);
        //if (!consumed) {
        if (doubleBackToExit) {
            overridePendingTransition(0, 0);
            finishAffinity();
        } else {
            if (viewPager != null && viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem(0, false);
            } else if (homeFragment != null && homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 0) {
                if (homeFragment.homeForYou != null && homeFragment.homeForYou.viewPager.getCurrentItem() == 1) {
                    homeFragment.homeForYou.viewPager.setCurrentItem(0);
                } else if (homeFragment.homeForYou != null && homeFragment.homeForYou.viewPager.getCurrentItem() == 0) {

                    this.doubleBackToExit = true;
                    App.makeToast(getString(R.string.back_button_exit_description));
                    new Handler().postDelayed(() -> doubleBackToExit = false, 1500);
                }
            } else if (homeFragment != null && homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 1) {
                if (homeFragment.homeFollowing != null && homeFragment.homeFollowing.viewPager.getCurrentItem() == 1) {
                    homeFragment.homeFollowing.viewPager.setCurrentItem(0);
                } else if (homeFragment.homeFollowing != null && homeFragment.homeFollowing.viewPager.getCurrentItem() == 0) {

                    this.doubleBackToExit = true;
                    App.makeToast(getString(R.string.back_button_exit_description));
                    new Handler().postDelayed(() -> doubleBackToExit = false, 1500);
                }
            }
        }
    }

    private boolean tellFragments() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
//        Timber.d("onBack: total fragments %d %s", fragments.size(), fragments);
        for (Fragment f : fragments) {
            if (f instanceof BaseFragment) {
                Timber.d("onBack: telling %s", f);
                if (((BaseFragment) f).onBackPressed()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateWatchCount() {
        if (NetworkReceiver.isConnected()) {
            List<String> videoList = dbHelper.getWatchedVideos();
            Log.d(TAG, "updateWatchCount: " + videoList);
            if (videoList.size() > 0) {
                dbHelper.updateVideoStatus(1);
            }
        }
    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }

    private void initPermissionDialog() {
        dialogOverLayPermission = new DialogOverLayPermission();
        dialogOverLayPermission.setContext(this);
        dialogOverLayPermission.setCallBack((o, trim) -> {
            boolean isAllow = (boolean) o;
            if (isAllow) {
                requestOverLayPermission();
            }
            dialogOverLayPermission.dismissAllowingStateLoss();
        });
    }

    private void requestOverLayPermission() {
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, Constants.OVERLAY_REQUEST_CODE);
        }
    }


    private void openPermissionDialog() {
        opendialog = true;
        if (SharedPref.getBoolean(SharedPref.POP_UP_WINDOW_PERMISSION, true)) {
            if (dialogOverLayPermission != null && !dialogOverLayPermission.isAdded()) {
                dialogOverLayPermission.show(getSupportFragmentManager(), "PermissionDialog");
            }
        }
    }

    private void initPermissions() {

        storagePermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {
//                    pickGalleryImage();
                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, x.getKey())) {
                                storagePermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                Log.d("storagePermissionResult1", new Gson().toJson(result));
                                Log.d("storagePermissionResult2", String.valueOf(result.keySet()));
                                PermissionsUtils.openPermissionDialog(MainActivity.this, new OnOkCancelClickListener() {
                                    @Override
                                    public void onOkClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, MainActivity.this.getString(R.string.need_video_permission_to_access));
                                Log.d(TAG, "onActivityResult: permisstion check3333");
                            }
                            break;
                        }
                    }
                }

            }
        });
    }

    class MainPagerAdapter extends FragmentStatePagerAdapter {

        public MainPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 2:
                    /*searchFragment = new SearchFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TAG_FROM, Constants.TAG_BROADCAST);
                    searchFragment.setArguments(bundle);
                    return  searchFragment;*/
                    notificationFragment = new NotificationFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TAG_FROM, "mainactivity");
                    notificationFragment.setArguments(bundle);
                    return notificationFragment;
                case 4:
                   // return new ProfileFragment();
                    return new NewProfileFragment();
                default:
                    return new Fragment(); // dummy placeholders for activities
            }
        }

        @Override
        public int getCount() {
            return 5; // total menu items
        }
    }


    private void setupViewPager() {
        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPager.setAdapter(adapter);
        // viewPager.setOffscreenPageLimit(3);
        viewPager.setOffscreenPageLimit(5);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float offset, int offsetPx) { }

            @Override
            public void onPageSelected(int position) {
                // Video handling
                switch (position) {
                    case 0: // resume Home
                        if (homeFragment != null) {
                            if (homeFragment.homeFragViewPager != null &&
                                    homeFragment.homeFragViewPager.getCurrentItem() == 0) {
                                if (homeFragment.homeForYou != null) {
                                    homeFragment.homeForYou.getForYouVideoFragment()
                                            .exoplayerRecyclerViewForYou.setPlayControl(true);
                                }
                            } else if (homeFragment.homeFragViewPager != null &&
                                    homeFragment.homeFragViewPager.getCurrentItem() == 1) {
                                if (homeFragment.homeFollowing != null) {
                                    homeFragment.homeFollowing.getFollowingVideoFragment()
                                            .followingExoplayerRecyclerView.setPlayControl(true);
                                }
                            }
                        }
                        break;
                    case 2:
                    case 4:
                        pauseVideoPlayed();
                        break;
                    default: // positions 1 & 3 are activities
                        pauseVideoPlayed();
                        break;
                }

                // update bottom nav icons
                setLivzaNavigation(position);

                // handle checked state only for fragments
                if (prevMenuItem != null) prevMenuItem.setChecked(false);
                if ((position == 0 || position == 2 || position == 4) &&
                        position < bottomNavigation.getMenu().size()) {
                    bottomNavigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = bottomNavigation.getMenu().getItem(position);
                }
            }

            @Override public void onPageScrollStateChanged(int state) { }
        });
    }

    private void setupBottomNavigation() {
        int activeColor = ContextCompat.getColor(this, R.color.colorPrimary);
        int inactiveColor = ContextCompat.getColor(this, R.color.colorWhite);

        /*for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
            MenuItem item = bottomNavigation.getMenu().getItem(i);
            Drawable icon = item.getIcon();
            if (icon == null) continue;

            Drawable wrappedIcon = DrawableCompat.wrap(icon.mutate());

            if (item.getItemId() == R.id.menuUpload) {
                // 🟣 Don't tint the upload icon
                DrawableCompat.setTintList(wrappedIcon, null);
            } else {
                // 🎨 Tint all others
                DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(this, R.color.nav_color));
            }

            item.setIcon(wrappedIcon);
        }*/


        bottomNavigation.setOnItemSelectedListener(menuItem -> {
         /*   for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
                MenuItem item = bottomNavigation.getMenu().getItem(i);
                Drawable icon = item.getIcon();

                if (item.getItemId() == R.id.menuUpload) {
                    // No tint for this one
                    DrawableCompat.setTintList(icon, null); // disables tint
                } else {
                    // Apply tint based on selection
                    if (item.getItemId() == menuItem.getItemId()) {
                        DrawableCompat.setTint(icon, activeColor);
                    } else {
                        DrawableCompat.setTint(icon, inactiveColor);
                    }
                }

                View menuItemView = bottomNavigation.findViewById(item.getItemId());
                if (menuItemView != null) {
                    menuItemView.setBackgroundResource(android.R.color.transparent); // Transparent background
                }

            }
         */   switch (menuItem.getItemId()) {
                case R.id.menuLive:
                    // viewPager.setCurrentItem(0, false);
                    if (viewPager.getCurrentItem() == 0) {
                        if (homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 0) {
                            EventBus.getDefault().post(new ForYouScrollEvent(true));
                        } else if (homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 1) {
                            EventBus.getDefault().post(new FollowingScrollEvent(true));
                        }
                    } else {
                        viewPager.setCurrentItem(0, false); // crash
                    }
                    return true;

                case R.id.menuLiveStream:
                    //startActivity(new Intent(this, SwipeLiveActivity.class));
                    List<String> permissions = Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);
                    requestPermissionIfNeeded(permissions, (allGranted, grantedList, deniedList) -> {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(getApplicationContext(), SwipeLiveActivity.class));
                            }
                        }, 100);

                    });

                    return false;

                case R.id.menuSearch:
                    EventBus.getDefault().post(new FollowingSwipePrevent(true));
                    viewPager.setCurrentItem(2, false);
                    return true;

                case R.id.menuUpload:
                    if (Constants.Login_method.equals("skip")) {
                        login();
                    } else {
                        // uploadvideo(); // old behaviour
                        startActivity(new Intent(getApplicationContext(), StartLiveActivity.class));
                    }
                    return false;

                case R.id.menuProfile:
                    if (Constants.Login_method.equals("skip")) {
                        login();
                    } else {
                        viewPager.setCurrentItem(4, false);
                    }
                    return true;
            }
            return false;
        });
    }

    private void setLivzaNavigation(int position) {
        // reset all icons
        bottomNavigation.getMenu().findItem(R.id.menuLiveStream)
                .setIcon(R.drawable.group_633439);
        bottomNavigation.getMenu().findItem(R.id.menuUpload)
                .setIcon(R.drawable.add2x);

        //hi backup
        /*bottomNavigation.getMenu().findItem(R.id.menuLiveStream)
                .setIcon(R.drawable.group_633439);
        bottomNavigation.getMenu().findItem(R.id.menuUpload)
                .setIcon(R.drawable.profile_2__1_);*/


        // activate only fragment tabs
        switch (position) {
            case 0:
                bottomNavigation.getMenu().findItem(R.id.menuLive).setIcon(getDrawable(R.drawable.home_active4));
                bottomNavigation.getMenu().findItem(R.id.menuSearch).setIcon(getDrawable(R.drawable.new_message_inactive_1));
                bottomNavigation.getMenu().findItem(R.id.menuProfile).setIcon(getDrawable(R.drawable.new_profile_inactive_1));
                break;
            case 1:
                pauseVideoPlayed();
                break;
            case 2:
                //new_search_active
                //img_1_search_Selected
                //img_!_search_unselected
                pauseVideoPlayed();
//                bottomNavigation.getMenu().findItem(R.id.menuLive).setIcon(getDrawable(R.drawable.home_inactive4));
//                bottomNavigation.getMenu().findItem(R.id.menuSearch).setIcon(getDrawable(R.drawable.img_1_search_selected));
//                bottomNavigation.getMenu().findItem(R.id.menuProfile).setIcon(getDrawable(R.drawable.new_profile_inactive_1));

                bottomNavigation.getMenu().findItem(R.id.menuLive).setIcon(getDrawable(R.drawable.home_inactive4));
                bottomNavigation.getMenu().findItem(R.id.menuSearch).setIcon(getDrawable(R.drawable.message_active4)); // Change to active notification icon
                bottomNavigation.getMenu().findItem(R.id.menuProfile).setIcon(getDrawable(R.drawable.new_profile_inactive_1));
                break;
            case 3:
                pauseVideoPlayed();
                break;
            case 4:
                pauseVideoPlayed();
                bottomNavigation.getMenu().findItem(R.id.menuLive).setIcon(getDrawable(R.drawable.home_inactive4));
                bottomNavigation.getMenu().findItem(R.id.menuSearch).setIcon(getDrawable(R.drawable.new_message_inactive_1));
                bottomNavigation.getMenu().findItem(R.id.menuProfile).setIcon(getDrawable(R.drawable.profile_active4));
                break;
        }
    }


    // Method to show alert dialog before opening settings
    private void showSettingsPermissionDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open app settings
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.cancel_txt), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // Optional: Add any cancel logic here
                    }
                })
                .setCancelable(false) // Prevent dismissing by tapping outside
                .show();
    }

    private void requestPermissionIfNeeded(List<String> permissions, RequestCallback requestCallback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }

        PermissionX.init(this).permissions(permissions).onExplainRequestReason((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(Manifest.permission.CAMERA)) {
                    message = this.getString(R.string.permission_explain_camera);
                } else if (deniedList.contains(Manifest.permission.RECORD_AUDIO)) {
                    message = this.getString(R.string.permission_explain_mic);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(Manifest.permission.CAMERA)) {
                        message = this.getString(R.string.permission_explain_camera);
                    } else if (deniedList.contains(Manifest.permission.RECORD_AUDIO)) {
                        message = this.getString(R.string.permission_explain_mic);
                    }
                } else {
                    message = this.getString(R.string.permission_explain_camera_mic);
                }
            }
            scope.showRequestReasonDialog(deniedList, message, getString(R.string.ok));
        }).onForwardToSettings((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(Manifest.permission.CAMERA)) {
                    message = this.getString(R.string.settings_camera);
                } else if (deniedList.contains(Manifest.permission.RECORD_AUDIO)) {
                    message = this.getString(R.string.settings_mic);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(Manifest.permission.CAMERA)) {
                        message = this.getString(R.string.settings_camera);
                    } else if (deniedList.contains(Manifest.permission.RECORD_AUDIO)) {
                        message = this.getString(R.string.settings_mic);
                    }
                } else {
                    message = this.getString(R.string.settings_camera_mic);
                }
            }
            scope.showForwardToSettingsDialog(deniedList, message, getString(R.string.settings),
                    getString(R.string.cancel_txt));
        }).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                                 @NonNull List<String> deniedList) {
                if (requestCallback != null) {
                    requestCallback.onResult(allGranted, grantedList, deniedList);
                }
            }
        });
    }

    private void changeLangugae(String language, String deviceId, Context context, String languageCode, String languageName) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put("device_id", deviceId);
            map.put("language", language);
            Log.d("ChangeLanguage", "changeLangugae: params" + new Gson().toJson(map));

            Call<CommonResponse> call = apiInterface.changeLangugae(GetSet.getToken(), map);
            call.enqueue(new Callback<CommonResponse>() {
                @Override
                public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                    Log.d( "changeLangugae: res" , new Gson().toJson(response.body()));
                }

                @Override
                public void onFailure(Call<CommonResponse> call, Throwable t) {

                }
            });
        }
    }

}