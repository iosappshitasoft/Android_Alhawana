package com.app.alhawanajd;import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.app.alhawanajd.Video.PrivacyBottomSheet;
import com.app.alhawanajd.databinding.ActivityAccountPrivacyBinding; // 1. Import the binding class
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.model.VideoPrivacyModel;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

// No more ButterKnife imports

@SuppressLint("NonConstantResourceId")
public class AccountPrivacy extends BaseFragmentActivity {

    private static final String TAG = AccountPrivacy.class.getSimpleName();

    // 2. Declare the binding object
    private ActivityAccountPrivacyBinding binding;

    private ApiInterface apiInterface;

    private final List<VideoPrivacyModel> postCommandList = new ArrayList<>();
    private final List<VideoPrivacyModel> postMessageList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 3. Inflate the layout using View Binding
        binding = ActivityAccountPrivacyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        initView();
    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        // 4. Access all views through the binding object
        binding.includeHeader.txtTitle.setText(getString(R.string.privacy));


        if (LocaleManager.isRTL()) {
            binding.includeHeader.btnBack.setScaleX(-1);
        } else {
            binding.includeHeader.btnBack.setScaleX(1);
        }

        // 5. Set click listeners manually
        binding.blockLay.setOnClickListener(v -> startActivity(new Intent(AccountPrivacy.this, BlockedUsersActivity.class)));

        binding.postCommandLay.setOnClickListener(v -> {
            String currentValue = binding.resultPostCommand.getText().toString();
            PrivacyBottomSheet sheet = new PrivacyBottomSheet(
                    "Who can post comments", currentValue, value -> updatePrivateChat("postCommand", value)
            );
            sheet.show(getSupportFragmentManager(), "PrivacyBottomSheet");
        });


        binding.messageLay.setOnClickListener(v -> {
            String currentValue = binding.resultPostMessage.getText().toString();
            PrivacyBottomSheet sheet = new PrivacyBottomSheet(
                    "Who can message you", currentValue, value -> updatePrivateChat("postMessage", value)
            );
            sheet.show(getSupportFragmentManager(), "PrivacyBottomSheet");
        });

        binding.includeHeader.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        setSettings();
    }


    private void updatePrivateChat(String type, String getData) {
        ProfileRequest request = new ProfileRequest();
        if (type.equals("postCommand"))
            request.setComment_privacy(getData);
        else if (type.equals("postMessage"))
            request.setMessage_privacy(getData);
        updateSettings(type, request);
    }

    private void updateSettings(String type, ProfileRequest request) {
        if (NetworkReceiver.isConnected()) {
            if (GetSet.getUserId() != null) {
                request.setUserId(GetSet.getUserId());
                request.setProfileId(GetSet.getUserId());

                Timber.d("getProfile request: %s", new Gson().toJson(request));

                Call<ProfileResponse> call = apiInterface.getProfile(request);
                call.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ProfileResponse profile = response.body();

                            if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                                Timber.d("onResponse: %s", new Gson().toJson(profile));

                                if (type.equalsIgnoreCase("postCommand")) {
                                    GetSet.setPostCommand(profile.getCommentPrivacy());
                                    SharedPref.putString(SharedPref.POST_COMMAND, GetSet.getPostCommand());

                                    if (Constants.TAG_FRIENDS.equals(profile.getCommentPrivacy()) || Constants.TAG_FANS.equals(profile.getCommentPrivacy()))
                                        binding.resultPostCommand.setText(getResources().getString(R.string.settings_Fans));
                                    else if (profile.getCommentPrivacy().equals(getString(R.string.settings_everyone))){
                                        binding.resultPostCommand.setText(getString(R.string.settings_everyone));
                                    }else if (profile.getCommentPrivacy().equals(getString(R.string.settings_Off))){
                                        binding.resultPostCommand.setText(getString(R.string.settings_Off));
                                    }
                                } else if (type.equalsIgnoreCase("postMessage")) {
                                    GetSet.setSendMessage(profile.getMessagePrivacy());
                                    SharedPref.putString(SharedPref.SEND_MESSAGE, GetSet.getSendMessage());

                                    if (Constants.TAG_FRIENDS.equals(profile.getMessagePrivacy()) || Constants.TAG_FANS.equals(profile.getMessagePrivacy()))
                                        binding.resultPostMessage.setText(getResources().getString(R.string.settings_Fans));
                                    else if (profile.getMessagePrivacy().equals(getString(R.string.settings_everyone))){
                                        binding.resultPostMessage.setText(getString(R.string.settings_everyone));
                                    }else if (profile.getMessagePrivacy().equals(getString(R.string.settings_Off))){
                                        binding.resultPostMessage.setText(getString(R.string.settings_Off));
                                    }
                                }

                                binding.mainLay.setAlpha(1.0f);
                                binding.mProgressbar.setAlpha(0f);
                                binding.mProgressbar.setVisibility(View.GONE);

                                binding.postCommandLay.setEnabled(true);
                                binding.blockLay.setEnabled(true);
                                binding.messageLay.setEnabled(true);
                                binding.includeHeader.btnBack.setEnabled(true);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
                        call.cancel();
                    }
                });
            }
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void setSettings() {
        Log.d(TAG, "setSettings: " + GetSet.getPostCommand());
        postCommandList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), GetSet.getPostCommand().equals(Constants.TAG_EVERYONE)));
        postCommandList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), (GetSet.getPostCommand().equals(Constants.TAG_FANS) || GetSet.getPostCommand().equals(Constants.TAG_FRIENDS))));
        postCommandList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), GetSet.getPostCommand().equals(Constants.TAG_OFF)));

        Log.d(TAG, "setSettings_message: " + GetSet.getSendMessage());
        postMessageList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), GetSet.getSendMessage().equals(Constants.TAG_EVERYONE)));
        postMessageList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), (GetSet.getSendMessage().equals(Constants.TAG_FANS) || GetSet.getSendMessage().equals(Constants.TAG_FRIENDS))));
        postMessageList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), GetSet.getSendMessage().equals(Constants.TAG_OFF)));

        if (GetSet.getPostCommand().equals(Constants.TAG_EVERYONE)) {
            binding.resultPostCommand.setText(getResources().getString(R.string.settings_everyone));
        } else if (GetSet.getPostCommand().equals(Constants.TAG_FRIENDS) || GetSet.getPostCommand().equals(Constants.TAG_FANS)) {
            binding.resultPostCommand.setText(getResources().getString(R.string.settings_Fans));
        } else {
            binding.resultPostCommand.setText(getResources().getString(R.string.settings_Off));
        }

        if (GetSet.getSendMessage().equals(Constants.TAG_EVERYONE)) {
            binding.resultPostMessage.setText(getResources().getString(R.string.settings_everyone));
        } else if (GetSet.getSendMessage().equals(Constants.TAG_FRIENDS) || GetSet.getSendMessage().equals(Constants.TAG_FANS)) {
            binding.resultPostMessage.setText(getResources().getString(R.string.settings_Fans));
        } else {
            binding.resultPostMessage.setText(getResources().getString(R.string.settings_Off));
        }

        binding.mainLay.setAlpha(1.0f);
        binding.mProgressbar.setAlpha(0f);
        binding.mProgressbar.setVisibility(View.GONE);

        Timber.i("postCommandList %s", App.getGsonPrettyInstance().toJson(postCommandList));
        Timber.i("postSendMessageList %s", App.getGsonPrettyInstance().toJson(postMessageList));
    }


    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), binding.parentLay, NetworkReceiver.isConnected());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        // You can show a snack here if needed
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Timber.d("requestCode: %s", requestCode);
        Timber.d("resultcode: %s", resultCode);

        if (requestCode == Constants.SETTING_POST_COMMAND) {
            if (resultCode != RESULT_CANCELED) {
                binding.postCommandLay.setEnabled(false);
                binding.blockLay.setEnabled(false);
                binding.messageLay.setEnabled(false);
                binding.includeHeader.btnBack.setEnabled(false);

                binding.mainLay.setAlpha(0.5f);
                binding.mProgressbar.setAlpha(1.0f);
                binding.mProgressbar.setVisibility(View.VISIBLE);

                postCommandList.clear();

                String getData = Objects.requireNonNull(data).getStringExtra("privacyName");

                if (getData.equals(Constants.TAG_FANS) || getData.equals(Constants.TAG_FRIENDS))
                    updatePrivateChat("postCommand", Constants.TAG_FRIENDS);
                else
                    updatePrivateChat("postCommand", getData);
            }
        }
    }
}
