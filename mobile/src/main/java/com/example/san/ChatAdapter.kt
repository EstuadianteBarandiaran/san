package com.example.san


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.san.model.ChatMessage
import android.view.View


class ChatAdapter(private val mensajes: MutableList<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMensaje: TextView = view.findViewById(R.id.txtMensajeUser)
    }

    inner class BotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMensaje: TextView = view.findViewById(R.id.txtMensajeBot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mensaje_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mensaje_bot, parent, false)
            BotViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mensaje = mensajes[position]
        if (holder is UserViewHolder) {
            holder.txtMensaje.text = mensaje.mensaje
        } else if (holder is BotViewHolder) {
            holder.txtMensaje.text = mensaje.mensaje
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mensajes[position].isUser) 1 else 0
    }

    override fun getItemCount() = mensajes.size

    fun agregarMensaje(mensaje: ChatMessage) {
        mensajes.add(mensaje)
        notifyItemInserted(mensajes.size - 1)
    }
    fun getUltimoMensajeUsuario(): String? {
        // Busca desde el final el último mensaje que sea del usuario
        return mensajes.asReversed().firstOrNull { it.isUser }?.mensaje
    }

}