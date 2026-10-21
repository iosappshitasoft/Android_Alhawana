package com.app.helper.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.alhawanajd.Utility;
import com.app.helper.StorageUtils;
import com.app.utils.Constants;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.util.Arrays;

public class TempVideoWorker extends Worker {

    public TempVideoWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        try {
            String url = getInputData().getString("url");

            File tempVideo = new File(StorageUtils.getInstance(getApplicationContext()).getExtCacheDir(), Constants.TAG_VIDEO_TRIM + Utility.VIDEO_FORMAT);
//            inputVideoFile =
            String[] cmd = {
                    "-y",
                    "-i",
                    url,
                    "-c", "copy",
                    tempVideo.getAbsolutePath()
            };

            FFmpegSession session = FFmpegKit.execute(String.join(" ", cmd));

            if (ReturnCode.isSuccess(session.getReturnCode())) {

                return Result.success(
                        new Data.Builder()
                                .putString("temp_path", tempVideo.getAbsolutePath())
                                .build()
                );

            } else {
                return Result.failure();
            }

        } catch (Exception e) {
            return Result.failure();
        }
    }
}


