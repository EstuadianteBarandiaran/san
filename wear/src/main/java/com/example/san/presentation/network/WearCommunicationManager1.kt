package com.example.san.presentation.network

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.tasks.await

class WearCommunicationManager1(
    context: Context,
    private val listener: WearMessageListener
) {
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }
    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(context) }
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        // Registrar listener de mensajes
        messageClient.addListener { event ->
            listener.onMessageReceived(event.path, String(event.data))
        }
    }

    suspend fun sendMessage(path: String, data: String): Boolean {
        return try {
            val nodes = nodeClient.connectedNodes.await()
            Log.d("WearComm", "Nodos conectados: ${nodes.map { it.id }}") // <- ESTE LOG

            if (nodes.isEmpty()) {
                Log.e("WearComm", "No hay nodos conectados")
                return false
            }

            nodes.forEach { node ->
                Log.d("WearComm", "Enviando mensaje a nodo: ${node.displayName}, id: ${node.id}") // <- ESTE LOG
                messageClient.sendMessage(node.id, path, data.toByteArray()).await()
            }

            Log.d("WearComm", "Mensaje enviado correctamente: $path -> $data") // <- ESTE LOG
            true
        } catch (e: Exception) {
            Log.e("WearComm", "Error enviando mensaje: ${e.message}")
            listener.onError(e.message ?: "Error desconocido")
            false
        }
    }


    fun cleanup() {
        messageClient.removeListener { }
        scope.coroutineContext.cancel()
    }
}
