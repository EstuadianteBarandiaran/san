package com.example.san.presentation.network



interface WearMessageListener {
    fun onMessageReceived(path: String, data: String)
    fun onError(error: String)
}
