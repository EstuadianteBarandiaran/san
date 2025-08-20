package com.example.san.dao

import android.util.Log

data class AlarmaProgramada(val hora: String, val mensaje: String)
data class Alarmas1(val hora: String, val mensaje: String, val index: String)

suspend fun extraerAlarmasProgramadas(dao: ConfiguracionDao): List<AlarmaProgramada> {
    val todasLasConfiguraciones = dao.obtenerAlarmas()
    val alarmas = mutableListOf<AlarmaProgramada>()

    Log.d("AlarmUtils", "Total configuraciones: ${todasLasConfiguraciones.size}")

    // Buscar todas las configuraciones de estado de alarma
    val estadosAlarma = todasLasConfiguraciones.filter { it.clave.startsWith("AlarmaEstado") }

    Log.d("AlarmUtils", "Estados de alarma encontrados: ${estadosAlarma.size}")

    for (estado in estadosAlarma) {
        val index = estado.clave.removePrefix("AlarmaEstado").toIntOrNull() ?: continue

        // Solo procesar alarmas activas
        if (estado.valor != "true") {
            Log.d("AlarmUtils", "Alarma $index está inactiva")
            continue
        }

        Log.d("AlarmUtils", "Procesando alarma activa índice: $index")

        // Buscar hora, minutos y mensaje para este índice
        val horaConfig = todasLasConfiguraciones.find { it.clave == "AlarmaHora$index" }
        val minutosConfig = todasLasConfiguraciones.find { it.clave == "AlarmaMinutos$index" }
        val mensajeConfig = todasLasConfiguraciones.find { it.clave == "AlarmaMensaje$index" }

        if (horaConfig != null && minutosConfig != null) {
            val hora = horaConfig.valor
            val minutos = minutosConfig.valor
            val mensaje = mensajeConfig?.valor ?: "⏰ Alarma programada: $hora:$minutos"

            val horaFormateada = "${hora.padStart(2, '0')}:${minutos.padStart(2, '0')}"
            alarmas.add(AlarmaProgramada(horaFormateada, mensaje))

            Log.d("AlarmUtils", "Alarma agregada: $horaFormateada - $mensaje")
        } else {
            Log.d("AlarmUtils", "Faltan datos para alarma índice: $index")
        }
    }

    Log.d("AlarmUtils", "Total alarmas extraídas: ${alarmas.size}")
    return alarmas
}

suspend fun extraerAlarmas(dao: ConfiguracionDao): List<Alarmas1> {
    val todasLasConfiguraciones = dao.obtenerAlarmas()
    val alarmas = mutableListOf<Alarmas1>()

    // Buscar todas las configuraciones de estado de alarma
    val estadosAlarma = todasLasConfiguraciones.filter { it.clave.startsWith("AlarmaEstado") }

    for (estado in estadosAlarma) {
        val index = estado.clave.removePrefix("AlarmaEstado").toIntOrNull() ?: continue

        // Buscar hora, minutos y mensaje para este índice
        val horaConfig = todasLasConfiguraciones.find { it.clave == "AlarmaHora$index" }
        val minutosConfig = todasLasConfiguraciones.find { it.clave == "AlarmaMinutos$index" }
        val mensajeConfig = todasLasConfiguraciones.find { it.clave == "AlarmaMensaje$index" }

        if (horaConfig != null && minutosConfig != null) {
            val hora = horaConfig.valor
            val minutos = minutosConfig.valor
            val mensaje = mensajeConfig?.valor ?: "⏰ Alarma programada: $hora:$minutos"

            val horaFormateada = "${hora.padStart(2, '0')}:${minutos.padStart(2, '0')}"
            alarmas.add(Alarmas1(horaFormateada, mensaje, index.toString()))
        }
    }

    return alarmas
}