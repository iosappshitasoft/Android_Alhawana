package com.app.alhawanajd;

import static com.app.alhawanajd.App.isPreventMultipleClick;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

//import com.google.android.gms.ads.AdView;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.alhawanajd.profile.FavouritesActivity;
import com.app.helper.AppWebSocket;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.model.GetSet;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("NonConstantResourceId")
public class SettingsActivity extends BaseFragmentActivity implements View.OnClickListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    ImageView btnBack;
    TextView txtTitle;
    TextView txtSubTitle;
    ImageView btnSettings;
    Toolbar toolbar;
    LinearLayout settingsLay;
    LinearLayout interestLay;
    LinearLayout privacyLay;
    LinearLayout inviteLay;
    LinearLayout helpLay;
    LinearLayout settingMainLay;
    /*@BindView(R.id.blockLay)
    LinearLayout blockLay;*/
    LinearLayout languageLay;
    LinearLayout bookmarkLay;
    LinearLayout logoutLay , del_accLay, coinsLay, giftsLay;
//    AdView adView;
    /*@BindView(R.id.ratingLay)
    LinearLayout ratingLay;*/
    private ApiInterface apiInterface;
    private DialogInterest interestDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        findViews();
        initView();
    }

    private void findViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        txtSubTitle = findViewById(R.id.txtSubTitle);
        btnSettings = findViewById(R.id.btnSettings);
        settingsLay = findViewById(R.id.settingsLay);
        interestLay = findViewById(R.id.interestLay);
        privacyLay = findViewById(R.id.privacyLay);
        inviteLay = findViewById(R.id.inviteLay);
        helpLay = findViewById(R.id.helpLay);
        bookmarkLay = findViewById(R.id.bookmarkLay);
        settingMainLay = findViewById(R.id.settingMainLay);
        languageLay = findViewById(R.id.languageLay);
        logoutLay = findViewById(R.id.logoutLay);
        del_accLay = findViewById(R.id.del_accLay);
        coinsLay = findViewById(R.id.coinsLay);
        giftsLay = findViewById(R.id.giftsLay);
//        adView = findViewById(R.id.adView);

        btnBack.setOnClickListener(this);
        settingsLay.setOnClickListener(this);
        interestLay.setOnClickListener(this);
        privacyLay.setOnClickListener(this);
        inviteLay.setOnClickListener(this);
        helpLay.setOnClickListener(this);
        logoutLay.setOnClickListener(this);
        del_accLay.setOnClickListener(this);
        languageLay.setOnClickListener(this);
        bookmarkLay.setOnClickListener(this);
        giftsLay.setOnClickListener(this);
        coinsLay.setOnClickListener(this);
    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

        txtTitle.setText(getString(R.string.privacy_settings));
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
        loadAd();
    }

    private void loadAd() {
//        BannerAdUtils.loadBannerAd(this, (FrameLayout) findViewById(R.id.adLay));
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onClick(View view) {
        if (isPreventMultipleClick())
            return;
        switch (view.getId()) {
            case R.id.btnBack:
                App.preventMultipleClick(btnBack);
                onBackPressed();
                break;
            case R.id.settingsLay:
                App.preventMultipleClick(settingsLay);
                Intent settings = new Intent(getApplicationContext(), GeneralSettingsActivity.class);
                startActivity(settings);
                break;
            case R.id.interestLay:
                App.preventMultipleClick(interestLay);
                showInterestDialog();
                break;
            case R.id.privacyLay:
                App.preventMultipleClick(privacyLay);
                GetSet.setSendMessage(SharedPref.getString(SharedPref.SEND_MESSAGE, "Everyone"));
                GetSet.setPostCommand(SharedPref.getString(SharedPref.POST_COMMAND, "Everyone"));
                Intent privacy = new Intent(getApplicationContext(), AccountPrivacy.class);
                startActivity(privacy);
                break;
            case R.id.inviteLay:
                App.preventMultipleClick(inviteLay);
                Intent invite = new Intent(getApplicationContext(), EarnGemsActivity.class);
                startActivity(invite);
                break;
            case R.id.helpLay:
                App.preventMultipleClick(helpLay);
                Intent help = new Intent(getApplicationContext(), HelpActivity.class);
                startActivity(help);
                break;
            /*case R.id.blockLay: {
                Intent intent = new Intent(getApplicationContext(), BlockedUsersActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
            break;*/
            case R.id.languageLay:
                App.preventMultipleClick(languageLay);
                Intent Language = new Intent(getApplicationContext(), LanguageActivity.class);
                startActivity(Language);
                break;
            case R.id.bookmarkLay: {
                App.preventMultipleClick(bookmarkLay);
                Intent intent = new Intent(getApplicationContext(), FavouritesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
            break;
            case R.id.logoutLay:
                App.preventMultipleClick(logoutLay);
                openLogoutDialog();
                break;
            case R.id.coinsLay:
                App.preventMultipleClick(coinsLay);
                Intent gemsIntent = new Intent(getApplicationContext(), GemsStoreActivity.class);
                gemsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(gemsIntent);
                break;
            case R.id.giftsLay:
                App.preventMultipleClick(giftsLay);
                Intent giftIntent = new Intent(getApplicationContext(), ConvertGiftActivity.class);
                giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(giftIntent);
                break;
            case R.id.del_accLay:
                App.preventMultipleClick(logoutLay);
                opendeleteDialog();
            /*case R.id.ratingLay:
                App.preventMultipleClick(ratingLay);
                AppRate.with(SettingsActivity.this)
                        .setStoreType(StoreType.GOOGLEPLAY) //default is Google, other option is Amazon
                        .setInstallDays(3) // default 10, 0 means install day.
                        .setLaunchTimes(10) // default 10 times.
                        .setRemindInterval(2) // default 1 day.
                        .setShowLaterButton(false) // default true.
                        .setDebug(false) // default false.
                        .setCancelable(false) // default false.
                        .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                            @Override
                            public void onClickButton(int which) {

                            }
                        })
                        .setTitle(R.string.rate_dialog_title)
                        .setMessage(R.string.rate_dialog_message)
                        .setTextLater(R.string.rate_dialog_cancel)
                        .setTextNever(R.string.rate_dialog_no)
                        .setTextRateNow(R.string.rate_dialog_ok)
                        .monitor();

                AppRate.showRateDialogIfMeetsConditions(this);
                break;*/
        }
//        setDisabledView();
    }

    /*private void setDisabledView(){
        App.preventMultipleClick(logoutLay);
        App.preventMultipleClick(btnBack);
        App.preventMultipleClick(helpLay);
        App.preventMultipleClick(settingsLay);
        App.preventMultipleClick(privacyLay);
        App.preventMultipleClick(inviteLay);
        App.preventMultipleClick(languageLay);
    }*/

    private void showInterestDialog() {

        interestDialog = new DialogInterest();
        interestDialog.setFrom(Constants.TAG_PROFILE);
        interestDialog.setContext(SettingsActivity.this);

        interestDialog.setCallBack((o, trim) -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
        interestDialog.setCancelable(false);
        interestDialog.show(getSupportFragmentManager(), TAG);
    }

    private void openLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setMessage(getString(R.string.really_want_logout));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                removeDeviceID(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            }
        });
        builder.setNegativeButton(getString(R.string.no_txt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
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

    //Delete Account

    private void opendeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setMessage(getString(R.string.really_want_del_acc));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                delete_account();
            }
        });
        builder.setNegativeButton(getString(R.string.no_txt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
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


    public void delete_account() {
        ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
        if (NetworkReceiver.isConnected()) {
            mExecutorService.execute(new Runnable() {
                public void run() {
                    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                    Call<HashMap<String, String>> call = apiInterface.delete_account(GetSet.getUserId());
                    call.enqueue(new Callback<HashMap<String, String>>() {
                        @Override
                        public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                            Log.i(TAG, "Account_delete: " + response);

                            overridePendingTransition(0, 0);
                            GetSet.reset();
                            SharedPref.clearAll();
                            DBHelper.getInstance(SettingsActivity.this).clearDB();
                            AppWebSocket.getInstance(SettingsActivity.this).disconnect();
                            Intent logout = new Intent(getApplicationContext(), LoginActivity.class);
                            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(logout);
                            finish();
                            App.makeToast(getString(R.string.delete_acc_successfully));
                        }

                        @Override
                        public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

                        }


                    });
                }
            });
        }
    }

    //logout
    private void removeDeviceID(String deviceId) {
        Call<HashMap<String, String>> call = apiInterface.pushSignOut(deviceId);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                overridePendingTransition(0, 0);
                GetSet.reset();
                SharedPref.clearAll();
                DBHelper.getInstance(SettingsActivity.this).clearDB();
                AppWebSocket.getInstance(SettingsActivity.this).disconnect();
                Intent logout = new Intent(getApplicationContext(), LoginActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logout);
                finish();
                App.makeToast(getString(R.string.logged_out_successfully));
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG,"onBackPressed : " + Constants.SHOULD_RECREATE_ACTIVITIES_ON_RESUME);
        if (interestDialog != null && interestDialog.isAdded()) {
            interestDialog.dismiss();
        } else {
            /*if (Constants.SHOULD_RECREATE_ACTIVITIES_ON_RESUME) {
                recreate();
                Constants.SHOULD_RECREATE_ACTIVITIES_ON_RESUME = false;
            } else {
                super.onBackPressed();
            }*/
            super.onBackPressed();
        }
    }
}
