package com.app.utils;

import android.util.Log;
import org.json.JSONArray;
import java.io.IOException;
import java.net.URLEncoder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TranslateUtil {

    public interface TranslationCallback {
        void onTranslated(String translatedText, String originalText, String detectedLang);
    }

    public static void translateText(String text, String targetLang, TranslationCallback callback) {
        try {
            String encodedText = URLEncoder.encode(text, "UTF-8");

            String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl="
                    + targetLang + "&dt=t&q=" + encodedText;

            Log.d("TranslateAPI", "Request URL: " + url);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            // ✅ Execute asynchronously
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    callback.onTranslated(null, null, null);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onTranslated(null, null, null);
                        return;
                    }

                    String responseBody = response.body().string();
                    Log.d("TranslateAPI", "Response: " + responseBody);

                    try {
                        // ✅ Parse response
                        JSONArray mainArray = new JSONArray(responseBody);
                        JSONArray firstArray = mainArray.getJSONArray(0);
                        JSONArray translationArray = firstArray.getJSONArray(0);

                        // Extract data
                        String translatedText = translationArray.getString(0);
                        String originalText = translationArray.getString(1);
                        String detectedLang = mainArray.getString(2);

                        callback.onTranslated(translatedText, originalText, detectedLang);

                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onTranslated(null, null, null);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            callback.onTranslated(null, null, null);
        }
    }
}
