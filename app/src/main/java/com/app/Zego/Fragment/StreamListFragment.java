package com.app.Zego.Fragment;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;


import static com.app.alhawanajd.App.pauseExternalAudio;
import static com.app.alhawanajd.App.preventMultipleClick;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.Zego.ApiBase.ZegoApiClient;
import com.app.Zego.activity.StartLiveActivity;
import com.app.Zego.internal.ZEGOLiveStreamingManager;
import com.app.Zego.internal.sdk.ZEGOSDKManager;
import com.app.Zego.internal.sdk.basic.ZEGOSDKCallBack;
import com.app.Zego.pkbattle.activity.SwipeLiveActivity;
import com.app.addon.livestream.PlayerActivity;
import com.app.addon.livestream.external.avloading.AVLoadingIndicatorView2;
import com.app.addon.livestream.external.helper.LocaleManager;
import com.app.addon.livestream.external.helper.NetworkReceiver;
import com.app.addon.livestream.external.helper.SocketIO;
import com.app.addon.livestream.external.model.LiveStreamRequest;
import com.app.addon.livestream.external.model.LiveStreamResponse;

import com.app.addon.livestream.external.model.StreamDetails;
import com.app.addon.livestream.external.utils.ApiInterface;
import com.app.addon.livestream.external.utils.Constants;
import com.app.addon.livestream.external.utils.StreamConstants;
import com.app.addon.livestream.liveVideoPlayer.UserVideoActivity;
import com.app.alhawanajd.App;
import com.app.alhawanajd.BuildConfig;
import com.app.alhawanajd.MainActivity;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.helper.Okay_CancelCallback;
import com.app.helper.PermissionsUtils;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.utils.AdminData;
import com.app.utils.SharedPref;
import com.app.utils.Util;
import com.app.utils.api.CommonApi;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.cashgate.chobi.Zego.callbacks.FragmentReloadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreamListFragment extends Fragment implements View.OnClickListener, SocketIO.SocketEvents, FragmentReloadCallback {
    Handler handler = new Handler();
    Runnable runnable;

    private Socket mSocket;
    int delay = 5000;

    @Override
    public void onPause() {
        super.onPause();
        // This is where you can perform actions when the fragment is paused.
        // For example, stop animations or release resources.
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(true);
        adapter.showLoading(false);
        streamLists.clear();
        adapter.notifyDataSetChanged();
        for (Call<LiveStreamResponse> liveStreamResponseCall : liveStreamApiCall) {
            liveStreamResponseCall.cancel();
        }
        liveStreamApiCall = new ArrayList<>();
        previousTotal = 0;
        currentPage = 0;
        totalItemCount = 0;
        visibleItemCount = 0;
        firstVisibleItem = 0;
        getLiveStreams(currentPage);


        CommonApi.getAppDefaultData(getContext());
    }

    boolean isScrolled = false;
    private static final String TAG = "StreamListFragment";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton btnAddStream;
    private LinearLayout nullLay;
    private AppCompatImageView nullImage;
    private TextView nullText;
    public static final String[] MANDATORY_PERMISSIONSFORANDROID12 = {MODIFY_AUDIO_SETTINGS,
            RECORD_AUDIO, INTERNET, CAMERA, WAKE_LOCK, BLUETOOTH_CONNECT, READ_PHONE_STATE};
    int currentPage = 0;
    private List<StreamDetails> streamLists = new ArrayList<>();
    private int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 10;
    private GridLayoutManager mLayoutManager;
    private ApiInterface apiInterface;
    ProfileResponse profileResponse;
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private boolean mPermissionsGranted = false;
    private final String[] mRequiredPermissions = new String[]{
            CAMERA,
            RECORD_AUDIO,
            WRITE_EXTERNAL_STORAGE
    };

    private boolean isLoading = true;
    private String from;
    private Long selectedPosition;
    private List<Call<LiveStreamResponse>> liveStreamApiCall = new ArrayList<>();
    private Context context;
    private ActivityResultLauncher<String[]> gallerypermissionResult;
    private LiveStreamAdapter adapter;
    private ActivityResultLauncher<String[]> phonepermissionresult;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    String getstreamUrl;
    TextView title;
    ImageView btnBack, btnSettings;

    private SocketIO socketIO;


    private void publishStream() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("text", "hello");
            Log.e(TAG, "publishStream: ::::::::" + mSocket.connected());
            if (mSocket.connected()) {
                mSocket.emit("_cluster", jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private final Emitter.Listener clusterListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "" + args[0].toString(), Toast.LENGTH_SHORT).show();
                /*  new AlertDialog.Builder(getActivity()).setTitle("Cluster")
                          .setMessage(args[0].toString()+"")
                          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which) {
                                  dialog.dismiss();
                              }
                          })
                          .create().show();*/
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_stream_list, container, false);
        //for arabic kindly check LocaleManger "getLanguage" because it is not used anywhere
        LocaleManager.updateResources(getContext(), LocaleManager.getLanguageCode(getContext()));

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

        phonepermissionresult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
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
                    int i = 0;
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        i++;
                        Log.d(TAG, "onActivityResult: " + i);
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), x.getKey())) {
                                phonepermissionresult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                openPermissionDialog(context, new Okay_CancelCallback() {
                                    @Override
                                    public void onOkayClicked(Object o) {
                                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(i);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, context.getString(R.string.storage_error));
                            }
                            break;
                        }
                    }
                }

            }
        });

        context = getContext();
        apiInterface = ZegoApiClient.INSTANCE.getClient(requireActivity()).create(ApiInterface.class);

        findViews(v);
        getFromIntent();
        initView();

        String userId = GetSet.getUserId();
        if (userId != null && !userId.isEmpty()) {
            signInZEGOSDK(GetSet.getUserId(), GetSet.getUserName(), (err, msg) -> {
                Log.e(TAG, "onClick: ::::::StreamList ZEGo login :::" + err + "::::" + msg);
            });
        }else{
            signInZEGOSDK(GetSet.getUserId(), GetSet.getUserName(), (err, msg) -> {
                Log.e(TAG, "onClick: ::::::StreamList ZEGo login :::" + err + "::::" + msg);
            });
        }

        CommonApi.getAppDefaultData(getContext());

        return v;

    }

    public static void openPermissionDialog(Context mContext, Okay_CancelCallback callback, String description) {
        new AlertDialog.Builder(mContext)
                .setMessage("Enable Phone Permission")
                .setPositiveButton(mContext.getString(R.string.oaky_txt), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onOkayClicked("");
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(mContext.getString(R.string.cancel_txt), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onCancelClicked("");
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void findViews(View v) {
        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);
        recyclerView = v.findViewById(R.id.recyclerView);
        btnAddStream = v.findViewById(R.id.btnAddStream);
        nullLay = v.findViewById(R.id.nullLay);
        nullImage = v.findViewById(R.id.nullImage);
        nullText = v.findViewById(R.id.nullText);

        btnBack = v.findViewById(R.id.btnBack);
        btnSettings = v.findViewById(R.id.btnSettings);
        title = v.findViewById(R.id.title);
        title.setVisibility(View.VISIBLE);

        btnBack.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.GONE);


// Get the existing layout params of the FAB
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) btnAddStream.getLayoutParams();

        layoutParams.setMargins(20, 0, 34, 20);

// Apply the updated layout params to the FAB
        btnAddStream.setLayoutParams(layoutParams);

        //  btnSettings.setImageResource(R.drawable.profile_home);
        if(GetSet.isLogged()) {
            initProfile();
        }
        //  text_test.setVisibility(View.GONE);

        btnBack.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnAddStream.setOnClickListener(this);
    }

    private void signInZEGOSDK(String userID, String userName, ZEGOSDKCallBack callback) {
        //Aug4 crash fixes
        if (!TextUtils.isEmpty(userID)) {
            ZEGOSDKManager.getInstance().connectUser(userID, userName, callback);
        }


    }


    private void initProfile() {
        if (GetSet.getUserId() != null&& !GetSet.getUserId().isEmpty()) {
            profileResponse = new ProfileResponse();
            profileResponse.setUserId(GetSet.getUserId());
            /*profileResponse.setLoginId(GetSet.getLoginId());*/
            profileResponse.setName(GetSet.getName());
            profileResponse.setUserName(GetSet.getUserName());
            profileResponse.setUserImage(GetSet.getUserImage());
            profileResponse.setDob(GetSet.getDob());
            profileResponse.setGender(GetSet.getGender());
            /*profileResponse.setAge(GetSet.getAge());*/
            profileResponse.setFollowings(Integer.valueOf(GetSet.getFollowingCount()));
            profileResponse.setFollowers(Integer.valueOf(GetSet.getFollowersCount()));
            /*profileResponse.setLocation(GetSet.getLocation());
            profileResponse.setLoginId(GetSet.getLoginId());*/
            profileResponse.setAvailableGems(Float.valueOf(String.valueOf(GetSet.getGems())));

            //   profileResponse.setAvailableGems(com.app.chobi.model.GetSet.getGems());
            profileResponse.setAvailableGifts(Integer.valueOf("" + GetSet.getGifts()));
            profileResponse.setVideosCount(Integer.valueOf("" + GetSet.getVideos()));
            /*profileResponse.setWatchedCount(GetSet.getVideosHistory());*/
            profileResponse.setPremiumMember(GetSet.getPremiumMember());
            profileResponse.setPremiumExpiryDate(GetSet.getPremiumExpiry());
            /*profileResponse.setPrivacyAge(GetSet.getPrivacyAge());*/
            /*profileResponse.setUserCanMessage(GetSet.geti());*/
            profileResponse.setBio(GetSet.getBio());
            profileResponse.setLikes(GetSet.getLikes());
            profileResponse.setAge(Integer.valueOf(GetSet.getAge()));
            setProfile(profileResponse);
        }
    }


    private void getFromIntent() {
       
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_USER_ID)) {
            GetSet.setUserId(getArguments().getString(StreamConstants.TAG_USER_ID));
        }
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_USER_NAME)) {
            GetSet.setUserName(getArguments().getString(StreamConstants.TAG_USER_NAME));
        }
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_TOKEN)) {
            GetSet.setToken(getArguments().getString(StreamConstants.TAG_TOKEN));
        }
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_USER_IMAGE)) {
            GetSet.setImageUrl(getArguments().getString(StreamConstants.TAG_USER_IMAGE));
        }
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_STREAM_BASE_URL)) {
            getstreamUrl = getArguments().getString(StreamConstants.TAG_STREAM_BASE_URL);
            GetSet.setStream_base_url(getstreamUrl);
        }


        Log.d(TAG, "getFromIntent: " + getstreamUrl);

    }

    private void initView() {
        nullImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_video));
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorPrimary), ContextCompat.getColor(getContext(), R.color.colorPrimary));

        adapter = new LiveStreamAdapter(context, streamLists);
        mLayoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isPositionFooter(position) ? mLayoutManager.getSpanCount() : 1;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            adapter.showLoading(false);
            streamLists.clear();
            adapter.notifyDataSetChanged();
            for (Call<LiveStreamResponse> liveStreamResponseCall : liveStreamApiCall) {
                liveStreamResponseCall.cancel();
            }
            liveStreamApiCall = new ArrayList<>();
            previousTotal = 0;
            currentPage = 0;
            totalItemCount = 0;
            visibleItemCount = 0;
            firstVisibleItem = 0;
            getLiveStreams(currentPage);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                // Check if the first visible item position is 0 (first item)
                if (firstVisibleItem == 0) {
                    isScrolled = false;
                    // The RecyclerView has been scrolled to the first item
                    // Add your logic here
                }
                if (dy > 0) {//check for scroll down
                    isScrolled = true;
                    if (isLoading) {
                        if (totalItemCount > previousTotal) {
                            isLoading = false;
                            previousTotal = totalItemCount;
                            currentPage++;
                        }
                    }

                    if (!isLoading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        isLoading = true;
                        Log.i(TAG, "onScrolled: " + currentPage);
                        getLiveStreams(currentPage);
                    }
                }
            }
        });

        /*To load first ten items*/
        swipeRefresh(true);


    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            previousTotal = 0;
            currentPage = 0;
            totalItemCount = 0;
            visibleItemCount = 0;
            firstVisibleItem = 0;
            getLiveStreams(currentPage);
        }
    }

    String streamName, streamType;

    public void getLiveStreams() {
        if (NetworkReceiver.isConnected()) {
            LiveStreamRequest request = new LiveStreamRequest();
            request.setUserId(SharedPref.getString(SharedPref.USER_ID, ""));
            request.setProfileId(SharedPref.getString(SharedPref.USER_ID, ""));
            request.setSortBy(StreamConstants.TAG_RECENT);
            request.setLimit("" + StreamConstants.MAX_LIMIT);
            request.setOffset("" + (StreamConstants.MAX_LIMIT * 0));
            request.setType(StreamConstants.TAG_LIVE);
            Log.e(TAG, "getLiveStreamsParams: " + new Gson().toJson(request));
            Call<LiveStreamResponse> call = apiInterface.getCurrentStreams1(GetSet.getToken(), request);
            liveStreamApiCall.add(call);
            call.enqueue(new Callback<LiveStreamResponse>() {
                @Override
                public void onResponse(Call<LiveStreamResponse> call, Response<LiveStreamResponse> response) {
                    Log.e(TAG, "getLiveStreamsRes: " + new Gson().toJson(response.body()));
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            if (response.body().getResult().size() > 0) {
//                                Log.e(TAG, response.body().getResult().get(0).getName());
                                if (streamName != null && !streamName.equals(response.body().getResult().get(0).getName())/*&&!streamType.equals(response.body().getResult().get(0).getType())*/) {
                                    swipeRefreshLayout.setRefreshing(true);
                                    adapter.showLoading(false);
                                    streamLists.clear();
                                    adapter.notifyDataSetChanged();
                                    for (Call<LiveStreamResponse> liveStreamResponseCall : liveStreamApiCall) {
                                        liveStreamResponseCall.cancel();
                                    }
                                    liveStreamApiCall = new ArrayList<>();
                                    previousTotal = 0;
                                    currentPage = 0;
                                    totalItemCount = 0;
                                    visibleItemCount = 0;
                                    firstVisibleItem = 0;
                                    getLiveStreams(currentPage);
                                }
                            }
                        }
                    }

                }

                @Override
                public void onFailure(Call<LiveStreamResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    public void getLiveStreams(int offset) {
        if (NetworkReceiver.isConnected()) {
            if (!isAdded() || getActivity() == null) return;
            nullLay.setVisibility(View.GONE);
            if (!swipeRefreshLayout.isRefreshing()) {
                adapter.showLoading(true);
            }

            LiveStreamRequest request = new LiveStreamRequest();
            request.setUserId(SharedPref.getString(SharedPref.USER_ID,""));
            request.setProfileId(SharedPref.getString(SharedPref.USER_ID,""));
            request.setSortBy(StreamConstants.TAG_RECENT);
            request.setLimit("" + StreamConstants.MAX_LIMIT);
            request.setOffset("" + (StreamConstants.MAX_LIMIT * offset));
            request.setType(StreamConstants.TAG_LIVE);
            if (getActivity() != null && isAdded()) {
                Log.e(TAG, "getLiveStreamsParams: " + new Gson().toJson(request));
                Call<LiveStreamResponse> call = apiInterface.getCurrentStreams1(GetSet.getToken(), request);
                liveStreamApiCall.add(call);
                call.enqueue(new Callback<LiveStreamResponse>() {
                    @Override
                    public void onResponse(Call<LiveStreamResponse> call, Response<LiveStreamResponse> response) {
                        Log.e(TAG, "getLiveStreamsRes: " + new Gson().toJson(response.body()));
                       // if (!isAdded() || getActivity() == null) return;
                        try{
                            if (response.isSuccessful()) {
                                if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                    nullLay.setVisibility(View.GONE);
//                            streamLists.clear();
                                    //july8 added for blcoked streams removed by list
                                    streamLists.addAll(response.body().getResult());
                                    streamName = response.body().getResult().get(0).getName();
                                  /*  List<StreamDetails> filteredList = new ArrayList<>();
                                    for (StreamDetails stream : response.body().getResult()) {
                                        if ("false".equals(stream.getStreamblock()) && "false".equals(stream.getStreamkickout())) {
                                            filteredList.add(stream);
                                        }
                                    }

                                    streamLists.addAll(filteredList);

                                    if (!filteredList.isEmpty()) {
                                        streamName = filteredList.get(0).getName();
                                    }*/
                                }else if (response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                                    App.makeToast(getString(R.string.Account_block));
                                    GetSet.reset();
                                    SharedPref.clearAll();
                                    DBHelper.getInstance(getContext()).clearDB();
                                    Intent logout = new Intent(getContext(), MainActivity.class);
                                    logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(logout);
                                }
                            }
                        }catch (NullPointerException e){
                            e.printStackTrace();
                            App.makeToast(getString(R.string.txt_null));
                        }

                        if (!isAdded()) return;

                        if (streamLists.size() == 0) {
                            nullImage.setImageResource(R.drawable.no_video);
                            nullText.setText(getString(R.string.no_videos_found));
                            nullLay.setVisibility(View.VISIBLE);
                        } else {
                            nullLay.setVisibility(View.GONE);
                        }

                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefresh(false);
                            isLoading = true;
                        }
                        adapter.showLoading(false);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<LiveStreamResponse> call, Throwable t) {
                        call.cancel();
                        Log.e(TAG, "getLiveStreams: " + t.getMessage());
                        if (currentPage != 0)
                            currentPage--;
                    }
                });
            }
        } else {
            if (getActivity() != null && isAdded()) {
                if (currentPage != 0)
                    currentPage--;
                if (!swipeRefreshLayout.isRefreshing()) {
                    adapter.showLoading(false);
                } else {
                    if (streamLists.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                        nullText.setText(getString(R.string.no_internet_connection));
                    }
                    swipeRefresh(false);
                }
            }

        }
    }
    private void setProfile(ProfileResponse profile) {
        Glide.with(this)
                .load(profile.getUserImage())
                .error(R.drawable.profile_home)
                .placeholder(R.drawable.profile_home)
                .into(btnSettings);
    }


    public void deleteWatchHistory() {

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnBack) {
            Activity activity = getActivity();
            if (activity instanceof SwipeLiveActivity
                    && ((SwipeLiveActivity) activity).closeRightSlider()) {
                return;
            }
            requireActivity().finish();
        }


        if (view.getId() == R.id.btnSettings) {
            Intent myVideos = new Intent(getContext(), UserVideoActivity.class);
            myVideos.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(myVideos);

        } else if (view.getId() == R.id.text_test) {
            Intent myVideos = new Intent(getContext(), UserVideoActivity.class);
            myVideos.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(myVideos);

        } else if (view.getId() == R.id.btnAddStream) {
//            publishStream();
            App.preventMultipleClick(view);
            //unSubscribeStream(GetSet.getUserId());
            getProfile(); //-- working on dev server
            /*Intent stream = new Intent(context, StartLiveActivity.class);
            stream.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(stream);*/

        }
    }





    public void getProfile() {
        if (NetworkReceiver.isConnected()) {
            if (GetSet.getUserId() != null) {
                ProfileRequest request = new ProfileRequest();
                request.setUserId(GetSet.getUserId());
                request.setProfileId(GetSet.getUserId());
                Call<ProfileResponse> call = apiInterface.getProfile(request);
                call.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        if (response.isSuccessful()&&response.body()!=null){
                            Log.e("Profile Response", new Gson().toJson(response.body()));
                            ProfileResponse profile = response.body();
                            try{
                                if (profile != null){
                                    if (profile.getStatus() != null)
                                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                                           /* if(profile.getAllow_livestream().equals(Constants.TAG_TRUE)){
                                                checkAllowLiveStream(profile);
                                            }else {
                                                if (profile.getFollowers() >= AdminData.LIVE_ALLOW_COUNT) {
                                                    checkAllowLiveStream(profile);
                                                } else {
                                                    Toast.makeText(context, getString(R.string.only_people_with_more_than_100_followers_can_post_live, "" + AdminData.LIVE_ALLOW_COUNT), Toast.LENGTH_SHORT).show();
//
                                                }
                                            }*/
                                            checkAllowLiveStream(profile);
                                        }

                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }

                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
                        t.printStackTrace();
                        Log.e(TAG, "onFailure: :::::::profile::::" + t.getMessage());
                    }
                });
            }
        }
    }

    private void checkAllowLiveStream(ProfileResponse profile) {
        Log.e(TAG, "checkAllowLiveStream: :::::::" + AdminData.is_livestream);
        if (AdminData.is_livestream.equalsIgnoreCase("1")) {

            if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(getContext(), CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA, RECORD_AUDIO}, 100);
                } else if (ContextCompat.checkSelfPermission(getActivity(), BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{BLUETOOTH_CONNECT}, 100);
                } else {
                    pauseExternalAudio(getContext());
                    Intent stream = new Intent(context, StartLiveActivity.class);
                    stream.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(stream);
                    //requireActivity().finishAffinity();
                }
            } else if (SDK_INT >= Build.VERSION_CODES.S) {
                if (isPermissionsGranted(MANDATORY_PERMISSIONSFORANDROID12)) {
                    Intent stream = new Intent(context, StartLiveActivity.class);
                    stream.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(stream);
                    //requireActivity().finishAffinity();
                } else {
                    Toast.makeText(getContext(), getString(R.string.read_phone_state_permission_forandroid12), Toast.LENGTH_LONG).show();
                    request(MANDATORY_PERMISSIONSFORANDROID12);
                }
            } else if (SDK_INT >= Build.VERSION_CODES.M) {
                mPermissionsGranted = hasPermissions(getActivity(), mRequiredPermissions);
                if (!mPermissionsGranted) {
                    ActivityCompat.requestPermissions(getActivity(), mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
                } else {
                    Intent stream = new Intent(context, StartLiveActivity.class);
                    stream.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(stream);
                    //requireActivity().finishAffinity();
                }
            } else {
                Intent stream = new Intent(context, StartLiveActivity.class);
                stream.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(stream);
                //requireActivity().finishAffinity();
            }
        } else {
            Toast.makeText(context, getString(R.string.live_stream_service_unavailable), Toast.LENGTH_SHORT).show();
        }
    }
    private void requestStoragePermissionss() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gallerypermissionResult.launch(PermissionsUtils.READ_STORAGE_camera_PERMISSION13);
        }
    }

    private static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions)
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }

    private void request(String[] permissions) {
        phonepermissionresult.launch(permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionsGranted = true;
        if (requestCode == 100) {
            if (checkPermissions(permissions, getActivity())) {
                //  ImagePicker.pickImage(this, getString(R.string.select_your_image));
            } else {
                if (shouldShowRationale(permissions, getActivity())) {
                    ActivityCompat.requestPermissions(getActivity(), permissions, 100);
                } else {
                    Toast.makeText(getContext(), getString(R.string.need_publish_permission_to_access), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 100);
                }
            }
        }
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (hasPermissions(getContext(), mRequiredPermissions)) {

                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.CAMERA) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                    Manifest.permission.MODIFY_AUDIO_SETTINGS) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                    Manifest.permission.RECORD_AUDIO)) {

                    } else {
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.permission)
                                .setMessage(getString(R.string.app_doesnot_work_without_permissions))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        try {
                                            //Open the specific App Info page:
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
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

    private boolean checkPermissions(String[] permissionList, Context activity) {
        boolean isPermissionsGranted = false;
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                isPermissionsGranted = true;
            } else {
                isPermissionsGranted = false;
                break;
            }
        }
        return isPermissionsGranted;
    }

    private boolean shouldShowRationale(String[] permissions, Activity activity) {
        boolean showRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showRationale = true;
            } else {
                showRationale = false;
                break;
            }
        }
        return showRationale;
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

    public class LiveStreamAdapter extends RecyclerView.Adapter {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        List<StreamDetails> streamLists;
        private boolean showLoading = false;
        Context context;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        public LiveStreamAdapter(Context context, List<StreamDetails> streamLists) {
            this.streamLists = streamLists;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_addon, parent, false);
                return new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livestreaming_progress, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            if (viewHolder instanceof MyViewHolder) {
                final StreamDetails stream = streamLists.get(position);
                MyViewHolder holder = (MyViewHolder) viewHolder;

                holder.txtTitle.setText(stream.getTitle());
                Glide.with(context).load(stream.getPublisherImage())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.alhawana))
                        .into(holder.imageThumbnail);

                if (stream.type.equals("live")) {
                    holder.txtDuration.setVisibility(View.GONE);
                    holder.txtLive.setVisibility(View.VISIBLE);
                    if (stream.mode.equalsIgnoreCase("private")){
                        float newSize = 10; // New text size in scaled pixels (sp)
                        holder.txtLive.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize);
                        holder.txtLive.setText(stream.streamType.equals("single")?"Pri"+" "+"Live":"Pri"+" "+stream.streamType);
                    }else{
                        holder.txtLive.setText(stream.streamType.equals("single")?"Live":stream.streamType);
                    }
                } else {
                    holder.txtDuration.setVisibility(View.VISIBLE);
                    holder.txtDuration.setText(stream.getPlayBackDuration());
                    holder.txtLive.setVisibility(View.GONE);
                }

//                holder.verified.setVisibility(stream.getProfile_verified().equals("1") ? View.VISIBLE : View.GONE);


                holder.txtUserName.setText(stream.getPostedBy());
                holder.txtViewCount.setText(Util.formatNumberToAbbreviation(Long.parseLong(stream.getWatchCount())));

                // String type = stream.getType();
                String type = stream.getStreamType();
                Log.e(TAG, "type : " + type);
                Log.e(TAG, "image : " + stream.publisherImage + "test");
               // String img = stream.publisherImage + "";

                holder.parentLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        preventMultipleClick(view);
                        Log.e(TAG, stream.type);
                        if (stream.type.equals(StreamConstants.TAG_LIVE)) {
                            pauseExternalAudio(context);
                            Log.e(TAG, "stream.publisherId :" + stream.publisherId);
                            Log.e(TAG, "stream.postedBy : " + stream.postedBy);
                            Log.e(TAG, "stream.name" + stream.getName());
                            Log.e(TAG, "ENTERS HERE1");
                            ZEGOLiveStreamingManager.getInstance().init();
                            signInZEGOSDK(GetSet.getUserId(), GetSet.getUserName(), (err, msg) -> {
                                Log.e(TAG, "onClick: ::::::StreamList ZEGo login :::" + err + "::::" + msg);
                            });

                        /*    if(stream.streamblock.equals("false")){
                                if(stream.streamkickout.equals("false")) {
                                    Intent intent = new Intent(requireContext(), SwipeLiveActivity.class);
                                    intent.putExtra("from", "viewer"); //from this page
                                    intent.putExtra("type", type);
                                    intent.putExtra("publisherName", stream.getPostedBy());
                                    intent.putExtra(StreamConstants.TAG_TITLE, stream.getTitle());
                                    intent.putExtra(StreamConstants.TAG_STREAM_NAME, stream.getName());
                                    intent.putExtra(StreamConstants.TAG_INVITE_LINK, stream.getInviteLink());
                                    intent.putExtra(StreamConstants.TAG_STREAM_TOKEN, "");
                                    intent.putExtra("hostId", stream.publisherId);
                                    intent.putExtra("hostName", stream.postedBy);
                                    intent.putExtra("hostImage", stream.publisherImage);
                                    intent.putExtra("host", false);
                                    intent.putExtra("liveID", stream.getName());
                                    intent.putExtra("follow_sts" , stream.getFollow());
                                    intent.putExtra("host_level" , stream.getUser_level());
                                    Log.d(TAG, "onClick_userLevel: "+stream.getUser_level());
                                    intent.putExtra("last_gifted_date" , stream.getLast_gifted_date());
                                    intent.putExtra("profile_verified", stream.getProfile_verified());
                                    intent.putExtra("main_stream_name", stream.getMainStreamName());
                                  //  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(intent);
                                    requireActivity().finishAffinity();

                                }else {
                                    Toast.makeText(context, getString(R.string.stream_kickOut_aleart), Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(context, getString(R.string.stream_block_alert), Toast.LENGTH_SHORT).show();
                            }*/

                            Intent intent = new Intent(requireContext(), SwipeLiveActivity.class);
                            //intent.putExtra("from", "viewer"); //from this page
                            //intent.putExtra("type", type);
                            //intent.putExtra("publisherName", stream.getPostedBy());
                            //intent.putExtra(StreamConstants.TAG_TITLE, stream.getTitle());
                            intent.putExtra(StreamConstants.TAG_STREAM_NAME, stream.getName());
                            //intent.putExtra(StreamConstants.TAG_INVITE_LINK, stream.getInviteLink());
                            //intent.putExtra(StreamConstants.TAG_STREAM_TOKEN, "");
                            intent.putExtra("hostId", stream.publisherId);
                            intent.putExtra("hostName", stream.postedBy);
                            intent.putExtra("hostImage", stream.publisherImage);
                          //  intent.putExtra("host", false);
                            intent.putExtra("liveID", stream.getName());
                           // intent.putExtra("follow_sts" , stream.getFollow());
                            //intent.putExtra("host_level" , stream.getUser_level());
                           // Log.d(TAG, "onClick_userLevel: "+stream.getUser_level());
                           // intent.putExtra("last_gifted_date" , stream.getLast_gifted_date());
                           // intent.putExtra("profile_verified", stream.getProfile_verified());
                           // intent.putExtra("main_stream_name", stream.getMainStreamName());
                            //  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                            requireActivity().finishAffinity();

                        } else {
                            pauseExternalAudio(context);
                            Intent intent = new Intent(context, PlayerActivity.class);
                            selectedPosition = (long) position;
                            intent.putExtra(StreamConstants.TAG_STREAM_DATA, stream);
                            intent.putExtra(Constants.TAG_FROM, StreamConstants.TAG_RECENT);
                            startActivityForResult(intent, StreamConstants.DELETE_REQUEST_CODE);
                        }
                    }
                });
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
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = streamLists.size();
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

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            AVLoadingIndicatorView2 progress;
            FrameLayout progressLay;

            public LoadingViewHolder(View view) {
                super(view);
                progress = view.findViewById(R.id.progressBarNew);
                progressLay = view.findViewById(R.id.progressLay);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageThumbnail, iconView, verified;
            TextView txtViewCount, txtDuration, txtLive, txtTitle, txtUserName;
            RelativeLayout statusLay, parentLay;
            LinearLayout layoutBackground;

            public MyViewHolder(View view) {
                super(view);
                imageThumbnail = view.findViewById(R.id.imageThumbnail);
                iconView = view.findViewById(R.id.iconView);
                verified = view.findViewById(R.id.verified);
                txtViewCount = view.findViewById(R.id.txtViewCount);
                txtDuration = view.findViewById(R.id.txtDuration);
                txtTitle = view.findViewById(R.id.txtTitle);
                txtUserName = view.findViewById(R.id.txtUserName);
                txtLive = view.findViewById(R.id.txtLive);
                parentLay = view.findViewById(R.id.parentLay);

                LinearLayout container = view.findViewById(R.id.container_streamDetails);
                assert container != null;

            }


        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StreamConstants.DELETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (selectedPosition != null) {
                streamLists.remove(selectedPosition.intValue());
                adapter.notifyItemRemoved(selectedPosition.intValue());
                adapter.notifyItemRangeChanged(selectedPosition.intValue(), (adapter.getItemCount() - selectedPosition.intValue()));
                selectedPosition = null;
            }
        }
    }

    @Override
    public void onDataReceived() {
        swipeRefreshLayout.setRefreshing(true);
        adapter.showLoading(false);
        streamLists.clear();
        adapter.notifyDataSetChanged();
        for (Call<LiveStreamResponse> liveStreamResponseCall : liveStreamApiCall) {
            liveStreamResponseCall.cancel();
        }
        liveStreamApiCall = new ArrayList<>();
        previousTotal = 0;
        currentPage = 0;
        totalItemCount = 0;
        visibleItemCount = 0;
        firstVisibleItem = 0;
        getLiveStreams(currentPage);
    }


}
