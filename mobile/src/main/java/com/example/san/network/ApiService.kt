package com.example.san.network

import com.example.san.model.MensajeRespuesta
import com.example.san.model.PesoEstatura
import com.example.san.model.ResultadoIMC
import retrofit2.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class ChatRequest(val mensaje: String)
data class ChatResponse(val respuesta: String)


interface ApiService {
    @POST("/chatbot")
    fun enviarMensaje(@Body request: ChatRequest): Call<ChatResponse>

    @POST("/predecir")
    fun predecirIMC(@Body datos: PesoEstatura): Call<ResultadoIMC>

    @POST("chatbot/aprender")
    fun aprender(@Body body: Map<String, String>): Call<Map<String, String>>

    @GET("mensaje_inicial")
    suspend fun obtenerMensajeInicial(): Response<MensajeRespuesta>

}
