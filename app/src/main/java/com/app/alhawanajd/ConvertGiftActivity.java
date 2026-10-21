package com.app.alhawanajd;

import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.LoadAdError;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.OnOkCancelClickListener;
import com.app.model.ConvertGiftRequest;
import com.app.model.ConvertGiftResponse;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.utils.AdminData;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class ConvertGiftActivity extends BaseFragmentActivity {

    private static final String TAG = ConvertGiftActivity.class.getSimpleName();

    /*@BindView(R.id.btnEdit)
    ImageView btnEdit;*/

    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtGiftsCount)
    TextView txtGiftsCount;
    @BindView(R.id.txtPriceDetail)
    TextView txtPriceDetail;
    @BindView(R.id.txtCashDetail)
    TextView txtCashDetail;

    @BindView(R.id.countDisplay)
    TextView countDisplay;

    @BindView(R.id.btnWithdraw)
    Button btnWithdraw;
    ApiInterface apiInterface;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    /*@BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;*/
//    @BindView(R.id.adView)
//    AdView adView;
    @BindView(R.id.btnGems)
    RadioButton btnGems;
    @BindView(R.id.btnCash)
    RadioButton btnCash;
    AlertDialog dialog;
    /* @BindView(R.id.optionLay)
     RadioGroup optionLay;*/
   /* @BindView(R.id.optionLay)
    LinearLayout optionLay;*/
    @BindView(R.id.optionCoins)
    LinearLayout optionCoins;
    @BindView(R.id.optionCash)
    LinearLayout optionCash;

    @BindView(R.id.topLay)
    LinearLayout topLay;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.nullLay1)
    RelativeLayout nullLay;

    @BindView(R.id.nullText)
    TextView nullText;


    DialogCreditGems alertDialog;
    private AppUtils appUtils;

    ProfileResponse profileResponse;

    // Gems to money addon
    public String conversiongems = null;
    public String conversioncash = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_convert_gift);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);

        profileResponse = new ProfileResponse();

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        initView();
    }

    private void initView() {

        //commented for design approval progressBar.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
        btnBack.setImageDrawable(getDrawable(R.drawable.back));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = AppUtils.getStatusBarHeight(getApplicationContext());
        // toolbar.setLayoutParams(params);

        txtTitle.setText(getResources().getString(R.string.my_gifts));
        txtTitle.setTextColor(getResources().getColor(R.color.colorWhite));
        // btnSettings.setVisibility(View.INVISIBLE);

      /*commented on jan8  topLay.setAlpha(0.4f);
        topLay.setVisibility(View.GONE);
        btnBack.setEnabled(false);
        btnWithdraw.setEnabled(false);*/
        /*btnEdit.setVisibility(View.GONE);*/

        /*optionLay.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.btnGems) {
                // Handle Gems button selected
            } else if (checkedId == R.id.btnCash) {
                // Handle Cash button selected
            }
        });*/

        // default to Gems selected
        btnGems.setChecked(true);
        btnCash.setChecked(false);

        // ensure the UI texts are populated immediately
        updatePriceTexts();

    }

//    private void loadAd() {
//        if (AdminData.isAdEnabled()) {
    ////            MobileAds.initialize(this, AdminData.googleAdsId);
//            AdRequest adRequest = new AdRequest.Builder().build();
//            adView.loadAd(adRequest);
//            adView.setAdListener(new AdListener() {
//                @Override
//                public void onAdLoaded() {
//                    // Code to be executed when an ad finishes loading.
//                }
//
//                @Override
//                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                    super.onAdFailedToLoad(loadAdError);
//                }
//
//                @Override
//                public void onAdOpened() {
//                    // Code to be executed when an ad opens an overlay that
//                    // covers the screen.
//                }
//
//                @Override
//                public void onAdClicked() {
//                    // Code to be executed when the user clicks on an ad.
//                }
//
//                @Override
//                public void onAdClosed() {
//                    // Code to be executed when the user is about to return
//                    // to the app after tapping on an ad.
//                }
//            });
//        }
//    }

    @SuppressLint("StringFormatInvalid")
    private void getGems() {
        //txtGiftsCount  no use
        if (GetSet.getGifts() > 0) {
            countDisplay.setVisibility(View.VISIBLE);
            countDisplay.setText(String.valueOf(GetSet.getGifts()));
            txtGiftsCount.setVisibility(View.VISIBLE);
            btnWithdraw.setVisibility(View.VISIBLE);

            // optionLay.setVisibility((("" + AdminData.showMoneyConversion).equals("1")) ? View.VISIBLE : View.GONE);

            if ((AdminData.showMoneyConversion).equals("1")) {
                if (btnGems.isChecked()) {
                    if (LocaleManager.isRTL()) {
                        txtPriceDetail.setText("لديك" + "  " + GetSet.getGifts() + "  " + "هدايا تستحق" + "  " + "(" + GetSet.getGiftEarnings() + "عملات معدنية" + ")");
                    } else {
                        txtPriceDetail.setText(String.format(getString(R.string.gift_to_gems), "" + GetSet.getGifts(), GetSet.getGiftEarnings()));
                    }
                }
                else if (btnCash.isChecked()) {
                    if (profileResponse.getMoneyPriceValue() != null) {
                        if (profileResponse.getMoneyPriceValue() > 0) {
                            getcash();
                            if (LocaleManager.isRTL()) {
                                txtCashDetail.setText("لديك" +"  "+GetSet.getGifts()+"  "+"هدايا تستحق"+"  "+"("+String.valueOf(profileResponse.getMoneyPriceValue())+"$"+")");
                            }else{
                                txtCashDetail.setText(String.format(getString(R.string.gift_to_cash), "" + GetSet.getGifts(), String.valueOf(profileResponse.getMoneyPriceValue())));
                            }

                        } else {
                            txtCashDetail.setText(String.format(getString(R.string.gift_to_cash_no), "" + GetSet.getGifts()));
                        }
                    } else {
                        txtCashDetail.setText(String.format(getString(R.string.gift_to_cash_no), "" + GetSet.getGifts()));
                    }
                }
                /*getcash();*/
            } else {
                if (LocaleManager.isRTL()) {
                    txtPriceDetail.setText("لديك" +"  "+GetSet.getGifts()+"  "+"هدايا تستحق"+"  "+"("+GetSet.getGiftEarnings()+"عملات معدنية"+")");
                }else{
                    txtPriceDetail.setText(String.format(getString(R.string.gift_to_gems), "" + GetSet.getGifts(), GetSet.getGiftEarnings()));
                }

            }

        } else {
            txtPriceDetail.setText(getResources().getString(R.string.no_gift));
            txtPriceDetail.setVisibility(View.VISIBLE);

            txtCashDetail.setText(String.format(getString(R.string.gift_to_cash_no), "" + GetSet.getGifts()));

            btnWithdraw.setVisibility(View.INVISIBLE);
            countDisplay.setVisibility(View.INVISIBLE);
            //  optionLay.setVisibility(View.GONE);
        }

        topLay.setAlpha(1f);
        topLay.setVisibility(View.VISIBLE);
        btnBack.setEnabled(true);
        btnWithdraw.setEnabled(true);

        progressBar.setVisibility(View.GONE);
    }

    @SuppressLint("StringFormatInvalid")
    private void getcash() {

        Double gems = Double.valueOf(profileResponse.getMoneyPriceValue());
        if (LocaleManager.isRTL()) {
            txtCashDetail.setText("لديك" +"  "+gems+"  "+"هدايا تستحق"+"  "+"("+profileResponse.getMoneyCurrencySymbol()+"$"+")");
        }else{
            txtCashDetail.setText(format(getString(R.string.gift_to_cash), " " + gems, "" + profileResponse.getMoneyCurrencySymbol()));
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

    @OnClick({R.id.btnBack, R.id.btnWithdraw, R.id.btnGems, R.id.btnCash})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnGems:
                btnGems.setChecked(true);
                btnCash.setChecked(false);
                if (LocaleManager.isRTL()) {
                    txtPriceDetail.setText("لديك" +"  "+GetSet.getGifts()+"  "+"هدايا تستحق"+"  "+"("+GetSet.getGiftEarnings()+"عملات معدنية"+")");
                }else{
                    txtPriceDetail.setText(String.format(getString(R.string.gift_to_gems), "" + GetSet.getGifts(), GetSet.getGiftEarnings()));
                }
                btnWithdraw.setText(getString(R.string.convert_gems));
                break;
            case R.id.btnCash:
                btnCash.setChecked(true);
                btnGems.setChecked(false);
                if (profileResponse.getMoneyPriceValue() != null) {
                    if (profileResponse.getMoneyPriceValue() > 0) {
                        getcash();


                        if (LocaleManager.isRTL()) {
                            txtCashDetail.setText("لديك" +"  "+GetSet.getGifts()+"  "+"هدايا تستحق"+"  "+"("+profileResponse.getMoneyPriceValue()+"$"+")");
                        }else{
                            txtCashDetail.setText(String.format(getString(R.string.gift_to_cash), "" + GetSet.getGifts(), String.valueOf(profileResponse.getMoneyPriceValue())));
                        }

                    } else {
                        txtCashDetail.setText(String.format(getString(R.string.gift_to_cash_no), "" + GetSet.getGifts()));
                    }
                } else {
                    txtCashDetail.setText(String.format(getString(R.string.gift_to_cash_no), "" + GetSet.getGifts()));
                }

                btnWithdraw.setText(R.string.convert_money);
                break;
            case R.id.btnWithdraw:
                if (GetSet.getGifts() > 0) {
                    if (btnGems.isChecked()) {
                        App.preventMultipleClick(btnWithdraw);
                        showConfirmDialog("");
                    } else {
                        if (btnCash.isChecked()) {

                            if (SharedPref.getString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id()) != null && !SharedPref.getString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id()).equals("")) {
                                //     if (Float.parseFloat(profileResponse.getMoneyPriceValue()) > 0){
                                String pay = SharedPref.getString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id());
//                            showPayPalDialog();
                                //confirmDialog();

                                showConfirmDialog(pay);
                             /*   }else {
                                    App.makeToast(getString(R.string.gift_to_cash_not_enough));
                                }*/
                            } else {
                                App.makeToast(getString(R.string.update_paypal_id));
                                Intent profileIntent = new Intent(this, EditProfileActivity.class);
                                profileIntent.putExtra(Constants.TAG_PROFILE_DATA, profileResponse);
                                startActivityForResult(profileIntent, Constants.PROFILE_REQUEST_CODE);
                                Objects.requireNonNull(this).overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
                            }
                        }
                    }
                } else showAlertDialog(getString(R.string.you_dont_have_any_gifts));
                break;
        }
    }


    //
    private void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConvertGiftActivity.this, com.google.android.material.R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        builder.setMessage(getString(R.string.convert_gifts_to_money_desc));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                convertGems(dialog);
               /* String payPalId = (String) o;
                updatePayment(payPalId);*/
//                updatePayment(dialog);
                updatePayments(dialog);
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_light);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_light);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);
    }

    private void updatePayments(DialogInterface dialog) {
        if (NetworkReceiver.isConnected()) {

            String pay = SharedPref.getString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id());

            ConvertGiftRequest request = new ConvertGiftRequest();
            request.setGemsRequested(GetSet.getGiftCoversionEarnings());
            request.setUserId(GetSet.getUserId());
            request.setUserName(GetSet.getUserName());
            request.setPayPalId(pay);

            Log.i(TAG, "updatePaymentconver: " + request);
            Log.i(TAG, "updatePaymentconvert: " + profileResponse.getMoneyPriceValue() + GetSet.getUserId() + GetSet.getUserName() + GetSet.getPaypal_id());

            Timber.d("updatePayments: params %s", new Gson().toJson(request));

            Call<ConvertGiftResponse> call = apiInterface.convertToMoney(request);
            call.enqueue(new Callback<ConvertGiftResponse>() {
                @Override
                public void onResponse(Call<ConvertGiftResponse> call, Response<ConvertGiftResponse> response) {
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        Log.i(TAG, "updatePaymentconvertonResponse: " + response);
                        Toast.makeText(ConvertGiftActivity.this, "Waiting for Admin Approval...Money Will be transfer to your Account.", Toast.LENGTH_SHORT).show();
                        Log.e("Successful: ", "Waiting for Admin Approval...Money Will be transfer to your Account.");
                        finish();
                    } else {
                        App.makeToast(getString(R.string.not_enough_gems));
                        /*dialogPayPal.hideLoading();
                        dialogPayPal.dismissAllowingStateLoss();*/
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ConvertGiftResponse> call, Throwable t) {
                    t.printStackTrace();
                    Log.i(TAG, "updatePaymentconvertonFailure: " + t.getMessage());
                }
            });

        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void showConfirmDialog(String paypalID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConvertGiftActivity.this);
        if (paypalID.equals(""))
            builder.setMessage(getString(R.string.convert_gifts_to_gems_desc));
        else builder.setMessage(getString(R.string.convert_gifts_to_money_desc));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                convertGems(dialog, paypalID);
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog = builder.create();
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

    private void convertGems(DialogInterface dialog, String paypal) {
        progressBar.setVisibility(View.VISIBLE);
        ConvertGiftRequest request = new ConvertGiftRequest();
        request.setUserId(GetSet.getUserId());
        request.setType(btnGems.isChecked() ? Constants.TAG_GEMS : Constants.TAG_CASH);
        if (btnCash.isChecked()) request.setPayPalId(paypal);


        Timber.d("convertGems: params %s", new Gson().toJson(request));

        Call<ConvertGiftResponse> call = apiInterface.convertGifts(request);
        call.enqueue(new Callback<ConvertGiftResponse>() {
            @Override
            public void onResponse(Call<ConvertGiftResponse> call, Response<ConvertGiftResponse> response) {

                dialog.cancel();

                Timber.d("convertGems: response status %s", response.body().getStatus());
                Timber.d("convertGems: response message %s", response.body().getMessage());

                if (response.body().getStatus().equals(Constants.TAG_TRUE)) {

                    if (btnGems.isChecked()) {
                        GetSet.setGems(response.body().getTotalGems());
                        SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                        getGems();
                        showAlertDialog(getString(R.string.gems_purchase_response));
                    } else if (btnCash.isChecked()) {
                        GetSet.setGems(Float.valueOf(0));
                        SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                        showAlertDialog(getString(R.string.request_sent_successfully));
                    }
                } else {

                    dialog.dismiss();
                    Toast.makeText(ConvertGiftActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ConvertGiftResponse> call, Throwable t) {
                Timber.d("onFailure: %s", t.getMessage());
                dialog.cancel();
            }
        });
    }

    private void showAlertDialog(String message) {
        alertDialog = new DialogCreditGems();
        alertDialog.setContext(ConvertGiftActivity.this);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(message);
        alertDialog.setCallBack(new OnOkCancelClickListener() {
            @Override
            public void onOkClicked(Object o) {
                alertDialog.dismissAllowingStateLoss();
                finish();
            }

            @Override
            public void onCancelClicked(Object o) {
                alertDialog.dismissAllowingStateLoss();
                finish();
            }
        });
        progressBar.setVisibility(View.GONE);
        alertDialog.show(getSupportFragmentManager(), TAG);
    }

    private void getProfile() {

        // if (isConnected && GetSet.isIsLogged()) {

        if (NetworkReceiver.isConnected()) {

            if (GetSet.getUserId() != null && apiInterface != null) {
                ProfileRequest request = new ProfileRequest();
                request.setUserId(GetSet.getUserId());
                request.setProfileId(GetSet.getUserId());
                Call<ProfileResponse> call = apiInterface.getProfile(request);
                call.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                        ProfileResponse profile = response.body();
                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {

                            Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(response.body()));


                            profileResponse = profile;
                            GetSet.setUserId(profile.getUserId());
                            /*GetSet.setLoginId(profile.getLoginId());*/
                            GetSet.setName(profile.getName());
                            GetSet.setUserName(profile.getUserName());
                            GetSet.setDob(profile.getDob());
                            GetSet.setAge(String.valueOf(profile.getAge()));
                            GetSet.setGender(profile.getGender());
                            GetSet.setUserImage(profile.getUserImage());
                            /*GetSet.setLocation(profile.getLocation());*/
                            GetSet.setFollowingCount(String.valueOf(profile.getFollowings()));
                            GetSet.setFollowersCount(String.valueOf(profile.getFollowers()));
                            GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(String.valueOf(profile.getAvailableGifts())) : 0L);
                            GetSet.setGems(profile.getAvailableGems() != null ? profile.getAvailableGems() : 0F);
                            GetSet.setVideos(profile.getVideosCount() != null ? Long.parseLong(String.valueOf(profile.getVideosCount())) : 0L);
                            /*GetSet.setVideosHistory(profile.getWatchedCount() != null ? profile.getWatchedCount() : 0L);*/
                            GetSet.setPremiumMember(profile.getPremiumMember());
                            GetSet.setPremiumExpiry(profile.getPremiumExpiryDate());
                            /*GetSet.setPrivacyAge(profile.getPrivacyAge());*/
                            GetSet.setPrivacyContactMe(String.valueOf(profile.getUserCanMessage()));
                            /*GetSet.setShowNotification(profile.getShowNotification());*/
                            GetSet.setFollowNotification(String.valueOf(profile.getFollowNotification()));
                            GetSet.setChatNotification(String.valueOf(profile.getChatNotification()));
                            GetSet.setGiftEarnings(String.valueOf(profile.getGiftEarnings()));
                            GetSet.setReferalLink(profile.getReferalLink());
                            GetSet.setCreatedAt(profile.getCreatedAt());
                            GetSet.setBio(profile.getBio());
                            GetSet.setLikes(profile.getLikes());

                            GetSet.setAge(String.valueOf(profile.getAge()));
                            GetSet.setPostCommand(profile.getCommentPrivacy());
                            GetSet.setSendMessage(profile.getMessagePrivacy());

                            GetSet.setPaypal_id(profile.getPaypalId());

                            SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                            SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());
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
                            SharedPref.putLong(SharedPref.VIDEOS_HISTORY, GetSet.getVideosHistory());
                            SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                            SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                            SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                            SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                            SharedPref.putString(SharedPref.SHOW_NOTIFICATION, GetSet.getShowNotification());
                            SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                            SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                            SharedPref.putBoolean(SharedPref.INTEREST_NOTIFICATION, GetSet.getInterestNotification());
                            SharedPref.putString(SharedPref.GIFT_EARNINGS, GetSet.getGiftEarnings());
                            SharedPref.putString(SharedPref.REFERAL_LINK, GetSet.getReferalLink());
                            SharedPref.putString(SharedPref.CREATED_AT, GetSet.getCreatedAt());

                            SharedPref.putString(SharedPref.POST_COMMAND, GetSet.getPostCommand());
                            SharedPref.putString(SharedPref.SEND_MESSAGE, GetSet.getSendMessage());

//                            loadAd();
                            getGems();


                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
                        call.cancel();
                    }
                });
            }
        } else {
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getString(R.string.no_internet_connection));
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
        getProfile();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    private void updatePriceTexts() {
        // Ensure views visible
        txtPriceDetail.setVisibility(View.VISIBLE);
        txtCashDetail.setVisibility(View.VISIBLE);

        long gifts = GetSet.getGifts(); // might be 0 if not loaded yet
        String giftEarnings = GetSet.getGiftEarnings() != null ? GetSet.getGiftEarnings() : "0";

        // If server-side conversion toggle exists
        boolean moneyConversionEnabled = ("1".equals(AdminData.showMoneyConversion));

        // Gems (coins) text — always show using local cached GetSet
        if (LocaleManager.isRTL()) {
            txtPriceDetail.setText("لديك" + "  " + gifts + "  " + "هدايا تستحق" + "  " + "(" + giftEarnings + "عملات معدنية" + ")");
        } else {
            txtPriceDetail.setText(String.format(getString(R.string.gift_to_gems), "" + gifts, giftEarnings));
        }

        // Cash text — depends on profileResponse.moneyPriceValue (may be null until getProfile returns)
        if (moneyConversionEnabled) {
            if (profileResponse != null && profileResponse.getMoneyPriceValue() != null && profileResponse.getMoneyPriceValue() > 0) {
                // show converted cash value
                double moneyValue = profileResponse.getMoneyPriceValue();
                if (LocaleManager.isRTL()) {
                    txtCashDetail.setText("لديك" + "  " + gifts + "  " + "هدايا تستحق" + "  " + "(" + String.valueOf(moneyValue) + profileResponse.getMoneyCurrencySymbol() + ")");
                } else {
                    txtCashDetail.setText(String.format(getString(R.string.gift_to_cash), "" + gifts, String.valueOf(moneyValue)));
                }
            } else {
                // show fallback (no cash conversion available)
                txtCashDetail.setText(String.format(getString(R.string.gift_to_cash_no), "" + gifts));
            }
        } else {
            // if conversion disabled, show same as gems (or whatever you prefer)
            if (LocaleManager.isRTL()) {
                txtCashDetail.setText("لديك" + "  " + gifts + "  " + "هدايا تستحق" + "  " + "(" + giftEarnings + "عملات معدنية" + ")");
            } else {
                txtCashDetail.setText(String.format(getString(R.string.gift_to_gems), "" + gifts, giftEarnings));
            }
        }
    }

}
