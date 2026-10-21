package com.app.utils.api;

import android.util.Log;

import com.app.model.GetSet;
import com.app.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Created by hitasoft on 12/3/18.
 */

public class ApiClient {

    private static final String TAG = ApiClient.class.getSimpleName();
    private static Retrofit retrofit = null;
    private static Retrofit uploadRetrofit = null;
    private static final int HTTP_TIMEOUT_MS = 30000;
    private static final int HTTP_READ_TIMEOUT = 30000;

    public static Retrofit getClient() {

        if (retrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(/*new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(@NonNull String s) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        Timber.tag(TAG).d("%s", App.getGsonPrettyInstance().toJson(jsonObject));
                    } catch (JSONException e) {
                        Timber.tag(TAG).d("%s", s);
                    }
                }
            }*/);
            logging.level(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .readTimeout(HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                    .addInterceptor(logging)
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(@NotNull Chain chain) throws IOException {
                            Request request = null;
                            Request original = chain.request();
//                            Timber.tag(TAG).d("Authorization: %s", GetSet.getAuthToken());
                            // Request customization: add request headers
                            Request.Builder requestBuilder = original.newBuilder()
                                    .addHeader(Constants.TAG_AUTHORIZATION,
                                            GetSet.getAuthToken() != null ? GetSet.getAuthToken() : "")
                                    .addHeader("Connection", "close").
                                    addHeader(Constants.TAG_USER_ID,GetSet.getUserId() != null ? GetSet.getUserId() : "");


                            Timber.tag(TAG).i("getClient: %s", "-" + GetSet.getAuthToken());
                            Log.d(TAG, "intercept_userId: "+GetSet.getUserId());
                            request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.API_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getUploadClient() {

        if (uploadRetrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.level(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(2, TimeUnit.MINUTES)
                    .readTimeout(2, TimeUnit.MINUTES)
                    .addInterceptor(logging)
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(@NotNull Chain chain) throws IOException {
                            Request request = null;
                            Request original = chain.request();
                            // Request customization: add request headers
                            Request.Builder requestBuilder = original.newBuilder().
                                    addHeader(Constants.TAG_AUTHORIZATION,
                                            GetSet.getAuthToken() != null ? GetSet.getAuthToken() : "").
                                    addHeader(Constants.TAG_USER_ID,GetSet.getUserId() != null ? GetSet.getUserId() : "");


                            //       Timber.tag(TAG).i("InterceptToken: %s", "-" + GetSet.getAuthToken());
                            Log.d(TAG, "intercept_Token: "+GetSet.getAuthToken());
                            Log.d(TAG, "intercept_userId: "+GetSet.getUserId());
                            request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                    })
                    .build();

            uploadRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.API_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return uploadRetrofit;
    }

}