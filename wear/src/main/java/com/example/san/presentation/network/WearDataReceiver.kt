package com.example.san.presentation.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.san.presentation.alarm.AlarmManagerHelper
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.time.LocalTime

class WearDataReceiver : WearableListenerService() {

    override fun onCreate() {
        super.onCreate()
        crearCanalDeNotificacion(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        val path = messageEvent.path
        val data = String(messageEvent.data)

        Log.d("WearDataReceiver", "📩 Mensaje recibido: $path -> $data")

        when (path) {
            "/response_imc" -> {
                Log.d("WearDataReceiver", "🔍 Procesando IMC")
                sendToUI("imc", data)
            }
            "/response_calories" -> {
                Log.d("WearDataReceiver", "🔍 Procesando calorías")
                sendToUI("calories", data)
            }
            "/update_calories_response" -> {
                Log.d("WearDataReceiver", "🔄 Procesando actualización de calorías")
                sendToUI("update_response", data)
            }
            "/error" -> {
                Log.d("WearDataReceiver", "⚠️ Procesando error")
                sendToUI("error", data)
            }
            "/alarmas_configuradas" -> {
                Log.d("WearDataReceiver", "⏰ Procesando alarmas")
                mostrarNotificacionAlarma(data)
            }
            "/sync_alarmas" -> {
                Log.d("WearDataReceiver", "🔄 Sincronizando alarmas")

                val partes = data.split("|")
                val activasRaw = partes.getOrNull(0)?.removePrefix("ACTIVAS:") ?: ""
                val canceladasRaw = partes.getOrNull(1)?.removePrefix("CANCELADAS:") ?: ""

                val horas = activasRaw.split(",").filter { it.isNotBlank() }
                val horasLocalTime = horas.mapNotNull {
                    runCatching { LocalTime.parse(it) }.getOrNull()
                }

                val idsCanceladas = canceladasRaw.split(",").mapNotNull {
                    runCatching { it.toInt() }.getOrNull()
                }

                AlarmManagerHelper.cancelarAlarmas(context = this, indices = idsCanceladas)
                AlarmManagerHelper.programarAlarmas(context = this, horas = horasLocalTime)
            }
            else -> {
                Log.w("WearDataReceiver", "❓ Ruta desconocida: $path")
            }
        }
    }

    private fun sendToUI(type: String, value: String) {
        val intent = Intent("WearMessage")
        intent.putExtra("type", type)
        intent.putExtra("value", value)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun mostrarNotificacionAlarma(texto: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏰ Alarmas Programadas")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun crearCanalDeNotificacion(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarmas Wear",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de alarmas enviadas desde el teléfono"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "wear_alarm_channel"
        private const val NOTIFICATION_ID = 5678
    }
}
