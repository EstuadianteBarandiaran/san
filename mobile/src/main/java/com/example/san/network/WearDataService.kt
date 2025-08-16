package com.example.san.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class WearDataService : WearableListenerService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val firestore by lazy { Firebase.firestore }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        val data = String(messageEvent.data)
        Log.d("WearService", "📥 Mensaje recibido: $path -> $data")

        when (path) {
            "/request_imc" -> {
                Log.d("WearService", "🔍 Procesando solicitud de IMC")
                getIMCFromFirestore()
            }
            "/request_calories" -> {
                Log.d("WearService", "🔍 Procesando solicitud de calorías")
                processCaloriesRequest()
            }
            "/update_calories" -> {
                Log.d("WearService", "🔍 Procesando actualización de calorías")
                updateCaloriesInFirestore(data)
            }
            else -> Log.w("WearService", "⚠️ Ruta desconocida: $path")
        }
    }

    private fun getIMCFromFirestore() {
        serviceScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: run {
                    Log.e("WearService", "❌ Usuario no autenticado")
                    sendError("/response_imc", "Usuario no autenticado")
                    return@launch
                }

                val snapshot = firestore.collection("User")
                    .document(uid)
                    .get()
                    .await()

                val imc = snapshot.getDouble("imc") ?: run {
                    Log.e("WearService", "❌ Campo IMC no encontrado en Firestore")
                    sendError("/response_imc", "IMC no disponible")
                    return@launch
                }

                val formattedIMC = "%.2f".format(imc)
                Log.d("WearService", "✅ IMC obtenido: $formattedIMC")
                sendMessage("/response_imc", formattedIMC)

            } catch (e: Exception) {
                Log.e("WearService", "❌ Error al obtener IMC", e)
                sendError("/response_imc", "Error al obtener IMC: ${e.localizedMessage}")
            }
        }
    }


    private fun processCaloriesRequest() {
        serviceScope.launch {
            try {
                val calories = 1800 // TODO: reemplazar con cálculo real
                sendMessage("/response_calories", calories.toString())
            } catch (e: Exception) {
                sendError("/response_calories", "Error al obtener calorías")
            }
        }
    }

    private fun updateCaloriesInFirestore(caloriesData: String) {
        serviceScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: run {
                    sendError("/update_calories_response", "Usuario no autenticado")
                    return@launch
                }

                val newCalories = caloriesData.toIntOrNull() ?: run {
                    sendError("/update_calories_response", "Datos inválidos")
                    return@launch
                }

                val todayDateOnly  = java.time.LocalDate.now().toString()
                val docRef = firestore.collection("User")
                    .document(uid)
                    .collection("Calories")
                    .document(todayDateOnly)

                val snapshot = docRef.get().await()
                val currentCalories = snapshot.getLong("calories")?.toInt() ?: 0
                val updatedCalories = currentCalories + newCalories

                docRef.set(
                    mapOf(
                        "calories" to updatedCalories,
                        "timestamp" to todayDateOnly
                    )
                ).await()

                Log.d("WearService", "✅ Calorías actualizadas: $updatedCalories")
                sendMessage("/update_calories_response", "success")

            } catch (e: Exception) {
                Log.e("WearService", "❌ Error al actualizar calorías", e)
                sendError("/update_calories_response", "Error al actualizar: ${e.localizedMessage}")
            }
        }
    }


    private fun sendMessage(path: String, payload: String) {
        val nodeClient = Wearable.getNodeClient(this)
        val messageClient = Wearable.getMessageClient(this)

        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                messageClient.sendMessage(node.id, path, payload.toByteArray())
                    .addOnSuccessListener {
                        Log.d("WearService", "✅ Mensaje enviado: $path -> $payload")
                    }
                    .addOnFailureListener {
                        Log.e("WearService", "❌ Error al enviar: $path", it)
                    }
            }
        }
    }

    private fun sendError(path: String, message: String) {
        serviceScope.launch {
            sendMessage(path, "ERROR: $message")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wear Connection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para comunicación con Wear OS"
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Comunicación Wear OS")
            .setContentText("Conectado con tu reloj")
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }


    companion object {
        private const val NOTIFICATION_ID = 1234
        private const val CHANNEL_ID = "wear_comm_channel"

        fun startService(context: Context) {
            val intent = Intent(context, WearDataService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d("WearService", "🚀 Servicio iniciado desde el teléfono")
        }
    }
    suspend fun enviarAlarmasAlReloj(context: Context, alarmas: List<String>) = withContext(Dispatchers.IO) {
        val messageClient = Wearable.getMessageClient(context)
        val nodeClient = Wearable.getNodeClient(context)

        val mensaje = "Alarmas: " + alarmas.joinToString(", ")

        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                messageClient.sendMessage(node.id, "/alarmas_configuradas", mensaje.toByteArray())
                    .addOnSuccessListener {
                        Log.d("AlarmaSync", "✅ Alarmas enviadas al reloj: $mensaje")
                    }
                    .addOnFailureListener {
                        Log.e("AlarmaSync", "❌ Error al enviar alarmas", it)
                    }
            }
        }
    }



}
