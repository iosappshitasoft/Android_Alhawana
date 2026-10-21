package com.app.alhawanajd;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.app.Zego.internal.sdk.ZEGOSDKManager;
import com.app.Zego.internal.sdk.basic.ZEGOSDKCallBack;
import com.app.addon.livestream.external.helper.NetworkReceiver;
import com.app.addon.livestream.external.utils.Constants;
import com.app.alhawanajd.databinding.ActivityLoginMobileBinding;
import com.app.helper.AppWebSocket;
import com.app.model.AddDeviceRequest;
import com.app.model.AdminMessageResponse;
import com.app.model.GetSet;
import com.app.model.SignInRequest;
import com.app.model.SignInResponse;
import com.app.utils.AdminData;
import com.app.utils.AppUtils;
import com.app.utils.DevicePref;
import com.app.utils.InfoUtils;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;

import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLoginMobile extends BaseActivity {

    ActivityLoginMobileBinding binding;
    long maxTimeInMilliseconds = 60000;// in your case
    private ApiInterface apiInterface;

    String verify_id,isEdit;
    String verification_sid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            int tint = R.color.surface_color;

            window.setStatusBarColor(ContextCompat.getColor(this, tint)); // Set status bar color

            // Remove the SYSTEM_UI_FLAG_LIGHT_STATUS_BAR flag to use light icons
            int flags = window.getDecorView().getSystemUiVisibility();
            flags = flags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // Clear the flag for light status bar icons
            window.getDecorView().setSystemUiVisibility(flags);
        }

        binding = ActivityLoginMobileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        binding.btnBack.setOnClickListener(v -> onBackPressed());

        isEdit = getIntent().getStringExtra("Edit");
        binding.btnverify.setOnClickListener(v -> {
            if(!binding.phone.getText().toString().trim().isEmpty() && binding.phone.getText().toString().length() > 6 && binding.phone.getText().toString().length() < 12){
                showLoading();
                getotp(binding.phone.getText().toString(),binding.countyCodePicker.getSelectedCountryCode());
            }else {
                Toast.makeText(this, "please enter valid phone number", Toast.LENGTH_SHORT).show();
            }

        });

        binding.resend.setOnClickListener(v -> {
            if(!binding.phone.getText().toString().trim().isEmpty() && binding.phone.getText().toString().length() > 6 && binding.phone.getText().toString().length() < 12){
                getotp(binding.phone.getText().toString(),binding.countyCodePicker.getSelectedCountryCode());
                binding.txttimer.setVisibility(View.VISIBLE);
                binding.resend.setVisibility(View.GONE);
                startTimer(maxTimeInMilliseconds, 1000);

            }else {
                Toast.makeText(this, "please enter valid phone number", Toast.LENGTH_SHORT).show();
            }

        });


        binding.pinview.addTextChangedListener(textWatcher);

    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    public void startTimer(final long finish, long tick) {
        CountDownTimer t;
        t = new CountDownTimer(finish, tick) {

            public void onTick(long millisUntilFinished) {
                long remainedSecs = millisUntilFinished / 1000;
                binding.txttimer.setText("Resend code in " + (remainedSecs / 60) + ":" + (remainedSecs % 60));// manage it accordign to you
            }

            public void onFinish() {
                binding.txttimer.setVisibility(View.GONE);
                binding.resend.setVisibility(View.VISIBLE);
                cancel();
            }
        }.start();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // get the content of both the edit text
            if (s.length() == 6){
                showLoading();
                VerifyOtp(binding.phone.getText().toString().trim(),binding.countyCodePicker.getSelectedCountryCode());

            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void signIn(String loginId, String type, String email, String name, String image) {
        if (NetworkReceiver.isConnected()) {
            showLoading();
            final String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            SignInRequest request = new SignInRequest();
            request.setLoginId(loginId);
            request.setType(type);
            request.setLocation(GetSet.getLocation()+"");
            request.setDevice_id(deviceId);
            Log.e("paramLogin","-"+request);
//            Timber.i("signIn: %s", new Gson().toJson(request));
            Call<SignInResponse> call = apiInterface.callSignIn(request);
            call.enqueue(new Callback<SignInResponse>() {
                @Override
                public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                    if (response.isSuccessful()) {
                        SignInResponse signin = response.body();
                        if (signin.getStatus().equals(Constants.TAG_TRUE)) {
                            GetSet.setLogged(true);
                            SharedPref.putBoolean(SharedPref.IS_LOGGED, true);
                            SharedPref.putString(SharedPref.LOGIN_ID, loginId);
                            SharedPref.putString(SharedPref.LOGIN_TYPE, type);
                            if (type.equalsIgnoreCase(Constants.TAG_FACEBOOK)) {
                                SharedPref.putString(SharedPref.FACEBOOK_NAME, name);
                                SharedPref.putString(SharedPref.FACEBOOK_EMAIL, email);
                                SharedPref.putString(SharedPref.FACEBOOK_IMAGE, image);
                            }
                            GetSet.setLoginId(loginId);
                            GetSet.setLoginType(type);
                            if (!signin.isAccountExists()) {
                                hideLoading();
                                goToMain();
                            }
                            else {

                                // App.makeToast(signin.getLocation());

                                GetSet.setUserId(signin.getUserId());
                                GetSet.setLoginId(signin.getLoginId());
                                GetSet.setName(signin.getName());
                                GetSet.setDob(signin.getDob());
                                GetSet.setAge(signin.getAge());
                                GetSet.setGender(signin.getGender());
                                /*GetSet.setLocation(signin.getLocation());*/
                                GetSet.setAuthToken(signin.getAuthToken());
                                GetSet.setUserImage(signin.getUserImage());
                                GetSet.setFollowersCount("0");
                                GetSet.setFollowingCount("0");
                                GetSet.setInterestsCount(0);
                                GetSet.setFriendsCount(0);
                                GetSet.setUnlocksLeft(0);
                                GetSet.setGems(0F);
                                GetSet.setGifts(0L);
                                GetSet.setPremiumMember(Constants.TAG_TRUE);
                                GetSet.setPremiumExpiry("");
                                GetSet.setPrivacyAge(signin.getPrivacyAge());
                                GetSet.setPrivacyContactMe(signin.getPrivacyContactMe());
                                GetSet.setFollowNotification(signin.getFollowNotification());
                                GetSet.setChatNotification(signin.getChatNotification());
                                GetSet.setShowNotification(signin.getShowNotification());
                                GetSet.setInterestNotification(signin.getInterestNotification());

                                GetSet.setPremiumMember(signin.getPremiumMember());


                                AppUtils.callerList.clear();
                                AppUtils.callerList.add(GetSet.getUserId());

                                SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                                SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());
                                SharedPref.putString(SharedPref.NAME, GetSet.getName());
                                SharedPref.putString(SharedPref.DOB, GetSet.getDob());
                                SharedPref.putString(SharedPref.AGE, GetSet.getAge());
                                SharedPref.putString(SharedPref.GENDER, GetSet.getGender());
                                /*SharedPref.putString(SharedPref.LOCATION, GetSet.getLocation());*/
                                SharedPref.putString(SharedPref.AUTH_TOKEN, GetSet.getAuthToken());
                                SharedPref.putString(SharedPref.USER_IMAGE, GetSet.getUserImage());
                                SharedPref.putString(SharedPref.FOLLOWERS_COUNT, GetSet.getFollowersCount());
                                SharedPref.putString(SharedPref.FOLLOWINGS_COUNT, GetSet.getFollowingCount());
                                SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                                SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());
                                SharedPref.putInt(SharedPref.UNLOCKS_LEFT, GetSet.getUnlocksLeft());
                                SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                                SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                                SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                                SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                                SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                                SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                                SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                                SharedPref.putString(SharedPref.TAG_ACCESS_TOKENS, signin.getAuthToken());

                                addDeviceID();
                            }
                        } else if (signin.getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                            App.makeToast(getString(R.string.contact_admin_desc));
                            hideLoading();
                        }else if(signin.getStatus().equals("accountdeleted")){
                            hideLoading();
                            opendeleteDialog();
                        }
                        else {
                            App.makeToast(getString(R.string.something_went_wrong));
                            hideLoading();
                        }
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                        hideLoading();
                    }

                }

                @Override
                public void onFailure(Call<SignInResponse> call, Throwable t) {
                    hideLoading();
                    call.cancel();
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void getotp(String phone,String cc){
        SignInRequest request = new SignInRequest();
        request.setPhone(phone);
        request.setCc("+"+cc);
        Call<SignInResponse> call = apiInterface.callSignOtp(request);
        call.enqueue(new Callback<SignInResponse>() {
            @Override
            public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                if (response.isSuccessful()) {
                    SignInResponse signin = response.body();
                    hideLoading();
                    if (signin.getStatus().equals(Constants.TAG_TRUE)) {
                        verify_id = signin.getVerify_id();
                        verification_sid = signin.getVerification_sid();
                        binding.ccp.setVisibility(View.GONE);
                        binding.btnverify.setVisibility(View.GONE);
                        binding.otplay.setVisibility(View.VISIBLE);
                        binding.mobilenum.setText("+"+binding.countyCodePicker.getSelectedCountryCode()+binding.phone.getText().toString());
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(binding.phone.getWindowToken(), 0);
                        startTimer(maxTimeInMilliseconds, 1000);

                    } else if (signin.getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                        App.makeToast(getString(R.string.contact_admin_desc));
                        hideLoading();
                    }else if(signin.getStatus().equals("accountdeleted")){
                        hideLoading();
                        opendeleteDialog();
                    }
                    else {
                        App.makeToast(getString(R.string.something_went_wrong));
                        hideLoading();
                    }
                } else {
                    App.makeToast(getString(R.string.something_went_wrong));
                    hideLoading();
                }

            }

            @Override
            public void onFailure(Call<SignInResponse> call, Throwable t) {
                hideLoading();
                call.cancel();
            }
        });
    }
    private void VerifyOtp(String phone,String cc){
        SignInRequest request = new SignInRequest();
        request.setPhone(phone);
        request.setCc("+"+cc);
        request.setOtp(binding.pinview.getText().toString());
        request.setVerify_id(verify_id);
        request.setVerification_sid(verification_sid);
        Call<SignInResponse> call = apiInterface.verifyOtp(request);
        call.enqueue(new Callback<SignInResponse>() {
            @Override
            public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                if (response.isSuccessful()) {
                    SignInResponse signin = response.body();
                    hideLoading();
                    if (signin.getStatus().equals(Constants.TAG_TRUE)) {
                        Log.e("countrycode",binding.countyCodePicker.getSelectedCountryCode());

                        if (isEdit !=null && isEdit.equalsIgnoreCase("true")) {
                            Intent intent = new Intent();
                            intent.putExtra("phone", "+"+binding.countyCodePicker.getSelectedCountryCode()+binding.phone.getText().toString());
                           // Log.e("Test" , "+"+binding.countyCodePicker.getSelectedCountryCode()+binding.phone.getText().toString());
                            //intent.putExtra("phone",binding.phone.getText().toString());
                            setResult(101, intent);
                            finish();
                        } else {
                            signIn("+"+binding.countyCodePicker.getSelectedCountryCode()+binding.phone.getText().toString().trim(), Constants.TAG_PHONENUMBER, null, null, null);
                        }
                    } else if (signin.getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                        App.makeToast(getString(R.string.contact_admin_desc));
                        hideLoading();
                    } else if (signin.getStatus().equals("accountdeleted")) {
                        hideLoading();
                        opendeleteDialog();
                    } else {
                        App.makeToast(signin.getMessage());
                        binding.pinview.setText("");
                        hideLoading();
                    }
                } else {
                    App.makeToast(getString(R.string.something_went_wrong));
                    hideLoading();
                }

            }

            @Override
            public void onFailure(Call<SignInResponse> call, Throwable t) {
                hideLoading();
                call.cancel();
            }
        });
    }

    private void showLoading() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        binding.progressBar.setIndeterminate(true);
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        binding.progressBar.setIndeterminate(false);
        binding.progressBar.setVisibility(View.GONE);
    }

    private void opendeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLoginMobile.this);
        builder.setMessage(getString(R.string.response_del_acc));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
       /* builder.setNegativeButton(getString(R.string.no_txt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });*/
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_regular);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);

    }

    private void addDeviceID() {
        /*OSDeviceState deviceState = OneSignal.getDeviceState();
        if (deviceState != null) {
            oneSignalId = deviceState.getUserId();
            Log.i(TAG, "addDeviceId: " + oneSignalId);
        }*/
        final String token = DevicePref.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        Log.e("TAG", "addDeviceID: :::::::::::::::"+deviceId );

        AddDeviceRequest request = new AddDeviceRequest();
        request.setUserId(GetSet.getUserId());
        request.setDeviceToken(token);
        //request.setDeviceToken(oneSignalId);
        request.setDeviceType(Constants.TAG_DEVICE_TYPE);
        request.setDeviceId(deviceId);
        request.setDeviceModel(AppUtils.getDeviceName());
        request.setAppVersion( InfoUtils.INSTANCE.getVersionName(this));
        //request.setOneSignalId(oneSignalId);

        Call<Map<String, String>> call3 = apiInterface.pushSignIn(request);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                addAdminMessages();
                goToMain();
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void addAdminMessages() {
        String msgAt = "" + (System.currentTimeMillis() / 1000);
        String createdAt = AppUtils.getCurrentUTCTime(getApplicationContext());

        AdminMessageResponse.MessageData messageData = new AdminMessageResponse().new MessageData();
        messageData.setMsgFrom(Constants.TAG_ADMIN);
        messageData.setMsgId(GetSet.getUserId() + (System.currentTimeMillis() / 1000));
        messageData.setMsgType(Constants.TAG_TEXT);
        messageData.setMsgData("" + AdminData.welcomeMessage);
        messageData.setMsgAt(msgAt);
        messageData.setCreateaAt(createdAt);
        //dbHelper.addAdminMessage(messageData);
        messageData.setMsgAt(createdAt);
        messageData.setCreateaAt(msgAt);
        //int unseenCount = dbHelper.getUnseenMessagesCount(GetSet.getUserId());
        //unseenCount = unseenCount + 1;
        //dbHelper.addAdminRecentMessage(messageData, unseenCount);
    }

    private void goToMain() {

        if (GetSet.getUserId() != null) {
            AppWebSocket.mInstance = null;
            AppWebSocket.getInstance(this);
        }
        hideLoading();
        Log.e("TAG","userId : " + GetSet.getUserId() + ", " + "userName : " + GetSet.getName());
        if (GetSet.getName() == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else {
            signInZEGOSDK(GetSet.getUserId(), GetSet.getName(), (errorCode, message) -> {
                if (errorCode == 0||errorCode==6000111) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }else {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            });
        }
    }
    private void signInZEGOSDK(String userID, String userName, ZEGOSDKCallBack callback) {
        if(userID != null && !userID.trim().isEmpty()) {
            ZEGOSDKManager.getInstance().connectUser(userID, userName, callback);
        }
    }

}
