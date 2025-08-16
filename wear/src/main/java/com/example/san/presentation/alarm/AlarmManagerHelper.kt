package com.example.san.presentation.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId

object AlarmManagerHelper {

    private const val TAG = "AlarmManagerHelper"

    fun programarAlarmas(context: Context, horas: List<LocalTime>) {
        if (horas.isEmpty()) {
            Log.w(TAG, "⚠️ Lista de horas vacía, no se programaron alarmas")
            return
        }

        for (hora in horas) {
            val triggerMillis = calcularMillisDesdeHora(hora)
            val pendingIntent = crearPendingIntent(context, hora)
            programarAlarmaSegura(context, triggerMillis, pendingIntent)
            Log.d(TAG, "⏰ Programando alarma para $hora")
        }
    }

    fun cancelarAlarmas(context: Context, indices: List<Int>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        indices.forEach { id ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "🛑 Alarma cancelada con ID $id")
        }
    }

    fun programarAlarmaSegura(
        context: Context,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "✅ Exact alarm programada con permiso")
                } else {
                    alarmManager.setWindow(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        5 * 60 * 1000L, // ventana de 5 minutos
                        pendingIntent
                    )
                    Log.w(TAG, "⚠️ Permiso denegado, usando setWindow")
                }
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "✅ Exact alarm programada (API < 31)")
            }

            else -> {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "✅ Alarm programada con set() (API < 19)")
            }
        }
    }

    fun crearPendingIntent(context: Context, hora: LocalTime): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("hora", hora.toString())
        }

        val requestCode = hora.toSecondOfDay()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun calcularMillisDesdeHora(hora: LocalTime): Long {
        val ahora = LocalDateTime.now()
        val fechaHora = ahora.withHour(hora.hour).withMinute(hora.minute).withSecond(0).withNano(0)

        val fechaFinal = if (fechaHora.isBefore(ahora)) {
            fechaHora.plusDays(1)
        } else {
            fechaHora
        }

        return fechaFinal.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
