package com.app.external;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;

import java.io.IOException;

public class MediaPlayerUtils {

    public static MediaPlayer mediaPlayer;
    private static MediaPlayerUtils.Listener listener;
    private static Handler mHandler;
    private static int currentPosition = 0;
    private static Uri audioUri;

    /**
     * Get database instance
     *
     * @return database handler instance
     */
    public static void getInstance() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        if (mHandler == null) {
            mHandler = new Handler();
        }
    }

    /**
     * Release mediaPlayer
     */
    public static void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static void pauseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public static void playMediaPlayer(int progress) {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.seekTo(progress);
            mHandler.postDelayed(mRunnable, 100);
        }
    }

    public static void applySeekBarValue(int selectedValue) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(selectedValue);
            mHandler.postDelayed(mRunnable, 100);
        }
    }

    public static int getCurrentPosition() {
        return currentPosition;
    }

    public static void setCurrentPosition(int position) {
        currentPosition = position;
    }

    /**
     * Start mediaPlayer
     *
     * @param seekBar
     * @param context
     * @param audioUrl sd card media file
     * @param progress
     * @throws IOException exception
     */
    public static void startAndPlayMediaPlayer(Context context, Uri audioUrl, final Listener listener, int progress) throws IOException {
        MediaPlayerUtils.listener = listener;
        getInstance();
        if (isPlaying()) {
            pauseMediaPlayer();
        }
        releaseMediaPlayer();
        getInstance();
        audioUri = audioUrl;
        mediaPlayer.setDataSource(context, audioUrl);
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        mHandler.postDelayed(mRunnable, 100);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
//                listener.onAudioComplete();
                listener.setMediaDuration(mediaPlayer.getDuration());
                playMediaPlayer(progress);
            }
        });
    }

    public static boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public static int getTotalDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public static int getCurrentDuration() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    private static MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            MediaPlayerUtils.releaseMediaPlayer();
            currentPosition = 0;
            listener.onAudioComplete();
        }
    };

    private static Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (isPlaying()) {
                    mHandler.postDelayed(mRunnable, 100);
                    currentPosition = mediaPlayer.getCurrentPosition();
                    listener.onAudioUpdate(mediaPlayer.getCurrentPosition());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public static String getUrl() {
        return audioUri != null ? "" + audioUri : null;
    }

    public interface Listener {
        void onAudioComplete();

        void onAudioUpdate(int currentPosition);

        void setMediaDuration(int duration);
    }

}