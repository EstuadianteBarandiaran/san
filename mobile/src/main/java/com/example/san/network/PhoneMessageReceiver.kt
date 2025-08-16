package com.example.san.network


import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class PhoneMessageReceiver : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        val data = String(messageEvent.data)

        Log.d("PhoneMessageReceiver", "Mensaje recibido: $path -> $data")

        // Procesar rutas y reenviar a la UI si es necesario
        val intent = Intent("WearMessage")
        intent.putExtra("type", when (path) {
            "/response_imc" -> "imc"
            "/response_calories" -> "calories"
            "/update_calories_response" -> "update_response"
            "/error" -> "error"
            else -> null
        })
        intent.putExtra("value", data)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
