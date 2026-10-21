package com.app.Zego.Pusher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PusherViewModelFactory(private val repository: PusherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PusherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PusherViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}