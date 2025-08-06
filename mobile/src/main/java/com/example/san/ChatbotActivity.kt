package com.example.san

import android.os.Bundle


import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.san.model.ChatMessage



class ChatbotActivity : AppCompatActivity(), Chatbot.ChatbotListener {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatbot: Chatbot

    // Guarda la última pregunta que no entendió
    private var ultimaPreguntaNoEntendida: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        // tu setup normal...
        chatAdapter = ChatAdapter(mutableListOf())
        val recyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        recyclerView.adapter = chatAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        chatbot = Chatbot(this)

        val edtMensaje = findViewById<EditText>(R.id.edtMensaje)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)

        btnEnviar.setOnClickListener {
            val mensaje = edtMensaje.text.toString()
            if (mensaje.isNotBlank()) {
                chatAdapter.agregarMensaje(ChatMessage(mensaje, true))
                edtMensaje.text.clear()

                // Si estamos en modo aprendizaje (esperando respuesta), enviamos esa respuesta
                if (ultimaPreguntaNoEntendida != null) {
                    chatbot.aprender(ultimaPreguntaNoEntendida!!, mensaje)
                    ultimaPreguntaNoEntendida = null
                } else {
                    // Enviar mensaje normal al chatbot
                    chatbot.enviarMensaje(mensaje)
                }
            }
        }

    }

    override fun onRespuestaBot(respuesta: String) {
        chatAdapter.agregarMensaje(ChatMessage(respuesta, false))

        if (respuesta.contains("No entiendo la pregunta", ignoreCase = true)) {
            // Guardamos la última pregunta para el aprendizaje
            ultimaPreguntaNoEntendida = chatAdapter.getUltimoMensajeUsuario()
            // Puedes también mostrar un Toast o mensaje para que el usuario sepa que debe responder para enseñar
            Toast.makeText(this, "Por favor, escribe la respuesta correcta para que aprenda.", Toast.LENGTH_LONG).show()
        }

        scrollAlUltimoMensaje()
    }

    override fun onAprendizajeExitoso(mensaje: String) {
        chatAdapter.agregarMensaje(ChatMessage(mensaje, false))
        scrollAlUltimoMensaje()
    }

    override fun onError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }





    private fun scrollAlUltimoMensaje() {
        val recyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
    }
}
