package com.app.helper.workers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.app.helper.StorageUtils;
import com.app.utils.Constants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class FileDownloadWorker extends ListenableWorker {
    private static final String TAG = FileDownloadWorker.class.getSimpleName();
    private Context mContext;
    private StorageUtils storageUtils;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.getMainLooper());

    public FileDownloadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.mContext = context;
        this.storageUtils = StorageUtils.getInstance(context);
    }

    @Override
    public synchronized ListenableFuture<Result> startWork() {
        // Do the work here--in this case, download the file.
//        String attachment = getInputData().getString(Constants.TAG_ATTACHMENT);
        String url = getInputData().getString(Constants.TAG_URL);
        String fileName = getInputData().getString(Constants.TAG_FILE_NAME);
        File tempFile = new File(getInputData().getString(Constants.TAG_OUTPUT_PATH));
        Timber.tag(TAG).d("startWork: %s", tempFile.getAbsolutePath());
        return CallbackToFutureAdapter.getFuture(completer -> {
            Callback callback = new Callback() {
                @Override
                public void onFailure(Exception e) {
                    completer.setException(e);
                }

                @Override
                public void onResponse(boolean status, Object response) {
                    if (status) {
                        completer.set(Result.success((Data) response));
                    }
                }
            };
            downloadFromUrl(url, tempFile, callback);
            return completer;
        });
    }

    private void downloadFromUrl(String url, File tempFile, Callback callback) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                    byte dataBuffer[] = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResponse(true, createOutputData(tempFile.getAbsolutePath()));
                        }
                    });
                } catch (IOException e) {
                    Timber.e("downloadFromUrl: %s", e.getMessage());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(e);
                        }
                    });
                }
            }
        });
    }

    private Data createOutputData(String filePath) {
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_OUTPUT_PATH, filePath);
        return builder.build();
    }

    private void setError(CallbackToFutureAdapter.Completer<Result> completer) {
        completer.set(Result.failure(Data.EMPTY));
    }

    private interface Callback {
        void onResponse(boolean status, Object o);

        void onFailure(Exception e);
    }

}