package com.app.alhawanajd;

import static android.os.Build.VERSION.SDK_INT;

import static com.app.alhawanajd.MainActivity.openPermissionsDialog;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.app.alhawanajd.BuildConfig;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.external.CustomSwipeRefresh;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.OkayCancelCallback;
import com.app.helper.PermissionsUtils;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.GetSet;
import com.app.model.HashTagRequest;
import com.app.model.HashTagResponse;
import com.app.model.MusicListResponse;
import com.app.model.VideoListResponse;
import com.app.model.event.VideoDeleteEvent;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class VideoListActivity extends BaseFragmentActivity implements AdapterItemClickListener, View.OnClickListener {
    private static final String TAG = VideoListActivity.class.getSimpleName();
    private Context mContext;

    private ImageView btnBack;
    private TextView txtTitle;
    private TextView txtSubTitle;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private CustomSwipeRefresh swipeRefreshLayout;
    private ConstraintLayout musicLay;
    private TextView txtUse;
    private LinearLayout useLay;
    private ImageView ivThumb;
    private LottieAnimationView lottieImage;
    private TextView txtMusicTitle;
    private TextView txtArthurName;
    private TextView txtDuration;
    private TextView txtLanguage;
    private ImageView btnBookMark;
    private ImageView btnPlay;
    private ProgressBar progressBar;
    private SeekBar audioSeekbar;
    private TextView txtPlayTime;
    private TextView txtTotalDuration;
    private ExtendedFloatingActionButton btnUse;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;

    private ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
    private VideosAdapter1 videosAdapter;
    private final List<VideoListResponse.Result> videoList = new ArrayList<>();
    private int limit = Constants.MAX_LIMIT, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    private String from, title, type, hashtag ,IsFavorite;
    private MusicListResponse.Result musicItem;
    private GridLayoutManager layoutManager;
    private ActivityResultLauncher<Intent> videoResultLauncher;
    private int resultCode = RESULT_CANCELED;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private int totalDuration = 0;
    private int lastProgress = 0;
    private boolean wasPlaying = false;
    public String[] permissionArray;
    private ActivityResultLauncher<String[]> galleryResultLauncher;
    private Handler mSeekbarUpdateHandler = new Handler(Looper.myLooper());
    private Runnable mUpdateSeekbar = new Runnable() {
        @Override
        public void run() {
            audioSeekbar.setProgress(mediaPlayer.getCurrentPosition());
            txtPlayTime.setText(App.milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
            mSeekbarUpdateHandler.postDelayed(this, 50);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        mContext = this;
        if (getIntent().hasExtra(Constants.TAG_TITLE))
            title = getIntent().getStringExtra(Constants.TAG_TITLE);
        if (getIntent().hasExtra(Constants.TAG_TYPE))
            type = getIntent().getStringExtra(Constants.TAG_TYPE);
        if (getIntent().hasExtra(Constants.TAG_FROM))
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        if (from != null && (from.equals(Constants.TAG_HASHTAGS) || from.equals(Constants.TAG_RELATED_HASHTAG))) {
            hashtag = getIntent().getStringExtra(Constants.TAG_TITLE);
            if (hashtag.startsWith("#")) {
                hashtag = hashtag.replace("#", "");
            }
        } else if (from != null && from.equals(Constants.TAG_MUSIC)) {
            musicItem = new Gson().fromJson(getIntent().getStringExtra(Constants.TAG_INTENT_DATA), MusicListResponse.Result.class);
        }
        findViews();
        initListener();
        initView();
        getVideosList();
        //   swipeRefreshLayout.setEnabled(false);
    }

    private void findViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        txtSubTitle = findViewById(R.id.txtSubTitle);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        musicLay = findViewById(R.id.musicLay);
        txtUse = findViewById(R.id.txtUse);
        useLay = findViewById(R.id.useLay);
        ivThumb = findViewById(R.id.ivThumb);
        lottieImage = findViewById(R.id.lottieImage);
        txtMusicTitle = findViewById(R.id.txtMusicTitle);
        txtArthurName = findViewById(R.id.txtArthurName);
        txtDuration = findViewById(R.id.txtDuration);
        txtLanguage = findViewById(R.id.txtLanguage);
        btnBookMark = findViewById(R.id.btnBookMark);
        btnPlay = findViewById(R.id.btnPlay);
        progressBar = findViewById(R.id.progressBar);
        audioSeekbar = findViewById(R.id.audioSeekbar);
        txtPlayTime = findViewById(R.id.txtPlayTime);
        txtTotalDuration = findViewById(R.id.txtTotalDuration);
        btnUse = findViewById(R.id.btnUse);
        nullLay = findViewById(R.id.nullLay);
        nullImage = findViewById(R.id.nullImage);
        nullText = findViewById(R.id.nullText);
    }

    private void initListener() {
        btnBack.setOnClickListener(this);
        useLay.setOnClickListener(this);
        txtUse.setOnClickListener(this);
        btnUse.setOnClickListener(this);
        btnBookMark.setOnClickListener(this);
    }

    public void initView() {
        txtSubTitle.setVisibility(View.GONE);
        btnUse.setVisibility(View.GONE);
        if (Constants.Login_method.equals("skip")){
            btnBookMark.setVisibility(View.GONE);
        }else {
            btnBookMark.setVisibility(View.VISIBLE);
        }
        if (title != null) {
            if (from != null && (from.equals(Constants.TAG_HASHTAGS) || from.equals(Constants.TAG_RELATED_HASHTAG))) {
                if (!title.startsWith("#")) {
                    txtTitle.setText("#" + title);
                } else {
                    txtTitle.setText(title);
                }
                btnUse.setText(mContext.getString(R.string.use_this_hash_tag));
                btnUse.setIcon(ContextCompat.getDrawable(mContext, R.drawable.hastage_use));
                btnUse.setVisibility(View.VISIBLE);
            } else {
                txtTitle.setText(title);
            }
        }
        if (from != null && from.equals(Constants.TAG_MUSIC)) {
            initMusicLay();
        } else {
            SlidrConfig config = new SlidrConfig.Builder()
                    .position(SlidrPosition.LEFT)
                    .sensitivity(1f)
                    .velocityThreshold(2400)
                    .distanceThreshold(0.25f)
                    .primaryColor(getResources().getColor(R.color.colorBlack))
                    .secondaryColor(getResources().getColor(R.color.colorBlack))
                    .build();
            Slidr.attach(this, config);
            musicLay.setVisibility(View.GONE);
        }
//        layoutManager = new NpaGridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getLayoutManager().setAutoMeasureEnabled(true);
        //recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);


        videosAdapter = new VideosAdapter1(VideoListActivity.this, videoList, this, "");
        recyclerView.setAdapter(videosAdapter);

        int spacingInDp = 6;
        int spacingInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                spacingInDp,
                mContext.getResources().getDisplayMetrics()
        );
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPx));


        videosAdapter.notifyDataSetChanged();
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return videosAdapter.isFooter(position) ? layoutManager.getSpanCount() : 1;
            }
        });

        float spacing = getResources().getDimension(R.dimen.activity_horizontal_margin);
        float smallMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                5,
                mContext.getResources().getDisplayMetrics()
        );
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefresh(true);
        });

        setScrollMethod();
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
        initActivityResultLauncher();
        swipeRefresh(true);

        galleryResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
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
                            if (ActivityCompat.shouldShowRequestPermissionRationale(VideoListActivity.this, x.getKey())) {
                                galleryResultLauncher.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                openPermissionsDialog(VideoListActivity.this, new OkayCancelCallback() {
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
                                }, VideoListActivity.this.getString(R.string.need_video_permission_to_access));
                            }
                            break;
                        }
                    }
                }

            }
        });






    }

    /*private void setScrollMethod2(){
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if(v.getChildAt(v.getChildCount() - 1) != null) {
                Log.e("checkScroll","-"+scrollY);
                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY && !isLoading) {
                    //code to fetch more data for endless scrolling
                    videoList.add(null);
                    videosAdapter.notifyItemInserted(videoList.size() - 1);
                    offset++;
                    isScroll = true;
                    isLoading = true;
                    getVideosList();
                }
            }
        });
    }
*/
    private void setScrollMethod() {
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (dy > 0 && !isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    videoList.add(null);
                    videosAdapter.notifyItemInserted(videoList.size() - 1);
                    offset++;
                    isScroll = true;
                    isLoading = true;
                    getVideosList();

                }
            }
        });
    }

    private void initMusicLay() {
        Timber.d("initMusicLay: %s, %s", musicItem.getSoundId(), musicItem.getSoundUrl());
        audioSeekbar.setProgress(0);
        musicLay.setVisibility(View.VISIBLE);
        String soundName;
        if (!TextUtils.isEmpty(musicItem.getSoundBy())) {
            soundName = getResources().getString(R.string.original_audio_by) + " " +
                    musicItem.getSoundBy();
        } else {
            soundName = musicItem.getTitle();
        }
        txtMusicTitle.setText(soundName);
        btnBookMark.setSelected(musicItem.getIsFavorite());

        txtPlayTime.setText(App.milliSecondsToTimer(0));
        txtPlayTime.setVisibility(View.VISIBLE);
        txtTotalDuration.setVisibility(View.VISIBLE);
        txtTotalDuration.setText(musicItem.getDuration());
//        txtPlayTime.setVisibility(View.INVISIBLE);
//        txtTotalDuration.setVisibility(View.INVISIBLE);
        audioSeekbar.setMax(0);
        Glide.with(mContext)
                .load(musicItem.getCoverImage() == null ? R.drawable.novideo_placeholder : musicItem.getCoverImage())
                .centerCrop()
                .placeholder(R.drawable.novideo_placeholder)
                .error(R.drawable.novideo_placeholder)
                .into(ivThumb);

        initAudioPlayer();
    }

    private void initAudioPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        audioSeekbar.setProgress(0);
        btnPlay.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(this, Uri.parse(musicItem.getSoundUrl()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
//        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                wasPlaying = true;
                btnPlay.setSelected(true);
                btnPlay.setVisibility(View.VISIBLE);
                /*txtTotalDuration.setText(App.milliSecondsToTimer(mediaPlayer.getDuration()));
                txtTotalDuration.setVisibility(View.VISIBLE);
                txtPlayTime.setVisibility(View.VISIBLE);*/
                progressBar.setVisibility(View.GONE);
                totalDuration = mediaPlayer.getDuration();
                audioSeekbar.setMax(totalDuration);
                if (App.getCurrentActivity() != null && App.getCurrentActivity() instanceof VideoListActivity) {
                    mediaPlayer.start();
                    mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
                } else {
                    clearMediaPlayer();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Timber.d("onCompletion");
                mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
                btnPlay.setSelected(false);
                audioSeekbar.setProgress(0);
                wasPlaying = true;
                txtPlayTime.setText(App.milliSecondsToTimer(0));
            }
        });
        setSeekBarListener();
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        pauseMediaPlayer();
                    } else {
                        if (mediaPlayer.getCurrentPosition() == audioSeekbar.getProgress()) {
                            mediaPlayer.start();
                        } else {
                            mediaPlayer.start();
                            mediaPlayer.seekTo(audioSeekbar.getProgress());
                        }
                        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
                        btnPlay.setSelected(true);
                    }
                } else {
                    initAudioPlayer();
                }
            }
        });
    }

    private void setSeekBarListener() {
        audioSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Timber.d("onProgressChanged: %d, %d", progress, mediaPlayer.getCurrentPosition());
                if (fromUser) {
//                    mediaPlayer.seekTo(seekBar.getProgress());
//                    audioSeekbar.setProgress(progress);
//                    mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
                } else {
//                    audioSeekbar.setProgress(Math.min(mediaPlayer.getCurrentPosition(), progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                swipeRefreshLayout.disableInterceptTouchEvent(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                swipeRefreshLayout.disableInterceptTouchEvent(false);
//                mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    Timber.d("onStopTrackingTouch: %d", seekBar.getProgress());
                    mediaPlayer.seekTo(seekBar.getProgress());
                    audioSeekbar.setProgress(Math.min(mediaPlayer.getCurrentPosition(), seekBar.getProgress()));
                    btnPlay.setSelected(true);
                }
            }
        });
    }

    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        int total = mediaPlayer.getDuration();
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                return;
            }
            audioSeekbar.setProgress(currentPosition);
        }
    }

    private void clearMediaPlayer() {
        btnPlay.setSelected(false);
        mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void initActivityResultLauncher() {
        videoResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

            }
        });
    }

    private void getVideosList() {
        if (NetworkReceiver.isConnected()) {
            if (from != null && from.equals(Constants.TAG_MUSIC)) {
                Map<String, String> map = new HashMap<>();
                map.put("user_id", GetSet.getUserId());
                map.put("token", GetSet.getAuthToken());
                map.put("limit", String.valueOf(Constants.MAX_LIMIT));
                map.put("sound_id", musicItem.getSoundId());
                map.put("type", "relatedsound");
                map.put("offset", String.valueOf(Constants.MAX_LIMIT * offset));
                //Timber.d("getVideosList: check apis params=> %s", App.getGsonPrettyInstance().toJson(map));

                Call<VideoListResponse> call = apiInterface.getSoundVideos(map);
                call.enqueue(new Callback<VideoListResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<VideoListResponse> call, @NotNull Response<VideoListResponse> response) {
                        swipeRefresh(false);
                        if (response.isSuccessful()) {
                            if (response.body().getStatus().equals("true")) {
                                if (isScroll) {
                                    videoList.remove(videoList.size() - 1);
                                    videosAdapter.notifyItemRemoved(videoList.size());
                                }
                                if (offset == 0) {
                                    videoList.clear();
                                }

                                List<VideoListResponse.Result> resultList = response.body().getResult();
                                videoList.addAll(resultList);

                                if (resultList.size()>0){
                                    IsFavorite = resultList.get(0).getIsFavorite();
                                    musicItem.setIsFavorite(IsFavorite.equals("true")?true:false);
                                    Log.d(TAG, "onResponse_ggg: "+new Gson().toJson(resultList));
                                    if (IsFavorite.equals("true")){
                                        btnBookMark.setSelected(Boolean.parseBoolean(IsFavorite));
                                    }else {
                                        btnBookMark.setSelected(false);
                                    }
                                }

                                if (resultList.size() > 0) {
                                    isLoading = false;
                                    videosAdapter.notifyDataSetChanged();
                                } else
                                    isLoading = true;
                                setNullLay();
                            } else if (response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                                App.makeToast(getString(R.string.Account_block));
                                GetSet.reset();
                                SharedPref.clearAll();
                                DBHelper.getInstance(VideoListActivity.this).clearDB();
                                Intent logout = new Intent(VideoListActivity.this, LoginActivity.class);
                                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(logout);
                            }
                            else {
                                //Remove bottom loading item
                                if (isScroll) {
                                    videoList.remove(videoList.size() - 1);
                                    videosAdapter.notifyItemRemoved(videoList.size());
                                }
                                isLoading = true;
                                setNullLay();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<VideoListResponse> call, Throwable t) {
                        t.printStackTrace();
                        swipeRefresh(false);
                        if (offset != 0)
                            offset--;
                        if (!videoList.isEmpty() && videoList.get(videoList.size() - 1) == null) {
                            videoList.remove(videoList.size() - 1);
                            videosAdapter.notifyItemRemoved(videoList.size());
                        }
                        call.cancel();
                    }
                });
            } else if (from != null && from.equals(Constants.TAG_RELATED_HASHTAG)) {
                HashTagRequest request = new HashTagRequest();
                request.setUserId(GetSet.getUserId());
                request.setOffset(Constants.MAX_LIMIT * offset);
                request.setLimit(Constants.MAX_LIMIT);
                request.setSearchKey(hashtag);
                request.setType(Constants.TAG_RELATED);
//                Timber.e("requestmap", "-" + new Gson().toJson(request));
                Call<HashTagResponse> call = apiInterface.getHashTags(request);
                call.enqueue(new Callback<HashTagResponse>() {
                    @Override
                    public void onResponse(Call<HashTagResponse> call, Response<HashTagResponse> response) {
                        swipeRefresh(false);
                        if (response.isSuccessful()) {
                            if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                if (isScroll) {
                                    videoList.remove(videoList.size() - 1);
                                    videosAdapter.notifyItemRemoved(videoList.size());
                                }
                                if (offset == 0) {
                                    videoList.clear();
                                }
                                List<VideoListResponse.Result> resultList = response.body().getResult().getVideos();
                                videoList.addAll(resultList);
                                if (resultList.size() > 0) {
                                    isLoading = false;
                                    videosAdapter.notifyDataSetChanged();
                                } else
                                    isLoading = true;
                                setNullLay();
                            } else {
                                //Remove bottom loading item
                                if (isScroll) {
                                    videoList.remove(videoList.size() - 1);
                                    videosAdapter.notifyItemRemoved(videoList.size());
                                }
                                isLoading = true;
                                setNullLay();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<HashTagResponse> call, Throwable t) {
                        Timber.tag(TAG).e("getVideosList: %s", t.getMessage());
                        call.cancel();
                        swipeRefresh(false);
                        if (offset != 0)
                            offset--;
                        if (!videoList.isEmpty() && videoList.get(videoList.size() - 1) == null) {
                            videoList.remove(videoList.size() - 1);
                            videosAdapter.notifyItemRemoved(videoList.size());
                        }
                    }
                });
            } else {
                HashMap<String, String> requestMap = new HashMap<>();
                if (Constants.Login_method.equals("skip")){
                    requestMap.put(Constants.TAG_USER_ID, "");
                }else {
                    requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
                }

                if (hashtag != null) {
                    requestMap.put(Constants.TAG_HASHTAG, hashtag);
                }
                requestMap.put(Constants.TAG_LIMIT, "" + limit);
                requestMap.put(Constants.TAG_OFFSET, "" + (offset * limit));
                requestMap.put(Constants.TAG_TYPE, type);
                Timber.tag(TAG).d(" getVideoList: %s", new Gson().toJson(requestMap));

                Call<VideoListResponse> call = apiInterface.getVideoList(requestMap);
                call.enqueue(new Callback<VideoListResponse>() {
                    @Override
                    public void onResponse(Call<VideoListResponse> call, Response<VideoListResponse> response) {
                        swipeRefresh(false);
                        if (response.isSuccessful()) {
                            if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                if (isScroll) {
                                    videoList.remove(videoList.size() - 1);
                                    videosAdapter.notifyItemRemoved(videoList.size());
                                }
                                if (offset == 0) {
                                    videoList.clear();
                                }
                                List<VideoListResponse.Result> resultList = response.body().getResult();
                                videoList.addAll(resultList);

                                if (resultList.size() > 0) {
                                    isLoading = false;
                                    videosAdapter.notifyDataSetChanged();
                                } else
                                    isLoading = true;
                                setNullLay();
                            } else {
                                //Remove bottom loading item
                                if (isScroll) {
                                    videoList.remove(videoList.size() - 1);
                                    videosAdapter.notifyItemRemoved(videoList.size());
                                }
                                isLoading = true;
                                setNullLay();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<VideoListResponse> call, Throwable t) {
                        Timber.tag(TAG).e("getVideosList: %s", t.getMessage());
                        t.printStackTrace();
                        swipeRefresh(false);
                        if (offset != 0)
                            offset--;
                        if (!videoList.isEmpty() && videoList.get(videoList.size() - 1) == null) {
                            videoList.remove(videoList.size() - 1);
                            videosAdapter.notifyItemRemoved(videoList.size());
                        }
                    }
                });
            }
        }
    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            offset = 0;
            totalItemCount = 0;
            lastVisibleItem = 0;
            getVideosList();
        }
    }

    private void setNullLay() {
        if (videoList.size() == 0) {
            nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_videos));
            nullText.setText(getString(R.string.no_videos_found));
            nullLay.setVisibility(View.VISIBLE);
            nullImage.setVisibility(View.VISIBLE);
        } else {
            nullLay.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VideoDeleteEvent event) {
        if (event.isVideoDeleted() && !TextUtils.isEmpty(event.getVideoId())) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                VideoListResponse.Result item = videoList.stream().filter(result1 ->
                        result1.getVideoId().equals(event.getVideoId())).findAny().orElse(null);
                if (item != null) {
                    int index = videoList.indexOf(item);
                    if (index != -1) {
                        videoList.remove(index);
                        videosAdapter.notifyItemRemoved(index);
                        videosAdapter.notifyItemRangeChanged(index, videoList.size());
                    }
                }
            } else {
                for (int i = 0; i < videoList.size(); i++) {
                    if (event.getVideoId().equals(videoList.get(i).getVideoId())) {
                        videoList.remove(i);
                        videosAdapter.notifyItemRemoved(i);
                        videosAdapter.notifyItemRangeChanged(i, videoList.size());
                        break;
                    }
                }
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseMediaPlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }

    private void pauseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        btnPlay.setSelected(false);
        mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnBack) {
            onBackPressed();
        } else if (view.getId() == R.id.useLay || view.getId() == R.id.txtUse) {
            if (Constants.Login_method.equals("skip")){
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra(Constants.TAG_SELECT_HASH_TAG, hashtag);
                startActivity(intent);
                finish();
            }else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionArray = PermissionsUtils.CAMERA_STORAGE_PERMISSION_V13;
                    if (PermissionsUtils.checkPermissionsArray(permissionArray, VideoListActivity.this)) {
                        Intent intent = new Intent(this, CameraActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.TAG_SOUND_ID, musicItem.getSoundId());
                        bundle.putString(Constants.TAG_SOUND_TITLE, musicItem.getTitle());
                        bundle.putString(Constants.TAG_SOUND_URL, musicItem.getSoundUrl());
                        bundle.putString(Constants.TAG_SOUND_IS_FAV, "" + musicItem.getIsFavorite());
                        bundle.putString(Constants.TAG_SOUND_COVER, musicItem.getCoverImage());
                        bundle.putString(Constants.TAG_SOUND_DURATION, musicItem.getDuration());
                        intent.putExtra(Constants.TAG_SOUND_DATA, bundle);
                        startActivity(intent);
                        overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_zoom_out);
                    } else {
                        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            galleryResultLauncher.launch(PermissionsUtils.CAMERA_STORAGE_PERMISSION_V13);
                        } else {
                            ActivityCompat.requestPermissions(VideoListActivity.this, permissionArray,
                                    100);
                        }
                    }
                } else {
                    Intent intent = new Intent(this, CameraActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TAG_SOUND_ID, musicItem.getSoundId());
                    bundle.putString(Constants.TAG_SOUND_TITLE, musicItem.getTitle());
                    bundle.putString(Constants.TAG_SOUND_URL, musicItem.getSoundUrl());
                    bundle.putString(Constants.TAG_SOUND_IS_FAV, "" + musicItem.getIsFavorite());
                    bundle.putString(Constants.TAG_SOUND_COVER, musicItem.getCoverImage());
                    bundle.putString(Constants.TAG_SOUND_DURATION, musicItem.getDuration());
                    intent.putExtra(Constants.TAG_SOUND_DATA, bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_zoom_out);
                }
            }
        } else if (view.getId() == R.id.btnUse) {
            if (Constants.Login_method.equals("skip")){
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra(Constants.TAG_SELECT_HASH_TAG, hashtag);
                startActivity(intent);
                finish();
            }else {
                Intent intent = new Intent(this, CameraActivity.class);
                intent.putExtra(Constants.TAG_SELECT_HASH_TAG, hashtag);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_zoom_out);
            }

        } else if (view.getId() == R.id.btnBookMark) {
            App.preventMultipleClick(btnBookMark);
            musicItem.setIsFavorite(!musicItem.getIsFavorite());
            resultCode = RESULT_OK;
            addFavourite(musicItem.getSoundId());
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (checkPermissions(permissions, VideoListActivity.this)) {
                //  ImagePicker.pickImage(this, getString(R.string.select_your_image));
            } else {
                if (shouldShowRationale(permissions, VideoListActivity.this)) {
                    ActivityCompat.requestPermissions(VideoListActivity.this, permissions, 100);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 100);
                }
            }
        }
    }

    private boolean checkPermissions(String[] permissionList, VideoListActivity activity) {
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

    private boolean shouldShowRationale(String[] permissions, VideoListActivity activity) {
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

    @Override
    public void onItemClicked(Object o, int position, String type) {
        VideoListResponse.Result item = (VideoListResponse.Result) o;
        /*Intent intent = new Intent(mContext, SingleVideoActivity.class);
        intent.putExtra(Constants.TAG_VIDEO_ID, item.getVideoId());
        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, item.getStreamThumbnail());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        videoResultLauncher.launch(intent);*/

        String typeStr = "", tagId = "", videoid = "";
        if (from == null) {
            typeStr = "videos";
            videoid = item.getVideoId();
            tagId = title;
        } else if (from.equals(Constants.TAG_RELATED_HASHTAG)) {
            typeStr = Constants.TAG_RELATED;
            videoid = item.getStreamId();
//            tagId = title;
            tagId = hashtag;
        } else if (from.equals(Constants.TAG_HASHTAGS)) {
            typeStr = "hashtags";
            tagId = hashtag;
            videoid = item.getVideoId();
        } else if (from.equals(Constants.TAG_MUSIC)) {
            typeStr = "musics";
            tagId = musicItem.getSoundId();
            videoid = item.getVideoId();
        } else {
            tagId = title;
        }

//        Timber.tag(TAG).d("onItemClicked: %s", new Gson().toJson(item));
//        Timber.tag(TAG).d("onItemClicked: %s, %s", tagId, typeStr);
        Intent intent = new Intent(mContext, MultipleVideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Constants.TAG_TYPE, typeStr);
        intent.putExtra(Constants.TAG_TAGID, tagId);
        intent.putExtra(Constants.TAG_VIDEO_ID, videoid);
        intent.putExtra(Constants.TAG_POSITION, item.getPosition());
        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, item.getStreamThumbnail());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent();
            intent.putExtra(Constants.TAG_FAV, musicItem.getIsFavorite());
            intent.putExtra(Constants.TAG_FROM, from);
            setResult(resultCode, intent);
        }
        super.onBackPressed();
    }

    private void addFavourite(String soundId) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_ADDIDTOFAV, soundId);
            map.put(Constants.TAG_FAV_TYPE, "sound");

            Call<Map<String, String>> call = apiInterface.setFav(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Log.e("checkResaddFav","-"+new Gson().toJson(response.body()));
                        if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (response.body().get("message").equals("Favoured successfully")) {
                                btnBookMark.setSelected(musicItem.getIsFavorite());
                                App.makeToast(getString(R.string.fav_success));
                            }else{
                                btnBookMark.setSelected(false);
                                App.makeToast(getString(R.string.fav_unsuccess));
                            }

                        }
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
}
