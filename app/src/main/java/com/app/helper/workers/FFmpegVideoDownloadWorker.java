package com.app.helper.workers;

//import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
//import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

//import com.arthenica.mobileffmpeg.Config;
//import com.arthenica.mobileffmpeg.ExecuteCallback;
//import com.arthenica.mobileffmpeg.FFmpeg;
//import com.arthenica.mobileffmpeg.Statistics;
//import com.arthenica.mobileffmpeg.StatisticsCallback;


import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;

import com.google.common.util.concurrent.ListenableFuture;
import com.app.alhawanajd.R;
import com.app.alhawanajd.Utility;
import com.app.helper.StorageUtils;
import com.app.utils.Constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;
public class FFmpegVideoDownloadWorker extends ListenableWorker {
    private static final String TAG = FFmpegVideoDownloadWorker.class.getSimpleName();
    private Context mContext;
    private StorageUtils storageUtils;
    private int downloadId;
    private NotificationManager mNotifyManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public FFmpegVideoDownloadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.mContext = context;
        this.storageUtils = StorageUtils.getInstance(context);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        downloadId = Math.toIntExact(System.currentTimeMillis() / 1000);
        mNotifyManager = getNotificationManager();
        NotificationCompat.Builder mBuilder = getNotificationBuilder();
        // Do the work here--in this case, download the file.
//        String attachment = getInputData().getString(Constants.TAG_ATTACHMENT);
        String url = getInputData().getString(Constants.TAG_URL);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = mContext.getString(R.string.app_name) + "VID_" + timeStamp + Utility.VIDEO_FORMAT;
        File tempFile = new File(StorageUtils.getInstance(mContext).getExtCacheDir(), fileName);
//        Timber.tag(TAG).d("startWork: %s", tempFile.getAbsolutePath());
        String finalUrl = url;
        int videoLength = MediaPlayer.create(mContext, Uri.parse(finalUrl)).getDuration();
        Timber.tag(TAG).d("Video Length: %s", "initial");
        updateNotification(downloadId, Math.round(0.1F), videoLength, mBuilder);
        return CallbackToFutureAdapter.getFuture(completer -> {
            List<String> commandList = new ArrayList<>();
            commandList.add("-i");
            commandList.add(finalUrl);
            commandList.add("-c");
            commandList.add("copy");
            commandList.add("-bsf:a");
            commandList.add("aac_adtstoasc");
            commandList.add(tempFile.getAbsolutePath());
            //   String[] command = commandList.toArray(new String[commandList.size()]);
//            Timber.tag(TAG).d("startWork: %s", Arrays.toString(commandList.toArray()));
       /*     Config.enableStatisticsCallback(new StatisticsCallback() {
                public void apply(Statistics newStatistics) {
                    float progress = Float.parseFloat(String.valueOf(newStatistics.getTime())) / videoLength;
                    float progressFinal = progress * 100;
                    Timber.tag(TAG).d("Video Length: %s", videoLength);
                    Timber.tag(TAG).d("Video Length: %s", progress);
                    Timber.tag(TAG).d("Video Length: %s", progressFinal);
                    try {
                        updateNotification(downloadId, (int) progressFinal, videoLength, mBuilder);
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            FFmpeg.executeAsync(command, new ExecuteCallback() {
                @Override
                public void apply(long executionId, int returnCode) {
                    if (returnCode == RETURN_CODE_SUCCESS) {
                        String outFileName = tempFile.getName();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Uri outputUri = StorageUtils.getInstance(mContext).saveFileInStorageV10(tempFile, outFileName, Constants.TAG_VIDEO);
                            completer.set(Result.success(createOutputData(outputUri.getPath())));
                        } else {
                            File outputFile = StorageUtils.getInstance(mContext).saveFileInStorage(tempFile, Constants.TAG_VIDEO, Constants.TAG_VIDEO);
                            StorageUtils.getInstance(mContext).refreshGallery(outputFile);
                            completer.set(Result.success(createOutputData(outputFile.getAbsolutePath())));
                        }
                        mBuilder.setContentTitle(mContext.getString(R.string.download_successfully));
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mNotifyManager.cancel(downloadId);
                            }
                        }, 1000);
                    } else if (returnCode == RETURN_CODE_CANCEL) {
                        Timber.tag(TAG).e("RETURN_CODE_CANCEL ");
                        FFmpeg.cancel();
                        setError(completer);
                    } else {
                        Timber.tag(TAG).e("Command execution failed with rc=%d", returnCode);
                        setError(completer);
                    }
                }
            });*/

            String command = String.join(" ", commandList);
            // Progress Tracking
            FFmpegKitConfig.enableStatisticsCallback(new StatisticsCallback() {
                @Override
                public void apply(Statistics newStatistics) {
                    float progress = (float) newStatistics.getTime() / videoLength;
                    float progressFinal = progress * 100;
                    Timber.tag(TAG).d("Processing Video: %s%%", progressFinal);
                    try {
                        updateNotification(downloadId, (int) progressFinal, videoLength, mBuilder);
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            FFmpegKit.executeAsync(command, session -> {
                long executionId = session.getSessionId();
                ReturnCode returnCode = session.getReturnCode();
                SessionState state = session.getState();

                Timber.tag(TAG).d("FFmpeg Execution ID: %s, State: %s, ReturnCode: %s", executionId, state, returnCode);

                mainHandler.post(() -> {
                    if (ReturnCode.isSuccess(returnCode)) {
                        String outFileName = tempFile.getName();
                        Uri outputUri;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            outputUri = StorageUtils.getInstance(mContext).saveFileInStorageV10(tempFile, outFileName, Constants.TAG_VIDEO);
                        } else {
                            File outputFile = StorageUtils.getInstance(mContext).saveFileInStorage(tempFile, Constants.TAG_VIDEO, Constants.TAG_VIDEO);
                            StorageUtils.getInstance(mContext).refreshGallery(outputFile);
                            outputUri = Uri.fromFile(outputFile);
                        }

                        completer.set(Result.success(createOutputData(outputUri.getPath())));
                        mBuilder.setContentTitle(mContext.getString(R.string.download_successfully));

                        new Handler(Looper.getMainLooper()).postDelayed(() -> mNotifyManager.cancel(downloadId), 1000);
                    } else if (ReturnCode.isCancel(returnCode)) {
                        Timber.tag(TAG).e("FFmpeg Execution Cancelled");
                        FFmpegKit.cancel();
                        setError(completer);
                    } else {
                        Timber.tag(TAG).e("FFmpeg Execution Failed: %s", returnCode);
                        setError(completer);
                    }
                });
            });

//            int returnCode = FFmpeg.execute(command);

            return completer;
        });
        // Indicate whether the work finished successfully with the Result
    }

    private NotificationManager getNotificationManager() {
        String CHANNEL_ID = mContext.getString(R.string.fundoo_download_channel);
        CharSequence channelName = mContext.getString(R.string.app_name);
        NotificationManager mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            notificationChannel.enableVibration(false);
            notificationChannel.setSound(null, null);
            if (mNotifyManager != null) {
                mNotifyManager.createNotificationChannel(notificationChannel);
            }
        }
        return mNotifyManager;
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, mContext.getString(R.string.fundoo_download_channel));
        mBuilder.setContentTitle(mContext.getString(R.string.download_in_progress))
                .setContentText("")
                .setSmallIcon(R.drawable.notification)
                .setSound(null)
                .setVibrate(null);
        return mBuilder;
    }

    private void updateNotification(int downloadId, int progress, int videoLength, NotificationCompat.Builder mBuilder) {
        mBuilder.setProgress(100, progress, false);
        mNotifyManager.notify(downloadId, mBuilder.build());
    }

    private Data createOutputData(String filePath) {
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_FILE_PATH, filePath);
        return builder.build();
    }

    private void setError(CallbackToFutureAdapter.Completer<Result> completer) {
        completer.set(Result.failure(Data.EMPTY));
    }

}
//public class FFmpegVideoDownloadWorker extends ListenableWorker {
//    private static final String TAG = FFmpegVideoDownloadWorker.class.getSimpleName();
//    private Context mContext;
//    private StorageUtils storageUtils;
//    private int downloadId;
//    private NotificationManager mNotifyManager;
//
//    public FFmpegVideoDownloadWorker(
//            @NonNull Context context,
//            @NonNull WorkerParameters params) {
//        super(context, params);
//        this.mContext = context;
//        this.storageUtils = StorageUtils.getInstance(context);
//    }
//
//    @NonNull
//    @Override
//    public ListenableFuture<Result> startWork() {
//        downloadId = Math.toIntExact(System.currentTimeMillis() / 1000);
//        mNotifyManager = getNotificationManager();
//        NotificationCompat.Builder mBuilder = getNotificationBuilder();
//        // Do the work here--in this case, download the file.
////        String attachment = getInputData().getString(Constants.TAG_ATTACHMENT);
//        String url = getInputData().getString(Constants.TAG_URL);
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String fileName = mContext.getString(R.string.app_name) + "VID_" + timeStamp + Utility.VIDEO_FORMAT;
//        File tempFile = new File(StorageUtils.getInstance(mContext).getExtCacheDir(), fileName);
////        Timber.tag(TAG).d("startWork: %s", tempFile.getAbsolutePath());
//        String finalUrl = url;
//        int videoLength = MediaPlayer.create(mContext, Uri.parse(finalUrl)).getDuration();
//        Timber.tag(TAG).d("Video Length: %s", "initial");
//        updateNotification(downloadId, Math.round(0.1F), videoLength, mBuilder);
//        return CallbackToFutureAdapter.getFuture(completer -> {
//            List<String> commandList = new ArrayList<>();
//            commandList.add("-i");
//            commandList.add(finalUrl);
//            commandList.add("-c");
//            commandList.add("copy");
//            commandList.add("-bsf:a");
//            commandList.add("aac_adtstoasc");
//            commandList.add(tempFile.getAbsolutePath());
//            String[] command = commandList.toArray(new String[commandList.size()]);
////            Timber.tag(TAG).d("startWork: %s", Arrays.toString(commandList.toArray()));
//            Config.enableStatisticsCallback(new StatisticsCallback() {
//                public void apply(Statistics newStatistics) {
//                    float progress = Float.parseFloat(String.valueOf(newStatistics.getTime())) / videoLength;
//                    float progressFinal = progress * 100;
//                    Timber.tag(TAG).d("Video Length: %s", videoLength);
//                    Timber.tag(TAG).d("Video Length: %s", progress);
//                    Timber.tag(TAG).d("Video Length: %s", progressFinal);
//                    try {
//                        updateNotification(downloadId, (int) progressFinal, videoLength, mBuilder);
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            FFmpeg.executeAsync(command, new ExecuteCallback() {
//                @Override
//                public void apply(long executionId, int returnCode) {
//                    if (returnCode == RETURN_CODE_SUCCESS) {
//                        String outFileName = tempFile.getName();
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                            Uri outputUri = StorageUtils.getInstance(mContext).saveFileInStorageV10(tempFile, outFileName, Constants.TAG_VIDEO);
//                            completer.set(Result.success(createOutputData(outputUri.getPath())));
//                        } else {
//                            File outputFile = StorageUtils.getInstance(mContext).saveFileInStorage(tempFile, Constants.TAG_VIDEO, Constants.TAG_VIDEO);
//                            StorageUtils.getInstance(mContext).refreshGallery(outputFile);
//                            completer.set(Result.success(createOutputData(outputFile.getAbsolutePath())));
//                        }
//                        mBuilder.setContentTitle(mContext.getString(R.string.download_successfully));
//                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                mNotifyManager.cancel(downloadId);
//                            }
//                        }, 1000);
//                    } else if (returnCode == RETURN_CODE_CANCEL) {
//                        Timber.tag(TAG).e("RETURN_CODE_CANCEL ");
//                        FFmpeg.cancel();
//                        setError(completer);
//                    } else {
//                        Timber.tag(TAG).e("Command execution failed with rc=%d", returnCode);
//                        setError(completer);
//                    }
//                }
//            });
//
////            int returnCode = FFmpeg.execute(command);
//
//            return completer;
//        });
//        // Indicate whether the work finished successfully with the Result
//    }
//
//    private NotificationManager getNotificationManager() {
//        String CHANNEL_ID = mContext.getString(R.string.fundoo_download_channel);
//        CharSequence channelName = mContext.getString(R.string.app_name);
//        NotificationManager mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, importance);
//            notificationChannel.enableVibration(false);
//            notificationChannel.setSound(null, null);
//            if (mNotifyManager != null) {
//                mNotifyManager.createNotificationChannel(notificationChannel);
//            }
//        }
//        return mNotifyManager;
//    }
//
//    private NotificationCompat.Builder getNotificationBuilder() {
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, mContext.getString(R.string.fundoo_download_channel));
//        mBuilder.setContentTitle(mContext.getString(R.string.download_in_progress))
//                .setContentText("")
//                .setSmallIcon(R.drawable.notification)
//                .setSound(null)
//                .setVibrate(null);
//        return mBuilder;
//    }
//
//    private void updateNotification(int downloadId, int progress, int videoLength, NotificationCompat.Builder mBuilder) {
//        mBuilder.setProgress(100, progress, false);
//        mNotifyManager.notify(downloadId, mBuilder.build());
//    }
//
//    private Data createOutputData(String filePath) {
//        Data.Builder builder = new Data.Builder();
//        builder.putString(Constants.TAG_FILE_PATH, filePath);
//        return builder.build();
//    }
//
//    private void setError(CallbackToFutureAdapter.Completer<Result> completer) {
//        completer.set(Result.failure(Data.EMPTY));
//    }
//
//}