package com.app.alhawanajd.profile;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.app.alhawanajd.App;
import com.app.alhawanajd.BuildConfig;
import com.app.alhawanajd.EditFieldActivity;
import com.app.alhawanajd.FollowersActivity;
import com.app.alhawanajd.ImageViewActivity;
import com.app.alhawanajd.LikedVideosFragment;
import com.app.alhawanajd.LoginActivity;
import com.app.alhawanajd.MyFavouriteVideosFragment;
import com.app.alhawanajd.MyVideosFragment;
import com.app.alhawanajd.NewEditProfileActivity;
import com.app.alhawanajd.R;
import com.app.alhawanajd.RepostedVideosFragment;
import com.app.alhawanajd.SettingsActivity;
import com.app.alhawanajd.search.SearchUserActivity;
import com.app.db.DBHelper;
import com.app.helper.LocaleManager;
import com.app.helper.PermissionsUtils;
import com.app.helper.StorageUtils;
import com.app.helper.callback.OnOkCancelClickListener;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.model.event.GetProfileEvent;
import com.app.utils.Constants;
import com.app.utils.FileUtil;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.api.CommonApi;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import android.view.WindowManager;
import android.graphics.BitmapFactory;

import androidx.activity.result.ActivityResult;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import droidninja.filepicker.FilePickerConst;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class NewProfileFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = NewProfileFragment.class.getSimpleName();

    // Views that actually exist in new_profile_fragment.xml
    private AppBarLayout appBarLayout;
    private ShapeableImageView profileImage;
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

    private TextView editLay,addBioButton;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;

    private ImageView btnFindFriends,plusAddBioIcon, plusIconName;
    private ImageView shareprofile;
    private ImageView more_icon,btnChangePhoto, topBarProfileIcon;

    // Non-UI
    private Context mContext;
    private ApiInterface apiInterface;
    private StorageUtils storageUtils;
    private ProfileResponse profileResponse;
    private boolean isVideoDeleted = false;
    private final int NUM_OF_TABS = 5;
    private HomePagerAdapter pagerAdapter;
    private String userId = null;
    private boolean fromEdit = false;
    private String typeWay = "";
    private Bitmap qrBitMap;
    private File currentPhotoFile;
    private Uri mCurrentPhotoUri;
    private ActivityResultLauncher<String[]> cameraPermissionResult;
    private ActivityResultLauncher<String[]> storagePermissionResult;
    private ActivityResultLauncher<Intent> galleryLauncherResult;
    private ActivityResultLauncher<Intent> cameraLauncherResult;   // <-- NEW
    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    public NewProfileFragment() {
        // Required empty public constructor
    }
    private static final int EDIT_FIELD_REQUEST_CODE = 2000;

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
                R.layout.new_profile_fragment, container, false);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        storageUtils = StorageUtils.getInstance(mContext);
        isVideoDeleted = false;

        Bundle args = getArguments();
        if (args != null) {
            String argUserId = args.getString("userId");
            if (argUserId != null) {
                // TODO: use argUserId if needed
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
        initPermissions();
        initActivityResult();
    }

    private void findViews(View view) {

        appBarLayout = view.findViewById(R.id.appBarLay);
        profileImage = view.findViewById(R.id.profileImage);
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

        editLay = view.findViewById(R.id.editLay);
        addBioButton = view.findViewById(R.id.addBioButton);
        plusAddBioIcon = view.findViewById(R.id.plusAddBioIcon);
        plusIconName = view.findViewById(R.id.plusIconName);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        progressBar = view.findViewById(R.id.progressBar);

        btnFindFriends = view.findViewById(R.id.btnFindFriends);
        shareprofile = view.findViewById(R.id.shareprofile);
        more_icon = view.findViewById(R.id.more_icon);
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto);
        topBarProfileIcon = view.findViewById(R.id.topBarProfileIcon);

        // Push AppBarLayout down a bit (status bar spacing)
       /* int topMarginInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());

        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) appBarLayout.getLayoutParams();
        params.topMargin = topMarginInPx;
        appBarLayout.setLayoutParams(params);*/

     /*   int topMarginInPx = getStatusBarHeight();
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) appBarLayout.getLayoutParams();
        params.topMargin = topMarginInPx;
        appBarLayout.setLayoutParams(params);*/
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void initView() {
        progressBar.setVisibility(GONE);

        editLay.setVisibility(View.VISIBLE);
        shareprofile.setVisibility(View.VISIBLE);

        initClickListener();
    }

    private void initClickListener() {
        btnFindFriends.setOnClickListener(this);
        profileImage.setOnClickListener(this);
        editLay.setOnClickListener(this);
        heartLay.setOnClickListener(this);
        fansLay.setOnClickListener(this);
        followingLay.setOnClickListener(this);

        plusIconName.setOnClickListener(v -> {
        App.preventMultipleClick(plusIconName);
        Intent intent = new Intent(getActivity(), EditFieldActivity.class);
        intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_NAME);
        intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getName());
        startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });

        plusAddBioIcon.setOnClickListener(v ->{
            App.preventMultipleClick(plusAddBioIcon);
            Intent intent = new Intent(getActivity(), EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_BIO);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getBio());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });
        addBioButton.setOnClickListener(v -> {
            App.preventMultipleClick(addBioButton);
            Intent intent = new Intent(getActivity(), EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_BIO);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getBio());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });

        btnChangePhoto.setOnClickListener(v->{
            App.preventMultipleClick(btnChangePhoto);
            openPickerDialog();
        });

        topBarProfileIcon.setOnClickListener(v ->{
            App.preventMultipleClick(topBarProfileIcon);
            Intent profileIntent = new Intent(getActivity(), NewEditProfileActivity.class);
            profileIntent.putExtra(Constants.TAG_PROFILE_DATA, profileResponse);
            startActivityForResult(profileIntent, Constants.PROFILE_REQUEST_CODE);
            requireActivity().overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
        });

        more_icon.setOnClickListener(v -> {
            Log.d(TAG, "Settings clicked");
            App.preventMultipleClick(more_icon);
            Intent primeIntent = new Intent(getActivity(), SettingsActivity.class);
            primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(primeIntent);
        });

        shareprofile.setOnClickListener(v -> {
            Log.d(TAG, "Share profile clicked");
            App.preventMultipleClick(shareprofile);
            shareMyProfile(profileResponse);
        });
    }

    private void initViewPager() {
        if (!isAdded()) return;

        // Create / reset adapter
        if (pagerAdapter != null) {
            pagerAdapter.resetViewPager();
        }
        pagerAdapter = new HomePagerAdapter(requireActivity());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(NUM_OF_TABS);
        viewPager.setCurrentItem(0, false);

        // Attach TabLayout and set icons (icon-only tabs)
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.calc);   // left icon
                    break;
                case 1:
                    tab.setIcon(R.drawable.img_1_private_unselected);       // second icon
                    break;
                case 2:
                    tab.setIcon(R.drawable.img_1_repost_unselected);    // middle icon
                    break;
                case 3:
                    tab.setIcon(R.drawable.img_1_bookmark_unselected);      // fourth icon
                    break;
                case 4:
                    tab.setIcon(R.drawable.heart_inactive);    // right icon
                    break;
            }
        }).attach();
    }


    private void shareMyProfile(ProfileResponse profile) {
        if (profile == null) return;

        String userId = GetSet.getUserId();
        if (userId != null) {
            String profileLink = profile.getShare_profile_link();
            String shareMsg = "Hi, check my profile ---> " + profileLink;
            Log.d(TAG, "shareMyProfile link: " + profileLink);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareMsg);
            startActivity(Intent.createChooser(intent, getString(R.string.share_link)));
        }
    }

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
                    return new MyVideosFragment(
                            GetSet.getUserId(),
                            getString(R.string.videos_txt),
                            Constants.TAG_OWN_PROFILE,
                            /*"profile"*/Constants.TAG_MYVIDEOS
                    );

                case 1:
                    return new MyVideosFragment(
                            GetSet.getUserId(),
                            getString(R.string.liked),
                            Constants.TAG_OWN_PROFILE,
                            Constants.TAG_PRIVATE_VIDEO
                    );

                case 2:
                    return new RepostedVideosFragment(GetSet.getUserId(), /*getString(R.string.liked_videos)*/getString(R.string.repost), Constants.TAG_OWN_PROFILE);
                case 3:
                    return new MyFavouriteVideosFragment(
                            GetSet.getUserId(),
                            getString(R.string.videos_txt),
                            Constants.TAG_OWN_PROFILE,
                            Constants.TAG_BOOKMARKS_VIDEO
                    );
                case 4:
                   return new LikedVideosFragment(
                            GetSet.getUserId(),
                            getString(R.string.liked),
                            Constants.TAG_OWN_PROFILE
                    );
            }
        }

        @Override
        public int getItemCount() {
            return NUM_OF_TABS; // 5
        }

        public void resetViewPager() {
            // no-op for now
        }
    }


    private void initProfile() {
        if (GetSet.getUserId() != null) {
            profileResponse = new ProfileResponse();
            profileResponse.setUserId(GetSet.getUserId());
            profileResponse.setName(GetSet.getName());
            profileResponse.setUserName(GetSet.getUserName());
            profileResponse.setUserImage(GetSet.getUserImage());
            profileResponse.setDob(GetSet.getDob());
            profileResponse.setGender(GetSet.getGender());
            profileResponse.setFollowings(Integer.valueOf(GetSet.getFollowingCount()));
            profileResponse.setFollowers(Integer.valueOf(GetSet.getFollowersCount()));
            profileResponse.setAvailableGems(GetSet.getGems());
            profileResponse.setAvailableGifts(Integer.valueOf("" + GetSet.getGifts()));
            profileResponse.setVideosCount(Integer.valueOf("" + GetSet.getVideos()));
            profileResponse.setPremiumMember(GetSet.getPremiumMember());
            profileResponse.setPremiumExpiryDate(GetSet.getPremiumExpiry());
            profileResponse.setBio(GetSet.getBio());
            profileResponse.setLikes(GetSet.getLikes());
            profileResponse.setAge(Integer.valueOf(GetSet.getAge()));
            profileResponse.setShare_profile_link(GetSet.getShare_profile_link());
            setProfile(profileResponse);
        }
    }

    private void getProfile() {
        if (GetSet.getUserId() != null && apiInterface != null) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());

            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call,
                                       @NotNull Response<ProfileResponse> response) {
                    if (response.isSuccessful() &&
                            response.body() != null &&
                            response.body().getStatus().equals(Constants.TAG_TRUE)) {

                        ProfileResponse profile = response.body();
                        profileResponse = profile;

                        Log.d("getProfile", new Gson().toJson(profile));

                        // Update GetSet / SharedPref (same as before)
                        GetSet.setUserId(profile.getUserId());
                        GetSet.setName(profile.getName());
                        GetSet.setUserName(profile.getUserName());
                        GetSet.setDob(profile.getDob());
                        GetSet.setGetPhonenumber(profile.getPhone_number());
                        GetSet.setAge(String.valueOf(profile.getAge()));
                        GetSet.setGender(profile.getGender());
                        GetSet.setUserImage(profile.getUserImage());
                        GetSet.setFollowingCount(String.valueOf(profile.getFollowings()));
                        GetSet.setFollowersCount(String.valueOf(profile.getFollowers()));
                        GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(String.valueOf(profile.getAvailableGifts())) : 0L);
                        GetSet.setGems(profile.getAvailableGems() != null ? (profile.getAvailableGems()) : 0F);
                        GetSet.setVideos(profile.getVideosCount() != null ? Long.parseLong(String.valueOf(profile.getVideosCount())) : 0L);
                        GetSet.setPremiumMember(profile.getPremiumMember());
                        GetSet.setPremiumExpiry(profile.getPremiumExpiryDate());
                        GetSet.setPrivacyContactMe(String.valueOf(profile.getUserCanMessage()));
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
                        /*GetSet.setInstagram_link(profile.getInstagramLink());
                        GetSet.setYoutube_link(profile.getYoutubeLink());*/
                        if (response.body().getInstagramLink() != null) {
                            GetSet.setInstagram_link(profile.getInstagramLink());
                        } else {
                            GetSet.setInstagram_link("");
                        }

                        if (response.body().getYoutubeLink() != null) {
                            GetSet.setYoutube_link(profile.getYoutubeLink());
                        } else {
                            GetSet.setYoutube_link("");
                        }


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
                        SharedPref.putString(SharedPref.PHONE_NUMBER, profile.getPhone_number());
                        SharedPref.putString(SharedPref.POST_COMMAND, GetSet.getPostCommand());
                        SharedPref.putString(SharedPref.SEND_MESSAGE, GetSet.getSendMessage());

                        if (profile.getVideosCount() != null &&
                                Long.parseLong(String.valueOf(profile.getVideosCount())) > 0) {
                            isVideoDeleted = false;
                        }
                        setProfile(profile);

                    } else if (response.body() != null &&
                            response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {

                        App.makeToast(getString(R.string.Account_block));
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(mContext).clearDB();
                        Intent logout = new Intent(mContext, LoginActivity.class);
                        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(logout);
                    } else {
                        initProfile();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ProfileResponse> call, @NotNull Throwable t) {
                    Log.e(TAG, "getProfile: " + t.getMessage());
                    call.cancel();
                    initProfile();
                }
            });
        }
    }

    private void setProfile(ProfileResponse profile) {
        if (profile != null && getActivity() != null && mContext != null) {
            Timber.tag(TAG).d("setProfile: %s", profile.getName());

            txtName.setText(profile.getName());

            Glide.with(mContext)
                    .load(profile.getUserImage())
                    .error(R.drawable.default_profile_image)
                    .placeholder(R.drawable.default_profile_image)
                    .into(profileImage);

            if (TextUtils.isEmpty(profile.getBio())) {
                userBio.setVisibility(View.GONE);
              /*  if (plusAddBioIcon != null) plusAddBioIcon.setVisibility(View.VISIBLE);
                if (addBioButton != null) addBioButton.setVisibility(View.VISIBLE);*/
            } else {
                userBio.setText(profile.getBio());
                userBio.setVisibility(View.VISIBLE);
               /* if (plusAddBioIcon != null) plusAddBioIcon.setVisibility(View.GONE);
                if (addBioButton != null) addBioButton.setVisibility(View.GONE);*/
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

            txtFollowersCount.setText(String.valueOf(
                    profile.getFollowers() != null ? profile.getFollowers() : 0));
            txtFollowingsCount.setText(String.valueOf(
                    profile.getFollowings() != null ? profile.getFollowings() : 0));

            genderImage.setImageResource(profile.getGender().equals(Constants.TAG_MALE)
                    ? R.drawable.men
                    : R.drawable.women);

            txtHeartCount.setText(String.valueOf(
                    profile.getLikes() != null ? profile.getLikes() : 0));
        }
    }

    @Override
    public void onClick(View view) {
        if (App.isPreventMultipleClick())
            return;

        switch (view.getId()) {
            case R.id.btnFindFriends: {
                Intent intent1 = new Intent(getActivity(), SearchUserActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent1);
                break;
            }
            case R.id.profileImage: {
                App.preventMultipleClick(profileImage);
                Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
                animateZoom(getActivity());
                break;
            }
            case R.id.editLay: {
                App.preventMultipleClick(editLay);
                Intent profileIntent = new Intent(getActivity(), NewEditProfileActivity.class);
                profileIntent.putExtra(Constants.TAG_PROFILE_DATA, profileResponse);
                startActivityForResult(profileIntent, Constants.PROFILE_REQUEST_CODE);
                requireActivity().overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
                break;
            }
            case R.id.followingLay: {
                App.preventMultipleClick(followingLay);
                Intent followersIntent = new Intent(getActivity(), FollowersActivity.class);
                followersIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWINGS);
                followersIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                startActivity(followersIntent);
                break;
            }
            case R.id.fansLay: {
                App.preventMultipleClick(fansLay);
                Intent followingsIntent = new Intent(getActivity(), FollowersActivity.class);
                followingsIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
                followingsIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                startActivity(followingsIntent);
                break;
            }
            case R.id.heartLay: {
                // if you want something on likes click, add here
                break;
            }
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
        Timber.d("onProfileUpdated: updated");
        setProfile(profileEvent.getProfileResponse());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.INTENT_REQUEST_CODE) {
            if (mContext != null) {
                Glide.with(mContext)
                        .load(GetSet.getUserImage())
                        .error(R.drawable.default_profile_image)
                        .placeholder(R.drawable.default_profile_image)
                        .into(profileImage);
            }
        } else if (resultCode == RESULT_OK && requestCode == Constants.PROFILE_REQUEST_CODE) {
            if (mContext != null && data != null) {
                profileResponse = (ProfileResponse) data.getSerializableExtra(Constants.TAG_PROFILE_DATA);
                setProfile(profileResponse);
            }
        }else if (resultCode == Activity.RESULT_OK && requestCode == EDIT_FIELD_REQUEST_CODE && data != null) {
            String fieldType = data.getStringExtra(EditFieldActivity.FIELD_TYPE);
            String updatedValue = data.getStringExtra(EditFieldActivity.FIELD_VALUE);

            if (fieldType != null && updatedValue != null) {
                switch (fieldType) {
                    case Constants.TAG_NAME:
                        txtName.setText(updatedValue);
                        break;
                   case Constants.TAG_BIO:
                       userBio.setText(updatedValue);
                        break;
                }
            }
        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    public void updateProfile() {
        getProfile();
    }

    private void openPickerDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
            } else {
                pickImage();
            }
        } else {
            // For Android 12 and below just go to picker (you already handle other perms)
            pickImage();
        }
    }

    private void requestCameraPermission() {
        cameraPermissionResult.launch(new String[]{CAMERA, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO});
    }

    private void pickImage() {

        View contentView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_profile_photo, null);

        BottomSheetDialog pickerOptionsSheet =
                new BottomSheetDialog(requireContext(), R.style.SimpleBottomDialog);

        pickerOptionsSheet.setCanceledOnTouchOutside(true);
        pickerOptionsSheet.setContentView(contentView);
        pickerOptionsSheet.setDismissWithAnimation(true);

        LinearLayout layoutCamera = contentView.findViewById(R.id.container_camera_option);
        LinearLayout layoutGallery = contentView.findViewById(R.id.container_gallery_option);
        LinearLayout layoutView = contentView.findViewById(R.id.container_photo_view);

        // View current photo
        layoutView.setOnClickListener(v -> {
            App.preventMultipleClick(profileImage);
            Intent intent = new Intent(requireActivity(), ImageViewActivity.class);
            intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            intent.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
            startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
            animateZoom(requireActivity());
            pickerOptionsSheet.dismiss();
        });

        // Camera
        layoutCamera.setOnClickListener(v -> {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (captureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                File photoFile = null;
                try {
                    currentPhotoFile = FileUtil.createImageFile(requireContext());
                    photoFile = currentPhotoFile;
                } catch (IOException ex) {
                    Timber.e(ex);
                }

                if (photoFile != null) {
                    mCurrentPhotoUri = FileProvider.getUriForFile(
                            requireContext(),
                            getString(R.string.file_provider_authority),
                            photoFile);

                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri);

                    // ✅ use Activity Result API instead of old startActivityForResult
                    cameraLauncherResult.launch(captureIntent);
                }
            }
            pickerOptionsSheet.dismiss();
        });

        // Gallery via FilePicker
        layoutGallery.setOnClickListener(v -> {
            FilePickerBuilder.getInstance()
                    .setMaxCount(1)
                    .setActivityTheme(R.style.MainTheme)
                    .setActivityTitle(getString(R.string.please_select_media))
                    .enableVideoPicker(false)
                    .enableImagePicker(true)
                    .enableCameraSupport(false)
                    .showGifs(false)
                    .showFolderView(false)
                    .enableSelectAll(false)
                    .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .pickPhoto(this, galleryLauncherResult);  // <-- this was NULL before

            pickerOptionsSheet.dismiss();
        });

        pickerOptionsSheet.show();
    }


    private void initPermissions() {

        cameraPermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                Timber.d("cameraPermissionResult: %s", result);
                boolean granted = true;
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }
                Timber.d("granted: %s", granted);

                if (granted) {

                } else {
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), x.getKey())) {
                                cameraPermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(getContext(), new OnOkCancelClickListener() {
                                    @Override
                                    public void onOkClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, getString(R.string.camera_permission_error));
                            }
                            break;
                        }
                    }
                }

            }
        });

        storagePermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {
//                    pickGalleryImage();
                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), x.getKey())) {
                                storagePermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(getContext(), new OnOkCancelClickListener() {
                                    @Override
                                    public void onOkClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, getString(R.string.storage_error));
                            }
                            break;
                        }
                    }
                }

            }
        });
    }

    private void initActivityResult() {

        // Handles result from FilePickerBuilder.pickPhoto(...)
        galleryLauncherResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            ArrayList<Uri> selectedPhotos =
                                    result.getData().getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                            if (selectedPhotos != null && !selectedPhotos.isEmpty()) {
                                Uri selectedImageUri = selectedPhotos.get(0);
                                compressImage(selectedImageUri);
                            }
                        }
                    }
                });

        // Handles result from cameraLauncherResult.launch(captureIntent)
        cameraLauncherResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && mCurrentPhotoUri != null) {
                            compressImage(mCurrentPhotoUri);
                        }
                    }
                });
    }

    private void compressImage(Uri imageUri) {
        try {
            // Decode the image from the given URI
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            if (originalBitmap != null) {
                // Resize to at most 800x800
                Bitmap resizedBitmap = resizeBitmap(originalBitmap, 800, 800);

                // Compress to JPEG (80% quality)
                String fileName = "TEMP_" + System.currentTimeMillis() + ".jpg";
                File compressedFile = new File(requireContext().getCacheDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(compressedFile);

                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

                outputStream.flush();
                outputStream.close();

                byte[] imageBytes = readFileToByteArray11(compressedFile);

                // Upload to server
                uploadImage(imageBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap resizeBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float aspectRatio = (float) width / height;

        if (width > maxWidth || height > maxHeight) {
            if (aspectRatio > 1) { // Landscape
                width = maxWidth;
                height = (int) (maxWidth / aspectRatio);
            } else { // Portrait
                height = maxHeight;
                width = (int) (maxHeight * aspectRatio);
            }
        }

        return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
    }

    // Helper method to read a file into a byte array
    private byte[] readFileToByteArray11(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        fileInputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadImage(byte[] imageBytes) {
        showLoading();

        RequestBody requestFile =
                RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData(Constants.TAG_PROFILE_IMAGE, "image.jpg", requestFile);
        RequestBody userid =
                RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());

        Call<Map<String, String>> call3 = apiInterface.uploadProfileImage(body, userid);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call,
                                   @NotNull Response<Map<String, String>> response) {
                hideLoading();
                Map<String, String> data = response.body();
                if (data != null &&
                        data.get(Constants.TAG_STATUS) != null &&
                        data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                    // Save new URL in GetSet + SharedPref   (same as ImageViewActivity)
                    GetSet.setUserImage(Constants.USER_IMAGE_URL + data.get(Constants.TAG_USER_IMAGE));
                    SharedPref.putString(Constants.TAG_USER_IMAGE, GetSet.getUserImage());

                    Timber.d("getUserImage: %s", GetSet.getUserImage());

                    // Update the UI profile image
                    if (mContext != null) {
                        Glide.with(mContext)
                                .load(GetSet.getUserImage())
                                .error(R.drawable.default_profile_image)
                                .placeholder(R.drawable.default_profile_image)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                call.cancel();
                hideLoading();
                Timber.e(t, "uploadProfileImage failed");
            }
        });
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        Activity activity = getActivity();
        if (activity != null) {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        Activity activity = getActivity();
        if (activity != null) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

}
