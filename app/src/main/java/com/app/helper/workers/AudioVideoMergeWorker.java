package com.app.helper.workers;

//import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
//import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;



import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

//import com.arthenica.mobileffmpeg.FFmpeg;
import com.google.common.util.concurrent.ListenableFuture;
import com.app.external.video_trimmer.utils.TrimmerUtils;
import com.app.alhawanajd.Utility;
import com.app.helper.StorageUtils;
import com.app.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class AudioVideoMergeWorker extends ListenableWorker {
    private static final String TAG = AudioVideoMergeWorker.class.getSimpleName();
    private Context mContext;

    public AudioVideoMergeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        final String videoPath = getInputData().getString(Constants.TAG_VIDEO);
        final String audioPath = getInputData().getString(Constants.TAG_AUDIO);
        final long lastMinValue = getInputData().getLong(Constants.TAG_VIDEO_START, 0);
        final long lastMaxValue = getInputData().getLong(Constants.TAG_VIDEO_END, 0);
        final File outputFile = StorageUtils.getInstance(mContext).getTempFile(mContext, Constants.TAG_MIX_VIDEO + Utility.VIDEO_FORMAT);
        long videoDuration = lastMaxValue - lastMinValue;
        long audioDuration = TrimmerUtils.getMediaDuration(new File(audioPath));
        Timber.tag(TAG).d("startWork: %s, %s", videoDuration, audioDuration);
        String[] mergeCommand = null;
        List<String> command = new ArrayList<>();
        if (audioDuration > videoDuration) {
            command.add("-ss");
            command.add(TrimmerUtils.formatCSeconds(lastMinValue));
            command.add("-i");
            command.add(videoPath);
            command.add("-t");
            command.add(TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue));
            command.add("-i");
            command.add(audioPath);
//            commandList.add("-threads 0");
//            commandList.add("-preset ultrafast");
            command.add("-vcodec");
            command.add("copy");
            command.add("-acodec");
            command.add("copy");
            command.add("-map");
            command.add("0:v:0");
            command.add("-map");
            command.add("1:a:0");
//            command.add("-b:v");
//            command.add("2M");
//            command.add("-c:v");
//            command.add("libx264");
//            command.add("-preset");
//            command.add("ultrafast");
//            command.add("-crf");
//            command.add("35");
//            command.add("-async");
//            command.add("1");
            command.add("-shortest");
            command.add("-y");
            command.add(outputFile.getAbsolutePath());
        } else {
            command.add("-ss");
            command.add(TrimmerUtils.formatCSeconds(lastMinValue));
            command.add("-i");
            command.add(videoPath);
            command.add("-t");
            command.add(TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue));
            command.add("-stream_loop");
            command.add("-1");
            command.add("-i");
            command.add(audioPath);
            command.add("-shortest");
//            command.add("-filter_complex");
//            command.add("0:1");
//            commandList.add("-threads 0");
//            commandList.add("-preset ultrafast");
            command.add("-vcodec");
            command.add("copy");
            command.add("-acodec");
            command.add("copy");
            command.add("-map");
            command.add("0:v:0");
            command.add("-map");
            command.add("1:a:0");
//            command.add("-b:v");
//            command.add("2M");
//            command.add("-c:v");
//            command.add("libx264");
//            command.add("-preset");
//            command.add("ultrafast");
//            command.add("-crf");
//            command.add("35");
//            command.add("-async");
//            command.add("1");
            command.add("-y");
            command.add(outputFile.getAbsolutePath());
        }
        Timber.d("AudioVideoMergeWorker: %s", Arrays.toString(command.toArray()));
        mergeCommand = command.toArray(new String[command.size()]);

        String[] finalMergeCommand = mergeCommand;
        return CallbackToFutureAdapter.getFuture(completer -> {
//            Timber.tag(TAG).d("startWork: %s", videoPath);
//            Timber.tag(TAG).d("startWork: %s", audioPath);
//            int rc = FFmpeg.execute(finalMergeCommand);
//

            //1/4/25
//            if (rc == RETURN_CODE_SUCCESS) {
////                FFmpeg.cancel();
//                Timber.d("RETURN_CODE_SUCCESS ");
////                StorageUtils.getInstance(mContext).saveFileInStorageV10(outputFile, outputFile.getAbsolutePath(), Constants.TAG_VIDEO);
//                completer.set(Result.success(createOutputData(outputFile.getAbsolutePath())));
//            } else if (rc == RETURN_CODE_CANCEL) {
//                Timber.e("RETURN_CODE_CANCEL ");
//                FFmpeg.cancel();
//                setError(completer);
//            } else {
//                Timber.e("Command execution failed with rc=%d", rc);
//                setError(completer);
//            }
            FFmpegKit.executeAsync(String.join(" ", finalMergeCommand), session -> {
                ReturnCode returnCode = session.getReturnCode();

                if (ReturnCode.isSuccess(returnCode)) {
                    Timber.d("RETURN_CODE_SUCCESS ");
                    completer.set(Result.success(createOutputData(outputFile.getAbsolutePath())));
                } else if (ReturnCode.isCancel(returnCode)) {
                    Timber.e("RETURN_CODE_CANCEL ");
                    setError(completer);
                } else {
                    Timber.e("Command execution failed with error: %s", session.getFailStackTrace());
                    setError(completer);
                }
            });

            return completer;
        });
    }

    private Data createOutputData(String mixVideoPath) {
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_VIDEO, mixVideoPath);
        return builder.build();
    }

    private void setError(CallbackToFutureAdapter.Completer<Result> completer) {
        completer.set(Result.failure(Data.EMPTY));
    }
}


//public class AudioVideoMergeWorker extends ListenableWorker {
//    private static final String TAG = AudioVideoMergeWorker.class.getSimpleName();
//    private Context mContext;
//
//    public AudioVideoMergeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
//        super(context, workerParams);
//        mContext = context;
//    }
//
//    @NonNull
//    @Override
//    public ListenableFuture<Result> startWork() {
//        final String videoPath = getInputData().getString(Constants.TAG_VIDEO);
//        final String audioPath = getInputData().getString(Constants.TAG_AUDIO);
//        final long lastMinValue = getInputData().getLong(Constants.TAG_VIDEO_START, 0);
//        final long lastMaxValue = getInputData().getLong(Constants.TAG_VIDEO_END, 0);
//        final File outputFile = StorageUtils.getInstance(mContext).getTempFile(mContext, Constants.TAG_MIX_VIDEO + Utility.VIDEO_FORMAT);
//        long videoDuration = lastMaxValue - lastMinValue;
//        long audioDuration = TrimmerUtils.getMediaDuration(new File(audioPath));
//        Timber.tag(TAG).d("startWork: %s, %s", videoDuration, audioDuration);
//        String[] mergeCommand = null;
//        List<String> command = new ArrayList<>();
//        if (audioDuration > videoDuration) {
//            command.add("-ss");
//            command.add(TrimmerUtils.formatCSeconds(lastMinValue));
//            command.add("-i");
//            command.add(videoPath);
//            command.add("-t");
//            command.add(TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue));
//            command.add("-i");
//            command.add(audioPath);
////            commandList.add("-threads 0");
////            commandList.add("-preset ultrafast");
//            command.add("-vcodec");
//            command.add("copy");
//            command.add("-acodec");
//            command.add("copy");
//            command.add("-map");
//            command.add("0:v:0");
//            command.add("-map");
//            command.add("1:a:0");
////            command.add("-b:v");
////            command.add("2M");
////            command.add("-c:v");
////            command.add("libx264");
////            command.add("-preset");
////            command.add("ultrafast");
////            command.add("-crf");
////            command.add("35");
////            command.add("-async");
////            command.add("1");
//            command.add("-shortest");
//            command.add("-y");
//            command.add(outputFile.getAbsolutePath());
//        } else {
//            command.add("-ss");
//            command.add(TrimmerUtils.formatCSeconds(lastMinValue));
//            command.add("-i");
//            command.add(videoPath);
//            command.add("-t");
//            command.add(TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue));
//            command.add("-stream_loop");
//            command.add("-1");
//            command.add("-i");
//            command.add(audioPath);
//            command.add("-shortest");
////            command.add("-filter_complex");
////            command.add("0:1");
////            commandList.add("-threads 0");
////            commandList.add("-preset ultrafast");
//            command.add("-vcodec");
//            command.add("copy");
//            command.add("-acodec");
//            command.add("copy");
//            command.add("-map");
//            command.add("0:v:0");
//            command.add("-map");
//            command.add("1:a:0");
////            command.add("-b:v");
////            command.add("2M");
////            command.add("-c:v");
////            command.add("libx264");
////            command.add("-preset");
////            command.add("ultrafast");
////            command.add("-crf");
////            command.add("35");
////            command.add("-async");
////            command.add("1");
//            command.add("-y");
//            command.add(outputFile.getAbsolutePath());
//        }
//        Timber.d("AudioVideoMergeWorker: %s", Arrays.toString(command.toArray()));
//        mergeCommand = command.toArray(new String[command.size()]);
//
//        String[] finalMergeCommand = mergeCommand;
//        return CallbackToFutureAdapter.getFuture(completer -> {
////            Timber.tag(TAG).d("startWork: %s", videoPath);
////            Timber.tag(TAG).d("startWork: %s", audioPath);
//            int rc = FFmpeg.execute(finalMergeCommand);
//
//            if (rc == RETURN_CODE_SUCCESS) {
////                FFmpeg.cancel();
//                Timber.d("RETURN_CODE_SUCCESS ");
////                StorageUtils.getInstance(mContext).saveFileInStorageV10(outputFile, outputFile.getAbsolutePath(), Constants.TAG_VIDEO);
//                completer.set(Result.success(createOutputData(outputFile.getAbsolutePath())));
//            } else if (rc == RETURN_CODE_CANCEL) {
//                Timber.e("RETURN_CODE_CANCEL ");
//                FFmpeg.cancel();
//                setError(completer);
//            } else {
//                Timber.e("Command execution failed with rc=%d", rc);
//                setError(completer);
//            }
//            return completer;
//        });
//    }
//
//    private Data createOutputData(String mixVideoPath) {
//        Data.Builder builder = new Data.Builder();
//        builder.putString(Constants.TAG_VIDEO, mixVideoPath);
//        return builder.build();
//    }
//
//    private void setError(CallbackToFutureAdapter.Completer<Result> completer) {
//        completer.set(Result.failure(Data.EMPTY));
//    }
//}
