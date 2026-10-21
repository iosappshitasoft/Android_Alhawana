package com.app.logging;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;
import timber.log.Timber;

public class FileTree extends Timber.Tree {
    public static final String TAG = FileTree.class.getCanonicalName() + ".logs";
    private static final String LOG_FILE_NAME = "fundoo.log";
    private static final long LOG_FILE_MAX_SIZE_THRESHOLD = 5 * 1024 * 1024;
    private static final long LOG_FILE_RETENTION = TimeUnit.DAYS.toMillis(14);
    /**
     * Flush sends a signal which allows the buffer to release it's
     * contents downstream.
     */
    private static final BehaviorSubject<Long> flush = BehaviorSubject.create();
    /**
     * Signal that the flush has completed
     */
    private static final BehaviorSubject<Long> flushCompleted = BehaviorSubject.create();
    private final SimpleDateFormat LOG_LINE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private final SimpleDateFormat LOG_FILE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
    private final String[] LOG_LEVELS = new String[]{
            "", "", "VERBOSE",
            "DEBUG",
            "INFO",
            "WARN",
            "ERROR",
            "ASSERT"
    };
    /**
     * The Observable which will receive the log messages which are
     * to be written to disk.
     */
    private final PublishSubject<LogElement> mLogBuffer = PublishSubject.create();
    @SuppressWarnings("FieldCanBeLocal")
    private final CompositeDisposable mDisposables;

    private final String filePath;
    private final Context mContext;
    private long processedCount = 0;

    public FileTree(@NonNull Context context) {
        this.mContext = context;
        String tempPath;
        try {
            tempPath = getLogsDirectoryFromPath(context.getFilesDir().getAbsolutePath());
        } catch (FileNotFoundException e) {
            Timber.e(e);
            tempPath = context.getFilesDir().getAbsolutePath();
        }

        filePath = tempPath;


        mDisposables = new CompositeDisposable();
        Disposable d = mLogBuffer
                .observeOn(Schedulers.computation())
                .doAfterNext(logElement -> {
                    processedCount++;

                    if (processedCount % 20 == 0)
                        flush();
                })
                .buffer(flush.mergeWith(Observable.interval(15, TimeUnit.SECONDS)))
                .subscribeOn(Schedulers.io())
                .subscribe(logElement -> {
                    try {
                        // Validate the existence of the file and grab it in
                        // preparation to write.
                        File f = getFile(filePath, LOG_FILE_NAME);

                        // Open the file and write.
                        final FileWriter fw = new FileWriter(f, true);
                        for (LogElement log : logElement) {
                            fw.append(String.format("%s\t%s\t%s\n",
                                    log.getDate(),
                                    LOG_LEVELS[log.getLevel()],
                                    log.getMessage()));
                        }

                        // Write a line indicating the number of log lines processed
                        fw.append(String.format("%s\t%s\t%s\n",
                                LOG_FILE_TIME_FORMAT.format(new Date()),
                                LOG_LEVELS[2],
                                "Flushing logs -- total processed: " + processedCount));
                        fw.flush();
                        flushCompleted.onNext(f.length());

                        Timber.d("FileTree: log save to %s", f.getAbsolutePath() );

                    } catch (Exception e) {
                        // Handle the any IO exceptions here
                        Timber.e(e);
                        Log.e(TAG, "FATAL: Cannot write log file " + filePath);
                    }
                }, throwable -> Log.e(TAG, "FATAL: Failed writing logs", throwable));
        mDisposables.add(d);

        mDisposables.add(
                flushCompleted.subscribeOn(Schedulers.io())
                        .filter(size -> size > LOG_FILE_MAX_SIZE_THRESHOLD)
                        .subscribe(aLong -> rotateLogs(filePath, LOG_FILE_NAME),
                                throwable -> Log.e(TAG, "FATAL: Failed writing logs", throwable))
        );

    }


    /**
     * Schedule this log to be written to disk.
     */
    @Override
    protected void log(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t) {
        // For the sake of simplicity we skip logging the exception,
        // but you can parse the exception and and emit it as needed.
        mLogBuffer.onNext(
                new LogElement(LOG_LINE_FORMAT.format(new Date()),
                        priority,
                        message)
        );
    }

    private void flush() {
        flush.onNext(1L);
    }

    private File getFile(@NonNull String path, @NonNull String name) throws IOException {
        File file = new File(path, name);

        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Unable to load log file.");
        }

        if (!file.canWrite()) {
            throw new IOException("Log file not writable.");
        }
        return file;
    }

    private String getLogsDirectoryFromPath(@NonNull String path) throws FileNotFoundException {
        final File dir = new File(path, "logs");

        if (!dir.exists() && !dir.mkdirs()) {
            throw new FileNotFoundException("Unable to create log file.");
        }
        return dir.getAbsolutePath();
    }

    /**
     * A utility function to rotate application logs. This function
     * operates in three steps.
     * 1. Compresses the existing log file into a gzip format.
     * 2. Truncate the existing log file to size zero to reset it.
     * 3. Grab all the compressed files that are outside the retention
     * period and delete them.
     */
    @SuppressWarnings("SameParameterValue")
    private void rotateLogs(@NonNull String path, @NonNull String name) throws IOException {
        final File file = getFile(path, name);

        if (!compress(file)) {
            // Unable to compress file
            throw new IOException("Failed to compress files for rotation");
        }
        // Truncate the log file to zero.
        new PrintWriter(file).close();

        // Iterate over the gzipped files in the directory and delete
        // the files outside the retention period.
        long currentTime = System.currentTimeMillis();
        if (file.getParentFile() != null && file.getParentFile().listFiles() != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //noinspection ConstantConditions
                Arrays.stream(file.getParentFile().listFiles()).filter(
                        it -> {
                            final String extension = it.getName().substring(it.getName().lastIndexOf('.'));
                            return extension.toLowerCase(Locale.ROOT).equals("gz") &&
                                    it.lastModified() + LOG_FILE_RETENTION < currentTime;
                        }
                ).peek(file1 -> {
                    File dataDir = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    Log.i(TAG, "Data DIR: " + dataDir.getAbsolutePath());
                    copyFile(filePath, file1.getName(), dataDir.getAbsolutePath());
                }).map(File::delete).close();
            } else {
                File[] files = file.getParentFile().listFiles();
                if (files != null)
                    for (File f : files) {
                        final String extension = f.getName().substring(f.getName().lastIndexOf('.'));
                        if (extension.toLowerCase(Locale.ROOT).equals("gz") &&
                                f.lastModified() + LOG_FILE_RETENTION < currentTime) {
                            File dataDir = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                            Log.i(TAG, "Data DIR: " + dataDir.getAbsolutePath());
                            copyFile(filePath, f.getName(), dataDir.getAbsolutePath());
                            //noinspection ResultOfMethodCallIgnored
                            f.delete();
                        }
                    }
            }
    }

    private void exportLogs() throws IOException {
        final File file = getFile(filePath, LOG_FILE_NAME);

        if (!compress(file)) {
            // Unable to compress file
            throw new IOException("Failed to compress files for rotation");
        }
        // Truncate the log file to zero.
        new PrintWriter(file).close();

        // Iterate over the gzipped files in the directory and move them to public Downloads/
        long currentTime = System.currentTimeMillis();
        File dataDir = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        Log.i(TAG, "Data DIR: " + dataDir.getAbsolutePath());
        if (file.getParentFile() != null && file.getParentFile().listFiles() != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //noinspection ConstantConditions
                Arrays.stream(file.getParentFile().listFiles()).filter(
                        it -> {
                            final String extension = it.getName().substring(it.getName().lastIndexOf('.'));
                            return extension.toLowerCase(Locale.ROOT).equals("gz") &&
                                    it.lastModified() + LOG_FILE_RETENTION < currentTime;
                        }
                ).peek(f -> copyFile(f.getAbsolutePath(), f.getName(), dataDir.getAbsolutePath())).map(File::delete).close();
            } else {
                File[] files = file.getParentFile().listFiles();
                if (files != null)
                    for (File f : files) {
                        final String extension = f.getName().substring(f.getName().lastIndexOf('.'));
                        if (extension.toLowerCase(Locale.ROOT).equals("gz") &&
                                f.lastModified() + LOG_FILE_RETENTION < currentTime) {
                            copyFile(filePath, f.getName(), dataDir.getAbsolutePath());
                            //noinspection ResultOfMethodCallIgnored
                            f.delete();
                        }
                    }
            }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean compress(File file) {
        try {
            // Create a new file for the compressed logs.
            @SuppressWarnings("ConstantConditions") File f = new File(file.getParentFile().getAbsolutePath(),
                    String.format("%s_%s.gz",
                            file.getName().substring(0, file.getName().lastIndexOf('.')),
                            LOG_FILE_TIME_FORMAT.format(new Date()))
            );

            FileInputStream inputStream = new FileInputStream(file);
            FileOutputStream outputStream = new FileOutputStream(f);
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);

            final byte[] buffer = new byte[1024];
            int length = inputStream.read(buffer);

            while (length > 0) {
                gzipOutputStream.write(buffer, 0, length);
                length = inputStream.read(buffer);
            }

            // Finish the compressing and close all stream
            gzipOutputStream.finish();
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // Not written by author
    private void copyFile(String inputPath, String inputFile, String outputPath) {

        InputStream in;
        OutputStream out;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + "/" + inputFile);
            out = new FileOutputStream(outputPath + "/" + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file (You have now copied the file)
            Log.i(TAG, "Copied file: " + outputPath + "/" + inputFile);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null)
                Log.e(TAG, e.getMessage());
        }
    }

    private static class LogElement {
        @NonNull
        private String date;
        private int level;
        @Nullable
        private String message;

        LogElement(@NonNull String date, int level, @Nullable String message) {
            this.date = date;
            this.level = level;
            this.message = message;
        }

        @NonNull
        public String getDate() {
            return date;
        }

        public void setDate(@NonNull String date) {
            this.date = date;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public @Nullable String getMessage() {
            return message;
        }

        public void setMessage(@Nullable String message) {
            this.message = message;
        }
    }
}

