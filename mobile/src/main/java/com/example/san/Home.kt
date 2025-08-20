package com.example.san

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.san.network.WearDataService
import com.example.san.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import android.widget.LinearLayout
import com.example.san.sync.sincronizarAlarmasConReloj
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView


class Home : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var recetasRecyclerView: RecyclerView
    private lateinit var recetasAdapter: RecetasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        //inicializar el RecyclerView
        recetasRecyclerView = findViewById(R.id.recetasRecyclerView)

        // Configurar el RecyclerView para las recetas
        setupRecetasRecyclerView()

        // Configurar los listeners del menú inferior
        setupBottomNavigation()

        // Iniciar servicio si no está corriendo
        if (!isServiceRunning(WearDataService::class.java)) {
            WearDataService.startService(this@Home)
        }

        // Observar cambios en el usuario
        lifecycleScope.launchWhenStarted {
            authViewModel.currentUser.collect { user ->
                if (user == null) {
                    navigateToLogin()
                }
            }
        }
    }

    private fun setupRecetasRecyclerView() {
        recetasRecyclerView = findViewById(R.id.recetasRecyclerView)
        recetasRecyclerView.layoutManager = LinearLayoutManager(this)

        // Obtener datos de ejemplo (en una app real, esto vendría de una base de datos o API)
        val listaRecetas = obtenerRecetasDeEjemplo()

        recetasAdapter = RecetasAdapter(listaRecetas)
        recetasRecyclerView.adapter = recetasAdapter
    }

    private fun obtenerRecetasDeEjemplo(): List<Receta> {
        return listOf(
            Receta(
                tipoComida = "Desayuno",
                titulo = "Avena con frutas y nueces",
                descripcion = "Avena integral con plátano, fresas y almendras. Rico en fibra y antioxidantes.",
                calorias = "350 kcal",
                imagenResId = R.drawable.breakfast_placeholder
            ),
            Receta(
                tipoComida = "Almuerzo",
                titulo = "Pollo al curry con arroz",
                descripcion = "Pechuga de pollo con salsa de curry light, acompañado de arroz integral y vegetales al vapor.",
                calorias = "450 kcal",
                imagenResId = R.drawable.lunch_placeholder
            ),
            Receta(
                tipoComida = "Cena",
                titulo = "Ensalada César light",
                descripcion = "Lechuga romana, pollo a la plancha, croutons integrales y aderezo light.",
                calorias = "320 kcal",
                imagenResId = R.drawable.dinner_placeholder
            ),
            Receta(
                tipoComida = "Snack",
                titulo = "Smoothie verde",
                descripcion = "Espinaca, piña, plátano y leche de almendras. Rico en vitaminas y minerales.",
                calorias = "200 kcal",
                imagenResId = R.drawable.snack_placeholder
            )
        )
    }

    private fun setupBottomNavigation() {
        // Botón de Inicio
        findViewById<LinearLayout>(R.id.homeButton).setOnClickListener {
            // Ya estamos en Home, no hacemos nada o podríamos recargar
            // recetasAdapter.updateData(obtenerRecetasDeEjemplo())
        }

        // Botón de Escáner Nutricional
        findViewById<LinearLayout>(R.id.planButton).setOnClickListener {
            val intent = Intent(this@Home, Nutricion::class.java)
            startActivity(intent)
        }

        // Botón de Chatbot
        findViewById<LinearLayout>(R.id.recipesButton).setOnClickListener {
            val intent = Intent(this@Home, ChatBienvenida::class.java)
            startActivity(intent)
        }

        // Botón de Perfil
        findViewById<LinearLayout>(R.id.profileButton).setOnClickListener {
            val intent = Intent(this@Home, Perfil::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this@Home, Home::class.java)
        startActivity(intent)
        finish()
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
}

// Data class para representar una receta
data class Receta(
    val tipoComida: String,
    val titulo: String,
    val descripcion: String,
    val calorias: String,
    val imagenResId: Int
)

// Adaptador para el RecyclerView
class RecetasAdapter(private val recetas: List<Receta>) :
    RecyclerView.Adapter<RecetasAdapter.RecetaViewHolder>() {

    class RecetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.breakfastImage)
        val tipoComida: TextView = itemView.findViewById(R.id.textTipoComida)
        val titulo: TextView = itemView.findViewById(R.id.breakfastTitle)
        val descripcion: TextView = itemView.findViewById(R.id.breakfastDescription)
        val calorias: TextView = itemView.findViewById(R.id.breakfastCalories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receta, parent, false)
        return RecetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        val receta = recetas[position]
        holder.imagen.setImageResource(receta.imagenResId)
        holder.tipoComida.text = receta.tipoComida
        holder.titulo.text = receta.titulo
        holder.descripcion.text = receta.descripcion
        holder.calorias.text = receta.calorias
    }

    override fun getItemCount() = recetas.size
}