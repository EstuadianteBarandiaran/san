package com.example.san.network

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class WearCommunicationManager(private val context: Context) {

    private val messageClient by lazy { Wearable.getMessageClient(context) }
    private val nodeClient by lazy { Wearable.getNodeClient(context) }

    suspend fun sendMessage(path: String, data: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val nodes = nodeClient.connectedNodes.await()
            Log.d("WearComm", "🔗 Nodos conectados: ${nodes.map { it.displayName }}")

            nodes.forEach { node ->
                Log.d("WearComm", "📤 Enviando a ${node.displayName} -> $path : $data")
                messageClient.sendMessage(node.id, path, data.toByteArray()).await()
            }

            Log.d("WearComm", "✅ Mensaje enviado: $path -> $data")
            true
        } catch (e: Exception) {
            Log.e("WearComm", "❌ Error al enviar mensaje: ${e.message}")
            false
        }
    }

    fun cleanup() {
        // Si en el futuro necesitas cerrar conexiones o liberar recursos
    }
}
