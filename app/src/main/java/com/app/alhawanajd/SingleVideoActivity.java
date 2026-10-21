package com.app.alhawanajd;

import static android.view.View.VISIBLE;
import static com.app.alhawanajd.home.HomeVideoAdapter.getTagged;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.TracksInfo;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.common.base.CharMatcher;
import com.google.gson.Gson;

import com.hendraanggrian.appcompat.socialview.autocomplete.Mention;
import com.hendraanggrian.appcompat.socialview.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;
import com.app.alhawanajd.BuildConfig;
import com.app.alhawanajd.R;

import com.app.data.model.Mention2;
import com.app.data.model.Tagged;
import com.app.db.DBHelper;
import com.app.external.CustomEditText;
import com.app.alhawanajd.home.CommentAdapter;
import com.app.alhawanajd.home.FollowingProfileUpdate;
import com.app.alhawanajd.home.HomeBottomDialog;
import com.app.alhawanajd.home.HomeVideoAdapter;
import com.app.alhawanajd.home.ReportAdapter;
import com.app.alhawanajd.home.eventbus.FollowingFollowFollowing;
import com.app.alhawanajd.home.eventbus.FollowingUpdateGiftMessageCount;
import com.app.alhawanajd.home.eventbus.FollowingVideoFav;
import com.app.alhawanajd.home.eventbus.FollowingVideoLike;
import com.app.alhawanajd.home.eventbus.ForYouFollowFollowing;
import com.app.alhawanajd.home.eventbus.ForYouProfileUpdate;
import com.app.alhawanajd.home.eventbus.ForYouUpdateGiftMessageCount;
import com.app.alhawanajd.home.eventbus.ForYouVideoFav;
import com.app.alhawanajd.home.eventbus.ForYouVideoLike;
import com.app.alhawanajd.home.eventbus.OtherProfileUpdate;
import com.app.alhawanajd.profile.MyProfileActivity;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.helper.LinkEllipseTextView;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.OnVerticalScrollListener;
import com.app.helper.PermissionsUtils;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.helper.callback.NetworkResultCallback;
import com.app.helper.callback.OnOkCancelClickListener;
import com.app.model.FollowRequest;
import com.app.model.GetSet;
import com.app.model.Gift;
import com.app.model.HomeResponse;
import com.app.model.MusicListResponse;
import com.app.model.ProfileResponse;
import com.app.model.SingleVideoResponse;
import com.app.model.UserList;
import com.app.model.VideoCommentResponse;
import com.app.utils.AdminData;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DevicePref;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiCallback;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.api.CommonApi;
import com.app.utils.api.ReportApiCallback;
import com.app.utils.exoutils.ExoPlayerUtils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import es.dmoral.toasty.Toasty;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SingleVideoActivity extends BaseFragmentActivity implements ReportApiCallback, HomeVideoAdapter.VideoAdapterListener {
    private static final String TAG = SingleVideoActivity.class.getSimpleName();
    public ConstraintLayout parentLay;
    public ProgressBar progressBar, progress_duration;
    public ImageView btnBack, parentBtnBack, mediaCoverImage, profileImage, video_play_pause, profileFollowIcon;
    public TextView marqueeText, txt_title, txt_heart_count, txt_gift_count, txt_comment_count, txtAudioTitle;
    public LinkEllipseTextView txt_description;
    public LinearLayout newadlayout,heartLay, giftLay, commentLay, moreLay, bottomLay, audioLay;
    public FrameLayout mediaContainer;
    public LinearLayout leftSideView;
    public LottieAnimationView heartAni;
    public LottieAnimationView lottieAudio;
    public ShapeableImageView heart, giftImageView;
    public LottieAnimationView icFavHeart;
    public RoundedImageView liveStreaming;
    private ConstraintLayout mainLay;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ProfileResponse profile;
    private HomeResponse.Result videoItem = null;
    private StyledPlayerView videoSurfaceView;
    private ExoPlayer videoPlayer;

    private ActivityResultLauncher<String[]> storagePermissionResult;
    private ActivityResultLauncher<Intent> videoEditResultLauncher;
    private ActivityResultLauncher<Intent> musicEditResultLauncher;
    private ActivityResultLauncher<Intent> profileEditResultLauncher;
    int displayHeight, displayWidth;
    private String getVideoId, getVideoThumnail, getStatus = "";
    private ApiInterface apiInterface;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private ScheduledExecutorService mExecutorService; // ProgressBar
    private PlayState videoState; // video pause and play
    private Boolean likePrevent = false;


    /*private View bottom_sheet_longpress;*/
    private BottomSheetDialog reportDialog;
    private View bottom_sheet_longpress;
    private BottomSheetDialog bottomSheetLongPressDiloag;
    private BottomSheetDialog videoCommandBottomDialog;
    private BottomSheetDialog commentEditDialog;
    private BottomSheetDialog giftDialog;
    private BottomSheetDialog shareDialog;

    private RecyclerView reportsView;
    private ImageView reportBtnBack;
    private TextView txtTitle;
    ReportAdapter reportAdapter;

    ViewPager viewPager;
    private LinearLayout sendLay;
    CircleIndicator pagerIndicator;
    private TextView txtAttachmentName, txtSend;
    private RelativeLayout giftNullLay;
    private ImageView giftNullImage;
    private TextView giftNullText;
    List<Gift> tempGiftList = new ArrayList<>();
    GiftAdapter giftAdapter;
    private int giftPosition = 0;
    LinearLayoutManager stickerLayoutManager;
    private RecyclerView recyclerView;
    private final int ITEM_LIMIT = 8;
    private String from = "", comment_open = "";
    private boolean isVideoViewAdded = false;

    private boolean searchUser = true;
    TextView commentCount, addComment, sendComment, noCommands;
    SocialAutoCompleteTextView mCommentsEditText;
    RecyclerView commentRec, userRecyclerView;
    CommentAdapter commendAdapter;
    LinearLayoutManager linearLayoutManager;
    ArrayList<VideoCommentResponse.Result> commendList = new ArrayList<>();
    String loadMoreCheckVideoCommand = "initial";
    private int lastOffsetVideo = -1;
    RelativeLayout progress, CommandLay, sendCommentLay;
    private LinearLayout rootView;
    LinearLayout headerBottom;
    private SwipeRefreshLayout mSwipeVideoComment = null;
    private int currentPage = 0;
    OnVerticalScrollListener videoScrollListener;
    ArrayList<UserList.Result> liveUser = new ArrayList<>();
    String tagcomment = "", tagged_user_name = "", tagged_user_id = "";
    public ArrayList<String> list_tagged_user_names = new ArrayList<>();
    public ArrayList<String> list_tagged_user_ids = new ArrayList<>();

    private HashSet<String> mentions = new HashSet();
    private ArrayList<Mention2> mentionsCache = new ArrayList<>();
    private DBHelper dbHelper;
    View musicLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_video);
        dbHelper = DBHelper.getInstance(this);
        mExecutorService = Executors.newSingleThreadScheduledExecutor();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        getVideoId = getIntent().getStringExtra(Constants.TAG_VIDEO_ID);
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        comment_open = getIntent().getStringExtra(Constants.TAG_COMMENT_OPEN);
        Log.d(TAG, "onCreate11: "+getVideoId+"  "+from+"  "+comment_open);
        findViews();
        initPermission();
        initResultLauncher();
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorBlack));
        parentBtnBack.setVisibility(View.GONE);
        parentBtnBack.bringToFront();

        btnBack.setVisibility(VISIBLE);
        leftSideView.setVisibility(View.GONE);
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        /*if (getIntent().getStringExtra(Constants.TAG_VIDEO_THUMBANIL) != null) {
            Glide.with(getApplicationContext())
                    .load(getIntent().getStringExtra(Constants.TAG_VIDEO_THUMBANIL))
                    .into(mediaCoverImage);
        }*/

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .build();
        Slidr.attach(this, config);
        mainLay.setEnabled(false);

        if (!TextUtils.isEmpty(getVideoId)) {
            getVideoResult("");
        } else {
            setNullLay();
        }

        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);

    }

    private void findViews() {
        parentLay = findViewById(R.id.parentLay);
        parentBtnBack = findViewById(R.id.btnBack);
        btnBack = findViewById(R.id.btnBack);
        txtAudioTitle = findViewById(R.id.txtAudioTitle);
        icFavHeart = findViewById(R.id.ic_fav_heart);
        mediaContainer = findViewById(R.id.mediaContainer);
        mediaCoverImage = findViewById(R.id.mediaCoverImage);
        txt_description = findViewById(R.id.txt_description);
        profileImage = findViewById(R.id.profileImage);
        txt_title = findViewById(R.id.txt_title);
        marqueeText = findViewById(R.id.marqueeText);
        newadlayout = findViewById(R.id.newadlayout);
        txt_heart_count = findViewById(R.id.txt_heart_count);
        txt_gift_count = findViewById(R.id.txt_gift_count);
        txt_comment_count = findViewById(R.id.txt_comment_count);
        giftLay = findViewById(R.id.giftLay);
        heartLay = findViewById(R.id.heartLay);
        commentLay = findViewById(R.id.commentLay);
        moreLay = findViewById(R.id.moreLay);
        video_play_pause = findViewById(R.id.video_play_pause);
        progressBar = findViewById(R.id.progressBar);
        mainLay = findViewById(R.id.mainLay);
        progress_duration = findViewById(R.id.progress_duration);
        profileFollowIcon = findViewById(R.id.profileFollowIcon);
        leftSideView = findViewById(R.id.leftSideView);
        heartAni = findViewById(R.id.heartAni);
        heart = findViewById(R.id.imgheart);
        giftImageView = findViewById(R.id.giftImageView);
        lottieAudio = findViewById(R.id.lottieAudio);
        audioLay = findViewById(R.id.audioLay);
        bottomLay = findViewById(R.id.bottomLay);
        nullLay = findViewById(R.id.nullLay);
        nullImage = findViewById(R.id.nullImage);
        nullText = findViewById(R.id.nullText);
        liveStreaming = findViewById(R.id.liveStreaming);
        musicLay = findViewById(R.id.musicLay);
        musicLay.setVisibility(View.GONE);
        marqueeText.setSelected(true);

        liveStreaming.setVisibility(View.GONE);

        liveStreaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Intent liveStreaming = new Intent(SingleVideoActivity.this, StreamListActivity.class);
                liveStreaming.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                liveStreaming.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                liveStreaming.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                liveStreaming.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                liveStreaming.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                startActivity(liveStreaming);*/
            }
        });
    }

    private void initPermission() {
        storagePermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {

                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(SingleVideoActivity.this, x.getKey())) {
                                storagePermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(SingleVideoActivity.this, new OnOkCancelClickListener() {
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

    private void initResultLauncher() {
        videoEditResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (videoItem != null) {
                        videoItem.setMode(intent.getStringExtra(Constants.TAG_PUBLISH_MODE));
                        videoItem.setVideoDescription(intent.getStringExtra(Constants.TAG_DESCRIPTION));
                        txt_description.setText(videoItem.getVideoDescription());
                    }
                }
            }
        });

        musicEditResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (videoItem != null) {
                        videoItem.getSoundtracks().setIsFavorite(intent.getBooleanExtra(Constants.TAG_FAV, false));
                    }
                }
            }
        });

        profileEditResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    giftLay.setClickable(false);
                    profileImage.setClickable(false);
                    profileFollowIcon.setClickable(false);
                    heartLay.setClickable(false);
                    txt_title.setClickable(false);
                    //shareLay.setClickable(false);
                    commentLay.setClickable(false);
                    mediaContainer.setClickable(false);
                    lottieAudio.setClickable(false);
                    mainLay.setEnabled(false);

                    if (!TextUtils.isEmpty(getVideoId)) {
                        getVideoResult("update");
                    } else {
                        setNullLay();
                    }
                }
            }
        });
    }

    private void shareVideoDialog(String link) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, link);
        startActivity(Intent.createChooser(intent, getString(R.string.share_link)));
    }

    private boolean appInstalledOrNot(String uri) {

        PackageManager pm = getApplication().getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }


    public void playerpause(Boolean state) {
        if (videoPlayer != null) videoPlayer.setPlayWhenReady(state);
        if (video_play_pause != null && video_play_pause.getVisibility() == VISIBLE)
            video_play_pause.setVisibility(View.GONE);
    }


    private void addVideoView() {
        mediaContainer.addView(videoSurfaceView);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
        videoSurfaceView.setVisibility(VISIBLE);
        videoSurfaceView.setAlpha(1);
    }

    public void profile() {
        Log.d(TAG, "onResponse: "+Constants.Login_method);
        if ((from != null && from.equals(Constants.TAG_FOR_YOU_PROFILE_UPDATE)) ||
                ((from != null && from.equals(Constants.TAG_OTHER_PROFILE_UPDATE))) ||
                ((from != null && from.equals(Constants.TAG_FOLLOWING_PROFILE_UPDATE)))) {
            setResult(Constants.PROFILE_VIDEO_RESET);
            finish();
            overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
        } else {
            playerpause(false);
            Intent profile;
            if (videoItem.getPublisherId().equals(GetSet.getUserId())) {
                profile = new Intent(SingleVideoActivity.this, MyProfileActivity.class);
            } else {
                profile = new Intent(SingleVideoActivity.this, OthersProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, videoItem.getPublisherId());
                profile.putExtra(Constants.TAG_USER_IMAGE, videoItem.getPublisherImage());
            }
            profile.putExtra(Constants.TAG_FROM, Constants.TAG_SINGLE_VIDEO);
            profileEditResultLauncher.launch(profile);
        }
    }

    private void followAPI(String publisherId, String publishImage) {

        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            followRequest.setUserId(publisherId);
            followRequest.setFollowerId(GetSet.getUserId());
            followRequest.setType(Constants.TAG_FOLLOW_USER);


            Timber.d("followUnfollowUser: %s", App.getGsonPrettyInstance().toJson(followRequest));

            Call<Map<String, String>> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    Map<String, String> followResponse = response.body();

                    if (followResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                        String userType = "";
                        if (GetSet.getUserId() != null) {
                            userType = (publisherId).equals(GetSet.getUserId()) ? "" : publisherId;
                            Timber.i("userType %s", userType);
                        }

                        if (from != null) {
                            switch (from) {
                                case Constants.TAG_FOR_YOU_PROFILE_UPDATE:
                                    EventBus.getDefault().post(new ForYouProfileUpdate(publisherId, publishImage, userType)); // Foryouprofile
                                    break;
                                case Constants.TAG_OTHER_PROFILE_UPDATE:
                                    EventBus.getDefault().post(new OtherProfileUpdate(publisherId)); // OtherProfileUpdate
                                    break;
                                case Constants.TAG_FOLLOWING_PROFILE_UPDATE:
                                    EventBus.getDefault().post(new FollowingProfileUpdate(publisherId, publishImage, userType)); // FollowingProfileUpdate
                                    break;
                            }
                        }


                        EventBus.getDefault().post(new ForYouFollowFollowing(false, publisherId)); // ForYouVideoFragment
                        EventBus.getDefault().post(new FollowingFollowFollowing(false, publisherId));

                        profileFollowIcon.clearAnimation();
                        profileFollowIcon.setVisibility(View.GONE);

                    }
                }

                @Override
                public void onFailure
                        (@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void heartFun() {

        Map<String, String> map = new HashMap<>();

        map.put("user_id", GetSet.getUserId());
        map.put("video_id", videoItem.getVideoId());
        map.put("token", GetSet.getAuthToken());

        Timber.d("heartFun: %s", App.getGsonPrettyInstance().toJson(map));

        Call<Map<String, String>> call = apiInterface.getHeartStatus(map);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                if (response.body() != null && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                    Timber.d("onResponse: %s", response.body());

                    getStatus = response.body().get(Constants.TAG_MESSAGE);
                    txt_heart_count.setText(response.body().get("likecount"));

                    if (response.body().get(Constants.TAG_MESSAGE).equals("Liked Successfully")) {
                        heartLay.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this, R.anim.anim_zoom_out));
                        EventBus.getDefault().post(new FollowingVideoLike(videoItem.getVideoId(), "liked", txt_heart_count.getText().toString()));
                        EventBus.getDefault().post(new ForYouVideoLike(videoItem.getVideoId(), "liked", txt_heart_count.getText().toString()));
                        heart.setImageResource(R.drawable.heart_filled);
                        videoItem.setIsLiked(true);
                    } else {
                        EventBus.getDefault().post(new FollowingVideoLike(videoItem.getVideoId(), "unliked", txt_heart_count.getText().toString()));
                        EventBus.getDefault().post(new ForYouVideoLike(videoItem.getVideoId(), "unliked", txt_heart_count.getText().toString()));
                        heart.setImageResource(R.drawable.heart_white);
                        videoItem.setIsLiked(false);
                    }

                    String userType = "", publisherId = videoItem.getPublisherId(),
                            publishImage = videoItem.getPublisherImage();
                    if (GetSet.getUserId() != null) {
                        userType = (publisherId).equals(GetSet.getUserId()) ? "" : publisherId;
                        Timber.i("userType %s", userType);
                    }

                    if (from != null) {
                        switch (from) {
                            case Constants.TAG_FOR_YOU_PROFILE_UPDATE:
                                EventBus.getDefault().post(new ForYouProfileUpdate(publisherId, publishImage, userType)); // Foryouprofile
                                break;
                            case Constants.TAG_OTHER_PROFILE_UPDATE:
                                EventBus.getDefault().post(new OtherProfileUpdate(publisherId)); // OtherProfileUpdate
                                break;
                            case Constants.TAG_FOLLOWING_PROFILE_UPDATE:
                                EventBus.getDefault().post(new FollowingProfileUpdate(publisherId, publishImage, userType)); // FollowingProfileUpdate
                                break;
                        }
                    }


                    likePrevent = false;
                }else if (response.body().get(Constants.TAG_STATUS).equals(com.app.utils.Constants.TAG_ACCOUNT_BLOCKED)) {
                    App.makeToast(getString(R.string.Account_block));
                    GetSet.reset();
                    SharedPref.clearAll();
                    DBHelper.getInstance(SingleVideoActivity.this).clearDB();
                    Intent logout = new Intent(SingleVideoActivity.this, LoginActivity.class);
                    logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(logout);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {


            }
        });


    }

    @Override
    public void onBackPressed() {
        cusBack();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if (videoCommandBottomDialog != null && videoCommandBottomDialog.isShowing())

        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.mainLay), false);
        } else {
            if (videoCommandBottomDialog != null && videoCommandBottomDialog.isShowing())
                playerpause(false);
            else playerpause(true);
        }
    }

    private void getVideoResult(String update) {
        if (NetworkReceiver.isConnected()) {
            mainLay.setVisibility(View.GONE);
            showLoading();
            HashMap<String, String> request = new HashMap<>();
            request.put("user_id", GetSet.getUserId());
            request.put("token", GetSet.getAuthToken());
            request.put("video_id", getVideoId);
            Timber.d("getParams: %s", App.getGsonPrettyInstance().toJson(request));

            Call<SingleVideoResponse> call = apiInterface.getSingleVideo(request);

            call.enqueue(new Callback<SingleVideoResponse>() {
                @Override
                public void onResponse(@NotNull Call<SingleVideoResponse> call, @NotNull Response<SingleVideoResponse> response) {
                    if (response.isSuccessful() && response.body().getResult() != null && response.body().getResult().size() > 0) {
                        hideLoading();
                        Timber.d("getVideoResult: %s", App.getGsonPrettyInstance().toJson(response.body().getResult()));
                        videoItem = response.body().getResult().get(0);
                        giftLay.setClickable(true);
                        profileImage.setClickable(true);
                        profileFollowIcon.setClickable(true);
                        heartLay.setClickable(true);
                        lottieAudio.setClickable(true);
                        txt_title.setClickable(true);
                        //   shareLay.setClickable(true);
                        commentLay.setClickable(true);
                        mediaContainer.setClickable(true);
                        leftSideView.setVisibility(VISIBLE);

                        if (videoItem.getIsLiked()) {
                            getStatus = "Liked Successfully";
                            heart.setImageResource(R.drawable.heart_filled);
                        } else {
                            getStatus = "UnLiked Successfully";
                            heart.setImageResource(R.drawable.heart_white);
                        }

                        mainLay.setVisibility(VISIBLE);

                        mainLay.animate()
                                .alpha(1.0f)
                                .setDuration(500).setStartDelay(0)
                                .setListener(null);

                        /*ConstraintLayout.LayoutParams progressParams = (ConstraintLayout.LayoutParams) progress_duration.getLayoutParams();
                        progressParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
                        progress_duration.setLayoutParams(progressParams);*/

                        if (response.body().getResult().get(0).getSoundtracks().getSoundId() == null)
                            lottieAudio.setVisibility(View.GONE);
                        else if (response.body().getResult().get(0).getSoundtracks().getSoundId().equals(""))
                            lottieAudio.setVisibility(View.GONE);
                        else
                            lottieAudio.setVisibility(View.VISIBLE);

                        bindView(response.body().getResult().get(0));

                        if (TextUtils.isEmpty(update)) {
                            initialize(response.body().getResult().get(0).getPlaybackUrl(), comment_open == null || !comment_open.equals(Constants.TAG_COMMENT_OPEN));
                            if (comment_open != null && comment_open.equals(Constants.TAG_COMMENT_OPEN)) {
                                videoDialog(videoItem.getPublisherId(), videoItem.getVideoId());
                            }
                        }

                        audioLay.setVisibility(View.GONE);
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bottomLay.getLayoutParams();
                        if (!TextUtils.isEmpty(response.body().getResult().get(0).getSoundtracks().getSoundId())) {
                            params.bottomMargin = 0;
                            audioLay.setVisibility(View.VISIBLE);
                            txtAudioTitle.setSelected(true);
                            String soundName;
                            if (!TextUtils.isEmpty(response.body().getResult().get(0).getSoundtracks().getSoundBy())) {
                                soundName = getResources().getString(R.string.original_audio_by) + " " +
                                        response.body().getResult().get(0).getSoundtracks().getSoundBy();
                            } else {
                                soundName = response.body().getResult().get(0).getSoundtracks().getTitle();
                            }
                            if (soundName.length() <= 30) {
                                txtAudioTitle.setMaxWidth((int) (DevicePref.getInstance(SingleVideoActivity.this).getInt(DevicePref.SCREEN_WIDTH, 0) * 0.4));
                            } else {
                                txtAudioTitle.setMaxWidth((int) (DevicePref.getInstance(SingleVideoActivity.this).getInt(DevicePref.SCREEN_WIDTH, 0)));
                            }
                            txtAudioTitle.setText(soundName);
                        } else {
                            params.bottomMargin = (int) getResources().getDimension(R.dimen.activity_vertical_margin);
                        }
                        bottomLay.setLayoutParams(params);

                        giftLay.setOnClickListener(v -> {
                            App.preventMultipleClick(giftLay);
                            if (GetSet.getUserId().equals(videoItem.getPublisherId())) {
                                /*Intent giftIntent = new Intent(SingleVideoActivity.this, ConvertGiftActivity.class);
                                giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(giftIntent);*/
                            } else {
                                openGiftDialog();
                            }
                        });

                        commentLay.setOnClickListener(v -> {
                            App.preventMultipleClick(commentLay);
                            playerpause(false);
                            videoDialog(videoItem.getPublisherId(), videoItem.getVideoId());
                        });

                        profileImage.setOnClickListener(v -> {
                                    Log.d(TAG, "onResponse: "+Constants.Login_method);
                            if (Constants.Login_method.equals("skip")){
                                GetSet.reset();
                                SharedPref.clearAll();
                                DBHelper.getInstance(getApplicationContext()).clearDB();
                                Intent logout = new Intent(getApplicationContext(), LoginActivity.class);
                                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                getApplicationContext().startActivity(logout);
                            }else {
                                App.preventMultipleClick(profileImage);
                                    profile();
                                }
                        }
                        );

                        String lang = LocaleManager.getLanguageCode(getApplicationContext());
                        if ("1".equals(AdminData.ad_status)) {
                            newadlayout.setVisibility(View.VISIBLE);

                            if ("ar".equalsIgnoreCase(lang)) {
                                // Arabic → Right-to-left marquee
                                marqueeText.setText(AdminData.arabic_ad_key);
                                marqueeText.setTextDirection(View.TEXT_DIRECTION_RTL);
                                // marqueeText.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                            } else {
                                // English and others → Left-to-right marquee
                                marqueeText.setText(AdminData.english_ad_key);
                                marqueeText.setTextDirection(View.TEXT_DIRECTION_LTR);
                                //marqueeText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                            }

                            // Common marquee properties
                            marqueeText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                            marqueeText.setSingleLine(true);
                            marqueeText.setMarqueeRepeatLimit(-1);

                            // 👇 Key part: request focus to make marquee actually scroll
                            marqueeText.setSelected(true);

                        } else {
                            newadlayout.setVisibility(View.GONE);
                        }

                        profileFollowIcon.setOnClickListener(v -> {
                            App.preventMultipleClick(profileFollowIcon);
                            profileFollowIcon.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this, R.anim.anim_zoom_out));
                            followAPI(videoItem.getPublisherId(), videoItem.getPublisherImage());
                        });


                        heartLay.setOnClickListener(v -> {
                            if (!likePrevent) {
                                likePrevent = true;
                                //heartLay.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this, R.anim.anim_zoom_in));
                                heartFun();
                            }

                        });

                        lottieAudio.setOnClickListener(v -> {
                            App.preventMultipleClick(lottieAudio);
                            if (videoItem.getSoundtracks().isUseAudio()) {
                                playerpause(false);
                                MusicListResponse.Result musicItem = new MusicListResponse().new Result();
                                musicItem.setSoundId(videoItem.getSoundtracks().getSoundId());
                                musicItem.setTitle(videoItem.getSoundtracks().getTitle());
                                musicItem.setSoundUrl(videoItem.getSoundtracks().getSoundUrl());
                                musicItem.setIsFavorite(videoItem.getSoundtracks().getIsFavorite() != null && videoItem.getSoundtracks().getIsFavorite());
                                musicItem.setCoverImage(videoItem.getSoundtracks().getCoverImage());
                                musicItem.setDuration(videoItem.getSoundtracks().getDuration());
                                Intent intent = new Intent(SingleVideoActivity.this, VideoListActivity.class);
                                intent.putExtra(Constants.TAG_FROM, Constants.TAG_MUSIC);
                                intent.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(musicItem));
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                musicEditResultLauncher.launch(intent);
                            } else {
                                App.makeToast(getString(R.string.audio_disabled_description));
                            }
                        });

                        txt_title.setOnClickListener(v -> profile());

                        moreLay.setOnClickListener(v -> {
                            App.preventMultipleClick(moreLay);
                            HomeBottomDialog bottomDialog = new HomeBottomDialog(SingleVideoActivity.this);
                            bottomDialog.openMenu(response.body().getResult(), 0, SingleVideoActivity.this);
                        });

                        txt_comment_count.setOnClickListener(v -> {
                            playerpause(false);
                            App.preventMultipleClick(txt_comment_count);
                            videoDialog(videoItem.getPublisherId(), videoItem.getVideoId());
                        });

                        //  mediaContainer.setOnClickListener(v -> togglePlay());

                        /*mediaContainer.setOnLongClickListener(v -> {

                            if (!videoItem.getPublisherId().equals(GetSet.getUserId())) {
                                reportAndFav();
                                return true;
                            }
                            return false;
                        });*/

                        btnBack.setOnClickListener(v -> {
                            cusBack();
                        });


                        // TODO: add double-tap heart
                        final GestureDetector gestureDetector = new GestureDetector(SingleVideoActivity.this, new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onDoubleTap(MotionEvent e) {

                                if (!videoItem.getIsLiked()) {

                                    if (!likePrevent) {
                                        likePrevent = true;
                                        heartFun();
                                    }

                                }

                                icFavHeart.setVisibility(View.VISIBLE);
                                icFavHeart.playAnimation();
                                icFavHeart.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this,
                                        R.anim.heart_animation));

                                icFavHeart.addAnimatorListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        Timber.i("onAnimationStart");
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        Timber.i("onAnimationEnd");
                                        icFavHeart.clearAnimation();
                                        icFavHeart.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                        Timber.i("onAnimationCancel");
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {
                                        Timber.i("onAnimationRepeat");
                                    }
                                });


                                return super.onDoubleTap(e);
                            }

                            @Override
                            public boolean onSingleTapConfirmed(MotionEvent e) {
                                if (icFavHeart.getVisibility() != View.VISIBLE) {
                                    togglePlay();
                                }
                                return super.onSingleTapConfirmed(e);
                            }

                            @Override
                            public void onLongPress(MotionEvent e) {
                                if (!videoItem.getPublisherId().equals(GetSet.getUserId())) {
                                    reportAndFav();
                                }
                                super.onLongPress(e);


                            }
                        });
                        setNullLay();
                        mediaContainer.setOnTouchListener((v, event) -> {
                            return gestureDetector.onTouchEvent(event);
                        });
                    } else {
                        mainLay.setVisibility(VISIBLE);
                        setNullLay();
                        hideLoading();
                        Toasty.error(SingleVideoActivity.this, R.string.story_not_found, Toasty.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<SingleVideoResponse> call, @NotNull Throwable t) {
                    Timber.e("getVideoResult: %s", t.getMessage());
                }
            });
        }
    }





    private void setNullLay() {
        if (videoItem != null) {
            nullLay.setVisibility(View.GONE);
            parentLay.setVisibility(VISIBLE);
        } else {
            nullLay.setVisibility(VISIBLE);
            nullImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_videos));
            nullText.setText(getString(R.string.video_not_found));
            nullText.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            parentLay.setVisibility(View.GONE);
        }
    }

    private void showLoading() {
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(false);
    }

    private void cusBack() {
        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(false);
            videoPlayer.release();
        }
        if (videoSurfaceView != null) {
            videoSurfaceView.removeAllViews();
        }
        isVideoViewAdded = true;
        if (videoCommandBottomDialog != null) videoCommandBottomDialog.cancel();
        if (isTaskRoot()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else {
            setResult(RESULT_OK, getIntent());
        }
        finish();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    private void reportAndFav() {

        LinearLayout favLay, reportLay;

        TextView txtFav, txtReport;

        bottom_sheet_longpress = getLayoutInflater().inflate(R.layout.bottmsheet_home_longpress, null);
        bottomSheetLongPressDiloag = new BottomSheetDialog(this, R.style.FavAndReportBottomSheetDialog); // Style here
        bottomSheetLongPressDiloag.setContentView(bottom_sheet_longpress);

        favLay = bottom_sheet_longpress.findViewById(R.id.favLay);
        txtFav = bottom_sheet_longpress.findViewById(R.id.txtFav);
        reportLay = bottom_sheet_longpress.findViewById(R.id.reportLay);
        txtReport = bottom_sheet_longpress.findViewById(R.id.txtReport);

        if (videoItem.getVideoIsFavorite())
            txtFav.setText(getString(R.string.unFav));
        else txtFav.setText(getString(R.string.addafav));

        // True means already fav
        if (videoItem.getIsVideoReported())
            txtReport.setText(getString(R.string.unreported));
        else txtReport.setText(getString(R.string.report_user));

        favLay.setOnClickListener(v -> {
            App.preventMultipleClick(favLay);
            favApi();
        });

        reportLay.setOnClickListener(v -> {
            App.preventMultipleClick(reportLay);
            if (txtReport.getText().toString().trim().equalsIgnoreCase(getResources().getString(R.string.report_user))) {
                playerpause(false);
                openReportDialog();

            } else {
                sendReport("", false);
            }
        });

        bottomSheetLongPressDiloag.show();

    }

    private void openReportDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet_report, null);
        reportDialog = new BottomSheetDialog(this, R.style.Bottom_ReportDialog); // Style here
        reportDialog.setContentView(bottomSheet);
        bottomSheet.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        WindowManager.LayoutParams params = reportDialog.getWindow().getAttributes();
        params.x = 0;
        reportDialog.getWindow().setAttributes(params);
        bottomSheet.requestLayout();
        reportsView = bottomSheet.findViewById(R.id.reportView);
        reportBtnBack = bottomSheet.findViewById(R.id.btnBack);
        txtTitle = bottomSheet.findViewById(R.id.txtTitle);

        reportBtnBack.setVisibility(View.GONE);
        txtTitle.setVisibility(View.GONE);
        txtTitle.setText(R.string.report_user);

        reportBtnBack.setOnClickListener(v -> {
            reportDialog.dismiss();
        });

        reportDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            @SuppressLint("CutPasteId") View bottomSheet1 = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet1).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet1).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet1).setHideable(true);
        });

        reportDialog.setOnDismissListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet12 = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet12).setState(BottomSheetBehavior.STATE_COLLAPSED);
            BottomSheetBehavior.from(bottomSheet12).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet12).setHideable(true);
            playerpause(true);
        });

        loadReports();
    }

    private void favApi() {

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_ADDIDTOFAV, videoItem.getVideoId());
            map.put(Constants.TAG_FAV_TYPE, "video");

            Timber.d("sendReport: %s", new Gson().toJson(map));

            Call<Map<String, String>> call = apiInterface.setFav(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> reportResponse = response.body();

                        Timber.d("setFav onResponse: %s", new Gson().toJson(response.body()));

                        if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (reportResponse.get(Constants.TAG_MESSAGE).equals(Constants.TAG_UNN_FAV)) {
                                EventBus.getDefault().post(new ForYouVideoFav(videoItem.getVideoId(), "Fav", false));
                                EventBus.getDefault().post(new FollowingVideoFav(videoItem.getVideoId(), "Fav", false));
                                videoItem.setVideoIsFavorite(false);
                                App.dialog(SingleVideoActivity.this, "", getResources().getString(R.string.add_fav_video_removed), getResources().getColor(R.color.colorBlack));
                            } else {
                                EventBus.getDefault().post(new ForYouVideoFav(videoItem.getVideoId(), "Fav", true));
                                EventBus.getDefault().post(new FollowingVideoFav(videoItem.getVideoId(), "Fav", false));
                                videoItem.setVideoIsFavorite(true);
                                App.dialog(SingleVideoActivity.this, "", getResources().getString(R.string.add_fav_video_success), getResources().getColor(R.color.colorBlack));
                            }

                            if (bottomSheetLongPressDiloag.isShowing())
                                bottomSheetLongPressDiloag.dismiss();

                        } else if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                            App.makeToast(getString(R.string.Account_block));
                            GetSet.reset();
                            SharedPref.clearAll();
                            DBHelper.getInstance(SingleVideoActivity.this).clearDB();
                            Intent logout = new Intent(SingleVideoActivity.this, LoginActivity.class);
                            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(logout);
                        }
                        else
                            App.makeToast(getString(R.string.something_went_wrong));
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }

                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                    App.makeToast(getString(R.string.something_went_wrong));
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void loadReports() {
        reportAdapter = new ReportAdapter(this, AdminData.reportList, getVideoId, -1, this);
        reportsView.setLayoutManager(new LinearLayoutManager(this));
        reportsView.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        reportDialog.show();
    }

    @Override
    public void onResponse(Object apiResponse, int adapterPosition) {
        updateReportResponse(apiResponse, adapterPosition);
    }

    private void updateReportResponse(Object apiResponse, int adapterPosition) {
        Response<Map<String, String>> response = (Response<Map<String, String>>) apiResponse;
        if (response.isSuccessful()) {
            Map<String, String> reportResponse = response.body();
            if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                if (reportResponse.get(Constants.TAG_MESSAGE).equals("Reported successfully")) {
                    App.dialog(SingleVideoActivity.this, "", getResources().getString(R.string.report_success_alert), getResources().getColor(R.color.color_green));
                    videoItem.setIsVideoReported(true);
                    playerpause(true);
                    EventBus.getDefault().post(new ForYouVideoFav(videoItem.getVideoId(), "Report", true));
                    EventBus.getDefault().post(new FollowingVideoFav(videoItem.getVideoId(), "Report", true));

                } else if (reportResponse.get(Constants.TAG_MESSAGE).equals("Report Undo successfully")) {
                    App.dialog(SingleVideoActivity.this, "", getResources().getString(R.string.undo_report_successfully), getResources().getColor(R.color.colorBlack));
                    EventBus.getDefault().post(new ForYouVideoFav(videoItem.getVideoId(), "Report", false));
                    EventBus.getDefault().post(new FollowingVideoFav(videoItem.getVideoId(), "Report", false));
                    videoItem.setIsVideoReported(false);
                }

                if (reportDialog != null && reportDialog.isShowing())
                    reportDialog.dismiss();

                if (bottomSheetLongPressDiloag.isShowing())
                    bottomSheetLongPressDiloag.dismiss();

            } else if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                App.makeToast(getString(R.string.Account_block));
                GetSet.reset();
                SharedPref.clearAll();
                DBHelper.getInstance(SingleVideoActivity.this).clearDB();
                Intent logout = new Intent(SingleVideoActivity.this, LoginActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logout);
            }
            else
                App.makeToast(getString(R.string.something_went_wrong));
        } else {
            App.makeToast(getString(R.string.something_went_wrong));
        }

    }

    @Override
    public void onFailure(Throwable t, int adapterPosition) {
        App.makeToast(getApplicationContext().getString(R.string.something_went_wrong));
    }

    private void sendReport(String title, boolean report) {
        if (NetworkReceiver.isConnected()) {
            CommonApi.reportVideo(getVideoId, title, report, new ApiCallback() {
                @Override
                public void onResponse(Object response) {
                    updateReportResponse(response, -1);
                }

                @Override
                public void onFailure(Throwable t) {
                    Timber.e("sendReport: %s", t.getMessage());
                    App.makeToast(getApplicationContext().getString(R.string.something_went_wrong));
                }
            });
        } else {
            Toasty.error(getApplicationContext(), getApplicationContext().getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void togglePlay() {

        if (videoPlayer != null) {
            if (videoState == PlayState.OFF) {
                videoPlayer.setPlayWhenReady(true);
                videoState = PlayState.ON;

            } else if (videoState == PlayState.ON) {
                videoPlayer.setPlayWhenReady(false);
                videoState = PlayState.OFF;
            }

            animatePlayControl();
        }
    }

    private void animatePlayControl() {
        if (video_play_pause != null) video_play_pause.setVisibility(VISIBLE);

        try {
            if (videoPlayer != null) {
                if (videoState == PlayState.OFF) {

                    if (video_play_pause != null) {
                        video_play_pause.setImageResource(R.drawable.video_play);

                        video_play_pause.animate().cancel();

                        video_play_pause.setAlpha(1f);
                    }


                } else if (videoState == PlayState.ON) {
                    if (video_play_pause != null) {
                        video_play_pause.setImageResource(R.drawable.video_play);

                        video_play_pause.animate().cancel();
                        video_play_pause.setAlpha(1f);
                        video_play_pause.animate().alpha(0f)
                                .setDuration(500).setStartDelay(100);
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void videoDialog(String publisherId, String videoId) {

        commendList.clear();
        tagged_user_id = "";
        tagged_user_name = "";
        list_tagged_user_ids.clear();
        list_tagged_user_names.clear();

        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_video_commend_view, null);
        videoCommandBottomDialog = new BottomSheetDialog(SingleVideoActivity.this, R.style.VideoCommentNewDialog); // Style here

        videoCommandBottomDialog.setContentView(sheetView);
        videoCommandBottomDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        addComment = sheetView.findViewById(R.id.addComment);
        commentCount = sheetView.findViewById(R.id.commentCount);
        commentRec = sheetView.findViewById(R.id.commentRec);
        sendCommentLay = sheetView.findViewById(R.id.sendCommentLay);
        rootView = sheetView.findViewById(R.id.rootView);
        headerBottom = sheetView.findViewById(R.id.headerBottom);
        mSwipeVideoComment = sheetView.findViewById(R.id.mSwipeVideoComment);
        ImageView btnClose = sheetView.findViewById(R.id.btnClose);
        noCommands = sheetView.findViewById(R.id.noCommands);

        mSwipeVideoComment.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));

        if (!SingleVideoActivity.this.isFinishing()) {
            videoCommandBottomDialog.show();
        }

        commendAdapter = new CommentAdapter(SingleVideoActivity.this, commendList, publisherId, videoId, 0, new AdapterItemClickListener() {
            @Override
            public void onItemClicked(Object o, int position, String type) {
                if (type.equals(Constants.TAG_USERS)) {
                    String userId = (String) o;
                    if (userId.equals(GetSet.getUserId())) {
                        Intent profile = new Intent(getApplicationContext(), MyProfileActivity.class);
                        profile.putExtra(Constants.TAG_EDIT, true);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(profile);
                    } else {
                        Intent profile = new Intent(getApplicationContext(), OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_ID, userId);
                        profile.putExtra(Constants.TAG_FROM, Constants.TAG_SINGLE_VIDEO);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(profile);
                    }
                } else {
                    String userName = (String) o;
                    if (userName.equals(GetSet.getUserName())) {
                        Intent profile = new Intent(getApplicationContext(), MyProfileActivity.class);
                        profile.putExtra(Constants.TAG_EDIT, true);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(profile);
                    } else {
                        Intent profile = new Intent(getApplicationContext(), OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_NAME, userName);
                        profile.putExtra(Constants.TAG_FROM, Constants.TAG_SINGLE_VIDEO);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(profile);
                    }
                }
            }
        });
//                commendAdapter = new CommentAdapter(this, commendList, publisherId, videoId);
        linearLayoutManager = new LinearLayoutManager(this);
        commentRec.setLayoutManager(linearLayoutManager);
        commentRec.setAdapter(commendAdapter);

        commentRec.setNestedScrollingEnabled(true);

        //     mSwipeVideoComment.setOnRefreshListener(this::swipeRefresh);

        mSwipeVideoComment.setOnRefreshListener(() -> swipeRefresh(true, videoId));


        videoScrollListener = new OnVerticalScrollListener() {
            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();


                if (commendList.size() <= lastOffsetVideo) return;
                else lastOffsetVideo = commendList.size();

                loadMoreCheckVideoCommand = "loadmore";
                getCommand(lastOffsetVideo, videoId);

            }
        };

        commentRec.addOnScrollListener(videoScrollListener);


        swipeRefresh(true, videoId);

        btnClose.setOnClickListener(v -> {
            if (mCommentsEditText != null)
                addComment.setText(mCommentsEditText.getText().toString());
            loadMoreCheckVideoCommand = "initial";
            commendList.clear();
            // exoplayerRecyclerViewForYou.setPlayControl(true);
            videoCommandBottomDialog.setDismissWithAnimation(true);
            if (videoCommandBottomDialog.isShowing()) videoCommandBottomDialog.dismiss();
            if (commentEditDialog != null && commentEditDialog.isShowing())
                commentEditDialog.dismiss();

            playerpause(true);
        });


        sendCommentLay.setOnClickListener(v -> {
            topBottomSheet(videoId, addComment.getText().toString());
        });

        videoCommandBottomDialog.setOnKeyListener((v, keyCode, event) -> {


            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (commentEditDialog != null && commentEditDialog.isShowing()) {
                    commentEditDialog.dismiss();
                    playerpause(false);
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        });

        videoCommandBottomDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
                playerpause(true);
            }
        });

    }

    private void topBottomSheet(String videoId, String setText) {

        View commentSentDialog = getLayoutInflater().inflate(R.layout.bottom_sheet_comment_sent, null);
        commentEditDialog = new BottomSheetDialog(SingleVideoActivity.this, R.style.SecondaryBottomSheet);

        commentEditDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        commentEditDialog.setContentView(commentSentDialog);
        commentEditDialog.getBehavior().setHideable(true);
        commentEditDialog.setCanceledOnTouchOutside(true);

        mCommentsEditText = commentSentDialog.findViewById(R.id.socialAutoCompleteTextView);
        sendComment = commentSentDialog.findViewById(R.id.sendComment);

        mCommentsEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                commentEditDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            } else {
                commentEditDialog.dismiss();
            }
        });

        lastOffsetVideo = 0;
        mCommentsEditText.requestFocus();
        /*mCommentsEditText.setThreshold(1);
        mCommentsEditText.setDropDownAnchor(R.id.rootView);
        mCommentsEditText.setDropDownHeight(0);  // don't show popup*/
        MentionArrayAdapter<Mention> adapter = new MentionArrayAdapter<>(this);
//        mCommentsEditText.setMentionAdapter(adapter);

        ListView mentionsList = commentSentDialog.findViewById(R.id.userListView);
        mentionsList.setAdapter(adapter);
        Toast[] singleTonToast = new Toast[1];

        Handler mainHandler = new Handler(Looper.getMainLooper());
        final long[] lastEdited = {0};
        Runnable inputFinishChecker = new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() > lastEdited[0] + 500 - 200) {
                    if (!TextUtils.isEmpty(mCommentsEditText.getText()) && searchUser) {
                        String s = mCommentsEditText.getText().toString();
                        if ((s.length() - s.replaceAll("@", "").length()) > 5) {
                            if (singleTonToast.length > 0 && singleTonToast[0] != null)
                                singleTonToast[0].cancel();
                            singleTonToast[0] = Toasty.info(SingleVideoActivity.this,
                                    "Only 5 tags allowed");
                            singleTonToast[0].show();
                            return;
                        }
                        final String q = getMentionsSuggestionQuery(s, mCommentsEditText.getSelectionEnd());
                        Log.d(TAG, String.format("inputFinishChecker: q=%s", q));
                        if (q.length() > 0) {
                            // TODO: fetch users for q
                            getLiveUsersForQuery(q, result -> {
                                //                            mentionsCache.clear();
                                adapter.clear();
                                adapter.notifyDataSetChanged();
                                mentionsCache.addAll(
                                        Stream.of(result)
                                                .map(result1 -> {
                                                            // for some difficulties we have to send the Mention
                                                            adapter.add(new Mention(result1.getUsername(),
                                                                    result1.getDisplayname(), result1.getAvatar()));
                                                            return new Mention2(result1.getUserId(), result1.getUsername(),
                                                                    result1.getName(), result1.getAvatar());
                                                        }
                                                )
                                                .collect(Collectors.toList())
                                );
                                Timber.d("getLiveUsersForQuery: %s", new Gson().toJson(mentionsCache));
                                if (adapter.getCount() > 0) {
                                    adapter.getFilter().filter(q);
                                    adapter.notifyDataSetChanged();
                                    mentionsList.setVisibility(View.VISIBLE);
                                } else {
                                    mentionsList.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            adapter.clear();
                            adapter.notifyDataSetChanged();
                            mentionsList.setVisibility(View.GONE);
                        }
                    } else {
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                        mentionsList.setVisibility(View.GONE);
                    }
                }
            }
        };

        mCommentsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mainHandler.removeCallbacks(inputFinishChecker);
                if (s.toString().length() >= 250) {
                    Toasty.info(SingleVideoActivity.this, R.string.max_reached, Toasty.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                lastEdited[0] = System.currentTimeMillis();
                mainHandler.postDelayed(inputFinishChecker, 500);
            }
        });

        mentionsList.setOnItemClickListener((parent, view, position, id) -> {
            final String selected = adapter.getItem(position).toString();
            //            Timber.d("setOnItemClickListener: selected %s, %s", position, selected);
            if (mCommentsEditText.getMentions().contains(selected)) {
                Toasty.info(this,
                        R.string.already_tagged).show();
                return;
            }
            getReplacedText(mCommentsEditText, selected, mCommentsEditText.getSelectionEnd());
            mentionsList.setVisibility(View.GONE);
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    searchUser = true;
                }
            }, 1000);
        });

        mCommentsEditText.setText(setText);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mCommentsEditText, InputMethodManager.SHOW_IMPLICIT);
        if (!SingleVideoActivity.this.isFinishing()) {
            commentEditDialog.show();
        }
        mCommentsEditText.requestFocus();

        sendComment.setOnClickListener(v -> {
            if (Constants.Login_method.equals("skip")){
                GetSet.reset();
                SharedPref.clearAll();
                DBHelper.getInstance(this).clearDB();
                Intent logout = new Intent(this, LoginActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(logout);
            }else {
                if (!Objects.requireNonNull(mCommentsEditText.getText()).toString().isEmpty()) {
                    postComments(mCommentsEditText.getText().toString(), videoId);
                    mCommentsEditText.setText("");
                    addComment.setText("");
                }
            }
        });

        commentEditDialog.setOnCancelListener(dialog -> {
            if (mCommentsEditText != null)
                addComment.setText(mCommentsEditText.getText().toString());
            loadMoreCheckVideoCommand = "initial";
            commendList.clear();
            //followingExoplayerRecyclerView.setPlayControl(true);
            videoCommandBottomDialog.setDismissWithAnimation(true);
            videoCommandBottomDialog.dismiss();
            commentEditDialog.dismiss();
        });

        CustomEditText.OnKeyPreImeListener onKeyPreImeListener = () -> {
            if (mCommentsEditText != null)
                addComment.setText(mCommentsEditText.getText().toString());
            //VideoCommentSentDialog.dismiss();
        };

//        mCommentsEditText.setOnKeyPreImeListener(onKeyPreImeListener);
    }

    @NonNull
    private String getMentionsSuggestionQuery(@NonNull String s, int position) {
        final int end = position;
        int start = -1;
        position--;
        while (position >= 0) {
            char c = s.charAt(position);
            if (CharMatcher.whitespace().matches(c)) {
                return "";  // User just typed space, we don't show suggestions
            } else if (c == '@') {
                if (start != -1) return s.substring(start, end);
                else return "";
            } else {
                start = position;
                position--;
            }
        }
        return "";
    }

    private boolean validMentionSearch(SocialAutoCompleteTextView ed, String s) {
        return ed.getMentions().size() != mentions.size();
    }


    private void swipeRefresh(final boolean refresh, String videoId) {
        mSwipeVideoComment.setRefreshing(refresh);

        if (refresh) {
            lastOffsetVideo = 0;
            currentPage = 0;
            loadMoreCheckVideoCommand = "initial";
            getCommand(currentPage, videoId);
        }
    }

    private void initialize(String playbackUrl, boolean autoPlay) {
        String mediaUrl = null;
        Uri hlsUri = null;

        videoSurfaceView = new StyledPlayerView(SingleVideoActivity.this);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(SingleVideoActivity.this);
        trackSelector.setParameters(
                trackSelector.buildUponParameters().setMaxVideoSizeSd());
        videoPlayer = new ExoPlayer.Builder(SingleVideoActivity.this)
                .setTrackSelector(trackSelector)
                .build();

        // Disable Player Control
        videoSurfaceView.setUseController(false);
        // Bind the player to the view.
        videoSurfaceView.setPlayer(videoPlayer);
        // Turn on Volume
        videoPlayer.setVolume(1f);
        videoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

        videoPlayer.addListener(new Player.Listener() {

            @Override
            public void onTracksInfoChanged(TracksInfo tracksInfo) {
                Player.Listener.super.onTracksInfoChanged(tracksInfo);
            }

            @Override
            public void onIsLoadingChanged(boolean isLoading) {
                Timber.d("onLoadingChanged: %s", isLoading);
            }

            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {
                Timber.d("onTimelineChanged: %s", timeline);
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {

                    case Player.STATE_BUFFERING:
                        mediaCoverImage.setAlpha(1f);
                        break;
                    case Player.STATE_ENDED:
                        dbHelper.insertVideoStatus(videoItem.getVideoId());
                        videoPlayer.seekTo(0);
                        break;
                    case Player.STATE_IDLE:
                        break;

                    case Player.STATE_READY:
                        mediaCoverImage.animate().alpha(0f)
                                .setDuration(300).setStartDelay(280);
                        progressBar.setVisibility(View.GONE);
                        if (!isVideoViewAdded) {
                            addVideoView();
                            videoState = PlayState.ON;
                            setProgressDuration();
                        }

                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });

        final String videoType = videoItem != null
                ? videoItem.getVideoType()
                : "normal";

        if (Objects.equals(videoType, "gallery")) {
            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        } else {
            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        }

        if (!isVideoViewAdded) {
            mediaUrl = playbackUrl;
            hlsUri = Uri.parse(mediaUrl);
            Log.e("mediaurl", "-" + mediaUrl);
            /** Play from HlsMediaSource*/
            MediaSource hlsMediaSource = ExoPlayerUtils.getHlsMediaSource(SingleVideoActivity.this, hlsUri);
            videoPlayer.setMediaSource(hlsMediaSource);
            videoPlayer.prepare();
            videoPlayer.setPlayWhenReady(autoPlay);
        }
    }

    public void getCommand(int currentPage, String videoId) {

        if (noCommands.getVisibility() == View.VISIBLE) noCommands.setVisibility(View.GONE);

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_VIDEO_ID, videoId);
            map.put(Constants.TAG_OFFSET, String.valueOf(currentPage));
            map.put(Constants.TAG_LIMIT, "20");

            Timber.d("getCommentParams: %s", new Gson().toJson(map));

            Call<VideoCommentResponse> call = apiInterface.getVideoComments(map);
            call.enqueue(new Callback<VideoCommentResponse>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(@NotNull Call<VideoCommentResponse> call, @NotNull Response<VideoCommentResponse> response) {

                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {

                        Timber.d("getCommentResponse=> %s", new Gson().toJson(response.body()));

                        noCommands.setVisibility(View.GONE);
                        commentRec.setVisibility(View.VISIBLE);

                        if (response.body().getIsUserCanComment()) {
                            sendCommentLay.setVisibility(View.VISIBLE);
                            Timber.d("getcommand onResponse: true");
                        } else {
                            Timber.d("getcommand  onResponse: false");
                            sendCommentLay.setVisibility(View.INVISIBLE);
                        }

                        /*Bundle payload = new Bundle();
                        homeApiResponse.get(adapterPosition).setVideoCommentCounts(String.valueOf(response.body().getCommentcount()));
                        payload.putString("video_comment_count", String.valueOf(response.body().getCommentcount()));
                        videoAdapter.notifyItemChanged(adapterPosition, payload);*/

                            /*commentRec.clearOnScrollListeners();
                            commendAdapter.notifyDataSetChanged();
                            commentRec.smoothScrollToPosition(0);
                            commentRec.addOnScrollListener(videoScrollListener);*/


                        if (loadMoreCheckVideoCommand.equals("initial")) {
                            commendList.clear();
                            commendList.addAll(response.body().getResult());
                            swipeRefresh(false, videoId);
                            mSwipeVideoComment.setEnabled(false);
                            commentRec.addOnScrollListener(videoScrollListener);
                            commentCount.setText(response.body().getCommentcount() + " " + getResources().getString(R.string.comments));

                            txt_comment_count.setText(String.valueOf(response.body().getCommentcount()));

                        } else if (loadMoreCheckVideoCommand.equals("loadmore")) {
                            commendList.addAll(response.body().getResult());
                        }

                        commendAdapter.notifyDataSetChanged();


                    } else if (response.body().getStatus().equals(Constants.TAG_FALSE)) {
                        swipeRefresh(false, videoId);
                        mSwipeVideoComment.setEnabled(false);

                        if (response.body().getIsUserCanComment()) {
                            sendCommentLay.setVisibility(View.VISIBLE);
                            Timber.d("getcommand onResponse: true");
                        } else {
                            Timber.d("getcommand  onResponse: false");
                            sendCommentLay.setVisibility(View.INVISIBLE);
                        }

                        if (commendList.size() == 0) {
                            noCommands.setVisibility(View.VISIBLE);
                            commentRec.setVisibility(View.GONE);
                        }
                    } else if (response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                        App.makeToast(getString(R.string.Account_block));
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(SingleVideoActivity.this).clearDB();
                        Intent logout = new Intent(SingleVideoActivity.this, LoginActivity.class);
                        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(logout);
                    }

                }

                @Override
                public void onFailure(@NotNull Call<VideoCommentResponse> call, @NotNull Throwable t) {
                    call.cancel();
                    App.makeToast(getString(R.string.something_went_wrong));
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }

    }

    public void postComments(final String comments, String videoId) {
        if (NetworkReceiver.isConnected()) {
            Tagged tagged = getTagged(mentionsCache, mCommentsEditText.getMentions());
            Timber.d("Mentions: ids=%s names=%s", tagged.getCommaSeparatedTaggedIdList(),
                    tagged.getCommaSeparatedTaggedNameList());
                    /*tagged_user_id = getTaggedUserIds(mentionsCache, mCommentsEditText.getMentions());
                    tagged_user_name = getTaggedUserNames(mentionsCache, mCommentsEditText.getMentions());*/

            Map<String, String> map = new HashMap<>();
            map.put("user_id", GetSet.getUserId());
            map.put("video_id", videoId);
            map.put("tagged_user_id", tagged.getCommaSeparatedTaggedNameList());
            map.put("tagged_id", tagged.getCommaSeparatedTaggedIdList());
            map.put("token", GetSet.getAuthToken());
            map.put("comment", comments);
            Timber.d("postComments: params %s", App.getGsonPrettyInstance().toJson(map));

            loadMoreCheckVideoCommand = "initial";
            mSwipeVideoComment.setEnabled(true);
            mSwipeVideoComment.setRefreshing(true);
            commentEditDialog.dismiss();

            Call<Map<String, String>> call3 = apiInterface.postComments(map);
            call3.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> data = response.body();
                        //                    Timber.d("onResponse: %s", new Gson().toJson(response.body()));
                        if (data != null && data.get(Constants.TAG_STATUS).equals("true")) {
                            list_tagged_user_ids.clear();
                            list_tagged_user_names.clear();
                            tagcomment = "";
                            tagcomment = "";
                            tagged_user_name = "";
                            tagged_user_id = "";

                            lastOffsetVideo = 0;
                            currentPage = 0;

                            commentRec.clearOnScrollListeners();
                            commentRec.scrollToPosition(0);
                            loadMoreCheckVideoCommand = "initial";
                            getCommand(0, videoId);

                            commentCount.setText(data.get("commentcount") + " " + getResources().getString(R.string.comments));
                            commentCount.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this, R.anim.anim_zoom_out));
                            txt_comment_count.setText(data.get("commentcount"));
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                    Timber.d("onFailure: %s", "response- failure");
                }
            });
        } else {
        }

    }

    private void bindView(HomeResponse.Result result) {
        if (GetSet.getUserId().equals(result.getPublisherId())) {
            profileFollowIcon.setVisibility(View.GONE);
        } else {

            if (result.getFollowedUser()) // If API come true that user already follow
                profileFollowIcon.setVisibility(View.GONE);
            else
                profileFollowIcon.setVisibility(VISIBLE);
        }


        Glide.with(getApplicationContext())
                .load(result.getPublisherImage())
                .placeholder(R.drawable.default_profile_image)
                .into(profileImage);

        /*Glide.with(getApplicationContext())
                .load(result.getStreamThumbnail())

                .into(mediaCoverImage);*/


        txt_title.setText(String.format("@%s", result.getPostedBy()));

        txt_heart_count.setText(result.getLikes());
        txt_gift_count.setText(result.getVideoGiftCounts());
        txt_comment_count.setText(result.getVideoCommentCounts());

        txt_description.setVisibility(View.GONE);
        txt_description.setIsLinkable(true);
        String getHash = result.getVideoDescription();
        if (!TextUtils.isEmpty(getHash)) {
            txt_description.setVisibility(VISIBLE);
            txt_description.setText(getHash);
            MovementMethod m = txt_description.getMovementMethod();
            if ((m == null) || !(m instanceof LinkMovementMethod)) {
                if (txt_description.getLinksClickable()) {
                    txt_description.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            txt_description.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {
                @Override
                public void onTextLinkClick(View textView, String clickedString) {
                    if (clickedString.contains("@")) {
                    } else if (clickedString.contains("#")) {
                        playerpause(false);
                        String user_param = clickedString.replace("#", "");
                        Intent intent = new Intent(SingleVideoActivity.this, VideoListActivity.class);
                        intent.putExtra(Constants.TAG_TITLE, user_param);
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_RELATED_HASHTAG);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    }
                }
            });

            txt_description.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                    return false;
                }
            });
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHalf = (displayMetrics.widthPixels * 35 / 100) - App.dpToPx(this, 0);
            txt_description.setMaxHeight(screenHalf);
        }
    }


    private void setProgressDuration() {
        runnable = () -> {
            if (videoSurfaceView != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progress_duration.setProgress((int) ((videoPlayer.getCurrentPosition() * 100) / videoPlayer.getDuration()), true);
                } else
                    progress_duration.setProgress((int) ((videoPlayer.getCurrentPosition() * 100) / videoPlayer.getDuration()));
            }
            mHandler.postDelayed(runnable, 1);
        };
        runnable.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoPlayer != null) videoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //  unregisterNetworkReceiver();
    }

    @Override
    public void setFavourite(boolean favourite, ArrayList<HomeResponse.Result> result, int adapterPosition) {
        videoItem.setVideoIsFavorite(favourite);
    }

    @Override
    public void setPlayback(boolean b, int adapterPosition, String tagPlayback) {
        playerpause(b);
    }

    @Override
    public void setVideoReport(boolean b, int adapterPosition) {
        videoItem.setIsVideoReported(b);
    }

    @Override
    public void checkStoragePermissions() {
        requestStoragePermissions();
    }

    @Override
    public void moveToIntent(Intent intent) {
        if (intent.hasExtra(Constants.TAG_EDIT)) {
            videoEditResultLauncher.launch(intent);
        } else {
            startActivity(intent);
        }
    }

    @Override
    public void setVideoDeleted(boolean isDeleted, String videoId) {
        finish();
    }

    @Override
    public void setVideoPlayBackSpeed(boolean speedChanged, String videoId, int adapterPosition, String playbackSpeed, float tagPlaybackSpeed) {

    }

    @Override
    public void setVideoCaption(boolean captionEnabled, String videoId, int adapterPosition, String typeCaption, String playBackUrl) {

    }

    @Override
    public void reposted(boolean reposted, String videoId, int adapterPosition, String action) {

    }

   /* @Override
    public void setVideoPlayBackSpeed(String videoId, int adapterPosition, float tagPlaybackSpeed) {

    }*/

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            storagePermissionResult.launch(PermissionsUtils.READ_STORAGE_PERMISSION);
        } else {
            storagePermissionResult.launch(PermissionsUtils.READ_WRITE_PERMISSIONS);
        }
    }

    public enum PlayState {
        ON, OFF
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.mainLay), isConnected);
    }

    private void openGiftDialog() {

        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_gift_home, null);
        giftDialog = new BottomSheetDialog(this, R.style.BottomSheetDialog); // Style here
        giftDialog.setContentView(sheetView);

        giftDialog.setOnDismissListener(dialogInterface -> {

        });

        viewPager = sheetView.findViewById(R.id.view);
        sendLay = sheetView.findViewById(R.id.sendLay);
        txtAttachmentName = sheetView.findViewById(R.id.txtAttachmentName);
        txtSend = sheetView.findViewById(R.id.txtSend);
        pagerIndicator = sheetView.findViewById(R.id.pagerIndicator);
        giftNullLay = sheetView.findViewById(R.id.nullLay);
        giftNullImage = sheetView.findViewById(R.id.nullImage);
        giftNullText = sheetView.findViewById(R.id.nullText);
        giftNullText.setText(getString(R.string.no_gift_founded));
        giftNullText.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        giftNullImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.gift));
        sendLay.setVisibility(View.GONE);

        setViewPager();
        giftDialog.show();

        txtSend.setOnClickListener(view -> {
            int count = Integer.parseInt((txt_gift_count.getText().toString()));
            giftDialog.dismiss();
            ObjectAnimator anim = ObjectAnimator
                    .ofFloat(giftLay, "translationX", 0, 25, -25, 25, -25, 15, -15, 10, -6, 0)
                    .setDuration(2000);
            anim.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    giftLay.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this, R.anim.anim_zoom_out_length));
                    Glide.with(SingleVideoActivity.this)
                            .load(tempGiftList.get(giftPosition).getGiftIcon())
                            .into(giftImageView);
                }

                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    /*   holder.giftLay.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));*/

                    giftImageView.setImageDrawable(getResources().getDrawable(R.drawable.gift));

                }
            });
            anim.start();
//            txt_gift_count.setText(String.valueOf(count + 1));
            sendGift();
            sendLay.setVisibility(View.GONE);
            txtAttachmentName.setText("");
        });
    }

    private void setViewPager() {

        int count;
        if (AdminData.giftList.size() <= ITEM_LIMIT) {
            count = 1;
        } else {
            count = AdminData.giftList.size() % ITEM_LIMIT == 0 ? AdminData.giftList.size() / ITEM_LIMIT : (AdminData.giftList.size() / ITEM_LIMIT) + 1;
        }

        if (giftNullLay != null) {
            if (!AdminData.giftList.isEmpty()) {
                viewPager.setVisibility(View.VISIBLE);
                giftNullLay.setVisibility(View.GONE);
                pagerIndicator.setVisibility(View.VISIBLE);
            } else {
                giftNullLay.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.INVISIBLE);
                pagerIndicator.setVisibility(View.GONE);
            }
        }

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this, count, Constants.TYPE_GIFTS);
        viewPager.setAdapter(pagerAdapter);
        pagerIndicator.setViewPager(viewPager);
        viewPager.setCurrentItem(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadItems();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    public class ViewPagerAdapter extends PagerAdapter {
        private final Context context;
        private int count = 0;
        private String type = "";

        public ViewPagerAdapter(Context context, int count, String type) {
            this.context = context;
            this.type = type;
            this.count = count;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.layout_stickers, container, false);
            recyclerView = itemView.findViewById(R.id.recyclerView);

            loadItems();

            container.addView(itemView, 0);
            return itemView;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup view, int position, @NonNull Object object) {
            view.removeView((View) object);
        }
    }

    private void loadItems() {
        /*Set Array list by 8*/
        int start = viewPager.getCurrentItem() * ITEM_LIMIT;
        int end = start + (ITEM_LIMIT - 1);

        if (end > AdminData.giftList.size()) {
            end = AdminData.giftList.size() - 1;
        } else if (AdminData.giftList.size() <= end) {
            end = AdminData.giftList.size() - 1;
        }
        loadGifts(start, end);
    }

    private void loadGifts(int start, int end) {

        tempGiftList = new ArrayList<>();

        /*tempGiftList used only for display, For send AdminData.giftList used*/

        for (int i = start; i <= end; i++) {
            tempGiftList.add(AdminData.giftList.get(i));
        }
        giftAdapter = new GiftAdapter(this, tempGiftList);
        Timber.d("loadGifts: %s", new Gson().toJson(tempGiftList));
        stickerLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(stickerLayoutManager);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(giftAdapter);
        giftAdapter.notifyDataSetChanged();
    }


    public void sendGift() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_VIDEO_ID, String.valueOf(videoItem.getVideoId()));
            map.put(Constants.TAG_GIFT_ID, AdminData.giftList.get(giftPosition).getGiftId());
            map.put(Constants.TAG_GEMS_EARNINGS, "" + AdminData.giftList.get(giftPosition).getGiftGems());
            map.put(Constants.TAG_GEMS_COUNT, "" + (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false").equals("true") ?
                    AdminData.giftList.get(giftPosition).getGiftGemsPrime() : AdminData.giftList.get(giftPosition).getGiftGems()));
            Timber.d("sendGift: %s", App.getGsonPrettyInstance().toJson(map));

            Call<Map<String, String>> call = apiInterface.sendGift(map);
            call.enqueue(new Callback<Map<String, String>>() {

                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.body() != null && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        // Update homeforyouvideo and homefollowingvideo page counts
                        giftDialog.dismiss();
                        EventBus.getDefault().post(new ForYouUpdateGiftMessageCount(videoItem.getVideoId(), "gift", response.body().get("giftcount")));
                        EventBus.getDefault().post(new FollowingUpdateGiftMessageCount(videoItem.getVideoId(), "gift", response.body().get("giftcount")));
                        txt_gift_count.setText(response.body().get("giftcount"));
                    }else if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                        App.makeToast(getString(R.string.Account_block));
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(SingleVideoActivity.this).clearDB();
                        Intent logout = new Intent(SingleVideoActivity.this, LoginActivity.class);
                        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(logout);
                    }
                }

                @Override

                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {

                }
            });
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayMetrics displayMetrics = Objects.requireNonNull(SingleVideoActivity.this).getResources().getDisplayMetrics();
        displayWidth = displayMetrics.widthPixels;
        displayHeight = displayMetrics.heightPixels;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @SuppressLint("NonConstantResourceId")
    public class GiftAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;
        private List<Gift> giftList;
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public GiftAdapter(Context context, List<Gift> giftList) {

            this.context = context;
            this.giftList = giftList;

        }

        @Override
        public RecyclerView.@NotNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_gifts, parent, false);
                viewHolder = new GiftAdapter.MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new GiftAdapter.FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.@NotNull ViewHolder holder, int pos) {
            if (holder instanceof GiftAdapter.MyViewHolder) {
                int position = holder.getAbsoluteAdapterPosition();
                final Gift gift = giftList.get(position);
                Timber.d("onBindViewHolder: %s", App.getGsonPrettyInstance().toJson(gift));
                ((GiftAdapter.MyViewHolder) holder).progressBar.setVisibility(VISIBLE);
                ((GiftAdapter.MyViewHolder) holder).giftImage.setVisibility(View.INVISIBLE);

                Glide.with(getApplicationContext())
                        .load(gift.getGiftIcon())
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                ((GiftAdapter.MyViewHolder) holder).progressBar.setVisibility(View.GONE);
                                ((GiftAdapter.MyViewHolder) holder).giftImage.setImageDrawable(context.getDrawable(R.drawable.gift_primary));
                                ((GiftAdapter.MyViewHolder) holder).giftImage.setVisibility(VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                ((GiftAdapter.MyViewHolder) holder).progressBar.setVisibility(View.GONE);
                                ((GiftAdapter.MyViewHolder) holder).giftImage.setImageDrawable(resource);
                                ((GiftAdapter.MyViewHolder) holder).giftImage.setVisibility(VISIBLE);
                                return false;
                            }
                        }).apply(new RequestOptions().error(R.drawable.gift_primary))
                        .into(((GiftAdapter.MyViewHolder) holder).giftImage);

                ((GiftAdapter.MyViewHolder) holder).txtGiftPrice.setText((GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) ? "" + gift.getGiftGemsPrime() : "" + gift.getGiftGems());

                ((GiftAdapter.MyViewHolder) holder).itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ((GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE) &&
                                GetSet.getGems() >= giftList.get(position).getGiftGemsPrime()) ||
                                (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE) &&
                                        GetSet.getGems() >= giftList.get(position).getGiftGems())) {
                            giftPosition = position;
                            sendLay.setVisibility(VISIBLE);
                            txtSend.setVisibility(VISIBLE);
                            txtAttachmentName.setText(giftList.get(position).getGiftTitle());
                        } else {
                            App.makeToast(getString(R.string.not_enough_gems));
                        }
                    }
                });
            } else if (holder instanceof GiftAdapter.FooterViewHolder) {
                GiftAdapter.FooterViewHolder footerHolder = (GiftAdapter.FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(VISIBLE);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = giftList.size();
            if (showLoading)
                itemCount++;
            return itemCount;
        }

        public boolean isPositionFooter(int position) {
            return position == getItemCount() - 1 && showLoading;
        }

        public void showLoading(boolean value) {
            showLoading = value;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView giftImage;
            TextView txtGiftPrice;
            ProgressBar progressBar;
            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                giftImage = view.findViewById(R.id.giftImage);
                txtGiftPrice = view.findViewById(R.id.txtGiftPrice);
                progressBar = view.findViewById(R.id.progressBar);
                itemLay = view.findViewById(R.id.itemLay);
                ViewGroup.LayoutParams params = itemLay.getLayoutParams();
                // Changes the height and width to the specified *pixels*
                params.width = displayWidth / 4;
                itemLay.setLayoutParams(params);
            }

        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            ProgressBar progressBar;

            public FooterViewHolder(View parent) {
                super(parent);
                progressBar = findViewById(R.id.progressBar);
            }
        }
    }

    public void getLiveUsersForQuery(
            @NonNull String q,
            @NonNull NetworkResultCallback<List<UserList.Result>> callback
    ) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<>();
            map.put("user_id", GetSet.getUserId());
            map.put("token", GetSet.getAuthToken());
            map.put("search_key", q);

            Call<UserList> call = apiInterface.getUsers(map);
            call.enqueue(new Callback<UserList>() {
                @Override
                public void onResponse(@NonNull Call<UserList> call, @NonNull Response<UserList> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Success
                        if (response.body().getResult() != null)
                            callback.onResult(response.body().getResult());
                    } else {
                        onFailure(call, new Exception(String.format(Locale.ENGLISH, "Unexpected response %d", response.code())));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserList> call, @NonNull Throwable t) {
                    call.cancel();
                    // Handle err
                    Timber.e(t);
                }
            });
        }
    }

    private void getReplacedText(EditText editText, String selected, int lastCursorPosition) {
        String toString = editText.getText().toString();
        StringBuilder builder = new StringBuilder(toString);
        int end = lastCursorPosition;
        int start = -1;
        lastCursorPosition--;
        while (lastCursorPosition >= 0) {
            char c = toString.charAt(lastCursorPosition);
            if (c == '@') {
                if (start == -1) start = lastCursorPosition;
                break;
            } else {
                start = lastCursorPosition;
                lastCursorPosition--;
            }
        }
        builder.replace(start, end, selected);
        editText.setText(builder.toString());
        editText.setSelection(builder.toString().length());
        searchUser = false;
    }

}