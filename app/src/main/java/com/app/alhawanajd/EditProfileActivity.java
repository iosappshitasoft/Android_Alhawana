package com.app.alhawanajd;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import com.app.alhawanajd.databinding.ActivityEditProfileBinding;
import com.bumptech.glide.Glide;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
import com.google.android.material.card.MaterialCardView;
import com.app.alhawanajd.R;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class EditProfileActivity extends BaseFragmentActivity {

    private static final String TAG = "EditProfileActivity";

   /* @BindView(R.id.profileImage)
    ImageView profileImage;

    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.txtTitle)
    TextView txtTitle;

    @BindView(R.id.txtPrimeTitle)
    TextView txtPrimeTitle;
    @BindView(R.id.txtPrice)
    TextView txtPrice;
    @BindView(R.id.premiumImage)
    ImageView premiumImage;

    @BindView(R.id.edtmobile)
    EditText edtmobile;
    @BindView(R.id.premiumLay)
    RelativeLayout premiumLay;
    @BindView(R.id.btnSubscribe)
    Button btnSubscribe;
    @BindView(R.id.primeBgLay)
    RelativeLayout primeBgLay;
    @BindView(R.id.subscribeLay)
    MaterialCardView subscribeLay;
    @BindView(R.id.txtRenewalTitle)
    TextView txtRenewalTitle;
    @BindView(R.id.txtRenewalDes)
    TextView txtRenewalDes;
    @BindView(R.id.btnRenewal)
    AppCompatButton btnRenewal;
    @BindView(R.id.renewalBgLay)
    RelativeLayout renewalBgLay;

    @BindView(R.id.renewalLay)
    MaterialCardView renewalLay;
    @BindView(R.id.edtName)
    EditText edtName;
    @BindView(R.id.edtemail)
    EditText edtemail;




    @BindView(R.id.edtusername)
    TextView edtusername;
    @BindView(R.id.txtGender)
    EditText txtGender;
    @BindView(R.id.txtDob)
    EditText txtDob;
    @BindView(R.id.edtBio)
    EditText edtBio;

    @BindView(R.id.edtPayPal)
    EditText edtPayPal;

    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
//    @BindView(R.id.adView)
//    AdView adView;
    @BindView(R.id.parentLay)
    RelativeLayout parentLay;

    @BindView(R.id.txtEmail)
    EditText txtEmail;*/



    private ApiInterface apiInterface;
    private ProfileResponse profileResponse;
    private AppUtils appUtils;

    /*RequestOptions profileImageRequest = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .dontAnimate();*/

    private String strLocation;
    private ActivityEditProfileBinding binding; // ADD THIS LINE


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
      //  setContentView(R.layout.activity_edit_profile);
       // ButterKnife.bind(this);


        // REPLACE your setContentView and ButterKnife.bind
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        profileResponse = (ProfileResponse) getIntent().getSerializableExtra(Constants.TAG_PROFILE_DATA);
        Log.d("ProfileData", profileResponse.toString());



        initView();

        if (GetSet.getUserId() != null) {
            profileResponse = new ProfileResponse();
            profileResponse.setUserId(GetSet.getUserId());
            /*profileResponse.setLoginId(GetSet.getLoginId());*/
            profileResponse.setName(GetSet.getName());

            profileResponse.setUserName(GetSet.getUserName());
            profileResponse.setUserImage(GetSet.getUserImage());
            profileResponse.setDob(GetSet.getDob());
            profileResponse.setGender(GetSet.getGender());
            /*profileResponse.setAge(GetSet.getAge());*/
            profileResponse.setFollowings(Integer.valueOf(GetSet.getFollowingCount()));
            profileResponse.setFollowers(Integer.valueOf(GetSet.getFollowersCount()));
            /*profileResponse.setLocation(GetSet.getLocation());
            profileResponse.setLoginId(GetSet.getLoginId());*/
            profileResponse.setAvailableGems(GetSet.getGems());
            profileResponse.setAvailableGifts(Integer.valueOf("" + GetSet.getGifts()));
            profileResponse.setVideosCount(Integer.valueOf("" + GetSet.getVideos()));
            /*profileResponse.setWatchedCount(GetSet.getVideosHistory());*/
            profileResponse.setPremiumMember(GetSet.getPremiumMember());
            profileResponse.setPremiumExpiryDate(GetSet.getPremiumExpiry());
            profileResponse.setPhone_number(GetSet.getGetPhonenumber());

            /*profileResponse.setPrivacyAge(GetSet.getPrivacyAge());*/
            /*profileResponse.setUserCanMessage(GetSet.geti());*/

            profileResponse.setBio(GetSet.getBio());
            profileResponse.setLikes(GetSet.getLikes());
            profileResponse.setAge(Integer.valueOf(GetSet.getAge()));
            profileResponse.setPaypalId(GetSet.getPaypal_id());


            setProfile(profileResponse);
        } /*else {

        }*/




        binding.edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() >=30){
                    Toast.makeText(EditProfileActivity.this, getString(R.string.reachlimt), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

        appUtils = new AppUtils(this);
        binding.edtName.setFilters(new InputFilter[]{AppUtils.SPECIAL_CHARACTERS_FILTER, new InputFilter.LengthFilter(Constants.MAX_LENGTH)});
        binding.edtPayPal.setFilters(new InputFilter[]{AppUtils.EMOJI_FILTER, new InputFilter.LengthFilter(Constants.MAX_LENGTH)});
        binding.edtName.setSelection(binding.edtName.getText().length());

        binding.edtName.requestFocus();

        if (LocaleManager.isRTL()) {
            binding.btnBack.setRotation(180);
        } else {
            binding.btnBack.setRotation(0);
        }

        binding.btnBack.setVisibility(View.VISIBLE);
        binding.btnBack.setImageResource(R.drawable.back);
        binding.txtTitle.setText("Edit Profile");
        // paymentLay.setVisibility((AdminData.showMoneyConversion.equals("1")) ? View.VISIBLE : GONE);

        /*edtBio.setOnKeyListener((v, keyCode, event) -> {
            Timber.d("initView: %s", keyCode);
            return false;
        });*/

//        loadAd();

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(binding.edtName.getText().toString()))
                    binding.edtName.setError(getString(R.string.enter_name));
                hideKeyboard(EditProfileActivity.this);
                saveProfile();
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(EditProfileActivity.this);
                onBackPressed();
            }
        });

        binding.primeBannerLayout.btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    if (NetworkReceiver.isConnected()) {
                        Intent prime = new Intent(getApplicationContext(), PrimeActivity.class);
                        startActivity(prime);
                    } else {
                        App.makeToast(getString(R.string.no_internet_connection));
                       // Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
                    }

                }
            }
        });

        binding.primeBannerLayout.subscribeLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    if (NetworkReceiver.isConnected()) {
                        Intent prime = new Intent(getApplicationContext(), PrimeActivity.class);
                        startActivity(prime);
                    } else {
                        App.makeToast(getString(R.string.no_internet_connection));
                       // Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
                    }

                }
            }
        });

        binding.renewalBannerLayout.btnRenewal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    if (NetworkReceiver.isConnected()) {
                        Intent prime = new Intent(getApplicationContext(), PrimeActivity.class);
                        startActivity(prime);
                    } else {
                        App.makeToast(getString(R.string.no_internet_connection));
                       // Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
                    }

                }
            }
        });

        binding.renewalBannerLayout.renewalLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    if (NetworkReceiver.isConnected()) {
                        Intent prime = new Intent(getApplicationContext(), PrimeActivity.class);
                        startActivity(prime);
                    } else {
                        App.makeToast(getString(R.string.no_internet_connection));
                      //  Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
                    }

                }
            }
        });

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.preventMultipleClick(binding.profileImage);
                Intent intent = new Intent(getApplicationContext(), ImageViewActivity.class);
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
                overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);

            }
        });

        binding.edtPayPal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.edtPayPal.requestFocus();
                binding.edtPayPal.setFocusableInTouchMode(true);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(binding.edtPayPal, InputMethodManager.SHOW_FORCED);

            }
        });
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


   /* @OnClick({R.id.btnSave, R.id.btnBack, R.id.profileImage, R.id.btnSubscribe, R.id.subscribeLay,
            R.id.btnRenewal, R.id.renewalLay *//*R.id.txtLocation,*//*, R.id.edtPayPal})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            *//*case R.id.btnSave:
                if (TextUtils.isEmpty(binding.edtName.getText().toString()))
                    binding.edtName.setError(getString(R.string.enter_name));
                hideKeyboard(EditProfileActivity.this);
                saveProfile();
                break;
            case R.id.btnBack:
                hideKeyboard(EditProfileActivity.this);
                onBackPressed();
                break;
            case R.id.btnSubscribe:
            case R.id.subscribeLay:
            case R.id.btnRenewal:
            case R.id.renewalLay:
                if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    if (NetworkReceiver.isConnected()) {
                        Intent prime = new Intent(getApplicationContext(), PrimeActivity.class);
                        startActivity(prime);
                    } else {
                        App.makeToast(getString(R.string.no_internet_connection));
                        Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
                    }

                }
                break;
            case R.id.profileImage:
                App.preventMultipleClick(binding.profileImage);
                Intent intent = new Intent(getApplicationContext(), ImageViewActivity.class);
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
                overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);
                break;
            case R.id.edtPayPal:

                binding.edtPayPal.requestFocus();
                binding.edtPayPal.setFocusableInTouchMode(true);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(binding.edtPayPal, InputMethodManager.SHOW_FORCED);
                break;*//*
            *//*case R.id.txtLocation:*//*
            //case R.id.BioLay:
                *//*Intent location = new Intent(this, LocationFilterActivity.class);
                location.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                location.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                location.putExtra(Constants.TAG_LOCATION, strLocation != null ? strLocation : "");
                startActivityForResult(location, Constants.LOCATION_REQUEST_CODE);
           *//*
            //  break;
        }
    }
*/

    private void saveProfile() {
        if (NetworkReceiver.isConnected()) {
            showLoading();

            String email = binding.edtemail.getText().toString().trim();
            String phone = binding.edtmobile.getText().toString().trim();

            if (email.isEmpty()) {
                hideLoading();
                Toast.makeText(this, getString(R.string.email_required), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                hideLoading();
                Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
                return;
            }

            if (phone.isEmpty()) {
                hideLoading();
                Toast.makeText(this, getString(R.string.phone_required), Toast.LENGTH_SHORT).show();
                return;
            }


            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());
            request.setName(binding.edtName.getText().toString());
            request.setPayPalId("" + binding.edtPayPal.getText());
            request.setBio(binding.edtBio.getText().toString());
            request.setEmail_id(email);
            request.setPhone_number(phone);
            request.setDate_of_birth(binding.txtDob.getText().toString());

            //request.setLocation("" + edtBio.getText());
            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {

                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        App.makeToast(getString(R.string.profile_updated_successfully));
//                        MainActivity.profileResponse = profile;
                        profileResponse = profile;
                        Log.d(TAG, "onResponse: profile..."+profile);
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
                        GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(String.valueOf(profile.getAvailableGifts())) : 0);
                        GetSet.setGems(profile.getAvailableGems() != null ? profile.getAvailableGems() : 0F);
                        GetSet.setVideos(profile.getVideosCount() != null ? Long.parseLong(String.valueOf(profile.getVideosCount())) : 0);
                        /*GetSet.setVideosHistory(profile.getWatchedCount() != null ? profile.getWatchedCount() : 0);*/
                        GetSet.setPremiumMember(profile.getPremiumMember());
                        GetSet.setPremiumExpiry(profile.getPremiumExpiryDate());
                        /*GetSet.setPrivacyAge(profile.getPrivacyAge());*/
                        GetSet.setPrivacyContactMe(profile.getBlockedByMe());
                        GetSet.setShowNotification(String.valueOf(profile.getShowNotification()));
                        GetSet.setFollowNotification(String.valueOf(profile.getFollowNotification()));
                        GetSet.setChatNotification(String.valueOf(profile.getChatNotification()));
                        GetSet.setGiftEarnings(String.valueOf(profile.getGiftEarnings()));
                        GetSet.setBio(profile.getBio());

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
                        SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                        SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());
                        SharedPref.putInt(SharedPref.UNLOCKS_LEFT, GetSet.getUnlocksLeft());
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
                        SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                        SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());
                        SharedPref.putString(SharedPref.BIO, GetSet.getBio());
                        SharedPref.putString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id());

                        SharedPref.putString(SharedPref.POST_COMMAND, GetSet.getPostCommand());
                        SharedPref.putString(SharedPref.SEND_MESSAGE, GetSet.getSendMessage());


                        hideLoading();

                        Intent intent = new Intent();
                        intent.putExtra(Constants.TAG_PROFILE_DATA, profile);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {

                    Log.d(TAG, "API onFailure: " + t.getMessage());

                    hideLoading();
                    call.cancel();
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
            //Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void setProfile(ProfileResponse profile) {
        Glide.with(getApplicationContext())
                .load(profile.getUserImage())
                .error(R.drawable.default_profile_image)
                .placeholder(R.drawable.default_profile_image)
                .into(binding.profileImage);

        binding.txtGender.setText(profile.getGender());
        binding.txtDob.setText(profile.getDob());
        binding.edtBio.setText(profile.getBio());
        binding.edtPayPal.setText(profile.getPaypalId());
        binding.edtName.setText(profile.getName());
        Log.d(TAG, "profileresponce.."+profile.getPhone_number());
        binding.edtmobile.setText(profile.getPhone_number());

        if (GetSet.getEmail() == null || GetSet.getEmail().isEmpty()){
           // emaillayout.setVisibility(GONE);
           // emailline.setVisibility(GONE);
        }else {
            binding.edtemail.setText(GetSet.getEmail());
           // emaillayout.setVisibility(View.VISIBLE);
           // emailline.setVisibility(View.VISIBLE);
        }

        if (GetSet.getUserName() == null || GetSet.getUserName().isEmpty()){
            // emaillayout.setVisibility(GONE);
            // emailline.setVisibility(GONE);
        }else {
            binding.edtusername.setText(GetSet.getUserName());
            // emaillayout.setVisibility(View.VISIBLE);
            // emailline.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.INTENT_REQUEST_CODE) {

            profileResponse.setUserImage(GetSet.getUserImage());
            Glide.with(getApplicationContext())
                    .load(GetSet.getUserImage())
                    .into(binding.profileImage);
        }
    }

    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        binding.progressBar.setVisibility(GONE);
    }

    private void initPrimeView() {
        /*Init Prime View*/
        binding.primeBannerLayout.subscribeLay.setVisibility(GONE);
        binding.renewalBannerLayout.renewalLay.setVisibility(GONE);
        Timber.d("initPrimeView: GetSet.getPremiumMember() %s", GetSet.getPremiumMember());
        Timber.d("initPrimeView: GetSet.getPrimeContent() %s", AppUtils.getPrimeContent(getApplicationContext()));

        if (GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            binding.renewalBannerLayout.renewalLay.setVisibility(GONE);
          /*working fine, commented by Jan7, 2025 for design approval. subscribeLay.setVisibility(View.VISIBLE);*/
            binding.primeBannerLayout.premiumLay.setVisibility(View.VISIBLE);
            binding.primeBannerLayout.btnSubscribe.setVisibility(GONE);
            binding.primeBannerLayout.txtPrimeTitle.setText(AppUtils.getPrimeTitle(getApplicationContext()));
            binding.primeBannerLayout.txtPrice.setText(AppUtils.getPrimeContent(getApplicationContext()));
        } else if (GetSet.isOncePurchased()) {
            binding.renewalBannerLayout.renewalLay.setVisibility(View.VISIBLE);
           /* working fine, commented by Jan7, 2025 for design approval  subscribeLay.setVisibility(GONE);*/
        } else {
            binding.primeBannerLayout.premiumLay.setVisibility(GONE);
          /*  working fine, commented by Jan7, 2025 for design approval  subscribeLay.setVisibility(View.VISIBLE);*/
            binding.primeBannerLayout.btnSubscribe.setVisibility(View.VISIBLE);
            binding.renewalBannerLayout.renewalLay.setVisibility(GONE);
            binding.primeBannerLayout.txtPrimeTitle.setText(AppUtils.getPrimeTitle(getApplicationContext()));
            binding.primeBannerLayout.txtPrice.setText(AppUtils.getPrimeContent(getApplicationContext()));
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if ((ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(EditProfileActivity.this);
        }
        return super.dispatchTouchEvent(ev);
    } */

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
        initPrimeView();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }
    public boolean isValidUsername(String username) {
        if (username.length() > 15) return false; // Optional limit
        return username.matches("^[a-z0-9._]+$");
    }


}
