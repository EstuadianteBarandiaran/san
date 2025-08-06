package com.example.san

import com.example.san.network.ChatRequest
import com.example.san.network.ChatResponse
import com.example.san.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Chatbot(private val listener: ChatbotListener) {

    interface ChatbotListener {
        fun onRespuestaBot(respuesta: String)
        fun onError(error: String)
        fun onAprendizajeExitoso(mensaje: String) // nuevo callback
    }

    fun enviarMensaje(mensaje: String) {
        val request = ChatRequest(mensaje)
        RetrofitInstance.api.enviarMensaje(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    listener.onRespuestaBot(response.body()!!.respuesta)
                } else {
                    listener.onError("Error en la respuesta")
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                listener.onError("Fallo en la conexión: ${t.message}")
            }
        })
    }


    fun aprender(pregunta: String, respuesta: String) {
        val body = mapOf("pregunta" to pregunta, "respuesta" to respuesta)
        RetrofitInstance.api.aprender(body).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    listener.onAprendizajeExitoso(response.body()?.get("mensaje") ?: "Aprendí algo nuevo.")
                } else {
                    listener.onError("Error en aprendizaje")
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                listener.onError("Fallo en la conexión de aprendizaje: ${t.message}")
            }
        })
    }
}

