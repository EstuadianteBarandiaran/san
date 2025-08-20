package com.example.san

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.san.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class Perfil : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val tvNombre = findViewById<TextView>(R.id.tvNombre)
        val tvEdad = findViewById<TextView>(R.id.tvEdad)
        val tvEstatura = findViewById<TextView>(R.id.tvEstatura)
        val tvPeso = findViewById<TextView>(R.id.tvPeso)
        val tvIMC = findViewById<TextView>(R.id.tvIMC)
        val tvComida = findViewById<TextView>(R.id.tvComida)
        val tvAgua = findViewById<TextView>(R.id.tvAgua)
        val btnCaloria = findViewById<Button>(R.id.btnCaloria)
        val btnLogOut = findViewById<ImageButton>(R.id.btnLogOut) // Cambiado a ImageButton

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("Perfil", "UID actual: $uid")

        if (uid != null) {
            viewModel.fetchUserData(uid)
        } else {
            Log.e("Perfil", "Usuario no autenticado")
        }

        // Configurar el botón de cierre de sesión (ImageButton)
        btnLogOut.setOnClickListener {
            viewModel.logOut()
        }

        // Configurar la barra de navegación inferior
        setupBottomNavigation()

        // Observar cambios en el usuario
        lifecycleScope.launchWhenStarted {
            viewModel.currentUser.collect { user ->
                if (user == null) {
                    // Redirigir al MainActivity si el usuario cierra sesión
                    val intent = Intent(this@Perfil, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.userData.collect { user ->
                if (user != null) {
                    Log.d("Perfil", "Datos recibidos: $user")
                    tvNombre.text = "Nombre: ${user.nombre}"
                    tvEdad.text = "Edad: ${user.edad} años"
                    tvEstatura.text = "Estatura: ${user.estatura} m"
                    tvPeso.text = "Peso: ${user.peso} kg"
                    tvIMC.text = "IMC: ${user.imc}"
                    tvComida.text = "Comida diaria: ${user.cantidadComida} kcal"
                    tvAgua.text = "Agua diaria: ${user.cantidadLitros} L"
                } else {
                    Log.e("Perfil", "No se encontraron datos del usuario")
                    tvNombre.text = "Nombre: -"
                    tvEdad.text = "Edad: -"
                    tvEstatura.text = "Estatura: -"
                    tvPeso.text = "Peso: -"
                    tvIMC.text = "IMC: -"
                    tvComida.text = "Comida diaria: -"
                    tvAgua.text = "Agua diaria: -"
                }
            }
        }

        btnCaloria.setOnClickListener {
            startActivity(Intent(this, HistorialCalorias::class.java))
        }
    }

    private fun setupBottomNavigation() {
        // Botón de Inicio
        findViewById<LinearLayout>(R.id.homeButton).setOnClickListener {
            val intent = Intent(this@Perfil, Home::class.java)
            startActivity(intent)
            finish() // Opcional: si quieres que al ir a Home se cierre Perfil
        }

        // Botón de Escáner Nutricional
        findViewById<LinearLayout>(R.id.planButton).setOnClickListener {
            val intent = Intent(this@Perfil, Nutricion::class.java)
            startActivity(intent)
        }

        // Botón de Chatbot
        findViewById<LinearLayout>(R.id.recipesButton).setOnClickListener {
            val intent = Intent(this@Perfil, ChatBienvenida::class.java)
            startActivity(intent)
        }

        // Botón de Perfil (ya estamos aquí, no hace nada o puedes recargar)
        findViewById<LinearLayout>(R.id.profileButton).setOnClickListener {
            // Ya estamos en Perfil, puedes recargar los datos si quieres
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                viewModel.fetchUserData(uid)
            }
        }
    }
}