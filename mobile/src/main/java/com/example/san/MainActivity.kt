package com.example.san

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val btnl: Button=findViewById(R.id.btnlogin)
        val btnr: Button=findViewById(R.id.btnregister)
        val btnc: Button=findViewById(R.id.btnchat)

        btnr.setOnClickListener {
            val intent= Intent(this, Register::class.java)
            startActivity(intent)
        }
        btnc.setOnClickListener {
            val intent= Intent(this, ChatBienvenida::class.java)
            startActivity(intent)
        }
        btnl.setOnClickListener {
            val intent= Intent(this, Login::class.java)
            startActivity(intent)
        }

    }

}
