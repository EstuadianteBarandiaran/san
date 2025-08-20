package com.example.san.dao


data class AlarmaProgramada(val hora: String, val mensaje: String)

suspend fun extraerAlarmasProgramadas(dao: ConfiguracionDao): List<AlarmaProgramada> {
    val activas = dao.obtenerAlarmasActivas()
    val alarmas = mutableListOf<AlarmaProgramada>()

    for (estado in activas) {
        val index = estado.clave.removePrefix("AlarmaEstado").toIntOrNull() ?: continue
        val hora = dao.obtenerValor("AlarmaHora$index") ?: continue
        val minutos = dao.obtenerValor("AlarmaMinutos$index") ?: continue
        val mensaje = dao.obtenerValor("AlarmaMensaje$index") ?: "⏰ Alarma programada: $hora:$minutos"

        val horaFormateada = "${hora.padStart(2, '0')}:${minutos.padStart(2, '0')}"
        alarmas.add(AlarmaProgramada(horaFormateada, mensaje))
    }

    return alarmas
}

