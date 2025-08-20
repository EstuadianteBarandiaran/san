package com.example.san

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.san.dao.AlarmaProgramada

class AlarmaAdapter(
    private var alarmas: List<AlarmaProgramada>,
    private val onDeleteClick: (AlarmaProgramada) -> Unit
) : RecyclerView.Adapter<AlarmaAdapter.AlarmaViewHolder>() {

    class AlarmaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHora: TextView = itemView.findViewById(R.id.tvHora)
        val tvMensaje: TextView = itemView.findViewById(R.id.tvMensaje)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar) // ← CORRECTO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarma, parent, false)
        return AlarmaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmaViewHolder, position: Int) {
        val alarma = alarmas[position]
        holder.tvHora.text = alarma.hora
        holder.tvMensaje.text = alarma.mensaje

        holder.btnEliminar.setOnClickListener {
            onDeleteClick(alarma)
        }
    }

    override fun getItemCount(): Int = alarmas.size

    fun updateList(nuevasAlarmas: List<AlarmaProgramada>) {
        alarmas = nuevasAlarmas
        notifyDataSetChanged()
    }
}