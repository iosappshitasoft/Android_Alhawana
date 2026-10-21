package com.app.Zego.ApiBase

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.app.addon.livestream.external.utils.ApiInterface
import com.app.alhawanajd.MainActivity
import com.app.model.GetSet
import com.app.utils.Constants
import com.app.utils.SharedPref

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


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
 * Created by Karthik Muru on 10/02/25.
 * cashgate-chobi-android project.
 */
class TokenAuthendicatior(val context: Context? =null) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // Check if response is actually 401
       /* if (response.code != 401||response.code != 400||response.code != 403) {
            return null
        }*/

        Log.d("JWTAuthenticator", "401 detected, refreshing token...")

        // Prevent infinite loop
      /*  if (response.request.header(Constants.TAG_AUTHORIZATION) != null) {
            return null
        }*/

        // Refresh the token
        val tokenResponse = refreshToken()

        if(tokenResponse == null){
            //todo unauth logout
            // Send broadcast to logout user

            context?.let {
                val debouncer = Debouncer(500) // 500ms delay
                debouncer.debounce {
                    Log.e("JWTAuthenticator", "authenticate: ::::"+Gson().toJson(tokenResponse) )
                    // Your task here
                    println("Executing debounced task")
                    Toast.makeText(context,"Access Denied,You need to login again!",Toast.LENGTH_SHORT).show()
                    Intent(context, MainActivity::class.java).apply {
                        GetSet.reset()
                        SharedPref.clearAll()
                        context.startActivity(this)
                    }
                }
            }



      /*      val intent = Intent("ACTION_LOGOUT")
            context.sendBroadcast(intent)*/
            return null
        }else{
            Log.d("JWTAuthenticator", "Token refreshed: ${tokenResponse.accessToken}")

            // Save new token
            GetSet.setAuthToken(tokenResponse.accessToken)
            SharedPref.putString(SharedPref.AUTH_TOKEN,GetSet.getAuthToken())
            SharedPref.putString(SharedPref.TAG_ACCESS_TOKENS,GetSet.getAuthToken())

            // Retry the failed request with new token
            return response.request.newBuilder()
                .header(Constants.TAG_AUTHORIZATION, tokenResponse.accessToken)
                .build()
        }


    }



    private fun refreshToken(): TokenResponse? {
        val apiService = Retrofit.Builder()
            .baseUrl(Constants.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)

        val request = TokenRequest(SharedPref.getString("refresh_token",""), GetSet.getUserId())
        Log.e("JWTAuthenticator", "refreshToken: :::::::"+Gson().toJson(request) )
        val call = apiService.refreshToken(request)

        return try {
            val response = call.execute()
            Log.e("JWTAuthenticator", "refreshing token "+Gson().toJson(response.body()))
            if (response.isSuccessful) response.body() else null
        } catch (e: IOException) {
            Log.e("JWTAuthenticator", "Error refreshing token", e)
            null
        }
    }


data class TokenResponse(
        @SerializedName("status")
        val status:String,
        @SerializedName("accesstoken")
        val accessToken:String,
    )
    data class TokenRequest(
        @SerializedName("refreshtoken")
        val refreshtoken:String,
        @SerializedName("userid")
        val userid:String,
    )
}

class Debouncer(private val delayMillis: Long) {
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    fun debounce(task: () -> Unit) {
        runnable?.let { handler.removeCallbacks(it) } // Cancel previous task
        runnable = Runnable { task() }
        handler.postDelayed(runnable!!, delayMillis)
    }
}