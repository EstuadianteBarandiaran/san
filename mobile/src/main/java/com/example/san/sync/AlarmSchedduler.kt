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
            val h = partes[0].toInt()
            val m = partes[1].toInt()

            val ahora = java.time.LocalDateTime.now()
            var objetivo = ahora.withHour(h).withMinute(m).withSecond(0).withNano(0)
            if (objetivo.isBefore(ahora)) objetivo = objetivo.plusDays(1)

            val delay = java.time.Duration.between(ahora, objetivo).toMillis()
            val data = workDataOf("hora" to alarma.hora, "mensaje" to alarma.mensaje)

            val request = OneTimeWorkRequestBuilder<AlarmTriggerWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}