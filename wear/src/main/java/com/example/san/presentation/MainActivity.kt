package com.example.san.presentation

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.san.presentation.network.WearCommunicationManager1
import com.example.san.presentation.network.WearMessageListener
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {

    private lateinit var wearManager: WearCommunicationManager1

    private var imcValue by mutableStateOf<Double?>(null)
    private var caloriesValue by mutableStateOf<Int?>(null)
    private var isLoadingIMC by mutableStateOf(false) // Estado separado para IMC
    private var isLoadingCalories by mutableStateOf(false) // Estado separado para calorías
    private var errorMessage by mutableStateOf<String?>(null)
    private var updateSuccess by mutableStateOf(false)

    // ✅ Nuevo launcher para permisos
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "✅ Permiso POST_NOTIFICATIONS concedido")
        } else {
            Log.e("MainActivity", "❌ Permiso POST_NOTIFICATIONS denegado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crearCanalNotificaciones(applicationContext)

        wearManager = WearCommunicationManager1(this, object : WearMessageListener {
            override fun onMessageReceived(path: String, data: String) {
                // Ya lo maneja WearDataReceiver
            }

            override fun onError(error: String) {
                errorMessage = error
            }
        })

        setContent {
            WearAppUI(
                context = this,
                imcValue = imcValue,
                caloriesValue = caloriesValue,
                isLoadingIMC = isLoadingIMC, // Pasar estado separado para IMC
                isLoadingCalories = isLoadingCalories, // Pasar estado separado para calorías
                errorMessage = errorMessage,
                updateSuccess = updateSuccess,
                onClearSuccess = { updateSuccess = false },
                onClearError = { errorMessage = null },
                onRequestIMC = { sendIMCRequest() },
                onRequestCalories = { launchCaloriesActivity() }
            )
        }

        solicitarPermisoNotificaciones()
    }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun sendIMCRequest() {
        isLoadingIMC = true // Solo activar carga para IMC
        errorMessage = null
        lifecycleScope.launch {
            val success = wearManager.sendMessage("/request_imc", "")
            isLoadingIMC = false // Solo desactivar carga para IMC
            if (!success) errorMessage = "No se pudo solicitar el IMC"
        }
    }

    private val caloriesResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val calories = result.data?.getIntExtra("CALORIES_RESULT", 0) ?: 0
            isLoadingCalories = true // Activar carga para calorías
            lifecycleScope.launch {
                val success = wearManager.sendMessage("/update_calories", calories.toString())
                isLoadingCalories = false // Desactivar carga para calorías
                updateSuccess = success
                if (!success) errorMessage = "No se pudo enviar las calorías"
            }
        }
    }

    private fun launchCaloriesActivity() {
        val intent = Intent(this, CaloriesActivity::class.java)
        intent.putExtra("INITIAL_CALORIES", caloriesValue ?: 0)
        caloriesResultLauncher.launch(intent)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val type = intent?.getStringExtra("type")
            val value = intent?.getStringExtra("value")

            when (type) {
                "imc" -> {
                    imcValue = value?.toDoubleOrNull()
                    isLoadingIMC = false // Asegurar que se desactive la carga cuando llegue la respuesta
                    errorMessage = null
                }
                "calories" -> {
                    caloriesValue = value?.toIntOrNull()
                    isLoadingCalories = false // Asegurar que se desactive la carga cuando llegue la respuesta
                    errorMessage = null
                }
                "update_response" -> {
                    updateSuccess = value == "success"
                    isLoadingCalories = false // Asegurar que se desactive la carga cuando llegue la respuesta
                    errorMessage = null
                }
                "error" -> {
                    errorMessage = value
                    isLoadingIMC = false // Desactivar carga en caso de error
                    isLoadingCalories = false // Desactivar carga en caso de error
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("WearMessage")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarmas"
            val descriptionText = "Notificaciones de alarmas programadas"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel("wear_alarm_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}