package com.example.san

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.san.viewmodel.AuthViewModel


import kotlin.getValue

class Home : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        val btnLogOut: Button=findViewById(R.id.btnLogOut)

        btnLogOut.setOnClickListener {
            authViewModel.logOut()

        }
        lifecycleScope.launchWhenStarted {
            authViewModel.currentUser.collect { user ->
                if (user == null) {
                    // Usuario logueado, cambia de pantalla
                    val intent = Intent(this@Home, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }


    }
}