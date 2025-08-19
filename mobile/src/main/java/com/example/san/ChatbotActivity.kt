package com.example.san

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.san.model.ChatMessage

class ChatbotActivity : AppCompatActivity(), Chatbot.ChatbotListener {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatbot: Chatbot
    private var ultimaPreguntaNoEntendida: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        // Configurar botón de retroceso
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        // Configuración del chat
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

                if (ultimaPreguntaNoEntendida != null) {
                    chatbot.aprender(ultimaPreguntaNoEntendida!!, mensaje)
                    ultimaPreguntaNoEntendida = null
                } else {
                    chatbot.enviarMensaje(mensaje)
                }
            }
        }
    }

    // Resto de tus métodos existentes (sin cambios)
    override fun onRespuestaBot(respuesta: String) {
        chatAdapter.agregarMensaje(ChatMessage(respuesta, false))

        if (respuesta.contains("No entiendo la pregunta", ignoreCase = true)) {
            ultimaPreguntaNoEntendida = chatAdapter.getUltimoMensajeUsuario()
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