package com.example.san

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.san.dao.extraerAlarmasProgramadas
import com.example.san.database.AppDatabase
import com.example.san.model.Configuracion
import com.example.san.sync.AlarmScheduler
import kotlinx.coroutines.launch

class RegistroAlarmaActivity : AppCompatActivity() {

    private lateinit var etHora: EditText
    private lateinit var etMinuto: EditText
    private lateinit var etMensaje: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var btnVolver: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_alarma)

        // Inicializar vistas con nuevos IDs
        etHora = findViewById(R.id.etHora)
        etMinuto = findViewById(R.id.etMinuto)
        etMensaje = findViewById(R.id.etMensaje)
        btnRegistrar = findViewById(R.id.btnRegistro)
        btnVolver = findViewById(R.id.btnVolver)

        btnRegistrar.setOnClickListener {
            registrarAlarma()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun registrarAlarma() {
        val hora = etHora.text.toString().trim()
        val minuto = etMinuto.text.toString().trim()
        val mensaje = etMensaje.text.toString().trim()

        if (hora.isEmpty() || minuto.isEmpty()) {
            Toast.makeText(this, "Hora y minuto son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val horaInt = hora.toIntOrNull()
        val minutoInt = minuto.toIntOrNull()

        if (horaInt == null || horaInt !in 0..23) {
            Toast.makeText(this, "Hora debe estar entre 0 y 23", Toast.LENGTH_SHORT).show()
            return
        }

        if (minutoInt == null || minutoInt !in 0..59) {
            Toast.makeText(this, "Minuto debe estar entre 0 y 59", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val dao = AppDatabase.getDatabase(applicationContext).configuracionDao()

                // Encontrar próximo índice disponible
                var proximoIndex = 1
                val todasLasConfiguraciones = dao.obtenerAlarmas()
                val indicesUsados = todasLasConfiguraciones
                    .filter { it.clave.startsWith("AlarmaEstado") }
                    .mapNotNull { it.clave.removePrefix("AlarmaEstado").toIntOrNull() }

                while (proximoIndex in indicesUsados) {
                    proximoIndex++
                }

                // Guardar alarma
                dao.insertar(Configuracion(clave = "AlarmaEstado$proximoIndex", valor = "true"))
                dao.insertar(Configuracion(clave = "AlarmaHora$proximoIndex", valor = hora))
                dao.insertar(Configuracion(clave = "AlarmaMinutos$proximoIndex", valor = minuto))
                dao.insertar(Configuracion(
                    clave = "AlarmaMensaje$proximoIndex",
                    valor = if (mensaje.isEmpty()) "⏰ Alarma ${hora.padStart(2, '0')}:${minuto.padStart(2, '0')}" else mensaje
                ))

                // ✅ Programar TODAS las alarmas (incluyendo la nueva)
                val todasLasAlarmas = extraerAlarmasProgramadas(dao)
                AlarmScheduler.programarAlarmasConMensajes(applicationContext, todasLasAlarmas)

                // ✅ Notificar éxito y cerrar
                runOnUiThread {
                    Toast.makeText(
                        this@RegistroAlarmaActivity,
                        "⏰ Alarma registrada para ${hora.padStart(2, '0')}:${minuto.padStart(2, '0')}",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@RegistroAlarmaActivity,
                        "Error al registrar alarma: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}