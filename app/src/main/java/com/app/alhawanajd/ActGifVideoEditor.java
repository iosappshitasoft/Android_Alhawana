package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.akexorcist.localizationactivity.ui.LocalizationActivity;
import com.app.alhawanajd.Video.PostVideoActivity;
import com.app.alhawanajd.Video.UploadSoundActivity;
import com.app.alhawanajd.adapter.ShareAdapter;
import com.app.external.video_trimmer.utils.CompressOption;
import com.app.external.video_trimmer.utils.CustomProgressView;
import com.app.external.video_trimmer.utils.FileUtils;
import com.app.external.video_trimmer.utils.LogMessage;
import com.app.external.video_trimmer.utils.TrimVideo;
import com.app.external.video_trimmer.utils.TrimVideoOptions;
import com.app.external.video_trimmer.utils.TrimmerUtils;
import com.app.helper.StorageUtils;
import com.app.helper.workers.AudioVideoMergeWorker;
import com.app.helper.workers.FileDownloadWorker;
import com.app.livedata.MessageLiveModel;
import com.app.model.ChatResponse;
import com.app.model.GetSet;
import com.app.model.HomeResponse;
import com.app.model.ShareItem;
import com.app.utils.Constants;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback;
import com.arthenica.ffmpegkit.ReturnCode;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class ActGifVideoEditor extends LocalizationActivity {


    private static final String TAG = ActGifVideoEditor.class.getSimpleName();
    private static final int PER_REQ_CODE = 115;
    private Context mContext;
    private StyledPlayerView playerView;
    private ExoPlayer exoPlayer;

    private ImageView ivMusic;
    private TextView txtSound;
    private ImageView imagePlayPause;
    private ImageView[] imageViews;
    private ImageView btnBack;

    private long totalDuration;

    private Dialog dialog;

    private Uri originalUri, uri;
    private String language;
    private File originalFile, trimFile, audioFile = null;
    private boolean isFromCamera = false;

    private TextView txtStartDuration, txtTrimDuration, txtEndDuration;
    private ProgressBar progressBar;
    private LinearLayout soundLay;
    private CrystalRangeSeekbar seekbar;
    private CrystalSeekbar seekbarController;
    private Button btnNext;

    private long lastMinValue = 0;
    private long lastMaxValue = 0;

    private String soundId = null, soundUrl = null;
    private MenuItem menuDone;
    private boolean isValidVideo = true, isVideoEnded;
    private Handler seekHandler;
    private Bundle bundle;


    private TrimVideoOptions trimVideoOptions;

    private long currentDuration, lastClickedTime;
    boolean isStickerMode = false;

    Runnable updateSeekbar = new Runnable() {
        @Override
        public void run() {
            try {
                currentDuration = exoPlayer.getCurrentPosition() / 1000;
                if (!exoPlayer.getPlayWhenReady())
                    return;
                if (currentDuration <= lastMaxValue)
                    seekbarController.setMinStartValue((int) currentDuration).apply();
                else
                    exoPlayer.setPlayWhenReady(false);
            } finally {
                seekHandler.postDelayed(updateSeekbar, 1000);
            }
        }
    };
    private String video_id = null;
    private String gethashTag = "";
    private CompressOption compressOption;
    private String outputPath;
    private String local;
    private int trimType;
    private long fixedGap, minGap, minFromGap, maxToGap;
    private boolean hidePlayerSeek, isAccurateCut, showFileLocationAlert;
    private CustomProgressView progressView;
    private ActivityResultLauncher<Intent> audioResultLauncher;
    private ProgressDialog progressDialog;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private boolean launchedFromShare = false;

    private String gifOutputPath = "";


    private Rect cropRect = null;

    private View cropOverlay;
    private EditText etStickerText;
    private Button btnToggleStickerMode, btnCropReset;

    CardView cardVideo;
    private MessageLiveModel liveModel;
    private float lastX = 0f;
    private float lastY = 0f;
    private float startTX = 0f;
    private float startTY = 0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_video_trimmer);
        FFmpegKitConfig.enableStatisticsCallback(null);
        liveModel = new ViewModelProvider(this).get(MessageLiveModel.class);
        findViews();

        bundle = getIntent().getExtras(); // Ensure bundle is available early

        Uri videoUriFromIntent = null;
      isStickerMode = getIntent().getBooleanExtra("isStickerMode", false);

        // 1. Try to get URI from ACTION_SEND
        Intent incoming = getIntent();
        if (Intent.ACTION_SEND.equals(getIntent().getAction()) && getIntent().getType() != null && getIntent().getType().startsWith("video/")) {
            launchedFromShare = true;
            videoUriFromIntent = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            if (videoUriFromIntent != null) {
                Log.d(TAG, "Received shared video URI: " + videoUriFromIntent.toString());
            } else {
                Log.e(TAG, "Received ACTION_SEND with null video URI.");
            }
        }

        // 2. If not from ACTION_SEND or URI is null, try to get from TRIM_VIDEO_URI
        if (videoUriFromIntent == null && bundle != null) { // Add null check for bundle
            String uriString = bundle.getString(TrimVideo.TRIM_VIDEO_URI);
            if (!TextUtils.isEmpty(uriString)) {
                videoUriFromIntent = Uri.parse(uriString);
                Log.d(TAG, "Loading from TRIM_VIDEO_URI: " + videoUriFromIntent.toString());
            } else {
                Log.e(TAG, "No TRIM_VIDEO_URI found in bundle.");
            }
        }

        // 3. Assign to class members
        originalUri = videoUriFromIntent;
        uri = videoUriFromIntent;

        // Crucial: check if URI is still null at this point
        if (originalUri == null) {
            Log.e(TAG, "FATAL: Video URI is null. Cannot proceed with trimming.");
            Toast.makeText(this, "Error: No video selected for trimming.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Now initialize trimVideoOptions and other values that depend on the bundle
        initValues(); // This will now *only* read other options, not the URI.

        setUpToolBar(getSupportActionBar(), trimVideoOptions.title);
        progressView = new CustomProgressView(this);

        initPlayer(); // Initialize player after URI is determined
        initActivityResultLauncher();

        String currentLanguageCode = Locale.getDefault().getLanguage();
        if(currentLanguageCode.equals("en")) {
            btnNext.setText("Next");
            txtSound.setText("Generate");
        }else{
            btnNext.setText("التالي");
            txtSound.setText("يولد");
        }

        btnNext.setVisibility(View.GONE);
        ivMusic.setVisibility(View.GONE);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if ((lastMaxValue - lastMinValue) <= AdminData.max_video_duration) {
                App.preventMultipleClick(btnNext);
                exoPlayer.setPlayWhenReady(false);
                Timber.d("btnNext: %s", audioFile);
                if (audioFile != null) {
                    mergeAudioVideo(audioFile.getPath());
                } else {
                    trimVideo();
                }
//                } else {
//                    Log.d(TAG, "onClick: tostydata");
//                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.preventMultipleClick(btnBack);
                //onBackPressed();
                if (launchedFromShare) {
                    launchedFromShare = false;
                    Intent intent = new Intent(ActGifVideoEditor.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    onBackPressed();
                }
            }
        });

   /*     cropOverlay = new View(this);
        cropOverlay.setBackground(getResources().getDrawable(R.drawable.crop_border));*/
        cropOverlay.setVisibility(View.GONE);
//        playerView.addView(cropOverlay);
        btnCropReset.setOnClickListener(v -> {
            cropRect = null;
            cropOverlay.setVisibility(View.GONE);
        });
//        enableVideoDrag(isStickerMode);
        btnToggleStickerMode.setOnClickListener(v -> {
            isStickerMode = !isStickerMode;
            cropOverlay.setVisibility(isStickerMode ? View.VISIBLE : View.GONE);
            btnToggleStickerMode.setText(isStickerMode ? "Editing..." : "Sticker Mode");
        });

        if (isStickerMode) {

            FrameLayout.LayoutParams lp =
                    (FrameLayout.LayoutParams) cropOverlay.getLayoutParams();


//            cropOverlay.setLayoutParams(lp);


            // 1. Reduce height
            ViewGroup.LayoutParams params = cardVideo.getLayoutParams();
            params.height = dpToPx(350); // Your custom height
            cardVideo.setLayoutParams(params);

            // 2. MODIFY CONSTRAINTS so it attaches to topBar, not centered
            ConstraintLayout.LayoutParams lp1 =
                    (ConstraintLayout.LayoutParams) cardVideo.getLayoutParams();

            lp1.topToBottom = R.id.topBar;
            lp1.bottomToTop = R.id.controlsLay; // keep this
            lp1.verticalBias = 0f; // <—— VERY IMPORTANT (forces it to TOP)

            cardVideo.setLayoutParams(lp1);

            // 3. Change stickerControls position
            LinearLayout stickerControls = findViewById(R.id.stickerControls);
            ConstraintLayout.LayoutParams stickerLp =
                    (ConstraintLayout.LayoutParams) stickerControls.getLayoutParams();

            stickerLp.topToBottom = R.id.cardVideo;
            stickerLp.bottomToTop = R.id.controlsLay;
            stickerLp.verticalBias = 0f;  // Move it up

            stickerControls.setLayoutParams(stickerLp);

            stickerControls.setVisibility(View.VISIBLE);



        }


//        if (checkStoragePermission())
        setDataInView(false);
        setupCropGesture();
        setupVideoDrag();

    }
    private void setupVideoDrag() {

        playerView.setOnTouchListener((v, event) -> {

            // Only drag in sticker mode
            if (!isStickerMode) return false;

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    startTX = playerView.getTranslationX();
                    startTY = playerView.getTranslationY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - lastX;
                    float dy = event.getRawY() - lastY;

                    playerView.setTranslationX(startTX + dx);
                    playerView.setTranslationY(startTY + dy);
                    return true;
            }
            return false;
        });
    }

    private void enableVideoDrag(boolean enable) {

        if (!enable) {
            playerView.setOnTouchListener(null);
            return;
        }

        playerView.setOnTouchListener(new View.OnTouchListener() {

            float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        dX = event.getRawX() - v.getX();
                        dY = event.getRawY() - v.getY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        v.setX(event.getRawX() - dX);
                        v.setY(event.getRawY() - dY);
                        return true;
                }

                return false;
            }
        });
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCropGesture() {

        cropOverlay.setOnTouchListener((v, e) -> true); // consume touch but do nothing

    }


    private void initValues() {
        mContext = this;
        Gson gson = new Gson();
        String videoOption = bundle.getString(TrimVideo.TRIM_VIDEO_OPTION);
        trimVideoOptions = gson.fromJson(videoOption, TrimVideoOptions.class);

        // If trimVideoOptions is null (e.g., from ACTION_SEND intent without custom options),
        // initialize it to a default instance.
        if (trimVideoOptions == null) {
            trimVideoOptions = new TrimVideoOptions();
            trimVideoOptions.mediaFrom = ""; // Ensure mediaFrom is not null
            Log.d(TAG, "initValues: trimVideoOptions was null, initialized to default.");
        }

        language = bundle != null ? bundle.getString("lan") : null; // Add null check for bundle
        if (TextUtils.isEmpty(language)) {
            language = "en"; // Default language if not provided
            Log.d(TAG, "initValues: language was null or empty, defaulted to 'en'.");
        }
        Log.d(TAG, "initValues_language: "+language);
        Log.d(TAG, "initValues_originalUri: "+originalUri);
        File destDir = StorageUtils.getInstance(this).getExtCacheDir();
        // Safely check mediaFrom, assuming default is not from camera if not specified
        isFromCamera = (trimVideoOptions.mediaFrom != null && trimVideoOptions.mediaFrom.equals("camera"));
        soundId = trimVideoOptions.soundId;
        soundUrl = trimVideoOptions.soundUrl;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.msg_please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        if (!TextUtils.isEmpty(soundId)) {
            setSoundSelected(true);
            if (!TextUtils.isEmpty(soundUrl)) {
                if (URLUtil.isValidUrl(soundUrl) && Patterns.WEB_URL.matcher(soundUrl).matches()) {
                    downloadFile();
                } else {
                    audioFile = new File(soundUrl);
                }
            }
            soundLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    App.makeToast("Sound already selected");
                }
            });
        } else {
            setSoundSelected(false);
            soundLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File outputFile = StorageUtils.getInstance(mContext).getTempFile(mContext, Constants.TAG_VIDEO_AUDIO_TRIM + Utility.VIDEO_FORMAT);
                    outputPath = outputFile.getAbsolutePath();


                }
            });
            soundLay.setOnClickListener(v -> {

                showProcessingDialog();
       /*             File outputFile = StorageUtils.getInstance(mContext)
                            .getTempFile(mContext, "GIF_" + System.currentTimeMillis() + ".gif");

                    gifOutputPath = outputFile.getAbsolutePath();

                    createGIFfromTrimmedVideo(trimFile.getAbsolutePath(), gifOutputPath);*/

                if(isStickerMode){
createStickerOutput();
                }
                else {
                    File outputFile = StorageUtils.getInstance(mContext).getTempFile(mContext, Constants.TAG_VIDEO_AUDIO_TRIM + Utility.VIDEO_FORMAT);
//            Timber.tag(TAG).d("trimVideo: %s", outputFile.exists());
//            Timber.tag(TAG).d("trimVideo: %s", uri);
//            if (outputFile.exists())
                    outputPath = outputFile.getAbsolutePath();


                    String[] complexCommand;
                    complexCommand = getAccurateCmd();

                    execFFmpegBinary(complexCommand, true);
                }
            });

        }
        if (isFromCamera) {
            String fileName = System.currentTimeMillis() + "." + StorageUtils.getInstance(this).getExtension(new File("" + originalUri));
            originalFile = StorageUtils.getInstance(this).saveFile(new File("" + originalUri), destDir, fileName);
        } else {
            String fileName = System.currentTimeMillis() + "." + StorageUtils.getInstance(this).getExtension(originalUri);
            originalFile = StorageUtils.getInstance(this).saveUri(originalUri, destDir, fileName);
        }
        trimFile = originalFile;
        seekHandler = new Handler();
    }

    private void createStickerOutput() {
        try {
            File outputFile = new File(
                    getCacheDir(),
                    "STICKER_" + System.currentTimeMillis() + ".gif"
            );

            String inputPath = trimFile.getAbsolutePath();
            String stickerText = etStickerText.getText().toString().trim();

            // -------- BUILD FILTER --------
            StringBuilder filterBuilder = new StringBuilder();

            // ---- Crop Filter ----
            if (cropRect != null) {
                int cropW = cropRect.width();
                int cropH = cropRect.height();
                int cropX = cropRect.left;
                int cropY = cropRect.top;

                filterBuilder.append(
                        String.format(Locale.US,
                                "crop=%d:%d:%d:%d",
                                cropW, cropH, cropX, cropY
                        )
                );
            }

            // ---- Text Filter ----
            if (!stickerText.isEmpty()) {
                if (filterBuilder.length() > 0) filterBuilder.append(",");

                String safeText = stickerText.replace("'", "\\'");

                filterBuilder.append(
                        "drawtext=text='" + safeText +
                                "':fontcolor=white:fontsize=32:" +
                                "x=(w-text_w)/2:" +
                                "y=h-(text_h*2)"
                );
            }

            String filter = filterBuilder.toString();

            Log.e("FILTER", filter);

            // -------- BUILD FFMPEG COMMAND --------
            String[] cmd;

            if (filter.isEmpty()) {
                // NO FILTER
                cmd = new String[]{
                        "-y",
                        "-i", inputPath,
                        "-loop", "0",
                        outputFile.getAbsolutePath()
                };
            } else {
                // WITH FILTER
                cmd = new String[]{
                        "-y",
                        "-i", inputPath,
                        "-vf", filter,
                        "-loop", "0",
                        outputFile.getAbsolutePath()
                };
            }

            Log.e("FFMPEG_CMD", Arrays.toString(cmd));

            // Execute
            execStickerFF(cmd, outputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void execStickerFF(String[] command, File output) {

//    progressDialog.show();
        FFmpegKit.executeAsync(String.join(" ", command), session -> {
            ReturnCode code = session.getReturnCode();

            runOnUiThread(() -> {
                dialog.dismiss();

                if (ReturnCode.isSuccess(code)) {
                    Toast.makeText(this, "Sticker Created!", Toast.LENGTH_SHORT).show();
                    openShareGIF(output.getAbsolutePath());
                } else {
                    Toast.makeText(this, "Failed: " + session.getFailStackTrace(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }


    private void setSoundSelected(boolean selected) {
        /*if (selected) {
            txtSound.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            ivMusic.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else {
            txtSound.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite));
            ivMusic.setColorFilter(ContextCompat.getColor(mContext, R.color.colorWhite));
        }*/
    }

    private void createGIFfromTrimmedVideo(String inputVideo, String gifOutputPath) {

        Log.d(TAG, "createGIFfromTrimmedVideo: "+inputVideo+"    "+gifOutputPath);

        showProcessingDialog(); // reuse your current loader

        String palettePath = gifOutputPath.replace(".gif", "_palette.png");

        String generatePalette =
                "-i " + inputVideo + " -vf fps=12,scale=480:-1:flags=lanczos,palettegen -y " + palettePath;

        String generateGif =
                "-i " + inputVideo +
                        " -i " + palettePath +
                        " -filter_complex \"fps=12,scale=480:-1:flags=lanczos[x];[x][1:v]paletteuse\"" +
                        " -y " + gifOutputPath;


        // Step 1: Generate palette
        FFmpegKit.executeAsync(generatePalette, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {

                // Step 2: GIF using palette
                FFmpegKit.executeAsync(generateGif, session2 -> {
                    runOnUiThread(() -> {
                        dialog.dismiss();

                        if (ReturnCode.isSuccess(session2.getReturnCode())) {
                            openShareGIF(gifOutputPath);
                        } else {
                            Toast.makeText(this, "Failed to create GIF", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            } else {
                dialog.dismiss();
                Toast.makeText(this, "Failed to generate palette", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openShareGIF(String gifPath) {

        if(isStickerMode){
            openRepostUsersBottomSheet();
        }
        else{
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/gif");

        Uri gifUri = FileProvider.getUriForFile(
                this,
                getString(R.string.file_provider_authority),
                new File(gifPath)
        );

        shareIntent.putExtra(Intent.EXTRA_STREAM, gifUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share GIF"));
    }
    }
    private void openRepostUsersBottomSheet() {


        liveModel.getRecentChats(20, 0).observe((LifecycleOwner) this, chatList -> {

            if (chatList == null || chatList.isEmpty()) {

                return;
            }

            if (chatList.size() > 0) {
                chatList.remove(0);
            }
            else{

            }

            List<ShareItem> shareItems = new ArrayList<>();

            for (ChatResponse chat : chatList) {
                shareItems.add(
                        new ShareItem(
                                chat.getUserImage(),     // image url
                                chat.getUserName(),             // user name
                                chat.getUserId()            // user id
                        )
                );
            }

            shareItems.add(
                    new ShareItem(
                            R.drawable.invie_friends,      // your profile image
                            "Invite",                  // name below image
                            "invite_friend"            // SPECIAL ID
                    )
            );
        });


        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.repost_users_bottom_sheet, null);

        RecyclerView rv = view.findViewById(R.id.repostUserListRv);
        rv.setLayoutManager(new LinearLayoutManager(this));

        String currentUserId = GetSet.getUserId();

        List<HomeResponse.Result.RepostedData> sortedList = new ArrayList<>();


        rv.setAdapter(new RepostUsersAdapter(this, sortedList));

        dialog.setContentView(view);
        dialog.show();
    }

    private void initActivityResultLauncher() {
        audioResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
//                        showProgressDialog();
                        Intent data = result.getData();
//                        releasePlayer();
//                        initPlayer();
                        if (!TextUtils.isEmpty(data.getStringExtra(Constants.TAG_SOUND_ID))) {
                            soundId = data.getStringExtra(Constants.TAG_SOUND_ID);
                            setSoundSelected(true);
                        }
                        soundUrl = data.getStringExtra(Constants.TAG_SOUND_URL);
//                        String duration = data.getStringExtra(Constants.TAG_SOUND_DURATION);
//                        String audioThumbnail = data.getStringExtra(Constants.TAG_SONG_ALBUM);
//                        String title = data.getStringExtra(Constants.TAG_SONG_TITLE);
                        audioFile = new File(soundUrl);
//                        mergeAudioVideo(soundPath);
                    }
                }
            }
        });
    }

    private void showProgressDialog() {
        mainHandler.post(new Runnable() {
            public void run() {
                if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.show();
                }
            }
        });
    }

    private void dismissProgressDialog() {
        mainHandler.post(new Runnable() {
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    /*@Override
    protected void attachBaseContext(@NotNull Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }*/

    private void findViews() {
        btnBack = findViewById(R.id.btnBack);
        playerView = findViewById(R.id.player_view_lib);
        imagePlayPause = findViewById(R.id.image_play_pause);
        seekbar = findViewById(R.id.range_seek_bar);
        txtStartDuration = findViewById(R.id.txt_start_duration);
        txtTrimDuration = findViewById(R.id.txt_trim_duration);
        txtEndDuration = findViewById(R.id.txt_end_duration);
        seekbarController = findViewById(R.id.seekbar_controller);
        progressBar = findViewById(R.id.progress_circular);
        txtSound = findViewById(R.id.txtSound);
        ivMusic = findViewById(R.id.ivMusic);
        soundLay = findViewById(R.id.soundLay);
        btnNext = findViewById(R.id.btnNext);
        ImageView imageOne = findViewById(R.id.image_one);
        ImageView imageTwo = findViewById(R.id.image_two);
        ImageView imageThree = findViewById(R.id.image_three);
        ImageView imageFour = findViewById(R.id.image_four);
        ImageView imageFive = findViewById(R.id.image_five);
        ImageView imageSix = findViewById(R.id.image_six);
        ImageView imageSeven = findViewById(R.id.image_seven);
        ImageView imageEight = findViewById(R.id.image_eight);
        imageViews = new ImageView[]{imageOne, imageTwo, imageThree,
                imageFour,imageFive,imageSix,imageSeven,imageEight};

        etStickerText = findViewById(R.id.etStickerText);
        btnToggleStickerMode = findViewById(R.id.btnToggleStickerMode);
        btnCropReset = findViewById(R.id.btnCropReset);

        cardVideo = findViewById(R.id.cardVideo);
        cropOverlay = findViewById(R.id.cropOverlay);

    }

    private void downloadFile() {
        showProgressDialog();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = mContext.getString(R.string.app_name) + "AUD_" + timeStamp + Utility.AUDIO_FORMAT;
        File tempFile = new File(StorageUtils.getInstance(mContext).getExtCacheDir(), fileName);

        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_URL, soundUrl);
        builder.putString(Constants.TAG_FILE_NAME, fileName);
        builder.putString(Constants.TAG_OUTPUT_PATH, tempFile.getAbsolutePath());
        builder.putString(Constants.TAG_FROM, Constants.TAG_AUDIO);

        OneTimeWorkRequest downloadWorkRequest = new OneTimeWorkRequest.Builder(FileDownloadWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .setInputData(builder.build())
                .build();
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        workManager.enqueue(downloadWorkRequest);
        workManager.getWorkInfoByIdLiveData(downloadWorkRequest.getId()).observe(ActGifVideoEditor.this, workInfo -> {
            if (workInfo.getState().isFinished()) {
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    String downloadPath = workInfo.getOutputData().getString(Constants.TAG_OUTPUT_PATH);
//                                    String duration = workInfo.getOutputData().getString(Constants.TAG_AUDIO_DURATION);
                    Timber.tag(TAG).d("downloadFile: %s", downloadPath);
                    audioFile = new File(downloadPath);
                    soundUrl = downloadPath;
                    mergeAudioVideo(downloadPath);
                    dismissProgressDialog();
                } else {
                    dismissProgressDialog();
                }
            }
        });
    }

    private void mergeAudioVideo(String soundPath) {
        showProcessingDialog();
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_VIDEO, originalFile.getAbsolutePath());
        builder.putString(Constants.TAG_AUDIO, soundPath);
        builder.putLong(Constants.TAG_VIDEO_START, lastMinValue);
        builder.putLong(Constants.TAG_VIDEO_END, lastMaxValue);

        OneTimeWorkRequest mergeWorkRequest = new OneTimeWorkRequest.Builder(AudioVideoMergeWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .setInputData(builder.build())
                .build();
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        workManager.enqueue(mergeWorkRequest);
        workManager.getWorkInfoByIdLiveData(mergeWorkRequest.getId()).observe(ActGifVideoEditor.this, workInfo -> {
            if (workInfo.getState().isFinished()) {
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    String mixVideoPath = workInfo.getOutputData().getString(Constants.TAG_VIDEO);
//                                    String duration = workInfo.getOutputData().getString(Constants.TAG_AUDIO_DURATION);
                    Timber.tag(TAG).d("mergeAudioVideo: %s", mixVideoPath);
                    trimFile = new File(mixVideoPath);
                    uri = Uri.parse(mixVideoPath);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setDataInView(true);
                            dismissProgressDialog();
                        }
                    }, 1000);

                    if (Constants.WATERMARK_ENABLED) {
                        addWatermark(mixVideoPath);
                    } else {
                        dismissProcessingDialog();
                        if (showFileLocationAlert)
                            showLocationAlert();
                        else {
                            if(launchedFromShare){
                                launchedFromShare = false;
                                Intent intent = new Intent(ActGifVideoEditor.this, PostVideoActivity.class);
                                if (soundId != null) {
                                    intent.putExtra(Constants.TAG_SOUND_ID, soundId);
                                    Log.d(TAG, "gotoPostVideoPage: soundid "+soundId);
                                }
                                if (soundUrl != null) {
                                    intent.putExtra(Constants.TAG_SOUND_URL, soundUrl);
                                    Log.d(TAG, "gotoPostVideoPage: soundurl "+soundUrl);
                                }
                                intent.putExtra(Constants.TAG_VIDEO_ID, video_id);
                                intent.putExtra(Constants.TAG_VIDEO_PATH, mixVideoPath);
                                intent.putExtra(Constants.TAG_SELECT_HASH_TAG, gethashTag);
                                intent.putExtra(Constants.TAG_TYPE, "gallery");
                                startActivity(intent);
                                finish();
                            }else {
                                Intent intent = new Intent();
                                intent.putExtra(TrimVideo.TRIMMED_VIDEO_PATH, mixVideoPath);
                                if (soundId != null) {
                                    intent.putExtra(Constants.TAG_SOUND_ID, soundId);
                                }
                                if (soundUrl != null) {
                                    intent.putExtra(Constants.TAG_SOUND_URL, soundUrl);
                                }
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                    }
                } else {
                    dismissProcessingDialog();
                }
            }
        });
    }

    private void addWatermark(String mixVideoPath) {
        final File outputFile = StorageUtils.getInstance(mContext).getTempFile(mContext, System.currentTimeMillis() + Utility.VIDEO_FORMAT);
        String watermarkUrl = "https://products.hitasoft.in/fundooUpdate/public/video/convert/30_1660805034text_logo.png";
        List<String> command = new ArrayList<>();
        command.add("-i");
        command.add(mixVideoPath);
        command.add("-i");
        command.add(watermarkUrl);
        command.add("-filter_complex");
        command.add("[1][0]scale2ref=w=oh*mdar:h=ih*0.07[logo][video];[logo]format=argb,colorchannelmixer=aa=0.4[logo transparent];[video][logo transparent]overlay=main_w-overlay_w-5:main_h-overlay_h-5:format=auto,format=yuv420p");
//        command.add("[1]lut=a=val*0.3[a];[0][a]overlay=main_w-overlay_w-5:main_h-overlay_h-5");
        command.add("-c:v");
        command.add("mpeg4");
        command.add("-preset");
        command.add("veryfast");
        command.add("-crf");
        command.add("23");
        command.add("-async");
        command.add("1");
        command.add("-y");
        command.add(outputFile.getAbsolutePath());
        Timber.tag(TAG).d("addWatermark: %s", Arrays.toString(command.toArray()));
        FFmpegKit.executeAsync(String.join(" ", command), new FFmpegSessionCompleteCallback() {
            @Override
            public void apply(FFmpegSession session) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dismissProcessingDialog();
                        if (session.equals(0)) {
                            if (showFileLocationAlert)
                                showLocationAlert();
                            else {
                                if(launchedFromShare){
                                    launchedFromShare = false;
                                    Intent intent = new Intent(ActGifVideoEditor.this, PostVideoActivity.class);
                                    if (soundId != null) {
                                        intent.putExtra(Constants.TAG_SOUND_ID, soundId);
                                        Log.d(TAG, "gotoPostVideoPage: soundid "+soundId);
                                    }
                                    if (soundUrl != null) {
                                        intent.putExtra(Constants.TAG_SOUND_URL, soundUrl);
                                        Log.d(TAG, "gotoPostVideoPage: soundurl "+soundUrl);
                                    }
                                    intent.putExtra(Constants.TAG_VIDEO_ID, video_id);
                                    intent.putExtra(Constants.TAG_VIDEO_PATH, outputFile.getAbsolutePath());
                                    intent.putExtra(Constants.TAG_SELECT_HASH_TAG, gethashTag);
                                    intent.putExtra(Constants.TAG_TYPE, "gallery");
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent();
                                    intent.putExtra(TrimVideo.TRIMMED_VIDEO_PATH, outputFile.getAbsolutePath());
                                    if (soundId != null) {
                                        intent.putExtra(Constants.TAG_SOUND_ID, soundId);
                                    }
                                    if (soundUrl != null) {
                                        intent.putExtra(Constants.TAG_SOUND_URL, soundUrl);
                                    }
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            }
                        } else {
                            LogMessage.v("Command cancelled");
                            if (dialog.isShowing())
                                dialog.dismiss();
                        }
                    }
                });
            }

           /* @Override
            public void apply(long executionId, int returnCode) {
                Timber.tag(TAG).d("addWatermark: %s, %s", executionId, returnCode);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dismissProcessingDialog();
                        if (returnCode == 0) {
                            if (showFileLocationAlert)
                                showLocationAlert();
                            else {
                                Intent intent = new Intent();
                                intent.putExtra(TrimVideo.TRIMMED_VIDEO_PATH, outputFile.getAbsolutePath());
                                if (soundId != null) {
                                    intent.putExtra(Constants.TAG_SOUND_ID, soundId);
                                }
                                if (soundUrl != null) {
                                    intent.putExtra(Constants.TAG_SOUND_URL, soundUrl);
                                }
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        } else {
                            LogMessage.v("Command cancelled");
                            if (dialog.isShowing())
                                dialog.dismiss();
                        }
                    }
                });
            }*/
        });
    }

    private void setUpToolBar(ActionBar actionBar, String title) {
        try {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(title != null ? title : getString(R.string.txt_edt_video));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SettingUp exoplayer
     **/

    private void initPlayer() {
        try {
            // 1. Create a TrackSelector and set it to prefer highest quality
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters()
                            .setMaxVideoSize(Integer.MAX_VALUE, Integer.MAX_VALUE) // allow highest resolution
                            .setForceHighestSupportedBitrate(true) // prefer high bitrate
            );

            // 2. Initialize the player with the trackSelector
            exoPlayer = new ExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();

            // 3. Attach player to view and set resize mode
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            playerView.setPlayer(exoPlayer);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void initPlayer() {
//        try {
//            exoPlayer = new ExoPlayer.Builder(this).build();
//            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
//            playerView.setPlayer(exoPlayer);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void setDataInView(boolean mixVideo) {
        try {
            Runnable fileUriRunnable = () -> {
                String path;
                if (!mixVideo && !isFromCamera) {
                  /*  path = FileUtils.getRealPath(ActGifVideoEditor.this, uri);
                    uri = Uri.parse(path);*/
                    path = FileUtils.getRealPath1(ActGifVideoEditor.this, uri);

                    if (path != null) {
                        uri = Uri.fromFile(new File(path));
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Invalid video file", Toast.LENGTH_SHORT).show());
                        return;
                    }

                }
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    totalDuration = TrimmerUtils.getMediaDuration(originalFile);
                    imagePlayPause.setOnClickListener(v ->
                            onVideoClicked());
                    Objects.requireNonNull(playerView.getVideoSurfaceView()).setOnClickListener(v ->
                            onVideoClicked());
                    initTrimData();
                    loadThumbnails();
                    buildMediaSource();
                    setUpSeekBar();

                });
            };
            Executors.newSingleThreadExecutor().execute(fileUriRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTrimData() {
        try {
            assert trimVideoOptions != null;
            trimType = TrimmerUtils.getTrimType(trimVideoOptions.trimType);
            hidePlayerSeek = trimVideoOptions.hideSeekBar;
            isAccurateCut = trimVideoOptions.accurateCut;
            local = trimVideoOptions.local;
            compressOption = trimVideoOptions.compressOption;
            showFileLocationAlert = trimVideoOptions.showFileLocationAlert;
            fixedGap = trimVideoOptions.fixedDuration;
            fixedGap = fixedGap != 0 ? fixedGap : totalDuration;
            minGap = trimVideoOptions.minDuration;
            minGap = minGap != 0 ? minGap : totalDuration;
            if (trimType == 3) {
                minFromGap = trimVideoOptions.minToMax[0];
                maxToGap = trimVideoOptions.minToMax[1];
                minFromGap = minFromGap != 0 ? minFromGap : totalDuration;
                maxToGap = maxToGap != 0 ? maxToGap : totalDuration;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (language.equals("ar")){
            setLanguage(new Locale(local != null ? local : "ar"));
        }else{
            setLanguage(new Locale(local != null ? local : "en"));
        }

    }

    private void onVideoClicked() {
        try {
            if (isVideoEnded) {
                seekTo(lastMinValue);
                exoPlayer.setPlayWhenReady(true);
                return;
            }
            if ((currentDuration - lastMaxValue) > 0)
                seekTo(lastMinValue);
            exoPlayer.setPlayWhenReady(!exoPlayer.getPlayWhenReady());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seekTo(long sec) {
        if (exoPlayer != null)
            exoPlayer.seekTo(sec * 1000);
    }

    private void buildMediaSource() {
        try {
//            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(originalFile)));
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                    Timber.tag(TAG).d("onPlayerStateChanged: %b, %d", playWhenReady, reason);
                    imagePlayPause.setVisibility(playWhenReady ? View.GONE :
                            View.VISIBLE);
                }

                @Override
                public void onPlaybackStateChanged(int state) {
                    switch (state) {
                        case Player.STATE_ENDED:
                            Timber.tag(TAG).d("onPlayerStateChanged: Video ended.");
                            imagePlayPause.setVisibility(View.VISIBLE);
                            isVideoEnded = true;
                            break;
                        case Player.STATE_READY:
                            isVideoEnded = false;
                            imagePlayPause.setVisibility(View.GONE);
                            startProgress();
                            Timber.tag(TAG).d("onPlayerStateChanged: Ready to play.");
                            break;
                        default:
                            break;
                        case Player.STATE_BUFFERING:
                            Timber.tag(TAG).d("onPlayerStateChanged: STATE_BUFFERING.");
                            break;
                        case Player.STATE_IDLE:
                            Timber.tag(TAG).d("onPlayerStateChanged: STATE_IDLE.");
                            break;
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     *  loading thumbnails
     * */
    private void loadThumbnails() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                long diff = totalDuration / 8;
                final int[] sec = {1};
                for (ImageView img : imageViews) {
                    long interval = (diff * sec[0]) * 1000000;
                    Bitmap thumb = StorageUtils.getInstance(mContext).getFrameAtTime(originalFile, interval, 100, 110);
                    if (thumb != null) {
                        if (sec[0] < totalDuration)
                            sec[0]++;
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(getApplicationContext())
                                        .load(thumb)
                                        .centerCrop()
                                        .transition(DrawableTransitionOptions.withCrossFade(300))
                                        .into(img);
                            }
                        });
                    }
                }
            }
        });
    }

    private void setUpSeekBar() {
        seekbar.setVisibility(View.VISIBLE);
        txtStartDuration.setVisibility(View.VISIBLE);
        txtEndDuration.setVisibility(View.VISIBLE);

        seekbarController.setMaxValue(totalDuration).apply();
        seekbar.setMaxValue(totalDuration).apply();
        seekbar.setMaxStartValue((float) totalDuration).apply();
        if (trimType == 1) {
            seekbar.setFixGap(fixedGap).apply();
            lastMaxValue = totalDuration;
        } else if (trimType == 2) {
            seekbar.setMaxStartValue((float) minGap);
            seekbar.setGap(minGap).apply();
            lastMaxValue = totalDuration;
        } else if (trimType == 3) {
            seekbar.setMaxStartValue((float) maxToGap);
            seekbar.setGap(minFromGap).apply();
            lastMaxValue = maxToGap;
        } else {
            seekbar.setGap(2).apply();
            lastMaxValue = totalDuration;
        }
        if (hidePlayerSeek)
            seekbarController.setVisibility(View.GONE);

        seekbar.setOnRangeSeekbarFinalValueListener((minValue, maxValue) -> {
            if (!hidePlayerSeek)
                seekbarController.setVisibility(View.GONE);
        });

        seekbar.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
            long minVal = (long) minValue;
            long maxVal = (long) maxValue;
            if (lastMinValue != minVal) {
                seekTo((long) minValue);
                if (!hidePlayerSeek)
                    seekbarController.setVisibility(View.GONE);
            }
            lastMinValue = minVal;
            lastMaxValue = maxVal;
            txtStartDuration.setText(TrimmerUtils.formatSeconds(minVal));
            txtEndDuration.setText(TrimmerUtils.formatSeconds(maxVal));
            txtTrimDuration.setText(TrimmerUtils.formatSeconds(maxVal - minVal));
            if (trimType == 3)
                setDoneColor(minVal, maxVal);
        });

        seekbarController.setOnSeekbarFinalValueListener(value -> {
            long value1 = (long) value;
            if (value1 < lastMaxValue && value1 > lastMinValue) {
                seekTo(value1);
                return;
            }
            if (value1 > lastMaxValue)
                seekbarController.setMinStartValue((int) lastMaxValue).apply();
            else if (value1 < lastMinValue) {
                seekbarController.setMinStartValue((int) lastMinValue).apply();
                if (exoPlayer.getPlayWhenReady())
                    seekTo(lastMinValue);
            }
        });
    }

    /**
     * will be called whenever seekBar range changes
     * it checks max duration is exceed or not.
     * and disabling and enabling done menuItem
     *
     * @param minVal left thumb value of seekBar
     * @param maxVal right thumb value of seekBar
     */
    private void setDoneColor(long minVal, long maxVal) {
        try {
            if (menuDone == null)
                return;
            //changed value is less than maxDuration
            if ((maxVal - minVal) <= maxToGap) {
                menuDone.getIcon().setColorFilter(
                        new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorWhite)
                                , PorterDuff.Mode.SRC_IN)
                );
                isValidVideo = true;
            } else {
                menuDone.getIcon().setColorFilter(
                        new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorWhiteLt)
                                , PorterDuff.Mode.SRC_IN)
                );
                isValidVideo = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PER_REQ_CODE) {
            if (isPermissionOk(grantResults))
                setDataInView(false);
            else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        if (progressView != null && progressView.isShowing())
            progressView.dismiss();
        deleteFile("temp_file");
        stopRepeatingTask();
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuDone = menu.findItem(R.id.action_done);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            //prevent multiple clicks
            if (SystemClock.elapsedRealtime() - lastClickedTime < 800)
                return true;
            lastClickedTime = SystemClock.elapsedRealtime();
            trimVideo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void trimVideo() {
        Log.d(TAG, "uriiii: "+uri);
        String watermark = "android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.alhawana;
//        Config.ignoreSignal(Signal.SIGXCPU);
        if (isValidVideo) {
            File outputFile = StorageUtils.getInstance(mContext).getTempFile(mContext, Constants.TAG_VIDEO_AUDIO_TRIM + Utility.VIDEO_FORMAT);
//            Timber.tag(TAG).d("trimVideo: %s", outputFile.exists());
//            Timber.tag(TAG).d("trimVideo: %s", uri);
//            if (outputFile.exists())
            outputPath = outputFile.getAbsolutePath();

            exoPlayer.setPlayWhenReady(false);
            showProcessingDialog();
            String[] complexCommand;
            if (compressOption != null) {
                Log.d(TAG, "trimVideo1111: ");
                complexCommand = getCompressionCmd();
            }
            else if (isAccurateCut) {
                Log.d(TAG, "trimVideo2222: ");
                //no changes in video quality
                //faster trimming command and given duration will be accurate
                complexCommand = getAccurateCmd();
            } else {
                Log.d(TAG, "trimVideo3333: ");
                //no changes in video quality
                //fastest trimming command however, result duration
                //will be low accurate(2-3 secs)
                complexCommand = new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                        "-i", String.valueOf(uri),
                        "-i", watermark,
                        "-filter_complex",
                        "[0:v]pad=iw:if(lte(ih\\,iw)\\,ih\\,2*trunc(iw*16/9/2)):(ow-iw)/2:(oh-ih)/2[v0];[1:v][v0]scale2ref[v1][v0];[v0][v1]overlay=x=(W-w)/2:y=(H-h)/2[v]",
                        "-t",
                        TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
                        "-async", "1", "-strict", "-2", "-c", "copy", "-y", outputPath};


                Timber.tag(TAG).d("complexCommand: %s", complexCommand.toString());
                Log.d(TAG, "uriiii: "+uri);
            }
            Log.d(TAG, "trimVideo4444: ");
            execFFmpegBinary(complexCommand, true);
        } else
            Toast.makeText(this, getString(R.string.txt_smaller) + " " + TrimmerUtils.getLimitedTimeFormatted(maxToGap), Toast.LENGTH_SHORT).show();
    }
    private String getRealPathFromURI(Context context, Uri contentUri) {
        String result = null;
        String[] proj = {MediaStore.Video.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                result = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return result;
    }

   /* private String[] getCompressionCmd() {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(trimFile.getAbsolutePath());
        String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        int w = TrimmerUtils.clearNull(width).isEmpty() ? 0 : Integer.parseInt(width);
        int h = Integer.parseInt(height);
        int rotation = TrimmerUtils.getVideoRotation(this, trimFile);
        if (rotation == 90 || rotation == 270) {
            int temp = w;
            w = h;
            h = temp;
        }
        //Default compression option
        if (compressOption.getWidth() != 0 || compressOption.getHeight() != 0
                || !compressOption.getBitRate().equals("0k")) {
            return new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", String.valueOf(uri), "-s", compressOption.getWidth() + "x" +
                    compressOption.getHeight(),
                    "-r", String.valueOf(compressOption.getFrameRate()),
                    "-vcodec", "mpeg4", "-b:v",
                    compressOption.getBitRate(), "-b:a", "48000", "-ac", "2", "-ar",
                    "22050", "-t",
                    TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), "-y", outputPath};
        }
        //Dividing high resolution video by 2(ex: taken with camera)
        else if (w >= 800) {
            w = w / 2;
            h = Integer.parseInt(height) / 2;
            return new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", String.valueOf(uri),
                    "-s", w + "x" + h, "-r", "30",
                    "-vcodec", "mpeg4", "-b:v",
                    "1M", "-b:a", "48000", "-ac", "2", "-ar", "22050",
                    "-t",
                    TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), "-y", outputPath};
        } else {
            return new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", String.valueOf(uri), "-s", w + "x" + h, "-r",
                    "30", "-vcodec", "mpeg4", "-b:v",
                    "400K", "-b:a", "48000", "-ac", "2", "-ar", "22050",
                    "-t",
                    TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue), "-y", outputPath};
        }
    }
*/

    /*private String[] getCompressionCmd() {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(trimFile.getAbsolutePath());
        String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        int w = TrimmerUtils.clearNull(width).isEmpty() ? 0 : Integer.parseInt(width);
        int h = Integer.parseInt(height);
        int rotation = TrimmerUtils.getVideoRotation(this, trimFile);

        if (rotation == 90 || rotation == 270) {
            int temp = w;
            w = h;
            h = temp;
        }

        // Trimming time values
        String startTime = TrimmerUtils.formatCSeconds(lastMinValue);
        String duration = TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue);

        // ✅ Updated command
        return new String[]{
                "-y",
                "-ss", startTime,            // trim start
                "-i", String.valueOf(uri),
                "-t", duration,              // trim duration
                "-c:v", "libx264",           // modern high-quality encoder
                "-preset", "slow",           // quality preset
                "-crf", "18",                // visually lossless
                "-pix_fmt", "yuv420p",       // ensures wide compatibility & good color
                "-vf", "scale=" + w + ":" + h, // keep exact original resolution
                "-c:a", "aac",               // safe audio codec
                "-b:a", "192k",              // good audio bitrate
                "-movflags", "+faststart",
                outputPath
        };
    }
*/

    private String[] getCompressionCmd() {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(trimFile.getAbsolutePath());
        String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        int w = TrimmerUtils.clearNull(width).isEmpty() ? 0 : Integer.parseInt(width);
        int h = Integer.parseInt(height);
        int rotation = TrimmerUtils.getVideoRotation(this, trimFile);
        if (rotation == 90 || rotation == 270) {
            int temp = w;
            w = h;
            h = temp;
        }

        // If custom compression options provided
        if (compressOption.getWidth() != 0 || compressOption.getHeight() != 0
                || !compressOption.getBitRate().equals("0k")) {
            return new String[]{
                    "-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", String.valueOf(uri),
                    "-s", compressOption.getWidth() + "x" + compressOption.getHeight(),
                    "-r", String.valueOf(compressOption.getFrameRate()),
                    "-vcodec", "mpeg4",
                    "-b:v", compressOption.getBitRate(),
                    "-b:a", "48000", "-ac", "2", "-ar", "22050",
                    "-t", TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
                    "-y", outputPath
            };
        }
        // High-resolution (>=800px)
        else if (w >= 800) {
            w = w / 2;
            h = Integer.parseInt(height) / 2;
            return new String[]{
                    "-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", String.valueOf(uri),
                    "-s", w + "x" + h,
                    "-r", "30",
                    "-vcodec", "mpeg4",
                    "-b:v", "1M",
                    "-b:a", "48000", "-ac", "2", "-ar", "22050",
                    "-t", TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
                    "-y", outputPath
            };
        }
        // Low-resolution fallback (your case)
        else {
          /*  return new String[]{
                    "-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-to", TrimmerUtils.formatCSeconds(lastMaxValue),
                    "-i", String.valueOf(uri),
                    "-c", "copy",
                    "-avoid_negative_ts", "1",
                    "-y", outputPath
            };*/

          /*  return new String[]{
                    "-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-to", TrimmerUtils.formatCSeconds(lastMaxValue),
                    "-i", String.valueOf(uri),
                    "-vf", "scale=trunc(iw/2)*2:trunc(ih/2)*2",
                    "-r", "30",
                    "-c:v", "libx264",
                    "-preset", "medium",
                    "-crf", "20",
                    "-pix_fmt", "yuv420p",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-movflags", "+faststart",
                    "-y", outputPath
            };*/

           /* return new String[]{
                    "-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-to", TrimmerUtils.formatCSeconds(lastMaxValue),
                    "-i", String.valueOf(uri),
                    "-avoid_negative_ts", "make_zero",
                    "-c:v", "mpeg4",
                    "-qscale:v", "2",
                    "-pix_fmt", "yuv420p",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-movflags", "+faststart",
                    "-y", outputPath
            };*/
            return new String[]{
                    "-ss", TrimmerUtils.formatCSeconds(lastMinValue),
                    "-i", String.valueOf(uri),
                    "-s", w + "x" + h,
                    "-r", "30",
                    "-vcodec", "mpeg4",
                    "-b:v", "400K",
                    "-b:a", "48000", "-ac", "2", "-ar", "22050",
                    "-t", TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
                    "-y", outputPath
            };
        }
    }


    private void execFFmpegBinary(final String[] command, boolean retry) {
        FFmpegKit.executeAsync(String.join(" ", command), new FFmpegSessionCompleteCallback() {
            @Override
            public void apply(FFmpegSession session) {
                String logs = session.getAllLogsAsString();
                String errorLogs = session.getFailStackTrace();
                int returnCode = session.getReturnCode().getValue();
                Log.d(TAG, "FFmpeg Output: " + logs);
                Log.d(TAG, "FFmpeg Error: " + errorLogs);
                Log.d(TAG, "Return Code: " + returnCode);
                Log.d(TAG, "Command: " + Arrays.toString(command));
                Log.d(TAG, "outputPathhhh: " + outputPath);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ReturnCode.isSuccess(session.getReturnCode())) {
                            if (dialog.isShowing())
                                dialog.dismiss();
                            if (showFileLocationAlert)
                                showLocationAlert();
                            else {
                                if(launchedFromShare){
                                    launchedFromShare = false;
                                    Intent intent = new Intent(ActGifVideoEditor.this, PostVideoActivity.class);
                                    if (soundId != null) {
                                        intent.putExtra(Constants.TAG_SOUND_ID, soundId);
                                        Log.d(TAG, "gotoPostVideoPage: soundid "+soundId);
                                    }
                                    if (soundUrl != null) {
                                        intent.putExtra(Constants.TAG_SOUND_URL, soundUrl);
                                        Log.d(TAG, "gotoPostVideoPage: soundurl "+soundUrl);
                                    }
                                    intent.putExtra(Constants.TAG_VIDEO_ID, video_id);
                                    intent.putExtra(Constants.TAG_VIDEO_PATH, outputPath);
                                    intent.putExtra(Constants.TAG_SELECT_HASH_TAG, gethashTag);
                                    intent.putExtra(Constants.TAG_TYPE, "gallery");
                                    startActivity(intent);
                                    finish();
                                } else {
                                  /*  Intent intent = new Intent();
                                    intent.putExtra(TrimVideo.TRIMMED_VIDEO_PATH, outputPath);
                                    if (soundId != null) {
                                        intent.putExtra(Constants.TAG_SOUND_ID, soundId);
                                    }
                                    setResult(RESULT_OK, intent);
                                    finish();*/
                                    File outputFile = StorageUtils.getInstance(mContext)
                                            .getTempFile(mContext, "GIF_" + System.currentTimeMillis() + ".gif");

                                    gifOutputPath = outputFile.getAbsolutePath();

                                    createGIFfromTrimmedVideo(outputPath,gifOutputPath);
                                }
                            }
                        } else if (ReturnCode.isCancel(session.getReturnCode())) {
                            LogMessage.v("Command cancelled");
                            if (dialog.isShowing())
                                dialog.dismiss();
                        } else {
                            // Failed case:
                            // line 489 command fails on some devices in
                            // that case retrying with accurateCmt as alternative command
                            if (retry && !isAccurateCut && compressOption == null) {
//                        File newFile = new File(outputPath);
//                        if (newFile.exists())
//                            newFile.delete();
                                execFFmpegBinary(getAccurateCmd(), false);
                            } else {
                             /*   FFmpegKit.executeAsync(Arrays.toString(new String[]{"-encoders"}), new FFmpegSessionCompleteCallback() {
                                    @Override
                                    public void apply(FFmpegSession session) {
                                        Log.d("FFmpeg111111111", session.getOutput());
                                    }
                                });*/
                                File newFile = new File(outputPath);
                                if (newFile.exists())
                                    newFile.delete();
                                if (dialog.isShowing())
                                    dialog.dismiss();
                                Toast.makeText(ActGifVideoEditor.this, mContext.getString(R.string.txt_failed_to_trim), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }

           /* @Override
            public void apply(long executionId, int returnCode) {
                Timber.tag(TAG).d("executeAsync: %s, %s", executionId, returnCode);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (returnCode == 0) {
                            if (dialog.isShowing())
                                dialog.dismiss();
                            if (showFileLocationAlert)
                                showLocationAlert();
                            else {
                                Intent intent = new Intent();
                                intent.putExtra(TrimVideo.TRIMMED_VIDEO_PATH, outputPath);
                                if (soundId != null) {
                                    intent.putExtra(Constants.TAG_SOUND_ID, soundId);
                                }
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        } else if (returnCode == 255) {
                            LogMessage.v("Command cancelled");
                            if (dialog.isShowing())
                                dialog.dismiss();
                        } else {
                            // Failed case:
                            // line 489 command fails on some devices in
                            // that case retrying with accurateCmt as alternative command
                            if (retry && !isAccurateCut && compressOption == null) {
//                        File newFile = new File(outputPath);
//                        if (newFile.exists())
//                            newFile.delete();
                                execFFmpegBinary(getAccurateCmd(), false);
                            } else {
                                File newFile = new File(outputPath);
                                if (newFile.exists())
                                    newFile.delete();
                                if (dialog.isShowing())
                                    dialog.dismiss();
                                Toast.makeText(ActGifVideoEditor.this, mContext.getString(R.string.txt_failed_to_trim), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }*/
        });
       /* mExecutorService.execute(new Runnable() {
            @Override
            public void run() {

            }
        });*/
    }

    private void showLocationAlert() {
        // dialog to ask user to open file location in file manager or not
        AlertDialog openFileLocationDialog = new AlertDialog.Builder(ActGifVideoEditor.this).create();
        openFileLocationDialog.setTitle(getString(R.string.open_file_location));
        openFileLocationDialog.setCancelable(true);

        // when user click yes
        openFileLocationDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes_txt), (dialogInterface, i) -> {
            // open file location
            Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uriFile = Uri.parse(outputPath);
            chooser.addCategory(Intent.CATEGORY_OPENABLE);
            chooser.setDataAndType(uriFile, "*/*");
            startActivity(chooser);
        });

        // when user click no and finish current activity
        openFileLocationDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no_txt), (dialogInterface, i) -> openFileLocationDialog.dismiss());

        // when user click no and finish current activity
        openFileLocationDialog.setOnDismissListener(dialogInterface -> {
            if(launchedFromShare){
                launchedFromShare = false;
                Intent intent = new Intent(ActGifVideoEditor.this, PostVideoActivity.class);
                if (soundId != null) {
                    intent.putExtra(Constants.TAG_SOUND_ID, soundId);
                    Log.d(TAG, "gotoPostVideoPage: soundid "+soundId);
                }
                if (soundUrl != null) {
                    intent.putExtra(Constants.TAG_SOUND_URL, soundUrl);
                    Log.d(TAG, "gotoPostVideoPage: soundurl "+soundUrl);
                }
                intent.putExtra(Constants.TAG_VIDEO_ID, video_id);
                intent.putExtra(Constants.TAG_VIDEO_PATH, outputPath);
                intent.putExtra(Constants.TAG_SELECT_HASH_TAG, gethashTag);
                intent.putExtra(Constants.TAG_TYPE, "gallery");
                startActivity(intent);
                finish();
            }  else {
                Intent intent = new Intent();
                intent.putExtra(TrimVideo.TRIMMED_VIDEO_PATH, outputPath);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        openFileLocationDialog.show();
    }

//    private String[] getAccurateCmd() {
//        /*String[] command = new String[]{"-ss", TrimmerUtils.formatCSeconds(lastMinValue),
//                "-i", String.valueOf(uri), "-t",
//                TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
//                "-async", "1", "-y", outputPath};*/
//        /*Drawable drawable = ContextCompat.getDrawable(this, R.drawable.app_logo_white);
//        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
//        *//*ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.setHasAlpha(true);
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);*//*
//        File watermark = StorageUtils.getInstance(this).saveToCacheDir(bitmap, "Watermark.png");*/
//        List<String> command = new ArrayList<>();
//        command.add("-ss");
//        command.add(TrimmerUtils.formatCSeconds(lastMinValue));
//        command.add("-i");
////        command.add(String.valueOf(uri));
//                command.add("\"" + uri.toString().trim() + "\""); // Wrap input video path
//
//        command.add(uri.toString().trim());
//        if (Constants.WATERMARK_ENABLED) {
//            String watermarkUrl = "https://products.hitasoft.in/fundooUpdate/public/video/convert/30_1660805034text_logo.png";
//            command.add("-i");
//            command.add(watermarkUrl);
//            command.add("-filter_complex");
//            command.add("[1][0]scale2ref=w=oh*mdar:h=ih*0.07[logo][video];[logo]format=argb,colorchannelmixer=aa=0.4[logo transparent];[video][logo transparent]overlay=main_w-overlay_w-5:main_h-overlay_h-5:format=auto,format=yuv420p");
//        }
//        command.add("-t");
//        command.add(TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue));
//        command.add("-c:v");
//        command.add("mpeg4");
//        command.add("-preset");
//        command.add("veryfast");
//        command.add("-crf");
//        command.add("23");
//        command.add("-async");
//        command.add("1");
//        command.add("-y");
////        command.add(outputPath);
//                command.add("\"" + outputPath + "\""); // Wrap output path
//
//
    ////        Timber.tag(TAG).d("getAccurateCmd: %s", Arrays.toString(command));
//        Timber.tag(TAG).d("getAccurateCmd: %s", Arrays.toString(command.toArray()));
//        return command.toArray(new String[command.size()]);
//    }
    private String[] getAccurateCmd() {
        List<String> command = new ArrayList<>();

        if (uri == null || uri.toString().isEmpty()) {
            Timber.e("Video URI is null or empty!");
            return null;
        }

        command.add("-ss");
        command.add(TrimmerUtils.formatCSeconds(lastMinValue));
        command.add("-i");
        command.add(trimFile != null ? trimFile.getAbsolutePath() : uri.toString().trim());
        if (Constants.WATERMARK_ENABLED) {
            String watermarkUrl = "https://products.hitasoft.in/fundooUpdate/public/video/convert/30_1660805034text_logo.png";
            command.add("-i");
            command.add(watermarkUrl);
            command.add("-filter_complex");
            command.add("[1][0]scale2ref=w=oh*mdar:h=ih*0.07[logo][video];[logo]format=argb,colorchannelmixer=aa=0.4[logo transparent];[video][logo transparent]overlay=main_w-overlay_w-5:main_h-overlay_h-5:format=auto,format=yuv420p");
        }

        command.add("-t");
        command.add(TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue));
        command.add("-c");
        command.add("copy");
        command.add("-y");
        command.add(outputPath);
        if (command.isEmpty()) {
            Timber.e("Generated FFmpeg command is empty!");
            return null;
        }

        Timber.tag(TAG).d("getAccurateCmd: %s", Arrays.toString(command.toArray()));
        return command.toArray(new String[0]);
    }

    private void showProcessingDialog() {
        try {
            dialog = new Dialog(this);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.alert_convert);
            TextView txtCancel = dialog.findViewById(R.id.txt_cancel);
            dialog.setCancelable(false);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            txtCancel.setOnClickListener(v -> {
                dialog.dismiss();
                FFmpegKit.cancel();
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissProcessingDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

//    private boolean checkStoragePermission() {
//        return PermissionsUtils.checkStoragePermission(mContext);
//    }

    private boolean checkPermission(String... permissions) {
        boolean allPermitted = false;
        for (String permission : permissions) {
            allPermitted = (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED);
            if (!allPermitted)
                break;
        }
        if (allPermitted)
            return true;
        ActivityCompat.requestPermissions(this, permissions,
                PER_REQ_CODE);
        return false;
    }

    private boolean isPermissionOk(int... results) {
        boolean isAllGranted = true;
        for (int result : results) {
            if (PackageManager.PERMISSION_GRANTED != result) {
                isAllGranted = false;
                break;
            }
        }
        return isAllGranted;
    }

    void startProgress() {
        updateSeekbar.run();
    }

    void stopRepeatingTask() {
        seekHandler.removeCallbacks(updateSeekbar);
    }
}
