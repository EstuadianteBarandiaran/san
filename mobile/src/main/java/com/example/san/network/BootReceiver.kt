package com.example.san.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.san.network.WearDataService


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            WearDataService.startService(context)
        }
    }
}