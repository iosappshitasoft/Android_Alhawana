package com.app.alhawanajd.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.app.alhawanajd.MyFavouriteVideosFragment;
import com.app.alhawanajd.NotificationFragment;
import com.app.alhawanajd.RepostedVideosFragment;
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
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.alhawanajd.ChatActivity;
import com.app.alhawanajd.FollowersActivity;
import com.app.alhawanajd.LikedVideosFragment;
import com.app.alhawanajd.LoginActivity;
import com.app.alhawanajd.MyVideosFragment;
import com.app.alhawanajd.SingleImageActivity;
import com.app.alhawanajd.home.eventbus.FollowingFollowFollowing;
import com.app.alhawanajd.home.eventbus.ForYouFollowFollowing;
import com.app.alhawanajd.home.eventbus.OtherProfileToFollowingProfile;
import com.app.alhawanajd.home.eventbus.OtherProfileToForYouProfile;
import com.app.alhawanajd.home.eventbus.OtherProfileUpdate;
import com.app.helper.AppWebSocket;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.model.FollowRequest;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.model.ReportResponse;
import com.app.utils.AdminData;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DevicePref;
import com.app.utils.Logging;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiCallback;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.api.CommonApi;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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


public class OthersProfileActivity extends BaseFragmentActivity implements View.OnClickListener, ApiCallback {

    private static final String TAG = OthersProfileActivity.class.getSimpleName();
    private Context mContext;
    private AppBarLayout appBarLayout;
    private LinearLayout contentLay  , full_screen;
    private ImageView btnBack,btnNotification, btnShare;
    private ImageView btnSettings,btnSettingsnew;
    private TextView txtTitle;
    private ShapeableImageView profileImage;
    private ImageView premiumImage;
    private LinearLayout nameLay;
    private TextView txtName;
    private ImageView genderImage;
    private TextView txtUserName;
    private TextView userBio,txtLink;
    private LinearLayout displayCountLay;
    private LinearLayout heartLay;
    private TextView txtHeartCount;
    private View divider;
    private LinearLayout fansLay;
    private TextView txtFollowersCount;
    private LinearLayout followingLay;
    private TextView txtFollowingsCount;
    private ImageView btnFindFriends;
    private LinearLayout editLay,shareprofile;
    private LinearLayout giftsLay;
    private LinearLayout coinsLay;
    private LinearLayout messageLay;
    private LinearLayout followLay, downArrowLay;
    private ConstraintLayout profileEditLay;
    private ConstraintLayout profileMessageLay;
    private ImageView btnFollow;
    private TextView txtFollowBack;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private RelativeLayout premiumLay;
    private MaterialCardView renewalLay, subscribeLay;
    private MaterialButton btnRenewal;
    private Button btnSubscribe;
    private TextView txtPrimeTitle, txtPrice;
    private ProgressBar progressBar;
    FrameLayout fragment_container;

    ProgressDialog pdLoading;
    private ApiInterface apiInterface;
    private ProfileResponse profile;
    private String profileId = "", from = "", profileUserName = "" , delete_status ="" , activate_status="";
    public String chatId;
    private String blockStatus = Constants.TAG_FALSE;
    boolean videoEnabled = false;
    private HomePagerAdapter pagerAdapter;
    private final int NUM_OF_TABS = 2;
    private BottomSheetDialog menuDialog, reportDialog;
    private RecyclerView reportsView;
    private UserReportAdapter reportAdapter;
    private DBHelper dbHelper;
    private ImageView more_icon;
    private boolean isProfileUpdated = false;
    private boolean isFollowApiRunning = false;
    boolean isBioVisible = false;
    LinearLayout bioLayout,linkLayout;
    TextView txtBio;
    ImageView arrowIcon;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(OtherProfileUpdate event) {
        Timber.d("onMessageEvent: %s", event.id);
        getProfile(event.id, "");
        if (btnFollow != null) btnFollow.setEnabled(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_others_profile);
        mContext = this;
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        profileId = getIntent().getStringExtra(Constants.TAG_USER_ID);
        profileUserName = getIntent().getStringExtra(Constants.TAG_USER_NAME) == null ? "" : getIntent().getStringExtra(Constants.TAG_USER_NAME);

        Log.d(TAG, "onCreate11111111111111111: "+from);
        dbHelper = DBHelper.getInstance(this);
        chatId = GetSet.getUserId() + profileId;
        blockStatus = dbHelper.getBlockStatus(chatId);
        findViews();
        initView();
        if (getIntent().hasExtra(Constants.TAG_INTENT_DATA)) {
            String jsonData = getIntent().getStringExtra(Constants.TAG_INTENT_DATA);
            profile = new Gson().fromJson(jsonData, ProfileResponse.class);
            setProfileView(profile);
        }

        getProfile(profileId, profileUserName);

//        btnSettings.setOnClickListener(v -> {
//            Log.d("TAG", "Settings Clicked");
//            Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show();
//        });

    }

    private void findViews() {
        appBarLayout = findViewById(R.id.appBarLay);
        contentLay = findViewById(R.id.contentLay);
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnNotification = findViewById(R.id.btnNotification);
//        btnSettings = findViewById(R.id.btnSettings);
        more_icon = findViewById(R.id.more_icon);
        txtBio = findViewById(R.id.txtBio);
//        btnSettingsnew = findViewById(R.id.btnSettingsnew);
//        txtTitle = findViewById(R.id.txtTitle);
        profileImage = findViewById(R.id.profileImage);
        premiumImage = findViewById(R.id.premiumImage);
        nameLay = findViewById(R.id.nameLay);
        txtName = findViewById(R.id.txtName);
        genderImage = findViewById(R.id.genderImage);
        txtUserName = findViewById(R.id.txtUserName);
        userBio = findViewById(R.id.userBio);
        displayCountLay = findViewById(R.id.displayCountLay);
        heartLay = findViewById(R.id.heartLay);
        txtHeartCount = findViewById(R.id.txtHeartCount);
        divider = findViewById(R.id.divider_views);
        fansLay = findViewById(R.id.fansLay);
        txtFollowersCount = findViewById(R.id.txtFollowersCount);
        followingLay = findViewById(R.id.followingLay);
        txtFollowingsCount = findViewById(R.id.txtFollowingsCount);
        profileMessageLay = findViewById(R.id.profileMessageLay);
        profileEditLay = findViewById(R.id.profileEditLay);
        editLay = findViewById(R.id.editLay);
        shareprofile = findViewById(R.id.shareprofile);
        btnFindFriends = findViewById(R.id.btnFindFriends);
        giftsLay = findViewById(R.id.giftsLay);
        coinsLay = findViewById(R.id.coinsLay);
        messageLay = findViewById(R.id.messageLay);
        followLay = findViewById(R.id.followLay);
        btnFollow = findViewById(R.id.btnFollow);
        bioLayout = findViewById(R.id.bioLayout);
        linkLayout = findViewById(R.id.linkLayout);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        premiumLay = findViewById(R.id.premiumLay);
        renewalLay = findViewById(R.id.renewalLay);
        subscribeLay = findViewById(R.id.subscribeLay);
        btnSubscribe = findViewById(R.id.btnSubscribe);
        txtPrimeTitle = findViewById(R.id.txtPrimeTitle);
        txtPrice = findViewById(R.id.txtPrice);
        btnRenewal = findViewById(R.id.btnRenewal);
        progressBar = findViewById(R.id.progressBar);
        full_screen = findViewById(R.id.full_screen);
        fragment_container = findViewById(R.id.fragment_container);

        txtLink = findViewById(R.id.txtLink);
        downArrowLay = findViewById(R.id.downArrowLay);
        txtFollowBack = findViewById(R.id.txtFollowBack);
        arrowIcon = findViewById(R.id.arrowIcon);

    }

    public void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        SlidrInterface slidrInterface = Slidr.attach(this, config);
        profileMessageLay.setVisibility(View.VISIBLE);
        Log.d(TAG, "profileId: "+profileId);

        profileEditLay.setVisibility(View.GONE);
       // tabLayout.setVisibility(View.GONE);

        // Waiting for API Data so the Views are disabled
        fansLay.setEnabled(false);
        profileImage.setEnabled(false);
        premiumImage.setEnabled(false);
        fansLay.setEnabled(false);
        followingLay.setEnabled(false);
        btnFollow.setEnabled(false);
        messageLay.setEnabled(false);
        editLay.setVisibility(View.GONE);
        txtBio.setVisibility(View.VISIBLE);
        bioLayout.setVisibility(View.VISIBLE);
        btnFindFriends.setVisibility(View.GONE);
        shareprofile.setVisibility(View.GONE);
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
            btnNotification.setRotation(180);
        } else {
            btnBack.setRotation(0);
            btnNotification.setRotation(0);
        }
        btnNotification.setVisibility(View.VISIBLE);
        btnShare.setVisibility(View.VISIBLE);

        pdLoading = new ProgressDialog(OthersProfileActivity.this, R.style.CameraAlertDialog);
        pdLoading.setCancelable(false);

        btnBack.setImageResource(R.drawable.back);
        btnBack.setVisibility(View.VISIBLE);
//        btnSettings.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.menu));
//        btnSettings.setVisibility(View.GONE);

        contentLay.setVisibility(View.GONE);
        appBarLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        profileImage.setOnClickListener(this);
        fansLay.setOnClickListener(this);
        followingLay.setOnClickListener(this);
        btnFollow.setOnClickListener(this);
        downArrowLay.setOnClickListener(this);
        followLay.setOnClickListener(this);
        txtFollowBack.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        messageLay.setOnClickListener(this);
        btnShare.setOnClickListener(this);
        btnNotification.setOnClickListener(this);
//        btnSettings.setOnClickListener(this);
//        btnSettingsnew.setOnClickListener(this);
        more_icon.setOnClickListener(view -> {

            Log.d(TAG, "onClickyess12: ");
            App.preventMultipleClick(more_icon);
            if (menuDialog != null && !menuDialog.isShowing()) {
                TextView txtReport = menuDialog.findViewById(R.id.txtReport);
                TextView txtBlock = menuDialog.findViewById(R.id.txtBlock);

                if (blockStatus != null) {
                    if (blockStatus.equals(Constants.TAG_FALSE))
                        txtBlock.setText(getString(R.string.block));
                    else txtBlock.setText(getString(R.string.unblock));
                }
                if (profile.getReport() == null) {
                } else if (profile.getReport())
                    txtReport.setText(getString(R.string.unreported));
                else txtReport.setText(getString(R.string.report_user));

                menuDialog.show();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }
    }

    private void getProfile(String getUserId, String userName) {
        if (GetSet.getUserId() != null && apiInterface != null) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(getUserId);
            request.setProfileId(getUserId);
          /*  if (!userName.equals("")){ request.setUser_name(userName);}
            else {request.setProfileId(getUserId);}*/
            Timber.d("getProfile params: %s", App.getGsonPrettyInstance().toJson(request));
            Call<ProfileResponse> call = apiInterface.getProfile(request);

            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                    Log.d(TAG, "onResponsedelete_status_other: "+"delete_status");


                    if (response.isSuccessful()) {
                        profile = response.body();
                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                            delete_status = profile.getDeleted_status();
                            activate_status = profile.getActivate_status();
                            Log.d(TAG, "onResponsedelete_status_other: "+delete_status);

                            if (delete_status.equals("1")){
                                full_screen.setVisibility(View.GONE);
                                contentLay.setVisibility(View.GONE);
                                opendeleteDialog();
                            }
                            if (activate_status.equals("false")){
                                full_screen.setVisibility(View.GONE);
                                contentLay.setVisibility(View.GONE);
                                opendeactivateDialog();
                            }
                            if (profileId == null) {
                                profileId = profile.getUserId();
                            }
                            else if (profileId.equals(""))
                                profileId = profile.getUserId();
                            if (!GetSet.getUserId().equals(profile.getUserId())) {
                                blockStatus = profile.getBlockedByMe();
                                chatId = GetSet.getUserId() + profileId;
                            }

                            initViewPager();
                            setProfileView(profile);
                            contentLay.setVisibility(View.VISIBLE);
                            appBarLayout.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            initReportDialog();
                            /*progressBar.setAlpha(0.0f);
                            contentLay.animate()
                                    .alpha(1.0f)
                                    .setListener(null);*/
                        }else if (profile.getStatus().equals(com.app.utils.Constants.TAG_ACCOUNT_BLOCKED)) {
                            App.makeToast(getString(R.string.Account_block));
                            com.app.model.GetSet.reset();
                            SharedPref.clearAll();
                            DBHelper.getInstance(OthersProfileActivity.this).clearDB();
                            Intent logout = new Intent(OthersProfileActivity.this, LoginActivity.class);
                            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(logout);
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ProfileResponse> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void opendeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OthersProfileActivity.this);
        builder.setMessage(getString(R.string.notification_del_acc));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
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

    private void opendeactivateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OthersProfileActivity.this);
        builder.setMessage(getString(R.string.deactive_admin));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
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

    private void setProfileView(ProfileResponse profile) {

        if (!TextUtils.isEmpty(profile.getYoutubeLink())) {
            downArrowLay.setVisibility(View.VISIBLE);
            Log.d(TAG, "setProfileView22222: ");
            linkLayout.setVisibility(View.VISIBLE);
            txtLink.setText(profile.getYoutubeLink());
        }

        if (profile.getPremiumMember().equals(Constants.TAG_TRUE))
            premiumImage.setVisibility(View.VISIBLE);
        else
            premiumImage.setVisibility(View.GONE);


        txtName.setText(profile.getName());
//        if (GetSet.getUserId() == null || GetSet.getUserId().equals(profile.getUserId())) {
//            txtTitle.setText(mContext.getString(R.string.my_profile));
//        } else {
//            txtTitle.setText(profile.getName());
//        }
        txtBio.setText(profile.getBio());
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
        txtHeartCount.setText(profile.getLikes() != null ? "" + profile.getLikes() : "" + 0);


        //commented on NOv27
        /*if (LocaleManager.isRTL()) {
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(50, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
            userBio.setLayoutParams(llp);
        }

        if (TextUtils.isEmpty(profile.getBio())) userBio.setVisibility(View.GONE);
        else {
            userBio.setText(profile.getBio());
            userBio.setVisibility(View.VISIBLE);
        }*/

        txtFollowersCount.setText(String.valueOf(profile.getFollowers() != null ? profile.getFollowers() : "0"));
        txtFollowingsCount.setText(String.valueOf(profile.getFollowings() != null ? profile.getFollowings() : "0"));
        if (profile.getGender() != null) {
            genderImage.setVisibility(View.VISIBLE);
            genderImage.setImageDrawable(profile.getGender().equals(Constants.TAG_MALE) ?
                    getResources().getDrawable(R.drawable.men) : getResources().getDrawable(R.drawable.women));
        } else {
            genderImage.setVisibility(View.INVISIBLE);
        }

//        txtOtherUserVCount.setText(String.valueOf(profile.getVideosCount() != null ? profile.getVideosCount() : "0"));
        setFollowStatus(profile.getFollow());
        if (btnFollow != null) btnFollow.setEnabled(true);
        // Click event activated
        fansLay.setEnabled(true);
        profileImage.setEnabled(true);
        premiumImage.setEnabled(true);
        fansLay.setEnabled(true);
        followingLay.setEnabled(true);
        btnFollow.setEnabled(true);
        messageLay.setEnabled(true);

        Glide.with(getApplicationContext())
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

    /*private void initViewPager() {
        if (pagerAdapter != null) {
            pagerAdapter.resetViewPager();
        }
        pagerAdapter = new HomePagerAdapter(OthersProfileActivity.this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(0);
    }*/

    private void initViewPager() {

        // Create / reset adapter
        if (pagerAdapter != null) {
            pagerAdapter.resetViewPager();
        }
        pagerAdapter = new HomePagerAdapter(OthersProfileActivity.this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(NUM_OF_TABS);
        viewPager.setCurrentItem(0, false);

        // Attach TabLayout and set icons (icon-only tabs)
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.img_1_private_unselected);   // left icon
                    break;
                case 1:
                    tab.setIcon(R.drawable.img_1_repost_unselected);       // second icon
                    break;
            }
        }).attach();
    }

    private void initReportDialog() {
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_profile, null);
        menuDialog = new BottomSheetDialog(this, R.style.SimpleBottomDialog); // Style here
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
        TextView shareprofile = sheetView.findViewById(R.id.shareprofile);

        if (profile.getReport() == null) {
        } else if (profile.getReport())
            txtReport.setText(getString(R.string.unreported));
        else txtReport.setText(getString(R.string.report_user));

        if (blockStatus != null) {
            if (blockStatus.equals(Constants.TAG_FALSE))
                txtBlock.setText(getString(R.string.block));
            else txtBlock.setText(getString(R.string.unblock));
        }

        shareprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareMyProfile(profile);
            }
        });

        txtBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constants.TAG_TYPE, Constants.TAG_BLOCK_USER);
                    json.put(Constants.TAG_RECEIVER_ID, profileId);
                    json.put(Constants.TAG_BLOCKED, true);
                    Logging.i(TAG, "blockUser: " + json);
                    AppWebSocket.getInstance(OthersProfileActivity.this).send(json.toString());
                    blockUnBlockUser(profileId);
                    if (dbHelper.isChatIdExists(chatId)) {
                        dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_TRUE);
                        dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
                        dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
                    } else {
                        dbHelper.insertBlockStatus(chatId, profileId, profileUserName, profile.getUserImage(), Constants.TAG_TRUE);
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
                 //unreported - Undo Report
                if (txtReport.getText().toString().equals(getString(R.string.unreported)))
                    reportUser(profileId, "", "");
                else openReportDialog();
            }
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
                            /*if (blockStatus.equals(Constants.TAG_FALSE))
                                blockStatus = Constants.TAG_TRUE;
                            else blockStatus = Constants.TAG_FALSE;

*/
                            if (responseMap.get(Constants.TAG_BLOCK_STATUS).equals("1")) {
                                blockStatus = Constants.TAG_TRUE;
                                App.makeToast(getString(R.string.user_blocked_successfully));
                                if (dbHelper.isChatIdExists(chatId)) {
                                    dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, "true");
                                } else {
                                    dbHelper.insertBlockStatus(chatId, profileId, profileUserName, profile.getUserImage(), Constants.TAG_TRUE);
                                }
                            } else {
                                blockStatus = Constants.TAG_FALSE;
                                if (dbHelper.isChatIdExists(chatId)) {
                                    dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_FALSE);
                                } else {
                                    dbHelper.insertBlockStatus(chatId, profileId, profileUserName, profile.getUserImage(), Constants.TAG_TRUE);
                                }

                                App.makeToast(getString(R.string.unblocked_successfully));
                            }
                            //    Log.i(TAG, "onResponse: " + responseMap);
                            isProfileUpdated = true;
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
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_user_report, null);
        reportDialog = new BottomSheetDialog(this, R.style.VideoCommentNewDialog); // Style here
        reportDialog.setContentView(sheetView);
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) sheetView.getParent());
        int maxHeight = (DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_HEIGHT, 0) * 80) / 100;
        mBehavior.setPeekHeight(maxHeight);
        sheetView.requestLayout();
        reportsView = sheetView.findViewById(R.id.reportView);
        reportDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });

        loadReports();
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

                    CommonApi.getProfile(profileId, new ApiCallback() {
                        @Override
                        public void onResponse(Object profileApiResponse) {
                            if (profileApiResponse instanceof ProfileResponse) {
                                Log.e(TAG,"After report profile response" + App.getGsonPrettyInstance().toJson(profileApiResponse));
                                    profile.setReport(((ProfileResponse) profileApiResponse).getReport());

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

                        @Override
                        public void onFailure(Throwable t) {

                        }
                    });

                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (reportDialog != null && reportDialog.isShowing()) {
                    reportDialog.dismiss();
                }
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        reportsView.setLayoutManager(new LinearLayoutManager(mContext));
        reportsView.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        reportDialog.show();
    }

    @Override
    public void onResponse(Object apiResponse) {
        if (menuDialog != null && menuDialog.isShowing())
            menuDialog.cancel();
        Response<ReportResponse> response = (Response<ReportResponse>) apiResponse;
        if (response.isSuccessful()) {
            ReportResponse responseMap = response.body();
            if (responseMap.getStatus().equals(Constants.TAG_TRUE)) {
                profile.setReport(!profile.getReport());
                Toast.makeText(mContext, responseMap.getMessage(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "onResponse_Message: "+responseMap.getMessage());
            }
        }
    }

    @Override
    public void onFailure(Throwable t) {
        if (menuDialog != null && menuDialog.isShowing())
            menuDialog.cancel();
    }

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

    private boolean isFollowingUser() {
        if (profile == null || profile.getFollow() == null) {
            return false;
        }
        String follow = profile.getFollow();
        return follow.equalsIgnoreCase(Constants.TAG_TRUE)
                || follow.equalsIgnoreCase(Constants.TAG_FOLLOW_USER);
    }

    private void followUnFollowUser() {
        if (isFollowApiRunning || profile == null || GetSet.getUserId() == null) {
            return;
        }
        if (!NetworkReceiver.isConnected()) {
            Toasty.error(getApplicationContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
            return;
        }

        isFollowApiRunning = true;
        progressBar.setVisibility(View.VISIBLE);
        FollowRequest followRequest = new FollowRequest();
        if (profileId != null) followRequest.setUserId(GetSet.getUserId());
        else followRequest.setUser_name(profileUserName);
        followRequest.setFollowerId(profileId);

        if (isFollowingUser())
            followRequest.setType(Constants.TAG_UNFOLLOW_USER);
        else
            followRequest.setType(Constants.TAG_FOLLOW_USER);

        Call<Map<String, String>> call = apiInterface.followUser(followRequest);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                isFollowApiRunning = false;
                progressBar.setVisibility(View.GONE);
                Map<String, String> followResponse = response.body();
                if (response.isSuccessful() && followResponse != null
                        && Constants.TAG_TRUE.equals(followResponse.get(Constants.TAG_STATUS))) {
                    isProfileUpdated = true;
                    txtFollowersCount.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.anim_zoom_in));
                    int getCount = Integer.parseInt(txtFollowersCount.getText().toString().replace("-", "0"));
                    String message = followResponse.get(Constants.TAG_MESSAGE);
                    boolean followed = Constants.TAG_FOLLOW_USER.equals(followRequest.getType())
                            || (message != null && message.toLowerCase().contains("followed successfully")
                            && !message.toLowerCase().contains("unfollowed"));
                    boolean unfollowed = Constants.TAG_UNFOLLOW_USER.equals(followRequest.getType())
                            || (message != null && message.toLowerCase().contains("unfollowed"));

                    if (followed && !unfollowed) {
                        txtFollowersCount.setText(String.valueOf(getCount + 1));
                        profile.setFollow(Constants.TAG_TRUE);
                    } else if (unfollowed) {
                        if (getCount != 0) {
                            txtFollowersCount.setText(String.valueOf(getCount - 1));
                        } else {
                            txtFollowersCount.setText("0");
                        }
                        profile.setFollow(Constants.TAG_FALSE);
                    }
                    if (isFollowingUser()) {
                        EventBus.getDefault().post(new ForYouFollowFollowing(false, profileId));
                        EventBus.getDefault().post(new FollowingFollowFollowing(false, profileId));
                    } else {
                        EventBus.getDefault().post(new ForYouFollowFollowing(true, profileId));
                        EventBus.getDefault().post(new FollowingFollowFollowing(true, profileId));
                    }
                    setFollowStatus(profile.getFollow());

                    EventBus.getDefault().post(new OtherProfileToForYouProfile(profileId));
                    EventBus.getDefault().post(new OtherProfileToFollowingProfile(profileId));
                    txtFollowersCount.startAnimation(AnimationUtils.loadAnimation(OthersProfileActivity.this, R.anim.anim_zoom_out));
                }
            }

            @Override
            public void onFailure
                    (@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                isFollowApiRunning = false;
                progressBar.setVisibility(View.GONE);
                call.cancel();
            }
        });
    }

    private void setFollowStatus(String follow) {
        if (follow != null) {
            if (follow.equals(Constants.TAG_TRUE)) {
                if(profileId.equalsIgnoreCase("676518e390396c9189355004")){
                    followLay.setVisibility(View.GONE);
                }
                followLay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_secondary_outline));
                btnFollow.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.added));
                txtFollowBack.setText(getString(R.string.followings));
            } else {
                if(profileId.equalsIgnoreCase("676518e390396c9189355004")){
                    followLay.setVisibility(View.GONE);
                }
                followLay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.follow_round_corner));
                btnFollow.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.add_friend));
                txtFollowBack.setText(getString(R.string.follow_new));
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.downArrowLay:{
                isBioVisible = !isBioVisible;

                if (isBioVisible) {
                    bioLayout.setVisibility(View.VISIBLE);
                    arrowIcon.setImageResource(R.drawable.arrow_d);
                } else {
                    bioLayout.setVisibility(View.GONE);
                    arrowIcon.setImageResource(R.drawable.arrow_u);
                }
            }
            break;
            case R.id.fansLay: {
                App.preventMultipleClick(fansLay);
                if (blockStatus == null || blockStatus.equals(Constants.TAG_FALSE)) {
                    Intent followersIntent = new Intent(OthersProfileActivity.this, FollowersActivity.class);
                    followersIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
                    followersIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                    followersIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                    startActivity(followersIntent);
                } else {
                    App.makeToast(mContext.getString(R.string.unblock_description));
                }
            }
            break;
            case R.id.followingLay:
                App.preventMultipleClick(followingLay);
                Log.d(TAG, "onClick_blockStatus: "+blockStatus);
                if (blockStatus == null || blockStatus.equals(Constants.TAG_FALSE)) {
                    Intent followingsIntent = new Intent(OthersProfileActivity.this, FollowersActivity.class);
                    followingsIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWINGS);
                    followingsIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                    startActivity(followingsIntent);
                } else {
                    App.makeToast(mContext.getString(R.string.unblock_description));
                }
                break;
            case R.id.followLay:
            case R.id.txtFollowBack:
            case R.id.btnFollow:
                App.preventMultipleClick(followLay);
                App.preventMultipleClick(txtFollowBack);
                if (blockStatus == null || blockStatus.equals(Constants.TAG_FALSE)) {
                    followUnFollowUser();
                } else {
                    App.makeToast(mContext.getString(R.string.unblock_description));
                }
                break;

            case R.id.btnBack:
                App.preventMultipleClick(btnBack);
                onBackPressed();
                break;
            case R.id.btnShare:
                App.preventMultipleClick(btnShare);
                shareMyProfile(profile);
                break;
            case R.id.btnNotification:
                NotificationFragment notificationFragment = new NotificationFragment();

                Bundle bundle = new Bundle();
                bundle.putString(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                bundle.putString(Constants.TAG_FROM, "otherprofile");
                notificationFragment.setArguments(bundle);

                // make container visible
                View container = findViewById(R.id.fragment_container);
                container.setVisibility(View.VISIBLE);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, notificationFragment)
                        .addToBackStack("NotificationFragment")
                        .commit();
                break;
//            case R.id.btnSettings:
////            case R.id.btnSettingsnew:
//            {
//                Log.d(TAG, "onClickyess: ");
//                App.preventMultipleClick(btnSettings);
//                if (menuDialog != null && !menuDialog.isShowing()) {
//                    TextView txtReport = menuDialog.findViewById(R.id.txtReport);
//                    TextView txtBlock = menuDialog.findViewById(R.id.txtBlock);
//
//                    if (blockStatus != null) {
//                        if (blockStatus.equals(Constants.TAG_FALSE))
//                            txtBlock.setText(getString(R.string.block));
//                        else txtBlock.setText(getString(R.string.unblock));
//                    }
//                    if (profile.getReport() == null) {
//                    } else if (profile.getReport())
//                        txtReport.setText(getString(R.string.unreported));
//                    else txtReport.setText(getString(R.string.report_user));
//
//                    menuDialog.show();
//                }
//            }
//            break;
            case R.id.messageLay:
                Log.d(TAG, "onClickyesas: ");
                App.preventMultipleClick(messageLay);
                if (Constants.Login_method.equals("skip")){
                    GetSet.reset();
                    SharedPref.clearAll();
                    DBHelper.getInstance(OthersProfileActivity.this).clearDB();
                    Intent logout = new Intent(OthersProfileActivity.this, LoginActivity.class);
                    logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(logout);
                }else {
                    if (blockStatus.equals(Constants.TAG_FALSE)) {
                        messageCheck();
                    } else {
                        App.makeToast(mContext.getString(R.string.unblock_description));
                    }
                }
                break;
            case R.id.profileImage:
                App.preventMultipleClick(profileImage);
                if (blockStatus == null ||blockStatus.equals(Constants.TAG_FALSE)) {
                    Intent singleImage = new Intent(OthersProfileActivity.this, SingleImageActivity.class);
                    singleImage.putExtra("image", String.valueOf(profile.getUserImage()));
                    startActivity(singleImage);
                    overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
                } else {
                    App.makeToast(mContext.getString(R.string.unblock_description));
                }
                break;

            default:
                break;
        }
    }

    private void messageCheck() {

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_PROFILE_ID, profile.getUserId());

            Call<Map<String, String>> call = apiInterface.messagecheck(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                    if (response.body() != null) {
                        if (response.body().get("status").equals("true")) {
                            Intent chatIntent = new Intent(OthersProfileActivity.this, ChatActivity.class);
                            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            chatIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                            chatIntent.putExtra(Constants.TAG_PARTNER_NAME, profile.getName());
                            chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, profile.getUserImage());
                            chatIntent.putExtra(Constants.TAG_BLOCKED_BY_ME, profile.getBlockedByMe());

                            startActivity(chatIntent);

/*                            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            chatIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                            chatIntent.putExtra(Constants.TAG_PARTNER_NAME, profile.getName());
                            chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, profile.getUserImage());
*//*                            chatIntent.putExtra(Constants.TAG_BLOCKED_BY_ME, profile.getBlockedByMe());*//*
                            startActivity(chatIntent);*/
                        } else {
                            App.dialog(OthersProfileActivity.this, "", getResources().getString(R.string.user_alert_message), getResources().getColor(R.color.colorBlack));
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
            Toasty.error(OthersProfileActivity.this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }

    }

  /*  public class HomePagerAdapter extends FragmentStateAdapter {
        public HomePagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {

                case 0:
                default: {
                    if (profileId.equals(""))
                        profileId = profileUserName;
                    Log.d(TAG, "createFragment_blockStatus: "+blockStatus);
                    return new MyVideosFragment(profileId, getString(R.string.videos_txt), Constants.TAG_OTHER_PROFILE ,  profileUserName
                    );

                }
                case 1: {
                    return new LikedVideosFragment(GetSet.getUserId(), getString(R.string.liked_videos), Constants.TAG_OWN_PROFILE);
                }
                case 2: {
                    return new FavSoundFragment(GetSet.getUserId());
                }

            }
        }

        @Override
        public int getItemCount() {
            return NUM_OF_TABS;
        }

        public void resetViewPager() {

        }
    }*/

    public class HomePagerAdapter extends FragmentStateAdapter {

        public HomePagerAdapter(@NonNull FragmentActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                default:
                if (profileId.equals("")){
                    profileId = profileUserName;
                }
                Log.d(TAG, "createFragment_blockStatus: "+blockStatus);
                return new MyVideosFragment(profileId, getString(R.string.videos_txt), Constants.TAG_OTHER_PROFILE ,  profileUserName);

                case 1:
                    return new RepostedVideosFragment(profileId, getString(R.string.repost), Constants.TAG_OTHER_PROFILE);
            }
        }

        @Override
        public int getItemCount() {
            return NUM_OF_TABS;
        }

        public void resetViewPager() {
            // no-op for now
        }
    }


    @Override
    protected void onDestroy() {
        //unregisterNetworkReceiver();
        super.onDestroy();
        /*if (!this.isDestroyed()) {
            Glide.with(getApplicationContext()).pauseRequests();
        }*/
    }

    @Override
    public void onBackPressed() {

        fragment_container.setVisibility(View.GONE);
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();

            // After popping, if NO fragments in back stack -> hide container
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                View container = findViewById(R.id.fragment_container);
                if (container != null) {
                    container.setVisibility(View.GONE);
                }
            }

        } else if (from != null && from.equals(Constants.TAG_SINGLE_VIDEO)) {
            if (isProfileUpdated) {
                Intent intent = new Intent();
                intent.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(profile));
                setResult(RESULT_OK, intent);
            }
            finish();
        } else if (profile != null) {
            Intent intent = new Intent();
            intent.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(profile));
            setResult(RESULT_OK, intent);
            finish();
        }  else {
            super.onBackPressed();
        }
        overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);

    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    /*@Override
    public void onNetworkChanged(boolean isConnected) {
        //AppUtils.showSnack(this, parentLay, isConnected);
    }*/

}


