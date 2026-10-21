package com.app.alhawanajd;


import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.app.alhawanajd.R;
import com.app.helper.StorageUtils;
import com.app.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class DownloadFiles extends AsyncTask<String, Integer, String> {
    private static final String TAG = "DownloadFiles";
    private final Context mContext;
    private final StorageUtils storageManager;

    public DownloadFiles(Context context) {
        this.mContext = context;
        storageManager = StorageUtils.getInstance(context);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Log.d(TAG, "onProgressUpdate: " + values[0]);
    }

    @Override
    protected String doInBackground(String... strings) {
        String from = strings[1];
        String fileName = getFileName(strings[0]);
        if (!fileName.toLowerCase().endsWith(".mp3")) {
            fileName = fileName + ".mp3";
        }
        boolean downloading = true;
        boolean downloaded = false;
        Log.d(TAG, "doInBackground: "+Build.VERSION.SDK_INT );
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            DownloadManager downloadmanager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(strings[0]));
            request.setTitle(fileName);
            request.setDescription(mContext.getString(R.string.downloading));
            //  request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            long fileId;
            switch (from) {
                case Constants.TAG_AUDIO_RECEIVE: {

                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, storageManager.getPathLivza(from) + fileName);
                    break;
                }
                case Constants.TAG_VIDEO: {
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, storageManager.getPathLivza(from) + fileName);
                    break;
                }

            }
            fileId = downloadmanager.enqueue(request);
            DownloadManager.Query query = null;
            query = new DownloadManager.Query();
            Log.d(TAG, "doInBackground: " + fileId);

            Cursor c = null;
            if (query != null) {
                query.setFilterByStatus(DownloadManager.STATUS_FAILED | DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_SUCCESSFUL | DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING);
            } else {
                downloaded = true;
                Uri downloadedUri = downloadmanager.getUriForDownloadedFile(fileId);
                return downloadedUri.getPath();
            }

            while (downloading) {
                c = downloadmanager.query(query);
                if (c.moveToFirst()) {
                   int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                        downloaded = true;
                        break;
                    }
                    if (status == DownloadManager.STATUS_FAILED) {
                        Log.e(TAG, "STATUS_FAILED");
                        downloading = false;
                        break;
                    }
                }
            }
            if (downloaded) {
                Uri downloadedUri = downloadmanager.getUriForDownloadedFile(fileId);
                Log.d(TAG, "doInBackground: " + downloadedUri);
                return downloadedUri.getPath();
            } else return null;
        }else{
            try {
                URL url = new URL(strings[0]);//Create Download URl
                Log.i(TAG, "doInBackground: " + url);
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();//Open Url Connection
                httpConn.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                httpConn.connect();//connect the URL Connection
                long mTotal = httpConn.getContentLength();
                int responseCode = httpConn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = httpConn.getInputStream();
                    FileOutputStream outputStream = null;
                    File downloadedFile = new File(mContext.getExternalCacheDir(), fileName);
                    outputStream = new FileOutputStream(downloadedFile);
                    int bytesRead = -1;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, bytesRead);
//                    publishProgress((int) (100 * bytesRead / mTotal));

                    }
                    outputStream.flush();
                    inputStream.close();
                    outputStream.close();

                    switch (from) {
                        case Constants.TAG_AUDIO_RECEIVE: {
                            String outputPath = storageManager.saveFileInStorageLivza(downloadedFile, Constants.TAG_AUDIO_RECEIVE);
                            File outputFile = new File(outputPath);
                            return outputFile.getAbsolutePath();
                        }


                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void doProgress(int progress) {
        publishProgress(progress);
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1);
        }
        return imgSplit;
    }

    protected abstract void onPostExecute(String downPath);
}