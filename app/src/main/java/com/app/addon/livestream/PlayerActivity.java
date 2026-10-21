package com.app.addon.livestream;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.danikula.videocache.CacheListener;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.TracksInfo;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.addon.livestream.external.avloading.AVLoadingIndicatorView2;
import com.app.addon.livestream.external.heartlayout.HeartLayout;
import com.app.addon.livestream.external.helper.CustomLayoutManager;

import com.app.addon.livestream.external.helper.NetworkReceiver;
import com.app.addon.livestream.external.helper.Utils;
import com.app.addon.livestream.external.model.StreamDetails;
import com.app.addon.livestream.external.utils.ApiClient;
import com.app.addon.livestream.external.utils.Constants;
import com.app.model.GetSet;
import com.app.addon.livestream.external.utils.StreamConstants;
import com.app.alhawanajd.App;
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.helper.LocaleManager;
import com.app.helper.StorageUtils;
import com.app.model.Report;
import com.app.utils.AdminData;
import com.app.utils.api.ApiInterface;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayerActivity extends BaseFragmentActivity implements PlayerControlView.VisibilityListener, PlaybackPreparer, CacheListener {
    final private static String TAG = PlayerActivity.class.getSimpleName();

    PlayerView playerView;
    AppCompatTextView txtTime;
    AppCompatTextView loadingTitle;
    AppCompatTextView loadingPublisherName;
    ImageView loadingImage;
    ImageView loadingPublisherColor;
    ImageView loadingPublisherImage;
    RelativeLayout loadingLay;
    ImageView btnComments;
    ImageView btnDetail;
    ImageView btnClose;
    ImageView btnRefresh;
    CheckBox btnPlay;
    RelativeLayout playLay;
    private ExoPlayer exoPlayer;
    private int startItemIndex;
    private long startPosition;
    private TracksInfo lastSeenTracksInfo;
    AVLoadingIndicatorView2 avBallIndicator;
    //    RecyclerView rvComments;
    HeartLayout heartLay;
    AppCompatTextView txtLiveCount, txtBottomDuration;
    FrameLayout parentLay;
    RelativeLayout timeLay;
    private boolean startAutoPlay = true;
    private boolean playWhenReady = true;
    private RelativeLayout liveCountLay;
    private RelativeLayout liveStatusLay;
    private FrameLayout liveTxtLay;
    AppCompatTextView bottomStreamTitle;
    RoundedImageView publisherImage;
    RoundedImageView publisherColorImage;
    AppCompatTextView txtPublisherName;
    /*    LinearLayout btnFollow;
        RoundedImageView btnUnFollow;*/
    LinearLayout bottomFirstLay, bottomDeleteLay, bottomReportLay, chatHideLay, bottomDetailsLay,
            bottomUserLay, bottomViewersLay, bottomGiftDetailsLay, bottomInternalShareLay,
            bottomExternalShareLay, bottomGiftsLay;
    AppCompatTextView bottomViewerCount, txtTotalLikes, txtReport, bottomGiftCount;
    RecyclerView bottomRecyclerView, bottomCommentsView, bottomGiftView;
    RelativeLayout bottomDurationLay, bottomLikesLay;
    TextView txtNoCommands;
    //    HorizontalScrollView hashTagsLay;
    private BottomSheetDialog bottomDialog, bottomViewersDialog, bottomCommentsDialog,
            bottomGiftsDialog, bottomHashTagsDialog;
    private Button btnMoreHashTag;
    //private HashtagView hashtagView, bottomHashTagView;
    private String streamName, streamUrl, publisherId, totalTime = "00:00", from = null;
    Handler handler = new Handler();
    ApiInterface apiInterface;
    private boolean shouldAutoPlay = true, durationSet = false;
    public static boolean isStreamDeleted = false;
    private CommentsAdapter commentsAdapter;
    // private GiftsAdapter giftsAdapter;
    ArrayList<StreamDetails.Comments> commentList = new ArrayList<>();
    List<StreamDetails.Comments> tempCommentList = new ArrayList<>();
    ArrayList<StreamDetails.Gifts> giftsList = new ArrayList<>();
    private LiveCommentsAdapter liveCommentsAdapter;
    ArrayList<HashMap<String, String>> liveCommentList = new ArrayList<HashMap<String, String>>();
    ArrayList<String> timeList = new ArrayList<>();
    private BottomSheetDialog reportDialog;
    private RecyclerView reportsView;
    private ReportAdapter reportAdapter;
    // AppUtils appUtils;
    private static DefaultBandwidthMeter BANDWIDTH_METER;
    private static String USER_AGENT;
    private boolean USE_BANDWIDTH_METER = false, needRetrySource;
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    private PlayerActivity context;

    Utils appUtils;

    public List<String> reportList = new ArrayList<String>();

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Runnable timerUpdate = new Runnable() {
        @Override
        public void run() {
            if (exoPlayer != null) {
                long currentMilliSeconds = exoPlayer.getCurrentPosition();
                String duration, commentDuration;
                duration = appUtils.getFormattedDuration(currentMilliSeconds);
                commentDuration = appUtils.getCommentsDuration(currentMilliSeconds);
                if (tempCommentList.size() > 0) {
                    if (timeList.contains(commentDuration.trim())) {
                        int firstIndex = timeList.indexOf(commentDuration.trim());
                        int lastIndex = timeList.lastIndexOf(commentDuration.trim());
                        List<StreamDetails.Comments> subList;
                        if (firstIndex == lastIndex) {
                            subList = Collections.singletonList(tempCommentList.get(firstIndex));
                        } else {
                            subList = tempCommentList.subList(firstIndex, lastIndex);
                        }
                        for (StreamDetails.Comments comments : subList) {
                            if (comments.getTime() != null && comments.getTime().equals(commentDuration)) {
                                setCommentUI(comments);
                            }
                        }
                    }
                }
                txtTime.setText(duration + " / " + totalTime);
                handler.postDelayed(timerUpdate, 1000);
            }
        }
    };
    private StreamDetails streamDetails;
    private StreamDetails apiStreamData;
    private DefaultTrackSelector trackSelector;
    DefaultLoadControl loadControl;
    private long resumePosition;
    private boolean isPlayerInitialized = false;
    private int resumeWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        appUtils = new Utils(this);

        playerView = findViewById(R.id.playerView);
        txtTime = findViewById(R.id.txtTime);
        loadingTitle = findViewById(R.id.loadingTitle);
        loadingPublisherName = findViewById(R.id.loadingPublisherName);
        loadingImage = findViewById(R.id.loadingImage);
        loadingPublisherColor = findViewById(R.id.loadingPublisherColor);
        loadingPublisherImage = findViewById(R.id.loadingPublisherImage);
        loadingLay = findViewById(R.id.loadingLay);
        btnComments = findViewById(R.id.btnComments);
        btnDetail = findViewById(R.id.btnDetail);
        btnClose = findViewById(R.id.btnClose);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnPlay = findViewById(R.id.btnPlay);
        playLay = findViewById(R.id.playLay);
        avBallIndicator = findViewById(R.id.avBallIndicator);
        //      rvComments = findViewById(R.id.rvComments); ;
        heartLay = findViewById(R.id.heartLay);
        txtLiveCount = findViewById(R.id.txtLiveCount);
        txtBottomDuration = findViewById(R.id.txtBottomDuration);
        parentLay = findViewById(R.id.parentLay);
        //  timeLay = findViewById(R.id.timeLay);

        reportList.add(0, "Abuse");
        reportList.add("In-Appropriate");
        reportList.add("Adult Content");


        context = PlayerActivity.this;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        //appUtils = new AppUtils(this);
        USER_AGENT = Util.getUserAgent(this, getString(R.string.app_name));
        BANDWIDTH_METER = new DefaultBandwidthMeter.Builder(this).build();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }


        getFromIntent();
        //checkUserIsActive();
        initView();
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getFromIntent() {
        Intent intent = getIntent();
        from = intent.getStringExtra(Constants.TAG_FROM);
        streamDetails = (StreamDetails) intent.getSerializableExtra(StreamConstants.TAG_STREAM_DATA);
        streamName = streamDetails.getName();
        streamUrl = streamDetails.getPlayBackUrl();
        Log.d(TAG, "streamUrl_playback" + streamUrl);
        //streamUrl = "http://media.hitasoft.in/LiveApp/streams/" + streamName + ".mp4";
        publisherId = streamDetails.getPublisherId();
/*        if (!NetworkReceiver.isConnected()) {
            App.makeToast(getString(R.string.no_internet_connection));
        }*/
    }

    private void initView() {

        Log.d(TAG, "streamDetails=> " + new Gson().toJson(streamDetails));

        initBottomDetailsDialog();
        initBottomViewersDialog();
        initBottomCommentsDialog();
        /*initBottomGiftDialog();
        initBottomHashTagDialog();*/
        avBallIndicator.setVisibility(VISIBLE);
        loadingTitle.setText(streamDetails.getTitle());
        loadingPublisherName.setText(streamDetails.postedBy);

        liveCommentsAdapter = new LiveCommentsAdapter(this, liveCommentList);
        CustomLayoutManager commentLayoutManager = new CustomLayoutManager(this);
        commentLayoutManager.setScrollEnabled(false);
        /*rvComments.setLayoutManager(commentLayoutManager);
        rvComments.setAdapter(liveCommentsAdapter);
        liveCommentsAdapter.notifyDataSetChanged();*/
        Log.i(TAG, "initView: " + streamDetails.getPostedBy());
        if (streamDetails.getPublisherName() != null) {
            txtPublisherName.setText(streamDetails.getPublisherName());
        } else if (streamDetails.getPostedBy() != null) {
            txtPublisherName.setText(streamDetails.getPostedBy());
        } else {
            txtPublisherName.setText("");
        }

        Glide.with(this)
                .load(streamDetails.getStreamThumbnail())
                .apply(new RequestOptions().error(R.drawable.rounded_square_semi_transparent)
                        .dontAnimate()
                        .transform(new BlurTransformation(25, 3)))
                .placeholder(R.drawable.alhawana)
                .into(loadingImage);

        Glide.with(this)
                .load(streamDetails.getPublisherImage())
                .placeholder(R.drawable.alhawana)
                .into(loadingPublisherImage);

        Random rnd = new Random();
        int color = Color.argb(60, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
        loadingPublisherColor.setBackgroundColor(color);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        btnPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (needRetrySource) {
                        setUpExoPlayer(streamUrl);
                    } else {
                        startPlayer();
                    }
                } else {
                    pausePlayer();
                }
            }
        });
        if (streamUrl != null) {
            setUpExoPlayer(streamUrl);
        }
        getStreamDetails(streamName);
    }

    private void setUpExoPlayer(String streamUrl) {
        HttpProxyCacheServer proxy = App.getProxy(this);
        proxy.registerCacheListener(this, streamUrl);
        String proxyUrl = proxy.getProxyUrl(streamUrl);
        Uri uri = Uri.parse("" + proxyUrl);
        boolean needNewPlayer = exoPlayer == null;
        if (needNewPlayer) {
            isPlayerInitialized = true;
            playerView.setControllerVisibilityListener(null);
            playerView.setControllerAutoShow(false);
            playerView.requestFocus();
            playerView.setKeepContentOnPlayerReset(true);
            lastSeenTracksInfo = TracksInfo.EMPTY;
            if (uri.toString().startsWith("file://")) {
                exoPlayer = new ExoPlayer.Builder(context).build();
            } else {
                final int loadControlStartBufferMs = 1500;
                /* Instantiate a DefaultLoadControl.Builder. */
                DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
                builder.setBufferDurationsMs(
                        DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                        loadControlStartBufferMs,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
                /* Build the actual DefaultLoadControl instance */
                loadControl = builder.build();
                ExoTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
                trackSelector = new DefaultTrackSelector(context, videoTrackSelectionFactory);
                exoPlayer = new ExoPlayer.Builder(context)
                        .setBandwidthMeter(BANDWIDTH_METER)
                        .setLoadControl(loadControl)
                        .setTrackSelector(trackSelector)
                        .build();
            }

            /*mediaSession = new MediaSessionCompat(this, TAG);
            mediaSessionConnector = new MediaSessionConnector(mediaSession);
            mediaSessionConnector.setPlaybackPreparer(new MyPlaybackPreparer());
            mediaSessionConnector.setPlayer(exoPlayer);*/

            exoPlayer.addListener(playerListener);
            exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true);
            exoPlayer.setPlayWhenReady(startAutoPlay);
            playerView.setPlayer(exoPlayer);

            //playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            //player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);

            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        }
        boolean haveStartPosition = startItemIndex != C.INDEX_UNSET;
        if (haveStartPosition) {
            exoPlayer.seekTo(startItemIndex, startPosition);
        }
        // Prepare the player with the source.
        MediaSource mediaSource = buildMediaSource(uri, StorageUtils.getMimeType_player("" + uri));
        exoPlayer.setMediaSource(mediaSource, !haveStartPosition);
        exoPlayer.prepare();
    }



    private void updateStartPosition() {
        if (exoPlayer != null) {
            startAutoPlay = exoPlayer.getPlayWhenReady();
            startItemIndex = exoPlayer.getCurrentMediaItemIndex();
            startPosition = Math.max(0, exoPlayer.getContentPosition());
        }
    }

    private void clearStartPosition() {
        startAutoPlay = true;
        startItemIndex = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }


    private boolean checkCachedState(String streamUrl) {
        HttpProxyCacheServer proxy = App.getProxy(this);
        return proxy.isCached(streamUrl);
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory(USE_BANDWIDTH_METER);
        Log.i(TAG, "buildMediaSource: " + type);
        switch (type) {
            case C.TYPE_SS: {
                Log.d(TAG, "buildMediaSource: smoth streeming");
                SsMediaSource.Factory ssMediaSourceFactory = new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false));
                return ssMediaSourceFactory.createMediaSource(MediaItem.fromUri(uri));
            }
            case C.TYPE_DASH: {
                Log.d(TAG, "buildMediaSource: dash");
                DashMediaSource.Factory dashMediaFactory = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false));
                return dashMediaFactory.createMediaSource(MediaItem.fromUri(uri));
            }
            case C.TYPE_HLS: {
                Log.d(TAG, "buildMediaSource: hls");
                HlsDataSourceFactory hlsDataSourceFactory = new DefaultHlsDataSourceFactory(buildDataSourceFactory(USE_BANDWIDTH_METER));
                return new HlsMediaSource.Factory(hlsDataSourceFactory)
                        .setAllowChunklessPreparation(true).createMediaSource(MediaItem.fromUri(uri));
            }
            case C.TYPE_OTHER: {
                Log.d(TAG, "buildMediaSource: others");
                if (!uri.getScheme().equals("rtmp")) {
                    if (uri.toString().startsWith("file://")) {
                        DefaultExtractorsFactory extractorsFactory =
                                new DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true);
                        DataSource.Factory dataSourceFactory =
                                new DefaultDataSourceFactory(this, USER_AGENT);
                        ProgressiveMediaSource.Factory factory = new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory);
                        return factory.createMediaSource(MediaItem.fromUri(uri));
                    } else {
                        return new ProgressiveMediaSource.Factory(buildDataSourceFactory(USE_BANDWIDTH_METER))
                                .createMediaSource(MediaItem.fromUri(uri));
                    }
                }
            }
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    /*@Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {

    }*/

    private void updatePlayControls(boolean isPlaying) {
        btnPlay.setChecked(isPlaying);
        if (!playWhenReady) {
            handler.removeCallbacks(timerUpdate);
        }
    }


    private Player.Listener playerListener = new Player.Listener() {

        @Override
        public void onPlayWhenReadyChanged(boolean whenReady, @Player.PlayWhenReadyChangeReason int reason) {
            Log.d(TAG, "onPlayWhenReadyChanged: " + whenReady + ", " + reason);
            playWhenReady = whenReady;
        }

        @Override
        public void onEvents(Player player, Player.Events events) {
            Log.d(TAG, "onEvents: " + player.isPlaying());
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Log.i(TAG, "onPlayerStateChanged: " + playbackState);
            Log.i(TAG, "onPlayerStateChanged: " + playWhenReady);
            setPlayBack(playbackState);
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            Log.i(TAG, "onIsPlayingChanged: " + isPlaying);
            updatePlayControls(isPlaying);
            if (isPlaying) {
                setPlayBack(Player.STATE_READY);
            }
        }

        @Override
        public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
            Log.i(TAG, "onPositionDiscontinuity: " + reason);
            if (needRetrySource) {
                // This will only occur if the user has performed a seek whilst in the error state. Update the
                // resume position so that if the user then retries, playback will resume from the position to
                // which they seeked.
                updateStartPosition();
            }

        }

        @Override
        public void onPlayerError(PlaybackException error) {
            try {
                String errorString = null;
                Log.i(TAG, "onPlayerError: " + error.errorCode);
                Log.e(TAG, "onPlayerError: " + error.getMessage());
                if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                    exoPlayer.seekToDefaultPosition();
                    exoPlayer.prepare();
                } else if (error.errorCode == ExoPlaybackException.TYPE_RENDERER) {
                    Exception cause = error;
                } else if (error.errorCode == ExoPlaybackException.TYPE_SOURCE) {
                    if (!NetworkReceiver.isConnected()) {
                        App.makeToast(getString(R.string.no_internet_connection));
                    } else {
                        //App.makeToast("Source error");
                        App.makeToast("Please wait, Video progressing");
                    }
                    finish();
                }
                needRetrySource = true;
                if (isBehindLiveWindow(error)) {
                    startAutoPlay = false;
                    clearStartPosition();
                    play(null);
                } else {
                    updateStartPosition();
                }
            } catch (Exception exception) {
                Log.e(TAG, "onPlayerError: " + exception.getMessage());
            }
        }
    };

    private void setPlayBack(int playBackState) {
        if (playWhenReady && playBackState == Player.STATE_READY) {
            btnPlay.setChecked(true);
            handler.post(timerUpdate);
            if (!durationSet) {
                if (from != null && !from.equals(Constants.TAG_HISTORY)&&!from.equalsIgnoreCase(Constants.TAG_SUBSCRIBE)) {
                    //  updateWatchCount();
                }
                durationSet = true;
                long duration = exoPlayer.getDuration();
                if (TimeUnit.MILLISECONDS.toHours(exoPlayer.getDuration()) != 0)
                    totalTime = String.format(Locale.ENGLISH, "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration),
                            TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                            TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
                else
                    totalTime = String.format(Locale.ENGLISH, "%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                            TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
                txtBottomDuration.setText(totalTime);
            }
            loadingImage.setVisibility(GONE);
            loadingLay.setVisibility(GONE);
            avBallIndicator.setVisibility(GONE);
        } else if (playBackState == Player.STATE_ENDED) {
            btnRefresh.setVisibility(VISIBLE);
            btnPlay.setVisibility(GONE);
            handler.removeCallbacks(timerUpdate);
            durationSet = false;
        } else if (playBackState == Player.STATE_BUFFERING) {
            avBallIndicator.setVisibility(VISIBLE);
            handler.removeCallbacks(timerUpdate);
        }
    }

    public void play(View view) {
        setUpExoPlayer(streamUrl);
    }

    private static boolean isBehindLiveWindow(PlaybackException e) {
        if (e.errorCode != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private void setCommentUI(StreamDetails.Comments comments) {
//        Logging.d(TAG, "setCommentUI: " + new Gson().toJson(comments));
        String type = comments.getType();
        switch (type) {
            case StreamConstants.TAG_STREAM_JOINED: {
                HashMap<String, String> map = new HashMap<>();
                map.put(Constants.TAG_USER_ID, comments.getUserId());
                map.put(Constants.TAG_USER_NAME, comments.getUserName());
                map.put(Constants.TAG_USER_IMAGE, comments.getUserImage());
                map.put(Constants.TAG_MESSAGE, comments.getMessage());
                map.put(Constants.TAG_TYPE, comments.getType());
                addComment(map);
            }
            break;
            case Constants.TAG_TEXT: {
                HashMap<String, String> map = new HashMap<>();
                map.put(Constants.TAG_USER_ID, comments.getUserId());
                map.put(Constants.TAG_USER_NAME, comments.getUserName());
                map.put(Constants.TAG_USER_IMAGE, comments.getUserImage());
                map.put(Constants.TAG_MESSAGE, comments.getMessage());
                map.put(Constants.TAG_TYPE, comments.getType());
                map.put(Constants.TAG_VISIBILITY, Constants.TAG_TRUE);
                addComment(map);
            }
            break;
            case StreamConstants.TAG_LIKED: {
                HashMap<String, String> map = new HashMap<>();
                map.put(Constants.TAG_USER_ID, comments.getUserId());
                map.put(Constants.TAG_USER_NAME, comments.getUserName());
                map.put(Constants.TAG_USER_IMAGE, comments.getUserImage());
                map.put(Constants.TAG_MESSAGE, comments.getMessage());
                map.put(Constants.TAG_TYPE, Constants.TAG_MESSAGE);
                try {
                    String userId = comments.getUserId();
                    String likeColor = comments.getLikeColor();
                    Log.i(TAG, "setCommentUI: " + likeColor);
                    heartLay.addHeart(Color.parseColor(likeColor));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
/*
            case Constants.TAG_GIFT: {
                HashMap<String, String> map = new HashMap<>();
                map.put(Constants.TAG_USER_ID, comments.getUserId());
                map.put(Constants.TAG_USER_NAME, comments.getUserName());
                map.put(Constants.TAG_USER_IMAGE, comments.getUserImage());
                map.put(StreamConstants.TAG_STREAM_NAME, streamName);
                map.put(Constants.TAG_GIFT_TITLE, comments.getGiftTitle());
                map.put(Constants.TAG_GIFT_ICON, comments.getGiftIcon());
                map.put(Constants.TAG_TYPE, comments.getType());
                addComment(map);
            }
            break;
*/
            default:
                break;
        }
    }

    private void addComment(HashMap<String, String> map) {
        liveCommentList.add(map);
        liveCommentsAdapter.notifyItemInserted(liveCommentList.size() - 1);
        //rvComments.scrollToPosition(liveCommentList.size() - 1);
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(context, null, buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSource.Factory();
    }


    /*private void checkUserIsActive() {
        Call<HashMap<String, String>> call = apiInterface.isActive(GetSet.getUserId());
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                HashMap<String, String> Response = response.body();
                if (response.isSuccessful()) {
                    if (Response.get(Constants.TAG_STATUS).equals(Constants.TAG_FALSE)) {
                        App.makeToast(getString(R.string.account_deactivated_by_admin));
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(PlayerActivity.this).clearDB();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }*/

/*    private void updateWatchCount() {
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
        requestMap.put(StreamConstants.TAG_STREAM_ID, streamDetails.getStreamId());

        Call<HashMap<String, String>> call = apiInterface.updateWatchCount(requestMap);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                HashMap<String, String> Response = response.body();
                if (response.isSuccessful()) {

                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }*/

    private void initBottomDetailsDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_details, null);
        bottomDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomDialog.setContentView(bottomSheet);

        bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        bottomFirstLay = bottomSheet.findViewById(R.id.bottomFirstLay);
        bottomDeleteLay = bottomSheet.findViewById(R.id.bottomDeleteLay);
        liveTxtLay = bottomSheet.findViewById(R.id.liveTxtLay);
        txtLiveCount = bottomSheet.findViewById(R.id.txtLiveCount);
        bottomStreamTitle = bottomSheet.findViewById(R.id.bottomStreamTitle);
        /*hashTagsLay = bottomSheet.findViewById(R.id.hashTagsLay);
        hashtagView = bottomSheet.findViewById(R.id.hashtagView);
        btnMoreHashTag = bottomSheet.findViewById(R.id.btnMoreHashTag);*/
        bottomUserLay = bottomSheet.findViewById(R.id.bottomUserLay);
        publisherImage = bottomSheet.findViewById(R.id.publisherImage);
        publisherColorImage = bottomSheet.findViewById(R.id.publisherColorImage);
        txtPublisherName = bottomSheet.findViewById(R.id.txtPublisherName);
/*        btnFollow = bottomSheet.findViewById(R.id.btnFollow);
        btnUnFollow = bottomSheet.findViewById(R.id.btnUnFollow);*/
        bottomDetailsLay = bottomSheet.findViewById(R.id.bottomDetailsLay);
        chatHideLay = bottomSheet.findViewById(R.id.chatHideLay);
        bottomReportLay = bottomSheet.findViewById(R.id.bottomReportLay);
        txtReport = bottomSheet.findViewById(R.id.txtReport);
        liveStatusLay = bottomSheet.findViewById(R.id.liveStatusLay);
        liveCountLay = bottomSheet.findViewById(R.id.liveCountLay);
/*
        bottomGiftDetailsLay = bottomSheet.findViewById(R.id.bottomGiftDetailsLay);
        bottomInternalShareLay = bottomSheet.findViewById(R.id.bottomInternalShareLay);
        bottomExternalShareLay = bottomSheet.findViewById(R.id.bottomExternalShareLay);
*/
        bottomStreamTitle.setText(streamDetails.getTitle() != null ? streamDetails.getTitle() : "");
        bottomUserLay.setVisibility(VISIBLE);
        liveStatusLay.setVisibility(GONE);
        chatHideLay.setVisibility(GONE);
        //btnFollow.setVisibility(GONE);

        if (LocaleManager.isRTL()) {
            liveTxtLay.setBackground(context.getResources().getDrawable(R.drawable.live_status_bg_rtl));
            liveCountLay.setBackground(context.getResources().getDrawable(R.drawable.live_count_bg_rtl));
        } else {
            liveTxtLay.setBackground(context.getResources().getDrawable(R.drawable.live_status_bg));
            liveCountLay.setBackground(context.getResources().getDrawable(R.drawable.live_count_bg));
        }

        if (("" + streamDetails.getPublisherId()).equals(GetSet.getUserId())) {
            bottomDeleteLay.setVisibility(VISIBLE);
            bottomReportLay.setVisibility(GONE);
        } else {
            bottomReportLay.setVisibility(VISIBLE);
            bottomDeleteLay.setVisibility(GONE);
        }

        /*appUtils.setHashTagItemPadding(hashtagView);
        hashtagView.setEllipsize(HashtagView.ELLIPSIZE_END);
        hashtagView.setRowCount(1);*/
        /*btnMoreHashTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomHashTagsDialog != null && !bottomHashTagsDialog.isShowing()) {
                    bottomHashTagsDialog.show();
                }
            }
        });*/

        bottomDetailsLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                if (bottomViewersDialog != null && !bottomViewersDialog.isShowing()) {
                    bottomViewersDialog.show();
                }
            }
        });

/*        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkReceiver.isConnected()) {
                    String blockStatus = DBHelper.getInstance(PlayerActivity.this).getBlockStatus(GetSet.getUserId() + publisherId);
                    if (blockStatus.equals(Constants.TAG_TRUE)) {
                        App.makeToast(getString(R.string.unblock_follow_description));
                    } else {
                        followUnfollowUser(publisherId, true);
                    }
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                }
            }
        });

        btnUnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkReceiver.isConnected()) {
                    followUnfollowUser(publisherId, false);
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                }
            }
        });*/

        bottomDeleteLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDeleteDialog(getString(R.string.really_delete_video));
            }
        });

        bottomReportLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomDialog != null && bottomDialog.isShowing()) {
                    bottomDialog.dismiss();
                }
                if (apiStreamData == null) {
                    openReportDialog();
                } else if (apiStreamData.getReported() != null && apiStreamData.getReported().equalsIgnoreCase(Constants.TAG_FALSE)) {
                    openReportDialog();
                } else {
                    sendReport("", false);
                }
            }
        });

        bottomDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        bottomDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        btnDetail.setOnClickListener(v -> {
            if (bottomDialog != null && !bottomDialog.isShowing())
                bottomDialog.show();
        });

        playLay.setOnClickListener(v -> {
            btnPlay.setChecked(!btnPlay.isChecked());
        });

        btnRefresh.setOnClickListener(v -> {
            btnRefresh.setVisibility(GONE);
            btnPlay.setVisibility(VISIBLE);
            if (exoPlayer != null) {
                exoPlayer.seekTo(0);
            }
            btnPlay.setChecked(true);
        });

        btnComments.setOnClickListener(v -> {

            if (bottomCommentsDialog != null && !bottomCommentsDialog.isShowing()) {
                bottomCommentsDialog.show();
            }

        });


        /* R.id.btnDetail, R.id.playLay, R.id.btnRefresh, R.id.btnComments*/




       /* bottomGiftDetailsLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                if (bottomGiftsDialog != null && !bottomGiftsDialog.isShowing()) {
                    bottomGiftsDialog.show();
                }
            }
        });*/

        /*bottomInternalShareLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                *//*if (apiStreamData != null) {

                    isPlayerInitialized = false;
                    btnPlay.setChecked(false);
                    pausePlayer();
                    Intent intent = new Intent(PlayerActivity.this, MutualListActivity.class);
                    intent.putExtra(Constants.TAG_TEXT, apiStreamData.getInviteLink());
                    intent.putExtra(StreamConstants.TAG_STREAM_NAME, streamName);
                    intent.putExtra(Constants.TAG_TITLE, apiStreamData.getTitle());
                    intent.putExtra(Constants.TAG_USER_NAME, apiStreamData.getPublisherName());
                    intent.putExtra(Constants.TAG_USER_IMAGE, apiStreamData.getPublisherImage());
                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_SHARE);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivityForResult(intent, Constants.MUTUAL_REQUEST_CODE);
                } else {
                    if (!NetworkReceiver.isConnected())
                        App.makeToast(getString(R.string.no_internet_connection));
                    else App.makeToast(getString(R.string.something_went_wrong));
                }*//*
            }
        });

        bottomExternalShareLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (apiStreamData != null) {

//                String msg = GetSet.getName() + " " + getString(R.string.stream_share_description) + " " + apiStreamData.getInviteLink();
*//*                    String msg = apiStreamData.getInviteLink();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.join) + " " + getString(R.string.app_name));
                    intent.putExtra(Intent.EXTRA_TEXT, msg);
                    startActivity(Intent.createChooser(intent, getString(R.string.share_link)));*//*
                } else {
                    *//*if (!NetworkReceiver.isConnected())
                        App.makeToast(getString(R.string.no_internet_connection));
                    else App.makeToast(getString(R.string.something_went_wrong));*//*
                }
            }
        });*/
    }

/*    private void setFollowButton(boolean status) {
        if (status) {
            btnUnFollow.setVisibility(VISIBLE);
            btnFollow.setVisibility(GONE);
        } else {
            btnUnFollow.setVisibility(GONE);
            btnFollow.setVisibility(VISIBLE);
        }
    }*/

/*    private void followUnfollowUser(String publisherId, boolean follow) {
        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            followRequest.setUserId(publisherId);
            followRequest.setFollowerId(GetSet.getUserId());
            if (follow) {
                followRequest.setType(Constants.TAG_FOLLOW_USER);
            } else {
                followRequest.setType(Constants.TAG_UNFOLLOW_USER);
            }
            Call<FollowResponse> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<FollowResponse>() {
                @Override
                public void onResponse(Call<FollowResponse> call, Response<FollowResponse> response) {
                    FollowResponse followResponse = response.body();
                    if (followResponse.getStatus().equals(Constants.TAG_TRUE)) {
                        if (followRequest.getType().equals(Constants.TAG_FOLLOW_USER)) {
//                            App.makeToast(getString(R.string.followed_successfully));
                            setFollowButton(true);
                        } else {
                            setFollowButton(false);
                        }
                    }
                }

                @Override
                public void onFailure(Call<FollowResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }*/

    private void initBottomViewersDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_viewers, null);
        bottomViewersDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomViewersDialog.setContentView(bottomSheet);

        bottomViewersLay = bottomSheet.findViewById(R.id.bottomViewersLay);
        bottomViewerCount = bottomSheet.findViewById(R.id.bottomViewerCount);
        bottomRecyclerView = bottomSheet.findViewById(R.id.bottomRecyclerView);
        txtBottomDuration = bottomSheet.findViewById(R.id.txtBottomDuration);
        bottomDurationLay = bottomSheet.findViewById(R.id.bottomDurationLay);
        bottomLikesLay = bottomSheet.findViewById(R.id.bottomLikesLay);
        txtTotalLikes = bottomSheet.findViewById(R.id.txtTotalLikes);

        bottomLikesLay.setVisibility(VISIBLE);
        bottomViewersLay.setVisibility(GONE);

        txtTotalLikes.setText(streamDetails.getLikes() != null ? streamDetails.getLikes() : "0");
        bottomViewersDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        bottomViewersDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
    }

    private void initBottomCommentsDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_comments, null);
        bottomCommentsDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomCommentsDialog.setContentView(bottomSheet);

        bottomCommentsView = bottomSheet.findViewById(R.id.bottomCommentsView);
        txtNoCommands = bottomSheet.findViewById(R.id.txtNoCommands);
        bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

        commentsAdapter = new CommentsAdapter(this, commentList);
        bottomCommentsView.setLayoutManager(new LinearLayoutManager(this));
        bottomCommentsView.setAdapter(commentsAdapter);
        bottomCommentsView.setHasFixedSize(true);
        commentsAdapter.notifyDataSetChanged();

        /*if (commentList.size() == 0) {
            txtNoCommands.setVisibility(VISIBLE);
            bottomCommentsView.setVisibility(GONE);
        } else {
            txtNoCommands.setVisibility(GONE);
            bottomCommentsView.setVisibility(VISIBLE);
        }*/

        bottomCommentsDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet1 = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet1).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet1).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet1).setHideable(true);
        });

        bottomCommentsDialog.setOnDismissListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet12 = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet12).setState(BottomSheetBehavior.STATE_COLLAPSED);
            BottomSheetBehavior.from(bottomSheet12).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet12).setHideable(true);
        });
    }

    /*private void initBottomGiftDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_gifts, null);
        bottomGiftsDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomGiftsDialog.setContentView(bottomSheet);

        bottomGiftsLay = bottomSheet.findViewById(R.id.bottomGiftsLay);
        bottomGiftCount = bottomSheet.findViewById(R.id.bottomGiftCount);
        bottomGiftView = bottomSheet.findViewById(R.id.bottomGiftView);

        giftsAdapter = new GiftsAdapter(this, giftsList);
        bottomGiftView.setLayoutManager(new LinearLayoutManager(this));
        bottomGiftView.setAdapter(giftsAdapter);
        bottomGiftView.setHasFixedSize(true);
        giftsAdapter.notifyDataSetChanged();

        bottomGiftsDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        bottomGiftsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
    }

    private void initBottomHashTagDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_hashtags, null);
        bottomHashTagsDialog = new BottomSheetDialog(this, R.style.Bottom_HashTagDialog); // Style here
        bottomHashTagsDialog.setContentView(bottomSheet);

        bottomHashTagView = bottomSheet.findViewById(R.id.bottomHashTagView);
        appUtils.setHashTagItemPadding(bottomHashTagView);

        bottomHashTagsDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        bottomHashTagsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
    }*/

    private void openReportDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet_report_addon, null);
        reportDialog = new BottomSheetDialog(PlayerActivity.this, R.style.Bottom_StreamDialog); // Style here
        reportDialog.setContentView(bottomSheet);


        ((View) bottomSheet.getParent()).setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlack));
        bottomSheet.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        WindowManager.LayoutParams params = reportDialog.getWindow().getAttributes();
        params.x = 0;
        reportDialog.getWindow().setAttributes(params);
        bottomSheet.requestLayout();
        reportsView = bottomSheet.findViewById(R.id.reportView);
        reportDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        reportDialog.setOnDismissListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet1 = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet1).setState(BottomSheetBehavior.STATE_COLLAPSED);
            BottomSheetBehavior.from(bottomSheet1).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet1).setHideable(true);
        });

        loadReports();
    }

    private void loadReports() {
        reportAdapter = new ReportAdapter(this, AdminData.livestream_reports);
        reportsView.setLayoutManager(new LinearLayoutManager(this));
        reportsView.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        reportDialog.show();
    }

    public void getStreamDetails(String streamName) {
        if (NetworkReceiver.isConnected()) {
            Call<StreamDetails> call3 = apiInterface.getStreamDetails(GetSet.getToken(), GetSet.getUserId(), streamName);
            call3.enqueue(new Callback<StreamDetails>() {
                @Override
                public void onResponse(Call<StreamDetails> call, Response<StreamDetails> response) {
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            apiStreamData = response.body();
                            if (apiStreamData.getStreamBlocked() == 1) {
                                //App.makeToast(getString(R.string.broadcast_deactivated_by_admin));
                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                            txtTotalLikes.setText(apiStreamData.getLikes());
                            bottomStreamTitle.setText(apiStreamData.getTitle());
                            bottomViewerCount.setText(apiStreamData.getWatchCount());
                            if (!("" + apiStreamData.getPublisherId()).equals(GetSet.getUserId())) {
                                bottomUserLay.setVisibility(VISIBLE);
                                txtPublisherName.setText(apiStreamData.getPostedBy());
                                Glide.with(getApplicationContext())
                                        .load(apiStreamData.getPublisherImage())
                                        .error(R.drawable.no_video)
                                        .into(publisherImage);
                                //setFollowButton(apiStreamData.getFollow().equals(Constants.TAG_TRUE));
                                txtReport.setText(apiStreamData.getReported().equals(Constants.TAG_FALSE) ? getString(R.string.report_broadcast) :
                                        getString(R.string.undo_report_broadcast));
                            }
                            //     setHashTagUI(apiStreamData.getHashTags());
                            commentList.clear();
                            tempCommentList.addAll(apiStreamData.getCommentsList());
                            timeList = new ArrayList<>();
                            for (StreamDetails.Comments comment : apiStreamData.getCommentsList()) {
                                if (!comment.getType().equals(StreamConstants.TAG_LIKED) &&
                                        !comment.getType().equals(StreamConstants.TAG_STREAM_JOINED)) {
                                    commentList.add(comment);
                                }
                                timeList.add(comment.getTime());
                            }
                            commentsAdapter.notifyDataSetChanged();


                            if (commentList.size() == 0) {
                                txtNoCommands.setVisibility(VISIBLE);
                                bottomCommentsView.setVisibility(GONE);
                            } else {
                                txtNoCommands.setVisibility(GONE);
                                bottomCommentsView.setVisibility(VISIBLE);
                            }


                         /*   giftsList.clear();
                            giftsList.addAll(apiStreamData.getGiftList());
                            bottomGiftCount.setText("" + giftsList.size());
                            giftsAdapter.notifyDataSetChanged();*/
                        } else {
                            if (response.body().getMessage() != null && response.body().getMessage().equals("No Stream found")) {
//                                App.makeToast(getString(R.string.no_stream_found));
                                finish();
                            } else {
                                // App.makeToast(getString(R.string.something_went_wrong));
                                finish();
                            }
                        }
                    } else {
                        finish();
                        //App.makeToast(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(Call<StreamDetails> call, Throwable t) {
                    call.cancel();
                    Log.e(TAG, "onFailure: " + t.getMessage());
                }
            });
        }
    }

    /*private void setHashTagUI(ArrayList<String> hashTags) {
        if (hashTags.size() > 0) {
            hashTagsLay.setVisibility(VISIBLE);
            if (hashTags.size() >= 2) {
                btnMoreHashTag.setVisibility(GONE);
                List<String> temp = new ArrayList<>();
                temp.add(hashTags.get(0));
                temp.add(hashTags.get(1));
                if (hashTags.size() > 2) {
                    btnMoreHashTag.setVisibility(VISIBLE);
                }

                hashtagView.setData(temp, new HashtagView.DataTransform<String>() {
                    @Override
                    public CharSequence prepare(String item) {
                        return context.getString(R.string.hash_symbol) + item;
                    }
                });
            } else {
                hashtagView.setData(hashTags, new HashtagView.DataTransform<String>() {
                    @Override
                    public CharSequence prepare(String item) {
                        return context.getString(R.string.hash_symbol) + item;
                    }
                });
                btnMoreHashTag.setVisibility(GONE);
            }
            bottomHashTagView.setData(hashTags, new HashtagView.DataTransform<String>() {
                @Override
                public CharSequence prepare(String item) {
                    return context.getString(R.string.hash_symbol) + item;
                }
            });
        } else {
            hashTagsLay.setVisibility(GONE);
        }
    }*/

    /*public void deleteVideo() {
        if (NetworkReceiver.isConnected()) {
            Call<Map<String, String>> call = apiInterface.deleteVideo(GetSet.getUserId(), streamDetails.getName());
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (from != null && from.equals(Constants.TAG_TRENDING)) {
                                isStreamDeleted = true;
                                finish();
                            } else {
                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                                App.makeToast(getString(R.string.video_deleted_successfully));
                                finish();
                            }
                        } else {
                            App.makeToast(getString(R.string.something_went_wrong));
                        }
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
//        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
//        App.makeToast(getString(R.string.no_internet_connection));
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*        if (requestCode == Constants.MUTUAL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (bottomDialog != null && bottomDialog.isShowing()) {
                    bottomDialog.dismiss();
                }
            }
            if (btnPlay.isChecked()) {
                startPlayer();
            } else {
                pausePlayer();
            }
        }*/
    }

   /* @OnClick({R.id.btnDetail, R.id.playLay, R.id.btnRefresh, R.id.btnComments,
            R.id.btnClose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnDetail:
                if (bottomDialog != null && !bottomDialog.isShowing())
                    bottomDialog.show();
                break;
            case R.id.playLay:
                btnPlay.setChecked(!btnPlay.isChecked());
                break;
            case R.id.btnRefresh:
                btnRefresh.setVisibility(GONE);
                btnPlay.setVisibility(VISIBLE);
                if (player != null) {
                    player.seekTo(0);
                }
                btnPlay.setChecked(true);
                break;
            case R.id.btnComments:
                if (bottomCommentsDialog != null && !bottomCommentsDialog.isShowing()) {
                    bottomCommentsDialog.show();
                }
                break;
            case R.id.btnClose:
                onBackPressed();
                break;
            default:
                break;
        }
    }*/

    private void openDeleteDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.yes_txt), (dialog, which) -> {
            dialog.cancel();
            deleteVideo();
        });
        builder.setNegativeButton(getString(R.string.no_txt), (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();
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

    public void deleteVideo() {
        Call<Map<String, String>> call = apiInterface.deleteVideo(GetSet.getToken(), GetSet.getUserId(), streamDetails.getName());
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        isStreamDeleted = true;
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        Toast.makeText(PlayerActivity.this, getString(R.string.video_deleted_successfully), Toast.LENGTH_SHORT).show();
                        /*                        makeToast(getString(R.string.video_deleted_successfully));*/
                        finish();
                    }
                } else {
                    Toast.makeText(PlayerActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    /*                    makeToast(getString(R.string.something_wrong));*/
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        releasePlayer();
        shouldAutoPlay = true;
        clearResumePosition();
        setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pauseExternalAudio(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(timerUpdate);
        pausePlayer();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(timerUpdate);
        releasePlayer();
        btnPlay.setChecked(false);
        resumeExternalAudio();
    }

    public void resumeExternalAudio() {
        if (Constants.isExternalPlay) {
            Constants.isExternalPlay = false;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "play");
            context.sendBroadcast(i);
        }
    }

    public void pauseExternalAudio(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            Constants.isExternalPlay = true;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            context.sendBroadcast(i);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getProxy(this).unregisterCacheListener(this);
    }

    private void releasePlayer() {
        if (Util.SDK_INT <= 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            if (exoPlayer != null) {
                shouldAutoPlay = exoPlayer.getPlayWhenReady();
                updateResumePosition();
                exoPlayer.release();
                exoPlayer = null;
                trackSelector = null;
            }
        }
    }

    private void updateResumePosition() {
        resumeWindow = exoPlayer.getCurrentWindowIndex();
        resumePosition = exoPlayer.isCurrentWindowSeekable() ? Math.max(0, exoPlayer.getContentPosition())
                : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    private void pausePlayer() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.getPlaybackState();
        }
    }


    private void startPlayer() {
        if (exoPlayer != null) {
            loadingImage.setVisibility(GONE);
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.getPlaybackState();
        }
    }

    @Override
    public void onVisibilityChange(int visibility) {

    }

    @Override
    public void preparePlayback() {
        Log.i(TAG, "preparePlayback: ");
        exoPlayer.retry();
    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {

    }

    public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<StreamDetails.Comments> commentList;
        Context context;
        Random rnd;
        public final int VIEW_TYPE_JOIN = 0;
        public final int VIEW_TYPE_TEXT = 1;
        public final int VIEW_TYPE_GIFT = 2;

        public CommentsAdapter(Context context, ArrayList<StreamDetails.Comments> commentList) {
            this.commentList = commentList;
            this.context = context;
            rnd = new Random();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_JOIN) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_join, parent, false);
                return new JoiningViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_TEXT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
                return new MyViewHolder(view);
            } /*else if (viewType == VIEW_TYPE_GIFT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live_gift, parent, false);
                return new GiftViewHolder(view);
            }*/

            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            if (viewHolder instanceof MyViewHolder) {
                final MyViewHolder holder = (MyViewHolder) viewHolder;
                StreamDetails.Comments comments = commentList.get(position);
                holder.txtMessage.setText(comments.getMessage());
                holder.txtUserName.setText("@" + comments.getUserName());

            } else if (viewHolder instanceof JoiningViewHolder) {
                final JoiningViewHolder joinViewHolder = (JoiningViewHolder) viewHolder;
                joinViewHolder.itemLay.setVisibility(GONE);
                StreamDetails.Comments comments = commentList.get(position);
                joinViewHolder.txtJoined.setText("@" + comments.getUserName() + " Joined");
            } /*else if (viewHolder instanceof GiftViewHolder) {
                final GiftViewHolder holder = (GiftViewHolder) viewHolder;
                StreamDetails.Comments comments = commentList.get(position);
                Glide.with(context)
                        .load(Constants.IMAGE_URL + comments.getGiftIcon())
                        .error(R.drawable.gift)
                        .placeholder(R.drawable.gift)
                        .into(holder.giftImage);
                if (LocaleManager.isRTL()) {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, 20));
                } else {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, -20));
                }
                String userName = "@" + comments.getUserName() + " " + getString(R.string.sent) + "\n";
                String giftTitle = "\"" + comments.getGiftTitle() + "\"";
                String gift = " " + getString(R.string.gift);
                Spannable spannable = new SpannableString(userName + giftTitle + gift);
                spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), userName.length(), (userName + giftTitle).length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.txtGiftName.setText(spannable, TextView.BufferType.SPANNABLE);
            }*/
        }

        @Override
        public int getItemCount() {
            return commentList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (commentList.get(position) != null) {
                String type = "" + commentList.get(position).getType();
                switch (type) {
                    case Constants.TAG_TEXT:
                        return VIEW_TYPE_TEXT;
                    case StreamConstants.TAG_STREAM_JOINED:
                        return VIEW_TYPE_JOIN;
                    /*case Constants.TAG_GIFT:
                        return VIEW_TYPE_GIFT;*/
                }
            }
            return VIEW_TYPE_TEXT;
        }

        public class JoiningViewHolder extends RecyclerView.ViewHolder {

            TextView txtJoined;

            FrameLayout itemLay;

            public JoiningViewHolder(View view) {
                super(view);

                txtJoined = view.findViewById(R.id.txtJoined);
                itemLay = view.findViewById(R.id.itemLay);
                //ButterKnife.bind(this, view);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView txtUserName;

            TextView txtMessage;

            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                //  ButterKnife.bind(this, view);

                txtUserName = view.findViewById(R.id.txtUserName);
                txtMessage = view.findViewById(R.id.txtMessage);
                itemLay = view.findViewById(R.id.itemLay);
            }
        }

        /*public class GiftViewHolder extends RecyclerView.ViewHolder {

            TextView txtGiftName;

            ImageView giftImage;

            RelativeLayout giftLay;

            public GiftViewHolder(View view) {
                super(view);
                //ButterKnife.bind(this, view);
            }
        }*/
    }

/*    public class GiftsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<StreamDetails.Gifts> giftsList = new ArrayList<>();
        Context context;
        Random rnd;

        public GiftsAdapter(Context context, ArrayList<StreamDetails.Gifts> giftsList) {
            this.giftsList = giftsList;
            this.context = context;
            rnd = new Random();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gifts_received, parent, false);
            return new GiftViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            if (viewHolder instanceof GiftViewHolder) {
                final GiftViewHolder holder = (GiftViewHolder) viewHolder;
                StreamDetails.Gifts gift = giftsList.get(position);
                Glide.with(context)
                        .load(Constants.IMAGE_URL + gift.getGiftIcon())
                        .error(R.drawable.gift)
                        .placeholder(R.drawable.gift)
                        .into(holder.giftImage);
                if (LocaleManager.isRTL()) {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, 20));
                } else {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, -20));
                }

                Glide.with(context)
                        .load(Constants.IMAGE_URL + gift.getUserImage())
                        .error(R.drawable.no_video)
                        .placeholder(R.drawable.no_video)
                        .into(holder.userImage);
                int color = Color.argb(60, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
                holder.colorImage.setBackgroundColor(color);
                String userName = "@" + gift.getUserName() + " " + getString(R.string.sent) + "\n";
                String giftTitle = "\"" + gift.getGiftTitle() + "\"";
                String strGift = " " + getString(R.string.gift);
                Spannable spannable = new SpannableString(userName + giftTitle + strGift);
                spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), userName.length(), (userName + giftTitle).length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.txtGiftName.setText(spannable, TextView.BufferType.SPANNABLE);
            }
        }

        @Override
        public int getItemCount() {
            return giftsList.size();
        }

        public class GiftViewHolder extends RecyclerView.ViewHolder {

            RoundedImageView userImage;

            RoundedImageView colorImage;

            FrameLayout profileLay;

            AppCompatTextView txtGiftName;

            ImageView giftImage;

            ConstraintLayout itemLay;

            public GiftViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);

                itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isPlayerInitialized = false;
                        btnPlay.setChecked(false);
                        pausePlayer();
                        if (giftsList.get(getAdapterPosition()).userId.equals(GetSet.getUserId())) {
                            Intent profile = new Intent(context, MyProfileActivity.class);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            profile.putExtra(Constants.TAG_PARTNER_ID, giftsList.get(getAdapterPosition()).getUserId());
                            profile.putExtra(Constants.TAG_FROM, Constants.TAG_SEARCH);
                            startActivity(profile);
                        } else {
                            Intent profile = new Intent(context, OthersProfileActivity.class);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            profile.putExtra(Constants.TAG_PARTNER_ID, giftsList.get(getAdapterPosition()).getUserId());
                            profile.putExtra(Constants.TAG_PARTNER_NAME, giftsList.get(getAdapterPosition()).getUserName());
                            profile.putExtra(Constants.TAG_PARTNER_IMAGE, giftsList.get(getAdapterPosition()).getUserImage());
                            profile.putExtra(Constants.TAG_FROM, Constants.TAG_SEARCH);
                            startActivity(profile);
                        }
                    }
                });
            }
        }
    }*/

    public class ReportAdapter extends RecyclerView.Adapter {
        private final Context context;
        private List<Report> reportList = new ArrayList<>();
        private RecyclerView.ViewHolder viewHolder;

        public ReportAdapter(Context context, List<Report> reportList) {
            this.context = context;
            this.reportList = reportList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_report_addon, parent, false);
            viewHolder = new MyViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MyViewHolder) holder).txtReport.setText(reportList.get(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return reportList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView txtReport;

            LinearLayout itemReportLay;

            public MyViewHolder(View view) {
                super(view);
                //ButterKnife.bind(this, view);
                txtReport = view.findViewById(R.id.txtReport);
                itemReportLay = view.findViewById(R.id.itemReportLay);

                itemReportLay.setOnClickListener(v -> {

                    reportDialog.dismiss();
                    if (bottomDialog != null && bottomDialog.isShowing()) {
                        bottomDialog.dismiss();
                    }
                    sendReport(String.valueOf(reportList.get(getAdapterPosition()).getTitle()), true);
                });

            }




/*            @OnClick(R.id.itemReportLay)
            public void onViewClicked() {

                reportDialog.dismiss();
                if (bottomDialog != null && bottomDialog.isShowing()) {
                    bottomDialog.dismiss();
                }
                sendReport(reportList.get(getAdapterPosition()).getTitle(), true);
            }*/

        }


    }

/*    private class Report {

        @SerializedName("title")
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }*/

    private void sendReport(String title, boolean report) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_NAME, streamName);
            map.put(Constants.TAG_TYPE, Constants.TAG_LIVE_STREAMING);
            if (report) {
                map.put(Constants.TAG_REPORT, title);
            }

            Log.d(TAG, "sendReport: params" + new Gson().toJson(map));

            Call<Map<String, String>> call = apiInterface.reportStream(GetSet.getToken(), map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> reportResponse = response.body();
                        if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (report) {
                                Toast.makeText(PlayerActivity.this, getString(R.string.reported_successfully), Toast.LENGTH_SHORT).show();
                                if (apiStreamData != null) {
                                    apiStreamData.setReported(Constants.TAG_TRUE);
                                }
                                txtReport.setText(getString(R.string.undo_report_broadcast));
                            } else {
                                Log.d(TAG, "onResponse_report: "+report);
                                Toast.makeText(PlayerActivity.this, getString(R.string.undo_report_successfully), Toast.LENGTH_SHORT).show();
                                if (apiStreamData != null) {
                                    apiStreamData.setReported(Constants.TAG_FALSE);
                                }
                                txtReport.setText(getString(R.string.report_broadcast));
                            }
                            if (bottomDialog != null && bottomDialog.isShowing()) {
                                bottomDialog.dismiss();
                            }
                        }
                    } else {
                        Toast.makeText(PlayerActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                    //  App.makeToast(getString(R.string.something_went_wrong));
                }
            });
        } else {
            //App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    public class LiveCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public final int VIEW_TYPE_JOIN = 0;
        public final int VIEW_TYPE_TEXT = 1;
        public final int VIEW_TYPE_GIFT = 2;
        ArrayList<HashMap<String, String>> commentList;
        Context context;

        public LiveCommentsAdapter(Context context, ArrayList<HashMap<String, String>> commentList) {
            this.commentList = commentList;
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return commentList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (commentList.get(position) != null) {
                String type = "" + commentList.get(position).get(Constants.TAG_TYPE);
                switch (type) {
                    case Constants.TAG_TEXT:
                        return VIEW_TYPE_TEXT;
                    case StreamConstants.TAG_STREAM_JOINED:
                        return VIEW_TYPE_JOIN;
                   /* case Constants.TAG_GIFT:
                        return VIEW_TYPE_GIFT;*/
                }
            }
            return VIEW_TYPE_TEXT;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_JOIN) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_join, parent, false);
                return new JoiningViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_TEXT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
                return new MyViewHolder(view);
            } /*else if (viewType == VIEW_TYPE_GIFT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live_gift, parent, false);
                return new GiftViewHolder(view);
            }*/
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

            if (viewHolder instanceof MyViewHolder) {
                final MyViewHolder holder = (MyViewHolder) viewHolder;
                final HashMap<String, String> map = commentList.get(position);
                holder.txtMessage.setText(map.get(Constants.TAG_MESSAGE));
                holder.txtUserName.setText("@" + map.get(Constants.TAG_USER_NAME));
                new CountDownTimer(5000, 1000) { //(timer_duration, timer_interval)
                    @Override
                    public void onTick(long millisUntilFinished) {
                        holder.itemLay.setVisibility(VISIBLE);
                        //runs every second (1000 ms)
                    }

                    @Override
                    public void onFinish() {
                        //Do your operations when timer is finised
                        setFadeAnimation(holder.itemLay, holder.getAdapterPosition());
                        holder.itemLay.setVisibility(GONE);

                    }
                }.start();

            } else if (viewHolder instanceof JoiningViewHolder) {
                final JoiningViewHolder joinViewHolder = (JoiningViewHolder) viewHolder;
                final HashMap<String, String> map = commentList.get(position);
                joinViewHolder.txtJoined.setText("@" + map.get(Constants.TAG_USER_NAME) + " Joined");
                new CountDownTimer(5000, 1000) { //(timer_duration, timer_interval)
                    @Override
                    public void onTick(long millisUntilFinished) {
                        joinViewHolder.itemLay.setVisibility(VISIBLE);
                        //runs every second (1000 ms)
                    }

                    @Override
                    public void onFinish() {
                        //Do your operations when timer is finised
                        setFadeAnimation(joinViewHolder.itemLay, joinViewHolder.getAdapterPosition());
                        joinViewHolder.itemLay.setVisibility(GONE);

                    }
                }.start();
            } /*else if (viewHolder instanceof GiftViewHolder) {
                final GiftViewHolder holder = (GiftViewHolder) viewHolder;
                final HashMap<String, String> map = commentList.get(position);
                Glide.with(context)
                        .load(Constants.IMAGE_URL + map.get(Constants.TAG_GIFT_ICON))
                        .error(R.drawable.gift)
                        .placeholder(R.drawable.gift)
                        .into(holder.giftImage);
                if (LocaleManager.isRTL()) {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, 20));
                } else {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, -20));
                }
                String userName = "@" + map.get(Constants.TAG_USER_NAME) + " " + getString(R.string.sent) + "\n";
                String giftTitle = "\"" + map.get(Constants.TAG_GIFT_TITLE) + "\"";
                String gift = " " + getString(R.string.gift);
                Spannable spannable = new SpannableString(userName + giftTitle + gift);
                spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), userName.length(), (userName + giftTitle).length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.txtGiftName.setText(spannable, TextView.BufferType.SPANNABLE);

                new CountDownTimer(5000, 1000) { //(timer_duration, timer_interval)
                    @Override
                    public void onTick(long millisUntilFinished) {
                        holder.giftLay.setVisibility(VISIBLE);
                        //runs every second (1000 ms)
                    }

                    @Override
                    public void onFinish() {
                        //Do your operations when timer is finised
                        setFadeAnimation(holder.giftLay, holder.getAdapterPosition());
                        holder.giftLay.setVisibility(GONE);

                    }
                }.start();
            }*/
        }

        public class JoiningViewHolder extends RecyclerView.ViewHolder {

            TextView txtJoined;

            FrameLayout itemLay;

            public JoiningViewHolder(View view) {
                super(view);
                //ButterKnife.bind(this, view);

                txtJoined = view.findViewById(R.id.txtJoined);
                itemLay = view.findViewById(R.id.itemLay);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView txtUserName;

            TextView txtMessage;

            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                // ButterKnife.bind(this, view);

                txtUserName = view.findViewById(R.id.txtUserName);
                txtMessage = view.findViewById(R.id.txtMessage);
                itemLay = view.findViewById(R.id.itemLay);
            }
        }

/*        public class GiftViewHolder extends RecyclerView.ViewHolder {

            TextView txtGiftName;

            ImageView giftImage;

            RelativeLayout giftLay;

            public GiftViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }*/

        private void setFadeAnimation(final View view, final int position) {
            /*sets animation from complete opaque state(1.0f) to complete transparent state(0.0f)*/
            AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
            anim.setDuration(StreamConstants.FADE_DURATION);
            view.startAnimation(anim);
        }
    }
}