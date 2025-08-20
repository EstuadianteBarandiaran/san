package com.example.san.sync

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.san.dao.AlarmaProgramada
import java.util.concurrent.TimeUnit

object AlarmScheduler {

    fun programarAlarmasConMensajes(context: Context, alarmas: List<AlarmaProgramada>) {
        for (alarma in alarmas) {
            val partes = alarma.hora.split(":")
            if (partes.size != 2) continue

            try {
                val h = partes[0].toInt()
                val m = partes[1].toInt()

                val ahora = java.time.LocalDateTime.now()
                var objetivo = ahora.withHour(h).withMinute(m).withSecond(0).withNano(0)
                if (objetivo.isBefore(ahora)) objetivo = objetivo.plusDays(1)

                val delay = java.time.Duration.between(ahora, objetivo).toMillis()
                val data = workDataOf(
                    "hora" to alarma.hora,
                    "mensaje" to alarma.mensaje,
                    "id" to System.currentTimeMillis().toInt() // Add unique ID
                )

                val request = OneTimeWorkRequestBuilder<AlarmTriggerWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("ALARM_${alarma.hora}") // Add tag for easier cancellation
                    .build()

                WorkManager.getInstance(context).enqueue(request)
                Log.d("AlarmScheduler", "Alarma programada para: ${alarma.hora} con mensaje: ${alarma.mensaje}")
            } catch (e: Exception) {
                Log.e("AlarmScheduler", "Error programando alarma: ${alarma.hora}", e)
            }
        }
    }

    fun cancelAlarm(context: Context, index: Int) {
        // Cancel by tag if you're using tags
        WorkManager.getInstance(context).cancelAllWorkByTag("ALARM_$index")
    }

    fun cancelAllAlarms(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
        Log.d("AlarmScheduler", "Todas las alarmas canceladas")
    }
}