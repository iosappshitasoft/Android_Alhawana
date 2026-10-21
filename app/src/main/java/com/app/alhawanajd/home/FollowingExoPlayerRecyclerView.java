package com.app.alhawanajd.home;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
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
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.helper.callback.PlayerRecyclerListener;
import com.app.model.GetSet;
import com.app.model.HomeResponse;
import com.app.utils.exoutils.ExoPlayerUtils;

import java.util.ArrayList;
import java.util.Objects;

import timber.log.Timber;

public class FollowingExoPlayerRecyclerView extends RecyclerView {

    private static final String TAG = FollowingExoPlayerRecyclerView.class.getSimpleName();
    public static final String AppName = "AppName";
    /**
     * PlayerViewHolder UI component
     * Watch PlayerViewHolder class
     */
    private ImageView mediaCoverImage, volumeControl;
    private ProgressBar progressBar;
            SeekBar progress_duration;
    private View viewHolderParent;
    private FrameLayout mediaContainer;
    private StyledPlayerView videoSurfaceView;
    private ExoPlayer videoPlayer;

    /**
     * variable declaration
     */
    // Media List
    private ArrayList<HomeResponse.Result> homepageResponseResult = new ArrayList<>();
    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    public int targetPosition;
    private Context context;
    private int playPosition = -1;
    private boolean isVideoViewAdded;
    private RequestManager requestManager;
    // controlling volume state
    private VolumeState volumeState;

    public PlayState videoState; // video pause and play
    Handler mHandler = new Handler();
    Runnable runnable;
    Runnable updateView;
    private DBHelper dbHelper;
    private PlayerRecyclerListener listener;

    private final View.OnClickListener videoViewClickListener = v -> {
        togglePlay();
    };

    public FollowingExoPlayerRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public FollowingExoPlayerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    Player.Listener playerListener = new Player.Listener() {

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
            Timber.tag(TAG).d("onPlaybackStateChanged: %s", playbackState);
            switch (playbackState) {

                case Player.STATE_BUFFERING:
                    mediaCoverImage.setAlpha(1f);

                    break;
                case Player.STATE_ENDED:
                    if (homepageResponseResult.size() > 0 && playPosition != -1) {
                        dbHelper.insertVideoStatus(homepageResponseResult.get(playPosition).getVideoId());
                    }
                    videoPlayer.seekTo(0);
                    break;
                case Player.STATE_IDLE:

                    break;
                case Player.STATE_READY:
                    mediaCoverImage.animate().alpha(0f)
                            .setDuration(300).setStartDelay(280);

                    videoState = PlayState.ON;
                    volumeState = VolumeState.ON;

                    // Set resize mode based on video orientation
                    if (videoPlayer.getVideoFormat() != null) {
                        int videoWidth = videoPlayer.getVideoFormat().width;
                        int videoHeight = videoPlayer.getVideoFormat().height;

                        // Check if video is portrait (height > width)
                        if (videoHeight > videoWidth) {
                            // Portrait video - fill the screen
                            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                            Timber.d("Portrait video detected - using ZOOM mode");
                        } else {
                            // Landscape video - play as it is
                            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                            Timber.d("Landscape video detected - using FIT mode");
                        }
                    }

                    if (!isVideoViewAdded) {
                        addVideoView();
                        progressBar();
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
            Timber.tag(TAG).e("onPlayerError: %s", error.getMessage());
            Player.Listener.super.onPlayerError(error);
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }
    };

    public void togglePlay() {

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

    private void init(Context context) {
        this.context = context.getApplicationContext();
        dbHelper = DBHelper.getInstance(context);
        Timber.tag(TAG).d("init:");
        Display display = ((WindowManager) Objects.requireNonNull(
                getContext().getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        videoSurfaceDefaultHeight = point.x;
        screenDefaultHeight = point.y;
        videoSurfaceView = new StyledPlayerView(this.context);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
        trackSelector.setParameters(
                trackSelector.buildUponParameters().setMaxVideoSizeSd());
        videoPlayer = new ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                //      .setLoadControl(loadControl)
                .build();

        // Disable Player Control
        videoSurfaceView.setUseController(false);
        // Bind the player to the view.
        videoSurfaceView.setPlayer(videoPlayer);
        // Turn on Volume
        setVolumeControl(VolumeState.ON);

        initPlayerListener();

        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    /*if (mediaCoverImage != null) {
                        // show the old thumbnail
                        mediaCoverImage.setVisibility(VISIBLE);
                    }*/


                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic

                    if (volumeControl != null) volumeControl.setVisibility(GONE);

                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true);
                    } else {
                        playVideo(false);
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {

                    Timber.i("SCROLL_STATE_DRAGGING");

                } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                }
            }


            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });


        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if (viewHolderParent != null && viewHolderParent.equals(view)) {
                    resetVideoView();
                }
            }
        });
    }

    private void initPlayerListener() {
        Timber.d("initPlayerListener");
        videoPlayer.addListener(playerListener);
    }

    private void progressBar() {
        progress_duration.setMax((int) videoPlayer.getDuration());
        runnable = () -> {
            if (videoPlayer != null) {
                progress_duration.setProgress((int) ((videoPlayer.getCurrentPosition())));
            }
            mHandler.postDelayed(runnable, 100);
        };
        runnable.run();

    }



    public void setPlayControl(boolean state) {
        Timber.tag(TAG).d("setPlayControl: %b", state);
        videoPlayer.setPlayWhenReady(state);
        if (volumeControl != null && volumeControl.getVisibility() == VISIBLE)
            volumeControl.setVisibility(GONE);

    }


    public void playVideo(boolean isEndOfList) {

        /*int targetPosition;*/

        if (!isEndOfList) {
      /*int startPosition = ((LinearLayoutManager) Objects.requireNonNull(
          getLayoutManager())).findFirstVisibleItemPosition();*/
            int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastCompletelyVisibleItemPosition();

            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1;
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            // if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);

                targetPosition =
                        startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
            } else {
                targetPosition = startPosition;
            }
        } else {
            targetPosition = homepageResponseResult.size() - 1;
        }


        // video is already playing so return
        if (targetPosition == playPosition) {
            return;
        }

        // set the position of the list-item that is to be played
        playPosition = targetPosition;

        Timber.d("playVideo: %s", playPosition);

        if (playPosition >= 0) {
            String userId = "";
            if (GetSet.getUserId() != null) {
                userId = homepageResponseResult.get(targetPosition).getPublisherId();
            }
            Timber.i("userId: %s", userId);
            if (TextUtils.isEmpty(userId)) {
//                EventBus.getDefault().post(new homeForYouSwipePrevent(true, "")); // swipe enabled
            } else {
//                EventBus.getDefault().post(new homeForYouSwipePrevent(false, ""));
//                listener.onProfileUpdate(new FollowingProfileUpdate(homepageResponseResult.get(playPosition).getPublisherId(), homepageResponseResult.get(playPosition).getPublisherImage(), ""));
            }
        }

        if (videoSurfaceView == null) {
            return;
        }

        // remove any old surface views from previously playing videos
        videoSurfaceView.setVisibility(INVISIBLE);
        removeVideoView(videoSurfaceView);

        int currentPosition = targetPosition - ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager())).findFirstCompletelyVisibleItemPosition();
        View child = getChildAt(currentPosition);
        Timber.tag(TAG).d("playVideo: child=> %s", child);

        if (child == null) {
            return;
        }

        HomeVideoAdapter.VideoViewHolder holder = (HomeVideoAdapter.VideoViewHolder) child.getTag();
//        Timber.tag(TAG).d("playVideo: holder=> %s instanse %s, %s", holder, child, String.valueOf(false));

        if (holder == null) {
            playPosition = -1;
            return;
        }

        viewHolderParent = holder.itemView;
        mediaContainer = holder.mediaContainer;
        mediaCoverImage = holder.video_thumbnail;
        progressBar = holder.progressBar;
        volumeControl = holder.video_play_pause;
        progress_duration = holder.progress_duration;

        videoSurfaceView.setPlayer(videoPlayer);
        //viewHolderParent.setOnClickListener(videoViewClickListener);

        String mediaUrl = null;
        Uri hlsUri = null;

        HomeResponse.Result data = homepageResponseResult.get(targetPosition);
        mediaUrl = data.getPlaybackUrl();
        if (mediaUrl != null) {
            hlsUri = Uri.parse(mediaUrl);
        }

        // Get video dimensions to determine orientation
        if (videoPlayer.getVideoFormat() != null) {
            int videoWidth = videoPlayer.getVideoFormat().width;
            int videoHeight = videoPlayer.getVideoFormat().height;

            // Check if video is portrait (height > width)
            if (videoHeight > videoWidth) {
                // Portrait video - fill the screen
                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            } else {
                // Landscape video - play as it is with black bars if needed
                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }
        } else {
            // Fallback if video format is not available yet
            if (Objects.equals(data.getVideoType(), "gallery")) {
                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            } else {
                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            }
        }

        Timber.tag(TAG).d("playVideo: %s", hlsUri);
        /** Play from HlsMediaSource*/
        MediaSource hlsMediaSource = ExoPlayerUtils.getHlsMediaSource(context, hlsUri);
        videoPlayer.setMediaSource(hlsMediaSource);
        videoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        videoPlayer.prepare();
        videoPlayer.setPlayWhenReady(true);
    }

    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     */
    private int getVisibleVideoSurfaceHeight(int playPosition) {

        int at = playPosition - ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager())).findFirstCompletelyVisibleItemPosition();

        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }

        int[] location = new int[2];
        child.getLocationInWindow(location);

        if (location[1] < 0) {
            return location[1] + videoSurfaceDefaultHeight;
        } else {
            return screenDefaultHeight - location[1];
        }
    }

    // Remove the old player
    private void removeVideoView(StyledPlayerView videoView) {
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
            isVideoViewAdded = false;
            viewHolderParent.setOnClickListener(null);
        }
    }

    private void addVideoView() {
        mediaContainer.addView(videoSurfaceView);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
        videoSurfaceView.setVisibility(VISIBLE);
        videoSurfaceView.setAlpha(1);
        //mediaCoverImage.setVisibility(GONE);


    }

    private void resetVideoView() {
        if (isVideoViewAdded) {
            mediaCoverImage.setAlpha(1f);
            //mediaCoverImage.setVisibility(VISIBLE);
            playPosition = -1;
            removeVideoView(videoSurfaceView);
            videoSurfaceView.setVisibility(INVISIBLE);
        }
    }

    public void releasePlayer() {

        if (videoPlayer != null) {
            videoSurfaceView = null;
            videoPlayer.release();
            videoPlayer = null;
        }

        viewHolderParent = null;
    }

    public void onPausePlayer() {
        if (videoPlayer != null) {
            videoPlayer.stop();
        }
    }


    private void animatePlayControl() {

        if (volumeControl != null) volumeControl.setVisibility(VISIBLE);

        try {
            if (videoPlayer != null) {
                if (videoState == PlayState.OFF) {

                    if (volumeControl != null) {
                        volumeControl.setImageResource(R.drawable.video_play);

                        volumeControl.animate().cancel();

                        volumeControl.setAlpha(1f);
                    }


                } else if (videoState == PlayState.ON) {
                    if (volumeControl != null) {
                        volumeControl.setImageResource(R.drawable.video_play);

                        volumeControl.animate().cancel();
                        volumeControl.setAlpha(1f);
                        volumeControl.animate().alpha(0f)
                                .setDuration(500).setStartDelay(100);
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //public void onRestartPlayer() {
    //  if (videoPlayer != null) {
    //   playVideo(true);
    //  }
    //}

    private void setVolumeControl(VolumeState state) {
        volumeState = state;
        if (state == VolumeState.OFF) {
            videoPlayer.setVolume(0f);
            //animateVolumeControl();
        } else if (state == VolumeState.ON) {
            videoPlayer.setVolume(1f);
            //animateVolumeControl();
        }
    }

    private void animateVolumeControl() {
        if (volumeControl != null) {
            volumeControl.bringToFront();
            if (volumeState == VolumeState.OFF) {
                volumeControl.setImageResource(R.drawable.video_pause);
                volumeControl.animate().cancel();
                volumeControl.setAlpha(1f);
            } else if (volumeState == VolumeState.ON) {
                volumeControl.setImageResource(R.drawable.video_play);
                volumeControl.animate().cancel();
                volumeControl.setAlpha(1f);
                volumeControl.animate().alpha(0f)
                        .setDuration(300).setStartDelay(100);
            }

        }
    }

    public void setMediaObjects(ArrayList<HomeResponse.Result> mHomepageResponseResult) {
        this.homepageResponseResult = mHomepageResponseResult;
    }

    public void setListener(PlayerRecyclerListener listener) {
        this.listener = listener;
    }

    public void resetPlayingPosition() {
        playPosition = -1;
    }

    public void setExoPlayView(int visibility) {
        if (videoSurfaceView != null && videoSurfaceView.getVideoSurfaceView() != null) {
            videoSurfaceView.getVideoSurfaceView().setVisibility(visibility);
        }
    }

    /**
     * Volume ENUM
     */
    private enum VolumeState {
        ON, OFF
    }

    public enum PlayState {ON, OFF}
}
