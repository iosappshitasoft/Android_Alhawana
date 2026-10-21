package com.app.alhawanajd.home;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.alhawanajd.App;
import com.app.alhawanajd.ChatActivity;
import com.app.alhawanajd.ConvertGiftActivity;
import com.app.alhawanajd.DialogCreditGems;
import com.app.alhawanajd.FollowersActivity;
import com.app.alhawanajd.GemsStoreActivity;
import com.app.alhawanajd.LikedVideosFragment;
import com.app.alhawanajd.LoginActivity;
import com.app.alhawanajd.MyVideosFragment;
import com.app.alhawanajd.PrimeActivity;
import com.app.alhawanajd.SettingsActivity;
import com.app.alhawanajd.SingleImageActivity;
import com.app.alhawanajd.home.eventbus.FollowingFollowFollowing;
import com.app.alhawanajd.home.eventbus.ForYouFollowFollowing;
import com.app.alhawanajd.home.eventbus.OtherProfileToFollowingProfile;
import com.app.alhawanajd.profile.UserReportAdapter;
import com.app.helper.AppWebSocket;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.OnOkCancelClickListener;
import com.app.helper.callback.ProfileImageClickListener;
import com.app.model.FollowRequest;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.model.ReportResponse;
import com.app.utils.AdminData;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DevicePref;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiCallback;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.api.CommonApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


/*public class FollowingProfileFragment extends BaseFragment {*/
public class FollowingProfileFragment extends Fragment implements ApiCallback {

    private static final String TAG = FollowingProfileFragment.class.getSimpleName();
    private Context mContext;

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
    private LinearLayout messageLay;
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

    private ProfilePagerAdapter pagerAdapter;
    AppUtils appUtils;
    ProfileResponse profile;
    ProfileImageClickListener profileImageClickListener;
    private ApiInterface apiInterface;
    private String profileId = null;
    Call<ProfileResponse> call;
    boolean videoEnabled = false;
    private String from = null;
    private BottomSheetDialog menuDialog, reportDialog;
    private RecyclerView rvReports;
    private UserReportAdapter reportAdapter;

    private DBHelper dbHelper;
    private String blockStatus = Constants.TAG_FALSE;
    private String chatId;
    private ImageView more_icon;

    public FollowingProfileFragment() {
        // Required empty public constructor
    }

    public static FollowingProfileFragment newInstance(ProfileImageClickListener mprofileImageClickListener) {
        FollowingProfileFragment fragment = new FollowingProfileFragment();
        fragment.profileImageClickListener = mprofileImageClickListener;
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_FROM)) {
            from = getArguments().getString(Constants.TAG_FROM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_following_profile, container, false);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(getActivity());
        dbHelper = DBHelper.getInstance(getActivity());

        findViews(view);

//        btnSettings = view.findViewById(R.id.btnSettings);
//        btnSettings.setOnClickListener(v -> {
//            Log.d("TAG", "Settings Clicked");
//            Toast.makeText(getContext(), "Clicked!", Toast.LENGTH_SHORT).show();
//        });
        return view;
    }

    private void findViews(ViewGroup view) {
        appBarLayout = view.findViewById(R.id.appBarLay);
        contentLay = view.findViewById(R.id.contentLay);
        btnBack = view.findViewById(R.id.btnBack);
        btnSettings = view.findViewById(R.id.btnSettings);
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
        more_icon = view.findViewById(R.id.more_icon);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        contentLay.setVisibility(View.GONE);
//        appBarLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }


//        btnSettings.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.menu));

//        if (Constants.Login_method.equals("skip")){
//            btnSettings.setVisibility(View.GONE);
//        }else {
//            btnSettings.setVisibility(View.VISIBLE);
//        }

        fansLay.setOnClickListener(v -> {
            App.preventMultipleClick(fansLay);
            if (blockStatus == null ||blockStatus.equals(Constants.TAG_FALSE)) {
                Intent followersIntent = new Intent(getContext(), FollowersActivity.class);
                followersIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
                followersIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                followersIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(followersIntent);
            } else {
                App.makeToast(mContext.getString(R.string.unblock_description));
            }
        });

        followingLay.setOnClickListener(v -> {
            App.preventMultipleClick(followingLay);
            if (blockStatus == null ||blockStatus.equals(Constants.TAG_FALSE)) {
                Intent followingsIntent = new Intent(getContext(), FollowersActivity.class);
                followingsIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWINGS);
                followingsIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                followingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(followingsIntent);
            } else {
                App.makeToast(mContext.getString(R.string.unblock_description));
            }
        });

        profileImage.setOnClickListener(v -> {
            App.preventMultipleClick(profileImage);
            if (blockStatus == null || blockStatus.equals(Constants.TAG_FALSE)) {
                Intent intent = new Intent(getContext(), SingleImageActivity.class);
                intent.putExtra("image", String.valueOf(profile.getUserImage()));
                startActivity(intent);
                animateZoom(getActivity());
            } else {
                App.makeToast(mContext.getString(R.string.unblock_description));
            }
        });

        btnFollow.setOnClickListener(v -> {
            App.preventMultipleClick(btnFollow);
            if (blockStatus == null || blockStatus.equals(Constants.TAG_FALSE)) {
                if (Constants.Login_method.equals("skip")){
                    login();
                }else {
                    followUnfollowUser(profile.getUserId());
                }

            } else {
                App.makeToast(mContext.getString(R.string.unblock_description));
            }
        });

        btnBack.setOnClickListener(v -> {
            profileImageClickListener.onUserClicked(false, null);
        });

        messageLay.setOnClickListener(v -> {
            Log.d(TAG, "initViewyesas: ");
                    if (Constants.Login_method.equals("skip")){
                        login();
                    }else {
                        App.preventMultipleClick(messageLay);

                        if (!TextUtils.isEmpty(profileId)) {
                            if (blockStatus.equals(Constants.TAG_FALSE)) {
                                messageCheck();
                            } else {
                                App.makeToast(mContext.getString(R.string.unblock_description));
                            }
                        }
                    }
          /*  Intent chatIntent = new Intent(getContext(), ChatActivity.class);
            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            chatIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
            chatIntent.putExtra(Constants.TAG_PARTNER_NAME, profile.getName());
            chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, profile.getUserImage());
            chatIntent.putExtra(Constants.TAG_BLOCKED_BY_ME, profile.getBlockedByMe());
            startActivity(chatIntent);*/
        });

        giftsLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.preventMultipleClick(giftsLay);
                if (GetSet.getGifts() == 0) {
                    DialogCreditGems alertDialog = new DialogCreditGems();
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
                }
            }
        });

        coinsLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.preventMultipleClick(coinsLay);
                Intent gemsIntent = new Intent(getActivity(), GemsStoreActivity.class);
                gemsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(gemsIntent);
            }
        });
//        btnSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "onClickoneyess: ");
//                App.preventMultipleClick(btnSettings);
//                if (profileId.equals(GetSet.getUserId())) {
//                    Intent primeIntent = new Intent(getActivity(), SettingsActivity.class);
//                    primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                    startActivity(primeIntent);
//                } else {
//                    if (menuDialog != null && !menuDialog.isShowing()) {
//                        TextView txtReport = menuDialog.findViewById(R.id.txtReport);
//                        TextView txtBlock = menuDialog.findViewById(R.id.txtBlock);
//
//                        if (blockStatus.equals(Constants.TAG_FALSE))
//                            txtBlock.setText(getString(R.string.block));
//                        else txtBlock.setText(getString(R.string.unblock));
//
//
//                        if (profile.getReport() == null) {
//                        } else if (profile.getReport())
//                            txtReport.setText(getString(R.string.unreported));
//                        else txtReport.setText(getString(R.string.report_user));
//
//                        menuDialog.show();
//                    }
//                }
//
//            }
//        });
//        btnSettings.setOnClickListener(view -> {
//            Log.d(TAG, "onClickoneyess: ");
//                App.preventMultipleClick(btnSettings);
//                if (profileId.equals(GetSet.getUserId())) {
//                    Intent primeIntent = new Intent(getActivity(), SettingsActivity.class);
//                    primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                    startActivity(primeIntent);
//                } else {
//                    if (menuDialog != null && !menuDialog.isShowing()) {
//                        TextView txtReport = menuDialog.findViewById(R.id.txtReport);
//                        TextView txtBlock = menuDialog.findViewById(R.id.txtBlock);
//
//                        if (blockStatus.equals(Constants.TAG_FALSE))
//                            txtBlock.setText(getString(R.string.block));
//                        else txtBlock.setText(getString(R.string.unblock));
//
//
//                        if (profile.getReport() == null) {
//                        } else if (profile.getReport())
//                            txtReport.setText(getString(R.string.unreported));
//                        else txtReport.setText(getString(R.string.report_user));
//
//                        menuDialog.show();
//                    }
//                }
//
//
//        });
        more_icon.setOnClickListener(view -> {
            Log.d(TAG, "onClickoneyess12: ");
            App.preventMultipleClick(btnSettings);
            if (profileId.equals(GetSet.getUserId())) {
                Intent primeIntent = new Intent(getActivity(), SettingsActivity.class);
                primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(primeIntent);
            } else {
                if (menuDialog != null && !menuDialog.isShowing()) {
                    TextView txtReport = menuDialog.findViewById(R.id.txtReport);
                    TextView txtBlock = menuDialog.findViewById(R.id.txtBlock);

                    if (blockStatus.equals(Constants.TAG_FALSE))
                        txtBlock.setText(getString(R.string.block));
                    else txtBlock.setText(getString(R.string.unblock));


                    if (profile.getReport() == null) {
                    } else if (profile.getReport())
                        txtReport.setText(getString(R.string.unreported));
                    else txtReport.setText(getString(R.string.report_user));

                    menuDialog.show();
                }
            }


        });
    }

    // Came Data From OthersProfileActivity
    @Subscribe(priority = 1)
    public void onMessageEvent(OtherProfileToFollowingProfile event) {
        if (profileId != null && profileId.equals(event.id))
            getUserProfile(event.id, "");
    }

    private void initViewPager() {
        if (pagerAdapter != null) {
            pagerAdapter.resetViewPager();
        }
        if (isAdded()) {
            pagerAdapter = new ProfilePagerAdapter(getActivity());
            viewPager.setAdapter(pagerAdapter);
            viewPager.setOffscreenPageLimit(1);
            viewPager.setCurrentItem(0);
            new TabLayoutMediator(tabLayout, viewPager,
                    new TabLayoutMediator.TabConfigurationStrategy() {
                        @Override
                        public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                        }
                    }).attach();
            tabLayout.removeAllTabs();
            View view1 = getLayoutInflater().inflate(R.layout.tab_item_profile, null);
            view1.findViewById(R.id.tab_icon).setBackgroundResource(R.drawable.tab_video_selector);
            tabLayout.addTab(tabLayout.newTab().setCustomView(view1));

            View view2 = getLayoutInflater().inflate(R.layout.tab_item_profile, null);
            view2.findViewById(R.id.tab_icon).setBackgroundResource(R.drawable.tab_heart_selector);
            tabLayout.addTab(tabLayout.newTab().setCustomView(view2));
            if (profileId != null && profileId.equals(GetSet.getUserId())) {
                tabLayout.setVisibility(View.VISIBLE);
            } else {
                tabLayout.setVisibility(View.GONE);
            }

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }
    }
    private void login(){
        GetSet.reset();
        SharedPref.clearAll();
        DBHelper.getInstance(getActivity()).clearDB();
        Intent logout = new Intent(getActivity(), LoginActivity.class);
        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(logout);
    }
    private void getUserProfile(String profileUserId, String getProfileImage) {
        if (NetworkReceiver.isConnected()) {
            if (call != null) call.cancel();
            blockStatus = Constants.TAG_FALSE;
            if (GetSet.getUserId() != null) {
                contentLay.setVisibility(View.GONE);
                appBarLayout.setVisibility(View.GONE);
                profileMessageLay.setVisibility(View.GONE);
                profileEditLay.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(getProfileImage)
                        .placeholder(R.drawable.default_profile_image)
                        .into(profileImage);

                genderImage.setVisibility(View.INVISIBLE);
                displayCountLay.setVisibility(View.INVISIBLE);
//                profileMessageLay.setVisibility(View.GONE);
//                profileEditLay.setVisibility(View.GONE);
                userBio.setText("");
                txtHeartCount.setText("-");
                txtFollowingsCount.setText("-");
                txtName.setText("");
                txtUserName.setText("");

                ProfileRequest request = new ProfileRequest();
                request.setUserId(GetSet.getUserId());
                request.setProfileId(profileUserId);
                call = apiInterface.getProfile(request);
                call.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                        if (response.isSuccessful()) {
//                            Timber.d("getUserProfile: %s", new Gson().toJson(response.body()));
                            if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                profile = response.body();
                                if (!GetSet.getUserId().equals(profile.getUserId())) {
                                    blockStatus = profile.getBlockedByMe();
                                    chatId = GetSet.getUserId() + profileId;
                                }
                                initReportDialog();
                                setProfileView(profile);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ProfileResponse> call, @NotNull Throwable t) {
                        Timber.e("getUserProfile: %s", t.getMessage());
                    }
                });
            }
            else{
                contentLay.setVisibility(View.GONE);
                appBarLayout.setVisibility(View.GONE);
                profileMessageLay.setVisibility(View.GONE);
                profileEditLay.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(getProfileImage)
                        .placeholder(R.drawable.default_profile_image)
                        .into(profileImage);

                genderImage.setVisibility(View.INVISIBLE);
                displayCountLay.setVisibility(View.INVISIBLE);
//                profileMessageLay.setVisibility(View.GONE);
//                profileEditLay.setVisibility(View.GONE);
                userBio.setText("");
                txtHeartCount.setText("-");
                txtFollowingsCount.setText("-");
                txtName.setText("");
                txtUserName.setText("");

                ProfileRequest request = new ProfileRequest();
                request.setUserId("");
                request.setProfileId(profileUserId);
                call = apiInterface.getProfile(request);
                call.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                        if (response.isSuccessful()) {
//                            Timber.d("getUserProfile: %s", new Gson().toJson(response.body()));
                            if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                profile = response.body();
                             /*   if (!GetSet.getUserId().equals(profile.getUserId())) {
                                    blockStatus = profile.getBlockedByMe();
                                    chatId = GetSet.getUserId() + profileId;
                                }
                                initReportDialog();*/
                                setProfileView(profile);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ProfileResponse> call, @NotNull Throwable t) {
                        Timber.e("getUserProfile: %s", t.getMessage());
                    }
                });
            }
        } else {
//            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void setProfileView(final ProfileResponse profile) {
        Timber.tag(TAG).d("setProfileView: %s", new Gson().toJson(profile));
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    initViewPager();
                    contentLay.setVisibility(View.VISIBLE);
                    appBarLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    if (profile.getPremiumMember().equals(Constants.TAG_TRUE))
                        premiumImage.setVisibility(View.VISIBLE);
                    else
                        premiumImage.setVisibility(View.GONE);

                    if (profile.getUserId().equals(GetSet.getUserId())) {
                        tabLayout.setVisibility(View.VISIBLE);
                        setPrimeView(profile);
                        profileEditLay.setVisibility(View.VISIBLE);
                    } else {
                        tabLayout.setVisibility(View.GONE);
                        subscribeLay.setVisibility(View.GONE);
                        renewalLay.setVisibility(View.GONE);
                        setFollowStatus(profile.getFollow());
                        profileMessageLay.setVisibility(View.VISIBLE);
                    }

                    txtName.setText(profile.getName());
                    if (GetSet.getUserId().equals(profile.getUserId())) {
                        txtTitle.setText(mContext.getString(R.string.my_profile));
                    } else {
                        txtTitle.setText(profile.getName());
                    }
                    txtUserName.setText(profile.getUserName());
                    txtHeartCount.setText(profile.getLikes() != null ? "" + profile.getLikes() : "" + 0);

                    if (TextUtils.isEmpty(profile.getBio())) userBio.setVisibility(View.GONE);
                    else {
                        userBio.setText(profile.getBio());
                        userBio.setVisibility(View.VISIBLE);
                    }

                    displayCountLay.setVisibility(View.VISIBLE);
                    txtFollowersCount.setText(String.valueOf(profile.getFollowers() != null ? profile.getFollowers() : "0"));
                    txtFollowingsCount.setText(String.valueOf(profile.getFollowings() != null ? profile.getFollowings() : "0"));
                    if (profile.getGender() != null) {
                        genderImage.setVisibility(View.VISIBLE);
                        genderImage.setImageDrawable(profile.getGender().equals(Constants.TAG_MALE) ?
                                mContext.getResources().getDrawable(R.drawable.men) : mContext.getResources().getDrawable(R.drawable.women));
                    } else {
                        genderImage.setVisibility(View.INVISIBLE);
                    }

                    if (btnFollow != null) btnFollow.setEnabled(true);
                    // Click event activated
                    fansLay.setEnabled(true);
                    profileImage.setEnabled(true);
                    premiumImage.setEnabled(true);
                    fansLay.setEnabled(true);
                    followingLay.setEnabled(true);
                    btnFollow.setEnabled(true);
                    messageLay.setEnabled(true);

                    Glide.with(mContext.getApplicationContext())
                            .load(profile.getUserImage())
                            .error(R.drawable.default_profile_image)
                            .placeholder(R.drawable.default_profile_image)
                            /*.addListener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.e(TAG, "Profile Image: onLoadFailed: " + e.getMessage(), e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })*/
                            .into(profileImage);
                }
            });
        }
    }

    private void setFollowStatus(String follow) {
        if (follow != null) {
            if (follow.equals(Constants.TAG_TRUE)) {
                if(profileId.equalsIgnoreCase("676518e390396c9189355004")){
                    followLay.setVisibility(View.GONE);
                }
                followLay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_secondary_outline));
                btnFollow.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.added));
            } else {
                if(profileId.equalsIgnoreCase("676518e390396c9189355004")){
                    followLay.setVisibility(View.GONE);
                }
                followLay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.follow_round_corner));
                btnFollow.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.add_friend));
            }
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

    private void initReportDialog() {
        if (getActivity() != null) {
            if (menuDialog != null) {
                menuDialog = null;
            }
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_profile, null);
            menuDialog = new BottomSheetDialog(getActivity(), R.style.SimpleBottomDialog); // Style here
            menuDialog.setContentView(sheetView);
            BottomSheetBehavior<View> mBehavior = BottomSheetBehavior.from((View) sheetView.getParent());
            int maxHeight = (DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_HEIGHT, 0) * 80) / 100;
            mBehavior.setPeekHeight(maxHeight);
            sheetView.requestLayout();
            menuDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                    View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                    BottomSheetBehavior.from(bottomSheet).setHideable(true);
                }
            });

            menuDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                    View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                    BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                    BottomSheetBehavior.from(bottomSheet).setHideable(true);
                }
            });
            TextView txtBlock = sheetView.findViewById(R.id.txtBlock);
            TextView txtReport = sheetView.findViewById(R.id.txtReport);

            if (profile.getReport() == null) {
            } else if (profile.getReport())
                txtReport.setText(getString(R.string.unreported));
            else txtReport.setText(getString(R.string.report_user));

            if (blockStatus == null || blockStatus.equals(Constants.TAG_FALSE))
                txtBlock.setText(getString(R.string.block));
            else txtBlock.setText(getString(R.string.unblock));

            txtBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put(Constants.TAG_TYPE, Constants.TAG_BLOCK_USER);
                        json.put(Constants.TAG_RECEIVER_ID, profileId);
                        json.put(Constants.TAG_BLOCKED, true);
                        Timber.i("blockUser: " + json);
                        AppWebSocket.getInstance(mContext).send(json.toString());
                        blockUnBlockUser(profileId);
                        if (dbHelper.isChatIdExists(chatId)) {
                            dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_TRUE);
                            dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
                            dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
                        } else {
                            dbHelper.insertBlockStatus(chatId, profileId, profile.getUserName(), profile.getUserImage(), Constants.TAG_TRUE);
                            if (dbHelper.isChatIdExists(chatId)) {
                                dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
                                dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
                            }
                        }
                        EventBus.getDefault().postSticky(profileId);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            txtReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    App.preventMultipleClick(view);
                    if (txtReport.getText().toString().equals(getString(R.string.unreported)))
                        reportUser(profileId, "", "");
                    else openReportDialog();
                }
            });
        }
    }

    private void blockUnBlockUser(String partnerId) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_BLOCK_USER_ID, partnerId);
            if (LocaleManager.isRTL()) {
                requestMap.put(Constants.TAG_LANGUAGE_CODE, "ar");
            }else{
                requestMap.put(Constants.TAG_LANGUAGE_CODE, "en");
            }
            Call<HashMap<String, String>> call = apiInterface.blockUser(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        menuDialog.dismiss();
                        HashMap<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (blockStatus.equals(Constants.TAG_FALSE))
                                blockStatus = Constants.TAG_TRUE;
                            else blockStatus = Constants.TAG_FALSE;
//                            Log.i(TAG, "onResponse: " + responseMap);
                            App.makeToast(responseMap.get(Constants.TAG_MESSAGE));
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                }
            });
        }
    }

    private void openReportDialog() {
        if (getActivity() != null) {
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_user_report, null);
            reportDialog = new BottomSheetDialog(getActivity(), R.style.VideoCommentNewDialog); // Style here
            reportDialog.setContentView(sheetView);
            BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) sheetView.getParent());
            int maxHeight = (DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_HEIGHT, 0) * 80) / 100;
            mBehavior.setPeekHeight(maxHeight);
            sheetView.requestLayout();
            rvReports = sheetView.findViewById(R.id.reportView);
            reportDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {

                }
            });

            loadReports();
        }
    }

    private void loadReports() {
        reportAdapter = new UserReportAdapter(mContext, AdminData.userReportList, profileId, new ApiCallback() {
            @Override
            public void onResponse(Object apiResponse) {
                if (apiResponse instanceof ReportResponse) {
                    ReportResponse reportResponse = (ReportResponse) apiResponse;
                    Toast.makeText(mContext, reportResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    if (reportDialog != null && reportDialog.isShowing()) {
                        reportDialog.dismiss();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Close dialog here
                if (reportDialog != null && reportDialog.isShowing()) {
                    reportDialog.dismiss();
                }
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        rvReports.setLayoutManager(new LinearLayoutManager(mContext));
        rvReports.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        reportDialog.show();
    }

    @Override
    public void onResponse(Object apiResponse) {
        if (menuDialog != null && menuDialog.isShowing())
            menuDialog.cancel();
        if (reportDialog != null && reportDialog.isShowing())
            reportDialog.dismiss();
        Response<ReportResponse> response = (Response<ReportResponse>) apiResponse;
        if (response.isSuccessful()) {
            ReportResponse responseMap = response.body();
            if (responseMap.getStatus().equals(Constants.TAG_TRUE)) {
                profile.setReport(!profile.getReport());
                Toast.makeText(mContext, getString(R.string.success_unreport), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onFailure(Throwable t) {
        if (menuDialog != null && menuDialog.isShowing())
            menuDialog.cancel();
    }

  /*  private void reportUser(String partnerId, String title, String reprotId) {
        if (NetworkReceiver.isConnected()) {
            CommonApi.reportUser(partnerId, title, reprotId,  new ApiCallback() {
                @Override
                public void onResponse(Object apiResponse) {
                    if (menuDialog != null && menuDialog.isShowing())
                        menuDialog.cancel();
                    Response<ReportResponse> response = (Response<ReportResponse>) apiResponse;
                    if (response.isSuccessful()) {
                        ReportResponse responseMap = response.body();
                        if (responseMap.getStatus().equals(Constants.TAG_TRUE)) {
                            profile.setReport(!profile.getReport());
                            Toast.makeText(mContext, R.string.success_report, Toast.LENGTH_LONG).show();

                        }
                    }

                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }
*/
    private void reportUser(String partnerId, String title, String reprotId) {
        if (NetworkReceiver.isConnected()) {
            CommonApi.reportUser(partnerId, title, reprotId, new ApiCallback() {
                @Override
                public void onResponse(Object apiResponse) {
                    if (menuDialog != null && menuDialog.isShowing())
                        menuDialog.cancel();

                    ReportResponse responseMap = (ReportResponse) apiResponse; // correct cast
                    Log.d(TAG, "onResponse_response: " + new Gson().toJson(responseMap));

                    CommonApi.getProfile(profileId, new ApiCallback() {
                        @Override
                        public void onResponse(Object profileApiResponse) {
                            if (profileApiResponse instanceof ProfileResponse) {
                                Log.e(TAG,"After report profile response" + App.getGsonPrettyInstance().toJson(profileApiResponse));
                                if (responseMap.getStatus().equals(Constants.TAG_TRUE)) {
                                    profile.setReport(((ProfileResponse) profileApiResponse).getReport());
                                    Toast.makeText(mContext, responseMap.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "onResponse_getMessage: " + responseMap.getMessage());

                                    // update txtReport text if menuDialog exists
                                    if (menuDialog != null) {
                                        TextView txtReport = menuDialog.findViewById(R.id.txtReport);
                                        if (txtReport != null) {
                                            if (profile.getReport()) {
                                                txtReport.setText(getString(R.string.unreported)); // Undo Report
                                            } else {
                                                txtReport.setText(getString(R.string.report_user));
                                            }
                                        }
                                    }

                                    // close dialogs
                                    if (menuDialog != null && menuDialog.isShowing()) {
                                        menuDialog.dismiss();
                                    }
                                    if (reportDialog != null && reportDialog.isShowing()) {
                                        reportDialog.dismiss();
                                    }
                                }

                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {

                        }
                    });

                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    private void messageCheck() {
        if (NetworkReceiver.isConnected()) {

            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_PROFILE_ID, profileId);
            Timber.i("messageCheck: params=> %s", new Gson().toJson(map));

            Call<Map<String, String>> call = apiInterface.messagecheck(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().get("status").equals("true")) {
                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            chatIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                            chatIntent.putExtra(Constants.TAG_PARTNER_NAME, profile.getName());
                            chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, profile.getUserImage());
                            chatIntent.putExtra(Constants.TAG_BLOCKED_BY_ME, profile.getBlockedByMe());

                            startActivity(chatIntent);
                        } else {
                            App.dialog(mContext, "", mContext.getResources().getString(R.string.user_alert_message), mContext.getResources().getColor(R.color.colorBlack));
                        }
                    }

                }

                @Override
                public void onFailure
                        (@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        } else {
            Toasty.error(mContext, mContext.getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }

    }

    public static void animateZoom(Context context) {
        ((Activity) context).
                overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }

    private void followUnfollowUser(String publisherId) {

        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            followRequest.setUserId(publisherId);
            followRequest.setFollowerId(GetSet.getUserId());
            if (profile.getFollow().equals(Constants.TAG_FALSE))
                followRequest.setType(Constants.TAG_FOLLOW_USER);
            else
                followRequest.setType(Constants.TAG_UNFOLLOW_USER);

            Call<Map<String, String>> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<java.util.Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> followResponse = response.body();
                        if (followResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
//                                Timber.d("onResponse: %s=> ", App.getGsonPrettyInstance().toJson(followResponse));
                            txtFollowersCount.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_in));
                            int getCount = Integer.parseInt(txtFollowersCount.getText().toString().replace("-", "0"));

                            if (followResponse.get(Constants.TAG_MESSAGE).equals("Followed successfully")) {
                                txtFollowersCount.setText(String.valueOf(getCount + 1));
                                profile.setFollow(Constants.TAG_TRUE);
                            } else if (followResponse.get(Constants.TAG_MESSAGE).equals("Unfollowed successfully")) {
                                txtFollowersCount.setText(String.valueOf(getCount - 1));
                                profile.setFollow(Constants.TAG_FALSE);
                            }

                            if (profile.getFollow().equals(Constants.TAG_FOLLOW_USER)) {
                                EventBus.getDefault().post(new ForYouFollowFollowing(false, publisherId));
                                EventBus.getDefault().post(new FollowingFollowFollowing(false, publisherId));

                            } else {
                                EventBus.getDefault().post(new ForYouFollowFollowing(true, publisherId));
                                EventBus.getDefault().post(new FollowingFollowFollowing(true, publisherId));
                            }
                            setFollowStatus(profile.getFollow());

                            txtFollowersCount.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));
                        }

                    }
                }

                @Override
                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void goToPrime() {
        Intent prime = new Intent(getActivity(), PrimeActivity.class);
        prime.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(prime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public void refreshProfile(FollowingProfileUpdate profileUpdate) {
        Timber.tag(TAG).d("refreshProfile: %s", profileUpdate.id);
        if (profileUpdate != null && getActivity() != null) {
            profileId = profileUpdate.id;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume: %s", profileId);
        if (!TextUtils.isEmpty(profileId)) {
            getUserProfile(profileId, "");
        }
    }

    @Override
    public void onPause() {
        if (call != null) call.cancel();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class ProfilePagerAdapter extends FragmentStateAdapter {
        int NUM_OF_TABS = 1;

        public ProfilePagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                default: {
                    return new MyVideosFragment(profileId, getString(R.string.videos_txt), Constants.TAG_OTHER_PROFILE , blockStatus);
                }
                case 1: {
                    return new LikedVideosFragment(GetSet.getUserId(), getString(R.string.liked_videos), Constants.TAG_OWN_PROFILE);
                }
            }
        }

        @Override
        public int getItemCount() {
            if (profileId != null && profileId.equals(GetSet.getUserId())) NUM_OF_TABS = 2;
            else NUM_OF_TABS = 1;
            return NUM_OF_TABS;
        }

        public void resetViewPager() {

        }
    }
}