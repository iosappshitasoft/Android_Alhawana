package com.app.utils.exoutils;

import android.content.Context;
import android.net.Uri;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsMultivariantPlaylist;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.app.alhawanajd.App;
import com.app.utils.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

public class ExoPlayerUtils {

    private static final String TAG = ExoPlayerUtils.class.getSimpleName();

    public static MediaSource getHlsMediaSource(Context context, Uri hlsUri) {
        HlsMediaSource mediaSource = null;
        // Create a data source factory.
        if (App.isCacheEnabled) {
            CacheDataSource.Factory datasourceFactory = getCacheDataSourceFactory(hlsUri);
            MediaItem mediaItem = getMediaItem(hlsUri);
            mediaSource = new HlsMediaSource.Factory(datasourceFactory).createMediaSource(mediaItem);
        } else {
            DataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true);
            // Create a HLS media source pointing to a playlist uri.
            HlsMediaSource.Factory sourceFactory = new HlsMediaSource.Factory(httpDataSourceFactory)
                    .setAllowChunklessPreparation(true);
            mediaSource = sourceFactory.createMediaSource(MediaItem.fromUri(hlsUri));
        }
        return mediaSource;
    }

    public static MediaSource getMp4MediaSource(Uri hlsUri) {
        ProgressiveMediaSource mediaSource = null;
        MediaItem mediaItem = getMediaItem(hlsUri);
        if (App.isCacheEnabled) {
            CacheDataSource.Factory datasourceFactory = getCacheDataSourceFactory(hlsUri);
            mediaSource = new ProgressiveMediaSource.Factory(datasourceFactory)
                    .createMediaSource(mediaItem);
        } else {
            DataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true);
            // Create a HLS media source pointing to a playlist uri.
            mediaSource = new ProgressiveMediaSource.Factory(httpDataSourceFactory)
                    .createMediaSource(mediaItem);
        }
        return mediaSource;
    }

    public static CacheDataSource.Factory getCacheDataSourceFactory(Uri hlsUri) {
        CacheDataSource.Factory cacheDataSourceFactory =
                new CacheDataSource.Factory()
                        .setCache(App.getSimpleCache())
                        .setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory()
                                .setAllowCrossProtocolRedirects(true));
        return cacheDataSourceFactory;
    }

    public static MediaItem getMediaItem(Uri hlsUri) {
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(hlsUri)
                .setStreamKeys(
                        Collections.singletonList(
                                new StreamKey(HlsMultivariantPlaylist.GROUP_INDEX_VARIANT, 0)))
                .build();
        return mediaItem;
    }

    /**
     * Used to starting caching a video
     */
    public static void cacheVideo(String hlsUri, Context mContext) {
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_VIDEO, hlsUri);
        OneTimeWorkRequest translateWorkRequest = new OneTimeWorkRequest.Builder(CacheVideoWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .setInputData(builder.build())
                .build();
        WorkManager workManager = WorkManager.getInstance(mContext);
        workManager.enqueue(translateWorkRequest);
    }

    public static File createSrtFile(String captionText,Context context) throws IOException {
        File srtFile = new File(context.getFilesDir(), "caption.srt");
        FileWriter writer = new FileWriter(srtFile);

        writer.write("1\n");
        writer.write("00:00:00,000 --> 00:59:59,000\n"); // show whole video duration
        writer.write(captionText + "\n");
        writer.close();

        return srtFile;
    }

}
