package com.example.san.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.san.R

class CaloriasAdapter(private val items: List<Pair<String, Int>>) :
    RecyclerView.Adapter<CaloriasAdapter.CaloriasViewHolder>() {

    class CaloriasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFecha: TextView = itemView.findViewById(R.id.tvFechaItem)
        val tvCalorias: TextView = itemView.findViewById(R.id.tvCaloriasItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaloriasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calorias, parent, false)
        return CaloriasViewHolder(view)
    }

    override fun onBindViewHolder(holder: CaloriasViewHolder, position: Int) {
        val (fecha, calorias) = items[position]
        holder.tvFecha.text = fecha
        holder.tvCalorias.text = "$calorias kcal"
    }

    override fun getItemCount(): Int = items.size
}
