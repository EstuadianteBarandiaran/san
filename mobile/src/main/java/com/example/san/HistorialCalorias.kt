package com.example.san

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.san.adapter.CaloriasAdapter
import com.example.san.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

class HistorialCalorias : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_calorias)

        // Configurar el botón de regreso (ImageButton)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            // Crear intent para ir a la actividad de perfil
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
            finish() // Opcional: cierra esta actividad
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvHistorial)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            viewModel.fetchCaloriesHistory(uid) { history ->
                recyclerView.adapter = CaloriasAdapter(history)
            }
        }
    }
}