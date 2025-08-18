import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.san.R
import com.example.san.Receta

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