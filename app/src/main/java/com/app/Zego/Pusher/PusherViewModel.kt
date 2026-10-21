package com.app.Zego.Pusher

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.app.Zego.Model.MessageData
import com.pusher.client.connection.ConnectionStateChange
import org.json.JSONObject

class PusherViewModel(private val repository: PusherRepository) : ViewModel() {

    val connectionState: LiveData<ConnectionStateChange> = repository.connectionState
    val pusherError: LiveData<String> = repository.pusherError
    val msgReceived: LiveData<JSONObject> = repository.msgReceived
    val viewersinfo: LiveData<JSONObject> = repository.viewersinfo
    val likeReceived: LiveData<JSONObject> = repository.likeReceived
    val streamInfo: LiveData<JSONObject> = repository.streamInfo
    val updatedgem: LiveData<JSONObject> = repository.updatedGem

   /* val msgReceived: LiveData<MessageData> = repository.msgReceived
    val gemsUpdated: LiveData<Int> = repository.gemsUpdated
  */

    fun initPusher(liveID: String?) = repository.initPusher(liveID.toString())
    fun getPusherService() = repository.getPusherService()
    fun getSocketId(): String? = repository.getSocketId()

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }
}
