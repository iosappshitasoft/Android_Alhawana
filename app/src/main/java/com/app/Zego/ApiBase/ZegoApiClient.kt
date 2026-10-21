package com.app.Zego.ApiBase

import android.content.Context
import android.util.Log
import com.app.Zego.ApiBase.TokenAuthendicatior
import com.app.Zego.ApiBase.UserAgentInterceptor
import com.app.addon.livestream.external.utils.Constants
import com.app.model.GetSet
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/*
┏━━━┓━━━━━━━━┏┓┏━┓━━━━━━┏┓━━━━━━━━
┃┏━┓┃━━━━━━━━┃┃┃┏┛━━━━━━┃┃━━━━━━━━
┃┃━┃┃┏━━┓┏━━┓┃┗┛┛━┏━━┓┏━┛┃┏━━┓┏━━┓
┃┗━┛┃┃┏┓┃┃┏┓┃┃┏┓┃━┃┏┓┃┃┏┓┃┃┏┓┃┃━━┫
┃┏━┓┃┃┗┛┃┃┗┛┃┃┃┃┗┓┃┗┛┃┃┗┛┃┃┃━┫┣━━┃
┗┛━┗┛┃┏━┛┃┏━┛┗┛┗━┛┗━━┛┗━━┛┗━━┛┗━━┛
━━━━━┃┃━━┃┃━━━━━━━━━━━━━━━━━━━━━━━
━━━━━┗┛━━┗┛━━━━━━━━━━━━━━━━━━━━━━━

 * Copyright © 2025 HitaSoft
 * All rights reserved.
 *
 * Created by Karthik Muru on 10/03/25.
 * cashgate-chobi-android project.
 */

object ZegoApiClient {
    private val TAG: String = ZegoApiClient::class.java.simpleName

    private const val HTTP_TIMEOUT_MS = 60000
    private const val HTTP_READ_TIMEOUT = 60000

    fun getUploadClient():Retrofit {

          val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BASIC

                val client =
                    OkHttpClient.Builder()
                        .connectTimeout(2, TimeUnit.MINUTES)
                        .readTimeout(2, TimeUnit.MINUTES)
                        .addInterceptor(logging).addInterceptor(UserAgentInterceptor())
                        .authenticator(TokenAuthendicatior()) // Add JWT Authenticator
                        .addNetworkInterceptor(Interceptor { chain ->
                            var request: Request? = null
                            val original = chain.request()
                            // Request customization: add request headers
                            val requestBuilder = original.newBuilder().addHeader(Constants.TAG_AUTHORIZATION,
                            if (GetSet.getAuthToken() != null) GetSet.getAuthToken() else "").addHeader(Constants.TAG_USER_ID,
                                if (GetSet.getUserId() != null) GetSet.getUserId()
                                else "")


                            //       Timber.tag(TAG).i("InterceptToken: %s", "-" + GetSet.getAuthToken());
                            Log.d(TAG, "intercept_Token: " + GetSet.getAuthToken())
                            Log.d(TAG, "intercept_userId: " + GetSet.getUserId())
                            request = requestBuilder.build()
                            chain.proceed(request)
                        })
                        .build()

               val uploadRetrofit = Retrofit.Builder()
                    .baseUrl(com.app.utils.Constants.API_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            return uploadRetrofit
        }

    fun getClient(context: Context?): Retrofit {

            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val client =
                OkHttpClient.Builder() //                    .certificatePinner(certifyPinner)
                    .connectTimeout(HTTP_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
                    .readTimeout(HTTP_READ_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                    .addInterceptor(logging)
                    .addInterceptor(UserAgentInterceptor())
                    .authenticator(TokenAuthendicatior(context)) // Add JWT Authenticator
                    .addNetworkInterceptor(Interceptor { chain ->
                        val original = chain.request()
                        val requestBuilder = original.newBuilder()
                            .addHeader(
                                Constants.TAG_AUTHORIZATION,
                                if (GetSet.getAuthToken() != null) GetSet.getAuthToken() else ""
                            )
                            .addHeader(
                                Constants.TAG_USER_ID,
                                if (GetSet.getUserId() != null) GetSet.getUserId() else ""
                            )
                            .addHeader("Connection", "close")

                        Timber.tag(TAG).i("getClient: %s", "-" + GetSet.getAuthToken())
                        Log.d(TAG, "intercept_userId: " + GetSet.getUserId())

                        val request = requestBuilder.build()
                        chain.proceed(request)
                    })
                    .build()

           val retrofit = Retrofit.Builder()
                .baseUrl(com.app.utils.Constants.API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        return retrofit
    }
}