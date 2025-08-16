package com.example.san

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.san.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class Login : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)



        val button: ImageButton = findViewById(R.id.btnCircle)
        val btnl: Button = findViewById(R.id.btnRegister)
        val user = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)

        button.setOnClickListener {
            val user1: String = user.text.toString()
            val pass: String = password.text.toString()

            if (user1.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "Los datos están en blanco", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Inicia sesión
            authViewModel.loginUser(user1, pass) { errorMsg ->
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }

            // Escucha cuando el usuario ya esté logueado
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    authViewModel.currentUser.collect { firebaseUser ->
                        if (firebaseUser != null) {
                            val uid = firebaseUser.uid
                            authViewModel.checkIfUserDataExists(uid) { existe ->
                                if (existe) {
                                    startActivity(Intent(this@Login, Home::class.java))
                                    finish()

                                } else {
                                    startActivity(Intent(this@Login, RegistroPersonal::class.java))
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
        }

        // Botón para ir al registro
        btnl.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }
}
