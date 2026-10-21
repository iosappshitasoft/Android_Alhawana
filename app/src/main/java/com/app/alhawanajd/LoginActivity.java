package com.app.alhawanajd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.adapter.LanguageAdapter;
import com.app.helper.LocaleManager;
import com.app.model.CommonResponse;
import com.app.model.LanguageData;
import com.app.utils.Logging;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.app.db.DBHelper;
import com.app.helper.AppWebSocket;
import com.app.helper.NetworkReceiver;
import com.app.model.AddDeviceRequest;
import com.app.model.AdminMessageResponse;
import com.app.model.GetSet;
import com.app.model.SignInRequest;
import com.app.model.SignInResponse;
import com.app.utils.AdminData;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DevicePref;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.google.gson.Gson;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class LoginActivity extends BaseFragmentActivity implements View.OnClickListener
        /*implements OSSubscriptionObserver */ {

    private static final String TAG = LoginActivity.class.getSimpleName();
    LinearLayout btnMobile;
    MaterialButton btnFacebook;
    TextView txtTerms, skipBT;
    LinearLayout gpTxt, whatsappTxt;
    RelativeLayout parentLay;
    ProgressBar progressBar;
    ImageView iconAppLogo;
    MaterialCheckBox mCheckBox;
    TextureView textureView;

    private ActivityResultLauncher<Intent> mobileResultLauncher;
    private ApiInterface apiInterface;
    private MediaPlayer mMediaPlayer;
    // MediaPlayer instance to control playback of video file.
    private DBHelper dbHelper;
    private ImageView lang_icon;
    String oneSignalId = null;
    boolean mSignInClicked;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

      /*  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
  /*      View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        int uiOptions = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(uiOptions);*/

        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_login);
        dbHelper = DBHelper.getInstance(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        findViews();
        initActivityResultLauncher();
        initClickListener();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        dialog = new ProgressDialog(LoginActivity.this, R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.msg_please_wait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }

        lang_icon.setOnClickListener(view -> showBottomSheet(LoginActivity.this));



        //Addon
//        textureView.setSurfaceTextureListener(this);


        /*parentLay.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            int bottomNavHeight = view.getPaddingBottom() + windowInsets.getSystemWindowInsetBottom() + AppUtils.dpToPx(LoginActivity.this, 10);
            txtTerms.setTranslationY(-bottomNavHeight);
            return windowInsets.consumeSystemWindowInsets();
        });*/

        txtTerms.setText(Html.fromHtml(getString(R.string.terms_and_conditions_des)));
        //OneSignal.addSubscriptionObserver(this);

        Log.d(TAG, "onCreate: TAG_IS_FROM_LINK "+ getIntent().hasExtra(Constants.TAG_IS_FROM_LINK) +" ttt "+ getIntent().getStringExtra(Constants.TAG_IS_FROM_LINK));
        if (getIntent().hasExtra(Constants.TAG_IS_FROM_LINK)){
            if (getIntent().getStringExtra(Constants.TAG_IS_FROM_LINK).equals(Constants.TAG_TRUE)){
                if (getIntent().getStringExtra(Constants.TAG_FROM).equals(Constants.TAG_VIDEO)) {

                    openSeeVideoDialog();

                }
            }
        }

    }
    private void openSeeVideoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(R.string.login_to_see_video);
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
//                removeDeviceID(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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

    private void findViews() {
        btnMobile = findViewById(R.id.btnMobile);
        txtTerms = findViewById(R.id.txtTerms);
        parentLay = findViewById(R.id.parentLay);
        progressBar = findViewById(R.id.progressBar);
        iconAppLogo = findViewById(R.id.iconAppLogo);
        lang_icon = findViewById(R.id.lang_icon);
//        mCheckBox = findViewById(R.id.mcheckbox);
        textureView = findViewById(R.id.textureView);
        gpTxt = findViewById(R.id.gpTxt);
        skipBT = findViewById(R.id.skipBT);
        whatsappTxt = findViewById(R.id.whatsappTxt);
//        gpBtn = findViewById(R.id.gpBtn);
    }

    private void initActivityResultLauncher() {
        mobileResultLauncher = registerForActivityResult(new FirebaseAuthUIActivityResultContract(), new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
            @Override
            public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    // Successfully signed in
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null && user.getPhoneNumber() != null) {
                        signIn(user.getPhoneNumber(), Constants.TAG_PHONENUMBER, null, null, null);
                    }
                    AuthUI.getInstance()
                            .signOut(LoginActivity.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                    // ...
                } else {
                    // Sign in failed. If response is null the user canceled the
                    // sign-in flow using the back button. Otherwise check
                    // response.getError().getErrorCode() and handle the error.
                    // ...
//                    Timber.e("onActivityResult: %s", new Gson().toJson(result));
                    Timber.e("onActivityResult: %s", result.getResultCode());
                }
            }
        });
    }

    private void initClickListener() {
        btnMobile.setOnClickListener(this);
        txtTerms.setOnClickListener(this);
//        gpBtn.setOnClickListener(this);
        gpTxt.setOnClickListener(this);
        skipBT.setOnClickListener(this);
        whatsappTxt.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnMobile:
//                if (mCheckBox.isChecked()) {
                    Constants.Login_method = "Login";
                    App.preventMultipleClick(btnMobile);
                    verifyMobileNumber();
//                }
//                else alertToast();
                break;
            case R.id.txtTerms:
                App.preventMultipleClick(txtTerms);
                Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                startActivity(intent);
                break;
//            case R.id.gpBtn:
            case R.id.gpTxt:
//                if (mCheckBox.isChecked()) {
                    mSignInClicked = true;
                    Constants.Login_method = "Login";
                    googleLogout();
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
//                }
//                else alertToast();

                break;
            case R.id.whatsappTxt:
                Constants.Login_method = "Login";
                App.preventMultipleClick(whatsappTxt);
                googleLogout();
                startActivity(new Intent(getApplicationContext(), ActivityLoginMobile.class));
                break;
            case R.id.skipBT:
                Constants.Login_method = "skip";
                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                intent1.putExtra("skipvalue", "true");
                startActivity(intent1);
                finish();
                break;
        }
    }

    private void googleLogout() {
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                        }
                    });
        }
    }

    private void alertToast() {
        Toasty.info(LoginActivity.this, R.string.terms_of_use_warning, Toasty.LENGTH_SHORT).show();
    }

 /*   private void verifyMobileNumber() {
        // Create list of providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().setDefaultCountryIso("sa").build());
        // Create intent and add extras
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.Theme_FirebaseAuth)
                .setIsSmartLockEnabled(false)
                .build();
        // Launch the FirebaseUI intent
        mobileResultLauncher.launch(signInIntent);
    }*/

    private void verifyMobileNumber() {
        Intent i = new Intent(LoginActivity.this, FirebaseEnglishActivity.class);
        mobileResultLauncher.launch(i);
    }


    public void printKeyHash(Context pContext) {
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Timber.i("printHashKey: %s", hashKey);
            }
        } catch (Exception e) {
            Timber.e("printHashKey: %s", e.getMessage());
        }
    }

    private void signIn(String loginId, String type, String email, String name, String image) {
        if (NetworkReceiver.isConnected()) {
            showLoading();
            SignInRequest request = new SignInRequest();
            request.setLoginId(loginId);
            request.setType(type);
            Constants.LOGINMETHODE = "Login";
            Log.e("paramLogin....", "-" + type + email);
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
                                Log.d(TAG, "onResponse111111111111: ");
                                hideLoading();
                                goToMain();
                            } else {
                                Log.d(TAG, "onResponse2222222222222222: ");
                                // App.makeToast(signin.getLocation());

                                GetSet.setUserId(signin.getUserId());
                                GetSet.setUserName(signin.getName());
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
                            Log.d(TAG, "onResponse: 156");
                            App.makeToast(getString(R.string.contact_admin_desc));
                            openContactDialog();
                            hideLoading();
                        } else if (signin.getStatus().equals("accountdeleted")) {
                            Log.d(TAG, "onResponse: 572");
                            hideLoading();
                            opendeleteDialog();
                        } else {
                            App.makeToast(getString(R.string.something_went_wrong));
                            Log.d(TAG, "onResponse: 12567");
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

    private void goToMain() {

        if (GetSet.getUserId() != null) {
            AppWebSocket.mInstance = null;
            AppWebSocket.getInstance(this);

            String lang = LocaleManager.getLanguageCode(getApplicationContext());
            String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            if(lang.equalsIgnoreCase("en")){
                changeLangugae("English", deviceId, getApplicationContext(), lang, lang);
            }else{
                changeLangugae("Arabic", deviceId,  getApplicationContext(), lang, lang);
            }
        }
        hideLoading();

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();

    }

    private void showLoading() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }


    private void opendeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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

    private void openContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(getString(R.string.response_del_acc));
        builder.setPositiveButton(getString(R.string.contact_us), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                sendEmail();
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
        dialog.setCancelable(true);
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


    private void sendEmail() {
        try {

            String reportContent = "";
            if (LocaleManager.isRTL()) {
                reportContent = "\n\n" +"\nيرجى كتابة ملاحظاتك فوق هذا السطر"+
                        "رمز إصدار نظام تشغيل الجهاز: " + Build.VERSION.SDK_INT + "\n" +
                        "اسم رمز إصدار الجهاز: " + Build.VERSION.CODENAME + "\n" +
                        "اسم الجهاز: " + AppUtils.getDeviceName() + "\n" +
                        "رمز الإصدار: " + BuildConfig.VERSION_CODE + "\n" +
                        "اسم الإصدار: " + BuildConfig.VERSION_NAME + "\n" +
                        "اسم العبوة: " + BuildConfig.APPLICATION_ID + "\n" +
                        "نوع المبنى: " + BuildConfig.BUILD_TYPE;
            }else {
                reportContent = "\n\n"+"Please write your feedback above this line.\n" +

                        "DEVICE OS VERSION CODE: " + Build.VERSION.SDK_INT + "\n" +
                        "DEVICE VERSION CODE NAME: " + Build.VERSION.CODENAME + "\n" +
                        "DEVICE NAME: " + AppUtils.getDeviceName() + "\n" +
                        "VERSION CODE: " + BuildConfig.VERSION_CODE + "\n" +
                        "VERSION NAME: " + BuildConfig.VERSION_NAME + "\n" +
                        "PACKAGE NAME: " + BuildConfig.APPLICATION_ID + "\n" +
                        "BUILD TYPE: " + BuildConfig.BUILD_TYPE;
            }

            final Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{"" + AdminData.contactEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            emailIntent.putExtra(Intent.EXTRA_TEXT, reportContent);
            try {
                //start email intent
                startActivity(Intent.createChooser(emailIntent, "Email"));
            } catch (Exception e) {
                //if any thing goes wrong for example no email client application or any exception
                //get and show exception message
                App.makeToast(e.getMessage());
            }
        } catch (Exception e) {
            Logging.e(TAG, "sendEmail: " + e.getMessage());
        }
    }

    private void addDeviceID() {
        /*OSDeviceState deviceState = OneSignal.getDeviceState();
        if (deviceState != null) {
            oneSignalId = deviceState.getUserId();
            Log.i(TAG, "addDeviceId: " + oneSignalId);
        }*/
        final String token = DevicePref.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        AddDeviceRequest request = new AddDeviceRequest();
        request.setUserId(GetSet.getUserId());
        request.setDeviceToken(token);
        //request.setDeviceToken(oneSignalId);
        request.setDeviceType(Constants.TAG_DEVICE_TYPE);
        request.setDeviceId(deviceId);
        request.setDeviceModel(AppUtils.getDeviceName());
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
        dbHelper.addAdminMessage(messageData);
        messageData.setMsgAt(createdAt);
        messageData.setCreateaAt(msgAt);
        int unseenCount = dbHelper.getUnseenMessagesCount(GetSet.getUserId());
        unseenCount = unseenCount + 1;
        dbHelper.addAdminRecentMessage(messageData, unseenCount);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onactivity");
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }
//    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
//        Log.v(TAG, "handleSignInResult:" + completedTask.isSuccessful());
//        if (completedTask.isSuccessful()) {
//            // Signed in successfully, show authenticated UI.
//            try {
//                GoogleSignInAccount acct = completedTask.getResult(ApiException.class);
//
//                String personPhoto = "";
//
//                if (acct.getPhotoUrl() == null) {
//                    personPhoto = "";
//
//                } else {
//                    personPhoto = acct.getPhotoUrl().toString();
//                }
//
//                Constants.LOGINMETHODE = "google";
//                Constants.LOGIN_NAME   = acct.getDisplayName();
//
//                HashMap<String, String> gplusData = new HashMap<String, String>();
//                Constants.Mailid =  acct.getEmail();
//                gplusData.put(Constants.TAG_TYPE, "google");
//                gplusData.put(Constants.TAG_EMAIL, acct.getEmail());
//                gplusData.put(Constants.TAG_ID, acct.getId());
//                gplusData.put(Constants.TAG_FIRST_NAME, acct.getDisplayName());
//                gplusData.put(Constants.TAG_LAST_NAME, "");
//                gplusData.put(Constants.TAG_IMAGE_URL, personPhoto);
//
//                try {
//                    dialog.show();
//                } catch (WindowManager.BadTokenException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//
//                Google_signIn(personPhoto,acct.getDisplayName(), acct.getEmail(),acct.getId(),"google");

    /// /                sentDetails(gplusData);
//
//                Log.v(TAG, "personName" + acct.getDisplayName());
//                Log.v(TAG, "personPhoto" + personPhoto);
//                Log.v(TAG, "email" + acct.getEmail());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.v(TAG, "handleSignInResult:" + completedTask.isSuccessful());

        if (completedTask.isSuccessful()) {
            try {
                GoogleSignInAccount acct = completedTask.getResult(ApiException.class);

                String personPhoto = (acct.getPhotoUrl() != null) ? acct.getPhotoUrl().toString() : "";

                Constants.LOGINMETHODE = "google";
                Constants.LOGIN_NAME = acct.getDisplayName();

                Constants.Mailid = acct.getEmail();
                HashMap<String, String> gplusData = new HashMap<>();
                gplusData.put(Constants.TAG_TYPE, "google");
                gplusData.put(Constants.TAG_EMAIL, acct.getEmail());
                gplusData.put(Constants.TAG_ID, acct.getId());
                gplusData.put(Constants.TAG_FIRST_NAME, acct.getDisplayName());
                gplusData.put(Constants.TAG_LAST_NAME, "");
                gplusData.put(Constants.TAG_IMAGE_URL, personPhoto);

                try {
//                    dialog.show();
                } catch (WindowManager.BadTokenException e) {
                    e.printStackTrace();
                }

                Google_signIn(personPhoto, acct.getDisplayName(), acct.getEmail(), acct.getId(), "google");

                Log.v(TAG, "personName: " + acct.getDisplayName());
                Log.v(TAG, "personPhoto: " + personPhoto);
                Log.v(TAG, "email: " + acct.getEmail());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // ⛔ ADD THIS BLOCK to know WHY it's failing
            try {
                throw completedTask.getException();
            } catch (ApiException e) {
                Log.e(TAG, "Google sign-in failed with status code: " + e.getStatusCode(), e);

                switch (e.getStatusCode()) {
                    case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                        Log.e(TAG, "Sign in was cancelled by the user");
                        break;
                    case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                        Log.e(TAG, "Sign in failed");
                        break;
                    case GoogleSignInStatusCodes.NETWORK_ERROR:
                        Log.e(TAG, "Network error");
                        break;
                    case GoogleSignInStatusCodes.DEVELOPER_ERROR:
                        Log.e(TAG, "Developer error — check your Google API configuration");
                        break;
                    default:
                        Log.e(TAG, "Unhandled error");
                        break;
                }
            } catch (Exception ex) {
                Log.e(TAG, "Unexpected exception during sign-in", ex);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.ADDON_LOGIN_VIDEO) {
//            VideoAddon.initVideo(textureView, LoginActivity.this);
        }
        registerNetworkReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Constants.ADDON_LOGIN_VIDEO) {
//            VideoAddon.pauseMediaPlayer();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        if (Constants.ADDON_LOGIN_VIDEO) {
//            VideoAddon.stopMediaPlayer();
        }
        super.onDestroy();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        AppUtils.showSnack(this, parentLay, isConnected);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    /*@Override
    public void onOSSubscriptionChanged(OSSubscriptionStateChanges osSubscriptionStateChanges) {
        if (!osSubscriptionStateChanges.getFrom().isSubscribed() &&
                osSubscriptionStateChanges.getTo().isSubscribed()) {
            if (osSubscriptionStateChanges.getTo().getUserId() != null) {
                oneSignalId = osSubscriptionStateChanges.getTo().getUserId();
                Log.i(TAG, "onOSSubscriptionChanged: " + oneSignalId);
            }
        }

    }*/

    private void Google_signIn(String image, String name, String email, String loginId, String type) {
        if (NetworkReceiver.isConnected()) {
            showLoading();
            SignInRequest request = new SignInRequest();
            request.setLoginId(loginId);
            request.setType(type);
            request.setEmail(email);
            request.setName(name);
            request.setImage(image);
            Log.e("paramLogin", "-" + request);
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
                            } else {

                                // App.makeToast(signin.getLocation());

                                GetSet.setEmail(signin.getEmail());
                                GetSet.setFullName(signin.getFull_name());
                                GetSet.setPassword("");
                                GetSet.setImageUrl(signin.getPhoto());
                                GetSet.setRating(signin.getRating());
                                GetSet.setRatingUserCount(signin.getRating_user_count());

                                GetSet.setUserId(signin.getUserId());
                                GetSet.setLoginId(signin.getLoginId());
                                GetSet.setName(signin.getName());
                                GetSet.setDob(signin.getDob());
                                GetSet.setAge(signin.getAge());
                                GetSet.setGender(signin.getGender());

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
                            hideLoading();
                            Log.d(TAG, "onResponse: 12");
                            App.makeToast(getString(R.string.contact_admin_desc));
                            openContactDialog();
                        } else if (signin.getStatus().equals("accountdeleted")) {
                            Log.d(TAG, "onResponse: 132");
                            hideLoading();
                            opendeleteDialog();
                        } else {
                            Log.d(TAG, "onResponse: 1112");
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


    public void showBottomSheet(Activity activity) {
        // Create dialog only once
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.SecondaryBottomSheet);

        // Inflate view
        View view = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_privacy, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCanceledOnTouchOutside(true);

        // Set up views
        List<LanguageData> languageList = new ArrayList<>();
        RecyclerView recycler = view.findViewById(R.id.recyclerPrivacy);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        LinearLayout privacyLayout = view.findViewById(R.id.privacyLayout);
        privacyLayout.setBackgroundColor(getApplication().getColor(R.color.colorPrimaryDark));
        tvTitle.setText(getApplicationContext().getString(R.string.language));

        // Load language data
        String[] resourceArray = getResources().getStringArray(R.array.language_array);
        List<String> tempList = Arrays.asList(resourceArray);
        for (String list : tempList) {
            String[] lang = list.split("-");
            LanguageData data = new LanguageData();
            data.language = lang[0];
            data.languageCode = lang[1];
            languageList.add(data);
        }

        // Set adapter
        recycler.setLayoutManager(new LinearLayoutManager(activity));
        LanguageAdapter languageAdapter = new LanguageAdapter(activity, languageList);
        recycler.setAdapter(languageAdapter);

        // Show bottom sheet
        bottomSheetDialog.show();
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
