package com.example.san.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.san.network.WearCommunicationManager



class AlarmTriggerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hora = inputData.getString("hora") ?: return Result.failure()
        val mensaje = inputData.getString("mensaje") ?: "⏰ Alarma"

        val enviado = WearCommunicationManager(applicationContext).sendMessage(
            "/alarmas_configuradas",
            mensaje
        )

        return if (enviado) Result.success() else Result.retry()
    }
}
