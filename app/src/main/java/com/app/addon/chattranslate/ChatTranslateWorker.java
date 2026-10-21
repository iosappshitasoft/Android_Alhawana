package com.app.addon.chattranslate;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.os.HandlerCompat;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.app.alhawanajd.R;
import com.app.utils.Constants;
import com.app.utils.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatTranslateWorker extends ListenableWorker {
    private static final String TAG = ChatTranslateWorker.class.getSimpleName();
    private Context mContext;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    public ChatTranslateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        final String message = getInputData().getString(Constants.TAG_MESSAGE);

        return CallbackToFutureAdapter.getFuture(completer -> {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String url = "https://translation.googleapis.com/language/translate/v2?target=" +
                            SharedPref.getString(SharedPref.CHAT_LANGUAGE, SharedPref.DEFAULT_CHAT_LANGUAGE) +
                            "&key=" + mContext.getString(R.string.google_api_key_) + "&q=" + message;
                    Log.i(TAG, "startWork: " + url);
                    BufferedReader reader = null;
                    try {
                        URL urlObj = new URL(url);
                        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                        conn.setDoOutput(true);
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("Accept-Charset", "UTF-8");

                        StringBuilder sb = new StringBuilder();
                        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        String translatedMsg = parseTranslatedMsg(sb.toString());
                        if (!TextUtils.isEmpty(translatedMsg)) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    completer.set(Result.success(createOutputData(translatedMsg)));
                                }
                            });
                        } else {
                            setError(completer);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        setError(completer);
                    }
                }
            });
            return completer;
        });
    }

    private void setError(CallbackToFutureAdapter.Completer<Result> completer) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                completer.set(Result.failure(Data.EMPTY));
            }
        });
    }

    private static String parseTranslatedMsg(String translatedMsg) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(translatedMsg);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            JSONArray translationArray = dataObject.getJSONArray("translations");
            if (translationArray.length() > 0) {
                JSONObject translatedText = translationArray.getJSONObject(0);
                translatedMsg = translatedText.optString("translatedText");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return translatedMsg;
    }

    private Data createOutputData(String translatedMsg) {
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_DATA, translatedMsg);
        return builder.build();
    }
}
