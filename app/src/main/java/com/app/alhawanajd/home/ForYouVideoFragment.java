package com.app.alhawanajd.home;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.alhawanajd.home.eventbus.VideoZoomEvent;
import com.app.helper.callback.AdapterSpeedChanger;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.app.alhawanajd.BuildConfig;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.alhawanajd.App;
import com.app.alhawanajd.LoginActivity;
import com.app.alhawanajd.MainActivity;
import com.app.alhawanajd.home.eventbus.ForYouFollowFollowing;
import com.app.alhawanajd.home.eventbus.ForYouUpdateGiftMessageCount;
import com.app.alhawanajd.home.eventbus.ForYouVideoFav;
import com.app.alhawanajd.home.eventbus.ForYouVideoLike;
import com.app.helper.LocaleManager;
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
import com.app.model.UserList;
import com.app.model.event.ForYouScrollEvent;
import com.app.model.event.VideoDeleteEvent;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.exoutils.ExoPlayerUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ForYouVideoFragment extends Fragment implements AdapterItemClickListener, PlayerRecyclerListener, AdapterSpeedChanger {

    private static final String TAG = ForYouVideoFragment.class.getSimpleName();
    private Context mContext;
    public static final int LOAD_MORE_MAX_LIMIT = 20;
    public ForYouExoPlayerRecyclerView exoplayerRecyclerViewForYou;
    private Call<HomeResponse> apiCall;
    public SpeedyLinearLayoutManager customLinearLayoutManager;
    private String from_key = "";

    // Video Page
    ArrayList<HomeResponse.Result> homeApiResponse = new ArrayList<>();

    ArrayList<UserList.Result> liveUser = new ArrayList<>();

    boolean tagging = true, scrollLoadMore = false, apiLoadMore = false, hasInterest = true;
    ProfileImageClickListener profileImageClickListener;
    Handler handler = new Handler();
    ProgressBar loadMoreProgress;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ApiInterface apiInterface;
    private ImageView btnBack;
    private TextView nullText;
    private ImageView nullImage;
    private RelativeLayout nullLay;
    private HomeVideoAdapter videoAdapter;
    private Boolean isViewPagerVisible = false;
    private String from = null;
    public boolean isDeleted = false;
    public boolean isscrollcheck = false;
    private SnapHelper snapHelper;
    Boolean isShowLoadMore = false;
    Runnable videoRunnable = new Runnable() {
        @Override
        public void run() {
            if (isViewPagerVisible && homeApiResponse.size() > 0) {
                Timber.i("isViewPagerVisible enter");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isscrollcheck){
                            exoplayerRecyclerViewForYou.playVideo(false);
                        }else{
                            exoplayerRecyclerViewForYou.playVideo(true);
                        }


                    }
                }, 1000);

//               exoplayerRecyclerViewForYou.setPlayControl(true);
                exoplayerRecyclerViewForYou.setPlayControl(videoAdapter.commentListDialog == null || !videoAdapter.commentListDialog.isShowing());
                handler.removeCallbacks(videoRunnable);
            } else {
                handler.postDelayed(videoRunnable, 1000);
            }
        }
    };

    // TODO: 23/10/21 @VishnuKumar
    public SpeedyLinearLayoutManager layoutManager;
    private String apiType = null, tagId = null, selectedVideoId = null;

    public static ArrayList<String> updatedVideoList = new ArrayList<>();
    //oct 3
    // private int lastVisibleItem = -1, lastOffset = -1, videoPosition = -1;

    private int lastVisibleItem = -1;
    private int lastOffset = 0;
    private int videoPosition = -1;

    private ProgressDialog progressDialog;
    private ActivityResultLauncher<String[]> storagePermissionResult;
    private ActivityResultLauncher<String[]> gallerypermissionResult;
    private ActivityResultLauncher<Intent> editResultLauncher;

    SharedPreferences watchPref;

    // 🔹 pagination variables
    private boolean isLoading = false;
    private boolean isLastPage = false;

    public static ForYouVideoFragment newInstance(ProfileImageClickListener mProfileImageClickListener) {
        ForYouVideoFragment forYouVideoFragment = new ForYouVideoFragment();
        forYouVideoFragment.profileImageClickListener = mProfileImageClickListener;
        return forYouVideoFragment;
    }

    // EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ForYouFollowFollowing event) {
        Timber.d("ForYouFollowFollowing isFollow=>  %s", event.isFollow);

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
                //  EventBus.getDefault().post(new ForYouProfileUpdate(event.PublisherId));
                videoAdapter.notifyItemChanged(i, payload);
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ForYouVideoLike event) {
        Timber.d("ForYouVideoLike =>  %s", event);

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
    public void onMessageEvent(ForYouUpdateGiftMessageCount event) {
        Timber.d("onMessageEvent: %s", event);

        for (int i = 0; i < homeApiResponse.size(); i++) {
            if (homeApiResponse.get(i).getVideoId().equals(event.videoId)) {


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
    public void onMessageEvent(ForYouVideoFav event) {
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
        } else /*if (event.isFav.equals("Sound"))*/ {
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
    public void onMessageEvent(ForYouScrollEvent event) {
        exoplayerRecyclerViewForYou.smoothScrollToPosition(0);
//        layoutManager.scrollToPositionWithOffset(0, 0);
        if (event.isRefresh()) {

        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VideoZoomEvent event) {
        Log.e(TAG,"SIVASHANKARI : 1" + event.availableHeightPx);
        if (customLinearLayoutManager == null) return;
        int pos = customLinearLayoutManager.findFirstVisibleItemPosition();
        View currentView = customLinearLayoutManager.findViewByPosition(pos);
        if (currentView == null) return;

        View mediaContainer = currentView.findViewById(R.id.mediaContainer);
        if (mediaContainer == null) return;

        int available = Math.max(0, event.availableHeightPx);
        ViewGroup.LayoutParams lp = mediaContainer.getLayoutParams();
        lp.height = available > 0 ? available : ViewGroup.LayoutParams.MATCH_PARENT;
        mediaContainer.setLayoutParams(lp);

        StyledPlayerView playerView = null;
        if (mediaContainer instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) mediaContainer;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View c = vg.getChildAt(i);
                if (c instanceof StyledPlayerView) {
                    playerView = (StyledPlayerView) c;
                    break;
                }
            }
        }

        if (playerView != null) {
            Log.e(TAG,"SIVASHANKARI : 1 playernot null" );
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    available > 0 ? available : ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.TOP
            );
            playerView.setLayoutParams(flp);
        }
    }





    private void shrinkVideo(View mediaContainer, View mediaCover) {
        if (mediaContainer == null) return;

        mediaContainer.animate()
                .scaleX(0.55f)       // shrink to ~55%
                .scaleY(0.55f)
                .translationY(-App.dpToPx(requireContext(), 160)) // move up slightly
                .setDuration(300)
                .start();

        if (mediaCover != null) {
            mediaCover.animate()
                    .scaleX(0.55f)
                    .scaleY(0.55f)
                    .translationY(-App.dpToPx(requireContext(), 160))
                    .setDuration(300)
                    .start();
        }
    }


    private void expandVideo(View mediaContainer, View mediaCover) {
        if (mediaContainer == null) return;

        mediaContainer.animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0)
                .setDuration(300)
                .start();

        if (mediaCover != null) {
            mediaCover.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY(0)
                    .setDuration(300)
                    .start();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            apiType = bundle.getString(Constants.TAG_TYPE);
            Timber.tag(TAG).d("onCreate apiType: %s", apiType);
            if (bundle.containsKey(Constants.TAG_FROM)) {
                from = bundle.getString(Constants.TAG_FROM);
                Timber.tag(TAG).d("onCreate from: %s", from);
            }

            if (bundle.containsKey(Constants.TAG_POSITION)) {
                videoPosition = bundle.getInt(Constants.TAG_POSITION);
                lastOffset = videoPosition;
                Timber.tag(TAG).d("onCreate videoPosition: %s", videoPosition);
                Timber.tag(TAG).d("onCreate lastOffset0: %s", lastOffset);
            } else {
                lastOffset = 0;
                Timber.tag(TAG).d("onCreate lastOffset1: %s", lastOffset);
            }

            if (bundle.containsKey(Constants.TAG_VIDEO_ID)) {
                tagId = bundle.getString(Constants.TAG_TAGID);
                selectedVideoId = bundle.getString(Constants.TAG_VIDEO_ID);
                Timber.tag(TAG).d("onCreate tagId: %s", tagId);
            }

            if (bundle.containsKey("from_key")) {
                from_key = bundle.getString("from_key");
            }
        }
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        //  Toast.makeText(getActivity(),""+apiType,Toast.LENGTH_LONG).show();
        watchPref = getContext().getSharedPreferences("WatchVideo", MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_foryou_video, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);


        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Loading..");
        progressDialog.setIndeterminate(true);

        updateWatchVideo();
        exoplayerRecyclerViewForYou = view.findViewById(R.id.videoRecyclerview);
        exoplayerRecyclerViewForYou.setMediaObjects(homeApiResponse);
        exoplayerRecyclerViewForYou.setListener(this);

        videoAdapter = new HomeVideoAdapter(getActivity(), homeApiResponse, initGlide(), apiType, this, from, from_key,this);
        layoutManager = new SpeedyLinearLayoutManager(getContext());
        exoplayerRecyclerViewForYou.setLayoutManager(layoutManager);
        exoplayerRecyclerViewForYou.setAdapter(videoAdapter);

        customLinearLayoutManager = new SpeedyLinearLayoutManager(mContext);
        exoplayerRecyclerViewForYou.setLayoutManager(customLinearLayoutManager);

        // Disable RecyclerView item change animations to prevent flicker when updating likes/comments
        // IMPORTANT: This must be AFTER setting the final LayoutManager
        RecyclerView.ItemAnimator animator = exoplayerRecyclerViewForYou.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        initPermission();
        initResultLauncher();

     /*   OnVerticalScrollListener scrollListener = new OnVerticalScrollListener() {
            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();

                if (homeApiResponse.size() < lastOffset) return;
                else lastOffset = homeApiResponse.size();
                scrollLoadMore = true;

                exoplayerRecyclerViewForYou.stopScroll();
                if (scrollLoadMore){
                    getForYouDataFromApi(lastOffset);
                }
            }

            @Override
            public void onScrolledDown(int dx, int dy) {
                super.onScrolledDown(dx, dy);
//                Timber.d("onScrolledDown: dx: %d dy: %d", dx, dy);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                EventBus.getDefault().post(new VideoZoomEvent(0));  //to reset the hide view in activity and fragments

                int visibleIndex = customLinearLayoutManager.findFirstVisibleItemPosition();
                int visibleIndexlast = customLinearLayoutManager.findLastVisibleItemPosition();
                Timber.d("onScrollStateChanged: %s", swipeRefreshLayout.isRefreshing());
                Timber.d("onScrollStateChanged_Index: %s", visibleIndex);
                Timber.d("onScrollStateChanged_visibleIndexlast: %s", visibleIndexlast);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                        newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                    if (apiType != null && apiType.equals(Constants.TAG_FOR_YOU)) {
                        videoAdapter.showLoader();
                        if (!swipeRefreshLayout.isRefreshing()) {
                            isscrollcheck = true;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    videoAdapter.showLoader();
                                    if (visibleIndexlast == 1){
                                        videoAdapter.hideLoader();
                                        getForYouDataFromApi(lastOffset);
                                    }
                                }
                            }, 1000);
                        }
                    }
                }

                *//*if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
\                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                    Timber.d("onScrollStateChanged: %s, %s", lastVisibleItem, lastOffset);
                    if (apiType != null && apiType.equals(Constants.TAG_FOR_YOU)) {
                        if (homeApiResponse.size() >= 10) {
                            if (lastVisibleItem == (homeApiResponse.size() - 1) - 5 && scrollLoadMore) {
                                getForYouDataFromApi(lastOffset);
                            }
                        } else {
                            if (lastVisibleItem == homeApiResponse.size() - 1) {
                                getForYouDataFromApi(lastOffset);
                            }
                        }
                    } else {
                        if (lastVisibleItem == (homeApiResponse.size() - 1) - 5 && scrollLoadMore) {
                            getForYouDataFromApi(lastOffset);
                        }
                    }
                }*//*

            }
        };
        exoplayerRecyclerViewForYou.addOnScrollListener(scrollListener);
*/

        OnVerticalScrollListener scrollListener = new OnVerticalScrollListener() {

            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();
                Log.e(TAG,"VIDEO_DBG : onScrolledToBottom " );
                if (scrollLoadMore && !isLoading && !isLastPage
                        && !swipeRefreshLayout.isRefreshing()
                        && homeApiResponse.size() > 0) {
                    getForYouDataFromApi(lastOffset);
                }
            }

            @Override
            public void onScrolledDown(int dx, int dy) {
                super.onScrolledDown(dx, dy);
                Log.e(TAG,"VIDEO_DBG : onScrolledDown " );
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.e(TAG,"VIDEO_DBG : onScrollStateChanged " );

                exoplayerRecyclerViewForYou.setPlayBackSpeed(false,1.0f);
                EventBus.getDefault().post(new VideoZoomEvent(0)); // reset hide views

                SharedPreferences prefs = mContext.getSharedPreferences("speed_prefs", Context.MODE_PRIVATE);
                prefs.edit().putFloat("speed_value", 1.0f).apply();

                int firstVisible = customLinearLayoutManager.findFirstVisibleItemPosition();
                int lastVisible = customLinearLayoutManager.findLastVisibleItemPosition();
                int totalCount = layoutManager.getItemCount();

                Timber.d("ScrollState: %s, first: %s, last: %s, total: %s",
                        newState, firstVisible, lastVisible, totalCount);

           /*     if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                        newState == RecyclerView.SCROLL_STATE_SETTLING) {

                    // Trigger load more if near end
                    if (scrollLoadMore && lastVisible >= totalCount - 2 && !swipeRefreshLayout.isRefreshing()) {
                        scrollLoadMore = false; // prevent multiple calls
                        videoAdapter.showLoader();
                        getForYouDataFromApi(lastOffset);
                    }
                }*/

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                        newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                    Timber.d("onScrollStateChanged: %s, %s", lastVisibleItem, lastOffset);
                    if (apiType != null && apiType.equals(Constants.TAG_FOR_YOU)) {
                        if (homeApiResponse.size() >= 10) {
                            if (lastVisibleItem >= (homeApiResponse.size() - 1) - 5 && scrollLoadMore && !isLoading && !isLastPage) {
                                getForYouDataFromApi(lastOffset);
                            }
                        } else {
                            if (lastVisibleItem >= homeApiResponse.size() - 1 && !isLoading && !isLastPage) {
                                getForYouDataFromApi(lastOffset);
                            }
                        }
                    } else {
                        if (lastVisibleItem >= (homeApiResponse.size() - 1) - 5 && scrollLoadMore && !isLoading && !isLastPage) {
                            getForYouDataFromApi(lastOffset);
                        }
                    }
                }
            }
        };
        exoplayerRecyclerViewForYou.addOnScrollListener(scrollListener);


        snapHelper = new PagerSnapHelper() {

            // Increase threshold values - adjust these as needed
            private static final int FLING_THRESHOLD_MULTIPLIER = 1; // Default is usually 1
            private static final float SCROLL_THRESHOLD = 0.25f; // 60% of item height/width needed to trigger snap


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


            /*
            @Override
            public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

                // Get current position more reliably
                int currentPosition = getCurrentVisiblePosition(linearLayoutManager);
                int itemCount = linearLayoutManager.getItemCount();

                if (itemCount == 0 || currentPosition == RecyclerView.NO_POSITION) {
                    return RecyclerView.NO_POSITION;
                }

                // Determine orientation and velocity
                boolean isVertical = linearLayoutManager.getOrientation() == LinearLayoutManager.VERTICAL;
                int velocity = isVertical ? velocityY : velocityX;

                // Calculate minimum velocity threshold
                int minVelocity = (int) (layoutManager.getHeight() * FLING_THRESHOLD_MULTIPLIER);
                if (!isVertical) {
                    minVelocity = (int) (layoutManager.getWidth() * FLING_THRESHOLD_MULTIPLIER);
                }

                // For low velocity, check scroll distance
                if (Math.abs(velocity) < minVelocity) {
                    View currentView = linearLayoutManager.findViewByPosition(currentPosition);
                    if (currentView == null) {
                        return currentPosition;
                    }

                    // Calculate scroll ratio
                    int viewSize = isVertical ? currentView.getHeight() : currentView.getWidth();
                    int viewOffset = isVertical ? Math.abs(currentView.getTop()) : Math.abs(currentView.getLeft());
                    float scrollRatio = (float) viewOffset / viewSize;

                    if (scrollRatio < SCROLL_THRESHOLD) {
                        return currentPosition; // Stay on current item
                    }
                }

                // Determine target position - FORCE single step movement
                int targetPosition = currentPosition;

                if (velocity > minVelocity) {
                    // Forward/down - go to next video
                    targetPosition = Math.min(currentPosition + 1, itemCount - 1);
                } else if (velocity < -minVelocity) {
                    // Backward/up - go to previous video (ONLY one step back)
                    targetPosition = Math.max(currentPosition - 1, 0);
                }

                // Log for debugging
                Timber.d("SnapHelper: velocity=%d, current=%d, target=%d", velocity, currentPosition, targetPosition);

                return targetPosition;
            }

            // Helper method to get current position more reliably
            private int getCurrentVisiblePosition(LinearLayoutManager layoutManager) {
                // Try to get the most visible item
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                int lastVisible = layoutManager.findLastVisibleItemPosition();

                if (firstVisible == RecyclerView.NO_POSITION) {
                    return RecyclerView.NO_POSITION;
                }

                // For single item per page (PagerSnapHelper behavior)
                // Return the first completely visible, or first visible if none completely visible
                int firstCompletelyVisible = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (firstCompletelyVisible != RecyclerView.NO_POSITION) {
                    return firstCompletelyVisible;
                }

                // If no completely visible item, find the most visible one
                View mostVisibleView = null;
                int mostVisiblePosition = firstVisible;
                int maxVisibleArea = 0;

                for (int i = firstVisible; i <= lastVisible; i++) {
                    View view = layoutManager.findViewByPosition(i);
                    if (view != null) {
                        int visibleArea = getVisibleArea(view, layoutManager);
                        if (visibleArea > maxVisibleArea) {
                            maxVisibleArea = visibleArea;
                            mostVisiblePosition = i;
                            mostVisibleView = view;
                        }
                    }
                }

                return mostVisiblePosition;
            }

            // Calculate visible area of a view
            private int getVisibleArea(View view, LinearLayoutManager layoutManager) {
                boolean isVertical = layoutManager.getOrientation() == LinearLayoutManager.VERTICAL;

                if (isVertical) {
                    int top = Math.max(0, view.getTop());
                    int bottom = Math.min(layoutManager.getHeight(), view.getBottom());
                    return Math.max(0, bottom - top);
                } else {
                    int left = Math.max(0, view.getLeft());
                    int right = Math.min(layoutManager.getWidth(), view.getRight());
                    return Math.max(0, right - left);
                }
            }
            @Override
            protected LinearSmoothScroller createScroller(RecyclerView.LayoutManager layoutManager) {
                return new LinearSmoothScroller(exoplayerRecyclerViewForYou.getContext()) {
                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        // Slower scrolling animation (higher value = slower)
                        return 200f / displayMetrics.densityDpi; // Default is usually 25f
                    }

                    @Override
                    protected int calculateTimeForScrolling(int dx) {
                        // Minimum time for scroll animation (in milliseconds)
                        return Math.max(100, super.calculateTimeForScrolling(dx)); //300
                    }
                };
            }    */
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

        gallerypermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
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
                                gallerypermissionResult.launch(result.keySet().toArray(new String[result.size()]));

                            } else {
                                /*PermissionsUtils.openPermissionDialogg(getApplicationContext(), new OkayCancelCallback() {
                                    @Override
                                    public void onOkayClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, ChatActivity.this.getString(R.string.storage_error));*/
                            }
                            break;
                        }
                    }
                }

            }
        });
    }

    private void initResultLauncher() {
        editResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    String videoId = intent.getStringExtra(Constants.TAG_VIDEO_ID);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        HomeResponse.Result item = homeApiResponse.stream().filter(result1 ->
                                result1.getVideoId().equals(videoId)).findAny().orElse(null);
                        if (item != null) {
                            int index = homeApiResponse.indexOf(item);
                            if (index != -1) {
                                Bundle payload = new Bundle();
                                item.setVideoDescription(intent.getStringExtra(Constants.TAG_DESCRIPTION));
                                item.setMode(intent.getStringExtra(Constants.TAG_PUBLISH_MODE));
                                item.getSoundtracks().setUseAudio(intent.getBooleanExtra(Constants.TAG_USE_AUDIO, false));
                                homeApiResponse.set(index, item);
                                payload.putString("video_description", intent.getStringExtra(Constants.TAG_DESCRIPTION));
                                payload.putString("mode", intent.getStringExtra(Constants.TAG_PUBLISH_MODE));
//                                payload.putBoolean(Constants.TAG_USE_AUDIO, intent.getBooleanExtra(Constants.TAG_USE_AUDIO, false));
                                videoAdapter.notifyItemChanged(index, payload);
                            }
                        }
                    } else {
                        for (int index = 0; index < homeApiResponse.size(); index++) {
                            if (videoId.equals(homeApiResponse.get(index).getVideoId())) {
                                Bundle payload = new Bundle();
                                HomeResponse.Result item = homeApiResponse.get(index);
                                item.setVideoDescription(intent.getStringExtra(Constants.TAG_DESCRIPTION));
                                item.setMode(intent.getStringExtra(Constants.TAG_PUBLISH_MODE));
                                homeApiResponse.set(index, item);
                                payload.putString("video_description", intent.getStringExtra(Constants.TAG_DESCRIPTION));
                                payload.putString("mode", intent.getStringExtra(Constants.TAG_PUBLISH_MODE));
                                videoAdapter.notifyItemChanged(index, payload);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }


    private void initView(@NotNull View rootView) {
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        btnBack = rootView.findViewById(R.id.btnBack);
        loadMoreProgress = rootView.findViewById(R.id.loadMoreProgress);
        // Null Layout
        nullText = rootView.findViewById(R.id.nullText);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullLay = rootView.findViewById(R.id.nullLay);
        nullText.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite));
        nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_videos));
        btnBack.setVisibility(View.VISIBLE);
        if (from != null && (from.equals(Constants.TAG_VIDEO) || from.equals(Constants.TAG_NOTIFICATION_PAGE))) {
            btnBack.setVisibility(View.VISIBLE);
            if (LocaleManager.isRTL()) {
                btnBack.setRotation(180);
            } else {
                btnBack.setRotation(0);
            }
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

        nullLay.setVisibility(View.GONE);

//        EventBus.getDefault().post(new homeForYouSwipePrevent(true, ""));

        isViewPagerVisible = true;

        swipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

//        if (GetSet.getUserId() != null) {
        swipeRefresh(true);
//        }

        swipeRefreshLayout.setOnRefreshListener(() -> {
            homeApiResponse.clear();
            videoAdapter.notifyDataSetChanged();
            swipeRefresh(true);
            //  exoplayerRecyclerViewForYou.playVideo(true);
            exoplayerRecyclerViewForYou.onPausePlayer();
//            EventBus.getDefault().post(new homeForYouSwipePrevent(true, ""));
            isViewPagerVisible = true;
        });
    }


    //correct code for oct month delivery for selected video and not previovs video play
 /*
    private void getForYouDataFromApi(final int finalLastOffset) {
        if (NetworkReceiver.isConnected()) {
            nullLay.setVisibility(View.GONE);
            if (!swipeRefreshLayout.isRefreshing()) {
                videoAdapter.showLoader();
            }

            HomeRequest homeRequest = new HomeRequest();
            homeRequest.setUserId(GetSet.getUserId() != null ? GetSet.getUserId() : "");
            homeRequest.setToken(GetSet.getAuthToken());
            homeRequest.setType(apiType);
            homeRequest.setLimit(String.valueOf(LOAD_MORE_MAX_LIMIT));

            if (apiCall != null) {
                apiCall.cancel();
                apiCall = null;
            }

            if (apiType != null && apiType.equals(Constants.TAG_FOR_YOU)) {
                // ✅ offset = 0,10,20...
                //homeRequest.setLimit(String.valueOf(LOAD_MORE_MAX_LIMIT));
                homeRequest.setOffset(String.valueOf(finalLastOffset));
                if (!hasInterest && lastOffset > 0) {
                    homeRequest.setRequestType("All");
                }
                Timber.d("getForYou HOME DATA: params %s", App.getGsonPrettyInstance().toJson(homeRequest));
                Timber.d("getForYou: params %s", App.getGsonPrettyInstance().toJson(homeRequest));
                apiCall = apiInterface.getHomeData(homeRequest);
            } else {
                homeRequest.setVideoId(selectedVideoId);
                homeRequest.setTagId(tagId); //tag id is userid
                homeRequest.setOffset(String.valueOf(finalLastOffset));
                //int limi = LOAD_MORE_MAX_LIMIT + videoPosition;
               // homeRequest.setLimit( limi+"");
                //homeRequest.setOffset("1");
                if (swipeRefreshLayout.isRefreshing()) {
                    Timber.tag(TAG).d("videoPosition: %s", videoPosition);
                    Timber.d("getForYou videoPosition: %s", videoPosition + "");
                    homeRequest.setPosition("" + videoPosition);
                   // homeRequest.setPosition("");
                }
                apiCall = apiInterface.getAllVideos(homeRequest);
                Timber.d("getForYou ALLVIDEO: HI params %s", App.getGsonPrettyInstance().toJson(homeRequest));
                Timber.d("getData: params %s", App.getGsonPrettyInstance().toJson(homeRequest));
            }

            apiCall.enqueue(new Callback<HomeResponse>() {
                @Override
                public void onResponse(@NotNull Call<HomeResponse> call, @NotNull Response<HomeResponse> response) {
                    Log.e(TAG,"Home api response : " + new Gson().toJson(response.body()));
                    if (response.isSuccessful() && response.body() != null && response.body().getStatus() != null) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            isscrollcheck = false;
                            apiLoadMore = response.body().getLoadMore();
                            int resultSize = response.body().getResult().size();
                            isShowLoadMore = resultSize > 0;
                            scrollLoadMore = true;

                            if (swipeRefreshLayout.isRefreshing()) { // Refresh
                                exoplayerRecyclerViewForYou.resetPlayingPosition();
                                homeApiResponse.clear();
                                homeApiResponse.addAll(response.body().getResult());
                                videoAdapter.notifyDataSetChanged();

                                exoplayerRecyclerViewForYou.post(() -> {
                                    customLinearLayoutManager.scrollToPositionWithOffset(0, 0);
                                    new Handler().postDelayed(() -> {
                                        exoplayerRecyclerViewForYou.setPlayControl(true);
                                        exoplayerRecyclerViewForYou.playVideo(false);
                                    }, 500);
                                });
                                handler.removeCallbacks(videoRunnable);
                                if (getActivity() != null) {
                                    if (getActivity() instanceof MainActivity) {
                                        MainActivity activity = (MainActivity) getActivity();
                                        if (activity.viewPager != null && activity.viewPager.getCurrentItem() == 0) {
                                            if (activity.getHomeFragment() != null &&
                                                    activity.getHomeFragment().homeFragViewPager != null &&
                                                    activity.getHomeFragment().homeFragViewPager.getCurrentItem() == 0) {
                                                handler.postDelayed(videoRunnable, 1500);
                                            }
                                        }
                                    } else {
                                        handler.postDelayed(videoRunnable, 1000);
                                    }
                                }
                            } else {
                                hideLoader();
                                int tempPosition = homeApiResponse.size();
                                homeApiResponse.addAll(response.body().getResult());
                                videoAdapter.notifyItemRangeInserted(tempPosition, resultSize);
                                if (lastVisibleItem != -1 && lastVisibleItem == tempPosition) {
                                    exoplayerRecyclerViewForYou.scrollToPosition(tempPosition);
                                }
                            }

                            if (App.isCacheEnabled) {
                                for (HomeResponse.Result result : response.body().getResult()) {
                                    ExoPlayerUtils.cacheVideo(result.getPlaybackUrl(), mContext);
                                }
                            }

                            if (swipeRefreshLayout.isRefreshing() && homeApiResponse.size() > 0) {
                                snapHelper.attachToRecyclerView(exoplayerRecyclerViewForYou);
                            }

                            // ✅ Proper row-offset increment
                            if (apiType != null && apiType.equals(Constants.TAG_FOR_YOU)) {
                                if (resultSize > 0 && apiLoadMore) {
                                    lastOffset = finalLastOffset + LOAD_MORE_MAX_LIMIT; // 0→10→20→30
                                } else {
                                    scrollLoadMore = false;
                                    isShowLoadMore = false;
                                }
                                swipeRefresh(false);
                            } else {
                                lastOffset = finalLastOffset + LOAD_MORE_MAX_LIMIT;
                                swipeRefresh(false);
                            }

                            hideLoader();
                            setNullLay(false);

                            if (finalLastOffset == 0 && homeApiResponse.isEmpty()) {
                                nullLay.setVisibility(View.VISIBLE);
                            }
                        } else if (response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                            App.makeToast(getString(R.string.Account_block));
                            GetSet.reset();
                            SharedPref.clearAll();
                            DBHelper.getInstance(mContext).clearDB();
                            Intent logout = new Intent(getContext(), LoginActivity.class);
                            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(logout);
                        }
                    } else {
                        scrollLoadMore = false;
                        hasInterest = false;
                        swipeRefresh(false);
                        hideLoader();
                        setNullLay(false);
                    }
                }

                @Override
                public void onFailure(@NotNull Call<HomeResponse> call, @NotNull Throwable t) {
                    Timber.e("onFailure: %s", t.getMessage());
                    if (!call.isCanceled()) {
                        hideLoader();
                        swipeRefresh(false);
                        // ❌ removed recursive call (it caused infinite loop)
                    }
                }
            });
        } else {
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(mContext.getString(R.string.no_internet_connection));
            swipeRefresh(false);
        }
    }
*/

    //correct code for nov month delivery for selected video and previosu video play
    private void getForYouDataFromApi(final int finalLastOffset) {
        if (finalLastOffset > 0 && (isLoading || isLastPage)) {
            return;
        }
        if (NetworkReceiver.isConnected()) {
            nullLay.setVisibility(View.GONE);
            if (!swipeRefreshLayout.isRefreshing()) {
                videoAdapter.showLoader();
            }

            HomeRequest homeRequest = new HomeRequest();
            homeRequest.setUserId(GetSet.getUserId() != null ? GetSet.getUserId() : "");
            homeRequest.setToken(GetSet.getAuthToken());
            homeRequest.setType(apiType);
           // homeRequest.setLimit(String.valueOf(LOAD_MORE_MAX_LIMIT));

            if (finalLastOffset == 0) {
                isLastPage = false;
                if (apiCall != null) {
                    apiCall.cancel();
                    apiCall = null;
                }
            }
            isLoading = true;

            if (apiType != null && apiType.equals(Constants.TAG_FOR_YOU)) {
                // ✅ offset = 0,20,40...
                homeRequest.setLimit(String.valueOf(LOAD_MORE_MAX_LIMIT));
                homeRequest.setOffset(String.valueOf(finalLastOffset));
                if (!hasInterest && lastOffset > 0) {
                    homeRequest.setRequestType("All");
                }
                Timber.d("getForYou HOME DATA: params %s", App.getGsonPrettyInstance().toJson(homeRequest));
                Timber.d("getForYou: params %s", App.getGsonPrettyInstance().toJson(homeRequest));
                apiCall = apiInterface.getHomeData(homeRequest);
            } else {
                homeRequest.setVideoId(selectedVideoId);
                homeRequest.setTagId(tagId); //tag id is userid
               // homeRequest.setOffset(String.valueOf(finalLastOffset));
                int limi = LOAD_MORE_MAX_LIMIT + videoPosition;
                homeRequest.setLimit( limi+"");
                homeRequest.setOffset("1");
                if (swipeRefreshLayout.isRefreshing()) {
                    Timber.tag(TAG).d("videoPosition: %s", videoPosition);
                    Timber.d("getForYou videoPosition: %s", videoPosition + "");
                    // homeRequest.setPosition("" + videoPosition);
                    homeRequest.setPosition("");
                }
                apiCall = apiInterface.getAllVideos(homeRequest);
                Timber.d("getForYou ALLVIDEO: HI params %s", App.getGsonPrettyInstance().toJson(homeRequest));
                Timber.d("getData: params %s", App.getGsonPrettyInstance().toJson(homeRequest));
            }

            apiCall.enqueue(new Callback<HomeResponse>() {
                @Override
                public void onResponse(@NotNull Call<HomeResponse> call, @NotNull Response<HomeResponse> response) {
                    Log.e(TAG,"Home api response : " + new Gson().toJson(response.body()));
                    if (response.isSuccessful() && response.body() != null && response.body().getStatus() != null) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            isscrollcheck = false;
                            apiLoadMore = response.body().getLoadMore();
                            int resultSize = response.body().getResult().size();
                            int insertedCount = resultSize;
                            isShowLoadMore = resultSize > 0;
                            scrollLoadMore = true;

                            if (swipeRefreshLayout.isRefreshing()) { // Refresh
                                exoplayerRecyclerViewForYou.resetPlayingPosition();
                                homeApiResponse.clear();
                                homeApiResponse.addAll(response.body().getResult());
                                videoAdapter.notifyDataSetChanged();

                                exoplayerRecyclerViewForYou.post(() -> {
                                    if(apiType.equals("myvideos") || apiType.equals("videos") || apiType.equals("favourite")|| apiType.equals("repostedvideos")|| apiType.equals("likedvideos")) {
                                        customLinearLayoutManager.scrollToPositionWithOffset((videoPosition-1), 0);
                                    }else{
                                        customLinearLayoutManager.scrollToPositionWithOffset(0, 0);
                                    }
                                    new Handler().postDelayed(() -> {
                                        exoplayerRecyclerViewForYou.setPlayControl(true);
                                        exoplayerRecyclerViewForYou.playVideo(false);
                                    }, 500);
                                });


                             /*   exoplayerRecyclerViewForYou.post(() -> {
                                    int targetIndex = 0;

                                    if (apiType.equals("myvideos") || apiType.equals("videos")) {
                                        // ✅ Find the exact index of the selected videoId in the refreshed data
                                        targetIndex = findIndexByVideoId(homeApiResponse, selectedVideoId);
                                        if (targetIndex < 0) {
                                            // Fallback if not found (e.g., server didn’t include it this page)
                                            targetIndex = Math.max(0, Math.min(videoPosition, homeApiResponse.size() - 1));
                                        }
                                    }

                                    // If you use a SnapHelper, temporarily detach to prevent it from snapping back
                                    // (uncomment if needed)
                                    // if (snapHelper != null) snapHelper.attachToRecyclerView(null);

                                    exoplayerRecyclerViewForYou.stopScroll();
                                    customLinearLayoutManager.scrollToPositionWithOffset(targetIndex, 0);

                                    // Reattach SnapHelper
                                    // if (snapHelper != null) exoplayerRecyclerViewForYou.post(() -> snapHelper.attachToRecyclerView(exoplayerRecyclerViewForYou));

                                    new Handler().postDelayed(() -> {
                                        exoplayerRecyclerViewForYou.setPlayControl(true);
                                        exoplayerRecyclerViewForYou.playVideo(false);
                                    }, 500);
                                });

                             */   handler.removeCallbacks(videoRunnable);
                                if (getActivity() != null) {
                                    if (getActivity() instanceof MainActivity) {
                                        MainActivity activity = (MainActivity) getActivity();
                                        if (activity.viewPager != null && activity.viewPager.getCurrentItem() == 0) {
                                            if (activity.getHomeFragment() != null &&
                                                    activity.getHomeFragment().homeFragViewPager != null &&
                                                    activity.getHomeFragment().homeFragViewPager.getCurrentItem() == 0) {
                                                handler.postDelayed(videoRunnable, 1500);
                                            }
                                        }
                                    } else {
                                        handler.postDelayed(videoRunnable, 1000);
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
                                    exoplayerRecyclerViewForYou.scrollToPosition(tempPosition);
                                }
                            }

                            if (App.isCacheEnabled) {
                                for (HomeResponse.Result result : response.body().getResult()) {
                                    ExoPlayerUtils.cacheVideo(result.getPlaybackUrl(), mContext);
                                }
                            }

                            if (swipeRefreshLayout.isRefreshing() && homeApiResponse.size() > 0) {
                                snapHelper.attachToRecyclerView(exoplayerRecyclerViewForYou);
                            }

                            // ✅ Proper row-offset increment 0 → 20 → 40
                            if (apiType != null && apiType.equals(Constants.TAG_FOR_YOU)) {
                                if (resultSize > 0) {
                                    lastOffset = finalLastOffset + LOAD_MORE_MAX_LIMIT;
                                    scrollLoadMore = resultSize >= LOAD_MORE_MAX_LIMIT || apiLoadMore;
                                    isLastPage = !scrollLoadMore;
                                } else {
                                    scrollLoadMore = false;
                                    isShowLoadMore = false;
                                    isLastPage = true;
                                }
                                swipeRefresh(false);
                            } else {
                                lastOffset = finalLastOffset + LOAD_MORE_MAX_LIMIT;
                                scrollLoadMore = resultSize >= LOAD_MORE_MAX_LIMIT;
                                isLastPage = !scrollLoadMore;
                                swipeRefresh(false);
                            }

                            if (insertedCount == 0 && finalLastOffset > 0) {
                                scrollLoadMore = false;
                                isShowLoadMore = false;
                                isLastPage = true;
                            }

                            isLoading = false;
                            hideLoader();
                            setNullLay(false);

                            if (finalLastOffset == 0 && homeApiResponse.isEmpty()) {
                                nullLay.setVisibility(View.VISIBLE);
                            }
                        } else if (response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                            isLoading = false;
                            App.makeToast(getString(R.string.Account_block));
                            GetSet.reset();
                            SharedPref.clearAll();
                            DBHelper.getInstance(mContext).clearDB();
                            Intent logout = new Intent(getContext(), LoginActivity.class);
                            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(logout);
                        } else {
                            isLoading = false;
                            swipeRefresh(false);
                            hideLoader();
                        }
                    } else {
                        isLoading = false;
                        scrollLoadMore = false;
                        hasInterest = false;
                        swipeRefresh(false);
                        hideLoader();
                        setNullLay(false);
                    }
                }

                @Override
                public void onFailure(@NotNull Call<HomeResponse> call, @NotNull Throwable t) {
                    Timber.e("onFailure: %s", t.getMessage());
                    if (!call.isCanceled()) {
                        isLoading = false;
                        hideLoader();
                        swipeRefresh(false);
                        // ❌ removed recursive call (it caused infinite loop)
                    }
                }
            });
        } else {
            isLoading = false;
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(mContext.getString(R.string.no_internet_connection));
            swipeRefresh(false);
        }
    }

    private int findIndexByVideoId(java.util.List<HomeResponse.Result> list, String videoId) {
        if (list == null || videoId == null) return -1;
        for (int i = 0; i < list.size(); i++) {
            HomeResponse.Result r = list.get(i);
            if (r != null && videoId.equals(r.getVideoId())) return i;
        }
        return -1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(apiType.equals("myvideos") || apiType.equals("most_liked") || apiType.equals("hashtags") || apiType.equals("videos")
                || apiType.equals("musics") || apiType.equals("related") || apiType.equals("likedvideos")|| apiType.equals("favourite")) {

            Log.e("checkIss","-");

            exoplayerRecyclerViewForYou.onPausePlayer();
            // exoplayerRecyclerViewForYou.playVideo(false);
            apiCall.cancel();
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
                exoplayerRecyclerViewForYou.setVisibility(View.GONE);
                nullLay.setVisibility(View.VISIBLE);

            } else {
                nullLay.setVisibility(View.GONE);
                exoplayerRecyclerViewForYou.setVisibility(View.VISIBLE);
            }
        } else {
            /*if (!NetworkStatus.isConnected()) {
            Toast.makeText(mContext, "Poor NetWork Connection..", Toast.LENGTH_SHORT).show();
            nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_videos));
            nullLay.setVisibility(View.VISIBLE);
            exoplayerRecyclerViewForYou.setVisibility(View.GONE);
            nullText.setText(mContext.getString(R.string.no_internet_connection));
        }*/
            if (homeApiResponse.size() == 0) {
                exoplayerRecyclerViewForYou.setVisibility(View.GONE);
                nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_videos));
                nullText.setText(mContext.getString(R.string.unable_to_reach_server));
                nullLay.setVisibility(View.VISIBLE);
            } else {
                exoplayerRecyclerViewForYou.setVisibility(View.VISIBLE);
                nullLay.setVisibility(View.GONE);
            }
        }
    }

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

    private void swipeRefresh(final boolean refresh) {
        if (refresh) hasInterest = true;
        swipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            getForYouDataFromApi(lastOffset = videoPosition != -1 ? videoPosition : 0);
            lastOffset = 0;
            //getForYouDataFromApi(0);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.MUSIC_REQUEST_CODE) {
            exoplayerRecyclerViewForYou.setPlayControl(true);
        } else if (requestCode == Constants.HASHTAG_REQUEST_CODE) {
            exoplayerRecyclerViewForYou.setPlayControl(true);
        } else if (requestCode == Constants.COMMENT_MUSIC_REQUEST_CODE) {
            exoplayerRecyclerViewForYou.setPlayControl(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    /*@Override
    public void onResume() {
        super.onResume();
        isViewPagerVisible = true;
      //  exoplayerRecyclerViewForYou.playVideo(true);
//        exoplayerRecyclerViewForYou.setPlayControl(videoAdapter.commentListDialog == null || !videoAdapter.commentListDialog.isShowing());
        handler.post(videoRunnable);
        if (!NetworkReceiver.isConnected()) {
            if (swipeRefreshLayout.isRefreshing())
                swipeRefresh(false);
        }
    }*/

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("ForYouVideoFragment onResume called");
        isViewPagerVisible = true;

        // Resume video playback
        if (exoplayerRecyclerViewForYou != null) {
            // Small delay to ensure views are ready
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isViewPagerVisible && exoplayerRecyclerViewForYou != null) {
                        Timber.d("onResume: homeApiResponse size: " + homeApiResponse.size());
                        if (homeApiResponse.isEmpty() && GetSet.getUserId() != null) {
                            Timber.d("onResume: homeApiResponse is empty, refreshing data.");
                            swipeRefresh(true);
                        } else {
                            exoplayerRecyclerViewForYou.resumePlayer();
                            exoplayerRecyclerViewForYou.setPlayControl(
                                    videoAdapter.commentListDialog == null || !videoAdapter.commentListDialog.isShowing()
                            );
                            // Ensure the correct video is scrolled into view
                            if (videoPosition != -1) {
                                exoplayerRecyclerViewForYou.smoothScrollToPosition(videoPosition);
                            }
                        }
                    } else {
                        Timber.d("onResume: exoplayerRecyclerViewForYou is null.");
                    }
                }
            }, 200);
        } else {
            Timber.d("onResume: exoplayerRecyclerViewForYou is null outside delayed runnable.");
        }

        // Resume the video runnable
        handler.post(videoRunnable);

        // Handle network connectivity
        if (!NetworkReceiver.isConnected()) {
            if (swipeRefreshLayout.isRefreshing())
                swipeRefresh(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause called");
        exoplayerRecyclerViewForYou.setPlayControl(false);
        if (!isscrollcheck){
            exoplayerRecyclerViewForYou.playVideo(false);
        }
        isViewPagerVisible = false;
        handler.removeCallbacks(videoRunnable);
        updateWatchVideo();
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

    private void updateWatchVideo() {
        String watchStr = watchPref.getString(Constants.TAG_ID, "");
        ArrayList<String> videoWatchlist = new ArrayList<>();

        if (!watchStr.equals("")) {
            JsonParser jsonParser = new JsonParser();
            jsonParser.parse(watchStr);
            Gson gson = new Gson();
            // below line is to get the type of our array list.
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            videoWatchlist = gson.fromJson(watchStr, type);
        }
        if (NetworkReceiver.isConnected() && !watchStr.equals("") && videoWatchlist.size() > 0) {
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(videoWatchlist);
            } catch (Exception e) {
            }

            HashMap<String, String> requestMap = new HashMap<>();
            if (Constants.Login_method.equals("skip")) {
                requestMap.put(Constants.TAG_USER_ID, "");
                requestMap.put(Constants.TAG_VIDEO_LIST, "");
            } else{
                requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
                requestMap.put(Constants.TAG_VIDEO_LIST, String.valueOf(jsonArray));
            }
            Call<HashMap<String, String>> call = apiInterface.updateWatchVideo(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        HashMap<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
//                            Log.i(TAG, "onResponse: " + responseMap);
                            SharedPreferences.Editor editor = watchPref.edit();
                            editor.putString(Constants.TAG_ID, "");
                            editor.commit();
                        } else if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                            App.makeToast(getString(R.string.Account_block));
                            GetSet.reset();
                            SharedPref.clearAll();
                            DBHelper.getInstance(mContext).clearDB();
                            Intent logout = new Intent(mContext, LoginActivity.class);
                            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(logout);
                        }
                    } else {

                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    Log.e("onFailurewatch", "-" + t.toString());
                    t.printStackTrace();

                }
            });
        }
    }

    /*@Override
    public void onProgress(String url, File file, int progress) {

        Timber.d("onProgress: progress %s ", progress);
        Timber.d("onProgress: url %s ", url);
    }

    @Override
    public void onError(Throwable e) {

    }*/

    private boolean appInstalledOrNot(String uri) {

        PackageManager pm = getContext().getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            storagePermissionResult.launch(PermissionsUtils.READ_STORAGE_PERMISSION);
        } else {
            storagePermissionResult.launch(PermissionsUtils.READ_WRITE_PERMISSIONS);
        }
    }

    private void requestStoragePermissions13() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gallerypermissionResult.launch(PermissionsUtils.DOWNLOAD_PERMISSION13);
        }
    }

    @Override
    public void onItemClicked(Object o, int position, String type) {

        Log.d(TAG, "onItemClicked1111111: "+type);
        switch (type) {
            case "COMMENT_OPEN": {
                if (exoplayerRecyclerViewForYou != null) {
                    exoplayerRecyclerViewForYou.setCommentDialogOpen(true);
                } Log.d("VIDEO_DBG", "COMMENT_OPEN received -> isCommentDialogOpen=true");
                // Immediately prevent resume attempts
                // Optionally shrink player:
                // setVideoShrunkByPercentage(true, 0.45f, false);
                break;
            }
            case "COMMENT_CLOSE":  {
                if (exoplayerRecyclerViewForYou != null) {
                    exoplayerRecyclerViewForYou.setCommentDialogOpen(false);
                } Log.d("VIDEO_DBG", "COMMENT_CLOSE received -> isCommentDialogOpen=false");
                // Restore video if needed
                break;
            }

            case Constants.TAG_COMMENT: {
                //Oct3 I commented below line for QA told dont pause the video as per client comment
                // exoplayerRecyclerViewForYou.setPlayControl(false);
                break;
            }
            case Constants.TAG_PLAYBACK: {
                boolean playBack = (boolean) o;
                exoplayerRecyclerViewForYou.setPlayControl(playBack);
                break;
            }
            case Constants.TAG_SINGLE_TAP:
                exoplayerRecyclerViewForYou.togglePlay();
                break;
            case Constants.TAG_GIFT:
            case Constants.TAG_LIKE:
            case Constants.TAG_DOWNLOAD:
            case Constants.TAG_HASHTAGS:
            case Constants.TAG_AUDIO:
            case Constants.TAG_SHARE:
            case Constants.TAG_FOLLOW:
            case Constants.TAG_REPORT:
                break;
            case Constants.TAG_PERMISSION: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestStoragePermissions13();
                } else {
                    requestStoragePermissions();
                }
                break;
            }
            case Constants.TAG_PROFILE: {
                if (profileImageClickListener != null) {
                    HomeResponse.Result result = (HomeResponse.Result) o;
                    if (result != null && !TextUtils.isEmpty(result.getPublisherId())) {
                        profileImageClickListener.onUserClicked(true, null);
                    }
                }
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
                String videoId = (String) o;
                EventBus.getDefault().post(new VideoDeleteEvent(true, videoId));
                exoplayerRecyclerViewForYou.onPausePlayer();
                homeApiResponse.remove(position);
                videoAdapter.notifyItemRemoved(position);
                videoAdapter.notifyItemRangeChanged(position, homeApiResponse.size());
                isDeleted = true;
                setNullLay(false);
                handler.removeCallbacks(videoRunnable);
                if (getActivity() != null) {
                    if (getActivity() instanceof MainActivity) {
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity.viewPager != null && activity.viewPager.getCurrentItem() == 0) {
                            handler.postDelayed(videoRunnable, 1000);
                        }
                    } else {
                        handler.postDelayed(videoRunnable, 1000);
                    }
                }
            }
            break;
            case Constants.TAG_EDIT: {
                Intent intent = (Intent) o;
                editResultLauncher.launch(intent);
            }
            break;
        }
    }

    @Override
    public void onItemPlaybackSpeed(Object o, int position, String videoId, float speed, String type) {
        Log.d(TAG, "onItemPlaybackSpeed22222: "+type);
        switch (type){
            case "playbackspeed":{
                Log.d(TAG, "onItemPlaybackSpeed111111111111: ");

                exoplayerRecyclerViewForYou.setPlayBackSpeed(true,speed);
            }
            break;
        }
    }

    @Override
    public void onItemPlaybackSpeed(Object o, int position, String videoId, String type, String url) {

        Log.d(TAG, "onItemPlaybackSpeed22222: "+type);
        switch (type){
            case "caption":{
                Log.d(TAG, "onItemPlaybackSpeed111111111111: ");

//                exoplayerRecyclerViewForYou.setPlayBackSpeed(true,speed);
                exoplayerRecyclerViewForYou.getCaption(url,"");
            }
            break;
        }
    }

    @Override
    public void onItemReposted(Object o, int position, String videoId, String action, String desc) {

    }


    @Override
    public void onProfileUpdate(FollowingProfileUpdate profileUpdate) {
        if (profileImageClickListener != null) {
            profileImageClickListener.onUserClicked(false, profileUpdate);
        }
    }



}