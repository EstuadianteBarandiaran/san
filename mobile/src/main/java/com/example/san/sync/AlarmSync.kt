package com.example.san.sync

import android.content.Context
import android.util.Log
import com.example.san.database.AppDatabase
import com.example.san.network.WearCommunicationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun sincronizarAlarmasConReloj(context: Context): Int = withContext(Dispatchers.IO) {
    val dao = AppDatabase.getDatabase(context).configuracionDao()

    val activas = mutableListOf<String>()
    val canceladas = mutableListOf<Int>()

    for (i in 1..5) {
        val horaStr = dao.obtenerValor("AlarmaHora$i") ?: continue
        val minutosStr = dao.obtenerValor("AlarmaMinutos$i") ?: continue
        val estadoRaw = dao.obtenerValor("AlarmaEstado$i") ?: "false"

        val estado = estadoRaw.trim().equals("true", ignoreCase = true)

        val horaFormateada = "%02d:%02d".format(
            horaStr.toIntOrNull() ?: 0,
            minutosStr.toIntOrNull() ?: 0
        )

        if (estado) {
            activas.add(horaFormateada)
        } else {
            canceladas.add(i)
        }
    }

    val mensaje = "ACTIVAS:${activas.joinToString(",")}|CANCELADAS:${canceladas.joinToString(",")}"
    Log.d("SyncAlarmas", "📤 Enviando al reloj: $mensaje")

    val wearComm = WearCommunicationManager(context)
    val enviado = wearComm.sendMessage("/sync_alarmas", mensaje)

    if (enviado) 1 else -1
}
