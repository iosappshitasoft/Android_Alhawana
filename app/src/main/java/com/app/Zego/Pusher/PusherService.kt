package com.app.Zego.Pusher

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PusherService {

    @POST("sendMsg")
    fun sendMessagePusher(@Header("Authorization")  user_token: String, @Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("live_usersetting")
    fun getLiveUserSetting(@Header("Authorization")  user_token: String, @Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("trigger")
    fun triggerEvent(@Header("Authorization")  user_token: String, @Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("subscribeStream")
    fun subscribeStream(@Header("Authorization")  user_token: String, @Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("unsubscribeStream")
    fun unsubscribeStream(@Header("Authorization")  user_token: String, @Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("sendlike")
    fun sendLike(@Header("Authorization")  user_token: String, @Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("update_likes")
    fun updateLikes(@Header("Authorization")  user_token: String, @Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("sendBlastlike")
    fun sendBlastlike(@Header("Authorization")  user_token: String, @Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("sendGift")
    fun sendGiftPusher(@Header("Authorization")  user_token: String, @Body eventData: JsonObject): Call<JsonObject>

    @POST("startBattle")
    fun startBattlePusher(@Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("endBattle")
    fun endBattlePusher(@Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("acceptBattle")
    fun acceptBattlePusher(@Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("stopstream")
    fun stopstreamPusher(@Header("Authorization")  user_token: String, @Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("sendTwoxgem")
    fun sendTwoxgemPusher(@Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("sendThreexgem")
    fun sendThreexgemPusher(@Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("declinematch")
    fun declinematchPusher(@Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("getstreamInfo")
    fun getStreamInfo(@Header("Authorization")  user_token: String, @Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("getpkstreamInfo")
    fun getPkStreamInfo(@Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("blockuser")
    fun sendBlockPusher(@Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("kickout")
    fun sendKickOut(@Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("streammoderator")
    fun makeAsModeratorNew(@Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("sendMultiGift")
    fun sendMultiGift(@Body eventData: JsonObject): Call<JsonObject>

    @POST("sendMultiTwoxgem")
    fun sendMultiTwoxgemPusher(@Body eventData: JsonObject): Call<HashMap<String, String>>

    @POST("sendMultiThreexgem")
    fun sendMultiThreexgemPusher(@Body eventData: JsonObject): Call<HashMap<String, String>>
}
