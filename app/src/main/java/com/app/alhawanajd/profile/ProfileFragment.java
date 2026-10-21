package com.app.alhawanajd.profile;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.app.alhawanajd.NewEditProfileActivity;
import com.app.alhawanajd.RepostedVideosFragment;
import com.app.alhawanajd.search.SearchActivity;
import com.app.alhawanajd.search.SearchUserActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.alhawanajd.App;
import com.app.alhawanajd.ConvertGiftActivity;
import com.app.alhawanajd.DialogCreditGems;
import com.app.alhawanajd.EditProfileActivity;
import com.app.alhawanajd.FollowersActivity;
import com.app.alhawanajd.GemsStoreActivity;
import com.app.alhawanajd.ImageViewActivity;
import com.app.alhawanajd.LikedVideosFragment;
import com.app.alhawanajd.LoginActivity;
import com.app.alhawanajd.MyVideosFragment;
import com.app.alhawanajd.PrimeActivity;
import com.app.alhawanajd.SettingsActivity;
import com.app.helper.LocaleManager;
import com.app.helper.StorageUtils;
import com.app.helper.callback.OnOkCancelClickListener;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.model.event.GetProfileEvent;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.api.CommonApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private AppBarLayout appBarLayout;
    private LinearLayout contentLay;
    private ImageView btnBack;
    private ImageView btnSettings;
    private TextView txtTitle;
    private ShapeableImageView profileImage;
    private ImageView premiumImage;
    private LinearLayout nameLay;
    private TextView txtName;
    private ImageView genderImage;
    private TextView txtUserName;
    private TextView userBio;
    private LinearLayout displayCountLay;
    private LinearLayout heartLay;
    private TextView txtHeartCount;
    private View divider;
    private LinearLayout fansLay;
    private TextView txtFollowersCount;
    private LinearLayout followingLay;
    private TextView txtFollowingsCount;
    private LinearLayout editLay;
    private LinearLayout giftsLay;
    private LinearLayout coinsLay;
    private LinearLayout messageLay,shareprofile;
    private LinearLayout followLay;
    private ConstraintLayout profileEditLay;
    private ConstraintLayout profileMessageLay;
    private ImageView btnFollow;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private RelativeLayout premiumLay;
    private MaterialCardView renewalLay, subscribeLay;
    private MaterialButton btnRenewal;
    private Button btnSubscribe;
    private TextView txtPrimeTitle, txtPrice;
    private ProgressBar progressBar;
    ImageView btnFindFriends;

    private Context mContext;
    private ApiInterface apiInterface;
    ProfileResponse profileResponse;
    DialogCreditGems alertDialog;
    private String userId = null;
    private boolean fromEdit = false;
    private String typeWay = "";
    private AppUtils appUtils;
    private String qrImagePath;
    private boolean isVideoDeleted = false;
    private Bitmap qrBitMap;
    private StorageUtils storageUtils;
    private final int NUM_OF_TABS = 3;
    private HomePagerAdapter pagerAdapter;
    private ImageView more_icon;

    Bundle bun;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_USER_ID)) {
            userId = getArguments().getString(Constants.TAG_USER_ID);
        }
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_EDIT)) {
            fromEdit = getArguments().getBoolean(Constants.TAG_EDIT, false);
        }
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_TYPE)) {
            typeWay = getArguments().getString(Constants.TAG_TYPE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_following_profile, container, false);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        storageUtils = StorageUtils.getInstance(mContext);
        isVideoDeleted = false;
        bun = new Bundle();

        Bundle args = getArguments();
        if (args != null) {
            String userId = args.getString("userId");
            if (userId != null) {
                // Load the user's profile using this userId
            }
        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        initView();
        initViewPager();

    }

    private void findViews(View view) {

        appBarLayout = view.findViewById(R.id.appBarLay);
        contentLay = view.findViewById(R.id.contentLay);
        btnBack = view.findViewById(R.id.btnBack);
        btnSettings = view.findViewById(R.id.btnSettings);
        more_icon = view.findViewById(R.id.more_icon);
        shareprofile = view.findViewById(R.id.shareprofile);
//        additionalImage = view.findViewById(R.id.additionalImage);
        txtTitle = view.findViewById(R.id.txtTitle);
        profileImage = view.findViewById(R.id.profileImage);
        premiumImage = view.findViewById(R.id.premiumImage);
        nameLay = view.findViewById(R.id.nameLay);
        txtName = view.findViewById(R.id.txtName);
        genderImage = view.findViewById(R.id.genderImage);
        txtUserName = view.findViewById(R.id.txtUserName);
        userBio = view.findViewById(R.id.userBio);
        displayCountLay = view.findViewById(R.id.displayCountLay);
        heartLay = view.findViewById(R.id.heartLay);
        txtHeartCount = view.findViewById(R.id.txtHeartCount);
        divider = view.findViewById(R.id.divider_views);
        fansLay = view.findViewById(R.id.fansLay);
        txtFollowersCount = view.findViewById(R.id.txtFollowersCount);
        followingLay = view.findViewById(R.id.followingLay);
        txtFollowingsCount = view.findViewById(R.id.txtFollowingsCount);
        profileEditLay = view.findViewById(R.id.profileEditLay);
        profileMessageLay = view.findViewById(R.id.profileMessageLay);
        editLay = view.findViewById(R.id.editLay);
        giftsLay = view.findViewById(R.id.giftsLay);
        coinsLay = view.findViewById(R.id.coinsLay);
        messageLay = view.findViewById(R.id.messageLay);
        followLay = view.findViewById(R.id.followLay);
        btnFollow = view.findViewById(R.id.btnFollow);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        premiumLay = view.findViewById(R.id.premiumLay);
        renewalLay = view.findViewById(R.id.renewalLay);
        subscribeLay = view.findViewById(R.id.subscribeLay);
        btnSubscribe = view.findViewById(R.id.btnSubscribe);
        txtPrimeTitle = view.findViewById(R.id.txtPrimeTitle);
        txtPrice = view.findViewById(R.id.txtPrice);
        btnRenewal = view.findViewById(R.id.btnRenewal);
        progressBar = view.findViewById(R.id.progressBar);
        btnFindFriends = view.findViewById(R.id.btnFindFriends);

        int topMarginInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) appBarLayout.getLayoutParams();
        params.topMargin = topMarginInPx;
        appBarLayout.setLayoutParams(params);

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

    }

    private void initView() {
        SpannableString content = new SpannableString(getString(R.string.subscribe));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        btnSubscribe.setText(content);

        progressBar.setVisibility(GONE);
       //  btnSettings.setImageResource(R.drawable.menu);
       // btnSettings.setVisibility(GONE);
       // profileEditLay.setVisibility(View.VISIBLE);
        profileMessageLay.setVisibility(GONE);
            editLay.setVisibility(View.VISIBLE);
            shareprofile.setVisibility(View.VISIBLE);
        btnBack.setVisibility(GONE);
            Log.e(TAG,"typeWay : " + typeWay +"TEST");
        if (typeWay.equals("Profile")) {
            editLay.setVisibility(View.VISIBLE);
            btnBack.setVisibility(View.VISIBLE);
            if (LocaleManager.isRTL()) {
                btnBack.setRotation(180);
            } else {
                btnBack.setRotation(0);
            }
        } else if (fromEdit) {
            editLay.setVisibility(View.VISIBLE);
            btnBack.setVisibility(GONE);
        } else {
            //    btnBack.setVisibility(View.VISIBLE); //ORIGINAL
            btnBack.setVisibility(GONE);
//            editLay.setVisibility(GONE);
        }
        initClickListener();
    }

    private void initClickListener() {
        btnFindFriends.setOnClickListener(this);
        profileImage.setOnClickListener(this);
        editLay.setOnClickListener(this);
//      btnSettings.setOnClickListener(this);
//      additionalImage.setOnClickListener(this);
        heartLay.setOnClickListener(this);
        fansLay.setOnClickListener(this);
        followingLay.setOnClickListener(this);
        giftsLay.setOnClickListener(this);
        coinsLay.setOnClickListener(this);
        btnSubscribe.setOnClickListener(this);
        subscribeLay.setOnClickListener(this);
        btnRenewal.setOnClickListener(this);
        renewalLay.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        /*btnRenewal.setOnClickListener(this);
        followingsLay.setOnClickListener(this);
        gemsLay.setOnClickListener(this);
        videoCountLay.setOnClickListener(this);
        btnShare.setOnClickListener(this);*/
    }

    private void initViewPager() {
        if (pagerAdapter != null) {
            pagerAdapter.resetViewPager();
        }
        if (isAdded()) {
            pagerAdapter = new HomePagerAdapter(getActivity());
            viewPager.setAdapter(pagerAdapter);
            viewPager.setOffscreenPageLimit(1);
            viewPager.setCurrentItem(0);
            new TabLayoutMediator(tabLayout, viewPager,
                    new TabLayoutMediator.TabConfigurationStrategy() {
                        @Override
                        public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                            TextView tabHeading = tab.view.findViewById(R.id.tab_heading);
                            if (tabHeading != null) {
                                tabHeading.setTextColor(ContextCompat.getColor(getContext(), R.color.white)); // Unselected color
                            }
                        }
                    }).attach();
            tabLayout.removeAllTabs();
            View view1 = getLayoutInflater().inflate(R.layout.tab_item_profile, null);
           // view1.findViewById(R.id.tab_icon).setBackgroundResource(R.drawable.tab_video_selector);
            TextView tabHeading1 = view1.findViewById(R.id.tab_heading);
            tabHeading1.setText(requireContext().getString(R.string.videos_txt));
            if (tabHeading1 != null) {
                tabHeading1.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary)); // Selected color
            }
            tabLayout.addTab(tabLayout.newTab().setCustomView(view1));

            View view2 = getLayoutInflater().inflate(R.layout.tab_item_profile, null);
          //  view2.findViewById(R.id.tab_icon).setBackgroundResource(R.drawable.tab_heart_selector);
            TextView tabHeading2 = view2.findViewById(R.id.tab_heading);
            tabHeading2.setText(requireContext().getString(R.string.liked));
            //tabHeading2.setText(requireContext().getString(R.string.videos_txt));
            tabLayout.addTab(tabLayout.newTab().setCustomView(view2));

         /*   TextView initialTabHeading = view1.findViewById(R.id.tab_heading);
            if (initialTabHeading != null) {
                initialTabHeading.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary)); // Selected color
            }*/
            View view3 = getLayoutInflater().inflate(R.layout.tab_item_profile, null);
            TextView tabHeading3 = view3.findViewById(R.id.tab_heading);
            tabHeading3.setText(requireContext().getString(R.string.repost));
            //tabHeading2.setText(requireContext().getString(R.string.videos_txt));
            tabLayout.addTab(tabLayout.newTab().setCustomView(view3));

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    TextView tabHeading = tab.view.findViewById(R.id.tab_heading);
                    if (tabHeading != null) {
                        tabHeading.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary)); // Selected color
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    TextView tabHeading = tab.view.findViewById(R.id.tab_heading);
                    if (tabHeading != null) {
                        tabHeading.setTextColor(ContextCompat.getColor(getContext(), R.color.tabbar_colour)); // Unselected color
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    // Optionally handle reselecting the tab
                }
            });
        }
        more_icon.setOnClickListener(view -> {
            Log.d(TAG, "aaaassss: ");
            App.preventMultipleClick(more_icon);
            Intent primeIntent = new Intent(getActivity(), SettingsActivity.class);
            primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(primeIntent);
        });
        shareprofile.setOnClickListener(view -> {
            Log.d(TAG, "aaaassss: ");
            App.preventMultipleClick(shareprofile);
            shareMyProfile(profileResponse);
        });


    }
    private void shareMyProfile(ProfileResponse profile) {
        String userId = GetSet.getUserId(); // or get from arguments
        if (userId != null) {
            String profileLink = profile.getShare_profile_link(); // App Link
            String shareMsg = "Hi, check my  profile ---> " + profileLink;
            Log.d(TAG, "shareMyProfilelinkkk: "+profileLink);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareMsg);
            startActivity(Intent.createChooser(intent, getString(R.string.share_link)));
        }
    }

    public class HomePagerAdapter extends FragmentStateAdapter {
        public HomePagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                default: {
                    return new MyVideosFragment(GetSet.getUserId(), getString(R.string.videos_txt), Constants.TAG_OWN_PROFILE , "profile");
                }
                case 1: {
                    return new LikedVideosFragment(GetSet.getUserId(), /*getString(R.string.liked_videos)*/getString(R.string.liked), Constants.TAG_OWN_PROFILE);
                }
                case 2: {
                    return new RepostedVideosFragment(GetSet.getUserId(), /*getString(R.string.liked_videos)*/getString(R.string.repost), Constants.TAG_OWN_PROFILE);
//                    return new FavSoundFragment(GetSet.getUserId());
                   // return new LikedVideosFragment(GetSet.getUserId(), /*getString(R.string.liked_videos)*/getString(R.string.liked), Constants.TAG_OWN_PROFILE);

                }

            }
        }

        @Override
        public int getItemCount() {
            return NUM_OF_TABS;
        }

        public void resetViewPager() {

        }
    }

    private void loadInterstitialAd() {
//        InterstitialAdUtils.loadInterstitialAd(getActivity(), Constants.TAG_PROFILE, new OnOkClickListener() {
//            @Override
//            public void onOkClicked(Object o, String type) {
//                Intent profileIntent = new Intent(mContext, EditProfileActivity.class);
//                profileIntent.putExtra(Constants.TAG_PROFILE_DATA, profileResponse);
//                startActivityForResult(profileIntent, Constants.PROFILE_REQUEST_CODE);
//                getActivity().overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
//            }
//        });
    }

    private void initProfile() {
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
            /*profileResponse.setPrivacyAge(GetSet.getPrivacyAge());*/
            /*profileResponse.setUserCanMessage(GetSet.geti());*/
            profileResponse.setBio(GetSet.getBio());
            profileResponse.setLikes(GetSet.getLikes());
            profileResponse.setAge(Integer.valueOf(GetSet.getAge()));
            profileResponse.setShare_profile_link(GetSet.getShare_profile_link());
            setProfile(profileResponse);
            setPrimeView(profileResponse);
        }
    }

    private void getProfile() {
        if (GetSet.getUserId() != null && apiInterface != null) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());

//            Timber.d("getProfile: %s", new Gson().toJson(request));

            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        ProfileResponse profile = response.body();
                        profileResponse = profile;

                        Log.d("getProfile",new Gson().toJson(profile));
                        GetSet.setUserId(profile.getUserId());
                        /*GetSet.setLoginId(profile.getLoginId());*/
                        GetSet.setName(profile.getName());
                        GetSet.setUserName(profile.getUserName());
                        GetSet.setDob(profile.getDob());
                        GetSet.setGetPhonenumber(profile.getPhone_number());
                        GetSet.setAge(String.valueOf(profile.getAge()));
                        GetSet.setGender(profile.getGender());
                        GetSet.setUserImage(profile.getUserImage());
                        /*GetSet.setLocation(profile.getLocation());*/
                        GetSet.setFollowingCount(String.valueOf(profile.getFollowings()));
                        GetSet.setFollowersCount(String.valueOf(profile.getFollowers()));
                        GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(String.valueOf(profile.getAvailableGifts())) : 0L);
                        GetSet.setGems(profile.getAvailableGems() != null ? (profile.getAvailableGems()) : 0F);
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

                        // Gift to money
                        GetSet.setPaypal_id(profile.getPaypalId());
                        SharedPref.putString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id());
                        SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                        SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());
                        SharedPref.putString(SharedPref.NAME, GetSet.getName());
                        SharedPref.putString(SharedPref.USER_NAME, GetSet.getUserName());
                        SharedPref.putString(SharedPref.DOB, GetSet.getDob());
                        SharedPref.putString(SharedPref.AGE, GetSet.getAge());
                        SharedPref.putString(SharedPref.GENDER, GetSet.getGender());
                        SharedPref.putString(SharedPref.USER_IMAGE, GetSet.getUserImage());
                        SharedPref.putString(SharedPref.PROFILE_SHARE_URL, GetSet.getShare_profile_link());
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
                        SharedPref.putString(SharedPref.INTEREST_LIST, profile.getInterestList());
                        SharedPref.putString(SharedPref.INTEREST_IDLIST, profile.getInterestIdList());
                        Log.e("PHONE_NUMBER", "onResponse: "+ profile.getPhone_number());
                        SharedPref.putString(SharedPref.PHONE_NUMBER, profile.getPhone_number());

                        SharedPref.putString(SharedPref.POST_COMMAND, GetSet.getPostCommand());
                        SharedPref.putString(SharedPref.SEND_MESSAGE, GetSet.getSendMessage());


                        if (profile.getVideosCount() != null && Long.parseLong(String.valueOf(profile.getVideosCount())) > 0) {
                            isVideoDeleted = false;
                        }
                        setProfile(profile);
                    } else if (response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                        App.makeToast(getString(R.string.Account_block));
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(mContext).clearDB();
                        Intent logout = new Intent(mContext, LoginActivity.class);
                        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(logout);
                    }
                    else {
                        initProfile();
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    Log.e(TAG, "getProfile: " + t.getMessage());
                    call.cancel();
                    initProfile();
                }
            });
        }
    }

    private void setProfile(ProfileResponse profile) {
        if (profile != null && getActivity() != null && mContext != null) {
//            setPrimeView(profile);
            Timber.tag(TAG).d("setProfile: %s", profile.getName());
          //  txtTitle.setText(mContext.getString(R.string.my_profile));
//            txtTitle.setText("Profile");
            txtTitle.setVisibility(View.VISIBLE);
//            txtTitle.setVisibility(View.VISIBLE);
            //btnBack.setVisibility(View.VISIBLE); - ORIGINAL
            txtName.setText(profile.getName());
            Glide.with(mContext)
                    .load(profile.getUserImage())
                    .error(R.drawable.default_profile_image)
                    .placeholder(R.drawable.default_profile_image)
                    .into(profileImage);

            if (TextUtils.isEmpty(profile.getBio()))
                userBio.setVisibility(View.GONE);
            else {
                userBio.setText(profile.getBio());
                userBio.setVisibility(View.VISIBLE);
            }


            if (txtUserName != null) {
                if (!TextUtils.isEmpty(profile.getUserName())) {
                    String userName = profile.getUserName();
                    if (!userName.startsWith("@")) {
                        userName = "@" + userName;
                    }
                    txtUserName.setText(userName);
                    txtUserName.setVisibility(View.VISIBLE);
                } else {
                    txtUserName.setVisibility(View.GONE);
                }
            }

            txtFollowersCount.setText(String.valueOf(profile.getFollowers() != null ? profile.getFollowers() : 0));
            txtFollowingsCount.setText(String.valueOf(profile.getFollowings() != null ? profile.getFollowings() : 0));
            genderImage.setImageResource(profile.getGender().equals(Constants.TAG_MALE) ?
                    R.drawable.men : R.drawable.women);

//            txtVideoCount.setText(String.valueOf(profile.getVideosCount() != null ? profile.getVideosCount() : 0));

            // txtGemsCount.setText(String.valueOf((profile.getAvailableGems() != null ? profile.getAvailableGems() : 0)));
//            txtGemsCount.setText(fmtFloat(profile.getAvailableGems()));
//            txtGiftsCount.setText(String.valueOf((profile.getAvailableGifts() != null ? profile.getAvailableGifts() : 0)));
            txtHeartCount.setText(String.valueOf(profile.getLikes() != null ? profile.getLikes() : 0));
          //  profileEditLay.setVisibility(View.VISIBLE);
        }
    }

    private void setPrimeView(ProfileResponse profile) {
        if (profile.getPremiumMember().equals(Constants.TAG_TRUE)) {
            premiumImage.setVisibility(View.VISIBLE);
            renewalLay.setVisibility(View.GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            premiumLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(mContext));
            txtPrice.setText(AppUtils.getPrimeContent(mContext));
        } else if (GetSet.isOncePurchased()) {
            renewalLay.setVisibility(View.VISIBLE);
            subscribeLay.setVisibility(View.GONE);
            premiumImage.setVisibility(View.GONE);
        } else {
            premiumImage.setVisibility(View.GONE);
            premiumLay.setVisibility(View.GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.VISIBLE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(mContext));
            txtPrice.setText(AppUtils.getPrimeContent(mContext));
        }
    }

    @Override
    public void onClick(View view) {
        if (App.isPreventMultipleClick())
            return;
        switch (view.getId()) {
            case R.id.btnFindFriends:
                Intent intent1 = new Intent(getActivity(), SearchUserActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent1);
                break;
            case R.id.btnBack:
                getActivity().finish();
                break;
            case R.id.profileImage: {
                App.preventMultipleClick(profileImage);
                Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
                animateZoom(getActivity());
            }
            break;
            case R.id.editLay:
                App.preventMultipleClick(editLay);
//                if (/*AdminData.isAdEnabled() &&*/ Constants.ADDON_INTERSTITIAL_AD) {
//                    loadInterstitialAd();
//                } else {
                   // Intent profileIntent = new Intent(getActivity(), EditProfileActivity.class);
                Intent profileIntent = new Intent(getActivity(), NewEditProfileActivity.class);
                profileIntent.putExtra(Constants.TAG_PROFILE_DATA, profileResponse);
                    startActivityForResult(profileIntent, Constants.PROFILE_REQUEST_CODE);
                    requireActivity().overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
//                }
                break;
            case R.id.btnRenewal:
            case R.id.renewalLay:
            case R.id.btnSubscribe:
            case R.id.subscribeLay:
                App.preventMultipleClick(renewalLay);
                App.preventMultipleClick(subscribeLay);
                if (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    Intent primeIntent = new Intent(getActivity(), PrimeActivity.class);
                    primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(primeIntent);
                }

                break;
//            case R.id.additionalImage:
//            case R.id.btnSettings:
//                Log.d(TAG, "Asss: ");
//                App.preventMultipleClick(btnSettings);
//                Intent primeIntent = new Intent(getActivity(), SettingsActivity.class);
//                primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                startActivity(primeIntent);
//                break;
            case R.id.followingLay:
                App.preventMultipleClick(followingLay);
                Intent followersIntent = new Intent(getActivity(), FollowersActivity.class);
                followersIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWINGS);
                followersIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                startActivity(followersIntent);
                break;
            case R.id.fansLay:
                App.preventMultipleClick(fansLay);
                Intent followingsIntent = new Intent(getActivity(), FollowersActivity.class);
                followingsIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
                followingsIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                startActivity(followingsIntent);
                break;
            case R.id.giftsLay: {
                App.preventMultipleClick(giftsLay);
                Intent giftIntent = new Intent(getActivity(), ConvertGiftActivity.class);
                giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(giftIntent);
               /* hided on jan8 for new design approval if (GetSet.getGifts() == 0) {
                    if (alertDialog != null && alertDialog.isAdded()) {
                        return;
                    }
                    alertDialog = new DialogCreditGems();
                    alertDialog.setCallBack(new OnOkCancelClickListener() {
                        @Override
                        public void onOkClicked(Object o) {
                            alertDialog.dismissAllowingStateLoss();
                        }

                        @Override
                        public void onCancelClicked(Object o) {

                        }
                    });
                    alertDialog.setContext(getActivity());
                    alertDialog.setMessage(getString(R.string.you_dont_have_any_gifts));
                    alertDialog.show(getChildFragmentManager(), TAG);
                } else {
                    Intent giftIntent = new Intent(getActivity(), ConvertGiftActivity.class);
                    giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(giftIntent);
                }*/
            }
            break;
            case R.id.coinsLay: {
                App.preventMultipleClick(coinsLay);
                Intent gemsIntent = new Intent(getActivity(), GemsStoreActivity.class);
                gemsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(gemsIntent);
            }
            break;
           /* case R.id.videoCard:
                App.preventMultipleClick(videoCard);
                Intent intent = new Intent(getContext(), ProfileVideos.class);
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_OWN_PROFILE);
                intent.putExtra(Constants.TAG_PROFILE_ID, GetSet.getUserId());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;*/
            default:
                break;
        }
    }

    public static void animateZoom(Context context) {
        ((Activity) context).
                overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        initProfile();
        getProfile();
        CommonApi.getAppDefaultData(mContext);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProfileUpdated(GetProfileEvent profileEvent) {
        Timber.d("onProfileUpdated: uptadted");
        setProfile(profileEvent.getProfileResponse());
    }

    /*@Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        App.makeToast(TAG + ":" + hidden);
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.INTENT_REQUEST_CODE) {
            if (mContext != null) {
                /*Glide.with(context)
                        .load(  GetSet.getUserImage())
                        .apply(App.getProfileImageRequest())
                        .into(profileImage);*/

                Glide.with(mContext)
                        .load(GetSet.getUserImage())
                        .error(R.drawable.default_profile_image)
                        .placeholder(R.drawable.default_profile_image)
                        // .apply(App.getProfileImageRequest())
                        .into(profileImage);
            }
        } else if (resultCode == RESULT_OK && requestCode == Constants.PROFILE_REQUEST_CODE) {
            if (mContext != null) {
                profileResponse = (ProfileResponse) data.getSerializableExtra(Constants.TAG_PROFILE_DATA);
                setProfile(profileResponse);
            }
        } /*else if (resultCode == RESULT_OK && requestCode == StreamConstants.DELETE_REQUEST_CODE) {
            isVideoDeleted = data.getBooleanExtra(Constants.IS_VIDEO_DELETED, false);
        }*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    public void updateProfile() {
        getProfile();
    }
}


