package com.app.Zego.Pusher

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.Zego.ApiBase.TokenAuthendicatior
import com.app.Zego.ApiBase.UserAgentInterceptor
import com.app.Zego.Model.MessageData
import com.app.addon.livestream.external.utils.Constants
import com.app.model.GetSet
import com.app.utils.api.ApiInterface
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpChannelAuthorizer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PusherRepository(
    private val authTokenProvider: () -> String?,
    private val userIdProvider: () -> String?
) {

    var TAG: String? = "12345 Pklivestreamingactivity PusherRepository"
    private val _connectionState = MutableLiveData<ConnectionStateChange>()
    val connectionState: LiveData<ConnectionStateChange> = _connectionState

    private val _pusherError = MutableLiveData<String>()
    val pusherError: LiveData<String> = _pusherError

    private val _msgReceived = MutableLiveData<JSONObject>()
    val msgReceived: LiveData<JSONObject> = _msgReceived

    private val _viewersinfo = MutableLiveData<JSONObject>()
    val viewersinfo: LiveData<JSONObject> = _viewersinfo

    private val _likeReceived = MutableLiveData<JSONObject>()
    val likeReceived: LiveData<JSONObject> = _likeReceived

    private val _streamInfo = MutableLiveData<JSONObject>()
    val streamInfo: LiveData<JSONObject> = _streamInfo

    private val _updatedGem = MutableLiveData<JSONObject>()
    val updatedGem: LiveData<JSONObject> = _updatedGem


    private lateinit var pusher: Pusher
    private lateinit var pusherService: PusherService

    fun initPusher(liveID: String) {
        val channelAuthorizer = HttpChannelAuthorizer(com.app.utils.Constants.PUSHER_AUTH_URL)
        val options = PusherOptions()
            .setCluster("ap2")
            .setChannelAuthorizer(channelAuthorizer)

        pusher = Pusher(com.app.utils.Constants.PUSHER_KEY, options)

        val httpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(com.app.utils.Constants.PUSHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        pusherService = retrofit.create(PusherService::class.java)

        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                _connectionState.postValue(change)
            }

            override fun onError(message: String?, code: String?, e: Exception?) {
                _pusherError.postValue("Pusher error: $code :: $message")
            }
        }, ConnectionState.ALL)

        // ✅ Keep channel reference
        val channel = pusher.subscribePrivate("private-$liveID", object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                Log.d(TAG, "Raw event: ${event?.eventName}, data: ${event?.data}")
            }

            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                _pusherError.postValue("Auth failed: $message")
            }

            override fun onSubscriptionSucceeded(channelName: String?) {
                Log.d(TAG, "Subscribed to $channelName")
            }
        })

        channel.bind("msgReceived", object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                event?.let {
                    try {
                        val json = JSONObject(it.data)
                        _msgReceived.postValue(json)
                        Log.d(TAG, "msgReceived: $json")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                Log.e(TAG, "Auth failed for msgReceived: $message", e)
            }

            override fun onSubscriptionSucceeded(channelName: String?) {
                Log.d(TAG, "Subscription succeeded for $channelName")
            }
        })


        channel.bind("viewersinfo", object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                event?.let {
                    try {
                        val json = JSONObject(it.data)
                        _viewersinfo.postValue(json)
                        Log.d(TAG, "viewersinfo: $json")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                Log.e(TAG, "Auth failed for msgReceived: $message", e)
            }

            override fun onSubscriptionSucceeded(channelName: String?) {
                Log.d(TAG, "Subscription succeeded for $channelName")
            }
        })

        channel.bind("likeReceived", object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                event?.let {
                    try {
                        val json = JSONObject(it.data)
                        _likeReceived.postValue(json)
                        Log.d(TAG, "likeReceived: $json")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                Log.e(TAG, "Auth failed for msgReceived: $message", e)
            }

            override fun onSubscriptionSucceeded(channelName: String?) {
                Log.d(TAG, "Subscription succeeded for $channelName")
            }
        })

        channel.bind("streamInfo", object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                event?.let {
                    try {
                        val json = JSONObject(it.data)
                        _streamInfo.postValue(json)
                        Log.d(TAG, "streamInfo: $json")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                Log.e(TAG, "Auth failed for streamInfo: $message", e)
            }

            override fun onSubscriptionSucceeded(channelName: String?) {
                Log.d(TAG, "Subscription succeeded for $channelName")
            }
        })

        channel.bind("updated_gem", object : PrivateChannelEventListener {
            override fun onEvent(event: PusherEvent?) {
                event?.let {
                    try {
                        val json = JSONObject(it.data)
                        _updatedGem.postValue(json)
                        Log.d(TAG, "updatedGem: $json")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onAuthenticationFailure(message: String?, e: Exception?) {
                Log.e(TAG, "Auth failed for streamInfo: $message", e)
            }

            override fun onSubscriptionSucceeded(channelName: String?) {
                Log.d(TAG, "Subscription succeeded for $channelName")
            }
        })

    }

    fun getPusherService(): PusherService = pusherService

    fun disconnect() {
        pusher.disconnect()
    }

    fun getSocketId(): String? {
        return if (::pusher.isInitialized) {
            pusher.connection.socketId
        } else {
            null
        }
    }

}

