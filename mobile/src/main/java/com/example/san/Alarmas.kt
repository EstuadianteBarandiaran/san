package com.example.san

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.san.dao.AlarmaProgramada
import com.example.san.dao.ConfiguracionDao
import com.example.san.dao.extraerAlarmasProgramadas
import com.example.san.database.AppDatabase
import com.example.san.sync.AlarmScheduler
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Alarmas : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlarmaAdapter
    private lateinit var dao: ConfiguracionDao
    private lateinit var fabAgregar: ExtendedFloatingActionButton

    // Contract para recibir resultado de la actividad de registro
    private val registerForActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            cargarAlarmas()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarmas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ INICIALIZAR EL DAO CORRECTAMENTE
        dao = AppDatabase.getDatabase(applicationContext).configuracionDao()

        setupRecyclerView()
        setupButtons()
        cargarAlarmas()
    }

    override fun onResume() {
        super.onResume()
        // ✅ Recargar alarmas cuando volvamos de agregar una nueva
        cargarAlarmas()
    }

    private fun setupButtons() {
        val fabReiniciar: ExtendedFloatingActionButton = findViewById(R.id.fabReiniciar)
        fabReiniciar.setOnClickListener {
            reiniciarAlarmas()
        }

        // Configurar botón de agregar
        fabAgregar = findViewById(R.id.fabAgregar)
        fabAgregar.setOnClickListener {
            agregarNuevaAlarma()
        }

        // Configurar botón de regreso
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rvHistorial)
        adapter = AlarmaAdapter(emptyList()) { alarma ->
            eliminarAlarma(alarma)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun reiniciarAlarmas() {
        lifecycleScope.launch {
            try {
                // 1. Cerrar la base de datos actual
                AppDatabase.getDatabase(applicationContext).close()

                // 2. Eliminar la base de datos
                applicationContext.deleteDatabase("mi_base_datos")

                // 3. Recrear la base de datos
                val newDao = AppDatabase.getDatabase(applicationContext).configuracionDao()
                dao = newDao

                // 4. Esperar para la creación
                delay(500)

                // 5. Recargar la lista (vacía)
                cargarAlarmas()

                // 6. Cancelar todas las alarmas existentes
                AlarmScheduler.cancelAllAlarms(applicationContext)

                Toast.makeText(this@Alarmas, "✅ Alarmas reiniciadas correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@Alarmas, "Error al reiniciar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarAlarmas() {
        lifecycleScope.launch {
            try {
                val alarmas = extraerAlarmasProgramadas(dao)
                Log.d("Alarmas", "Alarmas encontradas: ${alarmas.size}")
                alarmas.forEach { Log.d("Alarmas", "Alarma: ${it.hora} - ${it.mensaje}") }

                actualizarUI(alarmas)

                // ✅ Programar alarmas después de cargarlas
                AlarmScheduler.programarAlarmasConMensajes(applicationContext, alarmas)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Alarmas", "Error cargando alarmas: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@Alarmas, "Error cargando alarmas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun actualizarUI(alarmas: List<AlarmaProgramada>) {
        runOnUiThread {
            adapter.updateList(alarmas)
        }
    }

    private fun eliminarAlarma(alarma: AlarmaProgramada) {
        lifecycleScope.launch {
            try {
                val todasLasConfiguraciones = dao.obtenerAlarmas()

                for (config in todasLasConfiguraciones) {
                    if (config.clave.startsWith("AlarmaEstado")) {
                        val index = config.clave.removePrefix("AlarmaEstado").toIntOrNull() ?: continue

                        val horaConfig = todasLasConfiguraciones.find { it.clave == "AlarmaHora$index" }
                        val minutosConfig = todasLasConfiguraciones.find { it.clave == "AlarmaMinutos$index" }

                        if (horaConfig != null && minutosConfig != null) {
                            val horaFormateada = "${horaConfig.valor.padStart(2, '0')}:${minutosConfig.valor.padStart(2, '0')}"

                            if (horaFormateada == alarma.hora) {
                                // ✅ ELIMINAR TODOS los registros de esta alarma
                                dao.eliminar(config) // Eliminar estado
                                dao.eliminarPorClave("AlarmaHora$index")
                                dao.eliminarPorClave("AlarmaMinutos$index")
                                dao.eliminarPorClave("AlarmaMensaje$index")

                                // ✅ Cancelar la alarma en el sistema
                                AlarmScheduler.cancelAlarm(applicationContext, index)

                                break
                            }
                        }
                    }
                }

                // Recargar la lista
                cargarAlarmas()

                Toast.makeText(this@Alarmas, "Alarma eliminada", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@Alarmas, "Error eliminando alarma", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun agregarNuevaAlarma() {
        val intent = Intent(this, RegistroAlarmaActivity::class.java)
        registerForActivityResult.launch(intent)
    }
}