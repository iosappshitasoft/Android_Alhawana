package com.app.alhawanajd.Video;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.alhawanajd.BaseFragmentActivity;
import com.app.alhawanajd.R;
import com.app.helper.StorageUtils;
import com.app.model.GetSet;
import com.app.utils.Constants;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.daasuu.gpuv.camerarecorder.CameraRecordListener;
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorder;
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorderBuilder;
import com.daasuu.gpuv.camerarecorder.LensFacing;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class DuetRecordActivity extends BaseFragmentActivity {

    private static final String TAG = DuetRecordActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Views
    private FrameLayout cameraPreviewContainer;
    private FrameLayout videoPlayerContainer;
    private PlayerView playerView;
    private ImageView btnClose, btnRecord, btnGallery, btnFlipCamera, btnBack;
    private ProgressBar progressBar;
    private TextView txtRecordTime, btnDone;

    private String privacyType = "public";

    // Camera
    private GLSurfaceView sampleGLView;
    private GPUCameraRecorder gpuCameraRecorder;
    private LensFacing lensFacing = LensFacing.FRONT;
    private boolean isRecording = false;
    private boolean isFlipping = false;
    private File recordedVideoFile;

    // Video Player
    private ExoPlayer exoPlayer;
    private ExoPlayer galleryVideoPlayer;
    private PlayerView galleryPlayerView;
    private String originalVideoUrl;
    private String originalVideoId;
    private boolean isUsingGalleryVideo = false;

    // Recording
    private CountDownTimer recordTimer;
    private int recordSeconds = 0;
    private int maxDurationSeconds = 60;
    private ProgressDialog pdLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duet_record);

        // Get video data from intent
        originalVideoId = getIntent().getStringExtra(Constants.TAG_VIDEO_ID);
        originalVideoUrl = getIntent().getStringExtra(Constants.TAG_VIDEO_PATH);

        if (originalVideoUrl == null || originalVideoUrl.isEmpty()) {
            Toasty.error(this, "Video URL not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        checkPermissions();
    }

    private void initViews() {
        cameraPreviewContainer = findViewById(R.id.cameraPreviewContainer);
        videoPlayerContainer = findViewById(R.id.videoPlayerContainer);
        playerView = findViewById(R.id.playerView);
        btnClose = findViewById(R.id.btnClose);
        btnRecord = findViewById(R.id.btnRecord);
        btnGallery = findViewById(R.id.btnGallery);
        btnFlipCamera = findViewById(R.id.btnFlipCamera);
        progressBar = findViewById(R.id.progressBar);
        txtRecordTime = findViewById(R.id.txtRecordTime);
        btnDone = findViewById(R.id.btnDone);
        btnBack = findViewById(R.id.btnBack);

        pdLoading = new ProgressDialog(this, R.style.CameraAlertDialog);
        pdLoading.setMessage("Processing video...");
        pdLoading.setCancelable(false);

        btnClose.setOnClickListener(v -> finish());
        btnRecord.setOnClickListener(v -> toggleRecording());
        btnFlipCamera.setOnClickListener(v -> flipCamera());
        btnGallery.setOnClickListener(v -> {
            if (isUsingGalleryVideo) {
                // Switch back to camera mode
                switchToCameraMode();
            } else {
                // Open gallery to select video for duet
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                startActivityForResult(intent, 101);
            }
        });

        btnDone.setOnClickListener(v -> {
            if (recordedVideoFile != null && recordedVideoFile.exists()) {
                downloadAndMerge();
            } else {
                Toasty.error(this, "No video recorded.", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> resetRecording());

        // Initialize ExoPlayer for original video
        initializeVideoPlayer();
    }

    private void initializeVideoPlayer() {
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(originalVideoUrl));
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE); // Loop the video
        exoPlayer.prepare();
        exoPlayer.play();
    }

    private void checkPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[] { CAMERA, RECORD_AUDIO };
        } else {
            permissions = new String[] { CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE };
        }

        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            initCameraView();
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                initCameraView();
            } else {
                Toasty.error(this, "Camera and microphone permissions are required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initCameraView() {
        if (ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("Missing camera or audio permissions for initCameraView");
            return;
        }

        try {
            cameraPreviewContainer.removeAllViews();
            sampleGLView = new GLSurfaceView(this);
            sampleGLView.setEGLContextClientVersion(2);
            cameraPreviewContainer.addView(sampleGLView);

            gpuCameraRecorder = new GPUCameraRecorderBuilder(this, sampleGLView)
                    .cameraRecordListener(new CameraRecordListener() {
                        @Override
                        public void onGetFlashSupport(boolean flashSupport) {
                            Timber.d("Flash support: %s", flashSupport);
                        }

                        @Override
                        public void onRecordComplete() {
                            runOnUiThread(() -> {
                                isRecording = false;
                                stopRecordTimer();
                                progressBar.setVisibility(View.GONE);
                                txtRecordTime.setVisibility(View.GONE);
                                btnRecord.setImageResource(R.drawable.ic_play_white);

                                if (recordedVideoFile != null && recordedVideoFile.exists()) {
                                    // Show Done button and Privacy UI instead of auto-merging
                                    btnDone.setVisibility(View.VISIBLE);
                                    btnBack.setVisibility(View.VISIBLE);
                                    btnClose.setVisibility(View.INVISIBLE);

                                    // Hide other controls
                                    btnRecord.setVisibility(View.INVISIBLE);
                                    btnGallery.setVisibility(View.INVISIBLE);
                                    btnFlipCamera.setVisibility(View.INVISIBLE);
                                    findViewById(R.id.bottomControls).setVisibility(View.INVISIBLE);
                                }
                            });
                        }

                        @Override
                        public void onRecordStart() {
                            runOnUiThread(() -> {
                                isRecording = true;
                                startRecordTimer();
                                progressBar.setVisibility(View.VISIBLE);
                                txtRecordTime.setVisibility(View.VISIBLE);
                                btnRecord.setImageResource(R.drawable.ic_pause_white);
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            Timber.e(exception, "Camera recording error");
                            runOnUiThread(() -> {
                                Toasty.error(DuetRecordActivity.this, "Recording error: " + exception.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                isRecording = false;
                                stopRecordTimer();
                                progressBar.setVisibility(View.GONE);
                                txtRecordTime.setVisibility(View.GONE);
                                btnRecord.setImageResource(R.drawable.ic_play_white);
                            });
                        }

                        @Override
                        public void onCameraThreadFinish() {
                            Timber.d("Camera thread finished");
                            if (isFlipping) {
                                isFlipping = false;
                                runOnUiThread(() -> {
                                    initCameraView();
                                    String cameraType = (lensFacing == LensFacing.BACK) ? "back" : "front";
                                    Toasty.success(DuetRecordActivity.this, "Switched to " + cameraType + " camera",
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onVideoFileReady() {
                            Timber.d("Video file ready");
                        }
                    })
                    .videoSize(1080, 1920)
                    .lensFacing(lensFacing)
                    .build();

            Timber.d("Camera initialized with lens facing: %s", lensFacing);
        } catch (Exception e) {
            Timber.e(e, "Error initializing camera view");
            runOnUiThread(() -> {
                Toasty.error(this, "Failed to initialize camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void toggleRecording() {

        // If user selected a gallery video instead of camera
        if (isUsingGalleryVideo) {
            if (recordedVideoFile != null && recordedVideoFile.exists()) {
                // Always process original video (URL/local) then merge
                downloadAndMerge();
            } else {
                Toasty.error(this, "No video selected from gallery", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Normal camera recording mode
        if (gpuCameraRecorder == null) {
            Toasty.error(this, "Camera not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start camera recording
        if (!isRecording) {
            recordedVideoFile = new File(
                    StorageUtils.getInstance(this).getExtCacheDir(),
                    "duet_record_" + System.currentTimeMillis() + ".mp4");
            gpuCameraRecorder.start(recordedVideoFile.getAbsolutePath());
            // Reset UI for new recording
            btnDone.setVisibility(View.GONE);
            btnBack.setVisibility(View.INVISIBLE);
            btnClose.setVisibility(View.VISIBLE);

            findViewById(R.id.bottomControls).setVisibility(View.VISIBLE);
        } else {
            // Stop recording — onRecordComplete() will call downloadAndMerge()
            gpuCameraRecorder.stop();
        }
    }

    private void flipCamera() {
        // Don't allow camera flip during recording
        if (isRecording) {
            Toasty.warning(this, "Cannot flip camera while recording", Toast.LENGTH_SHORT).show();
            return;
        }

        // Toggle lens facing
        lensFacing = (lensFacing == LensFacing.FRONT) ? LensFacing.BACK : LensFacing.FRONT;

        isFlipping = true;

        // Cleanly release current camera + GL view
        releaseCamera();
    }

    private void releaseCamera() {
        try {
            if (gpuCameraRecorder != null) {
                // Stop first
                try {
                    gpuCameraRecorder.stop();
                } catch (Exception e) {
                    Timber.w(e, "Error while stopping recorder");
                }

                // Then release camera + resources
                try {
                    gpuCameraRecorder.release();
                } catch (Exception e) {
                    Timber.w(e, "Error while releasing recorder");
                }

                gpuCameraRecorder = null;
            }

            if (sampleGLView != null) {
                try {
                    sampleGLView.onPause(); // important for GL / camera cleanup
                } catch (Exception e) {
                    Timber.w(e, "Error while pausing SampleGLView");
                }

                cameraPreviewContainer.removeView(sampleGLView);
                sampleGLView = null;
            }
        } catch (Exception e) {
            Timber.e(e, "Error in releaseCamera()");
        }
    }

    private void startRecordTimer() {
        recordSeconds = 0;

        // Safely parse max video duration, fallback to 60 seconds on any issue
        maxDurationSeconds = 60;
        try {
            String durationStr = String.valueOf(GetSet.getMax_video_duration());
            if (durationStr != null && !durationStr.trim().isEmpty()) {
                maxDurationSeconds = Integer.parseInt(durationStr.trim());
            }
        } catch (NumberFormatException ignored) {
            // Keep default 60s if parsing fails
        }

        final int totalDuration = maxDurationSeconds;

        recordTimer = new CountDownTimer(totalDuration * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                recordSeconds++;
                int minutes = recordSeconds / 60;
                int seconds = recordSeconds % 60;
                txtRecordTime.setText(String.format("%02d:%02d", minutes, seconds));

                int progress = (int) ((recordSeconds * 100) / totalDuration);
                progressBar.setProgress(progress);

                // Auto stop at max duration
                if (recordSeconds >= totalDuration) {
                    if (gpuCameraRecorder != null && isRecording) {
                        gpuCameraRecorder.stop();
                    }
                }
            }

            @Override
            public void onFinish() {
                if (gpuCameraRecorder != null && isRecording) {
                    gpuCameraRecorder.stop();
                }
            }
        };
        recordTimer.start();
    }

    private void stopRecordTimer() {
        if (recordTimer != null) {
            recordTimer.cancel();
            recordTimer = null;
        }
    }

    /**
     * Merge the previously downloaded / local original video with recorded/gallery
     * video
     * using TikTok-style side-by-side, full-height, cropped horizontally.
     */
    private void mergeWithDownloadedOriginal(File originalVideoFile) {

        if (!pdLoading.isShowing()) {
            pdLoading.show();
        }

        File outputFile = new File(
                StorageUtils.getInstance(this).getExtCacheDir(),
                "duet_merged_" + System.currentTimeMillis() + ".mp4");

        int width = 540;
        int height = 1920;

        String command = String.format(
                "-y " +
                        "-i \"%s\" " +
                        "-i \"%s\" " +
                        "-filter_complex " +
                        "\"[0:v]scale=-1:%d,crop=%d:%d,setsar=1[v0];" +
                        "[1:v]scale=-1:%d,crop=%d:%d,setsar=1[v1];" +
                        "[v0][v1]hstack=inputs=2[v]\" " +
                        "-map \"[v]\" " +
                        "-map 0:a? " +
                        "-c:v mpeg4 " +
                        "-qscale:v 2 " +
                        "-r 30 " +
                        "-video_track_timescale 90000 " +
                        "-c:a aac -b:a 128k " +
                        "-shortest " +
                        "\"%s\"",

                originalVideoFile.getAbsolutePath(),
                recordedVideoFile.getAbsolutePath(),

                height, width, height,
                height, width, height,

                outputFile.getAbsolutePath());

        FFmpegKit.executeAsync(command, session -> runOnUiThread(() -> {
            pdLoading.dismiss();
            ReturnCode rc = session.getReturnCode();
            if (ReturnCode.isSuccess(rc)) {
                Intent intent = new Intent(this, PostVideoActivity.class);
                intent.putExtra(Constants.TAG_VIDEO_PATH, outputFile.getAbsolutePath());
                intent.putExtra(Constants.TAG_TYPE, "duet");
                intent.putExtra("is_duet", true);
                intent.putExtra(Constants.TAG_VIDEO_ID, originalVideoId);
                intent.putExtra(Constants.TAG_PUBLISH_MODE, privacyType);
                startActivity(intent);
                finish();
            } else {
                Toasty.error(this, "Failed to merge videos", Toast.LENGTH_SHORT).show();
                Timber.e("FFmpeg merge error: %s", session.getFailStackTrace());
            }
        }));
    }

    /**
     * Ensure the original video is in a local file (download if URL), then call
     * merge.
     * Handles both HTTP(s) URLs (HLS/MP4) and local file paths.
     */
    private void downloadAndMerge() {

        // Show loader once at the start of pipeline
        if (!pdLoading.isShowing()) {
            pdLoading.show();
        }

        // If path is already local file, skip download
        if (!originalVideoUrl.startsWith("http")) {
            File originalFile = new File(originalVideoUrl);
            if (originalFile.exists()) {
                mergeWithDownloadedOriginal(originalFile);
            } else {
                pdLoading.dismiss();
                Toasty.error(this, "Original video file not found", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Download original video via FFmpeg (supports HLS/.m3u8, MP4, etc.)
        File downloadedFile = new File(
                StorageUtils.getInstance(this).getExtCacheDir(),
                "duet_original_" + System.currentTimeMillis() + ".mp4");

        String downloadCommand = String.format(
                "-y -i \"%s\" -c copy \"%s\"",
                originalVideoUrl,
                downloadedFile.getAbsolutePath());

        FFmpegKit.executeAsync(downloadCommand, session -> {
            ReturnCode rc = session.getReturnCode();

            if (ReturnCode.isSuccess(rc)) {
                // Now merge the downloaded original with recorded/gallery video
                mergeWithDownloadedOriginal(downloadedFile);
            } else {
                runOnUiThread(() -> {
                    pdLoading.dismiss();
                    Toasty.error(this, "Failed to download original video", Toast.LENGTH_SHORT).show();
                    Timber.e("FFmpeg download error: %s", session.getFailStackTrace());
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
        }
        if (galleryVideoPlayer != null) {
            galleryVideoPlayer.setPlayWhenReady(true);
        }
        if (!isUsingGalleryVideo) {
            checkPermissions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
        if (galleryVideoPlayer != null) {
            galleryVideoPlayer.setPlayWhenReady(false);
        }
        releaseCamera();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri selectedVideoUri = data.getData();
            if (selectedVideoUri != null) {
                String mimeType = getContentResolver().getType(selectedVideoUri);
                if (mimeType != null && mimeType.startsWith("video")) {
                    // Replace camera preview with selected gallery video
                    replaceWithGalleryVideo(selectedVideoUri);
                } else {
                    Toasty.error(this, "Please select a video file only.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void replaceWithGalleryVideo(Uri videoUri) {
        try {
            // Release camera resources
            releaseCamera();
            isUsingGalleryVideo = true;

            // Remove camera view and add video player
            cameraPreviewContainer.removeAllViews();

            // Create PlayerView for gallery video
            galleryPlayerView = new PlayerView(this);
            galleryPlayerView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            galleryPlayerView.setUseController(false);

            galleryPlayerView.setScaleY(1.5f);

            cameraPreviewContainer.addView(galleryPlayerView);

            // Initialize ExoPlayer for gallery video
            galleryVideoPlayer = new ExoPlayer.Builder(this).build();
            galleryPlayerView.setPlayer(galleryVideoPlayer);

            MediaItem mediaItem = MediaItem.fromUri(videoUri);
            galleryVideoPlayer.setMediaItem(mediaItem);
            galleryVideoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE); // Loop the video

            // Mute gallery video in preview
            galleryVideoPlayer.setVolume(0f);

            galleryVideoPlayer.prepare();
            galleryVideoPlayer.play();

            // Correctly set recordedVideoFile path
            String path = getRealPathFromURI(videoUri);
            if (path != null) {
                recordedVideoFile = new File(path);
            }

            // Show Done button and Privacy UI
            btnDone.setVisibility(View.VISIBLE);
            btnBack.setVisibility(View.VISIBLE);
            btnClose.setVisibility(View.INVISIBLE);

            findViewById(R.id.bottomControls).setVisibility(View.INVISIBLE);

            // Hide flip camera button since we're using gallery video
            btnFlipCamera.setVisibility(View.GONE);

            // Change record button to "merge" icon style if needed (though it will be
            // hidden now)
            btnRecord.setImageResource(R.drawable.ic_pause_white);
            btnRecord.setVisibility(View.INVISIBLE);

            Toasty.success(this, "Gallery video loaded. Choose privacy and tap Done to merge.", Toast.LENGTH_LONG)
                    .show();

        } catch (Exception e) {
            Timber.e(e, "Error loading gallery video");
            Toasty.error(this, "Failed to load video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = { android.provider.MediaStore.Video.Media.DATA };
        android.database.Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contentUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Video.Media.DATA);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Timber.e(e, "Error getting real path from URI");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private void switchToCameraMode() {
        try {
            // Release gallery video player
            if (galleryVideoPlayer != null) {
                galleryVideoPlayer.release();
                galleryVideoPlayer = null;
            }

            // Remove gallery player view
            cameraPreviewContainer.removeAllViews();

            // Reset state
            isUsingGalleryVideo = false;
            recordedVideoFile = null;

            // Reinitialize camera
            initCameraView();

            // Show flip camera button again
            btnFlipCamera.setVisibility(View.VISIBLE);

            // Change record button back to record icon
            btnRecord.setImageResource(R.drawable.ic_play_white);

            // Hide Done and Privacy UI
            btnDone.setVisibility(View.GONE);
            btnBack.setVisibility(View.INVISIBLE);
            btnClose.setVisibility(View.VISIBLE);

            findViewById(R.id.bottomControls).setVisibility(View.VISIBLE);

            Toasty.success(this, "Switched back to camera mode", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Timber.e(e, "Error switching to camera mode");
            Toasty.error(this, "Failed to switch to camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void resetRecording() {
        try {
            if (galleryVideoPlayer != null) {
                galleryVideoPlayer.release();
                galleryVideoPlayer = null;
            }

            cameraPreviewContainer.removeAllViews();
            isUsingGalleryVideo = false;
            recordedVideoFile = null;

            btnDone.setVisibility(View.GONE);
            btnBack.setVisibility(View.GONE);
            btnClose.setVisibility(View.VISIBLE);
            btnRecord.setVisibility(View.VISIBLE);
            btnRecord.setImageResource(R.drawable.ic_play_white);
            btnGallery.setVisibility(View.VISIBLE);
            btnFlipCamera.setVisibility(View.VISIBLE);
            findViewById(R.id.bottomControls).setVisibility(View.VISIBLE);

            initCameraView();

            Toasty.info(this, "Cleared. You can record or pick again.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (galleryVideoPlayer != null) {
            galleryVideoPlayer.release();
            galleryVideoPlayer = null;
        }
        releaseCamera();
        stopRecordTimer();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
    }
}
