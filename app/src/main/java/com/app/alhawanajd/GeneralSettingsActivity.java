package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

//import com.google.android.gms.ads.AdView;
import com.app.alhawanajd.databinding.ActivityGeneralSettingsBinding;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.helper.AppWebSocket;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.StorageUtils;
import com.app.helper.workers.SettingsUpdateWorker;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class GeneralSettingsActivity extends BaseFragmentActivity implements AppWebSocket.WebSocketChannelEvents {

    private static final String TAG = GeneralSettingsActivity.class.getSimpleName();

    /*@BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btnPrivateChat)
    SwitchMaterial btnPrivateChat;
    @BindView(R.id.privateChatLay)
    RelativeLayout privateChatLay;*/
    /*@BindView(R.id.btnDnD)
    SwitchMaterial btnDnD;
    @BindView(R.id.dndLay)
    RelativeLayout dndLay;*/
    /*@BindView(R.id.btnFollow)
    SwitchMaterial btnFollow;
    @BindView(R.id.followLay)
    RelativeLayout followLay;
    @BindView(R.id.txtFollow)
    TextView txtFollow;*/

//    @BindView(R.id.adView)
//    AdView adView;


    private ApiInterface apiInterface;
    private AppUtils appUtils;
    private DBHelper dbHelper;
    private StorageUtils storageUtils;
    ProgressDialog pdLoading;
    private Boolean isLoading = false;
    private Boolean isChat = false;
    // 2. Declare the binding object
    private ActivityGeneralSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* setContentView(R.layout.activity_general_settings);
        ButterKnife.bind(this);*/

        binding = ActivityGeneralSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        dbHelper = DBHelper.getInstance(this);
        storageUtils = StorageUtils.getInstance(this);
        initView();

        setupClickListeners();
    }

    private void initView() {

        pdLoading = new ProgressDialog(GeneralSettingsActivity.this, R.style.CameraAlertDialog);
        pdLoading.setCancelable(false);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);
        binding.includeHeader.txtTitle.setText(getString(R.string.general_settings));
        setSettings();
        loadAd();

        if (LocaleManager.isRTL()) {
            binding.includeHeader.btnBack.setScaleX(-1);
        } else {
            binding.includeHeader.btnBack.setScaleX(1);
        }

    }

    private void loadAd() {
//        BannerAdUtils.loadBannerAd(this, (FrameLayout) findViewById(R.id.adLay));
    }


    private void setSettings() {
        binding.btnPrivateChat.setChecked(GetSet.getChatNotification().equals(Constants.TAG_TRUE));
        binding.txtFollow.setText(getString(R.string.notify_if_anyone_follows));
        binding.btnFollow.setChecked(GetSet.getFollowNotification().equals(Constants.TAG_TRUE));
    }

    private void updateSettings(String type, ProfileRequest request) {
        if (NetworkReceiver.isConnected()) {
//            showLoading();
            if (GetSet.getUserId() != null) {
                if (type.equalsIgnoreCase("chat")) {
                    GetSet.setChatNotification(String.valueOf(request.getChatNotification()));
                    SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                    binding.btnPrivateChat.setChecked(GetSet.getChatNotification().equals(Constants.TAG_TRUE));
                    binding.btnPrivateChat.setEnabled(true);
                } else if (type.equalsIgnoreCase("follow")) {
                    GetSet.setFollowNotification(String.valueOf(request.getFollowNotification()));
                    SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                    binding.btnFollow.setChecked(GetSet.getFollowNotification().equals(Constants.TAG_TRUE));
                    binding.btnFollow.setEnabled(true);
                }
                OneTimeWorkRequest workRequest =
                        new OneTimeWorkRequest.Builder(SettingsUpdateWorker.class)
                                .setInputData(createInputData(request))
                                .setConstraints(new Constraints.Builder()
                                        .setRequiredNetworkType(NetworkType.CONNECTED)
                                        .build())
                                .build();
                WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                workManager.enqueue(workRequest);
                workManager.getWorkInfoByIdLiveData(workRequest.getId()).observe(GeneralSettingsActivity.this, workInfo -> {
                    if (workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            String downloadPath = workInfo.getOutputData().getString(Constants.TAG_PROFILE_DATA);
                            Timber.tag(TAG).d("updateSettings: %s", downloadPath);
                        }
                    }
                });
            }
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private Data createInputData(final ProfileRequest request) {
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_PROFILE_DATA, new Gson().toJson(request));
        return builder.build();
    }

    public void showLoading() {
        /*Disable touch options*/
        isLoading = true;
        if(!pdLoading.isShowing())
            pdLoading.show();
    /*    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);*/
    }

    public void hideLoading() {
        /*Enable touch options*/
        isLoading = false;
        if(pdLoading.isShowing())
            pdLoading.cancel();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onWebSocketConnected() {

    }

    @Override
    public void onWebSocketMessage(String message) {

    }

    @Override
    public void onWebSocketClose() {

    }

    @Override
    public void onWebSocketError(String description) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }

   /* @OnClick({R.id.btnBack, R.id.btnPrivateChat, R.id.privateChatLay, R.id.btnFollow, R.id.followLay,})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnPrivateChat:
                if (NetworkReceiver.isConnected()) {
                    updatePrivateChat();
                }
                break;
            *//*case R.id.privateChatLay:
                if (NetworkReceiver.isConnected()) {
                    if(btnPrivateChat.isChecked())
                    updatePrivateChat();
                }
                break;*//*
            *//*case R.id.btnDnD:
                if (NetworkReceiver.isConnected()) {
                    if (btnDnD.isChecked()) {
                        btnPrivateChat.setChecked(false);
                        btnFollow.setChecked(false);
                    }
                    updateDND();
                }
                break;
            case R.id.dndLay:
                if (NetworkReceiver.isConnected()) {
                    btnDnD.setChecked(!btnDnD.isChecked());
                    if (btnDnD.isChecked()) {
                        btnPrivateChat.setChecked(false);
                        btnFollow.setChecked(false);
                    }
                    updateDND();
                }
                break;*//*
            case R.id.btnFollow:
                if (NetworkReceiver.isConnected()) {
                    updateFollow();
                }
                break;
            *//*case R.id.followLay:
                if (NetworkReceiver.isConnected()) {
                    btnFollow.setChecked(!btnFollow.isChecked());
                    updateFollow();
                }
                break;*//*

        }
    }
*/

    // 6. Create a method for setting click listeners
    private void setupClickListeners() {
        binding.includeHeader.btnBack.setOnClickListener(v -> onBackPressed());

        binding.btnPrivateChat.setOnClickListener(v -> {
            if (NetworkReceiver.isConnected()) {
                updatePrivateChat();
            }
        });

        binding.btnFollow.setOnClickListener(v -> {
            if (NetworkReceiver.isConnected()) {
                updateFollow();
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == Constants.PRIME_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setSettings();
            loadAd();
        }*/
    }

    private void updatePrivateChat() {
        binding.btnPrivateChat.setEnabled(false);
        ProfileRequest request = new ProfileRequest();
        request.setChatNotification("" + binding.btnPrivateChat.isChecked());
        updateSettings("chat", request);
    }

    private void updateDND() {
        /*ProfileRequest request = new ProfileRequest();
        request.setShowNotification("" + btnDnD.isChecked());
        if (btnDnD.isChecked()) {
            request.setChatNotification("" + btnPrivateChat.isChecked());
            request.setFollowNotification("" + btnFollow.isChecked());
        }
        updateSettings(request);*/
    }

    private void updateFollow() {
        binding.btnFollow.setEnabled(false);
        ProfileRequest request = new ProfileRequest();
        request.setFollowNotification("" + binding.btnFollow.isChecked());
        updateSettings("follow", request);
    }

}
