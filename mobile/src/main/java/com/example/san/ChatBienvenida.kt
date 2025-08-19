package com.example.san

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ChatBienvenida : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bienvenida)

        // Botón GET STARTED (sin cambios)
        findViewById<Button>(R.id.btnminji).setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }

        // Botón de retroceso (versión SIN animación)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            val intent = Intent(this, Home::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish() // Cierra esta actividad
        }
    }
}