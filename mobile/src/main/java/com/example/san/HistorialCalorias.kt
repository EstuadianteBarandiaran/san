package com.example.san

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.san.adapter.CaloriasAdapter
import com.example.san.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HistorialCalorias : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_calorias)

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

