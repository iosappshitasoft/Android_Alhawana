package com.app.addon;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;

import java.io.IOException;

import timber.log.Timber;

public class VideoAddon {
    private static final String TAG = VideoAddon.class.getSimpleName();
    private static MediaPlayer mediaPlayer;
    private static TextureView mTextureView;
    private static int lastPlayedPosition = -1;

    //Addon Login Video
    public static void initVideo(TextureView textureView, Context mContext) {
        mTextureView = textureView;
        mTextureView.setVisibility(View.VISIBLE);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                Timber.tag(TAG).d("onSurfaceTextureAvailable: ");
                Surface surface = new Surface(surfaceTexture);
                try {
                    AssetFileDescriptor afd = mContext.getAssets().openFd("loginvideo.mp4");
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mediaPlayer.setSurface(surface);
                    mediaPlayer.setLooping(true);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setVolume(0, 0);

                    // Play video when the media source is ready for playback.
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                        }
                    });

                } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
                    Log.e(TAG, "onSurfaceTextureAvailable: " + e.getMessage());
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                Log.e(TAG, "onSurfaceTextureSizeChanged: ");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                Log.e(TAG, "onSurfaceTextureDestroyed: ");
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
//                Log.e(TAG, "onSurfaceTextureUpdated: ");
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(lastPlayedPosition);
                    mediaPlayer.start();
                }
            }
        });
    }

    public static void pauseMediaPlayer() {
        if (mediaPlayer != null) {
            lastPlayedPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        }
    }

    public static void stopMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            mTextureView.setSurfaceTextureListener(null);
            mTextureView = null;
        }
    }
}
