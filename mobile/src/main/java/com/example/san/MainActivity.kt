package com.example.san

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.san.network.WearDataService

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Verificar y solicitar permisos antes de iniciar el servicio
        checkAndStartWearService()

        val btnLogin: Button = findViewById(R.id.btnlogin)
        val btnRegister: Button = findViewById(R.id.btnregister)
        val btnChat: Button = findViewById(R.id.btnchat)

        btnRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        btnChat.setOnClickListener {
            startActivity(Intent(this, ChatBienvenida::class.java))
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
    }

    private fun checkAndStartWearService() {
        val permissionsNeeded = arrayOf(
            android.Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE,
            android.Manifest.permission.BLUETOOTH_CONNECT
        )

        val granted = permissionsNeeded.all {
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }

        if (granted) {
            WearDataService.startService(this)
        } else {
            requestPermissions(permissionsNeeded, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            // Permisos otorgados, iniciar el servicio
            WearDataService.startService(this)
        }
    }
}
