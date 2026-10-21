package com.app.alhawanajd.home;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.alhawanajd.BuildConfig;
import com.app.alhawanajd.ChatActivity;
import com.app.alhawanajd.home.eventbus.VideoZoomEvent;
import com.app.helper.callback.AdapterSpeedChanger;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
//import com.app.hiliteapp.BuildConfig;
import com.app.alhawanajd.R;
import com.app.alhawanajd.App;
import com.app.alhawanajd.MainActivity;
import com.app.alhawanajd.home.eventbus.FollowingFollowFollowing;
import com.app.alhawanajd.home.eventbus.FollowingUpdateGiftMessageCount;
import com.app.alhawanajd.home.eventbus.FollowingVideoFav;
import com.app.alhawanajd.home.eventbus.FollowingVideoLike;
import com.app.helper.NetworkReceiver;
import com.app.helper.OnVerticalScrollListener;
import com.app.helper.PermissionsUtils;
import com.app.helper.SpeedyLinearLayoutManager;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.helper.callback.OnOkCancelClickListener;
import com.app.helper.callback.PlayerRecyclerListener;
import com.app.helper.callback.ProfileImageClickListener;
import com.app.model.GetSet;
import com.app.model.HomeResponse;
import com.app.model.Request.HomeRequest;
import com.app.model.event.FollowingScrollEvent;
import com.app.utils.Constants;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.exoutils.ExoPlayerUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class FollowingVideoFragment extends Fragment implements AdapterItemClickListener, PlayerRecyclerListener, AdapterSpeedChanger {

    private static final String TAG = FollowingVideoFragment.class.getSimpleName();
    public static final int LOAD_MORE_MAX_LIMIT = 20;

    private Call<HomeResponse> call;
    private ApiInterface apiInterface;
    private Context mContext;

    ProgressBar loadMoreProgress;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView nullText;
    private ImageView nullImage;
    private RelativeLayout nullLay;
    private ImageView btnBack;
    private String from, from_key ="";
    private Boolean isViewPagerVisible = false;

    // Video Page
    ArrayList<HomeResponse.Result> homeApiResponse = new ArrayList<>();

    public FollowingExoPlayerRecyclerView followingExoplayerRecyclerView;

    private HomeVideoAdapter videoAdapter;

    Boolean isShowLoadMore = false;
    float Y_BUFFER = 0f; // 10;
    float preX = 0f;
    float preY = 0f;
    public SpeedyLinearLayoutManager customLinearLayoutManager;
    private SnapHelper snapHelper;


    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable videoRunnable = new Runnable() {
        @Override
        public void run() {
            if (isViewPagerVisible && homeApiResponse.size() > 0) {
                Timber.i("isViewPagerVisible enter");
                followingExoplayerRecyclerView.playVideo(false);
                followingExoplayerRecyclerView.setPlayControl(true);
                handler.removeCallbacks(videoRunnable);
            } else {
                handler.postDelayed(videoRunnable, 1000);
            }
        }
    };

    private boolean firstTime = true;
    private int lastVisibleItem = -1, lastOffset = -1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    public ArrayList<String> list_tagged_user_names = new ArrayList<>();
    public ArrayList<String> list_tagged_user_ids = new ArrayList<>();
    ProfileImageClickListener profileImageClickListener;

    private ActivityResultLauncher<String[]> storagePermissionResult;

    /*public FollowingVideoFragment() {
        // Required empty public constructor
    }*/

    public static FollowingVideoFragment newInstance(ProfileImageClickListener mProfileImageClickListener) {

        FollowingVideoFragment followingVideoFragment = new FollowingVideoFragment();

        followingVideoFragment.profileImageClickListener = mProfileImageClickListener;
        return followingVideoFragment;
    }


    // update follow icon
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingFollowFollowing event) {
        Bundle payload = new Bundle();
        for (int i = 0; i < homeApiResponse.size(); i++) {
            if (homeApiResponse.get(i).getPublisherId().equals(event.PublisherId)) {
                if (homeApiResponse.get(i).getFollowedUser()) {
                    homeApiResponse.get(i).setFollowedUser(false);
                    payload.putString("follow", "false");
                } else {
                    homeApiResponse.get(i).setFollowedUser(true);
                    payload.putString("follow", "true");
                }
                // EventBus.getDefault().post(new FollowingProfileUpdate(event.PublisherId));
                videoAdapter.notifyItemChanged(i, payload);
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingVideoLike event) {
        Timber.d("FollowingVideoLike => %s", new Gson().toJson(event));
        Bundle payload = new Bundle();
        for (int i = 0; i < homeApiResponse.size(); i++) {
            if (homeApiResponse.get(i).getVideoId().equals(event.VideoId)) {
                if (event.LikeStatus.equals("unliked")) {
                    homeApiResponse.get(i).setIsLiked(false);
                    payload.putString("likes_status", "false");

                } else {
                    homeApiResponse.get(i).setIsLiked(true);
                    payload.putString("likes_status", "true");

                }
                payload.putString("likes_count", (event.LikeCount));
                homeApiResponse.get(i).setLikes(event.LikeCount);
                videoAdapter.notifyItemChanged(i, payload);
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingUpdateGiftMessageCount event) {
        Timber.d("onMessageEvent: %s", event);

        for (int i = 0; i < homeApiResponse.size(); i++) {
            if (homeApiResponse.get(i).getVideoId().equals(event.VideoId)) {

                Bundle payload = new Bundle();
                if (event.giftOrMessage.equals("gift")) {
                    payload.putString("update_gift_count", event.Count);
                    homeApiResponse.get(i).setVideoGiftCounts(event.Count);

                } else if (event.giftOrMessage.equals("message")) {
                    payload.putString("update_video_comment_count", event.Count);
                    homeApiResponse.get(i).setVideoGiftCounts(event.Count);
                }
                videoAdapter.notifyItemChanged(i, payload);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingVideoFav event) {
        Timber.d("onMessageEvent: %s", new Gson().toJson(event));

        if (!event.isFav.equals("Sound")) {
            for (int i = 0; i < homeApiResponse.size(); i++) {
                if (homeApiResponse.get(i).getVideoId().equals(event.videoId)) {
                    Bundle payload = new Bundle();
                    if (event.isFav.equals("Report"))
                        homeApiResponse.get(i).setIsVideoReported(event.status);
                    else
                        homeApiResponse.get(i).setVideoIsFavorite(event.status);

                    payload.putString(event.isFav, String.valueOf(event.status));
                    videoAdapter.notifyItemChanged(i, payload);
                }
            }
        } else if (event.isFav.equals("Sound")) {
            for (int i = 0; i < homeApiResponse.size(); i++) {
                //Here videoId means soundId
                if (homeApiResponse.get(i).getSoundtracks().getSoundId().equals(event.videoId)) {
                    Bundle payload = new Bundle();
                    homeApiResponse.get(i).getSoundtracks().setIsFavorite(event.status);
                    payload.putString(event.isFav, String.valueOf(event.status));
                    videoAdapter.notifyItemChanged(i, payload);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingScrollEvent event) {
        followingExoplayerRecyclerView.smoothScrollToPosition(0);
        if (event.isRefresh()) {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VideoZoomEvent event) {
        int position = customLinearLayoutManager.findFirstVisibleItemPosition();
        View currentView = customLinearLayoutManager.findViewByPosition(position);
        if (currentView == null) return;

        final View mediaContainer = currentView.findViewById(R.id.mediaContainer);
        if (mediaContainer == null) return;

        float t = event.currentZoomScale;
        if (Float.isNaN(t)) t = 0f;
        t = Math.max(0f, Math.min(1f, t));

        // Convert shrink (0..1) → free height fraction (1..0)
        float free = 1f - t;

        // When any shrink is applied, ensure FIT so horizontal videos don't fill screen
        try {
            if (t > 0f) {
                if (followingExoplayerRecyclerView != null) {
                    followingExoplayerRecyclerView.setExoPlayView(View.VISIBLE);
                }
                // Delegate to recycler to set FIT on current player view if needed
                // (Your recycler already switches modes on ready; this enforces FIT while shrunk.)
                // Optionally, if you have direct access to StyledPlayerView, setResizeMode(FIT) here.
            }
        } catch (Throwable ignored) {}

        // Anchor scaling from the top so the video stays aligned to the top edge
        mediaContainer.setPivotX(mediaContainer.getWidth() / 2f);
        mediaContainer.setPivotY(0f);

        // Scale uniformly so height ~= available space above the sheet
        mediaContainer.setScaleX(free);
        mediaContainer.setScaleY(free);
        mediaContainer.setTranslationY(0f);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_FROM)) {
            from = getArguments().getString(Constants.TAG_FROM);
        }

        if (getArguments() != null && getArguments().containsKey("from_key")) {
            from_key = getArguments().getString("from_key");
        }
        EventBus.getDefault().register(this);
        firstPositionVideo("offset");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_following_video, container, false);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Y_BUFFER = ViewConfiguration.get(mContext).getScaledPagingTouchSlop();
        initPermission();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        lastOffset = 0;
        // TODO: 23/10/21 @VishnuKumar
        followingExoplayerRecyclerView.setMediaObjects(homeApiResponse);
        videoAdapter = new HomeVideoAdapter(getActivity(), homeApiResponse, initGlide(), Constants.TAG_FOLLOWING, this, from_key,this);
        followingExoplayerRecyclerView.setAdapter(videoAdapter);
        customLinearLayoutManager = new SpeedyLinearLayoutManager(mContext);
        followingExoplayerRecyclerView.setLayoutManager(customLinearLayoutManager);

        firstTime = true;

        if (GetSet.getUserId() != null) {
            swipeRefresh(true);
        }

        OnVerticalScrollListener scrollListener = new OnVerticalScrollListener() {

            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();
                /*Timber.tag(TAG).d("onScrolledToBottom: %d", homeApiResponse.size());
                Timber.tag(TAG).d("onScrolledToBottom: %d", lastOffset);
                if (homeApiResponse.size() <= lastOffset) return;
                else lastOffset = homeApiResponse.size();

                if (isShowLoadMore) {
                    firstTime = false;
                    getForYouDataFromApi(lastOffset, "loadmore");
                }*/
                if (isShowLoadMore && !isLoading && !isLastPage
                        && !swipeRefreshLayout.isRefreshing()
                        && homeApiResponse.size() > 0) {
                    getFollowingDataFromApi(lastOffset, "play");
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                        newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    lastVisibleItem = customLinearLayoutManager.findLastVisibleItemPosition();
                    Timber.d("onScrollStateChanged: %s, %s", lastVisibleItem, lastOffset);
                    if (lastVisibleItem >= (homeApiResponse.size() - 1) - 5 && isShowLoadMore && !isLoading && !isLastPage) {
                        getFollowingDataFromApi(lastOffset, "play");
                    }
                }

                /*if (icFavHeart != null && icFavHeart.isAnimating()) {
                    Timber.d("onScrollStateChanged: animation stop enter");
                    *//*icFavHeart.setVisibility(View.GONE);
                    icFavHeart.clearAnimation();*//*
                    //  icFavHeart.cancelAnimation();
                    //icFavHeart.getAnimation().cancel();
                    // icFavHeart.
                    // ();
                    //icFavHeart.removeAllLottieOnCompositionLoadedListener();
                }*/
            }
        };

        followingExoplayerRecyclerView.addOnScrollListener(scrollListener);

        snapHelper = new PagerSnapHelper() {
            /* Override to disable the snap on loading view */
            @Nullable
            @org.jetbrains.annotations.Nullable
            @Override
            public View findSnapView(RecyclerView.LayoutManager layoutManager) {
                int position = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                Timber.d("SnapHelper: findSnapView %d, %d", homeApiResponse.size(), position);
                if (position == homeApiResponse.size()/* && false*/) {
                    return null;
                } else {
                    if (profileImageClickListener != null && homeApiResponse.size() > 0) {
                        profileImageClickListener.onUserClicked(false, new FollowingProfileUpdate(homeApiResponse.get(position == -1 ? 0 : position).getPublisherId(),
                                homeApiResponse.get(position == -1 ? 0 : position).getPublisherImage(), ""));
                    }
                    return super.findSnapView(layoutManager);
                }
            }
        };
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
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), x.getKey())) {
                                storagePermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(mContext, new OnOkCancelClickListener() {
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
                                }, mContext.getString(R.string.storage_error));
                            }
                            break;
                        }
                    }
                }

            }
        });
    }

    private void firstPositionVideo(String type) {
        if (firstTime) {
            new Handler().postDelayed(() -> {
                if (type.equals("play")) followingExoplayerRecyclerView.setPlayControl(true);
                else followingExoplayerRecyclerView.setPlayControl(false);
            }, 1100);
            firstTime = false;
        }
    }

    private void initView(@NotNull View rootView) {
        followingExoplayerRecyclerView = rootView.findViewById(R.id.FollowingVideoRecyclerview);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        loadMoreProgress = rootView.findViewById(R.id.loadMoreProgress);
        btnBack = rootView.findViewById(R.id.btnBack);
        followingExoplayerRecyclerView.setListener(this);
        // Null Layout
        nullText = rootView.findViewById(R.id.nullText);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullLay = rootView.findViewById(R.id.nullLay);
        nullText.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite));
        nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_videos));
        nullLay.setVisibility(View.GONE);
        if (from != null && from.equals(Constants.TAG_VIDEO)) {
            btnBack.setVisibility(View.VISIBLE);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }
            });
        } else {
            btnBack.setVisibility(View.GONE);
        }

        /*CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.MATCH_PARENT);
        params.bottomMargin = AppUtils.dpToPx(context, HomeFragment.BOTTOM_MARGIN);
        nullLay.setGravity(Gravity.CENTER);
        nullLay.setLayoutParams(params);*/

        swipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        //     swipeRefresh(true);


        swipeRefreshLayout.setOnRefreshListener(() -> {
            homeApiResponse.clear();
            videoAdapter.notifyDataSetChanged();
            swipeRefresh(true);
            followingExoplayerRecyclerView.onPausePlayer();
//            EventBus.getDefault().post(new FollowingSwipePrevent(true));
            isViewPagerVisible = true;
        });

    }


    private void getFollowingDataFromApi(int finalLastOffset, String type) {
        if (finalLastOffset > 0 && (isLoading || isLastPage)) {
            return;
        }

        nullLay.setVisibility(View.GONE);

        if (NetworkReceiver.isConnected()) {
            if (!swipeRefreshLayout.isRefreshing()) {
                videoAdapter.showLoader();
            }
            HomeRequest homeRequest = new HomeRequest();
            homeRequest.setUserId(GetSet.getUserId());
            homeRequest.setToken(GetSet.getAuthToken());
            homeRequest.setType(Constants.TAG_FOLLOWING);
            homeRequest.setLimit(String.valueOf(LOAD_MORE_MAX_LIMIT));
            homeRequest.setOffset(String.valueOf(finalLastOffset));

            if (finalLastOffset == 0) {
                isLastPage = false;
                if (call != null) {
                    call.cancel();
                    call = null;
                }
            }
            isLoading = true;
            call = apiInterface.getFollowingHomeData(homeRequest);
            call.enqueue(new Callback<HomeResponse>() {
                @Override
                public void onResponse(@NotNull Call<HomeResponse> call, @NotNull Response<HomeResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        hideLoader();
                        nullLay.setVisibility(View.GONE);
                        int resultSize = response.body().getResult() != null ? response.body().getResult().size() : 0;
                        int insertedCount = resultSize;
                        isShowLoadMore = resultSize > 0;

                        if (swipeRefreshLayout.isRefreshing()) {
                            followingExoplayerRecyclerView.resetPlayingPosition();
                            homeApiResponse.clear();
                            homeApiResponse.addAll(response.body().getResult());
                            videoAdapter.notifyDataSetChanged();
                            handler.removeCallbacks(videoRunnable);
                            if (getActivity() != null) {
                                if (getActivity() instanceof MainActivity) {
                                    MainActivity activity = (MainActivity) getActivity();
                                    if (activity.viewPager != null && activity.viewPager.getCurrentItem() == 0) {
                                        if (activity.getHomeFragment() != null && activity.getHomeFragment().homeFragViewPager != null &&
                                                activity.getHomeFragment().homeFragViewPager.getCurrentItem() == 1) {
                                            handler.postDelayed(videoRunnable, 1000);
                                        }
                                    }
                                }
                            }
                        } else {
                            hideLoader();
                            int tempPosition = homeApiResponse.size();
                            java.util.ArrayList<HomeResponse.Result> toAdd = new java.util.ArrayList<>();
                            if (response.body().getResult() != null) {
                                for (HomeResponse.Result item : response.body().getResult()) {
                                    if (item == null || TextUtils.isEmpty(item.getVideoId())) {
                                        toAdd.add(item);
                                        continue;
                                    }
                                    boolean exists = false;
                                    for (HomeResponse.Result old : homeApiResponse) {
                                        if (item.getVideoId().equals(old.getVideoId())) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) toAdd.add(item);
                                }
                            }
                            homeApiResponse.addAll(toAdd);
                            insertedCount = toAdd.size();
                            videoAdapter.notifyItemRangeInserted(tempPosition, toAdd.size());
                            if (lastVisibleItem != -1 && lastVisibleItem == tempPosition) {
                                followingExoplayerRecyclerView.scrollToPosition(tempPosition);
                            }
                        }
                        if (App.isCacheEnabled) {
                            for (HomeResponse.Result result : response.body().getResult()) {
                                ExoPlayerUtils.cacheVideo(result.getPlaybackUrl(), mContext);
                            }
                        }

                        // offset 0 → 20 → 40
                        if (resultSize > 0) {
                            lastOffset = finalLastOffset + LOAD_MORE_MAX_LIMIT;
                            isShowLoadMore = resultSize >= LOAD_MORE_MAX_LIMIT;
                            isLastPage = !isShowLoadMore;
                        } else {
                            isShowLoadMore = false;
                            isLastPage = true;
                        }
                        if (insertedCount == 0 && finalLastOffset > 0) {
                            isShowLoadMore = false;
                            isLastPage = true;
                        }
                    }
                    if (swipeRefreshLayout.isRefreshing() && homeApiResponse.size() > 0) {
                        snapHelper.attachToRecyclerView(followingExoplayerRecyclerView);
                    }
                    /*if (homeApiResponse.size() == 0) {
                        EventBus.getDefault().post(new homeForYouSwipePrevent(true, ""));
                    } else {
                        EventBus.getDefault().post(new homeForYouSwipePrevent(false, ""));
                    }*/
                    isLoading = false;
                    swipeRefresh(false);
                    hideLoader();
                    setNullLay(false);
                }

                @Override
                public void onFailure(@NotNull Call<HomeResponse> call, @NotNull Throwable t) {
                    Timber.e("onFailure: %s", t.getMessage());
                    if (!call.isCanceled()) {
                        isLoading = false;
                        hideLoader();
                        swipeRefresh(false);
                        if (homeApiResponse.size() == 0) {
//                        EventBus.getDefault().post(new FollowingSwipePrevent(true));
                            setNullLay(true);
                        }
                    }
                }
            });

        } else {
            isLoading = false;
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getString(R.string.no_internet_connection));
            swipeRefresh(false);
        }
    }

    private void hideLoader() {
        if (videoAdapter != null) {
            videoAdapter.hideLoader();
        }
        if (loadMoreProgress != null) {
            loadMoreProgress.setVisibility(View.GONE);
        }
    }

    private void setNullLay(boolean isServerError) {
        if (!isServerError) {
            if (GetSet.getUserId() != null && homeApiResponse.size() == 0) {
                nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_videos));
                nullText.setText(mContext.getString(R.string.no_broadcast_found));
                followingExoplayerRecyclerView.setVisibility(View.GONE);
                nullLay.setVisibility(View.VISIBLE);
            } else {
                nullLay.setVisibility(View.GONE);
                followingExoplayerRecyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            if (homeApiResponse.size() == 0) {
                followingExoplayerRecyclerView.setVisibility(View.GONE);
                nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_videos));
                nullText.setText(mContext.getString(R.string.unable_to_reach_server));
                nullLay.setVisibility(View.VISIBLE);
            } else {
                followingExoplayerRecyclerView.setVisibility(View.VISIBLE);
                nullLay.setVisibility(View.GONE);
            }
        }
    }

    /*@Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);

    }*/


    private RequestManager initGlide() {
        RequestOptions options = new RequestOptions();
        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }


    /*private void preDownloadVideos() {

        for (FollowingHomeResponse.Result home : homeApiResponse) {
            if (home.getPlaybackUrl() != null && !home.getPlaybackUrl().isEmpty()) {

                boolean isCached = mMediaLoader.isCached(home.getPlaybackUrl());
                if (!isCached && getContext() != null)
                    DownloadManager.getInstance(getContext()).enqueue(new DownloadManager.Request(home.getPlaybackUrl()), this);

            }
        }

    }*/

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            firstTime = true;
            lastOffset = 0;
            getFollowingDataFromApi(0, "play");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.MUSIC_REQUEST_CODE) {
            followingExoplayerRecyclerView.setPlayControl(true);
        } else if (requestCode == Constants.HASHTAG_REQUEST_CODE) {
            followingExoplayerRecyclerView.setPlayControl(true);
        } else if (requestCode == Constants.COMMENT_MUSIC_REQUEST_CODE) {
            followingExoplayerRecyclerView.setPlayControl(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.i("resume called");
        isViewPagerVisible = true;
//        followingExoplayerRecyclerView.setPlayControl(videoAdapter.commentListDialog == null || !videoAdapter.commentListDialog.isShowing());
        handler.post(videoRunnable);
        if (!NetworkReceiver.isConnected()) {
            if (swipeRefreshLayout.isRefreshing()) swipeRefresh(false);
        }
    }

    @Override
    public void onPause() {
        Timber.d("onPause: ");
        super.onPause();
        followingExoplayerRecyclerView.setPlayControl(false);
        isViewPagerVisible = false;
        handler.removeCallbacks(videoRunnable);
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

    @Override
    public void onItemClicked(Object o, int position, String type) {
        switch (type) {
            case Constants.TAG_COMMENT: {
                //Oct3 I commented below line for QA told dont pause the video as per client comment
                //followingExoplayerRecyclerView.setPlayControl(false);
                break;
            }
            case Constants.TAG_PLAYBACK: {
                boolean playBack = (boolean) o;
                followingExoplayerRecyclerView.setPlayControl(playBack);
                break;
            }
            case Constants.TAG_SINGLE_TAP:
                followingExoplayerRecyclerView.togglePlay();
                break;
            case Constants.TAG_GIFT:
                break;
            case Constants.TAG_LIKE:
//                heartFun(position);
                break;
            case Constants.TAG_DOWNLOAD:
            case Constants.TAG_HASHTAGS:
            case Constants.TAG_REPORT: {
                break;
            }
            case Constants.TAG_PERMISSION: {
                requestStoragePermissions();
                break;
            }
            case Constants.TAG_SHARE: {
                HomeResponse.Result item = (HomeResponse.Result) o;
//                shareVideoDialog(item.getShareLink());
                break;
            }
            case Constants.TAG_AUDIO: {

            }
            case Constants.TAG_FOLLOW: {
                break;
            }
            case Constants.TAG_PROFILE: {
                profileImageClickListener.onUserClicked(true, null);
                break;
            }
            case Constants.TAG_FOLLOWFROM_OTHERS: {
                HomeResponse.Result result = ((HomeResponse.Result) o);
                if (GetSet.getUserId() != null) {
                    String userType = (result.getPublisherId()).equals(GetSet.getUserId()) ? "" : result.getPublisherId();
                    profileImageClickListener.onUserClicked(false, new FollowingProfileUpdate(result.getPublisherId(), result.getPublisherImage(), userType));
                }
                break;
            }
            case Constants.TAG_DELETE: {
                homeApiResponse.remove(position);
                videoAdapter.notifyItemRemoved(position);
                videoAdapter.notifyItemRangeChanged(position, homeApiResponse.size());
                break;
            }

            case Constants.TAG_REPOST:{
                Log.d("REPOST", "o=" + o + " position=" + position + " type=" + type);

                Log.d(TAG, "onItemClicked: "+type);
//                private void openRepostEditSheet(String videoId, String repostText) {

//                    Dialog dialog = new Dialog(mContext, android.R.style.Theme_Black_NoTitleBar);
//                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                    dialog.setContentView(R.layout.repost_edit_sheet);
//
//                    Window window = dialog.getWindow();
//                    if (window != null) {
//                        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                        window.setGravity(Gravity.CENTER);   // <-- SHOW IN CENTER LIKE TIKTOK
//                        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                        window.setDimAmount(0.0f);           // No background dim (TikTok style)
//                    }
//
//                    EditText repostEditText = dialog.findViewById(R.id.repostEditText);
//                    TextView saveBtn = dialog.findViewById(R.id.repostAddBtn);
//
////                    repostEditText.setText(repostText);
//
//                    saveBtn.setOnClickListener(v -> {
//                        String updatedText = repostEditText.getText().toString().trim();
//                        dialog.dismiss();
//                    });
//
//                    dialog.setCanceledOnTouchOutside(true);
//                    dialog.show();
//                }
//            }
//                openRepostDialog("videoId");
//                opendeleteDialog();
                openRepostEditSheet();
            }
        }

    }
    private void openRepostEditSheet() {

        Dialog dialog = new Dialog(requireActivity(), android.R.style.Theme_Black_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.repost_edit_sheet);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0f);
        }

        EditText repostEditText = dialog.findViewById(R.id.repostEditText);
        TextView saveBtn = dialog.findViewById(R.id.repostAddBtn);

        saveBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void opendeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(getString(R.string.notification_del_acc));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
//                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(mContext, R.font.font_regular);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);

    }
    private void openRepostDialog(String videoId) {

        Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.repost_edit_sheet);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.0f);
        }

        dialog.setCanceledOnTouchOutside(true);

        dialog.show();
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            storagePermissionResult.launch(PermissionsUtils.READ_STORAGE_PERMISSION);
        } else {
            storagePermissionResult.launch(PermissionsUtils.READ_WRITE_PERMISSIONS);
        }
    }

    @Override
    public void onProfileUpdate(FollowingProfileUpdate profileUpdate) {
        if (profileImageClickListener != null) {
            profileImageClickListener.onUserClicked(false, profileUpdate);
        }
    }

    @Override
    public void onItemPlaybackSpeed(Object o, int position, String videoId, float speed, String type) {

    }

    @Override
    public void onItemPlaybackSpeed(Object o, int position, String videoId, String speed, String type) {

    }

    @Override
    public void onItemReposted(Object o, int position, String videoId, String action, String desc) {

    }


}