package com.app.addon.livestream.external.utils;
import static com.app.utils.Constants.API_URL;

import android.util.Log;


import com.app.model.GetSet;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

    private static Retrofit retrofit = null;
    private static Retrofit uploadRetrofit = null;
    private static final int HTTP_TIMEOUT_MS = 10000;

    private static final int HTTP_READ_TIMEOUT = 30000;

    public static ApiInterface getWebService() { return getClient().create(ApiInterface.class); }

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.level(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .addInterceptor(logging)
                    .readTimeout(3, TimeUnit.MINUTES)
                    .writeTimeout(3, TimeUnit.MINUTES)
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
//                            Request request = null;
//                            Request original = chain.request();
//                            Timber.tag(TAG).d("Authorization: %s", GetSet.getAuthToken());
                            // Request customization: add request headers
                            Request request = null;
                            Request original = chain.request();
                            // Request customization: add request headers
                            Request.Builder requestBuilder = original.newBuilder();
                            request = requestBuilder.build();
                            return chain.proceed(request);
//
//                            Timber.tag(TAG).i("getClient: %s", "-" + com.app.model.GetSet.getAuthToken());
//                            Log.d(TAG, "intercept_userId: "+ GetSet.getUserId());
//                            request = requestBuilder.build();
//                            return chain.proceed(request);
                        }
                    })
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                   // .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getUploadClient() {
        if (uploadRetrofit == null) {
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .callTimeout(60, TimeUnit.MINUTES)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(3, TimeUnit.MINUTES)
                    .writeTimeout(3, TimeUnit.MINUTES).build();

            uploadRetrofit = new Retrofit.Builder()
                    .client(httpClient)
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return uploadRetrofit;
    }

    public static Retrofit getClientnew() {

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
                                    .addHeader(com.app.utils.Constants.TAG_AUTHORIZATION,
                                            GetSet.getAuthToken() != null ? GetSet.getAuthToken() : "")
                                    .addHeader("Connection", "close").
                                    addHeader(com.app.utils.Constants.TAG_USER_ID,GetSet.getUserId() != null ? GetSet.getUserId() : "");


//                            Timber.tag(TAG).i("getClient: %s", "-" + GetSet.getAuthToken());
//                            Log.d(TAG, "intercept_userId: "+GetSet.getUserId());
                            request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
