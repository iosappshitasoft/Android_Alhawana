package com.app.alhawanajd;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daasuu.gpuv.camerarecorder.CameraRecordListener;
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorder;
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorderBuilder;
import com.daasuu.gpuv.camerarecorder.LensFacing;
import com.daasuu.gpuv.egl.filter.GlBrightnessFilter;
import com.daasuu.gpuv.egl.filter.GlFilter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.app.alhawanajd.BuildConfig;
import com.app.alhawanajd.R;
import com.app.external.video_trimmer.utils.TrimType;
import com.app.external.video_trimmer.utils.TrimVideo;
import com.app.alhawanajd.Video.FilterType;
import com.app.alhawanajd.Video.ImageFilterType;
import com.app.alhawanajd.Video.PostVideoActivity;
import com.app.alhawanajd.Video.UploadSoundActivity;
import com.app.helper.CountDownAnimation;
import com.app.helper.NetworkReceiver;
import com.app.helper.StorageUtils;
import com.app.model.GetSet;
import com.app.utils.AdminData;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Objects;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import es.dmoral.toasty.Toasty;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import timber.log.Timber;

public class CameraActivity extends BaseFragmentActivity implements CountDownAnimation.CountDownListener {

    private static final String TAG = CameraActivity.class.getSimpleName();
    private Context mContext;
    private boolean toggleClick = false;

    GLSurfaceView sampleGLView;
    public GPUCameraRecorder gpuCameraRecorder;

    FrameLayout frameLayout;
    private LinearLayout soundLay, gallaryLay, cameraOptions;

    ConstraintLayout BeforeVideoRecordingLayout;
    private ShapeableImageView flashIcon;
    private RelativeLayout recordLayout, filterRecLay, videoRecordingLayout;
    String getRecordTime, ChangeRecordTime = "";
    public int i = 0;
    protected LensFacing lensFacing = LensFacing.BACK;
    Boolean isFlash = false, isRecord = false;
    /*
     * protected int cameraWidth = 1280;
     * protected int cameraHeight = 720;
     * protected int videoWidth = 720;
     * protected int videoHeight = 720;
     */
    private CountDownTimer countDownTimer;
    private ProgressBar progress_bar;
    RecyclerView filterRecyclerview;
    FilterAdapter filterAdapter;
    private int isSelectedPosition = -1;
    File inputVideoFile;
    GPUImageView imageView2;
    Bitmap icon;

    private CountDownAnimation countDownAnimation;
    private Boolean isCountDownRunning = false;
    TextView textView;

    private String soundId = null, soundUrl = null, video_id = null, trimmedVideo = null;
    GlFilter setfilter;
    String getSound_title, getSound_isfav, getSound_cover, gethashTag;

    ShapeableImageView cameraPageCloseButton;
    private BottomSheetDialog reShootDialog;
    ProgressDialog pdLoading;
    private ActivityResultLauncher<Intent> galleryResultLauncher;
    private ActivityResultLauncher<Intent> audioResultLauncher;
    private ActivityResultLauncher<Intent> trimResultLauncher;
    private ActivityResultLauncher<Intent> postResultLauncher;
    private boolean isCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        mContext = this;
        pdLoading = new ProgressDialog(CameraActivity.this, R.style.CameraAlertDialog);
        // Previous files delete.
        StorageUtils.getInstance(this).clearTempCachee();
        initActivityResultLauncher();

        // Video File Path
        inputVideoFile = new File(StorageUtils.getInstance(this).getExtCacheDir(),
                Constants.TAG_VIDEO_TRIM + Utility.VIDEO_FORMAT);
        // Get Sound Files
        Bundle extras = getIntent().getBundleExtra(Constants.TAG_SOUND_DATA);
        // Get HashTag
        gethashTag = getIntent().getStringExtra(Constants.TAG_SELECT_HASH_TAG);

        if (extras != null) {
            soundId = extras.getString(Constants.TAG_SOUND_ID);
            getSound_title = extras.getString(Constants.TAG_SOUND_TITLE);
            soundUrl = extras.getString(Constants.TAG_SOUND_URL);
            getSound_isfav = extras.getString(Constants.TAG_SOUND_IS_FAV);
            getSound_cover = extras.getString(Constants.TAG_SOUND_COVER);
            ChangeRecordTime = extras.getString(Constants.TAG_SOUND_DURATION);
            ChangeRecordTime = AppUtils.convertToSeconds(ChangeRecordTime); // Convert to seconds
        }

        imageView2 = new GPUImageView(this);
        initView();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {

        frameLayout = findViewById(R.id.wrap_view);
        soundLay = findViewById(R.id.soundLay);
        gallaryLay = findViewById(R.id.gallaryLay);
        LinearLayout flipLay = findViewById(R.id.flipLay);
        LinearLayout filterLay = findViewById(R.id.filterLay);
        LinearLayout timerLay = findViewById(R.id.timerLay);
        LinearLayout flashLay = findViewById(R.id.flashLay);
        flashIcon = findViewById(R.id.flashIcon);
        ShapeableImageView recordIcon = findViewById(R.id.recordIcon);
        progress_bar = findViewById(R.id.progress_bar);
        cameraOptions = findViewById(R.id.cameraOptions);
        filterRecyclerview = findViewById(R.id.filterRecyclerview);
        recordLayout = findViewById(R.id.recordLayout);
        BeforeVideoRecordingLayout = findViewById(R.id.BeforeVideoRecordingLayout);
        filterRecLay = findViewById(R.id.filterRecLay);
        ImageView filterClose = findViewById(R.id.filterClose);
        textView = findViewById(R.id.textView);
        videoRecordingLayout = findViewById(R.id.videoRecordingLayout);
        ShapeableImageView tickRecord = findViewById(R.id.tickRecord);
        ShapeableImageView stopRecordIcon = findViewById(R.id.stopRecordIcon);
        cameraPageCloseButton = findViewById(R.id.cameraPageCloseButton);
        icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.filter_sample_image);
        filterRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        filterAdapter = new FilterAdapter(this);
        filterRecyclerview.setAdapter(filterAdapter);
        cameraOptions.setVisibility(View.VISIBLE);
        soundLay.setVisibility(View.GONE);
        gallaryLay.setVisibility(View.VISIBLE);
        videoRecordingLayout.setVisibility(View.GONE);
        recordLayout.setVisibility(View.VISIBLE);
        initCountDownAnimation();

        GlBrightnessFilter glBrightnessFilter = new GlBrightnessFilter();
        glBrightnessFilter.setBrightness(0.1f);
        setfilter = glBrightnessFilter;

        stopRecordIcon.setOnClickListener(v -> {
            App.preventMultipleClick(stopRecordIcon);
            stopRecording();
        });

        tickRecord.setOnClickListener(v -> {
            App.preventMultipleClick(tickRecord);
            stopRecording();
        });

        soundLay.setOnClickListener(v -> {
            App.preventMultipleClick(soundLay);
            Intent intent = new Intent(this, UploadSoundActivity.class);
            intent.putExtra(Constants.TAG_SOUND_ID, soundId);
            audioResultLauncher.launch(intent);
        });

        /*
         * gallaryLay.setOnClickListener(v -> {
         * App.preventMultipleClick(gallaryLay);
         * if (AdminData.galleryUpload) {
         * launchGallery();
         * } else if (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER,
         * "false").equals("true")) {
         * launchGallery();
         * } else {
         * App.makeToast(getApplicationContext().getString(R.string.prime_alert));
         * Intent intent = new Intent(this, PrimeActivity.class);
         * intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
         * Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         * startActivity(intent);
         * }
         * 
         * });
         */

        gallaryLay.setOnClickListener(v -> {
            App.preventMultipleClick(gallaryLay);
            if (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false").equals("false")
                    && !AdminData.galleryUpload) {
                // launchGallery();
                FilePickerBuilder.getInstance()
                        .setMaxCount(4)
                        .setActivityTheme(R.style.MainTheme13)
                        .setActivityTitle(getString(R.string.please_select_media))
                        .enableVideoPicker(true)
                        .enableImagePicker(false)
                        .enableCameraSupport(false)
                        .showGifs(false)
                        .showFolderView(false)
                        .enableSelectAll(false)
                        .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .pickPhoto(this, galleryResultLauncher);

            } else {
                App.makeToast(getApplicationContext().getString(R.string.prime_alert));
                Intent intent = new Intent(this, PrimeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }

        });

        flashLay.setOnClickListener(v -> {

            if (gpuCameraRecorder != null && gpuCameraRecorder.isFlashSupport()) {
                gpuCameraRecorder.switchFlashMode();
                gpuCameraRecorder.changeAutoFocus();
                if (!isFlash) {
                    isFlash = true;
                    flashIcon.setImageDrawable(getResources().getDrawable(R.drawable.flashon));
                } else {
                    isFlash = false;
                    flashIcon.setImageDrawable(getResources().getDrawable(R.drawable.flashoff));
                }
            } else
                Toasty.info(CameraActivity.this, R.string.flash_not_support, Toasty.LENGTH_SHORT).show();
        });
        timerLay.setOnClickListener(v -> {
            App.preventMultipleClick(timerLay);
            if (!isCountDownRunning) {
                cameraOptions.setVisibility(View.GONE);
                recordLayout.setVisibility(View.GONE);
                startCountDownAnimation();
            }
        });
        filterLay.setOnClickListener(v -> {
            App.preventMultipleClick(filterLay);
            cameraOptions.setVisibility(View.GONE);
            recordLayout.setVisibility(View.GONE);
            filterRecLay.setVisibility(View.VISIBLE);
        });
        filterClose.setOnClickListener(v -> {
            filterRecLay.setVisibility(View.GONE);
            cameraOptions.setVisibility(View.VISIBLE);
            recordLayout.setVisibility(View.VISIBLE);
        });
        flipLay.setOnClickListener(v -> {
            releaseCamera();
            if (lensFacing == LensFacing.BACK) {
                lensFacing = LensFacing.FRONT;
                setfilter = FilterType.gpuVideoView(0, this);
            } else {
                lensFacing = LensFacing.BACK;
                setfilter = FilterType.gpuVideoView(0, this);

            }
            toggleClick = true;
        });
        recordIcon.setOnClickListener(v -> {
            App.preventMultipleClick(recordIcon);
            findViewById(R.id.stopRecordIcon).setEnabled(false);
            findViewById(R.id.tickRecord).setEnabled(false);
            findViewById(R.id.stopRecordIcon).setAlpha(0.3f);
            findViewById(R.id.tickRecord).setAlpha(0.3f);
            recordVideo();
        });

        cameraPageCloseButton.setOnClickListener(v -> backPressed());
    }

    private void launchGallery() {
        if (!isRecord) {
            Uri collection;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                Log.d(TAG, "launchGallery_collection: " + collection);
            } else {
                collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            }
            Intent intent = new Intent();
            intent.setDataAndType(collection, "video/*");
            intent.setAction(Intent.ACTION_PICK);
            galleryResultLauncher.launch(Intent.createChooser(intent, "Select Video"));
        }
    }

    private void initActivityResultLauncher() {
        galleryResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            ArrayList<Uri> selectedPhotos = result.getData()
                                    .getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                            if (selectedPhotos != null && !selectedPhotos.isEmpty()) {
                                TrimVideo.activity(String.valueOf(selectedPhotos.get(0)))
                                        .setTrimType(TrimType.MIN_MAX_DURATION)
                                        .setCompressOption(null)
                                        .setSoundId(soundId != null ? soundId : null)
                                        .setSoundUrl(soundUrl != null ? soundUrl : null)
                                        .setAccurateCut(true)
                                        .setMediaFrom("gallery")
                                        .setMinToMax(Long.parseLong("5"), (long) AdminData.max_video_duration)
                                        .start(CameraActivity.this, trimResultLauncher);
                            } else {
                                Toast.makeText(CameraActivity.this, "No media selected", Toast.LENGTH_SHORT).show();
                            }
                            // Timber.tag(TAG).d("galleryResultLauncher: %s", result.getData().getData());
                            // Timber.d("galleryResultLauncher: %s", soundId);
                            // Timber.d("galleryResultLauncher: %s", soundUrl);

                        }
                    }
                });
        audioResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            if (result.getData() != null) {
                                Intent data = result.getData();
                                soundId = data.getStringExtra(Constants.TAG_SOUND_ID);
                                soundUrl = data.getStringExtra(Constants.TAG_SOUND_URL);
                                ChangeRecordTime = AppUtils
                                        .convertToSeconds(data.getStringExtra(Constants.TAG_SOUND_DURATION));
                                Timber.i("audioResultLauncher: %s, %s", soundId, soundUrl);
                            }

                        }
                    }
                });
        postResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // Timber.tag(TAG).d("postResultLauncher: %d", result.getResultCode());
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            /*
                             * Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                             * startActivity(intent);
                             * finish();
                             */
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            if (result.getData() != null && result.getData().getBooleanExtra("reshoot", false)) {
                                if (result.getData().hasExtra(Constants.TAG_SOUND_ID)) {
                                    soundId = result.getData().getStringExtra(Constants.TAG_SOUND_ID);
                                }
                                if (result.getData().hasExtra(Constants.TAG_SOUND_URL)) {
                                    soundUrl = result.getData().getStringExtra(Constants.TAG_SOUND_URL);
                                }
                                Timber.d("postResultLauncher: %s, %s", soundId, soundUrl);
                                reShoot();
                            } else {
                                finish();
                            }
                        }
                    }
                });
        trimResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            if (result.getData() != null) {
                                if (isCamera) {
                                    if (result.getData().hasExtra(Constants.TAG_SOUND_ID)) {
                                        soundId = result.getData().getStringExtra(Constants.TAG_SOUND_ID);
                                    }
                                    if (result.getData().hasExtra(Constants.TAG_SOUND_URL)) {
                                        soundUrl = result.getData().getStringExtra(Constants.TAG_SOUND_URL);
                                    }
                                    trimmedVideo = result.getData().getStringExtra(TrimVideo.TRIMMED_VIDEO_PATH);
                                    /*
                                     * File srcFile = new File(trimmedVideo);
                                     * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                     * StorageUtils.getInstance(mContext).saveFileInStorageV10(srcFile,
                                     * srcFile.getName(), Constants.TAG_VIDEO);
                                     * } else {
                                     * StorageUtils.getInstance(mContext).saveFileInStorage(srcFile,
                                     * Constants.TAG_VIDEO, Constants.TAG_VIDEO);
                                     * }
                                     */
                                    gotoPostVideoPage("camera");
                                } else {
                                    if (result.getData().hasExtra(Constants.TAG_SOUND_ID)) {
                                        soundId = result.getData().getStringExtra(Constants.TAG_SOUND_ID);
                                    }
                                    if (result.getData().hasExtra(Constants.TAG_SOUND_URL)) {
                                        soundUrl = result.getData().getStringExtra(Constants.TAG_SOUND_URL);
                                    }
                                    trimmedVideo = result.getData().getStringExtra(TrimVideo.TRIMMED_VIDEO_PATH);
                                    /*
                                     * File srcFile = new File(trimmedVideo);
                                     * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                     * StorageUtils.getInstance(mContext).saveFileInStorageV10(srcFile,
                                     * srcFile.getName(), Constants.TAG_VIDEO);
                                     * } else {
                                     * StorageUtils.getInstance(mContext).saveFileInStorage(srcFile,
                                     * Constants.TAG_VIDEO, Constants.TAG_VIDEO);
                                     * }
                                     */
                                    gotoPostVideoPage("gallery");
                                }
                            }

                        }
                    }
                });
    }

    public void deleteDirectory(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].isDirectory()) {
                            deleteDirectory(files[i]);
                        } else {
                            files[i].delete();
                        }
                    }
                }
            }
            file.delete();
        }
    }

    private void openReshootDialog() {

        View reShoot_sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_reshoot, null);
        reShootDialog = new BottomSheetDialog(CameraActivity.this, R.style.reshootBottomDialog); // Style here
        reShootDialog.setContentView(reShoot_sheetView);
        reShootDialog.getBehavior().setHideable(false);

        TextView textView = reShoot_sheetView.findViewById(R.id.title);
        textView.setText(R.string.alert_close_this_video);

        reShoot_sheetView.findViewById(R.id.reshoot).setOnClickListener(v -> {

            if (reShootDialog != null && reShootDialog.isShowing()) {
                if (gpuCameraRecorder != null)
                    gpuCameraRecorder.stop();
                isRecord = false;
                timerCancel();
            }
        });

        reShoot_sheetView.findViewById(R.id.txtcancel).setOnClickListener(v -> reShootDialog.dismiss());
        reShoot_sheetView.findViewById(R.id.exit).setOnClickListener(v -> finish());

        reShootDialog.show();
    }

    private void stopRecording() {
        try {
            timerCancel();
            if (gpuCameraRecorder != null) {
                gpuCameraRecorder.stop();
            }
            isRecord = false;
        } catch (Exception e) {
            Timber.i("Exception %s", e.getMessage());
        }
    }

    private void gotoPostVideoPage(String uploadVideoType) {
        if (reShootDialog != null)
            reShootDialog.dismiss();
        Intent intent = new Intent(CameraActivity.this, PostVideoActivity.class);
        if (soundId != null) {
            intent.putExtra(Constants.TAG_SOUND_ID, soundId);
            Log.d(TAG, "gotoPostVideoPage: soundid " + soundId);
        }
        if (soundUrl != null) {
            intent.putExtra(Constants.TAG_SOUND_URL, soundUrl);
            Log.d(TAG, "gotoPostVideoPage: soundurl " + soundUrl);
        }
        intent.putExtra(Constants.TAG_VIDEO_ID, video_id);
        if (trimmedVideo != null) {
            intent.putExtra(Constants.TAG_VIDEO_PATH, trimmedVideo);
        }
        intent.putExtra(Constants.TAG_SELECT_HASH_TAG, gethashTag);
        intent.putExtra(Constants.TAG_TYPE, uploadVideoType);
        postResultLauncher.launch(intent);
        overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }

    private void initCountDownAnimation() {
        countDownAnimation = new CountDownAnimation(textView, 5);
        countDownAnimation.setCountDownListener(this);
    }

    @Override
    public void onCountDownEnd(CountDownAnimation animation) {
        isCountDownRunning = false;
        if (!isRecord) {
            recordVideo();
        }
    }

    private void startCountDownAnimation() {
        Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
                0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        countDownAnimation.setAnimation(animationSet);
        countDownAnimation.setStartCount(5);
        countDownAnimation.start();
        isCountDownRunning = true;
    }

    private void recordVideo() {
        if (gpuCameraRecorder != null) {
            getRecordTime = "" + GetSet.getMax_video_duration();
            inputVideoFile = new File(StorageUtils.getInstance(this).getExtCacheDir(),
                    Constants.TAG_VIDEO_TRIM + Utility.VIDEO_FORMAT);
            gpuCameraRecorder.start(inputVideoFile.getAbsolutePath());
            cameraOptions.setVisibility(View.GONE);
            soundLay.setVisibility(View.GONE);
            gallaryLay.setVisibility(View.GONE);
            isRecord = true;
            recordLayout.setVisibility(View.VISIBLE);
            BeforeVideoRecordingLayout.setVisibility(View.GONE);
            videoRecordingLayout.setVisibility(View.VISIBLE);
        }
    }

    private Boolean checkFilePath(File filepath) {
        if (filepath != null)
            return filepath.exists();
        return false;
    }

    private void timerStart() {
        i = 0;
        long conSec = Long.parseLong(getRecordTime) * 1000;
        progress_bar.setMax(Integer.parseInt(getRecordTime));
        Timber.d("timerStart: getRec max %s", Integer.parseInt(getRecordTime));
        countDownTimer = new CountDownTimer(conSec, 1000) {
            public void onTick(long millisUntilFinished) {
                i++;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    progress_bar.setProgress((int) (i * Integer.parseInt(getRecordTime) / (conSec / 1000)), true);
                else
                    progress_bar.setProgress((int) (i * Integer.parseInt(getRecordTime) / (conSec / 1000)));

                if (i == 3) {
                    findViewById(R.id.stopRecordIcon).setAlpha(1f);
                    findViewById(R.id.tickRecord).setAlpha(1f);
                    findViewById(R.id.stopRecordIcon).setEnabled(true);
                    findViewById(R.id.tickRecord).setEnabled(true);
                }
            }

            public void onFinish() {
                stopRecording();
            }
        }.start();
    }

    private void timerCancel() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            progress_bar.setProgress(0);
            progress_bar.invalidate();
        }
    }

    private void releaseCamera() {
        try {

            if (sampleGLView != null) {
                sampleGLView.onPause();
            }
            if (gpuCameraRecorder != null) {
                gpuCameraRecorder.stop();
                gpuCameraRecorder.release();
                gpuCameraRecorder = null;
            }
            if (sampleGLView != null) {
                ((FrameLayout) findViewById(R.id.wrap_view)).removeView(sampleGLView);
                sampleGLView = null;
            }
        } catch (Exception e) {
            Timber.i("exception %s", e.getMessage());
        }

    }

    private void initCameraView() {
        boolean hasPermissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermissions = ActivityCompat.checkSelfPermission(getApplicationContext(),
                    CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            hasPermissions = ActivityCompat.checkSelfPermission(getApplicationContext(),
                    CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        }

        if (hasPermissions) {
            frameLayout.removeAllViews();
            sampleGLView = null;
            sampleGLView = new GLSurfaceView(CameraActivity.this);
            frameLayout.addView(sampleGLView);
            frameLayout.setFitsSystemWindows(true);

            gpuCameraRecorder = new GPUCameraRecorderBuilder(CameraActivity.this, sampleGLView)
                    .cameraRecordListener(new CameraRecordListener() {
                        @Override
                        public void onGetFlashSupport(boolean flashSupport) {
                        }

                        @Override
                        public void onRecordComplete() {

                        }

                        @Override
                        public void onRecordStart() {
                            timerStart();
                        }

                        @Override
                        public void onError(Exception exception) {
                        }

                        @Override
                        public void onCameraThreadFinish() {
                            if (toggleClick) {
                                CameraActivity.this.runOnUiThread(() -> {
                                    initCameraView();
                                });
                            }
                            toggleClick = false;
                        }

                        @Override
                        public void onVideoFileReady() {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                public void run() {
                                    videoRecordingLayout.setVisibility(View.GONE);
                                    recordLayout.setVisibility(View.VISIBLE);
                                    BeforeVideoRecordingLayout.setVisibility(View.VISIBLE);
                                    cameraOptions.setVisibility(View.VISIBLE);
                                    soundLay.setVisibility(View.GONE);
                                    gallaryLay.setVisibility(View.VISIBLE);
                                    progress_bar.setProgress(0);

                                    if (!isFinishing()) {
                                        if (reShootDialog != null && reShootDialog.isShowing()) {
                                            timerCancel();
                                            reShootDialog.dismiss();
                                        } else {
                                            isCamera = true;
                                            TrimVideo.activity(inputVideoFile.getAbsolutePath())
                                                    .setTrimType(TrimType.MIN_MAX_DURATION)
                                                    .setAccurateCut(true)
                                                    .setCompressOption(null)
                                                    .setSoundId(soundId != null ? soundId : null)
                                                    .setSoundUrl(soundUrl != null ? soundUrl : null)
                                                    .setMediaFrom("camera")
                                                    .setMinToMax(Long.parseLong("5"), AdminData.max_video_duration)
                                                    .start(CameraActivity.this, trimResultLauncher);
                                        }
                                    }
                                }
                            });
                        }
                    })
                    .lensFacing(lensFacing)
                    .mute(false)
                    .filter(isSelectedPosition == -1 ? setfilter : FilterType.gpuVideoView(isSelectedPosition, this))
                    .build();
        }

    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        Timber.i("onNetworkChanged %s", isConnected);
        if (!isConnected) {
            soundId = null;
            Toasty.error(Objects.requireNonNull(this), R.string.internet_disturb, Toasty.LENGTH_SHORT).show();
            pdLoading.dismiss();
        }

    }

    private void backPressed() {
        if (!isRecord) {
            overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_down);
            finish();
        } else
            openReshootDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();

        App.setCurrentActivity(this);

        overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_stay);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkRecordPermissions13();
        } else {
            checkRecordPermissions();
        }
        if (!NetworkReceiver.isConnected()) {
            onNetworkChanged(false);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        if (gpuCameraRecorder != null) {
            gpuCameraRecorder.stop();
            gpuCameraRecorder.release();
            gpuCameraRecorder = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        timerCancel();
        if (sampleGLView != null) {
            frameLayout.removeView(sampleGLView);
            sampleGLView = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String @NotNull [] permissions, int @NotNull [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.CAMERA_REQUEST_CODE:
                boolean permissionGranted = true;
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = false;
                        break;
                    }
                }
                if (permissionGranted) {
                    initCameraView();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (shouldShowRequestPermissionRationale(CAMERA)
                                && shouldShowRequestPermissionRationale(RECORD_AUDIO)) {
                            requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA)
                                && shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)
                                && shouldShowRequestPermissionRationale(RECORD_AUDIO)) {
                            requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                        } else {

                            boolean isCamera = true, isStorage = true, isRecord = true;
                            for (String permission : permissions) {
                                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                        permission) != PackageManager.PERMISSION_GRANTED) {
                                    if (permission.equals("android.permission.CAMERA"))
                                        isCamera = false;
                                    if (permission.equals("android.permission.WRITE_EXTERNAL_STORAGE "))
                                        isStorage = false;
                                    if (permission.equals("android.permission.RECORD_AUDIO"))
                                        isRecord = false;
                                }
                            }
                            if (!isCamera || !isStorage || !isRecord)
                                App.makeToast(getString(R.string.require_permission));

                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivity(i);
                        }
                    }
                }
                break;
        }
    }

    private boolean checkPermissions(String[] permissionList, CameraActivity activity) {
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

    private boolean shouldShowRationale(String[] permissions, CameraActivity activity) {
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

    public static Bitmap drawableToBitmap(@NonNull Drawable d) {
        Bitmap bitmap;
        if (d instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
            if (bitmapDrawable.getBitmap() != null)
                return bitmapDrawable.getBitmap();
        }
        if (d.getIntrinsicWidth() <= 0 || d.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);
        return bitmap;
    }

    private void reShoot() {
        try {
            progress_bar.setProgress(0);
            videoRecordingLayout.setVisibility(View.GONE);
            recordLayout.setVisibility(View.VISIBLE);
            BeforeVideoRecordingLayout.setVisibility(View.VISIBLE);
            cameraOptions.setVisibility(View.VISIBLE);
            soundLay.setVisibility(View.GONE);
            gallaryLay.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Timber.i("view exception %s", e.getMessage());
        }
    }

    private void checkRecordPermissions() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ||
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {
                    CAMERA, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO },
                    Constants.CAMERA_REQUEST_CODE);
        } else
            initCameraView();
    }

    private void checkRecordPermissions13() {
        Log.e("TAG", "checkRecordPermissions: ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED
                    ||
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getApplicationContext(),
                            READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getApplicationContext(),
                            READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getApplicationContext(),
                            READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "onRequestPermissionsResult13:Tif ");

                ActivityCompat.requestPermissions(CameraActivity.this, new String[] {
                        CAMERA, READ_MEDIA_AUDIO, READ_MEDIA_IMAGES, RECORD_AUDIO, READ_MEDIA_VIDEO },
                        Constants.CAMERA_REQUEST_CODE);
            } else {
                Log.d("TAG", "onRequestPermissionsResult:else ");
                initCameraView();
            }

        }

    }

    private class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {
        Context mContext;

        public FilterAdapter(Context context) {
            this.mContext = context;

            if (isSelectedPosition == -1)
                isSelectedPosition = 0;
        }

        @NonNull
        @Override
        public FilterAdapter.FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false);
            return new FilterAdapter.FilterViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull FilterAdapter.FilterViewHolder holder,
                @SuppressLint("RecyclerView") int position) {

            holder.filterTitle.setText(ImageFilterType.gpuImageView().get(position).name());
            holder.setGpuImage(holder.bmp, ImageFilterType.gpuImageView(position, mContext));

            holder.itemView.setOnClickListener(v -> {
                isSelectedPosition = position;
                if (lensFacing == LensFacing.FRONT && position == 0) {
                    GlBrightnessFilter glBrightnessFilter = new GlBrightnessFilter();
                    glBrightnessFilter.setBrightness(0.1f);
                    gpuCameraRecorder.setFilter(glBrightnessFilter);
                } else
                    gpuCameraRecorder.setFilter(FilterType.gpuVideoView(position, CameraActivity.this));

                notifyDataSetChanged();
            });

            if (isSelectedPosition == position)
                holder.imageView.setStrokeWidth(2.5f);
            else
                holder.imageView.setStrokeWidth(0f);

        }

        @Override
        public void onViewDetachedFromWindow(@NonNull FilterAdapter.FilterViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
        }

        @Override
        public int getItemCount() {
            return ImageFilterType.gpuImageView().size();
        }

        public class FilterViewHolder extends RecyclerView.ViewHolder {
            TextView filterTitle;
            ShapeableImageView imageView;
            Bitmap bmp;
            private GPUImage mGpuImage;

            public FilterViewHolder(@NonNull View itemView) {
                super(itemView);
                this.bmp = drawableToBitmap(
                        ResourcesCompat.getDrawable(getResources(), R.drawable.filter_sample_image, getTheme()));
                imageView = itemView.findViewById(R.id.imageView);
                filterTitle = itemView.findViewById(R.id.filterTitle);

            }

            public void setGpuImage(Bitmap bitmap, GPUImageFilter filter) {
                if (mGpuImage == null) {
                    mGpuImage = new GPUImage(CameraActivity.this);
                }
                mGpuImage.setFilter(filter);
                imageView.setImageBitmap(mGpuImage.getBitmapWithFilterApplied(bitmap));
            }
        }
    }

    private class SongDownload extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL u = new URL(strings[0]);
                URLConnection conn = u.openConnection();
                int contentLength = conn.getContentLength();

                DataInputStream stream = new DataInputStream(u.openStream());

                byte[] buffer = new byte[contentLength];
                stream.readFully(buffer);
                stream.close();

                DataOutputStream fos = new DataOutputStream(new FileOutputStream(strings[1]));
                fos.write(buffer);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Timber.d("FileNotFoundException: %s", e.getMessage());
                return null;
            } catch (IOException e) {
                Timber.d("IOException: %s", e.getMessage());
                return null;
            }
            return strings[1];
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // this method will be running on UI thread
            /* AlertDialog. */

            ValueAnimator processingAnimator = ValueAnimator.ofInt(0, 4);
            processingAnimator.setRepeatCount(Animation.INFINITE);
            processingAnimator.setDuration(1000L);
            processingAnimator.addUpdateListener(animation -> {
                // noinspection SetTextI18n
                pdLoading.setMessage("\tPlease wait" + getDots((Integer) processingAnimator.getAnimatedValue(), ""));
            });
            pdLoading.setCancelable(false);
            pdLoading.setOnShowListener(dialog -> processingAnimator.start());
            pdLoading.setOnDismissListener(dialog -> processingAnimator.cancel());
            pdLoading.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Timber.d("onPostExecute: result %s", result);

                Toasty.success(CameraActivity.this, getResources().getString(R.string.selected_song_sucess),
                        Toasty.LENGTH_SHORT).show();
                pdLoading.dismiss();
            }
        }
    }

    public static String getDots(int count, String outDots) {
        if (count <= 0)
            return outDots;
        return getDots(--count, outDots) + ".";
    }
}
