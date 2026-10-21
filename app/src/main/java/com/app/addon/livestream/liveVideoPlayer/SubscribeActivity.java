/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.app.addon.livestream.liveVideoPlayer;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.addon.livestream.external.avloading.AVLoadingIndicatorView2;
import com.app.addon.livestream.external.avloading.BallBeatIndicator;
import com.app.addon.livestream.external.heartlayout.HeartLayout;
import com.app.addon.livestream.external.helper.CustomLayoutManager;
import com.app.addon.livestream.external.helper.LocaleManager;
import com.app.addon.livestream.external.helper.SocketIO;
import com.app.addon.livestream.external.model.StreamDetails;
import com.app.addon.livestream.external.utils.ApiClient;
import com.app.addon.livestream.external.utils.Constants;
import com.app.model.GetSet;
import com.app.addon.livestream.external.utils.StreamConstants;
import com.app.alhawanajd.App;
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.model.Report;
import com.app.utils.AdminData;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiInterface;
import com.makeramen.roundedimageview.RoundedImageView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


import de.tavendo.autobahn.WebSocket;
import io.antmedia.webrtcandroidframework.IDataChannelObserver;
import io.antmedia.webrtcandroidframework.IWebRTCClient;
import io.antmedia.webrtcandroidframework.IWebRTCListener;
import io.antmedia.webrtcandroidframework.StreamInfo;
import io.antmedia.webrtcandroidframework.WebRTCClient;
import io.antmedia.webrtcandroidframework.apprtc.CallActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
/**
 * The audio component of a {@link Player}.
 */
public class SubscribeActivity extends BaseFragmentActivity implements OnClickListener, SocketIO.SocketEvents,
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, IWebRTCListener, IDataChannelObserver  {

/*    ExoPlayer.EventListener,
    PlaybackControlView.VisibilityListener,*/


    private static final String TAG = "SubscribeActivity";

    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

    private static DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    private final String webRTCMode = IWebRTCClient.MODE_PLAY;
    private WebRTCClient webRTCClient;
    private SurfaceViewRenderer cameraViewRenderer;
    private SurfaceViewRenderer pipViewRenderer;
    private final boolean enableDataChannel = true;
    final int RECONNECTION_PERIOD_MLS = 100;
    Handler reconnectionHandler = new Handler();
    Runnable reconnectionRunnable = new Runnable() {
        @Override
        public void run() {
            if (!webRTCClient.isStreaming()) {
                attempt2Reconnect();
                // call the handler again in case startStreaming is not successful
                reconnectionHandler.postDelayed(this, RECONNECTION_PERIOD_MLS);
            }
        }
    };


    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }


    private FrameLayout contentLay;
    private PlayerView playerView;
    private ImageView btnClose;
    private ImageView btnInfo;
    private RelativeLayout bottomLay;
    private RelativeLayout commentsLay;
    private RecyclerView rvComments;
    private HeartLayout heartLay;
    private LinearLayout viewersLay;
    private RelativeLayout viewLay;
    private ImageView iconView;
    private AppCompatTextView txtViewCount, txtLiveCount;
    private RecyclerView rvViewers;
    private RelativeLayout chatLay;
    private RelativeLayout messageLay, liveCountLay, liveStatusLay;
    private EditText edtMessage;
    private TextView freez_alert;
    private ImageView btnSend;
    private FrameLayout loadingLay, liveTxtLay;
    private ImageView loadingImage;
    private AppCompatTextView txtLoadingTitle;
    private AppCompatTextView loadingViewCount;
    private RoundedImageView loadingUserImage;
    private AppCompatTextView loadingUserName;
    private LinearLayout initializeLay;
    private AVLoadingIndicatorView2 initialIndicator;
    AppCompatTextView bottomTxtLiveCount, txtBottomDuration;
    RelativeLayout bottomDurationLay;
    AppCompatTextView bottomStreamTitle;
    RoundedImageView publisherImage;
    RoundedImageView publisherColorImage;
    AppCompatTextView txtPublisherName;
    AVLoadingIndicatorView2 avBallIndicator;
    private CustomLayoutManager commentLayoutManager;


    private RelativeLayout bottomLiveCountLay;
    private FrameLayout bottomLiveTxtLay;
    LinearLayout bottomFirstLay, bottomDeleteLay, bottomReportLay, chatHideLay, bottomDetailsLay,
            bottomUserLay, bottomTopLay, bottomViewersLay,
            bottomInternalShareLay, bottomExternalShareLay;
    AppCompatTextView bottomViewerCount, txtReport, bottomGiftCount;
    RecyclerView bottomRecyclerView, bottomGiftView;
    private BottomSheetDialog bottomDialog, bottomCommentsDialog, bottomGiftsDialog,
            giftDialog, bottomHashTagsDialog, reportDialog;
    private Button btnMoreHashTag;

    private RecyclerView giftsView, stickersView, recyclerView, reportsView;


    private ReportAdapter reportAdapter;


    private ApiInterface apiInterface;
    private Animation slideIn;
    private Animation slideOut;
    private boolean isAnimated = false, isUnsubscribed = false, isKeyPadVisible = false, mPermissionsGranted = false;

    private LinearLayoutManager stickerLayoutManager;
    private final int giftPosition = 0;
    private int finalGiftPosition;
    private final int ITEM_LIMIT = 8;
    List<String> tempSmileyList = new ArrayList<>();

    int displayHeight, displayWidth;
    private String from, streamName, streamImage, publisherId;
    private SocketIO socketIO;
    private Socket mSocket;
    ArrayList<StreamDetails.LiveViewers> viewersList = new ArrayList<>();
    ArrayList<HashMap<String, String>> commentList = new ArrayList<HashMap<String, String>>();
    private int publisherColor;

    private final String currentDuration = null;
    private StreamDetails streamInfo;
    private StreamDetails streamData;
    private GestureDetector gestureScanner;

    private final String[] mRequiredPermissions = new String[]{
            CAMERA,
            RECORD_AUDIO,
            WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private final ArrayList<String> hashTagList = new ArrayList<>();
    private final boolean isUserJoined = false;

    private boolean isStarted = false;


    private static String USER_AGENT;
    private final boolean USE_BANDWIDTH_METER = false;
    private boolean needRetrySource;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private boolean shouldAutoPlay = true;
    private int resumeWindow;
    private long resumePosition;
    private RtmpDataSource.RtmpDataSourceFactory rtmpDataSourceFactory;
    private CountDownTimer publishTimer;


    private LinearLayout debugRootView;
    private TextView debugTextView;
    private Button retryButton;

    //  private DataSource.Factory mediaDataSourceFactory;
    private CommentsAdapter commentsAdapter;
    private BottomListAdapter bottomListAdapter;

    //   private DebugTextViewHelper debugViewHelper;
    private ViewerListViewAdapter viewerListAdapter;


    protected String userAgent;
    private EditText videoNameEditText;
    private String userLikeColor;
    private Intent intent;
    public List<String> reportList = new ArrayList<String>();

    Handler handler = new Handler();
    Boolean isStopStreamCall = false;


    Runnable r = new Runnable() {
        @Override
        public void run() {
            if (isStopStreamCall) {
                Log.d(TAG, "run: isstramcall");
               // showToastAlert(getString( R.string.stream_end_description));
                unSubscribeStream();
                handler.removeCallbacks(r);
            }
        }
    };




    private void addComment(HashMap<String, String> map) {
        commentList.add(map);
        commentsAdapter.notifyItemInserted(commentList.size() - 1);
        rvComments.scrollToPosition(commentList.size() - 1);
    }


    private void setBottomDialogUI(StreamDetails details) {
        bottomListAdapter.notifyDataSetChanged();
        bottomStreamTitle.setText(details.getTitle());
        bottomViewerCount.setText(details.getWatchCount());
        bottomTxtLiveCount.setText(details.getWatchCount());
        txtPublisherName.setText(details.getPostedBy());
        Glide.with(getApplicationContext())
                .load(details.getPublisherImage())
                .error(R.drawable.alhawana)
                .into(publisherImage);

        Glide.with(getApplicationContext())
                .load(details.getPublisherImage())
                .error(R.drawable.alhawana)
                .into(loadingUserImage);
        publisherColorImage.setBackgroundColor(publisherColor);
        txtReport.setText(details.getReported().equals(Constants.TAG_FALSE) ? getString( R.string.report_broadcast) :
                getString( R.string.undo_report_broadcast));
        //setFollowButton(details.getFollow().equals(Constants.TAG_TRUE));
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

/*    private Emitter.Listener _streamBlocked = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "_streamBlocked: " + data);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(SubscribeActivity.this, getString(R.string.broadcast_deactivated_by_admin), Toast.LENGTH_SHORT).show();
                    unSubscribeStream();
                }
            });
        }
    };*/


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(0, 0);
        setContentView( R.layout.activity_subscribe);
        cameraViewRenderer = findViewById(R.id.camera_view_renderer);
        pipViewRenderer = findViewById(R.id.pip_view_renderer);


        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        BANDWIDTH_METER = new DefaultBandwidthMeter.Builder(this).build();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        shouldAutoPlay = true;
        clearResumePosition();
        //mediaDataSourceFactory = buildDataSourceFactory(true);
        rtmpDataSourceFactory = new RtmpDataSource.RtmpDataSourceFactory();

        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }


        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        reportList.add(0, "Abuse");
        reportList.add(1, "In-Appropriate");
        reportList.add(2, "Adult Content");

/*        View rootView = findViewById(R.id.root);
        rootView.setOnClickListener(this);*/

        slideIn = AnimationUtils.loadAnimation(this, R.anim.anim_slide_left_in);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.anim_slide_right_out);
        intent = getIntent();
        Bundle bundle = intent.getExtras();
        from = intent.getStringExtra(Constants.TAG_FROM);
       /* if (from.equalsIgnoreCase("MainActivity")){
            streamData=(StreamDetails)intent.getSerializableExtra("stream_data");
        }else {
            streamData = (StreamDetails) bundle.getSerializable(StreamConstants.TAG_STREAM_DATA);
        }*/
        streamData = (StreamDetails) bundle.getSerializable(StreamConstants.TAG_STREAM_DATA);

        streamName = streamData.getName();
        Log.d(TAG, "onCreate_streamName: "+streamName);
        streamImage = streamData.getStreamThumbnail();
        intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, true);
        intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, 30);
        intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, 1500);
        intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, true);
        intent.putExtra(CallActivity.EXTRA_DATA_CHANNEL_ENABLED, enableDataChannel);

        Log.i(TAG, "streamDataonCreate: "+new Gson().toJson(streamData));

        contentLay = findViewById( R.id.contentLay);
        playerView = findViewById( R.id.player_view);
        btnClose = findViewById( R.id.btnClose);
        btnInfo = findViewById( R.id.btnInfo);
        bottomLay = findViewById( R.id.bottomLay);
        commentsLay = findViewById( R.id.commentsLay);
        rvComments = findViewById( R.id.rv_comments);
        heartLay = findViewById( R.id.heartLay);
        viewersLay = findViewById( R.id.viewersLay);
        viewLay = findViewById( R.id.viewLay);
        iconView = findViewById( R.id.iconView);
        loadingViewCount = findViewById( R.id.txtViewCount);
        rvViewers = findViewById( R.id.rv_viewers);
        chatLay = findViewById( R.id.chatLay);
        messageLay = findViewById( R.id.messageLay);
        edtMessage = findViewById( R.id.edtMessage);
        btnSend = findViewById( R.id.btnSend);
        freez_alert = findViewById(R.id.freez_alert);
        loadingLay = findViewById( R.id.loadingLay);
        loadingImage = findViewById( R.id.loadingImage);
        txtLoadingTitle = findViewById( R.id.txtLoadingTitle);
        loadingUserImage = findViewById( R.id.loadingUserImage);
        loadingUserName = findViewById( R.id.loadingUserName);
        initializeLay = findViewById( R.id.initializeLay);
        initialIndicator = findViewById( R.id.initialIndicator);
        liveStatusLay = findViewById( R.id.liveStatusLay);
        liveTxtLay = findViewById( R.id.liveTxtLay);
        liveCountLay = findViewById( R.id.liveCountLay);
        txtLiveCount = findViewById( R.id.txtLiveCount);
        txtViewCount = findViewById( R.id.txtViewCount);
        avBallIndicator = findViewById( R.id.avBallIndicator);


/*        playerView.setControllerVisibilityListener(this);
        playerView.requestFocus();*/

        if (LocaleManager.isRTL()) {
            viewLay.setBackground(getResources().getDrawable( R.drawable.rounded_curve_rtl_bg));
            liveTxtLay.setBackground(getResources().getDrawable( R.drawable.live_status_bg_rtl));
            liveCountLay.setBackground(getResources().getDrawable( R.drawable.live_count_bg_rtl));
        } else {
            viewLay.setBackground(getResources().getDrawable( R.drawable.rounded_curve_bg));
            liveTxtLay.setBackground(getResources().getDrawable( R.drawable.live_status_bg));
            liveCountLay.setBackground(getResources().getDrawable( R.drawable.live_count_bg));
        }
        setInitializingLay();

        initView();
        initSocket();
        initWebRTCClient(streamName);


        btnSend.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnInfo.setOnClickListener(this);
        viewLay.setOnClickListener(this);
    }


    private void setInitializingLay() {
        loadingLay.setVisibility(VISIBLE);
        Glide.with(this)
                .load(streamData.getStreamThumbnail())
                .placeholder( R.drawable.alhawana)
                .error( R.drawable.alhawana)
                .into(loadingImage);

        Glide.with(this)
                .load(streamData.getPublisherImage())
                .error(R.drawable.alhawana)
                .placeholder(R.drawable.alhawana)
                .into(loadingUserImage);
        setBuffering(GONE);
        txtLoadingTitle.setText(streamData.getTitle());
        loadingUserName.setText(streamData.getPostedBy());
        initialIndicator.setIndicator(new BallBeatIndicator());
        initialIndicator.show();
        initializeLay.setVisibility(VISIBLE);
    }


    private void initSocket() {
        socketIO = new SocketIO(SubscribeActivity.this);
        mSocket = socketIO.getInstance();
        socketIO.setSocketEvents(SubscribeActivity.this);
        try {
            mSocket = IO.socket(StreamConstants.STREAM_SOCKET_IO_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.on(Socket.EVENT_CONNECT, onConnect);

        mSocket.on(StreamConstants.TAG_MESSAGE_RECEIVED, _msgReceived);
        mSocket.on(StreamConstants.TAG_STREAM_INFO, _streamInfo);
        mSocket.on(StreamConstants.TAG_SUBSCRIBER_LEFT, _subscriberLeft);
        mSocket.on(StreamConstants.TAG_STREAM_BLOCKED, _streamBlocked);
        mSocket.connect();
    }

    private void initWebRTCClient(String streamName) {
        webRTCClient = new WebRTCClient(this, this);
        webRTCClient.setVideoRenderers(pipViewRenderer, cameraViewRenderer);
        webRTCClient.setOpenFrontCamera(false);
        // String SERVER_URL = SharedPref.getString(SharedPref.STREAM_WEBSOCKET_URL, Constants.RTMP_URL);


        String SERVER_URL = "wss://mediaserver.hitasoft.in/WebRTCAppEE/websocket";
      //  String SERVER_URL = "ws://3.238.159.240/WebRTCAppEE/websocket"; //rsolive

        // intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, 24);
        webRTCClient.init(SERVER_URL, streamName, webRTCMode, "tokenId", intent);


        Log.i(TAG, "productinitStream: " + this.streamName + SERVER_URL + this.webRTCMode);

        Log.d( TAG, "subinitwebrtcstreamName: "+streamName );
        Log.d( TAG, "subinitwebrtcwebRTCMode: "+webRTCMode );
        Log.d( TAG, "subinitWebRTCClient: " + SERVER_URL );
        Log.d( TAG, "subinitWebRTCClientintent: " + intent);

        webRTCClient.setDataChannelObserver(this);
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                attempt2Reconnect();
            }
        }, 500);
    }

    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "onConnect: " + "EVENT_CONNECTED");
            //   subscribeStream();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // subscribeStream();
                    subscribeStream();
                    getStreamInfo(streamName);
                    sendUserJoined();
                    //  getStreamInfo(streamName);
                }
            });
        }
    };

    private final Emitter.Listener _msgReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "call: msgReceived: " + data);
                    String type = data.optString(Constants.TAG_TYPE);
                    Log.d(TAG, "run_type: "+type);
                    String keyword = data.optString("streamer_in_background");
                    Log.d(TAG, "run_keyword: "+keyword);

                    switch (type) {
                        case StreamConstants.TAG_IOS_FREEZ:
                            if (data.optString(StreamConstants.TAG_STREAM_NAME).equals(streamName)) {
                                if (keyword.equals("true")){
                                    freez_alert.setVisibility(VISIBLE);
                                }
                                else{
                                    freez_alert.setVisibility(GONE);
                                }
                                Log.d(TAG, "ios_freez: "+keyword);
                            }
                            break;

                            case StreamConstants.TAG_STREAM_JOINED:
                            if (data.optString(StreamConstants.TAG_STREAM_NAME).equals(streamName)) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID));
                                map.put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME));
                                map.put(Constants.TAG_USER_IMAGE, data.optString(Constants.TAG_USER_IMAGE));
                                map.put(Constants.TAG_MESSAGE, data.optString(Constants.TAG_MESSAGE));
                                map.put(Constants.TAG_TYPE, data.optString(Constants.TAG_TYPE));
                                addComment(map);
                                getStreamInfo(streamName);
                            }
                            break;
                        case Constants.TAG_TEXT: {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID));
                            map.put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME));
                            map.put(Constants.TAG_USER_IMAGE, data.optString(Constants.TAG_USER_IMAGE));
                            map.put(Constants.TAG_MESSAGE, data.optString(Constants.TAG_MESSAGE));
                            map.put(Constants.TAG_TYPE, Constants.TAG_TEXT);
                            map.put(Constants.TAG_VISIBILITY, Constants.TAG_TRUE);
                            addComment(map);
                            break;
                        }
                        case StreamConstants.TAG_LIKED: {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID));
                            map.put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME));
                            map.put(Constants.TAG_USER_IMAGE, data.optString(Constants.TAG_USER_IMAGE));
                            map.put(Constants.TAG_MESSAGE, data.optString(Constants.TAG_MESSAGE));
                            map.put(Constants.TAG_TYPE, Constants.TAG_MESSAGE);
                            try {
                                String userId = data.optString(Constants.TAG_USER_ID);
                                String likeColor = data.optString(StreamConstants.TAG_LIKE_COLOR);
                                heartLay.addHeart(Color.parseColor(likeColor));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }

                        case StreamConstants.TAG_STREAM_END: {
                            Log.d(TAG, "TAG_STREAM_END: ");
                            isStopStreamCall = true;
                        }

                        /*case Constants.TAG_GIFT: {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID));
                            map.put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME));
                            map.put(StreamConstants.TAG_STREAM_NAME, streamName);
                            map.put(Constants.TAG_GIFT_TITLE, data.optString(Constants.TAG_GIFT_TITLE));
                            map.put(Constants.TAG_GIFT_ICON, data.optString(Constants.TAG_GIFT_ICON));
                            map.put(Constants.TAG_TYPE, data.optString(Constants.TAG_TYPE));
                            addComment(map);
                            getStreamInfo(streamName);
                            break;
                        }*/
/*                        case StreamConstants.TAG_DURATION:
                            currentDuration = data.optString(StreamConstants.TAG_TIME);
                            if (!isUserJoined) {
                                isUserJoined = true;
                                sendUserJoined();
                            }
                            break;*/
                    }
                }
            });
        }
    };


    private final Emitter.Listener _subscriberLeft = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(TAG, "_subscriberLeft: " + data);
                    getStreamInfo(data.optString(StreamConstants.TAG_STREAM_NAME));
                }
            });
        }
    };


    private Emitter.Listener _streamInfo = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "_streamInfo: " + data);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (data.optString("status").equals("false")) {
                        // TODO: stop the stream.
                        return;
                    }
                    Gson gson = new Gson();
                    StreamDetails tempDetails = gson.fromJson("" + data, StreamDetails.class);
                    streamInfo = tempDetails;
                    txtViewCount.setText(tempDetails.getWatchCount() != null ? tempDetails.getWatchCount() : "");
                    publisherId = tempDetails.getPublisherId();
                    viewersList.clear();
                    viewersList.addAll(tempDetails.getLiveViewers());
                    viewerListAdapter.notifyDataSetChanged();
                    loadingViewCount.setText(tempDetails.getWatchCount() != null ? tempDetails.getWatchCount() : "");
                    setBottomDialogUI(tempDetails);
                }
            });
        }
    };

    private boolean is_streamBlocked = false;
    private boolean Is_networkchecked = false;
    private final Emitter.Listener _streamBlocked = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "_streamBlocked: " + data);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    is_streamBlocked = true;
                    unSubscribeStream();
                }
            });
        }
    };

    private void getStreamInfo(String streamName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_USER_ID, SharedPref.getString(SharedPref.USER_ID,""));
            jsonObject.put(Constants.TAG_USER_NAME, SharedPref.getString(SharedPref.USER_NAME,""));
            jsonObject.put(Constants.TAG_USER_IMAGE,SharedPref.getString(SharedPref.USER_IMAGE,""));
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            if (mSocket.connected()) {
                Log.d(TAG, "getStreamInfo: " + new Gson().toJson(jsonObject));
                mSocket.emit(StreamConstants.TAG_GET_STREAM_INFO, jsonObject);
            }
        } catch (JSONException e) {
            Log.d(TAG, "getStreamInfo: error=> " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void sendUserJoined() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_TYPE, StreamConstants.TAG_STREAM_JOINED);
            jsonObject.put(Constants.TAG_USER_ID, SharedPref.getString(SharedPref.USER_ID,""));
            jsonObject.put(Constants.TAG_USER_NAME, SharedPref.getString(SharedPref.USER_NAME,""));
            jsonObject.put(Constants.TAG_USER_IMAGE,SharedPref.getString(SharedPref.USER_IMAGE,""));
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            jsonObject.put(StreamConstants.TAG_TIME, currentDuration);
            Log.d(TAG, "sendUserJoined: " + new Gson().toJson(jsonObject));
            Log.d(TAG, "sendUserJoined_currentDuration: "+currentDuration);
            Log.d(TAG, "sendUserJoined: "+mSocket.connected());
            if (mSocket.connected()) {
                Log.d(TAG, "sendUserJoined: " + new Gson().toJson(jsonObject));
                mSocket.emit(StreamConstants.TAG_SEND_MESSAGE, jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "sendUserJoined: " + e.getMessage());
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: enter");
                getStreamInfo(streamName);
            }
        }, 1000);
    }

    private void initView() {
        gestureScanner = new GestureDetector(this, this);
        Random random = new Random();
        int i1 = random.nextInt(StreamConstants.LIKE_COLOR.length);
        publisherColor = Color.argb(60, random.nextInt(255), random.nextInt(255), random.nextInt(255));
        userLikeColor = StreamConstants.LIKE_COLOR[i1].trim();

/*        new HeightProvider(this).init().setHeightListener(new HeightProvider.HeightListener() {
            @Override
            public void onHeightChanged(int height) {
                if (height > 0) {
                    findViewById(R.id.bottomLay).setTranslationY(-height);
                } else {
                    findViewById(R.id.bottomLay).setTranslationY(0);
                }
            }
        });*/

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ABOVE, R.id.viewersLay);
        params.height = (int) (displayHeight * 0.4);
        commentsLay.setLayoutParams(params);

        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isAnimated) {
                    isAnimated = true;
                    slideOutAnim();
                } else if (charSequence.length() == 0) {
                    slideInAnim();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() >=75){
                    Toast.makeText(SubscribeActivity.this, getString(R.string.reachlimt), Toast.LENGTH_SHORT).show();
                }
            }
        });

        View view = findViewById( R.id.parentLay);
        findViewById( R.id.parentLay).getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = view.getRootView().getHeight() - view.getHeight();
                isKeyPadVisible = heightDiff > dpToPx(getApplicationContext(), 200);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvViewers.setLayoutManager(linearLayoutManager);
        viewerListAdapter = new ViewerListViewAdapter(this, viewersList);
        rvViewers.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvViewers.setAdapter(viewerListAdapter);
        rvViewers.setHasFixedSize(true);
        viewerListAdapter.notifyDataSetChanged();

        commentsAdapter = new CommentsAdapter(this, commentList);
        commentLayoutManager = new CustomLayoutManager(this);
        commentLayoutManager.setScrollEnabled(false);
        rvComments.setLayoutManager(commentLayoutManager);
        rvComments.setAdapter(commentsAdapter);
        commentsAdapter.notifyDataSetChanged();
        setContentLayTouchListener();

        initBottomDetailsDialog();
        initBottomViewersDialog();
        /*initBottomGiftDialog();
        initBottomHashTagDialog();*/
        getStreamDetails();
    }

    private void initBottomViewersDialog() {
        View bottomSheet = getLayoutInflater().inflate( R.layout.dialog_bottom_viewers, null);
        bottomCommentsDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomCommentsDialog.setContentView(bottomSheet);

        bottomViewersLay = bottomSheet.findViewById( R.id.bottomViewersLay);
        bottomViewerCount = bottomSheet.findViewById( R.id.bottomViewerCount);
        bottomRecyclerView = bottomSheet.findViewById( R.id.bottomRecyclerView);
        bottomTopLay = bottomSheet.findViewById( R.id.bottomTopLay);
        txtBottomDuration = bottomSheet.findViewById( R.id.txtBottomDuration);
        bottomDurationLay = bottomSheet.findViewById( R.id.bottomDurationLay);

        bottomDurationLay.setVisibility(GONE);

        bottomListAdapter = new BottomListAdapter(this, viewersList);
        bottomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bottomRecyclerView.setAdapter(bottomListAdapter);
        bottomRecyclerView.setHasFixedSize(true);
        bottomListAdapter.notifyDataSetChanged();

        bottomCommentsDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet1 = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet1).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet1).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet1).setHideable(true);
        });

        bottomCommentsDialog.setOnDismissListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet12 = dialog.findViewById( com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet12).setState(BottomSheetBehavior.STATE_COLLAPSED);
            BottomSheetBehavior.from(bottomSheet12).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet12).setHideable(true);
        });
    }

    private void initBottomDetailsDialog() {
        View bottomSheet = getLayoutInflater().inflate( R.layout.dialog_bottom_details, null);
        bottomDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomDialog.setContentView(bottomSheet);

        bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        bottomFirstLay = bottomSheet.findViewById( R.id.bottomFirstLay);
        bottomDeleteLay = bottomSheet.findViewById( R.id.bottomDeleteLay);
        bottomTxtLiveCount = bottomSheet.findViewById( R.id.txtLiveCount);
        bottomLiveTxtLay = bottomSheet.findViewById( R.id.liveTxtLay);
        bottomLiveCountLay = bottomSheet.findViewById( R.id.liveCountLay);
        bottomStreamTitle = bottomSheet.findViewById( R.id.bottomStreamTitle);

        bottomUserLay = bottomSheet.findViewById( R.id.bottomUserLay);
        publisherImage = bottomSheet.findViewById( R.id.publisherImage);
        publisherColorImage = bottomSheet.findViewById( R.id.publisherColorImage);
        txtPublisherName = bottomSheet.findViewById( R.id.txtPublisherName);

        bottomDetailsLay = bottomSheet.findViewById( R.id.bottomDetailsLay);
        chatHideLay = bottomSheet.findViewById( R.id.chatHideLay);
        bottomReportLay = bottomSheet.findViewById( R.id.bottomReportLay);
        txtReport = bottomSheet.findViewById( R.id.txtReport);


        if (LocaleManager.isRTL()) {
            bottomLiveTxtLay.setBackground(getResources().getDrawable( R.drawable.live_status_bg_rtl));
            bottomLiveCountLay.setBackground(getResources().getDrawable( R.drawable.live_count_bg_rtl));
        } else {
            bottomLiveTxtLay.setBackground(getResources().getDrawable( R.drawable.live_status_bg));
            bottomLiveCountLay.setBackground(getResources().getDrawable( R.drawable.live_count_bg));
        }

        bottomStreamTitle.setText(streamData.getTitle());
        bottomReportLay.setVisibility(VISIBLE);
        bottomUserLay.setVisibility(VISIBLE);
        bottomDeleteLay.setVisibility(GONE);
        chatHideLay.setVisibility(VISIBLE);

        bottomDetailsLay.setOnClickListener(view -> {
            bottomDialog.dismiss();
            if (bottomCommentsDialog != null && !bottomCommentsDialog.isShowing()) {
                bottomCommentsDialog.show();
            }
        });


        bottomReportLay.setOnClickListener(view -> {
            if (bottomDialog != null && bottomDialog.isShowing()) {
                bottomDialog.dismiss();
            }
            if (streamInfo == null) {
                openReportDialog();
            } else if (streamInfo.getReported().equals(Constants.TAG_FALSE)) {
                openReportDialog();
            } else {
                sendReport("", false);
            }
        });


        chatHideLay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                if (bottomLay.getVisibility() == VISIBLE)
                    slideDownAnim();
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
                View bottomSheet = dialog.findViewById( com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
    }

    private void slideDownAnim() {
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                0,
                bottomLay.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(false);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bottomLay.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        bottomLay.startAnimation(animate);
    }

    private void sendReport(String title, boolean report) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, SharedPref.getString(SharedPref.USER_ID,""));
        map.put(Constants.TAG_NAME, streamName);
        map.put(Constants.TAG_TYPE, Constants.TAG_LIVE_STREAMING);
        if (report) {
            map.put(Constants.TAG_REPORT, title);
        }

        Log.e("checkMap","="+map);
        Call<Map<String, String>> call = apiInterface.reportStream(SharedPref.getString(SharedPref.TAG_ACCESS_TOKENS , ""), map);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Map<String, String> reportResponse = response.body();
                    Log.e("checkReportRes",""+new Gson().toJson(response.body()));
                    if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        if (report) {

                            Toast.makeText(SubscribeActivity.this, getString( R.string.reported_successfully), Toast.LENGTH_SHORT).show();

                            if (streamInfo != null) {
                                streamInfo.setReported(Constants.TAG_TRUE);
                            }
                            txtReport.setText(getString( R.string.undo_report_broadcast));
                        } else {
                            Toast.makeText(SubscribeActivity.this, getString( R.string.undo_report_successfully), Toast.LENGTH_SHORT).show();

                            if (streamInfo != null) {
                                streamInfo.setReported(Constants.TAG_FALSE);
                            }
                            txtReport.setText(getString( R.string.report_broadcast));
                        }
                        if (bottomDialog != null && bottomDialog.isShowing()) {
                            bottomDialog.dismiss();
                        }

                    }

                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();

            }
        });
    }

    private void openReportDialog() {
        View bottomSheet = getLayoutInflater().inflate( R.layout.bottom_sheet_report_addon, null);
        reportDialog = new BottomSheetDialog(SubscribeActivity.this, R.style.Bottom_StreamDialog); // Style here
        reportDialog.setContentView(bottomSheet);
        ((View) bottomSheet.getParent()).setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlack));
        bottomSheet.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        WindowManager.LayoutParams params = reportDialog.getWindow().getAttributes();
        params.x = 0;
        reportDialog.getWindow().setAttributes(params);
        bottomSheet.requestLayout();
        reportsView = bottomSheet.findViewById( R.id.reportView);

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

        reportDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById( com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
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

    public void getStreamDetails() {
        Call<StreamDetails> call3 = apiInterface.getStreamDetails(SharedPref.getString(SharedPref.AUTH_TOKEN,""), SharedPref.getString(SharedPref.USER_ID,""), streamName);
        call3.enqueue(new Callback<StreamDetails>() {
            @Override
            public void onResponse(Call<StreamDetails> call, Response<StreamDetails> response) {
                if (response.isSuccessful()) {

                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        streamData = response.body();
                        if (!streamData.getType().equals(Constants.TAG_LIVE)) {
                            finish();
                        } else {
                            publishTimer = new CountDownTimer(20000, 1000) {
                                @Override
                                public void onTick(long l) {

                                }

                                @Override
                                public void onFinish() {
                                    if (!isStarted) {
                                      /*  Toast.makeText(SubscribeActivity.this, getString( R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                        finish();*/
                                    }
                                }
                            }.start();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<StreamDetails> call, Throwable t) {
                call.cancel();
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });

    }

    public int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private void slideInAnim() {
        isAnimated = false;
        TranslateAnimation animate = new TranslateAnimation(
                0,
                -50,
                0,
                0);
        animate.setDuration(200);
        animate.setFillAfter(false);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                btnSend.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        btnSend.startAnimation(animate);
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
    protected void onResume() {
        super.onResume();
        if (socketIO != null)
            socketIO.setSocketEvents(SubscribeActivity.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (is_streamBlocked){
            App.makeToast(getString(R.string.broadcast_deactivated_by_admin));
        } if (Is_networkchecked){
          //  App.makeToast("Network Error..!!");
            finish();
        }
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        unSubscribeStream();
        if (socketIO != null)
            socketIO.setSocketEvents(null);
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unSubscribeStream();
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT, onConnect);
            mSocket.off(StreamConstants.TAG_MESSAGE_RECEIVED, _msgReceived);
            mSocket.off(StreamConstants.TAG_STREAM_INFO, _streamInfo);
            mSocket.off(StreamConstants.TAG_SUBSCRIBER_LEFT, _subscriberLeft);
            mSocket.off(StreamConstants.TAG_STREAM_BLOCKED, _streamBlocked);
        }
        Constants.isInStream = false;
    }

    private void slideOutAnim() {
        btnSend.setVisibility(VISIBLE);
        if (LocaleManager.isRTL()) {
            btnSend.setRotation(180);
            TranslateAnimation animate = new TranslateAnimation(
                    -50,
                    0,
                    0,
                    0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            btnSend.startAnimation(animate);
        } else {
            btnSend.setRotation(0);
            btnSend.setVisibility(VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    -50,
                    0,
                    0,
                    0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            btnSend.startAnimation(animate);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                mPermissionsGranted = true;
                // Check the result of each permission granted
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mPermissionsGranted = false;
                        break;
                    }
                }
                if (mPermissionsGranted) {
                    initView();
                    initSocket();
                    initWebRTCClient(streamName);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(RECORD_AUDIO) &&
                                shouldShowRequestPermissionRationale(MODIFY_AUDIO_SETTINGS)) {
                            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
                        } else {
                            Intent intent = new Intent( Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + getApplication().getPackageName()));
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            App.makeToast(getString( R.string.camera_microphone_permission_error));
                            finish();
                        }
                    }
                }
                break;
            }
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Show the controls on any key event.
        playerView.showController();
        // If the event was not handled then see if the player view can handle it as a media key event.
        return super.dispatchKeyEvent(event) || playerView.dispatchMediaKeyEvent(event);
    }


    private void initializePlayer(String rtmpUrl) {



//        String URL = "rtmp://media.hitasoft.in/LiveApp/" + rtmpUrl;
//        String URL = "rtmp://3.108.238.2/LiveApp/" + rtmpUrl;
        String URL = "rtmp://3.238.159.240/LiveApp/" + rtmpUrl;


        Log.d(TAG, "initializePlayer: rtmp=> " + URL);

        Intent intent = getIntent();

        boolean needNewPlayer = player == null;
        if (needNewPlayer) {

            boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);


            /*@DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                    useExtensionRenderers()
                            ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;*/
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(SubscribeActivity.this);
            //  renderersFactory.setExtensionRendererMode(extensionRendererMode);
            playerView.setControllerVisibilityListener(null);
            playerView.setControllerAutoShow(false);
            playerView.requestFocus();
            playerView.setKeepContentOnPlayerReset(true);
            final int loadControlStartBufferMs = 1500;


            /* Instantiate a DefaultLoadControl.Builder. */
            DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
            builder.setBufferDurationsMs(
                    DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                    DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                    loadControlStartBufferMs,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);

            DefaultLoadControl loadControl = new DefaultLoadControl();
            //  TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
            // trackSelector = new DefaultTrackSelector(SubscribeActivity.this, videoTrackSelectionFactory);
            player = new SimpleExoPlayer.Builder(SubscribeActivity.this, renderersFactory)
                    .setBandwidthMeter(BANDWIDTH_METER)
                    .setLoadControl(loadControl)
                    .setTrackSelector(trackSelector)
                    .build();


            //    player.addListener(playerListener);
            playerView.setPlayer(player);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            // player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            player.setPlayWhenReady(shouldAutoPlay);
            /*player.setRepeatMode(Player.REPEAT_MODE_OFF);*/


/*            @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
                    useExtensionRenderers()
                            ? (preferExtensionDecoders ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
                    null, extensionRendererMode);*/
            //   player = ExoPlayerFactory.newSimpleInstance(this, trackSelector,
            //           new DefaultLoadControl(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),  500, 1500, 500, 1500),
            //           null, extensionRendererMode);
/*            player.addListener(this);

            eventLogger = new EventLogger(trackSelector);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setMetadataOutput(eventLogger);

            playerView.setPlayer(player);
            player.setPlayWhenReady(shouldAutoPlay);
            debugViewHelper = new DebugTextViewHelper(player, debugTextView);
            debugViewHelper.start();*/
        }
        if (needNewPlayer || needRetrySource) {
            //  String action = intent.getAction();
            Uri[] uris;
            String[] extensions;

            uris = new Uri[1];
            uris[0] = Uri.parse(URL);
            extensions = new String[1];
            extensions[0] = "";
            if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
                // The player will be reinitialized if the permission is granted.
                return;
            }
            MediaSource[] mediaSources = new MediaSource[uris.length];
            for (int i = 0; i < uris.length; i++) {
                mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
            }
            MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                    : new ConcatenatingMediaSource(mediaSources);
            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                player.seekTo(resumeWindow, resumePosition);
            }
            player.prepare(mediaSource, !haveResumePosition, false);
            needRetrySource = false;
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory(USE_BANDWIDTH_METER);
        switch (type) {
            case C.TYPE_SS:

                SsMediaSource.Factory ssMediaSourceFactory = new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false));
                return ssMediaSourceFactory.createMediaSource(MediaItem.fromUri(uri));
            case C.TYPE_DASH:
                DashMediaSource.Factory dashMediaFactory = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false));
                return dashMediaFactory.createMediaSource(MediaItem.fromUri(uri));
            case C.TYPE_HLS:
                HlsDataSourceFactory hlsDataSourceFactory = new DefaultHlsDataSourceFactory(buildDataSourceFactory(USE_BANDWIDTH_METER));
                return new HlsMediaSource.Factory(hlsDataSourceFactory)
                        .setAllowChunklessPreparation(true).createMediaSource(MediaItem.fromUri(uri));
            case C.TYPE_OTHER:
                if (uri.getScheme().equals("rtmp")) {

                    return new ProgressiveMediaSource.Factory(rtmpDataSourceFactory, new DefaultExtractorsFactoryForFLV())
                            .createMediaSource(MediaItem.fromUri(uri));
                } else {
                    return new ProgressiveMediaSource.Factory(buildDataSourceFactory(USE_BANDWIDTH_METER))
                            .createMediaSource(MediaItem.fromUri(uri));
                }
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private final  Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            Log.d(TAG, "onPlayerStateChanged: " + playbackState);
            if (playbackState == Player.STATE_BUFFERING) {
                if (isStopStreamCall) handler.postDelayed(r, 8000);
                setBuffering(VISIBLE);
            } else if (playbackState == Player.STATE_ENDED) {
                Log.d(TAG, "onPlayerStateChanged: STATE_ENDED");
                unSubscribeStream();
              //  showToastAlert(getString( R.string.stream_end_description));
            } else if (playWhenReady && playbackState == Player.STATE_READY) {
/*                if (isStopStreamCall) {
                    handler.postDelayed(r, 8000);
                    isStarted = true;
                }*/
                if (!isStarted) {
                    isStarted = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setBuffering(GONE);
                            if (loadingLay.getVisibility() == VISIBLE) {
                                loadingLay.setVisibility(GONE);
                                initialIndicator.hide();
                                initializeLay.setVisibility(GONE);
                            }
                            //subscribeStream();
                            Log.d(TAG, "run: "+"socket");

                        }
                    });
                }
                setBuffering(GONE);
            } else if (playbackState == Player.STATE_IDLE) {
                if (isStarted) {
                    unSubscribeStream();
                   // showToastAlert(getString( R.string.stream_end_description));
                }
            }
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {

        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            if (needRetrySource) {
                // This will only occur if the user has performed a seek whilst in the error state. Update the
                // resume position so that if the user then retries, playback will resume from the position to
                // which they seeked.
                updateResumePosition();
            }
        }



        /*@Override
        public void onPlayerError(ExoPlaybackException error) {

            Log.d(TAG, "onPlayerError: call");

        }*/

/*        @Override
        public void onPlayerError(@NotNull ExoPlaybackException error) {

            Log.d(TAG, "onPlayerError: call");
            unSubscribeStream();

        }*/

        /*@Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.d(TAG, "onPlayerError: call");
            unSubscribeStream();

        }*/


        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }


        public void onPlayerError(ExoPlaybackException e) {
            try {
                String errorString = null;
                Log.e(TAG, "onPlayerError: " + e.getMessage());
                Log.e(TAG, "onPlayerError: " + e.type);
                if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                    Exception cause = e.getRendererException();
                    if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                        // Special case for decoder initialization failures.
                        MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                                (MediaCodecRenderer.DecoderInitializationException) cause;
                       /* if (decoderInitializationException.diagnosticInfo == null) {
                            if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                                errorString = getString( R.string.error_querying_decoders);
                            } else if (decoderInitializationException.secureDecoderRequired) {
                                errorString = getString( R.string.error_no_secure_decoder,
                                        decoderInitializationException.mimeType);
                            } else {
                                errorString = getString( R.string.error_no_decoder,
                                        decoderInitializationException.mimeType);
                            }
                        } else {
                            errorString = getString( R.string.error_instantiating_decoder,
                                    decoderInitializationException.diagnosticInfo);
                        }*/
                    }
                } else {
                    if (e.type == ExoPlaybackException.TYPE_UNEXPECTED) {
                        if (e.getMessage() != null && e.getMessage().contains("com.google.android.exoplayer2.extractor.SeekMap$SeekPoints")) {
                            if (from.equals(Constants.TAG_SUBSCRIBE) ||
                                    from.equals(Constants.TAG_OTHER_PROFILE)) {
                                setResult(RESULT_OK);
                                finish();
                                return;
                            }
                        } else {
                            Log.e(TAG, "getUnexpectedException: " + e.getUnexpectedException().getMessage());
                        }
                    } else {
                        Log.e(TAG, "getUnexpectedException: " + e.getUnexpectedException().getMessage());
                    }
                }
                if (errorString != null) {
                    // App.makeToast(errorString);
                    Toast.makeText(SubscribeActivity.this, errorString, Toast.LENGTH_SHORT).show();
                }
                needRetrySource = true;
                if (isBehindLiveWindow(e)) {
                    shouldAutoPlay = false;
                    clearResumePosition();
                    play(null);
                } else {
                    updateResumePosition();
                }
            } catch (Exception exception) {

                unSubscribeStream();
                Toast.makeText(SubscribeActivity.this, getString( R.string.something_wrong), Toast.LENGTH_SHORT).show();
            }
        }

    };


    private void subscribeStream() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            jsonObject.put(Constants.TAG_USER_ID, SharedPref.getString(SharedPref.USER_ID,""));
            jsonObject.put(Constants.TAG_USER_NAME, SharedPref.getString(SharedPref.USER_NAME,""));
            jsonObject.put(Constants.TAG_USER_IMAGE,SharedPref.getString(SharedPref.USER_IMAGE,""));
            if (mSocket.connected()) {
                Log.e(TAG, "subscribeStream params: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_SUBSCRIBE_STREAM, jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "subscribeStream: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void showToastAlert(String message) {
        runOnUiThread(() -> Toast.makeText(SubscribeActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void unSubscribeStream() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_USER_ID,SharedPref.getString(SharedPref.USER_ID,""));
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            if (mSocket != null && mSocket.connected() && !isUnsubscribed) {
                isUnsubscribed = true;
//                mSocket.emit(StreamConstants.TAG_UNSUBSCRIBE_STREAM, jsonObject);
            }
        } catch (JSONException e) {

            e.printStackTrace();
        }
        if (webRTCClient != null) webRTCClient.stopStream();
        finish();
    }

    private void setBuffering(int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (visibility == VISIBLE) {
                    if (loadingLay.getVisibility() != VISIBLE) {
                        avBallIndicator.setVisibility(visibility);
                        contentLay.setOnTouchListener(null);
                        edtMessage.setEnabled(false);
                    }
                } else {
                    avBallIndicator.setVisibility(visibility);
                    setContentLayTouchListener();
                }
            }
        });
    }


    private void setContentLayTouchListener() {
        contentLay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureScanner.onTouchEvent(event);
            }
        });
        edtMessage.setEnabled(true);
    }


    private void releasePlayer() {
        if (player != null) {
            /*debugViewHelper.stop();
            debugViewHelper = null;*/
            shouldAutoPlay = player.getPlayWhenReady();
            updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;
            //trackSelectionHelper = null;

        }
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
                : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
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

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    // ExoPlayer.EventListener implementation

    /*@Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }*/

/*    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
    }*/

    /*@Override
    public void onPositionDiscontinuity() {
        if (needRetrySource) {
            // This will only occur if the user has performed a seek whilst in the error state. Update the
            // resume position so that if the user then retries, playback will resume from the position to
            // which they seeked.
            updateResumePosition();
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        // Do nothing.
    }*/

/*    @Override
    public void onPlayerError(ExoPlaybackException e) {
        //videoStartControlLayout.setVisibility(View.VISIBLE);
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof DecoderInitializationException) {
                // Special case for decoder initialization failures.
                DecoderInitializationException decoderInitializationException =
                        (DecoderInitializationException) cause;
                if (decoderInitializationException.diagnosticInfo == null) {
                    if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.diagnosticInfo);
                }
            }
        }
        if (errorString != null) {
            showToast(errorString);
        }
        needRetrySource = true;
        if (isBehindLiveWindow(e)) {
            clearResumePosition();
            play(null);
        } else {
            updateResumePosition();
            showControls();
        }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                    == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                showToast(R.string.error_unsupported_video);
            }
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                    == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                showToast(R.string.error_unsupported_audio);
            }
        }
    }*/

    private void showControls() {
        debugRootView.setVisibility(View.VISIBLE);
    }

    private void showToast(int messageId) {
        showToast(getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }


    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSource.Factory();
    }

    /*public boolean useExtensionRenderers() {
        return BuildConfig.FLAVOR.equals("withExtensions");
    }
*/
    public void play(View view) {
        /*    String URL = RTMP_BASE_URL + videoNameEditText.getText().toString();*/
        //String URL = "http://192.168.1.34:5080/vod/streams/test_adaptive.m3u8";

        String URL = "rtmp://media.hitasoft.in/LiveApp/";
        initializePlayer(streamName);
        //videoStartControlLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (isKeyPadVisible) {
            hideSoftKeyboard(SubscribeActivity.this);
        } else {
            sendLike();
        }
        return false;
    }

    private void sendLike() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_TYPE, StreamConstants.TAG_LIKED);
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_USER_NAME, GetSet.getUserName());
            jsonObject.put(Constants.TAG_USER_IMAGE, GetSet.getImageUrl());
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            jsonObject.put(StreamConstants.TAG_LIKE_COLOR, userLikeColor);
            heartLay.addHeart(Color.parseColor(userLikeColor));
            if (mSocket.connected()) {
                Log.i(TAG, "sendLike: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_SEND_MESSAGE, jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "sendLike: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
       /* if (e.getAction() == MotionEvent.ACTION_DOWN && bottomLay.getVisibility() == VISIBLE) {
            sendLike();
        }*/
        sendLike();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        float sensitivity = 50;
        // Get swipe delta value in x axis.
        float deltaX = e1.getX() - e2.getX();

        // Get swipe delta value in y axis.
        float deltaY = e1.getY() - e2.getY();
        // Get absolute value.
        float deltaXAbs = Math.abs(deltaX);
        float deltaYAbs = Math.abs(deltaY);
        // Minimal x and y axis swipe distance.
        int MIN_SWIPE_DISTANCE_X = 100;
        int MIN_SWIPE_DISTANCE_Y = 100;

        // Maximal x and y axis swipe distance.
        int MAX_SWIPE_DISTANCE_X = 1000;
        int MAX_SWIPE_DISTANCE_Y = 1000;

        Log.i(TAG, "onFling: ");
        if ((deltaYAbs >= MIN_SWIPE_DISTANCE_Y) && (deltaYAbs <= MAX_SWIPE_DISTANCE_Y)) {
            if (deltaY > 0) {
                if (bottomLay.getVisibility() != VISIBLE)
                    slideUpAnim();
            } else {
                if (bottomLay.getVisibility() == VISIBLE)
                    slideDownAnim();
            }
        }

        return true;
    }

    private void slideUpAnim() {
        bottomLay.setVisibility(VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                bottomLay.getHeight(),
                0);
        animate.setDuration(500);
        animate.setFillAfter(true);
        bottomLay.startAnimation(animate);
    }


/*
    @Override
    public void onClick(View v) {


    }
*/

    @Override
    public void onClick(View view) {
        /*if (view == retryButton) {
            play(null);
        }*/

        int id = view.getId();
        if (id == R.id.btnClose) {
            closeConfirmDialog(getString( R.string.really_want_exit));
        } else if (id == R.id.btnSend) {
            if (TextUtils.isEmpty(edtMessage.getText())) {
                App.makeToast(getString(R.string.enter_your_message));
            } else {
                sendChat();
            }
        } else if (id == R.id.btnInfo) {
            if (bottomDialog != null && !bottomDialog.isShowing())
                bottomDialog.show();
        } else if (id == R.id.viewLay) {
            if (bottomCommentsDialog != null && !bottomCommentsDialog.isShowing())
                bottomCommentsDialog.show();
        }


    }

    @Override
    public void onBackPressed() {
        closeConfirmDialog(getString( R.string.really_want_exit));
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    private void closeConfirmDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SubscribeActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton(getString( R.string.oaky_txt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                unSubscribeStream();
                showToastAlert(getString( R.string.stream_end_description));
                dialog.cancel();
            }
        });
        builder.setNegativeButton(getString( R.string.no_txt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont( R.font.font_regular);
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

    private void sendChat() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_TYPE, Constants.TAG_TEXT);
            jsonObject.put(Constants.TAG_USER_ID, SharedPref.getString(SharedPref.USER_ID,""));
            jsonObject.put(Constants.TAG_USER_NAME, SharedPref.getString(SharedPref.USER_NAME,""));
            jsonObject.put(Constants.TAG_USER_IMAGE, SharedPref.getString(SharedPref.USER_IMAGE,""));
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            jsonObject.put(Constants.TAG_MESSAGE, "" + edtMessage.getText());
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, SharedPref.getString(SharedPref.USER_ID,""));
            map.put(Constants.TAG_USER_NAME, SharedPref.getString(SharedPref.USER_NAME,""));
            map.put(Constants.TAG_USER_IMAGE, SharedPref.getString(SharedPref.USER_IMAGE,""));
            map.put(StreamConstants.TAG_STREAM_NAME, streamName);
            map.put(Constants.TAG_MESSAGE, "" + edtMessage.getText());
            map.put(Constants.TAG_TYPE, Constants.TAG_TEXT);
            addComment(map);
            edtMessage.getText().clear();
            if (mSocket.connected()) {
                Log.i(TAG, "sendChat: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_SEND_MESSAGE, jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "sendChat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onBufferedAmountChange(long previousAmount, String dataChannelLabel) {

    }

    @Override
    public void onStateChange(DataChannel.State state, String dataChannelLabel) {

    }

    @Override
    public void onMessage(DataChannel.Buffer buffer, String dataChannelLabel) {

    }

    @Override
    public void onMessageSent(DataChannel.Buffer buffer, boolean successful) {

    }

    private void attempt2Reconnect() {
        Log.w(getClass().getSimpleName(), "Attempt2Reconnect called");
        if (!webRTCClient.isStreaming()) {
            webRTCClient.startStream();
            if (webRTCMode.equals(IWebRTCClient.MODE_JOIN)) {
                pipViewRenderer.setZOrderOnTop(true);
            }
        }
    }

    @Override
    public void onDisconnected(String streamId) {

    }

    @Override
    public void onPublishFinished(String streamId) {

    }

    @Override
    public void onPlayFinished(String streamId) {
        unSubscribeStream();
        showToastAlert(getString( R.string.stream_end_description));
    }

    @Override
    public void onPublishStarted(String streamId) {

    }

    @Override
    public void onPlayStarted(String streamId) {
        Log.w(getClass().getSimpleName(), "onPlayStarted");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webRTCClient.switchVideoScaling(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                webRTCClient.getStreamInfoList();
                setBuffering(GONE);
                loadingLay.setVisibility(GONE);
                initialIndicator.hide();
                initializeLay.setVisibility(GONE);
                isStarted = true;
                //  subscribeStream();
                // sendUserJoined();
            }
        });
    }

    @Override
    public void noStreamExistsToPlay(String streamId) {
        Log.w(getClass().getSimpleName(), "noStreamExistsToPlay");
        unSubscribeStream();
        showToastAlert(getString( R.string.stream_end_description));
    }

    @Override
    public void onError(String description, String streamId) {

    }

    @Override
    public void onSignalChannelClosed(WebSocket.WebSocketConnectionObserver.WebSocketCloseNotification code, String streamId) {

    }

    @Override
    public void streamIdInUse(String streamId) {
        Log.w(getClass().getSimpleName(), "streamIdInUse");
    }

    @Override
    public void onIceConnected(String streamId) {
        // remove scheduled reconnection attempts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (reconnectionHandler.hasCallbacks(reconnectionRunnable)) {
                reconnectionHandler.removeCallbacks(reconnectionRunnable, null);
            }
        } else {
            reconnectionHandler.removeCallbacks(reconnectionRunnable, null);
        }
    }

    @Override
    public void onIceDisconnected(String streamId) {

    }

    @Override
    public void onPeerConnectionClosed(String streamId) {
       // showToastAlert(getString( R.string.stream_end_description));
        Is_networkchecked = true;
        unSubscribeStream();
    }

    @Override
    public void onTrackList(String[] tracks) {

    }

    @Override
    public void onBitrateMeasurement(String streamId, int targetBitrate, int videoBitrate, int audioBitrate) {

    }

    @Override
    public void onStreamInfoList(String streamId, ArrayList<StreamInfo> streamInfoList) {

    }

    /* @Override
     public void onBandwidthStateChange(ConnectionQuality bandwidthState) {

     }
 */
    public class ViewerListViewAdapter extends RecyclerView.Adapter<ViewerListViewAdapter.MyViewHolder> {

        ArrayList<StreamDetails.LiveViewers> viewersList;
        Context context;
        Random rnd;

        public ViewerListViewAdapter(Context context, ArrayList<StreamDetails.LiveViewers> viewersList) {
            this.viewersList = viewersList;
            this.context = context;
            rnd = new Random();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate( R.layout.item_viewers, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final StreamDetails.LiveViewers viewer = viewersList.get(position);
            int color = Color.argb(60, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
            Glide.with(context).load(viewer.getUserImage())
                    .apply(RequestOptions.circleCropTransform())
                    .thumbnail(0.5f)
                    .error(R.drawable.alhawana)
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(holder.userImage);
            holder.colorImage.setBackgroundColor(color);

            holder.itemLay.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return viewersList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView userImage;
            ImageView colorImage;
            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                userImage = view.findViewById( R.id.userImage);
                colorImage = view.findViewById( R.id.colorImage);
                itemLay = view.findViewById( R.id.itemLay);
            }
        }

    }

    public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public final int VIEW_TYPE_JOIN = 0;
        public final int VIEW_TYPE_TEXT = 1;
        public final int VIEW_TYPE_GIFT = 2;
        ArrayList<HashMap<String, String>> commentList;
        Context context;

        public CommentsAdapter(Context context, ArrayList<HashMap<String, String>> commentList) {
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

                }
            }
            return VIEW_TYPE_TEXT;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_JOIN) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate( R.layout.item_user_join, parent, false);
                return new JoiningViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_TEXT) {
                View view = LayoutInflater.from(parent.getContext()).inflate( R.layout.item_comment, parent, false);
                return new MyViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

            if (viewHolder instanceof MyViewHolder) {
                final MyViewHolder holder = (MyViewHolder) viewHolder;
                final HashMap<String, String> map = commentList.get(position);
                holder.txtMessage.setText(map.get(Constants.TAG_MESSAGE));
                holder.txtUserName.setText("@" + map.get(Constants.TAG_USER_NAME));
                new CountDownTimer(20000, 4000) { //(timer_duration, timer_interval)
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
                new CountDownTimer(20000, 4000) { //(timer_duration, timer_interval)
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
            }

        }

        public class JoiningViewHolder extends RecyclerView.ViewHolder {
            TextView txtJoined;
            RelativeLayout itemLay;

            public JoiningViewHolder(View view) {
                super(view);
                txtJoined = view.findViewById( R.id.txtJoined);
                itemLay = view.findViewById( R.id.itemLay);

            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView txtUserName;
            TextView txtMessage;
            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);

                txtUserName = view.findViewById( R.id.txtUserName);
                txtMessage = view.findViewById( R.id.txtMessage);
                itemLay = view.findViewById( R.id.itemLay);

            }
        }

    }



    private void setFadeAnimation(final View view, final int position) {
        /*sets animation from complete opaque state(1.0f) to complete transparent state(0.0f)*/
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(StreamConstants.FADE_DURATION);
        view.startAnimation(anim);
    }

    public class BottomListAdapter extends RecyclerView.Adapter<BottomListAdapter.MyViewHolder> {

        ArrayList<StreamDetails.LiveViewers> viewerList;
        Context context;
        Random rnd;

        public BottomListAdapter(Context context, ArrayList<StreamDetails.LiveViewers> viewerList) {
            this.viewerList = viewerList;
            this.context = context;
            rnd = new Random();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate( R.layout.item_viewer, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final StreamDetails.LiveViewers viewer = viewerList.get(position);

            int color = Color.argb(60, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
            Glide.with(context).load("" + viewer.getUserImage())
                    .apply(RequestOptions.circleCropTransform())
                    .thumbnail(0.5f)
                    .error(R.drawable.alhawana)
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(holder.userImage);
            holder.colorImage.setBackgroundColor(color);
            holder.txtUserName.setText(viewer.getUserName());
            holder.mainLay.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return viewerList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            RoundedImageView userImage;
            RoundedImageView colorImage;
            TextView txtUserName;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                userImage = view.findViewById( R.id.userImage);
                colorImage = view.findViewById( R.id.colorImage);
                txtUserName = view.findViewById( R.id.txtUserName);
                mainLay = view.findViewById( R.id.mainLay);

            }
        }
    }

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
                    .inflate( R.layout.live_streaming_item_report, parent, false);
            viewHolder = new MyViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {

            Log.d(TAG, "onBind: LiveSTreamList" + reportList.get(position).getTitle());

            /* ((MyViewHolder) holder).txtReport.setText(reportList.get(position));*/

            ((MyViewHolder) holder).txtReport.setText(reportList.get(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return reportList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView txtReport;
            LinearLayout layItemReport;

            public MyViewHolder(View view) {
                super(view);

                txtReport = view.findViewById( R.id.txtReport);
                layItemReport = view.findViewById( R.id.layItemReport);

                layItemReport.setOnClickListener(v -> {
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
}