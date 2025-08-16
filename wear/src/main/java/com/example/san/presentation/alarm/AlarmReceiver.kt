package com.example.san.presentation.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlin.math.absoluteValue

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val hora = intent.getStringExtra("hora") ?: "Desconocida"
        Log.d("AlarmReceiver", "⏰ Alarma activada a las $hora")

        val notification = NotificationCompat.Builder(context, "wear_alarm_channel")
            .setContentTitle("⏰ ¡Alarma!")
            .setContentText("Es hora: $hora")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationId = hora.hashCode().absoluteValue

        val canNotify = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (canNotify) {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } else {
            Log.w("AlarmReceiver", "⚠️ Permiso POST_NOTIFICATIONS no concedido")
        }
    }
}
