package com.app.utils.exoutils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.offline.Downloader;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloader;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheWriter;
import com.app.alhawanajd.App;
import com.app.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class CacheVideoWorker extends Worker {
    private static final String TAG = CacheVideoWorker.class.getSimpleName();
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.getMainLooper());
    private Context mContext;

    public CacheVideoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.mContext = context;
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        final String url = getInputData().getString(Constants.TAG_VIDEO);
        if (!TextUtils.isEmpty(url)) {
            mExecutorService.execute(new Runnable() {
                public void run() {
                    Uri hlsUri = Uri.parse(url);
//                    downloadHelper(hlsUri);
                    hlsDownloader(ExoPlayerUtils.getMediaItem(hlsUri), ExoPlayerUtils.getCacheDataSourceFactory(hlsUri));
//                    cacheWriter(hlsUri);
                }
            });
        }

        return Result.success();
    }

    private void cacheWriter(Uri hlsUri) {
        DataSpec dataSpec = new DataSpec(hlsUri);
        CacheWriter.ProgressListener progressListener = new CacheWriter.ProgressListener() {
            @Override
            public void onProgress(long requestLength, long bytesCached, long newBytesCached) {
                double downloadPercentage = (bytesCached * 100. / requestLength);
              //  Log.d(TAG, "onProgress: " + downloadPercentage + ", " + hlsUri);
            }
        };
        DataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true);
        CacheDataSource cacheDataSource = new CacheDataSource(App.getSimpleCache(), httpDataSourceFactory.createDataSource());
        CacheWriter cacheWriter = new CacheWriter(cacheDataSource, dataSpec, null, progressListener);
        try {
            cacheWriter.cache();
        } catch (IOException e) {
            //Log.e(TAG, "cacheVideo: " + e.getMessage());
            //            e.printStackTrace();
        }
    }

    private void downloadHelper(Uri hlsUri) {
        /*DownloadHelper downloadHelper =
                DownloadHelper.forMediaItem(
                        context,
                        MediaItem.fromUri(hlsUri),
                        new DefaultRenderersFactory(context),
                        httpDataSourceFactory);
        downloadHelper.prepare(new DownloadHelper.Callback() {
            @Override
            public void onPrepared(DownloadHelper helper) {
                Timber.e("onPrepared: ");
            }

            @Override
            public void onPrepareError(DownloadHelper helper, IOException e) {
                Timber.e("onPrepareError: %s", e.getMessage());
            }
        });*/
    }

    private void hlsDownloader(MediaItem mediaItem, CacheDataSource.Factory cacheDataSourceFactory) {
        Timber.d("hlsDownloader: ");
        HlsDownloader hlsDownloader =
                new HlsDownloader(mediaItem, cacheDataSourceFactory);
        Downloader.ProgressListener progressListener = new Downloader.ProgressListener() {
            @Override
            public void onProgress(long contentLength, long bytesDownloaded, float percentDownloaded) {
               // Timber.tag(TAG).d("onProgress: %s", percentDownloaded);
            }
        };
        // Perform the download.
        try {
            hlsDownloader.download(progressListener);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
