package com.example.san

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.san.network.WearDataService
import com.example.san.viewmodel.AuthViewModel
import kotlinx.coroutines.launch


import kotlin.getValue

import com.example.san.sync.sincronizarAlarmasConReloj // 👈 importa la función
import java.util.Calendar

class Home : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val btnLogOut: Button = findViewById(R.id.btnLogOut)
        val btnSyncAlarmas: Button = findViewById(R.id.btnSyncAlarmas)



        btnLogOut.setOnClickListener {
            authViewModel.logOut()
        }

        btnSyncAlarmas.setOnClickListener {
            lifecycleScope.launch {
                val resultado = sincronizarAlarmasConReloj(this@Home)
                if (resultado > 0) {
                    Log.d("Home", "✅ Alarmas sincronizadas con el reloj")
                } else {
                    Log.e("Home", "❌ Falló la sincronización")
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            authViewModel.currentUser.collect { user ->
                if (user == null) {
                    val intent = Intent(this@Home, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        if (!isServiceRunning(WearDataService::class.java)) {
            WearDataService.startService(this@Home)
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
}
