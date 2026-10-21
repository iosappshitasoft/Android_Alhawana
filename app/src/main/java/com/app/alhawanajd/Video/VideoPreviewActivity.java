package com.app.alhawanajd.Video;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.app.alhawanajd.R;
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.helper.LocaleManager;
import com.app.utils.Constants;

import java.io.File;

import timber.log.Timber;


public class VideoPreviewActivity extends BaseFragmentActivity {

    private static final String TAG = VideoPreviewActivity.class.getSimpleName();

    private StyledPlayerView playerView;
    private ExoPlayer exoPlayer;
    private DefaultTimeBar exoProgress;
    private File outputVideoFile;
    private String filePath;
    private ImageView btnBack;
    private Boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        playerView = findViewById(R.id.player_view);
//        exoProgress = playerView.findViewById(R.id.exo_progress);
        btnBack = findViewById(R.id.btnBack);

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }


        btnBack.setColorFilter(getResources().getColor(R.color.colorWhite));

//        outputVideoPath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_MIX_VIDEO + Utility.VIDEO_FORMAT);
            isEdit = getIntent().getBooleanExtra(Constants.TAG_EDIT, false);
        if(isEdit)
            filePath = getIntent().getStringExtra(Constants.TAG_VIDEO_PATH);
        else outputVideoFile = new File(getIntent().getStringExtra(Constants.TAG_VIDEO_PATH));


        btnBack.setOnClickListener(v -> closeActivity());
        initializePlayer();
        if(!isEdit)
        Log.e("pathtrim","-"+outputVideoFile.getAbsolutePath());
        else Log.e("pathtrim","-"+filePath);
    }

    private void initializePlayer() {

        String pathSend="";
        if(isEdit)
            pathSend = filePath;
        else pathSend = outputVideoFile.getAbsolutePath();

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(Uri.parse(pathSend))
                .build();
        exoPlayer = new ExoPlayer.Builder(getApplicationContext())
                .setMediaSourceFactory(createMediaSourceFactory())
                .build();
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        playerView.setPlayer(exoPlayer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build();
            exoPlayer.setAudioAttributes(audioAttributes, true);
        }
        // Prepare the content and ad to be played with the SimpleExoPlayer.
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();

        initListener();
        // Set PlayWhenReady. If true, content and ads will autoplay.
        exoPlayer.setPlayWhenReady(true);
    }

    private void initListener() {
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {

            }

            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_ENDED:
                        Timber.tag(TAG).d("onPlayerStateChanged: Video ended.");
                        break;
                    case Player.STATE_READY:
                            /*new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    btnPlay.setVisibility(View.INVISIBLE);
                                }
                            }, 500);*/
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
    }

    private MediaSource.Factory createMediaSourceFactory() {
        return new DefaultMediaSourceFactory(getApplicationContext());
    }

    @Override
    public void onStart() {
        super.onStart();

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
    }

    @Override
    public void onBackPressed() {
        closeActivity();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    public void closeActivity() {
        finish();
        overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_zoom_out);
    }

}