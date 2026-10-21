package com.app.addon.livestream.broadcaster.liveVideoBroadcaster;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT;
import static android.telephony.TelephonyManager.NETWORK_TYPE_CDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_IDEN;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UNKNOWN;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.app.alhawanajd.App.makeToast;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.addon.livestream.external.avloading.AVLoadingIndicatorView2;
import com.app.addon.livestream.external.avloading.BallBeatIndicator;
import com.app.addon.livestream.external.heartlayout.HeartLayout;
import com.app.addon.livestream.external.helper.CustomLayoutManager;
import com.app.addon.livestream.external.helper.LocaleManager;
import com.app.addon.livestream.external.helper.SocketIO;
import com.app.addon.livestream.external.helper.Utils;
import com.app.addon.livestream.external.model.StreamDetails;
import com.app.addon.livestream.external.utils.Constants;
import com.app.db.DBHelper;
import com.app.alhawanajd.LoginActivity;
import com.app.model.GetSet;
import com.app.addon.livestream.external.utils.StreamConstants;
import com.app.alhawanajd.App;
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.helper.NetworkReceiver;
import com.app.model.StartStreamResponse;
import com.app.utils.AppUtils;
import com.app.utils.Logging;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.makeramen.roundedimageview.RoundedImageView;
import de.tavendo.autobahn.WebSocket;


import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;


import io.antmedia.webrtcandroidframework.IDataChannelObserver;
import io.antmedia.webrtcandroidframework.IWebRTCListener;
import io.antmedia.webrtcandroidframework.StreamInfo;
import io.antmedia.webrtcandroidframework.WebRTCClient;
import io.antmedia.webrtcandroidframework.apprtc.AppRTCAudioManager;
import io.antmedia.webrtcandroidframework.apprtc.CallActivity;

import org.webrtc.EglRenderer;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;


import io.antmedia.webrtcandroidframework.IWebRTCClient;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PublishActivity extends BaseFragmentActivity implements SocketIO.SocketEvents,
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, IWebRTCListener, IDataChannelObserver,View.OnClickListener {

    private static final String TAG = PublishActivity.class.getSimpleName();

    private GLSurfaceView mGLView;
    private FrameLayout cameraLay;
    private PreviewView previewView;
    private ConstraintLayout publishLay;
    private EditText edtTitle;
    private RelativeLayout tipsLay ;
    private LinearLayout loadingTipsLay;
    private TextView txtTips , textfront , textback , txtTitle;
    private ImageView btnCancelTips, btnCancel;
    private Button btnGoLive;
    private BottomSheetDialog bottomCameraDialog;
    private LinearLayout frontCameraLay;
    private LinearLayout backCameraLay;
    private RelativeLayout bottomLay;
    private RelativeLayout commentsLay;
    private RecyclerView rvComments;
    private HeartLayout heartLay;
    private LinearLayout viewersLay;
    private RelativeLayout viewLay;
    private ImageView iconView;
    private AppCompatTextView txtViewCount;
    private RecyclerView rvViewers;
    private LinearLayout stopLay;
    private Button btnStopBroadCast;
    private RoundedImageView btnDetail;
    private RelativeLayout loadingLay;
    private ImageView loadingImage;
    private LinearLayout initializeLay;
    private AVLoadingIndicatorView2 avBallIndicator;
    private AppCompatTextView txtLiveCount;
    private RelativeLayout liveCountLay;
    private FrameLayout liveTxtLay;
    private View transparentCover;
    RelativeLayout bottomDurationLay;
    AppCompatTextView bottomStreamTitle;
    RoundedImageView publisherImage;
    RoundedImageView publisherColorImage;
    AppCompatTextView txtPublisherName ;
    LinearLayout bottomFirstLay, bottomDeleteLay, bottomReportLay, chatHideLay, bottomDetailsLay,
            bottomUserLay, bottomTopLay, bottomViewersLay;
    AppCompatTextView bottomViewerCount, txtBottomDuration , broad_text , text_hide;
    RecyclerView bottomRecyclerView, bottomGiftView;
    private BottomSheetDialog bottomDialog, bottomCommentsDialog, bottomGiftsDialog, bottomHashTagsDialog;
    private View bottomSheet;
    private String  streamMode = null, streamLink = null,
            inviteSocketLink = null, inviteApiLink = null;
    private static final long FADE_DURATION = 1000;
    // private Intent mLiveVideoBroadcasterServiceIntent;
    //  private ILiveVideoBroadcaster mLiveVideoBroadcaster;
    private Animation fadeOut, shake;
    // Gestures are used to toggle the focus modes
    protected GestureDetectorCompat mAutoFocusDetector = null;
    private boolean mIsRecording = false, isPublished = false;
    private ApiInterface apiInterface;
    private String title, streamName, getVideoId, streamToken = null;
    private final Handler handler = new Handler();
    private int resultCode = RESULT_CANCELED;
    private SurfaceViewRenderer cameraViewRenderer;
    private SurfaceViewRenderer pipViewRenderer;
    FrameLayout cameraView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ArrayList<StreamDetails.LiveViewers> viewersList = new ArrayList<>();
    ArrayList<HashMap<String, String>> commentList = new ArrayList<HashMap<String, String>>();
    final int RECONNECTION_PERIOD_MLS = 100;
    private ViewerListViewAdapter viewerListAdapter;
    private BottomListAdapter bottomListAdapter;
    private CommentsAdapter commentsAdapter;
    private final String[] mRequiredPermissions = new String[]{
            CAMERA,
            RECORD_AUDIO,
            WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private SocketIO socketIO;
    private Socket mSocket;
    private int displayHeight, displayWidth;
    private final boolean isStreamStarted = false;
    private final Handler serviceHandler = new Handler();
    private String webRTCMode = IWebRTCClient.MODE_PUBLISH;
    private WebRTCClient webRTCClient;
    private boolean enableDataChannel = true;
    @Nullable
    private AppRTCAudioManager audioManager;
    public Bitmap screenBitmap;
    public String from;
    public ByteArrayOutputStream screenStream = new ByteArrayOutputStream();
    private final Runnable bindServiceRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(TAG, "run: bindServiceRunnable");
                //checkServiceIsConnected();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
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

    public EglRenderer.FrameListener captureFrameListener = new EglRenderer.FrameListener() {
        public void onFrame(Bitmap bitmap) {
            PublishActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    PublishActivity.this.cameraViewRenderer.removeFrameListener(PublishActivity.this.captureFrameListener);
                }
            });
            Bitmap unused = PublishActivity.this.screenBitmap = bitmap;
            if (PublishActivity.this.screenBitmap != null) {
                PublishActivity.this.screenBitmap.compress(Bitmap.CompressFormat.PNG, 90, PublishActivity.this.screenStream);
            }
        }
    };

    public InputStream imageStream;


    private GestureDetector gestureScanner;
    Utils appUtils;
    AppUtils utils;

    //    private DatabaseHandler dbHelper;
    private static final long SECONDS_INTERVAL = 1000;
    private static final long SCREEN_UPLOAD_INTERVAL = 10000;
    private boolean isCameraOpened = false;
    private int lensFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private SharedPreferences pref;
    private Context context;
    ProgressBar progress_bar;
    private final Runnable imageUpload = new Runnable() {
        @Override
        public void run() {
            try {
                takeScreenShot();
                handler.postDelayed(imageUpload, SCREEN_UPLOAD_INTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "onConnect: " + "EVENT_CONNECTED");
        }
    };
    private final Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "onDiconnect: " + "EVENT_DISCCONNECTED");
        }
    };
    private final Emitter.Listener _msgReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "msgReceived: Lisener" + data);
                    String type = data.optString(Constants.TAG_TYPE);
                    String keyword = data.optString("streamer_in_background");
                    Log.d(TAG, "run_keyword: "+keyword);
                    switch (type) {
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
                    }
                }
            });
        }
    };
    private final Emitter.Listener _subscriberLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
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
    private final Emitter.Listener _streamInfo = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "_streamInfo: " + data);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Gson gson = new Gson();
                    StreamDetails tempDetails = gson.fromJson("" + data, StreamDetails.class);
                    txtViewCount.setText(tempDetails.getWatchCount() != null ? tempDetails.getWatchCount() : "");
                    viewersList.clear();
                    viewersList.addAll(tempDetails.getLiveViewers());
                    viewerListAdapter.notifyDataSetChanged();
                    setBottomDialogUI(tempDetails);
                }
            });
        }
    };
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    /*private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LiveVideoBroadcaster.LocalBinder binder = (LiveVideoBroadcaster.LocalBinder) service;
            if (mLiveVideoBroadcaster == null) {
                mLiveVideoBroadcaster = binder.getService();
                mLiveVideoBroadcaster.init(PublishActivity.this, mGLView);
                mLiveVideoBroadcaster.setAdaptiveStreaming(true);
            }
            mLiveVideoBroadcaster.openCamera(lensFacing);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isCameraOpened = true;
                }
            }, 1000);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mLiveVideoBroadcaster = null;
        }
    };*/
    private long startTime;
    Timer netWorktimer = new Timer();
    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startTime += SECONDS_INTERVAL;
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(Constants.TAG_TYPE, StreamConstants.TAG_DURATION);
                        jsonObject.put(StreamConstants.TAG_TIME, appUtils.getCommentsDuration(startTime));
                        jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
                        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mSocket != null && mSocket.connected()) {
                                    mSocket.emit(StreamConstants.TAG_SEND_MESSAGE, jsonObject);
                                }
                            }
                        }, 500);

                    } catch (JSONException e) {
                        Log.e(TAG, "timerTask: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private boolean isBroadCastStopped = false;
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
                    triggerStopRecording();

                }
            });

        }
    };

    public void backgroundInNetWorkSpeed(){

        netWorktimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // this code will be executed after 2 seconds
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        backgroundInNetWorkSpeed();
                        checkNetworkType();
                    }
                });

            }
        }, 10000);

    }

    private boolean connectionStatus;

    private void checkNetworkType(){

        connectionStatus = isConnectedFast(getApplicationContext());
        if(!connectionStatus){
            // connection is slow
            //Toast.makeText(getApplicationContext(),"Network is poor",Toast.LENGTH_LONG).show();
        } else {
            // connection is fast
            //Toast.makeText(getApplicationContext(),"goodabnd",Toast.LENGTH_LONG).show();

        }

    }


    private NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    private boolean isConnectedFast(Context context){
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && isConnectionFast(info.getType(),info.getSubtype(), context));
    }

    private boolean isConnectionFast(int type, int subType, Context context){
        if(type == ConnectivityManager.TYPE_WIFI){
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int speedMbps = wifiInfo.getLinkSpeed();

            Log.e("NetSpeed","-"+speedMbps);
            switch(speedMbps){
                case NETWORK_TYPE_1xRTT:
                    Toast.makeText(getApplicationContext(),"Poor Network connection",Toast.LENGTH_SHORT).show();
                    return false; // ~ 50-100 kbps
                case NETWORK_TYPE_CDMA:
                    Toast.makeText(getApplicationContext(),"Poor Network connection",Toast.LENGTH_SHORT).show();
                    return false; // ~ 14-64 kbps
                case NETWORK_TYPE_EDGE:
                    Toast.makeText(getApplicationContext(),"Poor Network connection",Toast.LENGTH_SHORT).show();
                    return false; // ~ 50-100 kbps
                case NETWORK_TYPE_EVDO_0:
                    Toast.makeText(getApplicationContext(),"Poor Network connection",Toast.LENGTH_SHORT).show();
                    return true; // ~ 400-1000 kbps
                case NETWORK_TYPE_EVDO_A:
                    //Toast.makeText(getApplicationContext(),"High Network connection",Toast.LENGTH_SHORT).show();
                    return true; // ~ 600-1400 kbps
                case NETWORK_TYPE_GPRS:
                    Toast.makeText(getApplicationContext(),"Poor Network connection",Toast.LENGTH_SHORT).show();
                    return false; // ~ 100 kbps
                case NETWORK_TYPE_HSDPA:
                    //Toast.makeText(getApplicationContext(),"High Network connection"+speedMbps,Toast.LENGTH_SHORT).show();
                    return true; // ~ 2-14 Mbps
                case NETWORK_TYPE_HSPA:
                    //Toast.makeText(getApplicationContext(),"High Network connection"+speedMbps,Toast.LENGTH_SHORT).show();
                    return true; // ~ 700-1700 kbps
                case NETWORK_TYPE_HSUPA:
                    //Toast.makeText(getApplicationContext(),"High Network connection"+speedMbps,Toast.LENGTH_SHORT).show();
                    return true; // ~ 1-23 Mbps
                case NETWORK_TYPE_UMTS:
                    //Toast.makeText(getApplicationContext(),"High Network connection"+speedMbps,Toast.LENGTH_SHORT).show();
                    return true; // ~ 400-7000 kbps

                // Above API level 7, make sure to set android:targetSdkVersion to appropriate level to use these

                case NETWORK_TYPE_EHRPD: // API level 11
                    //Toast.makeText(getApplicationContext(),"High Network connection"+speedMbps,Toast.LENGTH_SHORT).show();
                    return true; // ~ 1-2 Mbps
                case NETWORK_TYPE_EVDO_B: // API level 9
                    //Toast.makeText(getApplicationContext(),"High Network connection"+speedMbps,Toast.LENGTH_SHORT).show();
                    return true; // ~ 5 Mbps
                case NETWORK_TYPE_HSPAP: // API level 13
                    //Toast.makeText(getApplicationContext(),"High Network connection"+speedMbps,Toast.LENGTH_SHORT).show();
                    return true; // ~ 10-20 Mbps
                case NETWORK_TYPE_IDEN: // API level 8
                    //Toast.makeText(getApplicationContext(),"High Network connection"+speedMbps,Toast.LENGTH_SHORT).show();
                    return false; // ~25 kbps
                case NETWORK_TYPE_LTE: // API level 11
                    //Toast.makeText(getApplicationContext(),"High Network connection"+speedMbps,Toast.LENGTH_SHORT).show();
                    return true; // ~ 10+ Mbps
                // Unknown
                case NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
            //return true;
        } else if(type == ConnectivityManager.TYPE_MOBILE){

            switch(subType){
                case NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps

                // Above API level 7, make sure to set android:targetSdkVersion to appropriate level to use these

                case NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }

    }

    private void setBottomDialogUI(StreamDetails details) {
        bottomListAdapter.notifyDataSetChanged();
        bottomStreamTitle.setText(details.getTitle());
        bottomViewerCount.setText(details.getWatchCount());
        txtLiveCount.setText(details.getWatchCount());
    }

    private void addComment(HashMap<String, String> map) {
        commentList.add(map);
        commentsAdapter.notifyItemInserted(commentList.size() - 1);
        rvComments.scrollToPosition(commentList.size() - 1);
    }

    private void publishStream() {
        Log.d(TAG, "publishStreamcheck: ");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", GetSet.getUserId());
            jsonObject.put("stream_name", streamName);
            Log.d(TAG, "publishStream_chck_name: "+streamName);

            if (mSocket != null && mSocket.connected()) {
                Logging.i(TAG, "publishStream: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_PUBLISH_STREAM, jsonObject);
            }
            String str2 = TAG;
            //Log.i(str2, "socketpublishStream: " + mSocket.connected());
        } catch (JSONException e) {
            Log.e(TAG, "publishStream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopStreamSocket() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_TYPE, "streamend");
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            if (mSocket != null && mSocket.connected()) {
                Log.i(TAG, "stopStreamSocket: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_STOP_STREAM, jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "stopStreamSocket error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*Socket: stopstream
    {
        "type": "streamend",
            "user_id": "602bd7ad7e9169fb60edff0d",
            "user_name": "RaJ",
            "user_image": "https:\/\/fundoo.appkodes.in\/public\/img\/accounts\/profile_image-1613486047703",
            "stream_name": "Video_sT69vGvNynpX",
    }*/

    private void getStreamInfo(String streamName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            if (mSocket != null && mSocket.connected()) {
                Log.i(TAG, "getStreamInfo: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_GET_STREAM_INFO, jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "getStreamInfo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static final String[] MANDATORY_PERMISSIONSFORANDROID12 = {MODIFY_AUDIO_SETTINGS,
            RECORD_AUDIO, INTERNET, CAMERA, WAKE_LOCK, BLUETOOTH_CONNECT,READ_PHONE_STATE};

    private boolean mPermissionsGranted = false;
    private boolean isRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        overridePendingTransition(0, 0);
        context = this;
        Intent intent = getIntent();

//        this.cameraViewRendererAg = findViewById(R.id.camera_view_renderer_ag);
        title = intent.getStringExtra(StreamConstants.TAG_TITLE);
        streamName = intent.getStringExtra(StreamConstants.TAG_STREAM_NAME);
        Log.d(TAG, "onCreatecheckname: "+streamName);
        streamToken = intent.getStringExtra(StreamConstants.TAG_STREAM_TOKEN);
        lensFacing = intent.getIntExtra(StreamConstants.TAG_LENS_FACING, lensFacing);
        fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out_fast);
        shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        //binding on resume not to having leaked service connection
        //   mLiveVideoBroadcasterServiceIntent = new Intent(this, LiveVideoBroadcaster.class);
        //this makes service do its job until done
        //  startService(mLiveVideoBroadcasterServiceIntent);
        setContentView(R.layout.activity_publish);
        pref = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Constants.isInStream = true;
        appUtils = new Utils(this);
        //     dbHelper = DatabaseHandler.getInstance(this);
        gestureScanner = new GestureDetector(this, this);
        cameraViewRenderer = findViewById(R.id.camera_view_renderer);
        pipViewRenderer = findViewById(R.id.pip_view_renderer);
        cameraView = findViewById(R.id.cameraView);
        findViews();

        // Configure the GLSurfaceView.  This will start the Renderer thread, with an
        // appropriate EGL activity.
        if (mGLView != null) {
            mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        }

      /*  if(lensFacing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            cameraViewRenderer.setMirror(lensFacing == Camera.CameraInfo.CAMERA_FACING_FRONT);  //this line to mirror for frontcamera
        else cameraViewRenderer.setMirror(false);
*/

        // If running on Android 6 (Marshmallow) and later, check to see if the necessary permissions
        // have been granted
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionsGranted = hasPermissions(this, mRequiredPermissions);
            if (!mPermissionsGranted)
                ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
        } else
            mPermissionsGranted = true;*/

        this.cameraProviderFuture = ProcessCameraProvider.getInstance(this.context);
        initCameraView();
        // return if the user hasn't granted the app the necessary permissions
       // if (!mPermissionsGranted) return;

        initView();
        initSocket();
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                initSocket();
            }
        }, 500);
        // initCameraView();
//        checkServiceIsConnected();
        // backgroundInNetWorkSpeed();
        streamLink = intent.getStringExtra(StreamConstants.TAG_INVITE_LINK);
        audioManager = AppRTCAudioManager.create(PublishActivity.this);
    }


    private void initCameraView() {
        //if (SDK_INT>=Build.VERSION_CODES.S){

        pauseExternalAudio(this);
        bindCameraUseCases();
      /*  new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bindCameraUseCases();
            }
        }, 500);*/

        //}
        //  bindCameraUseCases();
    }

    private void bindCameraUseCases() {
     /*   // Get screen metrics used to setup camera for full screen resolution
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenAspectRatio = aspectRatio(displayMetrics.widthPixels, displayMetrics.heightPixels);
        int rotation = previewView.getDisplay().getRotation();
        // Bind the CameraProvider to the LifeCycleOwner
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();*/

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /*void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.createSurfaceProvider());

        cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview);
    }*/

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        int lensfac = 0;
        if(lensFacing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            lensfac = CameraSelector.LENS_FACING_FRONT;             //for front camera
        } else{
            lensfac = CameraSelector.LENS_FACING_BACK;             //for back camera
        }

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensfac)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());


        cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview);
    }


    /*@Override
    public void onNetworkChange(boolean isConnected) {
        if (!isConnected) {
            finish();
        }
    }*/

    /*private void checkServiceIsConnected() {
        if (isCameraOpened) {
            Log.d(TAG, "checkServiceIsConnected: amera");
            startPublish();
        } else {
            Log.d(TAG, "checkServiceIsConnected: else");
            serviceHandler.postDelayed(bindServiceRunnable, 1000);
        }
    }*/

    public void findViews() {
        mGLView = findViewById(R.id.cameraPreview_surfaceView);
        publishLay = findViewById(R.id.publishLay);
        edtTitle = findViewById(R.id.edtTitle);
        loadingTipsLay = findViewById(R.id.loadingTipsLay);
        tipsLay = findViewById(R.id.tipsLay);
        txtTips = findViewById(R.id.txtTips);
        btnCancelTips = findViewById(R.id.btnCancelTips);
        btnCancel = findViewById(R.id.btnCancel);

        btnGoLive = findViewById(R.id.btnGoLive);
        bottomLay = findViewById(R.id.bottomLay);
        commentsLay = findViewById(R.id.commentsLay);
        rvComments = findViewById(R.id.rv_comments);
        heartLay = findViewById(R.id.heartLay);
        viewersLay = findViewById(R.id.viewersLay);
        viewLay = findViewById(R.id.viewLay);
        iconView = findViewById(R.id.iconView);
        txtViewCount = findViewById(R.id.txtViewCount);
        rvViewers = findViewById(R.id.rv_viewers);
        stopLay = findViewById(R.id.stopLay);
        btnStopBroadCast = findViewById(R.id.btnStopBroadCast);
        btnDetail = findViewById(R.id.btnDetail);
        loadingLay = findViewById(R.id.loadingLay);
        loadingImage = findViewById(R.id.loadingImage);
        initializeLay = findViewById(R.id.initializeLay);
        avBallIndicator = findViewById(R.id.avBallIndicator);
        transparentCover = findViewById(R.id.transparent_cover);
        progress_bar = findViewById(R.id.progress_bar);
        previewView = findViewById(R.id.previewView);



        avBallIndicator.setIndicator(new BallBeatIndicator());
        btnStopBroadCast.setOnClickListener(this);
        btnDetail.setOnClickListener(this);
        btnGoLive.setOnClickListener(this);
        tipsLay.setOnClickListener(this);
        viewLay.setOnClickListener(this);
        btnCancelTips.setOnClickListener(this);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });



        edtTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() >=80){
                    Toast.makeText(context, getString(R.string.reachlimt), Toast.LENGTH_SHORT).show();

                }
            }
        });


    }

    public void initView() {
        // Initialize the UI controls
        if (LocaleManager.isRTL()) {
            viewLay.setBackground(getResources().getDrawable(R.drawable.rounded_curve_rtl_bg));
        } else {
            viewLay.setBackground(getResources().getDrawable(R.drawable.rounded_curve_bg));
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ABOVE, R.id.viewersLay);
        params.height = (int) (displayHeight * 0.4);
        commentsLay.setLayoutParams(params);

        // setLoadingLay(VISIBLE);
        Glide.with(this)
                .load(GetSet.getImageUrl())
                .placeholder(R.drawable.alhawana)
                .error(R.drawable.alhawana)
                .thumbnail(0.1f)
                .into(loadingImage);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvViewers.setLayoutManager(linearLayoutManager);
        viewerListAdapter = new ViewerListViewAdapter(this, viewersList);
        rvViewers.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvViewers.setAdapter(viewerListAdapter);
        rvViewers.setHasFixedSize(true);
        viewerListAdapter.notifyDataSetChanged();

        commentsAdapter = new CommentsAdapter(this, commentList);
        CustomLayoutManager commentLayoutManager = new CustomLayoutManager(this);
        commentLayoutManager.setScrollEnabled(false);
        rvComments.setLayoutManager(commentLayoutManager);
        rvComments.setAdapter(commentsAdapter);
        commentsAdapter.notifyDataSetChanged();

        transparentCover.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureScanner.onTouchEvent(event);
            }
        });
        initBottomDetailsDialog();
        initBottomViewersDialog();


    }

    private void initBottomDetailsDialog() {
        bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_details, null);
        bottomDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomDialog.setContentView(bottomSheet);
        bottomDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });

        bottomFirstLay = bottomSheet.findViewById(R.id.bottomFirstLay);
        bottomDeleteLay = bottomSheet.findViewById(R.id.bottomDeleteLay);
        liveCountLay = bottomSheet.findViewById(R.id.liveCountLay);
        liveTxtLay = bottomSheet.findViewById(R.id.liveTxtLay);
        txtLiveCount = bottomSheet.findViewById(R.id.txtLiveCount);
        bottomStreamTitle = bottomSheet.findViewById(R.id.bottomStreamTitle);
        bottomUserLay = bottomSheet.findViewById(R.id.bottomUserLay);
        publisherImage = bottomSheet.findViewById(R.id.publisherImage);
        publisherColorImage = bottomSheet.findViewById(R.id.publisherColorImage);
        txtPublisherName = bottomSheet.findViewById(R.id.txtPublisherName);
        bottomDetailsLay = bottomSheet.findViewById(R.id.bottomDetailsLay);
        broad_text = bottomSheet.findViewById(R.id.broad_text);
        text_hide = bottomSheet.findViewById(R.id.text_hide);
        chatHideLay = bottomSheet.findViewById(R.id.chatHideLay);
        bottomReportLay = bottomSheet.findViewById(R.id.bottomReportLay);

        if (LocaleManager.isRTL()) {
            liveTxtLay.setBackground(getResources().getDrawable(R.drawable.live_status_bg_rtl));
            liveCountLay.setBackground(getResources().getDrawable(R.drawable.live_count_bg_rtl));

        } else {
            liveTxtLay.setBackground(getResources().getDrawable(R.drawable.live_status_bg));
            liveCountLay.setBackground(getResources().getDrawable(R.drawable.live_count_bg));
        }

        bottomStreamTitle.setText(title);
        bottomDeleteLay.setVisibility(GONE);
        bottomReportLay.setVisibility(GONE);
        chatHideLay.setVisibility(VISIBLE);
        bottomUserLay.setVisibility(GONE);

        bottomDetailsLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                if (bottomCommentsDialog != null && !bottomCommentsDialog.isShowing()) {
                    bottomCommentsDialog.show();
                }
            }
        });

        chatHideLay.setOnClickListener(new View.OnClickListener() {
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
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
    }

    private void initBottomViewersDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_viewers, null);
        bottomCommentsDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomCommentsDialog.setContentView(bottomSheet);

        bottomViewersLay = bottomSheet.findViewById(R.id.bottomViewersLay);
        bottomViewerCount = bottomSheet.findViewById(R.id.bottomViewerCount);
        bottomRecyclerView = bottomSheet.findViewById(R.id.bottomRecyclerView);
        bottomTopLay = bottomSheet.findViewById(R.id.bottomTopLay);
        txtBottomDuration = bottomSheet.findViewById(R.id.txtBottomDuration);
        bottomDurationLay = bottomSheet.findViewById(R.id.bottomDurationLay);

        bottomDurationLay.setVisibility(GONE);

        bottomListAdapter = new BottomListAdapter(this, viewersList);
        bottomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bottomRecyclerView.setAdapter(bottomListAdapter);
        bottomRecyclerView.setHasFixedSize(true);
        bottomListAdapter.notifyDataSetChanged();

        bottomCommentsDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        bottomCommentsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
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

    private void initSocket() {
        SocketIO.mSocket = null;
        socketIO = new SocketIO(PublishActivity.this);
        mSocket = socketIO.getInstance();
        socketIO.setSocketEvents(PublishActivity.this);

//        mSocket.on(Socket.EVENT_CONNECT, onConnect);
//        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(StreamConstants.TAG_STREAM_BLOCKED, _streamBlocked);
        mSocket.on(StreamConstants.TAG_MESSAGE_RECEIVED, _msgReceived);
        mSocket.on(StreamConstants.TAG_SUBSCRIBER_LEFT, _subscriberLeft);
        mSocket.on(StreamConstants.TAG_STREAM_INFO, _streamInfo);
      //  mSocket.on(StreamConstants.TAG_STREAM_BLOCKED, _streamBlocked);
        mSocket.connect();
    }

   /* private void startPublish() {
        if (!mIsRecording) {
            if (mLiveVideoBroadcaster != null) {
                if (!mLiveVideoBroadcaster.isConnected()) {
                    String builder = pref.getString(StreamConstants.TAG_STREAM_BASE_URL, StreamConstants.RTMP_URL)
                            + streamName + "?" + StreamConstants.TAG_TOKEN + "=" + (streamToken != null ? streamToken : "");
                    Log.d(TAG, "startPublish builder: " + builder);
                    Log.d(TAG, "streamName=> " + streamName);

                    new PublishStreamTask().execute(builder);
                } else {
                    Toast.makeText(PublishActivity.this, getString(R.string.streaming_not_finished), Toast.LENGTH_SHORT).show();
                    //makeToast(getString(R.string.streaming_not_finished));
                    finish();
                }
            } else {
                Toast.makeText(PublishActivity.this, getString(R.string.oopps_shouldnt_happen), Toast.LENGTH_SHORT).show();
                //makeToast(getString(R.string.oopps_shouldnt_happen));
                finish();
            }
        } else {
            triggerStopRecording();
        }
    }*/

    @Override
    public void onBufferedAmountChange(long previousAmount, String dataChannelLabel) {

    }

    @Override
    public void onStateChange(DataChannel.State state, String dataChannelLabel) {
        Log.d(TAG, "onStateChange: " + state + ", " + dataChannelLabel);
    }

    @Override
    public void onMessage(DataChannel.Buffer buffer, String dataChannelLabel) {

    }

    @Override
    public void onMessageSent(DataChannel.Buffer buffer, boolean successful) {

    }

    @Override
    public void onDisconnected(String streamId) {
        Log.e(TAG, "onDisconnected: " + streamId);
    }

    @Override
    public void onPublishFinished(String streamId) {
        triggerStopRecording();
    }

    @Override
    public void onPlayFinished(String streamId) {
        Log.w(getClass().getSimpleName(), "onPlayFinished");
    }

    @Override
    public void onPublishStarted(String streamId) {
        Log.v(TAG, "onPublishStarted: " + streamId);
        if (audioManager != null) {
            if (audioManager.hasWiredHeadset()) {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET);
            } else {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            }
            startAudioManager();
        }
        initStream();
     /*   Task<ShortDynamicLink> shortLinkTask = utils.getStreamShareLink(streamLink);
//        Log.d(TAG, "onPublishStarted: " + shortLinkTask.toString());
        shortLinkTask.addOnCompleteListener(PublishActivity.this, new OnCompleteListener<ShortDynamicLink>() {
            @Override
            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                Log.v(TAG, "onPublishStarted: " + task.isSuccessful() + ", streamlink " + streamLink);
                if (task.isSuccessful()) {
                    // Short link created
                    Uri shortLink = task.getResult().getShortLink();
                    inviteApiLink = "" + shortLink;
                    inviteSocketLink = GetSet.getUserName() + " " + getString(R.string.stream_share_description) + " " + shortLink;
                    setLoadingLay(GONE);
                    resultCode = RESULT_OK;
                    initStream();
                } else {
                    // Error
                    Log.e(TAG, "onPublishStarted: " + ", streamlink: " + streamLink);
                    App.makeToast(getString(R.string.something_went_wrong));
                    triggerStopRecording();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onPublishStarted: " + e.getMessage());
                App.makeToast(getString(R.string.something_went_wrong));
                triggerStopRecording();
            }
        });*/

    }

    @Override
    public void onPlayStarted(String streamId) {
        Log.w(getClass().getSimpleName(), "onPlayStarted");
        webRTCClient.switchVideoScaling(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        webRTCClient.getStreamInfoList();
    }

    @Override
    public void noStreamExistsToPlay(String streamId) {
        Log.w(getClass().getSimpleName(), "noStreamExistsToPlay");
        //  finish();
    }

    @Override
    public void onError(String description, String streamId) {
        Log.e(TAG, "onError: " + description);
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
        Is_networkchecked = true;
        finish();
    }

    @Override
    public void onTrackList(String[] tracks) {

    }

    @Override
    public void onBitrateMeasurement(String streamId, int targetBitrate, int videoBitrate, int audioBitrate) {
        Log.d(TAG, "onBitrateMeasurement: " + streamId + ", " + targetBitrate + ", " + videoBitrate + ", " + audioBitrate);
    }

    @Override
    public void onStreamInfoList(String streamId, ArrayList<StreamInfo> streamInfoList) {

    }

    /* @SuppressLint("StaticFieldLeak")
     private class PublishStreamTask extends AsyncTask<String, String, Boolean> {
         @Override
         protected void onPreExecute() {
             setLoadingLay(VISIBLE);
         }

         @Override
         protected Boolean doInBackground(String... url) {
             Log.i(TAG, "doInBackground: " + url[0]);
             return mLiveVideoBroadcaster.startBroadcasting(url[0]);
         }

         @Override
         protected void onPostExecute(Boolean result) {
             Log.i(TAG, "onPostExecute: " + result);
             mIsRecording = result;
             if (result) {
                 isPublished = true; // Changed - Once stream live after that back button pressed
                 serviceHandler.removeCallbacks(bindServiceRunnable);
                 setLoadingLay(GONE);
                 resultCode = RESULT_OK;
               //  initStream();
             } else {
                 //makeToast(getString(R.string.something_wrong));
                 Toast.makeText(PublishActivity.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
                 triggerStopRecording();
             }
         }
     }
 */
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if(netWorktimer!=null){
            netWorktimer.cancel();
            netWorktimer=null;
        }
    }

    private void startAudioManager() {
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(
                    AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
            }
        });
    }

    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    public void triggerStopRecording() {
        stopAudioManager();
        stopTimer();
        if (webRTCClient != null) {
            webRTCClient.stopStream();
        }
        stopStream();
    }

    public void stopAudioManager() {
        if (audioManager != null) {
            /*Reset audio manager to previous state*/
            audioManager.stop();
            audioManager = null;
        }
    }

    private void setLoadingLay(final int visiblity) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (visiblity == VISIBLE) {
                    avBallIndicator.show();
                } else {
                    avBallIndicator.hide();
                }
                loadingLay.setVisibility(visiblity);
                initializeLay.setVisibility(visiblity);
            }
        });
    }

    private void setPublishLayVisible(final int visiblity) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (visiblity == VISIBLE) {
                    publishLay.setVisibility(visiblity);
                    bottomLay.setVisibility(GONE);
                } else {
                    publishLay.setVisibility(visiblity);
                    bottomLay.setVisibility(VISIBLE);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //this lets activity bind
        //  bindService(mLiveVideoBroadcasterServiceIntent, mConnection, 0);
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    public static String convertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);

    }

    private void initStream() {
        startStream();
        publishStream();
        startTime = 0;
        timer = new Timer();
        //  timer.scheduleAtFixedRate(timerTask, 0, SECONDS_INTERVAL);
        /*for (ProfileResponse profileResponse : selectedMutual) {
            sendChat(inviteSocketLink, Constants.TAG_LIVE, profileResponse);
        }*/
    }

    @Override
    public void onBackPressed() {
        if (isPublished) {
            closeConfirmDialog(getString(R.string.stop_publish_description));
        } else {
            finish();
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    /**
     * Android Activity lifecycle methods
     */
    @Override
    protected void onResume() {
        super.onResume();
        // If running on Android 6 (Marshmallow) or above, check to see if the necessary permissions
        // have been granted

        if (socketIO != null) {
            socketIO.setSocketEvents(PublishActivity.this);
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.checkSelfPermission(context,
                CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{CAMERA}, Constants.CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (is_streamBlocked){
            App.makeToast(getString(R.string.your_broadcast_deactivated_by_admin));
        }
        if (Is_networkchecked){
          //  App.makeToast(getString(R.string.network_error));
            finish();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (socketIO != null) {
            socketIO.setSocketEvents(null);
        }
        triggerStopRecording();
//        mLiveVideoBroadcaster.pause();
//        unbindService(mConnection);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //   mLiveVideoBroadcaster.setDisplayOrientation();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        handler.removeCallbacks(imageUpload);
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT, onConnect);
//            mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
            mSocket.off(StreamConstants.TAG_MESSAGE_RECEIVED, _msgReceived);
            mSocket.off(StreamConstants.TAG_SUBSCRIBER_LEFT, _subscriberLeft);
            mSocket.off(StreamConstants.TAG_STREAM_INFO, _streamInfo);
            mSocket.off(StreamConstants.TAG_STREAM_BLOCKED, _streamBlocked);
        }
        Constants.isInStream = false;
        // App.makeToast(getString(R.string.your_stream_end_description));
    }

    private boolean getIsRecording() {
        return mIsRecording || loadingLay.getVisibility() == VISIBLE;
    }

    /**
     * Click handler for the switch camera button
     */
    /*public void onSwitchCamera() {
        if (mLiveVideoBroadcaster != null) {
            mLiveVideoBroadcaster.changeCamera();
        }
    }*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mAutoFocusDetector != null)
            mAutoFocusDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    public void startStream() {
        if (NetworkReceiver.isConnected()) {

            Map<String, String> map = new HashMap<>();
            map.put(StreamConstants.TAG_PUBLISHER_ID, GetSet.getUserId());
            map.put(StreamConstants.TAG_TITLE, title);
            map.put(StreamConstants.TAG_NAME, streamName);
            map.put(StreamConstants.TAG_RECORDING, "" + true);

            Log.i(TAG, "startStream: " + new Gson().toJson(map));
            Call<StartStreamResponse> call3 = apiInterface.startStream(GetSet.getToken(), map);
            call3.enqueue(new Callback<StartStreamResponse>() {
                @Override
                public void onResponse(Call<StartStreamResponse> call, Response<StartStreamResponse> response) {
                    if (response.isSuccessful()) {


                        StartStreamResponse streamResponse = response.body();
                        if (streamResponse.getStatus().equals(Constants.TAG_TRUE)) {
                            hideProgress();
                            handler.post(imageUpload);
                        } else {
                            Toast.makeText(PublishActivity.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
                            makeToast(getString(R.string.something_went_wrong));
                            triggerStopRecording();
                        }
                    }
                }

                @Override
                public void onFailure(Call<StartStreamResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage());
                    //makeToast(getString(R.string.something_wrong));
                    Toast.makeText(PublishActivity.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
                    call.cancel();
                    triggerStopRecording();
                }
            });
        }
    }

    public void stopStream() {
        if (NetworkReceiver.isConnected() && streamName != null) {
            Is_networkchecked = false;
            if (!isBroadCastStopped) {
                progress_bar.setVisibility(VISIBLE);
                Is_networkchecked = false;
                isBroadCastStopped = true;
                btnStopBroadCast.setOnClickListener(null);
                Map<String, String> map = new HashMap<>();
                map.put(StreamConstants.TAG_PUBLISHER_ID, GetSet.getUserId());
                map.put(Constants.TAG_NAME, streamName);

                Log.i(TAG, "stopStreamapion Params: " + map);

                Call<Map<String, String>> call3 = apiInterface.stopStream(GetSet.getToken(), map);
                call3.enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        Log.i(TAG, "stopStreamapionResponse: " + response);
                        hideProgress();
                        Is_networkchecked = false;
                        closeActivity();
                    }

                    @Override
                    public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                        Log.i(TAG, "stopStreamapionResponse: " + t.getMessage());
                        progress_bar.setVisibility(GONE);
                        setPublishLayVisible(VISIBLE);
                    }
                });
            }
        } else {
            setPublishLayVisible(VISIBLE);
        }
    }

    private void closeActivity() {
        setResult(resultCode);
        finish();
    }

    public void takeScreenShot() {
        mGLView.queueEvent(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {

                EGL10 egl = (EGL10) EGLContext.getEGL();
                GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();



                /*EGL10 egl = (EGL10) EGLContext.getEGL();
                GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
                Bitmap bitmap = createBitmapFromGLSurface(0, 0, mGLView.getWidth(), mGLView.getHeight(), gl);
                File file = StorageManager.getInstance(PublishActivity.this).
                        saveToCacheDir(bitmap, "Temp_".concat("" + System.currentTimeMillis()).concat(".jpg"));
                if (file != null) {
                    byte[] bytes = new byte[0];
                    try {
                        bytes = FileUtils.readFileToByteArray(file);
                        uploadImage(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }*/


                final Bitmap bitmap = Bitmap.createBitmap(mGLView.getWidth(), mGLView.getHeight(),
                        Bitmap.Config.ARGB_8888);

                final HandlerThread handlerThread = new HandlerThread("PixelCopier");
                handlerThread.start();

                PixelCopy.request(mGLView, bitmap, (copyResult) -> {
                    if (copyResult == PixelCopy.SUCCESS) {
                        Log.e(TAG, bitmap.toString());

                        saveToCacheDir(bitmap, "Temp_".concat("" + System.currentTimeMillis()).concat(".jpg"));
                    } else {
                        Toast toast = Toast.makeText(PublishActivity.this,
                                "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                        toast.show();
                    }
                    handlerThread.quitSafely();
                }, new Handler(handlerThread.getLooper()));

                Log.i(TAG, "takeScreenshotrun: " + gl);


                saveToCacheDir(bitmap, "Temp_".concat("" + System.currentTimeMillis()).concat(".jpg"));
                if (bitmap != null) {
                    uploadImage(bitmap);
                }
            }
        });
    }


    public File saveToCacheDir(Bitmap bitmap, String filename) {
        boolean stored = false;
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
            Log.i(TAG, "saveToCacheDir: " + mDataDir);
        } else {
            mDataDir = context.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
            Log.i(TAG, "saveToCacheDire: " + mDataDir);
        }

        File file = new File(mDataDir.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;
        Log.i(TAG, "saveToCacheDirec: " + file);

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stored ? file : null;
    }

    // from other answer in this question
    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl) {

        int[] bitmapBuffer = new int[w * h];
        int[] bitmapSource = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            Log.e(TAG, "createBitmapFromGLSurface: " + e.getMessage(), e);
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

    public void uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] imageBytes = stream.toByteArray();

        RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData(StreamConstants.TAG_STREAM_IMAGE, streamName + ".jpeg", requestFile);
        RequestBody publisherId = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
        RequestBody videoId = RequestBody.create(getVideoId, MediaType.parse("multipart/form-data"));
        RequestBody streamNameBody = RequestBody.create(streamName, MediaType.parse("multipart/form-data"));
        Log.i(TAG, "uploadImages: " + bitmap);
        Call<Map<String, String>> call3 = apiInterface.uploadStreamImage(GetSet.getToken(), body, publisherId, videoId, streamNameBody);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                Log.i(TAG, "uploadImagesonResponse: " + response);
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                call.cancel();
            }
        });
    }

/*    public void uploadImage(byte[] imageBytes) {
        RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData(StreamConstants.TAG_STREAM_IMAGE, streamName + ".jpeg", requestFile);
        RequestBody publisherId = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
        RequestBody streamNameBody = RequestBody.create(streamName, MediaType.parse("multipart/form-data"));

        Call<Map<String, String>> call3 = apiInterface.uploadStreamImage(GetSet.getToken(), body, publisherId, streamNameBody);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Log.i(TAG, "uploadImageRes: " + response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                Log.e(TAG, "uploadImage: " + t.getMessage());
            }
        });
    }*/

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnStopBroadCast) {
            closeConfirmDialog(getString(R.string.stop_publish_description));
        } else if (id == R.id.btnDetail) {
            if (bottomDialog != null && !bottomDialog.isShowing())
                bottomDialog.show();
        } else if (id == R.id.viewLay) {
            if (bottomCommentsDialog != null && !bottomCommentsDialog.isShowing())
                bottomCommentsDialog.show();
        } else if (id == R.id.btnCancelTips) {
            tipsLay.setVisibility(GONE);
            tipsLay.startAnimation(fadeOut);
        }
        else if (id == R.id.btnGoLive) {
            if (com.app.utils.Constants.Login_method.equals("skip")) {
                GetSet.reset();
                SharedPref.clearAll();
                DBHelper.getInstance(this).clearDB();
                Intent logout = new Intent(this, LoginActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(logout);

            } else {
                if (TextUtils.isEmpty(edtTitle.getText().toString().trim())) {
                    tipsLay.setVisibility(VISIBLE);
                    tipsLay.startAnimation(shake);
                } /*else if (!checkStreamPermissions()) {
                requestStreamPermissions();
            } */ else if (!NetworkReceiver.isConnected()) {
                    makeToast(context.getString(R.string.no_internet_connection));
                } else {
                    hideSoftKeyboard(this);
                    pauseExternalAudio(this);
                    btnGoLive.setEnabled(false);
                    unBindCamera();
                    openBottomCameraDialog();
                }
            }
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

    public void openBottomCameraDialog() {
        if (bottomCameraDialog == null) {
            View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_camera, null);
            bottomCameraDialog = new BottomSheetDialog(this, R.style.Bottom_WhiteDialog); // Style here
            bottomCameraDialog.setContentView(bottomSheet);
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            frontCameraLay = bottomSheet.findViewById(R.id.frontCameraLay);
            backCameraLay = bottomSheet.findViewById(R.id.backCameraLay);
            txtTitle = bottomSheet.findViewById(R.id.txtTitle);
            textfront = bottomSheet.findViewById(R.id.textfront);
            textback = bottomSheet.findViewById(R.id.textback);


            frontCameraLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    unBindCamera();
                    frontCameraLay.setEnabled(false);
                    pauseExternalAudio(PublishActivity.this);
                    cameraView.setVisibility(View.GONE);
                    getStreamName(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }
            });

            backCameraLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    unBindCamera();
                    backCameraLay.setEnabled(false);
                    cameraView.setVisibility(View.GONE);
                    pauseExternalAudio(PublishActivity.this);
                    getStreamName(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
            });

            bottomCameraDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                    View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                    BottomSheetBehavior.from(bottomSheet).setHideable(true);
                    frontCameraLay.setEnabled(true);
                    backCameraLay.setEnabled(true);
                    btnGoLive.setEnabled(false);
                }
            });

            bottomCameraDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                    View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                    BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                    BottomSheetBehavior.from(bottomSheet).setHideable(true);
                    frontCameraLay.setEnabled(true);
                    backCameraLay.setEnabled(true);
                    btnGoLive.setEnabled(true);
                    bindCameraUseCases();
                }
            });
        }

        if (!bottomCameraDialog.isShowing()) {
            bottomCameraDialog.show();
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

    private void getStreamName(final int mLensFacing) {
        if (NetworkReceiver.isConnected()) {
            btnGoLive.setEnabled(false);
            showProgress();
            Call<HashMap<String, String>> call = apiInterface.getStreamName(GetSet.getToken(), GetSet.getUserId());
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        isPublished = true;
                        HashMap<String, String> map = response.body();
                        Log.i(TAG, "getStreamNameRes: " + map);
                        if (map.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            title = edtTitle.getText().toString().trim();
                            getVideoId = map.get("video_id");
                            streamName = map.get(StreamConstants.TAG_STREAM_NAME);
                            streamToken = map.get(StreamConstants.TAG_STREAM_TOKEN);
                            setPublishLayVisible(GONE);
                            setLoadingLay(VISIBLE);
                            /*if (lensFacing != mLensFacing) {
                                  onSwitchCamera();
                            }*/
                            lensFacing = mLensFacing;
                            init_WebRtc();
                            bindCameraUseCases();
                            edtTitle.getText().clear();
                            btnGoLive.setEnabled(true);
                            bottomCameraDialog.dismiss();
                            if (bottomStreamTitle != null)
                                bottomStreamTitle.setText(title); // Added QA Review
                        }
                    } else {
                        btnGoLive.setEnabled(true);
                        //    initCameraView();
                    }
                    hideProgress();
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    t.printStackTrace();
                    hideProgress();
                    btnGoLive.setEnabled(true);
                    //  initCameraView();
                }
            });
        }
    }


    public void unBindCamera() {
        ProcessCameraProvider cameraProvider = null;
        try {
            cameraProvider = cameraProviderFuture.get();// Must unbind the use-cases before rebinding them
            cameraProvider.unbindAll();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int aspectRatio(int width, int height) {
        int previewRatio = Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - AspectRatio.RATIO_4_3) <= Math.abs(previewRatio - AspectRatio.RATIO_16_9)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    private void init_WebRtc(){
        Intent intent = getIntent();
        intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, true);
        intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, 30);
        intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, 1500);
        intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, true);
        intent.putExtra(CallActivity.EXTRA_DATA_CHANNEL_ENABLED, enableDataChannel);
        Log.d(TAG, "init_WebRtc: ");
        webRTCClient = new WebRTCClient(this, this);
        webRTCClient.setOpenFrontCamera(lensFacing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        webRTCClient.setVideoRenderers(pipViewRenderer, cameraViewRenderer);
        Log.d(TAG, "init_WebRtcameraViewRendererc: "+cameraViewRenderer);
        if (cameraViewRenderer != null && captureFrameListener != null) {
            cameraViewRenderer.addFrameListener(captureFrameListener, 1);
        }
        String SERVER_URL = "wss://mediaserver.hitasoft.in/WebRTCAppEE/websocket";

     //   String SERVER_URL = "ws://3.238.159.240/WebRTCAppEE/websocket"; //rsolive

        //  String SERVER_URL = SharedPref.getString(SharedPref.STREAM_BASE_URL, Constants.RTMP_URL);
        // intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, 24);
        setLoadingLay(GONE);
        webRTCClient.init(SERVER_URL, streamName, webRTCMode, "tokenId", intent);
        Log.i(TAG, "productinitStream: " + this.streamName + SERVER_URL + this.webRTCMode);

        Log.d( TAG, "initwebrtcstreamName: "+streamName );
        Log.d( TAG, "initwebrtcwebRTCMode: "+webRTCMode );
        Log.d( TAG, "initWebRTCClient: " + SERVER_URL );
        Log.d( TAG, "initWebRTCClientintent: " + intent);
        webRTCClient.setDataChannelObserver(this);
        initVideoFrameListener();
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                attempt2Reconnect();
            }
        }, 500);
    }

    private void initVideoFrameListener() {

    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(context, CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStreamPermissions() {
        return ContextCompat.checkSelfPermission(context, CAMERA)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStreamPermissions() {
        if (ActivityCompat.checkSelfPermission(context,
                CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context,
                        RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context,
                        WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, Constants.STREAM_REQUEST_CODE);
        }
    }

    public boolean isPermissionsGranted(String[] permissions) {
        boolean isGranted = false;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            } else {
                isGranted = true;
            }
        }
        return isGranted;
    }

    public void showProgress() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    public void hideProgress() {
        /*Enable touch options*/
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v1) {
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

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        unBindCamera();
        Log.i(TAG, "onDoubleTapEvent: " + motionEvent.getAction());
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//            onSwitchCamera();
            Log.e("onTouchEvent","-"+motionEvent.getAction());
            webRTCClient.onCameraSwitch();
            bindCameraUseCases();
            if(lensFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraViewRenderer.setMirror(false);  //this line to mirror for frontcamera
                lensFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            else {
                lensFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
                cameraViewRenderer.setMirror(lensFacing == Camera.CameraInfo.CAMERA_FACING_FRONT);
            }

        }
        return false;
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

    private void slideDownAnim() {
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                0,
                bottomLay.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
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
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_join, parent, false);
                return new JoiningViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_TEXT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
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

                joinViewHolder.txtJoined.setText("@" + map.get(Constants.TAG_USER_NAME) +getString(R.string.stream_joined));
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
            }
        }

        public class JoiningViewHolder extends RecyclerView.ViewHolder {
            TextView txtJoined;
            RelativeLayout itemLay;

            public JoiningViewHolder(View view) {
                super(view);
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
                txtUserName = view.findViewById(R.id.txtUserName);
                txtMessage = view.findViewById(R.id.txtMessage);
                itemLay = view.findViewById(R.id.itemLay);
            }
        }
    }

    private void setFadeAnimation(final View view, final int position) {
        /*sets animation from complete opaque state(1.0f) to complete transparent state(0.0f)*/
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(FADE_DURATION);
        view.startAnimation(anim);
    }

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
                    .inflate(R.layout.item_viewers, parent, false);

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
            holder.itemLay.setOnClickListener(new View.OnClickListener() {
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
                userImage = view.findViewById(R.id.userImage);
                colorImage = view.findViewById(R.id.colorImage);
                itemLay = view.findViewById(R.id.itemLay);
            }
        }
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
                    .inflate(R.layout.item_viewer, parent, false);
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
            holder.mainLay.setOnClickListener(new View.OnClickListener() {
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
                mainLay = view.findViewById(R.id.mainLay);
                userImage = view.findViewById(R.id.userImage);
                txtUserName = view.findViewById(R.id.txtUserName);
                colorImage = view.findViewById(R.id.colorImage);
            }
        }
    }

    private void closeConfirmDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PublishActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.oaky_txt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //   handler.removeCallbacks(imageUpload);

                Toast.makeText(context, "setPositiveButton", Toast.LENGTH_SHORT).show();
//                App.makeToast(getString(R.string.your_stream_end_description));
//                PublishActivity.this.triggerStopRecording();
//                finish();
//                dialog.cancel();
            }
        });
        builder.setNegativeButton(getString(R.string.no_txt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_regular);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTextColor(R.color.colorBlack);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTextColor(R.color.colorBlack);
        btn2.setTypeface(typeface);

    }

    //
// Callback invoked in response to a call to ActivityCompat.requestPermissions() to interpret
// the results of the permissions request
//
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionsGranted = true;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (hasPermissions(this, mRequiredPermissions)) {

                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    Manifest.permission.MODIFY_AUDIO_SETTINGS) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    Manifest.permission.RECORD_AUDIO)) {

                    } else {
                        new AlertDialog.Builder(PublishActivity.this)
                                .setTitle(R.string.permission)
                                .setMessage(getString(R.string.app_doesnot_work_without_permissions))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        try {
                                            //Open the specific App Info page:
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                            startActivity(intent);

                                        } catch (ActivityNotFoundException e) {
                                            //e.printStackTrace();

                                            //Open the generic Apps page:
                                            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                            startActivity(intent);

                                        }
                                    }
                                })
                                .show();
                    }
                }
            }
        }
    }


    //Antmedia
    private void attempt2Reconnect() {
        Log.w(getClass().getSimpleName(), "Attempt2Reconnect called");
        if (!webRTCClient.isStreaming()) {
            webRTCClient.startStream();
            if (webRTCMode.equals(IWebRTCClient.MODE_JOIN)) {
                pipViewRenderer.setZOrderOnTop(true);
            }
        }
    }

    //    Utility method to check the status of a permissions request for an array of permission identifiers
    private static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions)
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }


}